package com.chernykh.moodmovieapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chernykh.moodmovieapp.R;
import com.chernykh.moodmovieapp.adapters.MovieAdapter;
import com.chernykh.moodmovieapp.models.Movie;
import com.chernykh.moodmovieapp.utils.MovieDataGenerator;
import java.util.List;

public class MoviesFragment extends Fragment {

    private RecyclerView rvMovies;
    private TextView tvTitle;
    private String currentMood;
    private String currentGenre;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies, container, false);

        tvTitle = view.findViewById(R.id.tvTitle);
        rvMovies = view.findViewById(R.id.rvMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getArguments() != null) {
            currentMood = getArguments().getString("mood");
            currentGenre = getArguments().getString("genre");

            updateTitle();
            loadMovies();
        }

        return view;
    }

    private void updateTitle() {
        String moodText = "";
        switch (currentMood) {
            case "happy": moodText = "Веселое"; break;
            case "sad": moodText = "Грустное"; break;
            case "neutral": moodText = "Нейтральное"; break;
        }

        tvTitle.setText(String.format("Фильмы для %s настроения\nЖанр: %s", moodText, currentGenre));
    }

    private void loadMovies() {
        List<Movie> movies = MovieDataGenerator.getMoviesByMoodAndGenre(currentMood, currentGenre);
        MovieAdapter adapter = new MovieAdapter(getContext(), movies, currentGenre, currentMood);
        rvMovies.setAdapter(adapter);
    }

    public void setMovieData(String mood, String genre) {
        this.currentMood = mood;
        this.currentGenre = genre;

        if (tvTitle != null) {
            updateTitle();
            loadMovies();
        }
    }
}