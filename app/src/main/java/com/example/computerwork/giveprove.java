package com.example.computerwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class giveprove extends AppCompatActivity {

    private static final String TAG = "giveprove";
    private static final String SUPABASE_URL = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Users";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZvbXpjZG5pa2R3aGljZWNscG9jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE0NzEyMzUsImV4cCI6MjA3NzA0NzIzNX0.yeveyPQEG7FdYHsf4ga9GDB3dAmiWGhqjJ1wlrMrWlo";

    // 🔹 Константы кэширования
    private static final String PREF_CACHE_NAME = "GiveProveCache";
    private static final String KEY_CACHED_USERS = "cachedUserLogins";
    private static final String KEY_CACHE_TIMESTAMP = "cacheTimestamp";
    private static final long CACHE_DURATION_MS = 10 * 60 * 1000;

    private static final int TIMEOUT_SECONDS = 30;

    private Spinner spinnerUsers;
    private Spinner spinnerRoles;
    private Button updateButton;

    private final ArrayList<String> userLoginList = new ArrayList<>();
    private final ArrayList<String> cachedUserList = new ArrayList<>();
    private OkHttpClient client;
    private SharedPreferences cachePrefs;
    private Handler mainHandler;

    private boolean isActivityActive = false;

    private final String[] roleOptions = { "Мастер", "Пользователь" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giveprove);

        isActivityActive = true;
        mainHandler = new Handler(Looper.getMainLooper());
        cachePrefs = getSharedPreferences(PREF_CACHE_NAME, MODE_PRIVATE);

        client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        spinnerUsers = findViewById(R.id.spinnerUser);
        spinnerRoles = findViewById(R.id.spinnerRole);
        updateButton = findViewById(R.id.button10);

        spinnerRoles.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roleOptions));

        loadUsersWithCache();

        updateButton.setOnClickListener(v -> {
            String selectedUserLogin = getSpinnerValue(spinnerUsers);
            String selectedRole = getSpinnerValue(spinnerRoles);
            updateUserRole(selectedUserLogin, selectedRole);
        });

        spinnerUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (!selected.equals("Загрузка...") && !selected.equals("Нет данных")) {
                    Log.d(TAG, "Выбран пользователь: " + selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private String getSpinnerValue(Spinner spinner) {
        if (spinner == null) return "";
        Object item = spinner.getSelectedItem();
        return item != null ? item.toString().trim() : "";
    }

    private void loadUsersWithCache() {
        if (loadUsersFromCache()) {
            Log.d(TAG, "Загружено из кэша: " + cachedUserList.size());
            updateSpinner(cachedUserList);
        } else {
            Log.d(TAG, "Кэш пуст или устарел");
            ArrayList<String> loadingList = new ArrayList<>();
            loadingList.add("Загрузка...");
            updateSpinner(loadingList);
        }
        fetchUserLoginsFromNetwork(false);
    }

    private boolean loadUsersFromCache() {
        if (isCacheExpired()) {
            Log.d(TAG, "Кэш устарел");
            return false;
        }

        String cachedJson = cachePrefs.getString(KEY_CACHED_USERS, "");
        if (cachedJson.isEmpty()) {
            return false;
        }

        try {
            JSONArray jsonArray = new JSONArray(cachedJson);
            cachedUserList.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                cachedUserList.add(jsonArray.getString(i));
            }
            return !cachedUserList.isEmpty();

        } catch (JSONException e) {
            Log.e(TAG, "Ошибка парсинга кэша: " + e.getMessage(), e);
            clearUserCache();
            return false;
        }
    }

    private void saveUsersToCache(ArrayList<String> users) {
        if (users == null || users.isEmpty()) return;

        JSONArray jsonArray = new JSONArray(users);
        cachePrefs.edit()
                .putString(KEY_CACHED_USERS, jsonArray.toString())
                .putLong(KEY_CACHE_TIMESTAMP, System.currentTimeMillis())
                .apply();
        Log.d(TAG, "Сохранено в кэш: " + users.size());
    }

    private boolean isCacheExpired() {
        long lastUpdate = cachePrefs.getLong(KEY_CACHE_TIMESTAMP, 0);
        return (System.currentTimeMillis() - lastUpdate) > CACHE_DURATION_MS;
    }

    private void clearUserCache() {
        cachePrefs.edit()
                .remove(KEY_CACHED_USERS)
                .remove(KEY_CACHE_TIMESTAMP)
                .apply();
    }

    private void fetchUserLoginsFromNetwork(boolean showErrors) {
        String url = SUPABASE_URL.trim() + "?select=Login&order=Login.asc";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Ошибка сети: " + e.getMessage(), e);
                if (showErrors && isActivityActive) {
                    runOnUiThreadSafe(() ->
                            Toast.makeText(giveprove.this, "Ошибка подключения", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                String jsonResponse = "";
                try {
                    if (response.body() != null) {
                        jsonResponse = response.body().string();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Ошибка чтения ответа", e);
                    if (showErrors && isActivityActive) {
                        runOnUiThreadSafe(() ->
                                Toast.makeText(giveprove.this, "Ошибка чтения данных", Toast.LENGTH_SHORT).show()
                        );
                    }
                    return;
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }

                try {
                    ArrayList<String> freshList = parseUserLoginsFromJson(jsonResponse);

                    if (isActivityActive) {
                        runOnUiThreadSafe(() -> {
                            userLoginList.clear();
                            userLoginList.addAll(freshList);
                            saveUsersToCache(userLoginList);
                            updateSpinner(userLoginList);

                            if (showErrors && !userLoginList.isEmpty()) {
                                Toast.makeText(giveprove.this,
                                        "Обновлено: " + userLoginList.size() + " пользователей",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Ошибка парсинга: " + e.getMessage() + " | Ответ: " + jsonResponse, e);
                    if (showErrors && isActivityActive) {
                        runOnUiThreadSafe(() ->
                                Toast.makeText(giveprove.this, "Ошибка данных", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }
        });
    }
    private ArrayList<String> parseUserLoginsFromJson(String jsonResponse) throws JSONException {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            return new ArrayList<>();
        }

        JSONArray array = new JSONArray(jsonResponse.trim());
        ArrayList<String> freshList = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            String login = obj.optString("Login", "");
            if (!login.isEmpty()) {
                freshList.add(login);
            }
        }
        return freshList;
    }

    private void updateUserRole(String selectedUserLogin, String selectedRole) {
        if (selectedUserLogin.isEmpty() ||
                selectedUserLogin.equals("Загрузка...") ||
                selectedUserLogin.equals("Нет данных")) {
            Toast.makeText(this, "Выберите пользователя", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject updateData = new JSONObject();
        try {
            updateData.put("Status", selectedRole);
        } catch (JSONException e) {
            Log.e(TAG, "Ошибка JSON", e);
            Toast.makeText(this, "Ошибка подготовки", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(
                updateData.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        String url = SUPABASE_URL.trim() + "?Login=eq." + selectedUserLogin;

        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Ошибка обновления: " + e.getMessage(), e);
                if (isActivityActive) {
                    runOnUiThreadSafe(() ->
                            Toast.makeText(giveprove.this, "Ошибка сети", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                String responseBody = "";
                try {
                    if (response.body() != null) {
                        responseBody = response.body().string();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Ошибка чтения", e);
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }

                if (isActivityActive) {
                    String finalResponseBody = responseBody;
                    runOnUiThreadSafe(() -> {
                        if (response.isSuccessful() || response.code() == 204) {
                            Log.d(TAG, "Успех: " + "Пользователь: " + spinnerUsers.getSelectedItem() + " Должность: " + spinnerRoles.getSelectedItem());
                            Toast.makeText(giveprove.this, "Должность обновлена!", Toast.LENGTH_SHORT).show();
                            clearUserCache();
                            fetchUserLoginsFromNetwork(false);
                        } else {
                            Log.e(TAG, "Ошибка: " + response.code() + " | " + finalResponseBody);
                            Toast.makeText(giveprove.this, "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
    private void updateSpinner(ArrayList<String> users) {
        if (!isActivityActive) return;

        if (users == null || users.isEmpty()) {
            users = new ArrayList<>();
            users.add("Нет данных");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                users
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUsers.setAdapter(adapter);
    }

    private void runOnUiThreadSafe(Runnable action) {
        if (isActivityActive && !isFinishing() && !isDestroyed()) {
            runOnUiThread(action);
        }
    }

    public void ReturnToUserFunc(View view) {
        startActivity(new Intent(this, user.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityActive = true;
        if (isCacheExpired() && !userLoginList.isEmpty()) {
            Log.d(TAG, "Кэш устарел, обновляем...");
            fetchUserLoginsFromNetwork(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityActive = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityActive = false;
        if (client != null && client.dispatcher() != null && client.dispatcher().executorService() != null) {
            client.dispatcher().executorService().shutdown();
        }
    }
}