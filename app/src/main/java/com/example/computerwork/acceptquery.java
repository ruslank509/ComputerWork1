package com.example.computerwork;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class acceptquery extends AppCompatActivity {

    private static final String SUPABASE_URL = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Queries";
    private static final String SUPABASE_URL_PRODUCTS = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Products";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZvbXpjZG5pa2R3aGljZWNscG9jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE0NzEyMzUsImV4cCI6MjA3NzA0NzIzNX0.yeveyPQEG7FdYHsf4ga9GDB3dAmiWGhqjJ1wlrMrWlo";

    private Spinner spinnerQueries;
    private Spinner spinnerModel;
    private Spinner spinnerType;
    private Spinner spinnerModule;
    private EditText conclusionEditText;
    private Button updateButton;
    private CheckBox addComponent;
    private LinearLayout spinnersLinear;

    private final ArrayList<String> numberQueryList = new ArrayList<>();
    private final ArrayList<String> modelList = new ArrayList<>();
    private final ArrayList<String> typeList = new ArrayList<>();
    private final ArrayList<String> nameList = new ArrayList<>();

    private final OkHttpClient client = new OkHttpClient();
    private boolean isOperationInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceptquery);

        spinnerQueries = findViewById(R.id.spinnerQueries);
        conclusionEditText = findViewById(R.id.editTextConclusion);
        updateButton = findViewById(R.id.button9);
        addComponent = findViewById(R.id.checkBoxUsingComponent);
        spinnersLinear = findViewById(R.id.spinners_container);
        spinnerModel = findViewById(R.id.spinnerUsingModel);
        spinnerType = findViewById(R.id.spinnerUsingType);
        spinnerModule = findViewById(R.id.spinnerUsingModule);

        spinnersLinear.setVisibility(View.GONE);

        addComponent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                spinnersLinear.setVisibility(View.VISIBLE);
                fetchProducts();
            } else {
                spinnersLinear.setVisibility(View.GONE);
            }
        });

        updateButton.setOnClickListener(v -> {
            if (isOperationInProgress) {
                Toast.makeText(this, "Подождите, выполняется операция...", Toast.LENGTH_SHORT).show();
                return;
            }

            String conclusion = conclusionEditText.getText().toString().trim();
            String status = "Рассмотрен";
            String noUsedProduct = "Не использовано";

            if (conclusion.isEmpty()) {
                Toast.makeText(this, "Вы не ввели заключение!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (spinnerQueries.getSelectedItem() == null) {
                Toast.makeText(this, "Выберите заявку!", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedNumberQuery = spinnerQueries.getSelectedItem().toString();
            boolean useComponent = addComponent.isChecked();

            isOperationInProgress = true;
            updateButton.setEnabled(false);

            if (!useComponent) {
                updateQuery(selectedNumberQuery, status, conclusion, noUsedProduct);
            } else {
                if (spinnerModel.getSelectedItem() == null ||
                        spinnerType.getSelectedItem() == null ||
                        spinnerModule.getSelectedItem() == null) {
                    Toast.makeText(this, "Выберите Model, Type и Name!", Toast.LENGTH_SHORT).show();
                    isOperationInProgress = false;
                    updateButton.setEnabled(true);
                    return;
                }

                String usedProductModel = spinnerModel.getSelectedItem().toString().trim();
                String usedProductType = spinnerType.getSelectedItem().toString().trim();
                String usedProductName = spinnerModule.getSelectedItem().toString().trim();

                updateQuery(selectedNumberQuery, status, conclusion, usedProductModel,
                        usedProductName, usedProductModel, usedProductType);
            }
        });

        fetchNumberQueries();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем данные при возврате на экран
        fetchNumberQueries();
        if (addComponent.isChecked()) {
            fetchProducts();
        }
    }

    private void fetchNumberQueries() {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "?select=NumberQuery&Status=eq.В обработке")
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(acceptquery.this,
                            "Ошибка загрузки заявок", Toast.LENGTH_SHORT).show();
                    finishOperation();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        numberQueryList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            numberQueryList.add(obj.getString("NumberQuery"));
                        }

                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    acceptquery.this,
                                    android.R.layout.simple_spinner_item,
                                    numberQueryList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerQueries.setAdapter(adapter);
                            finishOperation();
                        });

                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(acceptquery.this,
                                    "Ошибка парсинга данных", Toast.LENGTH_SHORT).show();
                            finishOperation();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(acceptquery.this,
                                "Ошибка ответа сервера", Toast.LENGTH_SHORT).show();
                        finishOperation();
                    });
                }
            }
        });
    }

    private void fetchProducts() {
        Request request = new Request.Builder()
                .url(SUPABASE_URL_PRODUCTS + "?select=Name,Model,Type")
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(acceptquery.this,
                            "Ошибка загрузки продуктов", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(response.body().string());

                        modelList.clear();
                        typeList.clear();
                        nameList.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            if (obj.has("Model") && !obj.isNull("Model"))
                                modelList.add(obj.getString("Model"));
                            if (obj.has("Type") && !obj.isNull("Type"))
                                typeList.add(obj.getString("Type"));
                            if (obj.has("Name") && !obj.isNull("Name"))
                                nameList.add(obj.getString("Name"));
                        }

                        runOnUiThread(() -> {
                            ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(
                                    acceptquery.this,
                                    android.R.layout.simple_spinner_item,
                                    modelList);
                            modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerModel.setAdapter(modelAdapter);

                            ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                                    acceptquery.this,
                                    android.R.layout.simple_spinner_item,
                                    typeList);
                            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerType.setAdapter(typeAdapter);

                            ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(
                                    acceptquery.this,
                                    android.R.layout.simple_spinner_item,
                                    nameList);
                            nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerModule.setAdapter(nameAdapter);
                        });

                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(acceptquery.this,
                                "Ошибка парсинга продуктов", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(acceptquery.this,
                            "Ошибка загрузки продуктов", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateQuery(String numberQuery, String status, String conclusion, String usedProduct,
                             String productName, String productModel, String productType) {
        JSONObject updateData = new JSONObject();
        try {
            updateData.put("Status", status);
            updateData.put("Conclusion", conclusion);
            updateData.put("UsedProduct", usedProduct);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка формирования данных", Toast.LENGTH_SHORT).show();
            finishOperation();
            return;
        }

        RequestBody body = RequestBody.create(
                updateData.toString(),
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "?NumberQuery=eq." + numberQuery)
                .patch(body)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(acceptquery.this,
                            "Ошибка обновления", Toast.LENGTH_SHORT).show();
                    Log.e("Supabase", "Ошибка обновления", e);
                    finishOperation();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : "empty body";
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(acceptquery.this, "Заявка обновлена!", Toast.LENGTH_SHORT).show();

                        // Если использовался компонент, удаляем его из Products
                        if (!usedProduct.equals("Не использовано")) {
                            deleteProductAndUpdate(productName, productModel, productType);
                        } else {
                            // Иначе просто обновляем список заявок
                            fetchNumberQueries();
                        }
                    } else {
                        Toast.makeText(acceptquery.this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
                        Log.e("Supabase", "Ошибка: " + response.code() + " " + response.message() + " | " + responseBody);
                        finishOperation();
                    }
                });
            }
        });
    }

    private void updateQuery(String numberQuery, String status, String conclusion, String usedProduct) {
        updateQuery(numberQuery, status, conclusion, usedProduct, null, null, null);
    }

    private void deleteProductAndUpdate(String name, String model, String type) {
        String url = SUPABASE_URL_PRODUCTS
                + "?Name=eq." + name
                + "&Model=eq." + model
                + "&Type=eq." + type;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(acceptquery.this,
                            "Ошибка удаления продукта", Toast.LENGTH_SHORT).show();
                    Log.e("Supabase", "Ошибка DELETE", e);
                    finishOperation();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : "empty body";
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(acceptquery.this, "Продукт удалён из Products", Toast.LENGTH_SHORT).show();

                        // Сбрасываем выбор в спиннерах
                        spinnerModel.setSelection(0);
                        spinnerType.setSelection(0);
                        spinnerModule.setSelection(0);

                        // Обновляем список продуктов
                        fetchProducts();

                        // Обновляем список заявок
                        fetchNumberQueries();
                    } else {
                        Toast.makeText(acceptquery.this, "Ошибка удаления продукта", Toast.LENGTH_SHORT).show();
                        Log.e("Supabase", "DELETE ошибка: " + response.code() + " " + response.message() + " | " + responseBody);
                        finishOperation();
                    }
                });
            }
        });
    }

    private void finishOperation() {
        isOperationInProgress = false;
        updateButton.setEnabled(true);
    }

    public void ReturnToMs(View view) {
        startActivity(new Intent(this, master.class));
    }
}














