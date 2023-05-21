package com.thanh.musicplayer;

import static com.thanh.musicplayer.ApplicationConstants.CHANNEL_ID;
import static com.thanh.musicplayer.ApplicationConstants.INTENT_BUNDLE_SONG;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MusicPlayerService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {
    private static final int ACTION_PAUSE = 1;
    private static final int ACTION_RESUME = 2;
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Song song = (Song) bundle.get(INTENT_BUNDLE_SONG);
            if (song != null) {
                startSong(song);
                sendNotification(song);
            }
        }

        return START_NOT_STICKY;
    }

    private void startSong(Song song) {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.reset();
                }
                mediaPlayer.setDataSource(song.getUrl());
                mediaPlayer.prepareAsync();
                isPlaying = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void resumeMusic() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    private void sendNotification(Song song) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_music_notification);
        remoteViews.setTextViewText(R.id.textViewSongName, song.getSongName());
        remoteViews.setTextViewText(R.id.textViewArtistName, song.getArtistName());
        remoteViews.setImageViewResource(R.id.buttonPlayPause, R.drawable.ic_pause);


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setCustomContentView(remoteViews)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentIntent(pendingIntent)
                .setSound(null)
                .build();

        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mediaPlayer != null) mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
    }
}
