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
import java.util.HashMap;

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

    private Spinner spinnerPrice, spinnerName, spinnerModel;
    private Button deleteButton;
    private EditText editTextLogin;

    private final ArrayList<String> priceList = new ArrayList<>();
    private final HashMap<String, String> priceToNameMap = new HashMap<>();
    private final HashMap<String, String> priceToModelMap = new HashMap<>();

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        spinnerPrice = findViewById(R.id.spinnerPrice);
        spinnerName = findViewById(R.id.spinnerName);
        spinnerModel = findViewById(R.id.spinnerModel);
        deleteButton = findViewById(R.id.button);
        editTextLogin = findViewById(R.id.editTextCustomer);

        deleteButton.setOnClickListener(v -> {
            String selectedPrice = (String) spinnerPrice.getSelectedItem();
            String login = editTextLogin.getText().toString().trim();
            String name = (String) spinnerName.getSelectedItem();
            String model = (String) spinnerModel.getSelectedItem();

            if (login.isEmpty() || selectedPrice == null || name == null || model == null) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            // Сначала: вставка заказа
            addOrder(login, name, model);

            // Затем: удаление товара
            deleteProductByPrice(selectedPrice);
        });

        spinnerPrice.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedPrice = priceList.get(position);
                spinnerName.setAdapter(new ArrayAdapter<>(order.this,
                        android.R.layout.simple_spinner_item,
                        new String[]{priceToNameMap.get(selectedPrice)}));

                spinnerModel.setAdapter(new ArrayAdapter<>(order.this,
                        android.R.layout.simple_spinner_item,
                        new String[]{priceToModelMap.get(selectedPrice)}));
            }

            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        fetchProducts();
    }

    private void fetchProducts() {
        Request request = new Request.Builder()
                .url(PRODUCTS_URL + "?select=Price,Name,Model")
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(order.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        priceList.clear();
                        priceToNameMap.clear();
                        priceToModelMap.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String price = obj.getString("Price");
                            String name = obj.getString("Name");
                            String model = obj.getString("Model");

                            priceList.add(price);
                            priceToNameMap.put(price, name);
                            priceToModelMap.put(price, model);
                        }

                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(order.this,
                                    android.R.layout.simple_spinner_item, priceList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerPrice.setAdapter(adapter);
                        });

                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(order.this, "Ошибка обработки данных", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(order.this, "Ошибка ответа сервера", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void addOrder(String login, String name, String model) {
        JSONObject json = new JSONObject();
        try {
            json.put("LoginUser", login);
            json.put("NameProduct", name);
            json.put("ModelProduct", model);
        } catch (JSONException e) {
            Toast.makeText(this, "Ошибка формирования заказа", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(ORDERS_URL)
                .post(RequestBody.create(json.toString(), MediaType.get("application/json")))
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(order.this, "Ошибка добавления заказа", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(order.this, "Не удалось сохранить заказ", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void deleteProductByPrice(String price) {
        Request request = new Request.Builder()
                .url(PRODUCTS_URL + "?Price=eq." + price)
                .delete()
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(order.this, "Ошибка удаления", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(order.this, "Товар куплен!", Toast.LENGTH_SHORT).show();
                        fetchProducts(); // обновить список
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(order.this, "Не удалось удалить товар", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    public void ReturnToMenu(View view) {
        startActivity(new Intent(this, menuuser.class));
    }
}



