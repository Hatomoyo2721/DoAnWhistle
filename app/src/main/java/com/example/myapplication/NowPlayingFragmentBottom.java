package com.example.myapplication;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;
import static com.example.myapplication.MainActivity.ARTIST_TO_FRAG;
import static com.example.myapplication.MainActivity.MUSIC_FILE_LAST_PLAYED;
import static com.example.myapplication.MainActivity.PATH_TO_FRAG;
import static com.example.myapplication.MainActivity.SHOW_MINI_PLAYER;
import static com.example.myapplication.MainActivity.SONG_NAME_TO_FRAG;
import static com.example.myapplication.MainActivity.musicFiles;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection {

    ImageView nextBtn, albumArt;
    TextView artist, songName;
    FloatingActionButton playPauseBtn;
    View rootView;
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
        rootView = inflater.inflate(R.layout.fragment_now_playing_bottom,
                container, false);
        intialViews();
        setClickListeners();

        return rootView;
    }

    private void intialViews() {
        artist = rootView.findViewById(R.id.song_artist_miniPlayer);
        songName = rootView.findViewById(R.id.song_name_miniPlayer);
        albumArt = rootView.findViewById(R.id.bottom_album_art);
        nextBtn = rootView.findViewById(R.id.skip_next_bottom);
        playPauseBtn = rootView.findViewById(R.id.play_pause_miniPlayer);
    }

    private void setClickListeners() {
        nextBtn.setOnClickListener(v -> handleNextButtonClick());
        playPauseBtn.setOnClickListener(v -> handlePlayPauseButtonClick());
    }

    private void handleNextButtonClick() {
        if (musicService != null) {
            musicService.nextBtnClicked();
            updateStoredMusicPreferences();
            updateMiniPlayerUI();
        }
    }

    private void handlePlayPauseButtonClick() {
        if (musicService != null) {
            musicService.playPauseBtnClicked();
            updatePlayPauseButtonUI();
        }
    }

    private void updateStoredMusicPreferences() {
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
        }
    }

    private void updateMiniPlayerUI() {
        if (SHOW_MINI_PLAYER) {
            if (PATH_TO_FRAG != null) {
                try {
                    byte[] art = getAlbumArt(PATH_TO_FRAG);
                    Glide.with(getContext()).load(art != null ? art : R.drawable.question_mark)
                            .into(albumArt);
                    songName.setText(SONG_NAME_TO_FRAG);
                    artist.setText(ARTIST_TO_FRAG);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updatePlayPauseButtonUI() {
        if (musicService.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.baseline_pause_black);
        } else {
            playPauseBtn.setImageResource(R.drawable.baseline_play_arrow_mini);
        }
    }

        @Override
        public void onResume() {
            super.onResume();
            try {
                if (SHOW_MINI_PLAYER) {
                    if (PATH_TO_FRAG != null) {
                        byte[] art = getAlbumArt(PATH_TO_FRAG);
                        if (art != null) {
                            Glide.with(this).load(art)
                                    .into(albumArt);
                        } else {
                            Glide.with(this).load(R.drawable.question_mark)
                                    .into(albumArt);
                        }
                        songName.setText(SONG_NAME_TO_FRAG);
                        artist.setText(ARTIST_TO_FRAG);
                        Intent intent = new Intent(getContext(), MusicService.class);
                        if (getContext() != null)
                            getContext().bindService(intent, this, BIND_AUTO_CREATE);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null && musicService != null) {
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