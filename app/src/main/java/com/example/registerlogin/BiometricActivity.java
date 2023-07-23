package com.example.registerlogin;

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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

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
    private Button start_authentication;

    //private static final String KEY_NAME = "my_key";
    private KeyStore keyStore;
    private PublicKey publicKey;
    private static final String TAG = "BiometricActivity";

    @TargetApi(Build.VERSION_CODES.P)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric);

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
                super.onAuthenticationSucceeded(result);
                notifyUser("인증에 성공하였습니다");
                // 처리를 진행하세요
                //generateKeyPair();
                // 키 쌍 생성 확인 코드 추가
                checkKeyPairExistence();
                // 공개키를 서버로 전송
                try {
                    sendPublicKeyToServer(publicKey);
                } catch (CertificateException | KeyManagementException | NoSuchAlgorithmException |
                         KeyStoreException | IOException e) {
                    throw new RuntimeException(e);
                }
            }

        };




        start_authentication = findViewById(R.id.start_authentication);
        start_authentication.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.P)
            @Override
            public void onClick(View view) {
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

    private void checkKeyPairExistence() {
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (keyStore.containsAlias(userID)) {
                // 키 쌍이 존재함
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(userID, null);
                publicKey = privateKeyEntry.getCertificate().getPublicKey();
                // 공개 키와 개인 키 출력
                Log.d(TAG, "Public Key: " + Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP));
                Toast.makeText(getApplicationContext(), "이미 저장된 생체정보입니다. ", Toast.LENGTH_SHORT).show();
            } else {
                // 키 쌍이 존재하지 않음
                Log.d(TAG, "Key pair not found");
                generateKeyPair();
            }
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException | UnrecoverableEntryException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "키 저장소 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void generateKeyPair() {
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (!keyStore.containsAlias(userID)) {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(userID, KeyProperties.PURPOSE_SIGN)
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                        .build());

                KeyPair keyPair = keyPairGenerator.generateKeyPair();
                publicKey = keyPair.getPublic();

                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(userID, null);
                //PrivateKey privateKey = privateKeyEntry.getPrivateKey();
//                PrivateKey privateKey = keyPair.getPrivate();

                // 공개키를 서버로 전송
                sendPublicKeyToServer(publicKey);

                Log.d(TAG, "공개키: " + Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP));
                //Log.d(TAG, "비밀키: " + Base64.encodeToString(privateKey.getEncoded(), Base64.NO_WRAP));
            } else {
                // 키 쌍이 이미 존재함
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(userID, null);
                publicKey = privateKeyEntry.getCertificate().getPublicKey();
                // 공개 키와 개인 키 출력
                Log.d(TAG, "Public Key: " + Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP));
                Toast.makeText(getApplicationContext(), "이미 저장된 생체정보입니다. ", Toast.LENGTH_SHORT).show();
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | KeyStoreException | CertificateException | IOException | UnrecoverableEntryException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "키 생성 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendPublicKeyToServer(PublicKey publicKey) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");

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

        SavePKRequest savePKRequest = new SavePKRequest(publicKeyString, userID, responseListener, errorListener, BiometricActivity.this);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(savePKRequest);
    }

    private CancellationSignal getCancellationSignal() {
        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(() -> notifyUser("Authentication was Cancelled by the user"));
        return cancellationSignal;
    }

    @TargetApi(Build.VERSION_CODES.P)
    private Boolean checkBiometricSupport() {
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
}
