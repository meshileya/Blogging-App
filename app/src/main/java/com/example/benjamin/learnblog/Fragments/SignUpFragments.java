package com.example.benjamin.learnblog.Fragments;


import android.app.ProgressDialog;
import android.content.Intent;
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

import com.example.benjamin.learnblog.AllPostActivity;
import com.example.benjamin.learnblog.BaseActivity;
import com.example.benjamin.learnblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragments extends BaseActivity.BaseFragment implements View.OnClickListener{

    private static final String TAG = SignUpFragments.class.getSimpleName();
    private static final String REQUIRED = "Required";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    //[Declaring Views]
    private EditText mName, mEmail, mPassword;
    private Button normSignInButton;

    View rootView;
    public SignUpFragments() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);

        mName = rootView.findViewById(R.id.et_name);
        mEmail = rootView.findViewById(R.id.et_email);
        mPassword = rootView.findViewById(R.id.et_password);
        normSignInButton = rootView.findViewById(R.id.btn_sign_up);
        normSignInButton.setOnClickListener(this);
        //[Referencing Views]*Ends*

        mProgressDialog = new ProgressDialog(getContext());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        return rootView;
    }
    private void startRegister(){
        final String name = mName.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(name)){
            mProgressDialog.setMessage("Signing Up...");
            mProgressDialog.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        String user_id = mAuth.getCurrentUser().getUid();
                        DatabaseReference currentUserDb = mDatabase.child(user_id);
                        currentUserDb.child("name").setValue(name);
                        currentUserDb.child("image").setValue("default");
                        hideProgressDialog();

                        Intent mainIntent = new Intent(getActivity(), AllPostActivity.class);
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
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
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_sign_up:
                startRegister();
                break;
        }
    }
}
