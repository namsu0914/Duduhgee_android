package com.example.registerlogin;

import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {
    //서버 URL 설정(php파일 연동)
    final static private String URL = "http://172.20.26.202:8888/Register.php";
    //final static private String URL = "https://591f-211-227-109-84.ngrok.io//Register.php";
    private Map<String ,String > map;

    public RegisterRequest(String userID, String userPassword, String userName, int userAge, Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);

        map= new HashMap<>();
        map.put("userID",userID);
        map.put("userPassword", userPassword);
        map.put("userName", userName);
        map.put("uerAge", userAge + "");

    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
