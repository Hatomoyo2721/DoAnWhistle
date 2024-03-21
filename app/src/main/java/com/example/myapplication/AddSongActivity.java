package com.example.myapplication;

import android.content.Intent;
import android.content.UriMatcher;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Map;
import java.util.Objects;

public class AddSongActivity extends AppCompatActivity {
    EditText uploadName, uploadArtist, uploadSinger, uploadAlbum;
    Button saveBtn, uploadFile, uploadImage;
    String audioUrl, imageUrl;
    Uri uriAu, uriImg;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference ref = db.collection("Music");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);

        Objects.requireNonNull(getSupportActionBar()).hide();

        uploadArtist = findViewById(R.id.uploadArtistSong);
        uploadName = findViewById(R.id.uploadNameSong);
        uploadSinger = findViewById(R.id.uploadNameSinger);
        uploadAlbum = findViewById(R.id.uploadNameAlbum);
        saveBtn = findViewById(R.id.btn_save_songs);
        uploadFile = findViewById(R.id.btnSelectFile);
        uploadImage = findViewById(R.id.btnSelectImage);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {

                        if (o.getResultCode() == RESULT_OK) {
                            Intent intent = o.getData();
                            uriAu = intent.getData();
                            uploadFile.setText(uriAu.toString());
                        } else {
                            Toast.makeText(AddSongActivity.this, "No file selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Photo = new Intent(Intent.ACTION_GET_CONTENT);
                Photo.setType("audio/*");
                activityResultLauncher.launch(Photo);
            }
        });

        ActivityResultLauncher<Intent> imageActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {

                        if (o.getResultCode() == RESULT_OK) {
                            Intent intent = o.getData();
                            uriImg = intent.getData();
                        } else {
                            Toast.makeText(AddSongActivity.this, "No file selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                imageActivityResultLauncher.launch(intent);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void saveData() {
        String name = uploadName.getText().toString().trim();
        String artist = uploadArtist.getText().toString().trim();
        String singer = uploadSinger.getText().toString().trim();
        String album = uploadAlbum.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(artist) || TextUtils.isEmpty(singer) || uriImg == null) {
            Toast.makeText(AddSongActivity.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uriAu == null) {
            Toast.makeText(AddSongActivity.this, "Please select an audio file", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(artist) && !TextUtils.isEmpty(singer)) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Audio")
                    .child(uriAu.getLastPathSegment());
            storageReference.putFile(uriAu).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> audioUriTask = taskSnapshot.getStorage().getDownloadUrl();
                    audioUriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            audioUrl = uri.toString();

                            StorageReference imageStorageReference = FirebaseStorage.getInstance().getReference().child("Image")
                                    .child(uriImg.getLastPathSegment());
                            imageStorageReference.putFile(uriImg).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Task<Uri> imageUriTask = taskSnapshot.getStorage().getDownloadUrl();
                                    imageUriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            imageUrl = uri.toString();

                                            String duration = "";
                                            MusicFiles musicFiles = new MusicFiles(audioUrl, name, artist, duration, album);
                                            MediaPlayer mediaPlayer = MediaPlayer.create(AddSongActivity.this, uriAu);
                                            duration = String.valueOf(Integer.parseInt(String.valueOf(mediaPlayer.getDuration() / 1000)));

                                            ref.add(musicFiles).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Toast.makeText(AddSongActivity.this, "Thêm bài hát thành công", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }

    }
}