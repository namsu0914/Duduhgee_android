package com.example.registerlogin;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class VerifyRequest extends StringRequest{
    final static private String URL = "http://192.168.0.12:80/Verify.php";
    //집: 192.168.45.185
    //final static private String URL = "https://591f-211-227-109-84.ngrok.io//Register.php";
    private Map<String ,String > map;

    public VerifyRequest(String SignString, String userID, Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);

        map= new HashMap<>();
        map.put("SignString",SignString);
        map.put("userID", userID);

    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
