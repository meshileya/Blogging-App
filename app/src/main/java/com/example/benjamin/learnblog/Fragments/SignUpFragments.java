package com.example.benjamin.learnblog.Fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.benjamin.learnblog.AllPostActivity;
import com.example.benjamin.learnblog.BaseActivity;
import com.example.benjamin.learnblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragments extends BaseActivity.BaseFragment implements View.OnClickListener{

    private static final String TAG = SignUpFragments.class.getSimpleName();
    private static final String REQUIRED = "Required";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mUserStorage;
    //[Declaring Views]
    private EditText mName, mEmail, mPassword;
    private ImageButton mProfilePics;
    private Button normSignUnButton;

    private final int GALLERY_REQUEST = 1;
    private Uri dpUri = null;
    View rootView;
    public SignUpFragments() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);

        mProfilePics = rootView.findViewById(R.id.btn_profile_pic);
        mName = rootView.findViewById(R.id.et_name);
        mEmail = rootView.findViewById(R.id.et_email);
        mPassword = rootView.findViewById(R.id.et_password);
        normSignUnButton = rootView.findViewById(R.id.btn_sign_up);
        normSignUnButton.setOnClickListener(this);
        mProfilePics.setOnClickListener(this);
        //[Referencing Views]*Ends*

        mProgressDialog = new ProgressDialog(getContext());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserStorage = FirebaseStorage.getInstance().getReference("Profile Images");

        return rootView;
    }

    public void selectImage(){
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Choose display picture"), GALLERY_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            dpUri = data.getData();
            Log.d(TAG, "Display picture Uri: " + dpUri);
            CropImage.activity(dpUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(getContext(), this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                dpUri = result.getUri();
                Log.d(TAG, "Picture Uri: " + dpUri);
                mProfilePics.setImageURI(dpUri);
            }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
                Log.d(TAG, "Error code: " + error);
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRegister(){
        final String name = mName.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(name) && dpUri != null){
            mProgressDialog.setMessage("Signing Up...");
            mProgressDialog.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        StorageReference dpPath = mUserStorage.child(dpUri.getLastPathSegment());

                        dpPath.putFile(dpUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        String downloadUri = taskSnapshot.getDownloadUrl().toString();
                                        String user_id = mAuth.getCurrentUser().getUid();
                                        DatabaseReference currentUserDb = mDatabase.child(user_id);

                                        currentUserDb.child("name").setValue(name);
                                        currentUserDb.child("propics").setValue(downloadUri);
                                        hideProgressDialog();

                                        Intent mainIntent = new Intent(getActivity(), AllPostActivity.class);
                                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(mainIntent);
                                    }
                                });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgressDialog();
                    Log.e(TAG, "Failure: Sign up Failed" + e.getMessage());
                    showSnackBar(rootView.findViewById(R.id.cl_snack_bar), e.getMessage(), Snackbar.LENGTH_INDEFINITE);
                }
            });
        }else {
            mName.setError(REQUIRED);
            mEmail.setError(REQUIRED);
            mPassword.setError(REQUIRED);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_sign_up:
                startRegister();
                break;
            case R.id.btn_profile_pic:
                selectImage();
                break;
        }
    }
}
