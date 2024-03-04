package com.example.myapplication;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;
import static com.example.myapplication.MainActivity.ARTIST_TO_FRAG;
import static com.example.myapplication.MainActivity.MUSIC_FILE_LAST_PLAYED;
import static com.example.myapplication.MainActivity.PATH_TO_FRAG;
import static com.example.myapplication.MainActivity.SHOW_MINI_PLAYER;
import static com.example.myapplication.MainActivity.SONG_NAME_TO_FRAG;
import static com.example.myapplication.MainActivity.musicFiles;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection {

    ImageView nextBtn, albumArt;
    TextView artist, songName;
    FloatingActionButton playPauseBtn;
    View v;
    MusicService musicService;
    public static final String MUSIC_FILE_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";

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

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getContext(), "Next", Toast.LENGTH_SHORT).show();
                if (musicService != null) {
                    musicService.nextBtnClicked();
                    if (getActivity() != null) {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE).edit();
                        editor.putString(MUSIC_FILE, musicService.musicFiles.get(musicService.position).getPath());
                        editor.putString(ARTIST_NAME, musicService.musicFiles.get(musicService.position).getArtist());
                        editor.putString(SONG_NAME, musicService.musicFiles.get(musicService.position).getTitle());
                        editor.apply();

                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE);
                        String path = preferences.getString(MUSIC_FILE, null);
                        String artistName = preferences.getString(ARTIST_NAME, null);
                        String song_name = preferences.getString(SONG_NAME, null);
                        if (path != null) {
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            ARTIST_TO_FRAG = artistName;
                            SONG_NAME_TO_FRAG = song_name;
                        }
                        else {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            ARTIST_TO_FRAG = null;
                            SONG_NAME_TO_FRAG = null;
                        }

                        try {
                            if (SHOW_MINI_PLAYER) {
                                if (PATH_TO_FRAG != null) {
                                    byte[] art = getAlbumArt(PATH_TO_FRAG);
                                    if (art != null) {
                                        Glide.with(getContext()).load(art)
                                                .into(albumArt);
                                    }
                                    else {
                                        Glide.with(getContext()).load(R.drawable.question_mark)
                                                .into(albumArt);
                                    }
                                    songName.setText(SONG_NAME_TO_FRAG);
                                    artist.setText(ARTIST_TO_FRAG);
                                }
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getContext(), "Run / Pause", Toast.LENGTH_SHORT).show();
                if (musicService != null) {
                    musicService.playPauseBtnClicked();
                    if (musicService.isPlaying()) {
                        playPauseBtn.setImageResource(R.drawable.baseline_pause_black);
                    }
                    else {
                        playPauseBtn.setImageResource(R.drawable.baseline_play_arrow_mini);
                    }
                }
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (SHOW_MINI_PLAYER) {
                if (PATH_TO_FRAG != null) {
                    byte[] art = getAlbumArt(PATH_TO_FRAG);
                    if (art != null) {
                        Glide.with(getContext()).load(art)
                                .into(albumArt);
                    }
                    else {
                        Glide.with(getContext()).load(R.drawable.question_mark)
                                .into(albumArt);
                    }
                    songName.setText(SONG_NAME_TO_FRAG);
                    artist.setText(ARTIST_TO_FRAG);
                    Intent intent = new Intent(getContext(), MusicService.class);
                    if (getContext() != null) {
                        getContext().bindService(intent, this, BIND_AUTO_CREATE);
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null) {
            getContext().unbindService(this);
        }
    }

    private byte[] getAlbumArt(String s) throws IOException {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(s);
            byte[] art = retriever.getEmbeddedPicture();
            retriever.release();

            if (art != null) {
                return art;
            }
            return null;

        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;

    }
}