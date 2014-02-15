package gov.moandor.androidweibo.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.util.GlobalContext;

import java.util.ArrayList;
import java.util.List;

public class SmileyPicker extends GridView {
    private static List<String> sItems = new ArrayList<String>();
    private static List<Bitmap> sBitmaps = new ArrayList<Bitmap>();
    
    static {
        for (String key : GlobalContext.getEmotionNameMap().keySet()) {
            sItems.add(key);
            sBitmaps.add(GlobalContext.getEmotion(key));
        }
    }
    
    public SmileyPicker(Context context) {
        super(context);
        initView(context);
    }
    
    public SmileyPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }
    
    public SmileyPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }
    
    private void initView(Context context) {
        setNumColumns(AUTO_FIT);
        setAdapter(new GridAdapter(context));
    }
    
    public static String getKey(int position) {
        return sItems.get(position);
    }
    
    private static class GridAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        
        public GridAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        @Override
        public int getCount() {
            return sBitmaps.size();
        }
        
        @Override
        public Object getItem(int position) {
            return sItems.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return sItems.get(position).hashCode();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView view;
            if (convertView != null) {
                view = (ImageView) convertView;
            } else {
                view = (ImageView) mInflater.inflate(R.layout.smiley_picker_item, null);
            }
            view.setImageBitmap(sBitmaps.get(position));
            return view;
        }
    }
}
