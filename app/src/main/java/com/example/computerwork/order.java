package com.example.computerwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class order extends AppCompatActivity {

    private static final String PRODUCTS_URL = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Products";
    private static final String ORDERS_URL = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Orders";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZvbXpjZG5pa2R3aGljZWNscG9jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE0NzEyMzUsImV4cCI6MjA3NzA0NzIzNX0.yeveyPQEG7FdYHsf4ga9GDB3dAmiWGhqjJ1wlrMrWlo";

    private Spinner spinnerName, spinnerModel;
    private Button buyButton;
    private EditText editTextLogin;
    private final HashMap<String, List<String>> nameToModels = new HashMap<>();
    private final List<String> uniqueNames = new ArrayList<>();

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        spinnerName = findViewById(R.id.spinnerName);
        spinnerModel = findViewById(R.id.spinnerModel);
        buyButton = findViewById(R.id.button);
        editTextLogin = findViewById(R.id.editTextCustomer);

        buyButton.setOnClickListener(v -> handleBuy());

        spinnerName.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) spinnerName.getSelectedItem();
                updateModelSpinner(name);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}

        });

        fetchProducts();
    }

    private void fetchProducts() {
        Request request = new Request.Builder()
                .url(PRODUCTS_URL + "?select=Name,Model")
                .addHeader("apikey", API_KEY.trim())
                .addHeader("Authorization", "Bearer " + API_KEY.trim())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(order.this, "Ошибка сети", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(response.body().string());

                        nameToModels.clear();
                        uniqueNames.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String name = obj.getString("Name");
                            String model = obj.getString("Model");

                            nameToModels.computeIfAbsent(name, k -> new ArrayList<>()).add(model);
                        }

                        uniqueNames.addAll(new LinkedHashSet<>(nameToModels.keySet()));

                        runOnUiThread(() -> {
                            ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(order.this,
                                    android.R.layout.simple_spinner_item, uniqueNames);
                            nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerName.setAdapter(nameAdapter);

                            if (!uniqueNames.isEmpty()) {
                                updateModelSpinner(uniqueNames.get(0));
                            }
                        });

                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(order.this, "Ошибка JSON", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(order.this, "Ошибка сервера", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateModelSpinner(String name) {
        List<String> models = nameToModels.get(name);
        if (models == null) models = new ArrayList<>();
        ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, models);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModel.setAdapter(modelAdapter);
    }

    private void handleBuy() {
        String login = editTextLogin.getText().toString().trim();
        String name = (String) spinnerName.getSelectedItem();
        String model = (String) spinnerModel.getSelectedItem();

        if (login.isEmpty() || name == null || model == null) {
            Toast.makeText(this, "Выберите товар и введите логин", Toast.LENGTH_SHORT).show();
            return;
        }

        addOrder(login, name, model);
        deleteProductByNameAndModel(name, model);
    }

    private void addOrder(String login, String name, String model) {
        Random rand = new Random();
        int numberOrder = 1000 + rand.nextInt(9000);
        JSONObject json = new JSONObject();
        try {
            json.put("LoginUser", login);
            json.put("NameProduct", name);
            json.put("ModelProduct", model);
            json.put("NumberOrder", numberOrder);
        } catch (JSONException e) {
            runOnUiThread(() -> Toast.makeText(this, "Ошибка заказа", Toast.LENGTH_SHORT).show());
            return;
        }

        Request request = new Request.Builder()
                .url(ORDERS_URL)
                .post(RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8")))
                .addHeader("apikey", API_KEY.trim())
                .addHeader("Authorization", "Bearer " + API_KEY.trim())
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(order.this, "Не отправлен заказ", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) {}
        });
    }
    private void deleteProductByNameAndModel(String name, String model) {
        // Прямая подстановка (без кодирования!)
        String url = PRODUCTS_URL + "?Name=eq." + name + "&Model=eq." + model;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("apikey", API_KEY.trim())
                .addHeader("Authorization", "Bearer " + API_KEY.trim())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(order.this, "Ошибка удаления", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(order.this, "Товар куплен!", Toast.LENGTH_SHORT).show();
                        fetchProducts(); // обновить
                    } else {
                        Toast.makeText(order.this, "Не удалён (код " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void ReturnToMenu(View view) {
        startActivity(new Intent(this, menuuser.class));
    }
}



