package com.yarud.abuaziz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yarud.abuaziz.R;
import com.yarud.abuaziz.utils.RecyclerViewItem;
import com.yarud.abuaziz.models.ModelChat;
import com.yarud.abuaziz.models.ModelHeader;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterChat extends RecyclerView.Adapter {
    private List<RecyclerViewItem> recyclerViewItems;
    private static final int HEADER_ITEM = 0;
    private static final int CHAT_ITEM = 1;
    private Context context;

    public AdapterChat(List<RecyclerViewItem> recyclerViewItems, Context context){
        this.recyclerViewItems = recyclerViewItems;
        this.context = context;
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
            ModelHeader modelHeader = (ModelHeader) recyclerViewItem;
            if (modelHeader.getIMAGEURL() != null){
                Glide.with(context).load(modelHeader.getIMAGEURL()).placeholder(R.drawable.ic_account).into(headerHolder.profilePhoto);
            }
            headerHolder.namaProfile.setText(modelHeader.getEMAILPROFILE());
            headerHolder.emailProfile.setText(modelHeader.getEMAILPROFILE());
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
        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            profilePhoto = itemView.findViewById(R.id.profilephoto);
            namaProfile = itemView.findViewById(R.id.namaprofile);
            emailProfile = itemView.findViewById(R.id.emailprofile);
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
}
