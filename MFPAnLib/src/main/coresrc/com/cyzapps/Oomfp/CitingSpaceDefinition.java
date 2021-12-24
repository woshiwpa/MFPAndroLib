/*
 * MFP project, CitingSpaceDefinition.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Oomfp;

import com.cyzapps.JGI2D.DisplayLib;
import com.cyzapps.JGI2D.DrawLib;
import com.cyzapps.JGI2D.EventLib;
import com.cyzapps.JPlatformHW.PlatformInfo;
import com.cyzapps.Jfcalc.BuiltInFunctionLib;
import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.ExDataLib;
import com.cyzapps.Jfcalc.IOLib;
import com.cyzapps.Jfcalc.MFPDateTime;
import com.cyzapps.Jfcalc.ParallelCompLib;
import com.cyzapps.Jfcalc.PlotLib;
import com.cyzapps.Jfcalc.RTCMMediaLib;
import com.cyzapps.Jfcalc.ReflectionLib;
import com.cyzapps.Jfdatastruct.ArrayBasedDictionary;
import com.cyzapps.Jfdatastruct.ArrayBasedList;
import com.cyzapps.Jmfp.FunctionEntry;
import com.cyzapps.Jmfpcompiler.AnnotationLib;
import com.cyzapps.Multimedia.ImageLib;
import com.cyzapps.Multimedia.SoundLib;
import com.cyzapps.OSAdapter.LangFileManager;
import com.cyzapps.OSAdapter.ParallelManager.CallObject;
import com.cyzapps.Oomfp.OoErrProcessor.ERRORTYPES;
import com.cyzapps.Oomfp.OoErrProcessor.JOoMFPErrException;
import com.cyzapps.adapter.MFPAdapter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Difference between citing space and class includes
 * 1. citing space must be a part of another citing space, while a class can be a part of a citing space or another class
 * 2. citing space cannot have non-static members
 * 3. citing space cannot be instantiated.
 * 4. citing space's path space can be more than one.
 */
public class CitingSpaceDefinition extends SpaceMember {
	private static final long serialVersionUID = 1L;

    //root of citingspace without user defined classes/functions
	public static final CitingSpaceDefinition CSD_TOP_SYS = new CitingSpaceDefinition(new String[] {""}, null);
    
    // root class definition in main entity
    public static CitingSpaceDefinition mscsdTOPFull = null;

    public static CitingSpaceDefinition getTopCSD() {   // find right CSD for this thread.
        Long currentThreadId = Thread.currentThread().getId();
        if (!CallObject.msmapThreadId2SessionInfo.containsKey(currentThreadId)) {
            if (mscsdTOPFull == null) {
                return CSD_TOP_SYS;
            } else {
                return mscsdTOPFull;
            }
        } else {
            return CallObject.msmapThreadId2SessionInfo.get(currentThreadId).getCSDTop();
        }
    }
    
    public String mstrPureName = "";   // class name without citing space path
    
    public String mstrFullNameWithCS = ""; // full class name with citing space path
        
    public String[] mstrarrayCS = new String[] { "" };    // citing space path.
        
    public LinkedList<String[]> mlistPathSpaces = new LinkedList<String[]>();   // string[0] must be the first if it is there and its existance implies cs is builtin
    
    // body
    public LinkedList<MemberFunction> mlistFuncMembers = new LinkedList<MemberFunction>();   // static class function members
    
    //public LinkedList<MemberVariable> mlistStaticVarMembers = new LinkedList<MemberVariable>();    // static class variable members. Should not supported considering thread safe
    
    public LinkedList<CitingSpaceDefinition> mlistCitingSpaceDefs = new LinkedList<CitingSpaceDefinition>();  // sub class definitions

    public CitingSpaceDefinition(CitingSpaceDefinition src2Cpy) {
        mstrPureName = src2Cpy.mstrPureName;    // class name without citing space path
        mstrFullNameWithCS = src2Cpy.mstrFullNameWithCS; // full class name with citing space path
        mstrarrayCS = src2Cpy.mstrarrayCS.clone();      // citing space path. clone is shallow copy but is fine for string elements.
        for (String[] pathSpaces : src2Cpy.mlistPathSpaces) {
            mlistPathSpaces.add(pathSpaces.clone());   // string[0] must be the first if it is there and its existance implies cs is builtin
        }
        for (MemberFunction staticFuncMember : src2Cpy.mlistFuncMembers) {
            mlistFuncMembers.add(staticFuncMember);   // member function class is read only so we only add reference, no deepcopy needed.
        }
        for (CitingSpaceDefinition csd : src2Cpy.mlistCitingSpaceDefs) {
            if (csd instanceof MFPClassDefinition) {
                mlistCitingSpaceDefs.add(csd);  // built-in class definition (i.e. object) should only have one copy.
            } else {
                mlistCitingSpaceDefs.add(new CitingSpaceDefinition(csd));
            }
        }
    }
    
    public CitingSpaceDefinition(String[] strarrayAbsoluteCS) {
        mstrPureName = strarrayAbsoluteCS[strarrayAbsoluteCS.length - 1];
        for (int idx = 0; idx < strarrayAbsoluteCS.length; idx ++) {
            if (idx > 0) {
                mstrFullNameWithCS += "::";
            }
            mstrFullNameWithCS += strarrayAbsoluteCS[idx];
        }
        mstrarrayCS = strarrayAbsoluteCS;
        // if no path space added, this means the citing space is actually a "bridge" citing space, like
        // xxx::yyy::zzz(), then xxx and yyy are birdge citing spaces.
    }
    
    public CitingSpaceDefinition(String[] strarrayAbsoluteCS, String[] strarrayPathSpaces) {
        mstrPureName = strarrayAbsoluteCS[strarrayAbsoluteCS.length - 1];
        for (int idx = 0; idx < strarrayAbsoluteCS.length; idx ++) {
            if (idx > 0) {
                mstrFullNameWithCS += "::";
            }
            mstrFullNameWithCS += strarrayAbsoluteCS[idx];
        }
        mstrarrayCS = strarrayAbsoluteCS;
        if (strarrayPathSpaces != null) {
            mlistPathSpaces.add(strarrayPathSpaces);
        }
    }
    
