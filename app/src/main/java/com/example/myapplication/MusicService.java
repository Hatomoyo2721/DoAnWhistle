package com.example.myapplication;

import static com.example.myapplication.ApplicationClass.ACTION_NEXT;
import static com.example.myapplication.ApplicationClass.ACTION_PLAY;
import static com.example.myapplication.ApplicationClass.ACTION_PREVIOUS;
import static com.example.myapplication.PlayerActivity.listSongs;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.AtomicFile;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.security.Provider;
import java.util.ArrayList;

//27 - 02 - 2024
public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    //IBinder là giao thức kết nối giữa Server và 1 thành phần khác (thường là activity)
    IBinder myBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind", "Method");
        return myBinder;
    }

    //1 activity connect to services
    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        if (myPosition != -1) {
            playMedia(myPosition);
        }

        String actionName = intent.getStringExtra("ActionName");
        if (actionName != null) {
            switch (actionName) {

                case "playPause":
                    Toast.makeText(this, "PlayPause", Toast.LENGTH_SHORT).show();
                    if (actionPlaying != null) {
                        Log.e("Inside", "Action");
                        actionPlaying.playPauseBtnClicked();
                    }
                    break;

                case "Next":
                    Toast.makeText(this, "Next", Toast.LENGTH_SHORT).show();
                    if (actionPlaying != null) {
                        Log.e("Inside", "Action");
                        actionPlaying.nextBtnClicked();
                    }
                    break;

                case "Previous":
                    Toast.makeText(this, "Previous", Toast.LENGTH_SHORT).show();
                    if (actionPlaying != null) {
                        Log.e("Inside", "Action");
                        actionPlaying.prevBtnClicked();
                    }
                    break;

//            case ACTION_PLAY:
//                if (actionPlaying != null) {
//                    actionPlaying.playPauseBtnClicked();
//                }
//                break;
//
//            case ACTION_NEXT:
//                if (actionPlaying != null) {
//                    actionPlaying.nextBtnClicked();
//                }
//                break;
//
//            case ACTION_PREVIOUS:
//                if (actionPlaying != null) {
//                    actionPlaying.prevBtnClicked();
//                }
//                break;
            }
        }
        return START_STICKY;
//      return START_STICKY_COMPATIBILITY;
    }

    private void playMedia(int StartPosition) {
        musicFiles = listSongs;
        position = StartPosition;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (musicFiles != null) {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    void start() {
        mediaPlayer.start();
    }

    boolean isPlaying() {
        return  mediaPlayer.isPlaying();
    }

    void stop() {
        mediaPlayer.stop();
    }

    void release() {
        mediaPlayer.release();
    }

    void pause() {
        mediaPlayer.pause();
    }

    int getDuration() {
        return mediaPlayer.getDuration();
    }

    void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    void createMediaPlayer(int position) {
        uri = Uri.parse(musicFiles.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    void OnCompleted() {
        mediaPlayer.setOnCompletionListener(this);
    }


    // 3 / 3 / 2024
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked();
        }
        createMediaPlayer(position);
        mediaPlayer.start();
        OnCompleted();
    }

    void setCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }
}
