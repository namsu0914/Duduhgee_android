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

public class BuyActivity extends AppCompatActivity {

    private Button btn_buy;
    //private static final String KEY_NAME = userID;
    private KeyStore keyStore;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private BiometricPrompt.AuthenticationCallback authenticationCallback;
    private CancellationSignal cancellationSignal = null;
    private static final String TAG = BuyActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        btn_buy = findViewById(R.id.btn_buy);

        // 구매하기 버튼
        btn_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String userID = intent.getStringExtra("userID");

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

                                boolean hasAccess = checkPrivateKeyAccess(userID);
                                if (hasAccess) {
                                    // 개인 키에 액세스할 수 있는 권한이 있음
                                    Log.d(TAG, "개인 키에 액세스할 수 있는 권한이 있음");
                                } else {
                                    // 개인 키에 액세스할 수 있는 권한이 없음
                                    Log.d(TAG, "개인 키에 액세스할 수 있는 권한이 없음");
                                    // TODO: 개인 키 생성 또는 액세스 권한 요청 로직을 추가해야 합니다.
                                    // generateKeyPair();
                                    // requestPrivateKeyAccess(userID);
                                }

                                if (keyStore.containsAlias(userID)) {
                                    // 키 쌍이 존재함
                                    // 공개 키와 개인 키 출력
                                    Log.d(TAG, "키스토어 저장됨");
                                } else {
                                    // 키 쌍이 존재하지 않음
                                    Log.d(TAG, "Key pair not found");
                                    // TODO: 키 쌍 생성 로직을 추가해야 합니다.
                                    // generateKeyPair();
                                }

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

                                        KeyStore.PrivateKeyEntry privateKeyEntry = null;
                                        try {
                                            privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(userID, null);
                                        } catch (KeyStoreException e) {
                                            throw new RuntimeException(e);
                                        } catch (NoSuchAlgorithmException e) {
                                            throw new RuntimeException(e);
                                        } catch (UnrecoverableEntryException e) {
                                            throw new RuntimeException(e);
                                        }
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

                                        try {
                                            verifySignature(signedChallenge, challenge, userID); // userID에 실제 사용자의 ID를 전달해야 함
                                        } catch (KeyStoreException | CertificateException |
                                                 IOException | NoSuchAlgorithmException |
                                                 UnrecoverableEntryException |
                                                 KeyManagementException e) {
                                            throw new RuntimeException(e);
                                        }

                                    }
                                };
                                if (checkBiometricSupport()) {
                                    BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(BuyActivity.this)
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

                            } else {
                                Log.e(TAG, "Challenge key not found in JSON response");
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "오류가 발생하였습니다. ", Toast.LENGTH_SHORT).show();
                            throw new RuntimeException(e);
                        } catch (CertificateException | KeyStoreException | IOException |
                                 NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                BuyRequest buyRequest = new BuyRequest(responseListener);
                RequestQueue queue = Volley.newRequestQueue(BuyActivity.this);
                queue.add(buyRequest);
            }
        });
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

        VerifyRequest verifyRequest = new VerifyRequest(userID, chall, Base64.encodeToString(signString, Base64.NO_WRAP), stringpublicKey, responseListener, BuyActivity.this);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(verifyRequest);
    }

    private void notifyUser(String message) {
        Toast.makeText(BuyActivity.this, message, Toast.LENGTH_SHORT).show();
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
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
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