    public boolean isEmpty() {
        return (mlistPathSpaces.size() == 0) && (mlistCitingSpaceDefs.size() == 0)
                && (mlistFuncMembers.size() == 0);
    }
    
    public boolean canBeRemoved() {
        return isEmpty();
    }
    
    
    public static enum CheckMFPSLibMode {
        CHECK_EVERYTHING,
        CHECK_USER_DEFINED_ONLY,
        CHECK_COMMAND_INPUT_ONLY,
        CHECK_USER_DEFINED_AND_COMMAND_INPUT,
    }
    
    public boolean shouldPathSpaceBeChecked(CheckMFPSLibMode checkMode, String[] pathSpace) {
        if (checkMode == CheckMFPSLibMode.CHECK_EVERYTHING) {
            return true;   // if we check everything, clearly pathSpace should be checked
        } else {
            int libPathSpaceType = MFPAdapter.getLibPathSpaceType(pathSpace);
            if (libPathSpaceType == 1
                    && (checkMode == CheckMFPSLibMode.CHECK_USER_DEFINED_ONLY
                        || checkMode == CheckMFPSLibMode.CHECK_USER_DEFINED_AND_COMMAND_INPUT)) {
                return true;   // if it includes user defined path, and the pathSpace is user defined lib
            } else if (libPathSpaceType == 2
                    && (checkMode == CheckMFPSLibMode.CHECK_COMMAND_INPUT_ONLY
                        || checkMode == CheckMFPSLibMode.CHECK_USER_DEFINED_AND_COMMAND_INPUT)) {
                return true;   // if it includes command input path, and the pathSpace is command line lib
            }
        }
        return false;
    }
    public boolean isMFPSLibEmpty(CheckMFPSLibMode checkMode) {    // does not include any (sys or user) pre-defined stuff.
        for (int i = 0; i < mlistPathSpaces.size(); i ++) {
            if (mlistPathSpaces.get(i).length > 0) {
                // this means it has an external path space
                if (shouldPathSpaceBeChecked(checkMode, mlistPathSpaces.get(i))) {
                    return false;   // if the pathspace is related, the lib is NOT empty
                }
            }
        }
        for (int i = 0; i < mlistCitingSpaceDefs.size(); i ++) {
            if (!mlistCitingSpaceDefs.get(i).isMFPSLibEmpty(checkMode))   {
                // this means it has a sub citing space which is not externally empty
                return false;
            }
        }
        // need not to worry about class, variable or function because if any of them is external, the path spaces must be external.
        return true;
    }

    // use this function to avoid mscsdTOPFull exposed to other projects.
    public static boolean noMFPSUserLibLoaded() {
        return mscsdTOPFull == null || mscsdTOPFull.isMFPSLibEmpty(CheckMFPSLibMode.CHECK_USER_DEFINED_AND_COMMAND_INPUT);
    }

    // use this function to avoid mscsdTOPFull or mscsdTOPSys exposed to other projects.
    public static void createTopCSDFull() {
        mscsdTOPFull = new CitingSpaceDefinition(CSD_TOP_SYS);
    }
    // this function accepts one parameter, which is clearing mode.
    public void clearMFPSLibs(CheckMFPSLibMode clearMode) {
        for (int i = 0; i < mlistPathSpaces.size(); i ++) {
            if (mlistPathSpaces.get(i).length > 0) {
                // this means it is an external / internal predefined mfps path space
                if (shouldPathSpaceBeChecked(clearMode, mlistPathSpaces.get(i))) {
                    mlistPathSpaces.remove(i);
                    i --;
                }
            }
        }
        
        for (int i = 0; i < mlistFuncMembers.size(); i ++) {
            if (mlistFuncMembers.get(i).mstrarrayPathSpace.length > 0) {
                // this means it is an external / internal predefined path space
                if (shouldPathSpaceBeChecked(clearMode, mlistFuncMembers.get(i).mstrarrayPathSpace)) {
                    mlistFuncMembers.remove(i);
                    i --;
                }
            }
        }
        
        for (int i = 0; i < mlistCitingSpaceDefs.size(); i ++) {
            CitingSpaceDefinition csd = mlistCitingSpaceDefs.get(i);
            csd.clearMFPSLibs(clearMode);
            // after clearExternal, a sub citing space should not have no path space but still have an external var or func.
            // the only exception is constructor function of a class
            if (csd.canBeRemoved()) {
                mlistCitingSpaceDefs.remove(i);
                if (csd instanceof MFPClassDefinition) {
                    // remove class definition from class definition map.
                    MFPClassDefinition.getClassDefinitionMap().remove(((MFPClassDefinition) csd).selfTypeDef.toString());
                    // remove the constructor function from mlistFuncMembers
                    for (int idx = 0; idx < mlistFuncMembers.size(); idx ++)    {
                        MemberFunction mf = mlistFuncMembers.get(idx);
                        if (mf instanceof MFPClassDefinition.ConstructorFunction
                                && ((MFPClassDefinition.ConstructorFunction)mf).getAbsNameWithCS().equals(((MFPClassDefinition) csd).mstrFullNameWithCS)
                                && ((MFPClassDefinition.ConstructorFunction)mf).getMaxNumParam() == 0)    {
                            // This is the constructor, we need to delete it.
                            mlistFuncMembers.remove(idx);
                            break;
                        }
                    }
                }
                i --;
            }
        }
    }
    
