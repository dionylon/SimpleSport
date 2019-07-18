package com.dionys.mymap.entity;

import android.content.Context;
import android.content.SharedPreferences;

public class User {

    public static final String INFO_NAME="user_info";
    public static final String USER_NAME="user_name";
    public static final String PASSWORD="password";
    public static final String WEIGHT="weight";
    public static final String HEIGHT="height";

    private String userName;
    private String password;
    private float weight;
    private float height;

    public void save(Context context){

        SharedPreferences sharedPreferences = context.getSharedPreferences(INFO_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_NAME,userName);
        editor.putString(PASSWORD,password);
        editor.putFloat(WEIGHT, weight);
        editor.putFloat(HEIGHT, height);
        editor.apply();

    }
    public static User getSavedUser(Context context){
        User user = new User();

        SharedPreferences sp = context.getSharedPreferences(INFO_NAME,Context.MODE_PRIVATE);
        user.setUserName(sp.getString(USER_NAME,"未命名"));
        user.setPassword(sp.getString(PASSWORD,"1234"));
        user.setWeight(sp.getFloat(WEIGHT,60));
        user.setHeight(sp.getFloat(HEIGHT,1.70f));

        return user;

    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

}
