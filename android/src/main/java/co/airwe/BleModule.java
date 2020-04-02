package co.airwe;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.Arrays;

public class BleModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener, BeaconConsumer {

    private final String TAG = "BleModule";

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private final ReactApplicationContext reactContext;
    private BeaconManager mBeaconManager;

    public BleModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        reactContext.addLifecycleEventListener(this);
        reactContext.addActivityEventListener(this);

        mBeaconManager = BeaconManager.getInstanceForApplication(reactContext.getApplicationContext());

        mBeaconManager.bind(this);
    }

    @Override
    public String getName() {
        return "Ble";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    @Override
    public void onHostResume() {
//      if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
//        Intent enableBtIntent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
//        reactContext.startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT, new Bundle());
//      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Android M Permission check 
        if (reactContext.getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
          final AlertDialog.Builder builder = new AlertDialog.Builder(reactContext.getApplicationContext());
          builder.setTitle("This app needs location access");
          builder.setMessage("Please grant location access so this app can detect beacons.");
          builder.setPositiveButton(android.R.string.ok, null);
          builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
              getCurrentActivity().requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_COARSE_LOCATION
              );
            }
          });

          builder.show();
        }
      }
    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {

    }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {

    Log.d(TAG, "onActivityResult: requestCode: " + requestCode + " resultCode: " + resultCode);
    Log.d(TAG, data.toString());
  }

  @Override
  public void onBeaconServiceConnect() {
    mBeaconManager.removeAllMonitorNotifiers();
    mBeaconManager.addMonitorNotifier(new MonitorNotifier() {
      @Override
      public void didEnterRegion(Region region) {
        Log.i(TAG, "I just saw a beacon for the first time");
      }

      @Override
      public void didExitRegion(Region region) {
        Log.i(TAG, "I no longer see a beacon");
      }

      @Override
      public void didDetermineStateForRegion(int state, Region region) {
        Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
      }
    });

    try {
      mBeaconManager.startMonitoringBeaconsInRegion(
        new Region("airweMonitoringRegion", null, null, null);
    } catch (RemoteException e) {
      Log.e(TAG, "Exception monitoring region: " + e.getMessage());
    }


    Beacon beacon = new Beacon.Builder()
      .setId1("2f214454-cf9d-4004-adf2-f4a13bacffa4")
      .setId2("1")
      .setId3("2")
      .setManufacturer(0x0118)
      .setTxPower(-59)
      .setDataFields(Arrays.asList(new Long[]{0l}))
      .build();
    BeaconParser beaconParser = new BeaconParser()
      .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
    BeaconTransmitter beaconTransmitter = new BeaconTransmitter(reactContext.getApplicationContext(), beaconParser);
    beaconTransmitter.setAdvertiseTxPowerLevel();
    beaconTransmitter.startAdvertising(beacon);
  }

  @Override
  public Context getApplicationContext() {
    return null;
  }

  @Override
  public void unbindService(ServiceConnection serviceConnection) {
  }

  @Override
  public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
    return false;
  }
}
