package com.cround.cround.signin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cround.cround.MainActivity;
import com.cround.cround.R;
import com.cround.cround.api.CroundApiRequest;
import com.cround.cround.api.EmailCredentials;
import com.cround.cround.api.SuccessfulResponse;
import com.cround.cround.api.UnsuccessfulResponse;
import com.cround.cround.api.UsernameCredentials;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInFragment extends Fragment {

    private SignInActivity signInActivity;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView hintTextView;
    private Button signInButton;
    private Button signUpButton;
    private FirebaseAuth firebaseAuth;

    public SignInFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SignInFragment newInstance(SignInActivity signInActivity) {
        SignInFragment fragment = new SignInFragment();
        fragment.signInActivity = signInActivity;
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
        View view = inflater.inflate(R.layout.fragment_signin, container, false);
        usernameEditText = view.findViewById(R.id.fragment_signIn_editText_username);
        passwordEditText = view.findViewById(R.id.fragment_signIn_editText_password);
        hintTextView = view.findViewById(R.id.fragment_signIn_textView_hint);
        hintTextView.setText("");
        signInButton = view.findViewById(R.id.fragment_signIn_button_signIn);
        signUpButton = view.findViewById(R.id.fragment_signIn_button_signUp);
        firebaseAuth = FirebaseAuth.getInstance();
//        firebaseAuth.useEmulator("10.0.2.2", 9099); // TODO
        initialise();

        return view;
    }

    private void initialise() {
        initialiseSignInButton();
        initialiseSignUpButton();
    }

    private void initialiseSignInButton() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hintTextView.setText("");
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                    String email = username; // redundancy for clarity
                    emailSignIn(email, password);
                } else {
                    usernameSignIn(username, password);
                }
            }
        });
    }

    private void emailSignIn(String email, String password) {
        signInActivity.getCroundApi()
                .validateEmailCredentials(new CroundApiRequest<>(new EmailCredentials(email, password)))
                .enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    SuccessfulResponse<String> successfulResponse = new GsonBuilder().create()
                            .fromJson(new BufferedReader(response.body().charStream()),
                                    new TypeToken<SuccessfulResponse<String>>(){}.getType());
                    String customToken = successfulResponse.getResult().getData();
                    customTokenSignIn(customToken);
                } else {
                    int errno = new GsonBuilder().create()
                            .fromJson(new BufferedReader(response.errorBody().charStream()),
                                    UnsuccessfulResponse.class)
                            .getResponseError()
                            .getErrno();
                    switch (errno) {
                        case 1:
                            Log.e("Sign In", "Request is malformed.");
                            break;
                        case 2:
                            Log.e("Sign In", "Email is malformed.");
                            break;
                        case 3:
                            Log.e("Sign In", "Password is malformed.");
                            break;
                        case 4:
                            Log.e("Sign In", "Email does not exist.");
                            hintTextView.setText(R.string
                                    .fragment_signIn_textView_hint_invalidCredentials);
                            break;
                        case 5:
                            Log.e("Sign In", "Invalid password.");
                            hintTextView.setText(R.string
                                    .fragment_signIn_textView_hint_invalidCredentials);
                            break;
                        case 0:
                        default:
                            Log.e("Sign In", "Unknown error.");
                            break;
                    }
                    Toast.makeText(signInActivity, "Could not sign-in. Please try again.", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                StringBuilder sb = new StringBuilder();
                sb.append(t.toString());
                for (StackTraceElement ste : t.getStackTrace()) {
                    sb.append(ste.toString()).append("\n");
                }
                Log.e("Sign In", sb.toString());
                Toast.makeText(signInActivity, "Could not sign-in. Please try again.", Toast.LENGTH_SHORT);
            }
        });
    }

    private void usernameSignIn(String username, String password) {
        signInActivity.getCroundApi()
                .validateUsernameCredentials(new CroundApiRequest<>(new UsernameCredentials(username, password)))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    SuccessfulResponse<String> successfulResponse = new GsonBuilder().create()
                            .fromJson(new BufferedReader(response.body().charStream()),
                                    new TypeToken<SuccessfulResponse<String>>(){}.getType());
                    String customToken = successfulResponse.getResult().getData();
                    customTokenSignIn(customToken);
                } else {
                    int errno = new GsonBuilder().create()
                            .fromJson(new BufferedReader(response.errorBody().charStream()),
                                    UnsuccessfulResponse.class)
                            .getResponseError()
                            .getErrno();
                    switch (errno) {
                        case 1:
                            Log.e("Sign In", "Request is malformed.");
                            break;
                        case 2:
                            Log.e("Sign In", "Username is malformed.");
                            break;
                        case 3:
                            Log.e("Sign In", "Password is malformed.");
                            break;
                        case 4:
                            Log.e("Sign In", "Username does not exist.");
                            hintTextView.setText(R.string
                                    .fragment_signIn_textView_hint_invalidCredentials);
                            break;
                        case 5:
                            Log.e("Sign In", "Invalid password.");
                            hintTextView.setText(R.string
                                    .fragment_signIn_textView_hint_invalidCredentials);
                            break;
                        case 0:
                        default:
                            Log.e("Sign In", "Unknown error.");
                            break;
                    }
                    Toast.makeText(signInActivity, "Could not sign-in. Please try again.", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                StringBuilder sb = new StringBuilder();
                sb.append(t.toString());
                for (StackTraceElement ste : t.getStackTrace()) {
                    sb.append(ste.toString()).append("\n");
                }
                Log.e("Sign In", sb.toString());
                Toast.makeText(signInActivity, "Could not sign-in. Please try again.", Toast.LENGTH_SHORT);
            }
        });
    }

    private void customTokenSignIn(String customToken) {
        firebaseAuth.signInWithCustomToken(customToken).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d("Sign In", "Custom token validated.");
                    signInActivity.enterMainActivity();
                } else {
                    Toast.makeText(signInActivity, "Could not sign-in. Please try again.", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void initialiseSignUpButton() {
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInActivity.loadSignUpFragment();
            }
        });
    }
}