package dev.cadevi.seu.accesscontrol.controllers;

public class FirebaseUtils {
    private static final String BASE_URL = "https://rfidauth-1123a.firebaseio.com/";

    public static String getAllData() {
        return BASE_URL + "accesRfid.json";
    }

    public static String getAccessForUser(String userID) {
        return BASE_URL + "accesRfid/" + userID + ".json";
    }
}
