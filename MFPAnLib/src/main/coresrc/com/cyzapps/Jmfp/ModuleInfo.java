/*
 * MFP project, ModuleInfo.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.DCHelper.CurPos;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.ExprAnalyzer;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.Oomfp.MFPClassDefinition;
import com.cyzapps.Oomfp.MemberFunction;
import com.cyzapps.adapter.MFPAdapter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tonyc
 */
public class ModuleInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final int FUNCTION_MODULE = 1;
    public static final int CLASS_MODULE = 2;
    
    public String mstrModuleName = "";
    public int mnModuleParam1 = 0;
    private String mclassDefFullNameWithCS = null;   // null means not a class module
    private transient MFPClassDefinition mclassDef = null;  // mclassDef is expensive to serialize so that it will not be serialized.
    public MFPClassDefinition getClassDef() {
        if (mclassDefFullNameWithCS == null) {
            // if it is null, means not a member function. This variable is initialized after class defintion is constructed
            return null;
        } else {
            if (mclassDef == null) {
                // ownerClassDefinition hasn't been cached.
                mclassDef = MFPClassDefinition.getClassDefinitionMap().get(mclassDefFullNameWithCS);
            }
            return mclassDef;
        }
    }
    public void setClassDef(MFPClassDefinition clsDef) {
        if (clsDef == null) {
            mclassDefFullNameWithCS = null;
            mclassDef = null;
        } else {
            mclassDefFullNameWithCS = clsDef.selfTypeDef.toString();
            mclassDef = clsDef;
        }
    }
    
    public int mnModuleType = FUNCTION_MODULE;   // 0 means variable, 1 means function, 2 means class.
    
    //public Object m_objExtraInfo1 = null;   // extra info is not used in compare, but will be used in compile.
    public int compare(ModuleInfo info) {
        if (mnModuleType > info.mnModuleType) {
            return 1;
        } else if (mnModuleType < info.mnModuleType) {
            return -1;
        } else {
            if (mnModuleType == CLASS_MODULE) {
                return mstrModuleName.compareToIgnoreCase(info.mstrModuleName);
            } else {    // function module.                
                int nModuleNameCompare = mstrModuleName.compareToIgnoreCase(info.mstrModuleName);
                if (nModuleNameCompare != 0) {
                    return nModuleNameCompare;
                } else if (mnModuleParam1 > info.mnModuleParam1) {
                    return 1;
                } else if (mnModuleParam1 < info.mnModuleParam1) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }
    
    public static void mergeIntoList(LinkedList<ModuleInfo> listSrcInfos, LinkedList<ModuleInfo> listDestInfos) {
        for (ModuleInfo info : listSrcInfos) {
            boolean bAdded = false;
            int nCompareLast = -2, nCompareThis;
            for (int idx = 0; idx < listDestInfos.size(); idx ++) {
                nCompareThis = listDestInfos.get(idx).compare(info);
                int nCompareBoth = nCompareLast * nCompareThis;
                if (nCompareBoth < 0) {
                    listDestInfos.add(idx, info);
                    bAdded = true;
                    break;
                } else if (nCompareBoth == 0) {
                    bAdded = true;
                    break;
                } else {
                    nCompareLast = nCompareThis;
                    continue;
                }
            }
            if (!bAdded) {
                listDestInfos.add(info);
            }
        }
    }
    
    public static LinkedList<ModuleInfo> getReferredModulesFromString(String str, ProgContext progContext) throws InterruptedException {
        AbstractExpr aexpr = null;
        try {
            aexpr = ExprAnalyzer.analyseExpression(str, new CurPos(), new LinkedList<Variable>(), progContext);
        } catch (JFCALCExpErrException ex) {
            ex.printStackTrace();   // do not do anything at this moment
        } catch (JSmartMathErrException ex) {
            ex.printStackTrace();   // do not do anything at this moment
        }
        if (aexpr == null) {
            return new LinkedList<ModuleInfo>();
        } else {
            LinkedList<ModuleInfo> listFunctions = aexpr.getReferredModules(progContext);
            return listFunctions;
        }
    }
    
    public boolean isModuleInfoInList(LinkedList<ModuleInfo> listModuleInfo) {
        for (ModuleInfo info : listModuleInfo) {
            if (compare(info) == 0) {
                return true;
            }
        }
        return false;
    }
       
    public static LinkedList<ModuleInfo> mergeModuleInfo2List(ModuleInfo moduleInfo, LinkedList<ModuleInfo> listModuleInfos) {
        boolean bInserted = false;
        for (int idx = 0; idx < listModuleInfos.size(); idx ++) {
            if (moduleInfo.compare(listModuleInfos.get(idx)) < 0) {
                idx ++;
            } else if (moduleInfo.compare(listModuleInfos.get(idx)) > 0) {
                listModuleInfos.add(idx + 1, moduleInfo);
                bInserted = true;
                break;
            } else { // == 0
                bInserted = true;
                break;
            }
        }
        if (!bInserted) {
            listModuleInfos.add(moduleInfo);
        }
        return listModuleInfos;
    }
    
    public static class ReferenceUnit {
        private FunctionEntry funcEntry = null;
        public FunctionEntry getFunctionEntry() {
            return funcEntry;
        }
        private MFPClassDefinition clsDef = null;
        public MFPClassDefinition getClassDefinition() {
            return clsDef;
        }
        public ReferenceUnit(FunctionEntry functionEntry) {
            funcEntry = functionEntry;
            clsDef = null;
        }
        public ReferenceUnit(MFPClassDefinition mfpClsDef) {
            clsDef = mfpClsDef;
            funcEntry = null;
        }
        @Override
        public boolean equals(Object o) {
            // If the object is compared with itself then return true   
            if (o == this) { 
                return true; 
            } 

            /* Check if o is an instance of ReferenceUnit or not 
              "null instanceof [type]" also returns false */
            if (!(o instanceof ReferenceUnit)) { 
                return false; 
            }
            
            ReferenceUnit refUnit = (ReferenceUnit)o;
            if (clsDef == refUnit.clsDef) {
                if (clsDef != null) {
                    return true;    // both are class definition, point to the same class definition.
                } else {
                    // both are function entry
                    if (funcEntry.getAbsNameWithCS().equals(refUnit.funcEntry.getAbsNameWithCS())
                            && funcEntry.getMinNumParam() == refUnit.funcEntry.getMinNumParam()
                            && funcEntry.isIncludeOptParam() == refUnit.funcEntry.isIncludeOptParam()) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {    // clsDef != refUnit.clsDef, these two class definition must be different.
                return false;
            }
        }
        
        @Override
        public int hashCode() 
        { 

            int result = 17;
            if (funcEntry != null) {
                result = 31 * result + funcEntry.hashCode();
            }
            if (clsDef != null) {
                result = 59 * result + clsDef.hashCode();
            }
            return result;
        }
    }

    
    // cannot directly call CitingSpaceDefinition::getAllFunctionDefRecur considering a situation that
    // there is xxx(4...) so find xxx(5...) should return some functionentries but CitingSpaceDefinition::getAllFunctionDefRecur
    // will return nothing. strShrinkedRawName is shrinked and small letter function name with full or partial CS path
    public static LinkedList<MemberFunction> getReferredMFDefs(String strShrinkedRawName, int nNumOfParams, boolean bOptionalParam) {
        List<String[]> lCitingSpaces = MFPAdapter.getAllCitingSpaces(null); // mfp.* + root cs, i.e. ""
        LinkedList<MemberFunction> listMf = new LinkedList<MemberFunction>();
        if (strShrinkedRawName.equals("*")) {
            // add all the predefined function
            listMf = CitingSpaceDefinition.getTopCSD().getAllFunctionDefRecur();
        } else {
            // add all the predefined functions who can be cited as strShrinkedRawName
            LinkedList<MemberFunction> listTheseMf = CitingSpaceDefinition.lookupFunctionDef(strShrinkedRawName, -1, true, lCitingSpaces);
            // now compare their number of parameters
            for (MemberFunction mf : listTheseMf)    {
                boolean bParamNumMatch = false;
                if (nNumOfParams < 0) {
                    bParamNumMatch = true;
                } else {
                    int nMinParamNum = nNumOfParams;
                    int nMaxParamNum = bOptionalParam?Integer.MAX_VALUE:nMinParamNum;
                    int nMinParamNumMF = mf.getMinNumParam();
                    int nMaxParamNumMF = mf.isIncludeOptParam()?Integer.MAX_VALUE:nMinParamNumMF;
                    if (nMinParamNum >= nMinParamNumMF && nMaxParamNum <= nMaxParamNumMF)   {
                        bParamNumMatch = true;
                    }
                }
                if (bParamNumMatch) {
                    listMf.add(mf);
                }
            }
        }
        LinkedList<MemberFunction> listReturnedMfs = new LinkedList<MemberFunction>();
        // check and filter off duplicates.
        for (MemberFunction mf : listMf)    {
            boolean bShouldAdd = true;
            for (MemberFunction mfAdded : listReturnedMfs)  {
                if (mfAdded.getAbsNameWithCS().equals(mf.getAbsNameWithCS())
                        && mfAdded.getMinNumParam() == mf.getMinNumParam()
                        && mfAdded.isIncludeOptParam() == mf.isIncludeOptParam()) {
                    bShouldAdd = false;
                    break;
                }
            }
            if (bShouldAdd) {
                listReturnedMfs.add(mf);
            }
        }
        return listReturnedMfs;
    }
    
    /*
    public static void getReferredFuncDefs(String strShrinkedRawName, int nNumOfParams, Statement statementCall, LinkedList<ModuleInfo> listAllReferredFuncs) throws InterruptedException, JMFPCompErrException {
        // cannot call getReferredMFDefs because this is to find referred calls while that is to find referred definitions.
        
        List<String[]> lCitingSpaces = MFPAdapter.getAllCitingSpaces(null);  // use statement 
        LinkedList<MemberFunction> listMf = new LinkedList<MemberFunction>();
        if (strShrinkedRawName.equals("*")) {
            // add all the predefined function
            listMf = CitingSpaceDefinition.mscsdTOP.getAllFunctionDefRecur();
        } else if (nNumOfParams < 0) {
            // add all the predefined functions who can be cited as strShrinkedRawName
            listMf = CitingSpaceDefinition.LookupFunctionDef(strShrinkedRawName, -1, true, lCitingSpaces);
        } else {
            // add the particular function
            MemberFunction mf = CitingSpaceDefinition.locateFunctionCall(strShrinkedRawName, nNumOfParams, lCitingSpaces);
            if (mf != null) {
                listMf.add(mf);
            }
        }
        if (listMf.size() == 0) {
            if (statementCall == null) {
                throw new JMFPCompErrException("", 0, 0, ERRORTYPES.UNDEFINED_FUNCTION, strShrinkedRawName);
            } else {
                throw new JMFPCompErrException(statementCall.m_strFilePath,
                                               statementCall.m_nStartLineNo, statementCall.m_nEndLineNo,
                                               ERRORTYPES.UNDEFINED_FUNCTION,
                                               strShrinkedRawName);
            }
        }
        LinkedList<ModuleInfo> listReferredFuncs = new LinkedList<ModuleInfo>();
        for (MemberFunction mf : listMf) {
            if (mf instanceof FunctionEntry) {
                ModuleInfo infoThis = new ModuleInfo();
                infoThis.m_nModuleType = ModuleInfo.FUNCTION_MODULE;
                infoThis.m_strModuleName = mf.getAbsNameWithCS();
                infoThis.m_nModuleParam1 = nNumOfParams >= 0? nNumOfParams : (mf.isIncludeOptParam()?nNumOfParams:mf.getMinNumParam());
                infoThis.m_objExtraInfo1 = statementCall;
                listReferredFuncs.add(infoThis);
                FunctionEntry funcEntry = (FunctionEntry) mf;
                ProgContext progContext = new ProgContext();
                progContext.mstaticProgContext.mstCallingFunc = funcEntry.m_sf;
                for (int idx = funcEntry.m_nStartStatementPos + 1; idx < funcEntry.m_sLines.length; idx ++) {
                    Statement statement = funcEntry.m_sLines[idx];
                    LinkedList<ModuleInfo> listThisStatementReferred = statement.m_statementType.getReferredModules(progContext);
                    for (ModuleInfo eachReferred : listThisStatementReferred) {
                        eachReferred.m_objExtraInfo1 = statement;
                    }
                    ModuleInfo.mergeIntoList(listThisStatementReferred, listReferredFuncs);
                    if (statement.m_statementType.mstrType.equals("endf")) {
                        break;
                    }
                }
                LinkedList<ModuleInfo> listAllReferredFuncsCpy = new LinkedList<ModuleInfo>();
                listAllReferredFuncsCpy.addAll(listAllReferredFuncs);
                ModuleInfo.mergeIntoList(listReferredFuncs, listAllReferredFuncs);
                for (ModuleInfo info : listReferredFuncs) {
                    if (info.m_nModuleType == ModuleInfo.FUNCTION_MODULE
                            && (!info.m_strModuleName.equalsIgnoreCase(mf.getAbsNameWithCS()) || info.m_nModuleParam1 != nNumOfParams)
                            && !info.isModuleInfoInList(listAllReferredFuncsCpy)) {
                        getReferredFuncDefs(info.m_strModuleName, info.m_nModuleParam1, (Statement)(info.m_objExtraInfo1), listAllReferredFuncs);
                    }
                }
            }
        }
    }
     */
    
    public static class GetReferredFuncEntriesParamStruct {
        public String mstrShrinkedRawName;
        public int mnNumOfParams;
        public boolean mbOptionalParam;
    }
    // this function returns all the function entries from a call...endcall block of code
    public static LinkedList<ReferenceUnit> getReferenceUnits4CallBlock(Statement[] sLines, int nStartStatementPos, Statement_function sf,
                                                                    LinkedList<GetReferredFuncEntriesParamStruct> processedParamStructs,
                                                                    CompileAdditionalInfo cai)
                                                                   throws InterruptedException {
        LinkedList<ReferenceUnit> listRu = new LinkedList<ReferenceUnit>();                
        ProgContext progContext = new ProgContext();
        progContext.mstaticProgContext.setCallingFunc(sf);
        ScriptAnalyzer.InFunctionCSManager inFuncCSMgr = new ScriptAnalyzer.InFunctionCSManager(progContext.mstaticProgContext);
        LinkedList<ModuleInfo> listAllReferenceModules = new LinkedList<ModuleInfo>();
        try {
            InFuncCSRefAnalyzer.getAllReferredModules(nStartStatementPos, sLines,
                    InFuncCSRefAnalyzer.CALL_ENDCALL, listAllReferenceModules, cai, progContext, inFuncCSMgr);
        } finally {
            // do this to prevent an exception throw in getAllFuncModules function and the number of cs stacks
            // changed without restoring.
            inFuncCSMgr.popAllCSStacks();
        }
        for (ModuleInfo eachReferred : listAllReferenceModules) {
            if (eachReferred.mnModuleType == ModuleInfo.CLASS_MODULE) {
                Boolean bShouldAdd = true;
                MFPClassDefinition clsDef = eachReferred.getClassDef();
                ReferenceUnit refUnit = new ReferenceUnit(clsDef);
                for (ReferenceUnit ru : listRu)  {
                    if (refUnit.equals(ru)) {
                        bShouldAdd = false;
                        break;
                    }
                }
                if (bShouldAdd) {
                    listRu.add(refUnit);
                }
            }
			// have to remove duplicates for both class module and function module
			boolean bProcessedEntry = false;
			for (GetReferredFuncEntriesParamStruct existParamStruct : processedParamStructs)    {
				if (existParamStruct.mbOptionalParam == false
						&& existParamStruct.mnNumOfParams == eachReferred.mnModuleParam1
						&& existParamStruct.mstrShrinkedRawName.equals(eachReferred.mstrModuleName)) {
					bProcessedEntry = true;
					break;
				}
			}
			if (bProcessedEntry) {
				continue;   // no need to process it again.
			}
			LinkedList<ReferenceUnit> listTheseRUs = getReferenceUnits(eachReferred.mstrModuleName,
																			eachReferred.mnModuleParam1,
																			false, processedParamStructs, cai);
			// check and filter off duplicates.
			for (ReferenceUnit ruThis : listTheseRUs)    {
				boolean bShouldAdd = true;
				for (ReferenceUnit ruAdded : listRu)  {
					if (ruAdded.equals(ruThis)) {
						bShouldAdd = false;
						break;
					}
				}
				if (bShouldAdd) {
					listRu.add(ruThis);
				}
			}
        }
        
        return listRu;
    }    
    
    // this function assume citing space is default citing space, i.e. "" and ["", "mfp", "*"], so if using shrinked name,
    // this function searches only default citing space. If developer wants to get referred funcentries for an out of default
    // cs function, parameter strShrinkedRawName is shrinked and small letter function name with full or partial CS path.
    public static LinkedList<ReferenceUnit> getReferenceUnits(String strShrinkedRawName, int nNumOfParams, boolean bOptionalParam,
                                                                    LinkedList<GetReferredFuncEntriesParamStruct> processedParamStructs,
                                                                    CompileAdditionalInfo cai)
                                                                   throws InterruptedException {
        GetReferredFuncEntriesParamStruct paramStruct = new GetReferredFuncEntriesParamStruct();
        paramStruct.mstrShrinkedRawName = strShrinkedRawName;
        paramStruct.mnNumOfParams = nNumOfParams;
        paramStruct.mbOptionalParam = bOptionalParam;
        processedParamStructs.add(paramStruct);
        LinkedList<MemberFunction> listMf = getReferredMFDefs(strShrinkedRawName, nNumOfParams, bOptionalParam);
        LinkedList<ReferenceUnit> listRu = new LinkedList<ReferenceUnit>();
        for (MemberFunction mf : listMf) {
            if (mf instanceof FunctionEntry) {
                FunctionEntry funcEntry = (FunctionEntry) mf;
                listRu.add(new ReferenceUnit(funcEntry));
            }
        }
        for (MemberFunction mf : listMf) {
            LinkedList<ModuleInfo> listAllReferredModules = new LinkedList<ModuleInfo>();
            if (mf instanceof MFPClassDefinition.ConstructorFunction) {
                // this is a class constructor
                MFPClassDefinition mfpClsDef = MFPClassDefinition.getClassDefinitionMap().get(mf.getAbsNameWithCS());
                listAllReferredModules = mfpClsDef.getReferredModules(cai);

            } else if (mf instanceof FunctionEntry) {
                FunctionEntry funcEntry = (FunctionEntry) mf;
                ProgContext progContext = new ProgContext();
                progContext.mstaticProgContext.setCallingFunc(funcEntry.m_sf);
                ScriptAnalyzer.InFunctionCSManager inFuncCSMgr = new ScriptAnalyzer.InFunctionCSManager(progContext.mstaticProgContext);
                try {
                    InFuncCSRefAnalyzer.getAllReferredModules(funcEntry.m_nStartStatementPos, funcEntry.m_sLines,
                            InFuncCSRefAnalyzer.FUNCTION_ENDF, listAllReferredModules, cai, progContext, inFuncCSMgr);
                } finally {
                    // do this to prevent an exception throw in getAllFuncModules function and the number of cs stacks
                    // changed without restoring.
                    inFuncCSMgr.popAllCSStacks();
                }
            }
            for (ModuleInfo eachReferred : listAllReferredModules) {
                if (eachReferred.mnModuleType == ModuleInfo.CLASS_MODULE) {
                    Boolean bShouldAdd = true;
                    MFPClassDefinition clsDef = eachReferred.getClassDef();
                    ReferenceUnit refUnit = new ReferenceUnit(clsDef);
                    for (ReferenceUnit ru : listRu)  {
                        if (refUnit.equals(ru)) {
                            bShouldAdd = false;
                            break;
                        }
                    }
                    if (bShouldAdd) {
                        listRu.add(refUnit);
                    }
                }
				// have to remove duplicates for both class module and function module
				boolean bProcessedEntry = false;
				for (GetReferredFuncEntriesParamStruct existParamStruct : processedParamStructs)    {
					if (existParamStruct.mbOptionalParam == false
							&& existParamStruct.mnNumOfParams == eachReferred.mnModuleParam1
							&& existParamStruct.mstrShrinkedRawName.equals(eachReferred.mstrModuleName)) {
						bProcessedEntry = true;
						break;
					}
				}
				if (bProcessedEntry) {
					continue;   // no need to process it again.
				}
				LinkedList<ReferenceUnit> listTheseRUs = getReferenceUnits(eachReferred.mstrModuleName,
																				eachReferred.mnModuleParam1,
																				false, processedParamStructs, cai);
				// check and filter off duplicates.
				for (ReferenceUnit ruThis : listTheseRUs)    {
					boolean bShouldAdd = true;
					for (ReferenceUnit ruAdded : listRu)  {
						if (ruAdded.equals(ruThis)) {
							bShouldAdd = false;
							break;
						}
					}
					if (bShouldAdd) {
						listRu.add(ruThis);
					}
				}
            }
        }
        
        return listRu;
    }    

    public static LinkedList<String> getReferredFilePathsAndOtherInfo4CallBlock(Statement[] sLines, int nStartStatementPos, Statement_function sf, CompileAdditionalInfo cai) throws InterruptedException {
        LinkedList<ReferenceUnit> listRu = getReferenceUnits4CallBlock(sLines, nStartStatementPos, sf, new LinkedList<GetReferredFuncEntriesParamStruct>(), cai);
        LinkedList<String> listReferredFilePaths = new LinkedList<String>();
        for (ReferenceUnit ru : listRu) {
            boolean bAdded = false;
            String strFilePath = null;
            if (ru.getFunctionEntry() != null) { // this is a function entry
                FunctionEntry fe = ru.getFunctionEntry();
                /*fe.mstrarrayPathSpace should never be null.*/
                if (fe.getPathSpace().length == 0 || fe.getPathSpace()[0].length() == 0 
                       || fe.getPathSpace()[0].charAt(0) == '\u0000') {
                    continue;   // this is a system built in or predefined lib file or invalid lib.
                }
                strFilePath = MFPAdapter.cvtPathSpace2Path(fe.getPathSpace());
            } else {    // this is a class definition
                MFPClassDefinition clsDef = ru.getClassDefinition();
                if (clsDef.mlistPathSpaces.size() != 1) {
                    strFilePath = null;
                } else {
                    String[] pathSpace = clsDef.mlistPathSpaces.get(0);
                    if (pathSpace.length == 0 || pathSpace[0].length() == 0 
                       || pathSpace[0].charAt(0) == '\u0000') {
                        continue;   // this is a system built in or predefined lib file or invalid lib.
                    }
                    strFilePath = MFPAdapter.cvtPathSpace2Path(pathSpace);
                }
            }
            if (strFilePath == null || strFilePath.trim().length() == 0) {
                continue;
            }
            for (String strExist : listReferredFilePaths) {
                if (strExist.equals(strFilePath)) {
                    bAdded = true;
                    break;
                }
            }
            if (!bAdded)    {
                listReferredFilePaths.add(strFilePath);
            }
        }
        return listReferredFilePaths;
    }    

    public static LinkedList<String> getReferredFilePathsAndOtherInfo(String strShrinkedRawName, int nNumOfParams, boolean bOptionalParam, CompileAdditionalInfo cai) throws InterruptedException {
        LinkedList<ReferenceUnit> listRu = getReferenceUnits(strShrinkedRawName, nNumOfParams, bOptionalParam, new LinkedList<GetReferredFuncEntriesParamStruct>(), cai);
        LinkedList<String> listReferredFilePaths = new LinkedList<String>();
        for (ReferenceUnit ru : listRu) {
            boolean bAdded = false;
            String strFilePath = null;
            if (ru.getFunctionEntry() != null) { // this is a function entry
                FunctionEntry fe = ru.getFunctionEntry();
                /*fe.mstrarrayPathSpace should never be null.*/
                if (fe.getPathSpace().length == 0 || fe.getPathSpace()[0].length() == 0 
                       || fe.getPathSpace()[0].charAt(0) == '\u0000') {
                    continue;   // this is a system built in or predefined lib file or invalid lib.
                }
                strFilePath = MFPAdapter.cvtPathSpace2Path(fe.getPathSpace());
            } else {    // this is a class definition
                MFPClassDefinition clsDef = ru.getClassDefinition();
                if (clsDef.mlistPathSpaces.size() != 1) {
                    strFilePath = null;
                } else {
                    String[] pathSpace = clsDef.mlistPathSpaces.get(0);
                    if (pathSpace.length == 0 || pathSpace[0].length() == 0 
                       || pathSpace[0].charAt(0) == '\u0000') {
                        continue;   // this is a system built in or predefined lib file or invalid lib.
                    }
                    strFilePath = MFPAdapter.cvtPathSpace2Path(pathSpace);
                }
            }
            if (strFilePath == null || strFilePath.trim().length() == 0) {
                continue;
            }
            for (String strExist : listReferredFilePaths) {
                if (strExist.equals(strFilePath)) {
                    bAdded = true;
                    break;
                }
            }
            if (!bAdded)    {
                listReferredFilePaths.add(strFilePath);
            }
        }
        return listReferredFilePaths;
    }    
}
