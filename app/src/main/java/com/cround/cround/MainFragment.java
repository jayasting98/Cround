package com.cround.cround;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class MainFragment extends Fragment {

    private BottomNavigationView bottomNavigationView;
    private MainActivity mainActivity;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
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
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        bottomNavigationView = v.findViewById(R.id.activity_main_barBottom);
        mainActivity = (MainActivity) getActivity();
        firebaseAuth = mainActivity.getFirebaseAuth();
        firebaseUser = firebaseAuth.getCurrentUser();
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (firebaseUser == null) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_nav_fragment_main_to_nav_fragment_signin);
        }
        mainActivity.loadUserDetails();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                switch (item.getItemId()) {
                    case R.id.nav_timeline:
                        fragmentTransaction.replace(R.id.fragment_main_frameLayout, new TimelineFragment());
                        fragmentTransaction.commit();
                        return true;
                    case R.id.nav_explore:
                        fragmentTransaction.replace(R.id.fragment_main_frameLayout, new ExploreFragment());
                        fragmentTransaction.commit();
                        return true;
                    case R.id.nav_notifications:
                        fragmentTransaction.replace(R.id.fragment_main_frameLayout, new NotificationsFragment());
                        fragmentTransaction.commit();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_main_frameLayout, new TimelineFragment());
        fragmentTransaction.commit();
//        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }
}