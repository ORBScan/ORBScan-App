package com.example.orbscantemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class FeaturesDescription extends AppCompatActivity {

    ImageButton previousFeatureBtn, nextFeatureBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features_description);

        previousFeatureBtn = findViewById(R.id.previousFeatureButton);
        nextFeatureBtn = findViewById(R.id.nextFeatureButton);

        previousFeatureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FeaturesDescription.this,IntroductionDescription.class);
                startActivity(intent);
                overridePendingTransition(0,0);
                finish();
            }
        });

        nextFeatureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FeaturesDescription.this, Dashboard.class);
                startActivity(intent);
                finish();
            }
        });
    }
}