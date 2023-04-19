package com.example.registerlogin;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText et_id, et_pass, et_name, et_age;
    private Button btn_register;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) { //액티비티 시작시 처음으로 시작되는 생명주기
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        et_id=findViewById(R.id.et_id);
        et_pass=findViewById(R.id.et_pass);
        et_name=findViewById(R.id.et_name);
        et_age=findViewById(R.id.et_age);

        //회원가입 클릭시 수행
        btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //editText에 현재 입력된 값을 가져옴(get)
                String userID = et_id.getText().toString();
                String userPass = et_pass.getText().toString();
                String userName = et_name.getText().toString();
                int userAge=Integer.parseInt((et_age.getText().toString()));

                Response.Listener<String> responseListner = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject= new JSONObject(response);
                            boolean success= jsonObject.getBoolean("success"); // 서버통신 잘 됐냐?
                            if (success) {
                                Toast.makeText(getApplicationContext(), "회원 등록에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent= new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }else{
                                Toast.makeText(getApplicationContext(), "회원 등록에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                //서버로 Volley를 이용하여 요청
                RegisterRequest registerRequest = new RegisterRequest(userID, userPass,userName, userAge, responseListner);
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                queue.add(registerRequest);

            }
        });

    }
}