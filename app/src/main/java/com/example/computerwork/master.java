package com.example.computerwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class master extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_master);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.master), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void Exit (View view){
        Intent intent = new Intent(this, authorization.class);
        startActivity(intent);
    }
    public void Accept (View view){
        Intent intent = new Intent(this, acceptquery.class);
        startActivity(intent);
    }
    public void Check (View view){
        Intent intent = new Intent(this, checkquery.class);
        startActivity(intent);
    }
}
