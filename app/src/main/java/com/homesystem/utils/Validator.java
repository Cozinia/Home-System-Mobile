package com.homesystem.utils;

public class Validator {

    public static boolean checkNullOrEmpty(String field){
        return field == null || field.trim().isEmpty();
    }


    public static boolean checkRegexEmail(String email){
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return email.matches(emailPattern);
    }

    public static boolean checkRegexPassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        return password.matches(passwordPattern);
    }



}
