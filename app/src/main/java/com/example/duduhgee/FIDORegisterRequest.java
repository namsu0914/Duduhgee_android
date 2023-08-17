package com.example.duduhgee;

import static com.example.duduhgee.RegisterRequest.getPinnedCertSslSocketFactory;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class FIDORegisterRequest extends StringRequest {
    final static private String URL = "https://192.168.0.5:443/FIDORegisterRequest.php";
    private Map<String ,String > map;
    public FIDORegisterRequest(String userID, Response.Listener<String> listener, Context context) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        super(Method.POST, URL, listener, null);
        map= new HashMap<>();
        map.put("userID",userID);
        SSLSocketFactory sslSocketFactory = getPinnedCertSslSocketFactory(context, R.raw.server);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
    }
}
