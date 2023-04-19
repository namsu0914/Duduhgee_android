package com.example.registerlogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    private TextView tv_id, tv_pass;
    private Button btn_enroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_id=findViewById(R.id.tv_id);
        //tv_pass=findViewById(R.id.tv_pass);
        btn_enroll=findViewById(R.id.btn_enroll);

        btn_enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FingerprintActivity.class);
                startActivity(intent);
            }
        });


        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        String userPass = intent.getStringExtra("userPass");

        tv_id.setText(userID);
        //tv_pass.setText(userPass);

    }
}