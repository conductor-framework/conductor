package io.ddavison.conductor.listeners;

import io.ddavison.conductor.Locomotive;
import io.ddavison.conductor.util.ScreenShotUtil;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    private Locomotive driver;

    @Override
    public void onTestStart(ITestResult iTestResult) {
        if (iTestResult.getInstance() instanceof Locomotive) {
            driver = (Locomotive) iTestResult.getInstance();
        }
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {

    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (driver.configuration != null && driver.configuration.isScreenshotOnFail()) {
            ScreenShotUtil.take(driver,
                    result.getTestClass().getName() + "." + result.getMethod().getMethodName(),
                    result.getThrowable().getMessage());
            driver.quit();
        }
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        if (driver != null) {
            driver.quit();
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
        if (driver != null) {
            driver.quit();
        }
    }

    @Override
    public void onStart(ITestContext iTestContext) {

    }

    @Override
    public void onFinish(ITestContext iTestContext) {
        if (driver != null) {
            driver.quit();
        }
    }
}
