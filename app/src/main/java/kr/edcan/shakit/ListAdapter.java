package kr.edcan.shakit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by NTCS on 2015-03-29.
 */

public class ListAdapter extends ArrayAdapter<AppInfo> {
    // 레이아웃 XML을 읽어들이기 위한 객체
    private LayoutInflater mInflater;
    ArrayList<AppInfo> object;
    public ListAdapter(Context context, ArrayList<AppInfo> object) {

        // 상위 클래스의 초기화 과정
        // context, 0, 자료구조
        super(context, 0, object);
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.object = object;

    }

    // 보여지는 스타일을 자신이 만든 xml로 보이기 위한 구문
    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View view = null;

        // 현재 리스트의 하나의 항목에 보일 컨트롤 얻기

        if (v == null) {
            // XML 레이아웃을 직접 읽어서 리스트뷰에 넣음
            view = mInflater.inflate(R.layout.list_content_add, parent,false);
            view.setTag(position);
        } else {

            view = v;
        }

        // 자료를 받는다.
        final AppInfo data = this.object.get(position);
        if (data != null) {
            TextView appName = (TextView) view.findViewById(R.id.list_item_title);
            TextView packageName = (TextView) view.findViewById(R.id.list_item_subtitle);
            ImageView iconview = (ImageView) view.findViewById(R.id.list_item_icon);
            appName.setTextSize(18);
            packageName.setTextSize(8);
            appName.setText(data.getAppName());
            packageName.setText(data.getPackageName());
            iconview.setImageDrawable(data.getIcon());
        }
        return view;
    }
}
