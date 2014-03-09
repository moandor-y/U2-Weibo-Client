package gov.moandor.androidweibo.adapter;

import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Utilities;

public abstract class AbsBaseAdapter extends BaseAdapter {
    LayoutInflater mInflater = GlobalContext.getActivity().getLayoutInflater();
    float mFontSize = Utilities.getFontSize();
}
