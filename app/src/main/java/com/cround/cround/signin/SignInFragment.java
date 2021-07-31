package com.cround.cround.signin;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInFragment extends Fragment {

    private MainActivity mainActivity;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView hintTextView;
    private Button signInButton;
    private TextView signUpTextView;
    private FirebaseAuth firebaseAuth;

    public SignInFragment() {
        // Required empty public constructor
    }

    public static SignInFragment newInstance() {
        SignInFragment fragment = new SignInFragment();
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
        signUpTextView = view.findViewById(R.id.fragment_signIn_textView_signUp);
        mainActivity = (MainActivity) getActivity();
        firebaseAuth = mainActivity.getFirebaseAuth();
        initialise();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    private void initialise() {
        initialiseSignInButton();
        initialiseSignUpButton();
    }

    private void initialiseSignInButton() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                InputMethodManager inputMethodManager = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
//                inputMethodManager.hideSoftInputFromWindow(mainActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
        mainActivity.getCroundApi()
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
                    Toast.makeText(mainActivity, "Could not sign-in. Please try again.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(mainActivity, "Could not sign-in. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void usernameSignIn(String username, String password) {
        mainActivity.getCroundApi()
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
                    Toast.makeText(mainActivity, "Could not sign-in. Please try again.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(mainActivity, "Could not sign-in. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void customTokenSignIn(String customToken) {
        firebaseAuth.signInWithCustomToken(customToken).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d("Sign In", "Custom token validated.");
                    NavHostFragment.findNavController(SignInFragment.this).navigate(R.id.action_nav_fragment_signin_to_nav_fragment_main);
                } else {
                    Toast.makeText(mainActivity, "Could not sign-in. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initialiseSignUpButton() {
        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SignInFragment.this)
                        .navigate(R.id.action_nav_fragment_signin_to_nav_fragment_signup);
            }
        });
    }
}