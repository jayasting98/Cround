package com.cround.cround.signin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.cround.cround.MainActivity;
import com.cround.cround.R;
import com.cround.cround.api.CroundApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.regex.Pattern;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignInActivity extends AppCompatActivity {

    private static final String PASSWORD_REGEX_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?!.*[^0-9A-Za-z!@#$%^&*\\-_+=?]).{10,60}$";
//            "^" // must apply from start
//            + "(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])" // must have lowercase, uppercase, digits
//            + "(?!.*[^0-9A-Za-z!@#$%^&*\-_+=?])" // for excluding characters other than these
//            + ".{10,60}" // minimum length 10, maximum 60
//            + "$"; // must apply until end
    public static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX_PATTERN);
    private static final String USERNAME_REGEX_PATTERN = "^(?!.*[^a-zA-Z0-9\\-_]).{1,20}$";
    public static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX_PATTERN);

    private FragmentManager fragmentManager;
    private CroundApi croundApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        fragmentManager = getSupportFragmentManager();
        loadSignInFragment();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CroundApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        croundApi = retrofit.create(CroundApi.class);
    }

    public void loadSignInFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.activity_signIn_frameLayout, SignInFragment.newInstance(this));
        fragmentTransaction.commit();
    }

    public void loadSignUpFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.activity_signIn_frameLayout, SignUpFragment.newInstance(this));
        fragmentTransaction.commit();
    }

    public void enterMainActivity() {
        Intent loadSignedInUIIntent = new Intent(this, MainActivity.class);
        startActivity(loadSignedInUIIntent);
        finish();
    }

    public CroundApi getCroundApi() {
        return croundApi;
    }
}