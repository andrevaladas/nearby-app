package com.chronosystems.nearbyapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chronosystems.nearbyapp.utils.AppActivity;
import com.chronosystems.nearbyapp.utils.AppConstants;

/**
 * Device Request Activity Modal
 *
 * @author andrevaladas
 */
public class RequestActivity extends AppCompatActivity {

    private String mRemoteName;
    private Bitmap mRemoteProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_modal);
        setFinishOnTouchOutside(false);// disable back pressed

        mRemoteName = getIntent().getStringExtra(AppConstants.EXTRA.DEVICE_NAME);
        mRemoteProfile = getIntent().getParcelableExtra(AppConstants.EXTRA.DEVICE_PROFILE);

        final ImageView mUserImage = (ImageView) findViewById(R.id.iv_request_profile);
        final TextView mUserName = (TextView) findViewById(R.id.tv_request_name);
        final Button mBtnReject = (Button) findViewById(R.id.bt_request_cancel_action);
        final Button mBtnAccept = (Button) findViewById(R.id.bt_request_confirm_action);

        mUserName.setText(mRemoteName);
        mUserImage.setImageBitmap(mRemoteProfile);

        mBtnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Reject Action", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        mBtnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AppActivity.go(RequestActivity.this,
                        RequestLoaderActivity.class,
                        getIntent().getExtras(),
                        AppConstants.ACTIVITY.REQUEST_RESULT_CODE, 1,
                        true);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == AppConstants.ACTIVITY.REQUEST_RESULT_CODE) {
            setResult(resultCode);
            if (RESULT_OK == resultCode) {
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        //disable back pressed
    }
}