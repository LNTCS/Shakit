package kr.edcan.shakit;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;

/**
 * Created by NTCS on 2015-04-10.
 */
public class Sensitive extends Activity {
    TextView seneitive_tv;
    Context mContext;
    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;

    int Max = 0;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;
    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;

    ButtonFlat buttonFlat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sensitive);
        mContext = this;
        pref = getSharedPreferences("Setting", Context.MODE_PRIVATE);
        editor = pref.edit();
        buttonFlat = (ButtonFlat) findViewById(R.id.sensitive_dismiss);
        buttonFlat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Max < 500){
                    Toast.makeText(mContext,"민감도는 최소 500 입니다.",Toast.LENGTH_SHORT).show();
                    return;
                }
                editor.putInt("sens",Max);
                editor.commit();
                finish();
            }
        });

        seneitive_tv = (TextView)findViewById(R.id.seneitive_tv);
        sensorManager= (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (accelerormeterSensor != null)sensorManager.registerListener(listener, accelerormeterSensor,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

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
                    Max = (Max<speed)? (int) speed : Max;
                    Max = (Max/100)*100;
                    seneitive_tv.setText(Max + "");

                }
                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];
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
    }
}
