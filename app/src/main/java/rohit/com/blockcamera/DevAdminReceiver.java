package rohit.com.blockcamera;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DevAdminReceiver extends DeviceAdminReceiver{
    @Override
    public void onEnabled(Context context, Intent intent) {
        if (MainActivity.staticMainActivity != null) {
            MainActivity.staticMainActivity.recreate();
        }
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.admin_receiver_status_disable_warning);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        if (MainActivity.staticMainActivity != null) {
            MainActivity.staticMainActivity.recreate();
        }
    }
}