    public void unloadFileOrFolder(String[] strarrayPathSpace)    {
        for (int i = 0; i < mlistPathSpaces.size(); i ++) {
            if (MFPAdapter.isMFPSLibFileOrSubFile(strarrayPathSpace, mlistPathSpaces.get(i)))    {
                mlistPathSpaces.remove(i);
                i--;
            }
        }
        
        for (int i = 0; i < mlistFuncMembers.size(); i ++)    {
            MemberFunction mf = mlistFuncMembers.get(i);
            if (mf instanceof FunctionEntry && MFPAdapter.isMFPSLibFileOrSubFile(strarrayPathSpace, mf.mstrarrayPathSpace))    {
                // no need to worry about internal or external function because internal function's path space is string[0]
                // which cannot be an external file.
                mlistFuncMembers.remove(i);
                i--;
            }
        }
        
        for (int i = 0; i < mlistCitingSpaceDefs.size(); i++) {
            CitingSpaceDefinition csd = mlistCitingSpaceDefs.get(i);
            csd.unloadFileOrFolder(strarrayPathSpace);
            if (csd.canBeRemoved()) {
                mlistCitingSpaceDefs.remove(i);
                if (csd instanceof MFPClassDefinition) {
                    // remove class definition from class definition map.
                    MFPClassDefinition.getClassDefinitionMap().remove(((MFPClassDefinition) csd).selfTypeDef.toString());
                    // remove the constructor function from mlistFuncMembers
                    for (int idx = 0; idx < mlistFuncMembers.size(); idx ++)    {
                        MemberFunction mf = mlistFuncMembers.get(idx);
                        if (mf instanceof MFPClassDefinition.ConstructorFunction
                                && ((MFPClassDefinition.ConstructorFunction)mf).getAbsNameWithCS().equals(((MFPClassDefinition) csd).mstrFullNameWithCS)
                                && ((MFPClassDefinition.ConstructorFunction)mf).getMaxNumParam() == 0)    {
                            // This is the constructor, we need to delete it.
                            mlistFuncMembers.remove(idx);
                            break;
                        }
                    }
                }
                i --;
            }
        }
    }
    
    public boolean addMemberNoExcept(SpaceMember sm) {
        try {
            addMember(sm);
            return true;
        } catch (JOoMFPErrException e) {
            return false;
        }
    }
    public void addMember(SpaceMember sm) throws JOoMFPErrException {
        if (sm instanceof MFPClassDefinition) {
            ((MFPClassDefinition)sm).LinkCitingSpaceDefinition();
            addMember(((MFPClassDefinition)sm).mfConstructor);
        } else if (sm instanceof MemberFunction) {
            String[] strarrayFullCS = null;
            if (sm instanceof BaseBuiltInFunction) {
                // built-in function
                strarrayFullCS = ((BaseBuiltInFunction) sm).getFullCS();
            } else {
                // external functions and internal predefined functions
                strarrayFullCS = ((FunctionEntry) sm).getStatementFunction().getFunctionNameWithCS().split("::");
            }
            if (strarrayFullCS.length <= mstrarrayCS.length) {
                throw new JOoMFPErrException(ERRORTYPES.ERROR_SPACE_NAMES_DONT_MATCH);   // cs doesn't match
            }
            int idx = 0;
            for (; idx < mstrarrayCS.length; idx ++) {
                if (!strarrayFullCS[idx].equals(mstrarrayCS[idx])) {
                    throw new JOoMFPErrException(ERRORTYPES.ERROR_SPACE_NAMES_DONT_MATCH);   // cs doesn't match
                }
            }
            // now cs match. check if the sub-cs exist
            CitingSpaceDefinition csdAddIn = this;
            for (; idx < strarrayFullCS.length - 1; idx ++) {
                String strSpaceName = strarrayFullCS[idx];
                int idx1 = 0;
                for (; idx1 < csdAddIn.mlistCitingSpaceDefs.size(); idx1 ++) {
                    if (strSpaceName.equals(csdAddIn.mlistCitingSpaceDefs.get(idx1).mstrPureName)) {
                        // find it
                        break;
                    }
                }
                if (idx1 == csdAddIn.mlistCitingSpaceDefs.size())   {
                    // cannot find it
                    break;
                } else {
                    csdAddIn = csdAddIn.mlistCitingSpaceDefs.get(idx1);
                }
            }

            // now create the citing spaces.
            for (; idx < strarrayFullCS.length - 1; idx ++) {
                String strSpaceName = strarrayFullCS[idx];
                String[] strarrayCS = new String[csdAddIn.mstrarrayCS.length + 1];
                System.arraycopy(csdAddIn.mstrarrayCS, 0, strarrayCS, 0, csdAddIn.mstrarrayCS.length);
                strarrayCS[csdAddIn.mstrarrayCS.length] = strSpaceName;
                // this is a bridging citing space, no need to add path space.
                CitingSpaceDefinition csd = new CitingSpaceDefinition(strarrayCS);
                csdAddIn.mlistCitingSpaceDefs.add(csd);
                csdAddIn = csd;
            }
            
            // for functions, do not identify declaration confliction.
            csdAddIn.mlistFuncMembers.addFirst((MemberFunction)sm);
        } else if (sm instanceof CitingSpaceDefinition) {
            ((CitingSpaceDefinition)sm).LinkCitingSpaceDefinition();
        } else {
            //TODO
            throw new java.lang.IllegalStateException("This method hasn't been implemented yet!");
        }
    }
    
