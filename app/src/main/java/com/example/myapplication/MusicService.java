package com.example.myapplication;

import static com.example.myapplication.ApplicationClass.ACTION_NEXT;
import static com.example.myapplication.ApplicationClass.ACTION_PLAY;
import static com.example.myapplication.ApplicationClass.ACTION_PREVIOUS;
import static com.example.myapplication.ApplicationClass.CHANNEL_ID;
import static com.example.myapplication.PlayerActivity.listSongs;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.AtomicFile;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;
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
    MediaSessionCompat mediaSessionCompat;
    private static final int NOTIFICATION_ID = 1;
    public static final String MUSIC_FILE_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";
    public static final String ACTION_PLAY_PAUSE_MS = "com.example.myapplication.ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT_MS = "com.example.myapplication.ACTION_NEXT";
    public static final String ACTION_PREVIOUS_MS = "com.example.myapplication.ACTION_PREVIOUS";

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    //1 activity connect to services
    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            Log.w("MusicService", "onStartCommand: Intent is null");
            return START_NOT_STICKY;
        }

        int myPosition = intent.getIntExtra("servicePosition", -1);
        if (myPosition != -1) {
            position = myPosition;
            playMedia(position);
            return START_NOT_STICKY;
        }

        String actionName = intent.getStringExtra("ActionName");
        if (actionName != null) {
            switch (actionName) {

                case "playPause":
                    playPauseBtnClicked();
                    break;

                case "Next":
                    nextBtnClicked();
                    break;

                case "Previous":
                    previousBtnClicked();
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void playMedia(int StartPosition) {
        musicFiles = listSongs;
        position = StartPosition;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        if (musicFiles != null && position < musicFiles.size()) {
            createMediaPlayer(position);
            if (mediaPlayer != null) {
                mediaPlayer.start();
            } else {
                Log.w("MusicService", "playMedia: mediaPlayer is null after creation");
            }
        } else {
            Log.e("MusicService", "musicFiles is empty or position is invalid");
        }
    }

    void start() {
        mediaPlayer.start();
    }

    boolean isPlaying() {
        return mediaPlayer.isPlaying();
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

    void createMediaPlayer(int positionInner) {
        position = positionInner;
        if (musicFiles != null && position < musicFiles.size()) {
            uri = Uri.parse(musicFiles.get(position).getPath());

            SharedPreferences.Editor editor = getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE).edit();
            editor.putString(MUSIC_FILE, uri.toString());
            editor.putString(ARTIST_NAME, musicFiles.get(position).getArtist());
            editor.putString(SONG_NAME, musicFiles.get(position).getTitle());
            editor.apply();

            mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
        } else {
            Log.e("MusicService", "musicFiles is empty or position is invalid");
        }
    }

    void OnCompleted() {
        mediaPlayer.setOnCompletionListener(this);
    }


    // 3 / 3 / 2024
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked();
//            if (mediaPlayer != null) {
//                createMediaPlayer(position);
//                mediaPlayer.start();
//                OnCompleted();
//            }
            if (position < musicFiles.size()) {
                createMediaPlayer(position);
                mediaPlayer.start();
                OnCompleted();
                notifySongChanged();
            } else {
                stopSelf();
            }
        }
    }

    private void notifySongChanged() {
        if (actionPlaying != null) {
            actionPlaying.onSongChanged();
        }
    }

    void setCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }

    @SuppressLint({"NotificationId0", "MissingPermission"})
    void showNotification(int playPauseBtn) throws IOException {

        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.
                getBroadcast(this, 0, prevIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.
                getBroadcast(this, 0, pauseIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.
                getBroadcast(this, 0, nextIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        byte[] picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = null;
        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.question_mark);
        }

        mediaSessionCompat = new MediaSessionCompat(this, "tag");

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_small_music)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .setLargeIcon(thumb)
                .addAction(R.drawable.baseline_skip_previous, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.baseline_skip_next, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .build();

        NotificationManagerCompat mangerCompat = NotificationManagerCompat.from(this);
        mangerCompat.notify(NOTIFICATION_ID, notification);
//        startForeground(NOTIFICATION_ID, notification);
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

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    void playPauseBtnClicked() {
        if (actionPlaying != null) {
            actionPlaying.playPauseBtnClicked();
            sendBroadCast(String.valueOf(new Intent(ACTION_PLAY_PAUSE_MS)));
        }
    }

    void previousBtnClicked() {
        if (actionPlaying != null) {
            actionPlaying.prevBtnClicked();
            sendBroadCast(String.valueOf(new Intent(ACTION_PREVIOUS_MS)));
        }
    }

    void nextBtnClicked() {
        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked();
            sendBroadCast(String.valueOf(new Intent(ACTION_NEXT_MS)));
        }
    }

    private void sendBroadCast(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
}

