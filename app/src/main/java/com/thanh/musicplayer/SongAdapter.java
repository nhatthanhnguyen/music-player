package com.thanh.musicplayer;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongViewHolder> {
    private List<Song> songs;
    private OnItemClickedListener onItemClickedListener;

    public SongAdapter(List<Song> songs, OnItemClickedListener onItemClickedListener) {
        this.songs = songs;
        this.onItemClickedListener = onItemClickedListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_song_item, parent, false);
        return new SongViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.textViewSongName.setText(song.getSongName());
        holder.textViewArtistName.setText(song.getArtistName());
        Picasso.get().load(song.getSongImageUrl()).into(holder.imageViewSong);
        if (song.isSelected())
            holder.textViewSongName.setTextColor(holder.itemView.getContext().getColor(R.color.green));
        else
            holder.textViewSongName.setTextColor(holder.itemView.getContext().getColor(R.color.white));
        holder.itemView.setOnClickListener(v -> {
            ApiSongs.toggleSelected(song.getId());
            onItemClickedListener.onSongItemClickedListener(song);
            if (song.isSelected())
                holder.textViewSongName.setTextColor(holder.itemView.getContext().getColor(R.color.green));
            else
                holder.textViewSongName.setTextColor(holder.itemView.getContext().getColor(R.color.white));
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
}
