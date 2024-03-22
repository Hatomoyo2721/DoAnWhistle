package com.example.myapplication.LoginReg;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    EditText regName, regEmail, regUsername, regPassword, confirmPassword;
    TextView loginView;
    Button btnResgiter;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    String UserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Objects.requireNonNull(getSupportActionBar()).hide();

        regName = findViewById(R.id.inputName);
        regEmail = findViewById(R.id.inputEmail);
        regUsername = findViewById(R.id.inputUsername);
        regPassword = findViewById(R.id.inputPassword);
        confirmPassword = findViewById(R.id.inputConfirmPassword);
        loginView = findViewById(R.id.alreadyHaveAccount);
        btnResgiter = findViewById(R.id.btnRegister);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnResgiter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveData();
            }
        });

        loginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
    }

    private void saveData() {
        final String username = regUsername.getText().toString().trim();
        final String name = regName.getText().toString().trim();
        final String email = regEmail.getText().toString().trim();
        final String password = regPassword.getText().toString().trim();
        final String confirmPass = confirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(RegisterActivity.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPass)) {
            Toast.makeText(RegisterActivity.this, "Mật khẩu không trùng khớp", Toast.LENGTH_SHORT).show();
            return;
        } else if (password.length() <= 5) {
            Toast.makeText(RegisterActivity.this, "Mật khẩu ít nhất phải có 6 kí tự", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    UserID = mAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = firestore.collection("users").document(UserID);
                    Map<String, Object> users = new HashMap<>();
                    users.put("name", name);
                    users.put("username", username);
                    users.put("email", email);
                    users.put("password", password);
                    users.put("role", "User");
                    documentReference.set(users).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
        });
    }
}