package gov.moandor.androidweibo.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.DialogFragment;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import gov.moandor.androidweibo.R;

public class WeiboTextUrlSpan extends ClickableSpan implements ParcelableSpan {
    private static final String LONG_CLICK_DIALOG = "long_click_dialog";
    
    private final String mUrl;
    
    public WeiboTextUrlSpan(String url) {
        mUrl = url;
    }
    
    @Override
    public int getSpanTypeId() {
        return 11;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUrl);
    }
    
    @Override
    public void onClick(View widget) {
        Uri uri = Uri.parse(mUrl);
        Context context = widget.getContext();
        Utilities.openUri(context, uri);
    }
    
    public void onLongClick(View widget) {
        LongClickDialogFragment dialog = new LongClickDialogFragment();
        String url = mUrl;
        if (mUrl.startsWith("androidweibo")) {
            url = mUrl.substring(mUrl.lastIndexOf("//") + 2);
        }
        Bundle args = new Bundle();
        args.putString(LongClickDialogFragment.URL, url);
        dialog.setArguments(args);
        dialog.show(GlobalContext.getActivity().getSupportFragmentManager(), LONG_CLICK_DIALOG);
    }
    
    @Override
    public void updateDrawState(TextPaint tp) {
        int color = Utilities.getColor(R.attr.link_color);
        tp.setColor(color);
    }
    
    public static class LongClickDialogFragment extends DialogFragment {
        private static final String URL = "url";
        
        private String mUrl;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mUrl = getArguments().getString(URL);
        }
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(R.array.span_long_click, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                    case 0:
                        Uri uri = Uri.parse(mUrl);
                        Utilities.openUri(getActivity(), uri);
                        break;
                    case 1:
                        Utilities.copyText(mUrl);
                        break;
                    }
                }
            });
            return builder.create();
        }
    }
}
