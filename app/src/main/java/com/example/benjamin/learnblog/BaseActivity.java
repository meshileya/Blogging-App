package com.example.benjamin.learnblog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Benjamin on 12/18/2017.
 */

public class BaseActivity extends AppCompatActivity{

//    public static BaseActivity self;

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    /*@Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        self = this;
    }*/

    /**
     * [Snack Bar]Handling popping up of snack bar
     * */
    public static void showSnackBar(View view, String message, int duration){
        final Snackbar snackbar = Snackbar.make(view, message, duration);

        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });

        View snackStyle = snackbar.getView();
        snackStyle.setBackgroundResource(R.color.colorPrimary);
        snackbar.show();
    }

    /**
     * [Progress Dialog]
     * */
    public void showProgressDialog(){
        if (mProgressDialog == null){
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog(){
        if (mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    public static class BaseFragment extends Fragment{

//    public static BaseActivity self;

        @VisibleForTesting
        public ProgressDialog mProgressDialog;

    /*@Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        self = this;
    }*/

        /**
         * [Snack Bar]Handling popping up of snack bar
         * */
        public static void showSnackBar(View view, String message, int duration){
            final Snackbar snackbar = Snackbar.make(view, message, duration);

            snackbar.setAction("Dismiss", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.dismiss();
                }
            });

            View snackStyle = snackbar.getView();
            snackStyle.setBackgroundResource(R.color.colorPrimary);
            snackbar.show();
        }

        /**
         * [Progress Dialog]
         * */
        public void showProgressDialog(){
            if (mProgressDialog == null){
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setMessage("Loading...");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
            }
            mProgressDialog.show();
        }

        public void hideProgressDialog(){
            if (mProgressDialog != null && mProgressDialog.isShowing()){
                mProgressDialog.dismiss();
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            hideProgressDialog();
        }
    }
}
