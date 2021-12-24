/*
 * MFP project, MFPClassDefinition.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Oomfp;

import com.cyzapps.Jfcalc.BuiltInFunctionLib;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassClass;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.CompileAdditionalInfo;
import com.cyzapps.Jmfp.ErrorProcessor;
import com.cyzapps.Jmfp.FunctionEntry;
import com.cyzapps.Jmfp.InFuncCSRefAnalyzer;
import com.cyzapps.Jmfp.ModuleInfo;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.ScriptAnalyzer;
import com.cyzapps.Jmfp.Statement;
import com.cyzapps.Jmfp.StatementType;
import com.cyzapps.Jmfp.Statement_function;
import com.cyzapps.Jmfp.Statement_class;
import com.cyzapps.OSAdapter.ParallelManager.CallObject;
import com.cyzapps.adapter.MFPAdapter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class defines the node inheritance and is immutable (except the children).
 * And the instance of this class should be singleton.
 * @author Tony
 */
public final class MFPClassDefinition extends CitingSpaceDefinition {
	private static final long serialVersionUID = 1L;

    public static final Map<String, MFPClassDefinition> CLASS_DEF_SYS_MAP = new HashMap<String, MFPClassDefinition>();   // system level MFPClassDefinition map (i.e. maximum subset of main entity and a sandbox).
    
    // main entity class type definition map. null means it hasn't been initialized.
    // we do not initialize it to new HashMap<String, MFPClassDefinition>() here because
    // it is possible that an initialized msclassDefSysMap doesn't have any user defined
    // class.
	public static Map<String, MFPClassDefinition> msclassDefFullMap = null;
            
