package com.lawnscape;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PostJobActivity extends Activity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //make sure user is logged in and has an account
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(PostJobActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //user is logged in
                    currentUser = FirebaseAuth.getInstance().getCurrentUser();
                }
            }
        };

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bot_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.add_job_nav_bar:
                                Intent job = new Intent(PostJobActivity.this, PostJobActivity.class);
                                startActivity(job);
                                break;
                            case R.id.profile_nav_bar:
                                Intent prof = new Intent(PostJobActivity.this,
                                        ViewMyProfileActivity.class);
                                startActivity(prof);
                                break;
                        }
                        return true;
                    }
                }
        );
    }
    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    public void postJob(View v){
        // grab the widgets as objects
        TextView etTitle = (TextView) findViewById(R.id.etPostJobTitle);
        TextView etLocation = (TextView) findViewById(R.id.etPostJobLocation);
        TextView etDescription = (TextView) findViewById(R.id.etPostJobDescription);

        String newTitle = etTitle.getText().toString();
        String newLoc = etLocation.getText().toString();
        String newDesc = etDescription.getText().toString();
        String userID = currentUser.getUid().toString();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myJobsRef = database.getReference("Jobs");
        DatabaseReference myUserJobRef = database.getReference("Users").child(currentUser.getUid().toString()).child("jobs").push();

        // Add a job
        DatabaseReference newJobRef = myJobsRef.push();
        if(newDesc.equals("")){
            newDesc = "No description";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd mm:ss");
        Job newJob = new Job(sdf.format(new Date()),newTitle, newLoc, newDesc, userID, newJobRef.getKey());
        newJobRef.setValue(newJob);
        myUserJobRef.setValue(newJobRef.getKey());
        finish();
    }
}
