package com.yarud.abuaziz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;
import com.yarud.abuaziz.adapters.AdapterChat;
import com.yarud.abuaziz.models.ModelChat;
import com.yarud.abuaziz.models.ModelHeader;
import com.yarud.abuaziz.services.StreamingService;
import com.yarud.abuaziz.utils.DBHandler;
import com.yarud.abuaziz.utils.HandlerServer;
import com.yarud.abuaziz.utils.RecyclerViewItem;
import com.yarud.abuaziz.utils.ResponServer;
import com.yarud.abuaziz.utils.ResponShoutcast;
import com.yarud.abuaziz.utils.ServiceAddress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements InternetConnectivityListener {
    private static final String TAG = "MainActivity";
    private ImageButton btn_player, btn_stop;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mServerStreaming = mRootRef.child("streamingaddress");
    private DatabaseReference mAlamat = mRootRef.child("alamat");
    public String streamingURL, base_url, token_fcm;
    private TextView tv_messageError, tv_titlekajian, tv_pemateri;
    private ProgressBar progressBarPlayer;
    private Boolean internetConnection = true;
    private CardView cv_nointernet;
    private Animation animFadeIn, animFadeOut;
    private RelativeLayout view_sukses;
    private ConstraintLayout view_offline;
    private RelativeLayout titlekajian;
    private boolean doubleBackToExitPressedOnce = false;
    private DBHandler dbHandler;
    private String ID_LOGIN, JUDUL_KAJIAN, PEMATERI;
    private LinearLayout kirim_pesan;
    private ModelHeader modelHeader;
    private ModelChat modelChat;
    private JSONArray jsonArrayChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLinkStreaming();

        btn_player = findViewById(R.id.btn_player);
        btn_stop = findViewById(R.id.btn_stop);
        tv_messageError = findViewById(R.id.tv_messageError);
        tv_titlekajian = findViewById(R.id.tv_titlekajian);
        tv_pemateri = findViewById(R.id.tv_pemateri);
        progressBarPlayer = findViewById(R.id.progressBarPlayer);
        cv_nointernet = findViewById(R.id.cv_nointernet);
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        view_sukses = findViewById(R.id.view_sukses);
        view_offline = findViewById(R.id.view_offline);
        titlekajian = findViewById(R.id.titlekajian);
        kirim_pesan = findViewById(R.id.kirim_pesan);
        dbHandler = new DBHandler(this);

        InternetAvailabilityChecker.init(this);
        InternetAvailabilityChecker mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        mInternetAvailabilityChecker.addInternetConnectivityListener(this);

        daftarkanBroadcast();
        playStreaming();

        modelHeader = new ModelHeader("", "", null);
        modelChat = new ModelChat("","","","","",null,"");
        initRecyclerView();

        btn_player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playStreaming();
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopStreaming();
            }
        });

        generateTokenFCM();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_chat);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AdapterChat(crateListData(), this, dbHandler));
    }

    private List<RecyclerViewItem> crateListData() {
        List<RecyclerViewItem> recyclerViewItems = new ArrayList<>();
        ModelHeader header = modelHeader;
        recyclerViewItems.add(header);

//        String[] profileImage = {"https://cdn.pixabay.com/photo/2016/11/18/17/42/barbecue-1836053_640.jpg"};
//        String[] id = {""};
//        String[] namaPengirim = {"Yadi"};
//        String[] jam = {"j"};
//        String[] waktu = {"j"};
//        String[] pesan = {"Tunduh"};
//        String[] id_login = {"Tunduh"};
//        for (int i = 0; i < profileImage.length; i++) {
//            ModelChat chat = new ModelChat(id[i], pesan[i],waktu[i],jam[i],namaPengirim[i],profileImage[i],id_login[i]);
//            recyclerViewItems.add(chat);
//        }
//        recyclerViewItems.add(modelChat);
        if (jsonArrayChat != null){
            for (int i = 0; i < jsonArrayChat.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArrayChat.getJSONObject(i);
                    modelChat = new ModelChat(
                            jsonObject.getString("id"),
                            jsonObject.getString("pesan"),
                            jsonObject.getString("waktu"),
                            jsonObject.getString("jam"),
                            jsonObject.getString("id_login"),
                            jsonObject.getString("photo"),
                            jsonObject.getString("pengirim")
                    );
                    recyclerViewItems.add(modelChat);
                } catch (JSONException e) {
                    modelChat = new ModelChat("","","","","",null,"");
                    recyclerViewItems.add(modelChat);
                    e.printStackTrace();
                }
            }
        } else {
            modelChat = new ModelChat("","","","","",null,"");
            recyclerViewItems.add(modelChat);
        }

        return recyclerViewItems;
    }

    private void generateTokenFCM() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token_fcm = instanceIdResult.getToken();
                Log.e(TAG, "onSuccess: " + token_fcm);
            }
        });
        FirebaseMessaging.getInstance().subscribeToTopic("JUDULKAJIAN");
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDataChatting();
        getCurrentUser();
        getJudulKajian();
    }

    private void getCurrentUser() {
        ID_LOGIN = checkUserOnDB();
        if (ID_LOGIN == null){
            kirim_pesan.setVisibility(View.GONE);
        } else {
            kirim_pesan.setVisibility(View.VISIBLE);
            getProfileUntukHeader();
        }
    }

    private void getProfileUntukHeader() {
        List<String> list = new ArrayList<>();
        list.add(ID_LOGIN);
        HandlerServer handlerServer = new HandlerServer(this, ServiceAddress.GETPROFILE);
        synchronized (this){
            handlerServer.sendDataToServer(new ResponServer() {
                @Override
                public void gagal(String result) {
                    dbHandler.deleteDB();
                    initRecyclerView();
                }

                @Override
                public void berhasil(JSONArray jsonArray) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            modelHeader = new ModelHeader(
                                    jsonObject.getString("nama"),
                                    jsonObject.getString("email"),
                                    jsonObject.getString("photo")
                            );
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    initRecyclerView();
                }
            }, list);
        }
    }

    private void getDataChatting() {
        List<String> list = new ArrayList<>();
        list.add(ID_LOGIN);
        HandlerServer handlerServer = new HandlerServer(this, ServiceAddress.GETDATACHAT);
        synchronized (this){
            handlerServer.sendDataToServer(new ResponServer() {
                @Override
                public void gagal(String result) {
                    Log.e(TAG, "gagal ambil data chatting: "+ result);
                }

                @Override
                public void berhasil(JSONArray jsonArray) {
                    jsonArrayChat = jsonArray;
                }
            }, list);
        }
    }

    private String checkUserOnDB(){
        ArrayList<HashMap<String, String>> userDB = dbHandler.getUser(1);
        for (Map<String, String> map : userDB){
            ID_LOGIN = map.get("id_login");
        }
        return ID_LOGIN;
    }

    private void playStreaming() {
        if (!internetConnection){
            noInternetConnection();
        } else {
            yesInternetConection();
            getStatusStreaming();
        }
    }

    private void stopStreaming() {
        Intent intent = new Intent("exit");
        sendBroadcast(intent);
    }

    private void getStatusStreaming(){
        suksesLoading();
        if (streamingURL == null) {
            getLinkStreaming();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    suksesLoading();
                    if (streamingURL == null){
                        streamingURL = "http://122.248.39.157:8050";
                        if (isMyServiceRunning()){
                            new ServiceStreaming().execute();
                        } else {
                            Log.e(TAG, "getStatusStreaming: Service Already Running");
                            suksesPlay();
                        }
                    } else {
                        playStreaming();
                    }
                }
            }, 3000);
        } else {
            Log.e(TAG, "onCreate: "+ base_url);
            if (isMyServiceRunning()){
                new ServiceStreaming().execute();
            } else {
                Log.e(TAG, "getStatusStreaming: Service Already Running");
                suksesPlay();
            }
        }
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (StreamingService.class.getName().equals(service.service.getClassName())) {
                return false;
            }
        }
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    private class ServiceStreaming extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            Log.e(TAG, "onPreExecute: " + streamingURL);
            getJudulKajian();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            Log.e(TAG, "doInBackground: "+ streamingURL);
            HandlerServer handlerServer = new HandlerServer(MainActivity.this, streamingURL+"/statistics?json=1");
            handlerServer.getStatusServerShoutcast(new ResponShoutcast() {
                @Override
                public void result(JSONObject jsonObject) {
                    try {
                        String activestreams = jsonObject.getString("activestreams");
                        Log.e(TAG, "result activestreams: "+ activestreams);
                        if (!activestreams.equals("1")){
                            Toast.makeText(MainActivity.this, "Saat ini tidak ada Kajian Online Streaming", Toast.LENGTH_SHORT).show();
                            view_sukses.setVisibility(View.GONE);
                            view_offline.setVisibility(View.VISIBLE);
                            suksesStop();
                        } else {
                            view_sukses.setVisibility(View.VISIBLE);
                            view_offline.setVisibility(View.GONE);
                            jalankanStreaming();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return null;
        }
    }

    private void jalankanStreaming(){
        Bundle bundle = new Bundle();
        bundle.putString("url", streamingURL);
        bundle.putString("name", "MA'HAD ABU AZIZ");
        bundle.putString("judul_kajian", JUDUL_KAJIAN);
        bundle.putString("pemateri", PEMATERI);
        Intent intent = new Intent(MainActivity.this, StreamingService.class);
        intent.putExtras(bundle);
        startService(intent);
        getJudulKajian();
    }

    private void getJudulKajian() {
        if (base_url == null){
            titlekajian.setVisibility(View.GONE);
        } else {
            titlekajian.setVisibility(View.VISIBLE);
            List<String> list = new ArrayList<>();
            list.add(""); list.add("SURAMPAK");
            HandlerServer handlerServer = new HandlerServer(MainActivity.this, base_url+ ServiceAddress.INFOKAJIAN);
            synchronized (this) {
                handlerServer.sendDataToServer(new ResponServer() {
                    @Override
                    public void gagal(String result) {
                        Log.e(TAG, "gagal: GETJUDULKAJIAN: " + result);
                    }

                    @Override
                    public void berhasil(JSONArray jsonArray) {
                        JSONObject object;
                        try {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                object = jsonArray.getJSONObject(i);
                                JUDUL_KAJIAN = object.getString("judul_kajian");
                                PEMATERI = object.getString("pemateri");
                                tv_titlekajian.setText(JUDUL_KAJIAN);
                                tv_pemateri.setText(PEMATERI);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, list);
            }
        }
    }

    private void getLinkStreaming() {
        mServerStreaming.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                streamingURL = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mAlamat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                base_url = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        Log.e(TAG, "onInternetConnectivityChanged: " + isConnected);
        if (isConnected){
            internetConnection = true;
            yesInternetConection();
        } else {
            internetConnection = false;
            noInternetConnection();
        }
    }

    private void noInternetConnection(){
        tv_messageError.setText(R.string.tidak_ada_koneksi_internert);
        cv_nointernet.setVisibility(View.VISIBLE);
        cv_nointernet.setAnimation(animFadeIn);
        suksesLoading();
    }

    private void yesInternetConection(){
        cv_nointernet.setAnimation(animFadeOut);
        cv_nointernet.setVisibility(View.GONE);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(TAG, "onLowMemory: CALL");
        InternetAvailabilityChecker.getInstance().removeAllInternetConnectivityChangeListeners();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tekan lagi untuk keluar aplikasi", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        sendBroadcast(new Intent("exit"));
    }

    private void daftarkanBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("mediaplayed");
        filter.addAction("mediastoped");
        filter.addAction("lemot");
        filter.addAction("tidaklemot");
        filter.addAction("streamingError");
        filter.addAction("pausePlayer");
        filter.addAction("errorsenddata");
        filter.addAction("JUDULKAJIAN");
        registerReceiver(broadcastReceiver, filter);
    }

    private int countError = 1;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String param = intent.getAction();
            assert param != null;
            switch (param){
                case "mediaplayed":
                    suksesPlay();
                    Log.e(TAG, "onReceive: mediaPlayed");
                    break;
                case "mediastoped":
                    progressBarPlayer.setVisibility(View.INVISIBLE);
                    suksesStop();
                    Log.e(TAG, "onReceive: meidaStoped");
                    break;
                case "lemot":
                    String bufferCode = intent.getStringExtra("lemot");
                    assert bufferCode != null;
                    if (bufferCode.equals("703")){
                        Log.e(TAG, "onReceive: Sedang buffering 703");
                    }
                    if (bufferCode.equals("702")){
                        Log.e(TAG, "onReceive: Buffer Completed 702");
                    }
                    if (bufferCode.equals("701")){
                        Log.e(TAG, "onReceive: Buffer Completed 701");
                    }
                    tv_messageError.setText(R.string.koneksi_lambat);
                    cv_nointernet.setAnimation(animFadeIn);
                    break;
                case "tidaklemot":
                    cv_nointernet.setAnimation(animFadeOut);
                case "streamingError":
                    Log.e(TAG, "onReceive: ERROR");
                    countError = countError+1;
                    if (countError <= 2){
                        if (isMyServiceRunning()){
                            suksesLoading();
                            new ServiceStreaming().execute();
                        }
                    } else {
                        suksesStop();
                        Log.e(TAG, "onReceive: ERROR");
                        Toast.makeText(context, "Ada kesalahan, tidak dapat memutar Streaming, hubungi Developer", Toast.LENGTH_SHORT).show();
                    }
                    Log.e(TAG, "onReceive: " + countError);
                    break;
                case "pausePlayer":
                    suksesStop();
                    Log.e(TAG, "onReceive: Pause Media");
                    break;
                case "errorsenddata":
                    Log.e(TAG, "onReceive: Error SendData Volley");
                    break;
                case "JUDULKAJIAN":
                    tv_titlekajian.setText(intent.getStringExtra("judulKajian"));
                    tv_pemateri.setText(intent.getStringExtra("pemateri"));
                    break;
            }
        }
    };

    private void suksesPlay(){
        progressBarPlayer.setVisibility(View.INVISIBLE);
        btn_stop.setVisibility(View.VISIBLE);
        btn_player.setVisibility(View.INVISIBLE);
    }

    private void suksesStop(){
        progressBarPlayer.setVisibility(View.INVISIBLE);
        btn_stop.setVisibility(View.INVISIBLE);
        btn_player.setVisibility(View.VISIBLE);
    }

    private void suksesLoading(){
        progressBarPlayer.setVisibility(View.VISIBLE);
        btn_stop.setVisibility(View.INVISIBLE);
        btn_player.setVisibility(View.INVISIBLE);
    }



}
