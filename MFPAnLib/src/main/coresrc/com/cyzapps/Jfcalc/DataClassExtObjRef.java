// MFP project, DataClassExtObjRef.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;

/**
 * This class caters reference of an external object, and of course null.
 * Note that the reference is immutable. Also, an external object can be a
 * primitive data type like integer, double, string, etc. However, we do
 * not allow it to be converted to DataClassSingleNum, DataClassComplex or
 * DataClassString etc by convertDatum2NewType function because the
 * convertDatum2NewType function is called by MFP implicitly. We only allow
 * explicit conversion between external object and internal data type to
 * avoid unexpected bug.
 * @author tony
 *
 */
public final class DataClassExtObjRef extends DataClass {
    private static final long serialVersionUID = 1L;
	private DATATYPES menumDataType = DATATYPES.DATUM_NULL;
    // this is for java object reference. If data type is DATUM_NULL, mobj must be null.
    // However, if mobj is null, data type is not necessarily DATUM_NULL.
    private transient Object mobj = null; // if mobj not serializable, an exception will be thrown. so have to be transient.
    
    @Override
    public String getTypeName() {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return "null";
        } else if (DCHelper.isDataClassType(this, DATATYPES.DATUM_REF_EXTOBJ)) {
            return "alien";
        } else {
            return "alien";
        }
    }
    
    @Override
    public String getTypeFullName() {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return "::mfp::lang::null";
        } else if (DCHelper.isDataClassType(this, DATATYPES.DATUM_REF_EXTOBJ)) {
            return "::mfp::lang::alien";
        } else {
            return "::mfp::lang::alien";
        }
    }
    
    public DataClassExtObjRef()   {
        super();
    }    

    public DataClassExtObjRef(Object obj) throws JFCALCExpErrException    {
        super();
        setDataClassExtObjRef(obj);
    }

    private void setDataClassExtObjRef(Object obj) throws JFCALCExpErrException    {
        menumDataType = DATATYPES.DATUM_REF_EXTOBJ;
        // do not check if obj is null because this can happen.
        // This is different from DataClassString where null string is not allowed.
        mobj = obj;
        validateDataClass();
    }
    
    @Override
    public void validateDataClass_Step1() throws JFCALCExpErrException    {
        super.validateDataClass_Step1();
        if (isNull())    {
            mobj = null;
        }
    }
    
    @Override
    public DATATYPES getDataClassType()    {
        return menumDataType;
    }

    public Object getExternalObject() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        }
        return mobj;
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
            // this must be an external object reference, and destination is not null.
            if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_REF_EXTOBJ)) {
                // convert to an external object reference, we just copy itself even if it is an extended type.
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
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_EXTOBJ_REFERENCE);
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
            if (datum instanceof DataClassExtObjRef) {
                return getExternalObject() == ((DataClassExtObjRef) datum).getExternalObject();
            }
        }
        return false;
    }
    
    public static int compareObjs(Object obj1, Object obj2) {
    	if (obj1 == obj2) {
    		return 0;
    	} else if (obj1 == null) {
    		// object2 must not be null
    		return -1;
    	} else if (obj2 == null) {
    		// object1 must not be null
    		return 1;
    	} else {
    		// both objects are not null
    		if (obj1.hashCode() > obj2.hashCode()) {
    			return 1;
    		} else if (obj1.hashCode() < obj2.hashCode()) {
    			return -1;
    		} else {
    			return 0;
    		}
    	}
    }
    /**
     * copy itself (light copy) and return a new reference.
     * To enable extended type compatibility, use
     * new DataClassExtObjRef(getExternalObject());
     * instead of
     * new DataClassExtObjRef(mobj);
     * @return 
     * @throws com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException
     */
    @Override
    public DataClass copySelf() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return new DataClassExtObjRef();
        } else {
            DataClassExtObjRef datumReturn = new DataClassExtObjRef(getExternalObject());
            return datumReturn;
        }
    }
    
    /**
     * copy itself (deep copy) and return a new reference.
     * To enable extended type compatibility, use
     * new DataClassExtObjRef(getExternalObject());
     * instead of
     * new DataClassExtObjRef(mobj);
     * @return 
     * @throws com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException 
     */
    @Override
    public DataClass cloneSelf() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return new DataClassExtObjRef();
        } else {
            DataClassExtObjRef datumReturn = new DataClassExtObjRef(getExternalObject());
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
        } else if (mobj == null) {
            strOutput = "external_null";
        } else {
        	strOutput = mobj.toString();
        }
        return strOutput;
    }
    
    @Override
    public int getHashCode() throws JFCALCExpErrException {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return 0;
        }
        return mobj.hashCode();
    }
}

