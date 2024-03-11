package com.example.myapplication.LoginReg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    EditText regName, regEmail, regUsername, regPassword, confirmPassword;
    TextView loginView;
    Button btnResgiter;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        regName = findViewById(R.id.inputName);
        regEmail = findViewById(R.id.inputEmail);
        regUsername = findViewById(R.id.inputUsername);
        regPassword = findViewById(R.id.inputPassword);
        confirmPassword = findViewById(R.id.inputConfirmPassword);
        loginView = findViewById(R.id.alreadyHaveAccount);
        btnResgiter = findViewById(R.id.btnRegister);

        btnResgiter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                String name = regName.getText().toString().trim();
                String email = regEmail.getText().toString().trim();
                String username = regUsername.getText().toString().trim();
                String password = regPassword.getText().toString().trim();
                String confirmPass = confirmPassword.getText().toString().trim();

                if (!password.equals(confirmPass)) {
                    Toast.makeText(RegisterActivity.this, "Password does not match", Toast.LENGTH_SHORT).show();
                    return; // Stop the registration process
                }

                HelperClass helperClass = new HelperClass(name, username, email, password, confirmPass);
                reference.child(username).setValue(helperClass);

                Toast.makeText(RegisterActivity.this, "Register successfully", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(RegisterActivity.this, Log.class);
                startActivity(i);
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
}