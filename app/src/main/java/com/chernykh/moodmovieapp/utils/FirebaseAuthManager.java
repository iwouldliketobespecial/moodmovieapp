package com.chernykh.moodmovieapp.utils;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class FirebaseAuthManager {

    private static FirebaseAuthManager instance;
    private FirebaseAuth mAuth;
    private Context context;

    private FirebaseAuthManager(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseAuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseAuthManager(context);
        }
        return instance;
    }

    // Регистрация
    public void register(String email, String password, String username, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            callback.onSuccess(user);
                                        } else {
                                            String error = updateTask.getException() != null ?
                                                    updateTask.getException().getMessage() : "Неизвестная ошибка";
                                            callback.onFailure(error);
                                        }
                                    });
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Неизвестная ошибка";
                        callback.onFailure(error);
                    }
                });
    }

    // Вход
    public void login(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Неизвестная ошибка";
                        callback.onFailure(error);
                    }
                });
    }

    // Выход
    public void logout() {
        mAuth.signOut();
    }

    // Проверка авторизации
    public boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }
}