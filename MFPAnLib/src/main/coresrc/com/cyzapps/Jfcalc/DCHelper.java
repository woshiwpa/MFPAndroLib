// MFP project, DCHelper.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import java.io.Serializable;

import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;

public class DCHelper {
    /*     cursor position class. cursor means where we are currently analyzing
     *  This class is used to wrap an int position variable so that we can use
     *  reference parameter. */
    public static class CurPos 
    {
        public int m_nPos;
    }
    
    public static class DCUncopibleFeatures implements Serializable
    {
		private static final long serialVersionUID = 1L;
		public int mnValidatedCnt = 0;
    }

    public static enum DATATYPES
    {
        DATUM_BASE_OBJECT,    // base class of any other data types
        DATUM_NULL,
        DATUM_MFPDEC,   // nan and inf are not independent types because double can be nan or inf.
        DATUM_MFPINT,  // nan and inf are not independent types because int can be nan or inf.
        DATUM_MFPBOOL,  // boolean can be inf too.
        DATUM_STRING,    // a string
        DATUM_REF_DATA,    // a reference to a list of data
        DATUM_COMPLEX,    // complex number
        DATUM_REF_FUNC,    // a reference to a function
        DATUM_REF_EXTOBJ,	// a reference to an external object (a JAVA object).
        DATUM_ABSTRACT_EXPR,    // an abstract expr
        DATUM_CLASS_INSTANCE,    // user defined class
    }
    
    // remember this function is not visible out of package. It is different
    // from instanceof. This function can identify sub class from parent class
    // but instanceof cannot.
    protected static String getDataTypeClassName(DATATYPES enumDataType) {
        switch(enumDataType) {
        case DATUM_BASE_OBJECT:
            return "DataClass";
        case DATUM_NULL:
        	return "DataClassNull";
        case DATUM_MFPDEC:
        case DATUM_MFPINT:
        case DATUM_MFPBOOL:
        	return "DataClassSingleNum";
        case DATUM_STRING:
        	return "DataClassString";
        case DATUM_REF_DATA:
        	return "DataClassArray";
        case DATUM_COMPLEX:
        	return "DataClassComplex";
        case DATUM_REF_FUNC:
        	return "DataClassFuncRef";
        case DATUM_REF_EXTOBJ:
        	return "DataClassExtObjRef";
        case DATUM_ABSTRACT_EXPR:
            return "DataClassAExpr";
        default:
            return "";    // not supported yet.
        }
    }
        
    public static final MFPNumeric THE_MAX_RELATIVE_ERROR_OF_TRIGONOMETRIC_CALCULATION = new MFPNumeric("5.0e-24");
    
    public static final int MAX_REPEATING_VALIDATION_CNT_ALLOWED = 32;  // if a data reference is validated more than 32 times, it implies a recursive validation error.

    public static final MFPNumeric THE_NULL_DATA_VALUE = MFPNumeric.ZERO;
    
    public static boolean isDataClassType(DATATYPES dataType2Comp, DATATYPES dataType1)	{
    	return dataType2Comp == dataType1;
    }
    public static boolean isDataClassType(DATATYPES dataType2Comp, DATATYPES dataType1, DATATYPES dataType2)	{
    	return dataType2Comp == dataType1 || dataType2Comp == dataType2;
    }
    public static boolean isDataClassType(DATATYPES dataType2Comp, DATATYPES dataType1, DATATYPES dataType2, DATATYPES dataType3)	{
    	return dataType2Comp == dataType1 || dataType2Comp == dataType2 || dataType2Comp == dataType3;
    }
    public static boolean isDataClassType(DATATYPES dataType2Comp, DATATYPES dataType1, DATATYPES dataType2, DATATYPES dataType3, DATATYPES dataType4)	{
    	return dataType2Comp == dataType1 || dataType2Comp == dataType2 || dataType2Comp == dataType3 ||  dataType2Comp == dataType4;
    }
    
    /*// this method is too slow in Android.
    public static boolean isDataClassType(DATATYPES dataType2Comp, DATATYPES... dataTypes) {
        for (DATATYPES dataType : dataTypes) {
            if (dataType2Comp == dataType) {
                return true;
            }
        }
        return false;
    }*/
    
