package com.thanh.musicplayer;

import static com.thanh.musicplayer.MusicPlayerApplication.ACTION_NEXT;
import static com.thanh.musicplayer.MusicPlayerApplication.ACTION_PAUSE;
import static com.thanh.musicplayer.MusicPlayerApplication.ACTION_PREV;
import static com.thanh.musicplayer.MusicPlayerApplication.ACTION_RESUME;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.squareup.picasso.Picasso;

import java.io.IOException;

public class MusicPlayerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_PREV -> prevSong(MainActivity.musicPlayerService.currentSong);
            case ACTION_PAUSE -> pauseSong();
            case ACTION_RESUME -> resumeSong();
            case ACTION_NEXT -> nextSong(MainActivity.musicPlayerService.currentSong);
        }
    }

    private void prevSong(Song currentSong) {
        MainActivity.musicPlayerService.mediaPlayer.pause();
        Song newSong = ApiSongs.skipToPrevious(currentSong.getId());
        if (newSong == null) {
            MainActivity.musicPlayerService.isPlaying = false;
            MainActivity.buttonPlayPause.setImageResource(R.drawable.ic_play_arrow);
            MainActivity.musicPlayerService.sendNotificationMedia(currentSong);
            return;
        }
        try {
            MainActivity.linearLayoutMiniPlayer.setVisibility(View.VISIBLE);
            MainActivity.textViewSongName.setText(newSong.getSongName());
            MainActivity.textViewArtistName.setText(newSong.getArtistName());
            MainActivity.buttonPlayPause.setImageResource(R.drawable.ic_pause);
            Picasso.get().load(newSong.getSongImageUrl()).into(MainActivity.imageViewSong);
            MainActivity.musicPlayerService.mediaPlayer.reset();
            MainActivity.musicPlayerService.mediaPlayer.setDataSource(newSong.getUrl());
            MainActivity.musicPlayerService.mediaPlayer.prepareAsync();
            MainActivity.musicPlayerService.isPlaying = true;
            MainActivity.musicPlayerService.sendNotificationMedia(newSong);
            MainActivity.musicPlayerService.currentSong = newSong;
            MainActivity.updateRecyclerView(newSong);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void nextSong(Song currentSong) {
        MainActivity.musicPlayerService.mediaPlayer.pause();
        Song newSong = ApiSongs.skipToNext(currentSong.getId());
        if (newSong == null) {
            MainActivity.musicPlayerService.isPlaying = false;
            MainActivity.buttonPlayPause.setImageResource(R.drawable.ic_play_arrow);
            MainActivity.musicPlayerService.sendNotificationMedia(currentSong);
            return;
        }
        try {
            MainActivity.linearLayoutMiniPlayer.setVisibility(View.VISIBLE);
            MainActivity.textViewSongName.setText(newSong.getSongName());
            MainActivity.textViewArtistName.setText(newSong.getArtistName());
            MainActivity.buttonPlayPause.setImageResource(R.drawable.ic_pause);
            Picasso.get().load(newSong.getSongImageUrl()).into(MainActivity.imageViewSong);

            MainActivity.musicPlayerService.mediaPlayer.reset();
            MainActivity.musicPlayerService.mediaPlayer.setDataSource(newSong.getUrl());
            MainActivity.musicPlayerService.mediaPlayer.prepareAsync();
            MainActivity.musicPlayerService.isPlaying = true;
            MainActivity.musicPlayerService.sendNotificationMedia(newSong);
            MainActivity.musicPlayerService.currentSong = newSong;
            MainActivity.updateRecyclerView(newSong);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void resumeSong() {
        MainActivity.musicPlayerService.mediaPlayer.start();
        MainActivity.musicPlayerService.isPlaying = true;
        MainActivity.buttonPlayPause.setImageResource(R.drawable.ic_pause);
        MainActivity.musicPlayerService.sendNotificationMedia(MainActivity.musicPlayerService.currentSong);
    }

    private void pauseSong() {
        MainActivity.musicPlayerService.mediaPlayer.pause();
        MainActivity.musicPlayerService.isPlaying = false;
        MainActivity.buttonPlayPause.setImageResource(R.drawable.ic_play_arrow);
        MainActivity.musicPlayerService.sendNotificationMedia(MainActivity.musicPlayerService.currentSong);
    }
}
