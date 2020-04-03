package co.airwe;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;


public class BleModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener, BeaconConsumer{

    private final String TAG = "BleModule";

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    private static final int BLE_SCAN_PERIOD = 1100; // 1.1s
    private static final int BLE_SCAN_FREQUENCY = 60000; // 1min
    private static final String BLE_REGION_ID = "co.airwe.region";


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
    public void startScanning() {
      Notification.Builder builder = new Notification.Builder(reactContext);

      builder.setSmallIcon(android.R.mipmap.ic_launcher);
      builder.setContentTitle("Scanning for Beacons");

      //Intent intent = new Intent(this, getCurrentActivity().getClass());
      String pn = getReactApplicationContext().getApplicationContext().getPackageName();
      Intent intent = reactContext.getPackageManager().getLaunchIntentForPackage(pn);
      PendingIntent pendingIntent = PendingIntent.getActivity(
        reactContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
      );
      builder.setContentIntent(pendingIntent);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
          "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("My Notification Channel Description");
        NotificationManager notificationManager = (NotificationManager) reactContext.getSystemService(
          Context.NOTIFICATION_SERVICE
        );
        notificationManager.createNotificationChannel(channel);
        builder.setChannelId(channel.getId());
      }

      mBeaconManager.enableForegroundServiceScanning(builder.build(), 456);
      // For the above foreground scanning service to be useful, you need to disable
      // JobScheduler-based scans (used on Android 8+) and set a fast background scan
      // cycle that would otherwise be disallowed by the operating system.
      //
      mBeaconManager.setEnableScheduledScanJobs(false);
      mBeaconManager.setBackgroundBetweenScanPeriod(BLE_SCAN_FREQUENCY);
      mBeaconManager.setBackgroundScanPeriod(BLE_SCAN_PERIOD);

      try {
        mBeaconManager.startMonitoringBeaconsInRegion(new Region(BLE_REGION_ID, null, null, null));
      } catch (RemoteException e) {
        Log.e(TAG, "Error trying to monitor Region: " + BLE_REGION_ID);
        e.printStackTrace();
      }

    }


    @ReactMethod
    public void stopScanning() {
      try {
        mBeaconManager.stopMonitoringBeaconsInRegion(new Region(BLE_REGION_ID, null, null, null));
      } catch (RemoteException e) {
        Log.e(TAG, "Error trying to stop monitoring Region: " + BLE_REGION_ID);
        e.printStackTrace();
      }
    }

    @Override
    public void onHostResume() {

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
                PERMISSION_REQUEST_FINE_LOCATION
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
