

//package com.example.registerlogin;
//
//import android.util.Base64;
//import android.util.Log;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.security.InvalidKeyException;
//import java.security.KeyStore;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.PrivateKey;
//import java.security.Signature;
//import java.security.SignatureException;
//import java.security.UnrecoverableEntryException;
//import java.security.cert.CertificateException;
//
//public class SignatureActivity extends AppCompatActivity {
//    private static final String TAG = "SignatureActivity";
//    private static final String alias = "my_key";
//
//    public String signChallenge(String challenge, PrivateKey privateKey) {
//        try {
//            Signature signature = Signature.getInstance("SHA256withRSA");
//            signature.initSign(privateKey);
//            signature.update(challenge.getBytes());
//            byte[] encodedSignature = signature.sign();
//            String signedChallenge = Base64.encodeToString(encodedSignature, Base64.DEFAULT);
//            Log.d(TAG, "서명: " + signedChallenge); // 로그 출력
//            return signedChallenge;
//        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//}
package com.example.asm;

import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

public class ASM_SignatureActivity extends AppCompatActivity {
    private static final String TAG = "SignatureActivity";
    //private static final String KEY_NAME = "my_key";

    public byte[] signChallenge(String challenge, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(challenge.getBytes());
            byte[] encodedSignature = signature.sign();
            String signedChallenge = Base64.encodeToString(encodedSignature, Base64.NO_WRAP);

            return encodedSignature;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            Log.d(TAG, "Exception occurred: " + e.getMessage(), e); // Log the exception
            return null;
        }
    }

    // Other methods and code within the SignatureActivity class
}

