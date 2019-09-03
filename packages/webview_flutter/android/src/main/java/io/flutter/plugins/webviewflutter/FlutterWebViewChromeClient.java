package io.flutter.plugins.webviewflutter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import io.flutter.plugin.common.PluginRegistry;

import static android.app.Activity.RESULT_OK;

@SuppressWarnings("unchecked")
public class FlutterWebViewChromeClient extends WebChromeClient {

    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    private static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private Context context;
    private PluginRegistry.Registrar registrar;

    public FlutterWebViewChromeClient(
            final Context context,
            final PluginRegistry.Registrar registrar) {
        this.context = context;
        this.registrar = registrar;
        addResultListener();
    }

    // For 3.0+ Devices (Start)
    // onActivityResult attached before constructor
    protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        registrar.activity().startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
    }

    // For Lollipop 5.0+ Devices
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onShowFileChooser(
            WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        if (uploadMessage != null) {
            uploadMessage.onReceiveValue(null);
        }
        uploadMessage = filePathCallback;

        Intent contentSelectionIntent  = fileChooserParams.createIntent();
        try {
            registrar.activity().startActivityForResult(contentSelectionIntent, REQUEST_SELECT_FILE);
        } catch (ActivityNotFoundException e) {
            uploadMessage = null;
            Toast.makeText(context, "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
            return false;
        } catch (Exception e) {
            Toast.makeText(context, "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            uploadMessage = null;
            return false;
        }
        return true;
    }

    //For Android 4.1 only
    protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        mUploadMessage = uploadMsg;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        registrar.activity().startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
    }

    protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        registrar.activity().startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
    }

    private void addResultListener() {
        registrar.addActivityResultListener(new PluginRegistry.ActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int responseCode, Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (requestCode == REQUEST_SELECT_FILE) {
                    if (uploadMessage == null)
                        return false;
                    uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(responseCode, intent));
                    uploadMessage = null;
                }
            } else if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == mUploadMessage)
                    return false;
                // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
                // Use RESULT_OK only if you're implementing WebView inside an Activity
                Uri result = intent == null || responseCode != RESULT_OK ? null : intent.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
            return true;
            }
        });
    }
}
