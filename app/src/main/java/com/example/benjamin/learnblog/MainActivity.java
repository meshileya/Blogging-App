package com.example.benjamin.learnblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();
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
    DatabaseReference mDbReference;
    StorageReference mStorageReference;
    /*[Start][Fire-base] Declaration*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        /*[Start][Fire-base] Initialization*/

        mProgressDialog = new ProgressDialog(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add){
            startActivity(new Intent(MainActivity.this, PostActivity.class));
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

    private void selectImage(){
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

            /*try{
                mImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Log.d(TAG, String.valueOf(mImageBitmap));
                imageButton.setImageBitmap(mImageBitmap);
                image_placeholderText.setVisibility(View.INVISIBLE);
            }catch (IOException e){
                e.printStackTrace();
            }*/
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
                    Uri downloadUri = taskSnapshot.getDownloadUrl();

                    /*[push()] generates a unique key*/
                    DatabaseReference newPost = mDbReference.push();

                    // Adding content into the unique key generated in the database
                    newPost.child("Image_link").setValue(downloadUri.toString());
                    newPost.child("Title").setValue(postTitle);
                    newPost.child("Post").setValue(postBody);

                    mProgressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Posting Successful", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mProgressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Posting Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            mPostTitle.setError(REQUIRED);
            mPostBody.setError(REQUIRED);
        }
    }
}