    public static boolean isDataClassType(DataClass dc, DATATYPES dataType1)	{
    	DATATYPES dataType2Comp = dc.getDataClassType();
    	return dataType2Comp == dataType1;
    }
    public static boolean isDataClassType(DataClass dc, DATATYPES dataType1, DATATYPES dataType2)	{
    	DATATYPES dataType2Comp = dc.getDataClassType();
    	return dataType2Comp == dataType1 || dataType2Comp == dataType2;
    }
    public static boolean isDataClassType(DataClass dc, DATATYPES dataType1, DATATYPES dataType2, DATATYPES dataType3)	{
    	DATATYPES dataType2Comp = dc.getDataClassType();
    	return dataType2Comp == dataType1 || dataType2Comp == dataType2 || dataType2Comp == dataType3;
    }
    public static boolean isDataClassType(DataClass dc, DATATYPES dataType1, DATATYPES dataType2, DATATYPES dataType3, DATATYPES dataType4)	{
    	DATATYPES dataType2Comp = dc.getDataClassType();
    	return dataType2Comp == dataType1 || dataType2Comp == dataType2 || dataType2Comp == dataType3 ||  dataType2Comp == dataType4;
    }
    
    /*// this method is too slow in Android.
    public static boolean isDataClassType(DataClass dc, DATATYPES... dataTypes) {
        for (DATATYPES dataType : dataTypes) {
            if (dc.getDataClassType() == dataType) {
                return true;
            }
        }
        return false;
    }*/
    
    public static boolean isBaseObj(DataClass dc) {
        return isDataClassType(dc, DATATYPES.DATUM_BASE_OBJECT);
    }
    
    public static boolean isClassInstance(DataClass dc) {
        return isDataClassType(dc, DATATYPES.DATUM_CLASS_INSTANCE);
    }
    
    public static boolean isPrimitiveOrArray(DataClass dc) {
        return (dc instanceof DataClassNull) || (dc instanceof DataClassSingleNum) || (dc instanceof DataClassComplex)
                || (dc instanceof DataClassArray) || (dc instanceof DataClassString) || (dc instanceof DataClassAExpr)
                || (dc instanceof DataClassFuncRef) || (dc instanceof DataClassExtObjRef);
    }
    
    public static boolean isSingleBoolean(DataClass dc)    {
        if (dc instanceof DataClassSingleNum) {
            return ((DataClassSingleNum)dc).isSingleBoolean();
        }
        return false;
    }
    
    public static boolean isSingleInteger(DataClass dc)    {
        if (dc instanceof DataClassSingleNum) {
            return ((DataClassSingleNum)dc).isSingleInteger();
        }
        return false;
    }
    
    public static boolean isSingleDouble(DataClass dc)    {
        if (dc instanceof DataClassSingleNum) {
            return ((DataClassSingleNum)dc).isSingleDouble();
        }
        return false;
    }
    
    public static boolean isNumericalData(DataClass dc, boolean bLookOnNullAsZero) throws JFCALCExpErrException    {
        if (dc instanceof DataClassNull) {
            return ((DataClassNull)dc).isNumericalData(bLookOnNullAsZero);
        } else if (dc instanceof DataClassSingleNum) {
            return ((DataClassSingleNum)dc).isNumericalData(bLookOnNullAsZero);
        } else if (dc instanceof DataClassComplex) {
            return ((DataClassComplex)dc).isNumericalData(bLookOnNullAsZero);
        } else if (dc instanceof DataClassArray) {
            return ((DataClassArray)dc).isNumericalData(bLookOnNullAsZero);
        } else {
            return false;
        }
    }
    
    public static boolean isZeros(DataClass dc, boolean bExplicitNullIsZero) throws JFCALCExpErrException    {
        if (dc instanceof DataClassNull) {
            return ((DataClassNull)dc).isZeros(bExplicitNullIsZero);
        } else if (dc instanceof DataClassSingleNum) {
            return ((DataClassSingleNum)dc).isZeros(bExplicitNullIsZero);
        } else if (dc instanceof DataClassComplex) {
            return ((DataClassComplex)dc).isZeros(bExplicitNullIsZero);
        } else if (dc instanceof DataClassArray) {
            return ((DataClassArray)dc).isZeros(bExplicitNullIsZero);
        } else {
            return false;
        }
    }
    
