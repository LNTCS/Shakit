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

public class DataAdapter extends ArrayAdapter<CData> {
    // 레이아웃 XML을 읽어들이기 위한 객체
    private LayoutInflater mInflater;

    public DataAdapter(Context context, ArrayList<CData> object) {

        // 상위 클래스의 초기화 과정
        // context, 0, 자료구조
        super(context, 0, object);
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    // 보여지는 스타일을 자신이 만든 xml로 보이기 위한 구문
    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        View view = null;

        // 현재 리스트의 하나의 항목에 보일 컨트롤 얻기
        if (v == null) {
            // XML 레이아웃을 직접 읽어서 리스트뷰에 넣음
            view = mInflater.inflate(R.layout.list_setting_content, parent,false);
            view.setTag(position);
        } else {

            view = v;
        }

        // 자료를 받는다.
        final CData data = this.getItem(position);
        if (data != null) {
            ImageView icon = (ImageView) view.findViewById(R.id.list_item_icon);
            TextView title = (TextView) view.findViewById(R.id.list_item_title);
            TextView subtitle = (TextView) view.findViewById(R.id.list_item_subtitle);
            title.setText(data.getLabel());
            icon.setImageDrawable(data.getIcon());
            subtitle.setText(data.getPackage());
        }
        return view;
    }
}
