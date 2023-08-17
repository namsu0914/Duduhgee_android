package com.example.duduhgee;

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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

public class MainActivity extends AppCompatActivity {

    private TextView tv_id;
    private Button btn_info;

    //private static final String KEY_NAME = userID;
    private KeyStore keyStore;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private BiometricPrompt.AuthenticationCallback authenticationCallback;
    private CancellationSignal cancellationSignal = null;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_id = findViewById(R.id.tv_id);
        btn_info = findViewById(R.id.btn_info);

        ImageView imageViewClickable = findViewById(R.id.imageViewClickable);
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");

        imageViewClickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the new activity when the ImageView is clicked
                Intent intent = new Intent(MainActivity.this, BuyActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });

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
        tv_id.setText(userID);
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

        VerifyRequest verifyRequest = new VerifyRequest(userID, chall, Base64.encodeToString(signString, Base64.NO_WRAP), stringpublicKey, responseListener, MainActivity.this);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(verifyRequest);
    }

    private void notifyUser(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private CancellationSignal getCancellationSignal() {
        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(() -> notifyUser("Authentication was Cancelled by the user"));
        return cancellationSignal;
    }

    private boolean checkPrivateKeyAccess(String keyAlias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            return keyStore.entryInstanceOf(keyAlias, KeyStore.PrivateKeyEntry.class);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                 IOException e) {
            e.printStackTrace();
        }
        return false;
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

}