    public static boolean isEye(DataClass dc, boolean bExplicitNullIsZero) throws JFCALCExpErrException    {
        if (dc instanceof DataClassSingleNum) {
            return ((DataClassSingleNum)dc).isEye(bExplicitNullIsZero);
        } else if (dc instanceof DataClassComplex) {
            return ((DataClassComplex)dc).isEye(bExplicitNullIsZero);
        } else if (dc instanceof DataClassArray) {
            return ((DataClassArray)dc).isEye(bExplicitNullIsZero);
        } else {
            return false;
        }
    }

    public static boolean isEqual(DataClass dcSrc, DataClass dcDest, double errorScale) throws JFCALCExpErrException    {
        if (dcSrc instanceof DataClassSingleNum) {
            return ((DataClassSingleNum)dcSrc).isEqual(dcDest, errorScale);
        } else if (dcSrc instanceof DataClassComplex) {
            return ((DataClassComplex)dcSrc).isEqual(dcDest, errorScale);
        } else if (dcSrc instanceof DataClassArray) {
            return ((DataClassArray)dcSrc).isEqual(dcDest, errorScale);
        } else {
            return dcSrc.isEqual(dcDest);
        }
    }

    public static boolean isBitEqual(DataClass dcSrc, DataClass dcDest) throws JFCALCExpErrException    {
        double errorScale = 1;
        if (dcSrc instanceof DataClassSingleNum) {
            return ((DataClassSingleNum)dcSrc).isEqual(dcDest, errorScale);
        } else if (dcSrc instanceof DataClassComplex) {
            return ((DataClassComplex)dcSrc).isEqual(dcDest, errorScale);
        } else if (dcSrc instanceof DataClassArray) {
            return ((DataClassArray)dcSrc).isBitEqual(dcDest);
        } else {
            return dcSrc.isBitEqual(dcDest);
        }
    }
    
