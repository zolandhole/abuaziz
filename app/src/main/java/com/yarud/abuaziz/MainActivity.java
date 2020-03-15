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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.yarud.abuaziz.models.ModelIklan;
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
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements InternetConnectivityListener {
    private static final String TAG = "MainActivity";
    private ImageButton btn_player, btn_stop;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mServerStreaming = mRootRef.child("streamingaddress");
    private DatabaseReference mAlamat = mRootRef.child("alamat");
    public String streamingURL, base_url, token_fcm;
    private TextView tv_messageError, tv_titlekajian, tv_pemateri, juduliklan, descriptioniklan;
    private ProgressBar progressBarPlayer, main_loading;
    private Boolean internetConnection = true;
    private CardView cv_nointernet;
    private Animation animFadeIn, animFadeOut;
    private RelativeLayout view_sukses, rl_newmessage;
    private ConstraintLayout view_offline;
    private RelativeLayout titlekajian;
    private boolean doubleBackToExitPressedOnce = false;
    private DBHandler dbHandler;
    private String ID_LOGIN, JUDUL_KAJIAN, PEMATERI;
    private LinearLayout kirim_pesan;
    private ModelHeader modelHeader;
    private ModelIklan modelIklan;
    private JSONArray jsonArrayChat;
    private Button btn_send;
    private RecyclerView recyclerView;
    private AdapterChat adapterChat;
    private List<RecyclerViewItem> recyclerViewItems;
    private LinearLayoutManager linearLayoutManager;
    private ImageView photoIklan;

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
        btn_send = findViewById(R.id.streaming_sendpesan);
        rl_newmessage = findViewById(R.id.rl_newmessage);
        recyclerView = findViewById(R.id.recycler_chat);
        recyclerView.setVisibility(View.GONE);
        photoIklan = findViewById(R.id.photoiklan);
        juduliklan = findViewById(R.id.juduliklan);
        descriptioniklan = findViewById(R.id.descriptioniklan);
        main_loading = findViewById(R.id.main_loading);
        Button btn_listkajian = findViewById(R.id.btn_listkajian);

        InternetAvailabilityChecker.init(this);
        InternetAvailabilityChecker mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        mInternetAvailabilityChecker.addInternetConnectivityListener(this);


        main_loading.setVisibility(View.VISIBLE);
        daftarkanBroadcast();
        playStreaming();
        getDataChatting();

        modelHeader = new ModelHeader("", "", null);
        modelIklan = new ModelIklan(null, "", "");
        getDataIklan();

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
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kirimPesan();
            }
        });

        btn_listkajian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RekamanKajianActivity.class));
            }
        });
        rl_newmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.smoothScrollToPosition(Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 1);
                rl_newmessage.setVisibility(View.GONE);
            }
        });


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int scrollposition = linearLayoutManager.findLastVisibleItemPosition();
                if (scrollposition == Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 2){
                    rl_newmessage.setVisibility(View.GONE);
                }
            }
        });
        generateTokenFCM();
    }

    private void kirimPesan() {
        final EditText editTextPesan = findViewById(R.id.streaming_edittext);
        String pesan = editTextPesan.getText().toString().trim();
        final ProgressBar progressBar_send = findViewById(R.id.progress_bar_send);
        if (!pesan.equals("")){
            btn_send.setVisibility(View.GONE);
            progressBar_send.setVisibility(View.VISIBLE);
            editTextPesan.setEnabled(false);
            List<String> list = new ArrayList<>();
            list.add(ID_LOGIN);
            list.add(pesan);
            HandlerServer handlerServer = new HandlerServer(MainActivity.this, ServiceAddress.TAMBAHCHAT);
            synchronized (this){
                handlerServer.sendDataToServer(new ResponServer() {
                    @Override
                    public void gagal(String result) {
                        Log.e(TAG, "gagal: " + result);
                        Toast.makeText(MainActivity.this, "Gagal Mengirim pesan, silahkan coba lagi", Toast.LENGTH_SHORT).show();
                        btn_send.setVisibility(View.VISIBLE);
                        progressBar_send.setVisibility(View.GONE);
                        editTextPesan.setEnabled(true);
                    }

                    @Override
                    public void berhasil(JSONArray jsonArray) {
                        Log.e(TAG, "berhasil: " + jsonArray);
                        editTextPesan.setText("");
                        btn_send.setVisibility(View.VISIBLE);
                        progressBar_send.setVisibility(View.GONE);
                        editTextPesan.setEnabled(true);
                        recyclerView.smoothScrollToPosition(Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 1);
                    }
                }, list);
            }
        } else {
            Toast.makeText(this, "Isi pesan di kolom", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initRecyclerView() {
        linearLayoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapterChat = new AdapterChat(crateListData(), this, dbHandler);
        recyclerView.setAdapter(adapterChat);
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null){
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });
//        recyclerView.smoothScrollToPosition(Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() -1);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private List<RecyclerViewItem> crateListData() {
        recyclerViewItems = new ArrayList<>();
        ModelHeader header = modelHeader;
        recyclerViewItems.add(header);

        ModelIklan iklan = modelIklan;
        recyclerViewItems.add(iklan);

        if (jsonArrayChat != null){
            for (int i = 0; i < jsonArrayChat.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArrayChat.getJSONObject(i);
                    ModelChat modelChat = new ModelChat(
                            jsonObject.getString("id"),
                            jsonObject.getString("pesan"),
                            jsonObject.getString("jam"),
                            jsonObject.getString("photo"),
                            jsonObject.getString("pengirim")
                    );
                    recyclerViewItems.add(modelChat);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
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
        FirebaseMessaging.getInstance().subscribeToTopic("PESANUSER");
        FirebaseMessaging.getInstance().subscribeToTopic("UPDATEIKLAN");
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
                            getDataIklan();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }, list);
        }
    }

    private void getDataIklan() {
        List<String> list = new ArrayList<>();
        list.add(ID_LOGIN);
        HandlerServer handlerServer = new HandlerServer(this, ServiceAddress.GETDATAIKLAN);
        synchronized (this){
            handlerServer.sendDataToServer(new ResponServer() {
                @Override
                public void gagal(String result) {
                    Log.e(TAG, "gagal ambil data Iklan: "+ result);
                }

                @Override
                public void berhasil(JSONArray jsonArray) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            modelIklan = new ModelIklan(
                                    jsonObject.getString("photoiklan"),
                                    jsonObject.getString("juduliklan"),
                                    jsonObject.getString("deskripsiiklan")
                            );
                            Log.e(TAG, "berhasil: " + jsonArray);
                            updateIklan(jsonObject.getString("photoiklan"), jsonObject.getString("juduliklan"), jsonObject.getString("deskripsiiklan"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    getDataChatting();
                }
            }, list);
        }
    }

    private void updateIklan(String photoiklan, String juduliklans, String deskripsiiklan) {
        Glide.with(MainActivity.this).load(photoiklan).placeholder(R.drawable.button_abu).into(photoIklan);
        juduliklan.setText(juduliklans);
        descriptioniklan.setText(deskripsiiklan);
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
                    initRecyclerView();
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
                        String test;
                        main_loading.setVisibility(View.GONE);
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
        if (isMyServiceRunning()){
            suksesStop();
        } else {
            suksesPlay();
        }
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
            sendBroadcast(new Intent("exitrekaman"));
            sendBroadcast(new Intent("exit"));
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
        filter.addAction("PESANUSER");
        filter.addAction("UPDATEIKLAN");
        registerReceiver(broadcastReceiver, filter);
    }

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
                    checkStatusServer();
                    tv_messageError.setText(R.string.koneksi_lambat);
                    cv_nointernet.setAnimation(animFadeIn);
                    break;
                case "tidaklemot":
                    cv_nointernet.setAnimation(animFadeOut);
                case "streamingError":
                    Log.e(TAG, "onReceive: ERROR");
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
                    sendBroadcast(new Intent("exitrekaman"));
                    break;
                case "PESANUSER":
                    Log.e(TAG, "onReceive: " + intent.getStringExtra("pesan"));
                    ModelChat item = new ModelChat(
                            intent.getStringExtra("id"),
                            intent.getStringExtra("pesan"),
                            intent.getStringExtra("jam"),
                            intent.getStringExtra("photo"),
                            intent.getStringExtra("pengirim")
                    );
                    recyclerViewItems.add(item);
                    adapterChat.notifyDataSetChanged();

                    int scrollposition = linearLayoutManager.findLastVisibleItemPosition();
                    if (scrollposition != Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 2){
                        rl_newmessage.setVisibility(View.VISIBLE);
                    } else {
                        rl_newmessage.setVisibility(View.GONE);
                        recyclerView.smoothScrollToPosition(Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 1);
                    }
                    break;
                case "UPDATEIKLAN":
                    Log.e(TAG, "onReceive: " + intent.getStringExtra("photoiklan"));
                    modelIklan = new ModelIklan(
                            intent.getStringExtra("photoiklan"),
                            intent.getStringExtra("juduliklan"),
                            intent.getStringExtra("deskripsiiklan")
                    );
                    recyclerViewItems.set(1,modelIklan);
                    adapterChat.notifyItemChanged(1);
                    updateIklan(intent.getStringExtra("photoiklan"), intent.getStringExtra("juduliklan"), intent.getStringExtra("deskripsiiklan"));
                    break;
            }
        }
    };

    private void checkStatusServer() {
        HandlerServer handlerServer = new HandlerServer(MainActivity.this, streamingURL+"/statistics?json=1");
        handlerServer.getStatusServerShoutcast(new ResponShoutcast() {
            @Override
            public void result(JSONObject jsonObject) {
                try {
                    String activestreams = jsonObject.getString("activestreams");
                    if (!activestreams.equals("1")){
                        Toast.makeText(MainActivity.this, "Kajian telah berakhir", Toast.LENGTH_LONG).show();
                        view_sukses.setVisibility(View.GONE);
                        view_offline.setVisibility(View.VISIBLE);
                        suksesStop();
                        stopStreaming();
                        clear();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

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

    public void clear() {
        int size = recyclerViewItems.size();
        recyclerViewItems.clear();
        adapterChat.notifyItemRangeRemoved(0, size);
    }

}
