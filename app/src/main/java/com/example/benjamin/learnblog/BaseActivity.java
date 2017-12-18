package com.example.benjamin.learnblog;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by Benjamin on 12/18/2017.
 */

public class Dialogs extends Activity{

    public static Dialogs self;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        self = this;
    }

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
}
