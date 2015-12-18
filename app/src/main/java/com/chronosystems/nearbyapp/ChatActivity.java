package com.chronosystems.nearbyapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.chronosystems.nearbyapp.conversation.Conversation;
import com.chronosystems.nearbyapp.conversation.ConversationManager;
import com.chronosystems.nearbyapp.domain.DeviceUser;
import com.chronosystems.nearbyapp.domain.builders.NearbyDeviceBuilder;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;
import com.chronosystems.nearbyapp.domain.wrappers.NearbyDeviceWrapper;
import com.chronosystems.nearbyapp.fragments.ChatFragment;
import com.chronosystems.nearbyapp.model.DeviceUserService;
import com.chronosystems.nearbyapp.utils.AppConstants;

/**
 * Chat Activity
 *
 * @author andrevaladas
 */
public class ChatActivity extends AppCompatActivity {

    private static final String CHAT_FRAGMENT_TAG = "chat_fragment_tag";

    private ChatFragment mChatFragment;
    private NearbyDeviceWrapper mRemoteDeviceWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //configure keyboard after to initialize maximized
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //keep always on
        getWindow().setBackgroundDrawableResource(R.drawable.bg_chat); //set background image without stretch it ;)

        final Toolbar toolbar = (Toolbar) findViewById(R.id.tb_chat);
        setSupportActionBar(toolbar);

        final LayoutInflater mInflater = LayoutInflater.from(this);
        final View mCustomView = mInflater.inflate(R.layout.actionbar_chat, null);
        final TextView tvName = (TextView) mCustomView.findViewById(R.id.tv_actionbar_chat_name);
        final ImageView ivProfile = (ImageView) mCustomView.findViewById(R.id.iv_action_bar_chat_profile);

        getSupportActionBar().setCustomView(mCustomView);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // load device user data
        loadDeviceUserData();

        tvName.setText(mRemoteDeviceWrapper.getParent().getName());
        ivProfile.setImageBitmap(mRemoteDeviceWrapper.getProfileBitmap());

        // FRAGMENT
        mChatFragment = (ChatFragment) getSupportFragmentManager().findFragmentByTag(CHAT_FRAGMENT_TAG);
        if (mChatFragment == null) {
            mChatFragment = new ChatFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.rl_fragment_container, mChatFragment, CHAT_FRAGMENT_TAG);
            ft.commit();
        }
    }

    private void loadDeviceUserData() {

        final String deviceId = getIntent().getStringExtra(AppConstants.EXTRA.DEVICE_ID);
        final Bitmap profile = getIntent().getParcelableExtra(AppConstants.EXTRA.DEVICE_PROFILE);

        Conversation conversation = ConversationManager.get(deviceId);
        if (conversation == null) {

            final String endpointId = getIntent().getStringExtra(AppConstants.EXTRA.DEVICE_ENDPOINT);
            final String bluetooth = getIntent().getStringExtra(AppConstants.EXTRA.DEVICE_BLUETOOTH);
            final String name = getIntent().getStringExtra(AppConstants.EXTRA.DEVICE_NAME);

            NearbyDevice nearbyDevice = NearbyDeviceBuilder.with(getApplicationContext())
                    .deviceId(deviceId)
                    .endpointId(endpointId)
                    .bluetooth(bluetooth)
                    .name(name)
                    .build();
            conversation = ConversationManager.register(nearbyDevice);
        }

        mRemoteDeviceWrapper = new NearbyDeviceWrapper(conversation.getDevice());
        mRemoteDeviceWrapper.setProfileBitmap(profile);

        //TODO load (history messages) data from database
        final DeviceUser deviceUser = DeviceUserService
                .with(this)
                .deviceId(deviceId)
                .load();
        mRemoteDeviceWrapper.setDeviceUser(deviceUser);
    }

    public NearbyDeviceWrapper getRemoteDeviceWrapper() {
        return mRemoteDeviceWrapper;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //NotificationAlertEvent.clearNotifications(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}