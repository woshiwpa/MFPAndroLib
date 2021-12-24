// MFP project, DataClassComplex.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class caters reference of complex, and of course null.
 * Note that the reference itself is immutable. And real and image are also immutable.
 * A design standard is, for extended class type, it is treated as complex if DATATYPE
 * is unknown.
 * @author tony
 *
 */
public final class DataClassComplex extends DataClass
{
	private static final long serialVersionUID = 1L;
	private DATATYPES menumDataType = DATATYPES.DATUM_NULL;   
    private DataClass[] mdataList = new DataClass[0];    // this is for ref_data
    private String melemFullTypeName = "::mfp::lang::numeric";
    
    @Override
    public String getTypeName() {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return "null";
        } else if (DCHelper.isDataClassType(this, DATATYPES.DATUM_COMPLEX)) {
            return "complex(" + melemFullTypeName.substring(melemFullTypeName.lastIndexOf(":")) + ")";
        } else {
            return "complex(" + melemFullTypeName.substring(melemFullTypeName.lastIndexOf(":")) + ")";   // will not be here.
        }
    }
    
    @Override
    public String getTypeFullName() {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return "::mfp::lang::null";
        } else if (DCHelper.isDataClassType(this, DATATYPES.DATUM_COMPLEX)) {
            return "::mfp::lang::complex(" + melemFullTypeName + ")";
        } else {
            return "::mfp::lang::complex(" + melemFullTypeName + ")";    // will not be here.
        }
    }
    
    public DataClassComplex()   {
        super();
    }    

    /**
     * construct a complex using real and image
     * @param mfpNumReal
     * @param mfpNumImage
     * @throws JFCALCExpErrException
     */
    public DataClassComplex(MFPNumeric mfpNumReal, MFPNumeric mfpNumImage) throws JFCALCExpErrException    {
        super();
        setComplex(mfpNumReal, mfpNumImage);
    }
    
    /**
     * construct a complex using r and theta
     * @param mfpNumRadAngle
     * @throws JFCALCExpErrException
     */
    public DataClassComplex(MFPNumeric[] mfpNumRadAngle) throws JFCALCExpErrException    {
        super();
        if (mfpNumRadAngle.length != 2) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        }
        MFPNumeric mfpNumReal = mfpNumRadAngle[0].multiply(MFPNumeric.cos(mfpNumRadAngle[1]));
        MFPNumeric mfpNumImage = mfpNumRadAngle[0].multiply(MFPNumeric.sin(mfpNumRadAngle[1]));
        if (!mfpNumImage.isActuallyZero()
                && mfpNumReal.divide(mfpNumImage).abs().compareTo(DCHelper.THE_MAX_RELATIVE_ERROR_OF_TRIGONOMETRIC_CALCULATION) < 0) {
            // comparing to image, real is too small so that it should be set to zero.
            mfpNumReal = MFPNumeric.ZERO;
        } else if (!mfpNumReal.isActuallyZero()
                && mfpNumImage.divide(mfpNumReal).abs().compareTo(DCHelper.THE_MAX_RELATIVE_ERROR_OF_TRIGONOMETRIC_CALCULATION) < 0) {
            // comparing to real, image is too small so that it should be set to zero.
            mfpNumImage = MFPNumeric.ZERO;
        }

        setComplex(mfpNumReal, mfpNumImage);
    }

    /**
     * construct a complex using real and image
     * @param dataReal
     * @param dataImage
     * @throws JFCALCExpErrException
     */
    public DataClassComplex(DataClass dataReal, DataClass dataImage) throws JFCALCExpErrException    {
        super();
        if (!(dataReal instanceof DataClassSingleNum) || !(dataImage instanceof DataClassSingleNum)) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
        }
        setComplex(((DataClassSingleNum)dataReal).getDataValue(),
                ((DataClassSingleNum)dataImage).getDataValue());
    }

    /**
     * construct a complex using r and theta
     * @param dataList
     * @throws JFCALCExpErrException
     */
    public DataClassComplex(DataClass[] dataList) throws JFCALCExpErrException    {
        super();
        if (dataList.length != 2) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        } else if (!(dataList[0] instanceof DataClassSingleNum) || !(dataList[1] instanceof DataClassSingleNum)) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
        }
        
        MFPNumeric mfpNumRad = ((DataClassSingleNum)dataList[0]).getDataValue();
        MFPNumeric mfpNumAngle = ((DataClassSingleNum)dataList[1]).getDataValue();
        MFPNumeric mfpNumReal = mfpNumRad.multiply(MFPNumeric.cos(mfpNumAngle));
        MFPNumeric mfpNumImage = mfpNumRad.multiply(MFPNumeric.sin(mfpNumAngle));
        if (!mfpNumImage.isActuallyZero()
                && mfpNumReal.divide(mfpNumImage).abs().compareTo(DCHelper.THE_MAX_RELATIVE_ERROR_OF_TRIGONOMETRIC_CALCULATION) < 0) {
            // comparing to image, real is too small so that it should be set to zero.
            mfpNumReal = MFPNumeric.ZERO;
        } else if (!mfpNumReal.isActuallyZero()
                && mfpNumImage.divide(mfpNumReal).abs().compareTo(DCHelper.THE_MAX_RELATIVE_ERROR_OF_TRIGONOMETRIC_CALCULATION) < 0) {
            // comparing to real, image is too small so that it should be set to zero.
            mfpNumImage = MFPNumeric.ZERO;
        }
        setComplex(mfpNumRad, mfpNumAngle);
    }

    /**
     * This private function is for internal construction use only.
     * @param mfpNumReal
     * @param mfpNumImage
     * @throws JFCALCExpErrException
     */
    private void setComplex(MFPNumeric mfpNumReal, MFPNumeric mfpNumImage) throws JFCALCExpErrException    {
        menumDataType = DATATYPES.DATUM_COMPLEX;
        mdataList = new DataClass[2];
        if (mfpNumImage == null)    {
            mdataList[0] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
        } else  {
            mdataList[0] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumReal.toDblOrNanInfMFPNum());
        }
        if (mfpNumImage == null)    {
            mdataList[1] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
        } else  {
            mdataList[1] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumImage.toDblOrNanInfMFPNum());
        }
        validateDataClass();    
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
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            mdataList = new DataClass[0];
        } else { // for both DATATYPES.DATUM_COMPLEX and extended types.
            if (mdataList != null && mdataList.length > 0)    {
                if (mdataList.length > 2)    {
                    DataClass[] dataList = new DataClass[2];
                    dataList[0] = mdataList[0];
                    dataList[1] = mdataList[1];
                    mdataList = dataList;
                }
                // here, real/image data type must be one of boolean, integer or double,
                // cannot be an extended type.
                if (!DCHelper.isDataClassType(mdataList[0], DATATYPES.DATUM_MFPBOOL,
                        DATATYPES.DATUM_MFPINT, DATATYPES.DATUM_MFPDEC)) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_TYPE);
                }
                mdataList[0].validateDataClass_Step1();
                if (mdataList.length > 1 && mdataList[1] != null)    {
                    if (!DCHelper.isDataClassType(mdataList[1], DATATYPES.DATUM_MFPBOOL,
                            DATATYPES.DATUM_MFPINT, DATATYPES.DATUM_MFPDEC)) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_TYPE);
                    }
                    mdataList[1].validateDataClass_Step1();    // recursive calling
                }
            }
        }
    }
    
    @Override
    public DATATYPES getDataClassType()    {
        return menumDataType;
    }
    
    public DataClass[] getDataList() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        } else {
            return mdataList;
        }
    }

    public MFPNumeric[] getComplex() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        } else {
            MFPNumeric[] mfpNumListRealImage = new MFPNumeric[2];
            mfpNumListRealImage[0] = getReal();
            mfpNumListRealImage[1] = getImage();
            return mfpNumListRealImage;
        }
    }
    
    public MFPNumeric[] getComplexRadAngle() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        } else {
            MFPNumeric[] mfpNumListRadAngle = new MFPNumeric[2];
            MFPNumeric mfpNumReal = getReal();
            MFPNumeric mfpNumImage = getImage();
            mfpNumListRadAngle[0] = MFPNumeric.hypot(mfpNumReal, mfpNumImage);
            if (!mfpNumImage.isActuallyZero() && !mfpNumImage.isNanOrInf()
                    && mfpNumReal.divide(mfpNumImage).abs().compareTo(DCHelper.THE_MAX_RELATIVE_ERROR_OF_TRIGONOMETRIC_CALCULATION) < 0)    {
                // comparing to image, real is too small so that it should be set to zero.
                mfpNumReal =MFPNumeric.ZERO;
            } else if (!mfpNumReal.isActuallyZero() && !mfpNumReal.isNanOrInf()
                    && mfpNumImage.divide(mfpNumReal).abs().compareTo(DCHelper.THE_MAX_RELATIVE_ERROR_OF_TRIGONOMETRIC_CALCULATION) < 0)    {
                // comparing to real, image is too small so that it should be set to zero.
                mfpNumImage =MFPNumeric.ZERO;
            }
            mfpNumListRadAngle[1] = MFPNumeric.atan2(mfpNumImage, mfpNumReal);
            return mfpNumListRadAngle;
        }
    }
    
    public MFPNumeric getReal() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        } else if (mdataList == null || mdataList.length == 0
                || mdataList[0] == null || DCHelper.isDataClassType(mdataList[0], DATATYPES.DATUM_NULL))    {
            return MFPNumeric.ZERO;
        } else if (DCHelper.isDataClassType(mdataList[0], DATATYPES.DATUM_MFPBOOL)) {
            return ((DataClassSingleNum)mdataList[0]).getDataValue().toIntOrNanInfMFPNum();
        } else if (DCHelper.isDataClassType(mdataList[0], DATATYPES.DATUM_MFPINT, DATATYPES.DATUM_MFPDEC))    {
            return ((DataClassSingleNum)mdataList[0]).getDataValue();
        } else { // mdataList[0] cannot be an extended type coz in constructor it has been set as double.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
        }
    }
    
    public DataClass getRealDataClass() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        } else if (mdataList == null || mdataList.length == 0
                || mdataList[0] == null || DCHelper.isDataClassType(mdataList[0], DATATYPES.DATUM_NULL))    {
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
        } else if (DCHelper.isDataClassType(mdataList[0], DATATYPES.DATUM_MFPBOOL)) {
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT,
                    ((DataClassSingleNum)mdataList[0]).getDataValue().toIntOrNanInfMFPNum());
        } else if (DCHelper.isDataClassType(mdataList[0], DATATYPES.DATUM_MFPINT))    {
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT,
                    ((DataClassSingleNum)mdataList[0]).getDataValue());
        } else if (DCHelper.isDataClassType(mdataList[0], DATATYPES.DATUM_MFPDEC))    {
            return new DataClassSingleNum(DATATYPES.DATUM_MFPDEC,
                    ((DataClassSingleNum)mdataList[0]).getDataValue());
        } else { // mdataList[0] cannot be an extended type coz in constructor it has been set as double.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
        }
    }
    
    public MFPNumeric getImage() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        } else if (mdataList == null || mdataList.length < 2
                || mdataList[1] == null || DCHelper.isDataClassType(mdataList[1], DATATYPES.DATUM_NULL))    {
            return MFPNumeric.ZERO;
        } else if (DCHelper.isDataClassType(mdataList[1], DATATYPES.DATUM_MFPBOOL)) {
            return ((DataClassSingleNum)mdataList[1]).getDataValue().toIntOrNanInfMFPNum();
        } else if (DCHelper.isDataClassType(mdataList[1], DATATYPES.DATUM_MFPINT, DATATYPES.DATUM_MFPDEC))    {
            return ((DataClassSingleNum)mdataList[1]).getDataValue();
        } else { // mdataList[1] cannot be an extended type coz in constructor it has been set as double.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
        }
    }
    
    public DataClass getImageDataClass() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        } else if (mdataList == null || mdataList.length < 2
                || mdataList[1] == null || DCHelper.isDataClassType(mdataList[1], DATATYPES.DATUM_NULL))    {
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
        } else if (DCHelper.isDataClassType(mdataList[1], DATATYPES.DATUM_MFPBOOL)) {
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT,
                    ((DataClassSingleNum)mdataList[1]).getDataValue().toIntOrNanInfMFPNum());
        } else if (DCHelper.isDataClassType(mdataList[1], DATATYPES.DATUM_MFPINT))    {
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT,
                    ((DataClassSingleNum)mdataList[1]).getDataValue());
        } else if (DCHelper.isDataClassType(mdataList[1], DATATYPES.DATUM_MFPDEC))    {
            return new DataClassSingleNum(DATATYPES.DATUM_MFPDEC,
                    ((DataClassSingleNum)mdataList[1]).getDataValue());
        } else { // mdataList[1] cannot be an extended type coz in constructor it has been set as double.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
        }
    }
    
    // change data type. Note that this function guarantees that, for a data array, its elements will not change.
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
            // it is complex
            if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_MFPBOOL, DATATYPES.DATUM_MFPINT, DATATYPES.DATUM_MFPDEC)) {
                // we shouldnt use getImage.isZero() because GetImage may return a very very very
                // small double which is the result of something like 1/3 - 1/4 - 1/12.
                if (!getImage().isActuallyZero())    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_COMPLEX);
                }
                MFPNumeric mfpValue = getReal();
                if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_MFPBOOL)) {
                    try {
                        mfpValue = mfpValue.toBoolMFPNum();
                    } catch (ArithmeticException e) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN);
                    }
                } else if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_MFPINT)) {
                    mfpValue = mfpValue.toIntOrNanInfMFPNum();
                } else {    // double.
                    mfpValue = mfpValue.toDblOrNanInfMFPNum();
                }
                return new DataClassSingleNum(enumNewDataType, mfpValue);
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
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_COMPLEX);    // will not be here.
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
            // I am a complex or an extended class type. datum's type should not be
            // limited to complex either. It can also be a extended class type.
            if (datum instanceof DataClassSingleNum
                    && MFPNumeric.isEqual(getReal(), ((DataClassSingleNum)datum).getDataValue(), errorScale)
                    && MFPNumeric.isEqual(getImage(), MFPNumeric.ZERO, errorScale))    {
                return true;
            } else if (datum instanceof DataClassComplex
                    && MFPNumeric.isEqual(((DataClassComplex)datum).getReal(), getReal(), errorScale)
                    && MFPNumeric.isEqual(((DataClassComplex)datum).getImage(), getImage(), errorScale))    {
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
     * To enable extended type compatibility, use
     * new DataClassComplex(getReal(), getImage());
     * instead of
     * new DataClassComplex(mmfpNumReal, mmfpNumImage);
     */
    @Override
    public DataClass copySelf() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return new DataClassComplex();
        } else {
            DataClassComplex datumReturn = new DataClassComplex(getReal(), getImage());
            return datumReturn;
        }
    }
    
    /**
     * copy itself (deep copy) and return a new reference.
     * To enable extended type compatibility, use
     * new DataClassComplex(getReal(), getImage());
     * instead of
     * new DataClassComplex(mmfpNumReal, mmfpNumImage);
     */
    @Override
    public DataClass cloneSelf() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return new DataClassComplex();
        } else {
            DataClassComplex datumReturn = new DataClassComplex(getReal(), getImage());
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
            MFPNumeric mfpNumReal = getReal(), mfpNumImage = getImage();
            String strOutputReal = mfpNumReal.toString();
            String strRealImageConn = "+";
            if (mfpNumImage.isActuallyNegative())    {
                mfpNumImage = MFPNumeric.MINUS_ONE.multiply(mfpNumImage);
                strRealImageConn = "-";
            }
            String strOutputImage = mfpNumImage.toString();
            if (mfpNumReal.isActuallyZero() && mfpNumImage.isActuallyZero())    {
                strOutput = "0";
            } else if (mfpNumReal.isActuallyZero())    {
                if (strOutputImage.equals("1"))    {
                    strOutput = "i";
                } else    {    // nani and infi are supported
                    strOutput = strOutputImage + "i";
                }
                if (strRealImageConn.equals("-"))    {
                    strOutput = strRealImageConn + strOutput;
                }
            } else if (mfpNumImage.isActuallyZero())    {
                strOutput = strOutputReal;
            } else    {
                if (MFPNumeric.isEqual(mfpNumImage, MFPNumeric.ONE))    {
                    strOutput = strOutputReal + " " + strRealImageConn + " " + "i";
                } else {    // nani and infi are supported.
                    strOutput = strOutputReal + " " + strRealImageConn + " " + strOutputImage + "i";
                }
            }
        }
        return strOutput;
    }
    
    @Override
    public int getHashCode() throws JFCALCExpErrException {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return 0;
        }
        return getReal().hashCode() + 1000003 * getImage().hashCode();
    }
}

