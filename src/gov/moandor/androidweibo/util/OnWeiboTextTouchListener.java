package gov.moandor.androidweibo.util;

import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.fragment.AbsTimelineFragment;

/**
 * Created by Moandor on 7/3/2014.
 */
public class OnWeiboTextTouchListener implements View.OnTouchListener {
    private AbsTimelineFragment<?, ?> mFragment;

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
                WeiboTextUrlSpan[] spans = text.getSpans(
                        0, text.length(), WeiboTextUrlSpan.class);
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
                if (found && !hasActionMode()) {
                    consumeEvent = true;
                }
                if (found && !consumeEvent) {
                    clearBackgroundColorSpans(text, textView);
                }
                if (consumeEvent) {
                    BackgroundColorSpan span = new BackgroundColorSpan(
                            Utilities.getColor(R.attr.link_pressed_background_color));
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
        BackgroundColorSpan[] spans = text.getSpans(
                0, text.length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan span : spans) {
            text.removeSpan(span);
            textView.setText(text);
        }
    }

    private boolean hasActionMode() {
        return mFragment != null && mFragment.hasActionMode();
    }

    public void setFragment(AbsTimelineFragment<?, ?> fragment) {
        mFragment = fragment;
    }
}
