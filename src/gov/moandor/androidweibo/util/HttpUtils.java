package gov.moandor.androidweibo.util;

import org.json.JSONException;
import org.json.JSONObject;

import gov.moandor.androidweibo.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpUtils {
    private static final int CONNECT_TIMEOUT = 10 * 1000;
    private static final int READ_TIMEOUT = 10 * 1000;
    private static final int DOWNLOAD_CONNECT_TIMEOUT = 15 * 1000;
    private static final int DOWNLOAD_READ_TIMEOUT = 60 * 1000;
    private static final int UPLOAD_CONNECT_TIMEOUT = 15 * 1000;
    private static final int UPLOAD_READ_TIMEOUT = 5 * 60 * 1000;
    private static final String BOUNDARY = "-----------------------------7db1c5232222b";
    private static final String ENCODING = "UTF-8";
    
    private static final HostnameVerifier sDoNotVerify = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
    
    public static void trustAllHosts() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }
            
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
        }};
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            Logger.logExcpetion(e);
        }
    }
    
    public static enum Method {
        POST, GET
    }
    
    public static final class UrlHelper {
        //u2
        public static final String APPKEY = "1578201915";
        public static final String AUTH_REDIRECT = "https://api.weibo.com/oauth2/default.html";
        
        //weiciyuan
        //public static final String APPKEY = "1065511513";
        //public static final String AUTH_REDIRECT = "https://api.weibo.com/oauth2/default.html";
        
        public static final String OAUTH2_AUTHORIZE = "https://api.weibo.com/oauth2/authorize";
        public static final String STATUSES_FRIENDS_TIMELINE = "https://api.weibo.com/2/statuses/friends_timeline.json";
        public static final String STATUSES_BILATERAL_TIMELINE =
                "https://api.weibo.com/2/statuses/bilateral_timeline.json";
        public static final String STATUSES_MENTIONS = "https://api.weibo.com/2/statuses/mentions.json";
        public static final String STATUSES_REPOST_TIMELINE = "https://api.weibo.com/2/statuses/repost_timeline.json";
        public static final String STATUSES_SHOW = "https://api.weibo.com/2/statuses/show.json";
        public static final String STATUSES_USER_TIMELINE = "https://api.weibo.com/2/statuses/user_timeline.json";
        public static final String STATUSES_UPDATE = "https://api.weibo.com/2/statuses/update.json";
        public static final String STATUSES_REPOST = "https://api.weibo.com/2/statuses/repost.json";
        public static final String STATUSES_UPLOAD = "https://upload.api.weibo.com/2/statuses/upload.json";
        public static final String STATUSES_DESTROY = "https://api.weibo.com/2/statuses/destroy.json";
        public static final String ACCOUNT_GET_UID = "https://api.weibo.com/2/account/get_uid.json";
        public static final String USERS_SHOW = "https://api.weibo.com/2/users/show.json";
        public static final String COMMENTS_TO_ME = "https://api.weibo.com/2/comments/to_me.json";
        public static final String COMMENTS_BY_ME = "https://api.weibo.com/2/comments/by_me.json";
        public static final String COMMENTS_MENTIONS = "https://api.weibo.com/2/comments/mentions.json";
        public static final String COMMENTS_SHOW = "https://api.weibo.com/2/comments/show.json";
        public static final String COMMENTS_CREATE = "https://api.weibo.com/2/comments/create.json";
        public static final String COMMENTS_REPLY = "https://api.weibo.com/2/comments/reply.json";
        public static final String COMMENTS_DESTROY = "https://api.weibo.com/2/comments/destroy.json";
        public static final String FRIENDSHIPS_FRIENDS = "https://api.weibo.com/2/friendships/friends.json";
        public static final String FRIENDSHIPS_FOLLOWERS = "https://api.weibo.com/2/friendships/followers.json";
        public static final String FRIENDSHIPS_CREATE = "https://api.weibo.com/2/friendships/create.json";
        public static final String FRIENDSHIPS_DESTROY = "https://api.weibo.com/2/friendships/destroy.json";
        public static final String REMIND_UNREAD_COUNT = "https://rm.api.weibo.com/2/remind/unread_count.json";
        public static final String REMIND_SET_COUNT = "https://rm.api.weibo.com/2/remind/set_count.json";
        public static final String SEARCH_TOPICS = "https://api.weibo.com/2/search/topics.json";
        public static final String SEARCH_SUGGESTIONS_AT_USERS = "https://api.weibo.com/2/search/suggestions/at_users.json";
        public static final String FAVORITES = "https://api.weibo.com/2/favorites.json";
        public static final String FAVORITES_CREATE = "https://api.weibo.com/2/favorites/create.json";
        public static final String FAVORITES_DESTROY = "https://api.weibo.com/2/favorites/destroy.json";
    }
    
    private static Proxy getProxy() {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if (!TextUtils.isEmpty(proxyHost) && !TextUtils.isEmpty(proxyPort)) {
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.valueOf(proxyPort)));
        } else {
            return null;
        }
    }
    
    public static String executeNormalTask(Method httpMethod, String urlAddress, HttpParams params)
            throws WeiboException {
        switch (httpMethod) {
        case GET:
            return doGet(urlAddress, params);
        case POST:
            return doPost(urlAddress, params);
        }
        return null;
    }
    
    public static boolean executeDownloadTask(String urlAddress, String cachePath, DownloadListener listener) {
        return !Thread.currentThread().isInterrupted() && download(urlAddress, cachePath, listener);
    }
    
    public static boolean executeUploadTask(String urlAddress, HttpParams params, String path, String fileParamKey, 
            UploadListener listener) throws WeiboException {
        return !Thread.currentThread().isInterrupted() && uploadFile(urlAddress, params, path, fileParamKey, listener);
    }
    
    private static boolean download(String urlAddress, String cachePath, DownloadListener listener) {
        File cacheFile = FileUtils.createFile(cachePath);
        if (cacheFile == null) {
            return false;
        }
        BufferedOutputStream out = null;
        InputStream in = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlAddress);
            Proxy proxy = getProxy();
            if (proxy != null) {
                connection = (HttpURLConnection) url.openConnection(proxy);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.setConnectTimeout(DOWNLOAD_CONNECT_TIMEOUT);
            connection.setReadTimeout(DOWNLOAD_READ_TIMEOUT);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            connection.connect();
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                return false;
            }
            int total = connection.getContentLength();
            int sum = 0;
            int read = 0;
            out = new BufferedOutputStream(new FileOutputStream(cacheFile));
            in = new BufferedInputStream(connection.getInputStream());
            final Thread thread = Thread.currentThread();
            byte[] buffer = new byte[1024];
            while ((read = in.read(buffer)) != -1) {
                if (thread.isInterrupted()) {
                    in.close();
                    out.close();
                    connection.disconnect();
                    cacheFile.delete();
                    throw new InterruptedIOException();
                }
                sum += read;
                out.write(buffer, 0, read);
                if (listener != null && total > 0) {
                    listener.onPushProgress(sum, total);
                }
            }
            return true;
        } catch (IOException e) {
            Logger.logExcpetion(e);
            cacheFile.delete();
        } finally {
            Utilities.closeSilently(in);
            Utilities.closeSilently(out);
            if (connection != null) {
                connection.disconnect();
            }
        }
        return false;
    }
    
    private static String readResult(HttpURLConnection connection) throws WeiboException {
        InputStream in = null;
        BufferedReader buffer = null;
        try {
            in = connection.getInputStream();
            if ("gzip".equals(connection.getContentEncoding())) {
                in = new GZIPInputStream(in);
            }
            buffer = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = buffer.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.network_error));
        } finally {
            Utilities.closeSilently(buffer);
            Utilities.closeSilently(in);
            connection.disconnect();
        }
    }
    
    private static String doGet(String urlAddress, HttpParams params) throws WeiboException {
        try {
            URL url = new URL(urlAddress + "?" + params.getParams());
            Proxy proxy = getProxy();
            HttpURLConnection connection;
            if (proxy != null) {
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection(proxy);
                https.setHostnameVerifier(sDoNotVerify);
                connection = https;
            } else {
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(sDoNotVerify);
                connection = https;
            }
            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            connection.connect();
            return handleResponse(connection);
        } catch (IOException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.network_error));
        }
    }
    
    private static String doPost(String urlAddress, HttpParams params) throws WeiboException {
        try {
            URL url = new URL(urlAddress);
            Proxy proxy = getProxy();
            HttpsURLConnection connection;
            if (proxy != null) {
                connection = (HttpsURLConnection) url.openConnection(proxy);
            } else {
                connection = (HttpsURLConnection) url.openConnection();
            }
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            connection.connect();
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(params.getParams().getBytes());
            out.flush();
            out.close();
            return handleResponse(connection);
        } catch (IOException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.network_error));
        }
    }
    
    private static String handleResponse(HttpURLConnection connection) throws WeiboException {
        int responseCode;
        try {
            responseCode = connection.getResponseCode();
        } catch (IOException e) {
            Logger.logExcpetion(e);
            connection.disconnect();
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.network_error));
        }
        if (responseCode != HttpURLConnection.HTTP_OK) {
            handleError(connection);
        }
        return readResult(connection);
    }
    
    private static void handleError(HttpURLConnection connection) throws WeiboException {
        String result = readError(connection);
        try {
            JSONObject json = new JSONObject(result);
            String err = json.optString("error_description");
            if (TextUtils.isEmpty(err)) {
                err = json.optString("error");
            }
            int code = json.optInt("error_code");
            WeiboException e = new WeiboException(code);
            e.mOriError = err;
            throw e;
        } catch (JSONException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.unknown_error));
        }
    }
    
    private static String readError(HttpURLConnection connection) throws WeiboException {
        InputStream in = null;
        BufferedReader buffer = null;
        try {
            in = connection.getErrorStream();
            if (in == null) {
                throw new WeiboException(GlobalContext.getInstance().getString(R.string.network_error));
            }
            if ("gzip".equals(connection.getContentEncoding())) {
                in = new GZIPInputStream(in);
            }
            buffer = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = buffer.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.network_error));
        } finally {
            Utilities.closeSilently(buffer);
            Utilities.closeSilently(in);
            connection.disconnect();
        }
    }
    
    private static boolean uploadFile(String urlAddress, HttpParams params, String path, String fileParamKey, 
            UploadListener listener) throws WeiboException {
        StringBuffer sb = new StringBuffer();
        sb.append("--").append(BOUNDARY).append("\r\n");
        Iterator<String> keys = params.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = params.getParam(key);
            sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"\r\n\r\n");
            sb.append(value).append("\r\n");
            sb.append("--").append(BOUNDARY).append("\r\n");
        }
        File file = new File(path);
        sb.append("Content-Disposition: form-data; name=\"").append(fileParamKey).append("\"; ");
        sb.append("filename=\"").append(file.getName()).append("\"\r\n");
        sb.append("Content-Type: image/png").append("\r\n\r\n");
        String paramString = sb.toString();
        HttpURLConnection connection = null;
        BufferedOutputStream out = null;
        FileInputStream in = null;
        try {
            byte[] paramBytes = paramString.getBytes(ENCODING);
            String end = "\r\n--" + BOUNDARY + "--\r\n";
            byte[] endBytes = end.getBytes(ENCODING);
            int length = paramBytes.length + (int) file.length() + endBytes.length;
            URL url = new URL(urlAddress);
            Proxy proxy = getProxy();
            if (proxy != null) {
                connection = (HttpURLConnection) url.openConnection(proxy);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setConnectTimeout(UPLOAD_CONNECT_TIMEOUT);
            connection.setReadTimeout(UPLOAD_READ_TIMEOUT);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-type", "multipart/form-data;boundary=" + BOUNDARY);
            connection.setRequestProperty("Content-Length", String.valueOf(length));
            connection.setFixedLengthStreamingMode(length);
            connection.connect();
            int sent = 0;
            if (listener != null) {
                listener.onTransferring(sent, length);
            }
            out = new BufferedOutputStream(connection.getOutputStream());
            out.write(paramBytes);
            sent += paramBytes.length;
            in = new FileInputStream(file);
            int read;
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            while ((read = in.read(buffer, 0, bufferSize)) > 0) {
                out.write(buffer, 0, read);
                sent += read;
                if (sent % 50 == 0) {
                    out.flush();
                }
                if (listener != null) {
                    listener.onTransferring(sent, length);
                }
            }
            out.write(endBytes);
            if (listener != null) {
                listener.onTransferring(length, length);
            }
            out.flush();
            if (listener != null) {
                listener.onWaitResponse();
            }
            int responseCode = connection.getResponseCode();
            if (listener != null) {
                listener.onComplete();
            }
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                handleError(connection);
            }
        } catch (IOException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.network_error));
        } finally {
            Utilities.closeSilently(in);
            Utilities.closeSilently(out);
            if (connection != null) {
                connection.disconnect();
            }
        }
        return false;
    }
    
    public static interface DownloadListener {
        public void onPushProgress(int progress, int max);
        
        public void onComplete();
        
        public void onCancelled();
    }
    
    public static interface UploadListener {
        public void onTransferring(int sent, int total);
        
        public void onWaitResponse();
        
        public void onComplete();
    }
}
