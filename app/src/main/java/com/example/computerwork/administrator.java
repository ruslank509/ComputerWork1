package com.example.computerwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class administrator extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_administrator);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        }
    public void SentMessage(View view){
        Toast toast = Toast.makeText(this, "Добро пожаловать, пользователь!", Toast.LENGTH_LONG);
        toast.show();

    }
    public void junctionExit(View view) {
        Intent intent = new Intent(this, authorization.class);
        startActivity(intent);
    }
    public void AdmToInv(View view){
        Intent intent = new Intent(this, inventory.class);
        startActivity(intent);
    }
    public void AdmToPark(View view){
        Intent intent = new Intent(this, parks.class);
        startActivity(intent);
    }
    public void AdmToUser(View view){
        Intent intent = new Intent(this, user.class);
        startActivity(intent);
    }


}