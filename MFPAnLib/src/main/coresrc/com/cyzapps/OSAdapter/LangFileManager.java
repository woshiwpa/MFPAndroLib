// MFP project, LangFileManager.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.OSAdapter;

import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.IOLib;
import com.cyzapps.Jmfp.CompileAdditionalInfo;
import com.cyzapps.OSAdapter.ParallelManager.CallObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class LangFileManager {
    

    public static final String STRING_ASSET_SCRIPT_LIB_FOLDER_EXTENSION = "_lib";
    public static final String STRING_ASSET_JNI_BINS_FOLDER = "jni";
    public static final String STRING_ASSET_JNI64_BINS_FOLDER = "jni64";
    public static final String STRING_ASSET_SCRIPT_LIB_FOLDER = "predef_lib";
    public static final String STRING_ASSET_MANUAL_SCRIPTS_FOLDER = "manual_scripts";
    public static final String STRING_ASSET_SCRIPT_MATH_LIB_FILE = "math.mfps";
    public static final String STRING_ASSET_SCRIPT_MISC_LIB_FILE = "misc.mfps";
    public static final String STRING_ASSET_SCRIPT_SIG_PROC_LIB_FILE = "sig_proc.mfps";
    public static final String STRING_ASSET_CHARTS_FOLDER_EXTENSION = "_lib";
    public static final String STRING_ASSET_CHARTS_FOLDER = "charts_lib";
    public static final String STRING_ASSET_CHART_EXAMPLE1_FILE = "chart_example1.mfpc";
    public static final String STRING_ASSET_CHART_EXAMPLE2_FILE = "chart_example2.mfpc";
    public static final String STRING_ASSET_ZIP_FILE = "assets.7z";
    public static final String STRING_ASSET_MANUAL_PDF_FILE = "manual.pdf";
    public static final String STRING_ASSET_MFP_APP_ZIP_FILE = "AnMFP.zip";
    public static final String STRING_ASSET_JMATHCMD_JAR_FILE = "JMFPLang.jar";
    public static final String STRING_ASSET_JMFPLANG_JAR_FILE = "JMFPLang.jar";
    public static final String STRING_ASSET_MFPLANG_CMD_FILE = "mfplang.cmd";
    public static final String STRING_ASSET_MFPLANG_SH_FILE = "mfplang.sh";
    public static final String STRING_ASSET_INTERNAL_FUNC_INFO_FILE = "InternalFuncInfo.txt";
    public static final String STRING_ASSET_ENGLISH_LANG_FOLDER = "en"; 
    public static final String STRING_ASSET_SCHINESE_LANG_FOLDER = "zh-CN"; 
    public static final String STRING_ASSET_TCHINESE_LANG_FOLDER = "zh-TW";
    public static final String STRING_ASSET_LANGUAGEINFO_FOLDER = "LanguageInfo";
    public static final String STRING_ASSET_MFP_KEY_WORDS_INFO_FILE = "MFPKeyWordsInfo.txt";

    public static final String STRING_COMMAND_INPUT_FILE_PATH = "command_input";
    public static final String STRING_PATH_DIVISOR = System.getProperty("file.separator");
    public static final String STRING_EXTENSION_INITIAL = ".";
    public static final String STRING_ASSETS_FOLDER = "assets";  // assets folder.
    public static final String STRING_DEFAULT_SCRIPT_FOLDER = "scripts";
    public static final String STRING_EXAMPLE_FOLDER = "examples";
    public static final String STRING_DEFAULT_CHART_FOLDER = "charts";
    public static final String STRING_SCRIPT_EXTENSION = ".mfps";
    public static final String STRING_CHART_EXTENSION = ".mfpc";
    
    public static final String STRING_SANDBOX_RESOURCE_FOLDER = "resource";
    
    public static String msstrScriptFolder = "";
    public static String msstrChartFolder = "";
    
    public static Boolean mbOutputCSDebugInfo = true;

    public abstract String[] list(String strFolder, boolean bAndroidAsset) throws IOException;    // list all the files in the folder.
    
    public abstract InputStream open(String fileName, boolean bAndroidAsset) throws IOException;    // open an asset file in Android.
    
    public abstract InputStream openZippedFileEntry(
    		String zipFileName,
    		String zippedFileEntryPath,
    		boolean bAndroidAsset
    		) throws IOException;    // open file entry in a zipped asset file in Android.
    
    public abstract boolean isDirectory(String strPath, boolean bAndroidAsset);    // identify if the path is a directory.
    
    public abstract Boolean isOnAndroid();    // am I running on android?
    
    public abstract Boolean isMFPApp(); // am I an mfp app?
    
    public Boolean isSandBoxSession() {   // am I a call session?
        Long thisThreadId = Thread.currentThread().getId();
        return CallObject.msmapThreadId2SessionInfo.containsKey(thisThreadId);
    }
    
    public abstract String getAssetFilePath(String assetFile);	// get asset file path.
    
    public abstract String getAppBaseFullPath();
    
    // Override it in AnMFP!
    public byte[] readFileToBytes(String pathSrc) throws JFCALCExpErrException {
        return IOLib.readFileToBytes(pathSrc);
    }
    
    // Override it in AnMFP!
    public byte[] readFileToBytes(SourceDestPathMapInfo fileInfo) throws JFCALCExpErrException {
        if (fileInfo.getSrcPath().length() > 0) {
            return IOLib.readFileToBytes(fileInfo.getSrcPath());
        } else if (fileInfo.mnSrcZipType != 1) {
            InputStream is = null;
            ByteArrayOutputStream os = null;
            try {
                // normal zip file
                is = openZippedFileEntry(fileInfo.mstrSrcZipPath, fileInfo.mstrSrcZipEntry, false);
                os = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                return os.toByteArray();
            } catch (IOException ex) {
                Logger.getLogger(LangFileManager.class.getName()).log(Level.SEVERE, null, ex);
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_FILE);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch(IOException e) {
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch(IOException e) {
                    }
                }
            }
        } else {
            throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_FILE_TYPE);
        }
    }
    
    // Override it in AnMFP!
    public InputStream readFileToInputStream(SourceDestPathMapInfo fileInfo) throws JFCALCExpErrException {
        if (fileInfo.getSrcPath().length() > 0) {
            try {
                return new FileInputStream(fileInfo.getSrcPath());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LangFileManager.class.getName()).log(Level.SEVERE, null, ex);
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FILE_NOT_FOUND);
            }
        } else if (fileInfo.mnSrcZipType != 1) {
            try {
                return openZippedFileEntry(fileInfo.mstrSrcZipPath, fileInfo.mstrSrcZipEntry, false);
            } catch (IOException ex) {
                Logger.getLogger(LangFileManager.class.getName()).log(Level.SEVERE, null, ex);
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_FILE);
            }
        } else {
            throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_FILE_TYPE);
        }
    }    
    
    
    public String getPreDefLibFullPath() {
        if (isOnAndroid()) {
            return STRING_ASSET_SCRIPT_LIB_FOLDER;
        } else {
            return getAppBaseFullPath() + STRING_PATH_DIVISOR + STRING_ASSETS_FOLDER
                    + STRING_PATH_DIVISOR + STRING_ASSET_SCRIPT_LIB_FOLDER;
        }
    }
    
    public String getAssetFolderFullPath()   {
        return getAppBaseFullPath() + STRING_PATH_DIVISOR + STRING_ASSETS_FOLDER;
    }
    
    public String getAssetJNIFolderFullPath()    {
        return getAppBaseFullPath() + STRING_PATH_DIVISOR + STRING_ASSETS_FOLDER
                + STRING_PATH_DIVISOR + STRING_ASSET_JNI_BINS_FOLDER;
    }
    
    public String getAssetJNI64FolderFullPath()    {
        return getAppBaseFullPath() + STRING_PATH_DIVISOR + STRING_ASSETS_FOLDER
                + STRING_PATH_DIVISOR + STRING_ASSET_JNI64_BINS_FOLDER;
    }
    
    public String getAssetLibFolderFullPath()    {
        return getAppBaseFullPath() + STRING_PATH_DIVISOR + STRING_ASSETS_FOLDER
                + STRING_PATH_DIVISOR + STRING_ASSET_SCRIPT_LIB_FOLDER;
    }
    
    public String getAssetInterFuncInfoPath()    {
        return getAppBaseFullPath() + STRING_PATH_DIVISOR + STRING_ASSETS_FOLDER
                + STRING_PATH_DIVISOR + STRING_ASSET_INTERNAL_FUNC_INFO_FILE;
    }
    
    public String getAssetMFPKeyWordsInfoPath()    {
        return getAppBaseFullPath() + STRING_PATH_DIVISOR + STRING_ASSETS_FOLDER
                + STRING_PATH_DIVISOR + STRING_ASSET_MFP_KEY_WORDS_INFO_FILE;
    }
    
    public String getScriptFolderFullPath()    {
        Long thisThreadId = Thread.currentThread().getId();
        if (CallObject.msmapThreadId2SessionInfo.containsKey(thisThreadId)) {
            // this is a sandbox session
            String outputFolder = CallObject.msmapThreadId2SessionInfo.get(thisThreadId).getLibPath();
            return outputFolder + STRING_PATH_DIVISOR + STRING_DEFAULT_SCRIPT_FOLDER;
        } else if (msstrScriptFolder.equals(""))   {
            return getAppBaseFullPath() + STRING_PATH_DIVISOR + STRING_DEFAULT_SCRIPT_FOLDER;
        } else  {
            return msstrScriptFolder;   // always assume the last '/' has been removed.
        }
    }
    
    public abstract LinkedList<String> getAdditionalUserLibs();

    public String getChartFolderFullPath()    {
        if (msstrChartFolder.equals(""))    {
            return getAppBaseFullPath() + STRING_PATH_DIVISOR + STRING_DEFAULT_CHART_FOLDER;
        } else  {
            return msstrChartFolder;   // always assume the last '/' has been removed.
        }
    }
    
    public String getExampleFolderFullPath()    {
        return getAppBaseFullPath() + STRING_PATH_DIVISOR + STRING_EXAMPLE_FOLDER;
    }
    
    public String getExampleScriptFolderFullPath()    {
        return getAppBaseFullPath() + STRING_PATH_DIVISOR + STRING_EXAMPLE_FOLDER
                 + STRING_PATH_DIVISOR + STRING_DEFAULT_SCRIPT_FOLDER;
    }    
    
    public String getExampleChartFolderFullPath()    {
        return getAppBaseFullPath() + STRING_PATH_DIVISOR + STRING_EXAMPLE_FOLDER
                 + STRING_PATH_DIVISOR + STRING_DEFAULT_CHART_FOLDER;
    }
    
    public String getExamplePdfManualFullPath()    {
        return getAppBaseFullPath() + STRING_PATH_DIVISOR + STRING_EXAMPLE_FOLDER
                 + STRING_PATH_DIVISOR + STRING_ASSET_MANUAL_PDF_FILE;
    }

    public boolean isNotSysLibPath(String strPath) {
        // for Android, we can check if am == null. If am == null, we are
        // not reading asset so it must not be a sys lib path.
        return strPath.length() < getAssetLibFolderFullPath().length();
    }
    
    public abstract Map<String, String> mapUsrLib2CompiledLib(LinkedList<String> listFilePaths) throws IOException;
    
    public abstract void loadPredefLibs();

    public class SourceDestPathMapInfo {
        protected String mstrSrcPath;
        public String getSrcPath() {
            return mstrSrcPath;
        }
        protected int mnSrcZipType;
        public int getZipType() {
            return mnSrcZipType;
        }
        protected String mstrSrcZipPath;
        public String getSrcZipPath() {
            return mstrSrcZipPath;
        }
        protected String mstrSrcZipEntry;
        public String getSrcZipEntry() {
            return mstrSrcZipEntry;
        }
        protected String mstrDestPath;
        public String getDestPath() {
            return mstrDestPath;
        }
        public SourceDestPathMapInfo(String srcPath, int nSrcZipType, String srcZipPath, String srcZipEntry, String destPath) {
            mstrSrcPath = srcPath.trim();
            mnSrcZipType = nSrcZipType;
            mstrSrcZipPath = srcZipPath.trim();
            mstrSrcZipEntry = srcZipEntry.trim();
            mstrDestPath = destPath.trim();
        }
        public SourceDestPathMapInfo(String srcPath, String destPath) {
            mstrSrcPath = srcPath;
            mnSrcZipType = 0;
            mstrSrcZipPath = "";
            mstrSrcZipEntry = "";
            mstrDestPath = destPath;
        }
        public SourceDestPathMapInfo(int nSrcZipType, String srcZipPath, String srcZipEntry, String destPath) {
            mstrSrcPath = "";
            mnSrcZipType = nSrcZipType;
            mstrSrcZipPath = srcZipPath;
            mstrSrcZipEntry = srcZipEntry;
            mstrDestPath = destPath;
        }
    }
    public abstract void mapAllNonDirMultiLevelChildResources(CompileAdditionalInfo.AssetCopyCmd acc, LinkedList<SourceDestPathMapInfo> srcDestPathMapList);
    
    public File createTempDir() throws IOException {
        final File temp;
        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
        if(!(temp.delete()))  {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if(!(temp.mkdir()))  {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }
}
