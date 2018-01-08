package com.example.benjamin.learnblog.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.benjamin.learnblog.Fragments.SignInFragment;
import com.example.benjamin.learnblog.Fragments.SignUpFragment;

/**
 * Created by Benjamin on 05-Jan-18.
 */

public class TabsPagerAdapter extends FragmentPagerAdapter{

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int pages) {
        switch(pages){
            case 0:
                // Sign in activity
                return new SignInFragment();
            case 1:
                // Sign Up activity
                return new SignUpFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal number of tabs
        return 2;
    }
}
