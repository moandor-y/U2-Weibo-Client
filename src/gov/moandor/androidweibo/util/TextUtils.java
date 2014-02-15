package gov.moandor.androidweibo.util;

import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
    private static final Pattern URL = Pattern.compile("http://[a-zA-Z0-9+&@#/%?=~_\\-|!:,\\.;]*[a-zA-Z0-9+&@#/%=~_|]");
    private static final Pattern TOPIC = Pattern.compile("#[\\p{Print}\\p{InCJKUnifiedIdeographs}&&[^#]]+#");
    private static final Pattern MENTION = Pattern.compile("@[\\w\\p{InCJKUnifiedIdeographs}-]{1,26}");
    private static final Pattern EMOTION = Pattern.compile("\\[(\\S+?)\\]");
    private static final String URL_SCHEME = "http://";
    private static final String TOPIC_SCHEME = "androidweibo.topic://";
    private static final String MENTION_SCHEME = "androidweibo.user://";
    
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }
    
    public static SpannableString addWeiboLinks(String text) {
        if (text.startsWith("[") && text.endsWith("]")) {
            text += " ";
        }
        SpannableString result = SpannableString.valueOf(text);
        Linkify.addLinks(result, MENTION, MENTION_SCHEME);
        Linkify.addLinks(result, URL, URL_SCHEME);
        Linkify.addLinks(result, TOPIC, TOPIC_SCHEME);
        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        WeiboTextUrlSpan weiboTextUrlSpan = null;
        for (URLSpan urlSpan : urlSpans) {
            weiboTextUrlSpan = new WeiboTextUrlSpan(urlSpan.getURL());
            int start = result.getSpanStart(urlSpan);
            int end = result.getSpanEnd(urlSpan);
            result.removeSpan(urlSpan);
            result.setSpan(weiboTextUrlSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        addEmotions(result);
        return result;
    }
    
    private static void addEmotions(SpannableString value) {
        Matcher matcher = EMOTION.matcher(value);
        while (matcher.find()) {
            String found = matcher.group(0);
            int start = matcher.start();
            int end = matcher.end();
            if (end - start < 8) {
                Bitmap bitmap = GlobalContext.getEmotion(found);
                if (bitmap != null) {
                    ImageSpan span =
                            new ImageSpan(GlobalContext.getActivity(), bitmap, DynamicDrawableSpan.ALIGN_BASELINE);
                    value.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }
}
