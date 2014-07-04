package gov.moandor.androidweibo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.AbsItemBean;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.fragment.AbsTimelineFragment;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.OnWeiboTextTouchListener;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.TimeUtils;
import gov.moandor.androidweibo.util.Utilities;

public abstract class AbsTimelineListAdapter<T extends AbsItemBean> extends AbsBaseAdapter
        implements ISelectableAdapter<T> {
    private static final int MAX_COUNT = 500;

    protected OnWeiboTextTouchListener mTextOnTouchListener = new OnWeiboTextTouchListener();
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
        mTextOnTouchListener.setFragment(mFragment);
    }

    public void onUserStateChanged(WeiboUser user) {
        for (T bean : mBeans) {
            if (bean.weiboUser.id == user.id) {
                bean.weiboUser = user;
            }
        }
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
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) holder.avatar.getLayoutParams();
            params.width = 0;
            params.rightMargin = 0;
        }
    }

    public void updateState() {
        mAvatarType = Utilities.getAvatarType();
    }

    public List<T> getItems() {
        return new ArrayList<T>(mBeans);
    }

    public void add(T bean) {
        mBeans.add(bean);
    }

    public void addAll(List<T> beans) {
        mBeans.addAll(beans);
    }

    public void addFirst(T bean) {
        mBeans.add(0, bean);
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

    @Override
    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
    }

    @Override
    public T getSelectedItem() {
        return mBeans.get(mSelectedPosition);
    }

    @Override
    public int getSelection() {
        return mSelectedPosition;
    }

    abstract View inflateLayout(LayoutInflater inflater, ViewGroup parent);

    abstract ViewHolder initViewHolder(View view);

    public static interface OnAvatarClickListener {
        public void onAvatarClick(int position);
    }

    public static interface OnAvatarLongClickListener {
        public void onAvatarLongClick(int position);
    }

    static class ViewHolder {
        public ImageView avatar;
        public TextView userName;
        public TextView time;
        public TextView text;
    }
}
