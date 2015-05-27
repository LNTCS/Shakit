package kr.edcan.shakit;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by NTCS on 2015-03-29.
 */

public class SettingAdapter extends ArrayAdapter<String> {
    // 레이아웃 XML을 읽어들이기 위한 객체
    private LayoutInflater mInflater;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public SettingAdapter(Context context, ArrayList<String> object) {
        // 상위 클래스의 초기화 과정
        // context, 0, 자료구조
        super(context, 0, object);
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pref = context.getSharedPreferences("Setting", Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // 보여지는 스타일을 자신이 만든 xml로 보이기 위한 구문
    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View view = null;
        // 현재 리스트의 하나의 항목에 보일 컨트롤 얻기
        if (v == null) {
        } else {
            view = v;
        }

        // 자료를 받는다.
        final String data = this.getItem(position);
        if (data != null) {
            view = mInflater.inflate(R.layout.list_content_setting, parent,false);
            final TextView setting_name = (TextView) view.findViewById(R.id.setting_name);
            final TextView setting_option = (TextView) view.findViewById(R.id.setting_option);
            final ImageView setting_img = (ImageView) view.findViewById(R.id.setting_img);
            switch (position){
                case 0:
                    setting_name.setText("민감도 설정");
                    setting_option.setText(pref.getInt("sens", 2000) + "");
                    break;
                case 1:
                    setting_name.setText("박스 테마");
                    setting_option.setText(pref.getString("theme","검정"));
                    break;
                case 2:
                    setting_name.setText("검색창");
                    if(pref.getString("search_bar","켜짐").equals("켜짐")){
                        setting_img.setImageResource(R.drawable.tgl_settings_on);
                    }else{
                        setting_img.setImageResource(R.drawable.tgl_settings_off);
                    }
                    break;
                case 3:
                    setting_name.setText("상단바 알림");
                    if(pref.getBoolean("notify",true)){
                        setting_img.setImageResource(R.drawable.tgl_settings_on);
                    }else{
                        setting_img.setImageResource(R.drawable.tgl_settings_off);
                    }
                    break;
                case 4:
                    setting_name.setText("부팅시 자동 실행");
                    if(pref.getBoolean("auto",true)){
                        setting_img.setImageResource(R.drawable.tgl_settings_on);
                    }else{
                        setting_img.setImageResource(R.drawable.tgl_settings_off);
                    }
                    break;
                case 5:
                    setting_name.setText("가로모드에서 사용안함");
                    if(pref.getBoolean("landscape",true)){
                        setting_img.setImageResource(R.drawable.tgl_settings_on);
                    }else{
                        setting_img.setImageResource(R.drawable.tgl_settings_off);
                    }
                    break;
                case 6:
                    setting_name.setText("흔들었을 때 진동");
                    if(pref.getBoolean("vibe",true)){
                        setting_img.setImageResource(R.drawable.tgl_settings_on);
                    }else{
                        setting_img.setImageResource(R.drawable.tgl_settings_off);
                    }
                    break;
                case 7:
                    setting_name.setText("앱 하나 설정시 바로 실행");
                    if(pref.getBoolean("only",true)){
                        setting_img.setImageResource(R.drawable.tgl_settings_on);
                    }else{
                        setting_img.setImageResource(R.drawable.tgl_settings_off);
                    }
                    break;
                case 8:
                    setting_name.setText("팝업제한 어플 설정");
                    break;
                case 9:
                    setting_name.setText("앱 종료");
                    break;
                case 10:
                    view = mInflater.inflate(R.layout.list_about, null);
                    if(pref.getBoolean("egg",false)) {
                        ImageView img = (ImageView) view.findViewById(R.id.about);
                        RotateAnimation rotateAnimation1 = new RotateAnimation(0, 360,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);
                        rotateAnimation1.setInterpolator(new LinearInterpolator());
                        rotateAnimation1.setDuration(1000);
                        rotateAnimation1.setRepeatCount(0);
                        img.startAnimation(rotateAnimation1);
                        editor.putBoolean("egg",false);
                        editor.commit();
                    }
                    break;
            }
        }
        return view;
    }
}
