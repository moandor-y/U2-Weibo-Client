package gov.moandor.androidweibo.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.ImageUtils;

import java.io.File;

public class MainDrawerListAdapter extends BaseAdapter {
    private LayoutInflater mInflater = GlobalContext.getActivity().getLayoutInflater();
    
    @Override
    public int getCount() {
        return GlobalContext.getAccountCount();
    }
    
    @Override
    public Account getItem(int position) {
        return GlobalContext.getAccount(position);
    }
    
    @Override
    public long getItemId(int position) {
        return GlobalContext.getAccount(position).id;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.account_list_item, parent, false);
            viewHolder = initViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Account account = GlobalContext.getAccount(position);
        DownloadAvatarTask task = new DownloadAvatarTask(account.avatarURL, viewHolder.avatarView);
        task.execute();
        viewHolder.nameView.setText(account.name);
        viewHolder.nameView.getPaint().setFakeBoldText(true);
        if (position != GlobalContext.getCurrentAccountIndex()) {
            viewHolder.tickView.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.tickView.setVisibility(View.VISIBLE);
        }
        return convertView;
    }
    
    private static class ViewHolder {
        public ImageView avatarView;
        public TextView nameView;
        public ImageView tickView;
    }
    
    private static ViewHolder initViewHolder(View view) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.avatarView = (ImageView) view.findViewById(R.id.avatar);
        viewHolder.nameView = (TextView) view.findViewById(R.id.name);
        viewHolder.tickView = (ImageView) view.findViewById(R.id.tick);
        return viewHolder;
    }
    
    private static class DownloadAvatarTask extends MyAsyncTask<Void, Void, Bitmap> {
        private String mUrl;
        private ImageView mView;
        
        public DownloadAvatarTask(String url, ImageView view) {
            mUrl = url;
            mView = view;
        }
        
        @Override
        protected Bitmap doInBackground(Void... v) {
            Bitmap bitmap = GlobalContext.getBitmapCache().get(mUrl);
            if (bitmap != null) {
                return bitmap;
            }
            int avatarWidth =
                    GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.list_avatar_width);
            int avatarHeight =
                    GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.list_avatar_height);
            String path = FileUtils.getAccountAvatarPathFromUrl(mUrl);
            File file = new File(path);
            if (file.exists()) {
                bitmap = ImageUtils.getBitmapFromFile(file.getAbsolutePath(), avatarWidth, avatarHeight);
                if (bitmap != null) {
                    return bitmap;
                }
            }
            if (ImageUtils.getBitmapFromNetwork(mUrl, path, null)) {
                return ImageUtils.getBitmapFromFile(path, avatarWidth, avatarHeight);
            }
            cancel(true);
            return null;
        }
        
        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                GlobalContext.getBitmapCache().put(mUrl, result);
                mView.setImageBitmap(result);
            }
        }
    }
}
