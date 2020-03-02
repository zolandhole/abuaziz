package com.yarud.abuaziz;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.yarud.abuaziz.utils.HandlerServer;
import com.yarud.abuaziz.utils.ResponServer;
import com.yarud.abuaziz.utils.ServiceAddress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RekamanKajianActivity extends AppCompatActivity {

    private static final String TAG = "RekamanKajianActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rekaman_kajian);
        getListRekaman();
    }

    private void getListRekaman() {
        List<String> list = new ArrayList<>();
        list.add("surampak");
        HandlerServer handlerServer = new HandlerServer(RekamanKajianActivity.this, ServiceAddress.GETDATAREKAMAN);
        synchronized (this){
            handlerServer.sendDataToServer(new ResponServer() {
                @Override
                public void gagal(String result) {
                    Log.e(TAG, "gagal: " + result);
                }

                @Override
                public void berhasil(JSONArray jsonArray) {
                    JSONObject object;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            object = jsonArray.getJSONObject(i);
                            String id = object.getString("id");
                            if (!id.equals("0")){
                                Log.e(TAG, "berhasil: data ada");
                            } else {
                                Log.e(TAG, "berhasil: data tidak ada");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, list);
        }
    }
}
