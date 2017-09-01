package news.chen.yu.ionic;

import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONException;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.alibaba.sdk.android.feedback.util.IUnreadCountCallback;

public class AlicloudFeedback extends CordovaPlugin {
    private boolean __init = false;

    private void init() {
        if(!this.__init) {
            this.__init = true;
            String appKey = preferences.getString("app_key", "");
            String appSecret = preferences.getString("app_secret", "");
            FeedbackAPI.init(cordova.getActivity().getApplication(), appKey, appSecret);
        }
    }
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.init();
        if (action.equals("open")) {
            String param = args.getString(0);
            this.open(param, callbackContext);
            callbackContext.success();
            return true;
        }

        return false;
    }

    private void open(String param, final CallbackContext callbackContext) {
        try {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    FeedbackAPI.openFeedbackActivity();
                    callbackContext.success();
                }
            });
            
        } catch(Exception e) {
            callbackContext.error(e.toString());
        }
    }

    private void fetchUnreadCount(String param, final CallbackContext callbackContext) {
        try {
            FeedbackAPI.getFeedbackUnreadCount(new IUnreadCountCallback() {
                @Override
                public void onSuccess(final int unreadCount) {
                    callbackContext.success(unreadCount);
                }

                @Override
                public void onError(int i, String s) {
                    callbackContext.error(s);
                }
            });

        } catch(Exception e) {
            callbackContext.error(e.toString());
        }
    }
}