package com.testcase.vr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.testcase.vr.utils.crypters.Paranoid;

import java.io.File;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements Paranoid.OnProgressListener {

    private ProgressDialog progressDialog;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Button buttonEncrypt = (Button) rootView.findViewById(R.id.button_encrypt);
        Button buttonDecrypt = (Button) rootView.findViewById(R.id.button_decrypt_and_play);
        Button buttonPlayLast = (Button) rootView.findViewById(R.id.button_play_last);

        buttonEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFileAndEncrypt();
            }
        });

        buttonDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decryptAndPlay();
            }
        });

        buttonPlayLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(App.getDefaultDecodedFilePath());
                if (!file.exists())
                    showMessage("Error!","We have not it yet!");
                else
                    playVideo(App.getDefaultDecodedFilePath());
            }
        });

        return rootView;
    }

    private void decryptAndPlay() {
        FilePickerDialog dialog = getFilePickerDialog();
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files.length==0) return;
                showProgressDialog("Decoding process", String.format("Decoding file %s",files[0]));  //yes yes hardcoded strings, i know
                Paranoid.with(App.getContext()).decodeFile(new File(files[0]), App.getDefaultDecodedFilePath(), MainActivityFragment.this);
            }
        });

        dialog.show();
    }

    private void chooseFileAndEncrypt(){
        FilePickerDialog dialog = getFilePickerDialog();
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files.length==0) return;
                showProgressDialog("Encoding process", String.format("Encoding file %s",files[0]));  //yes yes hardcoded strings, i know
                Paranoid.with(App.getContext()).encodeAndSaveFile(new File(files[0]),MainActivityFragment.this);
            }
        });

        dialog.show();
    }

    private FilePickerDialog getFilePickerDialog(){
        DialogProperties properties=new DialogProperties();
        properties.selection_mode=DialogConfigs.SINGLE_MODE;
        properties.selection_type=DialogConfigs.FILE_SELECT;
        properties.root=new File(DialogConfigs.STORAGE_DIR);
        properties.extensions=null;

        return new FilePickerDialog(getContext(),properties);
    }

    @Override
    public void OnProgress(int percents, boolean done) {
        if (!done)
            updateProgressDialog(percents);
        else {
         }
    }

    private void updateProgressDialog(int percents) {
        if (progressDialog==null || !progressDialog.isShowing()) return;
        if (progressDialog.isIndeterminate()) progressDialog.setIndeterminate(false);
        progressDialog.setProgress(percents);

    }

    @Override
    public void OnError(String errorText) {
        dismissProgressDialog();
        showMessage("Error!", errorText);
    }

    @Override
    public void OnComplete(Object result) {
        dismissProgressDialog();

        if (result instanceof Bundle && ((Bundle) result).containsKey("address")) {
            playVideo(((Bundle) result).getString("address"));
        }
        else {
            showMessage("DONE!", "Successfully completed!");
        }
    }

    private void playVideo(String address) {
        Intent intent = new Intent((MainActivity)getHost(),VRActivity.class);
        intent.putExtra("address",address);
        startActivity(intent);
    }

    private void showMessage(String title, String message) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
        adb.setTitle(title);
        adb.setMessage(message);
        adb.setIcon(android.R.drawable.ic_dialog_info);
        adb.setNeutralButton("i see...",null);
        adb.create();
        adb.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog==null || !progressDialog.isShowing()) return;
        progressDialog.cancel();
    }


    private void showProgressDialog(String title, String message) {
        if (progressDialog==null) progressDialog = new ProgressDialog(getContext());
        if (progressDialog.isShowing()) progressDialog.cancel();

        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setIndeterminate(true);

        progressDialog.show();
    }
}
