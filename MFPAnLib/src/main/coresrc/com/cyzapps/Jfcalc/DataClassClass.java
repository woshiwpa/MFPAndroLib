/*
 * MFP project, DataClassClass.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.ErrorProcessor;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.FunctionEntry;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.ScriptAnalyzer;
import com.cyzapps.Jmfp.ScriptAnalyzer.FuncRetException;
import com.cyzapps.Jmfp.ScriptAnalyzer.InFunctionCSManager;
import com.cyzapps.Jmfp.ScriptAnalyzer.ScriptStatementException;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.SMErrProcessor;
import com.cyzapps.Oomfp.MFPClassDefinition;
import com.cyzapps.Oomfp.MFPClassInstance;
import com.cyzapps.Oomfp.MFPDataTypeDef;
import com.cyzapps.Oomfp.MemberFunction;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class caters reference of an MFP class, and of course null.
 * Note that the reference itself is immutable. However, class member values may change.
 * @author Tony
 */
public class DataClassClass extends DataClass {
	private static final long serialVersionUID = 1L;
	private DCHelper.DATATYPES menumDataType = DCHelper.DATATYPES.DATUM_NULL;   

    protected MFPClassInstance minstance = new MFPClassInstance();  // instance of MFPClassDefinition.OBJECT

   @Override
    public String getTypeName() {
        if (DCHelper.isDataClassType(getDataClassType(), DCHelper.DATATYPES.DATUM_NULL)) {
            return "null";
        } else {
            String fullTypeName = minstance.classDefFullNameWithCS;
            return fullTypeName.substring(fullTypeName.lastIndexOf(":"));
        }
    }
    
    @Override
    public String getTypeFullName() {
        if (DCHelper.isDataClassType(getDataClassType(), DCHelper.DATATYPES.DATUM_NULL)) {
            return "::mfp::lang::null";
        } else {
            return minstance.classDefFullNameWithCS;
        }
    }
    
    public DataClassClass()   {
        super();
    }    

    public DataClassClass(MFPClassInstance classInstance) throws ErrProcessor.JFCALCExpErrException {
        super();
        setClassInstance(classInstance);
    }

    private void setClassInstance(MFPClassInstance classInstance) throws ErrProcessor.JFCALCExpErrException {
        minstance = classInstance;
        menumDataType = DCHelper.DATATYPES.DATUM_CLASS_INSTANCE;
        validateDataClass();    
    }

    @Override
    public void validateDataClass_Step1() throws ErrProcessor.JFCALCExpErrException    {
        super.validateDataClass_Step1();
        if (DCHelper.isDataClassType(this, DCHelper.DATATYPES.DATUM_NULL))    {
            try {
                minstance = new MFPClassInstance(MFPClassDefinition.OBJECT);
            } catch (ErrorProcessor.JMFPCompErrException ex) {
                Logger.getLogger(DataClassClass.class.getName()).log(Level.SEVERE, null, ex);
                // will never be here because it is initializing an OBJECT.
            }
        }
    }
    
    @Override
    public DCHelper.DATATYPES getDataClassType()    {
        return menumDataType;
    }
    
