package com.example.registerlogin;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class SavePKRequest extends StringRequest {
    private static final String URL = "http://192.168.0.12:80/SavePK.php";
    private final Map<String, String> map;

    public SavePKRequest(String publicKey, String userID, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, URL, listener, errorListener);

        map = new HashMap<>();
        map.put("userID", userID);
        map.put("publicKey", publicKey);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}

