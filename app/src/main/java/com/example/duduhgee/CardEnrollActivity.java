package com.example.duduhgee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.rp.RP_CardEnrollRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class CardEnrollActivity extends AppCompatActivity {
    private EditText et_card, et_cvc, et_date, et_pw;
    private Button btn_enroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardenroll);

        et_card = findViewById(R.id.et_card);
        et_cvc = findViewById(R.id.et_cvc);
        et_date = findViewById(R.id.et_date);
        et_pw = findViewById(R.id.et_pw);

        //회원가입 클릭시 수행
        btn_enroll = findViewById(R.id.cardenroll);
        btn_enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");

                //editText에 현재 입력된 값을 가져옴(get)
                String c_num = et_card.getText().toString();
                String c_cvc = et_cvc.getText().toString();
                String c_date = et_date.getText().toString();
                String c_pw = et_pw.getText().toString();

                // Function to check for special characters
                boolean containsSpecialCharacters = containsOnlyNumbers(c_num) || containsOnlyNumbers(c_cvc) || containsOnlyNumbers(c_date) || containsOnlyNumbers(c_pw);

                if (!containsSpecialCharacters) {
                    Toast.makeText(CardEnrollActivity.this, "숫자 이외에는 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Response.Listener<String> responseListner = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success"); // 서버통신 잘 됐냐?
                            if (success) {
                                Toast.makeText(getApplicationContext(), "카드 등록에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CardEnrollActivity.this, BiometricActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "카드 등록에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "카드 등록 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                            throw new RuntimeException(e);
                        }
                    }
                };

                //서버로 Volley를 이용하여 요청
                RP_CardEnrollRequest rp_cardEnrollRequest = null;
                try {
                    rp_cardEnrollRequest = new RP_CardEnrollRequest(userID, c_num, c_cvc, c_date, c_pw, responseListner, CardEnrollActivity.this);
                } catch (CertificateException | IOException | KeyStoreException |
                         NoSuchAlgorithmException | KeyManagementException e) {
                    throw new RuntimeException(e);
                }

                RequestQueue queue = Volley.newRequestQueue(CardEnrollActivity.this);
                queue.add(rp_cardEnrollRequest);
            }
        });
    }

    // Function to check for special characters
    public boolean containsOnlyNumbers(String input) {
        return input.matches("^[0-9]+$");
    }
}