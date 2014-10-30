package com.app4am.app4am;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class CheckUpdateIntentService extends IntentService {

    private static final String TAG = CheckUpdateIntentService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CHECK_UPDATE = "com.app4am.app4am.action.CHECK_UPDATE";

    private static final String EXTRA_PARAM_VERSION_CODE = "com.app4am.app4am.extra.PARAM_VERSION_CODE";
    private static final String EXTRA_PARAM_VERSION_NAME = "com.app4am.app4am.extra.PARAM_VERSION_NAME";

    public static final String BUNDLE_KEY_PACKAGE_VERSION_CODE = "bundle_key_package_version_code";
    public static final String BUNDLE_KEY_PACKAGE_VERSION_NAME = "bundle_key_package_version_name";
    public static final String BUNDLE_KEY_PACKAGE_DOWNLOAD_URI = "bundle_key_package_download_uri";

    public CheckUpdateIntentService() {
        super("CheckUpdateIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionCheckUpdate(Context context, String param_version_code, String param_version_name) {
        Intent intent = new Intent(context, CheckUpdateIntentService.class);
        intent.setAction(ACTION_CHECK_UPDATE);
        intent.putExtra(EXTRA_PARAM_VERSION_CODE, param_version_code);
        intent.putExtra(EXTRA_PARAM_VERSION_NAME, param_version_name);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_UPDATE.equals(action)) {
                final String param_version_code = intent.getStringExtra(EXTRA_PARAM_VERSION_CODE);
                final String param_version_name = intent.getStringExtra(EXTRA_PARAM_VERSION_NAME);
                handleActionCheckUpdate(param_version_code, param_version_name);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCheckUpdate(String param_version_code, String param_version_name) {
        // Check startCrawler
/*
        NetAgent netAgent = ((App4amApplication) getApplication()).getNetAgent();
        Request okRequest = netAgent.getRequestBuilder()
                .url("http://192.168.1.106/~dino/check.txt")
                .get()
                .build();

        try {
            Response okResponse = netAgent.syncCall(okRequest);
            if (okResponse.isSuccessful()) {
                String result = okResponse.body().string();
*/

        final String packageDownloadUri = "http://192.168.1.106/~dino/app-release.apk";
        final int versionCode = 1;
        final String versionName = "0.1-alpha";
        // TODO: check version info
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.CheckUpdateReceiver.ACTION_UPDATE_AVAILABLE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(BUNDLE_KEY_PACKAGE_VERSION_CODE, versionCode);
        broadcastIntent.putExtra(BUNDLE_KEY_PACKAGE_VERSION_NAME, versionName);
        broadcastIntent.putExtra(BUNDLE_KEY_PACKAGE_DOWNLOAD_URI, packageDownloadUri);
        sendBroadcast(broadcastIntent);
/*
            } else {
                switch (okResponse.code()) {
                    case HttpStatus.SC_FORBIDDEN:
                        Log.d(TAG, "status: " + HttpStatus.SC_FORBIDDEN);
                        break;
                    case HttpStatus.SC_NOT_FOUND:
                        Log.d(TAG, "status: " + HttpStatus.SC_NOT_FOUND);
                        break;
                    default:
                        Log.d(TAG, "status: " + okResponse.code());
                        break;
                }
            }

        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
*/

    }
}
