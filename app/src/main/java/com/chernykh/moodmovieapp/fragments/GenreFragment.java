package com.chernykh.moodmovieapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chernykh.moodmovieapp.R;
import com.chernykh.moodmovieapp.adapters.GenreAdapter;
import com.chernykh.moodmovieapp.models.Genre;
import java.util.ArrayList;
import java.util.List;

public class GenreFragment extends Fragment {

    private RecyclerView rvGenres;
    private TextView tvMoodInfo;
    private String currentMood;

    private OnGenreSelectedListener listener;

    public interface OnGenreSelectedListener {
        void onGenreSelected(String mood, String genre);
    }

    public void setOnGenreSelectedListener(OnGenreSelectedListener listener) {
        this.listener = listener;
    }

    public void setCurrentMood(String mood) {
        this.currentMood = mood;
        if (tvMoodInfo != null) {
            updateMoodInfo();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_genre, container, false);

        tvMoodInfo = view.findViewById(R.id.tvMoodInfo);
        rvGenres = view.findViewById(R.id.rvGenres);
        rvGenres.setLayoutManager(new GridLayoutManager(getContext(), 2));

        if (currentMood != null) {
            updateMoodInfo();
            setupGenres();
        }

        return view;
    }

    private void updateMoodInfo() {
        String moodText = "";
        switch (currentMood) {
            case "happy": moodText = "Веселое настроение"; break;
            case "sad": moodText = "Грустное настроение"; break;
            case "neutral": moodText = "Нейтральное настроение"; break;
            default: moodText = "Настроение не определено";
        }
        tvMoodInfo.setText("Ваше настроение: " + moodText);
    }

    private void setupGenres() {
        List<Genre> genres = getRecommendedGenres(currentMood);

        GenreAdapter adapter = new GenreAdapter(genres, genre -> {
            if (listener != null) {
                listener.onGenreSelected(currentMood, genre.getName());
            }
        });

        rvGenres.setAdapter(adapter);
    }

    private List<Genre> getRecommendedGenres(String mood) {
        List<Genre> genres = new ArrayList<>();

        switch (mood) {
            case "happy":
                genres.add(new Genre("Комедия"));
                genres.add(new Genre("Мюзикл"));
                genres.add(new Genre("Приключения"));
                genres.add(new Genre("Семейный"));
                break;
            case "sad":
                genres.add(new Genre("Драма"));
                genres.add(new Genre("Мелодрама"));
                genres.add(new Genre("Фэнтези"));
                genres.add(new Genre("Артхаус"));
                break;
            case "neutral":
            default:
                genres.add(new Genre("Боевик"));
                genres.add(new Genre("Детектив"));
                genres.add(new Genre("Фантастика"));
                genres.add(new Genre("Триллер"));
                break;
        }

        return genres;
    }
}