    public void LinkCitingSpaceDefinition() throws JOoMFPErrException {
        if (mstrarrayCS == null || (mstrarrayCS.length > 0 && mstrarrayCS[0].length() != 0)) {
            throw new JOoMFPErrException(ERRORTYPES.ERROR_INVALID_CLASS_OR_CITINGSPACE);  // absolute citing space.
        }
        CitingSpaceDefinition csdAddIn = CitingSpaceDefinition.getTopCSD();
        if (mstrarrayCS.length > 1) {
            // this means it is not a top level cs
            for (int idx = 1; idx < mstrarrayCS.length; idx ++) {
                String strSpaceName = mstrarrayCS[idx];
                int idx1 = 0;
                int nThisCsdCSDefCnt = csdAddIn.mlistCitingSpaceDefs.size();
                for (; idx1 < nThisCsdCSDefCnt; idx1 ++) {
                    if (csdAddIn.mlistCitingSpaceDefs.get(idx1).mstrPureName.equals(strSpaceName)) {
                        // the cs exists
                        if (idx == mstrarrayCS.length - 1) {
                            // need to add the pathspace.
                            LinkedList<String[]> listPathSpaces = mlistPathSpaces;
                            LinkedList<String[]> listAddInPathSpaces = csdAddIn.mlistCitingSpaceDefs.get(idx1).mlistPathSpaces;
                            for (int idx2 = 0; idx2 < listPathSpaces.size(); idx2 ++) {
                                String[] strarrayPathSpace = listPathSpaces.get(idx2);
                                int idx3 = 0;
                                for (; idx3 < listAddInPathSpaces.size(); idx3 ++) {
                                    if (Arrays.equals(strarrayPathSpace, listAddInPathSpaces.get(idx3))) {
                                        // the path space exists
                                        break;
                                    }
                                }
                                if (idx3 == listAddInPathSpaces.size()) {
                                    // the path space doesn't exists. Append it to the tail of listAddInPathSpaces.
                                    listAddInPathSpaces.add(strarrayPathSpace);
                                }
                            }
                        }
                        csdAddIn = csdAddIn.mlistCitingSpaceDefs.get(idx1);
                        break;
                    }
                }
                if (idx1 == nThisCsdCSDefCnt) {
                    // the cs does not exists
                    CitingSpaceDefinition csd = this;
                    if (idx < mstrarrayCS.length - 1) {
                        // intermedium cs.
                        String[] strarrayCS = new String[csdAddIn.mstrarrayCS.length + 1];
                        System.arraycopy(csdAddIn.mstrarrayCS, 0, strarrayCS, 0, csdAddIn.mstrarrayCS.length);
                        strarrayCS[csdAddIn.mstrarrayCS.length] = strSpaceName;
                        csd = new CitingSpaceDefinition(strarrayCS);
                    }
                    csdAddIn.mlistCitingSpaceDefs.add(csd);
                    csdAddIn = csd;
                }
            }
        } else {
            // top level CS, do nothing.
        }
    }
    
    // this function allows a * at the end of the last strarrayNamePartialCS element for a wild card match
    public MemberFunction locateFunctionCall(String[] strarrayNamePartialCS, int nParamNum) {
        if (strarrayNamePartialCS.length == 0) {
            return null;
        }
        CitingSpaceDefinition csdThis = this;
        int idx1 = 0;
        for (; idx1 < strarrayNamePartialCS.length - 1; idx1 ++) {
            int idx2 = 0;
            int nThisCsdCSDefCnt = csdThis.mlistCitingSpaceDefs.size();
            for (; idx2 < nThisCsdCSDefCnt; idx2 ++) {
                CitingSpaceDefinition csd = csdThis.mlistCitingSpaceDefs.get(idx2);
                if (csd.mstrPureName.equals(strarrayNamePartialCS[idx1])) {
                    csdThis = csd;
                    break;
                }
            }
            if (idx2 == nThisCsdCSDefCnt) {
                return null;    // cannot find the function.
            }
        }
        int idx2 = 0;
        for (; idx2 < csdThis.mlistFuncMembers.size(); idx2 ++) {
            MemberFunction mf = csdThis.mlistFuncMembers.get(idx2);
            String strMFFuncAbsName = mf.getAbsNameWithCS();
            String strCSPart = csdThis.mstrFullNameWithCS;
            int nMinNumParam = mf.getMinNumParam();
            int nMaxNumParam = mf.getMaxNumParam();
            boolean bNameMatch = false;
            if (strarrayNamePartialCS[strarrayNamePartialCS.length - 1].length() > 0
                    && strarrayNamePartialCS[strarrayNamePartialCS.length - 1]
                            .charAt(strarrayNamePartialCS[strarrayNamePartialCS.length - 1].length() - 1) == '*') {
                // use wildcard match
                String strName = strCSPart + "::" + strarrayNamePartialCS[strarrayNamePartialCS.length - 1];
                strName = strName.substring(0, strName.length() - 1);
                bNameMatch = strMFFuncAbsName.substring(0, Math.min(strMFFuncAbsName.length(), strName.length())).equals(strName);
            } else {
                bNameMatch = strMFFuncAbsName.equals(strCSPart + "::" + strarrayNamePartialCS[strarrayNamePartialCS.length - 1]);
            }
            if (bNameMatch && ((nParamNum >= nMinNumParam && (nMaxNumParam == -1 || nParamNum <= nMaxNumParam))
                        || nParamNum == -1)){
                // match
                return mf;
            }
        }
        // cannot find the function.
        return null;
    }
    
    public MemberFunction locateFunctionCallRecur(String[] strarrayNamePartialCS, int nParamNum) {
        MemberFunction mf = locateFunctionCall(strarrayNamePartialCS, nParamNum);
        if (mf == null) {
            int idx2 = 0;
            for (; idx2 < mlistCitingSpaceDefs.size(); idx2 ++) {
                CitingSpaceDefinition csd = mlistCitingSpaceDefs.get(idx2);
                if ((mf = csd.locateFunctionCallRecur(strarrayNamePartialCS, nParamNum)) != null) {
                    break;
                }
            }
        }
        return mf;
    }
    
