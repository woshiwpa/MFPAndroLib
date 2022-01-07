package com.cyzapps.adapter;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.cyzapps.mfpanlib.R;
import com.cyzapps.mfpanlib.MFPAndroidLib;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import androidx.core.os.EnvironmentCompat;
import android.util.Log;
import android.util.Pair;

public class AndroidStorageOptions {
    public static final String LOG_TAG = "AndroidStorageOptions";

	public static final String SELECTED_STORAGE_PATH = "selected_storage_path";

    private static ArrayList<Pair<String, String>> mMounts = new ArrayList<Pair<String, String>>();
    private static ArrayList<String> mLabels = new ArrayList<String>();

    public static String[] labels;
    public static String[] mountPoints;
    public static String[] paths;
    public static int count = 0;
    
    private static String msstrSelectedStoragePath = "";

    private static String msstrDefaultStoragePath = getDefaultStorageMountPoint();
    
    private static final String TAG = AndroidStorageOptions.class.getSimpleName();

    public static String getDefaultStorageMountPoint() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static void detectDefaultStoragePath() {
        File root = new File(getDefaultStorageMountPoint());
        if (root.exists() && root.isDirectory() && !root.canWrite()) {
            // I cannot write to default storage mount point, not good
            File f = MFPAndroidLib.getContext().getExternalFilesDir(null);
            msstrDefaultStoragePath = f.getAbsolutePath();
        } else {
            msstrDefaultStoragePath = getDefaultStorageMountPoint();
        }
    }

    public static String getDefaultStoragePath( )	{
        File f = MFPAndroidLib.getContext().getExternalFilesDir(null);
        if (f == null) {
            f = Environment.getExternalStorageDirectory();  // this function will be depreciated in version 29.
        }
        msstrDefaultStoragePath = f.getAbsolutePath();
		return msstrDefaultStoragePath;
    }

    public static String getSelectedStorageMountPoint( )	{
        if (msstrSelectedStoragePath == null || msstrSelectedStoragePath.trim().length() == 0)	{
            return getDefaultStorageMountPoint();
        } else	{
            for (int idx = 0; idx < mountPoints.length; idx ++) {
                if (msstrSelectedStoragePath.equals(paths[idx])) {
                    return mountPoints[idx];    // find the selected one
                }
            }
            for (int idx = 0; idx < mountPoints.length; idx ++) {
                if (msstrSelectedStoragePath.startsWith(mountPoints[idx])) {
                    return mountPoints[idx];    // find a mount point which should be for selected storage path
                }
            }
            // nothing found. Use storage path as mount point.
            return msstrSelectedStoragePath;
        }
    }

    public static String getSelectedStoragePath( )	{
        if (msstrSelectedStoragePath == null || msstrSelectedStoragePath.trim().length() == 0)	{
            return getDefaultStoragePath();
        } else	{
            return msstrSelectedStoragePath;
        }
    }

    public static void setSelectedStoragePath(int nId)	{
    	if (nId < 0 || nId >= count)	{
            if (msstrSelectedStoragePath == null || msstrSelectedStoragePath.trim().length() == 0) {
                // if both Id and msstrSelectedStoragePath are invalid, use default storage path.
                msstrSelectedStoragePath = getDefaultStoragePath();
            }
    	} else	{
    		msstrSelectedStoragePath = paths[nId];
    	}
    }

    public static void setSelectedStoragePath(String strSelectedStoragePath)	{
   		msstrSelectedStoragePath = strSelectedStoragePath;
    }

    public static void determineStorageOptions() {
        // this can work at least up to Android 7.0
        getExternalStorageDirectories();

        testAndCleanMountsList();

        setProperties();
    }

    private static void testAndCleanMountsList() {
        /*
         * Now that we have a cleaned list of mount paths Test each one to make
         * sure it's a valid and available path. If it is not, remove it from
         * the list.
         */
        for (int i = 0; i < mMounts.size(); i++) {
            // "normal external storage
            String mount = mMounts.get(i).second;
            File root = new File(mount);
            if (!root.exists() || !root.isDirectory() || !root.canWrite())
                mMounts.remove(i--);
        }
    }

    @SuppressLint("NewApi")
	@SuppressWarnings("unchecked")
    private static void setProperties() {
        /*
         * At this point all the paths in the list should be valid. Build the
         * public properties.
         */
        mLabels = new ArrayList<String>();

        int j = 0;
        if (mMounts.size() > 0) {
            if (mMounts.get(0).first.equals(getDefaultStorageMountPoint())  // this is required because default storage may not have write permission and is removed.
                        && (!Environment.isExternalStorageRemovable() || Environment.isExternalStorageEmulated()))
                mLabels.add(MFPAndroidLib.getContext().getString(R.string.internal_storage));
            else {
                mLabels.add(MFPAndroidLib.getContext().getString(R.string.external_sd_card) + " 1");
                j = 1;
            }

            if (mMounts.size() > 1) {
                for (int i = 1; i < mMounts.size(); i++) {
                    mLabels.add(MFPAndroidLib.getContext().getString(R.string.external_sd_card)  + (i + j));
                }
            }
        }

        labels = new String[mLabels.size()];
        mLabels.toArray(labels);

        mountPoints = new String[mMounts.size()];
        paths = new String[mMounts.size()];
        for (int idx = 0; idx < mMounts.size(); idx ++) {
            mountPoints[idx] = mMounts.get(idx).first;
            paths[idx] = mMounts.get(idx).second;
        }
        count = Math.min(labels.length, paths.length);

    }

    /* returns external storage paths (directory of external memory card) as array of Strings */
    public static void getExternalStorageDirectories() {
        mMounts.clear();
        mLabels.clear();

        // insert default storage path first. This is actually internal memory. It can be read/write at root folder.
        mMounts.add(new Pair<String, String>(getDefaultStorageMountPoint(), getDefaultStoragePath()));

        List<Pair<String, String>> results = new ArrayList<Pair<String, String>>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = MFPAndroidLib.getContext().getExternalFilesDirs(null);
            String internalRoot = getDefaultStorageMountPoint().toLowerCase();

            for (File file : externalDirs) {
                if(file==null) //solved NPE on some Lollipop devices
                    continue;
                String path = file.getAbsolutePath().split("/Android")[0];

                if(path.toLowerCase().startsWith(internalRoot))
                    continue;

                boolean addPath = false;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addPath = Environment.isExternalStorageRemovable(file);
                } else{
                    addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
                }

                if(addPath){
                    results.add(new Pair<String, String>(path, file.getAbsolutePath()));
                }
            }
        }

        if(results.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            String output = "";
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if(!output.trim().isEmpty()) {
                String devicePoints[] = output.split("\n");
                for(String voldPoint: devicePoints) {
                    String[] voldPointParts = voldPoint.split(" ");
                    if (voldPointParts.length > 2 && !voldPointParts[2].equals(getDefaultStorageMountPoint())) {
                        // add to result if it is not default storage
                        results.add(new Pair<String, String>(voldPointParts[2], voldPointParts[2]));
                    }
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).first.toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    Log.d(LOG_TAG, results.get(i) + " might not be extSDcard");
                    results.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).first.toLowerCase().contains("ext") && !results.get(i).first.toLowerCase().contains("sdcard")) {
                    Log.d(LOG_TAG, results.get(i)+" might not be extSDcard");
                    results.remove(i--);
                }
            }
        }
        // from SDK 24, Android doesn't support access removable storage via FileProvider
        // to make life easy, I always store things in internal storage
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mMounts.addAll(results);
        }
    }
}