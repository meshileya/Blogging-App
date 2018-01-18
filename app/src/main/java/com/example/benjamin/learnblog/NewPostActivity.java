package com.example.benjamin.learnblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class NewPostActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = NewPostActivity.class.getSimpleName();
    private static final String REQUIRED = "Required";

    /*[Start] View Declarations*/
    private ImageButton imageButton;
    private EditText mPostTitle, mPostBody;
    private Button btn_Submit;
    private TextView image_placeholderText;
    /*[End] View declaration*/

    private final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri = null;
    private ProgressDialog mProgressDialog;

    /*[Start][Fire-base] Declaration*/
    private DatabaseReference mDbReference;
    private StorageReference mStorageReference;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDbReferenceUsers;

    /*[Start][Fire-base] Declaration*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        /*[start] View initialization + [onClick Listener]*/
        imageButton = findViewById(R.id.ibtn_image_placeholder);
        imageButton.setOnClickListener(this);

        mPostTitle = findViewById(R.id.ed_post_title);
        mPostBody = findViewById(R.id.ed_post_body);

        btn_Submit = findViewById(R.id.btn_submit_post);
        btn_Submit.setOnClickListener(this);

        image_placeholderText = findViewById(R.id.tv_image_text_placeholder);
        /*[End] View initialization + [onClick Listener]*/

        /*[Start][Fire-base] Initialization*/
        mDbReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        mStorageReference = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mDbReferenceUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        /*[Start][Fire-base] Initialization*/

        mProgressDialog = new ProgressDialog(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.all_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_all_post){
            startActivity(new Intent(NewPostActivity.this, AllPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.ibtn_image_placeholder:
                selectImage();
                break;
            case R.id.btn_submit_post:
                startPosting();
                break;
        }
    }

    public void selectImage(){
        // Set up intent to open a gallery
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);

        //show only images no videos.
        galleryIntent.setType("image/*");

        //Always show the chooser (if there are multiple apps that can access the gallery]
        /**
         * startActivity for result takes in 2 params
         * Intent params
         * A request code
         * */
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // This method is tied to the [selectImage] method. it handles the result of the chosen image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Handle validation for the above intent

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            mImageUri =  data.getData();
            Log.d(TAG, "Generated Uri: " + String.valueOf(mImageUri));

            imageButton.setImageURI(mImageUri);
            image_placeholderText.setVisibility(View.INVISIBLE);
        }
    }

    private  void startPosting(){
        final String postTitle = mPostTitle.getText().toString().trim();
        final String postBody = mPostBody.getText().toString().trim();

        mProgressDialog.setTitle(R.string.app_name);
        mProgressDialog.setMessage("Posting");
        mProgressDialog.show();

        if (!TextUtils.isEmpty(postTitle) && !TextUtils.isEmpty(postBody) && mImageUri != null){
            StorageReference filePath = mStorageReference.child("Blog_Image").child(mImageUri.getLastPathSegment());

            filePath.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri downloadUri = taskSnapshot.getDownloadUrl();

                    /*[push()] generates a unique key*/
                    final DatabaseReference newPost = mDbReference.push();

                    mDbReferenceUsers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Adding content into the unique key generated in the database
                            newPost.child("image").setValue(downloadUri.toString());
                            newPost.child("title").setValue(postTitle);
                            newPost.child("content").setValue(postBody);
                            newPost.child("uid").setValue(mCurrentUser.getUid());

                            newPost.child("ownerDp").setValue(dataSnapshot.child("propics").getValue());
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Intent toAllPost = new Intent(NewPostActivity.this, AllPostActivity.class);
                                                toAllPost.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(toAllPost);
                                                finish();
                                            }
                                        }
                                    });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "Log in database error: " + databaseError.getMessage());
                            Toast.makeText(NewPostActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    mProgressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mProgressDialog.dismiss();
                    Log.d(TAG, "Posting failed: " + e.getMessage());
                    Toast.makeText(NewPostActivity.this, "Posting Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            mPostTitle.setError(REQUIRED);
            mPostBody.setError(REQUIRED);
        }
    }
}
