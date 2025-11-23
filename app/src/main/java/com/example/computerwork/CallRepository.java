package com.example.computerwork;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CallRepository {

    private static final String SUPABASE_URL = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Queries";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZvbXpjZG5pa2R3aGljZWNscG9jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE0NzEyMzUsImV4cCI6MjA3NzA0NzIzNX0.yeveyPQEG7FdYHsf4ga9GDB3dAmiWGhqjJ1wlrMrWlo";

    private final OkHttpClient client;
    private final Context context;

    public CallRepository(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
    }

    // Ваш callback интерфейс для уведомления о результате
    public interface Callback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void sendBookingToSupabase(String login, String problem, String status, Callback callback) {
        Random rand = new Random();
        int randomNum = rand.nextInt(9999);
        JSONObject clientsData = new JSONObject();
        try {
            clientsData.put("NumberQuery", randomNum);
            clientsData.put("LoginUser", login);
            clientsData.put("Crush", problem);
            clientsData.put("Status", status);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onFailure("Ошибка формирования данных");
            return;
        }

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

        // Используем okhttp3.Callback с полным именем пакета, чтобы не путать с нашим интерфейсом
        client.newCall(requestUsers).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("SupabaseRequest", "Ошибка", e);
                callback.onFailure("Ошибка при добавлении");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "empty body";
                    Log.e("SupabaseRequest", "Ошибка Users: " + response.code() + " - " + response.message() + " | " + errorBody);
                    callback.onFailure("Ошибка при добавлении данных");
                }
            }
        });
    }
}



