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
import java.util.regex.*;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class registration extends AppCompatActivity {

    private static final String SUPABASE_URL_CLIENTS = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Clients";
    private static final String SUPABASE_URL_USERS = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Users";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZvbXpjZG5pa2R3aGljZWNscG9jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE0NzEyMzUsImV4cCI6MjA3NzA0NzIzNX0.yeveyPQEG7FdYHsf4ga9GDB3dAmiWGhqjJ1wlrMrWlo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        Button AddButton = findViewById(R.id.buttonReg);
        EditText Login = findViewById(R.id.editTextTextLog);
        EditText Email = findViewById(R.id.editTextTextEmailAddress);
        EditText Password = findViewById(R.id.editTextTextPassword);
        EditText RepeatPassword = findViewById(R.id.editTextTextPassword2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registration), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AddButton.setOnClickListener(v -> {
            String login = Login.getText().toString().trim();
            String email = Email.getText().toString().trim();
            String password = Password.getText().toString().trim();
            String repeatpassword = RepeatPassword.getText().toString().trim();

            String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            if (!email.matches(EMAIL_REGEX)) {
                Toast.makeText(getApplicationContext(), "Некорректный email!", Toast.LENGTH_SHORT).show();
            }

            if (!repeatpassword.equals(password)) {
                Toast.makeText(this, "Пароль не подтверждён!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (login.isEmpty() || email.isEmpty() || password.isEmpty() || repeatpassword.isEmpty()) {
                Toast.makeText(this, "Заполните обязательные поля!", Toast.LENGTH_SHORT).show();
                return;
            }
            else if (email.matches(EMAIL_REGEX)) {
                checkIfEmailExists(login, email, password);
            }
        });
    }

    private void checkIfEmailExists(String login, String email, String password) {
        OkHttpClient client = new OkHttpClient();
        String url = SUPABASE_URL_USERS + "?Email=eq." + email;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(registration.this, "Ошибка при проверке email", Toast.LENGTH_SHORT).show();
                    Log.e("EmailCheck", "Ошибка проверки", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "[]";
                    if (responseBody.equals("[]")) {
                        sendBookingToSupabase(login, email, password);
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(registration.this, "Пользователь с этим email уже зарегистрирован!", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(registration.this, "Ошибка при проверке email", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void sendBookingToSupabase(String login, String email, String password) {
        JSONObject clientsData = new JSONObject();
        try {
            clientsData.put("Login", login);
            clientsData.put("Email", email);
            clientsData.put("Password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody clientsBody = RequestBody.create(
                clientsData.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request requestClients = new Request.Builder()
                .url(SUPABASE_URL_CLIENTS)
                .post(clientsBody)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();
//Ошибка добавления
        client.newCall(requestClients).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(registration.this, "Ошибка при добавлении в Clients", Toast.LENGTH_SHORT).show();
                    Log.e("SupabaseRequest", "Ошибка Clients", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject usersData = new JSONObject();
                    try {
                        usersData.put("Login", login);
                        usersData.put("Email", email);
                        usersData.put("Password", password);
                        usersData.put("Status", "Пользователь");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }

                    RequestBody usersBody = RequestBody.create(
                            usersData.toString(),
                            MediaType.get("application/json; charset=utf-8")
                    );

                    Request requestUsers = new Request.Builder()
                            .url(SUPABASE_URL_USERS)
                            .post(usersBody)
                            .addHeader("apikey", SUPABASE_API_KEY)
                            .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                            .addHeader("Content-Type", "application/json")
                            .build();

                    client.newCall(requestUsers).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(() -> {
                                Toast.makeText(registration.this, "Ошибка при добавлении в Users", Toast.LENGTH_SHORT).show();
                                Log.e("SupabaseRequest", "Ошибка Users", e);
                            });
                        }
//Успешная регистрация
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                runOnUiThread(() -> {
                                    Toast.makeText(registration.this, "Вы успешно зарегистрированы!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(registration.this, authorization.class);
                                    startActivity(intent);
                                });
                            } else {
                                String errorBody = response.body() != null ? response.body().string() : "empty body";
                                Log.e("SupabaseRequest", "Ошибка Users: " + response.code() + " - " + response.message() + " | " + errorBody);
                                runOnUiThread(() -> {
                                    Toast.makeText(registration.this, "Ошибка при добавлении в Users", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });

                } else {
                    String errorBody = response.body() != null ? response.body().string() : "empty body";
                    Log.e("SupabaseRequest", "Ошибка Clients: " + response.code() + " - " + response.message() + " | " + errorBody);
                    runOnUiThread(() -> {
                        Toast.makeText(registration.this, "Ошибка при добавлении в Clients", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    public void SuccessRegistration(View view) {
        Toast toast = Toast.makeText(this, "Регистрация прошла успешно!", Toast.LENGTH_SHORT);
        toast.show();
        Intent intent = new Intent(this, authorization.class);
        startActivity(intent);
    }

    public void junctionReturn(View view) {
        Intent intent = new Intent(this, authorization.class);
        startActivity(intent);
    }
}


