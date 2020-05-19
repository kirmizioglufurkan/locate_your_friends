package com.furkan.locateyourfriends;

public class User {

    public String username;
    public String email;
    public String password;
    public String imageUrl;
    public String name;
    public String surname;
    public String phoneNumber;
    public String code;
    public boolean isSharing;
    public double lat;
    public double lng;

    public User() {
    }

    public User(String username, String email, String password, String name, String surname, String phoneNumber, String code, boolean isSharing, double latitude, double longitude, String imageUrl) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.code = code;
        this.isSharing = isSharing;
        this.lat = latitude;
        this.lng = longitude;
        this.imageUrl = imageUrl;
    }

}
