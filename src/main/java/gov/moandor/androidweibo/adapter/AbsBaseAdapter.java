package gov.moandor.androidweibo.adapter;

import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Utilities;

public abstract class AbsBaseAdapter extends BaseAdapter {
    protected LayoutInflater mInflater = GlobalContext.getActivity().getLayoutInflater();
    protected float mFontSize = Utilities.getFontSize();
}
