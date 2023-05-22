package com.thanh.musicplayer;

import static com.thanh.musicplayer.ApplicationConstants.BUNDLE_MUSIC_ACTION;
import static com.thanh.musicplayer.ApplicationConstants.BUNDLE_SONG;
import static com.thanh.musicplayer.ApplicationConstants.BUNDLE_STATUS_PLAYER;
import static com.thanh.musicplayer.ApplicationConstants.INTENT_DATA_TO_ACTIVITY;
import static com.thanh.musicplayer.ApplicationConstants.INTENT_MUSIC_ACTION_TO_SERVICE;
import static com.thanh.musicplayer.MusicPlayerService.ACTION_NEXT;
import static com.thanh.musicplayer.MusicPlayerService.ACTION_PAUSE;
import static com.thanh.musicplayer.MusicPlayerService.ACTION_PREV;
import static com.thanh.musicplayer.MusicPlayerService.ACTION_RESUME;
import static com.thanh.musicplayer.MusicPlayerService.ACTION_START;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnItemClickedListener {
    private RecyclerView recyclerView;
    private LinearLayout linearLayoutMiniPlayer;
    private ImageView imageViewSong;
    private TextView textViewSongName;
    private TextView textViewArtistName;
    private ImageButton buttonPlayPause;
    private List<Song> songs = new ArrayList<>();
    private SongAdapter songAdapter;
    private Song currentSong;
    private boolean isPlaying;
    private Intent musicIntent;
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
            updateRecyclerView(currentSong);
            handleLayoutMusic(action);
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    private void updateRecyclerView(Song currentSong) {
        for (Song song : songs) {
            song.setSelected(song.getId().equals(currentSong.getId()));
        }
        songAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT_DATA_TO_ACTIVITY));

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fetchSongs();
        songAdapter = new SongAdapter(songs, this);
        recyclerView.setAdapter(songAdapter);

        linearLayoutMiniPlayer = findViewById(R.id.linearLayoutMiniPlayer);
        textViewSongName = findViewById(R.id.textViewSongName);
        textViewArtistName = findViewById(R.id.textViewArtistName);
        buttonPlayPause = findViewById(R.id.buttonPlayPause);
        imageViewSong = findViewById(R.id.imageViewSong);
    }

    private void fetchSongs() {
        songs = ApiSongs.songs;
    }

    @Override
    public void onSongItemClickedListener(Song song) {
        musicIntent = new Intent(this, MusicPlayerService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_SONG, song);
        musicIntent.putExtras(bundle);
        startService(musicIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(musicIntent);
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
            case ACTION_NEXT, ACTION_PREV -> showInformationForSong();
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
        intent.putExtra(INTENT_MUSIC_ACTION_TO_SERVICE, action);
        startService(intent);
    }
}