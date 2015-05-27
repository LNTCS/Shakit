package kr.edcan.shakit;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.view.WindowManager;

import java.util.ArrayList;

/**
 * Created by NTCS on 2015-04-01.
 */
public class ShakeService extends Service {
    ArrayList<CData> alist;
    TinyDB tinyDB;
    NotificationManager manager;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    Context mContext;
    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;

    private static int SHAKE_THRESHOLD;
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;
    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;
    CustomDialogService dialogService;
    boolean isOpened = false;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        pref = getSharedPreferences("Setting", Context.MODE_PRIVATE);
        editor = pref.edit();
        if (pref.getBoolean("finish", false)) {
            editor.putBoolean("finish", false);
            editor.putBoolean("finish2", true);
            editor.commit();
            Intent i = new Intent(mContext, Destroy.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FILL_IN_ACTION);
            startActivity(i);
        }else if(pref.getBoolean("finish2",false)){
            editor.putBoolean("finish2", false);
            editor.commit();
            Intent i = new Intent(mContext, Destroy.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FILL_IN_ACTION);
            startActivity(i);
        }
        tinyDB = new TinyDB(mContext);
        SHAKE_THRESHOLD = pref.getInt("sens",2000);
        if(SHAKE_THRESHOLD < 500){
            SHAKE_THRESHOLD=500;
        }
        sensorManager= (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (accelerormeterSensor != null)sensorManager.registerListener(listener, accelerormeterSensor,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        alist = new ArrayList<CData>();
        for(int i = 0 ; i < tinyDB.getInt("SavedAppCnt");i++){
            Drawable d = null;
            String s = tinyDB.getList("SavedPackage").get(i);
            try {
                d = getPackageManager().getApplicationIcon(s);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if(d==null){
                alist.add(new CData(mContext, "삭제된 어플", "삭제된 어플", getResources().getDrawable(R.drawable.icon)));
            }else{
                alist.add(new CData(mContext, tinyDB.getList("SavedName").get(i), s, d));
            }
        }
        dialogService = new CustomDialogService(mContext, alist);
        dialogService.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        Intent svc = new Intent(this, ShakeService.class);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mb = new NotificationCompat.Builder(getBaseContext());
        mb.setSmallIcon(R.drawable.notiicon_working);
        mb.setContentTitle("Shakit!");
        mb.setContentText("흔들어!");
        mb.setPriority(NotificationCompat.PRIORITY_LOW);
        mb.setOngoing(true);
        Intent myIntent = new Intent(this, Destroy.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,   myIntent, Intent.FILL_IN_ACTION);
        mb.addAction(R.drawable.delete, "종료", pendingIntent);
        Notification notification = mb.build();
        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(mContext, 0,
                notificationIntent, 0);
        notification.setLatestEventInfo(mContext, "Shakit", "현재 실행중 입니다.", intent);
        if(pref.getBoolean("notify",true)){
            manager.notify(99, notification);
        }
    }
    private SensorEventListener listener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long currentTime = System.currentTimeMillis();
                long gabOfTime = (currentTime - lastTime);
                if (gabOfTime > 100) {
                    lastTime = currentTime;
                    x = event.values[SensorManager.DATA_X];
                    y = event.values[SensorManager.DATA_Y];
                    z = event.values[SensorManager.DATA_Z];

                    speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;

                    if (speed > SHAKE_THRESHOLD && !isOpened) {
                        ActivityManager mActivityManager =(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                        String mPackageName;
                        if(Build.VERSION.SDK_INT > 20){
                            mPackageName   = mActivityManager.getRunningAppProcesses().get(0).processName;
                        }
                        else{
                            mPackageName = mActivityManager.getRunningTasks(1).get(0).baseActivity.getPackageName();
                        }
                        if(tinyDB.getList("blocked").contains(mPackageName)){
                            return;
                        }
                        if(pref.getBoolean("landscape",true) && getResources().getConfiguration().orientation==2){
                            return;
                        }
                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        boolean isScreenOn = pm.isScreenOn();
                        if(!isScreenOn){
                            return;
                        }
                        if(alist.size()==1){
                            if(mPackageName.equals(alist.get(0).getPackage())){
                            }else{
                                if(pref.getBoolean("vibe",true)){
                                    Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                    vibe.vibrate(30);
                                }
                                openApp(mContext,alist.get(0).getPackage());
                            }
                            return;
                        }

                        if(pref.getBoolean("vibe",true)){
                            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibe.vibrate(30);
                        }
                        isOpened = true;
                        dialogService.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                isOpened = false;
                            }
                        });
                        dialogService.setCancelable(true);
                        dialogService.setCanceledOnTouchOutside(true);
                        dialogService.show();
                    }
                    lastX = event.values[DATA_X];
                    lastY = event.values[DATA_Y];
                    lastZ = event.values[DATA_Z];
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null)
            sensorManager.unregisterListener(listener);
        manager.cancelAll();
    }
    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i == null) {
            return false;
            //throw new PackageManager.NameNotFoundException();
        }
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(i);
        return true;
    }
}
