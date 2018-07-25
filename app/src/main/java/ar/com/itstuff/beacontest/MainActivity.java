package ar.com.itstuff.beacontest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.RemoteException;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    protected static final String TAG = "MainActivity";
    private BeaconManager beaconManager;
    private TextView beaconsDetected;
    private TextView beaconDistance;
    private TextView textLog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        // Detect the main identifier (UID) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        // Detect the telemetry (TLM) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        // Detect the URL frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));

        beaconManager.getBeaconParsers().add(new BeaconParser()
                 .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);

        beaconsDetected = (TextView)findViewById(R.id.tvBeaconsDetected);
        beaconDistance = (TextView)findViewById(R.id.tvBeaconDistance);
        textLog = (TextView)findViewById(R.id.tvLogs);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.d(TAG,"onBeaconServiceConnect");
        // Identifier myBeaconNamespaceId = Identifier.parse("0x2f234454f4911ba9ffa6");
        // Identifier myBeaconInstanceId = Identifier.parse("0x000000000001");
        Identifier myBeaconNamespaceId = null;
        Identifier myBeaconInstanceId = null;

        final Region region = new Region("MyBeacons", myBeaconNamespaceId, myBeaconInstanceId,null);

        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.d(TAG,"didEnterRegion");
                try {
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                Log.d(TAG,"didExitRegion");
                try {
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                final String beaconsFound = Integer.toString(state);
                Log.d(TAG, "I have just switched from seeing/not seeing beacons: "+beaconsFound);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        beaconsDetected.setText(beaconsFound);
                    }
                });

            }
        });
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    final double distance = beacons.iterator().next().getDistance();
                    Log.d(TAG, "The first beacon I see is about "+distance+" meters away.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            beaconDistance.setText(Double.toString(distance));
                        }
                    });
                }
            }
        });
        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
        }
        catch (RemoteException e) {
            Log.d(TAG,"Error Starting Monitoring");
            e.printStackTrace();
        }

    }
    @UiThread
    void updateText(String message) {
        beaconsDetected.setText(message);
    }
}
