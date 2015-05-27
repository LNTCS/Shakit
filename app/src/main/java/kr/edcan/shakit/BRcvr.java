package kr.edcan.shakit;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by NTCS on 2015-04-05.
 */
public class BRcvr extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.i("BOOTSVC", "Intent received");
            SharedPreferences pref;
            pref = context.getSharedPreferences("Setting", Context.MODE_PRIVATE);
            if(pref.getBoolean("auto",true)) {
                ComponentName cn = new ComponentName(context.getPackageName(), ShakeService.class.getName());
                ComponentName svcName = context.startService(new Intent().setComponent(cn));
                if (svcName == null) {
                    Log.e("BOOTSVC", "Could not start service " + cn.toString());
                }
            }
        }
    }
}
