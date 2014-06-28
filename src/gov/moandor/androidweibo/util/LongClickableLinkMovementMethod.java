package gov.moandor.androidweibo.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.text.Layout;
import android.text.NoCopySpan;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;

public class LongClickableLinkMovementMethod extends ScrollingMovementMethod {
    private static final int CLICK = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;
    private static LongClickableLinkMovementMethod sInstance;
    private static Object FROM_BELOW = new NoCopySpan.Concrete();
    private CheckForLongPress mPendingCheckForLongPress;
    private Handler mHandler = new Handler();
    private boolean mHasPerformedLongPress;
    private boolean mPressed;
    private boolean mLongClickable = true;
    private float[] mLastEvent = new float[2];

    public static LongClickableLinkMovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new LongClickableLinkMovementMethod();
        }
        return sInstance;
    }

    public void setLongClickable(boolean value) {
        mLongClickable = value;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected boolean handleMovementKey(TextView widget, Spannable buffer, int keyCode, int movementMetaState,
                                        KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (KeyEvent.metaStateHasNoModifiers(movementMetaState)) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0
                            && action(CLICK, widget, buffer)) {
                        return true;
                    }
                }
                break;
        }
        return super.handleMovementKey(widget, buffer, keyCode, movementMetaState, event);
    }

    @Override
    protected boolean up(TextView widget, Spannable buffer) {
        if (action(UP, widget, buffer)) {
            return true;
        }
        return super.up(widget, buffer);
    }

    @Override
    protected boolean down(TextView widget, Spannable buffer) {
        if (action(DOWN, widget, buffer)) {
            return true;
        }
        return super.down(widget, buffer);
    }

    @Override
    protected boolean left(TextView widget, Spannable buffer) {
        if (action(UP, widget, buffer)) {
            return true;
        }
        return super.left(widget, buffer);
    }

    @Override
    protected boolean right(TextView widget, Spannable buffer) {
        if (action(DOWN, widget, buffer)) {
            return true;
        }
        return super.right(widget, buffer);
    }

    private boolean action(int what, TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        int padding = widget.getTotalPaddingTop() + widget.getTotalPaddingBottom();
        int areatop = widget.getScrollY();
        int areabot = areatop + widget.getHeight() - padding;
        int linetop = layout.getLineForVertical(areatop);
        int linebot = layout.getLineForVertical(areabot);
        int first = layout.getLineStart(linetop);
        int last = layout.getLineEnd(linebot);
        WeiboTextUrlSpan[] candidates = buffer.getSpans(first, last, WeiboTextUrlSpan.class);
        int a = Selection.getSelectionStart(buffer);
        int b = Selection.getSelectionEnd(buffer);
        int selStart = Math.min(a, b);
        int selEnd = Math.max(a, b);
        if (selStart < 0) {
            if (buffer.getSpanStart(FROM_BELOW) >= 0) {
                selStart = selEnd = buffer.length();
            }
        }
        if (selStart > last) {
            selStart = selEnd = Integer.MAX_VALUE;
        }
        if (selEnd < first) {
            selStart = selEnd = -1;
        }
        switch (what) {
            case CLICK:
                if (selStart == selEnd) {
                    return false;
                }
                WeiboTextUrlSpan[] link = buffer.getSpans(selStart, selEnd, WeiboTextUrlSpan.class);
                if (link.length != 1) {
                    return false;
                }
                link[0].onClick(widget);
                break;
            case UP:
                int beststart,
                        bestend;
                beststart = -1;
                bestend = -1;
                for (WeiboTextUrlSpan candidate : candidates) {
                    int end = buffer.getSpanEnd(candidate);
                    if (end < selEnd || selStart == selEnd) {
                        if (end > bestend) {
                            beststart = buffer.getSpanStart(candidate);
                            bestend = end;
                        }
                    }
                }
                if (beststart >= 0) {
                    Selection.setSelection(buffer, bestend, beststart);
                    return true;
                }
                break;
            case DOWN:
                beststart = Integer.MAX_VALUE;
                bestend = Integer.MAX_VALUE;
                for (WeiboTextUrlSpan candidate : candidates) {
                    int start = buffer.getSpanStart(candidate);

                    if (start > selStart || selStart == selEnd) {
                        if (start < beststart) {
                            beststart = start;
                            bestend = buffer.getSpanEnd(candidate);
                        }
                    }
                }
                if (bestend < Integer.MAX_VALUE) {
                    Selection.setSelection(buffer, beststart, bestend);
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();
            x += widget.getScrollX();
            y += widget.getScrollY();
            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);
            WeiboTextUrlSpan[] link = buffer.getSpans(off, off, WeiboTextUrlSpan.class);
            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    if (!mHasPerformedLongPress) {
                        link[0].onClick(widget);
                    }
                    mPressed = false;
                    mLastEvent = new float[2];
                } else if (action == MotionEvent.ACTION_DOWN) {
                    mPressed = true;
                    mLastEvent[0] = event.getX();
                    mLastEvent[1] = event.getY();
                    checkForLongClick(link, widget);
                    Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
                }
                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            float[] position = {event.getX(), event.getY()};
            int slop = 6;
            float xInstance = Math.abs(mLastEvent[0] - position[0]);
            float yInstance = Math.abs(mLastEvent[1] - position[1]);
            double instance = Math.sqrt(Math.hypot(xInstance, yInstance));
            if (instance > slop) {
                mPressed = false;
            }
        } else {
            mPressed = false;
            mLastEvent = new float[2];
        }
        return super.onTouchEvent(widget, buffer, event);
    }

    private void checkForLongClick(WeiboTextUrlSpan[] spans, View widget) {
        mHasPerformedLongPress = false;
        mPendingCheckForLongPress = new CheckForLongPress(spans, widget);
        mHandler.postDelayed(mPendingCheckForLongPress, ViewConfiguration.getLongPressTimeout());
    }

    public void removeLongClickCallback() {
        if (mPendingCheckForLongPress != null) {
            mHandler.removeCallbacks(mPendingCheckForLongPress);
            mPendingCheckForLongPress = null;
        }
    }

    public boolean isPressed() {
        return mPressed;
    }

    @Override
    public void initialize(TextView widget, Spannable text) {
        Selection.removeSelection(text);
        text.removeSpan(FROM_BELOW);
    }

    @Override
    public void onTakeFocus(TextView view, Spannable text, int dir) {
        Selection.removeSelection(text);
        if ((dir & View.FOCUS_BACKWARD) != 0) {
            text.setSpan(FROM_BELOW, 0, 0, Spanned.SPAN_POINT_POINT);
        } else {
            text.removeSpan(FROM_BELOW);
        }
    }

    private class CheckForLongPress implements Runnable {
        private WeiboTextUrlSpan[] mSpans;
        private View mWidget;

        public CheckForLongPress(WeiboTextUrlSpan[] spans, View widget) {
            mSpans = spans;
            mWidget = widget;
        }

        @Override
        public void run() {
            if (isPressed() && mLongClickable) {
                mSpans[0].onLongClick(mWidget);
                mHasPerformedLongPress = true;
            }
        }
    }
}
