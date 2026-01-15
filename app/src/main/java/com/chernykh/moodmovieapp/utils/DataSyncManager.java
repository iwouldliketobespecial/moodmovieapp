package com.chernykh.moodmovieapp.utils;

import android.content.Context;
import android.util.Log;
import java.util.List;
import java.util.Map;

public class DataSyncManager {
    private static final String TAG = "DataSyncManager";

    private DatabaseHelper localDb;
    private FirebaseDataManager cloudDb;
    private String userId;
    private Context context;

    public DataSyncManager(Context context, String userId) {
        this.context = context;
        this.localDb = new DatabaseHelper(context);
        this.cloudDb = new FirebaseDataManager(userId);
        this.userId = userId;
    }

    // Синхронизация при входе пользователя
    public void syncOnLogin() {
        Log.d(TAG, "Начало синхронизации для пользователя: " + userId);

        // 1. Загружаем историю из облака
        syncHistoryCloudToLocal();

        // 2. Загружаем избранное из облака
        syncFavoritesCloudToLocal();
    }

    private void syncHistoryCloudToLocal() {
        cloudDb.getHistoryFromCloud(new FirebaseDataManager.FirestoreCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> cloudHistory) {
                for (Map<String, Object> item : cloudHistory) {
                    String title = (String) item.get("title");

                    if (!localDb.isInHistory(title)) {
                        localDb.addToHistory(
                                title,
                                (String) item.get("description"),
                                ((Number) item.get("ratingImdb")).doubleValue(),
                                ((Number) item.get("ratingKp")).doubleValue(),
                                (String) item.get("genre"),
                                (String) item.get("mood")
                        );
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Ошибка загрузки истории из облака: " + e.getMessage());
            }
        });
    }

    private void syncFavoritesCloudToLocal() {
        cloudDb.getFavoritesFromCloud(new FirebaseDataManager.FirestoreCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> cloudFavorites) {
                for (Map<String, Object> item : cloudFavorites) {
                    String title = (String) item.get("title");

                    if (!localDb.isFavorite(title)) { // ← тут правильно isFavorite
                        localDb.addToFavorites(
                                title,
                                (String) item.get("description"),
                                ((Number) item.get("ratingImdb")).doubleValue(),
                                ((Number) item.get("ratingKp")).doubleValue(),
                                (String) item.get("genre"),
                                (String) item.get("mood")
                        );
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Ошибка загрузки избранного из облака: " + e.getMessage());
            }
        });
    }

    // Отправка всех локальных данных в облако
    public void syncLocalToCloud() {
        Log.d(TAG, "Отправка локальных данных в облако");

        // Синхронизация истории
        List<DatabaseHelper.HistoryItem> localHistory = localDb.getHistory();
        for (DatabaseHelper.HistoryItem item : localHistory) {
            cloudDb.saveHistoryToCloud(FirebaseDataManager.createHistoryData(
                    item.getTitle(),
                    item.getDescription(),
                    item.getRatingImdb(),
                    item.getRatingKp(),
                    item.getGenre(),
                    item.getMood()
            ));
        }

        // Синхронизация избранного
        List<DatabaseHelper.FavoriteItem> localFavorites = localDb.getFavorites();
        for (DatabaseHelper.FavoriteItem item : localFavorites) {
            cloudDb.saveFavoriteToCloud(FirebaseDataManager.createFavoriteData(
                    item.getTitle(),
                    item.getDescription(),
                    item.getRatingImdb(),
                    item.getRatingKp(),
                    item.getGenre(),
                    item.getMood()
            ));
        }

        Log.d(TAG, "Локальные данные отправлены в облако");
    }

    // Закрытие соединений
    public void close() {
        if (localDb != null) {
            localDb.close();
        }
    }
}