package com.example.computerwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class menuuser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menuuser);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.menuuser), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void ExitUser(View view){
        Intent intent = new Intent(this, authorization.class);
        startActivity(intent);
    }
    public void Product(View view){
        Intent intent = new Intent(this, order.class);
        startActivity(intent);
    }
    public void Order(View view){
        Intent intent = new Intent(this, call.class);
        startActivity(intent);
    }
}