    public static final MFPClassDefinition OBJECT = new MFPClassDefinition();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(OBJECT);}
    
    public static Map<String, MFPClassDefinition> getClassDefinitionMap() {   // find right MFPDataTypeDef for this thread.
        Long currentThreadId = Thread.currentThread().getId();
        if (!CallObject.msmapThreadId2SessionInfo.containsKey(currentThreadId)) {
            if (msclassDefFullMap == null) {
                return CLASS_DEF_SYS_MAP;
            } else {
                return msclassDefFullMap;
            }
        } else {
            return CallObject.msmapThreadId2SessionInfo.get(currentThreadId).getClassDefinitionMap();
        }
    }
    
    public static MFPClassDefinition addClassDefinition2Map(MFPClassDefinition classDef) {
        Map<String, MFPClassDefinition> classDefMap = getClassDefinitionMap();
        classDefMap.put(classDef.selfTypeDef.toString(), classDef); // OK, we always replace old classDef by new one.
        return classDef;
    }

    static {
        // we do not add built-in types except object because object is the base type
        // of all defined MFP types.
        CLASS_DEF_SYS_MAP.put(OBJECT.selfTypeDef.toString(), OBJECT);
    }
    
    public final MFPDataTypeDef selfTypeDef;

    public final LinkedList<String[]> lCitingSpaceStack;
    public final LinkedList<LinkedList<String[]>> lUsingCitingSpacesStack;
    
    public class ParentClassInfo  implements Serializable {
        private static final long serialVersionUID = 1L;
        private String parentClassName; // this is not full cs path name
        private String parentClassFullName = null; // this is full cs path name.
        // we cannot cache parentClassDef here because, it is possible that
        // after reloading source file, parent class definition is changed.
        // so we have to always use parentClassName to find class defintion.
        //private MFPClassDefinition parentClassDef;
        
        public ParentClassInfo() {
            parentClassFullName = parentClassName = OBJECT.selfTypeDef.toString();
            //parentClassDef = OBJECT;
        }
        
        public ParentClassInfo(String pcName) {
            parentClassName = pcName;
            if (pcName.equals(OBJECT.selfTypeDef.toString())) {
                parentClassFullName = OBJECT.selfTypeDef.toString();
                //parentClassDef = OBJECT;
            } else {
                // note that we'd better not to call getReferredCitingSpaces to merge lCitingSpaceStack and lUsingCitingSpacesStack
                // in the very beginning and cache the listCSPaths, because before loading all citingspaces, new citingspaces
                // can still be added-in.
                List<String[]> listCSPaths = MFPAdapter.getReferredCitingSpaces(lCitingSpaceStack, lUsingCitingSpacesStack);
                List<CitingSpaceDefinition> listCSes = CitingSpaceDefinition.lookupCSDef(parentClassName, listCSPaths);
                int idx = 0;
                for (; idx < listCSes.size(); idx ++) {
                    if (listCSes.get(idx) instanceof MFPClassDefinition) {
                        parentClassFullName = listCSes.get(idx).mstrFullNameWithCS;
                        //parentClassDef = (MFPClassDefinition)listCSes.get(idx);
                        break;
                    }
                }
                if (idx == listCSes.size()) {
                    // cannot find the parent now. But possibly we can find it later after some new libs are loaded.
                    parentClassFullName = null;
                    //parentClassDef = null;
                }
            }
        }
        
        public MFPClassDefinition getClassDef() {
            if (null == parentClassFullName) {
                // if parentClassFullName is null, we need to find it first. This is later binding
                List<String[]> listCSPaths = MFPAdapter.getReferredCitingSpaces(lCitingSpaceStack, lUsingCitingSpacesStack);
                List<CitingSpaceDefinition> listCSes = CitingSpaceDefinition.lookupCSDef(parentClassName, listCSPaths);
                int idx = 0;
                for (; idx < listCSes.size(); idx ++) {
                    if (listCSes.get(idx) instanceof MFPClassDefinition) {
                        parentClassFullName = listCSes.get(idx).mstrFullNameWithCS;
                        //parentClassDef = (MFPClassDefinition)listCSes.get(idx);
                        break;
                    }
                }
            }
            if (null == parentClassFullName) {
                return null;
            } else {
                Map<String, MFPClassDefinition> clsDefMap = getClassDefinitionMap();
                return clsDefMap.get(parentClassFullName);
            }
            /*
            if (parentClassDef == null) {
                // if parentClassDef is null, we need to find it first.
                List<String[]> listCSPaths = MFPAdapter.getReferredCitingSpaces(lCitingSpaceStack, lUsingCitingSpacesStack);
                List<CitingSpaceDefinition> listCSes = CitingSpaceDefinition.lookupCSDef(parentClassName, listCSPaths);
                int idx = 0;
                for (; idx < listCSes.size(); idx ++) {
                    if (listCSes.get(idx) instanceof MFPClassDefinition) {
                        parentClassDef = (MFPClassDefinition)listCSes.get(idx);
                        break;
                    }
                }
                if (idx == listCSes.size()) {
                    // cannot find the parent now. But possibly we can find it later after some new libs are loaded.
                    parentClassDef = null;
                }
            }
            return parentClassDef;
            */
        }
    }
    public final ParentClassInfo[] parentClassInfos;
    // this list cannot be reassigned but you can add new memebers into it.
    public final LinkedList<MFPClassDefinition> children = new LinkedList<MFPClassDefinition>();
    
    public final LinkedList<MemberFunction> privateFunctions = new LinkedList<MemberFunction>();  // this includes all the private functions. Public functions are defined is citingspace definition.
    public final MFPClassDefinition[] publicInnerClasses;  // this includes all the public inner classes before they are added in citingspacedef.
    public final MFPClassDefinition[] privateInnerClasses;  // this includes all the private inner classes. Public inner classes are defined in citingspacedef.

    public final ClassVariableInfo[] privateVariables;   // this includes all the private class variables.

    public final ClassVariableInfo[] publicVariables; // this includes all the public class variables.
    
    public final int[] helpStatementsPos;   // help and endh statment positions
    
    public final String[] rawLines; // raw text code lines
    
    public final Statement[] statements;    // statements of the code
    public final int classDeclarePosition;  // class statement declare position
    
    public final MemberFunction mfConstructor;  // constructor function. This function cannot be overridden.
    // user overrides functions. They are always public. They all throw two types of exceptions,
    // one is self is null, the other is incorrect number of parameters. Equals and ToString do
    // not throw other type of exceptions. Copy and DeepCopy may throw exception when initialize
    // a class instance.
    public final MemberFunction mfEquals;
    public final MemberFunction mfIsSame;
    public final MemberFunction mfToString;
    public final MemberFunction mfCopy;
    public final MemberFunction mfDeepCopy;
    public final MemberFunction mfHash;
    private MFPClassDefinition() {
        super(new String[] {"", "mfp", "lang", "object"});
        // this is the constructor for top level class, i.e. object
        // fullType name is "::mfp::lang::object";
        selfTypeDef = MFPDataTypeDef.createDataTypeDef("::mfp::lang::object");
        lCitingSpaceStack = new LinkedList<String[]>();
        lUsingCitingSpacesStack = new LinkedList<LinkedList<String[]>>();
        parentClassInfos = new ParentClassInfo[0];
        privateVariables = new ClassVariableInfo[0];
        publicVariables = new ClassVariableInfo[0];
        publicInnerClasses = new MFPClassDefinition[0];
        privateInnerClasses = new MFPClassDefinition[0];
        
        // object is a built-in type so that user defined code information should be null;
        helpStatementsPos = null;
        rawLines = null;
        statements = null;
        classDeclarePosition = -1;
        
        mfConstructor = new ConstructorFunction(null, null, null);
        mfEquals = new DefaultEqualsFunction();
        mlistFuncMembers.add(mfEquals);
        mfIsSame = new DefaultIsSameFunction();
        mlistFuncMembers.add(mfIsSame);
        mfToString = new DefaultToStringFunction();
        mlistFuncMembers.add(mfToString);
        mfCopy = new DefaultCopyFunction();
        mlistFuncMembers.add(mfCopy);
        mfDeepCopy = new DefaultDeepCopyFunction();
        mlistFuncMembers.add(mfDeepCopy);
        mfHash = new DefaultHashFunction();
        mlistFuncMembers.add(mfHash);
    }

    private MFPClassDefinition(int clsDeclarePos, String[] strarrayPathSpaces,
                                LinkedList<String[]> lCSPathes,
                                LinkedList<LinkedList<String[]>> lUsingCSPathes,
                                LinkedList<MFPClassDefinition> publicInnerClassList,
                                LinkedList<MFPClassDefinition> privateInnerClassList,
                                LinkedList<ClassVariableInfo> publicVariableList,
                                LinkedList<ClassVariableInfo> privateVariableList,
                                LinkedList<FunctionEntry> publicFunctionList,
                                LinkedList<FunctionEntry> privateFunctionList,
                                int[] helpBlock, String[] strLines, Statement[] sStatements) {
        
        super(((Statement_class)sStatements[clsDeclarePos].mstatementType).getClassNameWithCS().split("::"), strarrayPathSpaces);
        Statement_class myStatementType = (Statement_class)sStatements[clsDeclarePos].mstatementType;
        maccess = myStatementType.access;   // set private or public
        String fullTypeName = myStatementType.getClassNameWithCS();
        String[] parentNameArray = myStatementType.superNameArray;
        selfTypeDef = MFPDataTypeDef.createDataTypeDef(fullTypeName);
        lCitingSpaceStack = new LinkedList<String[]>();
        lCitingSpaceStack.addAll(lCSPathes);    // have to add all because lCSPathes may change outside
        lUsingCitingSpacesStack = new LinkedList<LinkedList<String[]>>();
        lUsingCitingSpacesStack.addAll(lUsingCSPathes);
        if (parentNameArray.length == 0) {
            // if no parent is declared, OBJECT is the parent.
            parentClassInfos = new ParentClassInfo[1];
            parentClassInfos[0] = new ParentClassInfo();
        } else {
            parentClassInfos = new ParentClassInfo[parentNameArray.length];
            for (int idx = 0; idx < parentClassInfos.length; idx ++) {
                parentClassInfos[idx] = new ParentClassInfo(parentNameArray[idx]);   // set parentClassInfo. Note that it might still be null. But it can be loaded later.
            }
        }
        publicVariables = publicVariableList.toArray(new ClassVariableInfo[]{});
        privateVariables = privateVariableList.toArray(new ClassVariableInfo[]{});
        privateInnerClasses = privateInnerClassList.toArray(new MFPClassDefinition[] {});
        privateFunctions.addAll(privateFunctionList);
        // save all public functions before add them to citing space definition.
        publicInnerClasses = publicInnerClassList.toArray(new MFPClassDefinition[] {});
        mlistFuncMembers.addAll(publicFunctionList);
        
        helpStatementsPos = helpBlock;
        rawLines = strLines;
        statements = sStatements;
        classDeclarePosition = clsDeclarePos;
        
        mfConstructor = new ConstructorFunction(helpStatementsPos, rawLines, statements);
        MemberFunction memberFunctionEquals = new DefaultEqualsFunction();
        mlistFuncMembers.add(memberFunctionEquals);
        MemberFunction memberFunctionIsSame = new DefaultIsSameFunction();
        mlistFuncMembers.add(memberFunctionIsSame);
        MemberFunction memberFunctionToString = new DefaultToStringFunction();
        mlistFuncMembers.add(memberFunctionToString);
        MemberFunction memberFunctionCopy = new DefaultCopyFunction();
        mlistFuncMembers.add(memberFunctionCopy);
        MemberFunction memberFunctionDeepCopy = new DefaultDeepCopyFunction();
        mlistFuncMembers.add(memberFunctionDeepCopy);
        MemberFunction memberFunctionHash = new DefaultHashFunction();
        mlistFuncMembers.add(memberFunctionHash);

        // now try to find overridden.
        for (int idx = 0; idx < publicFunctionList.size(); idx ++) {
            FunctionEntry fe = publicFunctionList.get(idx);
            Statement_function stateFunc = publicFunctionList.get(idx).getStatementFunction();
            String[] paramStrs = stateFunc.m_strParams;
            if (paramStrs.length > 0 && paramStrs[0].equals("self")) {  // no need to check case for self as Statement_function has turned to small case
                int feMinNumParam = fe.getMinNumParam();
                int feMaxNumParam = fe.getMaxNumParam();
                if (feMaxNumParam == -1) {
                    feMaxNumParam = 100;    // 100 is a number larger than any built-in default function parameter count.
                }
                if (stateFunc.getFunctionPureName().equals("__equals__") && feMinNumParam <= 2 && feMaxNumParam >= 2) {
                    // this member must be a function which is __equals__(self, obj)
                    memberFunctionEquals = fe;
                } else if (stateFunc.getFunctionPureName().equals("__is_same__") && feMinNumParam <= 2 && feMaxNumParam >= 2) {
                    // this member must be a function which is __equals__(self, obj)
                    memberFunctionIsSame = fe;
                } else if (stateFunc.getFunctionPureName().equals("__to_string__") && feMinNumParam <= 1 && feMaxNumParam >= 1) {
                    // this member must be a function which is __to_string__(self)
                    memberFunctionToString = fe;
                } else if (stateFunc.getFunctionPureName().equals("__copy__") && feMinNumParam <= 1 && feMaxNumParam >= 1) {
                    // this member must be a function which is __copy__(self)
                    memberFunctionCopy = fe;
                } else if (stateFunc.getFunctionPureName().equals("__deep_copy__") && feMinNumParam <= 1 && feMaxNumParam >= 1) {
                    // this member must be a function which is __deep_copy__(self)
                    memberFunctionDeepCopy = fe;
                } else if (stateFunc.getFunctionPureName().equals("__hash__") && feMinNumParam <= 1 && feMaxNumParam >= 1) {
                    // this member must be a function which is __hash__(self)
                    memberFunctionHash = fe;
                }
            }
        }
        mfEquals = memberFunctionEquals;
        mfIsSame = memberFunctionIsSame;
        mfToString = memberFunctionToString;
        mfCopy = memberFunctionCopy;
        mfDeepCopy = memberFunctionDeepCopy;
        mfHash = memberFunctionHash;
    }
    
    public static MFPClassDefinition createClassDefinition(int classDeclarationPos, String[] strarrayPathSpaces,
                                                    LinkedList<String[]> lCSPathes,
                                                    LinkedList<LinkedList<String[]>> lUsingCSPathes,
                                                    LinkedList<MFPClassDefinition> publicInnerClassList,
                                                    LinkedList<MFPClassDefinition> privateInnerClassList,
                                                    LinkedList<ClassVariableInfo> publicVariableList,
                                                    LinkedList<ClassVariableInfo> privateVariableList,
                                                    LinkedList<FunctionEntry> publicFunctionList,
                                                    LinkedList<FunctionEntry> privateFunctionList,
                                                    int[] helpBlock, String[] strLines, Statement[] sStatements) {
        MFPClassDefinition mfpClassDef = new MFPClassDefinition(classDeclarationPos, strarrayPathSpaces, lCSPathes, lUsingCSPathes,
                publicInnerClassList, privateInnerClassList, publicVariableList, privateVariableList, publicFunctionList, privateFunctionList,
                helpBlock, strLines, sStatements);
        for (FunctionEntry fe: privateFunctionList) {
            fe.getStatementFunction().setOwnerClassDef(mfpClassDef);
        }
        for (MemberFunction mf: publicFunctionList) {
            if (mf instanceof FunctionEntry) {
                ((FunctionEntry)mf).getStatementFunction().setOwnerClassDef(mfpClassDef);
            }
        }
        return addClassDefinition2Map(mfpClassDef);
    }
    
        
    /* constructor function */
    public class ConstructorFunction extends BuiltInFunctionLib.BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;
        
        public final int[] helpStatementsPos;   // help and endh statment positions
        public final String[] rawLines; // raw text code lines
        public final Statement[] statements;    // statements of the code

        public ConstructorFunction(int[] helpBlock, String[] strLines, Statement[] sStatements) {
            mstrProcessedNameWithFullCS = selfTypeDef.toString();
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 0;
            mnMinParamNum = 0;
            
            helpStatementsPos = helpBlock;
            rawLines = strLines;
            statements = sStatements;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException
        {
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            try {
                DataClassClass obj = new DataClassClass(new MFPClassInstance(MFPClassDefinition.this));
                return obj;
            } catch (ErrorProcessor.JMFPCompErrException ex) {
                Logger.getLogger(MFPClassDefinition.class.getName()).log(Level.SEVERE, null, ex);
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CANNOT_INITIALIZE_CLASS_INSTANCE);
            }
        }
    }
        
    /* default equals function */
    public class DefaultEqualsFunction extends BuiltInFunctionLib.BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public DefaultEqualsFunction() {
            mstrProcessedNameWithFullCS = selfTypeDef.toString() + "::__equals__";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException
        {
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass selfObj = tsParameter.removeLast();
            if (DCHelper.isDataClassType(selfObj, DATATYPES.DATUM_NULL)) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_NULL_REFERENCE);
            }
            DataClass toCompareObj = tsParameter.removeLast();
            if (selfObj instanceof DataClassClass && toCompareObj instanceof DataClassClass && !toCompareObj.isNull()) {
                if (((DataClassClass)selfObj).getClassInstance() == ((DataClassClass)toCompareObj).getClassInstance()) {
                    return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
                }
            }
            return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
        }
    }
        
    /* default is_same function */
    public class DefaultIsSameFunction extends BuiltInFunctionLib.BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public DefaultIsSameFunction() {
            mstrProcessedNameWithFullCS = selfTypeDef.toString() + "::__is_same__";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException
        {
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass selfObj = tsParameter.removeLast();
            if (DCHelper.isDataClassType(selfObj, DATATYPES.DATUM_NULL)) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_NULL_REFERENCE);
            }
            DataClass toCompareObj = tsParameter.removeLast();
            if (selfObj instanceof DataClassClass && toCompareObj instanceof DataClassClass && !toCompareObj.isNull()) {
                if (((DataClassClass)selfObj).getClassInstance() == ((DataClassClass)toCompareObj).getClassInstance()) {
                    return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
                }
            }
            return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
        }
    }
        
    /* default toString function */
    public class DefaultToStringFunction extends BuiltInFunctionLib.BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public DefaultToStringFunction() {
            mstrProcessedNameWithFullCS = selfTypeDef.toString() + "::__to_string__";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException
        {
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass selfObj = tsParameter.removeLast();
            if (DCHelper.isDataClassType(selfObj, DATATYPES.DATUM_NULL)) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_NULL_REFERENCE);
            }
            String retStr = ((DataClassClass)selfObj).getClassInstance().toString();
            return new DataClassString(retStr);
        }
    }
        
    /* default copy function */
    public class DefaultCopyFunction extends BuiltInFunctionLib.BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public DefaultCopyFunction() {
            mstrProcessedNameWithFullCS = selfTypeDef.toString() + "::__copy__";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException
        {
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass selfObj = tsParameter.removeLast();
            if (DCHelper.isDataClassType(selfObj, DATATYPES.DATUM_NULL)) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_NULL_REFERENCE);
            }
            DataClassClass selfClassObj = DCHelper.lightCvtOrRetDCClass(selfObj);
            if (selfClassObj.getClassInstance().getClassDefinition() == null) {
                // this could happen in deserialization.
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_MFP_CLASS_TYPE);
            }
            return new DataClassClass(selfClassObj.getClassInstance().copySelf());
        }
    }
        
    /* default deep_copy function */
    public class DefaultDeepCopyFunction extends BuiltInFunctionLib.BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public DefaultDeepCopyFunction() {
            mstrProcessedNameWithFullCS = selfTypeDef.toString() + "::__deep_copy__";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException
        {
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass selfObj = tsParameter.removeLast();
            if (DCHelper.isDataClassType(selfObj, DATATYPES.DATUM_NULL)) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_NULL_REFERENCE);
            }
            DataClassClass selfClassObj = DCHelper.lightCvtOrRetDCClass(selfObj);
            if (selfClassObj.getClassInstance().getClassDefinition() == null) {
                // this could happen in deserialization.
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_MFP_CLASS_TYPE);
            }
            return new DataClassClass(selfClassObj.getClassInstance().cloneSelf());
        }
    }

    /* default hash function */
    public class DefaultHashFunction extends BuiltInFunctionLib.BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public DefaultHashFunction() {
            mstrProcessedNameWithFullCS = selfTypeDef.toString() + "::__hash__";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException
        {
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass selfObj = tsParameter.removeLast();
            if (DCHelper.isDataClassType(selfObj, DATATYPES.DATUM_NULL)) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_NULL_REFERENCE);
            }
            DataClassClass selfClassObj = DCHelper.lightCvtOrRetDCClass(selfObj);
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(selfClassObj.getClassInstance().hashCode()));
        }
    }
    //add OBJECT into citingspace structure
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(OBJECT);}
    
    @Override
    public void unloadFileOrFolder(String[] strarrayPathSpace)    {
        for (int i = 0; i < mlistPathSpaces.size(); i ++) {
            if (MFPAdapter.isMFPSLibFileOrSubFile(strarrayPathSpace, mlistPathSpaces.get(i)))    {
                // ok, this means it can be unloaded.
                isRemoved = true;
                break;
            }
        }
    }
    
    @Override
    public void clearMFPSLibs(CheckMFPSLibMode clearMode) {
        for (int i = 0; i < mlistPathSpaces.size(); i ++) {
            if (mlistPathSpaces.get(i).length > 0) {
                // this means it is an external / internal predefined mfps path space
                if (shouldPathSpaceBeChecked(clearMode, mlistPathSpaces.get(i))) {
                    // ok, this means it can be unloaded.
                    isRemoved = true;
                    break;
                }
            }
        }
    }
    
    private boolean isRemoved = false;
    @Override
    public boolean canBeRemoved() {
        return isRemoved;
    }
    
    // functionName has been uncapitalized. But ownerName is only trimmed. But we only need to compare it to self,
    // and create a temp variable, so no need to lowercase it. Simply compare owner name to self ignore case.
    public MemberFunction getMemberFunction(String ownerName, String functionName, int numOfParams, SpaceMember.AccessRestriction access, ProgContext progContext) {
        // first, let's look at public functions.
        for (MemberFunction mf: mlistFuncMembers) {
            if (mf.getPureNameWithoutCS().equals(functionName)) {
                int feMaxNumParam = mf.getMaxNumParam();
                int feMinNumParam = mf.getMinNumParam();
                if ((numOfParams >= feMinNumParam) && (feMaxNumParam == -1 || numOfParams <= feMaxNumParam)) {
                    return mf;
                }
            }
        }
        // ok, cannot find the member function from public function list, so we need to search parent classes
        for (ParentClassInfo parentClsInfo : parentClassInfos) {
            MFPClassDefinition parentClass = parentClsInfo.getClassDef();
            if (parentClass == null) {
                // we just continue. We do not throw exception here because if super class is invalid but we
                // find a function before going to super class, we do not throw exception anyway.
                continue;
            } else {
                // only search public functions of parent.
                MemberFunction mf = parentClass.getMemberFunction(ownerName,
                                                                functionName, numOfParams, SpaceMember.AccessRestriction.PUBLIC,
                                                                progContext);
                if (mf != null) {
                    return mf;
                }
            }
        }
        // if we are allowed to search private.
        if (access == SpaceMember.AccessRestriction.PRIVATE) {
            // no luck in the public searching, let's look at private function list only if the owner is
            // self or the owner is the same class as ownerClassDef. Note that private function cannot be
            // overridden. 
            Statement_function sf = progContext.mstaticProgContext.getCallingFunc();
            MFPClassDefinition funcOwnerClassDef = sf == null ? null:sf.getOwnerClassDef();
            if (ownerName.equalsIgnoreCase("self") || this == funcOwnerClassDef) {
                // this is self.xxxx. This implies that ownerClassDef cannot be null.
                for (MemberFunction mf: funcOwnerClassDef.privateFunctions) {
                    if (mf.getPureNameWithoutCS().equals(functionName)) {
                        int feMaxNumParam = mf.getMaxNumParam();
                        int feMinNumParam = mf.getMinNumParam();
                        if ((numOfParams >= feMinNumParam) && (feMaxNumParam == -1 || numOfParams <= feMaxNumParam)) {
                            return mf;
                        }
                    }
                }
            }
        }
        return null;
    }

    // note that this function will return class module itself.
    public LinkedList<ModuleInfo> getReferredModules(CompileAdditionalInfo cai) {
        LinkedList<ModuleInfo> listAllModules2Ret = new LinkedList<ModuleInfo>();
        if (this == MFPClassDefinition.OBJECT) {
            return listAllModules2Ret;  // never add object class as it is not user defined.
        }
        String absFuncNameWithCS = this.mstrFullNameWithCS;
        ModuleInfo moduleInfo = new ModuleInfo();
        moduleInfo.mnModuleType = ModuleInfo.CLASS_MODULE;
        moduleInfo.mstrModuleName = absFuncNameWithCS;
        moduleInfo.setClassDef(this);
        listAllModules2Ret.add(moduleInfo);

        // Although not formally supported, variable initialization may include function calls
        for (ClassVariableInfo varInfo: privateVariables) {
            LinkedList<ModuleInfo> listAllVariableModules = varInfo.getReferredModules(cai);
            // now merge this module
            ModuleInfo.mergeIntoList(listAllVariableModules, listAllModules2Ret);
        }
        for (ClassVariableInfo varInfo: publicVariables) {
            LinkedList<ModuleInfo> listAllVariableModules = varInfo.getReferredModules(cai);
            // now merge this module
            ModuleInfo.mergeIntoList(listAllVariableModules, listAllModules2Ret);
        }     
        
        LinkedList<MemberFunction> allMfs = new LinkedList<MemberFunction>();
        allMfs.addAll(privateFunctions);    // private functions
        allMfs.addAll(mlistFuncMembers);    // public functions
        for (MemberFunction mf: allMfs) {
            if (mf instanceof FunctionEntry) {
                FunctionEntry funcEntry = (FunctionEntry)mf;
                ProgContext progContext = new ProgContext();
                progContext.mstaticProgContext.setCallingFunc(funcEntry.getStatementFunction());
                ScriptAnalyzer.InFunctionCSManager inFuncCSMgr = new ScriptAnalyzer.InFunctionCSManager(progContext.mstaticProgContext);
                LinkedList<ModuleInfo> listAllFuncModules = new LinkedList<ModuleInfo>();
                try {
                    InFuncCSRefAnalyzer.getAllReferredModules(funcEntry.getStartStatementPos(), funcEntry.getStatementLines(),
                            InFuncCSRefAnalyzer.FUNCTION_ENDF, listAllFuncModules, cai, progContext, inFuncCSMgr);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MFPClassDefinition.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    // do this to prevent an exception throw in getAllFuncModules function and the number of cs stacks
                    // changed without restoring.
                    inFuncCSMgr.popAllCSStacks();
                }
                // now merge this module
                ModuleInfo.mergeIntoList(listAllFuncModules, listAllModules2Ret);
            }
        }
        
        // now go through its parents
        for (ParentClassInfo clsInfo: this.parentClassInfos) {
            MFPClassDefinition cls = clsInfo.getClassDef();
            if (cls == null) {
                // parent doesn't exist, so the whole class cannot be instanced. So we need not to worry about it.
                return new LinkedList<ModuleInfo>();
            }
            LinkedList<ModuleInfo> listParentModuleInfos = cls.getReferredModules(cai);
            // now merge this module
            ModuleInfo.mergeIntoList(listParentModuleInfos, listAllModules2Ret);
        }
        return listAllModules2Ret;
    }

    @Override
    public void LinkCitingSpaceDefinition() throws OoErrProcessor.JOoMFPErrException {
        if (mstrarrayCS == null || (mstrarrayCS.length > 0 && mstrarrayCS[0].length() != 0)) {
            throw new OoErrProcessor.JOoMFPErrException(OoErrProcessor.ERRORTYPES.ERROR_INVALID_CLASS_OR_CITINGSPACE);  // absolute citing space.
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
                            // the cs does exist and it conflicts with the MFPClassDefinition
                            if (csdAddIn.mlistCitingSpaceDefs.get(idx1) instanceof MFPClassDefinition)  {
                                // destination is a class, replace it
                                csdAddIn.mlistCitingSpaceDefs.set(idx1, this);
                            } else {
                                // destination is a normal citingspace
                                throw new OoErrProcessor.JOoMFPErrException(OoErrProcessor.ERRORTYPES.ERROR_SPACE_NAME_CONFLICT);  // absolute citing space.                                
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
    
}
