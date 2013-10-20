package com.sony.mibox;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import fi.iki.elonen.NanoHTTPD;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity implements HttpServer.OnRequestListener {
    public static String TAG = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mHttpServer = new HttpServer(8080);
        mHttpServer.setOnRequestListener(this);
        mSSDPServer = new SSDPServer(this);
        try {
            mHttpServer.start();
            mSSDPServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHttpServer != null) {
            mHttpServer.stop();
            mHttpServer = null;
        }

        if (mSSDPServer != null) {
            mSSDPServer.stop();
        }
    }

    @Override
    public NanoHTTPD.Response onRequest(String action) {
        if (action.equals("applist")) {
            return getAppList();
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

        return new NanoHTTPD.Response(applications.toString());
    }

    private HttpServer mHttpServer;
    private SSDPServer mSSDPServer;
}
