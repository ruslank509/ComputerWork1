package com.example.computerwork;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class acceptquery extends AppCompatActivity {

    private static final String SUPABASE_URL = "https://lincidhuobbcjwwccsty.supabase.co/rest/v1/Queries";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxpbmNpZGh1b2JiY2p3d2Njc3R5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQ3MTQyMjEsImV4cCI6MjA1MDI5MDIyMX0.H7pAHmmfd1-bdeammV-UqdC9aaCQU0GOnkX4CDdYg4s";

    private Spinner spinnerQueries;
    private EditText conclusionEditText;
    private Button updateButton;

    private final ArrayList<String> numberQueryList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceptquery);

        spinnerQueries = findViewById(R.id.spinnerQueries);
        conclusionEditText = findViewById(R.id.editTextConclusion);
        updateButton = findViewById(R.id.button9);

        updateButton.setOnClickListener(v -> {
            String conclusion = conclusionEditText.getText().toString().trim();
            String Status = "Рассмотрен";
            if (conclusion.isEmpty()) {
                Toast.makeText(this, "Вы не ввели заключение!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (spinnerQueries.getSelectedItem() == null) {
                Toast.makeText(this, "Выберите заявку!", Toast.LENGTH_SHORT).show();
                return;
            }
            String selectedNumberQuery = spinnerQueries.getSelectedItem().toString();
            updateQueryWithConclusion(selectedNumberQuery, Status, conclusion);
        });

        fetchNumberQueries();
    }

    private void fetchNumberQueries() {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "?select=NumberQuery")
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(acceptquery.this, "Ошибка загрузки заявок", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        numberQueryList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            numberQueryList.add(obj.getString("NumberQuery"));
                        }

                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(acceptquery.this, android.R.layout.simple_spinner_item, numberQueryList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerQueries.setAdapter(adapter);
                        });

                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(acceptquery.this, "Ошибка парсинга данных", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(acceptquery.this, "Ошибка ответа сервера", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateQueryWithConclusion(String numberQuery, String status, String conclusion) {
        JSONObject updateData = new JSONObject();
        try {
            updateData.put("NumberQuery", numberQuery);
            updateData.put("Status", "Рассмотрен");
            updateData.put("Conslusion", conclusion);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка формирования данных", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(updateData.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "?NumberQuery=eq." + numberQuery)
                .patch(body)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(acceptquery.this, "Ошибка обновления", Toast.LENGTH_SHORT).show());
                Log.e("Supabase", "Ошибка обновления", e);
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : "empty body";
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(acceptquery.this, "Заявка обновлена!", Toast.LENGTH_SHORT).show();
                        fetchNumberQueries();
                    } else {
                        Toast.makeText(acceptquery.this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
                        Log.e("Supabase", "Ошибка: " + response.code() + " " + response.message() + " | " + responseBody);
                    }
                });
            }
        });
    }

    public void ReturnToMs(View view) {
        startActivity(new Intent(this, master.class));
    }
}












