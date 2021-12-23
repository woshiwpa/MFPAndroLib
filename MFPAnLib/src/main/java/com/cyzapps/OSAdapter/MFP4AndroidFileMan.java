/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.OSAdapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.widget.Toast;

import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.IOLib;
import com.cyzapps.Jmfp.AnnoType_build_asset;
import com.cyzapps.Jmfp.CompileAdditionalInfo;
import com.cyzapps.Jmfp.ErrorProcessor;
import com.cyzapps.OSAdapter.ParallelManager.CallObject;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.adapter.AndroidStorageOptions;
import com.cyzapps.adapter.MFPAdapter;
import com.cyzapps.adapter.FunctionNameMapper;

import com.cyzapps.mfpanlib.MFPAndroidLib;
import com.cyzapps.mfpanlib.R;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cyzapps.adapter.MFPAdapter.loadLibCodeString;

/**
 *
 * @author tony
 */
public class MFP4AndroidFileMan extends LangFileManager {
    public static String msstrAppFolder = "AnMath";
    public static final String STRING_CONFIG_FOLDER = "config";
    public static final String STRING_SCRIPT_FOLDER = "scripts";
    public static final String STRING_SCRIPT_MANUAL_FOLDER = "manual";
    public static final String STRING_CHART_FOLDER = "charts";
    public static final String STRING_CHART_SNAPSHOT_FOLDER = "chart_snaps";
    public static final String STRING_FOLDER = "folder";
    public static final String STRING_UPPER_FOLDER = "upper_folder";
    public static final String STRING_BMP_EXTENSION = ".bmp";
    public static final String STRING_JPG_EXTENSION = ".jpg";
    public static final String STRING_JPEG_EXTENSION = ".jpeg";
    public static final String STRING_TXT_EXTENSION = ".txt";
    public static final String STRING_PDF_EXTENSION = ".pdf";
    public static final String STRING_UNKNOWN_FILE = "unknown_file";

    public static final String STRING_ASSET_USER_SCRIPT_LIB_ZIP = "userdef_lib.zip";
    public static final String STRING_ASSET_RESOURCE_ZIP = "resource.zip";

    public static final String STRING_ASSET_MFP_ANDROID_LIB_AAR = "MFPAnLib-release.aar";
    public static final String STRING_ASSET_WEBRTC_LIB_AAR = "google-webrtc-1.0.19742.aar";

    public static LinkedList<String> m_slFailedFilePaths = new LinkedList<String>();

    protected AssetManager mAssetManager = null;

    public MFP4AndroidFileMan(AssetManager assetManager) {
        mAssetManager = assetManager;
    }

