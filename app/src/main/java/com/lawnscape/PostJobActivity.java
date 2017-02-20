package com.lawnscape;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PostJobActivity extends Activity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);
        Button profile_btn = (Button)findViewById(R.id.profile_btn);

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

        profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostJobActivity.this, ProfileActivity.class));
            }
        });

        /* Fragment NavBar (Broken)
        // guide followed from https://yout.be/tXpw4cEvecY
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FragmentNavBar fragmentNavBar = new FragmentNavBar();
        fragmentTransaction.add(fragmentNavBar, "nav bar");
        fragmentTransaction.commit();
        */
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
        int success = 0;
        TextView etTitle = (TextView) findViewById(R.id.etPostJobTitle);
        TextView etLocation = (TextView) findViewById(R.id.etPostJobLocation);
        TextView etDescription = (TextView) findViewById(R.id.etPostJobDescription);

        String newTitle = etTitle.getText().toString();
        String newLoc = etLocation.getText().toString();
        String newDesc = etDescription.getText().toString();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myJobsRef = database.getReference("Jobs");

        // Add a job
        if(!newTitle.equals("")&&(!newLoc.equals(""))){
            DatabaseReference newJobRef = myJobsRef.push();
            if(!newDesc.isEmpty()){
                newDesc = "No description";
            }
            newJobRef.setValue(new Job(newTitle, newLoc, newDesc));
        }
        finish();
    }
}
