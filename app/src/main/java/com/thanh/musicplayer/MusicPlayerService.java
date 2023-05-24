package com.thanh.musicplayer;

import static com.thanh.musicplayer.MusicPlayerApplication.ACTION_NEXT;
import static com.thanh.musicplayer.MusicPlayerApplication.ACTION_PAUSE;
import static com.thanh.musicplayer.MusicPlayerApplication.ACTION_PREV;
import static com.thanh.musicplayer.MusicPlayerApplication.ACTION_RESUME;
import static com.thanh.musicplayer.MusicPlayerApplication.CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class MusicPlayerService extends Service implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener {
    private final IBinder binder = new MusicBinder();
    private Runnable runnable;
    public MediaPlayer mediaPlayer = null;
    public boolean isPlaying;
    public boolean isShuffle;
    public int repeatMode = 0;
    public Song currentSong;
    public boolean min5 = false;
    public boolean min10 = false;
    public boolean min15 = false;

    public CountDownTimer countDownTimer;

    public class MusicBinder extends Binder {
        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

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
        return binder;
    }

    public void sendNotificationMedia(Song song) {
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this, "tag");

        Intent prevIntent = (new Intent(getBaseContext(), MusicPlayerReceiver.class)).setAction(ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = (new Intent(getBaseContext(), MusicPlayerReceiver.class)).setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent resumeIntent = (new Intent(getBaseContext(), MusicPlayerReceiver.class)).setAction(ACTION_RESUME);
        PendingIntent resumePendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = (new Intent(getBaseContext(), MusicPlayerReceiver.class)).setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Notification notification = new NotificationCompat.Builder(MusicPlayerService.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_music_note)
                        .setSound(null)
                        .setSubText(song.getArtistName())
                        .setContentTitle(song.getSongName())
                        .setContentText(song.getArtistName())
                        .setLargeIcon(bitmap)
                        .addAction(R.drawable.ic_skip_previous, "Previous", prevPendingIntent)
                        .addAction(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow, isPlaying ? "Pause" : "Play",
                                isPlaying ? pausePendingIntent : resumePendingIntent)
                        .addAction(R.drawable.ic_skip_next, "Next", nextPendingIntent)
                        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2)
                                .setMediaSession(mediaSessionCompat.getSessionToken())
                        ).build();

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

    public void seekBarSetup() {
        runnable = () -> {
            MainActivity.appCompatSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            if (PlayerActivity.isBound) {
                PlayerActivity.textViewCurrentPosition.setText(Utils.formatTime(mediaPlayer.getCurrentPosition()));
                PlayerActivity.appCompatSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            }
            new Handler(Looper.getMainLooper()).postDelayed(runnable, 1000);
        };
        new Handler(Looper.getMainLooper()).postDelayed(runnable, 0);
    }

    public void startTimer() {
        long milliseconds = 0;
        if (MainActivity.musicPlayerService.min5) milliseconds = 5000;
        if (MainActivity.musicPlayerService.min10) milliseconds = 10000;
        if (MainActivity.musicPlayerService.min15) milliseconds = 15000;
        countDownTimer = new CountDownTimer(milliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (min5 || min10 || min15) {
                    pauseSong();
                    if (PlayerActivity.isBound) {
                        PlayerActivity.buttonTimer.setImageResource(R.drawable.ic_alarm);
                        min5 = false;
                        min10 = false;
                        min15 = false;
                    }
                }
                countDownTimer = null;
            }
        }.start();

    }

    private void pauseSong() {
        mediaPlayer.pause();
        isPlaying = false;
        sendNotificationMedia(currentSong);
        MainActivity.buttonPlayPause.setImageResource(R.drawable.ic_play_arrow);
        PlayerActivity.buttonPlayPause.setImageResource(R.drawable.ic_play_circle);
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
