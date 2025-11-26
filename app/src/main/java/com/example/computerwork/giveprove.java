package com.example.computerwork;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class giveprove extends AppCompatActivity {

    private static final String SUPABASE_URL = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Users";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZvbXpjZG5pa2R3aGljZWNscG9jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE0NzEyMzUsImV4cCI6MjA3NzA0NzIzNX0.yeveyPQEG7FdYHsf4ga9GDB3dAmiWGhqjJ1wlrMrWlo";

    private Spinner spinnerUsers;
    private Spinner spinnerRoles;
    private Button updateButton;
    String[] typeOptions = {
            "Мастер", "Пользователь"
    };

    private final ArrayList<String> UserLoginList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giveprove);

        spinnerUsers = findViewById(R.id.spinnerUser);
        spinnerRoles = findViewById(R.id.spinnerRole);
        updateButton = findViewById(R.id.button10);
        spinnerRoles.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typeOptions));
        fetchUserLogin();
        updateButton.setOnClickListener(v -> {
            String selectedUserLogin = spinnerUsers.getSelectedItem().toString();
            String selectedRole = spinnerRoles.getSelectedItem().toString();
                updateUsersRole(selectedUserLogin, selectedRole);
        });
    }

    private void fetchUserLogin() {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "?select=Login")
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(giveprove.this, "Ошибка загрузки пользователей", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        UserLoginList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            UserLoginList.add(obj.getString("Login"));
                        }

                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(giveprove.this, android.R.layout.simple_spinner_item, UserLoginList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerUsers.setAdapter(adapter);
                        });

                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(giveprove.this, "Ошибка парсинга данных", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(giveprove.this, "Ошибка ответа сервера", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateUsersRole(String selectedUserLogin, String selectedRole) {
        JSONObject updateData = new JSONObject();
        try {
            updateData.put("Login", selectedUserLogin);
            updateData.put("Status", selectedRole);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка формирования данных", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(updateData.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "?Login=eq." + selectedUserLogin)
                .patch(body)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(giveprove.this, "Ошибка обновления", Toast.LENGTH_SHORT).show());
                Log.e("Supabase", "Ошибка обновления", e);
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : "empty body";
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(giveprove.this, "Должность обновлена!", Toast.LENGTH_SHORT).show();
                        //fetchNumberQueries();
                    } else {
                        Toast.makeText(giveprove.this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
                        Log.e("Supabase", "Ошибка: " + response.code() + " " + response.message() + " | " + responseBody);
                    }
                });
            }
        });
    }

    public void ReturnToUserFunc(View view) {
        startActivity(new Intent(this, user.class));
    }
}