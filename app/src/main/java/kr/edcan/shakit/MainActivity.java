package kr.edcan.shakit;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends Activity implements AbsListView.OnScrollListener ,DndListView.DropListener{

    Context mContext;
    // The height of your fully expanded header view (same than in the xml layout)
    int headerHeight;
    // The height of your fully collapsed header view. Actually the Toolbar height (56dp)
    int minHeaderHeight;
    // The left margin of the Toolbar title (according to specs, 72dp)
    int toolbarTitleLeftMargin;
    // Added after edit
    int minHeaderTranslation;
    private DndListView listView;

    static boolean isFoot = true;
    // Header views
    private View headerView;
    private RelativeLayout headerContainer;
    LinearLayout actionTitle;
    TextView header_sub,header_sub_ori;
    LayoutInflater inflater;
    static DataAdapter adapter;
    static ArrayList<CData> alist;
    FloatingActionButton listSet;
    ImageView btn_option;
    ArrayList<String> Name,Package;

    RelativeLayout empty ;
    TinyDB tinyDB;
    View footerView;
    ArrayList<AppInfo> apps;
    ArrayList<String> appstr;
    ArrayList<AppInfo> tmpapps;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    static ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        apps = getPackages();
        Name = new ArrayList<String>();
        pref = getSharedPreferences("Setting", Context.MODE_PRIVATE);
        editor = pref.edit();
        Package = new ArrayList<String>();
        tinyDB = new TinyDB(mContext);
        listView = (DndListView) findViewById(R.id.listview);
        btn_option = (ImageView) findViewById(R.id.btn_option);
        actionTitle = (LinearLayout) findViewById(R.id.action_title);
        listSet = (FloatingActionButton) findViewById(R.id.action_plus);
        empty = (RelativeLayout) findViewById(R.id.empty);
        btn_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, Setting.class));
            }
        });
        listSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tmpapps= new ArrayList<AppInfo>();
                appstr = new ArrayList<String>();
                ListCopy();
                ArrayList<Integer> del = new ArrayList<Integer>();
                for(AppInfo ai : apps){
                    appstr.add(ai.getPackageName());
                    tmpapps.add(ai);
                }
                Log.e("",tmpapps.size() + "");
                for (String s : Package){
                    if(appstr.contains(s)){
                        del.add(appstr.indexOf(s));
                    }
                }
                for(int i : del){
                    tmpapps.remove(apps.get(i));
                }
                Collections.sort(tmpapps, new NameAscCompare());

                View listif = inflater.inflate(R.layout.list_dialog, null, false);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                alertDialogBuilder.setView(listif);
                final AlertDialog listDia = alertDialogBuilder.create();
                ListView listLay = (ListView)listif.findViewById(R.id.dialog_list);
                ListAdapter listAdapter = new ListAdapter(mContext,tmpapps);
                listLay.setAdapter(listAdapter);
                listLay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AppInfo appInfo = tmpapps.get(position);
                        adapter.add(new CData(mContext, appInfo.getAppName(), appInfo.getPackageName(), appInfo.getIcon()));
                        adapter.notifyDataSetChanged();
                        //TODO 리스트 추가
                        ListCopy();
                        ListPut();
                        UpdateMsg();
                        listDia.dismiss();
                        if(pref.getBoolean("about",true)){
                            startActivity(new Intent(mContext, AboutSlide.class));
                        }
                    }
                });
                listDia.show();
            }
        });
        inflater = getLayoutInflater();
        alist = new ArrayList<CData>();
        adapter = new DataAdapter(this, alist);
        headerHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        minHeaderHeight = getResources().getDimensionPixelSize(R.dimen.action_bar_height);
        toolbarTitleLeftMargin =getResources().getDimensionPixelSize(R.dimen.toolbar_left_margin);
        headerHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        minHeaderTranslation = -headerHeight + getResources().getDimensionPixelOffset(R.dimen.headerview);
        headerView = inflater.inflate(R.layout.header_view, listView, false);
        footerView= inflater.inflate(R.layout.list_footer, listView, false);
        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        headerContainer = (RelativeLayout) headerView.findViewById(R.id.header_container);
        listView.addHeaderView(headerView, null, false);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);
        listView.setDropListener(this);
        header_sub = (TextView) findViewById(R.id.header_subtitle_ori);
        header_sub_ori = (TextView) headerView.findViewById(R.id.header_subtitle);
        header_sub_ori.setText("바로가기가 설정된 앱 "+alist.size()+"개");
        header_sub.setText("바로가기가 설정된 앱 " + alist.size() + "개");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(isFoot){
                    return;
                }
                final CData data = adapter.getItem(position - 1);
                dia(data.getLabel() + "\n정말로 삭제 하시겠습니까?", data);
            }
        });
        listView.setEmptyView(empty);
