package com.yarud.abuaziz.models;

import com.yarud.abuaziz.utils.RecyclerViewItem;

public class ModelChat extends RecyclerViewItem {
    private String id, pesan, waktu, jam, id_login, photo, pengirim;

    public ModelChat(String id, String pesan, String waktu, String jam, String id_login, String photo, String pengirim) {
        this.id = id;
        this.pesan = pesan;
        this.waktu = waktu;
        this.jam = jam;
        this.id_login = id_login;
        this.photo = photo;
        this.pengirim = pengirim;
    }

    public String getId() { return id; }

    public String getPesan() {
        return pesan;
    }

    public String getWaktu() {
        return waktu;
    }

    public String getJam() {
        return jam;
    }

    public String getId_login() {
        return id_login;
    }

    public String getPhoto() {
        return photo;
    }

    public String getPengirim() {
        return pengirim;
    }
}
