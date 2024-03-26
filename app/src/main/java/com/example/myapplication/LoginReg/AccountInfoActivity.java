package com.example.myapplication.LoginReg;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MusicService;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Objects;

public class AccountInfoActivity extends AppCompatActivity {
    Button userAddSong;
    TextView info_name, info_mail, info_username;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    String UserID;
    MusicService musicService;
    final String USER_DATA_KEY = "user_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        Objects.requireNonNull(getSupportActionBar()).hide();

        ImageView backBtn = findViewById(R.id.back_button_info);
        userAddSong = findViewById(R.id.user_add_song);
        info_name = findViewById(R.id.info_name);
        info_mail = findViewById(R.id.info_email);
        info_username = findViewById(R.id.info_username);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        UserID = auth.getCurrentUser().getUid();
        DocumentReference documentReference = firestore.collection("users").document(UserID);
        ListenerRegistration registration = documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                info_name.setText(value.getString("name"));
                info_mail.setText(value.getString("email"));
                info_username.setText(value.getString("username"));
            }
        });

        DocumentReference userDocRef = firestore.collection("users").document(UserID);
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String role = documentSnapshot.getString("role");
                if (role.equals("Admin")) {
                    userAddSong.setVisibility(View.VISIBLE);
                }
                else {
                    userAddSong.setVisibility(View.GONE);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        userAddSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(AccountInfoActivity.this, AddSongActivity.class);
//                startActivity(intent);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}