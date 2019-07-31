package com.project.logan.spotifyalbumoftheday;

import android.content.Context;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.AlbumSimple;

import static android.content.ContentValues.TAG;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<AlbumSimple> ALBUMS = new ArrayList<>();
    private Context CONTEXT;

    public RecyclerViewAdapter(ArrayList<AlbumSimple> t, Context c){
        if(t != null) ALBUMS = t;
        CONTEXT = c;
    }

    public void updateAlbums(ArrayList<AlbumSimple> a){
        ALBUMS = a;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Log.d(TAG, "onBindViewHolder called");

        viewHolder.text.setText(ALBUMS.get(i).name);
    }

    @Override
    public int getItemCount() {
        return ALBUMS.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView){
            super(itemView);

            parentLayout = itemView.findViewById(R.id.parent_layout);
            text = itemView.findViewById(R.id.listitem_text);
        }
    }
}
