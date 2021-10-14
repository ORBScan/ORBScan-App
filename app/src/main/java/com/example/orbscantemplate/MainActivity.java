package com.example.orbscantemplate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN = 3000;
    ImageView logo, line1, line2, line3, line4, tagline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logo = findViewById(R.id.logoimg);
        line1 = findViewById(R.id.upperline1);
        line2 = findViewById(R.id.upperline2);
        line3 = findViewById(R.id.lowerline1);
        line4 = findViewById(R.id.lowerline2);
        tagline = findViewById(R.id.curvedText);

        tagline.setVisibility(View.INVISIBLE);
        blink();
        move1();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, WelcomeMessage.class);
                startActivity(intent);
                finish();
            }
        },SPLASH_SCREEN);
    }

    public void blink(){
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        logo.startAnimation(animation1);
    }

    public void move1(){
        Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move);
        Animation animation3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move2);
        Animation animation4 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move3);
        Animation animation5 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move4);
        Animation animation7 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        line1.startAnimation(animation3);
        line2.startAnimation(animation2);
        line3.startAnimation(animation4);
        line4.startAnimation(animation5);
        tagline.startAnimation(animation7);
        tagline.setVisibility(View.VISIBLE);
    }
}