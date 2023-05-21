package com.thanh.musicplayer;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SongViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewSongName;
    public TextView textViewArtistName;

    public SongViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewSongName = itemView.findViewById(R.id.textViewSongName);
        textViewArtistName = itemView.findViewById(R.id.textViewArtistName);
    }
}
