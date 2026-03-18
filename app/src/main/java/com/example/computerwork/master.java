package com.example.computerwork;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
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

    private boolean doubleBackToExitPressedOnce = false;
    private Handler backPressHandler;
    private Runnable resetBackPressFlag;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_master);

        sessionManager = new SessionManager(this);
        backPressHandler = new Handler(Looper.getMainLooper());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.master), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBackPressedHandler();
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

    private void saveSessionAndExitApp() {
        sessionManager.updateLastActivity();
        Toast.makeText(this, "Сеанс сохранён. До встречи!", Toast.LENGTH_SHORT).show();
        finishAffinity();
    }

    public void Exit(View view) {
        showExitConfirmationDialog();
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
