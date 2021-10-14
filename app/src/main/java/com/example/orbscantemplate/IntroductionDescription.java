package com.example.orbscantemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class IntroductionDescription extends AppCompatActivity {

    ImageButton previousIntroBtn, nextIntronBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction_description);

        previousIntroBtn = findViewById(R.id.previousIntroButton);
        nextIntronBtn = findViewById(R.id.nextIntroButton);

        previousIntroBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IntroductionDescription.this,WelcomeMessage.class);
                startActivity(intent);
                overridePendingTransition(0,0);
                finish();
            }
        });

        nextIntronBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IntroductionDescription.this,FeaturesDescription.class);
                startActivity(intent);
                overridePendingTransition(0,0);
                finish();
            }
        });
    }
}