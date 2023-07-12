package com.example.registerlogin;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class CheckPKRequest extends StringRequest {
    final static private String URL = "http://192.168.0.12:80/CheckPK.php";
    private Map<String ,String > map;

    public CheckPKRequest(String userID, Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);

        map= new HashMap<>();
        map.put("userID",userID);
    }
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}