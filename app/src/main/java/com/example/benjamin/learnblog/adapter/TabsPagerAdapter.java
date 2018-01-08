package com.example.benjamin.learnblog.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.benjamin.learnblog.Fragments.SignInFragments;
import com.example.benjamin.learnblog.Fragments.SignUpFragments;

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
                return new SignInFragments();
            case 1:
                // Sign Up activity
                return new SignUpFragments();
        }
        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal number of tabs
        return 2;
    }
}
