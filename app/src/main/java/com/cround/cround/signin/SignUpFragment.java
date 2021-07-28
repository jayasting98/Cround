package com.cround.cround.signin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

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
import com.cround.cround.api.SignUpCredentials;
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
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpFragment extends Fragment {

    private static final String PASSWORD_REGEX_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?!.*[^0-9A-Za-z!@#$%^&*\\-_+=?]).{10,60}$";
//            "^" // must apply from start
//            + "(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])" // must have lowercase, uppercase, digits
//            + "(?!.*[^0-9A-Za-z!@#$%^&*\-_+=?])" // for excluding characters other than these
//            + ".{10,60}" // minimum length 10, maximum 60
//            + "$"; // must apply until end
    public static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX_PATTERN);
    private static final String USERNAME_REGEX_PATTERN = "^(?!.*[^a-zA-Z0-9\\-_]).{1,20}$";
    public static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX_PATTERN);

    private MainActivity mainActivity;
    private EditText emailEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView hintTextView;
    private Button signUpButton;
    private Button signInButton;
    private FirebaseAuth firebaseAuth;

    public SignUpFragment() {
        // Required empty public constructor
    }

    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();
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
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        emailEditText = view.findViewById(R.id.fragment_signUp_editText_email);
        usernameEditText = view.findViewById(R.id.fragment_signUp_editText_username);
        passwordEditText = view.findViewById(R.id.fragment_signUp_editText_password);
        hintTextView = view.findViewById(R.id.fragment_signUp_textView_hint);
        hintTextView.setText("");
        signUpButton = view.findViewById(R.id.fragment_signUp_button_signUp);
        signInButton = view.findViewById(R.id.fragment_signUp_button_signIn);
        mainActivity = (MainActivity) getActivity();
        firebaseAuth = mainActivity.getFirebaseAuth();
        initialise();
        return view;
    }

    private void initialise() {
        initialiseSignUpButton();
        initialiseSignInButton();
    }

    private void initialiseSignUpButton() {
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hintTextView.setText("");
                String username = usernameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (username.length() < 1 || email.length() < 1 || password.length() < 1) {
                    hintTextView.setText(R.string.fragment_signUp_textView_hint_allRequired);
                } else if (username.length() > 20) {
                    hintTextView.setText(R.string.fragment_signUp_textView_hint_longUsername);
                } else if (!isUsernameValid(username)) {
                    hintTextView.setText(R.string.fragment_signUp_textView_hint_invalidUsername);
                } else if (Pattern.compile("^[\\-_]+.*|.*[\\-_]+$").matcher(username).matches()) {
                    hintTextView.setText(R.string.fragment_signUp_textView_hint_usernameStartEnd);
                } else if (!isEmailValid(email)) {
                    hintTextView.setText(R.string.fragment_signUp_textView_hint_invalidEmail);
                } else if (password.length() < 10) {
                    hintTextView.setText(R.string.fragment_signUp_textView_hint_shortPassword);
                } else if (password.length() > 60) {
                    hintTextView.setText(R.string.fragment_signUp_textView_hint_longPassword);
                } else if (!Pattern.compile("^(?=.*[0-9]).{10,60}$").matcher(password).matches()) {
                    hintTextView.setText(R.string.fragment_signUp_textView_hint_passwordNumbers);
                } else if (!Pattern.compile("^(?=.*[a-z]).{10,60}$").matcher(password).matches()) {
                    hintTextView.setText(R.string.fragment_signUp_textView_hint_passwordLowercase);
                } else if (!Pattern.compile("^(?=.*[A-Z]).{10,60}$").matcher(password).matches()) {
                    hintTextView.setText(R.string.fragment_signUp_textView_hint_passwordUppercase);
                } else if (!isPasswordValid(password)) {
                    hintTextView.setText(R.string.fragment_signUp_textView_hint_invalidPassword);
                } else {
                    signUp(username, email, password);
                }
            }
        });
    }

    private void signUp(String username, String email, String password) {
        mainActivity.getCroundApi()
                .registerAccount(new CroundApiRequest<>(new SignUpCredentials(username, email, password))).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    usernameSignIn(username, password);
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
                            Log.e("Sign In", "Username is required.");
                            hintTextView.setText(R.string.fragment_signUp_textView_hint_allRequired);
                            break;
                        case 4:
                            Log.e("Sign In", "Username must be at most 20 characters.");
                            hintTextView.setText(R.string
                                    .fragment_signUp_textView_hint_longUsername);
                            break;
                        case 5:
                            Log.e("Sign In", "Username has invalid characters.");
                            hintTextView.setText(R.string
                                    .fragment_signUp_textView_hint_invalidUsername);
                            break;
                        case 6:
                            Log.e("Sign In", "Username starts or ends with - or _.");
                            hintTextView.setText(R.string
                                    .fragment_signUp_textView_hint_usernameStartEnd);
                            break;
                        case 7:
                            Log.e("Sign In", "Email is malformed.");
                            break;
                        case 8:
                            Log.e("Sign In", "Email is required.");
                            hintTextView.setText(R.string
                                    .fragment_signUp_textView_hint_allRequired);
                            break;
                        case 9:
                            Log.e("Sign In", "Email is invalid.");
                            hintTextView.setText(R.string
                                    .fragment_signUp_textView_hint_invalidEmail);
                            break;
                        case 10:
                            Log.e("Sign In", "Password is malformed.");
                            break;
                        case 11:
                            Log.e("Sign In", "Password is too short.");
                            hintTextView.setText(R.string
                                    .fragment_signUp_textView_hint_shortPassword);
                            break;
                        case 12:
                            Log.e("Sign In", "Password is too long.");
                            hintTextView.setText(R.string
                                    .fragment_signUp_textView_hint_longPassword);
                            break;
                        case 13:
                            Log.e("Sign In", "Password must contain numbers.");
                            hintTextView.setText(R.string
                                    .fragment_signUp_textView_hint_passwordNumbers);
                            break;
                        case 14:
                            Log.e("Sign In", "Password must contain lowercase letters.");
                            hintTextView.setText(R.string
                                    .fragment_signUp_textView_hint_passwordLowercase);
                            break;
                        case 15:
                            Log.e("Sign In", "Password must contain uppercase letters.");
                            hintTextView.setText(R.string
                                    .fragment_signUp_textView_hint_passwordUppercase);
                            break;
                        case 16:
                            Log.e("Sign In", "Password can only contain the special characters !@#$%^&*-_+=?.");
                            hintTextView.setText(R.string
                                    .fragment_signUp_textView_hint_invalidPassword);
                            break;
                        case 17:
                            Log.e("Sign In", "The username already exists.");
                            hintTextView.setText(R.string
                                    .fragment_signUp_textView_hint_usernameExists);
                            break;
                        case 18:
                            Log.e("Sign In", "The email already exists.");
                            hintTextView.setText(R.string
                                    .fragment_signUp_textView_hint_emailExists);
                            break;
                        case 0:
                        default:
                            Log.e("Sign In", "Unknown error.");
                            break;
                    }
                    Toast.makeText(mainActivity, "Could not sign-up. Please try again.", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                StringBuilder sb = new StringBuilder();
                sb.append(t.toString());
                for (StackTraceElement ste : t.getStackTrace()) {
                    sb.append(ste.toString()).append("\n");
                }
                Log.e("Sign Up", sb.toString());
                Toast.makeText(mainActivity, "Could not sign-up. Please try again.", Toast.LENGTH_SHORT);
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
                    Toast.makeText(mainActivity, "Could not sign-in. Please try again.", Toast.LENGTH_SHORT);
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
                Toast.makeText(mainActivity, "Could not sign-in. Please try again.", Toast.LENGTH_SHORT);
            }
        });
    }

    private void customTokenSignIn(String customToken) {
        firebaseAuth.signInWithCustomToken(customToken).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d("Sign In", "Custom token validated.");
                    NavHostFragment.findNavController(SignUpFragment.this)
                            .navigate(R.id.action_nav_fragment_signup_to_nav_fragment_main);
                } else {
                    Toast.makeText(mainActivity, "Could not sign-in. Please try again.", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private boolean isPasswordValid(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isUsernameValid(String username) {
        return USERNAME_PATTERN.matcher(username).matches();
    }

    private void initialiseSignInButton() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SignUpFragment.this)
                        .navigate(R.id.action_nav_fragment_signup_to_nav_fragment_signin);
            }
        });
    }
}