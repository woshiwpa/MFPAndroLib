// MFP project, MFPAdapter.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.adapter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassComplex;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.IOLib;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.ClassAnalyzer;
import com.cyzapps.Jmfp.ErrorProcessor;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.FunctionEntry;
import com.cyzapps.Jmfp.ScriptAnalyzer.ScriptStatementException;
import com.cyzapps.Jmfp.Statement;
import com.cyzapps.Jmfp.Statement_citingspace;
import com.cyzapps.Jmfp.Statement_class;
import com.cyzapps.Jmfp.Statement_endclass;
import com.cyzapps.Jmfp.Statement_endcs;
import com.cyzapps.Jmfp.Statement_endf;
import com.cyzapps.Jmfp.Statement_endh;
import com.cyzapps.Jmfp.Statement_function;
import com.cyzapps.Jmfp.Statement_help;
import com.cyzapps.Jmfp.Statement_using;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.Oomfp.MemberFunction;
import com.cyzapps.Oomfp.OoErrProcessor.JOoMFPErrException;
import com.cyzapps.OSAdapter.LangFileManager;
import com.cyzapps.Oomfp.CitingSpaceDefinition.CheckMFPSLibMode;
import com.cyzapps.Oomfp.MFPClassDefinition;
import org.apache.commons.lang3.StringEscapeUtils;

public class MFPAdapter {
    public static final int INT_ASSET_PATH_MAX_CHILD_LEVEL = 32;    // assume asset path cannot be as deep as 32 level.
        
    public static final int MAX_NUMBER_OF_OPEN_FILES = 2048;
    public static final long FD_EXPIRY_TIME = 3600000;
    
    public static int msnBitsofPrecision = 8;
    public static int msnBigSmallThresh = 8;
    public static MFPNumeric mmfpNumBigThresh = new MFPNumeric(100000000);
    public static MFPNumeric mmfpNumSmallThresh = new MFPNumeric("0.00000001"); //0.00000001 has to be accurately represented. So use string
    
    public static double msdPlotChartVariableFrom = -5.0;
    public static double msdPlotChartVariableTo = 5.0;

    public static int msnWebRTCDebugLevel = 0;
    
    public static LinkedList<String[]> mslSysAddCitingSpaces = new LinkedList<String[]>();
    
    public static byte[] m_sbyteBuffer = new byte[32768];    //make it static so that save realloc time.
    
    public static class MFPKeyWordInfo  {
        public String mstrKeyWordName = "";
        public Map<String, String> mmapHelpInfo = new HashMap<String, String>();
        
        public String extractHelpFromMFPKeyWordInfo(String strLang)    {
            String strLangLowerCase = strLang.trim().toLowerCase(Locale.US);
            String strHelp = mmapHelpInfo.get(strLangLowerCase);
            if (strHelp == null)    {
                strHelp = mmapHelpInfo.get("default");
                if (strHelp == null)    {
                    strHelp = "";
                }
            }
            return strHelp;
        }
    }
    
    public static class InternalFuncInfo {

        public String mstrFuncName = "";
        public int mnLeastNumofParams = 0;
        public boolean mbOptParam = false;
        public String[] mstrlistHelpInfo = new String[0];
    }

    public static LinkedList<MFPKeyWordInfo> m_slMFPKeyWordInfo = new LinkedList<MFPKeyWordInfo>();
    public static LinkedList<InternalFuncInfo> m_slInternalFuncInfo = new LinkedList<InternalFuncInfo>();
    
    public static class DefLibPath  {
        public String mstrLibPath = "";
        public long mnLastModifiedTime = 0;
        public LinkedList<DefLibPath> mlSubLibPath = null;  // null means a file.
        public void clear() {
            mstrLibPath = "";
            mnLastModifiedTime = 0;
            if (mlSubLibPath != null)  {
                while (mlSubLibPath.size() > 0)    {
                    mlSubLibPath.removeFirst().clear();
                }
            }
            mlSubLibPath = null;
        }
        public boolean isEmpty()    {
            if (mstrLibPath.equals("") && mnLastModifiedTime == 0 && mlSubLibPath == null) {
                return true;
            }
            return false;
        }
    }
    
    public static LinkedList<DefLibPath> m_slistSysDefLibPath = new LinkedList<DefLibPath>();
    public static LinkedList<DefLibPath> m_slistUsrDefLibPath = new LinkedList<DefLibPath>();
    
    public static LinkedList<String> m_slFailedFilePaths = new LinkedList<String>();
    
    // todo: an easier way might be replace Top CSD by CSD_TOP_SYS. Think about it.
    public static void clear(CheckMFPSLibMode checkMode)
    {
        if (checkMode != CheckMFPSLibMode.CHECK_EVERYTHING) {
            CitingSpaceDefinition.getTopCSD().clearMFPSLibs(checkMode);
            if (checkMode != CheckMFPSLibMode.CHECK_COMMAND_INPUT_ONLY) {
                m_slistUsrDefLibPath.clear();
            }
        } else {    // clear everything
            m_slMFPKeyWordInfo.clear();
            m_slInternalFuncInfo.clear();
            CitingSpaceDefinition.getTopCSD().clearMFPSLibs(checkMode);
            m_slistSysDefLibPath.clear();
            m_slFailedFilePaths.clear();
            m_slistUsrDefLibPath.clear();
        }
    }
    
    public static boolean isEmpty()
    {
        if (m_slMFPKeyWordInfo.size() == 0 && m_slInternalFuncInfo.size() == 0 && CitingSpaceDefinition.getTopCSD().isMFPSLibEmpty(CheckMFPSLibMode.CHECK_EVERYTHING)
                && m_slistSysDefLibPath.isEmpty() && m_slistUsrDefLibPath.isEmpty() && m_slFailedFilePaths.size() == 0)
            return true;
        return false;
    }

    public static boolean isSameMFPSLibFile(String strPath1, String strPath2, LangFileManager lfm)    {
        String[] strarrayPathSpace1 = cvtPath2PathSpace(strPath1, lfm);
        String[] strarrayPathSpace2 = cvtPath2PathSpace(strPath2, lfm);
        return isSameMFPSLibFile(strarrayPathSpace1, strarrayPathSpace2);
    }
    
    public static boolean isSameMFPSLibFile(String[] strarrayPathSpace1, String[] strarrayPathSpace2)    {
        if (strarrayPathSpace1 == null)    {
            return false;
        }
        if (strarrayPathSpace2 == null)    {
            return false;
        }
        if (strarrayPathSpace1.length != strarrayPathSpace2.length)    {
            return false;
        }
        for (int i = 0; i < strarrayPathSpace1.length; i++)    {
            if (strarrayPathSpace1[i].equals(strarrayPathSpace2[i]) == false)    {
                return false;
            }
        }
        return true;
    }

    public static boolean isMFPSLibFileOrSubFile(String strPathParent, String strPathChild, LangFileManager lfm)    {
        String[] strarrayPathSpaceParent = cvtPath2PathSpace(strPathParent, lfm);
        String[] strarrayPathSpaceChild = cvtPath2PathSpace(strPathChild, lfm);
        return isMFPSLibFileOrSubFile(strarrayPathSpaceParent, strarrayPathSpaceChild);
    }

