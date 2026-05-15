package com.example.computerwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class call extends AppCompatActivity {

    private static final String TAG = "callActivity";
    private static final String PREFS_NAME = "UserSession";
    private static final String KEY_USER_LOGIN = "user_login";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private EditText problemEditText;
    private Button addButton;
    private CallRepository callRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isUserAuthenticated()) {
            Log.w(TAG, "Пользователь не авторизован. Перенаправление...");
            redirectToLogin();
            return; // Прерываем выполнение, чтобы не вызывать setContentView
        }

        setContentView(R.layout.activity_call);

        problemEditText = findViewById(R.id.editTextText2);
        addButton = findViewById(R.id.button3);

        if (problemEditText == null || addButton == null) {
            Log.e(TAG, "Ошибка: ID EditText или Button не найдены в XML");
            Toast.makeText(this, "Ошибка загрузки формы", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        callRepository = new CallRepository(this);

        addButton.setOnClickListener(v -> {
            String problem = problemEditText.getText().toString().trim();
            String login = getAuthenticatedUserLogin();

            // Валидация полей
            if (problem.isEmpty()) {
                Toast.makeText(this, "Вы не указали проблему!", Toast.LENGTH_SHORT).show();
                problemEditText.requestFocus();
                return;
            }

            if (login == null || login.isEmpty()) {
                Toast.makeText(this, "Ошибка сессии. Войдите заново.", Toast.LENGTH_LONG).show();
                redirectToLogin();
                return;
            }

            setButtonLoading(true);
            String status = "В обработке";

            callRepository.checkUserExists(login, new CallRepository.UserCheckCallback() {

                @Override
                public void onUserExists() {
                    callRepository.sendBookingToSupabase(login, problem, status, new CallRepository.Callback() {

                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(call.this, "Ваш запрос добавлен!", Toast.LENGTH_SHORT).show();
                                problemEditText.setText("");
                                setButtonLoading(false);
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            runOnUiThread(() -> {
                                Log.e(TAG, "Ошибка отправки: " + errorMessage);
                                Toast.makeText(call.this, errorMessage, Toast.LENGTH_LONG).show();
                                setButtonLoading(false);
                            });
                        }
                    });
                }

                @Override
                public void onUserNotFound() {
                    runOnUiThread(() -> {
                        Log.w(TAG, "Пользователь не найден в БД: " + login);
                        Toast.makeText(call.this, "Пользователь не найден в системе", Toast.LENGTH_LONG).show();
                        setButtonLoading(false);
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Ошибка проверки: " + errorMessage);
                        Toast.makeText(call.this, errorMessage, Toast.LENGTH_LONG).show();
                        setButtonLoading(false);
                    });
                }
            });
        });
    }

    private boolean isUserAuthenticated() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private String getAuthenticatedUserLogin() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USER_LOGIN, null);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, menuuser.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setButtonLoading(boolean isLoading) {
        runOnUiThread(() -> {
            if (isLoading) {
                addButton.setEnabled(false);
                addButton.setText("Проверка...");
            } else {
                addButton.setEnabled(true);
                addButton.setText("Отправить");
            }
        });
    }

    public void ReturnToUs(View view) {
        Intent intent = new Intent(this, menuuser.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ReturnToUs(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

