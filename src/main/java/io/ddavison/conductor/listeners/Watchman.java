package io.ddavison.conductor.listeners;

import io.ddavison.conductor.Locomotive;
import io.ddavison.conductor.util.ScreenShotUtil;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public abstract class Watchman extends TestWatcher {

    public abstract Locomotive getLocomotive();
    public abstract void quit();

    private boolean failure;
    private Throwable e;

    @Override
    protected void failed(Throwable e, Description description) {
        if (getLocomotive().configuration.isScreenshotOnFail()) {
            failure = true;
            this.e = e;
        }
    }

    /**
     * Take screenshot if the test failed.
     */
    @Override
    protected void finished(Description description) {
        super.finished(description);
        if (getLocomotive().configuration.isScreenshotOnFail()) {
            if (failure) {
                ScreenShotUtil.take(getLocomotive(),
                        description.getDisplayName(),
                        e.getMessage());
            }
        }
        quit();
    }
}
