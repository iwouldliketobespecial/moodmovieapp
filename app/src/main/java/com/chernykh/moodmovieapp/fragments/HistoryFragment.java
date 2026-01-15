package com.chernykh.moodmovieapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chernykh.moodmovieapp.R;
import com.chernykh.moodmovieapp.MovieDetailActivity;
import com.chernykh.moodmovieapp.adapters.HistoryAdapter;
import com.chernykh.moodmovieapp.adapters.RecommendationAdapter;
import com.chernykh.moodmovieapp.models.Movie;
import com.chernykh.moodmovieapp.utils.DatabaseHelper;
import com.chernykh.moodmovieapp.utils.FirebaseAuthManager;
import com.chernykh.moodmovieapp.utils.FirebaseDataManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class HistoryFragment extends Fragment {
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private RecyclerView rvHistory, rvRecommendations;
    private TextView tvEmptyHistory, tvRecommendationsTitle;
    private Button btnClearHistory, btnGetRecommendations;

    private List<Movie> recommendedMovies = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        rvHistory = view.findViewById(R.id.rvHistory);
        rvRecommendations = view.findViewById(R.id.rvRecommendations);
        tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory);
        tvRecommendationsTitle = view.findViewById(R.id.tvRecommendationsTitle);
        btnClearHistory = view.findViewById(R.id.btnClearHistory);
        btnGetRecommendations = view.findViewById(R.id.btnGetRecommendations);

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecommendations.setLayoutManager(new LinearLayoutManager(getContext()));

        loadHistory(dbHelper);

        btnClearHistory.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuthManager.getInstance(getContext()).getCurrentUser();
            if (user != null) {
                FirebaseDataManager firebaseDataManager = new FirebaseDataManager(user.getUid());
                firebaseDataManager.getHistoryFromCloud(new FirebaseDataManager.FirestoreCallback() {
                    @Override
                    public void onSuccess(List<Map<String, Object>> cloudHistory) {
                        for (Map<String, Object> item : cloudHistory) {
                            String documentId = (String) item.get("id");
                            if (documentId != null) {
                                firestore.collection("users").document(user.getUid()).collection("history").document(documentId).delete();
                            }
                        }
                        dbHelper.clearHistory();
                        loadHistory(dbHelper);
                        hideRecommendations();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        dbHelper.clearHistory();
                        loadHistory(dbHelper);
                        hideRecommendations();
                    }
                });
            } else {
                dbHelper.clearHistory();
                loadHistory(dbHelper);
                hideRecommendations();
            }
        });

        btnGetRecommendations.setOnClickListener(v -> {
            showRecommendations(dbHelper);
        });

        return view;
    }

    private void loadHistory(DatabaseHelper dbHelper) {
        List<DatabaseHelper.HistoryItem> history = dbHelper.getHistory();

        if (history.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
            tvEmptyHistory.setText("История просмотров пуста");
        } else {
            tvEmptyHistory.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);

            HistoryAdapter adapter = new HistoryAdapter(
                    getContext(),
                    history,
                    item -> openMovieDetails(item)
            );

            rvHistory.setAdapter(adapter);
        }
    }

    private void showRecommendations(DatabaseHelper dbHelper) {
        String favoriteGenre = dbHelper.getMostWatchedGenre();

        if (favoriteGenre.isEmpty()) {
            hideRecommendations();
            return;
        }

        List<DatabaseHelper.HistoryItem> history = dbHelper.getHistory();
        Set<String> watchedTitles = new HashSet<>();

        for (DatabaseHelper.HistoryItem item : history) {
            watchedTitles.add(item.getTitle().toLowerCase());
        }

        recommendedMovies.clear();

        if (favoriteGenre.equals("Комедия")) {
            addMovieIfNotWatched("Трудный ребёнок (1990)", "Озорной мальчик устраивает хаос в семье", 5.9, 7.1, watchedTitles);
            addMovieIfNotWatched("Один дома (1990)", "Мальчик защищает дом от грабителей на Рождество", 7.7, 8.0, watchedTitles);
            addMovieIfNotWatched("Маска (1994)", "Робкий банковский служащий находит маску с магическими силами", 6.9, 7.4, watchedTitles);
            addMovieIfNotWatched("Тупой и ещё тупее (1994)", "Два друга отправляются в путешествие", 7.3, 7.7, watchedTitles);
            addMovieIfNotWatched("Полицейский из Беверли-Хиллз (1984)", "Детектив из Детройта в Беверли-Хиллз", 7.4, 7.8, watchedTitles);
        } else if (favoriteGenre.equals("Драма")) {
            addMovieIfNotWatched("Зелёная миля (1999)", "Надзиратель узнаёт о даре заключённого", 8.6, 9.1, watchedTitles);
            addMovieIfNotWatched("Побег из Шоушенка (1994)", "Банкир планирует побег из тюрьмы", 9.3, 9.2, watchedTitles);
            addMovieIfNotWatched("Форрест Гамп (1994)", "Человек становится свидетелем истории", 8.8, 9.0, watchedTitles);
            addMovieIfNotWatched("Список Шиндлера (1993)", "Бизнесмен спасает евреев", 9.0, 8.9, watchedTitles);
            addMovieIfNotWatched("Крёстный отец (1972)", "Патриарх мафии передаёт дело сыну", 9.2, 9.0, watchedTitles);
        } else if (favoriteGenre.equals("Фантастика")) {
            addMovieIfNotWatched("Интерстеллар (2014)", "Путешествие через червоточину", 8.6, 8.9, watchedTitles);
            addMovieIfNotWatched("Матрица (1999)", "Хакер узнаёт, что реальность - симуляция", 8.7, 8.8, watchedTitles);
            addMovieIfNotWatched("Начало (2010)", "Вор внедряет идеи через сны", 8.8, 8.7, watchedTitles);
            addMovieIfNotWatched("Чужой (1979)", "Экипаж сталкивается с инопланетянином", 8.5, 8.7, watchedTitles);
            addMovieIfNotWatched("Пятый элемент (1997)", "Таксист помогает спасти мир", 7.7, 8.0, watchedTitles);
        } else if (favoriteGenre.equals("Боевик")) {
            addMovieIfNotWatched("Тёмный рыцарь (2008)", "Бэтмен против Джокера", 9.0, 9.0, watchedTitles);
            addMovieIfNotWatched("Гладиатор (2000)", "Генерал становится гладиатором", 8.5, 8.7, watchedTitles);
            addMovieIfNotWatched("Крепкий орешек (1988)", "Полицейский спасает заложников", 8.2, 8.4, watchedTitles);
            addMovieIfNotWatched("Терминатор 2 (1991)", "Киберорганизм защищает мальчика", 8.6, 8.6, watchedTitles);
            addMovieIfNotWatched("Миссия невыполнима (1996)", "Агент очищает своё имя", 7.1, 7.3, watchedTitles);
        } else if (favoriteGenre.equals("Триллер")) {
            addMovieIfNotWatched("Остров проклятых (2010)", "Сыщик расследует исчезновение", 8.2, 8.4, watchedTitles);
            addMovieIfNotWatched("Семь (1995)", "Детективы выслеживают убийцу", 8.6, 8.8, watchedTitles);
            addMovieIfNotWatched("Игра (1997)", "Банкир попадает в опасную игру", 7.8, 8.0, watchedTitles);
            addMovieIfNotWatched("Сплит (2016)", "Мужчина с 23 личностями похищает девушек", 7.3, 7.5, watchedTitles);
            addMovieIfNotWatched("Паразиты (2019)", "Бедная семья внедряется в богатый дом", 8.6, 8.8, watchedTitles);
        } else if (favoriteGenre.equals("Мелодрама")) {
            addMovieIfNotWatched("Титаник (1997)", "Аристократка влюбляется в художника", 7.9, 8.4, watchedTitles);
            addMovieIfNotWatched("Дневник памяти (2004)", "Пожилой мужчина читает историю любви", 7.8, 8.1, watchedTitles);
            addMovieIfNotWatched("Великий Гэтсби (2013)", "Миллионер надеется вернуть любовь", 7.2, 7.5, watchedTitles);
            addMovieIfNotWatched("Предложение (2009)", "Редактор вынуждена выйти замуж", 7.1, 7.3, watchedTitles);
            addMovieIfNotWatched("Город ангелов (1998)", "Ангел отказывается от бессмертия", 6.7, 7.0, watchedTitles);
        } else if (favoriteGenre.equals("Приключения")) {
            addMovieIfNotWatched("Пираты Карибского моря (2003)", "Капитан Джек Воробей спасает девушку", 8.1, 8.3, watchedTitles);
            addMovieIfNotWatched("Индиана Джонс (1981)", "Археолог ищет Ковчег Завета", 8.4, 8.5, watchedTitles);
            addMovieIfNotWatched("Назад в будущее (1985)", "Подросток путешествует во времени", 8.5, 8.6, watchedTitles);
            addMovieIfNotWatched("Парк Юрского периода (1993)", "Тематический парк с динозаврами", 8.2, 8.4, watchedTitles);
            addMovieIfNotWatched("Звёздные войны (1977)", "Фермер присоединяется к повстанцам", 8.6, 8.7, watchedTitles);
        } else if (favoriteGenre.equals("Фэнтези")) {
            addMovieIfNotWatched("Властелин колец (2001)", "Хоббит должен уничтожить кольцо", 8.8, 9.0, watchedTitles);
            addMovieIfNotWatched("Гарри Поттер и узник Азкабана (2004)", "Гарри узнаёт о крестном отце", 7.9, 8.2, watchedTitles);
            addMovieIfNotWatched("Хроники Нарнии: Принц Каспиан (2008)", "Певеси возвращается в Нарнию", 6.5, 6.8, watchedTitles);
            addMovieIfNotWatched("Пэн: Путешествие в Нетландию (2015)", "Происхождение Питера Пэна", 6.7, 6.9, watchedTitles);
            addMovieIfNotWatched("Стражи снов (2012)", "Духи защищают детские сны", 7.3, 7.6, watchedTitles);
        } else if (favoriteGenre.equals("Семейный")) {
            addMovieIfNotWatched("Гарри Поттер (2001)", "Мальчик-сирота узнаёт, что он волшебник", 7.6, 7.9, watchedTitles);
            addMovieIfNotWatched("Король Лев (1994)", "Львёнок Симба бежит из своего королевства", 8.5, 8.8, watchedTitles);
            addMovieIfNotWatched("Хроники Нарнии (2005)", "Дети попадают в волшебный мир через шкаф", 6.9, 7.1, watchedTitles);
            addMovieIfNotWatched("История игрушек (1995)", "Игрушки оживают, когда людей нет рядом", 8.3, 8.5, watchedTitles);
            addMovieIfNotWatched("Уоллес и Громит (1989)", "Изобретатель и его собака расследуют дела", 8.2, 8.4, watchedTitles);
        } else if (favoriteGenre.equals("Мюзикл")) {
            addMovieIfNotWatched("Поющие под дождём (1952)", "Звезда немого кино в эпоху звука", 8.3, 8.5, watchedTitles);
            addMovieIfNotWatched("Ла-Ла Ленд (2016)", "Джазовый пианист и начинающая актриса", 8.0, 7.8, watchedTitles);
            addMovieIfNotWatched("Звуки музыки (1965)", "Гувернантка учит детей петь", 8.0, 8.2, watchedTitles);
            addMovieIfNotWatched("Величайший шоумен (2017)", "Финеас Барнум создаёт цирк", 7.6, 7.5, watchedTitles);
            addMovieIfNotWatched("Чикаго (2002)", "Хористка убивает любовника", 7.2, 7.4, watchedTitles);
        } else if (favoriteGenre.equals("Артхаус")) {
            addMovieIfNotWatched("Вечное сияние чистого разума (2004)", "Пара стирает друг друга из памяти", 8.3, 8.5, watchedTitles);
            addMovieIfNotWatched("Амели (2001)", "Молодая женщина меняет жизни людей", 8.3, 8.5, watchedTitles);
            addMovieIfNotWatched("Аватар (2009)", "Парализованный морпех на Пандоре", 7.9, 8.2, watchedTitles);
            addMovieIfNotWatched("Догвилль (2003)", "Девушка скрывается в маленьком городке", 7.9, 8.1, watchedTitles);
            addMovieIfNotWatched("Малхолланд Драйв (2001)", "Актриса приезжает в Голливуд", 7.9, 8.2, watchedTitles);
        } else if (favoriteGenre.equals("Детектив")) {
            addMovieIfNotWatched("Семь (1995)", "Детективы выслеживают убийцу", 8.6, 8.8, watchedTitles);
            addMovieIfNotWatched("Молчание ягнят (1991)", "Стажёр ФБР обращается к каннибалу", 8.6, 8.7, watchedTitles);
            addMovieIfNotWatched("Достать ножи (2019)", "Детектив расследует смерть", 7.9, 8.1, watchedTitles);
            addMovieIfNotWatched("Шерлок Холмс (2009)", "Сыщик расследует ритуальные убийства", 7.6, 7.8, watchedTitles);
            addMovieIfNotWatched("Помни (2000)", "Мужчина ищет убийцу жены", 8.4, 8.6, watchedTitles);
        } else {
            addMovieIfNotWatched("Интерстеллар (2014)", "Путешествие через червоточину", 8.6, 8.9, watchedTitles);
            addMovieIfNotWatched("Зелёная миля (1999)", "Надзиратель узнаёт о даре заключённого", 8.6, 9.1, watchedTitles);
            addMovieIfNotWatched("Тёмный рыцарь (2008)", "Бэтмен против Джокера", 9.0, 9.0, watchedTitles);
        }

        if (recommendedMovies.isEmpty()) {
            tvRecommendationsTitle.setText("🎯 Все фильмы в жанре " + favoriteGenre + " уже просмотрены!\nПопробуйте другой жанр.");
            tvRecommendationsTitle.setVisibility(View.VISIBLE);
            rvRecommendations.setVisibility(View.GONE);
        } else {
            RecommendationAdapter adapter = new RecommendationAdapter(getContext(), recommendedMovies, favoriteGenre);
            rvRecommendations.setAdapter(adapter);

            tvRecommendationsTitle.setText("🎯 Любимый жанр: " + favoriteGenre);
            tvRecommendationsTitle.setVisibility(View.VISIBLE);
            rvRecommendations.setVisibility(View.VISIBLE);
        }
    }

    private void addMovieIfNotWatched(String title, String description, double ratingImdb, double ratingKp, Set<String> watchedTitles) {
        if (!watchedTitles.contains(title.toLowerCase())) {
            recommendedMovies.add(new Movie(title, description, ratingImdb, ratingKp));
        }
    }

    private void hideRecommendations() {
        tvRecommendationsTitle.setVisibility(View.GONE);
        rvRecommendations.setVisibility(View.GONE);
    }

    private void openMovieDetails(DatabaseHelper.HistoryItem item) {
        Intent intent = new Intent(getContext(), MovieDetailActivity.class);
        intent.putExtra("title", item.getTitle());
        intent.putExtra("description", item.getDescription());
        intent.putExtra("ratingImdb", item.getRatingImdb());
        intent.putExtra("ratingKp", item.getRatingKp());
        intent.putExtra("genre", item.getGenre());
        intent.putExtra("mood", item.getMood());
        startActivity(intent);
    }
}