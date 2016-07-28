package com.testcase.vr.utils.crypters;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.testcase.vr.utils.executorWithFeedback.ExecutorWithFeedback;

/**
 * Created by AlexShredder on 28.07.2016.
 */
public class Paranoid {

    private Context context;

    private Paranoid(Context context) {
        this.context = context;
    }

    public static Paranoid with(Context context){
        return new Paranoid(context);
    }

    public void encodeAndSaveFile(final File file, final OnProgressListener onProgressListener) {

        ExecutorWithFeedback.MyJob job = new ExecutorWithFeedback.MyJob() {
            @Override
            protected Bundle doJob() {

                String errorText = "";
                try {

                    ContextWrapper cw = new ContextWrapper(context);

                    File encryptedFile = new File(file.getParentFile().getPath(),file.getName()+"_encrypted");

                    Crypto crypto = new Crypto(new SharedPrefsBackedKeyChain(context),
                            new SystemNativeCryptoLibrary());

                    if (!crypto.isAvailable()) {
                        return null;
                    }

                    BufferedOutputStream fileStream = new BufferedOutputStream(new FileOutputStream(encryptedFile));
                    OutputStream outputStream = crypto.getCipherOutputStream(fileStream, new Entity("pam param pam pam"));

                    int read;
                    byte[] buffer = new byte[1024];
                    long fileSize = file.length();
                    long gotBites = 0L;
                    final int[] percent = new int[1];
                    percent[0]=0;

                    BufferedInputStream  bis = new BufferedInputStream(new FileInputStream(file));
                    while ((read = bis.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                        gotBites+=buffer.length;
                        int tempPercent = (int) (gotBites*100/fileSize);
                        if (tempPercent>percent[0]) {
                            percent[0]=tempPercent;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    onProgressListener.OnProgress(Math.min(percent[0],100), false);
                                }
                            });
                        }
                    }
                    outputStream.close();
                    bis.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    errorText = e.getLocalizedMessage();
                }

                Bundle bundle = new Bundle();
                if (!errorText.isEmpty()) bundle.putString("Error",errorText);
                return bundle;
            }
        };

        job.setOnCompleteListener(new ExecutorWithFeedback.OnCompleteListener() {
            @Override
            public void onComplete(Bundle result) {
                if (onProgressListener!=null) {
                    if (result.containsKey("Error"))
                        onProgressListener.OnError(result.getString("Error"));
                    else
                        onProgressListener.OnComplete(result);
                }
            }
        });

        job.execute();

    }

    public void decodeFile(final File file, final String decodeTo, final OnProgressListener onProgressListener) {

        ExecutorWithFeedback.MyJob job = new ExecutorWithFeedback.MyJob() {
            @Override
            protected Bundle doJob() {

                String errorText = "";
                File decryptedFile = null;
                try {

                    ContextWrapper cw = new ContextWrapper(context);

                    decryptedFile = new File(decodeTo);

                    Crypto crypto = new Crypto(new SharedPrefsBackedKeyChain(context),
                            new SystemNativeCryptoLibrary());

                    if (!crypto.isAvailable()) {
                        return null;
                    }

                    BufferedInputStream fileStream = new BufferedInputStream((new FileInputStream(file)));

                    int read;
                    byte[] buffer = new byte[1024];
                    long fileSize = file.length();
                    long gotBites = 0L;
                    final int[] percent = new int[1];
                    percent[0]=0;

                    InputStream inputStream = crypto.getCipherInputStream(fileStream, new Entity("pam param pam pam"));
                    OutputStream outputStream = new FileOutputStream(decryptedFile);
                    BufferedInputStream bis = new BufferedInputStream(inputStream);
                    while ((read = bis.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                        gotBites+=buffer.length;
                        int tempPercent = (int) (gotBites*100/fileSize);
                        if (tempPercent>percent[0]) {
                            percent[0]=tempPercent;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    onProgressListener.OnProgress(Math.min(percent[0],100), false);
                                }
                            });
                        }
                    }
                    bis.close();
                    inputStream.close();
                    outputStream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    errorText = e.getLocalizedMessage();
                }

                Bundle bundle = new Bundle();
                if (decryptedFile!=null && decryptedFile.exists()) bundle.putString("address",decryptedFile.getPath());
                if (!errorText.isEmpty()) bundle.putString("Error",errorText);
                return bundle;
            }
        };

        job.setOnCompleteListener(new ExecutorWithFeedback.OnCompleteListener() {
            @Override
            public void onComplete(Bundle result) {
                if (onProgressListener!=null) {
                    if (result.containsKey("Error"))
                        onProgressListener.OnError(result.getString("Error"));
                    else
                        onProgressListener.OnComplete(result);
                }
            }
        });

        job.execute();

    }


    public interface OnProgressListener{
        void OnProgress(int percents, boolean done);
        void OnError(String errorText);
        void OnComplete(Object result);
    }

}
