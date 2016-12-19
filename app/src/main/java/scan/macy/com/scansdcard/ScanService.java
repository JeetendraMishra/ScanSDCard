package scan.macy.com.scansdcard;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Jeetendra Kumar Mishra on 12/16/16.
 */

public class ScanService extends Service {

    private String TAG = ScanSDCardActivity.class.getName();
    private ScanTask scanTask;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand ScanService");
        scanTask = new ScanTask(getApplicationContext());
        scanTask.execute("");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy ScanService");
        scanTask.cancel(true);
        super.onDestroy();
    }
}
