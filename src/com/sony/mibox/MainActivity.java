package com.sony.mibox;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.nio.ByteBuffer;
import java.util.List;

public class MainActivity extends Activity {
    public static String TAG = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Get all installed applications.
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> appList = pm.queryIntentActivities(intent, 0);

        Bitmap bitmap =((BitmapDrawable)appList.get(0).loadIcon(pm)).getBitmap();

        String imageString = image2String(bitmap);
        Bitmap imageBitmap = string2Image(imageString, bitmap.getWidth(), bitmap.getHeight());

        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(imageBitmap);
    }

    public String image2String(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(buffer);
        return Base64.encodeToString(buffer.array(), Base64.DEFAULT);
    }

    public Bitmap string2Image(String imageStringData, int width, int height) {
        byte[] bytes = Base64.decode(imageStringData, Base64.DEFAULT);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }
}