    public static DataClassAExpr lightCvtOrRetDCAExpr(DataClass dc) throws JFCALCExpErrException {
        if (dc.isNull()) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE);
        } else if (dc instanceof DataClassAExpr) {
            return (DataClassAExpr) dc;
        } else {
            return (DataClassAExpr) dc.convertData2NewType(DATATYPES.DATUM_ABSTRACT_EXPR);
        }
    }
    
    /**
     * Try to light convert dc to desired data class type.
     * @param dc
     * @return null (if not convertible) or a valid data class type (if convertible)
     */
    public static DataClassAExpr try2LightCvtOrRetDCAExpr(DataClass dc) {
        try {
            DataClassAExpr datumReturn = lightCvtOrRetDCAExpr(dc);
            return datumReturn;
        } catch (JFCALCExpErrException ex) {
            return null;
        }
    }
    
    public static DataClassArray lightCvtOrRetDCArray(DataClass dc) throws JFCALCExpErrException {
        if (dc.isNull()) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE);
        } else if (dc instanceof DataClassArray) {
            return (DataClassArray) dc;
        } else {
            return (DataClassArray) dc.convertData2NewType(DATATYPES.DATUM_REF_DATA);
        }
    }
    
    /**
     * Try to light convert dc to desired data class type.
     * @param dc
     * @return null (if not convertible) or a valid data class type (if convertible)
     */
    public static DataClassArray try2LightCvtOrRetDCArray(DataClass dc) {
        try {
            DataClassArray datumReturn = lightCvtOrRetDCArray(dc);
            return datumReturn;
        } catch (JFCALCExpErrException ex) {
            return null;
        }
    }
    
    /**
     * This function is different from lightCvtOrRetDCArray, this function will convert a Null to []
     * instead of throw an exception.
     * @param dc
     * @return
     * @throws com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException 
     */
    public static DataClassArray lightCvtOrRetDCArrayNoExcept(DataClass dc) throws JFCALCExpErrException {
        if (dc.isNull()) {
            return new DataClassArray(new DataClass[]{}); // convert to a zero size array.
        } else if (dc instanceof DataClassArray) {
            return (DataClassArray) dc;
        } else {
            return (DataClassArray) dc.convertData2NewType(DATATYPES.DATUM_REF_DATA);
        }
    }
    
    /**
     * Try to light convert dc to desired data class type.
     * @param dc
     * @return null (if not convertible) or a valid data class type (if convertible)
     */
    public static DataClassArray try2LightCvtOrRetDCArrayNoExcept(DataClass dc) {
        try {
            DataClassArray datumReturn = lightCvtOrRetDCArrayNoExcept(dc);
            return datumReturn;
        } catch (JFCALCExpErrException ex) {
            return null;
        }
    }
    
    public static DataClassComplex lightCvtOrRetDCComplex(DataClass dc) throws JFCALCExpErrException {
        if (dc.isNull()) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE);
        } else if (dc instanceof DataClassComplex) {
            return (DataClassComplex) dc;
        } else {
            return (DataClassComplex) dc.convertData2NewType(DATATYPES.DATUM_COMPLEX);
        }
    }
    
    /**
     * Try to light convert dc to desired data class type.
     * @param dc
     * @return null (if not convertible) or a valid data class type (if convertible)
     */
    public static DataClassComplex try2LightCvtOrRetDCComplex(DataClass dc) {
        try {
            DataClassComplex datumReturn = lightCvtOrRetDCComplex(dc);
            return datumReturn;
        } catch (JFCALCExpErrException ex) {
            return null;
        }
    }
    
    public static DataClassFuncRef lightCvtOrRetDCFuncRef(DataClass dc) throws JFCALCExpErrException {
        if (dc.isNull()) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE);
        } else if (dc instanceof DataClassFuncRef) {
            return (DataClassFuncRef) dc;
        } else {
            return (DataClassFuncRef) dc.convertData2NewType(DATATYPES.DATUM_REF_FUNC);
        }
    }
    
    /**
     * Try to light convert dc to desired data class type.
     * @param dc
     * @return null (if not convertible) or a valid data class type (if convertible)
     */
    public static DataClassFuncRef try2LightCvtOrRetDCFuncRef(DataClass dc) {
        try {
            DataClassFuncRef datumReturn = lightCvtOrRetDCFuncRef(dc);
            return datumReturn;
        } catch (JFCALCExpErrException ex) {
            return null;
        }
    }
    
    public static DataClassExtObjRef lightCvtOrRetDCExtObjRef(DataClass dc) throws JFCALCExpErrException {
        if (dc.isNull()) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE);
        } else if (dc instanceof DataClassExtObjRef) {
            return (DataClassExtObjRef) dc;
        } else {
            return (DataClassExtObjRef) dc.convertData2NewType(DATATYPES.DATUM_REF_EXTOBJ);
        }
    }
    
    /**
     * Try to light convert dc to desired data class type.
     * @param dc
     * @return null (if not convertible) or a valid data class type (if convertible)
     */
    public static DataClassExtObjRef try2LightCvtOrRetDCExtObjRef(DataClass dc) {
        try {
            DataClassExtObjRef datumReturn = lightCvtOrRetDCExtObjRef(dc);
            return datumReturn;
        } catch (JFCALCExpErrException ex) {
            return null;
        }
    }
    
    public static DataClassClass lightCvtOrRetDCClass(DataClass dc) throws JFCALCExpErrException {
        if (dc.isNull()) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE);
        } else if (dc instanceof DataClassClass) {
            return (DataClassClass) dc;
        } else {
            return (DataClassClass) dc.convertData2NewType(DATATYPES.DATUM_CLASS_INSTANCE);
        }
    }
    
    /**
     * Try to light convert dc to desired data class type.
     * @param dc
     * @return null (if not convertible) or a valid data class type (if convertible)
     */
    public static DataClassClass try2LightCvtOrRetDCClass(DataClass dc) {
        try {
            DataClassClass datumReturn = lightCvtOrRetDCClass(dc);
            return datumReturn;
        } catch (JFCALCExpErrException ex) {
            return null;
        }
    }
    
    public static DataClassNull lightCvtOrRetDCNull(DataClass dc) throws JFCALCExpErrException {
        if (dc.isNull()) {
            return new DataClassNull();
        } else {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_ANY_OTHER_DATATYPE_TO_NONEXIST);
        }
    }
    
    /**
     * Try to light convert dc to desired data class type.
     * @param dc
     * @return null (if not convertible) or a valid data class type (if convertible)
     */
    public static DataClassNull try2LightCvtOrRetDCNull(DataClass dc) {
        try {
            DataClassNull datumReturn = lightCvtOrRetDCNull(dc);
            return datumReturn;
        } catch (JFCALCExpErrException ex) {
            return null;
        }
    }
    
    public static DataClassSingleNum lightCvtOrRetDCSingleNum(DataClass dc) throws JFCALCExpErrException {
        if (dc.isNull()) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE);
        } else if (dc instanceof DataClassSingleNum) {
            return (DataClassSingleNum) dc;
        } else {
            return (DataClassSingleNum) dc.convertData2NewType(DATATYPES.DATUM_MFPDEC);
        }
    }
    
    /**
     * Try to light convert dc to desired data class type.
     * @param dc
     * @return null (if not convertible) or a valid data class type (if convertible)
     */
    public static DataClassSingleNum try2LightCvtOrRetDCSingleNum(DataClass dc) {
        try {
            DataClassSingleNum datumReturn = lightCvtOrRetDCSingleNum(dc);
            return datumReturn;
        } catch (JFCALCExpErrException ex) {
            return null;
        }
    }
    
    public static DataClassSingleNum lightCvtOrRetDCMFPDec(DataClass dc) throws JFCALCExpErrException {
        if (isDataClassType(dc, DATATYPES.DATUM_MFPDEC)) {
            return (DataClassSingleNum) dc;
        } else {
            return (DataClassSingleNum) dc.convertData2NewType(DATATYPES.DATUM_MFPDEC);
        }
    }
    
    /**
     * Try to light convert dc to desired data class type.
     * @param dc
     * @return null (if not convertible) or a valid data class type (if convertible)
     */
    public static DataClassSingleNum try2LightCvtOrRetDCMFPDec(DataClass dc) {
        try {
            DataClassSingleNum datumReturn = lightCvtOrRetDCMFPDec(dc);
            return datumReturn;
        } catch (JFCALCExpErrException ex) {
            return null;
        }
    }
    
    public static DataClassSingleNum lightCvtOrRetDCMFPInt(DataClass dc) throws JFCALCExpErrException {
        if (isDataClassType(dc, DATATYPES.DATUM_MFPINT)) {
            return (DataClassSingleNum) dc;
        } else {
            return (DataClassSingleNum) dc.convertData2NewType(DATATYPES.DATUM_MFPINT);
        }
    }
    
    /**
     * Try to light convert dc to desired data class type.
     * @param dc
     * @return null (if not convertible) or a valid data class type (if convertible)
     */
    public static DataClassSingleNum try2LightCvtOrRetDCMFPInt(DataClass dc) {
        try {
            DataClassSingleNum datumReturn = lightCvtOrRetDCMFPInt(dc);
            return datumReturn;
        } catch (JFCALCExpErrException ex) {
            return null;
        }
    }
    
    public static DataClassSingleNum lightCvtOrRetDCMFPBool(DataClass dc) throws JFCALCExpErrException {
        if (isDataClassType(dc, DATATYPES.DATUM_MFPBOOL)) {
            return (DataClassSingleNum) dc;
        } else {
            return (DataClassSingleNum) dc.convertData2NewType(DATATYPES.DATUM_MFPBOOL);
        }
    }
    
    /**
     * Try to light convert dc to desired data class type.
     * @param dc
     * @return null (if not convertible) or a valid data class type (if convertible)
     */
    public static DataClassSingleNum try2LightCvtOrRetDCMFPBool(DataClass dc) {
        try {
            DataClassSingleNum datumReturn = lightCvtOrRetDCMFPInt(dc);
            return datumReturn;
        } catch (JFCALCExpErrException ex) {
            return null;
        }
    }
    
    public static DataClassString lightCvtOrRetDCString(DataClass dc) throws JFCALCExpErrException {
        if (dc.isNull()) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE);
        } else if (dc instanceof DataClassString) {
            return (DataClassString) dc;
        } else {
            return (DataClassString) dc.convertData2NewType(DATATYPES.DATUM_STRING);
        }
    }
    
    /**
     * Try to light convert dc to desired data class type.
     * @param dc
     * @return null (if not convertible) or a valid data class type (if convertible)
     */
    public static DataClassString try2LightCvtOrRetDCString(DataClass dc) {
        try {
            DataClassString datumReturn = lightCvtOrRetDCString(dc);
            return datumReturn;
        } catch (JFCALCExpErrException ex) {
            return null;
        }
    }
}
