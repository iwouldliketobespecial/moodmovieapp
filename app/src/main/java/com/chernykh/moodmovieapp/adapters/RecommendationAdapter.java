package com.chernykh.moodmovieapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.chernykh.moodmovieapp.R;
import com.chernykh.moodmovieapp.MovieDetailActivity;
import com.chernykh.moodmovieapp.models.Movie;
import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private List<Movie> movieList;
    private Context context;
    private String currentGenre;

    public RecommendationAdapter(Context context, List<Movie> movieList, String genre) {
        this.context = context;
        this.movieList = movieList;
        this.currentGenre = genre;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        holder.tvTitle.setText(movie.getTitle());

        String ratings = String.format("IMDb: %.1f | Кинопоиск: %.1f",
                movie.getRatingImdb(), movie.getRatingKp());
        holder.tvRating.setText(ratings);

        holder.tvDescription.setText(movie.getDescription());

        holder.ivPoster.setImageResource(getPosterResource(movie.getTitle()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra("title", movie.getTitle());
            intent.putExtra("description", movie.getDescription());
            intent.putExtra("ratingImdb", movie.getRatingImdb());
            intent.putExtra("ratingKp", movie.getRatingKp());
            intent.putExtra("genre", currentGenre);
            intent.putExtra("mood", "neutral");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    private int getPosterResource(String title) {
        if (title.contains("Трудный ребёнок")) return R.drawable.poster_problem_child;
        if (title.contains("Один дома")) return R.drawable.poster_home_alone;
        if (title.contains("Маска")) return R.drawable.poster_the_mask;
        if (title.contains("Тупой")) return R.drawable.poster_dumb_and_dumber;
        if (title.contains("Полицейский из Беверли")) return R.drawable.poster_beverly_hills_cop;
        if (title.contains("Пираты Карибского")) return R.drawable.poster_pirates_caribbean;
        if (title.contains("Индиана Джонс")) return R.drawable.poster_indiana_jones;
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
        ImageView ivPoster;
        TextView tvTitle, tvRating, tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}