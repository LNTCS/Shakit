package kr.edcan.shakit;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * Created by NTCS on 2015-04-11.
 */
public class AboutSlide extends Activity {
    TextView ok;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.about_slide);
        pref = getSharedPreferences("Setting", Context.MODE_PRIVATE);
        editor = pref.edit();
        editor.putBoolean("about",false);
        editor.commit();

        ok = (TextView) findViewById(R.id.about_dismiss);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
