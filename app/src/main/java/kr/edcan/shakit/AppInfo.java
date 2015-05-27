package kr.edcan.shakit;

import android.graphics.drawable.Drawable;

/**
 * Created by NTCS on 2015-03-30.
 */
public class AppInfo {
    private String appname = "";
    private String pname = "";
    private String versionName = "";
    private int versionCode = 0;
    private Drawable icon;

    public String getPackageName(){
        return pname;
    }
    public String getAppName(){
        return appname;
    }
    public Drawable getIcon(){
        return icon;
    }
    public void setAppname(String s){
        appname = s;
    }
    public void setPname(String s){
        pname = s;
    }
    public void setVersionName(String s){
        versionName = s;
    }
    public void setVersionCode(int s){
        versionCode = s;
    }
    public void setIcon(Drawable d){
        icon = d;
    }
}
