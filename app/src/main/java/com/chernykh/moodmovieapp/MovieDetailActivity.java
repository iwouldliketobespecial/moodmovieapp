package com.chernykh.moodmovieapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.chernykh.moodmovieapp.R;
import com.chernykh.moodmovieapp.utils.DatabaseHelper;
import com.chernykh.moodmovieapp.utils.FirebaseAuthManager;
import com.chernykh.moodmovieapp.utils.FirebaseDataManager;
import com.chernykh.moodmovieapp.utils.SessionManager;
import com.google.firebase.auth.FirebaseUser;

public class MovieDetailActivity extends AppCompatActivity {

    private Button btnAddToFavorites;
    private String currentTitle;
    private DrawerLayout drawerLayout;
    private FirebaseDataManager firebaseDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        double ratingImdb = getIntent().getDoubleExtra("ratingImdb", 0.0);
        double ratingKp = getIntent().getDoubleExtra("ratingKp", 0.0);
        String genre = getIntent().getStringExtra("genre");
        String mood = getIntent().getStringExtra("mood");

        currentTitle = title;

        // Инициализация FirebaseDataManager если пользователь авторизован
        FirebaseUser user = FirebaseAuthManager.getInstance(this).getCurrentUser();
        if (user != null) {
            firebaseDataManager = new FirebaseDataManager(user.getUid());
        }

        TextView tvTitle = findViewById(R.id.tvMovieTitle);
        TextView tvDescription = findViewById(R.id.tvMovieDescription);
        TextView tvRatings = findViewById(R.id.tvMovieRatings);
        TextView tvGenre = findViewById(R.id.tvMovieGenre);
        TextView tvMood = findViewById(R.id.tvMovieMood);
        ImageView ivPoster = findViewById(R.id.ivMoviePoster);
        btnAddToFavorites = findViewById(R.id.btnAddToFavorites);

        tvTitle.setText(title);
        tvDescription.setText(description);

        String ratings = String.format("IMDb: %.1f/10 | Кинопоиск: %.1f/10", ratingImdb, ratingKp);
        tvRatings.setText(ratings);

        tvGenre.setText("Жанр: " + (genre != null ? genre : "Не указан"));
        tvMood.setText("Настроение: " + getMoodText(mood));

        ivPoster.setImageResource(getPosterResource(title));

        checkIfFavorite();

        btnAddToFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToFavorites(title, description, ratingImdb, ratingKp, genre, mood);
            }
        });

        saveToHistory(title, description, ratingImdb, ratingKp, genre, mood);

        setupNavigationDrawer();
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
            startActivity(new Intent(MovieDetailActivity.this, MainActivity.class));
            finish();
        });

        findViewById(R.id.menu_my_movies).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MovieDetailActivity.this, MyMoviesActivity.class));
            finish();
        });

        findViewById(R.id.menu_history).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MovieDetailActivity.this, HistoryActivity.class));
            finish();
        });

        findViewById(R.id.menu_logout).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            logoutUser();
        });
    }

    private void navigateToMain() {
        startActivity(new Intent(MovieDetailActivity.this, MainActivity.class));
        finish();
    }

    private void logoutUser() {
        FirebaseAuthManager authManager = FirebaseAuthManager.getInstance(this);
        authManager.logout();

        SessionManager sessionManager = new SessionManager(this);
        sessionManager.logout();

        Intent intent = new Intent(MovieDetailActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
    }

    private void checkIfFavorite() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        boolean isFavorite = dbHelper.isFavorite(currentTitle);
        dbHelper.close();

        if (isFavorite) {
            btnAddToFavorites.setText("✅ В избранном");
            btnAddToFavorites.setEnabled(false);
        }
    }

    private void addToFavorites(String title, String description,
                                double ratingImdb, double ratingKp,
                                String genre, String mood) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        boolean added = dbHelper.addToFavorites(title, description, ratingImdb, ratingKp, genre, mood);

        if (added) {
            Toast.makeText(this, "Фильм добавлен в избранное!", Toast.LENGTH_SHORT).show();
            btnAddToFavorites.setText("✅ В избранном");
            btnAddToFavorites.setEnabled(false);

            // ✅ СОХРАНЕНИЕ В ОБЛАКО
            if (firebaseDataManager != null) {
                firebaseDataManager.saveFavoriteToCloud(
                        FirebaseDataManager.createFavoriteData(
                                title, description, ratingImdb, ratingKp, genre, mood
                        )
                );
            }
        } else {
            Toast.makeText(this, "Ошибка добавления", Toast.LENGTH_SHORT).show();
        }
        dbHelper.close();
    }

    private String getMoodText(String mood) {
        switch (mood) {
            case "happy": return "Веселое";
            case "sad": return "Грустное";
            case "neutral": return "Нейтральное";
            default: return "Не определено";
        }
    }

    private int getPosterResource(String title) {
        // Веселое - Комедия
        if (title.contains("Трудный ребёнок")) return R.drawable.poster_problem_child;
        if (title.contains("Один дома")) return R.drawable.poster_home_alone;
        if (title.contains("Маска")) return R.drawable.poster_the_mask;
        if (title.contains("Тупой")) return R.drawable.poster_dumb_and_dumber;
        if (title.contains("Полицейский из Беверли")) return R.drawable.poster_beverly_hills_cop;

        // Веселое - Приключения
        if (title.contains("Пираты Карибского")) return R.drawable.poster_pirates_caribbean;
        if (title.contains("Индиана")) return R.drawable.poster_indiana_jones;
        if (title.contains("Назад в будущее")) return R.drawable.poster_back_to_future;
        if (title.contains("Парк Юрского")) return R.drawable.poster_jurassic_park;
        if (title.contains("Звёздные войны")) return R.drawable.poster_star_wars;

        // Веселое - Мюзикл
        if (title.contains("Поющие под дождём")) return R.drawable.poster_singing_in_rain;
        if (title.contains("Ла-Ла Ленд")) return R.drawable.poster_la_la_land;
        if (title.contains("Звуки музыки")) return R.drawable.poster_sound_of_music;
        if (title.contains("Величайший шоумен")) return R.drawable.poster_greatest_showman;
        if (title.contains("Чикаго")) return R.drawable.poster_chicago;

        // Веселое - Семейный
        if (title.contains("Гарри Поттер") && !title.contains("Азкабан")) return R.drawable.poster_harry_potter;
        if (title.contains("Король Лев")) return R.drawable.poster_lion_king;
        if (title.contains("Хроники Нарнии") && !title.contains("Принц")) return R.drawable.poster_narnia;
        if (title.contains("История игрушек")) return R.drawable.poster_toy_story;
        if (title.contains("Уоллес")) return R.drawable.poster_wallace_gromit;

        // Грустное - Драма
        if (title.contains("Зелёная миля")) return R.drawable.poster_green_mile;
        if (title.contains("Побег из Шоушенка")) return R.drawable.poster_shawshank_redemption;
        if (title.contains("Форрест Гамп")) return R.drawable.poster_forrest_gump;
        if (title.contains("Список Шиндлера")) return R.drawable.poster_schindlers_list;
        if (title.contains("Крёстный отец")) return R.drawable.poster_godfather;

        // Грустное - Мелодрама
        if (title.contains("Титаник")) return R.drawable.poster_titanic;
        if (title.contains("Великий Гэтсби")) return R.drawable.poster_great_gatsby;
        if (title.contains("Дневник памяти")) return R.drawable.poster_notebook;
        if (title.contains("Предложение")) return R.drawable.poster_proposal;
        if (title.contains("Город ангелов")) return R.drawable.poster_city_of_angels;

        // Грустное - Фэнтези
        if (title.contains("Властелин колец")) return R.drawable.poster_lord_of_rings;
        if (title.contains("Гарри Поттер") && title.contains("Азкабан")) return R.drawable.poster_harry_potter_prisoner;
        if (title.contains("Принц Каспиан")) return R.drawable.poster_narnia_caspian;
        if (title.contains("Пэн")) return R.drawable.poster_pan;
        if (title.contains("Стражи снов")) return R.drawable.poster_rise_of_guardians;

        // Грустное - Артхаус
        if (title.contains("Вечное сияние")) return R.drawable.poster_eternal_sunshine;
        if (title.contains("Амели")) return R.drawable.poster_amelie;
        if (title.contains("Аватар")) return R.drawable.poster_avatar;
        if (title.contains("Догвилль")) return R.drawable.poster_dogville;
        if (title.contains("Малхолланд")) return R.drawable.poster_mulholland_drive;

        // Нейтральное - Фантастика
        if (title.contains("Интерстеллар")) return R.drawable.poster_interstellar;
        if (title.contains("Матрица")) return R.drawable.poster_matrix;
        if (title.contains("Начало")) return R.drawable.poster_inception;
        if (title.contains("Чужой")) return R.drawable.poster_alien;
        if (title.contains("Пятый элемент")) return R.drawable.poster_fifth_element;

        // Нейтральное - Боевик
        if (title.contains("Тёмный рыцарь") || title.contains("Темный рыцарь")) return R.drawable.poster_dark_knight;
        if (title.contains("Гладиатор")) return R.drawable.poster_gladiator;
        if (title.contains("Крепкий орешек")) return R.drawable.poster_die_hard;
        if (title.contains("Терминатор 2")) return R.drawable.poster_terminator_2;
        if (title.contains("Миссия невыполнима")) return R.drawable.poster_mission_impossible;

        // Нейтральное - Детектив
        if (title.contains("Семь")) return R.drawable.poster_seven;
        if (title.contains("Молчание ягнят")) return R.drawable.poster_silence_lambs;
        if (title.contains("Достать ножи")) return R.drawable.poster_knives_out;
        if (title.contains("Шерлок Холмс")) return R.drawable.poster_sherlock_holmes;
        if (title.contains("Помни")) return R.drawable.poster_memento;

        // Нейтральное - Триллер
        if (title.contains("Остров проклятых")) return R.drawable.poster_shutter_island;
        if (title.contains("Игра")) return R.drawable.poster_game;
        if (title.contains("Сплит")) return R.drawable.poster_split;
        if (title.contains("Паразиты")) return R.drawable.poster_parasite;
        if (title.contains("Звонок")) return R.drawable.poster_ring;

        return android.R.drawable.ic_menu_gallery;
    }

    private void saveToHistory(String title, String description,
                               double ratingImdb, double ratingKp,
                               String genre, String mood) {

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.addToHistory(title, description, ratingImdb, ratingKp, genre, mood);
        dbHelper.close();

        if (firebaseDataManager != null) {
            firebaseDataManager.saveHistoryToCloud(
                    FirebaseDataManager.createHistoryData(
                            title, description, ratingImdb, ratingKp, genre, mood
                    )
            );
        }
    }
}