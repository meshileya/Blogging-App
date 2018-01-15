package com.example.benjamin.learnblog;

import android.content.Intent;
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

public class SinglePostActivity extends AppCompatActivity {

    private static final String TAG = SinglePostActivity.class.getSimpleName();
    //[View declaration]
    private ImageView mPostImage;
    private TextView mPostTitle;
    private TextView mPostContent;
    private Button mRemovePost;

    private String mPost_key = null;

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("BLog");
        mAuth = FirebaseAuth.getInstance();

        mPostImage = findViewById(R.id.iv_post_image);
        mPostTitle = findViewById(R.id.tv_post_title);
        mPostContent = findViewById(R.id.tv_post_content);

        mRemovePost = findViewById(R.id.btn_remove_post);

        Intent intentExtras = getIntent();
        Bundle bundleExtras = intentExtras.getExtras();

        if (bundleExtras != null){
            mPost_key = bundleExtras.getString("POST_KEY");
            Log.d(TAG, "Post key: " + mPost_key);
            retrievePostContent();
        }else{
            Toast.makeText(this, "No data in the Extras sent from previous activity", Toast.LENGTH_SHORT).show();
        }

        mRemovePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabaseReference.child(mPost_key).removeValue();
                startActivity(new Intent(SinglePostActivity.this, AllPostActivity.class));
            }
        });
    }

    private void retrievePostContent() {
        mDatabaseReference.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String post_image = (String) dataSnapshot.child("image").getValue();
                String post_title = (String) dataSnapshot.child("title").getValue();
                String post_content = (String) dataSnapshot.child("content").getValue();
                String post_uid = (String) dataSnapshot.child("uid").getValue();

                Picasso.with(SinglePostActivity.this).load(post_image).networkPolicy(NetworkPolicy.OFFLINE).into(mPostImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(SinglePostActivity.this).load(post_image).into(mPostImage);
                    }
                });
                mPostTitle.setText(post_title);
                mPostContent.setText(post_content);

                if (mAuth.getCurrentUser().getUid().equals(post_uid) ){
                    mRemovePost.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
