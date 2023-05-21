package com.thanh.musicplayer;

import static com.thanh.musicplayer.ApplicationConstants.BUNDLE_MUSIC_ACTION;
import static com.thanh.musicplayer.ApplicationConstants.BUNDLE_SONG;
import static com.thanh.musicplayer.ApplicationConstants.BUNDLE_STATUS_PLAYER;
import static com.thanh.musicplayer.ApplicationConstants.INTENT_DATA_TO_ACTIVITY;
import static com.thanh.musicplayer.ApplicationConstants.INTENT_MUSIC_ACTION;
import static com.thanh.musicplayer.MusicPlayerService.ACTION_PAUSE;
import static com.thanh.musicplayer.MusicPlayerService.ACTION_RESUME;
import static com.thanh.musicplayer.MusicPlayerService.ACTION_START;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements OnItemClickedListener {
    private RecyclerView recyclerView;
    private LinearLayout linearLayoutMiniPlayer;
    private ImageView imageViewSong;
    private TextView textViewSongName;
    private TextView textViewArtistName;
    private ImageButton buttonPlayPause;
    private Song currentSong;
    private boolean isPlaying;
    private Intent intent;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            currentSong = (Song) bundle.get(BUNDLE_SONG);
            isPlaying = (boolean) bundle.get(BUNDLE_STATUS_PLAYER);
            int action = (int) bundle.get(BUNDLE_MUSIC_ACTION);
            handleLayoutMusic(action);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT_DATA_TO_ACTIVITY));

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SongAdapter(ApiSongs.songs, this));

        linearLayoutMiniPlayer = findViewById(R.id.linearLayoutMiniPlayer);
        textViewSongName = findViewById(R.id.textViewSongName);
        textViewArtistName = findViewById(R.id.textViewArtistName);
        buttonPlayPause = findViewById(R.id.buttonPlayPause);
        imageViewSong = findViewById(R.id.imageViewSong);
    }

    @Override
    public void onSongItemClickedListener(Song song) {
        intent = new Intent(this, MusicPlayerService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_SONG, song);
        intent.putExtras(bundle);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void handleLayoutMusic(int action) {
        switch (action) {
            case ACTION_START -> {
                linearLayoutMiniPlayer.setVisibility(View.VISIBLE);
                showInformationForSong();
                setStatusButtonPlayPause();
            }
            case ACTION_RESUME, ACTION_PAUSE -> setStatusButtonPlayPause();
        }
    }

    private void showInformationForSong() {
        if (currentSong == null) {
            return;
        }
        Picasso.get().load(currentSong.getSongImageUrl()).into(imageViewSong);
        textViewSongName.setText(currentSong.getSongName());
        textViewArtistName.setText(currentSong.getArtistName());

        buttonPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                sendActionToService(ACTION_PAUSE);
            } else {
                sendActionToService(ACTION_RESUME);
            }
        });
    }

    private void setStatusButtonPlayPause() {
        if (isPlaying) {
            buttonPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            buttonPlayPause.setImageResource(R.drawable.ic_play_arrow);
        }
    }

    private void sendActionToService(int action) {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.putExtra(INTENT_MUSIC_ACTION, action);
        startService(intent);
    }
}