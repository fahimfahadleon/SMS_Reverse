package com.horoftech.smsreverse.viewmodel;

import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.horoftech.smsreverse.R;

import org.json.JSONObject;

public class ActivityMainViewModel extends ViewModel {
    public MutableLiveData<JSONObject> message = new MutableLiveData<>();
    public static final String SAVE = "save";
    public MutableLiveData<String>action = new MutableLiveData<>();
    String url;
    String receiver;
    String token;
    public void setToken(String token){
        this.token = token;
    }
    public String getToken(){
        return token;
    }
    public void setUrl(String url){
        this.url = url;
    }
    public void setReceiver(String receiver){
        this.receiver = receiver;
    }
    public String getReceiver(){
        return receiver;
    }
    public String getUrl(){
        return url;
    }
    public void onClick(View vi){
        int id = vi.getId();
        if(id == R.id.save){
            action.setValue(SAVE);
        }
    }
    public void onMessageReceived(String message){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message",message);
            this.message.setValue(jsonObject);
        }catch (Exception e){
            //ignored
        }

    }
}
