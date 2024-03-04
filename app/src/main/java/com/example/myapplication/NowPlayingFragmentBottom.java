package com.example.myapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NowPlayingFragmentBottom extends Fragment {

    ImageView nextBtn, albumArt;
    TextView artist, songName;
    FloatingActionButton playPauseBtn;
    View v;

    public NowPlayingFragmentBottom() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_now_playing_bottom,
                container, false);
        artist = v.findViewById(R.id.song_artist_miniPlayer);
        songName = v.findViewById(R.id.song_name_miniPlayer);
        albumArt = v.findViewById(R.id.bottom_album_art);
        nextBtn = v.findViewById(R.id.skip_next_bottom);
        playPauseBtn = v.findViewById(R.id.play_pause_miniPlayer);
        return v;
    }
}