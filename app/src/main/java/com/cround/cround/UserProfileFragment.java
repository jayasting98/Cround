package com.cround.cround;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cround.cround.api.SuccessfulResponse;
import com.cround.cround.api.UnsuccessfulResponse;
import com.cround.cround.api.UserDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileFragment extends Fragment {

    MainActivity mainActivity;
    TextView displayNameTextView;
    TextView usernameTextView;
    TextView descriptionTextView;
    UserDetails userDetails;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    public static UserProfileFragment newInstance() {
        UserProfileFragment fragment = new UserProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        mainActivity = (MainActivity) getActivity();
        displayNameTextView = view.findViewById(R.id.fragment_user_profile_displayName);
        usernameTextView = view.findViewById(R.id.fragment_user_profile_username);
        descriptionTextView = view.findViewById(R.id.fragment_user_profile_description);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String uid = UserProfileFragmentArgs.fromBundle(getArguments()).getNavArgUserProfileUid();
        if (mainActivity.getFirebaseAuth().getCurrentUser().getUid().equals(uid)) {
            mainActivity.loadUserDetails();
            userDetails = mainActivity.getUserDetails();
            if (userDetails == null) {
                Toast.makeText(mainActivity, "Unknown error", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigateUp();
            }
            initialiseUserProfile();
        } else {
            mainActivity.requestWithAuthorisation(new Consumer<Map<String, String>>() {
                @Override
                public void accept(Map<String, String> headerMap) {
                    mainActivity.getCroundApi()
                            .getUserDetails(uid, headerMap).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                SuccessfulResponse<UserDetails> userDetailsSuccessfulResponse =
                                        new GsonBuilder().create().fromJson(response.body().charStream(),
                                                new TypeToken<SuccessfulResponse<UserDetails>>() {
                                                }.getType());
                                userDetails = userDetailsSuccessfulResponse.getResult().getData();
                                initialiseUserProfile();
                            } else {
                                int errno = new GsonBuilder().create()
                                        .fromJson(response.errorBody().charStream(), UnsuccessfulResponse.class)
                                        .getResponseError()
                                        .getErrno();
                                switch (errno) {
                                    case 1:
                                        Toast.makeText(mainActivity, "User does not exist", Toast.LENGTH_SHORT).show();
                                        NavHostFragment.findNavController(UserProfileFragment.this)
                                                .navigateUp();
                                        break;
                                    case 0:
                                    default:
                                        Log.e("UPF.onViewCreated", "Unknown server-side error.");
                                        break;
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e("UPF.onViewCreated", "Unknown error.");
                            StringBuilder sb = new StringBuilder();
                            sb.append(t.toString());
                            for (StackTraceElement ste : t.getStackTrace()) {
                                sb.append(ste.toString()).append("\n");
                            }
                            Log.e("UPF.onViewCreated", sb.toString());
                        }
                    });
                }
            });
        }
    }

    private void initialiseUserProfile() {
        if (userDetails.isDeleted()) {
            usernameTextView.setText("DELETED");
        } else {
            if (userDetails.getDisplayName() != null) {
                displayNameTextView.setText(userDetails.getDisplayName());
            }
            usernameTextView.setText(userDetails.getUsername());
            if (userDetails.getDescription() != null) {
                descriptionTextView.setText(userDetails.getDescription());
            }
        }
    }
}