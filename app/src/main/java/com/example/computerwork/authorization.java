package com.example.computerwork;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class authorization extends AppCompatActivity {

    private static final String TAG = "authorization";
    private static final String SUPABASE_URL_USERS = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Users";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZvbXpjZG5pa2R3aGljZWNscG9jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE0NzEyMzUsImV4cCI6MjA3NzA0NzIzNX0.yeveyPQEG7FdYHsf4ga9GDB3dAmiWGhqjJ1wlrMrWlo";

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        sessionManager = new SessionManager(this);

        if (sessionManager.isSessionValid()) {
            autoLogin();
            return;
        }

        initLoginForm();
    }
    private void autoLogin() {
        String savedLogin = sessionManager.getSavedLogin();
        String savedPassword = sessionManager.getSavedPassword();
        String savedRole = sessionManager.getSavedRole();

        Log.d(TAG, "Авто-вход для пользователя: " + savedLogin);

        Toast.makeText(this, "Добро пожаловать обратно, " + savedLogin + "!", Toast.LENGTH_SHORT).show();

        sessionManager.updateLastActivity();
        navigateByRole(savedRole);
    }

    private void navigateByRole(String role) {
        Intent intent;

        if ("Пользователь".equalsIgnoreCase(role)) {
            intent = new Intent(this, menuuser.class);
        } else if ("Администратор".equalsIgnoreCase(role)) {
            intent = new Intent(this, administrator.class);
        } else if ("Мастер".equalsIgnoreCase(role)) {
            intent = new Intent(this, master.class);
        } else {
            sessionManager.logout();
            return;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private void initLoginForm() {
        EditText loginEdit = findViewById(R.id.editTextText3);
        EditText passwordEdit = findViewById(R.id.editTextTextPassword3);
        Button loginBtn = findViewById(R.id.button);

        if (sessionManager.hasSavedLogin()) {
            loginEdit.setText(sessionManager.getSavedLogin());
        }

        loginBtn.setOnClickListener(v -> {
            String login = loginEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                return;
            }

            authorizeUser(login, password);
        });
    }
    private void authorizeUser(String login, String password) {
        OkHttpClient client = new OkHttpClient();

        String filterUrl = SUPABASE_URL_USERS + "?Login=eq." + login + "&select=Password,Status";
        Request request = new Request.Builder()
                .url(filterUrl)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(authorization.this, "Ошибка подключения", Toast.LENGTH_SHORT).show()
                );
                Log.e(TAG, "Ошибка подключения", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(authorization.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                String jsonResponse = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(jsonResponse);
                    if (jsonArray.length() == 0) {
                        runOnUiThread(() ->
                                Toast.makeText(authorization.this, "Пользователь не найден!", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    JSONObject user = jsonArray.getJSONObject(0);
                    String storedPassword = user.getString("Password");
                    String status = user.optString("Status", "");

                    if (storedPassword.equals(password)) {
                        runOnUiThread(() -> {
                            sessionManager.saveLoginData(login, password, status);

                            Toast.makeText(authorization.this,
                                    "Вход выполнен! Добро пожаловать, " + login,
                                    Toast.LENGTH_SHORT).show();

                            navigateByRole(status);
                        });
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(authorization.this, "Неверный пароль", Toast.LENGTH_SHORT).show()
                        );
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Ошибка разбора JSON", e);
                    runOnUiThread(() ->
                            Toast.makeText(authorization.this, "Ошибка данных", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
    public void junction(View view) {
        Intent intent = new Intent(this, registration.class);
        startActivity(intent);
    }

    public void openWebsite(View view) {
        String websiteUrl = "https://ruslank509.github.io/ComputerWork1/".trim();
        try {
            Uri uri = Uri.parse(websiteUrl);
            if (uri.getScheme() == null) {
                throw new IllegalArgumentException("Некорректный URL");
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            Intent chooser = Intent.createChooser(intent, "Выберите браузер");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            } else {
                Toast.makeText(this, "Нет приложений для открытия веб-страниц", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Website", "Ошибка при открытии сайта", e);
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}