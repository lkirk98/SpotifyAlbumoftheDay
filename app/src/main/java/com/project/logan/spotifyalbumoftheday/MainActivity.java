package com.project.logan.spotifyalbumoftheday;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.ErrorDetails;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.Result;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    private SpotifyService SERVICE;
    private Set<AlbumSimple> ALBUMS = new HashSet<>();
    final String PLAYLIST_TITLE = "Album of the Day";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Set up the spotify api with user access token */
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(getIntent().getExtras().getString("access_token"));
        SERVICE = api.getService();
    }

    /* Kicks off requesting all saved tracks and finding their albums */
    public void onClickPopulate(View view){
        //TODO make it evident to the user that data is being requested and processed
        new RetrieveSpotifyTracks().execute();
    }


    private void populatePlaylistWithAlbum(){
        AlbumSimple album = null;

        /* Get a random album */
        int rand = (int) (Math.random() * ALBUMS.size());
        for(AlbumSimple a : ALBUMS){
            if(rand-- == 0){
                album = a;
                break;
            }
        }

        /* Get the tracks from the album. Will go into RetrieveAlbumTracks async task
         * and from there fillPlaylist() will be called.
         */
        new RetrieveAlbumTracks().execute(album);
    }

    //simply adds the album that track t is from into the global set of albums
    private void addAlbumFromTrack(Track t){
        ALBUMS.add(t.album);
    }


    /* Class responsible for asynchronously for requesting all saved songs from the spotify web api*/
    private class RetrieveSpotifyTracks extends AsyncTask<Void, Void, Void>{
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
                        addAlbumFromTrack(t.track);
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

        protected void onPostExecute(){
            //TODO remove the alert or disable to the user since everything is done now
        }
    }
}