    public LinkedList<MemberFunction> lookUpFunctionDef(String[] strarrayNamePartialCS, int nParamNum, boolean bIncludeOpt) {
        LinkedList<MemberFunction> listMFs = new LinkedList<MemberFunction>();
        if (strarrayNamePartialCS.length == 0) {
            return listMFs;
        }
        CitingSpaceDefinition csdThis = this;
        int idx1 = 0;
        for (; idx1 < strarrayNamePartialCS.length - 1; idx1 ++) {
            int idx2 = 0;
            int nThisCsdCSDefCnt = csdThis.mlistCitingSpaceDefs.size();
            for (; idx2 < nThisCsdCSDefCnt; idx2 ++) {
                CitingSpaceDefinition csd = csdThis.mlistCitingSpaceDefs.get(idx2);
                if (csd.mstrPureName.equals(strarrayNamePartialCS[idx1])) {
                    csdThis = csd;
                    break;
                }
            }
            if (idx2 == nThisCsdCSDefCnt) {
                return listMFs;    // cannot find the function.
            }
        }
        int idx2 = 0;
        for (; idx2 < csdThis.mlistFuncMembers.size(); idx2 ++) {
            MemberFunction mf = csdThis.mlistFuncMembers.get(idx2);
            String strMFFuncAbsName = mf.getAbsNameWithCS();
            String strCSPart = csdThis.mstrFullNameWithCS;
            int nMinNumParam = mf.getMinNumParam();
            int nMaxNumParam = mf.getMaxNumParam();
            boolean bNameMatch = false;
            if (strarrayNamePartialCS[strarrayNamePartialCS.length - 1].length() > 0
                    && strarrayNamePartialCS[strarrayNamePartialCS.length - 1]
                            .charAt(strarrayNamePartialCS[strarrayNamePartialCS.length - 1].length() - 1) == '*') {
                // use wildcard match
                String strName = strCSPart + "::" + strarrayNamePartialCS[strarrayNamePartialCS.length - 1];
                strName = strName.substring(0, strName.length() - 1);
                bNameMatch = strMFFuncAbsName.substring(0, Math.min(strMFFuncAbsName.length(), strName.length())).equals(strName);
            } else {
                bNameMatch = strMFFuncAbsName.equals(strCSPart + "::" + strarrayNamePartialCS[strarrayNamePartialCS.length - 1]);
            }
            if (bNameMatch && ((nParamNum == nMinNumParam && (nMaxNumParam != nMinNumParam) == bIncludeOpt)
                        || nParamNum == -1)){
                // match
                listMFs.add(mf);
            }
        }
        // cannot find the function.
        return listMFs;
    }
    
    public LinkedList<MemberFunction> lookUpFunctionDefRecur(String[] strarrayNamePartialCS, int nParamNum, boolean bIncludeOpt) {
        LinkedList<MemberFunction> listMFs = lookUpFunctionDef(strarrayNamePartialCS, nParamNum, bIncludeOpt);
        int idx2 = 0;
        for (; idx2 < mlistCitingSpaceDefs.size(); idx2 ++) {
            CitingSpaceDefinition csd = mlistCitingSpaceDefs.get(idx2);
            LinkedList<MemberFunction> listTheseMFs = csd.lookUpFunctionDefRecur(strarrayNamePartialCS, nParamNum, bIncludeOpt);
            listMFs.addAll(listTheseMFs);
        }
        return listMFs;
    }
    
    public LinkedList<CitingSpaceDefinition> lookUpCSDef(String[] strarrayNamePartialCS) {
        LinkedList<CitingSpaceDefinition> listCSs = new LinkedList<CitingSpaceDefinition>();
        if (strarrayNamePartialCS.length == 0) {
            return listCSs;
        }
        CitingSpaceDefinition csdThis = this;
        int idx1 = 0;
        // in the following for loop, we go through each name element in strarrayNamePartialCS
        // except the last one (because the last one could be *). If we could find a match in
        // csdThis's child (grandchild or grandgrandchild...), we continue, otherwise, we exit.
        for (; idx1 < strarrayNamePartialCS.length - 1; idx1 ++) {
            int idx2 = 0;
            int nThisCsdCSDefCnt = csdThis.mlistCitingSpaceDefs.size();
            for (; idx2 < nThisCsdCSDefCnt; idx2 ++) {
                CitingSpaceDefinition csd = csdThis.mlistCitingSpaceDefs.get(idx2);
                if (csd.mstrPureName.equals(strarrayNamePartialCS[idx1])) {
                    csdThis = csd;
                    break;
                }
            }
            if (idx2 == nThisCsdCSDefCnt) {
                return listCSs;    // cannot find the citing space.
            }
        }
        // ok, we find a match. Now we need to check if the last name element in
        // strarrayNamePartialCS has a star in the tail. If so, it is a wild card
        // match
        int idx2 = 0;
        for (; idx2 < csdThis.mlistCitingSpaceDefs.size(); idx2 ++) {
            CitingSpaceDefinition cs = csdThis.mlistCitingSpaceDefs.get(idx2);
            String strCSDefAbsName = cs.mstrFullNameWithCS;
            String strCSPathPart = csdThis.mstrFullNameWithCS;
            boolean bNameMatch = false;
            if (strarrayNamePartialCS[strarrayNamePartialCS.length - 1].length() > 0
                    && strarrayNamePartialCS[strarrayNamePartialCS.length - 1]
                            .charAt(strarrayNamePartialCS[strarrayNamePartialCS.length - 1].length() - 1) == '*') {
                // use wildcard match
                String strName = strCSPathPart + "::" + strarrayNamePartialCS[strarrayNamePartialCS.length - 1];
                strName = strName.substring(0, strName.length() - 1);
                bNameMatch = strCSDefAbsName.substring(0, Math.min(strCSDefAbsName.length(), strName.length())).equals(strName);
            } else {
                bNameMatch = strCSDefAbsName.equals(strCSPathPart + "::" + strarrayNamePartialCS[strarrayNamePartialCS.length - 1]);
            }
            if (bNameMatch){
                // match
                listCSs.add(cs);
            }
        }
        // cannot find the function.
        return listCSs;
    }
    
