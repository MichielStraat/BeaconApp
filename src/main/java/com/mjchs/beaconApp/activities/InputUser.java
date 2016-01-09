package com.mjchs.beaconApp.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mjchs.beaconApp.R;

/**
 * Created by mjchs on 9-1-2016.
 */
public class InputUser extends DialogFragment
{
    private NoticeDialogListener mListener;

    public interface NoticeDialogListener {
        public void onInput(String text);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try
        {

            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_user, null);

        final AlertDialog d = builder.setView(dialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText input = (EditText) dialogView.findViewById(R.id.input);
                        mListener.onInput(input.getText().toString());
                    }
                }).create();


        return d;
    }
}
