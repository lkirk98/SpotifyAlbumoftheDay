package com.project.logan.spotifyalbumoftheday;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.Result;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.RetrofitError;

import static android.support.constraint.Constraints.TAG;


public class AlbumFragment extends Fragment {

    private SpotifyService SERVICE;
    private ArrayList<AlbumSimple> ALBUMS = new ArrayList<>();
    private ArrayList<String> ALBUM_TITLES = new ArrayList<>(); /* TODO temp workaround for not adding albums more than once */
    private AlbumSimple CURRENT_ALBUM = null;
    final String PLAYLIST_TITLE = "Album of the Day";
    private View VIEW;
    private RecyclerViewAdapter RECYCLER_ADAPTER;

    public AlbumFragment() {
        /* Required empty constructor */
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(getArguments().getString("access_token"));
        SERVICE = api.getService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_album, container, false);
        VIEW = v;
        initRecycler(v);
        updateAlbumText();
        return v;
    }

    private void initRecycler(View v){
        Log.d(TAG, "initialize recycler view album");
        RecyclerView recycler = v.findViewById(R.id.recycler_album);
        RECYCLER_ADAPTER = new RecyclerViewAdapter(ALBUMS, getContext());
        recycler.setAdapter(RECYCLER_ADAPTER);//TODO make method
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
    }



    /* Kicks off requesting all saved tracks and finding their albums */
    public void populate(View view){
        //TODO make it evident to the user that data is being requested and processed
        new RetrieveSpotifyTracks().execute();
    }

    private void updateAlbumText(){
        TextView v = VIEW.findViewById(R.id.current_album);
        if(CURRENT_ALBUM == null){
            v.setText("No current album of the day");
        } else {
            v.setText("Current album of the day: \n" + CURRENT_ALBUM.name);
        }
    }



    private void populatePlaylistWithAlbum(){
        AlbumSimple album = null;

        /* Get a random album */
        int rand = (int) (Math.random() * ALBUMS.size());
        for(AlbumSimple a : ALBUMS){//TODO regular loop
            if(rand-- == 0){
                album = a;
                break;
            }
        }

        /* Get the tracks from the album. Will go into RetrieveAlbumTracks async task
         * and from there fillPlaylist() will be called.
         */
        CURRENT_ALBUM = album;
        new RetrieveAlbumTracks().execute(album);
    }

    private void addAlbumsToRecycler(){
        RECYCLER_ADAPTER.updateAlbums(ALBUMS);
        RECYCLER_ADAPTER.notifyDataSetChanged();
        /*
        int size = ALBUMS.size();
        for(int i = 0; i < size; i++){//TODO don't add each album if it already existed
            RECYCLER_ADAPTER.addItem(ALBUMS.get(i));
            RECYCLER_ADAPTER.notifyItemInserted(i);
        }*/
    }

    /* Class responsible for asynchronously for requesting all saved songs from the spotify web api*/
    private class RetrieveSpotifyTracks extends AsyncTask<Void, Void, Void> {
        final int NUM_FETCH = 50; /* Number of tracks to fetch at a time */

        /* Track number is the offset at which to request tracks */
        protected Void doInBackground(Void... args){
            int count = 0;
            boolean done = false;

            Map<String, Object> options = new HashMap<String, Object>();
            options.put(SpotifyService.LIMIT, NUM_FETCH);
            /* Need to have done boolean in case a user's library has a number of track divisible
             * by NUM_FETCH
             */
            while(count % NUM_FETCH == 0 && !done){
                /* Offset the total number of tracks already fetched */
                options.put(SpotifyService.OFFSET, count);
                try {
                    Pager<SavedTrack> mySavedTracks = SERVICE.getMySavedTracks(options);
                    List<SavedTrack> tracks = mySavedTracks.items;

                    if(tracks.size() == 0) done = true;

                    for(SavedTrack t : tracks){
                        if(!ALBUM_TITLES.contains(t.track.album.name)){
                            ALBUMS.add(t.track.album);
                            ALBUM_TITLES.add(t.track.album.name);
                        }
                        count++;
                    }
                } catch (RetrofitError error) {
                    SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                    // handle error
                    System.out.println(spotifyError.getMessage());
                }
            }
            return null;
        }

        protected void onPostExecute(Void arg){
            /* Now that all the songs have been requested, next step is to populate the album of
             * the day playlist with a random album
             */
            addAlbumsToRecycler();
            populatePlaylistWithAlbum();
        }
    }

    /* Class that will handle the requesting of tracks from spotify web api */
    private class RetrieveAlbumTracks extends AsyncTask<AlbumSimple, Void, Void>{
        UserPrivate USER = null;

        /* Responsible for creating and populating the playlist that will actually hold the tracks
         * for the album of the day
         */
        protected Void doInBackground(AlbumSimple... album){
            /* Populate the USER field for use later */
            try {
                USER = SERVICE.getMe();
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                // handle error
                System.out.println(spotifyError.getMessage());
            }

            /* Need to find if the album of the day playlist already exists or not,
             * and if it doesn't, create it
             */
            /* Have to work with two different objects because the spotify web wrapper likes to
             * use un-interchangeable simple and regular versions of classes. smh
             */
            PlaylistSimple albumOfDayPlaylist_simple = null;
            Playlist albumOfDayPlaylist_reg = null;
            try {
                Pager<PlaylistSimple> playlists = SERVICE.getMyPlaylists();
                boolean found = false;

                for(PlaylistSimple p : playlists.items){
                    if(p.name.equals(PLAYLIST_TITLE)){
                        albumOfDayPlaylist_simple = p;
                        found = true;
                        break;
                    }
                }

                if(!found) albumOfDayPlaylist_reg = createPlaylist();
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                // handle error
                System.out.println(spotifyError.getMessage());
            }

            /* Next: Obtain the tracks from the random album */
            Pager<Track> tracks = null;
            try{
                tracks = SERVICE.getAlbumTracks(album[0].id);
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                // handle error
                System.out.println(spotifyError.getMessage());
            }

            /* Finally, populate the playlist with songs from the album */
            try{
                StringBuilder trackURIs = new StringBuilder();
                for(Track t : tracks.items){
                    trackURIs.append(t.uri + ",");
                }
                /* Good old fencepost problem. Take off last comma */
                trackURIs.deleteCharAt(trackURIs.length() - 1);

                String playlist_id = albumOfDayPlaylist_simple != null ?
                        albumOfDayPlaylist_simple.id : albumOfDayPlaylist_reg.id;


                Result r = SERVICE.replaceTracksInPlaylist(USER.id, playlist_id, trackURIs.toString(), new Object());
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                // handle error
                System.out.println(spotifyError.getMessage());
            }

            return null;
        }

        private Playlist createPlaylist(){
            Playlist p = null;
            try{
                Map<String, Object> options = new HashMap<>();
                options.put("name", PLAYLIST_TITLE);
                p = SERVICE.createPlaylist(USER.id, options);
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                // handle error
                System.out.println(spotifyError.getMessage());
            }
            return p;
        }

        protected void onPostExecute(Void arg){
            //TODO remove the alert or disable to the user since everything is done now
            System.out.println("updating text");
            updateAlbumText();
        }
    }
}
