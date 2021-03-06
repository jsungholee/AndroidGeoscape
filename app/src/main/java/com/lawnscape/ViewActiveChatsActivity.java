package com.lawnscape;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewActiveChatsActivity extends Activity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();

    ArrayList<User> userList;
    ArrayList<String> useridList;
    UserListAdapter userAdapter;

    ListView userListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_chats);
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //make sure user is logged in and has an account
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is not logged in
                    // launch login activity
                    //startActivity(new Intent(ViewJobsListsActivity.this, LoginActivity.class));
                    //finish();
                    System.out.println("LOG IN ERROR ");
                } else {
                    //user is logged in
                    currentUser = user;
                    DatabaseReference myChatsRef = database.getReference("Users").child(currentUser.getUid()).child("chatids");
                    //Gonna hold all the jobs, must init for adaptor
                    userList = new ArrayList<User>();
                    useridList = new ArrayList<String>();
                    //Put the jobs into the adaptor
                    //Find the listview widget and set up a connection to our ArrayList
                    userListView = (ListView) findViewById(R.id.lvJobRequesters);
                    userAdapter = new UserListAdapter(ViewActiveChatsActivity.this, userList);
                    userListView.setAdapter(userAdapter);
                    //Get the users the current user has chat messages with
                    myChatsRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            DatabaseReference myUserRef = database.getReference("Users");
                            useridList.clear();
                            for (DataSnapshot curUserid : dataSnapshot.getChildren()) {
                                useridList.add(curUserid.getKey().toString());
                            }
                            //Causes the listview to update with a list of user objects using UserListAdapter
                            myUserRef.addValueEventListener(
                                    new UserListVEListener(ViewActiveChatsActivity.this, userList, useridList, userAdapter));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    if (!useridList.isEmpty()) {
                    }

                    //This handles clicks on individual user items from the list
                    // and bring you to a job specific chat page with the user
                    userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                                long id) {
                            User selectedUser = (User) userAdapter.getItem(position);
                            Intent chatIntent = new Intent(ViewActiveChatsActivity.this, ChatActivity.class);
                            chatIntent.putExtra("otherid", selectedUser.getUserid());
                            startActivity(chatIntent);
                        }
                    });
                    // Hold down on a user in the chat list to get a popup
                    userListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
                                                long id) {
                            //Creating the instance of PopupMenu
                            PopupMenu popup = new PopupMenu(ViewActiveChatsActivity.this, view);
                            popup.getMenuInflater().inflate(R.menu.popup_user_menu, popup.getMenu());
                            //registering popup with OnMenuItemClickListener
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {

                                    final User selectedUser = (User) userAdapter.getItem(position);
                                    switch (item.getItemId()){
                                        case R.id.longclickDeleteChat:
                                            final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                            //remove the chat from the list of all chats for both users with a listener
                                            DatabaseReference myChatidRef = database.getReference("Users").child(currentUser.getUid().toString()).child("chatids");
                                            //doesnt delete the actual chat log ;)
                                            myChatidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    dataSnapshot.child(selectedUser.getUserid()).getRef().removeValue();
                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {  }
                                            });
                                            myChatidRef = database.getReference("Users").child(selectedUser.getUserid()).child("chatids");
                                            myChatidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    dataSnapshot.child(currentUser.getUid()).getRef().removeValue();
                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {  }
                                            });
                                            userList.remove(selectedUser);
                                            userAdapter.notifyDataSetChanged();
                                            return true;
                                        case R.id.longclickViewProfile:
                                            Intent viewProfileIntent = new Intent(ViewActiveChatsActivity.this, ViewUserProfileActivity.class);
                                            viewProfileIntent.putExtra("UserID", selectedUser.getUserid());
                                            startActivity(viewProfileIntent);
                                            return true;
                                    }
                                    return true;
                                }
                            });
                            popup.show();//showing popup menu
                            return true;
                        }
                    });
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        // Boiler plate Authentication
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Boiler plate Authentication
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    /*******************
     * Menu Handling
     *******************/
    //make the menu show up
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_posts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.viewPostsMenuMyProfile:
                startActivity(new Intent(this, ViewMyProfileActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuAllChats:
                startActivity(new Intent(this, ViewActiveChatsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuMyJobs:
                startActivity(new Intent(this, ViewMyPostsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuAllJobs:
                Intent allJobsViewIntent = new Intent(this, ViewJobsListsActivity.class);
                allJobsViewIntent.putExtra("View", "all");
                startActivity(allJobsViewIntent);
                finish();
                return true;

            case R.id.viewPostsMenuSearch:
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            case R.id.viewPostsMenuJobsMap:
                Intent savedJobsViewIntent = new Intent(this, MapJobsActivity.class);
                startActivity(savedJobsViewIntent);
                finish();
                return true;
            case R.id.viewPostsMenuSignOut:
                auth.signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}