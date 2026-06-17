package com.example.computerwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class master extends AppCompatActivity {

    private static final int DOUBLE_PRESS_DELAY = 2000;
    private static final String PREFS_NAME = "UserSession";
    private static final String KEY_USER_LOGIN = "user_login";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private boolean doubleBackToExitPressedOnce = false;
    private Handler backPressHandler;
    private Runnable resetBackPressFlag;
    private TextView textViewWelcome;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_master);

        sessionManager = new SessionManager(this);
        backPressHandler = new Handler(Looper.getMainLooper());
        textViewWelcome = findViewById(R.id.textView3);
        updateWelcomeText();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.master), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBackPressedHandler();
    }
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

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!doubleBackToExitPressedOnce) {
                    doubleBackToExitPressedOnce = true;

                    Toast.makeText(master.this,
                            "Нажмите ещё раз для выхода",
                            Toast.LENGTH_SHORT).show();

                    resetBackPressFlag = () -> doubleBackToExitPressedOnce = false;
                    backPressHandler.postDelayed(resetBackPressFlag, DOUBLE_PRESS_DELAY);

                }

                showExitConfirmationDialog();
            }
        });
    }
    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Выход из приложения")
                .setMessage("Вы действительно хотите выйти?")
                .setIcon(android.R.drawable.ic_dialog_alert)

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
    private void ExitFromMenu(){
        new AlertDialog.Builder(this)
                .setTitle("Выход из аккаунта")
                .setMessage("Вы действительно хотите выйти из учётной записи?")
                .setIcon(R.drawable.ic_launcher_foreground)

                .setPositiveButton("Да, выйти", (dialog, which) -> {
                    sessionManager.logout();

                    Intent intent = new Intent(master.this, authorization.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })

                .setNegativeButton("Нет, остаться", (dialog, which) -> {
                    dialog.dismiss();
                })

                .setCancelable(false)
                .show();
    }

    private void saveSessionAndExitApp() {
        sessionManager.updateLastActivity();
        Toast.makeText(this, "Сеанс сохранён. До встречи!", Toast.LENGTH_SHORT).show();
        finishAffinity();
    }

    public void Exit(View view) {
        showExitConfirmationDialog();
    }
    public void Authorize (View view){
        ExitFromMenu();
    }

    public void Accept(View view) {
        Intent intent = new Intent(this, acceptquery.class);
        startActivity(intent);
    }

    public void Check(View view) {
        Intent intent = new Intent(this, checkquery.class);
        startActivity(intent);
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
