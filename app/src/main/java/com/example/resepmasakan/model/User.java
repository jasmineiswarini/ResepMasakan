package com.example.resepmasakan.model;

public class User {
    private String id, judul, detail, gambar;

    public User(String judul){

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User(String judul, String detail, String gambar) {
        this.judul = judul;
        this.detail = detail;
        this.gambar =gambar;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getGambar() {
        return gambar;
    }

    public void setGambar(String gambar) {
        this.gambar = gambar;
    }
}
