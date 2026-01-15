package com.chernykh.moodmovieapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import com.chernykh.moodmovieapp.R;
import com.chernykh.moodmovieapp.fragments.GenreFragment;
import com.chernykh.moodmovieapp.fragments.MoodTestFragment;
import com.chernykh.moodmovieapp.fragments.MoviesFragment;
import com.chernykh.moodmovieapp.utils.FirebaseAuthManager;
import com.chernykh.moodmovieapp.utils.SessionManager;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity
        implements MoodTestFragment.OnMoodSelectedListener,
        GenreFragment.OnGenreSelectedListener {

    private String currentMood = "neutral";
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupNavigationDrawer();

        boolean showTest = getIntent().getBooleanExtra("showTest", false);

        if (showTest) {
            showMoodTestFragment();
        } else {
            showQuickMoodSelection();
        }
    }

    private void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        TextView tvNavUserName = findViewById(R.id.tvNavUserName);
        TextView tvNavUserEmail = findViewById(R.id.tvNavUserEmail);

        FirebaseUser user = FirebaseAuthManager.getInstance(this).getCurrentUser();
        if (user != null) {
            tvNavUserName.setText(user.getDisplayName() != null ?
                    user.getDisplayName() : "Пользователь");
            tvNavUserEmail.setText(user.getEmail());
        }

        findViewById(R.id.menu_home).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        });

        findViewById(R.id.menu_my_movies).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(HomeActivity.this, MyMoviesActivity.class));
            finish();
        });

        findViewById(R.id.menu_history).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
            finish();
        });

        findViewById(R.id.menu_logout).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            logoutUser();
        });
    }

    private void navigateToMain() {
        startActivity(new Intent(HomeActivity.this, MainActivity.class));
        finish();
    }

    private void logoutUser() {
        FirebaseAuthManager authManager = FirebaseAuthManager.getInstance(this);
        authManager.logout();

        SessionManager sessionManager = new SessionManager(this);
        sessionManager.logout();

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
    }

    private void showMoodTestFragment() {
        MoodTestFragment fragment = new MoodTestFragment();
        fragment.setOnMoodSelectedListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void showQuickMoodSelection() {
        QuickMoodFragment fragment = new QuickMoodFragment();
        fragment.setOnMoodSelectedListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void showGenreFragment() {
        GenreFragment fragment = new GenreFragment();
        fragment.setCurrentMood(currentMood);
        fragment.setOnGenreSelectedListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack("genre");
        transaction.commit();
    }

    private void showMoviesFragment(String mood, String genre) {
        MoviesFragment fragment = new MoviesFragment();
        Bundle args = new Bundle();
        args.putString("mood", mood);
        args.putString("genre", genre);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack("movies");
        transaction.commit();
    }

    @Override
    public void onMoodSelected(String mood) {
        currentMood = mood;
        showGenreFragment();
    }

    @Override
    public void onGenreSelected(String mood, String genre) {
        showMoviesFragment(mood, genre);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public static class QuickMoodFragment extends androidx.fragment.app.Fragment {

        private MoodTestFragment.OnMoodSelectedListener listener;

        public void setOnMoodSelectedListener(MoodTestFragment.OnMoodSelectedListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater,
                                              android.view.ViewGroup container,
                                              Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(android.view.Gravity.CENTER);
            layout.setPadding(50, 50, 50, 50);

            TextView title = new TextView(getActivity());
            title.setText("Выберите настроение:");
            title.setTextSize(24);
            title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
            title.setGravity(android.view.Gravity.CENTER);
            layout.addView(title);

            String[] moods = {"😊 Веселый", "😐 Нейтральный", "😔 Грустный"};
            String[] moodValues = {"happy", "neutral", "sad"};

            for (int i = 0; i < moods.length; i++) {
                android.widget.Button btn = new android.widget.Button(getActivity());
                btn.setText(moods[i]);
                btn.setTextSize(18);
                btn.setPadding(30, 20, 30, 20);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 10, 0, 10);

                btn.setLayoutParams(params);

                final String mood = moodValues[i];
                btn.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onMoodSelected(mood);
                    }
                });

                layout.addView(btn);
            }

            return layout;
        }
    }
}