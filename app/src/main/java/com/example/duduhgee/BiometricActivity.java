package com.example.duduhgee;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.rp.RP_DeleteRequest;
import com.example.rp.RP_PaymentDetailRequest;
import com.example.rp.RP_SavePKRequest;
import com.example.asm.ASM_checkKeyPairExistence;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

public class BiometricActivity extends AppCompatActivity {

    private CancellationSignal cancellationSignal = null;
    private BiometricPrompt.AuthenticationCallback authenticationCallback;
    private Button btn_auth;
    private Button btn_del;
    private Button btn_card;
    private TextView tv_product1, tv_amount1, tv_unitprice1, tv_totalprice1;
    private TextView tv_product2, tv_amount2, tv_unitprice2, tv_totalprice2;

    private boolean start_authenticationIsClicked = false;
    private boolean delete_bioIsClicked = false;

    private KeyStore keyStore;
    private PublicKey publicKey;
    private static final String TAG = "BiometricActivity";

    @SuppressLint("MissingInflatedId")
    @TargetApi(Build.VERSION_CODES.P)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);

        btn_auth = findViewById(R.id.start_authentication);
        btn_del  = findViewById(R.id.delete_bio);
        btn_card = findViewById(R.id.btn_card);

        tv_product1 = findViewById(R.id.tv_product1);
        tv_amount1 = findViewById(R.id.tv_amount1);
        tv_unitprice1 = findViewById(R.id.tv_unitprice1);
        tv_totalprice1 = findViewById(R.id.tv_totalprice1);
        tv_product2 = findViewById(R.id.tv_product2);
        tv_amount2 = findViewById(R.id.tv_amount2);
        tv_unitprice2 = findViewById(R.id.tv_unitprice2);
        tv_totalprice2 = findViewById(R.id.tv_totalprice2);

        Response.Listener<String> responseListner = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonarray = new JSONArray(response);
                    for(int i=0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        String product       = jsonobject.getString("product");
                        String amount    = jsonobject.getString("amount");
                        String unitPrice  = jsonobject.getString("unitPrice");
                        String totalPrice = jsonobject.getString("totalPrice");
                        if(i==0){
                            tv_product1.setText(product);
                            tv_amount1.setText(amount);
                            tv_unitprice1.setText(unitPrice);
                            tv_totalprice1.setText(totalPrice);
                        }else
                            tv_product2.setText(product);
                            tv_amount2.setText(amount);
                            tv_unitprice2.setText(unitPrice);
                            tv_totalprice2.setText(totalPrice);
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        RP_PaymentDetailRequest paymentDetailRequest = null;
        paymentDetailRequest = new RP_PaymentDetailRequest(responseListner);
        RequestQueue queue = Volley.newRequestQueue(BiometricActivity.this);
        queue.add(paymentDetailRequest);



        authenticationCallback = new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                notifyUser("Authentication Failed");
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                notifyUser("Authentication Error: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");
                super.onAuthenticationSucceeded(result);
                notifyUser("인증에 성공하였습니다");

                if (start_authenticationIsClicked) {
                    ASM_checkKeyPairExistence checkkp = new ASM_checkKeyPairExistence();
                    boolean iskeyEX = checkkp.ASM_checkkeypairexistence(userID);
                    // 공개키를 서버로 전송
                    //RP_sendPublicKeyToServer(publicKey);
                    try {
                        keyStore = KeyStore.getInstance("AndroidKeyStore");
                    } catch (KeyStoreException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        keyStore.load(null);
                    } catch (CertificateException | IOException | NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                    KeyStore.PrivateKeyEntry privateKeyEntry = null;
                    try {
                        privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(userID, null);
                    } catch (KeyStoreException | NoSuchAlgorithmException |
                             UnrecoverableEntryException e) {
                        throw new RuntimeException(e);
                    }
                    publicKey = privateKeyEntry.getCertificate().getPublicKey();
                    if(iskeyEX){
                        try {
                            RP_sendpublickeytoserver(publicKey, userID);
                        } catch (CertificateException | IOException | KeyStoreException |
                                 NoSuchAlgorithmException | KeyManagementException e) {
                            throw new RuntimeException(e);
                        }
                    }

                } else if (delete_bioIsClicked) {
                    try {
                        deletebio();
                    } catch (KeyStoreException | CertificateException | IOException |
                             NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                }
            }
        };


        btn_auth.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.P)
            @Override
            public void onClick(View view) {

                start_authenticationIsClicked = true;
                delete_bioIsClicked = false;
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String header = jsonObject.getString("Header");
                            String username = jsonObject.getString("Username");
                            String challenge = jsonObject.getString("Challenge");
                            String policy = jsonObject.getString("Policy");

                            Log.d(TAG,"Header: "+header);
                            Log.d(TAG,"Username: "+username);
                            Log.d(TAG,"Challenge: "+challenge);
                            Log.d(TAG,"Policy: "+policy);


                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "오류가 발생하였습니다. ", Toast.LENGTH_SHORT).show();
                            throw new RuntimeException(e);
                        }
                    }
                };
                FIDORegisterRequest fidoRegisterRequest = null;
                try {
                    Intent intent = getIntent();
                    String userID = intent.getStringExtra("userID");
                    fidoRegisterRequest = new FIDORegisterRequest(userID, responseListener, BiometricActivity.this);
                } catch (CertificateException | IOException | KeyStoreException |
                         NoSuchAlgorithmException | KeyManagementException e) {
                    throw new RuntimeException(e);
                }
                RequestQueue queue = Volley.newRequestQueue(BiometricActivity.this);
                queue.add(fidoRegisterRequest);

                if (checkBiometricSupport()) {
                    BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(BiometricActivity.this)
                            .setTitle("지문 인증을 시작합니다")
                            .setSubtitle("지문 인증 시작")
                            .setDescription("지문")
                            .setNegativeButton("Cancel", getMainExecutor(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    notifyUser("Authentication Cancelled");
                                }
                            }).build();

                    biometricPrompt.authenticate(getCancellationSignal(), getMainExecutor(), authenticationCallback);
                }
            }
        });

        btn_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");

                intent = new Intent(BiometricActivity.this, CardEnrollActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });

        btn_del.setOnClickListener(new View.OnClickListener() {

            @TargetApi(Build.VERSION_CODES.P)

            @Override
            public void onClick(View v) {

                delete_bioIsClicked = true;
                start_authenticationIsClicked = false;

                if (checkBiometricSupport()) {
                    BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(BiometricActivity.this)
                            .setTitle("지문 인증을 시작합니다")
                            .setSubtitle("지문 인증 시작")
                            .setDescription("지문")
                            .setNegativeButton("Cancel", getMainExecutor(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    notifyUser("Authentication Cancelled");
                                }
                            }).build();

                    biometricPrompt.authenticate(getCancellationSignal(), getMainExecutor(), authenticationCallback);

                }
            }
        });

    }


    public void RP_sendpublickeytoserver(PublicKey publicKey, String userID) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {


        String publicKeyString = Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP);

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    if (success) {
                        Log.d(TAG, "공개키 전송 성공");
                    } else {
                        Log.d(TAG, "공개키 전송 실패");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "공개키 전송 에러: " + error.getMessage());
            }
        };

        RP_SavePKRequest savePKRequest = new RP_SavePKRequest(publicKeyString, userID, responseListener, errorListener, BiometricActivity.this);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(savePKRequest);
    }

    private CancellationSignal getCancellationSignal() {
        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(() -> notifyUser("Authentication was Cancelled by the user"));
        return cancellationSignal;
    }

    @TargetApi(Build.VERSION_CODES.P)
    public Boolean checkBiometricSupport() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isDeviceSecure()) {
            notifyUser("Fingerprint authentication has not been enabled in settings");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            notifyUser("Fingerprint Authentication Permission is not enabled");
            return false;
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            return true;
        } else {
            return false;
        }
    }

    private void notifyUser(String message) {
        Toast.makeText(BiometricActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void deletebio() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");

        if (keyStore.containsAlias(userID)){
            Log.d(TAG, "키스토어에 있습니다.");
            keyStore.deleteEntry(userID);
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");

                        if (success) {
                            Toast.makeText(getApplicationContext(), "생체정보가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "삭제 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            RP_DeleteRequest deleteRequest;
            try {
                deleteRequest = new RP_DeleteRequest(userID, responseListener, BiometricActivity.this);
            } catch (CertificateException | IOException | KeyStoreException |
                     NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
            RequestQueue queue = Volley.newRequestQueue(BiometricActivity.this);
            queue.add(deleteRequest);
            if (keyStore.containsAlias(userID)){
                Log.d(TAG, "키스토어에서 삭제 실패");
            } else {
                Log.d(TAG, "키스토어에서 삭제됨");
            }
        } else {
            Log.d(TAG, "키스토어에 없습니다. ");
        }
    }
}