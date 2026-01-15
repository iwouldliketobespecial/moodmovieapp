package com.chernykh.moodmovieapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseDataManager {

    private final FirebaseFirestore firestore;
    private final String userId;

    public FirebaseDataManager(String userId) {
        this.userId = userId;
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void saveHistoryToCloud(Map<String, Object> historyData) {
        firestore.collection("users")
                .document(userId)
                .collection("history")
                .add(historyData)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("История сохранена с ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    System.err.println("Ошибка сохранения истории: " + e.getMessage());
                });
    }

    public void saveFavoriteToCloud(Map<String, Object> favoriteData) {
        firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .add(favoriteData)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Избранное сохранено с ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    System.err.println("Ошибка сохранения избранного: " + e.getMessage());
                });
    }

    public void getHistoryFromCloud(FirestoreCallback callback) {
        firestore.collection("users")
                .document(userId)
                .collection("history")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> historyList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> data = document.getData();
                        data.put("id", document.getId());
                        historyList.add(data);
                    }

                    callback.onSuccess(historyList);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }

    public void getFavoritesFromCloud(FirestoreCallback callback) {
        firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> favoritesList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> data = document.getData();
                        data.put("id", document.getId());
                        favoritesList.add(data);
                    }

                    callback.onSuccess(favoritesList);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }

    public void removeFavorite(String documentId) {
        firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Фильм удален из избранного");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Ошибка удаления: " + e.getMessage());
                });
    }

    public void checkIfFavorite(String movieTitle, FavoriteCheckCallback callback) {
        firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .whereEqualTo("title", movieTitle)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isFavorite = !queryDocumentSnapshots.isEmpty();
                    String documentId = isFavorite ?
                            queryDocumentSnapshots.getDocuments().get(0).getId() : null;
                    callback.onResult(isFavorite, documentId);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }

    public interface FirestoreCallback {
        void onSuccess(List<Map<String, Object>> data);
        void onFailure(Exception e);
    }

    public interface FavoriteCheckCallback {
        void onResult(boolean isFavorite, String documentId);
        void onError(Exception e);
    }

    public static Map<String, Object> createHistoryData(String title, String description,
                                                        double ratingImdb, double ratingKp,
                                                        String genre, String mood) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("description", description);
        map.put("ratingImdb", ratingImdb);
        map.put("ratingKp", ratingKp);
        map.put("genre", genre);
        map.put("mood", mood);
        map.put("timestamp", System.currentTimeMillis());
        return map;
    }

    public static Map<String, Object> createFavoriteData(String title, String description,
                                                         double ratingImdb, double ratingKp,
                                                         String genre, String mood) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("description", description);
        map.put("ratingImdb", ratingImdb);
        map.put("ratingKp", ratingKp);
        map.put("genre", genre);
        map.put("mood", mood);
        map.put("addedAt", System.currentTimeMillis());
        return map;
    }
}