    public LinkedList<CitingSpaceDefinition> lookUpCSDefRecur(String[] strarrayNamePartialCS) {
        LinkedList<CitingSpaceDefinition> listCSs = lookUpCSDef(strarrayNamePartialCS);
        int idx2 = 0;
        for (; idx2 < mlistCitingSpaceDefs.size(); idx2 ++) {
            CitingSpaceDefinition csd = mlistCitingSpaceDefs.get(idx2);
            LinkedList<CitingSpaceDefinition> listTheseCSs = csd.lookUpCSDefRecur(strarrayNamePartialCS);
            listCSs.addAll(listTheseCSs);
        }
        return listCSs;
    }
    
    // this function identifies if calling style of function and find the first match.
    // this function assumes
    // 1. strShrinkedRawName is shrinked and small letter function name with full or partial CS path
    // 2. if nParamNum == -1, do not compare number of parameters
    // 3. lCitingSpaces can include wild card char (*)
    public static MemberFunction locateFunctionCall(String strShrinkedRawName, int nParamNum,   // function 1
                                            List<String[]> lCitingSpaces) { // citing space
        if (strShrinkedRawName == null || strShrinkedRawName.length() == 0
                || strShrinkedRawName.charAt(strShrinkedRawName.length() - 1) == ':') {
            // invalid function name
            return null;
        }
        String[] strarrayCS = strShrinkedRawName.split("::");
        if (strarrayCS.length == 0) {
            return null;
        } else if (strarrayCS[0].length() == 0) {
            // ok, this is absolute path. No need to worry about citing spaces
            String[] strarrayPartialCS = new String[strarrayCS.length - 1];
            System.arraycopy(strarrayCS, 1, strarrayPartialCS, 0, strarrayCS.length - 1);
            MemberFunction mf = CitingSpaceDefinition.getTopCSD().locateFunctionCall(strarrayPartialCS, nParamNum);
            return mf;
        } else {
            // this is relative path. Need to consider citing spaces.
            int idx = 0;
            for (; idx < lCitingSpaces.size(); idx ++) {
                boolean bFindThisCitingSpace = true;
                String[] strarrayCitingSpace = lCitingSpaces.get(idx);    // strarrayCitingSpace != null and size > 0
                CitingSpaceDefinition csdThis = CitingSpaceDefinition.getTopCSD();
                if (!csdThis.mstrPureName.equals(strarrayCitingSpace[0])) {
                    continue;
                }
                int idx1 = 1;   //strarrayCitingSpace[idx1] must equal CitingSpaceDefinition.getTopCSD().mstrPureName so no need to check
                for (; idx1 < strarrayCitingSpace.length - 1; idx1 ++) {
                    int idx2 = 0;
                    int nThisCsdCSDefCnt = csdThis.mlistCitingSpaceDefs.size();
                    for (; idx2 < nThisCsdCSDefCnt; idx2 ++) {
                        CitingSpaceDefinition csd = csdThis.mlistCitingSpaceDefs.get(idx2);
                        if (csd.mstrPureName.equals(strarrayCitingSpace[idx1])) {
                            csdThis = csd;
                            break;
                        }
                    }
                    if (idx2 == nThisCsdCSDefCnt) {
                        bFindThisCitingSpace = false;    // cannot find the function.
                        break;
                    }
                }
                if (!bFindThisCitingSpace) {
                    continue;   // look for next citing space
                }
                
                if (strarrayCitingSpace[strarrayCitingSpace.length - 1].equals("*")) {
                    // means the citing space includes all sub-spaces.
                    MemberFunction mf = csdThis.locateFunctionCallRecur(strarrayCS, nParamNum);
                    if (mf != null) {
                        // match
                        return mf;
                    }
                } else {
                    // no sub-space to consider.
                    if (idx1 == strarrayCitingSpace.length - 1) {
                        int idx2 = 0;
                        int nThisCsdCSDefCnt = csdThis.mlistCitingSpaceDefs.size();
                        for (; idx2 < nThisCsdCSDefCnt; idx2 ++) {
                            CitingSpaceDefinition csd = csdThis.mlistCitingSpaceDefs.get(idx2);
                            if (csd.mstrPureName.equals(strarrayCitingSpace[strarrayCitingSpace.length - 1])) {
                                csdThis = csd;
                                break;
                            }
                        }
                        if (idx2 == nThisCsdCSDefCnt) {
                            bFindThisCitingSpace = false;    // cannot find the function.
                            continue;
                        }
                    }
                    MemberFunction mf = csdThis.locateFunctionCall(strarrayCS, nParamNum);
                    if (mf != null) {
                        // match
                        return mf;
                    }
                }
            }
            // cannot find a match.
            return null;
        }
    }
    
    public LinkedList<MemberFunction> getAllFunctionDef() {
        LinkedList<MemberFunction> listMfs = new LinkedList<MemberFunction>();
        listMfs.addAll(mlistFuncMembers);
        return listMfs;
    }
    
    public LinkedList<MemberFunction> getAllFunctionDefRecur() {
        LinkedList<MemberFunction> listMfs = getAllFunctionDef();
        for (CitingSpaceDefinition csd : mlistCitingSpaceDefs) {
            listMfs.addAll(csd.getAllFunctionDefRecur());
        }
        return listMfs;
    }
    
