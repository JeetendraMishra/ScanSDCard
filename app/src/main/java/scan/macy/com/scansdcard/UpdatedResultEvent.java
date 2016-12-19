package scan.macy.com.scansdcard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeetendra Kumar Mishra on 12/16/16.
 */

public class UpdatedResultEvent {
    private List<File> mFiles = new ArrayList<File>();

    UpdatedResultEvent(List<File> files){
        mFiles = files;
    }
    public List<File> getResult(){
        return mFiles;
    }
}
