package com.example.registerlogin;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

public class BuyRequest extends StringRequest {
    final static private String URL = "http://192.168.0.12:80/SendChallenge.php";

    public BuyRequest(Response.Listener<String> listener){
        super(Method.GET, URL, listener, null);
    }

}
