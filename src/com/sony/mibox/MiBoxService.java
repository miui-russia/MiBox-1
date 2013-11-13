package com.sony.mibox;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.util.Log;
import com.chrisplus.rootmanager.RootManager;
import fi.iki.elonen.NanoHTTPD;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MiBoxService extends Service implements HttpServer.OnRequestListener {
    public static final String TAG = "MiBoxService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHttpServer = new HttpServer(8080);
        mHttpServer.setOnRequestListener(this);
        mSSDPServer = new SSDPServer(this);
        try {
            mHttpServer.start();
            mSSDPServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRootManager = RootManager.getInstance();
        if (mRootManager.hasRooted() && mRootManager.grantPermission()) {
            mRootManager.runCommand("chmod 777 /dev/uinput");
            init();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHttpServer != null) {
            mHttpServer.stop();
            mHttpServer = null;
        }

        if (mSSDPServer != null) {
            mSSDPServer.stop();
        }

        if (mRootManager.hasRooted()) {
            destroy();
        }
    }

    @Override
    public NanoHTTPD.Response onRequest(String action, Map<String, String> params) {
        Log.d(TAG, "action = " + action);

        if (action.equals("applist")) {
            return getAppList();
        } else if(action.equals("run")) {
            return runApplication(params.get("package"), params.get("class"));
        } else if(action.equals("injectkey")) {
            int keyCode = Integer.valueOf(params.get("keycode"));
            injectKey(keyCode);
            return new NanoHTTPD.Response("OK");
        }
        return null;
    }

    // Get all installed applications.
    private NanoHTTPD.Response getAppList() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> appList = pm.queryIntentActivities(intent, 0);

        JSONArray appArray = new JSONArray();
        for (ResolveInfo info : appList) {
            JSONObject app = new JSONObject();
            try {
                app.put("name", info.loadLabel(pm).toString());
                app.put("package", info.activityInfo.packageName);
                app.put("class", info.activityInfo.name);

                JSONObject icon = new JSONObject();
                Bitmap bitmap =  ((BitmapDrawable)info.loadIcon(pm)).getBitmap();
                icon.put("content", Utils.image2String(bitmap));
                icon.put("width", bitmap.getWidth());
                icon.put("height", bitmap.getHeight());
                app.put("icon", icon);

                appArray.put(app);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject applications = new JSONObject();
        try {
            applications.put("applications", appArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "application/json", applications.toString());
    }

    private NanoHTTPD.Response runApplication(String packageName, String className) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(packageName, className));

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            return new NanoHTTPD.Response(e.toString());
        }

        return new NanoHTTPD.Response("OK");
    }

    private native int init();
    private native void destroy();
    private native int injectKey(int keyCode);

    private HttpServer mHttpServer;
    private SSDPServer mSSDPServer;
    private RootManager mRootManager;

    static {
        System.loadLibrary("uinput");
    }
}
