// MFP project, DataClassFuncRef.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;

/**
 * This class caters reference of function, and of course null.
 * Note that the reference is immutable.
 * @author tony
 *
 */
public final class DataClassFuncRef extends DataClass {
	private static final long serialVersionUID = 1L;
	private DATATYPES menumDataType = DATATYPES.DATUM_NULL;   
    private String mstrFunctionName = "";    // this is for ref_func
    
    @Override
    public String getTypeName() {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return "null";
        } else if (DCHelper.isDataClassType(this, DATATYPES.DATUM_REF_FUNC)) {
            return "function";
        } else {
            return "function";
        }
    }
    
    @Override
    public String getTypeFullName() {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return "::mfp::lang::null";
        } else if (DCHelper.isDataClassType(this, DATATYPES.DATUM_REF_FUNC)) {
            return "::mfp::lang::function";
        } else {
            return "::mfp::lang::function";
        }
    }
        
    public DataClassFuncRef()   {
        super();
    }    

    public DataClassFuncRef(String str) throws JFCALCExpErrException    {
        super();
        setDataClassFuncRef(str);
    }

    private void setDataClassFuncRef(String strFuncName) throws JFCALCExpErrException    {
      menumDataType = DATATYPES.DATUM_REF_FUNC;
      mstrFunctionName = strFuncName;
      validateDataClass();
    }
    
    @Override
    public void validateDataClass_Step1() throws JFCALCExpErrException    {
        super.validateDataClass_Step1();
        if (isNull())    {
            mstrFunctionName = "";
        } else if (mstrFunctionName == null) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        }
    }
    
    @Override
    public DATATYPES getDataClassType()    {
        return menumDataType;
    }

    public String getFunctionName() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        }
        return mstrFunctionName;
    }
    
    @Override
    public DataClass convertData2NewType(DATATYPES enumNewDataType) throws JFCALCExpErrException
    {
        if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_BASE_OBJECT)) {
            return copySelf();    // if change to a base data type, we just return a light copy of itself.
        } else if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_NULL)) {
            if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
                return copySelf();
            } else {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_ANY_OTHER_DATATYPE_TO_NONEXIST);
            }
        } else if (!DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            // this must be a function reference, and destination is not null.
            if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_REF_FUNC)) {
                // convert to a function reference, we just copy itself even if it is an extended type.
                return copySelf();
            } else if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_ABSTRACT_EXPR)) {
                // change to an aexpr.
                AbstractExpr aexpr = AEInvalid.AEINVALID;
                try {
                    aexpr = new AEConst(copySelf());    //light copy or deep copy?, think about it. TODO
                } catch (Exception e) {
                    // will not arrive here.
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE);
                }
                return new DataClassAExpr(aexpr);
            } else {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_FUNCTION_REFERENCE);
            }
        } else {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE);
        }
    }    

    @Override
    public boolean isEqual(DataClass datum) throws JFCALCExpErrException
    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            if (DCHelper.isDataClassType(datum, DATATYPES.DATUM_NULL))    {
                return true;
            }
        } else if (!DCHelper.isDataClassType(datum, DATATYPES.DATUM_NULL))   {
            if (datum instanceof DataClassFuncRef) {
                return getFunctionName().equals(((DataClassFuncRef) datum).getFunctionName());
            }
        }
        return false;
    }
    
    /**
     * copy itself (light copy) and return a new reference.
     * To enable extended type compatibility, use
     * new DataClassFuncRef(getFunctionName());
     * instead of
     * new DataClassFuncRef(mstrFuncName);
     */
    @Override
    public DataClass copySelf() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return new DataClassFuncRef();
        } else {
            DataClassFuncRef datumReturn = new DataClassFuncRef(getFunctionName());
            return datumReturn;
        }
    }
    
    /**
     * copy itself (deep copy) and return a new reference.
     * To enable extended type compatibility, use
     * new DataClassFuncRef(getFunctionName());
     * instead of
     * new DataClassFuncRef(mstrFuncName);
     */
    @Override
    public DataClass cloneSelf() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return new DataClassFuncRef();
        } else {
            DataClassFuncRef datumReturn = new DataClassFuncRef(getFunctionName());
            return datumReturn;
        }
    }
    
    @Override
    public String toString()    {
        String strReturn = "";
        try {
            strReturn = output();
        } catch (JFCALCExpErrException e) {
            strReturn = e.toString();
            e.printStackTrace();
        }
        return strReturn;
    }
    
    @Override
    public String output() throws JFCALCExpErrException    {
        String strOutput = "";
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            strOutput = "NULL";
        } else {
            strOutput = String.format("Function Name: %s", getFunctionName());
        }
        return strOutput;
    }
    
    @Override
    public int getHashCode() throws JFCALCExpErrException {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return 0;
        }
        return mstrFunctionName.hashCode();
    }
}

