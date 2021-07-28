package com.cround.cround;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.cround.cround.signin.SignInActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private NavigationView navigationView;
    private NavController navController;
    private DrawerLayout drawerLayout;
    private View drawerMenuHeader;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            exitToSignInActivity(); // if signed out, exit now
        } else {
//        exitToSignInActivity();
            enableExitWhenSignedOut(); // if not signed out, auto-exit if signed out later

            drawerLayout = findViewById(R.id.activity_main_layout_drawer);
            navigationView = findViewById(R.id.nav_view);
            drawerMenuHeader = navigationView.getHeaderView(0);

            initialiseNavigation();

            String displayName = getUserDisplayName();
            String username = getUserUsername();
            int points = getUserPoints();
            TextView drawerMenuHeaderNameTextView = drawerMenuHeader.findViewById(R.id.menu_drawer_header_name);
            drawerMenuHeaderNameTextView.setText(displayName);
            TextView drawerMenuHeaderUsernameTextView = drawerMenuHeader.findViewById(R.id.menu_drawer_header_username);
            drawerMenuHeaderUsernameTextView.setText(username);
            TextView drawerMenuHeaderPointsTextView = drawerMenuHeader.findViewById(R.id.menu_drawer_header_points);
            drawerMenuHeaderPointsTextView.setText(String.valueOf(points));
        }
    }

    private void enableExitWhenSignedOut() {
        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull @NotNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    exitToSignInActivity();
                }
            }
        });
    }

    private void exitToSignInActivity() {
//        navController.navigate(R.id.action_nav_fragment_main_to_nav_fragment_signin);
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void initialiseNavigation() {
        Toolbar mainActivityToolbar = findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(mainActivityToolbar);

        Set<Integer> topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.nav_fragment_main);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(topLevelDestinations)
                        .setOpenableLayout(drawerLayout)
                        .build();
        NavigationUI.setupWithNavController(
                mainActivityToolbar, navController, appBarConfiguration);

        initialiseDrawerMenuNavigation();
    }

    private void initialiseDrawerMenuNavigation() {
        initialiseDrawerMenuHeaderNavigation();
        initialiseDrawerMenuItemNavigation();
    }

    private void initialiseDrawerMenuHeaderNavigation() {
        drawerMenuHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_nav_fragment_main_to_nav_fragment_user_profile);
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
                        navController.navigate(R.id.action_nav_fragment_main_to_nav_fragment_settings);
                        break;
                    case R.id.nav_signOut:
                        AuthUI.getInstance().signOut(MainActivity.this);
                        break;
                    default:
                }
                drawerLayout.close();
                return false;
            }
        });
    }

    private String getUserDisplayName() {
        return firebaseUser.getDisplayName();
    }

    private String getUserUsername() {
        return firebaseUser.getEmail();
    }

    private int getUserPoints() {
        return 9000;
    }
}