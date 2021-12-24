// MFP project, DataClass.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import java.io.Serializable;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Oomfp.MFPClassDefinition;
import com.cyzapps.Oomfp.MFPClassInstance;

/**
 * This DataClass will never be used. If we declare an ::mfp::lang::object, we
 * use DataClassClass.
 * @author youxi
 */
public abstract class DataClass implements Serializable
{        
	private static final long serialVersionUID = 1L;

	public abstract String getTypeName();    // this function cannot be static because it will be inherited by sub-classes.
    
    public abstract String getTypeFullName();    // this function cannot be static because it will be inherited by sub-classes.
    
    /**
     * This constructor is only visible to itself and its children.
     * In other words, out of classes of DataClass and its children,
     * statement like new DataClass() is wrong.
     */
    protected DataClass()    {
    }
    
    public void validateDataClass_Step1() throws JFCALCExpErrException {
    }
    
    public void validateDataClass_Step2()    {
    }
    
    public void validateDataClass() throws JFCALCExpErrException {
        validateDataClass_Step1();
        validateDataClass_Step2();
    }
    
    public DATATYPES getDataClassType() {
        return DATATYPES.DATUM_BASE_OBJECT;
    }
    
    public boolean isNull() {
        return DCHelper.isDataClassType(getDataClassType(), DATATYPES.DATUM_NULL);
    }
    
    public boolean isBaseObj() {
        return DCHelper.isBaseObj(this);
    }
    
    public boolean isClassInstance() {
        return DCHelper.isClassInstance(this);
    }
    
    public boolean isPrimitiveOrArray() {
        return DCHelper.isPrimitiveOrArray(this);
    }
    
    public DataClass getThisOrNull() {
        return isNull()?new DataClassNull():this;
    }
    // convert data type and return a dataclass with new type. return a light copy of src data,
    // if convert to a object, it just returns a light copy of itself.
    public abstract DataClass convertData2NewType(DATATYPES enumNewDataType) throws JFCALCExpErrException;

    public int[] recalcDataArraySize() throws JFCALCExpErrException    {
        return new int[0];    // any type which is not data array returns news int[0]
    }

    public abstract boolean isEqual(DataClass datum) throws JFCALCExpErrException;
    
    // this function compare two dataclass bit by bit. Note that it is only
    // used in suspend_until_cond function, which is called when a variable is
    // updated from a different call object. Because comparison between old
    // value and new value is NOT carried out in a call thread, class definition
    // and stack is invisible. As such, we cannot use user defined __equal__
    // function. We have to compare bit by bit.
    public boolean isBitEqual(DataClass datum) throws JFCALCExpErrException {
        return isEqual(datum);
    }
    
    public abstract int getHashCode() throws JFCALCExpErrException;
    
    // copy itself (light copy).
    public abstract DataClass copySelf() throws JFCALCExpErrException;
    
    // clone itself (deep copy).
    public abstract DataClass cloneSelf() throws JFCALCExpErrException;
    
    public abstract String output() throws JFCALCExpErrException;
}

