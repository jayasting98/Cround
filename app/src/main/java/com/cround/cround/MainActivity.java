package com.cround.cround;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Consumer;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.cround.cround.api.CroundApi;
import com.cround.cround.api.SuccessfulResponse;
import com.cround.cround.api.UnsuccessfulResponse;
import com.cround.cround.api.UserDetails;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private NavigationView navigationView;
    private NavController navController;
    private DrawerLayout drawerLayout;
    private View drawerMenuHeader;
    private FirebaseUser firebaseUser;
    private CroundApi croundApi;
    private UserDetails userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CroundApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        croundApi = retrofit.create(CroundApi.class);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        drawerLayout = findViewById(R.id.activity_main_layout_drawer);
        navigationView = findViewById(R.id.nav_view);
        drawerMenuHeader = navigationView.getHeaderView(0);

        initialiseNavigation();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.useEmulator("10.0.2.2", 9099); // TODO
    }

    private void initialiseNavigation() {
        initialiseDrawerMenuNavigation();
    }

    public void requestWithAuthorisation(Map<String, String> headerMap, Consumer<Map<String, String>> headerMapConsumer) {
        firebaseUser.getIdToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    String idToken = task.getResult().getToken();
                    headerMap.put("Authorization", "Bearer " + idToken);
                    headerMapConsumer.accept(headerMap);
                }
            }
        });
    }

    public void requestWithAuthorisation(Consumer<Map<String, String>> headerMapConsumer) {
        requestWithAuthorisation(new HashMap<>(), headerMapConsumer);
    }

    public void loadUserDetails() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null && (userDetails == null || currentUser != firebaseUser)) {
            firebaseUser = currentUser;
            requestWithAuthorisation(new Consumer<Map<String, String>>() {
                @Override
                public void accept(Map<String, String> headerMap) {
                    croundApi.getUserDetails(firebaseUser.getUid(), headerMap).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                SuccessfulResponse<UserDetails> userDetailsSuccessfulResponse =
                                        new GsonBuilder().create()
                                                .fromJson(new BufferedReader(response.body().charStream()),
                                                        new TypeToken<SuccessfulResponse<UserDetails>>(){}
                                                                .getType());
                                userDetails = userDetailsSuccessfulResponse.getResult().getData();
                                initialiseDrawerMenuHeader();
                            } else {
                                int errno = new GsonBuilder()
                                        .create().fromJson(new BufferedReader(response.errorBody()
                                                .charStream()), UnsuccessfulResponse.class)
                                        .getResponseError()
                                        .getErrno();
                                switch (errno) {
                                    case -1:
                                    case 1:
                                        Log.e("loadUserDetails", "User does not exist.");
                                        break;
                                    case 0:
                                    default:
                                        Log.e("loadUserDetails", "Unknown server-side error.");
                                        break;
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e("loadUserDetails", "Unknown error.");
                            StringBuilder sb = new StringBuilder();
                            sb.append(t.toString());
                            for (StackTraceElement ste : t.getStackTrace()) {
                                sb.append(ste.toString()).append("\n");
                            }
                            Log.e("loadUserDetails", sb.toString());
                        }
                    });
                }
            });
        }
    }

    public void initialiseDrawerMenuHeader() {
        String displayName = userDetails.getDisplayName();
        String username = userDetails.getUsername();
        long points = userDetails.getPoints();
        TextView drawerMenuHeaderNameTextView = drawerMenuHeader.findViewById(R.id.menu_drawer_header_name);
        drawerMenuHeaderNameTextView.setText(displayName);
        TextView drawerMenuHeaderUsernameTextView = drawerMenuHeader.findViewById(R.id.menu_drawer_header_username);
        drawerMenuHeaderUsernameTextView.setText(username);
        TextView drawerMenuHeaderPointsTextView = drawerMenuHeader.findViewById(R.id.menu_drawer_header_points);
        drawerMenuHeaderPointsTextView.setText(String.valueOf(points));
    }

    private void initialiseDrawerMenuNavigation() {
        initialiseDrawerMenuHeaderNavigation();
        initialiseDrawerMenuItemNavigation();
    }

    private void initialiseDrawerMenuHeaderNavigation() {
        drawerMenuHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.cround.cround.MainFragmentDirections.ActionNavFragmentMainToNavFragmentUserProfile loadOwnUserProfileAction
                        = MainFragmentDirections.actionNavFragmentMainToNavFragmentUserProfile(firebaseUser.getUid());
                navController.navigate(loadOwnUserProfileAction);
                drawerLayout.close();
            }
        });
    }

    private void initialiseDrawerMenuItemNavigation() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_fragment_settings:
                        navController.navigate(MainFragmentDirections.actionNavFragmentMainToNavFragmentSettings());
                        break;
                    case R.id.nav_signOut:
                        firebaseAuth.signOut();
                        navController.navigate(MainFragmentDirections.actionNavFragmentMainToNavFragmentSignin());
                        break;
                    default:
                }
                drawerLayout.close();
                return false;
            }
        });
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public CroundApi getCroundApi() {
        return croundApi;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }
}