package com.chernykh.moodmovieapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import com.chernykh.moodmovieapp.fragments.HistoryFragment;
import com.chernykh.moodmovieapp.utils.FirebaseAuthManager;
import com.chernykh.moodmovieapp.utils.SessionManager;
import com.google.firebase.auth.FirebaseUser;

public class HistoryActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView tvNavUserName, tvNavUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        drawerLayout = findViewById(R.id.drawer_layout);
        tvNavUserName = findViewById(R.id.tvNavUserName);
        tvNavUserEmail = findViewById(R.id.tvNavUserEmail);

        FirebaseUser user = FirebaseAuthManager.getInstance(this).getCurrentUser();
        if (user != null) {
            tvNavUserName.setText(user.getDisplayName() != null ?
                    user.getDisplayName() : "Пользователь");
            tvNavUserEmail.setText(user.getEmail());
        }

        findViewById(R.id.menu_home).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(HistoryActivity.this, MainActivity.class));
            finish();
        });

        findViewById(R.id.menu_my_movies).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(HistoryActivity.this, MyMoviesActivity.class));
            finish();
        });

        findViewById(R.id.menu_history).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.menu_logout).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            logoutUser();
        });

        HistoryFragment fragment = new HistoryFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void logoutUser() {
        FirebaseAuthManager authManager = FirebaseAuthManager.getInstance(this);
        authManager.logout();

        SessionManager sessionManager = new SessionManager(this);
        sessionManager.logout();

        Intent intent = new Intent(HistoryActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
    }
}