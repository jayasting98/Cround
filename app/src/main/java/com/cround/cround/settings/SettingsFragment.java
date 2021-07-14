package com.cround.cround.settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.widget.Toast;

import com.cround.cround.R;
import com.cround.cround.settings.DeleteAccountSettingDialogFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends PreferenceFragmentCompat {
    private DeleteAccountSettingDialogFragment
            .DeleteAccountSettingDialogListener deleteAccountSettingDialogListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deleteAccountSettingDialogListener = new DeleteAccountSettingDialogFragment
                .DeleteAccountSettingDialogListener() {
            @Override
            public void onDeleteAccountSettingDialogDeleteClick(
                    DialogFragment dialog, String password) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (password == null || password.equals("")) {
                    onReauthenticationFailure();
                    return;
                }

                AuthCredential credential = EmailAuthProvider
                        .getCredential(user.getEmail(), password);

                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getContext(),
                                                                "Account deleted.",
                                                                Toast.LENGTH_SHORT).show();
                                                        signOut();
                                                    }
                                                }
                                            });
                                } else {
                                    onReauthenticationFailure();
                                }
                            }
                        });
            }

            @Override
            public void onDeleteAccountSettingDialogCancelClick(
                    DialogFragment dialog) {
                dialog.getDialog().cancel();
            }
        };
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference deleteAccountPreference = getPreferenceManager().findPreference("delete_account");
        deleteAccountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DeleteAccountSettingDialogFragment deleteAccountSettingDialogFragment =
                        new DeleteAccountSettingDialogFragment(deleteAccountSettingDialogListener);
                deleteAccountSettingDialogFragment.show(getParentFragmentManager(),
                        "Delete Account Setting Dialog Fragment");

                return false;
            }
        });
    }

    private void onReauthenticationFailure() {
        Toast.makeText(getContext(), "Invalid password.",
                Toast.LENGTH_SHORT).show();

        DeleteAccountSettingDialogFragment deleteAccountSettingDialogFragment =
                new DeleteAccountSettingDialogFragment(deleteAccountSettingDialogListener);
        deleteAccountSettingDialogFragment.show(getParentFragmentManager(),
                "Delete Account Setting Dialog Fragment Retry");
    }

    private void signOut() {
        // sign out using Firebase
        AuthUI.getInstance().signOut(getContext());
    }
}