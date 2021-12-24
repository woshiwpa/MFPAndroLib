// MFP project, MFPSProcessor.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Locale;

import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.IOLib;

public class MFPSProcessor {
    public static class MFPSExecInfo {
        private  String mstrFuncName = "";    // function name
        //public void setFuncName(String str) { mstrFuncName = str; }
        public String getFuncName() { return mstrFuncName; }
        private  String mstrParamList = "";    // function parameter list
        //public void setParamList(String str) { mstrParamList = str; }    // for unit test.
        public String getParamList() { return mstrParamList; }
        private LinkedList<Integer> mlistRepPlaces = new LinkedList<Integer>();
        public LinkedList<Integer> getListRepPlaces() { return mlistRepPlaces; }
        private LinkedList<Boolean> mlistRepType = new LinkedList<Boolean>();    // string is true.
        public LinkedList<Boolean> getListRepType() { return mlistRepType; }
        private Boolean mbWithStrOpt = false;
        public Boolean getWithStrOpt() { return mbWithStrOpt; }
        private Boolean mbWithValOpt = false;
        public Boolean getWithValOpt() { return mbWithValOpt; }
        private String mstrParamListWithoutOpt = "";
        public String getParamListWithoutOpt() { return mstrParamListWithoutOpt; }
        public Boolean isNeedsParamInput() {
            // if false, it means this file DEFINITELY needs no parameter input.
            if (mstrParamList == null) {
                return true;
            } else if (mlistRepPlaces.size() == 0 && !mbWithStrOpt && !mbWithValOpt) {
                return false;
            } else {
                return true;
            }
        }
    };
    
    protected String mstrFolderPath = "";    // canonical path
    protected String mstrFilePath = "";    // canonical path
    protected String mstrFileName = "";
    
    public MFPSProcessor(String strFilePath) throws IOException {
        String strCanonicalPath = strFilePath;
        try {
            strCanonicalPath = IOLib.getCanonicalPath(strFilePath);    // all path must be converted to canonical.
        } catch (JFCALCExpErrException e) {
            throw new IOException();
        }
        File f = new File(strCanonicalPath);
        mstrFolderPath = f.getParent();
        if (mstrFolderPath == null) {
            throw new IOException();    // no parent.
        }
        mstrFilePath = strCanonicalPath;
        mstrFileName = f.getName();
        if (mstrFileName == null || mstrFileName.length() == 0) {
            throw new IOException();    // invalid file name.
        }
    }
    
    public String getFolderPath() {
        return mstrFolderPath;
    }
    
    public String getFilePath() {
        return mstrFilePath;
    }
    
    public String getFileName() {
        return mstrFileName;
    }
    
