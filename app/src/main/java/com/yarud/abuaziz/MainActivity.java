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
import com.yarud.abuaziz.utils.HandlerServer;
import com.yarud.abuaziz.utils.RecyclerViewItem;
import com.yarud.abuaziz.utils.ResponServer;
import com.yarud.abuaziz.utils.ResponShoutcast;
import com.yarud.abuaziz.utils.ServiceAddress;
import com.yarud.abuaziz.utils.Space;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

        InternetAvailabilityChecker.init(this);
        InternetAvailabilityChecker mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        mInternetAvailabilityChecker.addInternetConnectivityListener(this);

        daftarkanBroadcast();
        initRecyclerView();
        playStreaming();
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
        recyclerView.addItemDecoration(new Space(20));
        recyclerView.setAdapter(new AdapterChat(crateListData(), this));
    }

    private List<RecyclerViewItem> crateListData() {
        List<RecyclerViewItem> recyclerViewItems = new ArrayList<>();
        ModelHeader header = new ModelHeader(
                "YADI RUDIYANSAH",
                "yadi.rudiyansah@hariff.com",
                "https://cdn.pixabay.com/photo/2017/09/30/15/10/pizza-2802332_640.jpg"
        );
        recyclerViewItems.add(header);

        String[] profileImage = {"https://cdn.pixabay.com/photo/2016/11/18/17/42/barbecue-1836053_640.jpg",
                "https://cdn.pixabay.com/photo/2016/07/11/03/23/chicken-rice-1508984_640.jpg",
                "https://cdn.pixabay.com/photo/2017/03/30/08/10/chicken-intestine-2187505_640.jpg",
                "https://cdn.pixabay.com/photo/2017/02/15/15/17/meal-2069021_640.jpg",
                "https://cdn.pixabay.com/photo/2017/06/01/07/15/food-2362678_640.jpg"};
        String[] namaPengirim = {"Yadi", "Rudi", "Yansah", "Surampak", "Sakosoy"};
        String[] jam = {"j","a","m","e","d"};
        String[] waktu = {"j","a","m","e","d"};
        String[] pesan = {"Tunduh", "pisan", "euy", "hayang", "sare"};
        String[] id_login = {"Tunduh", "pisan", "euy", "hayang", "sare"};
        for (int i = 0; i < profileImage.length; i++) {
            ModelChat chat = new ModelChat(pesan[i],waktu[i],jam[i],namaPengirim[i],profileImage[i],id_login[i]);
            recyclerViewItems.add(chat);
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
        getJudulKajian();
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
                                tv_titlekajian.setText(object.getString("judul_kajian"));
                                tv_pemateri.setText(object.getString("pemateri"));
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
