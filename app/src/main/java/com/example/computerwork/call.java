package com.example.computerwork;

import android.content.Intent;
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

    private EditText problemEditText;
    private EditText nameEditText;
    private Button addButton;

    private CallRepository callRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        problemEditText = findViewById(R.id.editTextText2);
        nameEditText = findViewById(R.id.editTextText4);
        addButton = findViewById(R.id.button3);

        callRepository = new CallRepository(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.call), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String problem = problemEditText.getText().toString().trim();
            String status = "В обработке";

            if (name.isEmpty() && problem.isEmpty()) {
                Toast.makeText(this, "Заполните обязательные поля!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (problem.isEmpty()) {
                Toast.makeText(this, "Вы не указали проблему!", Toast.LENGTH_SHORT).show();
                problemEditText.requestFocus();
                return;
            }
            if (name.isEmpty()) {
                Toast.makeText(this, "Вы не указали логин!", Toast.LENGTH_SHORT).show();
                nameEditText.requestFocus();
                return;
            }

            setButtonLoading(true);
            callRepository.checkUserExists(name, new CallRepository.UserCheckCallback() {

                @Override
                public void onUserExists() {
                    Log.d(TAG, "✅ Пользователь найден: " + name);
                    callRepository.sendBookingToSupabase(name, problem, status, new CallRepository.Callback() {

                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(call.this, "✅ Ваш запрос добавлен!", Toast.LENGTH_SHORT).show();

                                nameEditText.setText("");
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
                    Log.w(TAG, "Пользователь не найден: " + name);

                    runOnUiThread(() -> {
                        Toast.makeText(call.this, "Пользователь '" + name + "' не найден в системе!", Toast.LENGTH_LONG).show();

                        nameEditText.setError("Такого пользователя нет");
                        nameEditText.requestFocus();
                        setButtonLoading(false);
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "⚠️ Ошибка проверки: " + errorMessage);

                    runOnUiThread(() -> {
                        Toast.makeText(call.this, "⚠️ " + errorMessage, Toast.LENGTH_LONG).show();
                        setButtonLoading(false);
                    });
                }
            });
        });
    }
    private void setButtonLoading(boolean isLoading) {
        if (isLoading) {
            addButton.setEnabled(false);
            addButton.setText("Проверка...");
        } else {
            addButton.setEnabled(true);
            addButton.setText("Отправить");
        }
    }
    public void ReturnToUs(View view) {
        Intent intent = new Intent(this, menuuser.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // 🔹 Перехват нажатия кнопки "Назад"
        super.onBackPressed();
        ReturnToUs(null);
    }
}

