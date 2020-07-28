package com.example.testscroll;

import android.app.Application;
import android.content.Context;

/**
 * @author foxcoder
 * @since 2020-07-27
 */
public class TestScrollApplication extends Application {

    private static TestScrollApplication mApp;

    public static TestScrollApplication getApplication() {
        return mApp;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mApp = this;
    }
}
