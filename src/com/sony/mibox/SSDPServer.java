package com.sony.mibox;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.*;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDN;

public class SSDPServer {

    private UDN udn = UDN.uniqueSystemIdentifier("Demo MiBox Controller");

    public SSDPServer(Context context) {
        mContext = context;
    }

    public void start() {
        Intent intent = new Intent(mContext, AndroidUpnpServiceImpl.class);
        mContext.getApplicationContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void stop() {
        mContext.getApplicationContext().unbindService(mServiceConnection);
    }

    private LocalDevice createDevice() throws ValidationException {
        DeviceType type = new UDADeviceType("MiBoxService", 1);

        DeviceDetails details = new DeviceDetails("MiBox Service",
                new ManufacturerDetails("SONY"),
                new ModelDetails("MiBox Service", "A service to let remote client to control MiBox"));

        return new LocalDevice(new DeviceIdentity(udn), type, details, (LocalService)null);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mUpnpService = (AndroidUpnpService)iBinder;
            try {
                LocalDevice device = createDevice();
                mUpnpService.getRegistry().addDevice(device);
            } catch (ValidationException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mUpnpService = null;
        }
    };

    private Context mContext;
    private AndroidUpnpService mUpnpService;
}
