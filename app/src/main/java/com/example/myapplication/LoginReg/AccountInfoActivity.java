package com.example.myapplication.LoginReg;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.AddSongActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.MusicService;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.rpc.Help;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AccountInfoActivity extends AppCompatActivity {
    Button btnLogout, userAddSong;
    TextView info_name, info_mail, info_username;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    String UserID;
    MusicService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        Objects.requireNonNull(getSupportActionBar()).hide();

        ImageView backBtn = findViewById(R.id.back_button_info);
        btnLogout = findViewById(R.id.logout_button_info);
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

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(AccountInfoActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AccountInfoActivity.this, LoginActivity.class);
                startActivity(intent);
                musicService.stopSelf();
                finish();
            }
        });

        userAddSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountInfoActivity.this, AddSongActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}