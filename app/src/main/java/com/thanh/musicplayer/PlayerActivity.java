package com.thanh.musicplayer;

import static com.thanh.musicplayer.MusicPlayerApplication.OBJECT_SONG;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {
    public static MusicPlayerService musicPlayerService;
    public static boolean isBound;
    Intent musicIntent;
    ImageButton buttonBack;
    public static ImageView imageViewSong;
    public static TextView textViewSongName, textViewArtistName;
    public static AppCompatSeekBar appCompatSeekBar;
    public static TextView textViewCurrentPosition, textViewMax;
    public static ImageButton buttonTimer;
    public static ImageButton buttonShuffle, buttonPrevious, buttonPlayPause, buttonNext, buttonRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        setControl();
        musicIntent = new Intent(this, MusicPlayerService.class);
        startService(musicIntent);
        bindService(musicIntent, serviceConnection, BIND_AUTO_CREATE);
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
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Song song;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            song = bundle.getSerializable(OBJECT_SONG, Song.class);
        } else {
            song = (Song) bundle.getSerializable(OBJECT_SONG);
        }
        Picasso.get().load(song.getSongImageUrl()).into(imageViewSong);
        textViewSongName.setText(song.getSongName());
        textViewArtistName.setText(song.getArtistName());
        appCompatSeekBar.setProgress(0);
        appCompatSeekBar.setMax(song.getLength() * 1000);
        buttonBack.setOnClickListener(v -> {
            finish();
        });
        buttonPlayPause.setOnClickListener(v -> {
            buttonPlayPauseClicked();
        });
        buttonShuffle.setOnClickListener(v -> {
            buttonShuffleClicked();
        });
        buttonNext.setOnClickListener(v -> {
            buttonNextClicked();
        });
        buttonPrevious.setOnClickListener(v -> {
            buttonPreviousClicked();
        });
        buttonRepeat.setOnClickListener(v -> {
            buttonRepeatClicked();
        });
        buttonTimer.setOnClickListener(v -> {
            if (MainActivity.musicPlayerService.min5 ||
                    MainActivity.musicPlayerService.min10 ||
                    MainActivity.musicPlayerService.min15)
                buttonTimerSetOrNotSetTimerClicked();
            else
                buttonTimerSetTimerClicked();
        });
    }

    private void setControl() {
        buttonBack = findViewById(R.id.buttonBack);
        imageViewSong = findViewById(R.id.imageViewSong);
        textViewSongName = findViewById(R.id.textViewSongName);
        textViewArtistName = findViewById(R.id.textViewArtistName);
        appCompatSeekBar = findViewById(R.id.seekBar);
        textViewCurrentPosition = findViewById(R.id.textViewCurrentPosition);
        textViewMax = findViewById(R.id.textViewMax);
        buttonShuffle = findViewById(R.id.buttonShuffle);
        buttonPrevious = findViewById(R.id.buttonPrevious);
        buttonPlayPause = findViewById(R.id.buttonPlayPause);
        buttonNext = findViewById(R.id.buttonNext);
        buttonRepeat = findViewById(R.id.buttonRepeat);
        buttonTimer = findViewById(R.id.buttonTimer);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBound = true;
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) service;
            musicPlayerService = binder.getService();
            musicPlayerService.seekBarSetup();
            textViewCurrentPosition.setText(Utils.formatTime(musicPlayerService.mediaPlayer.getCurrentPosition()));
            if (musicPlayerService.isPlaying) {
                buttonPlayPause.setImageResource(R.drawable.ic_pause_circle);
            } else {
                buttonPlayPause.setImageResource(R.drawable.ic_play_circle);
            }

            if (musicPlayerService.isShuffle) {
                buttonShuffle.setImageResource(R.drawable.ic_shuffle_on);
            } else {
                buttonShuffle.setImageResource(R.drawable.ic_shuffle);
            }

            switch (musicPlayerService.repeatMode) {
                case 0 -> buttonRepeat.setImageResource(R.drawable.ic_repeat);
                case 1 -> buttonRepeat.setImageResource(R.drawable.ic_repeat_1);
                case 2 -> buttonRepeat.setImageResource(R.drawable.ic_repeat_2);
            }

            if (MainActivity.musicPlayerService.min5 ||
                    MainActivity.musicPlayerService.min10 ||
                    MainActivity.musicPlayerService.min15) {
                buttonTimer.setImageResource(R.drawable.ic_alarm_on);
            } else {
                buttonTimer.setImageResource(R.drawable.ic_alarm);
            }
            textViewMax.setText(Utils.formatTime(musicPlayerService.currentSong.getLength() * 1000));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        stopService(musicIntent);
        unbindService(serviceConnection);
    }

    private void buttonPlayPauseClicked() {
        if (musicPlayerService.isPlaying) {
            musicPlayerService.isPlaying = false;
            musicPlayerService.mediaPlayer.pause();
            buttonPlayPause.setImageResource(R.drawable.ic_play_circle);
            musicPlayerService.sendNotificationMedia(musicPlayerService.currentSong);
            MainActivity.buttonPlayPause.setImageResource(R.drawable.ic_play_arrow);
        } else {
            musicPlayerService.isPlaying = true;
            musicPlayerService.mediaPlayer.start();
            buttonPlayPause.setImageResource(R.drawable.ic_pause_circle);
            musicPlayerService.sendNotificationMedia(musicPlayerService.currentSong);
            MainActivity.buttonPlayPause.setImageResource(R.drawable.ic_pause);
        }
    }

    private void buttonRepeatClicked() {
        musicPlayerService.repeatMode = (musicPlayerService.repeatMode + 1) % 3;
        switch (musicPlayerService.repeatMode) {
            case 0 -> buttonRepeat.setImageResource(R.drawable.ic_repeat);
            case 1 -> buttonRepeat.setImageResource(R.drawable.ic_repeat_1);
            case 2 -> buttonRepeat.setImageResource(R.drawable.ic_repeat_2);
        }
    }

    private void buttonPreviousClicked() {
        MainActivity.musicPlayerService.mediaPlayer.pause();
        Song newSong;
        if (MainActivity.musicPlayerService.isShuffle)
            newSong = ApiSongs.skipShuffle();
        else
            newSong = ApiSongs.skipToPrevious(musicPlayerService.currentSong.getId(), MainActivity.musicPlayerService.repeatMode);
        if (newSong == null) {
            MainActivity.musicPlayerService.isPlaying = false;
            MainActivity.buttonPlayPause.setImageResource(R.drawable.ic_play_arrow);
            MainActivity.musicPlayerService.sendNotificationMedia(MainActivity.musicPlayerService.currentSong);
            return;
        }
        try {
            MainActivity.linearLayoutMiniPlayer.setVisibility(View.VISIBLE);
            MainActivity.textViewSongName.setText(newSong.getSongName());
            MainActivity.textViewArtistName.setText(newSong.getArtistName());
            MainActivity.buttonPlayPause.setImageResource(R.drawable.ic_pause);
            MainActivity.appCompatSeekBar.setMax(newSong.getLength() * 1000);
            Picasso.get().load(newSong.getSongImageUrl()).into(MainActivity.imageViewSong);

            MainActivity.musicPlayerService.mediaPlayer.reset();
            MainActivity.musicPlayerService.mediaPlayer.setDataSource(newSong.getUrl());
            MainActivity.musicPlayerService.mediaPlayer.prepareAsync();
            MainActivity.musicPlayerService.isPlaying = true;
            MainActivity.musicPlayerService.sendNotificationMedia(newSong);
            MainActivity.musicPlayerService.currentSong = newSong;
            MainActivity.updateRecyclerView(newSong);
            if (PlayerActivity.isBound) {
                Picasso.get().load(newSong.getSongImageUrl()).into(PlayerActivity.imageViewSong);
                textViewSongName.setText(newSong.getSongName());
                textViewArtistName.setText(newSong.getArtistName());
                appCompatSeekBar.setMax(newSong.getLength() * 1000);
                textViewMax.setText(Utils.formatTime(newSong.getLength() * 1000));
                textViewCurrentPosition.setText(Utils.formatTime(0));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void buttonShuffleClicked() {
        if (musicPlayerService.isShuffle) {
            musicPlayerService.isShuffle = false;
            buttonShuffle.setImageResource(R.drawable.ic_shuffle);
        } else {
            musicPlayerService.isShuffle = true;
            buttonShuffle.setImageResource(R.drawable.ic_shuffle_on);
        }
    }

    private void buttonNextClicked() {
        MainActivity.musicPlayerService.mediaPlayer.pause();
        Song newSong;
        if (musicPlayerService.isShuffle)
            newSong = ApiSongs.skipShuffle();
        else
            newSong = ApiSongs.skipToNext(MainActivity.musicPlayerService.currentSong.getId(), musicPlayerService.repeatMode);
        if (newSong == null) {
            MainActivity.musicPlayerService.isPlaying = false;
            MainActivity.buttonPlayPause.setImageResource(R.drawable.ic_play_arrow);
            if (PlayerActivity.isBound) {
                PlayerActivity.buttonPlayPause.setImageResource(R.drawable.ic_play_circle);
            }
            MainActivity.musicPlayerService.sendNotificationMedia(MainActivity.musicPlayerService.currentSong);
            return;
        }
        try {
            MainActivity.linearLayoutMiniPlayer.setVisibility(View.VISIBLE);
            MainActivity.textViewSongName.setText(newSong.getSongName());
            MainActivity.textViewArtistName.setText(newSong.getArtistName());
            MainActivity.buttonPlayPause.setImageResource(R.drawable.ic_pause);
            MainActivity.appCompatSeekBar.setMax(newSong.getLength() * 1000);
            Picasso.get().load(newSong.getSongImageUrl()).into(MainActivity.imageViewSong);

            MainActivity.musicPlayerService.mediaPlayer.reset();
            MainActivity.musicPlayerService.mediaPlayer.setDataSource(newSong.getUrl());
            MainActivity.musicPlayerService.mediaPlayer.prepareAsync();
            MainActivity.musicPlayerService.isPlaying = true;
            MainActivity.musicPlayerService.sendNotificationMedia(newSong);
            MainActivity.musicPlayerService.currentSong = newSong;
            MainActivity.updateRecyclerView(newSong);
            if (PlayerActivity.isBound) {
                Picasso.get().load(newSong.getSongImageUrl()).into(PlayerActivity.imageViewSong);
                textViewSongName.setText(newSong.getSongName());
                textViewArtistName.setText(newSong.getArtistName());
                appCompatSeekBar.setMax(newSong.getLength() * 1000);
                textViewMax.setText(Utils.formatTime(newSong.getLength() * 1000));
                textViewCurrentPosition.setText(Utils.formatTime(0));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void buttonTimerSetTimerClicked() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.layout_bottom_timer_set);
        dialog.show();
        dialog.findViewById(R.id.min_5).setOnClickListener(v -> {
            Toast.makeText(getBaseContext(), "Music will stop after 5 minutes", Toast.LENGTH_SHORT).show();
            buttonTimer.setImageResource(R.drawable.ic_alarm_on);
            MainActivity.musicPlayerService.min5 = true;
            MainActivity.musicPlayerService.min10 = false;
            MainActivity.musicPlayerService.min15 = false;
            MainActivity.musicPlayerService.startTimer();
            dialog.dismiss();
        });
        dialog.findViewById(R.id.min_10).setOnClickListener(v -> {
            Toast.makeText(getBaseContext(), "Music will stop after 10 minutes", Toast.LENGTH_SHORT).show();
            buttonTimer.setImageResource(R.drawable.ic_alarm_on);
            MainActivity.musicPlayerService.min5 = false;
            MainActivity.musicPlayerService.min10 = true;
            MainActivity.musicPlayerService.min15 = false;
            MainActivity.musicPlayerService.startTimer();
            dialog.dismiss();
        });
        dialog.findViewById(R.id.min_15).setOnClickListener(v -> {
            Toast.makeText(getBaseContext(), "Music will stop after 15 minutes", Toast.LENGTH_SHORT).show();
            buttonTimer.setImageResource(R.drawable.ic_alarm_on);
            MainActivity.musicPlayerService.min5 = false;
            MainActivity.musicPlayerService.min10 = false;
            MainActivity.musicPlayerService.min15 = true;
            MainActivity.musicPlayerService.startTimer();
            dialog.dismiss();
        });
    }

    private void buttonTimerSetOrNotSetTimerClicked() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.layout_bottom_timer_set_or_not);
        dialog.show();
        dialog.findViewById(R.id.min_5).setOnClickListener(v -> {
            Toast.makeText(getBaseContext(), "Music will stop after 5 minutes", Toast.LENGTH_SHORT).show();
            buttonTimer.setImageResource(R.drawable.ic_alarm_on);
            MainActivity.musicPlayerService.min5 = true;
            MainActivity.musicPlayerService.min10 = false;
            MainActivity.musicPlayerService.min15 = false;
            MainActivity.musicPlayerService.startTimer();
            dialog.dismiss();
        });
        dialog.findViewById(R.id.min_10).setOnClickListener(v -> {
            Toast.makeText(getBaseContext(), "Music will stop after 10 minutes", Toast.LENGTH_SHORT).show();
            buttonTimer.setImageResource(R.drawable.ic_alarm_on);
            MainActivity.musicPlayerService.min5 = false;
            MainActivity.musicPlayerService.min10 = true;
            MainActivity.musicPlayerService.min15 = false;
            MainActivity.musicPlayerService.startTimer();
            dialog.dismiss();
        });
        dialog.findViewById(R.id.min_15).setOnClickListener(v -> {
            Toast.makeText(getBaseContext(), "Music will stop after 15 minutes", Toast.LENGTH_SHORT).show();
            buttonTimer.setImageResource(R.drawable.ic_alarm_on);
            MainActivity.musicPlayerService.min5 = false;
            MainActivity.musicPlayerService.min10 = false;
            MainActivity.musicPlayerService.min15 = true;
            MainActivity.musicPlayerService.startTimer();
            dialog.dismiss();
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(v -> {
            Toast.makeText(getBaseContext(), "Turn off the timer", Toast.LENGTH_SHORT).show();
            buttonTimer.setImageResource(R.drawable.ic_alarm);
            MainActivity.musicPlayerService.min5 = false;
            MainActivity.musicPlayerService.min10 = false;
            MainActivity.musicPlayerService.min15 = false;
            dialog.dismiss();
        });
    }
}