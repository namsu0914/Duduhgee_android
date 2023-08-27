package com.example.rp;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

public class RP_BuyRequest extends StringRequest {
    final static private String URL = "https://192.168.0.5:443/SendChallenge.php";

    public RP_BuyRequest(Response.Listener<String> listener){
        super(Method.GET, URL, listener, null);
    }

}
