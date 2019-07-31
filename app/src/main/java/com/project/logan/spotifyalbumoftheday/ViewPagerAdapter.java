package com.project.logan.spotifyalbumoftheday;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private Fragment ALBUM_FRAGMENT;
    private Fragment PLAYLIST_FRAGMENT;

    public ViewPagerAdapter(FragmentManager fm, Fragment album, Fragment playlist){
        super(fm);
        ALBUM_FRAGMENT = album;
        PLAYLIST_FRAGMENT = playlist;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0: return ALBUM_FRAGMENT;
            case 1: return PLAYLIST_FRAGMENT;
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0: return "ALBUMS";
            case 1: return "PLAYLISTS";
            default: return null;
        }
    }
}
