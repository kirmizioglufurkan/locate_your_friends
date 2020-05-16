package com.furkan.locateyourfriends;

public class CreateUser {

    public String username,email,password,name,surname,phoneNumber,code,isSharing,lat,lng,imageUrl;

    public void CreateUser() {}

    public CreateUser(String username, String email, String password, String name, String surname, String phoneNumber, String code, String isSharing, String lat, String lng, String imageUrl) {
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