    @Override
    public String[] list(String path, boolean bAndroidAsset) throws IOException {
        if (bAndroidAsset) {
            return mAssetManager.list(path);
        } else {
            File file = new File(path);
            try {
                String[] strarrayList = file.list();    // if strarrayList is null, it means an IO exception.
                if (strarrayList == null) {
                    throw new IOException("Invalid file path : " + path);
                }
                return strarrayList;
            } catch (SecurityException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
    }

    @Override
    public InputStream open(String path, boolean bAndroidAsset) throws IOException {
        if (bAndroidAsset) {
            return mAssetManager.open(path);
        } else {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(path);
            } catch (SecurityException e) {
                throw new IOException(e.getMessage(), e);
            } // it may also throw a FileNotFoundException, which is a type of IOException.
            return inputStream;
        }
    }

    /**
     * This function read a specific entry from a zipped file.
     * @param strZipFile : asset zip file.
     * @param strZippedFileEntryPath : path of zipped file entry in the zip. Note that it cannot be a folder
     * @param bAndroidAsset
     * @return : InputStream
     * @throws java.io.IOException
     */
    @Override
    public InputStream openZippedFileEntry(String strZipFile, String strZippedFileEntryPath, boolean bAndroidAsset) throws IOException {
        ArchiveInputStream ais = null;

        try {
            File fZippedFileEntry = new File(strZippedFileEntryPath);
            ais = new ArchiveStreamFactory().createArchiveInputStream("zip",
                    bAndroidAsset? mAssetManager.open(strZipFile) : new FileInputStream(strZipFile));

            ZipArchiveEntry entry = null;
            while ((entry = (ZipArchiveEntry) ais.getNextEntry()) != null) {
                File fEntry = new File(entry.getName());
                if (fZippedFileEntry.compareTo(fEntry) == 0) {
                    if (entry.isDirectory()) {
                        // we cannot extract a dictionary
                        return null;
                    } else {
                        return ais;
                    }
                }
            }
        } catch (ArchiveException ae) {
            throw new IOException("ArchiveException thrown");
        }
        return null;
    }

    @Override
    public boolean isDirectory(String path, boolean bAndroidAsset) {
        if (bAndroidAsset) {
            try {
                String[] files = mAssetManager.list(path);
                return files != null && files.length > 0;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            File file = new File(path);
            return file.isDirectory();
        }
    }

    @Override
    public Boolean isOnAndroid() {
        return true;
    }

    @Override
    public Boolean isMFPApp() { return MFPAndroidLib.getReadScriptsResFromAsset(); }

    @Override
    public String getAssetFilePath(String assetFile) {
        // asset resource target is only for mfp app.
        if (isMFPApp() && assetFile.equalsIgnoreCase(AnnoType_build_asset.ASSET_RESOURCE_TARGET)) {
            return "resource.zip";
        }
        return null;
    }

    @Override
    public String getAppBaseFullPath() {
        return AndroidStorageOptions.getSelectedStoragePath()
                + STRING_PATH_DIVISOR + msstrAppFolder;
    }

    @Override
    public byte[] readFileToBytes(String pathSrc) throws ErrProcessor.JFCALCExpErrException {
        if (isMFPApp()) {
            InputStream is = null;
            try {
                if (pathSrc.startsWith(getScriptFolderFullPath())) {
                    // this file is in user defined script zip
                    String pathZipEntry = pathSrc.substring(getScriptFolderFullPath().length() + STRING_PATH_DIVISOR.length());
                    is = openZippedFileEntry(getScriptFolderFullPath(), pathZipEntry, true);
                    byte[] fileBytes = IOUtils.toByteArray(is);
                    return fileBytes;
                } else if (pathSrc.startsWith(getAssetResourceFullPath())) {
                    // this file is in resource zip
                    String pathZipEntry = pathSrc.substring(getAssetResourceFullPath().length() + STRING_PATH_DIVISOR.length());
                    is = openZippedFileEntry(getAssetResourceFullPath(), pathZipEntry, true);
                    byte[] fileBytes = IOUtils.toByteArray(is);
                    return fileBytes;
                } else {
                    is = open(pathSrc, !pathSrc.startsWith("/")); // if pathSrc.startsWith "/", it is a sandbox file. Otherwise, it is an asset file. We need not consider Windows path here.
                    int size = is.available();
                    if (size < 0) {
                        throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_FILE);
                    }
                    byte[] fileBytes = new byte[size];
                    is.read(fileBytes);
                    return fileBytes;
                }
            } catch (IOException e) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CANNOT_READ_FILE);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            // if not MFP APP, just use super class version.
            return super.readFileToBytes(pathSrc);
        }
    }

