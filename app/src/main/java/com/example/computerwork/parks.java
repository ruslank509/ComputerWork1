package com.example.computerwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class parks extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_park);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.park), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void ParkToAdm(View view){
        Intent intent = new Intent(this, administrator.class);
        startActivity(intent);
    }
    public void CheckPark (View view){
        Intent intent = new Intent(this, checkpark.class);
        startActivity(intent);
    }
}
