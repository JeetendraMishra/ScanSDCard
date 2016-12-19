package scan.macy.com.scansdcard;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeetendra Kumar Mishra on 12/16/16.
 */

public class ScanTask extends AsyncTask<String, String, String> {

    private String TAG = ScanSDCardActivity.class.getName();
    List<File> files = new ArrayList<File>();
    private ProgressDialog mProgressDialog;
    private Context activity;

    public ScanTask(Context activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mProgressDialog.setMessage("Progress start");
        mProgressDialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        Log.i(TAG, "doInBackground ScanTask");

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.i(TAG, "sdcard mounted");
            String sdCardState = Environment.getExternalStorageState();
            if( !sdCardState.equals(Environment.MEDIA_MOUNTED ) ) {
                Log.i(TAG, "no sd card");

            } else {
                File root = Environment.getExternalStorageDirectory();
                extractAllFiles(root);
            }

            Log.i(TAG, "total file : "+files.size());
        }else {
            Log.i(TAG, "sdcard not mounted");
        }

        return null;
    }

    public void extractAllFiles(File file) {
        if (!isCancelled()) {
            publishProgress(file.getAbsolutePath());

            if( file.isDirectory() ) {
                Log.i(TAG, "file is directory : "+file.getAbsolutePath());
                String[] filesAndDirectories = file.list();
                if(filesAndDirectories != null && filesAndDirectories.length > 0){
                    for( String fileOrDirectory : filesAndDirectories) {
                        File f = new File(file.getAbsolutePath() + "/" + fileOrDirectory);
                        extractAllFiles(f);
                    }
                }
            } else {
                Log.i(TAG, "file is not directory : "+file.getAbsolutePath());
                files.add(file);
            }
        } else {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    protected void onProgressUpdate(String... progress) {
        if(mProgressDialog == null){
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.show();
        }
        mProgressDialog.setMessage("Scanning "+progress[0]+"..");
    }

    @Override
    protected void onPostExecute(String result) {

        EventBus.getDefault().post(new UpdatedResultEvent(files));

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}
