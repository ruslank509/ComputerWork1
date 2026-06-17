package com.example.computerwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
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

    private Spinner spinnerName, spinnerModel, spinnerType;
    private Button buyButton;

    // 🔹 Новая структура: Type -> Name -> List<Model>
    private final HashMap<String, HashMap<String, List<String>>> typeToNameToModels = new HashMap<>();
    private final List<String> uniqueTypes = new ArrayList<>();

    private final OkHttpClient client = new OkHttpClient();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isSessionValid()) {
            Toast.makeText(this, "Сессия истекла. Требуется авторизация", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, authorization.class));
            finish();
            return;
        }
        sessionManager.updateLastActivity();

        spinnerType = findViewById(R.id.spinnerType);
        spinnerName = findViewById(R.id.spinnerName);
        spinnerModel = findViewById(R.id.spinnerModel);
        buyButton = findViewById(R.id.button);

        buyButton.setOnClickListener(v -> handleBuy());

        // 🔹 1. Слушатель для Type (фильтрует Name)
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = (String) parent.getSelectedItem();
                if (selectedType != null && !selectedType.isEmpty()) {
                    updateNameSpinner(selectedType);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 🔹 2. Слушатель для Name (фильтрует Model)
        spinnerName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = (String) spinnerType.getSelectedItem();
                String selectedName = (String) parent.getSelectedItem();
                if (selectedType != null && selectedName != null) {
                    updateModelSpinner(selectedType, selectedName);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        fetchProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.isSessionValid()) {
            sessionManager.updateLastActivity();
        }
    }

    private void fetchProducts() {
        Request request = new Request.Builder()
                .url(PRODUCTS_URL + "?select=Name,Model,Type")
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
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        typeToNameToModels.clear();
                        uniqueTypes.clear();

                        // 🔹 Строим вложенную структуру данных
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String type = obj.optString("Type", "Не указан"); // если Type пустой
                            String name = obj.getString("Name");
                            String model = obj.getString("Model");

                            typeToNameToModels
                                    .computeIfAbsent(type, k -> new HashMap<>())
                                    .computeIfAbsent(name, k -> new ArrayList<>())
                                    .add(model);
                        }

                        uniqueTypes.addAll(typeToNameToModels.keySet());

                        runOnUiThread(() -> {
                            // 🔹 Заполняем spinnerType
                            ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(order.this,
                                    android.R.layout.simple_spinner_item, uniqueTypes);
                            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerType.setAdapter(typeAdapter);

                            // Инициализируем остальные спиннеры для первого типа
                            if (!uniqueTypes.isEmpty()) {
                                updateNameSpinner(uniqueTypes.get(0));
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

    // 🔹 Обновление списка имён по выбранному типу
    private void updateNameSpinner(String type) {
        HashMap<String, List<String>> nameToModels = typeToNameToModels.get(type);
        List<String> names = (nameToModels != null) ? new ArrayList<>(nameToModels.keySet()) : new ArrayList<>();

        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerName.setAdapter(nameAdapter);
    }

    // 🔹 Обновление списка моделей по типу и имени
    private void updateModelSpinner(String type, String name) {
        HashMap<String, List<String>> nameToModels = typeToNameToModels.get(type);
        List<String> models = (nameToModels != null) ? nameToModels.getOrDefault(name, new ArrayList<>()) : new ArrayList<>();

        ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, models);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModel.setAdapter(modelAdapter);
    }

    private void handleBuy() {
        String login = sessionManager.getSavedLogin();
        String email = sessionManager.getSavedEmail();
        String type = (String) spinnerType.getSelectedItem();
        String name = (String) spinnerName.getSelectedItem();
        String model = (String) spinnerModel.getSelectedItem();

        if (login == null || login.isEmpty()) {
            Toast.makeText(this, "Ошибка авторизации. Выполните вход", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, authorization.class));
            finish();
            return;
        }

        if (name == null || name.isEmpty() || model == null || model.isEmpty()) {
            Toast.makeText(this, "Выберите товар", Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.updateLastActivity();

        addOrder(login, email, name, model);
        deleteProductByNameAndModel(name, model);
    }

    //Оформление заказа
    private void addOrder(String login, String email, String name, String model) {
        Random rand = new Random();
        int numberOrder = 1000 + rand.nextInt(9000);

        JSONObject json = new JSONObject();
        try {
            json.put("LoginUser", login);
            json.put("EmailUser", email);
            json.put("NameProduct", name);
            json.put("ModelProduct", model);
            json.put("NumberOrder", numberOrder);
        } catch (JSONException e) {
            runOnUiThread(() -> Toast.makeText(this, "Ошибка формирования заказа", Toast.LENGTH_SHORT).show());
            return;
        }

        Request request = new Request.Builder()
                .url(ORDERS_URL)
                .post(RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8")))
                .addHeader("apikey", API_KEY.trim())
                .addHeader("Authorization", "Bearer " + API_KEY.trim())
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(order.this, "Не отправлен заказ", Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(order.this, "Ошибка сервера при заказе", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void deleteProductByNameAndModel(String name, String model) {
        try {
            String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString());
            String encodedModel = URLEncoder.encode(model, StandardCharsets.UTF_8.toString());
            String url = PRODUCTS_URL + "?Name=eq." + encodedName + "&Model=eq." + encodedModel;

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
                            fetchProducts();
                        } else {
                            Toast.makeText(order.this, "Не удалён (код " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(order.this, "Ошибка кодирования", Toast.LENGTH_SHORT).show());
        }
    }

    public void ReturnToMenu(View view) {
        startActivity(new Intent(this, menuuser.class));
    }
}



