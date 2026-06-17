package com.example.computerwork;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class checkusersorder extends AppCompatActivity {

    private static final String SUPABASE_URL_INVENTORY =
            "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Orders";
    private static final String SUPABASE_API_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZvbXpjZG5pa2R3aGljZWNscG9jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE0NzEyMzUsImV4cCI6MjA3NzA0NzIzNX0.yeveyPQEG7FdYHsf4ga9GDB3dAmiWGhqjJ1wlrMrWlo";

    private static final String TAG = "Supabase";
    private SessionManager sessionManager;

    RecyclerView recyclerView;
    OrderAdapter adapter;
    List<OrderModel> Inventory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkusersorder);
        sessionManager = new SessionManager(this);
        recyclerView = findViewById(R.id.ordersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(Inventory);
        recyclerView.setAdapter(adapter);

        // 1. Получаем логин текущего пользователя
        String currentLogin = sessionManager.getSavedLogin();

        if (currentLogin == null || currentLogin.isEmpty()) {
            Toast.makeText(this, "Не удалось определить пользователя", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "currentLogin is empty");
            return;
        }

        // 2. Загружаем заказы именно для этого пользователя
        fetchOrdersByLogin(currentLogin);
    }

    private void fetchOrdersByLogin(String login) {
        OkHttpClient client = new OkHttpClient();

        // Формируем URL с фильтром PostgREST: ?LoginUser=eq.<логин>
        String encodedLogin;
        try {
            encodedLogin = URLEncoder.encode(login, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            encodedLogin = login;
        }

        String url = SUPABASE_URL_INVENTORY + "?LoginUser=eq." + encodedLogin;

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
                runOnUiThread(() ->
                        Toast.makeText(checkusersorder.this,
                                "Ошибка при получении данных",
                                Toast.LENGTH_SHORT).show()
                );
                Log.e(TAG, "Ошибка сети: ", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    Log.e(TAG, "Пустой ответ от сервера");
                    return;
                }

                if (!response.isSuccessful()) {
                    String body = response.body().string();
                    Log.e(TAG, "HTTP " + response.code() + ": " + body);
                    runOnUiThread(() ->
                            Toast.makeText(checkusersorder.this,
                                    "Ошибка сервера: " + response.code(),
                                    Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                try {
                    String body = response.body().string();
                    Log.d(TAG, "Ответ: " + body);

                    JSONArray jsonArray = new JSONArray(body);

                    Inventory.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        OrderModel inventory = new OrderModel();
                        inventory.LoginUser   = obj.optString("LoginUser");
                        inventory.NameProduct = obj.optString("NameProduct");
                        inventory.ModelProduct = obj.optString("ModelProduct");
                        Inventory.add(inventory);
                    }

                    runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        if (Inventory.isEmpty()) {
                            Toast.makeText(checkusersorder.this,
                                    "У вас пока нет заказов",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (JSONException e) {
                    Log.e(TAG, "Ошибка парсинга JSON: ", e);
                }
            }
        });
    }
}