    public static MFPSExecInfo getExecInfo(String str) {
        String strExecFunc = str.trim();
        int idxBracket = strExecFunc.indexOf('(');
        if (strExecFunc.length() == 0 || idxBracket == 0) {
            // no function name.
            return null;
        } else if (idxBracket == -1) {
            MFPSExecInfo mfpsExecInfo = new MFPSExecInfo();
            mfpsExecInfo.mstrFuncName = strExecFunc;
            mfpsExecInfo.mstrParamList = null;    // any number of parameters.
            return mfpsExecInfo;
        } else if (strExecFunc.charAt(strExecFunc.length() - 1) != ')')    {
            // if ) cannot be found but ( is there
            return null;
        } else {
            MFPSExecInfo mfpsExecInfo = new MFPSExecInfo();
            mfpsExecInfo.mstrFuncName = strExecFunc.substring(0, idxBracket).trim();
            mfpsExecInfo.mstrParamList = strExecFunc.substring(idxBracket + 1, strExecFunc.length() - 1).trim();

            // match parameter pattern.
            mfpsExecInfo.mbWithStrOpt = false;
            mfpsExecInfo.mbWithValOpt = false;
            mfpsExecInfo.mstrParamListWithoutOpt = mfpsExecInfo.mstrParamList;
            if (mfpsExecInfo.mstrParamListWithoutOpt.length() >= 4
                    && mfpsExecInfo.mstrParamListWithoutOpt.substring(mfpsExecInfo.mstrParamListWithoutOpt.length() - 4,
                                                            mfpsExecInfo.mstrParamListWithoutOpt.length()).equals("#...")) {
                mfpsExecInfo.mbWithValOpt = true;
                mfpsExecInfo.mstrParamListWithoutOpt = mfpsExecInfo.mstrParamListWithoutOpt
                                                        .substring(0, mfpsExecInfo.mstrParamListWithoutOpt.length() - 4).trim();
            } else if (mfpsExecInfo.mstrParamListWithoutOpt.length() >= 4
                    && mfpsExecInfo.mstrParamListWithoutOpt.substring(mfpsExecInfo.mstrParamListWithoutOpt.length() - 4,
                                                            mfpsExecInfo.mstrParamListWithoutOpt.length()).equals("@...")) {
                mfpsExecInfo.mbWithStrOpt = true;
                mfpsExecInfo.mstrParamListWithoutOpt = mfpsExecInfo.mstrParamListWithoutOpt
                                                        .substring(0, mfpsExecInfo.mstrParamListWithoutOpt.length() - 4).trim();
            } else if (mfpsExecInfo.mstrParamListWithoutOpt.length() >= 3
                    && mfpsExecInfo.mstrParamListWithoutOpt.substring(mfpsExecInfo.mstrParamListWithoutOpt.length() - 3,
                                                            mfpsExecInfo.mstrParamListWithoutOpt.length()).equals("...")) {
                mfpsExecInfo.mbWithStrOpt = true;
                mfpsExecInfo.mstrParamListWithoutOpt = mfpsExecInfo.mstrParamListWithoutOpt
                                                        .substring(0, mfpsExecInfo.mstrParamListWithoutOpt.length() - 3).trim();
            }
            if (mfpsExecInfo.mstrParamListWithoutOpt.length() >= 1 && mfpsExecInfo.mstrParamListWithoutOpt
                                                                    .charAt(mfpsExecInfo.mstrParamListWithoutOpt.length() - 1) == ',') {
                mfpsExecInfo.mstrParamListWithoutOpt = mfpsExecInfo.mstrParamListWithoutOpt
                                                        .substring(0, mfpsExecInfo.mstrParamListWithoutOpt.length() - 1);
                mfpsExecInfo.mstrParamListWithoutOpt.trim();
            }
            mfpsExecInfo.mlistRepPlaces = new LinkedList<Integer>();
            mfpsExecInfo.mlistRepType = new LinkedList<Boolean>();    // string is true.
            Boolean bInQuote = false;
            Boolean bEscaped = false;
            for (int idx = 0; idx < mfpsExecInfo.mstrParamListWithoutOpt.length(); idx ++) {
                if (!bInQuote) {
                    if (mfpsExecInfo.mstrParamListWithoutOpt.charAt(idx) == '@') {
                        mfpsExecInfo.mlistRepPlaces.add(idx);
                        mfpsExecInfo.mlistRepType.add(true);    // is string.
                    } else if (mfpsExecInfo.mstrParamListWithoutOpt.charAt(idx) == '#') {
                        mfpsExecInfo.mlistRepPlaces.add(idx);
                        mfpsExecInfo.mlistRepType.add(false);    // is not string.
                    } else if (mfpsExecInfo.mstrParamListWithoutOpt.charAt(idx) == '"')    {
                        bInQuote = true;
                        bEscaped = false;
                    }
                } else if (!bEscaped) {
                    if (mfpsExecInfo.mstrParamListWithoutOpt.charAt(idx) == '\\') {
                        // escaping char.
                        bEscaped = true;
                    } else if (mfpsExecInfo.mstrParamListWithoutOpt.charAt(idx) == '"') {
                        bInQuote = false;
                        bEscaped = false;
                    }
                } else {    // in escape mode.
                    bEscaped = false;
                }
            }

            return mfpsExecInfo;
        }
    }
    
    public MFPSExecInfo getExecInfo() {
        BufferedReader in = null;
        try {
            // do not use UTF8, use default.
            in = new BufferedReader(
                    new InputStreamReader(
                    new FileInputStream(mstrFilePath)/*, "UTF-8"*/
                    ));

            String str = null;
            while ((str = in.readLine()) != null) {
                if (str.trim().length() > 0 && str.trim().charAt(0) != '#') {
                    break;
                }
            }
            
            if (str == null) {
                return null;
            }
            String annotationExecEntry = "@execution_entry ";
            str = str.trim().toLowerCase(Locale.US);
            if (str.indexOf(annotationExecEntry) != 0) {
                return null;    // no @execution_entry statement
            } else {
                // ok, now analyze the auto-exec info.
                String autoExecInfo = str.substring(annotationExecEntry.length());
                return getExecInfo(autoExecInfo);
            }
        } catch (FileNotFoundException e) {
            // cannot find the file
            return null;
        } catch (IOException e1) {
            // reading error.
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // do nothing here.
                }
            }
        }
    }

    public String getFileInfo() {
        BufferedReader in = null;
        try {
            // do not use UTF8, use default.
            in = new BufferedReader(
                    new InputStreamReader(
                    new FileInputStream(mstrFilePath)/*, "UTF-8"*/
                    ));

            String strInfo = "";
            String str = null;
            int idx = 0;
            while ((str = in.readLine()) != null) {
                if (str.trim().length() == 0) {
                    continue;
                } else if (str.trim().charAt(0) == '#') {
                    String strContent = str.trim().substring(1);
                    if (idx == 0 && strContent.indexOf('!') == 0) {
                        // this is #! and it is the first line
                        continue;
                    } else {
                        strInfo += strContent + "\n";
                    }
                } else {
                    // break when see a nonempty line not starts with #
                    String annotationExecEntry = "@execution_entry ";
                    if (str.trim().toLowerCase(Locale.US).indexOf(annotationExecEntry) == 0) {
                        strInfo = str.trim() + "\n\n" + strInfo;
                    } else {
                        strInfo = "No " + annotationExecEntry + "\n\n" + strInfo;
                    }
                    break;
                }
            }

            return strInfo;
        } catch (FileNotFoundException e) {
            // cannot find the file
            return null;
        } catch (IOException e1) {
            // reading error.
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // do nothing here.
                }
            }
        }
    }
}
