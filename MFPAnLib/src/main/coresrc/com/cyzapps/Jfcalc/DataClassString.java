// MFP project, DataClassString.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;

/**
 * This class caters reference of string, and of course null.
 * Note that the reference itself is immutable. And string is also immutable.
 * @author tony
 *
 */
public final class DataClassString extends DataClass {
	private static final long serialVersionUID = 1L;
	private DATATYPES menumDataType = DATATYPES.DATUM_NULL;   
    private String mstrValue = "";    // this is for string type
    
    @Override
    public String getTypeName() {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return "null";
        } else if (DCHelper.isDataClassType(this, DATATYPES.DATUM_STRING)) {
            return "string";
        } else {
            return "string";    // should not be here
        }
    }
    
    @Override
    public String getTypeFullName() {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return "::mfp::lang::null";
        } else if (DCHelper.isDataClassType(this, DATATYPES.DATUM_STRING)) {
            return "::mfp::lang::string";
        } else {
            return "::mfp::lang::string";    // should not be here
        }
    }
    
    public DataClassString()   {
        super();
    }    

    public DataClassString(String str) throws JFCALCExpErrException    {
        super();
        setStringValue(str);
    }

    /**
     * This private function is for internal construction use only.
     * @param str : should not be null
     * @throws JFCALCExpErrException
     */
    private void setStringValue(String str) throws JFCALCExpErrException    {
        menumDataType = DATATYPES.DATUM_STRING;
        mstrValue = str;
        validateDataClass();    
    }
    
    @Override
    public void validateDataClass_Step1() throws JFCALCExpErrException    {
        super.validateDataClass_Step1();
        if (isNull())    {
            mstrValue = "";
        } else if (mstrValue == null)  {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        }
    }
    
    @Override
    public DATATYPES getDataClassType()    {
        return menumDataType;
    }
    
    public String getStringValue() throws JFCALCExpErrException {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        } else {
            return mstrValue;
        }
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
            if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_STRING)) {
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
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_STRING);
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
            if (datum instanceof DataClassString) {
                return getStringValue().equals(((DataClassString) datum).getStringValue());
            }
        }
        return false;
    }
    
    /**
     * copy itself (light copy) and return a new reference.
     * To enable extended type compatibility, use
     * new DataClassString(getStringValue());
     * instead of
     * new DataClassString(mstrValue);
     */
    @Override
    public DataClass copySelf() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return new DataClassString();
        } else {
            DataClassString datumReturn = new DataClassString(getStringValue());
            return datumReturn;
        }
    }
    
    /**
     * copy itself (deep copy) and return a new reference.
     * To enable extended type compatibility, use
     * new DataClassString(getStringValue());
     * instead of
     * new DataClassString(mstrValue);
     */
    @Override
    public DataClass cloneSelf() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return new DataClassString();
        } else {
            DataClassString datumReturn = new DataClassString(getStringValue());
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
            String strEscaped = StringEscapeUtils.escapeJava(getStringValue());
            String strTranslated = new UnicodeUnescaper().translate(strEscaped);
            strOutput = "\"" + strTranslated + "\"";
        }
        return strOutput;
    }
    
    @Override
    public int getHashCode() throws JFCALCExpErrException {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return 0;
        }
        return mstrValue.hashCode();
    }
}

