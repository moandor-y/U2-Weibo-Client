package gov.moandor.androidweibo.adapter;

import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.AbsItemBean;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.fragment.AbsTimelineFragment;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.LongClickableLinkMovementMethod;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.TimeUtils;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboTextUrlSpan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbsTimelineListAdapter<T extends AbsItemBean> extends AbsBaseAdapter {
    private static final int MAX_COUNT = 500;
    
    List<T> mBeans = new ArrayList<T>();
    AbsTimelineFragment<T, ?> mFragment;
    boolean mNoPictureModeEnabled = ConfigManager.isNoPictureMode();
    float mTimeFontSize = mFontSize - 3;
    private OnAvatarClickListener mOnAvatarClickListener;
    private OnAvatarLongClickListener mOnAvatarLongClickListener;
    private ImageDownloader.ImageType mAvatarType = Utilities.getAvatarType();
    private int mSelectedPosition = -1;
    
    @Override
    public int getCount() {
        return mBeans.size();
    }
    
    @Override
    public T getItem(int position) {
        return mBeans.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return mBeans.get(position).id;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflateLayout(mInflater, parent);
            holder = initViewHolder(convertView);
            convertView.setTag(holder);
            initLayout(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        T bean = mBeans.get(position);
        buildUserLayout(holder, bean.weiboUser, position);
        buildContent(holder, bean, position);
        buildTime(holder, bean, position);
        if (position == mSelectedPosition) {
            convertView.setBackgroundResource(R.color.ics_blue_semi);
        } else {
            convertView.setBackgroundResource(0);
        }
        return convertView;
    }
    
    public void setFragment(AbsTimelineFragment<T, ?> fragment) {
        mFragment = fragment;
    }
    
    void buildTime(ViewHolder holder, T bean, int position) {
        String time = TimeUtils.getListTime(bean);
        if (!time.equals(holder.time.getText().toString())) {
            holder.time.setText(time);
        }
    }
    
    void buildContent(ViewHolder holder, T bean, int position) {
        if (TextUtils.isEmpty(bean.textSpannable)) {
            bean.textSpannable = TextUtils.addWeiboLinks(bean.text);
        }
        holder.text.setText(bean.textSpannable);
    }
    
    void buildUserLayout(ViewHolder holder, WeiboUser user, int position) {
        if (!TextUtils.isEmpty(user.remark)) {
            holder.userName.setText(user.name + " (" + user.remark + ")");
        } else {
            holder.userName.setText(user.name);
        }
        if (!mNoPictureModeEnabled) {
            holder.avatar.setVisibility(View.VISIBLE);
            buildAvatar(holder.avatar, user, position);
        }
    }
    
    void buildAvatar(ImageView view, WeiboUser user, final int position) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnAvatarClickListener != null) {
                    mOnAvatarClickListener.onAvatarClick(position);
                }
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnAvatarLongClickListener != null) {
                    mOnAvatarLongClickListener.onAvatarLongClick(position);
                    return true;
                }
                return false;
            }
        });
        ImageDownloader.downloadAvatar(view, user, mFragment.isListViewFling(), mAvatarType);
    }
    
    void initLayout(ViewHolder holder) {
        holder.time.setTextSize(mTimeFontSize);
        holder.text.setTextSize(mFontSize);
        holder.userName.setTextSize(mFontSize);
        holder.userName.getPaint().setFakeBoldText(true);
        holder.text.setOnTouchListener(mTextOnTouchListener);
        if (mNoPictureModeEnabled) {
            holder.avatar.setVisibility(View.INVISIBLE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.avatar.getLayoutParams();
            params.width = 0;
            params.rightMargin = 0;
        }
    }
    
    public void updateState() {
        mAvatarType = Utilities.getAvatarType();
    }
    
    public List<T> getItems() {
        List<T> result = new ArrayList<T>();
        result.addAll(mBeans);
        return result;
    }
    
    public void addAll(List<T> beans) {
        mBeans.addAll(beans);
    }
    
    public void addAllFirst(List<T> beans) {
        mBeans.addAll(0, beans);
        int size = mBeans.size();
        if (size > MAX_COUNT) {
            mBeans.subList(MAX_COUNT, size).clear();
        }
    }
    
    public int positionOf(T bean) {
        return mBeans.indexOf(bean);
    }
	
	public int positionOf(long id) {
		for (int i = 0; i < mBeans.size(); i++) {
			if (mBeans.get(i).id == id) {
				return i;
			}
		}
		return -1;
	}
    
    public void updateDataSet(List<T> beans) {
        mBeans.clear();
        mBeans.addAll(beans);
    }
    
    public void updateDataSet(T[] beans) {
        mBeans.clear();
        mBeans.addAll(Arrays.asList(beans));
    }
    
    public void updatePosition(int position, T bean) {
        mBeans.set(position, bean);
    }
    
    public void removeItem(int position) {
        mBeans.remove(position);
    }
    
    public void clearDataSet() {
        mBeans.clear();
    }
    
    public void setOnAvatarClickListener(OnAvatarClickListener l) {
        mOnAvatarClickListener = l;
    }
    
    public void setOnAvatarLongClickListener(OnAvatarLongClickListener l) {
        mOnAvatarLongClickListener = l;
    }
    
    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
    }
    
    public T getSelectedItem() {
        return getItem(mSelectedPosition);
    }
    
    public int getSelection() {
        return mSelectedPosition;
    }
    
    View.OnTouchListener mTextOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            TextView textView = (TextView) v;
            Layout layout = textView.getLayout();
            int x = (int) event.getX();
            int y = (int) event.getY();
            int offset = 0;
            if (layout != null) {
                int line = layout.getLineForVertical(y);
                offset = layout.getOffsetForHorizontal(line, x);
            }
            SpannableString text = SpannableString.valueOf(textView.getText());
            LongClickableLinkMovementMethod.getInstance().onTouchEvent(textView, text, event);
            switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                WeiboTextUrlSpan[] spans = text.getSpans(0, text.length(), WeiboTextUrlSpan.class);
                boolean found = false;
                int foundStart = 0;
                int foundEnd = 0;
                for (WeiboTextUrlSpan span : spans) {
                    int start = text.getSpanStart(span);
                    int end = text.getSpanEnd(span);
                    if (start <= offset && offset <= end) {
                        found = true;
                        foundStart = start;
                        foundEnd = end;
                        break;
                    }
                }
                boolean consumeEvent = false;
                if (found && !mFragment.hasActionMode()) {
                    consumeEvent = true;
                }
                if (found && !consumeEvent) {
                    clearBackgroundColorSpans(text, textView);
                }
                if (consumeEvent) {
                    BackgroundColorSpan span =
                            new BackgroundColorSpan(Utilities.getColor(R.attr.link_pressed_background_color));
                    text.setSpan(span, foundStart, foundEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    textView.setText(text);
                }
                return consumeEvent;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                LongClickableLinkMovementMethod.getInstance().removeLongClickCallback();
                clearBackgroundColorSpans(text, textView);
                break;
            }
            return false;
        }
        
        private void clearBackgroundColorSpans(SpannableString text, TextView textView) {
            BackgroundColorSpan[] spans = text.getSpans(0, text.length(), BackgroundColorSpan.class);
            for (BackgroundColorSpan span : spans) {
                text.removeSpan(span);
                textView.setText(text);
            }
        }
    };
    
    static class ViewHolder {
        public ImageView avatar;
        public TextView userName;
        public TextView time;
        public TextView text;
    }
    
    public static interface OnAvatarClickListener {
        public void onAvatarClick(int position);
    }
    
    public static interface OnAvatarLongClickListener {
        public void onAvatarLongClick(int position);
    }
    
    abstract View inflateLayout(LayoutInflater inflater, ViewGroup parent);
    
    abstract ViewHolder initViewHolder(View view);
}
