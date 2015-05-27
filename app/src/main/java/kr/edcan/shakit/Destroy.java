package kr.edcan.shakit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

/**
 * Created by NTCS on 2015-04-01.
 */
public class Destroy extends Activity{

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref = getSharedPreferences("Setting", Context.MODE_PRIVATE);
        editor = pref.edit();
        if(pref.getBoolean("onSet",true) == false){
            editor.putBoolean("finish",true);
            editor.putBoolean("onSet",true);
            editor.commit();
        }
        ActivityCompat.finishAffinity(this);
        stopService(new Intent(this, ShakeService.class));
    }

    }
