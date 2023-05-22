package com.thanh.musicplayer;

import static com.thanh.musicplayer.ApplicationConstants.BUNDLE_MUSIC_ACTION;
import static com.thanh.musicplayer.ApplicationConstants.BUNDLE_SONG;
import static com.thanh.musicplayer.ApplicationConstants.BUNDLE_STATUS_PLAYER;
import static com.thanh.musicplayer.ApplicationConstants.CHANNEL_ID;
import static com.thanh.musicplayer.ApplicationConstants.INTENT_DATA_TO_ACTIVITY;
import static com.thanh.musicplayer.ApplicationConstants.INTENT_MUSIC_ACTION;
import static com.thanh.musicplayer.ApplicationConstants.INTENT_MUSIC_ACTION_TO_SERVICE;
import static com.thanh.musicplayer.ApplicationConstants.LOG_MUSIC_PLAYER;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

public class MusicPlayerService extends Service implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    public static final int ACTION_PAUSE = 1;
    public static final int ACTION_RESUME = 2;
    public static final int ACTION_START = 3;
    public static final int ACTION_NEXT = 4;
    public static final int ACTION_PREV = 5;
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying;
    private Song currentSong;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
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
            Song song = (Song) bundle.get(BUNDLE_SONG);
            if (song != null) {
                currentSong = song;
                startSong(song);
                sendNotificationMedia(song);
            }
        }

        int action = intent.getIntExtra(INTENT_MUSIC_ACTION_TO_SERVICE, 0);
        handleAction(action);

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
                sendActionToActivity(ACTION_START);
            } catch (IOException e) {
                Log.e(LOG_MUSIC_PLAYER, e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    private void handleAction(int action) {
        switch (action) {
            case ACTION_PAUSE -> pauseSong();
            case ACTION_RESUME -> resumeSong();
            case ACTION_NEXT -> nextSong();
            case ACTION_PREV -> prevSong();
        }
    }

    private void resumeSong() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            sendNotificationMedia(currentSong);
            sendActionToActivity(ACTION_START);
        }
    }

    private void pauseSong() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            sendNotificationMedia(currentSong);
            sendActionToActivity(ACTION_START);
        }
    }

    private void nextSong() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            Song newSong = ApiSongs.skipToNext(currentSong.getId());
            if (newSong == null) {
                isPlaying = false;
                sendNotificationMedia(currentSong);
                sendActionToActivity(ACTION_PAUSE);
                return;
            }
            currentSong = newSong;
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(currentSong.getUrl());
                mediaPlayer.prepareAsync();
                isPlaying = true;
                sendActionToActivity(ACTION_START);
                sendNotificationMedia(newSong);
            } catch (IOException | IllegalStateException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void prevSong() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            Song newSong = ApiSongs.skipToPrevious(currentSong.getId());
            if (newSong == null) {
                isPlaying = false;
                sendNotificationMedia(currentSong);
                sendActionToActivity(ACTION_PAUSE);
                return;
            }
            currentSong = newSong;
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(currentSong.getUrl());
                mediaPlayer.prepareAsync();
                isPlaying = true;
                sendActionToActivity(ACTION_START);
                sendNotificationMedia(newSong);
            } catch (IOException | IllegalStateException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendNotificationMedia(Song song) {
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this, "tag");
        mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING,
                        mediaPlayer.getCurrentPosition(),
                        1.0f)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
        );
        mediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder()
                .putLong(MediaMetadata.METADATA_KEY_DURATION, song.getLength() * 1000L)
                .build());
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MusicPlayerService.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_music_note)
                        .setSound(null)
                        .setSubText(song.getArtistName())
                        .setContentTitle(song.getSongName())
                        .setContentText(song.getArtistName())
                        .setLargeIcon(bitmap)
                        .addAction(R.drawable.ic_skip_previous, "Previous",
                                actionPendingIntent(MusicPlayerService.this, ACTION_PREV))
                        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2)
                                .setMediaSession(mediaSessionCompat.getSessionToken())
                        );
                if (isPlaying) {
                    notificationBuilder.addAction(R.drawable.ic_pause, "Pause",
                            actionPendingIntent(MusicPlayerService.this, ACTION_PAUSE));
                } else {
                    notificationBuilder.addAction(R.drawable.ic_play_arrow, "Play",
                            actionPendingIntent(MusicPlayerService.this, ACTION_RESUME));
                }
                notificationBuilder.addAction(R.drawable.ic_skip_next, "Next",
                        actionPendingIntent(MusicPlayerService.this, ACTION_NEXT));

                Notification notification = notificationBuilder.build();
                startForeground(1, notification);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        Picasso.get().load(song.getSongImageUrl()).into(target);
    }

    private PendingIntent actionPendingIntent(Context context, int action) {
        Intent intent = new Intent(this, MusicPlayerReceiver.class);
        intent.putExtra(INTENT_MUSIC_ACTION, action);
        return PendingIntent.getBroadcast(context.getApplicationContext(), action, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void sendActionToActivity(int action) {
        Intent intent = new Intent(INTENT_DATA_TO_ACTIVITY);
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_SONG, currentSong);
        bundle.putBoolean(BUNDLE_STATUS_PLAYER, isPlaying);
        bundle.putInt(BUNDLE_MUSIC_ACTION, action);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextSong();
    }
}
