package scan.macy.com.scansdcard;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Jeetendra Kumar Mishra on 12/16/16.
 */
public class ScanSDCardActivity extends AppCompatActivity {

    private ToggleButton button;
    private String TAG = ScanSDCardActivity.class.getName();
    private Intent scanServiceIntent;
    private Button share;
    private TextView displayStatistics;
    private EventBus mEventBus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_sdcard);

        button = (ToggleButton) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((ToggleButton) view).isChecked()){
                    // start scaning
                    Log.i(TAG, "start scanning");
                    scanServiceIntent = new Intent(ScanSDCardActivity.this, ScanService.class);
                    startService(scanServiceIntent);
                } else {
                    if (scanServiceIntent != null) {
                        // stop scanning
                        Log.i(TAG, "stop scanning");
                        stopService(scanServiceIntent);
                    }
                }
            }
        });

        share = (Button) findViewById(R.id.button2);
        share.setVisibility(View.INVISIBLE);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                File fileWithinMyDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/scanResult/scanResult.txt");

                if(fileWithinMyDir.exists()) {
                    Log.i(TAG, "sharing scanning result : "+fileWithinMyDir.getAbsolutePath());
                    intentShareFile.setType("application/pdf");
                    intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+fileWithinMyDir.getAbsolutePath()));

                    intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                            "Sharing File...");
                    intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

                    startActivity(Intent.createChooser(intentShareFile, "Share File"));
                } else {
                    Log.i(TAG, "sharing scanning result not found");
                }
            }
        });

        displayStatistics = (TextView) findViewById(R.id.textView2);
        displayStatistics.setText("");
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed ScanSDCardActivity");
        stopService(scanServiceIntent);
        super.onBackPressed();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEventBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mEventBus.unregister(this);
    }

    @Subscribe
    public void onRestoreUndoAction(UpdatedResultEvent event){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Name and size of 10 biggest files: \n\n");
        List<File> files = event.getResult();
        Log.i(TAG, "file scan size result : "+files.size());
        if (files != null && files.size() > 0) {
            long averageFileSize = 0;
            Map<String, Long> fileSizes = new HashMap<String, Long>();
            Map<String,Long> fileExtensions = new HashMap<String,Long>();
            for(File file : files){
                if(file != null){
                    averageFileSize += file.length();

                    String filename = file.getName();
                    String filenameArray[] = filename.split("\\.");
                    String extension = filenameArray[filenameArray.length-1];
                    Log.i(TAG,"extension : "+extension);
                    long count = 1;
                    if (fileExtensions.get(extension) != null) {
                        count += fileExtensions.get(extension);
                    }
                    fileExtensions.put(extension,count);

                    Log.i(TAG, "file name : "+file.getName()+", and file size: "+file.length());
                    if(fileSizes.keySet().size() <= 9){
                        fileSizes.put(file.getName(),file.length());
                    } else {
                        Set<String> keys = fileSizes.keySet();
                        if(keys != null && keys.size() == 10){
                            String minKey = keys.toArray()[0].toString();
                            long minSize = fileSizes.get(keys.toArray()[0]);
                            for(String key : keys){
                                long size = fileSizes.get(key);
                                if (minSize > size) {
                                    minSize = size;
                                    minKey = key;
                                }
                            }
                            if (file.getUsableSpace() > minSize) {
                                fileSizes.remove(minKey);
                                fileSizes.put(file.getName(),file.length());
                            }
                        }
                    }

                }
            }
            Log.i(TAG, "filtered 10 result : "+fileSizes.size());
            Set<String> keys = fileSizes.keySet();
            for(String key : keys){
                Log.i(TAG, "file name: "+key+", size: "+fileSizes.get(key));
                stringBuilder.append("file name: "+key+", size: "+fileSizes.get(key)+" bytes\n");
            }
            Log.i(TAG,"averageFileSize : "+averageFileSize/files.size());
            stringBuilder.append("\n\nAverage File Size: "+averageFileSize/files.size()+" bytes\n\n");
            stringBuilder.append("5 most frequent file extensions with their frequencies: \n\n");
            keys = fileExtensions.keySet();
            for(String key : keys){
                Log.i(TAG, "file extension: "+key+", count: "+fileExtensions.get(key));
            }

            Map<String,Long> fileExtensionsSort = new HashMap<String,Long>();

            for(String extension : fileExtensions.keySet()){
                if(fileExtensionsSort.keySet().size() <= 4){
                    fileExtensionsSort.put(extension,fileExtensions.get(extension));
                } else {
                    Set<String> keys2 = fileExtensionsSort.keySet();
                    if(keys2 != null && keys2.size() == 5){
                        String minKey = keys2.toArray()[0].toString();
                        long minSize = fileExtensionsSort.get(keys2.toArray()[0].toString());
                        for(String key : keys2){
                            long size = fileExtensionsSort.get(key);
                            if (minSize > size) {
                                minSize = size;
                                minKey = key;
                            }
                        }
                        if (fileExtensions.get(extension) > minSize) {
                            fileExtensionsSort.remove(minKey);
                            fileExtensionsSort.put(extension,fileExtensions.get(extension));
                        }
                    }
                }
            }
            Log.i(TAG,"sorted extension with count : "+fileExtensionsSort.size());
            keys = fileExtensionsSort.keySet();
            for(String key : keys){
                Log.i(TAG, "file extension sorted : "+key+", count: "+fileExtensionsSort.get(key));
                stringBuilder.append("file extension: "+key+", frequencies: "+fileExtensionsSort.get(key)+"\n");
            }

        }

        // writing statistics data to file for sharing
        File root = Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/scanResult");
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir, "scanResult.txt");

        try {
            FileOutputStream f = new FileOutputStream(file);
            f.write(stringBuilder.toString().getBytes());
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        share.setVisibility(View.VISIBLE);
        displayStatistics.setText(stringBuilder.toString());
        button.setChecked(false);
    }
}
