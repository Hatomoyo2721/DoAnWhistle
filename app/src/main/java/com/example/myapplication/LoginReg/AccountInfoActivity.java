package com.example.myapplication.LoginReg;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.Objects;

public class AccountInfoActivity extends AppCompatActivity {
    Button btnLogout, userListView;
    HelperClass helperClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        Objects.requireNonNull(getSupportActionBar()).hide();

        ImageView backBtn = findViewById(R.id.back_button_info);
        btnLogout = findViewById(R.id.logout_button_info);
        userListView = findViewById(R.id.user_list_info);
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

        String role = helperClass.getRole();
        if (role != null && role.equals("admin")) {
            userListView.setVisibility(View.VISIBLE);
        } else {
            userListView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}