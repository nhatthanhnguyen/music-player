package com.thanh.musicplayer;

import static com.thanh.musicplayer.MusicPlayerApplication.OBJECT_SONG;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnItemClickedListener, MediaPlayer.OnCompletionListener {
    public static RecyclerView recyclerView;
    public static LinearLayout linearLayoutMiniPlayer;
    public static ImageView imageViewSong;
    public static TextView textViewSongName;
    public static TextView textViewArtistName;
    public static ImageButton buttonPlayPause;
    public static AppCompatSeekBar appCompatSeekBar;
    public static List<Song> songs = new ArrayList<>();
    public static SongAdapter songAdapter;
    Intent musicIntent;
    public static MusicPlayerService musicPlayerService;
    boolean isBound;

    @SuppressLint("NotifyDataSetChanged")
    public static void updateRecyclerView(Song currentSong) {
        for (Song song : songs) {
            song.setSelected(song.getId().equals(currentSong.getId()));
        }
        songAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        musicIntent = new Intent(this, MusicPlayerService.class);
        startService(musicIntent);
        bindService(musicIntent, serviceConnection, BIND_AUTO_CREATE);

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
        appCompatSeekBar = findViewById(R.id.seekBar);
        appCompatSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicPlayerService.mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        linearLayoutMiniPlayer.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlayerActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(OBJECT_SONG, musicPlayerService.currentSong);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        buttonPlayPause.setOnClickListener(v -> {
            if (musicPlayerService.isPlaying) {
                buttonPlayPause.setImageResource(R.drawable.ic_play_arrow);
                musicPlayerService.mediaPlayer.pause();
                musicPlayerService.isPlaying = false;
                musicPlayerService.sendNotificationMedia(musicPlayerService.currentSong);
                if (PlayerActivity.isBound) {
                    PlayerActivity.buttonPlayPause.setImageResource(R.drawable.ic_play_circle);
                }
            } else {
                buttonPlayPause.setImageResource(R.drawable.ic_pause);
                musicPlayerService.mediaPlayer.start();
                musicPlayerService.isPlaying = true;
                musicPlayerService.sendNotificationMedia(musicPlayerService.currentSong);
                if (PlayerActivity.isBound) {
                    PlayerActivity.buttonPlayPause.setImageResource(R.drawable.ic_pause_circle);
                }
            }
        });
    }

    private void fetchSongs() {
        songs = ApiSongs.songs;
    }

    @Override
    public void onSongItemClickedListener(Song song) {
        try {
            linearLayoutMiniPlayer.setVisibility(View.VISIBLE);
            textViewSongName.setText(song.getSongName());
            textViewArtistName.setText(song.getArtistName());
            Picasso.get().load(song.getSongImageUrl()).into(imageViewSong);
            musicPlayerService.mediaPlayer.reset();
            musicPlayerService.mediaPlayer.setDataSource(song.getUrl());
            musicPlayerService.mediaPlayer.prepareAsync();
            musicPlayerService.isPlaying = true;
            musicPlayerService.currentSong = song;
            musicPlayerService.sendNotificationMedia(song);
            appCompatSeekBar.setProgress(0);
            appCompatSeekBar.setMax(song.getLength() * 1000);
            updateRecyclerView(song);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(musicIntent);
        unbindService(serviceConnection);
    }

//    @Override
//    protected void onStop() {
//        Log.d("Main Activity", "Stoppppppppppppppppppppp");
//        super.onStop();
//        stopService(musicIntent);
//        unbindService(serviceConnection);
//    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) service;
            musicPlayerService = binder.getService();
            musicPlayerService.seekBarSetup();
            musicPlayerService.mediaPlayer.setOnCompletionListener(MainActivity.this);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    public void onCompletion(MediaPlayer mp) {
        musicPlayerService.mediaPlayer.pause();
        Song newSong;
        if (musicPlayerService.isShuffle)
            newSong = ApiSongs.skipShuffle();
        else
            newSong = ApiSongs.skipToNext(musicPlayerService.currentSong.getId(), musicPlayerService.repeatMode);
        if (musicPlayerService.repeatMode == 2) {
            newSong = musicPlayerService.currentSong;
        }
        if (newSong == null) {
            musicPlayerService.isPlaying = false;
            buttonPlayPause.setImageResource(R.drawable.ic_play_arrow);
            musicPlayerService.sendNotificationMedia(musicPlayerService.currentSong);
            return;
        }
        try {
            linearLayoutMiniPlayer.setVisibility(View.VISIBLE);
            textViewSongName.setText(newSong.getSongName());
            textViewArtistName.setText(newSong.getArtistName());
            buttonPlayPause.setImageResource(R.drawable.ic_pause);
            Picasso.get().load(newSong.getSongImageUrl()).into(MainActivity.imageViewSong);

            musicPlayerService.mediaPlayer.reset();
            musicPlayerService.mediaPlayer.setDataSource(newSong.getUrl());
            musicPlayerService.mediaPlayer.prepareAsync();
            musicPlayerService.isPlaying = true;
            musicPlayerService.sendNotificationMedia(newSong);
            musicPlayerService.currentSong = newSong;
            updateRecyclerView(newSong);
            if (PlayerActivity.isBound) {
                Picasso.get().load(newSong.getSongImageUrl()).into(PlayerActivity.imageViewSong);
                PlayerActivity.textViewSongName.setText(newSong.getSongName());
                PlayerActivity.textViewArtistName.setText(newSong.getArtistName());
                PlayerActivity.appCompatSeekBar.setMax(newSong.getLength() * 1000);
                PlayerActivity.textViewMax.setText(Utils.formatTime(newSong.getLength() * 1000));
                PlayerActivity.textViewCurrentPosition.setText(Utils.formatTime(0));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}