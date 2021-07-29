package com.cround.cround.settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.util.Log;
import android.widget.Toast;

import com.cround.cround.MainActivity;
import com.cround.cround.R;
import com.cround.cround.api.UnsuccessfulResponse;
import com.cround.cround.settings.DeleteAccountSettingDialogFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends PreferenceFragmentCompat {
    private MainActivity mainActivity;
    private FirebaseAuth firebaseAuth;
    private DeleteAccountSettingDialogFragment
            .DeleteAccountSettingDialogListener deleteAccountSettingDialogListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        firebaseAuth = mainActivity.getFirebaseAuth();

        deleteAccountSettingDialogListener = new DeleteAccountSettingDialogFragment
                .DeleteAccountSettingDialogListener() {
            @Override
            public void onDeleteAccountSettingDialogDeleteClick(
                    DialogFragment dialog, String password) {
                final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                if (password == null || password.equals("")) {
                    onReauthenticationFailure();
                    return;
                }

                AuthCredential credential = EmailAuthProvider
                        .getCredential(firebaseUser.getEmail(), password);

                firebaseUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            deleteAccount();
                        } else {
                            onReauthenticationFailure();
                        }
                    }
                });
            }

            @Override
            public void onDeleteAccountSettingDialogCancelClick(DialogFragment dialog) {
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

    private void deleteAccount() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        mainActivity.requestWithAuthorisation(new Consumer<Map<String, String>>() {
            @Override
            public void accept(Map<String, String> headerMap) {
                mainActivity.getCroundApi().deleteUser(firebaseUser.getUid(), headerMap).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(mainActivity, "Account deleted.", Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(SettingsFragment.this)
                                    .navigate(R.id.action_nav_fragment_settings_to_nav_fragment_signin);
                        } else {
                            int errno = new GsonBuilder()
                                    .create().fromJson(new BufferedReader(response.errorBody()
                                            .charStream()), UnsuccessfulResponse.class)
                                    .getResponseError()
                                    .getErrno();
                            switch (errno) {
                                case 1:
                                    Log.e("deleteAccount", "User cannot delete other users' accounts.");
                                    break;
                                case 2:
                                    Log.e("deleteAccount", "Reauthentication required.");
                                    break;
                                case 0:
                                default:
                                    Log.e("deleteAccount", "Unknown server-side error.");
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("deleteAccount", "Unknown error.");
                        StringBuilder sb = new StringBuilder();
                        sb.append(t.toString());
                        for (StackTraceElement ste : t.getStackTrace()) {
                            sb.append(ste.toString()).append("\n");
                        }
                        Log.e("deleteAccount", sb.toString());
                    }
                });
            }
        });
    }
}