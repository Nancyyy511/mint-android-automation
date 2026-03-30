package TestObject;

import PageObject.P09_ReviewOrderPage;
import PageObject.P14_OrderJournalingPage;
import api.utils.RetryUtils;
import io.qameta.allure.Step;
import org.testng.Assert;
import utils.AllureUtils;
import utils.ScreenshotUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class OrderJournalingFlow {
    public static final String CONFIDENT = "Confident";
    public static final String UNCERTAIN = "Uncertain";
    public static final String OVER_EXPECTED = "Over expected";
    public static final String UNDER_EXPECTED = "Under expected";
    public static final List<String> BUY_DECISION_FACTORS = List.of(
            "Other",
            "Fundamental analysis",
            "Technical analysis"
    );

    private static final int MAX_SCAN_PASSES = 3;

    private final P09_ReviewOrderPage orderHistoryPage = new P09_ReviewOrderPage();
    private final P14_OrderJournalingPage journalingPage = new P14_OrderJournalingPage();
    private final Random random = new Random();

    @Step("Submit journaling for the first order without journaling")
    public JournalingSubmission submitForFirstOrderWithoutJournaling(String note) {
        return RetryUtils.retry(() -> iterateOrdersAndSubmit(note), 2, 1000);
    }

    @Step("Submit journaling from current order details")
    public JournalingSubmission submitFromCurrentOrder(String note) {
        return RetryUtils.retry(() -> {
            try {
                OrderContext order = readCurrentOrderContext();
                return submitJournaling(order, note);
            } catch (RuntimeException | AssertionError exception) {
                attachDiagnostics("submit-current-order");
                journalingPage.resetToOrderDetails();
                throw exception;
            }
        }, 2, 1000);
    }

    public boolean isSubmittedSuccessfully() {
        return journalingPage.isSubmittedSuccessfully();
    }

    public void validateSavedJournalingFromUi(JournalingSubmission submission) {
        journalingPage.openViewJournaling();

        if ("BUY".equalsIgnoreCase(submission.getOrderType()) && submission.getConfidence() != null) {
            Assert.assertTrue(journalingPage.containsVisibleValue(submission.getConfidence()),
                    "Saved confidence should be visible in View Journaling. expected=" + submission.getConfidence()
                            + ", visible=" + journalingPage.readVisibleTexts());
            for (String factor : submission.getDecisionFactors()) {
                Assert.assertTrue(journalingPage.containsVisibleValue(factor),
                        "Saved decision factor should be visible in View Journaling. expected=" + factor
                                + ", visible=" + journalingPage.readVisibleTexts());
            }
        }

        if (submission.getExpectationOutcome() != null && !submission.getExpectationOutcome().isBlank()) {
            Assert.assertTrue(journalingPage.containsVisibleValue(submission.getExpectationOutcome()),
                    "Saved expectation/outcome should be visible in View Journaling. expected="
                            + submission.getExpectationOutcome() + ", visible=" + journalingPage.readVisibleTexts());
        }

        if (submission.getNote() != null && !submission.getNote().isBlank()) {
            Assert.assertTrue(journalingPage.readNote().contains(submission.getNote()),
                    "Saved note should be visible in View Journaling. expected fragment=" + submission.getNote()
                            + ", actual=" + journalingPage.readNote());
        }

        journalingPage.closeViewJournaling();
    }

    public void validateNoJournalingEntryPointForPendingOrder(String excludedOrderId) {
        openOrderHistory();
        Set<String> visitedOrders = new HashSet<>();

        for (int pass = 1; pass <= MAX_SCAN_PASSES; pass++) {
            List<P09_ReviewOrderPage.VisibleOrder> visibleOrders = getOrdersList();
            for (P09_ReviewOrderPage.VisibleOrder order : visibleOrders) {
                if (!visitedOrders.add(order.summary())) {
                    continue;
                }

                openOrderFromHistory(order);
                OrderContext currentOrder = readCurrentOrderContext();

                if (excludedOrderId != null && excludedOrderId.equals(currentOrder.orderId())) {
                    returnToOrderHistory();
                    continue;
                }

                if ("Pending".equalsIgnoreCase(currentOrder.orderStatus())
                        || "Expired".equalsIgnoreCase(currentOrder.orderStatus())) {
                    Assert.assertFalse(journalingPage.hasViewJournalingButton(),
                            "Pending/expired order should not show View Journaling. orderId="
                                    + currentOrder.orderId() + ", status=" + currentOrder.orderStatus());
                    Assert.assertFalse(journalingPage.hasAddJournalingButton(),
                            "Pending/expired order should not show Add Journaling CTA. orderId="
                                    + currentOrder.orderId() + ", status=" + currentOrder.orderStatus());
                    logAction(currentOrder, "SKIPPED");
                    returnToOrderHistory();
                    return;
                }

                returnToOrderHistory();
            }

            orderHistoryPage.scrollOrderHistory();
        }

        throw new RuntimeException("No pending/expired order was found to validate journaling absence");
    }

    private JournalingSubmission iterateOrdersAndSubmit(String note) {
        int discoveredOrders = 0;
        int skippedOrders = 0;
        int viewedOrders = 0;
        Set<String> visitedOrders = new HashSet<>();

        try {
            openOrderHistory();

            for (int pass = 1; pass <= MAX_SCAN_PASSES; pass++) {
                FlowLogger.step("JOURNALING_FLOW", "Scanning Order History. pass=" + pass);
                List<P09_ReviewOrderPage.VisibleOrder> visibleOrders = getOrdersList();
                if (visibleOrders.isEmpty()) {
                    FlowLogger.step("JOURNALING_FLOW", "No orders are visible in Order History on pass=" + pass);
                }

                boolean discoveredNewOrderThisPass = false;
                for (P09_ReviewOrderPage.VisibleOrder order : visibleOrders) {
                    if (!visitedOrders.add(order.summary())) {
                        continue;
                    }

                    discoveredNewOrderThisPass = true;
                    discoveredOrders++;
                    openOrderFromHistory(order);

                    OrderContext currentOrder = readCurrentOrderContext();
                    FlowLogger.step("JOURNALING_FLOW", "Inspecting order. orderId=" + currentOrder.orderId()
                            + ", orderType=" + currentOrder.orderType()
                            + ", orderStatus=" + currentOrder.orderStatus()
                            + ", summary=" + order.summary());

                    if (isPendingOrder(currentOrder)) {
                        logAction(currentOrder, "SKIPPED");
                        skippedOrders++;
                        returnToOrderHistory();
                        continue;
                    }

                    if (hasJournaling()) {
                        openViewJournaling(currentOrder);
                        viewedOrders++;
                        returnToOrderHistory();
                        continue;
                    }

                    if (!journalingPage.hasAddJournalingButton()) {
                        FlowLogger.step("JOURNALING_FLOW", "Skipping order because no journaling entry point is visible. orderId="
                                + currentOrder.orderId() + ", orderType=" + currentOrder.orderType()
                                + ", orderStatus=" + currentOrder.orderStatus()
                                + ", visible=" + journalingPage.readVisibleTexts());
                        logAction(currentOrder, "SKIPPED");
                        skippedOrders++;
                        returnToOrderHistory();
                        continue;
                    }

                    JournalingSubmission submission = submitJournaling(currentOrder, note);
                    logAction(currentOrder, "SUBMITTED");
                    return submission;
                }

                if (!discoveredNewOrderThisPass && pass == MAX_SCAN_PASSES) {
                    break;
                }

                FlowLogger.step("JOURNALING_FLOW", "No submittable order found in current viewport. Scrolling order history, pass=" + pass);
                orderHistoryPage.scrollOrderHistory();
            }
        } catch (RuntimeException | AssertionError exception) {
            attachDiagnostics("scan-and-submit");
            throw exception;
        }

        if (discoveredOrders == 0) {
            throw new RuntimeException("No orders were found in Order History to inspect for journaling");
        }

        if (viewedOrders > 0 && skippedOrders + viewedOrders == discoveredOrders) {
            throw new RuntimeException("All inspected orders were skipped or already had journaling. inspected="
                    + discoveredOrders + ", viewed=" + viewedOrders + ", skipped=" + skippedOrders);
        }

        throw new RuntimeException("No valid order was found for journaling submission after inspecting "
                + discoveredOrders + " orders across " + MAX_SCAN_PASSES + " scan passes");
    }

    private void openOrderHistory() {
        RetryUtils.retry(() -> {
            FlowLogger.step("JOURNALING_FLOW", "Opening Order History");
            orderHistoryPage.openHistoryFromCurrentScreen();
            return true;
        }, 2, 750);
    }

    private List<P09_ReviewOrderPage.VisibleOrder> getOrdersList() {
        return RetryUtils.retry(orderHistoryPage::getVisibleOrders, 2, 500);
    }

    private boolean isPendingOrder(OrderContext order) {
        return "Pending".equalsIgnoreCase(order.orderStatus());
    }

    private boolean hasJournaling() {
        return journalingPage.hasViewJournalingButton();
    }

    private void openViewJournaling(OrderContext order) {
        RetryUtils.retry(() -> {
            FlowLogger.step("JOURNALING_FLOW", "Opening View Journaling. orderId=" + order.orderId()
                    + ", orderType=" + order.orderType() + ", orderStatus=" + order.orderStatus());
            journalingPage.openViewJournaling();
            Assert.assertTrue(journalingPage.isComposerOpen(),
                    "View Journaling bottom sheet should be displayed. orderId=" + order.orderId());
            logAction(order, "VIEWED");
            journalingPage.closeViewJournaling();
            Assert.assertTrue(journalingPage.hasViewJournalingButton() || journalingPage.hasAddJournalingButton(),
                    "Order details should be visible again after closing View Journaling. orderId=" + order.orderId());
            return true;
        }, 2, 500);
    }

    private JournalingSubmission submitJournaling(OrderContext order, String note) {
        return RetryUtils.retry(() -> {
            try {
                FlowLogger.step("JOURNALING_FLOW", "Preparing journaling submission. orderId=" + order.orderId()
                        + ", orderType=" + order.orderType()
                        + ", orderStatus=" + order.orderStatus()
                        + ", note=" + note);

                Assert.assertTrue(journalingPage.hasAddJournalingButton(),
                        "Add Journaling CTA should be visible for orderId=" + order.orderId()
                                + ", orderType=" + order.orderType());

                journalingPage.openComposer();
                JournalingSubmission submission = buildSubmission(order.orderId(), order.orderType(), note);
                applyFlowByOrderType(submission);

                FlowLogger.step("JOURNALING_FLOW", "Submitting journaling. " + submission.toDebugString());
                journalingPage.submit();
                Assert.assertTrue(journalingPage.isSubmittedSuccessfully(),
                        "Journaling submission should close the bottom sheet and show View Journaling for orderId="
                                + order.orderId());
                FlowLogger.step("JOURNALING_FLOW", "Submission completed. " + submission.toDebugString());

                validateSavedJournalingFromUi(submission);
                return submission;
            } catch (RuntimeException | AssertionError exception) {
                attachDiagnostics("submit-current-order");
                journalingPage.resetToOrderDetails();
                throw exception;
            }
        }, 2, 1000);
    }

    private void returnToOrderHistory() {
        RetryUtils.retry(() -> {
            try {
                orderHistoryPage.goBackToHistoryFromDetails();
            } catch (RuntimeException firstFailure) {
                FlowLogger.step("JOURNALING_FLOW", "Primary return to Order History failed. Retrying with fallback navigation");
                openOrderHistory();
            }
            openOrderHistory();
            return true;
        }, 2, 750);
    }

    private void openOrderFromHistory(P09_ReviewOrderPage.VisibleOrder order) {
        RetryUtils.retry(() -> {
            FlowLogger.step("JOURNALING_FLOW", "Opening order from history. summary=" + order.summary());
            orderHistoryPage.openVisibleOrder(order);
            return true;
        }, 2, 500);
    }

    private OrderContext readCurrentOrderContext() {
        return new OrderContext(
                orderHistoryPage.extractOrderIdFromCurrentDetails(),
                orderHistoryPage.getCurrentOrderType(),
                orderHistoryPage.getCurrentOrderStatusText()
        );
    }

    private JournalingSubmission buildSubmission(String orderId, String orderType, String note) {
        JournalingSubmission submission = new JournalingSubmission();
        submission.setOrderId(orderId);
        submission.setOrderType(orderType);
        submission.setNote(note);
        return submission;
    }

    private void applyFlowByOrderType(JournalingSubmission submission) {
        String orderType = submission.getOrderType().toUpperCase(Locale.ROOT);
        if ("BUY".equals(orderType)) {
            applyBuyFlow(submission);
            return;
        }
        if ("SELL".equals(orderType)) {
            applySellFlow(submission);
            return;
        }

        throw new RuntimeException("Unsupported order type for journaling: " + submission.getOrderType());
    }

    private void applyBuyFlow(JournalingSubmission submission) {
        String confidence = pickOne(List.of(CONFIDENT, UNCERTAIN));
        String expectation = pickOne(List.of(OVER_EXPECTED, UNDER_EXPECTED));

        submission.setConfidence(confidence);
        FlowLogger.step("JOURNALING_FLOW", "BUY journaling selections. confidence=" + confidence
                + ", expectation=" + expectation);
        journalingPage.selectPositiveOutcome(confidence);

        String decisionFactor = journalingPage.selectFirstAvailableDecisionFactor(BUY_DECISION_FACTORS);
        if (decisionFactor == null) {
            throw new RuntimeException("No supported decision factor is visible on the journaling sheet. visible="
                    + journalingPage.readVisibleTexts());
        }

        submission.setDecisionFactors(List.of(decisionFactor));
        FlowLogger.step("JOURNALING_FLOW", "BUY journaling selected decisionFactor=" + decisionFactor);

        if (journalingPage.hasOption(expectation)) {
            submission.setExpectationOutcome(expectation);
            journalingPage.selectPositiveOutcome(expectation);
        } else {
            submission.setExpectationOutcome(null);
            FlowLogger.step("JOURNALING_FLOW", "Expectation/outcome step is not visible for BUY journaling. "
                    + "Skipping outcome selection. visible=" + journalingPage.readVisibleTexts());
        }

        journalingPage.enterNote(submission.getNote());
    }

    private void applySellFlow(JournalingSubmission submission) {
        String outcome = pickOne(List.of(OVER_EXPECTED, UNDER_EXPECTED));
        submission.setDecisionFactors(List.of());

        FlowLogger.step("JOURNALING_FLOW", "SELL journaling selection. outcome=" + outcome);
        if (journalingPage.hasOption(outcome)) {
            submission.setExpectationOutcome(outcome);
            journalingPage.selectPositiveOutcome(outcome);
        } else {
            submission.setExpectationOutcome(null);
            FlowLogger.step("JOURNALING_FLOW", "Outcome step is not visible for SELL journaling. "
                    + "Skipping outcome selection. visible=" + journalingPage.readVisibleTexts());
        }
        journalingPage.enterNote(submission.getNote());
    }

    private String pickOne(List<String> values) {
        return values.get(random.nextInt(values.size()));
    }

    private void logAction(OrderContext order, String action) {
        FlowLogger.step("JOURNALING_FLOW", "Order action=" + action
                + ", orderId=" + order.orderId()
                + ", orderType=" + order.orderType()
                + ", orderStatus=" + order.orderStatus());
    }

    private void attachDiagnostics(String context) {
        FlowLogger.step("JOURNALING_FLOW", "Capturing diagnostics for " + context);
        var screenshot = ScreenshotUtils.capture("journaling-" + context);
        AllureUtils.attachFile("journaling-" + context + "-screenshot", screenshot, "image/png");
    }

    private record OrderContext(String orderId, String orderType, String orderStatus) {
    }
}
