package com.example.benjamin.learnblog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

/**
 * Created by Benjamin on 12/13/2017.
 */

public class AllPostActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private DatabaseReference mDbReference;

    public AllPostActivity() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_post);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = findViewById(R.id.rv_all_post);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDbReference = FirebaseDatabase.getInstance().getReference().child("Blog");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Query postQuery = FirebaseDatabase.getInstance().getReference().child("Blog").limitToFirst(50);

        FirebaseRecyclerOptions<Blog> options = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(postQuery, Blog.class)
                .build();

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(options) {
            @Override
            public BlogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.post_item, parent, false);
                return new BlogViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(BlogViewHolder holder, int position, Blog model) {
                holder.setTitle(model.getTitle());
                holder.setContent(model.getContent());
                holder.setImage(getApplicationContext(), model.getImage());
            }
        };
        firebaseRecyclerAdapter.startListening();
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

        public void setImage(Context context, String image){
            mPostImage = itemView.findViewById(R.id.iv_post_image);
            Picasso.with(context).load(image).into(mPostImage);
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
