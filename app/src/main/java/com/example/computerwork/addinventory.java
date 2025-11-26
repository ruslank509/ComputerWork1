package com.example.computerwork;

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
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class addinventory extends AppCompatActivity {

    private static final String SUPABASE_INVENTORIES_URL = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Inventories?select=Idinventory";
    private static final String SUPABASE_PRODUCTS_URL = "https://fomzcdnikdwhiceclpoc.supabase.co/rest/v1/Products";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZvbXpjZG5pa2R3aGljZWNscG9jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE0NzEyMzUsImV4cCI6MjA3NzA0NzIzNX0.yeveyPQEG7FdYHsf4ga9GDB3dAmiWGhqjJ1wlrMrWlo";

    Spinner spinnerType, spinnerModel, spinnerInventory;
    EditText Price;

    String[] typeOptions = {
            "Монитор", "Клавиатура", "Компьютерная мышь", "Вентилятор",
            "Процессор", "Материнская плата", "Видеокарта",
            "Оперативная память", "Жёсткий диск", "Термопаста", "Wifi-адаптер"
    };

    Map<String, String[]> modelOptions = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addinventory);

        spinnerType = findViewById(R.id.spinner2);
        spinnerModel = findViewById(R.id.spinner3);
        spinnerInventory = findViewById(R.id.spinner4);
        Button submitButton = findViewById(R.id.button10);

        // Заполнение моделей
        modelOptions.put("Монитор", new String[]{"Acer 21.5'", "Acer 23.8'", "Acer 24.5'", "Samsung 23.8'", "Samsung 24'", "Samsung 27'", "LG 23.8'", "LG 24.5'", "LG 27"});
        modelOptions.put("Клавиатура", new String[]{"Defender", "CBR (Cyber Brand Retail)", "Logitech", "SteelSeries Apex", "ASUS ROG Azoth", "ExeGate", "SVEN", "Smartbuy", "Panteon"});
        modelOptions.put("Компьютерная мышь", new String[]{"CBR (Cyber Brand Retail)", "Ritmix", "Borofone", "Гарнизон", "Fusion", "Qumo Office", "Smartbuy", "Aceline", "DEXP"});
        modelOptions.put("Вентилятор", new String[]{"5Bites: 3 pin, 3800 об/мин", "5Bites: 3 pin, 4200 об/мин", "5Bites: 4 pin, 2500 об/мин", "DEXP: 3 pin, 4350 об/мин", "DEXP: 3 pin, 6000 об/мин", "DEXP: 3 pin, 6500 об/мин", "Rexant: 2 pin, 5200 об/мин", "Rexant: 4300 об/мин", "Rexant: 5000 об/мин"});
        modelOptions.put("Процессор", new String[]{"Intel Core i3-10105F", "Intel Core i5-12400F", "Intel Core i7-12700KF", "Intel Core i9-10900K", "AMD Ryzen 3 3200G BOX", "AMD Ryzen 5 4500 BOX", "AMD Ryzen 5 4650G OEM", "AMD Ryzen 7 3700X OEM", "AMD Ryzen 7700X OEM"});
        modelOptions.put("Материнская плата", new String[]{"Colorful (AMD)", "MSI (Intel B760)", "GIGABYTE (AMD A620)", "GIGABYTE (Intel H610)", "Colorful (Intel B760)", "ASRock (Intel B760)", "ASUS PRIME (Intel B760)", "GIGABYTE (AMD B650)", "MAXSUN Challenger (AMD B650)"});
        modelOptions.put("Видеокарта", new String[]{"GIGABYTE GeForce GT 730", "GIGABYTE GeForce GT 1030", "PNY RTX A400", "ASRock Intel Arc A310", "ASRock Radeon RX 6400", "INNO3D GeForce GTX 1650", "Acer Intel Arc A750", "PowerColor AMD Radeon RX 6650", "ASUS GeForce RTX 3050 Dual"});
        modelOptions.put("Оперативная память", new String[]{"Patriot Signature (4 ГБ)", "DEXP (8 ГБ)", "Apacer (8 ГБ)", "Patriot Viper Elite II (4 ГБ)", "QUMO (4 ГБ)", "Kingston ValueRAM (4 ГБ)", "ADATA Premier (16 ГБ)", "Apacer TEX (16 ГБ)", "ExeGate HiPower (16 ГБ)"});
        modelOptions.put("Жёсткий диск", new String[]{"WD Purple (2 ТБ)", "WD Purple (4 ТБ)", "WD Red Pro (6 ТБ)", "WD Ultrastar (4 ТБ)", "WD Black (8 ТБ)", "Seagate Barracuda (2 ТБ)", "WD Blue (500 ГБ ~ 0,49 ТБ)", "WD Purple (1 ТБ)", "Seagate Skyhawk (2 ТБ)"});
        modelOptions.put("Термопаста", new String[]{"AeroCool Baraf-S (2 г)", "DEEPCOOL Z3 (1,5 г)", "STEEL [STP-G] (3 г)", "GemBird FreeZzz (1,5 г)", "ID-COOLING FROST X25 (4 г)", "Arctic Cooling MX-2 (8 г)", "ZALMAN ZM-STC10 (2 г)", "AeroCool Fuzion (1 г)", "Thermalright TF3 (2 г)"});
        modelOptions.put("Wifi-адаптер", new String[]{"TP-LINK Archer T2U Plus", "TP-LINK Archer TX55Е", "TP-LINK Archer TX20U Plus", "TP-LINK TL-WN821N", "TP-LINK Archer T2U Nano", "TP-LINK Archer T4U Plus", "RITMIX RWA-150", "DIGMA DWA-AC13002E", "Orient XGE-946ac"});

        spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typeOptions));

        spinnerType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedType = typeOptions[position];
                spinnerModel.setAdapter(new ArrayAdapter<>(addinventory.this, android.R.layout.simple_spinner_dropdown_item, modelOptions.get(selectedType)));
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        fetchInventoryIds(); // Загрузка ID инвентаря

        submitButton.setOnClickListener(v -> {
            String name = spinnerType.getSelectedItem().toString().trim();
            String model = spinnerModel.getSelectedItem().toString().trim();
            String inventory = spinnerInventory.getSelectedItem().toString().trim();

            sendBookingToSupabase(name, model, inventory);

        });
    }

    private void fetchInventoryIds() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SUPABASE_INVENTORIES_URL)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(addinventory.this, "Ошибка загрузки инвентаря", Toast.LENGTH_SHORT).show());
                Log.e("Supabase", "Ошибка запроса инвентаря", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        ArrayList<String> inventoryList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            inventoryList.add(jsonArray.getJSONObject(i).getString("Idinventory"));
                        }
                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(addinventory.this,
                                    android.R.layout.simple_spinner_dropdown_item, inventoryList);
                            spinnerInventory.setAdapter(adapter);
                        });
                    } catch (JSONException e) {
                        Log.e("Supabase", "Ошибка парсинга JSON", e);
                    }
                } else {
                    Log.e("Supabase", "Ошибка ответа: " + response.code());
                }
            }
        });
    }

    private void sendBookingToSupabase(String name, String model, String inventory) {
        JSONObject data = new JSONObject();
        try {
            data.put("Name", name);
            data.put("Model", model);
            data.put("Idinventory", inventory);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(data.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(SUPABASE_PRODUCTS_URL)
                .post(body)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(addinventory.this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
                    Log.e("SupabaseRequest", "Ошибка", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(addinventory.this, "Добавлено успешно!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(addinventory.this, "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}