    public static boolean isMFPSLibFileOrSubFile(String[] strarrayPathSpaceParent, String[] strarrayPathSpaceChild)    {
        if (strarrayPathSpaceParent == null)    {
            return false;
        }
        if (strarrayPathSpaceChild == null)    {
            return false;
        }
        if (strarrayPathSpaceChild.length < strarrayPathSpaceParent.length)    {
            return false;
        }
        for (int i = 0; i < strarrayPathSpaceParent.length; i++)    {
            if (strarrayPathSpaceChild[i].equals(strarrayPathSpaceParent[i]) == false)    {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Extract user defined lib folder from path. If the lib has been included in m_slistUsrDefLibPath,
     * return the lib folder, otherwise, return the file's parent folder. If the path does not include
     * a \ (or / in unix), return "".
     * @param strRawPath : the canonical path of mfps file.
     * @return the user defined lib folder.
     */
    public static String getUserDefFolderFromPath(String strRawPath) {
        for (DefLibPath defLibPath : m_slistUsrDefLibPath) {
            // first test if defLibPath.mstrLibPath is a valid directory. If not, jump to next defLibPath.
            File fLib = new File(defLibPath.mstrLibPath);
            try {
                if (!fLib.isDirectory()) {
                    continue;
                }
            } catch(Exception e) {
                continue;
            }
            // it is a valid directory.
            if (strRawPath.length() >= defLibPath.mstrLibPath.length()
                    && strRawPath.substring(0, defLibPath.mstrLibPath.length()).equals(defLibPath.mstrLibPath)) {
                return defLibPath.mstrLibPath;
            }
        }
        // ok, it is not included in m_slistUsrDefLibPath or it is a single file included in m_slistUsrDefLibPath
        int idxDiv = strRawPath.lastIndexOf(LangFileManager.STRING_PATH_DIVISOR);
        if (idxDiv == -1) {
            idxDiv = 0;
        }
        // skip duplicates STRING_PATH_DIVISORs
        while (idxDiv > 0 && strRawPath.substring(idxDiv - 1).indexOf(LangFileManager.STRING_PATH_DIVISOR) == 0) {
            idxDiv --;
        }
        return strRawPath.substring(0, idxDiv);
    }

    /**
     * This function set a path space to be predefined lib path sapce
     * @param strarrayPathSpace: The path space string array to set.
     * @return : true if set successfully. Otherwise false
     */
    public static boolean setPredefinedLibPathSpace(String[] strarrayPathSpace) {
        if (strarrayPathSpace.length > 0) {
            strarrayPathSpace[0] = "\u0000asset_pre_defined"; // this means it is an internal predefined lib. Note that char null is not allowed in Unix/Windows's path name
            return true;
        } else {
            return false;
        }
    }

    /**
     * This function identify if a path space is for predefined lib
     * @param strarrayPathSpace
     * @return -1 if it is built-in, 0 if it is predefined, 1 if it is user defined and 2 if it is command input.
     */
    public static int getLibPathSpaceType(String[] strarrayPathSpace) {
        if (strarrayPathSpace == null) {
            return -1;
        } else if (strarrayPathSpace.length > 0 && strarrayPathSpace[0].length() > 0 && strarrayPathSpace[0].charAt(0) == '\u0000') {
            if (strarrayPathSpace[0].equals("\u0000" + LangFileManager.STRING_COMMAND_INPUT_FILE_PATH)) {
                return 2;   // command input
            } else {
                return 0;   // predefined
            }
        } else {
            return 1;   // user defined if it is not null and not started by "\u0000"
        }
    }
    /**
     * note that this function is different from CvtPath2PathSpace in MFPFileManagerActivity bcoz it has a m_sstrRootPath.
     * The standard is here:
     * 1. Don't use small case path. Let user looks after case. In other words, app folder is case sensative;
     * 2. Any sub folder in app folder is case insensative and user guarantee a sub folder is unique.
     * @param strPathRaw: the canonical path of mfps file. If it is an Android asset file,
     *  it should be predef_lib/... (not canonical)
     * @param lfm
     * @return
     */
    public static String[] cvtPath2PathSpace(String strPathRaw, LangFileManager lfm)    {
        String strPath = strPathRaw;
        
        if (strPathRaw.length() >= lfm.getPreDefLibFullPath().length()
                && strPathRaw.substring(0, lfm.getPreDefLibFullPath().length()).equals(lfm.getPreDefLibFullPath()))    {    // predefined lib
            // this must be predefined lib path.
            strPath = strPath.substring(lfm.getPreDefLibFullPath().length());
            int i;
            for (i = 0; i < strPath.length(); i ++)    {
                if (strPath.substring(i, i + 1).equals(LangFileManager.STRING_PATH_DIVISOR) == false)    {
                    break;
                }
            }
            strPath = strPath.substring(i);
            String[] strarray;
            if (strPath.length() == 0) {
                strarray = new String[0];   // this is top level path.
            } else {
                String strPathDivisor4Regex = LangFileManager.STRING_PATH_DIVISOR;
                if (LangFileManager.STRING_PATH_DIVISOR.equals("\\")) {
                    strPathDivisor4Regex = "\\\\";
                }
                strarray = strPath.split(strPathDivisor4Regex + "+");
            }
            LinkedList<String> listPathSpace = new LinkedList<String>();
            for (i = 0; i < strarray.length; i ++)    {
                if (strarray[i].equals(".."))    {
                    if (listPathSpace.isEmpty())    {
                        // this means it is not a valid path inside root folder.
                        return null;
                    } else    {
                        listPathSpace.removeLast();
                    }
                } else if (strarray[i].equals(".") == false)    {
                    listPathSpace.addLast(strarray[i]);
                }
            }
            String[] strarrayPathSpace = new String[listPathSpace.size() + 1];
            setPredefinedLibPathSpace(strarrayPathSpace);
            int nSize = listPathSpace.size();
            for (i = 0; i < nSize; i ++)    {
                strarrayPathSpace[i + 1] = listPathSpace.remove();
            }
            return strarrayPathSpace;
        } else { // else, any path would be a user defined lib.
            String strUserDefFolder = getUserDefFolderFromPath(strPathRaw);
            strPath = strPath.substring(strUserDefFolder.length());
            int i;
            for (i = 0; i < strPath.length(); i ++)    {
                if (strPath.substring(i, i + 1).equals(LangFileManager.STRING_PATH_DIVISOR) == false)    {
                    break; // this guarantees that the first string is not "", So not ambiguity.
                }
            }
            strPath = strPath.substring(i);
            String[] strarray;
            if (strPath.length() == 0) {
                strarray = new String[0];   // this is top level path.
            } else {
                String strPathDivisor4Regex = LangFileManager.STRING_PATH_DIVISOR;
                if (LangFileManager.STRING_PATH_DIVISOR.equals("\\")) {
                    strPathDivisor4Regex = "\\\\";
                }
                strarray = strPath.split(strPathDivisor4Regex + "+");
            }
            LinkedList<String> listPathSpace = new LinkedList<String>();
            for (i = 0; i < strarray.length; i ++)    {
                if (strarray[i].equals(".."))    {
                    if (listPathSpace.isEmpty())    {
                        // this means it is not a valid path inside root folder.
                        return null;
                    } else    {
                        listPathSpace.removeLast();
                    }
                } else if (strarray[i].equals(".") == false)    {
                    listPathSpace.addLast(strarray[i]);
                }
            }
            String[] strarrayPathSpace = new String[listPathSpace.size() + 1];
            strarrayPathSpace[0] = strUserDefFolder;   // an external lib folder
            int nSize = listPathSpace.size();
            for (i = 0; i < nSize; i ++)    {
                strarrayPathSpace[i+1] = listPathSpace.remove();
            }
            return strarrayPathSpace;
        }
    }
    
    // note that this function is different from CvtPathSpace2Path in MFPFileManagerActivity
    // bcoz it has a m_sstrRootPath.
    public static String cvtPathSpace2Path(String[] strarrayPathSpace)    {
        // assume m_sstrRootPath always not ended by "/"
        if (strarrayPathSpace == null || strarrayPathSpace.length == 0) {
            // invalid path space, getLibPathSpaceType(strarrayPathSpace) == -1 in this case.
            return "";
        }
        String strPath;
        switch (getLibPathSpaceType(strarrayPathSpace)) {
            case 2:
                return "";  // command input.
            case 0:
                // internal mfps lib
                strPath = strarrayPathSpace[0];    // this will not cause any confusion with real file path.
                break;
            default:
                // external mfps lib (user defined)
                // this is one of the user defined lib path. Note that the characters' cases have been
                // determined in the first palace when user add user defined lib folder.
                strPath = strarrayPathSpace[0];
                break;
        }
        for (int i = 1; i < strarrayPathSpace.length; i ++)    {
            strPath += LangFileManager.STRING_PATH_DIVISOR + strarrayPathSpace[i];
        }
        return strPath;
    }

    // this function returns file extension from file name.
    public static String getFileExtFromName(String strFileName)    {
        // always assume strFileName is not null and assume this is a file not folder
        int nExtInitIndex = strFileName.lastIndexOf(LangFileManager.STRING_EXTENSION_INITIAL);
        int nDivLastIndex = strFileName.lastIndexOf(LangFileManager.STRING_PATH_DIVISOR);
        if (nExtInitIndex == -1 || nExtInitIndex <= nDivLastIndex)    {
            // no extension
            return "";
        } else {
            return strFileName.substring(nExtInitIndex);
        }
    }
    
    public static boolean copyAsset2SD(LangFileManager lfm, String strSrcPath, String strDestPath, int nIterLevel) {
        nIterLevel++;
        if (nIterLevel > INT_ASSET_PATH_MAX_CHILD_LEVEL)    {
            // prevent stack overflow exception here
            return false;
        }
        String strAssetFiles[] = null;
        boolean bReturnValue = true;
        try {
            strAssetFiles = lfm.list(strSrcPath, lfm.isOnAndroid());    // LangFileManager list a file (whether exists or not) returns a empty list.
            if (strAssetFiles.length == 0) {
                if (lfm.isOnAndroid()) {
                    // this is a file but not the .jar binary file.
                    if (!strSrcPath.equalsIgnoreCase(LangFileManager.STRING_ASSET_JMATHCMD_JAR_FILE)
                            && !strSrcPath.equalsIgnoreCase(LangFileManager.STRING_ASSET_JMFPLANG_JAR_FILE)
                            && !strSrcPath.equalsIgnoreCase(LangFileManager.STRING_ASSET_MFPLANG_CMD_FILE)
                            && !strSrcPath.equalsIgnoreCase(LangFileManager.STRING_ASSET_MFPLANG_SH_FILE))    {
                        if (copyAssetFile2SD(lfm, strSrcPath, strDestPath) == false)    {
                            return false;
                        }
                    }
                } else if (strSrcPath.substring(strSrcPath.length() - LangFileManager.STRING_SCRIPT_EXTENSION.length())
                        .toLowerCase(Locale.US).equals(LangFileManager.STRING_SCRIPT_EXTENSION) // toLowerCase is required here to convert .MFPS to .mfps
                        && (strSrcPath.equals(lfm.getPreDefLibFullPath()
                                            + LangFileManager.STRING_PATH_DIVISOR
                                            + LangFileManager.STRING_ASSET_SCRIPT_MATH_LIB_FILE)
                            || strSrcPath.equals(lfm.getPreDefLibFullPath()
                                                + LangFileManager.STRING_PATH_DIVISOR
                                                + LangFileManager.STRING_ASSET_SCRIPT_MISC_LIB_FILE))) {
                    // on JAVA platform. this is a script and it is math lib script or misc lib script.
                    if (copyAssetFile2SD(lfm, strSrcPath, strDestPath) == false)    {
                        return false;
                    }
                }
            } else     {
                // this is a folder
                File dir = new File(strDestPath);
                if (!dir.exists())    {
                    if (!dir.mkdirs())    {
                        return false;    // cannot create destination folder
                    }
                }
                for (int i = 0; i < strAssetFiles.length; ++i) {
                    String strChildSrcPath = strAssetFiles[i];
                    if (strSrcPath.equals("") == false)    {
                        strChildSrcPath = strSrcPath + LangFileManager.STRING_PATH_DIVISOR + strAssetFiles[i];
                    } else    {
                        if (strChildSrcPath.trim().equals(""))    {
                            // this situation might occur since user does report stack overflow.
                            // but this might be a feature of new Android OS.
                            return false;
                        }
                    }
                    boolean bThisCpyReturn = copyAsset2SD(lfm, strChildSrcPath,
                                strDestPath + LangFileManager.STRING_PATH_DIVISOR + strAssetFiles[i],
                                nIterLevel);
                    if (!bThisCpyReturn) {
                        bReturnValue = false;
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return bReturnValue;
    }

    public static boolean copyAssetFile2SD(LangFileManager lfm, String strSrcPath, String strDestPath) {

        InputStream in = null;
        OutputStream out = null;
        try {
            in = lfm.open(strSrcPath, lfm.isOnAndroid());    // same as FileInputStream function in JAVA.
            File dir = new File(strDestPath);
            if (dir.getParentFile() != null && !dir.getParentFile().exists())    {
                if (!dir.getParentFile().mkdirs())    {
                    return false;    // cannot create destination folder
                }
            }
            out = new FileOutputStream(strDestPath);

            int read;
            while ((read = in.read(m_sbyteBuffer)) != -1) {
                out.write(m_sbyteBuffer, 0, read);
            }
            out.flush();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
        	if (in != null) {
        		try {
					in.close();
				} catch (IOException e) {
					// do nothing but print Stack
					e.printStackTrace();
				}
        	}
        	if (out != null) {
        		try {
					out.close();
				} catch (IOException e) {
					// do nothing but print Stack
					e.printStackTrace();
				}
        	}
        }

    }

    // strPath here must be canonical
    public static void getSysLibFiles(LangFileManager lfm, String strPath)  {
        // this function will refresh syslib.
        if (m_slistSysDefLibPath.size() > 0) {
            m_slistSysDefLibPath.clear();
        }
        DefLibPath defLibPath = new DefLibPath();
        m_slistSysDefLibPath.add(defLibPath);
        getLibFiles(lfm, strPath, lfm.isOnAndroid(), defLibPath);
    }
    
    // in this function, we assume
    // 1. strPath can be either a file or a folder
    // 2. inside the function strPath will be converted to canonical path with right character cases.
    public static void getUsrLibFiles(LangFileManager lfm, String strPath)  {
        DefLibPath defLibPath = null;
        String strCanonicalPath;
        try {
            strCanonicalPath = IOLib.getCanonicalPath(strPath);
        } catch (JFCALCExpErrException e) {
            strCanonicalPath = strPath;    // have to use original path input.
            e.printStackTrace();
        }
        for (int idx = 0; idx < m_slistUsrDefLibPath.size(); idx ++) {
            DefLibPath defAddedLibPath = m_slistUsrDefLibPath.get(idx);
            if (defAddedLibPath.mstrLibPath.equals(strCanonicalPath)) {
                // ok, the user lib folder has been added, so refresh it and move it to the beginning.
                defLibPath = m_slistUsrDefLibPath.remove(idx);
                break;
            }
        }
        if (defLibPath == null) {
            defLibPath = new DefLibPath();
        } else {
            defLibPath.clear();
        }
        getLibFiles(lfm, strCanonicalPath, false, defLibPath);
        // now add the user lib path to the tail of the list.
        // this implies that the later added lib will have a higher priority than
        // previously added lib.
        m_slistUsrDefLibPath.add(defLibPath);
    }

    /**
     * get Lib files for predefined lib and user defined lib.
     * @param lfm : language file manager
     * @param strPath : this must be a canonical path if not an Android asset file, or relative path if an Android asset.
     * @param bAndroidAsset : in Android asset?
     * @param defLibPath
     */
    public static void getLibFiles(LangFileManager lfm, String strPath, boolean bAndroidAsset, DefLibPath defLibPath)
    {
        // here we do not identify if the path is a valid android asset lib path if we are in Android asset.
        // we check this in the bottom of the function.
        defLibPath.mstrLibPath = strPath;
        if (bAndroidAsset) {
            // this is an Android asset path
            defLibPath.mnLastModifiedTime = 0;
        } else {
            File file4Path = new File(strPath);
            defLibPath.mnLastModifiedTime = file4Path.lastModified();
        }
        if (!lfm.isDirectory(strPath, bAndroidAsset)) {    // this is a file (not that we do not validate if it is a valid mfps file here.
            defLibPath.mlSubLibPath = null;
        } else {    // this is a folder.
            defLibPath.mlSubLibPath = new LinkedList<DefLibPath>();
            String[] strSubFolderOrFiles; 
            try    {
                strSubFolderOrFiles = lfm.list(strPath, bAndroidAsset);
            } catch(IOException e)    {
                strSubFolderOrFiles = new String[0];
            }
            
            for (int index = 0; index < strSubFolderOrFiles.length; index ++)    {
                String strThisChild = strSubFolderOrFiles[index];
                String strLeafPath = strPath + LangFileManager.STRING_PATH_DIVISOR + strThisChild;
                // now identify strLeafPath is a folder or a file. Note that on JAVA, this is quite simple
                // i.e. File leafFile = new File(strLeafPath). However, in Android, no way to identify if a
                // asset path is a folder in old Android releases. So developer made a convention that if a
                // path is ended with _lib, it is a lib fold. The code to identify a path is a lib folder
                // would be:
                // strThisChild.length() >= STRING_ASSET_SCRIPT_LIB_FOLDER_EXTENSION.length()
                // && strThisChild.substring(strThisChild.length() - STRING_ASSET_SCRIPT_LIB_FOLDER_EXTENSION.length())
                // .toLowerCase(Locale.US).equals(STRING_ASSET_SCRIPT_LIB_FOLDER_EXTENSION)
                boolean bAddThisPath = false;
                if (lfm.isDirectory(strLeafPath, bAndroidAsset))    {
                    // this is a lib
                    bAddThisPath = true;    // now example folder is standalone, so always load a folder.
                } else if (strLeafPath.substring(strLeafPath.length() - LangFileManager.STRING_SCRIPT_EXTENSION.length())
                          .toLowerCase(Locale.US).equals(LangFileManager.STRING_SCRIPT_EXTENSION)) { // toLowerCase is required here to convert .MFPS to .mfps
                    // this is an mfps script file
                    if (!bAndroidAsset) {
                        // this is on normal JAVA platform, or on Android but not asset file. Every mfps is loaded
                        bAddThisPath = true;
                    } else if (strThisChild.toLowerCase(Locale.US).equals(LangFileManager.STRING_ASSET_SCRIPT_MATH_LIB_FILE)
                            || strThisChild.toLowerCase(Locale.US).equals(LangFileManager.STRING_ASSET_SCRIPT_MISC_LIB_FILE)
                            || strThisChild.toLowerCase(Locale.US).equals(LangFileManager.STRING_ASSET_SCRIPT_SIG_PROC_LIB_FILE))    {
                        // In Android asset we are loading predefined mfps, so it has to be math.mfps or misc.mfps or sig_proc.mfps
                        bAddThisPath = true;
                    }
                }
                if (bAddThisPath) {
                    DefLibPath defSubLibPath = new DefLibPath();
                    getLibFiles(lfm, strLeafPath, bAndroidAsset, defSubLibPath);
                    defLibPath.mlSubLibPath.add(defSubLibPath);
                }
            }
        }
    }
    
    public static void loadSysLib(LangFileManager lfm)    {
        for (DefLibPath defLibPath : m_slistSysDefLibPath) {
            loadLib(defLibPath, lfm.isOnAndroid(), lfm);
        }
    }
    
    public static void loadUsrLib(LangFileManager lfm)    {
        for (DefLibPath defLibPath : m_slistUsrDefLibPath) {
            loadLib(defLibPath, false, lfm);
        }
    }
    
    /**
     * load lib (can be either sys lib or user lib).
     * @param defLibPath
     * @param bAndroidAsset : if true, we are in Android and loading asset lib.
     * @param lfm
     */
    public static void loadLib(DefLibPath defLibPath, boolean bAndroidAsset, LangFileManager lfm)    {
        if (defLibPath != null) {
            if (defLibPath.mlSubLibPath == null)    {
                // this is a file
                loadFile(defLibPath.mstrLibPath, bAndroidAsset, lfm);
            } else  {
                ListIterator<DefLibPath> itr = defLibPath.mlSubLibPath.listIterator(); 
                while(itr.hasNext())    {
                    DefLibPath subDefLibPath = (DefLibPath)itr.next();
                    loadLib(subDefLibPath, bAndroidAsset, lfm);
                }
            }
        }
    } 
    
    /**
     * unload a user defined lib.
     * @param defLibPath
     * @param lfm
     */
    public static void unloadLib(DefLibPath defLibPath, LangFileManager lfm)    {
        if (defLibPath != null) {
            if (defLibPath.mlSubLibPath == null)    {
                // this is a file
                unloadFileOrFolder(defLibPath.mstrLibPath, lfm);
            } else  {
                ListIterator<DefLibPath> itr = defLibPath.mlSubLibPath.listIterator(); 
                while(itr.hasNext())    {
                    DefLibPath subDefLibPath = (DefLibPath)itr.next();
                    unloadFileOrFolder(subDefLibPath.mstrLibPath, lfm);
                }
            }
        }
    } 

    /* // comment this function because it cannot handle cached function call
    // in AEFunction, also it cannot handle parent class change (while child
    // class doesn't change).
    public static void reloadUsrLib(LangFileManager lfm)    {
        for (DefLibPath defLibPath : m_slistUsrDefLibPath) {
            reloadLib(defLibPath, lfm);
        }
    }*/
    
    /**
     * please note that reloadLib is only for user's lib, but we still need LangFileManager
     * because loadFileOrFolder need it.
     * @param defLibPath
     * @param lfm
     */
    /* // comment this function because it cannot handle cached function call
    // in AEFunction, also it cannot handle parent class change (while child
    // class doesn't change).
    public static void reloadLib(DefLibPath defLibPath, LangFileManager lfm)  {
        if (defLibPath != null) {
            File fileOrFolder = new File(defLibPath.mstrLibPath);
            long nLastModifiedTime = fileOrFolder.lastModified();
            if (nLastModifiedTime > defLibPath.mnLastModifiedTime)    {
                // this file or folder has been modified.
                unloadFileOrFolder(defLibPath.mstrLibPath, lfm);
                loadFileOrFolder(defLibPath.mstrLibPath, false, lfm);
                defLibPath.mnLastModifiedTime = nLastModifiedTime;
            } else if (defLibPath.mlSubLibPath != null) {
                // this is a folder, its sub folders or files may be modified.
                ListIterator<DefLibPath> itr = defLibPath.mlSubLibPath.listIterator(); 
                while(itr.hasNext())    {
                    DefLibPath subDefLibPath = (DefLibPath)itr.next();
                    reloadLib(subDefLibPath, lfm);
                }
            }
        }
    } */

    /**
     * unloadFileOrFolder needs LangFileManager because cvtPath2PathSpace needs it.
     * @param strPath
     * @param lfm
     */
    public static void unloadFileOrFolder(String strPath, LangFileManager lfm)    {
        // unloaded file should not be internal lib file (predefined lib).
        // return the number of functions unloaded.
        String[] strarrayPathSpace = cvtPath2PathSpace(strPath, lfm);
        if (strarrayPathSpace != null) {
            CitingSpaceDefinition.getTopCSD().unloadFileOrFolder(strarrayPathSpace);
        }
    }
    
    /**
     * load a file or a folder lib (can be user defined or sys defined)
     * @param strPath
     * @param bAndroidAsset
     * @param lfm
     * @return
     */
    public static int loadFileOrFolder(String strPath, boolean bAndroidAsset, LangFileManager lfm)    {
        int nNumofFailedLoading = 0;
    
        File f = new File(strPath);
        if (f.isDirectory())    {
            String[] strarrayChildren = f.list();
            if (strarrayChildren == null) {
                strarrayChildren = new String[0];
            }
            for (int i = 0; i < strarrayChildren.length; i ++)    {
                nNumofFailedLoading += loadFileOrFolder(strPath + LangFileManager.STRING_PATH_DIVISOR + strarrayChildren[i],
                                                        bAndroidAsset, lfm);
            }
        } else    {
            if (loadFile(strPath, bAndroidAsset, lfm) == false)    {
                nNumofFailedLoading ++;
            }
        }
        return nNumofFailedLoading;
    }
    
    /**
     * load functions in the file.
     * @param strFilePath : canonical path of the file.
     * @param bAndroidAsset : loading from an Android Asset file?
     * @param lfm
     * @return
     */
    public static boolean loadFile(String strFilePath, boolean bAndroidAsset, LangFileManager lfm)    {
        String[] strarrayPathSpace = cvtPath2PathSpace(strFilePath, lfm);
        if (strarrayPathSpace == null) {
            // path space is invalid.
            return false;
        } else if (strarrayPathSpace.length <= 1) {
            // if strarrayPathSpace.length == 0, it is not a valid lib, if == 1, it can only be a folder, not a file
            return false;
        } else if(getFileExtFromName(strFilePath).toLowerCase(Locale.US)    // this is required since .MFPS is also an mfps file
                .equals(LangFileManager.STRING_SCRIPT_EXTENSION) == false)    {
            // this file is not a script file.
            return false;
        }
            
        InputStream inputStream = null;
        if (!bAndroidAsset)    {
            // normal file
            try    {
                inputStream = new BufferedInputStream(new FileInputStream(strFilePath));
            } catch (FileNotFoundException e)    {
                // cannot find the file
                return false;
            }
        } else    {
            // asset file
            try {
                inputStream = lfm.open(strFilePath, bAndroidAsset);
            } catch(IOException e)    {
                // cannot open the file.
                return false;
            }
        }
        
        // using this way instead of BufferedReader, accelerate reading speed.
        String strFileContent = "";
        ByteArrayOutputStream baos = null;
        try    {
            baos = new ByteArrayOutputStream();
            int length = 0;
            while ((length = inputStream.read(m_sbyteBuffer)) != -1) {
                baos.write(m_sbyteBuffer, 0, length);
            }
            byte[] byteArray = baos.toByteArray();
            strFileContent = new String(byteArray, "UTF-8");
        } catch (Exception  e)    {
            // System.out.println("Cannot read file " + f.getPath());
            m_slFailedFilePaths.addLast(strFilePath);
            return false;
        } finally {
            try    {
                if (baos != null)    {
                    baos.close();
                }
            } catch (IOException e)    {
                
            }
            try    {
                if (inputStream != null)    {
                	inputStream.close();
                }
            } catch (IOException e)    {
                
            }
        }
        String[] strSrcLines = strFileContent.split("\n");
        
        try {
            return loadLibCodeString(strSrcLines, strFilePath, strarrayPathSpace);
        } catch (JMFPCompErrException ex) {
            Logger.getLogger(MFPAdapter.class.getName()).log(Level.SEVERE, null, ex);
            return true;    // we have to return true here because returning false means the whole file cannot be loaded.
        }
    }
        
    public static boolean loadLibCodeString(String[] strSrcLines, String strFilePath, String[] strarrayPathSpace) throws JMFPCompErrException {
        LinkedList<Statement> lAllStatements = new LinkedList<Statement>();
        LinkedList<String[]> lCitingSpaceStack = new LinkedList<String[]>();
        LinkedList<LinkedList<String[]>> lUsingCitingSpacesStack = new LinkedList<LinkedList<String[]>>();
        lUsingCitingSpacesStack.add(new LinkedList<String[]>());
        int nFunctionDeclarationPos = -1;   // the statement index of function declaration statement.
        int[] helpBlockPos = null;  // help block begin & end (if there is).
        boolean bInHelpBlock = false;
        int nFunctionLevel = 0;    // out-most function's function level is 0, inner function's level is 1, inner-inner is 2 ...
        int nHelpBlockStart = 0;
        int nHelpBlockEnd = 0;
        int nLastNonBlankLineNo = 0;
        int nClassDeclarationPos = -1;  // the statement index of class declaration statement.
        int nClassLevel = 0;    // out-most class's class level is 0, inner class's level is 1, inner-inner is 2 ...
        int nLineNo = 1;
        int nFunctionBlockStart = -1;   // this variable is only used to tell the starting point of incomplete function.
        int nClassBlockStart = -1;   // this variable is only used to tell the starting point of incomplete class.
        Statement sCurrent = null;
        int nLoadedFunctions = 0, nLoadedClasses = 0;
        for (String strLine : strSrcLines){
            Statement sLine = new Statement(strLine, strFilePath, nLineNo);
            if (bInHelpBlock == false && sCurrent != null)    {
                // this statement needs to be appended to last one (only if we are not in help block)
                sCurrent.concatenate(sLine);
            } else    {
                sCurrent = sLine;
            }
            if (bInHelpBlock == false && sCurrent.isFinishedStatement() == false)    {
                nLineNo ++;
                continue;
            }
            if (sCurrent.isEmpty()) {
                nLineNo ++;
                sCurrent = null;
                continue;
            }
            try    {
                sCurrent.analyze();
            } catch(JMFPCompErrException e)    {
                sCurrent.meAnalyze = e;
            } catch (Exception e)   {
                sCurrent.meAnalyze = e;
            }
            if (bInHelpBlock == false)    {
                lAllStatements.add(sCurrent);
                if (sCurrent.mstatementType != null)   {
                    if (sCurrent.mstatementType.getType().equals(Statement_function.getTypeStr()))    {
                        if (nFunctionLevel == 0) {
                            nFunctionBlockStart = lAllStatements.size() - 1;
                        }
                        if (nFunctionLevel == 0 && nClassLevel == 0) {	// we are in the outmost function.
                            // the function defined in this file
                            nFunctionDeclarationPos = lAllStatements.size() - 1;
                            helpBlockPos = null;
                            if (nHelpBlockStart > 0 && nHelpBlockEnd > 0 && nLastNonBlankLineNo == nHelpBlockEnd)    {
                                // block is above the function and no citingspace declaration between the help block
                                // and the function.
                                helpBlockPos = new int[2];
                                helpBlockPos[0] = nHelpBlockStart;
                                helpBlockPos[1] = nHelpBlockEnd;
                                nHelpBlockStart = nHelpBlockEnd = 0;
                            }
                            ((Statement_function)sCurrent.mstatementType).setCitingSpaces(lCitingSpaceStack, lUsingCitingSpacesStack);
                        }
                        nFunctionLevel ++;
                    } else if (sCurrent.mstatementType.getType().equals(Statement_endf.getTypeStr()))    {
                        //if (nFunctionLevel > 0)    // we cannot do this check because this will creat a function multiple times.
                        nFunctionLevel --;
                    	if (nFunctionLevel == 0 && nClassLevel == 0 && nFunctionDeclarationPos >= 0) {	// we are in the outmost function.
                            /* should not check function redefinition considering the following case:
                             * file a has function f1() and file b has function f1() as well. Now if a
                             * is deleted, f1() can still be called because b's f1 is valid now. 
                             */
                            Statement[] allStatementArray = lAllStatements.toArray(new Statement[0]);
                            FunctionEntry functionEntry = new FunctionEntry(strarrayPathSpace, (Statement_function)(allStatementArray[nFunctionDeclarationPos].mstatementType), nFunctionDeclarationPos,
                                                                            (Statement_endf)sCurrent.mstatementType, lAllStatements.size() - 1,
                                                                            helpBlockPos, strSrcLines, allStatementArray);
                            try {
                                CitingSpaceDefinition.getTopCSD().addMember(functionEntry);
                            } catch (JOoMFPErrException ex) {
                                // shouldn't throw the exception.
                                Logger.getLogger(MFPAdapter.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            nLoadedFunctions ++;
                    	}
                        if (nFunctionLevel < 0) {
                            nFunctionLevel = 0;    // ignore extra endf.
                        }
                    } else if (sCurrent.mstatementType.getType().equals(Statement_class.getTypeStr())) {
                        if (nClassLevel == 0) {
                            nClassBlockStart = lAllStatements.size() - 1;
                        }
                        if (nFunctionLevel == 0 && nClassLevel == 0) {
                            nClassDeclarationPos = lAllStatements.size() - 1;
                            helpBlockPos = null;
                            if (nHelpBlockStart > 0 && nHelpBlockEnd > 0 && nLastNonBlankLineNo == nHelpBlockEnd)    {
                                // block is above the function and no citingspace declaration between the help block
                                // and the function.
                                helpBlockPos = new int[2];
                                helpBlockPos[0] = nHelpBlockStart;
                                helpBlockPos[1] = nHelpBlockEnd;
                                nHelpBlockStart = nHelpBlockEnd = 0;
                            }
                        }
                        nClassLevel ++;
                    } else if (sCurrent.mstatementType.getType().equals(Statement_endclass.getTypeStr())) {
                        //if (nClassLevel > 0)     // we cannot do this check because this will create a class multiple times.
                        nClassLevel --;
                        if (nFunctionLevel == 0 && nClassLevel == 0 && nClassDeclarationPos >= 0) {
                            // ok, now let's parse the class. We need to ensure nClassDeclarationPos is not -1
                            int[] classStartEnd = new int[2];
                            classStartEnd[0] = nClassDeclarationPos;
                            classStartEnd[1] = lAllStatements.size() - 1;
                            MFPClassDefinition mfpClassDef =
                                    ClassAnalyzer.analyzeClass(strarrayPathSpace,
                                                               lAllStatements.toArray(new Statement[0]),
                                                               strSrcLines,
                                                               classStartEnd,
                                                               lCitingSpaceStack,
                                                               lUsingCitingSpacesStack);
                            ClassAnalyzer.addClass2CitingSpace(mfpClassDef);
                            nLoadedClasses ++;
                        }
                        if (nClassLevel < 0) {
                            nClassLevel = 0;    // ignore extra endclass.
                        }
                    } else if (nFunctionLevel == 0 && nClassLevel == 0 && sCurrent.mstatementType.getType().equals(Statement_citingspace.getTypeStr())) {
                        // convert relative CS to absolute CS
                        Statement_citingspace csStatementType = (Statement_citingspace)sCurrent.mstatementType;
                        csStatementType.convertRelativeCS2Absolute(lCitingSpaceStack.peek());
                        CitingSpaceDefinition csd = new CitingSpaceDefinition(csStatementType.m_strarrayCitingSpace, strarrayPathSpace);
                        try {
                            csd.LinkCitingSpaceDefinition();
                        } catch (JOoMFPErrException ex) {
                            // shouldn't throw this exception.
                            Logger.getLogger(MFPAdapter.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        lCitingSpaceStack.push(csStatementType.m_strarrayCitingSpace);
                        lUsingCitingSpacesStack.push(new LinkedList<String[]>());
                    } else if (nFunctionLevel == 0 && nClassLevel == 0 && sCurrent.mstatementType.getType().equals(Statement_endcs.getTypeStr())) {
                        lCitingSpaceStack.poll(); // lCitingSpaceStack could be empty before poll
                        if (lUsingCitingSpacesStack.size() > 1) {
                            lUsingCitingSpacesStack.poll();  // keep using citing space for the top level.
                        }
                    } else if (nFunctionLevel == 0 && nClassLevel == 0 && sCurrent.mstatementType.getType().equals(Statement_using.getTypeStr())) {
                        Statement_using usingStatementType = (Statement_using)sCurrent.mstatementType;
                        usingStatementType.convertRelativeCS2Absolute(lCitingSpaceStack.peek());
                        lUsingCitingSpacesStack.getFirst().add(usingStatementType.m_strarrayCitingSpace);
                    } else if (sCurrent.mstatementType.getType().equals(Statement_help.getTypeStr()))    {
                        bInHelpBlock = true;
                        nHelpBlockStart = nLineNo;
                        nHelpBlockEnd = 0;
                    }
                }
            } else if (sCurrent.mstatementType != null
                        && sCurrent.mstatementType.getType().equals(Statement_endh.getTypeStr()))    {
                lAllStatements.add(sCurrent);
                bInHelpBlock = false;
                nHelpBlockEnd = sCurrent.mnStartLineNo;    // do not use nLineNo; because endh may be followed by _.
            }
            if (strLine.trim().length() > 0) {
                nLastNonBlankLineNo = nLineNo;
            }
            sCurrent = null;
            nLineNo ++;
        }
        
        if (nFunctionLevel > 0) {
            throw new JMFPCompErrException(strFilePath,
                        lAllStatements.get(nFunctionBlockStart).mnStartLineNo,
                        lAllStatements.get(nFunctionBlockStart).mnEndLineNo,
                        ErrorProcessor.ERRORTYPES.INCOMPLETE_FUNCTION);
        }
        if (nClassLevel > 0) {
            throw new JMFPCompErrException(strFilePath,
                        lAllStatements.get(nClassBlockStart).mnStartLineNo,
                        lAllStatements.get(nClassBlockStart).mnEndLineNo,
                        ErrorProcessor.ERRORTYPES.INCOMPLETE_CLASS);
        }
        if (nLoadedFunctions == 0 && nLoadedClasses == 0) {
            throw new JMFPCompErrException(strFilePath,
                        1, strSrcLines.length,
                        ErrorProcessor.ERRORTYPES.NO_VALID_DEFINITION);
            
        }
        return true;    // means successful
    }
        
    public static FunctionEntry loadSession(String[] strlistSession) throws JMFPCompErrException    {
        LinkedList<Statement> lAllStatements = new LinkedList<Statement>();
        String strSessionName = "";
        Statement sSessionStart = new Statement("function session_function()", strSessionName, 0);
        sSessionStart.analyze();
        lAllStatements.add(sSessionStart);
        boolean bInHelpBlock = false;
        Statement sCurrent = null;
        for (int idx = 0; idx < strlistSession.length; idx ++)  {
            Statement sLine = new Statement(strlistSession[idx], strSessionName, idx + 1);
            if (bInHelpBlock == false && sCurrent != null)    {
                // this statement needs to be appended to last one (only if not in a help block)
                sCurrent.concatenate(sLine);
            } else    {
                sCurrent = sLine;
            }
            if (bInHelpBlock == false && sCurrent.isFinishedStatement() == false)    {
                continue;
            }
            if (sCurrent.isEmpty()) {   // skip any empty statement.
                sCurrent = null;
                continue;
            }
            try    {
                sCurrent.analyze();
            } catch(JMFPCompErrException e)    {
                // analyzing might trigger some exceptions which should be ignored.
                sCurrent.meAnalyze = e;
            } catch (Exception e)   {
                // analyzing might trigger some exceptions which should be ignored.
                sCurrent.meAnalyze = e;
            }
            if (bInHelpBlock == false)    {
                lAllStatements.add(sCurrent);
                if (sCurrent.mstatementType != null
                        && sCurrent.mstatementType.getType().equals(Statement_help.getTypeStr()))    {
                    bInHelpBlock = true;
                }
            } else if (sCurrent.mstatementType != null
                        && sCurrent.mstatementType.getType().equals(Statement_endh.getTypeStr()))    {
                lAllStatements.add(sCurrent);
                bInHelpBlock = false;
            }
            sCurrent = null;
        }
        Statement sSessionEnd = new Statement(Statement_endf.getTypeStr(), strSessionName, strlistSession.length + 1);
        sSessionEnd.analyze();
        lAllStatements.add(sSessionEnd);

        FunctionEntry functionEntry = new FunctionEntry(new String[0], (Statement_function)(sSessionStart.mstatementType), 0,
                                                        (Statement_endf)(sSessionEnd.mstatementType), lAllStatements.size() - 1,
                                                        new int[0], strlistSession, lAllStatements.toArray(new Statement[0]));
        return functionEntry;
    }

    /**
     * This function analyses statement for all the MFP defined functions in CitingSpaceDefinition.getTopCSD().
     * This means the statement will be converted to an AbstractExpr.
     */
    public static void analyseStatements() {
        LinkedList<MemberFunction> listAllMfs = CitingSpaceDefinition.getTopCSD().getAllFunctionDefRecur();
        for (MemberFunction mf : listAllMfs) {
            if (mf instanceof FunctionEntry) {
                // ok, this is an MFP defined function.
                int nStartPos = ((FunctionEntry) mf).getStartStatementPos();
                int nEndPos = ((FunctionEntry) mf).getEndStatementPos();
                if (nEndPos < 0) {
                	nEndPos = ((FunctionEntry) mf).getStatementLines().length;
                }
                for (int idx = nStartPos + 1; idx < nEndPos; idx ++) {
                	((FunctionEntry) mf).getStatementLines()[idx].analyze2((FunctionEntry) mf);
                }
            }
        }
    }
    
    /**
     * load internal function info. strFilePath must be a valid android asset file if working on Android.
     * @param lfm
     * @param strFilePath
     */
    public static void loadInternalFuncInfo(LangFileManager lfm, String strFilePath)    {
        InputStream inputStream = null;
        try {
            inputStream = lfm.open(strFilePath, lfm.isOnAndroid());
        } catch(IOException e)    {
            // cannot open the file.
            return;
        }
        // using this way instead of BufferedReader, accelerate reading speed.
        String strFileContent = "";
        ByteArrayOutputStream baos = null;
        try    {
            baos = new ByteArrayOutputStream();
            int length = 0;
            while ((length = inputStream.read(m_sbyteBuffer)) != -1) {
                baos.write(m_sbyteBuffer, 0, length);
            }
            byte[] byteArray = baos.toByteArray();
            strFileContent = new String(byteArray, "UTF-8");
        } catch (Exception  e)    {
            // System.out.println("Cannot read file " + f.getPath());
            return;
        } finally {
            try    {
                if (baos != null)    {
                    baos.close();
                }
            } catch (IOException e)    {
                
            }
            try    {
                if (inputStream != null)    {
                    inputStream.close();
                }
            } catch (IOException e)    {
                
            }
        }
        
        String[] strAllLines = strFileContent.split("\n");
        int nCurrentIdx = 0;
        while(nCurrentIdx < strAllLines.length && strAllLines[nCurrentIdx].trim().length() == 0) {
            nCurrentIdx ++; // remove all the blank lines in the very beginning.
        }
        InternalFuncInfo internalFuncInfoNew = null;
        while (nCurrentIdx < strAllLines.length){
            String strLine = strAllLines[nCurrentIdx];
            nCurrentIdx ++;
            String[] strlistFuncInfo = strLine.trim().split("\\s+");
            int nNumofParams = 0;
            if (strlistFuncInfo.length != 3 || strlistFuncInfo[0].equals(""))    {
                return;
            } else if (strlistFuncInfo[2].equalsIgnoreCase("true") == false
                    && strlistFuncInfo[2].equalsIgnoreCase("false") == false)    {
                return;
            } else    {
                try {
                    nNumofParams = Integer.parseInt(strlistFuncInfo[1]);
                } catch (NumberFormatException e)    {
                    return;
                }
                if (nNumofParams < 0)    {
                    return;
                }
            }
            internalFuncInfoNew = new InternalFuncInfo();
            internalFuncInfoNew.mstrFuncName = strlistFuncInfo[0].toLowerCase(Locale.US);
            internalFuncInfoNew.mnLeastNumofParams = nNumofParams;
            internalFuncInfoNew.mbOptParam = Boolean.parseBoolean(strlistFuncInfo[2]);
            
            boolean bInHelpBlock = false;
            String strHelpBlockTotal = "";
            while (nCurrentIdx < strAllLines.length)    {
                String strFollowingLine = strAllLines[nCurrentIdx];
                nCurrentIdx ++;
                if (strFollowingLine.trim().equalsIgnoreCase(Statement_help.getTypeStr()))    {
                    bInHelpBlock = true;
                } else if (strFollowingLine.trim().equalsIgnoreCase(Statement_endh.getTypeStr()))    {
                    strHelpBlockTotal += "\n" + strFollowingLine;
                    internalFuncInfoNew.mstrlistHelpInfo = strHelpBlockTotal.split("\\n");
                    break;
                }
                if (bInHelpBlock)    {
                    if (strHelpBlockTotal.equals(""))    {
                        strHelpBlockTotal = strFollowingLine;
                    } else {
                        strHelpBlockTotal += "\n" + strFollowingLine;
                    }
                }
            }
            m_slInternalFuncInfo.add(internalFuncInfoNew);
        }
    }

    /**
     * if this function is called in Android, strFilePath must be equal to STRING_ASSET_MFP_KEY_WORDS_INFO_FILE
     * @param lfm
     * @param strFilePath
     */
    public static void loadMFPKeyWordsInfo(LangFileManager lfm, String strFilePath)    {
        InputStream inputStream = null;
        ByteArrayOutputStream baos = null;
        String strAllInfo = "";
        byte[] byteArray = null;
        try    {
            if (lfm.isOnAndroid()) {
                inputStream = lfm.open(LangFileManager.STRING_ASSET_MFP_KEY_WORDS_INFO_FILE, true);
            } else {
                inputStream = new FileInputStream(strFilePath);
            }
            baos = new ByteArrayOutputStream();
            int length = 0;
            while ((length = inputStream.read(m_sbyteBuffer)) != -1) {
                baos.write(m_sbyteBuffer, 0, length);
            }
            byteArray = baos.toByteArray();
            strAllInfo = new String(byteArray, "UTF-8");
        } catch    (Exception e)    {
            
        } finally    {
            try    {
                if (baos != null)    {
                    baos.close();
                }
                if (inputStream != null)    {
                    inputStream.close();
                }
            } catch (IOException e)    {
                
            }
        }
        
        String[] strItemInfos = strAllInfo.split("\n\u25C0\u25C0\u25C0\u25C0");
        for (String str: strItemInfos)    {
            String strThisItem = str.trim();
            if (strThisItem.length() == 0)    {
                continue;
            }
            String[] strThisItemInfos = strThisItem.split("\u25B6\u25B6\u25B6\u25B6\n");
            if (strThisItemInfos.length != 2)    {
                continue;
            }
            String strThisItemTitle = strThisItemInfos[0].trim();
            String[] strThisItemContents = strThisItemInfos[1].trim().split("\u25C1\u25C1\u25C1\u25C1");
            Map<String, String> mapAllLangsInfo = new HashMap<String, String>();
            String strDefault = "";
            for (int idx = 0; idx < strThisItemContents.length; idx ++)    {
                String strItr = strThisItemContents[idx];
                String[] strarrayTmp = strItr.split("\u25B7\u25B7\u25B7\u25B7\n");
                if (strarrayTmp.length != 2)    {
                    continue;
                }
                String strLang = strarrayTmp[0].trim().toLowerCase(Locale.US);  // this is converting file text, so toLowerCase is OK.
                mapAllLangsInfo.put(strLang, strarrayTmp[1]);
                if (strDefault.length() == 0 || strLang.compareTo("english") == 0)    {
                    strDefault = strarrayTmp[1];
                }
            }
            mapAllLangsInfo.put("default", strDefault);

            String[] strarrayKeyWords = strThisItemTitle.split(",");
            for (String strItr : strarrayKeyWords)    {
                String strKeyWord = strItr.trim();
                if (strKeyWord.length() != 0)    {
                    MFPKeyWordInfo mfpKeyWordInfo = new MFPKeyWordInfo();
                    mfpKeyWordInfo.mstrKeyWordName = strKeyWord;
                    mfpKeyWordInfo.mmapHelpInfo = mapAllLangsInfo;
                    m_slMFPKeyWordInfo.add(mfpKeyWordInfo);
                }
            }
            
        }
    }
    
    // get help information for all the functions having the same name.
    // strMFPKeyWord has been lower cased.
    public static String getMFPKeyWordHelp(String strMFPKeyWord, String strLang)    {
        for (MFPKeyWordInfo itr : m_slMFPKeyWordInfo)    {
            if (itr.mstrKeyWordName.equals(strMFPKeyWord))    {
                return itr.extractHelpFromMFPKeyWordInfo(strLang);
            }
        }
        return null;    // this is not a keyword with help info.
    }

    // get help information from a specific help block.
    public static String extractHelpFromBlock(String[] strLines, int nStartLine, int nEndLine, String strLang)    {
        String strReturn = "";
        String strDefault = "";
        boolean bLanguageFound = false;
        if (nStartLine > 0 && nStartLine < strLines.length
                && nEndLine > 0 && nEndLine <= strLines.length
                && nStartLine < nEndLine)    {
            boolean bInSubBlock = false;
            boolean bInProperSubBlock = false;
            boolean bInDefaultSubBlock = false;
            for (int index = nStartLine; index < nEndLine - 1; index++)    {
                // means this part of block is for all the language
                String strLine = strLines[index];
                String strTrimLine = strLine.trim();
                if (strTrimLine.length() > 0 && strTrimLine.charAt(0) == '@')    {
                    if (strTrimLine.compareToIgnoreCase("@end") == 0)    {
                        if (bInDefaultSubBlock == true)    {
                            bInDefaultSubBlock = false;
                        }
                        if (bInProperSubBlock == true)    {
                            bInProperSubBlock = false;
                        }
                        if (bInSubBlock == true)    {
                            bInSubBlock = false;
                        }
                    } else if (strTrimLine.length() >= "@language:".length())    {
                        if (strTrimLine.compareToIgnoreCase("@language:") == 0)    {
                            bInDefaultSubBlock = true;
                        }
                        if (strTrimLine.compareToIgnoreCase("@language:" + strLang) == 0)    {
                            bLanguageFound = true;
                            bInProperSubBlock = true;
                        }
                        bInSubBlock = true;
                    }
                } else {
                    if (bInSubBlock == false)    {
                        strReturn += strLines[index] + "\n";
                        strDefault += strLines[index] + "\n";
                    }
                    if (bInProperSubBlock)    {
                        strReturn += strLines[index] + "\n";
                    }
                    if (bInDefaultSubBlock)    {
                        strDefault += strLines[index] + "\n";
                    }
                }
            }
        }
        
        if (bLanguageFound == false)    {
            return strDefault;
        } else    {
            return strReturn;
        }
    }
    
    // get help information for the function.
    public static String getFunctionHelp(String strFuncName, int nNumofParam, boolean bIncludeOpt, String strLang, List<String[]> listCSes)    {
        LinkedList<MemberFunction> listMFs = CitingSpaceDefinition.lookupFunctionDef(strFuncName, nNumofParam, bIncludeOpt, listCSes);
        String strReturn = "";
        String strFuncDeclares = "";
        for (MemberFunction mf : listMFs) {
            if (mf instanceof FunctionEntry) {
                String strFuncDeclare = "";
                FunctionEntry fe = (FunctionEntry)mf;
                if (fe.getStatementFunction().m_bIncludeOptParam)    {
                    strFuncDeclare += fe.getStatementFunction().getFunctionNameWithCS() + "("
                                + (fe.getStatementFunction().m_strParams.length - 1) + "...)";
                } else    {
                    strFuncDeclare += fe.getStatementFunction().getFunctionNameWithCS() + "("
                            + fe.getStatementFunction().m_strParams.length + ")";                        
                }
                if (strFuncDeclares.indexOf(strFuncDeclare) >= 0)    {
                    // this function has been defined before, should not be included.
                    continue;
                }
                strFuncDeclares += ";" + strFuncDeclare;
                strReturn += strFuncDeclare + " :\n";
                if (fe.getListHelpBlock() != null)    {
                    strReturn += extractHelpFromBlock(fe.getStringLines(),
                                                fe.getListHelpBlock()[0],
                                                fe.getListHelpBlock()[1],
                                                strLang);                        
                }
            } else if (mf instanceof MFPClassDefinition.ConstructorFunction) {
                // this is for class help information.
                String strFuncDeclare = mf.getAbsNameWithCS();
                strFuncDeclares += ";" + strFuncDeclare;
                strReturn += strFuncDeclare + " :\n";
                MFPClassDefinition.ConstructorFunction cf = (MFPClassDefinition.ConstructorFunction)mf;
                if (cf.helpStatementsPos != null)    {
                    strReturn += extractHelpFromBlock(cf.rawLines,
                                                cf.helpStatementsPos[0],
                                                cf.helpStatementsPos[1],
                                                strLang);
                }
            } else {
                String strFuncDeclare = "";
                strFuncDeclares += ";" + strFuncDeclare;
                BaseBuiltInFunction builtInFunc = (BaseBuiltInFunction) mf;
                ListIterator<InternalFuncInfo> itrIFI = m_slInternalFuncInfo.listIterator();
                while (itrIFI.hasNext())    {
                    InternalFuncInfo ifi = itrIFI.next();
                    // both ifi.mstrFuncName and builtInFunc.getAbsNameWithCS() are small cased, so just use equals.
                    if (ifi.mstrFuncName.equals(builtInFunc.getAbsNameWithCS())
                            && (nNumofParam == -1
                                || (ifi.mbOptParam == bIncludeOpt
                                    && ifi.mnLeastNumofParams == nNumofParam)))    { // ifi def may be different from function def.
                        if (ifi.mbOptParam)    {
                            strFuncDeclare = ifi.mstrFuncName + "(" + ifi.mnLeastNumofParams + "...)";
                        } else    {
                            strFuncDeclare = ifi.mstrFuncName + "(" + ifi.mnLeastNumofParams + ")";
                        }
                        if (strFuncDeclares.contains(strFuncDeclare))    {
                            // this function has been defined before, should not be included.
                            continue;
                        }
                        strReturn += strFuncDeclare + " :\n";
                        strReturn += extractHelpFromBlock(ifi.mstrlistHelpInfo,
                                                        1,
                                                        ifi.mstrlistHelpInfo.length,
                                                        strLang);
                    }
                }
            }
        }
        
        return strReturn;
    }
    
    // get help information for all the functions having the same name.
    public static String getFunctionHelp(String strFuncName, String strLang, List<String[]> listCSes)    {
        return getFunctionHelp(strFuncName, -1, false, strLang, listCSes);
    }

    // return data recorded (string[0]) and data shown (string[1])
    public static String[] outputDatum(DataClass datumAnswer) throws JFCALCExpErrException    {
        if (DCHelper.isPrimitiveOrArray(datumAnswer)) {
            // do not use getClassName function because it is more straightforward to use instanceof.
            return outputBuiltInDatum(datumAnswer);
        } else {
            // no exception will be thrown.
            return new String[] {datumAnswer.output(), datumAnswer.output()};
        }
    }
    // a sub function called by outputDatum.
    private static String[] outputBuiltInDatum(DataClass datumAnswer) throws JFCALCExpErrException    {
        datumAnswer.validateDataClass(); // prevent refer to itself
        
        String[] strarrayReturn = new String[2];
        String strAnswerRecorded = new String();
        String strAnswerShown = new String();
        
        /* try to convert a double value to integer if we can */
        if (DCHelper.isDataClassType(datumAnswer, DATATYPES.DATUM_MFPDEC)
                && DCHelper.lightCvtOrRetDCMFPDec(datumAnswer).getDataValue().isActuallyInteger()) {
            datumAnswer = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, DCHelper.lightCvtOrRetDCSingleNum(datumAnswer).getDataValue().toIntOrNanInfMFPNum());
        }
        
        if (DCHelper.isDataClassType(datumAnswer, DATATYPES.DATUM_NULL))
        {
            strAnswerShown = strAnswerRecorded = "NULL";
        }
        else if (DCHelper.isDataClassType(datumAnswer, DATATYPES.DATUM_MFPBOOL))
        {
            /* If the answer is a boolean */
            DataClassSingleNum datumAns = DCHelper.lightCvtOrRetDCSingleNum(datumAnswer);
            if (datumAns.getDataValue().mType == MFPNumeric.Type.MFP_NAN_VALUE)  {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN);
            }
            else if (datumAns.getDataValue().isActuallyZero())
            {
                strAnswerShown = strAnswerRecorded = "FALSE";
            }
            else
            {
                strAnswerShown = strAnswerRecorded = "TRUE";
            }
        }
        else if (DCHelper.isDataClassType(datumAnswer, DATATYPES.DATUM_MFPINT))
        {
            /* If the answer is an integer, assume integer is always in a range of long */
            DataClassSingleNum datumAns = DCHelper.lightCvtOrRetDCSingleNum(datumAnswer);
            if (datumAns.getDataValue().isNanOrInf()) {
                strAnswerShown = strAnswerRecorded = datumAns.getDataValue().toString();
            } else if (isVeryBigorSmallValue(datumAns.getDataValue())
                    && !datumAns.getDataValue().isActuallyZero())  { // 0 is also a very small value but should not use scientific notation
                Format format = null;
                if (msnBitsofPrecision != -1)   {
                    String strTmp = "";
                    for (int idx = 0; idx < msnBitsofPrecision; idx ++) {
                        strTmp += "#";
                    }
                    if (msnBitsofPrecision == 0)    {
                        format = new DecimalFormat("0E0", new DecimalFormatSymbols(Locale.US));    // otherwise, 0.5 may be output as 0,5 in spanish
                    } else  {
                        format = new DecimalFormat("0." + strTmp + "E0", new DecimalFormatSymbols(Locale.US));
                    }
                } else  {
                    format = new DecimalFormat("0.0E0", new DecimalFormatSymbols(Locale.US));
                }
                MFPNumeric mfpNumIntValue = datumAns.getDataValue().toIntOrNanInfMFPNum();
                strAnswerRecorded = strAnswerShown = format.format(mfpNumIntValue.toBigInteger());
            } else {
                strAnswerRecorded = strAnswerShown = datumAns.getDataValue().toIntOrNanInfMFPNum().toString();                
            }
        }
        else if (DCHelper.isDataClassType(datumAnswer, DATATYPES.DATUM_MFPDEC))
        {
            Format format = null;
            DataClassSingleNum datumAns = DCHelper.lightCvtOrRetDCSingleNum(datumAnswer);
            if (datumAns.getDataValue().isNanOrInf()) {
                strAnswerShown = strAnswerRecorded = datumAns.getDataValue().toString();
            } else if (isVeryBigorSmallValue(datumAns.getDataValue())) {
                String strTmp = "";
                for (int idx = 0; idx < msnBitsofPrecision; idx ++) {
                    strTmp += "#";
                }
                if (msnBitsofPrecision != -1)   {
                    if (msnBitsofPrecision == 0)    {
                        format = new DecimalFormat("0E0", new DecimalFormatSymbols(Locale.US));
                    } else  {
                        format = new DecimalFormat("0." + strTmp + "E0", new DecimalFormatSymbols(Locale.US));
                    }
                } else  {
                    format = new DecimalFormat("0.0E0", new DecimalFormatSymbols(Locale.US));
                }
                strAnswerRecorded = strAnswerShown = format.format(datumAns.getDataValue().toBigDecimal());
            }
            else
            {
                String strTmp = "";
                for (int idx = 0; idx < msnBitsofPrecision; idx ++) {
                    strTmp += "#";
                }
                if (msnBitsofPrecision != -1)   {
                    if (msnBitsofPrecision == 0)    {
                        format = new DecimalFormat("0", new DecimalFormatSymbols(Locale.US));
                    } else  {
                        String strValueString = datumAns.getDataValue().toString();
                        int decimalIndex = strValueString.indexOf( '.' );
                        int nNumofZerosB4SigDig = 0;
                        if (decimalIndex != -1)    {    // this means no decimal point, it is an integer
                            int idx = 0;
                            for (idx = 0; idx < strValueString.length(); idx ++)    {
                                if (strValueString.charAt(idx) >= '1' && strValueString.charAt(idx) <= '9')    {
                                    break;    // siginificant digit start from here.
                                }
                            }
                            if (idx > decimalIndex)    {
                                nNumofZerosB4SigDig = idx - decimalIndex - 1;
                            }
                        }
                        String strTmpZeroB4SigDig = "";
                        for (int idx = 0; idx < nNumofZerosB4SigDig; idx ++) {
                            strTmpZeroB4SigDig += "#";
                        }
                        format = new DecimalFormat("0." + strTmpZeroB4SigDig + strTmp, new DecimalFormatSymbols(Locale.US));
                    }
                } else  {
                    format = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
                }
                strAnswerRecorded = strAnswerShown = format.format(datumAns.getDataValue().doubleValue());
            }
        }
        else if (DCHelper.isDataClassType(datumAnswer, DATATYPES.DATUM_COMPLEX))
        {
            boolean bImageNegative = false;
            DataClassComplex datumAns = DCHelper.lightCvtOrRetDCComplex(datumAnswer);
            if (datumAns.getImage().isActuallyNegative())    {    // this means NAN is always positive.
                bImageNegative = true;
            }
            String[] strOutputReal = outputDatum(datumAns.getRealDataClass());
            DataClass datumImage = datumAns.getImageDataClass();
            String strRealImageConn = "+";
            if (bImageNegative)    {
                datumImage = new DataClassSingleNum(datumImage.getDataClassType(), datumAns.getImage().negate());
                strRealImageConn = "-";
            }
            String[] strOutputImage = outputDatum(datumImage);
            if (datumAns.getReal().isActuallyZero() && datumAns.getImage().isActuallyZero())    {
                strAnswerShown = strAnswerRecorded = "0";
            } else if (datumAns.getReal().isActuallyZero())    {
                if (strOutputImage[1].equals("1"))    {
                    strAnswerRecorded = strAnswerShown = "i";
                } else if (datumAns.getImage().isNanOrInf()) {
                    // inf*i is not infi but nan + infi. So have to use infi here. Same as nani.
                    strAnswerRecorded = strOutputImage[0] + "i";
                    strAnswerShown = strOutputImage[1] + "i";
                } else    {
                    strAnswerRecorded = strOutputImage[0] + " * i";
                    strAnswerShown = strOutputImage[1] + " * i";
                }
                if (bImageNegative)    {
                    strAnswerRecorded = strRealImageConn + strAnswerRecorded;
                    strAnswerShown = strRealImageConn + strAnswerShown;
                }
            } else if (datumAns.getImage().isActuallyZero())    {
                strAnswerRecorded = strOutputReal[0];
                strAnswerShown = strOutputReal[1];
            } else if (datumAns.getImage().isNanOrInf()) {
                // inf*i is not infi but nan + infi. So have to use infi here. Same as nani.
                // for nani, need not to worry about strRealImageConn because it should be positive.
                strAnswerRecorded = strOutputReal[0] + " " + strRealImageConn + " " + strOutputImage[0] + "i";
                strAnswerShown = strOutputReal[1] + " " + strRealImageConn + " " + strOutputImage[1] + "i";
            } else    {
                strAnswerRecorded = strOutputReal[0] + " " + strRealImageConn + " " + strOutputImage[0] + " * i";
                strAnswerShown = strOutputReal[1] + " " + strRealImageConn + " " + strOutputImage[1] + " * i";
            }
        }
        else if (DCHelper.isDataClassType(datumAnswer, DATATYPES.DATUM_REF_DATA))
        {
            strAnswerRecorded = strAnswerShown = "[";
            DataClassArray datumAns = DCHelper.lightCvtOrRetDCArray(datumAnswer);
            for (int index = 0; index < datumAns.getDataListSize(); index ++)
            {
                if (index == (datumAns.getDataListSize() - 1))
                {
                    strAnswerRecorded += (datumAns.getDataList()[index] == null)?
                            outputDatum(new DataClassNull())[0]
                            :outputDatum(datumAns.getDataList()[index])[0];
                    strAnswerShown += (datumAns.getDataList()[index] == null)?
                            outputDatum(new DataClassNull())[1]
                            :outputDatum(datumAns.getDataList()[index])[1];
                }
                else
                {
                    strAnswerRecorded += (datumAns.getDataList()[index] == null)?
                            outputDatum(new DataClassNull())[0]
                            :outputDatum(datumAns.getDataList()[index])[0] + ", ";
                    strAnswerShown += (datumAns.getDataList()[index] == null)?
                            outputDatum(new DataClassNull())[1]
                            :outputDatum(datumAns.getDataList()[index])[1] + ", ";
                }
            }
            strAnswerRecorded = strAnswerShown += "]";
        }
        else if (DCHelper.isDataClassType(datumAnswer, DATATYPES.DATUM_STRING))
        {
            strAnswerShown = strAnswerRecorded = "\"" + StringEscapeUtils.escapeJava(DCHelper.lightCvtOrRetDCString(datumAnswer).getStringValue()) + "\"";
        }
        else if (DCHelper.isDataClassType(datumAnswer, DATATYPES.DATUM_REF_FUNC))
        {
            String funcName = DCHelper.lightCvtOrRetDCFuncRef(datumAnswer).getFunctionName();
            strAnswerRecorded = funcName + "()";
            strAnswerShown = "Function name: " + funcName;
        }
        else if (DCHelper.isDataClassType(datumAnswer, DATATYPES.DATUM_REF_EXTOBJ))
        {
        	strAnswerShown = strAnswerRecorded = datumAnswer.output();	// actually calls object.toString if object is not null.
        }
        else if (DCHelper.isDataClassType(datumAnswer, DATATYPES.DATUM_ABSTRACT_EXPR))
        {
            strAnswerShown = strAnswerRecorded = datumAnswer.output();  // actually calls getAExpr().output().
        }
        strarrayReturn[0] = strAnswerRecorded;
        strarrayReturn[1] = strAnswerShown;
        return strarrayReturn;
    }
    
    public static String outputException(Exception e)    {
        String strOutput = new String();
        /* If there is an error */
        if (e instanceof JFCALCExpErrException)    {
            JFCALCExpErrException eExp = (JFCALCExpErrException)e;
            String strError = eExp.m_se.getErrorInfo();
            strOutput = String.format("%s\n", strError);
            if (eExp.m_strBlockName != null)    {
                String strTmp1 = new String();
                strTmp1 = String.format("In function %s :\n", eExp.m_strBlockName);
                String strTmp2 = outputException(eExp.m_exceptionLowerLevel);
                strOutput += strTmp1 + strTmp2;
            }
        } else if(e instanceof JMFPCompErrException)    {
            JMFPCompErrException eMFP = (JMFPCompErrException)e;
            String strTmp1 = new String();
            if (eMFP.m_se.m_strSrcFile != null && eMFP.m_se.m_strSrcFile.trim().length() > 0) {
                strTmp1 = String.format("\t%s ", eMFP.m_se.m_strSrcFile);
            } else  {
                strTmp1 = String.format("\t");
            }
            if (eMFP.m_se.m_nStartLineNo == eMFP.m_se.m_nEndLineNo)    {
                strTmp1 += String.format("Line %d : ", eMFP.m_se.m_nStartLineNo);
            } else    {
                strTmp1 += String.format("Lines %d-%d : ", eMFP.m_se.m_nStartLineNo, eMFP.m_se.m_nEndLineNo);
            }
            String strError = eMFP.m_se.getErrorInfo();
            String strTmp2 = new String();
            strTmp2 = String.format("%s\n", strError);
            strOutput = outputException(eMFP.m_exceptionLowerLevel);
            strOutput = strTmp1 + strTmp2 + strOutput;
        } else if (e instanceof ScriptStatementException){
            ScriptStatementException eSSE = (ScriptStatementException)e;
            if (eSSE.m_statement.mstrFilePath != null && eSSE.m_statement.mstrFilePath.trim().length() > 0) {
                strOutput = String.format("\t%s ", eSSE.m_statement.mstrFilePath);
            } else {
                strOutput = String.format("\t");
            }
            if (eSSE.m_statement.mnStartLineNo == eSSE.m_statement.mnEndLineNo)    {
                strOutput += String.format("Line %d : %s\n", eSSE.m_statement.mnStartLineNo, e.toString());
            } else    {    
                strOutput += String.format("Lines %d-%d : %s\n", eSSE.m_statement.mnStartLineNo,
                                                                eSSE.m_statement.mnEndLineNo,
                                                                e.toString());
            }
        } else if (e instanceof JSmartMathErrException) {
            JSmartMathErrException eSM = (JSmartMathErrException)e;
            String strError = eSM.m_se.getErrorInfo();
            strOutput = String.format("%s\n", strError);
            if (eSM.m_strBlockName != null)    {
                String strTmp1 = new String();
                strTmp1 = String.format("In function %s :\n", eSM.m_strBlockName);
                String strTmp2 = outputException(eSM.m_exceptionLowerLevel);
                strOutput += strTmp1 + strTmp2;
            }
        } else if (e != null)    {
            strOutput = String.format("%s\n", e.toString());
        }
        return strOutput;
    }

    public static boolean isVeryBigorSmallValue(MFPNumeric mfpNumValue)
    {
        if (msnBigSmallThresh < 0)
            return false;
        if ((mfpNumValue.compareTo(mmfpNumBigThresh) >= 0)
                || (mfpNumValue.compareTo(mmfpNumBigThresh.negate()) <= 0)
                || ((mfpNumValue.compareTo(mmfpNumSmallThresh)) <= 0)
                    && (mfpNumValue.compareTo(mmfpNumSmallThresh.negate()) >= 0)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static double getPlotChartVariableFrom()
    {
        return msdPlotChartVariableFrom;
    }
    
    public static double getPlotChartVariableTo()
    {
        return msdPlotChartVariableTo;
    }

    public static int getWebRTCDebugLevel()
    {
        return msnWebRTCDebugLevel;
    }

    /**
     * This function returns a list of referred citingspaces from citing space stacks and 
     * using citing space statement. Note that citingspace structure is like
     * citingspace ..... // citingspace 1
     * using citingspace ..... // citingspace 2
     * using citingspace ..... // citingspace 3
     * citingspace ..... // citingspace 4
     * using citingspace ..... // citingspace 5
     * endcs
     * endcs
     * Then citingspace order is:
     * 4, 5, 1, 3, 2
     * @param lCitingSpaceStack : citingspaces referred by citingspace statements
     * @param lUsingCitingSpacesStack : citingspaces referred by using citingspace statements
     */
    public static List<String[]> getReferredCitingSpaces(LinkedList<String[]> lCitingSpaceStack,
                                                        LinkedList<LinkedList<String[]>> lUsingCitingSpacesStack) {
        LinkedList<String[]> lCitingSpaces = new LinkedList<String[]>();
        // do not add any duplicate
        int numberOfCses = 0;
        if (null != lCitingSpaceStack) {
            numberOfCses = lCitingSpaceStack.size();
        }
        
        boolean bHasBeenAdded = false;
        for (int idx = 0; idx < numberOfCses; idx ++) {
            // add citing space extracted from citing space stack first.
            String[] strarrayCS = lCitingSpaceStack.get(idx);
            bHasBeenAdded = false;
            for (String[] cs : lCitingSpaces) {
                if (Arrays.equals(cs, strarrayCS)) {
                    bHasBeenAdded = true;
                    break;
                }
            }
            if (!bHasBeenAdded) {
                lCitingSpaces.add(strarrayCS);
            }
            // then add using citing spaces.
            LinkedList<String[]> lUsingCitingSpace = lUsingCitingSpacesStack.get(idx);
            for (int idx1 = lUsingCitingSpace.size() - 1; idx1 >= 0; idx1 --) {    // using citing space reference always from bottom to top.
                bHasBeenAdded = false;
                for (String[] cs : lCitingSpaces) {
                    if (Arrays.equals(cs, lUsingCitingSpace.get(idx1))) {
                        bHasBeenAdded = true;
                        break;
                    }
                }
                if (!bHasBeenAdded) {
                    lCitingSpaces.add(lUsingCitingSpace.get(idx1));
                }
            }
        }
        
        // add top level citing space
        String[] strarrayTopLvlCS = new String[] { "" };
        bHasBeenAdded = false;
        for (String[] cs : lCitingSpaces) {
            if (Arrays.equals(cs, strarrayTopLvlCS)) {
                bHasBeenAdded = true;
                break;
            }
        }
        if (!bHasBeenAdded) {
            lCitingSpaces.add(strarrayTopLvlCS);
        }
        
        // add the extra UsingCitingSpace
        LinkedList<String[]> lUsingCitingSpace = lUsingCitingSpacesStack.get(numberOfCses);
        for (int idx1 = lUsingCitingSpace.size() - 1; idx1 >= 0; idx1 --) {    // using citing space reference always from bottom to top.
            bHasBeenAdded = false;
            for (String[] cs : lCitingSpaces) {
                if (Arrays.equals(cs, lUsingCitingSpace.get(idx1))) {
                    bHasBeenAdded = true;
                    break;
                }
            }
            if (!bHasBeenAdded) {
                lCitingSpaces.add(lUsingCitingSpace.get(idx1));
            }
        }
        
        List<String[]> lAllCSs = MFPAdapter.getAllCitingSpaces(lCitingSpaces);
        return lAllCSs;
    }

    // return all system additional visible citing spaces in order. Note that the returned list is unmodifiable.
    public static List<String[]> getSysAddCitingSpaces() {
        if (mslSysAddCitingSpaces.size() == 0) {
            //TODO:
            // hasn't been initialized yet.
            String[] strarrayMFP = new String[] {"", "mfp", "*"};   // with a star in the end means including sub spaces.

            // pay attention to the order.
            mslSysAddCitingSpaces.add(strarrayMFP);
        }
        
        return Collections.unmodifiableList(mslSysAddCitingSpaces);
    }
    
    // assume lCSs have been trimmed and small capitalized and no duplicate inside
    public static List<String[]> getAllCitingSpaces(List<String[]> lUniqueCSs) {
        LinkedList<String[]> lAllCSs = new LinkedList<String[]>();
        if (lUniqueCSs == null) {
            lAllCSs.add(new String[]{""});
        } else {
            lAllCSs.addAll(lUniqueCSs);
        }
        List<String[]> lSysAddCSs = getSysAddCitingSpaces();
        for (String[] csSysAdd : lSysAddCSs) {
            boolean bHasBeenAdded = false;
            for (String[] cs : lAllCSs) {
                if (Arrays.equals(cs, csSysAdd)) {
                    bHasBeenAdded = true;
                    break;
                }
            }
            if (!bHasBeenAdded) {
                lAllCSs.add(csSysAdd);
            }
        }

        return Collections.unmodifiableList(lAllCSs);
    }
    
    public static String addEscapes(String strInput)    {
        String strOutput = "";
        if (strInput != null)    {
            for (int i = 0; i < strInput.length(); i++)    {
                char cCurrent = strInput.charAt(i);
                if (cCurrent == '\"')    {
                    strOutput += "\\\"";
                } else if (cCurrent == '\\')    {
                    strOutput += "\\\\";
                } else    {
                    strOutput += cCurrent;
                }
            }
        }
        return strOutput;
    }

    /**
     * This function parses the command line input and translate it into a mfp statement.
     * @param strarrayFileParams : the command line arguments after file name.
     * @param mfpsExecInfo : the function name and parameter list defined in the execute_entry annotation.
     * @return null means an invalid command line, otherwise it is avalid string.
     */
    public static String parseScriptParams(String[] strarrayFileParams, com.cyzapps.adapter.MFPSProcessor.MFPSExecInfo mfpsExecInfo) {
        if (mfpsExecInfo.getParamList() == null) {
            // all parameters are looked on as strings.
            String strParams = "(";
            for (int idx = 0; idx < strarrayFileParams.length; idx ++) {
                strParams += "\"" + addEscapes(strarrayFileParams[idx]) + "\"";
                if (idx < strarrayFileParams.length - 1) {
                    strParams += ",";
                }
            }
            strParams += ")";
            return mfpsExecInfo.getFuncName() + strParams;
        } else {
            // match parameter pattern.            
            int nNumOfParams = strarrayFileParams.length;
            if (nNumOfParams < mfpsExecInfo.getListRepPlaces().size()) {
                return null;
            }
            
            String strParams = "(";
            int lastPlace = 0;
            for (int idx = 0; idx < mfpsExecInfo.getListRepPlaces().size(); idx ++) {
                strParams += mfpsExecInfo.getParamListWithoutOpt().substring(lastPlace, mfpsExecInfo.getListRepPlaces().get(idx));
                String strParam = strarrayFileParams[idx];
                if (mfpsExecInfo.getListRepType().get(idx)) {
                    // string
                    strParam = "\"" + addEscapes(strarrayFileParams[idx]) + "\"";
                }
                strParams += strParam;
                lastPlace = mfpsExecInfo.getListRepPlaces().get(idx) + 1;
            }
            strParams += mfpsExecInfo.getParamListWithoutOpt().substring(lastPlace, mfpsExecInfo.getParamListWithoutOpt().length());
            for (int idx = mfpsExecInfo.getListRepPlaces().size(); idx < strarrayFileParams.length; idx ++) {
                // note that idx == 0 is a very special case which means all the parameters are optional
                if (mfpsExecInfo.getWithStrOpt()) {
                    strParams += ((idx == 0)?"":", ") + "\"" + addEscapes(strarrayFileParams[idx]) + "\"";
                } else if (mfpsExecInfo.getWithValOpt()) {
                    strParams += ((idx == 0)?"":", ") + strarrayFileParams[idx];
                }
            }
            strParams += ")";
            return mfpsExecInfo.getFuncName() + strParams;
        }
    }
    
}
