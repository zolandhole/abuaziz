package com.yarud.abuaziz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.yarud.abuaziz.adapters.AdapterRekaman;
import com.yarud.abuaziz.models.ModelRekaman;
import com.yarud.abuaziz.utils.HandlerServer;
import com.yarud.abuaziz.utils.RecyclerViewItem;
import com.yarud.abuaziz.utils.ResponServer;
import com.yarud.abuaziz.utils.ServiceAddress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RekamanKajianActivity extends AppCompatActivity {

    private static final String TAG = "RekamanKajianActivity";
    private RecyclerView recycler_rekaman;
    private List<RecyclerViewItem> recyclerViewItems;
    private LinearLayout linear_list_rekaman;
    private RelativeLayout relative_no_rekaman;
    private Button btn_back;
    private ModelRekaman listRekaman;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rekaman_kajian);
        linear_list_rekaman = findViewById(R.id.linear_list_rekaman);
        relative_no_rekaman = findViewById(R.id.relative_no_rekaman);
        recycler_rekaman = findViewById(R.id.recycler_rekaman);
        btn_back = findViewById(R.id.btn_back);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                    setupListRekaman(jsonArray);
                }
            }, list);
        }
    }

    private void setupListRekaman(JSONArray jsonArray) {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        DBHandler dbHandler = new DBHandler(this);
        recyclerViewItems = new ArrayList<>();
        AdapterRekaman adapterRekaman = new AdapterRekaman(crateListData(jsonArray), this);
        Log.e(TAG, "setupListRekaman: " + listRekaman.getNama());
        if (listRekaman != null){
            recycler_rekaman.setLayoutManager(linearLayoutManager);
            recycler_rekaman.setAdapter(adapterRekaman);
        }
    }

    private List<RecyclerViewItem> crateListData(JSONArray jsonArray) {
        recyclerViewItems = new ArrayList<>();

        if (jsonArray != null){
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String idRekaman = jsonObject.getString("id");
                    if (!idRekaman.equals("0")){
                        String nama = jsonObject.getString("nama").substring(0, jsonObject.getString("nama").length() - 4);
                        ModelRekaman modelRekaman = new ModelRekaman(
                                jsonObject.getString("id"),
                                nama,
                                jsonObject.getString("upload_date"),
                                jsonObject.getString("status")
                        );
                        listRekaman = modelRekaman;
                        Log.e(TAG, "crateListData: " + modelRekaman.getNama());
                        recyclerViewItems.add(modelRekaman);
                        relative_no_rekaman.setVisibility(View.GONE);
                        linear_list_rekaman.setVisibility(View.VISIBLE);
                    } else {
                        relative_no_rekaman.setVisibility(View.VISIBLE);
                        linear_list_rekaman.setVisibility(View.GONE);
                        btn_back.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return recyclerViewItems;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
