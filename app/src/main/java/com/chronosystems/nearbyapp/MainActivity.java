package com.chronosystems.nearbyapp;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.chronosystems.nearbyapp.utils.AppActivity;
import com.chronosystems.nearbyapp.helper.QuickPrefsHelper;
import com.chronosystems.nearbyapp.services.NearbyBroadcastService;
import com.chronosystems.nearbyapp.services.NearbyMessagesClient;
import com.chronosystems.nearbyapp.utils.AppConstants;
import com.chronosystems.nearbyapp.utils.AppSystem;
import com.google.android.gms.common.api.Status;

/**
 * Main Activity
 *
 * @author andrevaladas
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private NearbyMessagesClient mNearbyMessagesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO JUST FOR TESTING
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //keep always on

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_discovery_view);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // go to discovery activity
                AppActivity.go(MainActivity.this, DiscoveryActivity.class);

            }
        });

        final Switch broadcastSwitch = (Switch) findViewById(R.id.switch_broadcast);
        broadcastSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getApplicationContext(), "Service: " + broadcastSwitch.getTextOn(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Service: " + broadcastSwitch.getTextOff(), Toast.LENGTH_SHORT).show();
                }
                QuickPrefsHelper.update(getApplicationContext(), AppConstants.PREF.AUTO_BROADCAST_SWITCH, isChecked);
            }
        });

        findViewById(R.id.bt_my_endpoint_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String myEndpointId = QuickPrefsHelper.get(getBaseContext(), AppConstants.NEARBY.MY_ENDPOINT_ID, "---");
                Toast.makeText(getApplicationContext(), myEndpointId, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mNearbyMessagesClient = new NearbyMessagesClient(this, new NearbyMessagesClient.NearbyPermissionListener() {
            @Override
            public void onServiceConnected() {
                runBroadcastService();
            }

            @Override
            public void onResolvingNearbyPermissionError(Status status) {
                try {
                    status.startResolutionForResult(MainActivity.this, AppConstants.NEARBY.REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    Log.w(TAG, "onResolvingNearbyPermissionError error: " + e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }

    private void runBroadcastService() {
        if (!AppSystem.isServiceRunning(MainActivity.this, NearbyBroadcastService.class)) {
            startService(new Intent(MainActivity.this, NearbyBroadcastService.class));
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, NearbyBroadcastService.class));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mNearbyMessagesClient.finishedResolvingNearbyPermissionError();
        if (requestCode == AppConstants.NEARBY.REQUEST_RESOLVE_ERROR) {
            // User was presented with the Nearby opt-in dialog and pressed "Allow".
            if (resultCode == Activity.RESULT_OK) {
                //PERMISSION OK, try connect again
                runBroadcastService();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User was presented with the Nearby opt-in dialog and pressed "Deny". We cannot
                // proceed with any pending subscription and publication tasks. Reset state.
                Log.w(TAG, "PERMISSION CANCELED");
                finish();
            } else {
                Toast.makeText(this, "Failed to resolve error with code " + resultCode, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