    @Override
    public byte[] readFileToBytes(SourceDestPathMapInfo fileInfo) throws ErrProcessor.JFCALCExpErrException {
        if (isMFPApp() && fileInfo.getSrcPath().length() == 0 && fileInfo.mnSrcZipType == 1) {   // android asset zip file.
            InputStream is = null;
            try {
                is = openZippedFileEntry(fileInfo.mstrSrcZipPath, fileInfo.mstrSrcZipEntry, true);
                byte[] fileBytes = IOUtils.toByteArray(is);
                return fileBytes;
            } catch (IOException e) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CANNOT_READ_FILE);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            // if not android asset file, just use super class version.
            return super.readFileToBytes(fileInfo);
        }
    }

    @Override
    public InputStream readFileToInputStream(SourceDestPathMapInfo fileInfo) throws ErrProcessor.JFCALCExpErrException {
        if (fileInfo.getSrcPath().length() == 0 && fileInfo.mnSrcZipType == 1) {   // android asset zip file.
            try {
                return openZippedFileEntry(fileInfo.mstrSrcZipPath, fileInfo.mstrSrcZipEntry, true);
            } catch (IOException ex) {
                Logger.getLogger(LangFileManager.class.getName()).log(Level.SEVERE, null, ex);
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_FILE);
            }
        } else {
            // if not android asset file, just use super class version.
            return super.readFileToInputStream(fileInfo);
        }
    }

    @Override
    public LinkedList<String> getAdditionalUserLibs() {
        Long thisThreadId = Thread.currentThread().getId();
        if (CallObject.msmapThreadId2SessionInfo.containsKey(thisThreadId)) {
            // this is a sandbox session
            String outputFolder = CallObject.msmapThreadId2SessionInfo.get(thisThreadId).getLibPath();
            LinkedList<String> additionalNames = CallObject.msmapThreadId2SessionInfo.get(thisThreadId).getAdditionalLibNames();
            LinkedList<String> additionalUserLibs = new LinkedList<String>();
            for (String additionalName: additionalNames) {
                additionalUserLibs.add(outputFolder + STRING_PATH_DIVISOR + additionalName);
            }
            return additionalUserLibs;
        } else {
            return new LinkedList<String>();
        }
    }

    public String getConfigFolderFullPath()	{
        return AndroidStorageOptions.getSelectedStoragePath()
                + STRING_PATH_DIVISOR + msstrAppFolder + STRING_PATH_DIVISOR + STRING_CONFIG_FOLDER;
    }

    public String getAssetsFolderFullPath()	{
        return AndroidStorageOptions.getSelectedStoragePath()
                + STRING_PATH_DIVISOR + msstrAppFolder + STRING_PATH_DIVISOR + STRING_ASSETS_FOLDER;
    }

    @Override
    public String getScriptFolderFullPath()	{
        if (!isMFPApp()) {
            return AndroidStorageOptions.getSelectedStoragePath()
                    + STRING_PATH_DIVISOR + msstrAppFolder + STRING_PATH_DIVISOR + STRING_SCRIPT_FOLDER;
        } else {
            return STRING_ASSET_USER_SCRIPT_LIB_ZIP;
        }
    }

    public String getMFPAndroidLibAarFullPath() {
        return AndroidStorageOptions.getSelectedStoragePath()
                + STRING_PATH_DIVISOR + msstrAppFolder + STRING_PATH_DIVISOR + STRING_ASSET_MFP_ANDROID_LIB_AAR;
    }

    public String getWebRTCAarFullPath() {
        return AndroidStorageOptions.getSelectedStoragePath()
                + STRING_PATH_DIVISOR + msstrAppFolder + STRING_PATH_DIVISOR + STRING_ASSET_WEBRTC_LIB_AAR;
    }

    // this function is for MFP APP only
    public String getAssetResourceFullPath() {
        return STRING_ASSET_RESOURCE_ZIP;
    }

    @Override
    public String getChartFolderFullPath()	{
        return AndroidStorageOptions.getSelectedStoragePath()
                + STRING_PATH_DIVISOR + msstrAppFolder + STRING_PATH_DIVISOR + STRING_CHART_FOLDER;
    }

    @Override
    public String getExampleFolderFullPath()	{
        return AndroidStorageOptions.getSelectedStoragePath()
                + STRING_PATH_DIVISOR + msstrAppFolder + STRING_PATH_DIVISOR + STRING_EXAMPLE_FOLDER;
    }

    public String getScriptManualFolderFullPath()	{
        return AndroidStorageOptions.getSelectedStoragePath()
                + STRING_PATH_DIVISOR + msstrAppFolder + STRING_PATH_DIVISOR + STRING_SCRIPT_FOLDER
                + STRING_PATH_DIVISOR + STRING_SCRIPT_MANUAL_FOLDER;
    }

    public String getJMathCmdFileFullPath()	{
        return AndroidStorageOptions.getSelectedStoragePath()
                + STRING_PATH_DIVISOR + msstrAppFolder + STRING_PATH_DIVISOR + STRING_ASSET_JMATHCMD_JAR_FILE;
    }

    public String getJMFPLangFileFullPath()	{
        return AndroidStorageOptions.getSelectedStoragePath()
                + STRING_PATH_DIVISOR + msstrAppFolder + STRING_PATH_DIVISOR + STRING_ASSET_JMFPLANG_JAR_FILE;
    }

    public String getMfplangCmdFileFullPath()	{
        return AndroidStorageOptions.getSelectedStoragePath()
                + STRING_PATH_DIVISOR + msstrAppFolder + STRING_PATH_DIVISOR + STRING_ASSET_MFPLANG_CMD_FILE;
    }

    public String getMfplangShFileFullPath()	{
        return AndroidStorageOptions.getSelectedStoragePath()
                + STRING_PATH_DIVISOR + msstrAppFolder + STRING_PATH_DIVISOR + STRING_ASSET_MFPLANG_SH_FILE;
    }

    public String getAssetZipFileFullPath()	{
        return AndroidStorageOptions.getSelectedStoragePath()
                + STRING_PATH_DIVISOR + msstrAppFolder + STRING_PATH_DIVISOR + STRING_ASSET_ZIP_FILE;
    }

    public String getAssetMFPAppZipFileFullPath()	{
        return AndroidStorageOptions.getSelectedStoragePath()
                + STRING_PATH_DIVISOR + msstrAppFolder + STRING_PATH_DIVISOR + STRING_ASSETS_FOLDER + STRING_PATH_DIVISOR + STRING_ASSET_MFP_APP_ZIP_FILE;
    }

    public static boolean copyAssetScripts2SD(LangFileManager lfm, String strSrcPath, String strDestPath) {
        String strAssetFiles[] = null;
        boolean bReturnValue = true;
        try {
            String strScriptExt = STRING_SCRIPT_EXTENSION;
            if (strSrcPath.substring(strSrcPath.length() - strScriptExt.length())
                    .toLowerCase(Locale.US).equals(strScriptExt)) {
                // this is a script.
                if (MFPAdapter.copyAssetFile2SD(lfm, strSrcPath, strDestPath) == false)	{
                    return false;
                }
            } else if (strSrcPath.substring(strSrcPath.length() - STRING_ASSET_SCRIPT_LIB_FOLDER_EXTENSION.length())
                    .toLowerCase(Locale.US).equals(STRING_ASSET_SCRIPT_LIB_FOLDER_EXTENSION))	{
                File dir = new File(strDestPath);
                if (!dir.exists())	{
                    if (!dir.mkdirs())	{
                        return false;	// cannot create destination folder
                    }
                }
                strAssetFiles = lfm.list(strSrcPath, true);
                for (int i = 0; i < strAssetFiles.length; ++i) {
                    boolean bThisCpyReturn = copyAssetScripts2SD(lfm, strSrcPath + STRING_PATH_DIVISOR + strAssetFiles[i],
                            strDestPath + STRING_PATH_DIVISOR + strAssetFiles[i]);
                    if (!bThisCpyReturn) {
                        bReturnValue = false;
                    }
                }
            }
        } catch (IOException ex) {
            return false;
        }
        return bReturnValue;
    }

    /**
     * @param listFilePaths : a list of source FILE paths, not folder path. Also, path divisor has been correctly set
     * to be LangFileManager.STRING_PATH_DIVISOR
     * @return : map from source file paths to compiled source path (in the apk package asset or in remote call sandbox)
     * @throws IOException
     */
    @Override
    public Map<String, String> mapUsrLib2CompiledLib(LinkedList<String> listFilePaths) throws IOException {
        // keep in mind that listFilePaths is a list of source FILE paths, not folder path.
        Map<String, String> mappedUsrLibPaths = new HashMap<String, String>();
        for(String strFile : listFilePaths){
            // In Android, we dont call getCanonicalPath because we know the strFile will be case-sensitive
            // and will not include .. or . or a symbol link
            // now add strFile. Note that this is different from JAVA on pc. Here we do not need to support
            // multiple user libs (i.e. base user lib and additional user libs).
            String strRelativePath = strFile.substring(getScriptFolderFullPath().length());
            while(strRelativePath.indexOf(LangFileManager.STRING_PATH_DIVISOR) == 0) {
                strRelativePath = strRelativePath.substring(LangFileManager.STRING_PATH_DIVISOR.length());
            }
            // comply with Jcmdline
            strRelativePath = STRING_DEFAULT_SCRIPT_FOLDER + LangFileManager.STRING_PATH_DIVISOR + strRelativePath;
            mappedUsrLibPaths.put(strFile, strRelativePath);
        }
        return mappedUsrLibPaths;
    }

    public static boolean copyAssetCharts2SD(LangFileManager lfm, String strSrcPath, String strDestPath) {
        String strAssetFiles[] = null;
        boolean bReturnValue = true;
        try {
            String strScriptExt = STRING_CHART_EXTENSION;
            if (strSrcPath.substring(strSrcPath.length() - strScriptExt.length())
                    .toLowerCase(Locale.US).equals(strScriptExt)
                    && (strSrcPath.equals(STRING_ASSET_CHARTS_FOLDER
                    + STRING_PATH_DIVISOR
                    + STRING_ASSET_CHART_EXAMPLE1_FILE)
                    || strSrcPath.equals(STRING_ASSET_CHARTS_FOLDER
                    + STRING_PATH_DIVISOR
                    + STRING_ASSET_CHART_EXAMPLE2_FILE))) {
                // this is a chart.
                if (MFPAdapter.copyAssetFile2SD(lfm, strSrcPath, strDestPath) == false)	{
                    return false;
                }
            } else if (strSrcPath.substring(strSrcPath.length() - STRING_ASSET_CHARTS_FOLDER_EXTENSION.length())
                    .toLowerCase(Locale.US).equals(STRING_ASSET_CHARTS_FOLDER_EXTENSION))	{
                File dir = new File(strDestPath);
                if (!dir.exists())	{
                    if (!dir.mkdirs())	{
                        return false;	// cannot create destination folder
                    }
                }
                strAssetFiles = lfm.list(strSrcPath, true);
                for (int i = 0; i < strAssetFiles.length; ++i) {
                    boolean bThisCpyReturn = copyAssetCharts2SD(lfm, strSrcPath + STRING_PATH_DIVISOR + strAssetFiles[i],
                            strDestPath + STRING_PATH_DIVISOR + strAssetFiles[i]);
                    if (!bThisCpyReturn) {
                        bReturnValue = false;
                    }
                }
            }
        } catch (IOException ex) {
            return false;
        }
        return bReturnValue;
    }

    public static void loadZippedUsrDefLib(String strZipFile, MFP4AndroidFileMan mfp4AndroidFileMan) {
        ArchiveInputStream ais = null;

        try {
            ais = new ArchiveStreamFactory().createArchiveInputStream("zip", mfp4AndroidFileMan.mAssetManager.open(strZipFile));

            ZipArchiveEntry entry = null;
            while ((entry = (ZipArchiveEntry) ais.getNextEntry()) != null) {
                if(entry.isDirectory() || MFPAdapter.getFileExtFromName(entry.getName()).toLowerCase(Locale.US)
                        .equals(STRING_SCRIPT_EXTENSION) == false) {
                    // folder or not mfps file.
                    continue;
                } else  {
                    String strMFPSFilePath = strZipFile + STRING_PATH_DIVISOR + entry.getName();
                    String[] strarrayNameSpace = MFPAdapter.cvtPath2PathSpace(strMFPSFilePath, mfp4AndroidFileMan);
                    loadInputStream(strMFPSFilePath, strarrayNameSpace, ais, false);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ArchiveException ae) {
            ae.printStackTrace();
        } finally {
            try {
                if (ais != null) {
                    ais.close();
                }
            } catch (IOException ignored) {
            }
            ais = null;
        }
    }

    public static boolean loadInputStream(String strFilePath, String[] strarrayPathSpace, InputStream inputStream, boolean bCloseAfterUse)	{
        // using this way instead of BufferedReader, accelerate reading speed.
        String strFileContent = "";
        ByteArrayOutputStream baos = null;
        try	{
            baos = new ByteArrayOutputStream();
            int length = 0;
            while ((length = inputStream.read(MFPAdapter.m_sbyteBuffer)) != -1) {
                baos.write(MFPAdapter.m_sbyteBuffer, 0, length);
            }
            byte[] byteArray = baos.toByteArray();
            strFileContent = new String(byteArray, "UTF-8");
        } catch (Exception  e)	{
            // System.out.println("Cannot read file " + f.getPath());
            m_slFailedFilePaths.addLast(strFilePath);
            return false;
        } finally {
            try	{
                if (baos != null)	{
                    baos.close();
                }
                if (bCloseAfterUse) {
                    inputStream.close();
                }
            } catch (IOException e)	{

            }
        }

        String[] strSrcLines = strFileContent.split("\n");

        try {
            return loadLibCodeString(strSrcLines, strFilePath, strarrayPathSpace);
        } catch (ErrorProcessor.JMFPCompErrException ex) {
            Logger.getLogger(MFPAdapter.class.getName()).log(Level.SEVERE, null, ex);
            return true;    // we have to return true here because returning false means the whole file cannot be loaded.
        }
    }

    public static boolean msbIsReloadingAll = false;

    public static boolean canReloadAll()	{
        return (msbIsReloadingAll == false);
    }

    // the following function load predefined MFPS libs. It should be
    // called only once when App starts
    @Override
    public void loadPredefLibs() {
        MFPAdapter.clear(CitingSpaceDefinition.CheckMFPSLibMode.CHECK_EVERYTHING); // was false, which mean clear all

        MFPAdapter.getSysLibFiles(this, STRING_ASSET_SCRIPT_LIB_FOLDER);
        MFPAdapter.loadSysLib(this);	// load developer defined lib.
        MFPAdapter.loadInternalFuncInfo(this, STRING_ASSET_INTERNAL_FUNC_INFO_FILE);
        MFPAdapter.loadMFPKeyWordsInfo(this, STRING_ASSET_MFP_KEY_WORDS_INFO_FILE);
        // load system function (i.e. built-in and predefined) mappers.
        // This must be done before user defined functions are loaded.
        FunctionNameMapper.loadSysFuncInvertMap();
        FunctionNameMapper.loadSysFunc2FullCSMap();

        // now we save a copy of citing space definition sys and the saved copy will be csd Top full (for main entity).
        CitingSpaceDefinition.createTopCSDFull();
    }

    // the following function maps a resource path (stored in a CompileAdditionalInfo.AssetCopyCmd) to sand box path(s).
    // if the resource path is a folder, each individual file will be mapped to sand box's destination path map list. On the
    // other hand, if the resource path is a file, it would be mapped and stored into srcDestPathMapList. Here, srcDestPathMapList
    // is a SourceDestPathMapInfo object, which stores path (if it is a normal file) or zip info (if it is a zip entry).
    // as we are in a built APK, all the resource files are either zipped in android asset's resource.zip if we are in a main
    // entity, or in the resource folder of a temporary place if we are in a sand box. If we are in android asset's resource.zip,
    // we ignore the acc.mstrSrcPath but use getAssetResourceFullPath() + / + acc.mstrDestPath as the source path
    @Override
    public void mapAllNonDirMultiLevelChildResources(CompileAdditionalInfo.AssetCopyCmd acc, LinkedList<SourceDestPathMapInfo> srcDestPathMapList) {
        if (acc.isPlainPath()) {
            File f = new File(acc.mstrSrcPath);
            if (f.isFile()) {
                boolean bAddedEntry = false;
                for (SourceDestPathMapInfo sdpmi : srcDestPathMapList) {
                    if (0 == IOLib.comparePath(sdpmi.mstrSrcPath, acc.mstrSrcPath)
                            && 0 == IOLib.comparePath(sdpmi.mstrDestPath, acc.mstrDestPath)) {
                        bAddedEntry = true;
                        break;
                    }
                }
                if (!bAddedEntry) {
                    srcDestPathMapList.add(new SourceDestPathMapInfo(acc.mstrSrcPath, acc.mstrDestPath));
                }
            } else if (f.isDirectory()) {
                File[] childFiles = f.listFiles();
                for (File fChild : childFiles) {
                    if (fChild.isFile()) {
                        String strName = fChild.getName();
                        boolean bAddedEntry = false;
                        String strSrcPath = acc.mstrSrcPath.endsWith(LangFileManager.STRING_PATH_DIVISOR) ? (acc.mstrSrcPath + strName) : (acc.mstrSrcPath + LangFileManager.STRING_PATH_DIVISOR + strName);
                        String strDestPath = acc.mstrDestPath.endsWith(LangFileManager.STRING_PATH_DIVISOR) ? (acc.mstrDestPath + strName) : (acc.mstrDestPath + LangFileManager.STRING_PATH_DIVISOR + strName);
                        for (SourceDestPathMapInfo sdpmi : srcDestPathMapList) {
                            if (0 == IOLib.comparePath(sdpmi.mstrSrcPath, strSrcPath)
                                    && 0 == IOLib.comparePath(sdpmi.mstrDestPath, strDestPath)) {
                                bAddedEntry = true;
                                break;
                            }
                        }
                        if (!bAddedEntry) {
                            srcDestPathMapList.add(new SourceDestPathMapInfo(strSrcPath, strDestPath));
                        }
                    } else if (fChild.isDirectory()) {
                        String strName = fChild.getName();
                        CompileAdditionalInfo.AssetCopyCmd accChild = new CompileAdditionalInfo.AssetCopyCmd();
                        accChild.mstrSrcPath = acc.mstrSrcPath + LangFileManager.STRING_PATH_DIVISOR + strName;
                        accChild.mstrDestTarget = acc.mstrDestTarget;
                        accChild.mstrDestPath = acc.mstrDestPath + LangFileManager.STRING_PATH_DIVISOR + strName;
                        mapAllNonDirMultiLevelChildResources(accChild, srcDestPathMapList);
                    }
                }
            }
        } else if (acc.isNormalZip()) {
            ArchiveInputStream ais = null;

            try {
                ais = new ArchiveStreamFactory().createArchiveInputStream("zip", new FileInputStream(acc.mstrSrcZipPath));

                ZipArchiveEntry entry = null;
                while ((entry = (ZipArchiveEntry) ais.getNextEntry()) != null) {
                    if (entry.getName().startsWith(acc.mstrSrcZipEntry) // the entry is a child node of acc.mstrSrcZipEntry
                            && (entry.getName().equals(acc.mstrSrcZipEntry)
                                || acc.mstrSrcZipEntry.endsWith("/")
                                || entry.getName().substring(acc.mstrSrcZipEntry.length()).startsWith("/"))) {
                        // we can compare zip entry directly without caring about seperator
                        // because zip entry's seperator is always forward slash.
                        if (entry.isDirectory()) {
                            // this is a dictionary
                            continue;
                        } else {
                            // this is a file
                            boolean bAddedEntry = false;
                            for (SourceDestPathMapInfo sdpmi : srcDestPathMapList) {
                                if (0 == IOLib.comparePath(sdpmi.mstrSrcZipPath, acc.mstrSrcZipPath)
                                        && sdpmi.mstrSrcZipEntry.equals(entry.getName())
                                        && sdpmi.mnSrcZipType == acc.mnSrcZipType) {
                                    // this entry has been added, no need to add again.
                                    bAddedEntry = true;
                                    break;
                                }
                            }
                            if (!bAddedEntry) {
                                String strEntryPathRelativePart = entry.getName().substring(acc.mstrSrcZipEntry.length());
                                String strEntryDestFullPath = "";
                                if ((strEntryPathRelativePart.startsWith("/") && !acc.mstrDestPath.endsWith("/"))
                                    || (!strEntryPathRelativePart.startsWith("/") && acc.mstrDestPath.endsWith("/"))) {
                                    strEntryDestFullPath = acc.mstrDestPath + strEntryPathRelativePart;
                                } else if (strEntryPathRelativePart.startsWith("/") && acc.mstrDestPath.endsWith("/")) {
                                    strEntryDestFullPath = acc.mstrDestPath + strEntryPathRelativePart.substring(1); // remove "/" from beginning of strEntryPathRelativePart
                                } else {
                                    strEntryDestFullPath = acc.mstrDestPath + "/" + strEntryPathRelativePart;   // add "/" which is missing.
                                }
                                srcDestPathMapList.add(new SourceDestPathMapInfo(acc.mnSrcZipType, acc.mstrSrcZipPath, entry.getName(), strEntryDestFullPath));
                            }
                        }
                    }
                }
            } catch (ArchiveException ae) {
            } catch (IOException ioe) {
            }
        } else {    // this is Android asset file.
            // first, let's convert the source path because we are in the script lib zip file
            ArchiveInputStream ais = null;
            try {
                ais = new ArchiveStreamFactory().createArchiveInputStream("zip", mAssetManager.open(acc.mstrSrcZipPath));

                ZipArchiveEntry entry = null;
                while ((entry = (ZipArchiveEntry) ais.getNextEntry()) != null) {
                    if (entry.getName().startsWith(acc.mstrSrcZipEntry) // the entry is a child node of acc.mstrSrcZipEntry
                            && (entry.getName().equals(acc.mstrSrcZipEntry)
                            || acc.mstrSrcZipEntry.endsWith("/")
                            || entry.getName().substring(acc.mstrSrcZipEntry.length()).startsWith("/"))) {
                        // we can compare zip entry directly without caring about seperator
                        // because zip entry's seperator is always forward slash.
                        if (entry.isDirectory()) {
                            // this is a dictionary
                            continue;
                        } else {
                            // this is a file
                            boolean bAddedEntry = false;
                            for (SourceDestPathMapInfo sdpmi : srcDestPathMapList) {
                                if (sdpmi.mstrSrcZipPath.equals(acc.mstrSrcZipPath)
                                        && sdpmi.mstrSrcZipEntry.equals(entry.getName())
                                        && sdpmi.mnSrcZipType == acc.mnSrcZipType) {
                                    // this entry has been added, no need to add again.
                                    bAddedEntry = true;
                                    break;
                                }
                            }
                            if (!bAddedEntry) {
                                String strEntryPathRelativePart = entry.getName().substring(acc.mstrSrcZipEntry.length());
                                String strEntryDestFullPath = "";
                                if ((strEntryPathRelativePart.startsWith("/") && !acc.mstrDestPath.endsWith("/"))
                                        || (!strEntryPathRelativePart.startsWith("/") && acc.mstrDestPath.endsWith("/"))) {
                                    strEntryDestFullPath = acc.mstrDestPath + strEntryPathRelativePart;
                                } else if (strEntryPathRelativePart.startsWith("/") && acc.mstrDestPath.endsWith("/")) {
                                    strEntryDestFullPath = acc.mstrDestPath + strEntryPathRelativePart.substring(1); // remove "/" from beginning of strEntryPathRelativePart
                                } else {
                                    strEntryDestFullPath = acc.mstrDestPath + "/" + strEntryPathRelativePart;   // add "/" which is missing.
                                }
                                srcDestPathMapList.add(new SourceDestPathMapInfo(acc.mnSrcZipType, acc.mstrSrcZipPath, entry.getName(), strEntryDestFullPath));
                            }
                        }
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (ArchiveException ae) {
                ae.printStackTrace();
            } finally {
                try {
                    if (ais != null) {
                        ais.close();
                    }
                } catch (IOException ignored) {
                }
                ais = null;
            }
        }
    }

    // the following function is reloading all user defined libs or at activity reloading
    // note that this function can only be used in non-sandbox thread!!!!
    // in other words, CitingSpaceDefintion.getTOPCSD() has to return mscsdTOPFull.
    public static Thread reloadAllUsrLibs(final Context context, int nReloadingMode, final String strToastInfo)	{
        Thread threadReload = null;
        // functions haven't been loaded
        if (nReloadingMode == 1)	{	// asynchronous mode with progress dialog shown.
            final ProgressDialog dlgInitProgress = ProgressDialog.show(context, context.getString(R.string.please_wait),
                    context.getString(R.string.loading_user_defined_libs), true);
            final Handler handler = new Handler();

            threadReload = new Thread(new Runnable()	{

                @Override
                public void run() {
                    msbIsReloadingAll = true;

                    MFPAdapter.clear(CitingSpaceDefinition.CheckMFPSLibMode.CHECK_USER_DEFINED_ONLY);

                    AssetManager am = context.getAssets();
                    MFP4AndroidFileMan mfp4AnFileMan = new MFP4AndroidFileMan(am);

                    handler.post(new Runnable()	{
                        @Override
                        public void run() {
                            dlgInitProgress.setMessage(context.getString(R.string.loading_user_defined_libs));
                        }
                    });

                    // in Android, we do not support multipule user def libs. All the user def libs are
                    // in folder getScriptFolderFullPath(). So we clear m_slistUsrDefLibPath first and then
                    // we load everything.
                    MFPAdapter.m_slistUsrDefLibPath.clear();
                    MFPAdapter.getUsrLibFiles(mfp4AnFileMan, mfp4AnFileMan.getScriptFolderFullPath());
                    MFPAdapter.loadUsrLib(mfp4AnFileMan);	// load user defined lib.
                    // hide this function because we want to analyse statements on the fly
                    //MFPAdapter.analyseStatements(); // analyse the statements (using abstractexpr instead of string).
                    handler.post(new Runnable()	{
                        @Override
                        public void run() {
                            dlgInitProgress.dismiss();
                            if (strToastInfo != null && strToastInfo.trim().equals("") == false)	{
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(context, strToastInfo, duration);
                                toast.show();
                            }
                        }
                    });

                    msbIsReloadingAll = false;
                }
            });
            threadReload.start();
        } else if (nReloadingMode == 0)	{	// asynchronous mode without progress dialog shown
            final Handler handler = new Handler();
            threadReload = new Thread(new Runnable()	{

                @Override
                public void run() {
                    msbIsReloadingAll = true;

                    MFPAdapter.clear(CitingSpaceDefinition.CheckMFPSLibMode.CHECK_USER_DEFINED_ONLY);

                    AssetManager am = context.getAssets();
                    MFP4AndroidFileMan mfp4AnFileMan = new MFP4AndroidFileMan(am);

                    // in Android, we do not support multipule user def libs. All the user def libs are
                    // in folder getScriptFolderFullPath(). So we clear m_slistUsrDefLibPath first and then
                    // we load everything.
                    MFPAdapter.m_slistUsrDefLibPath.clear();
                    MFPAdapter.getUsrLibFiles(mfp4AnFileMan, mfp4AnFileMan.getScriptFolderFullPath());
                    MFPAdapter.loadUsrLib(mfp4AnFileMan);	// load user defined lib.
                    // hide this function because we want to analyse statements on the fly
                    //MFPAdapter.analyseStatements(); // analyse the statements (using abstractexpr instead of string).
                    handler.post(new Runnable()	{
                        @Override
                        public void run() {
                            if (strToastInfo != null && strToastInfo.trim().equals("") == false)	{
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(context, strToastInfo, duration);
                                toast.show();
                            }
                        }
                    });

                    msbIsReloadingAll = false;
                }
            });
            threadReload.start();
        } else	{// synchronous mode
            msbIsReloadingAll = true;

            MFPAdapter.clear(CitingSpaceDefinition.CheckMFPSLibMode.CHECK_USER_DEFINED_ONLY);

            AssetManager am = context.getAssets();
            MFP4AndroidFileMan mfp4AnFileMan = new MFP4AndroidFileMan(am);

            // in Android, we do not support multipule user def libs. All the user def libs are
            // in folder getScriptFolderFullPath(). So we clear m_slistUsrDefLibPath first and then
            // we load everything.
            MFPAdapter.m_slistUsrDefLibPath.clear();
            MFPAdapter.getUsrLibFiles(mfp4AnFileMan, mfp4AnFileMan.getScriptFolderFullPath());
            MFPAdapter.loadUsrLib(mfp4AnFileMan);	// load user defined lib.
            // hide this function because we want to analyse statements on the fly
            //MFPAdapter.analyseStatements(); // analyse the statements (using abstractexpr instead of string).
            if (strToastInfo != null && strToastInfo.trim().equals("") == false)	{
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, strToastInfo, duration);
                toast.show();
            }
            msbIsReloadingAll = false;
        }
        return threadReload;
    }
}
