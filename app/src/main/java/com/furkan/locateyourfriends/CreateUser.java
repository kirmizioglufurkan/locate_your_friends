package com.furkan.locateyourfriends;

public class CreateUser {

    public String username, email, password, name, surname, phoneNumber, code, imageUrl;
    public boolean isSharing;
    public double lat;
    public double lng;

    public void CreateUser() {}

    public CreateUser(String username, String email, String password, String name, String surname, String phoneNumber, String code, boolean isSharing, double lat, double lng, String imageUrl) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.code = code;
        this.isSharing = isSharing;
        this.lat = lat;
        this.lng = lng;
        this.imageUrl = imageUrl;
    }
}