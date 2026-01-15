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
import com.chernykh.moodmovieapp.utils.DatabaseHelper;
import com.chernykh.moodmovieapp.utils.FirebaseAuthManager;
import com.chernykh.moodmovieapp.utils.FirebaseDataManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyMoviesFragment extends Fragment {
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private RecyclerView rvHistory;
    private TextView tvEmptyHistory;
    private Button btnClearHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_movies, container, false);

        rvHistory = view.findViewById(R.id.rvHistory);
        tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory);
        btnClearHistory = view.findViewById(R.id.btnClearHistory);

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        loadFavorites(dbHelper);

        btnClearHistory.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuthManager.getInstance(getContext()).getCurrentUser();
            if (user != null) {
                FirebaseDataManager firebaseDataManager = new FirebaseDataManager(user.getUid());
                firebaseDataManager.getFavoritesFromCloud(new FirebaseDataManager.FirestoreCallback() {
                    @Override
                    public void onSuccess(List<Map<String, Object>> cloudFavorites) {
                        for (Map<String, Object> item : cloudFavorites) {
                            String documentId = (String) item.get("id");
                            if (documentId != null) {
                                firestore.collection("users").document(user.getUid()).collection("favorites").document(documentId).delete();
                            }
                        }
                        dbHelper.clearFavorites();
                        loadFavorites(dbHelper);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        dbHelper.clearFavorites();
                        loadFavorites(dbHelper);
                    }
                });
            } else {
                dbHelper.clearFavorites();
                loadFavorites(dbHelper);
            }
        });

        return view;
    }

    private void loadFavorites(DatabaseHelper dbHelper) {
        List<DatabaseHelper.FavoriteItem> favorites = dbHelper.getFavorites();

        if (favorites.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
            tvEmptyHistory.setText("Избранных фильмов пока нет");
        } else {
            tvEmptyHistory.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);

            HistoryAdapter adapter = new HistoryAdapter(
                    getContext(),
                    convertFavoritesToHistory(favorites),
                    item -> openMovieDetails(item)
            );

            rvHistory.setAdapter(adapter);
        }
    }

    private List<DatabaseHelper.HistoryItem> convertFavoritesToHistory(List<DatabaseHelper.FavoriteItem> favorites) {
        ArrayList<DatabaseHelper.HistoryItem> history = new ArrayList<>();

        for (DatabaseHelper.FavoriteItem favorite : favorites) {
            DatabaseHelper.HistoryItem item = new DatabaseHelper.HistoryItem();
            item.setTitle(favorite.getTitle());
            item.setDescription(favorite.getDescription());
            item.setRatingImdb(favorite.getRatingImdb());
            item.setRatingKp(favorite.getRatingKp());
            item.setGenre(favorite.getGenre());
            item.setMood(favorite.getMood());
            item.setTimestamp(favorite.getTimestamp());
            history.add(item);
        }

        return history;
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