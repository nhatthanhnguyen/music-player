package com.thanh.musicplayer;

import static com.thanh.musicplayer.ApplicationConstants.INTENT_MUSIC_ACTION;
import static com.thanh.musicplayer.ApplicationConstants.INTENT_MUSIC_ACTION_TO_SERVICE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MusicPlayerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int action = intent.getIntExtra(INTENT_MUSIC_ACTION, 0);

        Intent intentService = new Intent(context, MusicPlayerService.class);
        intentService.putExtra(INTENT_MUSIC_ACTION_TO_SERVICE, action);

        context.startService(intentService);
    }
}
