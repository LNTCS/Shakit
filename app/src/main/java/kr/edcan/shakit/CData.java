package kr.edcan.shakit;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Created by NTCS on 2015-03-29.
 */
public class CData {

    private String m_szLabel;
    private String m_szPackage;
    private Drawable m_szDrawable;

    public CData(Context context, String p_szLabel, String p_szDataFile , Drawable p_szDrawable) {
        m_szLabel = p_szLabel;
        m_szPackage = p_szDataFile;
        m_szDrawable = p_szDrawable;
    }

    public String getLabel() {
        return m_szLabel;
    }

    public String getPackage() {
        return m_szPackage;
    }
    public Drawable getIcon() {
        return m_szDrawable;
    }
}
