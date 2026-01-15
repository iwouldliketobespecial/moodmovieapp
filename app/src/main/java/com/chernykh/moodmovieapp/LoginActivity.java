package com.chernykh.moodmovieapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.chernykh.moodmovieapp.utils.FirebaseAuthManager;
import com.chernykh.moodmovieapp.utils.SessionManager;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuthManager authManager = FirebaseAuthManager.getInstance(this);
        sessionManager = new SessionManager(this);

        if (authManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvSwitchToRegister = findViewById(R.id.tvSwitchToRegister);

        if (sessionManager.hasSavedCredentials()) {
            etEmail.setText(sessionManager.getSavedEmail());
            etPassword.setText(sessionManager.getSavedPassword());
        }

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.login(email, password, new FirebaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    sessionManager.saveLoginData(email, password);

                    Toast.makeText(LoginActivity.this, "Вход успешен!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    String russianError;

                    if (error.contains("the supplied auth credential is incorrect") ||
                            error.contains("wrong-password") ||
                            error.contains("user-not-found")) {
                        russianError = "Неправильный email или пароль";
                    } else if (error.contains("invalid-email")) {
                        russianError = "Неверный формат email";
                    } else if (error.contains("network")) {
                        russianError = "Проблема с интернетом";
                    } else {
                        russianError = "Ошибка входа";
                    }

                    Toast.makeText(LoginActivity.this, russianError, Toast.LENGTH_LONG).show();
                }
            });
        });

        tvSwitchToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}