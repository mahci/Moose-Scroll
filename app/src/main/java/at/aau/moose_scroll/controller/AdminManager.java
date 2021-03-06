package at.aau.moose_scroll.controller;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import at.aau.moose_scroll.R;
import at.aau.moose_scroll.views.MainActivity;

public class AdminManager extends DeviceAdminReceiver {

    void showToast(Context context, String msg) {
        String status = context.getString(R.string.admin_receiver_status);
        Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_DEVICE_ADMIN_DISABLE_REQUESTED)) {
            abortBroadcast();
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_enabled));
        MainActivity.setIsAdim(true);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.admin_receiver_status_disable_warning);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_disabled));
    }
}