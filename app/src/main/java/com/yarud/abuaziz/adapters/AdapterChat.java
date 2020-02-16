package com.yarud.abuaziz.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.yarud.abuaziz.LoginActivity;
import com.yarud.abuaziz.R;
import com.yarud.abuaziz.utils.DBHandler;
import com.yarud.abuaziz.utils.RecyclerViewItem;
import com.yarud.abuaziz.models.ModelChat;
import com.yarud.abuaziz.models.ModelHeader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterChat extends RecyclerView.Adapter {
    private static final String TAG = "AdapterChat";
    private List<RecyclerViewItem> recyclerViewItems;
    private static final int HEADER_ITEM = 0;
    private static final int CHAT_ITEM = 1;
    private Context context;
    private DBHandler dbHandler;
    private String ID_LOGIN;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;

    public AdapterChat(List<RecyclerViewItem> recyclerViewItems, Context context, DBHandler dbHandler){
        this.recyclerViewItems = recyclerViewItems;
        this.context = context;
        this.dbHandler = dbHandler;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row;
        if (viewType == HEADER_ITEM){
            row = inflater.inflate(R.layout.layout_header, parent, false);
            return new HeaderHolder(row);
        } else {
            row = inflater.inflate(R.layout.layout_chat, parent, false);
            return new ChatHolder(row);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RecyclerViewItem recyclerViewItem = recyclerViewItems.get(position);
        if (holder instanceof HeaderHolder){
            HeaderHolder headerHolder = (HeaderHolder) holder;
            ID_LOGIN = checkUserOnDB();
            if (ID_LOGIN == null){
                headerHolder.view_register.setVisibility(View.VISIBLE);
                headerHolder.view_user.setVisibility(View.GONE);
            } else {
                headerHolder.view_register.setVisibility(View.GONE);
                headerHolder.view_user.setVisibility(View.VISIBLE);
                ModelHeader modelHeader = (ModelHeader) recyclerViewItem;
                if (modelHeader.getIMAGEURL() != null){
                    Glide.with(context).load(modelHeader.getIMAGEURL()).placeholder(R.drawable.ic_account).into(headerHolder.profilePhoto);
                }
                headerHolder.namaProfile.setText(modelHeader.getEMAILPROFILE());
                headerHolder.emailProfile.setText(modelHeader.getEMAILPROFILE());
            }
        } else {
            ChatHolder chatHolder = (ChatHolder) holder;
            ModelChat modelChat = (ModelChat) recyclerViewItem;
            if (modelChat.getPhoto() != null){
                Glide.with(context).load(modelChat.getPhoto()).placeholder(R.drawable.ic_account).into(chatHolder.streaming_photo);
            }
            chatHolder.streaming_dari.setText(modelChat.getId_login());
            chatHolder.streaming_jam.setText(modelChat.getJam());
            chatHolder.streaming_pesan.setText(modelChat.getPesan());
        }
    }

    @Override
    public int getItemViewType(int position) {
        RecyclerViewItem recyclerViewItem = recyclerViewItems.get(position);
        if (recyclerViewItem instanceof ModelHeader)
            return HEADER_ITEM;
        else if (recyclerViewItem instanceof ModelChat)
            return CHAT_ITEM;
        else
            return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return recyclerViewItems.size();
    }

    private class HeaderHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePhoto;
        TextView namaProfile, emailProfile;
        RelativeLayout view_register, view_user;
        Button btn_daftar, btn_logout;
        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            profilePhoto = itemView.findViewById(R.id.profilephoto);
            namaProfile = itemView.findViewById(R.id.namaprofile);
            emailProfile = itemView.findViewById(R.id.emailprofile);
            view_register = itemView.findViewById(R.id.ll_unregistered);
            view_user = itemView.findViewById(R.id.ll_registered);
            btn_daftar = itemView.findViewById(R.id.btn_daftar);
            btn_daftar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    keLoginActivity();
                }
            });
            btn_logout = itemView.findViewById(R.id.btn_gantiuser);
            btn_logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    keLoginActivity();
                }
            });
        }
    }

    private class ChatHolder extends RecyclerView.ViewHolder {
        CircleImageView streaming_photo;
        TextView streaming_dari, streaming_jam, streaming_pesan;

        ChatHolder(@NonNull View itemView) {
            super(itemView);
            streaming_photo = itemView.findViewById(R.id.streaming_photo);
            streaming_dari = itemView.findViewById(R.id.streaming_dari);
            streaming_jam = itemView.findViewById(R.id.streaming_jam);
            streaming_pesan = itemView.findViewById(R.id.streaming_pesan);
        }
    }

    private String checkUserOnDB(){
        ArrayList<HashMap<String, String>> userDB = dbHandler.getUser(1);
        for (Map<String, String> map : userDB){
            Log.e(TAG, "checkUserOnDB: " + map.get("id_login"));
            ID_LOGIN = map.get("id_login");
        }
        return ID_LOGIN;
    }

    private void keLoginActivity(){
        dbHandler.deleteDB();
        LoginManager.getInstance().logOut();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        googleSignInClient = GoogleSignIn.getClient(context, gso);
        googleSignInClient.signOut();
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }
}
