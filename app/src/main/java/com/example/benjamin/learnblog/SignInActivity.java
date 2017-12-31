package com.example.benjamin.learnblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = SignInActivity.class.getSimpleName();
    private static final String REQUIRED = "Required";

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN_CODE = 1;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    //[Declaring Views]
    private EditText mName, mEmail, mPassword;
    private Button normSignInButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //[Referencing Views]*Starts*
        mName = findViewById(R.id.et_name);
        mEmail = findViewById(R.id.et_email);
        mPassword = findViewById(R.id.et_password);
        normSignInButton = findViewById(R.id.btn_norm_sign_in);
        normSignInButton.setOnClickListener(this);
        //[Referencing Views]*Ends*

        SignInButton signIn = findViewById(R.id.gms_sign_in_button);
        signIn.setOnClickListener(this);


        /**
         * Crete a user [GoogleSignInOption] Object that will request the user data required for this app
         * Using the google sign in object with the [DEFAULT_SIGN_IN] parameter it request for the basic information for the user
         * [GoogleSignInOptions] Objects creates a new configuration
         * */

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail() // Not recommended because Email address might change
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
    }

    @Override
    protected void onStart() {
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
                signIn();
                break;
            case R.id.btn_norm_sign_in:
                startNormsRegister();
                break;
        }
    }

    private void startNormsRegister() {
        final String name = mName.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        //[Validate] provided details
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(name)){
            showProgressDialog();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference currentUserDb = mDatabaseReference.child(user_id);
                                Log.d(TAG, "Normal Sign in user id: " + currentUserDb);
                                currentUserDb.child("name").setValue(name);
                                currentUserDb.child("image").setValue("default");
                                hideProgressDialog();

                                startActivity(new Intent(SignInActivity.this, AllPostActivity.class));
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            Log.e(TAG, "Failure: Sign in Failed" + e.getMessage());
                            showSnackBar(findViewById(R.id.cl_snack_bar), "Sign in failed", Snackbar.LENGTH_LONG);
                        }
                    });
        }else{
            mName.setError(REQUIRED);
            mEmail.setError(REQUIRED);
            mPassword.setError(REQUIRED);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * This is the result returned from the Sign in Intent + a request code
         * Check if(the request code is equal to the Sign in code)
         * */
        if (requestCode == RC_SIGN_IN_CODE){
            // The task returned from this call is always completed so we won't need to call a listener
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google Sign in is successful authenticate with fire base + update the UI appropriately
                GoogleSignInAccount account = task.getResult(ApiException.class);
                authWithGoogle(account);
            }catch (ApiException e){
                //Google Sign in failed + update the UI appropriately
                Log.w(TAG, "Google sign in failed: ", e);
                showSnackBar(findViewById(R.id.cl_snack_bar), "Sign in Failed", Snackbar.LENGTH_INDEFINITE);
                updateUi(null);
            }
        }
    }

    private void authWithGoogle(GoogleSignInAccount account){
        Log.d(TAG, "fire base Auth with google: " + account.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "SignInWithCredentials:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            String user_id = mAuth.getCurrentUser().getUid();
                            mDatabaseReference.child(user_id);
                            String curretnUserId = mDatabaseReference.push().getKey();
                            Log.d(TAG, "Auth with google user id: " + user_id);
                            Log.d(TAG, "User Id via key: " + curretnUserId);
                            updateUi(user);
                        }else{
                            Log.w(TAG, "signInWithCredentials:failure", task.getException());
                            showSnackBar(findViewById(R.id.cl_snack_bar), "Authentication", Snackbar.LENGTH_SHORT);
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void updateUi(FirebaseUser user){
        hideProgressDialog();
        if (user != null){
            startActivity(new Intent(SignInActivity.this, AllPostActivity.class));
        }
    }
}
