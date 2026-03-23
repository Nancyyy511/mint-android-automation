package TestObject;

import PageObject.P02_LoginPage;
import PageObject.P10_TopUpPage;

import java.nio.file.Path;

public class TopUpFlow {

    public enum TopUpState {
        SUCCESS,
        FAILED,
        UNKNOWN
    }

    private static final String DEFAULT_USERNAME = "01282349004";
    private static final String DEFAULT_PASSWORD = "@Testing08";
    private static final String DEFAULT_RECEIPT_PATH = "C:\\Users\\nawny\\Downloads\\receipt (1).png";
    private static final String DEFAULT_DEPOSIT_DATE = "24/11/2024";
    private static final String DEFAULT_DEPOSIT_TIME = "11:55 am";
    private static final String DEFAULT_AMOUNT = "100";
    private static final String DEFAULT_INSTAPAY_ADDRESS = "@ramzy";

    private TopUpState state = TopUpState.UNKNOWN;
    private boolean returnedHome;

    public void completeTopUpFlow() {
        logStart("Top Up flow");

        try {
            new StartupFlow().completeStartupToLogin();

            P02_LoginPage loginPage = new P02_LoginPage();
            if (!loginPage.isHomeDisplayed()) {
                logStart("Login phase");
                loginPage.login(
                        System.getProperty("topup.username", DEFAULT_USERNAME),
                        System.getProperty("topup.password", DEFAULT_PASSWORD)
                );
                loginPage.handleSecurityQuestion();
                loginPage.enterPinZeroFourTimes();
                loginPage.waitForHomeScreen();
                logSuccess("Login phase");
            } else {
                System.out.println("[TOPUP][INFO] User already logged in");
            }

            completeTopUpFromCurrentSession();

            logSuccess("Top Up flow");
        } catch (Exception exception) {
            state = TopUpState.FAILED;
            returnedHome = false;
            captureFailureArtifacts("T12_TopUpTest");
            System.out.println("[TOPUP][FAILURE] " + exception.getMessage());
            throw exception;
        }
    }

    public void completeTopUpFromCurrentSession() {
        logStart("Top Up flow");
        try {
            P10_TopUpPage topUpPage = new P10_TopUpPage();
            topUpPage.openWallet();
            topUpPage.clickTopUp();
            topUpPage.selectInstapay();
            topUpPage.confirmExternalPayment();
            topUpPage.uploadReceipt(System.getProperty("topup.receiptPath", DEFAULT_RECEIPT_PATH));
            topUpPage.enterDepositDetails(
                    System.getProperty("topup.depositDate", DEFAULT_DEPOSIT_DATE),
                    System.getProperty("topup.depositTime", DEFAULT_DEPOSIT_TIME),
                    System.getProperty("topup.amount", DEFAULT_AMOUNT),
                    System.getProperty("topup.instapayAddress", DEFAULT_INSTAPAY_ADDRESS)
            );
            topUpPage.submitTopUp();
            topUpPage.verifySuccessScreen();
            state = topUpPage.isSuccessScreenDisplayed() ? TopUpState.SUCCESS : TopUpState.UNKNOWN;
            topUpPage.returnToHome();
            returnedHome = topUpPage.isReturnedHome();

            if (state != TopUpState.SUCCESS || !returnedHome) {
                throw new RuntimeException("Top Up flow did not finish on the expected final screens");
            }
            logSuccess("Top Up flow");
        } catch (Exception exception) {
            state = TopUpState.FAILED;
            returnedHome = false;
            captureFailureArtifacts("T12_TopUpTest");
            System.out.println("[TOPUP][FAILURE] " + exception.getMessage());
            throw exception;
        }
    }

    public TopUpState getState() {
        return state;
    }

    public boolean isSuccessScreenDisplayed() {
        return state == TopUpState.SUCCESS;
    }

    public boolean isReturnedHome() {
        return returnedHome;
    }

    private void captureFailureArtifacts(String testName) {
        try {
            Path screenshot = ScreenshotUtils.capture(testName);
            if (screenshot != null) {
                System.out.println("[TOPUP][INFO] Screenshot captured: " + screenshot);
            }
        } catch (Exception exception) {
            System.out.println("[TOPUP][INFO] Screenshot capture failed");
        }
    }

    private void logStart(String step) {
        System.out.println("[TOPUP][START] " + step);
    }

    private void logSuccess(String step) {
        System.out.println("[TOPUP][SUCCESS] " + step);
    }
}
