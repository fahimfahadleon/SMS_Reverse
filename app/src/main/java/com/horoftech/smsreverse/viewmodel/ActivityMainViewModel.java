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
    public void setUrl(String url){
        this.url = url;
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
    public void onMessageReceived(String sender,String message){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sender",sender);
            jsonObject.put("message",message);
            this.message.setValue(jsonObject);
        }catch (Exception e){
            //ignored
        }

    }
}
