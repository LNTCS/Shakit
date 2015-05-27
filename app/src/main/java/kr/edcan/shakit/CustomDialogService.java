package kr.edcan.shakit;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class CustomDialogService extends Dialog {
	Context mContext;
	MainActivity main;
    LinearLayout horizontalList,searchbar;
     ArrayList<CData> alist;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public CustomDialogService(Context context, ArrayList<CData> alist) {
		super(context);
		mContext = context;
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setGravity(Gravity.BOTTOM);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pref = context.getSharedPreferences("Setting", Context.MODE_PRIVATE);
        editor = pref.edit();
        if(pref.getString("theme","검정").equals("검정")){
            setContentView(R.layout.service_dialog_dark);
        }else if(pref.getString("theme","검정").equals("하양")){
            setContentView(R.layout.service_dialog_white);
        }else setContentView(R.layout.service_dialog_trans);
        LinearLayout bg = (LinearLayout) findViewById(R.id.dialog_bg);
        bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        horizontalList = (LinearLayout) findViewById(R.id.horizontalList);
        HorizontalScrollView hs = (HorizontalScrollView) findViewById(R.id.horizontal_scroll);
        hs.setFadingEdgeLength(200);
        searchbar = (LinearLayout) findViewById(R.id.dialog_searchbar);
        main = new MainActivity();
        if(pref.getString("search_bar","켜짐").equals("꺼짐")){
            searchbar.setVisibility(View.GONE);
        }else{
        }
        searchbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable d = null;
                try {
                    d =mContext.getPackageManager().getApplicationIcon("com.google.android.googlequicksearchbox");
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                if(d != null && mContext.getPackageManager().getLaunchIntentForPackage("com.google.android.googlequicksearchbox")!=null){
                    final Intent intent = new Intent();
                    final String action = Intent.ACTION_MAIN;
                    intent.setAction(action);
                    final String pkg = "com.google.android.googlequicksearchbox";
                    final String cls = "com.google.android.googlequicksearchbox.SearchActivity";
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setComponent(new ComponentName(pkg, cls));
                    mContext.startActivity(intent);
                    dismiss();
                }else{
                    Uri uri = Uri.parse("http://www.google.com");   // 1
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);  // 2
                    intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);  // 3
                    dismiss();
                }
            }
        });
        for(final CData cData : alist){
            Drawable d = cData.getIcon();
            ImageView img = new ImageView(mContext);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT);
            img.setLayoutParams(lp);
            img.setPadding(20,20,20,20);
            img.setAdjustViewBounds(true);
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            img.setImageDrawable(d);
            img.setClickable(true);
            img.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.on_selector));
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openApp(mContext,cData.getPackage());
                    dismiss();
                }
            });
            horizontalList.addView(img);
        }
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            dismiss();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
