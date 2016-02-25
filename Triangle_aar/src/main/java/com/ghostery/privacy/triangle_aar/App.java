package com.ghostery.privacy.triangle_aar;

import android.app.Application;
import android.content.Context;

/**
 * Created by Steven.Overson on 2/25/2016.
 */
public class App extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext()
    {
        return context;
    }
}

