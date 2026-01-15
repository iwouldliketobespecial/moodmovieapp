package com.chernykh.moodmovieapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chernykh.moodmovieapp.adapters.MovieAdapter;
import com.chernykh.moodmovieapp.models.Movie;
import com.chernykh.moodmovieapp.utils.MovieDataGenerator;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView rvSearchResults;
    private MovieAdapter adapter;
    private List<Movie> allMovies;
    private TextView tvNoResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        SearchView searchView = findViewById(R.id.searchViewMain);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        tvNoResults = findViewById(R.id.tvNoResults);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));

        allMovies = getAllMovies();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterMovies(newText.toLowerCase());
                return true;
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("search_query")) {
            String query = intent.getStringExtra("search_query");
            searchView.setQuery(query, false);
            filterMovies(query.toLowerCase());
        }
    }

    private List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();

        String[] moods = {"happy", "sad", "neutral"};
        String[][] genres = {
                {"Комедия", "Мюзикл", "Приключения", "Семейный"},
                {"Драма", "Мелодрама", "Фэнтези", "Артхаус"},
                {"Боевик", "Детектив", "Фантастика", "Триллер"}
        };

        for (int i = 0; i < moods.length; i++) {
            for (String genre : genres[i]) {
                movies.addAll(MovieDataGenerator.getMoviesByMoodAndGenre(moods[i], genre));
            }
        }

        return movies;
    }

    private void filterMovies(String query) {
        if (query.isEmpty()) {
            rvSearchResults.setAdapter(null);
            tvNoResults.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
            return;
        }

        List<Movie> filtered = new ArrayList<>();
        for (Movie movie : allMovies) {
            if (movie.getTitle().toLowerCase().contains(query)) {
                filtered.add(movie);
            }
        }

        if (filtered.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            rvSearchResults.setVisibility(View.GONE);
        } else {
            tvNoResults.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
            adapter = new MovieAdapter(this, filtered, "Все", "neutral");
            rvSearchResults.setAdapter(adapter);
        }
    }
}