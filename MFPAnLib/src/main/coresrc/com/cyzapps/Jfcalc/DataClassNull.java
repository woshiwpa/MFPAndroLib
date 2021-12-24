// MFP project, DataClassNull.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;

public final class DataClassNull extends DataClass {
	private static final long serialVersionUID = 1L;

	@Override
    public String getTypeName() {
        return "null";
    }
    
    @Override
    public String getTypeFullName() {
        return "::mfp::lang::null";
    }
    
    public DataClassNull()   {
        super();
    }    
    
    public boolean isNumericalData(boolean bLookOnNullAsZero) {
        return bLookOnNullAsZero;
    }
    
    @Override
    public DATATYPES getDataClassType()    {
        return DATATYPES.DATUM_NULL;
    }

    @Override
    public DataClass convertData2NewType(DATATYPES enumNewDataType) throws JFCALCExpErrException
    {
        if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_BASE_OBJECT)) {
            return copySelf();    // if change to a base data type, we just return a light copy of itself.
        } else if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_NULL)) {
            return copySelf();
        } else  {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE);
        }
    }

    @Override
    public boolean isEqual(DataClass datum) throws JFCALCExpErrException
    {
        if (DCHelper.isDataClassType(datum, DATATYPES.DATUM_NULL))    {
            return true;
        } else {
            return false;
        }
    }

    public boolean isZeros(boolean bExplicitNullIsZero) throws JFCALCExpErrException    {
        return bExplicitNullIsZero;
    }

    /**
     * copy itself (light copy) and return a new reference.
     */
    @Override
    public DataClass copySelf() throws JFCALCExpErrException    {
        return new DataClassNull();
    }
    
    /**
     * copy itself (deep copy) and return a new reference.
     */
    @Override
    public DataClass cloneSelf() throws JFCALCExpErrException    {
        return new DataClassNull();
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
        String strOutput = "NULL";
        return strOutput;
    }
    
    @Override
    public int getHashCode() throws JFCALCExpErrException {
        return 0;
    }
}