    public static LinkedList<MemberFunction> getAllFunctionDef(List<String[]> lCitingSpaces) {
        LinkedList<MemberFunction> listMFs = new LinkedList<MemberFunction>();
        int idx = 0;
        for (; idx < lCitingSpaces.size(); idx ++) {
            boolean bFindThisCitingSpace = true;
            String[] strarrayCitingSpace = lCitingSpaces.get(idx);    // strarrayCitingSpace != null and size > 0
            CitingSpaceDefinition csdThis = CitingSpaceDefinition.getTopCSD();
            int idx1 = 0;
            for (; idx1 < strarrayCitingSpace.length - 1; idx1 ++) {
                int idx2 = 0;
                int nThisCsdCSDefCnt = csdThis.mlistCitingSpaceDefs.size();
                for (; idx2 < nThisCsdCSDefCnt; idx2 ++) {
                    CitingSpaceDefinition csd = csdThis.mlistCitingSpaceDefs.get(idx2);
                    if (csd.mstrPureName.equals(strarrayCitingSpace[idx1])) {
                        csdThis = csd;
                        break;
                    }
                }
                if (idx2 == nThisCsdCSDefCnt) {
                    bFindThisCitingSpace = false;    // cannot find the function.
                    break;
                }
            }
            if (!bFindThisCitingSpace) {
                continue;   // look for next citing space
            }

            if (strarrayCitingSpace[strarrayCitingSpace.length - 1].equals("*")) {
                // means the citing space includes all sub-spaces.
                LinkedList<MemberFunction> listTheseMFs = csdThis.getAllFunctionDefRecur();
                listMFs.addAll(listTheseMFs);
            } else if (strarrayCitingSpace.length > 1) {
                // no sub-space to consider.
                int idx2 = 0;
                int nThisCsdCSDefCnt = csdThis.mlistCitingSpaceDefs.size();
                for (; idx2 < nThisCsdCSDefCnt; idx2 ++) {
                    CitingSpaceDefinition csd = csdThis.mlistCitingSpaceDefs.get(idx2);
                    if (csd.mstrPureName.equals(strarrayCitingSpace[strarrayCitingSpace.length - 1])) {
                        csdThis = csd;
                        break;
                    }
                }
                if (idx2 == nThisCsdCSDefCnt) {
                    bFindThisCitingSpace = false;    // cannot find the citingspace.
                    continue;
                }
                LinkedList<MemberFunction> listTheseMFs = csdThis.getAllFunctionDef();
                listMFs.addAll(listTheseMFs);
            } else {
                LinkedList<MemberFunction> listTheseMFs = csdThis.getAllFunctionDef();
                listMFs.addAll(listTheseMFs);
            }
        }
        // return all found mfs.
        return listMFs;
    }
        
    // this function all the functions which matches the function's definition.
    // this function assumes
    // 1. strShrinkedRawName is shrinked and small letter function name with full or partial CS path
    // 2. nParamNum is the minimum number of parameters of function 1. If it is -1, do not compare number of parameters
    // 3. lCitingSpaces can include wild card char (*)
    public static LinkedList<MemberFunction> lookupFunctionDef(String strShrinkedRawName, int nParamNum, boolean bWithOptionalParam,  // function 1
                                            List<String[]> lCitingSpaces) { // citing space
        LinkedList<MemberFunction> listMFs = new LinkedList<MemberFunction>();
        if (strShrinkedRawName == null || strShrinkedRawName.length() == 0
                || strShrinkedRawName.charAt(strShrinkedRawName.length() - 1) == ':') {
            // invalid function name
            return listMFs;
        }
        String[] strarrayCS = strShrinkedRawName.split("::");
        if (strarrayCS.length == 0) {
            return listMFs;
        } else if (strarrayCS[0].length() == 0) {
            // ok, this is absolute path. No need to worry about citing spaces
            String[] strarrayPartialCS = new String[strarrayCS.length - 1];
            System.arraycopy(strarrayCS, 1, strarrayPartialCS, 0, strarrayCS.length - 1);
            listMFs = CitingSpaceDefinition.getTopCSD().lookUpFunctionDef(strarrayPartialCS, nParamNum, bWithOptionalParam);
            return listMFs;
        } else {
            // this is relative path. Need to consider citing spaces.
            int idx = 0;
            for (; idx < lCitingSpaces.size(); idx ++) {
                boolean bFindThisCitingSpace = true;
                String[] strarrayCitingSpace = lCitingSpaces.get(idx);    // strarrayCitingSpace != null and size > 0
                CitingSpaceDefinition csdThis = CitingSpaceDefinition.getTopCSD();
                if (!csdThis.mstrPureName.equals(strarrayCitingSpace[0])) {
                    continue;
                }
                int idx1 = 1;
                for (; idx1 < strarrayCitingSpace.length - 1; idx1 ++) {
                    int idx2 = 0;
                    int nThisCsdCSDefCnt = csdThis.mlistCitingSpaceDefs.size();
                    for (; idx2 < nThisCsdCSDefCnt; idx2 ++) {
                        CitingSpaceDefinition csd = csdThis.mlistCitingSpaceDefs.get(idx2);
                        if (csd.mstrPureName.equals(strarrayCitingSpace[idx1])) {
                            csdThis = csd;
                            break;
                        }
                    }
                    if (idx2 == nThisCsdCSDefCnt) {
                        bFindThisCitingSpace = false;    // cannot find the function.
                        break;
                    }
                }
                if (!bFindThisCitingSpace) {
                    continue;   // look for next citing space
                }
                
                if (strarrayCitingSpace[strarrayCitingSpace.length - 1].equals("*")) {
                    // means the citing space includes all sub-spaces.
                    LinkedList<MemberFunction> listTheseMFs = csdThis.lookUpFunctionDefRecur(strarrayCS, nParamNum, bWithOptionalParam);
                    listMFs.addAll(listTheseMFs);
                } else if (strarrayCitingSpace.length > 1) {
                    // no sub-space to consider.
                    int idx2 = 0;
                    int nThisCsdCSDefCnt = csdThis.mlistCitingSpaceDefs.size();
                    for (; idx2 < nThisCsdCSDefCnt; idx2 ++) {
                        CitingSpaceDefinition csd = csdThis.mlistCitingSpaceDefs.get(idx2);
                        if (csd.mstrPureName.equals(strarrayCitingSpace[strarrayCitingSpace.length - 1])) {
                            csdThis = csd;
                            break;
                        }
                    }
                    if (idx2 == nThisCsdCSDefCnt) {
                        bFindThisCitingSpace = false;    // cannot find the function.
                        continue;
                    }
                    LinkedList<MemberFunction> listTheseMFs = csdThis.lookUpFunctionDef(strarrayCS, nParamNum, bWithOptionalParam);
                    listMFs.addAll(listTheseMFs);
                } else {
                    LinkedList<MemberFunction> listTheseMFs = csdThis.lookUpFunctionDef(strarrayCS, nParamNum, bWithOptionalParam);
                    listMFs.addAll(listTheseMFs);
                }
            }
            // return all found mfs.
            return listMFs;
        }
    }
    
