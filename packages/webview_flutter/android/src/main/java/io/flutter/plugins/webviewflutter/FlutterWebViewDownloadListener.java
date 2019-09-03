package io.flutter.plugins.webviewflutter;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;

import static android.content.Context.DOWNLOAD_SERVICE;

public class FlutterWebViewDownloadListener implements DownloadListener {

    private final Context context;

    public FlutterWebViewDownloadListener(final Context context) {
        this.context = context;
    }

    @Override
    public void onDownloadStart(
        String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        String cookie = CookieManager.getInstance().getCookie(url);
        request.addRequestHeader("Cookie",cookie);
        request.addRequestHeader("User-Agent",userAgent);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        DownloadManager dManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);

        dManager.enqueue(request);
    }
}
