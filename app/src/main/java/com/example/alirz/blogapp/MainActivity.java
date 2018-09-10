package com.example.alirz.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    //vidgets
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private BottomNavigationView bottomNavigationView;


    //vars
    private FirebaseAuth mAuth;
    private String current_user_id;
    private DocumentReference userRef;

    private String user_name;

    //fragments
    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;
    private NotificationsFragment notificationsFragment;
    private PostFragment postFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Blog App");
        fab = findViewById(R.id.main_fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, PostActivity.class));

            }
        });

        homeFragment = new HomeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container, homeFragment);
        transaction.commit();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        userRef = FirebaseFirestore.getInstance().collection("Users").document(current_user_id);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    user_name = task.getResult().getString("name");
                }
            }
        });


        // bottomNavigationView=findViewById(R.id.main_bottom_bar);

        // notificationsFragment = new NotificationsFragment();
        // postFragment = new PostFragment();
        // profileFragment = new ProfileFragment();
        // homeFragment = new HomeFragment();


        if (mAuth.getCurrentUser() != null) {

          /*  initiliazeFragment();


            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Fragment current_fragment=getSupportFragmentManager().findFragmentById(R.id.main_container);

                    switch (item.getItemId()) {
                        case R.id.ic_home:
                            placeFragments(homeFragment,current_fragment);
                            break;
                        case R.id.ic_notifications:
                            placeFragments(notificationsFragment,current_fragment);

                            break;

                        case R.id.ic_add:
                            placeFragments(postFragment,current_fragment);

                            break;
                        case R.id.ic_profile:
                            placeFragments(profileFragment,current_fragment);

                            break;

                    }
                    return false;
                }
            });
            */
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_logout:
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            case R.id.ic_search:
                break;
            case R.id.ic_settings:
                Intent i = new Intent(MainActivity.this, AccountSettingsActivity.class);
                i.putExtra("user_name", user_name);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (current_user_id == null) {
            Intent i = new Intent(MainActivity.this, WelcomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }

    }

    private void placeFragments(Fragment fragment, Fragment currentFragment) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (fragment == homeFragment) {
            transaction.hide(notificationsFragment);
            transaction.hide(postFragment);
            transaction.hide(profileFragment);
        }

        if (fragment == notificationsFragment) {
            transaction.hide(homeFragment);
            transaction.hide(postFragment);
            transaction.hide(profileFragment);
        }

        if (fragment == postFragment) {
            transaction.hide(notificationsFragment);
            transaction.hide(homeFragment);
            transaction.hide(profileFragment);
        }

        if (fragment == profileFragment) {
            transaction.hide(notificationsFragment);
            transaction.hide(postFragment);
            transaction.hide(homeFragment);
        }

        transaction.show(fragment);

        transaction.commit();
    }

    private void initiliazeFragment() {

       /*
        transaction.add(R.id.main_container,notificationsFragment);
        transaction.add(R.id.main_container,postFragment);
        transaction.add(R.id.main_container,profileFragment);

        transaction.hide(notificationsFragment);
        transaction.hide(postFragment);
        transaction.hide(profileFragment);
        transaction.commit();
        */
    }


}
