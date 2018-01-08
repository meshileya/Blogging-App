package com.example.benjamin.learnblog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by Benjamin on 12/13/2017.
 */

public class AllPostActivity extends BaseActivity {

    private RecyclerView mRecyclerView;

    private DatabaseReference mDbReference;
    private DatabaseReference mDatabaseUsers;

    private FirebaseAuth mAuth;

    FirebaseRecyclerAdapter<Blog, BlogViewHolder> fireBaseRecyclerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_post);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = findViewById(R.id.rv_all_post);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDbReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        mDatabaseUsers.keepSynced(true);
        mDbReference.keepSynced(true);

        checkUserExist();

        /**
         * [starts]
         * Retrieve data from the database using Fire base UI*/
        Query postQuery = FirebaseDatabase.getInstance().getReference().child("Blog").limitToFirst(50);

        FirebaseRecyclerOptions<Blog> options = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(postQuery, Blog.class)
                .build();

        fireBaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(options) {
            @Override
            public BlogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.post_item, parent, false);
                hideProgressDialog();
                return new BlogViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(BlogViewHolder holder, int position, Blog model) {
                holder.setTitle(model.getTitle());
                holder.setContent(model.getContent());
                holder.setImage(getApplicationContext(), model.getImage());
            }
        };
        mRecyclerView.setAdapter(fireBaseRecyclerAdapter);
        /**
         * [Ends]
         * Retrieve data from the database using Fire base UI*/
    }

    private void checkUserExist(){
        if (mAuth.getCurrentUser() != null){
            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)){
                        Intent setupAccount = new Intent(AllPostActivity.this, SetupAccount.class);
                        setupAccount.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupAccount);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserExist();
        fireBaseRecyclerAdapter.startListening();
        hideProgressDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
        fireBaseRecyclerAdapter.stopListening();
        hideProgressDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fireBaseRecyclerAdapter.startListening();
        showProgressDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_add_post:
                startActivity(new Intent(AllPostActivity.this, NewPostActivity.class));
                break;
            case R.id.sign_out:
                mAuth.signOut();
                Intent signOutIntent = new Intent(AllPostActivity.this, MainActivity.class);
                signOutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(signOutIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder{
        /*[starts] View declaration*/
        private ImageView mPostImage;
        private TextView mPostTitle;
        private TextView mPostContent;
        /*[End] View declaration*/

        View mView;

        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setImage(final Context context, final String image){
            mPostImage = itemView.findViewById(R.id.iv_post_image);
            Picasso.with(context).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(mPostImage, new Callback() {
                @Override
                public void onSuccess() {
                    /*Do nothing*/
                }

                @Override
                public void onError() {
                    Picasso.with(context).load(image).into(mPostImage);
                }
            });
        }
        public void setTitle(String title){
            mPostTitle = itemView.findViewById(R.id.tv_post_title);
            mPostTitle.setText(title);
        }

        public void setContent(String content){
            mPostContent = itemView.findViewById(R.id.tv_post_content);
            mPostContent.setText(content);
        }
    }
}
