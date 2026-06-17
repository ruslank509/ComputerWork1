package com.example.computerwork;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CallRepository {

    private static final String TAG = "CallRepository";

    // 🔹 URL-адреса Supabase (проверьте, что они соответствуют вашему проекту)
    private static final String SUPABASE_URL_QUERIES = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Queries";
    private static final String SUPABASE_URL_USERS = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Users";

    // 🔹 API-ключ (в продакшене используйте RLS и ограниченные права!)
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZvbXpjZG5pa2R3aGljZWNscG9jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE0NzEyMzUsImV4cCI6MjA3NzA0NzIzNX0.yeveyPQEG7FdYHsf4ga9GDB3dAmiWGhqjJ1wlrMrWlo";

    private final OkHttpClient client;
    private final Context context;

    // 🔹 Имена колонок в Supabase (должны точно совпадать с вашей схемой!)
    private static final String COLUMN_USER_LOGIN = "Login";        // в таблице Users
    private static final String COLUMN_QUERY_LOGIN = "LoginUser";   // в таблице Queries

    public CallRepository(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient();
    }

    // 🔹 Callback для проверки пользователя
    public interface UserCheckCallback {
        void onUserExists();
        void onUserNotFound();
        void onError(String errorMessage);
    }

    // 🔹 Callback для отправки заявки
    public interface Callback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    /**
     * Проверяет, существует ли пользователь с указанным логином в таблице Users
     */
    public void checkUserExists(String login, UserCheckCallback callback) {
        if (login == null || login.trim().isEmpty()) {
            callback.onUserNotFound();
            return;
        }

        String cleanLogin = login.trim();
        // Фильтр: ищем по полю "Login", выбираем только "id", ограничиваем 1 результатом
        String url = SUPABASE_URL_USERS + "?Login=eq." + cleanLogin + "&select=id&limit=1";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "count=exact")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Ошибка сети при проверке пользователя: " + e.getMessage(), e);
                callback.onError("Ошибка подключения: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "empty";
                        Log.e(TAG, "Ошибка сервера: " + response.code() + " - " + errorBody);
                        callback.onError("Ошибка сервера: " + response.code());
                        return;
                    }

                    String jsonResponse = response.body().string();
                    Log.d(TAG, "Ответ от Supabase (checkUser): " + jsonResponse);

                    JSONArray jsonArray = new JSONArray(jsonResponse);

                    if (jsonArray.length() > 0) {
                        Log.d(TAG, "Пользователь найден: " + cleanLogin);
                        callback.onUserExists();
                    } else {
                        Log.w(TAG, "Пользователь не найден: " + cleanLogin);
                        callback.onUserNotFound();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Ошибка парсинга JSON: " + e.getMessage(), e);
                    callback.onError("Ошибка обработки данных: " + e.getMessage());
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            }
        });
    }

    /**
     * Отправляет новую заявку в таблицу Queries
     * @param login логин пользователя (из сессии)
     * @param problem описание проблемы (из EditText)
     * @param status статус заявки
     * @param callback результат операции
     */

    public void sendBookingToSupabase(String login, String problem, String status, Callback callback) {
        Random rand = new Random();
        int randomNum = rand.nextInt(9000) + 1000; // 4-значный номер: 1000-9999

        JSONObject queryData = new JSONObject();
        try {
            queryData.put("NumberQuery", randomNum);
            queryData.put(COLUMN_QUERY_LOGIN, login);
            queryData.put("Crush", problem);
            queryData.put("Status", status);
        } catch (JSONException e) {
            Log.e(TAG, "Ошибка формирования JSON: " + e.getMessage(), e);
            callback.onFailure("Ошибка подготовки данных");
            return;
        }

        RequestBody body = RequestBody.create(
                queryData.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(SUPABASE_URL_QUERIES)
                .post(body)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Ошибка отправки заявки: " + e.getMessage(), e);
                callback.onFailure("Ошибка сети: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Заявка успешно отправлена для: " + login);
                        callback.onSuccess();
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "empty body";
                        Log.e(TAG, "Ошибка сервера: " + response.code() + " - " + errorBody);
                        String userMessage = parseSupabaseError(response.code(), errorBody);
                        callback.onFailure(userMessage);
                    }
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            }
        });
    }

    /**
     * Парсит ошибку от Supabase и возвращает понятное сообщение для пользователя
     */

    private String parseSupabaseError(int code, String errorBody) {
        if (code == 401 || code == 403) {
            return "Ошибка доступа: обратитесь к администратору";
        }
        if (code == 400) {
            return "Неверные данные: проверьте заполнение полей";
        }
        if (code == 409) {
            return "Заявка уже существует";
        }
        if (code >= 500) {
            return "Ошибка сервера: попробуйте позже";
        }
        if (errorBody.contains("foreign key") || errorBody.contains("violates foreign key")) {
            return "Пользователь не найден в системе";
        }
        return "Ошибка " + code + ": " + (errorBody.length() > 100 ? errorBody.substring(0, 100) + "..." : errorBody);
    }

}



