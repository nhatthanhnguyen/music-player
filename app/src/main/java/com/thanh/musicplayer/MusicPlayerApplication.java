package com.thanh.musicplayer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MusicPlayerApplication extends Application {
    public static final String CHANNEL_ID = "channel";
    public static final String OBJECT_SONG = "object_song";
    public static final String BUNDLE_STATUS_PLAYER = "status_player";
    public static final String BUNDLE_MUSIC_ACTION = "music_action";
    public static final String LOG_MUSIC_PLAYER = "Music Player";
    public static final String INTENT_MUSIC_ACTION = "music_action";
    public static final String INTENT_MUSIC_ACTION_TO_SERVICE = "music_action_service";
    public static final String INTENT_DATA_TO_ACTIVITY = "data_to_activity";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_RESUME = "resume";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_PREV = "prev";

    @Override
    public void onCreate() {
        super.onCreate();
        createChannelNotification();
    }

    private void createChannelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Service Channel",
                    NotificationManager.IMPORTANCE_NONE
            );
            channel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
