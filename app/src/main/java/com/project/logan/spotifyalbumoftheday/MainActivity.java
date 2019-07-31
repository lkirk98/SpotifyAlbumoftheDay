package com.project.logan.spotifyalbumoftheday;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class MainActivity extends AppCompatActivity {

    AlbumFragment ALBUM;
    PlaylistFragment PLAYLIST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Initialize the spotify service so that the fragments can make calls to the web api */
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(getIntent().getExtras().getString("access_token"));
        SpotifyService service = api.getService();

        /* Initialize the two fragments to be displayed in the pager */
        String token = getIntent().getExtras().getString("access_token");//token passed in through signin activity
        Bundle b = new Bundle();
        b.putString("access_token", token);
        ALBUM = new AlbumFragment();
        PLAYLIST = new PlaylistFragment();
        ALBUM.setArguments(b);
        PLAYLIST.setArguments(b);

        /* Setting up the tabbed view pager */
        ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), ALBUM, PLAYLIST);

        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void onClickPopulate(View view){
        ALBUM.populate(view);
    }
}
