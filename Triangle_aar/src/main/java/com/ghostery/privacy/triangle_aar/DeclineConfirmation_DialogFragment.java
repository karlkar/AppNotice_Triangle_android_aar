package com.ghostery.privacy.triangle_aar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DeclineConfirmation_DialogFragment extends DialogFragment
{
    public DeclineConfirmation_DialogFragment()
    {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.declineConfirmDialog_title));
        builder.setMessage(getActivity().getString(R.string.declineConfirmDialog_message));
        builder.setPositiveButton(getActivity().getString(R.string.declineConfirmDialog_posBtn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        // Close the app
        System.exit(0);
    }

}