    public MFPClassInstance getClassInstance() throws ErrProcessor.JFCALCExpErrException {
        if (DCHelper.isDataClassType(this, DCHelper.DATATYPES.DATUM_NULL)) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        }
        return minstance;
    }

    @Override
    public DataClass convertData2NewType(DCHelper.DATATYPES enumNewDataType) throws ErrProcessor.JFCALCExpErrException
    {
        if (DCHelper.isDataClassType(enumNewDataType, DCHelper.DATATYPES.DATUM_BASE_OBJECT)) {
            return copySelf();    // if change to a base data type, we just return a light copy of itself.
        } else if (DCHelper.isDataClassType(enumNewDataType, DCHelper.DATATYPES.DATUM_NULL)) {
            if (DCHelper.isDataClassType(this, DCHelper.DATATYPES.DATUM_NULL)) {
                return copySelf();
            } else {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CAN_NOT_CHANGE_ANY_OTHER_DATATYPE_TO_NONEXIST);
            }
        } else if (!DCHelper.isDataClassType(this, DCHelper.DATATYPES.DATUM_NULL)) {
            if (DCHelper.isDataClassType(this.menumDataType, enumNewDataType)) {
                return copySelf();
            } else if (DCHelper.isDataClassType(enumNewDataType, DCHelper.DATATYPES.DATUM_ABSTRACT_EXPR)) {
                // change to an aexpr.
                AbstractExpr aexpr = AEInvalid.AEINVALID;
                try {
                    aexpr = new AEConst(copySelf());    //light copy or deep copy?, think about it. TODO
                } catch (Exception e) {
                    // will not arrive here.
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE);
                }
                return new DataClassAExpr(aexpr);
            } else {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_CLASS_INSTANCE);
            }
        } else {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE);
        }
    }

    @Override
    public boolean isEqual(DataClass datum) throws ErrProcessor.JFCALCExpErrException
    {
        if (this == datum) {
            return true;
        } else if (DCHelper.isDataClassType(this, DCHelper.DATATYPES.DATUM_NULL))    {
            // class instance's member function doesn't support self being null
            // so that we have to use a standalone if branch to do it here.
            if (DCHelper.isDataClassType(datum, DCHelper.DATATYPES.DATUM_NULL))    {
                return true;
            }
        } else if (getClassInstance().getClassDefinition() == null) {
            // this is possible when we deserialize a object whose class definition doesn't exist
            // in the destination system.
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_MFP_CLASS_TYPE);
        } else  {
            MemberFunction mf = getClassInstance().getClassDefinition().mfEquals;
            ProgContext progContext = new ProgContext();
            progContext.mdynamicProgContext.mlVarNameSpaces = new LinkedList<LinkedList<Variable>>();
            DataClass ret;
            if (mf instanceof FunctionEntry) {
                FunctionEntry fe = ((FunctionEntry)mf);
                LinkedList<Variable> lParams = new LinkedList<Variable>();
                Variable var = new Variable(fe.getStatementFunction().m_strParams[0], this);
                lParams.add(var);
                var = new Variable(fe.getStatementFunction().m_strParams[1], datum);
                lParams.add(var);
                try {
                    // a function should not be able to read namespaces outside.
                    progContext.mstaticProgContext.setCallingFunc(fe.getStatementFunction());
                    InFunctionCSManager inFuncCSMgr = new InFunctionCSManager(progContext.mstaticProgContext);
                    ScriptAnalyzer sa = new ScriptAnalyzer();
                    sa.analyzeBlock(fe.getStatementLines(), fe.getStartStatementPos(), lParams, inFuncCSMgr, progContext);
                } catch(FuncRetException e)    {
                    if (null == e.m_datumReturn) {
                        throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA,
                            fe.getAbsNameWithCS(), e);
                    }
                    return DCHelper.lightCvtOrRetDCMFPBool(e.m_datumReturn).getDataValue().booleanValue();
                } catch(ScriptStatementException e)    {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                            fe.getAbsNameWithCS(), e);
                } catch(JMFPCompErrException e)    {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                            fe.getAbsNameWithCS(), e);
                } catch(Exception e)    {
                    // unexcepted exception
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                            fe.getAbsNameWithCS());   // unexcepted exception does not append to lower level
                }
            } else {
                LinkedList<DataClass> listParamValues = new LinkedList<DataClass>();
                listParamValues.add(datum);
                listParamValues.add(this);
                LinkedList<String> listParamRawInputs = new LinkedList<String>();
                listParamRawInputs.add(datum.toString());
                listParamRawInputs.add("self");        // doesn't matter to use self here as it calls a public function.
                try {
                    ret = ((BuiltInFunctionLib.BaseBuiltInFunction)mf).callAction(listParamValues, listParamRawInputs, progContext);
                    return DCHelper.lightCvtOrRetDCMFPBool(ret).getDataValue().booleanValue();
                } catch (InterruptedException ex) {
                    // will never be here.
                    Logger.getLogger(DataClassClass.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }
    
    // this function compare two dataclass bit by bit. Note that it is only
    // used in suspend_until_cond function, which is called when a variable is
    // updated from a different call object. Because comparison between old
    // value and new value is NOT carried out in a call thread, class definition
    // and stack is invisible. As such, we cannot use user defined __equal__
    // function. We have to compare bit by bit.
    @Override
    public boolean isBitEqual(DataClass datum) throws ErrProcessor.JFCALCExpErrException {
        if (this == datum) {
            return true;
        } else if (DCHelper.isDataClassType(this, DCHelper.DATATYPES.DATUM_NULL))    {
            // class instance's member function doesn't support self being null
            // so that we have to use a standalone if branch to do it here.
            if (DCHelper.isDataClassType(datum, DCHelper.DATATYPES.DATUM_NULL))    {
                return true;
            }
        } else if (!DCHelper.isDataClassType(datum, DCHelper.DATATYPES.DATUM_NULL))    {
            DataClassClass datumCls = DCHelper.lightCvtOrRetDCClass(datum);
            return minstance.isBitEqual(datumCls.minstance);
        }
        return false;
    }
    
    /**
     * copy itself (light copy) and return a new reference.
     */
    @Override
    public DataClass copySelf() throws ErrProcessor.JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DCHelper.DATATYPES.DATUM_NULL)) {
            // class instance's member function doesn't support self being null
            // so that we have to use a standalone if branch to do it here.
            return new DataClassNull();
        } else  {
            return new DataClassClass(minstance);   // light copy means we shouldn't copy minstance
        }
    }
    
    /**
     * copy itself (deep copy) and return a new reference.
     */
    @Override
    public DataClass cloneSelf() throws ErrProcessor.JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DCHelper.DATATYPES.DATUM_NULL)) {
            // class instance's member function doesn't support self being null
            // so that we have to use a standalone if branch to do it here.
            return new DataClassClass();
        } else if (getClassInstance().getClassDefinition() == null) {
            // this is possible when we deserialize a object whose class definition doesn't exist
            // in the destination system.
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_MFP_CLASS_TYPE);
        } else  {
            MemberFunction mf = getClassInstance().getClassDefinition().mfDeepCopy;
            ProgContext progContext = new ProgContext();
            progContext.mdynamicProgContext.mlVarNameSpaces = new LinkedList<LinkedList<Variable>>();
            DataClass ret = null;
            if (mf instanceof FunctionEntry) {
                FunctionEntry fe = ((FunctionEntry)mf);
                LinkedList<Variable> lParams = new LinkedList<Variable>();
                Variable var = new Variable(fe.getStatementFunction().m_strParams[0], this);
                lParams.add(var);
                try {
                    // a function should not be able to read namespaces outside.
                    progContext.mstaticProgContext.setCallingFunc(fe.getStatementFunction());
                    InFunctionCSManager inFuncCSMgr = new InFunctionCSManager(progContext.mstaticProgContext);
                    ScriptAnalyzer sa = new ScriptAnalyzer();
                    sa.analyzeBlock(fe.getStatementLines(), fe.getStartStatementPos(), lParams, inFuncCSMgr, progContext);
                } catch(FuncRetException e)    {
                    return e.m_datumReturn;
                } catch(ScriptStatementException e)    {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                            fe.getAbsNameWithCS(), e);
                } catch(JMFPCompErrException e)    {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                            fe.getAbsNameWithCS(), e);
                } catch(Exception e)    {
                    // unexcepted exception
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                            fe.getAbsNameWithCS());   // unexcepted exception does not append to lower level
                }
            } else {
                LinkedList<DataClass> listParamValues = new LinkedList<DataClass>();
                listParamValues.add(this);
                LinkedList<String> listParamRawInputs = new LinkedList<String>();
                listParamRawInputs.add("self");        
                try {
                    ret = ((BuiltInFunctionLib.BaseBuiltInFunction)mf).callAction(listParamValues, listParamRawInputs, progContext);
                } catch (InterruptedException ex) {
                    // will never be here.
                    Logger.getLogger(DataClassClass.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return ret;
        }
    }
    
    @Override
    public String toString()    {
        String strReturn = "";
        try {
            strReturn = output();
        } catch (ErrProcessor.JFCALCExpErrException e) {
            strReturn = e.toString();
            e.printStackTrace();
        }
        return strReturn;
    }
    
    @Override
    public String output() throws ErrProcessor.JFCALCExpErrException    {
        String strOutput = "";
        if (DCHelper.isDataClassType(this, DCHelper.DATATYPES.DATUM_NULL)) {
            // class instance's member function doesn't support self being null
            // so that we have to use a standalone if branch to do it here.
            strOutput = "NULL";
        } else if (getClassInstance().getClassDefinition() == null) {
            // this is possible when we deserialize a object whose class definition doesn't exist
            // in the destination system.
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_MFP_CLASS_TYPE);
        } else  {
            MemberFunction mf = getClassInstance().getClassDefinition().mfToString;
            ProgContext progContext = new ProgContext();
            progContext.mdynamicProgContext.mlVarNameSpaces = new LinkedList<LinkedList<Variable>>();
            if (mf instanceof FunctionEntry) {
                FunctionEntry fe = ((FunctionEntry)mf);
                LinkedList<Variable> lParams = new LinkedList<Variable>();
                Variable var = new Variable(fe.getStatementFunction().m_strParams[0], this);
                lParams.add(var);
                try {
                    // a function should not be able to read namespaces outside.
                    progContext.mstaticProgContext.setCallingFunc(fe.getStatementFunction());
                    InFunctionCSManager inFuncCSMgr = new InFunctionCSManager(progContext.mstaticProgContext);
                    ScriptAnalyzer sa = new ScriptAnalyzer();
                    sa.analyzeBlock(fe.getStatementLines(), fe.getStartStatementPos(), lParams, inFuncCSMgr, progContext);
                } catch(FuncRetException e)    {
                    if (null == e.m_datumReturn) {
                        throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA,
                            fe.getAbsNameWithCS(), e);
                    }
                    strOutput = DCHelper.lightCvtOrRetDCString(e.m_datumReturn).getStringValue();
                } catch(ScriptStatementException e)    {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                            fe.getAbsNameWithCS(), e);
                } catch(JMFPCompErrException e)    {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                            fe.getAbsNameWithCS(), e);
                } catch(Exception e)    {
                    // unexcepted exception
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                            fe.getAbsNameWithCS());   // unexcepted exception does not append to lower level
                }
            } else {
                LinkedList<DataClass> listParamValues = new LinkedList<DataClass>();
                listParamValues.add(this);
                LinkedList<String> listParamRawInputs = new LinkedList<String>();
                listParamRawInputs.add("self");        
                try {
                    DataClass ret = ((BuiltInFunctionLib.BaseBuiltInFunction)mf).callAction(listParamValues, listParamRawInputs, progContext);
                    strOutput = DCHelper.lightCvtOrRetDCString(ret).getStringValue();
                } catch (InterruptedException ex) {
                    // will never be here.
                    Logger.getLogger(DataClassClass.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return strOutput;
    }
    
    @Override
    public int getHashCode() throws JFCALCExpErrException {
        if (DCHelper.isDataClassType(this, DCHelper.DATATYPES.DATUM_NULL)) {
            return 0;
        } else if (getClassInstance().getClassDefinition() == null) {
            // this is possible when we deserialize a object whose class definition doesn't exist
            // in the destination system.
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_MFP_CLASS_TYPE);
        }
        MemberFunction mf = getClassInstance().getClassDefinition().mfHash;
        ProgContext progContext = new ProgContext();
        progContext.mdynamicProgContext.mlVarNameSpaces = new LinkedList<LinkedList<Variable>>();
        DataClass ret;
        if (mf instanceof FunctionEntry) {
            FunctionEntry fe = ((FunctionEntry)mf);
            LinkedList<Variable> lParams = new LinkedList<Variable>();
            Variable var = new Variable(fe.getStatementFunction().m_strParams[0], this);
            lParams.add(var);
            try {
                // a function should not be able to read namespaces outside.
                progContext.mstaticProgContext.setCallingFunc(fe.getStatementFunction());
                InFunctionCSManager inFuncCSMgr = new InFunctionCSManager(progContext.mstaticProgContext);
                ScriptAnalyzer sa = new ScriptAnalyzer();
                sa.analyzeBlock(fe.getStatementLines(), fe.getStartStatementPos(), lParams, inFuncCSMgr, progContext);
                return 0;   // will never be here
            } catch(FuncRetException e)    {
                if (null == e.m_datumReturn) {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA,
                        fe.getAbsNameWithCS(), e);
                }
                return DCHelper.lightCvtOrRetDCMFPInt(e.m_datumReturn).getDataValue().intValue();
            } catch(ScriptStatementException e)    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                        fe.getAbsNameWithCS(), e);
            } catch(JMFPCompErrException e)    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                        fe.getAbsNameWithCS(), e);
            } catch(Exception e)    {
                // unexcepted exception
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                        fe.getAbsNameWithCS());   // unexcepted exception does not append to lower level
            }
        } else {
            LinkedList<DataClass> listParamValues = new LinkedList<DataClass>();
            listParamValues.add(this);
            LinkedList<String> listParamRawInputs = new LinkedList<String>();
            listParamRawInputs.add("self");        // doesn't matter to use self here as it calls a public function.
            try {
                ret = ((BuiltInFunctionLib.BaseBuiltInFunction)mf).callAction(listParamValues, listParamRawInputs, progContext);
                return DCHelper.lightCvtOrRetDCMFPInt(ret).getDataValue().intValue();
            } catch (InterruptedException ex) {
                // will never be here.
                Logger.getLogger(DataClassClass.class.getName()).log(Level.SEVERE, null, ex);
                return 0;
            }
        }
    }
}
