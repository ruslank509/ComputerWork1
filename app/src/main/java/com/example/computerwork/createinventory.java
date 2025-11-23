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

public class createinventory extends AppCompatActivity {

    private static final String SUPABASE_URL = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Inventories";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZvbXpjZG5pa2R3aGljZWNscG9jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE0NzEyMzUsImV4cCI6MjA3NzA0NzIzNX0.yeveyPQEG7FdYHsf4ga9GDB3dAmiWGhqjJ1wlrMrWlo";

    private EditText numberInventory;
    private Button addButton;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_createinventory);

        numberInventory = findViewById(R.id.editTextNumber);
        addButton = findViewById(R.id.button14);
        client = new OkHttpClient();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.inventory1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addButton.setOnClickListener(v -> {
            String inventory = numberInventory.getText().toString().trim();
            if (inventory.isEmpty()) {
                Toast.makeText(this, "Введите номер!", Toast.LENGTH_SHORT).show();
            } else {
                sendBookingToSupabase(inventory);
            }
        });
    }

    public void Return(View view) {
        Intent intent = new Intent(this, inventory.class);
        startActivity(intent);
    }

    private void sendBookingToSupabase(String inventoryId) {
        String checkUrl = SUPABASE_URL + "?Idinventory=eq." + inventoryId;

        Request checkRequest = new Request.Builder()
                .url(checkUrl)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(checkRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(createinventory.this, "Ошибка подключения к базе", Toast.LENGTH_SHORT).show();
                    Log.e("Supabase", "Ошибка запроса проверки", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    if (responseBody.equals("[]")) {
                        createInventory(inventoryId);
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(createinventory.this, "Такой номер уже существует!", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(createinventory.this, "Ошибка при проверке данных", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void createInventory(String inventoryId) {
        JSONObject bookingData = new JSONObject();
        try {
            bookingData.put("Idinventory", inventoryId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

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
                runOnUiThread(() ->
                        Toast.makeText(createinventory.this, "Ошибка при создании", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(createinventory.this, "Инвентарь успешно создан!", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(createinventory.this, "Не удалось создать запись", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}


