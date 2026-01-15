package com.chernykh.moodmovieapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.chernykh.moodmovieapp.utils.DataSyncManager;
import com.chernykh.moodmovieapp.utils.FirebaseAuthManager;
import com.chernykh.moodmovieapp.utils.SessionManager;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView tvNavUserName, tvNavUserEmail;
    private SearchView searchView;
    private DataSyncManager dataSyncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuthManager authManager = FirebaseAuthManager.getInstance(this);

        if (!authManager.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        FirebaseUser user = authManager.getCurrentUser();
        if (user != null) {
            dataSyncManager = new DataSyncManager(this, user.getUid());
            dataSyncManager.syncOnLogin(); // Загружаем данные из облака
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        tvNavUserName = findViewById(R.id.tvNavUserName);
        tvNavUserEmail = findViewById(R.id.tvNavUserEmail);
        searchView = findViewById(R.id.searchView);

        if (user != null) {
            tvNavUserName.setText(user.getDisplayName() != null ?
                    user.getDisplayName() : "Пользователь");
            tvNavUserEmail.setText(user.getEmail());
        }

        findViewById(R.id.menu_home).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.menu_my_movies).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, MyMoviesActivity.class));
        });

        findViewById(R.id.menu_history).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        });

        findViewById(R.id.menu_logout).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            logoutUser();
        });

        setupSearchView();

        Button btnStartTest = findViewById(R.id.btnStartTest);
        Button btnSkipTest = findViewById(R.id.btnSkipTest);

        btnStartTest.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("showTest", true);
            startActivity(intent);
        });

        btnSkipTest.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("showTest", false);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Закрываем соединения при выходе
        if (dataSyncManager != null) {
            dataSyncManager.close();
        }
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                openSearchActivity(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void openSearchActivity(String query) {
        if (query.isEmpty()) {
            Toast.makeText(this, "Введите название фильма", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        intent.putExtra("search_query", query);
        startActivity(intent);
    }

    private void logoutUser() {
        FirebaseAuthManager authManager = FirebaseAuthManager.getInstance(this);
        authManager.logout();

        SessionManager sessionManager = new SessionManager(this);
        sessionManager.logout();

        // Закрываем синхронизатор при выходе
        if (dataSyncManager != null) {
            dataSyncManager.close();
            dataSyncManager = null;
        }

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
    }
}