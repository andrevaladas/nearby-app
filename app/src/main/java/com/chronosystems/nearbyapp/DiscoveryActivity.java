package com.chronosystems.nearbyapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.chronosystems.nearbyapp.fragments.DiscoveryFragment;
import com.chronosystems.nearbyapp.utils.AppConstants;

public class DiscoveryActivity extends AppCompatActivity {

    private static final String DISCOVERY_FRAGMENT_TAG = "discovery_fragment_tag";

    private Toolbar mToolbar;
    private DiscoveryFragment mDiscoveryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        mToolbar = (Toolbar) findViewById(R.id.tb_discovery);
        setSupportActionBar(mToolbar);

        mToolbar.setTitle("Discovery Activity");
        mToolbar.setSubtitle("Discovery nearby users");
        //mToolbar.setLogo(R.drawable.ic_nearby_app);
        mToolbar.setNavigationIcon(R.drawable.ic_nearby_app);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDiscoveryFragment.cancelDiscovery();
                onBackPressed();
            }
        });

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // FRAGMENT
        mDiscoveryFragment = (DiscoveryFragment) getSupportFragmentManager().findFragmentByTag(DISCOVERY_FRAGMENT_TAG);
        if (mDiscoveryFragment == null) {
            mDiscoveryFragment = new DiscoveryFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.rl_fragment_container, mDiscoveryFragment, DISCOVERY_FRAGMENT_TAG);
            ft.commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == AppConstants.ACTIVITY.REQUEST_RESULT_CODE) {
            if (RESULT_OK == resultCode) {
                mDiscoveryFragment.onDestroy();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        mDiscoveryFragment.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        mDiscoveryFragment.onDestroy();
        super.onBackPressed();
    }
}