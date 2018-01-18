package com.example.benjamin.learnblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupAccount extends BaseActivity implements
        View.OnClickListener{

    private static final String TAG = SetupAccount.class.getSimpleName();
    private static final String REQUIRED = "Required";

    //[View Declaration]
    private ImageButton dp;
    private EditText dpName;
    private Button finishSetup;

    //[Related to starting gallery]
    private final int GALLERY_REQUEST = 1;
    private Uri dpUri = null;

    //[Fire base related ]
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_account);

        mProgressDialog = new ProgressDialog(this);
        //[Views Initialization]
        dp = findViewById(R.id.btn_display_picture);
        dpName = findViewById(R.id.et_dp_name);
        finishSetup = findViewById(R.id.btn_finish_setup);

        //[Hooking up click listener]
        dp.setOnClickListener(this);
        finishSetup.setOnClickListener(this);

        //[Fire base initialization]
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_display_picture:
                 selectImage();
                break;
            case R.id.btn_finish_setup:
                startSetupAccount();
                break;
        }
    }
    public void selectImage(){
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Choose display picture"), GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            dpUri = data.getData();
            CropImage.activity(dpUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                dpUri = result.getUri();
                dp.setImageURI(dpUri);
            }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
                Log.d(TAG, "Error code: " + error);
                Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startSetupAccount() {
        //[Validate user]
        final String name = dpName.getText().toString().trim();
        final String user_id = mAuth.getCurrentUser().getUid();
        if (!TextUtils.isEmpty(name) && dpUri != null){
            mProgressDialog.setMessage("Finishing Setup...");
            mProgressDialog.show();
            // Save the image filePath into Class [Storage_Reference]
            /*This saves the image into fire_base storage*/
            StorageReference filePath = mStorageRef.child(dpUri.getLastPathSegment());
            // Put the file path into the URI
            filePath.putFile(dpUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUri = taskSnapshot.getDownloadUrl().toString();

                    mDatabaseUsers.child(user_id).child("name").setValue(name);
                    mDatabaseUsers.child(user_id).child("propics").setValue(downloadUri);

                    hideProgressDialog();
                    Intent mainIntent = new Intent(SetupAccount.this, AllPostActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Account setup error: " + e.getMessage());
                    /*Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();*/
                    Toast.makeText(SetupAccount.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else
            dpName.setError(REQUIRED);
    }
}
