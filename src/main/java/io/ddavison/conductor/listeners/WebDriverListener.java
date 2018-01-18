package io.ddavison.conductor.listeners;

import io.ddavison.conductor.Locomotive;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class WebDriverListener implements IInvokedMethodListener {

    private Locomotive driver;

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod() && testResult.getInstance() instanceof Locomotive) {
            driver = (Locomotive) testResult.getInstance();
        }
    }

    /*
        If driver is not null decide whether or not to quit the driver.
        If the screenshot feature is not enabled, quit the driver,
        Else (it is enabled) only quit the driver if the test succeeded (if the test fails, the driver
        will be quit after the screenshot has been taken.)
     */
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            if (driver != null) {
                if (!driver.configuration.isScreenshotOnFail()) {
                    driver.quit();
                } else if (testResult.isSuccess()) {
                    driver.quit();
                }
            }
        }
    }
}
