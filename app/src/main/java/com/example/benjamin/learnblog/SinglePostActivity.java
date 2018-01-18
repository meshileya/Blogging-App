package com.example.benjamin.learnblog;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SinglePostActivity extends AppCompatActivity {

    private static final String TAG = SinglePostActivity.class.getSimpleName();
    //[View declaration]
    private ImageView mPostImage;
    private CircleImageView profileImage;
    private TextView mUserName;
    private TextView mPostTitle;
    private TextView mPostContent;
    private Button mRemovePost;

    private String mPost_key;

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        mAuth = FirebaseAuth.getInstance();

        mPostImage = findViewById(R.id.iv_post_image);
        profileImage = findViewById(R.id.profile_image);
        mPostTitle = findViewById(R.id.tv_post_title);
        mPostContent = findViewById(R.id.tv_post_content);
        mUserName = findViewById(R.id.tv_username);

        mRemovePost = findViewById(R.id.btn_remove_post);

        mPost_key = getIntent().getStringExtra("POST_KEY");
        Log.d(TAG, "Post key: " + mPost_key);
        retrievePostContent();

        mRemovePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabaseReference.child(mPost_key).removeValue();
                startActivity(new Intent(SinglePostActivity.this, AllPostActivity.class));
                finish();
            }
        });
    }

    private void retrievePostContent() {
        Log.d(TAG, "Post key in method: " + mPost_key);
        mDatabaseReference.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String post_image = (String) dataSnapshot.child("image").getValue();
                String post_title = (String) dataSnapshot.child("title").getValue();
                String post_content = (String) dataSnapshot.child("content").getValue();
                String post_uid = (String) dataSnapshot.child("uid").getValue();
                String usrname = (String) dataSnapshot.child("username").getValue();

                final String profile_pics = (String) dataSnapshot.child("ownerDp").getValue();

                Picasso.with(SinglePostActivity.this).load(post_image).networkPolicy(NetworkPolicy.OFFLINE).into(mPostImage, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        Picasso.with(SinglePostActivity.this).load(post_image).into(mPostImage);
                    }
                });

                Picasso.with(SinglePostActivity.this).load(profile_pics).networkPolicy(NetworkPolicy.OFFLINE).into(profileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        Picasso.with(SinglePostActivity.this).load(profile_pics).into(profileImage);
                    }
                });
                mPostTitle.setText(post_title);
                Log.d(TAG, "Post Title: " + post_title);
                mPostContent.setText(post_content);
                Log.d(TAG, "Post Content: " + post_content);
                mUserName.setText(usrname);

                if (mAuth.getCurrentUser().getUid().equals(post_uid) ){
                    mRemovePost.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }
}
