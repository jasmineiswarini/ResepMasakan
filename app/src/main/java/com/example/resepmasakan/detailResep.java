package com.example.resepmasakan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class detailResep extends AppCompatActivity {
    private TextView e1, e2;
    private ImageView g1;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String id = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_resep);

        e1 = findViewById(R.id.d_judul);
        e2 = findViewById(R.id.d_detail);
        g1 = findViewById(R.id.d_gambar);

        Intent intent = getIntent();
        if (intent!=null);
        id = intent.getStringExtra("id");
        e1.setText(intent.getStringExtra("judul"));
        e2.setText(intent.getStringExtra("detail"));
        Glide.with(getApplicationContext()).load(intent.getStringExtra("gambar")).into(g1);
    }
}