    // this function all the citingspace which matches the citingspace's (partial name).
    // this function assumes
    // 1. strShrinkedRawName is shrinked and small letter citingspace name with full or partial CS path
    // 2. lCitingSpaces can include wild card char (*)
    public static LinkedList<CitingSpaceDefinition> lookupCSDef(String strShrinkedRawName, List<String[]> lCitingSpaces) { // citing space
        LinkedList<CitingSpaceDefinition> listCSs = new LinkedList<CitingSpaceDefinition>();
        if (strShrinkedRawName == null || strShrinkedRawName.length() == 0
                || strShrinkedRawName.charAt(strShrinkedRawName.length() - 1) == ':') {
            // invalid function name
            return listCSs;
        }
        String[] strarrayCS = strShrinkedRawName.split("::");
        if (strarrayCS.length == 0) {
            return listCSs;
        } else if (strarrayCS[0].length() == 0) {
            // ok, this is absolute cs path.
            String[] strarrayPartialCS = new String[strarrayCS.length - 1];
            System.arraycopy(strarrayCS, 1, strarrayPartialCS, 0, strarrayCS.length - 1);
            listCSs = CitingSpaceDefinition.getTopCSD().lookUpCSDef(strarrayPartialCS);
            return listCSs;
        } else {
            // this is relative path. Need to consider citing spaces.
            int idx = 0;
            for (; idx < lCitingSpaces.size(); idx ++) {
                boolean bFindThisCitingSpace = true;
                String[] strarrayCitingSpace = lCitingSpaces.get(idx);    // strarrayCitingSpace != null and size > 0
                CitingSpaceDefinition csdThis = CitingSpaceDefinition.getTopCSD();
                if (!csdThis.mstrPureName.equals(strarrayCitingSpace[0])) {
                    continue;
                }
                int idx1 = 1;
                for (; idx1 < strarrayCitingSpace.length - 1; idx1 ++) {
                    int idx2 = 0;
                    int nThisCsdCSDefCnt = csdThis.mlistCitingSpaceDefs.size();
                    for (; idx2 < nThisCsdCSDefCnt; idx2 ++) {
                        CitingSpaceDefinition csd = csdThis.mlistCitingSpaceDefs.get(idx2);
                        if (csd.mstrPureName.equals(strarrayCitingSpace[idx1])) {
                            csdThis = csd;
                            break;
                        }
                    }
                    if (idx2 == nThisCsdCSDefCnt) {
                        bFindThisCitingSpace = false;    // cannot find the citing space.
                        break;
                    }
                }
                if (!bFindThisCitingSpace) {
                    continue;   // look for next citing space
                }
                
                if (strarrayCitingSpace[strarrayCitingSpace.length - 1].equals("*")) {
                    // means the citing space includes all sub-spaces.
                    LinkedList<CitingSpaceDefinition> listTheseCSs = csdThis.lookUpCSDefRecur(strarrayCS);
                    listCSs.addAll(listTheseCSs);
                } else if (strarrayCitingSpace.length > 1) {
                    // no sub-space to consider.
                    int idx2 = 0;
                    int nThisCsdCSDefCnt = csdThis.mlistCitingSpaceDefs.size();
                    for (; idx2 < nThisCsdCSDefCnt; idx2 ++) {
                        CitingSpaceDefinition csd = csdThis.mlistCitingSpaceDefs.get(idx2);
                        if (csd.mstrPureName.equals(strarrayCitingSpace[strarrayCitingSpace.length - 1])) {
                            csdThis = csd;
                            break;
                        }
                    }
                    if (idx2 == nThisCsdCSDefCnt) {
                        bFindThisCitingSpace = false;    // cannot find the citing space.
                        continue;
                    }
                    LinkedList<CitingSpaceDefinition> listTheseCSs = csdThis.lookUpCSDef(strarrayCS);
                    listCSs.addAll(listTheseCSs);
                } else {
                    LinkedList<CitingSpaceDefinition> listTheseCSs = csdThis.lookUpCSDef(strarrayCS);
                    listCSs.addAll(listTheseCSs);
                }
            }
            // return all found CSs.
            return listCSs;
        }
    }

    static {
        // load the builtin functions.
        BuiltInFunctionLib.call2Load(LangFileManager.mbOutputCSDebugInfo);
        MFPDateTime.call2Load(LangFileManager.mbOutputCSDebugInfo);
        IOLib.call2Load(LangFileManager.mbOutputCSDebugInfo);
        ReflectionLib.call2Load(LangFileManager.mbOutputCSDebugInfo);
        PlotLib.call2Load(LangFileManager.mbOutputCSDebugInfo);
        DisplayLib.call2Load(LangFileManager.mbOutputCSDebugInfo);
        DrawLib.call2Load(LangFileManager.mbOutputCSDebugInfo);
        EventLib.call2Load(LangFileManager.mbOutputCSDebugInfo);
        ImageLib.call2Load(LangFileManager.mbOutputCSDebugInfo);
        SoundLib.call2Load(LangFileManager.mbOutputCSDebugInfo);
        ArrayBasedDictionary.call2Load(LangFileManager.mbOutputCSDebugInfo);
        ArrayBasedList.call2Load(LangFileManager.mbOutputCSDebugInfo);
        AnnotationLib.call2Load(LangFileManager.mbOutputCSDebugInfo);
        PlatformInfo.call2Load(LangFileManager.mbOutputCSDebugInfo);
        ParallelCompLib.call2Load(LangFileManager.mbOutputCSDebugInfo);
        RTCMMediaLib.call2Load(LangFileManager.mbOutputCSDebugInfo);
        ExDataLib.call2Load(LangFileManager.mbOutputCSDebugInfo);
    }
}
