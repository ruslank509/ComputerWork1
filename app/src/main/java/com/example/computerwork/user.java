package com.example.computerwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class user extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void UserToAdm(View view){
        Intent intent = new Intent(this, administrator.class);
        startActivity(intent);
    }
    public void QueryToUser(View view){
        Intent intent = new Intent(this, checkquery.class);
        startActivity(intent);
    }
    public void OrderToUser(View view){
        Intent intent = new Intent(this, checkorder.class);
        startActivity(intent);
    }
}

