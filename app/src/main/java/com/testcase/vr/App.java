package com.testcase.vr;

import android.app.Application;
import android.content.Context;



/**
 * Created by AlexShredder on 28.07.2016.
 */
public class App extends Application {
    private static Context context;
    private static String defaultDecodedFilePath;

    public App() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        defaultDecodedFilePath = getFilesDir()+"/decrypted.tmp";
    }

    public static String getDefaultDecodedFilePath() {
        return defaultDecodedFilePath;
    }

    public static Context getContext() {
        return context;
    }
}
