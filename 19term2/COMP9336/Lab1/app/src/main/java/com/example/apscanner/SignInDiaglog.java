package com.example.apscanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.AdapterView;

public class SignInDiaglog extends DialogFragment {
//    int position;
//    AdapterView<?> parent;

    public interface SignInDialogListener {
        void onDialogPositiveClick(DialogFragment dialogFragment, ScanResult scanResult);
        void onDialogNegativeClick(DialogFragment dialogFragment);
        ScanResult getScanResult();
    }

    SignInDialogListener listener;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // borrow from docs
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.wifidialog, null))
                // Add action buttons
                .setPositiveButton("SignIn", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        ScanResult s = listener.getScanResult();
                        listener.onDialogPositiveClick(SignInDiaglog.this, s);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SignInDiaglog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (SignInDialogListener) context;
    }
}
