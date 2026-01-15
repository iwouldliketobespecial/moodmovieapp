package com.chernykh.moodmovieapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Метод для получения любимого жанра
    public String getMostWatchedGenre() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT genre, COUNT(*) as count FROM " + TABLE_HISTORY +
                " GROUP BY genre ORDER BY count DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            String favoriteGenre = cursor.getString(0);
            cursor.close();
            return favoriteGenre;
        }
        cursor.close();
        return "";
    }

    private static final String DATABASE_NAME = "moodmovies.db";
    private static final int DATABASE_VERSION = 1;

    // Таблица истории просмотров
    private static final String TABLE_HISTORY = "view_history";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_RATING_IMDB = "rating_imdb";
    private static final String COLUMN_RATING_KP = "rating_kp";
    private static final String COLUMN_GENRE = "genre";
    private static final String COLUMN_MOOD = "mood";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    // Таблица избранных фильмов
    private static final String TABLE_FAVORITES = "favorites";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createHistoryTable = "CREATE TABLE " + TABLE_HISTORY + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_RATING_IMDB + " REAL,"
                + COLUMN_RATING_KP + " REAL,"
                + COLUMN_GENRE + " TEXT,"
                + COLUMN_MOOD + " TEXT,"
                + COLUMN_TIMESTAMP + " INTEGER)";
        db.execSQL(createHistoryTable);

        String createFavoritesTable = "CREATE TABLE " + TABLE_FAVORITES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT UNIQUE,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_RATING_IMDB + " REAL,"
                + COLUMN_RATING_KP + " REAL,"
                + COLUMN_GENRE + " TEXT,"
                + COLUMN_MOOD + " TEXT,"
                + COLUMN_TIMESTAMP + " INTEGER)";
        db.execSQL(createFavoritesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }


    public void addToHistory(String title, String description,
                             double ratingImdb, double ratingKp,
                             String genre, String mood) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_RATING_IMDB, ratingImdb);
        values.put(COLUMN_RATING_KP, ratingKp);
        values.put(COLUMN_GENRE, genre);
        values.put(COLUMN_MOOD, mood);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());

        db.insert(TABLE_HISTORY, null, values);
        db.close();
    }

    public List<HistoryItem> getHistory() {
        List<HistoryItem> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_HISTORY
                + " ORDER BY " + COLUMN_TIMESTAMP + " DESC LIMIT 50";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                HistoryItem item = new HistoryItem();
                item.setId(cursor.getInt(0));
                item.setTitle(cursor.getString(1));
                item.setDescription(cursor.getString(2));
                item.setRatingImdb(cursor.getDouble(3));
                item.setRatingKp(cursor.getDouble(4));
                item.setGenre(cursor.getString(5));
                item.setMood(cursor.getString(6));
                item.setTimestamp(cursor.getLong(7));

                history.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return history;
    }

    public void clearHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, null, null);
        db.close();
    }


    public boolean addToFavorites(String title, String description,
                                  double ratingImdb, double ratingKp,
                                  String genre, String mood) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_RATING_IMDB, ratingImdb);
        values.put(COLUMN_RATING_KP, ratingKp);
        values.put(COLUMN_GENRE, genre);
        values.put(COLUMN_MOOD, mood);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());

        long result = db.insertWithOnConflict(TABLE_FAVORITES, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
        db.close();

        return result != -1;
    }

    public boolean removeFromFavorites(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_FAVORITES, COLUMN_TITLE + " = ?",
                new String[]{title});
        db.close();

        return result > 0;
    }

    public boolean isInHistory(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_HISTORY + " WHERE " + COLUMN_TITLE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{title});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean isFavorite(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FAVORITES
                + " WHERE " + COLUMN_TITLE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{title});

        boolean isFavorite = cursor.getCount() > 0;
        cursor.close();
        db.close();

        return isFavorite;
    }

    public List<FavoriteItem> getFavorites() {
        List<FavoriteItem> favorites = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_FAVORITES
                + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                FavoriteItem item = new FavoriteItem();
                item.setId(cursor.getInt(0));
                item.setTitle(cursor.getString(1));
                item.setDescription(cursor.getString(2));
                item.setRatingImdb(cursor.getDouble(3));
                item.setRatingKp(cursor.getDouble(4));
                item.setGenre(cursor.getString(5));
                item.setMood(cursor.getString(6));
                item.setTimestamp(cursor.getLong(7));

                favorites.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return favorites;
    }

    public void clearFavorites() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, null, null);
        db.close();
    }


    public static class HistoryItem {
        private int id;
        private String title;
        private String description;
        private double ratingImdb;
        private double ratingKp;
        private String genre;
        private String mood;
        private long timestamp;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getRatingImdb() { return ratingImdb; }
        public void setRatingImdb(double ratingImdb) { this.ratingImdb = ratingImdb; }

        public double getRatingKp() { return ratingKp; }
        public void setRatingKp(double ratingKp) { this.ratingKp = ratingKp; }

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }

        public String getMood() { return mood; }
        public void setMood(String mood) { this.mood = mood; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public static class FavoriteItem {
        private int id;
        private String title;
        private String description;
        private double ratingImdb;
        private double ratingKp;
        private String genre;
        private String mood;
        private long timestamp;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getRatingImdb() { return ratingImdb; }
        public void setRatingImdb(double ratingImdb) { this.ratingImdb = ratingImdb; }

        public double getRatingKp() { return ratingKp; }
        public void setRatingKp(double ratingKp) { this.ratingKp = ratingKp; }

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }

        public String getMood() { return mood; }
        public void setMood(String mood) { this.mood = mood; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}