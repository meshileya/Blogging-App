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
import com.example.benjamin.learnblog.SetupAccount;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignInFragments extends BaseActivity.BaseFragment implements View.OnClickListener{

    private static final String TAG = SignInFragments.class.getSimpleName();
    private static final String REQUIRED = "Required";

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN_CODE = 1;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;

    //[Declaring Views]
    private EditText mEmail, mPassword;
    private Button normSignInButton;
    View rootView;

    public SignInFragments() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);

        //[Referencing Views]*Starts*
        mEmail = rootView.findViewById(R.id.et_email);
        mPassword = rootView.findViewById(R.id.et_password);
        normSignInButton = rootView.findViewById(R.id.btn_norm_sign_in);
        normSignInButton.setOnClickListener(this);
        //[Referencing Views]*Ends*

        SignInButton signIn = rootView.findViewById(R.id.gms_sign_in_button);
        signIn.setOnClickListener(this);

        mProgressDialog = new ProgressDialog(getContext());


        /**
         * Crete a user [GoogleSignInOption] Object that will request the user data required for this app
         * Using the google sign in object with the [DEFAULT_SIGN_IN] parameter it request for the basic information for the user
         * [GoogleSignInOptions] Objects creates a new configuration
         * */

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail() // Not recommended because Email address might change
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        // Enable offline capability
        mDatabaseUsers.keepSynced(true);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Check if user is currently signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUi(currentUser);
    }

    public void signIn(){
        // Start the intent for the user to select an email address to sign in / add an account
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN_CODE);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.gms_sign_in_button:
                // Google sing method
                signIn();
                break;
            case R.id.btn_norm_sign_in:
                // Sign in with email and password
                checkStartLogin();
                break;
        }
    }

    private void checkStartLogin() {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        //[Validate] provided details
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            mProgressDialog.setMessage("Checking Login...");
            mProgressDialog.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                hideProgressDialog();
                                checkUserExist();
                                String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference currentUserDb = mDatabaseUsers.child(user_id);
                                Log.d(TAG, "Normal Sign in user id: " + currentUserDb);
                                currentUserDb.child("image").setValue("default");
                                hideProgressDialog();

                                startActivity(new Intent(getActivity(), AllPostActivity.class));
                            }else
                                hideProgressDialog();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            Log.e(TAG, "Failure: Sign in Failed" + e.getMessage());
                            showSnackBar(rootView.findViewById(R.id.cl_snack_bar), "Sign in failed", Snackbar.LENGTH_LONG);
                        }
                    });
        }else{
            mEmail.setError(REQUIRED);
            mPassword.setError(REQUIRED);
        }
    }
    private void checkUserExist(){
        if (mAuth.getCurrentUser() != null){
            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(user_id)){
                        Intent successSignIn = new Intent(getContext(), AllPostActivity.class);
                        successSignIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(successSignIn);
                    }else{
                        Intent setupIntents = new Intent(getContext(), SetupAccount.class);
                        setupIntents.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntents);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    showSnackBar(rootView.findViewById(R.id.cl_snack_bar), "Log in failed", Snackbar.LENGTH_SHORT);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * This is the result returned from the Sign in Intent + a request code
         * Check if(the request code is equal to the Sign in code)
         * */
        if (requestCode == RC_SIGN_IN_CODE){
            // The task returned from this call is always completed so we won't need to call a listener
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            mProgressDialog.setMessage("Starting Sign in");
            mProgressDialog.show();
            try {
                //Google Sign in is successful authenticate with fire base + update the UI appropriately
                GoogleSignInAccount account = task.getResult(ApiException.class);
                authWithGoogle(account);
            }catch (ApiException e){
                //Google Sign in failed + update the UI appropriately
                Log.w(TAG, "Google sign in failed: ", e);
                mProgressDialog.dismiss();
                showSnackBar(rootView.findViewById(R.id.cl_snack_bar), "Sign in Failed", Snackbar.LENGTH_INDEFINITE);
                updateUi(null);
            }
        }
    }

    private void authWithGoogle(GoogleSignInAccount account){
        Log.d(TAG, "fire base Auth with google: " + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "SignInWithCredentials:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            String user_id = mAuth.getCurrentUser().getUid();
                            mDatabaseUsers.child(user_id);
                            String currentUserId = mDatabaseUsers.push().getKey();
                            Log.d(TAG, "Auth with google user id: " + user_id);
                            Log.d(TAG, "User Id via key: " + currentUserId);
                            updateUi(user);
                        }else{
                            Log.w(TAG, "signInWithCredentials:failure", task.getException());
                            showSnackBar(rootView.findViewById(R.id.cl_snack_bar), "Auth with google failed", Snackbar.LENGTH_SHORT);
                            mProgressDialog.dismiss();
                            checkUserExist();
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    private void updateUi(FirebaseUser user){
        hideProgressDialog();
        if (user != null){
            startActivity(new Intent(getActivity(), AllPostActivity.class));
        }
    }
}
