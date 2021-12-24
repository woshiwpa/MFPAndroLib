// MFP project, DataClassAExpr.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;

/**
 * This class caters reference of abstract expr, and of course null.
 * Note that the reference itself is immutable. And AbstractExpr in most
 * cases is immutable. The exception is AbstractExpr is an AConst and it
 * refers to a data class array. The element of the data class array may
 * change so that AConst is not immutable and the DataClassAExpr is not
 * immutable.
 * @author tony
 *
 */
public final class DataClassAExpr extends DataClass {
	private static final long serialVersionUID = 1L;
	private DATATYPES menumDataType = DATATYPES.DATUM_NULL;   
    private AbstractExpr maexpr = AEInvalid.AEINVALID;  // this is for expression type data class.

    @Override
    public String getTypeName() {
        if (DCHelper.isDataClassType(getDataClassType(), DATATYPES.DATUM_NULL)) {
            return "null";
        } else if (DCHelper.isDataClassType(getDataClassType(), DATATYPES.DATUM_ABSTRACT_EXPR)) {
            return "expression";
        } else {
            return "expression";
        }
    }
    
    @Override
    public String getTypeFullName() {
        if (DCHelper.isDataClassType(getDataClassType(), DATATYPES.DATUM_NULL)) {
            return "::mfp::lang::null";
        } else if (DCHelper.isDataClassType(getDataClassType(), DATATYPES.DATUM_ABSTRACT_EXPR)) {
            return "::mfp::lang::expression";
        } else {
            return "::mfp::lang::expression";
        }
    }
        
    public DataClassAExpr()   {
        super();
    }    

    public DataClassAExpr(AbstractExpr aexpr) throws JFCALCExpErrException {
        super();
        setAExpr(aexpr);
    }

    private void setAExpr(AbstractExpr aexpr) throws JFCALCExpErrException {
        menumDataType = DATATYPES.DATUM_ABSTRACT_EXPR;
        maexpr = aexpr;
        validateDataClass();    
    }

    @Override
    public void validateDataClass_Step1() throws JFCALCExpErrException    {
        super.validateDataClass_Step1();
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            maexpr = AEInvalid.AEINVALID;
        }
    }
    
    @Override
    public DATATYPES getDataClassType()    {
        return menumDataType;
    }
    
    public AbstractExpr getAExpr() throws JFCALCExpErrException {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        }
        return maexpr;
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
            if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_ABSTRACT_EXPR)) {
                return copySelf();
            } else if (getAExpr() instanceof AEConst) {    // this is a constant.
                DataClass datum = ((AEConst)getAExpr()).getDataClassRef().convertData2NewType(enumNewDataType);
                return datum;
            } else {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_ABSTRACT_EXPR);
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
            if (datum instanceof DataClassAExpr) {
                return getAExpr().isEqual(((DataClassAExpr)datum).getAExpr(), null);    // unconditional compare
            }
        }
        return false;
    }

    @Override
    public boolean isBitEqual(DataClass datum) throws JFCALCExpErrException
    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            if (DCHelper.isDataClassType(datum, DATATYPES.DATUM_NULL))    {
                return true;
            }
        } else if (!DCHelper.isDataClassType(datum, DATATYPES.DATUM_NULL))   {
            if (datum instanceof DataClassAExpr) {
                return getAExpr().isBitEqual(((DataClassAExpr)datum).getAExpr(), null);    // unconditional compare
            }
        }
        return false;
    }
    
    /**
     * copy itself (light copy) and return a new reference.
     * To enable extended type compatibility, use
     * new DataClassAExpr(getAExpr());
     * instead of
     * new DataClassAExpr(mAExpr);
     */
    @Override
    public DataClass copySelf() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return new DataClassAExpr();
        } else {
            DataClassAExpr datumReturn = new DataClassAExpr(getAExpr());
            return datumReturn;
        }
    }
    
    /**
     * copy itself (deep copy) and return a new reference.
     * To enable extended type compatibility, use
     * new DataClassAExpr(getAExpr());
     * instead of
     * new DataClassAExpr(mAExpr);
     */
    @Override
    public DataClass cloneSelf() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return new DataClassAExpr();
        } else {
            // do not clone maexpr because maexpr is immutable (even for AEConst,
            // its dataclass can be changed but its reference cannot be changed).
            DataClassAExpr datumReturn = new DataClassAExpr(getAExpr());
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
            try {
                strOutput = maexpr.output();
            } catch (JSmartMathErrException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_ABSTRACT_EXPR);
            }
        }
        return strOutput;
    }
    
    @Override
    public int getHashCode() throws JFCALCExpErrException {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return 0;
        }
        return maexpr.getHashCode();
    }
}
