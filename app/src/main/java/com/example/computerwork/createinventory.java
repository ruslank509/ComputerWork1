package com.example.computerwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;
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

public class createinventory extends AppCompatActivity {

    private static final String SUPABASE_URL = "https://lincidhuobbcjwwccsty.supabase.co/rest/v1/Inventories";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxpbmNpZGh1b2JiY2p3d2Njc3R5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQ3MTQyMjEsImV4cCI6MjA1MDI5MDIyMX0.H7pAHmmfd1-bdeammV-UqdC9aaCQU0GOnkX4CDdYg4s";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_createinventory);
        Button AddButton = findViewById(R.id.button14);
        EditText NumberInventory = findViewById(R.id.editTextNumber);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.inventory1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AddButton.setOnClickListener(v -> {
            String Inventory = NumberInventory.getText().toString().trim();
            if (Inventory.isEmpty()) {
                Toast.makeText(this, "Введите номер!", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                sendBookingToSupabase(Inventory);
            }
        });
    }

    public void Return(View view){
        Intent intent = new Intent(this, inventory.class);
        startActivity(intent);
    }
    private void sendBookingToSupabase(String Inventory) {
        JSONObject bookingData = new JSONObject();
        try {
            bookingData.put("Idinventory", Inventory);
            //bookingData.put("user_id", "user-uuid"); // замените на реальный UUID пользователя

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        Log.d("SupabaseRequest", "Отправляем данные в Supabase: " + bookingData.toString());

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(
                bookingData.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(SUPABASE_URL)
                .post(body)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(createinventory.this, "Ошибка при бронировании", Toast.LENGTH_SHORT).show();
                    Log.e("SupabaseRequest", "Ошибка при отправке запроса", e);
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseText = response.body().string();
                    Log.d("SupabaseRequest", "Ответ от Supabase: " + responseText);
                    runOnUiThread(() -> {
                        Toast.makeText(createinventory.this, "Инвентарь успешно создан!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Log.e("SupabaseRequest", "Ошибка: " + response.code() + " - " + response.message());
                    runOnUiThread(() -> {
                        Toast.makeText(createinventory.this, "Ошибка при отправке данных", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}

