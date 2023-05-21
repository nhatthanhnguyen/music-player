package com.thanh.musicplayer;

import static com.thanh.musicplayer.ApplicationConstants.INTENT_BUNDLE_SONG;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements OnItemClickedListener {
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SongAdapter(ApiSongs.songs, this));
    }

    @Override
    public void onSongItemClickedListener(Song song) {
        Intent intent = new Intent(this, MusicPlayerService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_BUNDLE_SONG, song);
        intent.putExtras(bundle);

        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}