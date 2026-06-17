package com.example.computerwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView; // 🔹 Добавляем импорт
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class menuuser extends AppCompatActivity {

    private static final int DOUBLE_PRESS_DELAY = 2000;

    // 🔹 Ключи должны ТОЧНО совпадать с теми, что в call.java
    private static final String PREFS_NAME = "UserSession";
    private static final String KEY_USER_LOGIN = "user_login";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private boolean doubleBackToExitPressedOnce = false;
    private Handler backPressHandler;
    private Runnable resetBackPressFlag;

    private SessionManager sessionManager;

    // 🔹 Поле для TextView с приветствием
    private TextView textViewWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menuuser);

        sessionManager = new SessionManager(this);
        backPressHandler = new Handler(Looper.getMainLooper());

        // 🔹 Инициализация TextView приветствия
        // Замените R.id.textViewWelcome на реальный ID вашего TextView в layout
        textViewWelcome = findViewById(R.id.textView7);
        updateWelcomeText(); // 🔹 Обновляем приветствие при старте

        // 🔹 БЕЗОПАСНО: используем системный ID вместо R.id.menuuser
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        setupBackPressedHandler();
    }

    // 🔹 Метод для обновления текста приветствия
    private void updateWelcomeText() {
        // Получаем логин из SessionManager (консистентно с другими активностями)
        String login = sessionManager.getSavedLogin();

        // Если в SessionManager пусто, пробуем получить из локальных SharedPreferences
        if (login == null || login.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            login = prefs.getString(KEY_USER_LOGIN, "");
        }

        // Формируем текст: логин или "пользователь" по умолчанию
        String userName = (login != null && !login.isEmpty()) ? login : "пользователь";
        String welcomeText = "Добро пожаловать, " + userName + "!";

        // Обновляем TextView, если он найден в layout
        if (textViewWelcome != null) {
            textViewWelcome.setText(welcomeText);
        }
    }

    // 🔹 Метод сохранения сессии (вызывать ПОСЛЕ успешной проверки логина/пароля)
    public void saveAuthenticatedUserSession(String login) {
        if (login == null || login.trim().isEmpty()) return;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_USER_LOGIN, login.trim())
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();

        // 🔹 Также обновляем ваш SessionManager (если он хранит логин)
        sessionManager.updateLastActivity();

        // 🔹 Обновляем приветствие, если активность уже создана
        updateWelcomeText();
    }

    // 🔹 Метод очистки сессии (при выходе из аккаунта)
    public void clearAuthenticatedUserSession() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!doubleBackToExitPressedOnce) {
                    doubleBackToExitPressedOnce = true;

                    Toast.makeText(menuuser.this,
                            "Нажмите ещё раз для выхода",
                            Toast.LENGTH_SHORT).show();

                    resetBackPressFlag = () -> doubleBackToExitPressedOnce = false;
                    backPressHandler.postDelayed(resetBackPressFlag, DOUBLE_PRESS_DELAY);
                    return;
                }
                showExitConfirmationDialog();
            }
        });
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Выход из приложения")
                .setMessage("Вы действительно хотите выйти?\n\nВаш вход будет сохранён — при следующем запуске не потребуется вводить пароль.")
                .setIcon(R.drawable.ic_launcher_foreground)
                .setPositiveButton("Да, выйти", (dialog, which) -> {
                    saveSessionAndExitApp();
                })
                .setNegativeButton("Нет, остаться", (dialog, which) -> {
                    dialog.dismiss();
                    doubleBackToExitPressedOnce = false;
                })
                .setCancelable(false)
                .show();
    }

    private void exitFromAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Выход из аккаунта")
                .setMessage("Вы действительно хотите выйти из учётной записи?")
                .setIcon(R.drawable.ic_launcher_foreground)
                .setPositiveButton("Да, выйти", (dialog, which) -> {
                    // 🔹 Очищаем сессию для call.java
                    clearAuthenticatedUserSession();
                    sessionManager.logout();

                    Intent intent = new Intent(menuuser.this, authorization.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Нет, остаться", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void saveSessionAndExitApp() {
        sessionManager.updateLastActivity();
        Toast.makeText(this, "Сеанс сохранён. До встречи!", Toast.LENGTH_SHORT).show();
        finishAffinity();
    }

    public void ExitUser(View view) {
        exitFromAccount();
    }

    public void Product(View view) {
        Intent intent = new Intent(this, order.class);
        startActivity(intent);
    }
    public void CheckOrder(View view) {
        Intent intent = new Intent(this, checkusersorder.class);
        startActivity(intent);
    }


    public void Order(View view) {
        // 🔹 Перед переходом можно проверить сессию (опционально)
        if (!isSessionValid()) {
            Toast.makeText(this, "Требуется авторизация", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, authorization.class);
            startActivity(intent);
            return;
        }
        Intent intent = new Intent(this, call.class);
        startActivity(intent);
    }

    // 🔹 Проверка: есть ли активная сессия
    private boolean isSessionValid() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 🔹 Обновляем приветствие при возврате в активность
        updateWelcomeText();
        if (sessionManager.isSessionValid()) {
            sessionManager.updateLastActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sessionManager.updateLastActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backPressHandler != null && resetBackPressFlag != null) {
            backPressHandler.removeCallbacks(resetBackPressFlag);
        }
    }
}
