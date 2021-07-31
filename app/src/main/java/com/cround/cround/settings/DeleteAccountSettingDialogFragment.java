package com.cround.cround.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.cround.cround.R;

public class DeleteAccountSettingDialogFragment extends DialogFragment {
    private DeleteAccountSettingDialogListener listener;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface DeleteAccountSettingDialogListener {
        public void onDeleteAccountSettingDialogDeleteClick(DialogFragment dialog, String password);
        public void onDeleteAccountSettingDialogCancelClick(DialogFragment dialog);
    }

    public DeleteAccountSettingDialogFragment(DeleteAccountSettingDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View view = inflater
                .inflate(R.layout.fragment_dialog_setting_delete_account, null);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete_account_setting_dialog_title)
                .setView(view)
                .setMessage(R.string.delete_account_setting_dialog_message)
                .setPositiveButton(R.string.delete_account_setting_dialog_affirmative,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                EditText passwordEditText = view.findViewById(R.id.fragment_dialog_setting_account_delete_password_edittext);
                                String password = passwordEditText.getText().toString();
                                listener.onDeleteAccountSettingDialogDeleteClick(
                                        DeleteAccountSettingDialogFragment.this, password);
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDeleteAccountSettingDialogCancelClick(
                                DeleteAccountSettingDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}