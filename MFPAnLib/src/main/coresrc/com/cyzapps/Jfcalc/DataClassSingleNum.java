// MFP project, DataClassSingleNum.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;

/**
 * This class caters reference of boolean, integer and double, and of course null.
 * Note that the reference is immutable.
 * A design standard is, for extended class type, it is treated as double if DATATYPE
 * is unknown.
 * @author tony
 *
 */
public final class DataClassSingleNum extends DataClass
{
	private static final long serialVersionUID = 1L;
	private DATATYPES menumDataType = DATATYPES.DATUM_NULL;   
    private MFPNumeric mmfpNumDataValue = DCHelper.THE_NULL_DATA_VALUE;   // this is for non-exist, double, integer and boolean, nan, inf
    
    @Override
    public String getTypeName() {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return "null";
        } else {
            return "numeric";
        }
    }
    
    @Override
    public String getTypeFullName() {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return "::mfp::lang::null";
        } else {
            return "::mfp::lang::numeric";    // should not be here
        }
    }
    
    public DataClassSingleNum()   {
        super();
    }    
    
    public DataClassSingleNum(DATATYPES enumDataType, MFPNumeric mfpNumDataValue) throws JFCALCExpErrException    {
        super();
        // initialize numerical data class
        if (!DCHelper.isDataClassType(enumDataType, /*DATATYPES.DATUM_NULL,*/ // DATUM_NULL shouldn't be initialized by this constructor.
                DATATYPES.DATUM_MFPBOOL, DATATYPES.DATUM_MFPINT, DATATYPES.DATUM_MFPDEC))   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
        }
        setDataValue(enumDataType, mfpNumDataValue);
    }
    
    /**
     * This function is only used in constructor.
     * @param enumDataType
     * @param mfpNumDataValue
     * @throws JFCALCExpErrException
     */
    private void setDataValue(DATATYPES enumDataType, MFPNumeric mfpNumDataValue) throws JFCALCExpErrException    {
        menumDataType = enumDataType;
        mmfpNumDataValue = (mfpNumDataValue == null)?MFPNumeric.ZERO:mfpNumDataValue;
        validateDataClass();
    }
    
    public boolean isSingleBoolean()    {
        try {
            if (!DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)
                && (getDataValue().isEqual(MFPNumeric.ONE)
                    || getDataValue().isEqual(MFPNumeric.ZERO))) {
                return true;
            }
        } catch (JFCALCExpErrException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean isSingleInteger()    {
        try {
            if (!DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)
                    && getDataValue().isActuallyInteger())    {
                return true;
            }
        } catch (JFCALCExpErrException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean isSingleDouble()    {
        if (!DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            return true;
        }
        return false;
    }
    public boolean isNumericalData(boolean bLookOnNullAsZero) {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return bLookOnNullAsZero;
        } else   {
            return true;
        }
    }
    
    @Override
    public void validateDataClass_Step1() throws JFCALCExpErrException    {
        super.validateDataClass_Step1();
    	switch(menumDataType) {
    	case DATUM_NULL:
    		mmfpNumDataValue = DCHelper.THE_NULL_DATA_VALUE;
    	case DATUM_MFPBOOL:
    		if (mmfpNumDataValue.isActuallyTrue())    {
                mmfpNumDataValue = MFPNumeric.TRUE;
            } else if (mmfpNumDataValue.isActuallyFalse())    {
                mmfpNumDataValue = MFPNumeric.FALSE;
            } else {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN);
            }
    		break;
    	case DATUM_MFPINT:
    		mmfpNumDataValue = mmfpNumDataValue.toIntOrNanInfMFPNum();
    		break;
    	default:
    		break;
    	}
    }
    
    @Override
    public DATATYPES getDataClassType()    {
        return menumDataType;
    }
    
    /**
     * This function will be overridden by sub class.
     * @return
     * @throws JFCALCExpErrException
     */
    public MFPNumeric getDataValue() throws JFCALCExpErrException    {
    	switch(menumDataType) {
    	case DATUM_NULL:
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
    	default:
            return mmfpNumDataValue;    // we have validated the class when initializing it, no need to do it again.
    	}
    }
    
    /**
     * Note that this function always return a new reference. Note that if there is a copy, use light copy.
     */
    @Override
    public DataClass convertData2NewType(DATATYPES enumNewDataType) throws JFCALCExpErrException
    {
        if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_BASE_OBJECT)) {
            return copySelf();    // if change to a base data type, we just return a light copy of itself.
        } else if (DCHelper.isDataClassType(this, enumNewDataType)) {    // src data type is the same as dest
            return copySelf();    // if change to a base data type, we just return a light copy of itself.
        } else if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_NULL)) {
            // source data type must be different from null, so throw exception.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_ANY_OTHER_DATATYPE_TO_NONEXIST);
        } else if (!DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            // it is boolean, integer or double.
            if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_MFPBOOL)) {
                try {
                    return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, getDataValue().toBoolMFPNum());
                } catch (ArithmeticException e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN);
                }
            } else if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_MFPINT)) {
                return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, getDataValue().toIntOrNanInfMFPNum());
            } else if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_MFPDEC)) {
                return new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, getDataValue().toDblOrNanInfMFPNum());
            } else if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_COMPLEX)) {
                return new DataClassComplex(getDataValue().toDblOrNanInfMFPNum(), MFPNumeric.ZERO);
            } else if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_REF_DATA)) {
                // light copy itself to array's first element.
                return new DataClassArray(new DataClass[] {copySelf()});
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
                // cannot convert to string or func reference.
                if (DCHelper.isDataClassType(this, DATATYPES.DATUM_MFPBOOL)) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_BOOLEAN);
                } else if (DCHelper.isDataClassType(this, DATATYPES.DATUM_MFPINT)) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_INTEGER);
                } else if (DCHelper.isDataClassType(this, DATATYPES.DATUM_MFPDEC)) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_DOUBLE);
                } else {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE);    // will not be here.
                }
            }
        } else {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE);
        }
    }    

    public boolean isEqual(DataClass datum, double errorScale) throws JFCALCExpErrException
    {
        if (errorScale < 0)    {
            errorScale = 0;    // ensure that error scale is always non-negative
        }
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            if (DCHelper.isDataClassType(datum, DATATYPES.DATUM_NULL))    {
                return true;
            }
        } else if (!DCHelper.isDataClassType(datum, DATATYPES.DATUM_NULL))   {
            // I am boolean, integer or double or an extended class type. datum's type should not be
            // limited to boolean, integer, double or complex either because it can also be a extended
            // class type.
            if (datum instanceof DataClassSingleNum
                    && MFPNumeric.isEqual(getDataValue(), ((DataClassSingleNum)datum).getDataValue(), errorScale))    {
                return true;
            } else if (datum instanceof DataClassComplex
                    && MFPNumeric.isEqual(((DataClassComplex)datum).getImage(), MFPNumeric.ZERO, errorScale)
                    && MFPNumeric.isEqual(((DataClassComplex)datum).getReal(), getDataValue(), errorScale))    {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean isEqual(DataClass datum) throws JFCALCExpErrException
    {
        return isEqual(datum, 1);
    }
    
    /**
     * This function identify if a data value is zero (false is zero)
     * or a data array is full-of-zero array
     * @param bExplicitNullIsZero : look on null as zero
     * @return true or false
     * @throws JFCALCExpErrException
     */
    public boolean isZeros(boolean bExplicitNullIsZero) throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            return bExplicitNullIsZero;
        } else    {
            DataClassSingleNum datum = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
            return isEqual(datum);
        }
    }
    
    /**
     * This function identify if a data value is I or 1 or [1].
     * @param bExplicitNullIsZero : look on null as zero
     * @return true or false
     * @throws JFCALCExpErrException
     */
    public boolean isEye(boolean bExplicitNullIsZero) throws JFCALCExpErrException    {
        DataClassSingleNum datumOne = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
        return isEqual(datumOne);
    }

    /**
     * copy itself (light copy) and return a new reference.
     */
    @Override
    public DataClass copySelf() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return new DataClassSingleNum();
        } else {
            DataClassSingleNum datumReturn = new DataClassSingleNum(menumDataType, mmfpNumDataValue);
            return datumReturn;
        }
    }
    
    /**
     * copy itself (deep copy) and return a new reference.
     */
    @Override
    public DataClass cloneSelf() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return new DataClassSingleNum();
        } else {
            DataClassSingleNum datumReturn = new DataClassSingleNum(menumDataType, mmfpNumDataValue);
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
            strOutput = getDataValue().toString();
        }
        return strOutput;
    }
    
    @Override
    public int getHashCode() throws JFCALCExpErrException {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return 0;
        }
        return mmfpNumDataValue.hashCode();
    }
}

