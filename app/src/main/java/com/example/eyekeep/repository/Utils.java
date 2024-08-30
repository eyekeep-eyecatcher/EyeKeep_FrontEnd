package com.example.eyekeep.repository;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {
    private static final String PREFS = "prefs";
    private static final String Access_Token = "AccessToken";
    private static final String Refresh_Token = "RefreshToken";
    private Context mContext;
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor prefsEditor;
    private static Utils instance;

    public static synchronized Utils init(Context context) {    // 인스턴스 초기화
        if(instance == null) {
            instance = new Utils(context);
        }
        return instance;
    }

    private Utils(Context context) {        //SharedPreferences , editer 초기화
        mContext = context;
        prefs = mContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefsEditor = prefs.edit();
    }

    public static void setAccessToken(String value) {       //AccessToken 저장
        prefsEditor.putString(Access_Token, value).apply();
    }

    public static String getAccessToken(String defValue) {      //AccessTOken 변환  없으면 defValue 반환
        return prefs.getString(Access_Token,defValue);
    }

    public static void setRefreshToken(String value) {      //RefreshToken 저장
        prefsEditor.putString(Refresh_Token, value).apply();
    }

    public static String getRefreshToken(String defValue) {     //RefreshToken 변환  없으면 defValue 반환
        return prefs.getString(Refresh_Token,defValue);
    }

    public static void clearToken() {           //저장된 데이터 삭제
        prefsEditor.clear().apply();
    }

}
