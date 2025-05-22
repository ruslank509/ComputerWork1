package com.example.computerwork;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class call extends AppCompatActivity {

    private EditText problemEditText;
    private EditText nameEditText;
    private Button addButton;

    private CallRepository callRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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

            if (problem.isEmpty() && name.isEmpty()) {
                Toast.makeText(this, "Заполните обязательные поля!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (problem.isEmpty()) {
                Toast.makeText(this, "Вы не указали проблему!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (name.isEmpty()) {
                Toast.makeText(this, "Вы не указали логин!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Вызов сетевого запроса через репозиторий
            callRepository.sendBookingToSupabase(name, problem, status, new CallRepository.Callback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() ->
                            Toast.makeText(call.this, "Ваш запрос добавлен!", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onFailure(String errorMessage) {
                    runOnUiThread(() ->
                            Toast.makeText(call.this, errorMessage, Toast.LENGTH_SHORT).show()
                    );
                }
            });
        });
    }

    public void ReturnToUs(View view){
        Intent intent = new Intent(this, menuuser.class);
        startActivity(intent);
    }
}

