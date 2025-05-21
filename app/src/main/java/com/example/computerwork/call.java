package com.example.computerwork;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class call extends AppCompatActivity {

    private static final String SUPABASE_URL = "https://lincidhuobbcjwwccsty.supabase.co/rest/v1/Queries";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxpbmNpZGh1b2JiY2p3d2Njc3R5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQ3MTQyMjEsImV4cCI6MjA1MDI5MDIyMX0.H7pAHmmfd1-bdeammV-UqdC9aaCQU0GOnkX4CDdYg4s";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_call);
        EditText Problem = findViewById(R.id.editTextText2);
        EditText Name = findViewById(R.id.editTextText4);
        Button AddButton = findViewById(R.id.button3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.call), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AddButton.setOnClickListener(v -> {
            String name = Name.getText().toString().trim();
            String problem = Problem.getText().toString().trim();
            String status = "В обработке";
            if (problem.isEmpty() & name.isEmpty()) {
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
            else {
                sendBookingToSupabase(name, problem, status);
            }
        });
    }
    private void sendBookingToSupabase(String login, String problem, String status) {
        // Данные для Clients (только Login, Email, Password)
        JSONObject clientsData = new JSONObject();
        try {
            clientsData.put("LoginUser", login);
            clientsData.put("Crush", problem);
            clientsData.put("Status", status);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(
                clientsData.toString(),
                MediaType.get("application/json; charset=utf-8")
        );
        Request requestUsers = new Request.Builder()
                .url(SUPABASE_URL)
                .post(body)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(requestUsers).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(call.this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
                    Log.e("SupabaseRequest", "Ошибка", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(call.this, "Ваш запрос добавлен!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "empty body";
                    Log.e("SupabaseRequest", "Ошибка Users: " + response.code() + " - " + response.message() + " | " + errorBody);
                    runOnUiThread(() -> {
                        Toast.makeText(call.this, "Ошибка при добавлении данных", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    public void ReturnToUs(View view){
        Intent intent = new Intent(this, menuuser.class);
        startActivity(intent);
    }
}
