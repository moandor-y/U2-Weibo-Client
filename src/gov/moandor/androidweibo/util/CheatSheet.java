package gov.moandor.androidweibo.util;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class CheatSheet {
    private static final int TOAST_HEIGHT = Utilities.dpToPx(48);
    
    public static void setup(View view, int textResId) {
        view.setOnLongClickListener(new OnLongClickListener(textResId));
    }
    
    private static class OnLongClickListener implements View.OnLongClickListener {
        private int mTextResId;
        
        public OnLongClickListener(int textResId) {
            mTextResId = textResId;
        }
        
        @Override
        public boolean onLongClick(View v) {
            int[] screenPos = new int[2];
            Rect displayFrame = new Rect();
            v.getLocationOnScreen(screenPos);
            v.getWindowVisibleDisplayFrame(displayFrame);
            Context context = GlobalContext.getInstance();
            int width = v.getWidth();
            int height = v.getHeight();
            int screenWidth = Utilities.getScreenWidth();
            Toast cheatSheet = Toast.makeText(context, mTextResId, Toast.LENGTH_SHORT);
            cheatSheet.setGravity(Gravity.TOP | Gravity.RIGHT, screenWidth - screenPos[0] - width / 2, displayFrame
                    .height()
                    - height - TOAST_HEIGHT);
            cheatSheet.show();
            return true;
        }
    }
}
