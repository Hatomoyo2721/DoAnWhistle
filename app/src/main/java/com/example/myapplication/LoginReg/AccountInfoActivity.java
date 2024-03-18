package com.example.myapplication.LoginReg;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AccountInfoActivity extends AppCompatActivity {
    Button btnLogout, userListView;
    HelperClass helperClass;
    TextView info_name, info_mail, info_username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        Objects.requireNonNull(getSupportActionBar()).hide();

        ImageView backBtn = findViewById(R.id.back_button_info);
        btnLogout = findViewById(R.id.logout_button_info);
        userListView = findViewById(R.id.user_list_info);
        info_name = findViewById(R.id.info_name);
        info_mail = findViewById(R.id.info_email);
        info_username = findViewById(R.id.info_username);

        helperClass = new HelperClass();

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
                finish();
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Lấy thông tin từ mỗi nút con
                        String name = userSnapshot.child("name").getValue(String.class);
                        String email = userSnapshot.child("email").getValue(String.class);
                        String username = userSnapshot.child("username").getValue(String.class);
                        String role = userSnapshot.child("role").getValue(String.class);

                        // Hiển thị thông tin trong TextView
                        info_name.setText(name);
                        info_mail.setText(email);
                        info_username.setText(username);

                        info_name.setText(name);
                        info_mail.setText(email);
                        info_username.setText(username);

                        if (Objects.equals(role, "admin")) {
                            userListView.setVisibility(View.VISIBLE);
                        } else {
                            userListView.setVisibility(View.GONE);
                        }

                        break;
                    }
                } else {
                    userListView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AccountInfoActivity", databaseError.getMessage());
                userListView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}