//TODO 초기세팅
        FirstSet();
        editor.putBoolean("finish", false);
        editor.putBoolean("finish2",false);
        editor.putBoolean("onSet",false);
        editor.commit();
        Intent Service = new Intent(this, ShakeService.class);
        startService(Service);
    }
    public void UpdateMsg(){
        header_sub_ori.setText("바로가기가 설정된 앱 " + alist.size() + "개");
        header_sub.setText("바로가기가 설정된 앱 " + alist.size() + "개");
        stopService(new Intent(mContext, ShakeService.class));
        startService(new Intent(mContext, ShakeService.class));
        if(alist.size() > 0){
            listView.removeFooterView(footerView);
            isFoot = false;
        }else {
            listView.addFooterView(footerView);
            isFoot = true;
        }
    }
    static class NameAscCompare implements Comparator<AppInfo> {
        @Override
        public int compare(AppInfo arg0, AppInfo arg1) {
            // TODO Auto-generated method stubu
            return arg0.getAppName().compareTo(arg1.getAppName());
        }
    }

    public void ListCopy(){
        Name.clear();
        Package.clear();
        for(int i=0; i < alist.size() ; i++){
            Name.add(alist.get(i).getLabel());
            Package.add(alist.get(i).getPackage());
        }
    }
    public void ListPut(){
        tinyDB.putList("SavedName",Name);
        tinyDB.putList("SavedPackage",Package);
        tinyDB.putInt("SavedAppCnt", Name.size());
    }
    public void FirstSet(){
        for(int i = 0 ; i < tinyDB.getInt("SavedAppCnt");i++){
            Drawable d = null;
            String s = tinyDB.getList("SavedPackage").get(i);
            try {
                d = getPackageManager().getApplicationIcon(s);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if(d==null){
//                adapter.add(new CData(mContext,"삭제된 어플","삭제된 어플",getResources().getDrawable(R.drawable.icon)));
            }else{
                adapter.add(new CData(mContext,tinyDB.getList("SavedName").get(i),s,d));
            }
        }
        UpdateMsg();
    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState){
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(pref.getBoolean("finish",false) || pref.getBoolean("finish2",false)){

        }else {
            startService(new Intent(mContext, ShakeService.class));
            bindService(new Intent(mContext, ShakeService.class), mConnection, 99);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        Integer scrollY = getScrollY(view);
        headerView.setTranslationY(Math.max(0, scrollY + minHeaderTranslation));
        float offset = 1 - Math.max(
                (float) (-minHeaderTranslation - scrollY) / -minHeaderTranslation, 0f);
        if(offset == 1){
            actionTitle.setVisibility(View.VISIBLE);
            headerView.setVisibility(View.GONE);
        }else{
            actionTitle.setVisibility(View.GONE);
            headerView.setVisibility(View.VISIBLE);
        }
    }

    public int getScrollY(AbsListView view)
    {
        View c = view.getChildAt(0);

        if (c == null)
            return 0;

        int firstVisiblePosition = view.getFirstVisiblePosition();
        int top = c.getTop();

        int headerHeight = 0;
        if (firstVisiblePosition >= 1)
            headerHeight = this.headerHeight;

        return -top + firstVisiblePosition * c.getHeight() + headerHeight;
    }
    public void dia(String text, CData Cdata) {
        final CData cdata = Cdata;
        AlertDialogWrapper.Builder alert = new AlertDialogWrapper.Builder(MainActivity.this);
        alert.setPositiveButton("네", new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // 닫기
                adapter.remove(cdata);
                //TODO 삭제
                ListCopy();
                ListPut();
                UpdateMsg();
            }
        });
        alert.setNegativeButton("아니요", new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // 닫기
            }
        });
        alert.setMessage(text);
        alert.show();
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

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    @Override
    public void drop(int from, int to) {
        if(to == 0)return;
        int before = from-1;
        int after = to-1;
        int Cnt = alist.size();
        CData c = alist.get(before);
        ArrayList<CData> tmpcds1 = new ArrayList<CData>();
        ArrayList<CData> tmpcdsA = new ArrayList<CData>();
        ArrayList<CData> tmpcdsB = new ArrayList<CData>();
        ArrayList<CData> tmpcdsC = new ArrayList<CData>();
        for (int i = 0 ; i < alist.size() ; i++){
            tmpcds1.add(alist.get(i));
        }
        if(before < after){
            adapter.clear();
            for (int i = 0 ; i < before; i++){
                tmpcdsA.add(tmpcds1.get(i));
                Log.i("",tmpcds1.get(i).getLabel() + "");
            }
            for (int i = before+1 ; i < after; i++){
                tmpcdsB.add(tmpcds1.get(i));
                Log.i("",tmpcds1.get(i).getLabel() + "");
            }
            for (int i = after ; i < Cnt; i++){
                tmpcdsC.add(tmpcds1.get(i));
                Log.i("",tmpcds1.get(i).getLabel() + "");
            }
            for (int i = 0 ; i < tmpcdsA.size() ; i++){
                adapter.add(tmpcdsA.get(i));
            }
            for (int i = 0 ; i < tmpcdsB.size() ; i++){
                adapter.add(tmpcdsB.get(i));
            }
            for (int i = 0 ; i < tmpcdsC.size() ; i++){
                adapter.add(tmpcdsC.get(i));
                if(i == 0 ){
                    adapter.add(c);
                }

            }
            adapter.notifyDataSetChanged();
        }else if(before > after){
            adapter.clear();
            for (int i = 0 ; i < after; i++){
                tmpcdsA.add(tmpcds1.get(i));
                Log.i("",tmpcds1.get(i).getLabel() + "");
            }
            for (int i = after ; i < before; i++){
                tmpcdsB.add(tmpcds1.get(i));
                Log.i("",tmpcds1.get(i).getLabel() + "");
            }
            for (int i = before+1 ; i < Cnt; i++){
                tmpcdsC.add(tmpcds1.get(i));
                Log.i("",tmpcds1.get(i).getLabel() + "");
            }
            for (int i = 0 ; i < tmpcdsA.size() ; i++){
                adapter.add(tmpcdsA.get(i));
            }
            adapter.add(c);
            for (int i = 0 ; i < tmpcdsB.size() ; i++){
                adapter.add(tmpcdsB.get(i));
            }
            for (int i = 0 ; i < tmpcdsC.size() ; i++){
                adapter.add(tmpcdsC.get(i));
            }
            adapter.notifyDataSetChanged();
        }
        ListCopy();
        ListPut();
        UpdateMsg();
        listView.setAdapter(adapter);
        listView.setSelection(after);
    }
}