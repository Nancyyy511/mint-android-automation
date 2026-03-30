package TestObject;

import PageObject.P09_ReviewOrderPage;
import PageObject.P14_OrderJournalingPage;
import api.utils.RetryUtils;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderInspectionService {
    private static final String FLOW = "HEALTH_CHECK";
    private static final String STEP = "STEP";
    private static final String NAVIGATION = "NAVIGATION";

    private final P09_ReviewOrderPage orderHistoryPage = new P09_ReviewOrderPage();
    private final P14_OrderJournalingPage journalingPage = new P14_OrderJournalingPage();
    private final OrderJournalingFlow orderJournalingFlow = new OrderJournalingFlow();
    private final HealthCheckService healthCheckService;
    private final boolean enableRecovery;

    public OrderInspectionService(HealthCheckService healthCheckService, boolean enableRecovery) {
        this.healthCheckService = healthCheckService;
        this.enableRecovery = enableRecovery;
    }

    public int inspectOrders(int minimumOrdersToValidate, int maximumOrdersToScan, int maxPasses) {
        Set<String> visitedSummaries = new HashSet<>();
        Set<String> visitedOrderIds = new HashSet<>();
        int validatedOrders = 0;
        int scannedOrders = 0;

        for (int pass = 1; pass <= maxPasses && validatedOrders < minimumOrdersToValidate && scannedOrders < maximumOrdersToScan; pass++) {
            healthCheckService.log("INFO", NAVIGATION, "history-open", null, "Opening Order History. pass=" + pass);
            NavigationResult navigation = NavigationUtils.safeNavigateToHistory(orderHistoryPage, "pass=" + pass, enableRecovery);
            if (!navigation.succeeded()) {
                healthCheckService.log("ERROR", NAVIGATION, "history-open-failed", null, navigation.details());
                healthCheckService.attemptSoftRecovery("history-open-pass-" + pass);
                continue;
            }

            healthCheckService.validateAppStability("while opening order history pass=" + pass, null);
            List<P09_ReviewOrderPage.VisibleOrder> visibleOrders = RetryUtils.retry(orderHistoryPage::getVisibleOrders, 2, 500);
            if (visibleOrders.isEmpty()) {
                healthCheckService.log("WARN", NAVIGATION, "history-empty", null, "No visible orders on pass=" + pass);
                continue;
            }

            for (P09_ReviewOrderPage.VisibleOrder order : visibleOrders) {
                if (scannedOrders >= maximumOrdersToScan || validatedOrders >= minimumOrdersToValidate) {
                    break;
                }
                if (!visitedSummaries.add(order.summary())) {
                    continue;
                }

                scannedOrders++;
                OrderInspectionResult result = inspectSingleOrder(order, scannedOrders, visitedOrderIds);
                if (result.validated()) {
                    validatedOrders++;
                }
            }

            if (validatedOrders < minimumOrdersToValidate && scannedOrders < maximumOrdersToScan) {
                healthCheckService.log("INFO", NAVIGATION, "history-scroll", null,
                        "Validated=" + validatedOrders + ", scanned=" + scannedOrders + ". Scrolling for more orders.");
                orderHistoryPage.scrollOrderHistory();
                healthCheckService.validateAppStability("after scrolling order history pass=" + pass, null);
            }
        }

        return validatedOrders;
    }

    private OrderInspectionResult inspectSingleOrder(P09_ReviewOrderPage.VisibleOrder order,
                                                     int sequence,
                                                     Set<String> visitedOrderIds) {
        healthCheckService.log("INFO", STEP, "inspect-order", null,
                "Inspecting order #" + sequence + " -> " + order.summary());

        if (!tryOpenOrder(order)) {
            handleNonCriticalFailure("order-open-failed", null, "Details screen did not open. summary=" + order.summary());
            return new OrderInspectionResult(false, "FAILED_OPEN");
        }

        String orderId = safeReadOrderId();
        if (orderId == null) {
            handleNonCriticalFailure("missing-order-id", null, "Order id could not be extracted. summary=" + order.summary());
            return new OrderInspectionResult(false, "FAILED_ID");
        }

        if (!visitedOrderIds.add(orderId)) {
            healthCheckService.log("INFO", FLOW, "order-skipped", orderId, "Order already inspected. summary=" + order.summary());
            navigateBackToHistory(orderId);
            return new OrderInspectionResult(false, "DUPLICATE");
        }

        healthCheckService.validateAppStability("after opening order", orderId);
        String orderStatus = safeReadOrderStatus(orderId);
        String orderType = safeReadOrderType(orderId);

        if (orderStatus == null || orderType == null) {
            handleNonCriticalFailure("missing-order-metadata", orderId,
                    "Order status/type could not be resolved. status=" + orderStatus + ", type=" + orderType);
            return new OrderInspectionResult(false, "FAILED_METADATA");
        }

        healthCheckService.log("INFO", FLOW, "order-opened", orderId,
                "Order lifecycle=OPENED, summary=" + order.summary() + ", status=" + orderStatus + ", type=" + orderType);

        if ("PENDING".equalsIgnoreCase(orderStatus)) {
            healthCheckService.log("WARN", FLOW, "order-skipped", orderId, "Order lifecycle=SKIPPED, reason=PENDING");
            navigateBackToHistory(orderId);
            return new OrderInspectionResult(false, "SKIPPED_PENDING");
        }

        int validated = handleJournalingForOrder(orderId);
        healthCheckService.log("INFO", FLOW, validated == 1 ? "order-validated" : "order-failed", orderId,
                "Order lifecycle=" + (validated == 1 ? "VALIDATED" : "FAILED"));
        return new OrderInspectionResult(validated == 1, validated == 1 ? "VALIDATED" : "FAILED_JOURNALING");
    }

    private int handleJournalingForOrder(String orderId) {
        if (journalingPage.hasAddJournalingButton()) {
            String note = "Build health check note for order " + orderId;
            healthCheckService.log("INFO", STEP, "journaling-handle", orderId, "Handling journaling mode=ADD");
            JournalingSubmission submission = orderJournalingFlow.submitFromCurrentOrder(note);
            if (submission == null || !orderJournalingFlow.isSubmittedSuccessfully()) {
                healthCheckService.log("ERROR", FLOW, "journaling-submit-failed", orderId, "Journaling submission failed");
                return 0;
            }
            healthCheckService.validateAppStability("after journaling submission", orderId);
            navigateBackToHistory(orderId);
            return 1;
        }

        if (journalingPage.hasViewJournalingButton()) {
            healthCheckService.log("INFO", STEP, "journaling-handle", orderId, "Handling journaling mode=VIEW");
            RetryUtils.retry(() -> {
                journalingPage.openViewJournaling();
                return true;
            }, 2, 500);
            if (!journalingPage.isComposerOpen()) {
                healthCheckService.log("ERROR", FLOW, "journaling-view-failed", orderId, "View Journaling bottom sheet did not appear");
                return 0;
            }
            journalingPage.closeViewJournaling();
            healthCheckService.validateAppStability("after viewing journaling", orderId);
            navigateBackToHistory(orderId);
            return 1;
        }

        healthCheckService.validateAppStability("before journaling fail", orderId, Duration.ofSeconds(2));
        healthCheckService.log("ERROR", FLOW, "journaling-missing", orderId,
                "No journaling entry point was available for non-pending order");
        return 0;
    }

    private boolean tryOpenOrder(P09_ReviewOrderPage.VisibleOrder order) {
        try {
            RetryUtils.retry(() -> {
                orderHistoryPage.openVisibleOrder(order);
                return true;
            }, 2, 500);
            return true;
        } catch (RuntimeException exception) {
            healthCheckService.log("WARN", NAVIGATION, "order-open-failed", null,
                    "summary=" + order.summary() + ", reason=" + summarizeException(exception));
            return false;
        }
    }

    private void navigateBackToHistory(String orderId) {
        RetryUtils.retry(() -> {
            orderHistoryPage.goBackToHistoryFromDetails();
            return true;
        }, 2, 750);
        healthCheckService.validateAppStability("after navigating back to history", orderId);
    }

    private String safeReadOrderId() {
        try {
            return RetryUtils.until(
                    "order id from order details",
                    orderHistoryPage::extractOrderIdFromCurrentDetails,
                    value -> value != null && !value.isBlank(),
                    Duration.ofSeconds(8),
                    Duration.ofMillis(400)
            );
        } catch (AssertionError exception) {
            return null;
        }
    }

    private String safeReadOrderStatus(String orderId) {
        try {
            return RetryUtils.until(
                    "order status for " + orderId,
                    orderHistoryPage::getCurrentOrderStatusText,
                    value -> value != null && !value.isBlank() && !"Unknown".equalsIgnoreCase(value),
                    Duration.ofSeconds(6),
                    Duration.ofMillis(400)
            );
        } catch (AssertionError exception) {
            return null;
        }
    }

    private String safeReadOrderType(String orderId) {
        try {
            return RetryUtils.until(
                    "order type for " + orderId,
                    orderHistoryPage::getCurrentOrderType,
                    value -> value != null && !value.isBlank(),
                    Duration.ofSeconds(6),
                    Duration.ofMillis(400)
            );
        } catch (AssertionError exception) {
            return null;
        }
    }

    private void handleNonCriticalFailure(String step, String orderId, String message) {
        healthCheckService.log("WARN", FLOW, step, orderId, message);
        if (enableRecovery) {
            healthCheckService.attemptSoftRecovery(step);
        }
    }

    private String summarizeException(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.replaceAll("\\s+", " ").trim();
    }

    private record OrderInspectionResult(boolean validated, String status) {
    }
}
