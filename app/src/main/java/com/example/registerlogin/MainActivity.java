package com.example.registerlogin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.registerlogin.BiometricActivity;
import com.example.registerlogin.BuyRequest;
import com.example.registerlogin.R;
import com.example.registerlogin.SignatureActivity;
import com.example.registerlogin.VerifyRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

public class MainActivity extends AppCompatActivity {

    private TextView tv_id;
    private Button btn_info;
    private Button btn_buy;
    //private static final String KEY_NAME = userID;
    private KeyStore keyStore;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_id = findViewById(R.id.tv_id);
        btn_info = findViewById(R.id.btn_info);
        btn_buy = findViewById(R.id.btn_buy);

        // 회원정보 버튼
        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");

                intent = new Intent(MainActivity.this, BiometricActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });

        // 구매하기 버튼
        btn_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (jsonObject.has("challenge")) {
                                String challenge = jsonObject.getString("challenge");

                                Log.d(TAG, "챌린지값: " + challenge);

                                keyStore = KeyStore.getInstance("AndroidKeyStore");
                                keyStore.load(null);
                                Intent intent = getIntent();
                                String userID = intent.getStringExtra("userID");

                                boolean hasAccess = checkPrivateKeyAccess(userID);
                                if (hasAccess) {
                                    // 개인 키에 액세스할 수 있는 권한이 있음
                                    Log.d(TAG, "개인 키에 액세스할 수 있는 권한이 있음");
                                } else {
                                    // 개인 키에 액세스할 수 있는 권한이 없음
                                    Log.d(TAG, "개인 키에 액세스할 수 있는 권한이 없음");
                                }

                                if (keyStore.containsAlias(userID)) {
                                    // 키 쌍이 존재함
                                    // 공개 키와 개인 키 출력
                                    Log.d(TAG, "키스토어 저장됨");
                                } else {
                                    // 키 쌍이 존재하지 않음
                                    Log.d(TAG, "Key pair not found");
                                }

                                KeyStore.Entry entry = keyStore.getEntry(userID, null);

                                if (entry instanceof KeyStore.PrivateKeyEntry) {
                                    // 개인키가 초기화되었으므로 접근 가능
                                    Log.d(TAG, "개인키 초기화됨");
                                } else {
                                    // 개인키가 초기화되지 않았으므로 접근 불가능
                                    Log.d(TAG, "개인키 초기화 안됨");
                                }

                                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(userID, null);
                                privateKey = privateKeyEntry.getPrivateKey();

                                SignatureActivity signatureActivity = new SignatureActivity();
                                byte[] signedChallenge = signatureActivity.signChallenge(challenge, privateKey);

                                if (signedChallenge != null) {
                                    // Method invocation was successful
                                    Log.d(TAG, "Signed Challenge: " + Base64.encodeToString(signedChallenge, Base64.NO_WRAP));
                                } else {
                                    // Method invocation failed
                                    Log.e(TAG, "Failed to sign the challenge");
                                }
                                //Toast.makeText(getApplicationContext(), "signed된 메시지2: " + Base64.encodeToString(signedChallenge, Base64.DEFAULT), Toast.LENGTH_SHORT).show();

                                //Intent intent = getIntent();
                                //String userID = intent.getStringExtra("userID");
                                verifySignature(signedChallenge, challenge, userID); // userID에 실제 사용자의 ID를 전달해야 함



                            } else {
                                Log.e(TAG, "Challenge key not found in JSON response");
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "오류가 발생하였습니다. ", Toast.LENGTH_SHORT).show();
                            throw new RuntimeException(e);
                        } catch (CertificateException | KeyStoreException | IOException |
                                 NoSuchAlgorithmException | UnrecoverableEntryException e) {
                            throw new RuntimeException(e);
                        } catch (KeyManagementException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                BuyRequest buyRequest = new BuyRequest(responseListener);
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                queue.add(buyRequest);
            }
        });

        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        tv_id.setText(userID);
    }

    private boolean checkPrivateKeyAccess(String keyAlias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            return keyStore.entryInstanceOf(keyAlias, KeyStore.PrivateKeyEntry.class);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void verifySignature(byte[] signString, String chall, String userID) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableEntryException, KeyManagementException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(userID, null);
        publicKey = privateKeyEntry.getCertificate().getPublicKey();
        String stringpublicKey = Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP);

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");

                    if (success) {
                        // 검증 성공
                        Toast.makeText(getApplicationContext(), "서명이 확인되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        // 검증 실패
                        Toast.makeText(getApplicationContext(), "서명이 유효하지 않습니다. ", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "서명 검증 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        VerifyRequest verifyRequest = new VerifyRequest(userID, chall,Base64.encodeToString(signString, Base64.NO_WRAP),stringpublicKey, responseListener, MainActivity.this);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(verifyRequest);
    }

}

