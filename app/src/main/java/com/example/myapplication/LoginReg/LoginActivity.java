package com.example.myapplication.LoginReg;

import android.content.Intent;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.Objects;

import kotlinx.coroutines.sync.SemaphoreImpl;

public class LoginActivity extends AppCompatActivity {

    EditText loginUsername, loginPassword;
    Button btnLogin;
    TextView registerView;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Objects.requireNonNull(getSupportActionBar()).hide();

        loginUsername = findViewById(R.id.Username);
        loginPassword = findViewById(R.id.Password);
        btnLogin = findViewById(R.id.btnLogin);
        registerView = findViewById(R.id.textViewSignUp);
        auth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateUsername() | !validatePassword()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập tên tài khoản", Toast.LENGTH_SHORT).show();
                }
                else {
                    checkUser();
                }
            }
        });

        registerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });
    }

    public Boolean validateUsername() {
        String username = loginUsername.getText().toString();
        if (username.isEmpty()) {
            loginUsername.setError("Tài khoản không thể trống");
            return false;
        }
        else {
            loginUsername.setError(null);
            return true;
        }
    }

    public Boolean validatePassword() {
        String password = loginPassword.getText().toString();
        if (password.isEmpty()) {
            loginPassword.setError("Mật khẩu không thể trống");
            return false;
        }
        else {
            loginPassword.setError(null);
            return true;
        }
    }

    public void checkUser() {
        String userUsername = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        if (!userUsername.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(userUsername).matches()) {
            if (!userPassword.isEmpty()) {
                auth.signInWithEmailAndPassword(userUsername, userPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "Đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
                        loginPassword.setError("Đăng nhập không hợp lệ");
                        loginPassword.requestFocus();
                    }
                });
            }
            else {
                loginPassword.setError("Đăng nhập không hợp lệ");
                loginPassword.requestFocus();
            }
        }
        else if (userUsername.isEmpty()) {
            loginUsername.setError("Tài khoản không tồn tại");
        }
        else {
            loginUsername.setError("Vui lý điền đầy đủ thông tin");
            loginUsername.requestFocus();
        }
    }
}