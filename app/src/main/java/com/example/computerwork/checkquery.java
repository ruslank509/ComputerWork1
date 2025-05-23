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
import java.util.ArrayList;
import java.util.List;
import java.lang.String;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class checkquery extends AppCompatActivity {

    private static final String SUPABASE_URL_INVENTORY = "https://lincidhuobbcjwwccsty.supabase.co/rest/v1/Queries";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxpbmNpZGh1b2JiY2p3d2Njc3R5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQ3MTQyMjEsImV4cCI6MjA1MDI5MDIyMX0.H7pAHmmfd1-bdeammV-UqdC9aaCQU0GOnkX4CDdYg4s"; // сократи для читаемости

    RecyclerView recyclerView;
    QueryAdapter adapter;
    List<QueryModel> Inventory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkquery);

        recyclerView = findViewById(R.id.usersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QueryAdapter(Inventory);
        recyclerView.setAdapter(adapter);

        fetchUsersFromSupabase();
    }

    private void fetchUsersFromSupabase() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(SUPABASE_URL_INVENTORY)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(checkquery.this, "Ошибка при получении данных", Toast.LENGTH_SHORT).show()
                );
                Log.e("Supabase", "Ошибка: ", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());

                        Inventory.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            QueryModel inventory = new QueryModel();
                            inventory.LoginUser = obj.optString("LoginUser");
                            inventory.Crush = obj.optString("Crush");
                            inventory.Status = obj.optString("Status");
                            Inventory.add(inventory);
                        }

                        runOnUiThread(() -> adapter.notifyDataSetChanged());

                    } catch (JSONException e) {
                        Log.e("Supabase", "Ошибка JSON: ", e);
                    }
                }
            }
        });
    }
}
