package com.furkan.locateyourfriends;

public class User {

    public String username, email, password, imageUrl, name, surname, phoneNumber, code;
    public boolean isSharing;
    public double lat;
    public double lng;

    public User() {
    }

    public User(String username, String email, String password, String name, String surname, String phoneNumber, String code, boolean isSharing, double lat, double lng, String imageUrl) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.code = code;
        this.isSharing = isSharing;
        this.lat = lat;
        this.lng = lat;
        this.imageUrl = imageUrl;
    }

}
