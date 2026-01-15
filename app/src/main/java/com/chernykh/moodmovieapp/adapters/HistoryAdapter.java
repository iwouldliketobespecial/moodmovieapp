package com.chernykh.moodmovieapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.chernykh.moodmovieapp.R;
import com.chernykh.moodmovieapp.MovieDetailActivity;
import com.chernykh.moodmovieapp.utils.DatabaseHelper;
import com.chernykh.moodmovieapp.utils.FirebaseAuthManager;
import com.chernykh.moodmovieapp.utils.FirebaseDataManager;
import com.google.firebase.auth.FirebaseUser;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<DatabaseHelper.HistoryItem> historyList;
    private Context context;
    private OnHistoryItemClickListener listener;
    private DatabaseHelper dbHelper;
    private FirebaseDataManager firebaseDataManager;

    public interface OnHistoryItemClickListener {
        void onHistoryItemClick(DatabaseHelper.HistoryItem item);
    }

    public HistoryAdapter(Context context, List<DatabaseHelper.HistoryItem> historyList, OnHistoryItemClickListener listener) {
        this.context = context;
        this.historyList = historyList;
        this.listener = listener;
        this.dbHelper = new DatabaseHelper(context);

        // Инициализация FirebaseDataManager если пользователь авторизован
        FirebaseUser user = FirebaseAuthManager.getInstance(context).getCurrentUser();
        if (user != null) {
            this.firebaseDataManager = new FirebaseDataManager(user.getUid());
        }
    }

    public HistoryAdapter(Context context, List<DatabaseHelper.HistoryItem> historyList) {
        this.context = context;
        this.historyList = historyList;
        this.listener = null;
        this.dbHelper = new DatabaseHelper(context);

        FirebaseUser user = FirebaseAuthManager.getInstance(context).getCurrentUser();
        if (user != null) {
            this.firebaseDataManager = new FirebaseDataManager(user.getUid());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DatabaseHelper.HistoryItem item = historyList.get(position);

        holder.tvTitle.setText(item.getTitle());

        String ratings = String.format("IMDb: %.1f | Кинопоиск: %.1f",
                item.getRatingImdb(), item.getRatingKp());
        holder.tvRating.setText(ratings);

        holder.tvGenre.setText("Жанр: " + item.getGenre());
        holder.tvMood.setText("Настроение: " + getMoodText(item.getMood()));

        String date = formatDate(item.getTimestamp());
        holder.tvDate.setText("Просмотрено: " + date);

        boolean isFavorite = dbHelper.isFavorite(item.getTitle());

        if (isFavorite) {
            holder.ivMoodIcon.setImageResource(android.R.drawable.star_big_on);
        } else {
            holder.ivMoodIcon.setImageResource(android.R.drawable.star_off);
        }

        holder.ivMoodIcon.setOnClickListener(v -> {
            boolean currentlyFavorite = dbHelper.isFavorite(item.getTitle());

            if (currentlyFavorite) {
                dbHelper.removeFromFavorites(item.getTitle());
                holder.ivMoodIcon.setImageResource(android.R.drawable.star_off);

                if (firebaseDataManager != null) {
                    firebaseDataManager.checkIfFavorite(item.getTitle(),
                            new FirebaseDataManager.FavoriteCheckCallback() {
                                @Override
                                public void onResult(boolean isFavorite, String documentId) {
                                    if (isFavorite && documentId != null) {
                                        firebaseDataManager.removeFavorite(documentId);
                                        Log.d("HistoryAdapter", "Удалено из облака: " + item.getTitle());
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("HistoryAdapter", "Ошибка удаления из облака: " + e.getMessage());
                                }
                            });
                }
            } else {
                // Добавляем в избранное
                dbHelper.addToFavorites(
                        item.getTitle(),
                        item.getDescription(),
                        item.getRatingImdb(),
                        item.getRatingKp(),
                        item.getGenre(),
                        item.getMood()
                );
                holder.ivMoodIcon.setImageResource(android.R.drawable.star_big_on);

                if (firebaseDataManager != null) {
                    firebaseDataManager.saveFavoriteToCloud(
                            FirebaseDataManager.createFavoriteData(
                                    item.getTitle(),
                                    item.getDescription(),
                                    item.getRatingImdb(),
                                    item.getRatingKp(),
                                    item.getGenre(),
                                    item.getMood()
                            )
                    );
                    Log.d("HistoryAdapter", "Добавлено в облако: " + item.getTitle());
                }
            }
        });

        holder.ivPoster.setImageResource(getPosterResource(item.getTitle()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHistoryItemClick(item);
            } else {
                openMovieDetails(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private String getMoodText(String mood) {
        switch (mood) {
            case "happy": return "Веселое";
            case "sad": return "Грустное";
            case "neutral": return "Нейтральное";
            default: return "Не определено";
        }
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void openMovieDetails(DatabaseHelper.HistoryItem item) {
        if (context != null) {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra("title", item.getTitle());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("ratingImdb", item.getRatingImdb());
            intent.putExtra("ratingKp", item.getRatingKp());
            intent.putExtra("genre", item.getGenre());
            intent.putExtra("mood", item.getMood());
            context.startActivity(intent);
        }
    }

    private int getPosterResource(String title) {

        if (title.contains("Трудный ребёнок")) return R.drawable.poster_problem_child;
        if (title.contains("Один дома")) return R.drawable.poster_home_alone;
        if (title.contains("Маска")) return R.drawable.poster_the_mask;
        if (title.contains("Тупой")) return R.drawable.poster_dumb_and_dumber;
        if (title.contains("Полицейский из Беверли")) return R.drawable.poster_beverly_hills_cop;
        if (title.contains("Пираты Карибского")) return R.drawable.poster_pirates_caribbean;
        if (title.contains("Индиана")) return R.drawable.poster_indiana_jones;
        if (title.contains("Назад в будущее")) return R.drawable.poster_back_to_future;
        if (title.contains("Парк Юрского")) return R.drawable.poster_jurassic_park;
        if (title.contains("Звёздные войны")) return R.drawable.poster_star_wars;
        if (title.contains("Поющие под дождём")) return R.drawable.poster_singing_in_rain;
        if (title.contains("Ла-Ла Ленд")) return R.drawable.poster_la_la_land;
        if (title.contains("Звуки музыки")) return R.drawable.poster_sound_of_music;
        if (title.contains("Величайший шоумен")) return R.drawable.poster_greatest_showman;
        if (title.contains("Чикаго")) return R.drawable.poster_chicago;
        if (title.contains("Гарри Поттер") && !title.contains("Азкабан")) return R.drawable.poster_harry_potter;
        if (title.contains("Король Лев")) return R.drawable.poster_lion_king;
        if (title.contains("Хроники Нарнии") && !title.contains("Принц")) return R.drawable.poster_narnia;
        if (title.contains("История игрушек")) return R.drawable.poster_toy_story;
        if (title.contains("Уоллес")) return R.drawable.poster_wallace_gromit;
        if (title.contains("Зелёная миля")) return R.drawable.poster_green_mile;
        if (title.contains("Побег из Шоушенка")) return R.drawable.poster_shawshank_redemption;
        if (title.contains("Форрест Гамп")) return R.drawable.poster_forrest_gump;
        if (title.contains("Список Шиндлера")) return R.drawable.poster_schindlers_list;
        if (title.contains("Крёстный отец")) return R.drawable.poster_godfather;
        if (title.contains("Титаник")) return R.drawable.poster_titanic;
        if (title.contains("Великий Гэтсби")) return R.drawable.poster_great_gatsby;
        if (title.contains("Дневник памяти")) return R.drawable.poster_notebook;
        if (title.contains("Предложение")) return R.drawable.poster_proposal;
        if (title.contains("Город ангелов")) return R.drawable.poster_city_of_angels;
        if (title.contains("Властелин колец")) return R.drawable.poster_lord_of_rings;
        if (title.contains("Гарри Поттер") && title.contains("Азкабан")) return R.drawable.poster_harry_potter_prisoner;
        if (title.contains("Принц Каспиан")) return R.drawable.poster_narnia_caspian;
        if (title.contains("Пэн")) return R.drawable.poster_pan;
        if (title.contains("Стражи снов")) return R.drawable.poster_rise_of_guardians;
        if (title.contains("Вечное сияние")) return R.drawable.poster_eternal_sunshine;
        if (title.contains("Амели")) return R.drawable.poster_amelie;
        if (title.contains("Аватар")) return R.drawable.poster_avatar;
        if (title.contains("Догвилль")) return R.drawable.poster_dogville;
        if (title.contains("Малхолланд")) return R.drawable.poster_mulholland_drive;
        if (title.contains("Интерстеллар")) return R.drawable.poster_interstellar;
        if (title.contains("Матрица")) return R.drawable.poster_matrix;
        if (title.contains("Начало")) return R.drawable.poster_inception;
        if (title.contains("Чужой")) return R.drawable.poster_alien;
        if (title.contains("Пятый элемент")) return R.drawable.poster_fifth_element;
        if (title.contains("Тёмный рыцарь") || title.contains("Темный рыцарь")) return R.drawable.poster_dark_knight;
        if (title.contains("Гладиатор")) return R.drawable.poster_gladiator;
        if (title.contains("Крепкий орешек")) return R.drawable.poster_die_hard;
        if (title.contains("Терминатор 2")) return R.drawable.poster_terminator_2;
        if (title.contains("Миссия невыполнима")) return R.drawable.poster_mission_impossible;
        if (title.contains("Семь")) return R.drawable.poster_seven;
        if (title.contains("Молчание ягнят")) return R.drawable.poster_silence_lambs;
        if (title.contains("Достать ножи")) return R.drawable.poster_knives_out;
        if (title.contains("Шерлок Холмс")) return R.drawable.poster_sherlock_holmes;
        if (title.contains("Помни")) return R.drawable.poster_memento;
        if (title.contains("Остров проклятых")) return R.drawable.poster_shutter_island;
        if (title.contains("Игра")) return R.drawable.poster_game;
        if (title.contains("Сплит")) return R.drawable.poster_split;
        if (title.contains("Паразиты")) return R.drawable.poster_parasite;
        if (title.contains("Звонок")) return R.drawable.poster_ring;

        return android.R.drawable.ic_menu_gallery;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster, ivMoodIcon;
        TextView tvTitle, tvRating, tvGenre, tvMood, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            ivMoodIcon = itemView.findViewById(R.id.ivMoodIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvMood = itemView.findViewById(R.id.tvMood);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}