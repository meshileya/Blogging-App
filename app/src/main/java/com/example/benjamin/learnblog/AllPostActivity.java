package com.example.benjamin.learnblog;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Benjamin on 12/13/2017.
 */

public class AllPostActivity extends BaseActivity {

    private RecyclerView mRecyclerView;

    private DatabaseReference mDbReference;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDbNodeLike;

    private FirebaseAuth mAuth;
    private boolean mDoLikes;

    /*private Uri xx;*/

    FirebaseRecyclerAdapter<Blog, BlogViewHolder> fireBaseRecyclerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_post);

        showProgressDialog();

        mDoLikes = false;
        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = findViewById(R.id.rv_all_post);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //[Initialize the fire base]
        mDbReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDbNodeLike = FirebaseDatabase.getInstance().getReference().child("Likes");

        mDatabaseUsers.keepSynced(true);
        mDbNodeLike.keepSynced(true);
        mDbReference.keepSynced(true);

        /**
         * [starts]
         * Retrieve data from the database using Fire base UI*/
        Query postQuery = FirebaseDatabase.getInstance().getReference().child("Blog");

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
                // Retrieve post key fot single post activity.
                final String post_key = getRef(position).getKey();

                holder.setTitle(model.getTitle());
                holder.setContent(model.getContent());
                holder.setImage(getApplicationContext(), model.getImage());
                holder.setUsername(model.getUsername());
                holder.setOwnerDp(getApplicationContext(), model.getOwnerDp());

                holder.setLikeBtn(post_key);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent singlePostIntent = new Intent(AllPostActivity.this, SinglePostActivity.class);
                        singlePostIntent.putExtra("POST_KEY", post_key);
                        startActivity(singlePostIntent);
                    }
                });

                holder.mBtnLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDoLikes = true;
                            mDbNodeLike.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (mDoLikes){
                                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){
                                            mDbNodeLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();

                                            mDoLikes = false;
                                        }else {
                                            mDbNodeLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("Default");

                                            mDoLikes = false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                    }
                });
            }
        };
        mRecyclerView.setAdapter(fireBaseRecyclerAdapter);
        /**
         * [Ends]
         * Retrieve data from the database using Fire base UI*/
        checkUserExist();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
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
        private CircleImageView mProfileImage;
        private TextView mPostTitle;
        private TextView mPostContent;
        private TextView mPostOwner;

        private ImageButton mBtnLike;
        private DatabaseReference sDbNodeLikes;
        private FirebaseAuth sNodeLikesAuth;
        /*[End] View declaration*/

        View mView;

        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mBtnLike = mView.findViewById(R.id.btn_like);

            sDbNodeLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
            sNodeLikesAuth = FirebaseAuth.getInstance();

            sDbNodeLikes.keepSynced(true);
        }

        private void setLikeBtn(final String post_key){
            // To enable real time database
            sDbNodeLikes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(post_key).hasChild(sNodeLikesAuth.getCurrentUser().getUid())){
                        mBtnLike.setImageResource(R.drawable.ic_action_unlike);
                    }else
                        mBtnLike.setImageResource(R.drawable.ic_action_like);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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
        public void setUsername(String username){
            mPostOwner = itemView.findViewById(R.id.tv_username);
            mPostOwner.setText(username);
        }

        public void setOwnerDp(final Context context, final String ownerDp) {
            mProfileImage = itemView.findViewById(R.id.profile_image);
            Picasso.with(context).load(ownerDp).networkPolicy(NetworkPolicy.OFFLINE).into(mProfileImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context).load(ownerDp).into(mProfileImage);
                }
            });
        }
    }
}
