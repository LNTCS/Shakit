package kr.edcan.shakit;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rey.material.widget.CheckBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by NTCS on 2015-04-01.
 */
public class Setting extends Activity implements AdapterView.OnItemClickListener{
    ListView listView;
    ArrayList<String> alist;
    SettingAdapter settingAdapter;
    Context mContext;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    int sens = 0;
    SeekBar slider;
    TextView seekbarval;
    boolean wrapInScrollView = true;
    TinyDB tinyDB;
    ArrayList<AppInfo> apps;
    ArrayList<String> blocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        mContext = this;
        apps = getPackages();
        Collections.sort(apps, new NameAscCompare());
        getActionBar().setDisplayHomeAsUpEnabled(true);
        listView = (ListView) findViewById(R.id.listview);
        tinyDB = new TinyDB(mContext);
        alist = new ArrayList<String>();
        for(int i = 0 ; i < 11 ; i++){
            alist.add("");
        }
        blocked = new ArrayList<String>();
        blocked = tinyDB.getList("blocked");
        settingAdapter = new SettingAdapter(mContext,alist);
        pref = getSharedPreferences("Setting", Context.MODE_PRIVATE);
        editor = pref.edit();
        listView.setAdapter(settingAdapter);
        listView.setOnItemClickListener(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    private ArrayList<AppInfo> getPackages() {
        // false = no system packages
        ArrayList<AppInfo> apps = getInstalledApps(false);

        final int max = apps.size();
        return apps;
    }


    private ArrayList<AppInfo> getInstalledApps(boolean getSysPackages) {
        ArrayList<AppInfo> res = new ArrayList<AppInfo>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
        for(int i=0;i<packs.size();i++) {
            PackageInfo p = packs.get(i);
            if ((!getSysPackages) && (p.versionName == null) || getPackageManager().getLaunchIntentForPackage(p.packageName)==null) {
                continue ;
            }
            AppInfo newInfo = new AppInfo();
            newInfo.setAppname(p.applicationInfo.loadLabel(getPackageManager()).toString());
            newInfo.setPname(p.packageName);
            newInfo.setVersionName(p.versionName);
            newInfo.setVersionCode(p.versionCode);
            newInfo.setIcon(p.applicationInfo.loadIcon(getPackageManager()));
            res.add(newInfo);
        }
        return res;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==40){
            settingAdapter.notifyDataSetChanged();
            stopService(new Intent(mContext, ShakeService.class));
            startService(new Intent(mContext, ShakeService.class));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                new MaterialDialog.Builder(this)
                        .title("방식 선택 (테스트 중)")
                        .titleColor(getResources().getColor(R.color.primary))
                        .items(new CharSequence[]{"슬라이더", "표준 수치","직접 흔들기"})
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                switch (which){
                                    case 0:
                                        MaterialDialog.Builder mDlg = new MaterialDialog.Builder(mContext)
                                                .title("흔들기 감도")
                                                .titleColor(getResources().getColor(R.color.primary))
                                                .customView(R.layout.seekbar_dialog, wrapInScrollView)
                                                .positiveText("완료")
                                                .callback(new MaterialDialog.ButtonCallback() {
                                                    @Override
                                                    public void onPositive(MaterialDialog dialog) {
                                                        super.onPositive(dialog);
                                                        editor.putInt("sens", sens * 100);
                                                        editor.commit();
                                                        settingAdapter.notifyDataSetChanged();
                                                        stopService(new Intent(mContext, ShakeService.class));
                                                        startService(new Intent(mContext, ShakeService.class));
                                                    }
                                                })
                                                .positiveColor(getResources().getColor(R.color.primary));
                                        MaterialDialog sliderDg = mDlg.build();
                                        slider = (SeekBar) sliderDg.getCustomView().findViewById(R.id.slider_sl_discrete);
                                        seekbarval = (TextView) sliderDg.getCustomView().findViewById(R.id.seekbarval);
                                        slider.setProgress(pref.getInt("sens",2000)/100-500);
                                        sens = slider.getProgress();
                                        seekbarval.setText(pref.getInt("sens",2000)+"");
                                        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                            @Override
                                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                progress += 5;
                                                sens = progress;
                                                seekbarval.setText(progress + "00");
                                            }
                                            @Override
                                            public void onStartTrackingTouch(SeekBar seekBar) {
                                            }
                                            @Override
                                            public void onStopTrackingTouch(SeekBar seekBar) {
                                            }
                                        });
                                        sliderDg.show();
                                        break;
                                    case 1:
                                        new MaterialDialog.Builder(mContext)
                                                .title("박스 테마")
                                                .titleColor(getResources().getColor(R.color.primary))
                                                .items(new CharSequence[]{"아주민감","민감", "보통","둔감","아주 둔감"})
                                                .itemsCallback(new MaterialDialog.ListCallback() {
                                                    @Override
                                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                        int sensV = 1000;
                                                        switch (which) {
                                                            case 0:
                                                                sensV = 1000;
                                                                break;
                                                            case 1:
                                                                sensV = 1800;
                                                                break;
                                                            case 2:
                                                                sensV = 2400;
                                                                break;
                                                            case 3:
                                                                sensV  = 2800;
                                                                break;
                                                            case 4:
                                                                sensV = 3300;
                                                                break;
                                                        }
                                                        editor.putInt("sens", sensV);
                                                        editor.commit();
                                                        settingAdapter.notifyDataSetChanged();
                                                        stopService(new Intent(mContext, ShakeService.class));
                                                        startService(new Intent(mContext, ShakeService.class));
                                                    }
                                                })
                                                .show();
                                        break;
                                    case 2:
                                        startActivityForResult(new Intent(mContext,Sensitive.class),40);
                                        break;
                                }
                            }
                        })
                        .show();
                break;
            case 1:
                new MaterialDialog.Builder(this)
                        .title("박스 테마")
                        .titleColor(getResources().getColor(R.color.primary))
                        .items(new CharSequence[]{"검정", "하양","투명"})
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                editor.putString("theme", text + "");
                                editor.commit();
                                settingAdapter.notifyDataSetChanged();
                                stopService(new Intent(mContext, ShakeService.class));
                                startService(new Intent(mContext, ShakeService.class));
                            }
                        })
                        .show();
                break;
            case 2:
                if(pref.getString("search_bar","켜짐").equals("켜짐")){
                    editor.putString("search_bar","꺼짐");
                    editor.commit();
                }else {
                    editor.putString("search_bar", "켜짐");
                    editor.commit();
                }
                settingAdapter.notifyDataSetChanged();
                Log.e("ㅁㄴㅇ", pref.getString("search_bar", "켜짐"));
                stopService(new Intent(mContext, ShakeService.class));
                startService(new Intent(mContext , ShakeService.class));
                break;
            case 3:
                if(pref.getBoolean("notify",true)){
                    editor.putBoolean("notify",false);
                }else {
                    editor.putBoolean("notify", true);
                }
                editor.commit();
                settingAdapter.notifyDataSetChanged();
                stopService(new Intent(mContext, ShakeService.class));
                startService(new Intent(mContext, ShakeService.class));
                break;
            case 4:
                if(pref.getBoolean("auto",true)){
                    editor.putBoolean("auto",false);
                }else {
                    editor.putBoolean("auto", true);
                }
                editor.commit();
                settingAdapter.notifyDataSetChanged();
                stopService(new Intent(mContext, ShakeService.class));
                startService(new Intent(mContext, ShakeService.class));
                break;
            case 5:
                if(pref.getBoolean("landscape",true)){
                    editor.putBoolean("landscape",false);
                }else {
                    editor.putBoolean("landscape", true);
                }
                editor.commit();
                settingAdapter.notifyDataSetChanged();
                stopService(new Intent(mContext, ShakeService.class));
                startService(new Intent(mContext, ShakeService.class));
                break;
            case 6:
                if(pref.getBoolean("vibe",true)){
                    editor.putBoolean("vibe",false);
                }else {
                    editor.putBoolean("vibe", true);
                }
                editor.commit();
                settingAdapter.notifyDataSetChanged();
                stopService(new Intent(mContext, ShakeService.class));
                startService(new Intent(mContext, ShakeService.class));
                break;
            case 7:
                if(pref.getBoolean("only",true)){
                    editor.putBoolean("only",false);
                }else {
                    editor.putBoolean("only", true);
                }
                editor.commit();
                settingAdapter.notifyDataSetChanged();
                stopService(new Intent(mContext, ShakeService.class));
                startService(new Intent(mContext, ShakeService.class));
                break;
            case 8:
                new LoadSync().execute();
                break;
            case 9:
                editor.putBoolean("finish",true);
                editor.putBoolean("onSet",true);
                editor.commit();
                stopService(new Intent(mContext , ShakeService.class));
                ActivityCompat.finishAffinity(Setting.this);
                break;
            case 10:
                editor.putBoolean("egg",true);
                editor.commit();
                settingAdapter.notifyDataSetChanged();
                break;
        }
    }

    class LoadSync extends AsyncTask<String,String,String>{
        MaterialDialog loaded,materialDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loaded = new MaterialDialog.Builder(mContext)
                    .title("어플 목록 로드중")
                    .content("잠시만 기다려주세요\n1분안에 끝날예정...")
                    .titleColor(getResources().getColor(R.color.primary)).build();
            loaded.show();

            final MaterialDialog.Builder blocks = new MaterialDialog.Builder(mContext)
                    .title("제한어플 설정")
                    .titleColor(getResources().getColor(R.color.primary))
                    .customView(R.layout.appinfo_list, wrapInScrollView)
                    .positiveText("완료")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            tinyDB.putList("blocked",blocked);
                        }
                    })
                    .positiveColor(getResources().getColor(R.color.primary));
            materialDialog = blocks.build();
        }

        @Override
        protected String doInBackground(String... params) {
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            View customView = materialDialog.getCustomView();
            final LinearLayout scLay = (LinearLayout)customView.findViewById(R.id.appinfo_scroll_lay);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            for( int i = 0 ; i <apps.size() ; i++){
                View adding  = inflater.inflate(R.layout.list_content, null, false);
                ImageView icon = (ImageView) adding.findViewById(R.id.list_item_icon);
                TextView title = (TextView) adding.findViewById(R.id.list_item_title);
                final TextView subtitle = (TextView) adding.findViewById(R.id.list_item_subtitle);
                final int cnt  = i;
                title.setText(apps.get(i).getAppName());
                icon.setImageDrawable(apps.get(i).getIcon());
                subtitle.setText(apps.get(i).getPackageName());
                final CheckBox checkBox = (CheckBox) adding.findViewById(R.id.checkbox_app);
                if(blocked.contains(subtitle.getText().toString())){
                    checkBox.setChecked(true);
                }
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (buttonView.getId() == R.id.checkbox_app) {
                            if (isChecked) {
                                blocked.add(subtitle.getText().toString());
                                Log.e("", "" + subtitle.getText().toString());
                                return;
                            } else {
                                blocked.remove(subtitle.getText().toString());
                                Log.e("", "" + subtitle.getText().toString());
                                return;
                            }
                        }
                    }
                });
                scLay.addView(adding);
            }
            materialDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    loaded.dismiss();
                }
            });
            //sdas
            materialDialog.show();
        }
    }

    static class NameAscCompare implements Comparator<AppInfo> {
        @Override
        public int compare(AppInfo arg0, AppInfo arg1) {
            // TODO Auto-generated method stub
            return arg0.getAppName().compareTo(arg1.getAppName());
        }
    }
}
