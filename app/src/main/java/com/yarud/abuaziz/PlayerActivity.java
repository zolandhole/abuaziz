package com.yarud.abuaziz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yarud.abuaziz.services.RekamanService;

import java.util.Objects;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "PlayerActivity";
    private String judul, upload_date, link, idSong;
    private Button player_btn_play, player_btn_stop, player_btn_pause, player_btn_resume;
    private TextView player_tanggal, player_judul_kajian, player_judul;
    private AppCompatSeekBar player_seekbar;
    private int maxSeek, progressSeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Intent intent = getIntent();
        idSong = intent.getStringExtra("id");
        judul = intent.getStringExtra("nama");
        upload_date = intent.getStringExtra("upload_date");
        link = intent.getStringExtra("status");

        player_btn_play = findViewById(R.id.player_btn_play);
        player_btn_stop = findViewById(R.id.player_btn_stop);
        player_btn_pause = findViewById(R.id.player_btn_pause);
        player_btn_resume = findViewById(R.id.player_btn_resume);
        player_tanggal = findViewById(R.id.player_tanggal);
        player_judul_kajian = findViewById(R.id.player_judul_kajian);
        player_judul = findViewById(R.id.player_judul);
        player_seekbar = findViewById(R.id.player_seekbar);

        Log.e(TAG, "onCreate: " + judul + upload_date + link);
        daftarkanBroadcast();
        playRekaman();
//        if (!isMyServiceRunning()){
//            playRekaman();
//        } else {
//            Log.e(TAG, "onCreate: HERE");
//            sendBroadcast(new Intent("idsong"));
//        }

        player_btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRekaman();
            }
        });

        player_btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRekaman();
            }
        });

        player_btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseRekaman();
            }
        });

        player_btn_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent("startrekaman"));
            }
        });
    }

    private void playRekaman() {
        player_tanggal.setText(upload_date);
        player_judul_kajian.setText(judul);
        Bundle bundle = new Bundle();
        bundle.putString("idsong", idSong);
        bundle.putString("url", link);
        bundle.putString("name", "MA'HAD ABU AZIZ");
        bundle.putString("judul_kajian", judul);
        bundle.putString("pemateri", "Rekaman Kajian");
        Intent intent = new Intent(PlayerActivity.this, RekamanService.class);
        intent.putExtras(bundle);
        startService(intent);
    }

    private void stopRekaman() {
        Intent intent = new Intent("exitrekaman");
        sendBroadcast(intent);
    }

    private void pauseRekaman(){
        Intent intent = new Intent("stoprekaman");
        sendBroadcast(intent);
    }

    private void daftarkanBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("mediaplayedrekaman");
        filter.addAction("pausePlayerrekaman");
        filter.addAction("streamingErrorrekaman");
        filter.addAction("lemotrekaman");
        filter.addAction("lemotrekaman");
        filter.addAction("tidaklemotrekaman");
        filter.addAction("mediastopedrekaman");
        filter.addAction("duration");
        filter.addAction("UPDATESEEK");
        filter.addAction("resultidsong");
        registerReceiver(broadcastReceiver, filter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String params = intent.getAction();
            assert params != null;
            switch (params){
                case "mediaplayedrekaman":
                    played();
                    break;
                case "mediastopedrekaman":
                    player_btn_play.setVisibility(View.VISIBLE);
                    player_btn_stop.setVisibility(View.GONE);
                    player_btn_pause.setVisibility(View.GONE);
                    player_btn_resume.setVisibility(View.GONE);
                    player_judul.setText(R.string.dihentikan);
                    player_seekbar.setProgress(0);
                    break;
                case "pausePlayerrekaman":
                    player_btn_play.setVisibility(View.GONE);
                    player_btn_stop.setVisibility(View.VISIBLE);
                    player_btn_pause.setVisibility(View.GONE);
                    player_btn_resume.setVisibility(View.VISIBLE);
                    player_judul.setText(R.string.dijeda);
                    break;
                case "duration":
                    maxSeek = intent.getIntExtra("duration", 0);
                    player_seekbar.setMax(intent.getIntExtra("duration", 0) - 1000);
                    break;
                case "UPDATESEEK":
                    updateSeekBar(intent.getIntExtra("valueseekbar", 0));
                    break;
                case "resultidsong":
                    Log.e(TAG, "onReceive: IDSONG" + Objects.requireNonNull(intent.getExtras()).getString("songid"));
                    if (idSong.equals(Objects.requireNonNull(intent.getExtras()).getString("songid"))){
                        player_tanggal.setText(upload_date);
                        player_judul_kajian.setText(judul);
                        player_seekbar.setMax(maxSeek);
                        player_seekbar.setProgress(progressSeek);
                        played();
                    }
                    break;
            }
        }
    };

    private void updateSeekBar(int valueseekbar) {
        progressSeek = valueseekbar;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            player_seekbar.setProgress(valueseekbar, true);
        } else {
            player_seekbar.setProgress(valueseekbar);
        }
        player_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    Intent intent = new Intent("userSkip");
                    intent.putExtra("progress", progress);
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void played(){
        player_btn_play.setVisibility(View.GONE);
        player_btn_stop.setVisibility(View.VISIBLE);
        player_btn_pause.setVisibility(View.VISIBLE);
        player_btn_resume.setVisibility(View.GONE);
        player_judul.setText(R.string.sedang_diputar);
    }

}
