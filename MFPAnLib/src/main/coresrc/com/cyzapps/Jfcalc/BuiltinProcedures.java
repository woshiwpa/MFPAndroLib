// MFP project, BuiltinProcedures.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;

public class BuiltinProcedures {
    // this class includes many built-in mathematic procedures
        
    public static DataClass evaluateNegSign(DataClass datumOperand) throws JFCALCExpErrException    {
        // note that if operand is an array, it should NOT be fully populated.
        // inside this function datumOperand should not be changed.
        if (!datumOperand.isPrimitiveOrArray()) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_TYPE);
        }
        DataClass datumReturn = new DataClassNull();
        if (DCHelper.isDataClassType(datumOperand, DATATYPES.DATUM_MFPBOOL)) {
            try {
                MFPNumeric mfpReturn = DCHelper.lightCvtOrRetDCMFPBool(datumOperand).getDataValue().toBoolMFPNum().negate();
                datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, mfpReturn);
            } catch (ArithmeticException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN);
            }
        } else if (DCHelper.isDataClassType(datumOperand, DATATYPES.DATUM_MFPINT)) {
            MFPNumeric mfpReturn = DCHelper.lightCvtOrRetDCMFPInt(datumOperand).getDataValue().toIntOrNanInfMFPNum().negate();
            datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, mfpReturn);
        } else if (DCHelper.isDataClassType(datumOperand, DATATYPES.DATUM_MFPDEC)) {
            MFPNumeric mfpReturn = DCHelper.lightCvtOrRetDCMFPDec(datumOperand).getDataValue().toDblOrNanInfMFPNum().negate();
            datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpReturn);
        } else if (DCHelper.isDataClassType(datumOperand, DATATYPES.DATUM_COMPLEX)) {
            DataClass datumReal = DCHelper.lightCvtOrRetDCComplex(datumOperand).getRealDataClass();  // not use deep copy here coz GetRealDataClass generates a new class 
            datumReal = evaluateNegSign(datumReal);
            DataClass datumImage = DCHelper.lightCvtOrRetDCComplex(datumOperand).getImageDataClass();    // not use deep copy here coz GetImageDataClass generates a new class 
            datumImage = DCHelper.lightCvtOrRetDCMFPDec(evaluateNegSign(datumImage));
            // now datumImage must be a DataClassBuiltIn type.
            if (DCHelper.lightCvtOrRetDCMFPDec(datumImage).getDataValue().isActuallyZero())    {
                datumReturn = datumReal;
            } else    {
                datumReturn = new DataClassComplex(datumReal, datumImage);
            }
        } else if (datumOperand.getThisOrNull() instanceof DataClassArray) {
            datumReturn = new DataClassNull();
            // data_ref type
            if (DCHelper.lightCvtOrRetDCArray(datumOperand).getDataList() == null) {
                // empty array.
                datumReturn = new DataClassArray(new DataClass[0]);
            } else  {
                DataClass[] dataListOprd = DCHelper.lightCvtOrRetDCArray(datumOperand).getDataList();
                DataClass[] dataListRet = new DataClass[dataListOprd.length];
                for (int idx = 0; idx < dataListOprd.length; idx ++)  {
                    if (dataListOprd[idx] == null)    {
                        dataListRet[idx] = new DataClassNull();  // if we find any null, we convert it first
                    }
                    if (!dataListOprd[idx].isNull() && !(dataListOprd[idx] instanceof DataClassSingleNum)
                            && !(dataListOprd[idx] instanceof DataClassComplex) && !(dataListOprd[idx] instanceof DataClassArray))    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
                    } else if (dataListOprd[idx].isNull())   {
                        // data_null is treated as a numerical (0) in data_reference (matrix)
                        dataListRet[idx] = new DataClassNull();
                    } else  {
                        dataListRet[idx] = evaluateNegSign(dataListOprd[idx]);
                    }
                }
                datumReturn = new DataClassArray(dataListRet);
            }
        } else {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
        }
        return datumReturn;
    }
    
    public static DataClass evaluateAdding(DataClass datumFirstOperand, DataClass datumSecondOperand) throws JFCALCExpErrException    {
        // note that if operand(s) are array, they should have been fully populated.
        // inside this function datumFirstOperand and datumSecondOperand should not be changed
    	// and returned value doesn't refer to any element in datumFirstOperand
    	// and datumSecondOperand even if datumFirstOperand or datumSecondOperand is zero.
        if (!DCHelper.isPrimitiveOrArray(datumFirstOperand) || !DCHelper.isPrimitiveOrArray(datumSecondOperand)) {
            if (!(datumFirstOperand instanceof DataClassString) && !(datumSecondOperand instanceof DataClassString)) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_TYPE);
            }   // as long as one data type is string we can still do the add.
        }
        DataClass datumReturn = new DataClassNull();
        if (datumFirstOperand.getThisOrNull() instanceof DataClassString
                        || datumSecondOperand.getThisOrNull() instanceof DataClassString)    {
            // any one of the parameter is string, so output a string.
            String str1st = new String(), str2nd = new String();
            if (datumFirstOperand.getThisOrNull() instanceof DataClassString)    {
                str1st = DCHelper.lightCvtOrRetDCString(datumFirstOperand).getStringValue();
            } else    {
                str1st = datumFirstOperand.output();    // do not use toString because output can throw exception from overridden function
            }
            if (datumSecondOperand.getThisOrNull() instanceof DataClassString)    {
                str2nd = DCHelper.lightCvtOrRetDCString(datumSecondOperand).getStringValue();
            } else    {
                str2nd = datumSecondOperand.output();    // do not use toString because output can throw exception from overridden function
            }
            
            datumReturn = new DataClassString(str1st + str2nd);    
        } else if ((!(datumFirstOperand.getThisOrNull() instanceof DataClassArray)
                        && DCHelper.isZeros(datumFirstOperand, false))
                || (!(datumSecondOperand.getThisOrNull() instanceof DataClassArray)
                        && DCHelper.isZeros(datumSecondOperand, false)))    {
            // if one of the operand is zero (not data reference zero).
            if (!(datumFirstOperand.getThisOrNull() instanceof DataClassArray)
                    && DCHelper.isZeros(datumFirstOperand, false))    {
                // first operand is zero. we don't care second operand's dimension.
                datumReturn = datumSecondOperand.cloneSelf();
            } else    {
                // second operand is zero. we don't care first operand's dimension.
                datumReturn = datumFirstOperand.cloneSelf();
            }            
        } else if (!(datumFirstOperand.getThisOrNull() instanceof DataClassArray)
                        || !(datumSecondOperand.getThisOrNull() instanceof DataClassArray))    {
            //at least one of the operand is not array.
            // if cannot be converted to complex, exception
            DataClassComplex datumFirstOperandCplx = DCHelper.lightCvtOrRetDCComplex(datumFirstOperand);
            DataClassComplex datumSecondOperandCplx = DCHelper.lightCvtOrRetDCComplex(datumSecondOperand);
            DataClassSingleNum datum1stReal = DCHelper.lightCvtOrRetDCMFPDec(datumFirstOperandCplx.getRealDataClass());
            DataClassSingleNum datum1stImage = DCHelper.lightCvtOrRetDCMFPDec(datumFirstOperandCplx.getImageDataClass());
            DataClassSingleNum datum2ndReal = DCHelper.lightCvtOrRetDCMFPDec(datumSecondOperandCplx.getRealDataClass());
            DataClassSingleNum datum2ndImage = DCHelper.lightCvtOrRetDCMFPDec(datumSecondOperandCplx.getImageDataClass());
            // datum1stReal, 1stImage, 2ndReal and 2ndImage are all DataClassSingleNum.
            datum1stReal = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC,
                    datum1stReal.getDataValue().add(datum2ndReal.getDataValue()));
            if (datum1stReal.isSingleInteger())    {
                datum1stReal = DCHelper.lightCvtOrRetDCMFPInt(datum1stReal);
            }
            datum1stImage = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC,
                    datum1stImage.getDataValue().add(datum2ndImage.getDataValue()));
            if (datum1stImage.isSingleInteger())    {
                datum1stImage = DCHelper.lightCvtOrRetDCMFPInt(datum1stImage);
            }
            datumReturn = new DataClassComplex(datum1stReal, datum1stImage);
            if (datum1stImage.getDataValue().isActuallyZero())    {
                datumReturn = datum1stReal;
            }            
        } else    {
            int nListLen1 = DCHelper.lightCvtOrRetDCArray(datumFirstOperand).getDataListSize();
            int nListLen2 = DCHelper.lightCvtOrRetDCArray(datumSecondOperand).getDataListSize();
            if (nListLen1 != nListLen2)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
            }
            // both of the operands are arrays
            DataClass[] dataList = new DataClass[nListLen1];
            int[] narrayIndex = new int[1];
            for (int index = 0; index < nListLen1; index ++)    {
                narrayIndex[0] = index;
                dataList[index] = evaluateAdding(DCHelper.lightCvtOrRetDCArray(datumFirstOperand).getDataAtIndexByRef(narrayIndex),
                        DCHelper.lightCvtOrRetDCArray(datumSecondOperand).getDataAtIndexByRef(narrayIndex));
            }
            datumReturn = new DataClassArray(dataList);
        }
        return datumReturn;
    }
    
    public static DataClass evaluateSubstraction(DataClass datumFirstOperand, DataClass datumSecondOperand) throws JFCALCExpErrException    {
        // note that if operand(s) are array, they should have been fully populated.
        // Moreover, the parameters datumFirstOperand and datumSecondOperand will not be modified inside
        // the function. and returned value doesn't refer to any element in datumFirstOperand
    	// and datumSecondOperand even if datumFirstOperand or datumSecondOperand is zero.
        if (!DCHelper.isPrimitiveOrArray(datumFirstOperand) || !DCHelper.isPrimitiveOrArray(datumSecondOperand)) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_TYPE);
        }
        DataClass datumReturn = new DataClassNull();
        if ((!(datumFirstOperand.getThisOrNull() instanceof DataClassArray)
                    && DCHelper.isZeros(datumFirstOperand, false))
                || (!(datumSecondOperand.getThisOrNull() instanceof DataClassArray)
                    && DCHelper.isZeros(datumSecondOperand, false)))    {
            // if one of the operand is zero (not data reference zero).
            if (!(datumFirstOperand.getThisOrNull() instanceof DataClassArray)
                    && DCHelper.isZeros(datumFirstOperand, false))    {
                datumReturn = evaluateNegSign(datumSecondOperand);
            } else    {
                // second operand is zero. we don't care first operand's dimension.
                datumReturn = datumFirstOperand.cloneSelf();
            }            
        } else if (!(datumFirstOperand.getThisOrNull() instanceof DataClassArray)
                || !(datumSecondOperand.getThisOrNull() instanceof DataClassArray))    {
            //at least one of the operand is not array.
            // if cannot be converted to complex, exception
            DataClassComplex datumFirstOperandCplx = DCHelper.lightCvtOrRetDCComplex(datumFirstOperand);
            DataClassComplex datumSecondOperandCplx = DCHelper.lightCvtOrRetDCComplex(datumSecondOperand);
            DataClassSingleNum datum1stReal = DCHelper.lightCvtOrRetDCMFPDec(datumFirstOperandCplx.getRealDataClass());
            DataClassSingleNum datum1stImage = DCHelper.lightCvtOrRetDCMFPDec(datumFirstOperandCplx.getImageDataClass());
            DataClassSingleNum datum2ndReal = DCHelper.lightCvtOrRetDCMFPDec(datumSecondOperandCplx.getRealDataClass());
            DataClassSingleNum datum2ndImage = DCHelper.lightCvtOrRetDCMFPDec(datumSecondOperandCplx.getImageDataClass());
            // datum1stReal, 1stImage, 2ndReal and 2ndImage are all DataClassBuiltIn.
            datum1stReal = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC,
                    datum1stReal.getDataValue().subtract(datum2ndReal.getDataValue()));
            if (datum1stReal.isSingleInteger())    {
                datum1stReal = DCHelper.lightCvtOrRetDCMFPInt(datum1stReal);
            }
            datum1stImage = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC,
                    datum1stImage.getDataValue().subtract(datum2ndImage.getDataValue()));
            if (datum1stImage.isSingleInteger())    {
                datum1stImage = DCHelper.lightCvtOrRetDCMFPInt(datum1stImage);
            }
            datumReturn = new DataClassComplex(datum1stReal, datum1stImage);
            if (datum1stImage.getDataValue().isActuallyZero())    {
                datumReturn = datum1stReal;
            }            
        } else    {
            int nListLen1 = DCHelper.lightCvtOrRetDCArray(datumFirstOperand).getDataListSize();
            int nListLen2 = DCHelper.lightCvtOrRetDCArray(datumSecondOperand).getDataListSize();
            if (nListLen1 != nListLen2)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
            }
            // both of the operands are arrays
            DataClass[] dataList = new DataClass[nListLen1];
            int[] narrayIndex = new int[1];
            for (int index = 0; index < nListLen1; index ++)    {
                narrayIndex[0] = index;
                dataList[index] = evaluateSubstraction(DCHelper.lightCvtOrRetDCArray(datumFirstOperand).getDataAtIndexByRef(narrayIndex),
                        DCHelper.lightCvtOrRetDCArray(datumSecondOperand).getDataAtIndexByRef(narrayIndex));
            }
            datumReturn = new DataClassArray(dataList);
        }
        return datumReturn;
    }
    
    public static DataClass evaluateMultiplication(DataClass datumFirstOperand, DataClass datumSecondOperand) throws JFCALCExpErrException    {
        // note that if operand(s) are array, they should have been fully populated.
        // the parameters datumFirstOperand and datumSecondOperand will not be modified inside
        // the function. And the returned result will not refer to any element of the parameters.
        if (!DCHelper.isPrimitiveOrArray(datumFirstOperand) || !DCHelper.isPrimitiveOrArray(datumSecondOperand)) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_TYPE);
        }
        DataClass datumReturn = new DataClassNull();
        if (!(datumFirstOperand.getThisOrNull() instanceof DataClassArray)
                && !(datumSecondOperand.getThisOrNull() instanceof DataClassArray))    {
            // if data1 and data2 are not arrays
            DataClassComplex datumFirstOperandCplx = DCHelper.lightCvtOrRetDCComplex(datumFirstOperand);
            DataClassComplex datumSecondOperandCplx = DCHelper.lightCvtOrRetDCComplex(datumSecondOperand);
            if (datumFirstOperandCplx.getImage().isActuallyZero()
                    && datumSecondOperandCplx.getImage().isActuallyZero()) {
                // if both data1 and data2 are real values. Need not to worry about string type because getImage in that case throws exceptions
                // ok, now the data types are ok
                MFPNumeric mfpNumResult = datumFirstOperandCplx.getReal().multiply(datumSecondOperandCplx.getReal());
                if (mfpNumResult.isActuallyInteger())   {
                    datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, mfpNumResult);
                } else {
                    datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumResult);
                }
            } else {
                // one of the data is complex.
                DataClassSingleNum datum1stReal = DCHelper.lightCvtOrRetDCMFPDec(datumFirstOperandCplx.getRealDataClass());
                DataClassSingleNum datum1stImage = DCHelper.lightCvtOrRetDCMFPDec(datumFirstOperandCplx.getImageDataClass());
                DataClassSingleNum datum2ndReal = DCHelper.lightCvtOrRetDCMFPDec(datumSecondOperandCplx.getRealDataClass());
                DataClassSingleNum datum2ndImage = DCHelper.lightCvtOrRetDCMFPDec(datumSecondOperandCplx.getImageDataClass());
                // datum1stReal, 1stImage, 2ndReal and 2ndImage are all DataClassBuiltIn.
                DataClassSingleNum datumResultReal = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC,
                        datum1stReal.getDataValue().multiply(datum2ndReal.getDataValue())
                        .subtract(datum1stImage.getDataValue().multiply(datum2ndImage.getDataValue())));
                if (datumResultReal.isSingleInteger())    {
                    datumResultReal = DCHelper.lightCvtOrRetDCMFPInt(datumResultReal);
                }
                DataClassSingleNum datumResultImage = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC,
                        datum1stReal.getDataValue().multiply(datum2ndImage.getDataValue())
                        .add(datum1stImage.getDataValue().multiply(datum2ndReal.getDataValue())));
                if (datumResultImage.isSingleInteger())    {
                    datumResultImage = DCHelper.lightCvtOrRetDCMFPInt(datumResultImage);
                }
                datumReturn = new DataClassComplex(datumResultReal, datumResultImage);
                if (DCHelper.lightCvtOrRetDCComplex(datumReturn).getImage().isActuallyZero())    {
                    datumReturn = datumResultReal;
                }
            }
        } else if (!(datumFirstOperand.getThisOrNull() instanceof DataClassArray)
                || !(datumSecondOperand.getThisOrNull() instanceof DataClassArray))    {
            // if one of the data is not array
            DataClassArray datumArray = new DataClassArray();
            DataClass datumNumber = new DataClassNull();
            boolean bNumber1st = true;
            if (!(datumFirstOperand.getThisOrNull() instanceof DataClassArray))    {
                datumArray = DCHelper.lightCvtOrRetDCArray(datumSecondOperand.copySelf());
                datumNumber = datumFirstOperand.copySelf();
            } else    {
                datumArray = DCHelper.lightCvtOrRetDCArray(datumFirstOperand.copySelf());
                datumNumber = datumSecondOperand.copySelf();
                bNumber1st = false;
            }
            DataClass[] dataList = new DataClass[datumArray.getDataListSize()];
            int[] narrayIndex = new int[1];
            for (int index = 0; index < datumArray.getDataListSize(); index ++)    {
                narrayIndex[0] = index;
                if (bNumber1st)    {
                    dataList[index] = evaluateMultiplication(datumNumber, datumArray.getDataAtIndexByRef(narrayIndex));
                } else    {
                    dataList[index] = evaluateMultiplication(datumArray.getDataAtIndexByRef(narrayIndex), datumNumber);
                }
            }
            datumReturn = new DataClassArray(dataList);
        } else    {
            // if both of the operands are arrays.
            int[] narrayFirstDims = datumFirstOperand.recalcDataArraySize();
            if (narrayFirstDims.length == 0
                    || narrayFirstDims[narrayFirstDims.length - 1] != DCHelper.lightCvtOrRetDCArray(datumSecondOperand).getDataListSize())    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
            }
            
            if (DCHelper.lightCvtOrRetDCArray(datumFirstOperand).getDataListSize() <= 0)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
            } else if (!(DCHelper.lightCvtOrRetDCArray(datumFirstOperand).getDataList()[0].getThisOrNull() instanceof DataClassArray))    {
                // because the array has been fully populated, we must have arrived at the last dimension
                int nFirstOperandDataListSize = DCHelper.lightCvtOrRetDCArray(datumFirstOperand).getDataListSize();
                if (nFirstOperandDataListSize != DCHelper.lightCvtOrRetDCArray(datumSecondOperand).getDataListSize())    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
                }
                for (int index = 0; index < nFirstOperandDataListSize; index ++)    {
                    if (DCHelper.lightCvtOrRetDCArray(datumFirstOperand).getDataList()[index].getThisOrNull() instanceof DataClassArray)    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
                    }
                    DataClass dataResultElem = evaluateMultiplication(DCHelper.lightCvtOrRetDCArray(datumFirstOperand).getDataList()[index],
                            DCHelper.lightCvtOrRetDCArray(datumSecondOperand).getDataList()[index]);
                    if (index == 0)    {
                        datumReturn = dataResultElem;
                    } else    {
                        datumReturn = evaluateAdding(datumReturn, dataResultElem);
                    }
                }
            } else    {
                int nFirstOperandDataListSize = DCHelper.lightCvtOrRetDCArray(datumFirstOperand).getDataListSize();
                DataClass[] dataList = new DataClass[nFirstOperandDataListSize];
                // because the array has been fully populated, recursion can be used.
                for (int index = 0; index < nFirstOperandDataListSize; index ++)    {
                    // we need not to worry about null return because only if 
                    // datumFirstOperand.GetDataListSize() > 0 we can enter this loop.
                    DataClass datumResultElem = evaluateMultiplication(DCHelper.lightCvtOrRetDCArray(datumFirstOperand).getDataList()[index], datumSecondOperand);
                    dataList[index] = datumResultElem;
                }
                datumReturn = new DataClassArray(dataList);
            }
        }
        return datumReturn;
    }
    
    public static DataClass divideByNumber(DataClass datumFirstOperand, DataClass datumSecondOperand) throws JFCALCExpErrException    {
        // in this function, the denominator should not be an array.
        if (!DCHelper.isPrimitiveOrArray(datumFirstOperand) || !DCHelper.isPrimitiveOrArray(datumSecondOperand)) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_TYPE);
        }
        if (!(datumFirstOperand.getThisOrNull() instanceof DataClassArray)
                && !(datumSecondOperand.getThisOrNull() instanceof DataClassArray))    {
            // if data1 and data2 are not arrays
            // should not directly compare datumSecondOperand.getDataValue() or its abs value with 0 coz it is more close to zero than
            // real and image.
            /* // no longer need divide by zero exception.
             * if (datumSecondOperand.isEqual(new DataClassNull(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO)))    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_DIVISOR_CAN_NOT_BE_ZERO);    
            }*/
            DataClassComplex datumFirstOperandCplx = DCHelper.lightCvtOrRetDCComplex(datumFirstOperand);
            DataClassComplex datumSecondOperandCplx = DCHelper.lightCvtOrRetDCComplex(datumSecondOperand);
            if (datumFirstOperandCplx.getImage().isActuallyZero()
                    && datumSecondOperandCplx.getImage().isActuallyZero()) {
                // if both data1 and data2 are real values. need not to worry about string type because getImage will throw exceptions if so.
                // ok, now the data types are ok
                MFPNumeric mfpNum1stOperand = datumFirstOperandCplx.getReal();
                MFPNumeric mfpNum2ndOperand = datumSecondOperandCplx.getReal();
                MFPNumeric mfpNumResult = MFPNumeric.divide(mfpNum1stOperand, mfpNum2ndOperand);
                DataClass datumReturn = new DataClassNull();
                if (mfpNumResult.isActuallyInteger())   {
                    datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, mfpNumResult);
                } else {
                    datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumResult);
                }
                return datumReturn;
            } else {
                DataClassSingleNum datum1stReal = DCHelper.lightCvtOrRetDCMFPDec(datumFirstOperandCplx.getRealDataClass());
                DataClassSingleNum datum1stImage = DCHelper.lightCvtOrRetDCMFPDec(datumFirstOperandCplx.getImageDataClass());
                DataClassSingleNum datum2ndReal = DCHelper.lightCvtOrRetDCMFPDec(datumSecondOperandCplx.getRealDataClass());
                DataClassSingleNum datum2ndImage = DCHelper.lightCvtOrRetDCMFPDec(datumSecondOperandCplx.getImageDataClass());
                DATATYPES enumRealType = DATATYPES.DATUM_MFPDEC;
                DATATYPES enumImageType = DATATYPES.DATUM_MFPDEC;
                MFPNumeric mfpNumTmp = datum2ndReal.getDataValue().multiply(datum2ndReal.getDataValue())
                        .add(datum2ndImage.getDataValue().multiply(datum2ndImage.getDataValue()));
                DataClass datumResultReal = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC,
                        MFPNumeric.divide(datum1stReal.getDataValue().multiply(datum2ndReal.getDataValue())
                        .add(datum1stImage.getDataValue().multiply(datum2ndImage.getDataValue())), mfpNumTmp));
                DataClass datumResultImage = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC,
                        MFPNumeric.divide(datum1stReal.getDataValue().negate()
                        .multiply(datum2ndImage.getDataValue()).add(datum1stImage.getDataValue()
                        .multiply(datum2ndReal.getDataValue())), mfpNumTmp));
                DataClass datumReturn = new DataClassComplex(datumResultReal, datumResultImage);
                if (DCHelper.lightCvtOrRetDCComplex(datumReturn).getImage().isActuallyZero())    {
                    datumReturn = datumResultReal;
                }
                return datumReturn;
            }
        } else if (!(datumSecondOperand.getThisOrNull() instanceof DataClassArray))    {
            // first operand is an array.
            DataClass[] dataList = new DataClass[DCHelper.lightCvtOrRetDCArray(datumFirstOperand).getDataListSize()];
            for (int index = 0; index < DCHelper.lightCvtOrRetDCArray(datumFirstOperand).getDataListSize(); index ++)    {
                dataList[index] = divideByNumber(DCHelper.lightCvtOrRetDCArray(datumFirstOperand).getDataList()[index],
                                                    datumSecondOperand);
            }
            DataClass datumReturn = new DataClassArray(dataList);
            return datumReturn;
        } else    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);    
        }
        
    }

    private static DataClass cvtMatrix2Vector(DataClass datumMatrix, int nCvtLevel) throws JFCALCExpErrException    {
        // assume that datumMatrix is a fully populated matrix and nCvtlevel is a positive integer.
        // this function will refer to each element of the parameter datumMatrix, but will not change its value.
        int[] narraySize = datumMatrix.recalcDataArraySize();
        if (narraySize.length <= nCvtLevel + 1)    {
            return datumMatrix;
        }
        DataClass datumReturn = new DataClassNull();
        DataClass[] dataList = new DataClass[0];
        datumMatrix = DCHelper.lightCvtOrRetDCArray(datumMatrix);
        for (int index = 0; index < DCHelper.lightCvtOrRetDCArray(datumMatrix).getDataListSize(); index ++)    {
            // cvtMatrix2Vector will return a matrix and so that we can convert.
            DataClassArray datumTmp = DCHelper.lightCvtOrRetDCArray(cvtMatrix2Vector(DCHelper.lightCvtOrRetDCArray(datumMatrix).getDataList()[index], nCvtLevel));
            DataClass[] dataList1 = new DataClass[dataList.length + datumTmp.getDataListSize()];
            for (int index1 = 0; index1 < dataList.length + datumTmp.getDataListSize(); index1 ++)    {
                if (index1 < dataList.length)    {
                    dataList1[index1] = dataList[index1];
                } else    {
                    dataList1[index1] = datumTmp.getDataList()[index1 - dataList.length];
                }
            }
            dataList = dataList1;
        }
        datumReturn = new DataClassArray(dataList);
        return datumReturn;
    }
    
    public static DataClass evaluateDivision(DataClass datumFirstOperand, DataClass datumSecondOperand) throws JFCALCExpErrException    {
        // note that if operand(s) are array, they should have been fully populated.
        // the parameters datumFirstOperand and datumSecondOperand will not be modified inside
        // the function. And the return value will not refer to any element of datumFirstOperand
    	// and datumSecondOperand
        DataClass datumReturn = new DataClassNull();
        if (!(datumFirstOperand.getThisOrNull() instanceof DataClassArray)
                && DCHelper.isEye(datumFirstOperand, false))    {
            // first operand is 1.
            datumReturn = evaluateReciprocal(datumSecondOperand);        
        } else if (datumFirstOperand.getThisOrNull() instanceof DataClassArray
                && datumSecondOperand.getThisOrNull() instanceof DataClassArray)    {
            int[] narrayFirstSize = datumFirstOperand.recalcDataArraySize();
            int[] narraySecondSize = datumSecondOperand.recalcDataArraySize();
            if (narrayFirstSize.length < 2 || narraySecondSize.length < 2)    {
                // one dim matrix division is not well defined. For example, [1, 2]/[2, 4] can
                // result in a 2 * 2 matrix, but can also returns 0.5.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
            }
            for (int index = 1; index < narraySecondSize.length; index ++)    {
                int nIndexGap = narrayFirstSize.length - narraySecondSize.length;
                if (narrayFirstSize[index + nIndexGap] != narraySecondSize[index])    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_PARAMETER_NOT_MATCH);
                }
            }
            DataClass datumT1stOprnd = evaluateTransposition(datumFirstOperand);
            DataClass datumT2ndOprnd = evaluateTransposition(datumSecondOperand);
            DataClass datumLinearizedTy = cvtMatrix2Vector(datumT1stOprnd, narrayFirstSize.length + 1 - narraySecondSize.length);
            DataClass datumLinearizedTA = cvtMatrix2Vector(datumT2ndOprnd, 1);
            datumReturn = leftDivideBy2DMatrix(datumLinearizedTy, datumLinearizedTA);
            datumReturn = evaluateTransposition(datumReturn);
        } else    {
            datumReturn = divideByNumber(datumFirstOperand, datumSecondOperand);
        }
        
        return datumReturn;
    }
    
    public static DataClass evaluateReciprocal(DataClass datum) throws JFCALCExpErrException    {
        // note that if operand is an array, it should have been fully populated.
        // the parameter datum will not be modified in this function.
        DataClass datumReturn = new DataClassNull();
        if (datum.getThisOrNull() instanceof DataClassArray)    {
            int[] narraySize = datum.recalcDataArraySize();
            if (narraySize.length != 2)    {
                // at this moment only support reciprocal of number of 2D matrix
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
            }
            DataClass datumEye = createEyeMatrix(narraySize[1], 2);
            datumReturn = evaluateDivision(datumEye, datum);
        } else    {    // it is just an number.
            datumReturn = divideByNumber(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE), datum);
        }
        return datumReturn;
    }
    
    public static DataClass evaluateLeftDivision(DataClass datumFirstOperand, DataClass datumSecondOperand) throws JFCALCExpErrException    {
        // note that if operand(s) are array, they should have been fully populated.
        // the parameters datumFirstOperand and datumSecondOperand will not be modified inside
        // the function.
        // also note that left division the first operator is divisor
        DataClass datumReturn = new DataClassNull();
        if (!(datumSecondOperand.getThisOrNull() instanceof DataClassArray)
                && DCHelper.isEye(datumSecondOperand, false))    {
            // second operand is 1.
            datumReturn = evaluateLeftReciprocal(datumFirstOperand);        
        } else if (datumFirstOperand.getThisOrNull() instanceof DataClassArray
                && datumSecondOperand.getThisOrNull() instanceof DataClassArray)    {
            int[] narrayFirstSize = datumFirstOperand.recalcDataArraySize();
            int[] narraySecondSize = datumSecondOperand.recalcDataArraySize();
            if (narrayFirstSize.length < 2 || narraySecondSize.length < 2)    {
                // one dim matrix left-division is not well defined.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
            }
            for (int index = 0; index < narrayFirstSize.length - 1; index ++)    {
                if (narrayFirstSize[index] != narraySecondSize[index])    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_PARAMETER_NOT_MATCH);
                }
            }
            DataClass datumLinearizedy = cvtMatrix2Vector(datumSecondOperand, narraySecondSize.length + 1 - narrayFirstSize.length);
            DataClass datumLinearizedA = cvtMatrix2Vector(datumFirstOperand, 1);
            datumReturn = leftDivideBy2DMatrix(datumLinearizedy, datumLinearizedA);
        } else    {
            datumReturn = divideByNumber(datumSecondOperand, datumFirstOperand);
        }
        
        return datumReturn;
    }
    
    public static DataClass evaluateLeftReciprocal(DataClass datum) throws JFCALCExpErrException    {
        // note that if operand is an array, it should have been fully populated.
        // the parameter datum will not be modified in this function.
        DataClass datumReturn = new DataClassNull();
        if (datum.getThisOrNull() instanceof DataClassArray)    {
            int[] narraySize = datum.recalcDataArraySize();
            if (narraySize.length != 2)    {
                // at this moment only support reciprocal of number of 2D matrix
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
            }
            DataClass datumEye = createEyeMatrix(narraySize[0], 2);
            datumReturn = evaluateLeftDivision(datum, datumEye);
        } else    {    // it is just an number.
            datumReturn = divideByNumber(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE), datum);
        }
        return datumReturn;
    }
    
    public static DataClass evaluateTransposition(DataClass datumOperand) throws JFCALCExpErrException    {
        return evaluateTransposition(datumOperand, datumOperand.recalcDataArraySize());
    }
    
    public static DataClass evaluateTransposition(DataClass datumOperand, int[] narrayDims) throws JFCALCExpErrException    {
        // assume this array (datumOperand) has been fully populated if the operand is an array
        // and narrayDims is the dim of the array. datumOperand will not be changed in this function
        // and the return value will not refer to any part of datumOperand.
        if (!(datumOperand.getThisOrNull() instanceof DataClassArray))    {
            // if it is not an array.
            DataClass datumReturn = datumOperand.copySelf();
            return datumReturn;
        } else if (narrayDims == null || narrayDims.length == 0)    {
            return datumOperand;    // do not transpose anything
        } else if (narrayDims.length == 1)    {
            // if this is a 1-D array, convert to x * 1 array
            DataClass datumReturn = new DataClassNull();
            DataClass[] dataList = new DataClass[narrayDims[0]];
            for (int index1 = 0; index1 < narrayDims[0]; index1 ++)    {
                DataClass datum = DCHelper.lightCvtOrRetDCArray(datumOperand).getDataList()[index1].copySelf();
                DataClass[] dataList1 = new DataClass[1];
                dataList1[0] = datum;
                dataList[index1] = new DataClassArray(dataList1);
            }
            datumReturn = new DataClassArray(dataList);
            return datumReturn;
        } else if (narrayDims.length == 2)    {
            // if narrayDims.length == 2, use a fast way
            DataClass datumReturn = new DataClassNull();
            DataClass[] dataList1 = new DataClass[narrayDims[1]];
            for (int index = 0; index < narrayDims[1]; index ++)    {
                dataList1[index] = new DataClassNull();
                DataClass[] dataList0 = new DataClass[narrayDims[0]];
                for (int index1 = 0; index1 < narrayDims[0]; index1 ++)    {
                    dataList0[index1] = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datumOperand).getDataList()[index1]).getDataList()[index].copySelf();
                }
                dataList1[index] = new DataClassArray(dataList0);
            }
            datumReturn = new DataClassArray(dataList1);
            return datumReturn;
        } else  {   // narrayDims.length > 2
            DataClass datumReturn = new DataClassArray(new DataClass[0]);
            int[] narrayNewDims = new int[narrayDims.length];
            for (int idx = 0; idx < narrayDims.length; idx ++)  {
                narrayNewDims[idx] = narrayDims[narrayDims.length - 1 -idx];
            }
            DataClassSingleNum datumDefault = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
            datumReturn = DataClassArray.constructDataArray(narrayNewDims, datumDefault); // populate an empty array
            DataClassArray datumVectorizedOprnd = DCHelper.lightCvtOrRetDCArray(cvtMatrix2Vector(datumOperand, 0)); // cvt the matrix to a list to transvert it
            for (int idx = 0; idx < datumVectorizedOprnd.getDataListSize(); idx ++) {
                int[] narrayIdx = new int[narrayDims.length];
                int nTmp = idx;
                for (int idx1 = 0; idx1 < narrayDims.length; idx1 ++)   {
                    narrayIdx[idx1] = nTmp % narrayDims[narrayDims.length - 1 - idx1];
                    nTmp /= narrayDims[narrayDims.length - 1 - idx1];
                }
                DataClassArray datumReturnArray = DCHelper.lightCvtOrRetDCArray(datumReturn);
                datumReturnArray.setDataAtIndexByRef(narrayIdx, datumVectorizedOprnd.getDataList()[idx].cloneSelf());
                datumReturn = datumReturnArray;
            }
            return datumReturn;
        }
    }
    
    public static DataClass evaluateDeterminant(DataClass datum2DSqrMatrixOprd) throws JFCALCExpErrException   {
        // this function is used to calculate determiinant of a 2D square matrix. Assume datum2DMatrix have been fully populated.
        // Note that the parameter will not be modified in this function.
        DataClassArray datum2DSqrMatrix = DCHelper.lightCvtOrRetDCArray(datum2DSqrMatrixOprd.cloneSelf());

        int[] narraySize2DSqrMatrix = datum2DSqrMatrix.recalcDataArraySize();
        if (narraySize2DSqrMatrix.length != 2 || narraySize2DSqrMatrix[0] == 0 || narraySize2DSqrMatrix[1] == 0
                || narraySize2DSqrMatrix[0] != narraySize2DSqrMatrix[1])    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
        }
        // datum2DSqrMatrix must be a fully populated 2D matrix.
        int nNumofLnSwaps = 0;
        for (int idxLn = 0; idxLn < narraySize2DSqrMatrix[0]; idxLn ++)    {
            int nMaxAbsLnIdx = idxLn;
            DataClassComplex datumTmp = DCHelper.lightCvtOrRetDCComplex(DCHelper.lightCvtOrRetDCArray(datum2DSqrMatrix.getDataList()[idxLn]).getDataList()[idxLn]);
            MFPNumeric mfpNumMaxAbsSqr = MFPNumeric.hypot(datumTmp.getReal(), datumTmp.getImage());
            for (int idxLn1 = idxLn + 1; idxLn1 < narraySize2DSqrMatrix[0]; idxLn1 ++)    {
                datumTmp = DCHelper.lightCvtOrRetDCComplex(DCHelper.lightCvtOrRetDCArray(datum2DSqrMatrix.getDataList()[idxLn1]).getDataList()[idxLn]);
                MFPNumeric mfpNumAbsSqr = MFPNumeric.hypot(datumTmp.getReal(), datumTmp.getImage());
                if (mfpNumAbsSqr.compareTo(mfpNumMaxAbsSqr) > 0)    {
                    nMaxAbsLnIdx = idxLn1;
                    mfpNumMaxAbsSqr = mfpNumAbsSqr;
                }
            }
            if (nMaxAbsLnIdx != idxLn)  {
                // swap the line to ensure the driagonal element always have the max abs value compared to others in the same
                // column but lower lines
                DataClass[] dataListTmp = DCHelper.lightCvtOrRetDCArray(datum2DSqrMatrix.getDataList()[idxLn]).getDataList();
                datum2DSqrMatrix.getDataList()[idxLn]
                        = new DataClassArray(DCHelper.lightCvtOrRetDCArray(datum2DSqrMatrix.getDataList()[nMaxAbsLnIdx]).getDataList());
                datum2DSqrMatrix.getDataList()[nMaxAbsLnIdx]
                        = new DataClassArray(dataListTmp);
                nNumofLnSwaps ++;
            }
            DataClassSingleNum datumTmp1 = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
            if (DCHelper.lightCvtOrRetDCArray(datum2DSqrMatrix.getDataList()[idxLn]).getDataList()[idxLn].isEqual(datumTmp1))   {
                // determinant is zero
                return datumTmp1;
            }
            for (int idxLn1 = idxLn + 1; idxLn1 < narraySize2DSqrMatrix[0]; idxLn1 ++)    {
                DataClass datumRowEliminateRatio
                    = divideByNumber(DCHelper.lightCvtOrRetDCArray(datum2DSqrMatrix.getDataList()[idxLn1]).getDataList()[idxLn],
                            DCHelper.lightCvtOrRetDCArray(datum2DSqrMatrix.getDataList()[idxLn]).getDataList()[idxLn]);
                datum2DSqrMatrix.getDataList()[idxLn1]
                    = evaluateSubstraction(datum2DSqrMatrix.getDataList()[idxLn1],
                            evaluateMultiplication(datumRowEliminateRatio, datum2DSqrMatrix.getDataList()[idxLn]));
            }
        }
        DataClass datumReturn = DCHelper.lightCvtOrRetDCArray(datum2DSqrMatrix.getDataList()[0]).getDataList()[0];
        for (int idx = 1; idx < narraySize2DSqrMatrix[0]; idx ++)   {
            datumReturn = evaluateMultiplication(datumReturn, DCHelper.lightCvtOrRetDCArray(datum2DSqrMatrix.getDataList()[idx]).getDataList()[idx]);
        }
        if (nNumofLnSwaps % 2 == 1) {
            DataClass datumTmp = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.MINUS_ONE);
            datumReturn = evaluateMultiplication(datumReturn, datumTmp);
        }
        return datumReturn;
    }
    
    public static DataClass leftDivideBy2DMatrix(DataClass datumNumeratorOperand, DataClass datum2DMatrixOperand) throws JFCALCExpErrException    {
        // this function is used to calculate x value in Ax = y where A is datum2DMatrixOperand, y is datumNumeratorOperand
        // assume A and y are both fully populated. Note that parameters will not be modified in this function and return
        // value will not refer to any part of the parameters.
        DataClassArray datumNumerator = DCHelper.lightCvtOrRetDCArray(datumNumeratorOperand.cloneSelf());
        DataClassArray datum2DMatrix = DCHelper.lightCvtOrRetDCArray(datum2DMatrixOperand.cloneSelf());
        
        int[] narraySizeNumerator = datumNumerator.recalcDataArraySize();
        int[] narraySize2DMatrix = datum2DMatrix.recalcDataArraySize();
        
        DataClassArray datumFormatedNumerator = datumNumerator;
        if (narraySizeNumerator.length == 1)    {
            if (narraySizeNumerator[0] == 0)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
            } else    {
                // this is 1D vector, in the transpose, it is turned to x * 1 vector
                datumFormatedNumerator = DCHelper.lightCvtOrRetDCArray(evaluateTransposition(datumNumerator));
            }
        } else    {
            for (int index = 0; index < narraySizeNumerator.length; index ++)    {
                if (narraySizeNumerator[index] == 0)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
                }
            }
        }
        narraySizeNumerator = new int[1];
        // datumFormatedNumerator must be an array and datum2DMatrix must be an 2D array.
        narraySizeNumerator[0] = datumFormatedNumerator.getDataListSize();
        
        if (narraySize2DMatrix.length != 2 || narraySize2DMatrix[0] == 0 || narraySize2DMatrix[1] == 0)    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
        }
        
        if (narraySize2DMatrix[0] < narraySize2DMatrix[1])    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INDEFINITE_RESULT);
        }
        
        if (narraySizeNumerator[0] != narraySize2DMatrix[0])    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
        }
        
        for (int index0 = 0; index0 < narraySize2DMatrix[1]; index0 ++)    {   // for every column
            for (int index1 = 0; index1 < narraySize2DMatrix[0]; index1 ++)    {   // for every line
                if (index0 == index1)    {
                    continue;
                } else    {
                    if (DCHelper.lightCvtOrRetDCArray(datum2DMatrix.getDataList()[index0]).getDataList()[index0]
                        .isEqual(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO)))    {
                        // if the diagonal element is 0.
                        int index2 = index0 + 1;
                        for (; index2 < narraySize2DMatrix[0]; index2 ++)    {
                            if (DCHelper.lightCvtOrRetDCArray(datum2DMatrix.getDataList()[index2]).getDataList()[index0]
                                .isEqual(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO)) == false)    {
                                datum2DMatrix.getDataList()[index0]
                                    = evaluateAdding(datum2DMatrix.getDataList()[index0], datum2DMatrix.getDataList()[index2]);
                                datumFormatedNumerator.getDataList()[index0]
                                    = evaluateAdding(datumFormatedNumerator.getDataList()[index0], datumFormatedNumerator.getDataList()[index2]);
                                break;
                            }
                        }
                        if (index2 == narraySize2DMatrix[0])    {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_ANSWER_FOR_MATRIX_DIVISION);
                        }
                    }
                    DataClass datumRowEliminateRatio
                        = divideByNumber(DCHelper.lightCvtOrRetDCArray(datum2DMatrix.getDataList()[index1]).getDataList()[index0],
                                DCHelper.lightCvtOrRetDCArray(datum2DMatrix.getDataList()[index0]).getDataList()[index0]);
                    datum2DMatrix.getDataList()[index1]
                        = evaluateSubstraction(datum2DMatrix.getDataList()[index1],
                                evaluateMultiplication(datumRowEliminateRatio, datum2DMatrix.getDataList()[index0]));
                    datumFormatedNumerator.getDataList()[index1]
                        = evaluateSubstraction(datumFormatedNumerator.getDataList()[index1],
                                evaluateMultiplication(datumRowEliminateRatio, datumFormatedNumerator.getDataList()[index0]));
                }
            }
        }
        for (int index0 = narraySize2DMatrix[1]; index0 < narraySize2DMatrix[0]; index0 ++)    {
            DataClassArray datumTmp = DCHelper.lightCvtOrRetDCArray(cvtMatrix2Vector(datumFormatedNumerator.getDataList()[index0], 0));
            for (int index1 = 0; index1 < datumTmp.getDataListSize(); index1 ++)    {
                if (DCHelper.isEqual(datumTmp.getDataList()[index1], 
                                new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO),
                                narraySize2DMatrix[1] * narraySize2DMatrix[1]) == false)    {   // the error tolerance scale is narraySize2DMatrix[1] * narraySize2DMatrix[1]
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_ANSWER_FOR_MATRIX_DIVISION);
                }
            }
        }
        DataClass[] dataList = new DataClass[narraySize2DMatrix[1]];
        for (int index0 = 0; index0 < narraySize2DMatrix[1]; index0 ++)    {
            DataClass datum = divideByNumber(datumFormatedNumerator.getDataList()[index0],
                    DCHelper.lightCvtOrRetDCArray(datum2DMatrix.getDataList()[index0]).getDataList()[index0]);        
            dataList[index0] = datum;
        }
        DataClass datumResult = new DataClassArray(dataList);
        return datumResult;
    }
    
    public static DataClass invert2DSquare(DataClass datum2DSquareOperand) throws JFCALCExpErrException    {
        // assume that datum2DSquare is always a 2D square matrix and it has been fully populated.
        // Moreover, the parameter datum2DSquareOperand will be not be modified inside the function.
        // nor the return value will refer to any part of the parameter.

        DataClassArray datum2DSquare = DCHelper.lightCvtOrRetDCArray(datum2DSquareOperand.cloneSelf());
        int[] narraySquareSize = datum2DSquare.recalcDataArraySize();
        if (narraySquareSize.length != 2
                || narraySquareSize[0] != narraySquareSize[1]
                || narraySquareSize[0] == 0)    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
        }
        // assume Matrix has been fully populated. datum2DSquare must be a 2D matrix. comment the following statement.
        //datum2DSquare.PopulateDataArray(narraySquareSize, true);
        
        DataClassArray datum2DInv = new DataClassArray(new DataClass[0]);
        DataClass datumDefault = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
        datum2DInv = DCHelper.lightCvtOrRetDCArray(DataClassArray.constructDataArray(narraySquareSize, datumDefault)); // datum2DInv is a 2D matrix.
        
        //initialize I
        for (int index0 = 0; index0 < narraySquareSize[0]; index0 ++)    {
            for (int index1 = 0; index1 < narraySquareSize[1]; index1 ++)    {
                // because datum2DInv is newly created and call AllocDataArray,
                // it should have been fully populated.
                if (index0 == index1)    {
                    DCHelper.lightCvtOrRetDCArray(datum2DInv.getDataList()[index0]).getDataList()[index1]
                            = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                } else    {
                    DCHelper.lightCvtOrRetDCArray(datum2DInv.getDataList()[index0]).getDataList()[index1]
                            = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                }
            }
        }
        
        for (int index0 = 0; index0 < narraySquareSize[0]; index0 ++)    {
            for (int index1 = 0; index1 < narraySquareSize[0]; index1 ++)    {
                if (index0 == index1)    {
                    continue;
                } else    {
                    DataClassComplex datumComplex = DCHelper.lightCvtOrRetDCComplex(DCHelper.lightCvtOrRetDCArray(datum2DSquare
                            .getDataList()[index0]).getDataList()[index0]);
                    MFPNumeric[] mfpNumRadAng = datumComplex.getComplexRadAngle();
                    DataClassSingleNum datumLargestAbs = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumRadAng[0]);
                    int nLargestAbsIdx = index0;
                    for (int idx = index0 + 1; idx < narraySquareSize[0]; idx ++)    {
                        datumComplex = DCHelper.lightCvtOrRetDCComplex(DCHelper.lightCvtOrRetDCArray(datum2DSquare
                                .getDataList()[idx]).getDataList()[index0]);
                        mfpNumRadAng = datumComplex.getComplexRadAngle();
                        DataClassSingleNum datumThisAbs = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumRadAng[0]);
                        // we can do direct value compare here because both datumLargestAbs and datumThisAbs are real.
                        if (datumLargestAbs.getDataValue().compareTo(datumThisAbs.getDataValue()) < 0)    {
                            datumLargestAbs = datumThisAbs;
                            nLargestAbsIdx = idx;
                        }
                    }
                    if (DCHelper.lightCvtOrRetDCArray(datum2DSquare.getDataList()[nLargestAbsIdx]).getDataList()[index0]
                                    .isEqual(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO)))    {
                        // even the largest abs value is zero.
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_MATRIX_CANNOT_BE_INVERTED);
                    }
                    if (nLargestAbsIdx != index0)    {
                        // swap the rows
                        DataClass datumTmp = datum2DSquare.getDataList()[index0];
                        datum2DSquare.getDataList()[index0] = datum2DSquare.getDataList()[nLargestAbsIdx];
                        datum2DSquare.getDataList()[nLargestAbsIdx] = datumTmp;
                        datumTmp = datum2DInv.getDataList()[index0];
                        datum2DInv.getDataList()[index0] = datum2DInv.getDataList()[nLargestAbsIdx];
                        datum2DInv.getDataList()[nLargestAbsIdx] = datumTmp;
                    }
                    /*
                     * the following code cannot guarantee to use maximum abs value at [index0][index0]
                    if (datum2DSquare.getDataList()[index0].getDataList()[index0]
                        .isEqual(new DataClassNull(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO)))    {
                        // if the diagonal element is 0.
                        int index2 = index0 + 1;
                        for (; index2 < narraySquareSize[0]; index2 ++)    {
                            if (datum2DSquare.getDataList()[index2].getDataList()[index0]
                                .isEqual(new DataClassNull(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO)) == false)    {
                                datum2DSquare.getDataList()[index0]
                                                            = evaluateAdding(datum2DSquare.getDataList()[index0], datum2DSquare.getDataList()[index2]);
                                datum2DInv.getDataList()[index0]
                                                            = evaluateAdding(datum2DInv.getDataList()[index0], datum2DInv.getDataList()[index2]);
                                break;
                            }
                        }
                        if (index2 == narraySquareSize[0])    {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_MATRIX_CANNOT_BE_INVERTED);
                        }
                    }*/
                    DataClass datumRowEliminateRatio
                        = divideByNumber(
                                DCHelper.lightCvtOrRetDCArray(datum2DSquare.getDataList()[index1]).getDataList()[index0],
                                DCHelper.lightCvtOrRetDCArray(datum2DSquare.getDataList()[index0]).getDataList()[index0]);
                    datum2DSquare.getDataList()[index1]
                        = evaluateSubstraction(
                                datum2DSquare.getDataList()[index1],
                                evaluateMultiplication(datumRowEliminateRatio, datum2DSquare.getDataList()[index0]));
                    datum2DInv.getDataList()[index1]
                        = evaluateSubstraction(
                                datum2DInv.getDataList()[index1],
                                evaluateMultiplication(datumRowEliminateRatio, datum2DInv.getDataList()[index0]));
                }
            }
        }
        for (int index0 = 0; index0 < narraySquareSize[0]; index0 ++)    {
            for (int index1 = 0; index1 < narraySquareSize[0]; index1 ++)    {
                DCHelper.lightCvtOrRetDCArray(datum2DInv.getDataList()[index0]).getDataList()[index1]
                    = divideByNumber(
                            DCHelper.lightCvtOrRetDCArray(datum2DInv.getDataList()[index0]).getDataList()[index1],
                            DCHelper.lightCvtOrRetDCArray(datum2DSquare.getDataList()[index0]).getDataList()[index0]);
            }
            
        }
        return datum2DInv;
    }
    
    public static DataClass evaluateExp(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClassComplex datum = DCHelper.lightCvtOrRetDCComplex(datumOperand);
        MFPNumeric mfpNumCoeff = MFPNumeric.exp(datum.getReal());
        MFPNumeric mfpNumImagePart = datum.getImage();
        DataClass datumReturnNum = new DataClassComplex(mfpNumCoeff.multiply(MFPNumeric.cos(mfpNumImagePart)),
                mfpNumCoeff.multiply(MFPNumeric.sin(mfpNumImagePart)));
        return datumReturnNum;
    }
    
    public static DataClass evaluateLog(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClassComplex datum = DCHelper.lightCvtOrRetDCComplex(datumOperand);
        MFPNumeric mfpNumRealPart = datum.getReal();
        MFPNumeric mfpNumImagePart = datum.getImage();
        /* log 0 can be handled now.
         * if (mfpNumRealPart.isActuallyZero() && mfpNumImagePart.isActuallyZero())   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }*/
        MFPNumeric mfpNumReturnReal = MFPNumeric.log(MFPNumeric.hypot(mfpNumRealPart, mfpNumImagePart));
        MFPNumeric mfpNumReturnImage = MFPNumeric.atan2(mfpNumImagePart, mfpNumRealPart);
        DataClass datumReturnNum = new DataClassComplex(mfpNumReturnReal, mfpNumReturnImage);
        return datumReturnNum;
    }
    
    public static DataClass evaluateSin(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClassComplex datum = DCHelper.lightCvtOrRetDCComplex(datumOperand);
        MFPNumeric mfpNumRealPart = datum.getReal();
        MFPNumeric mfpNumImagePart = datum.getImage();
        DataClass datumReturnNum = new DataClassNull();
        if (mfpNumImagePart.isActuallyZero())    {
            // this is a real number.
            datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.sin(mfpNumRealPart));
        } else  {
            // this is a complex number
            MFPNumeric mfpNumExpImage = MFPNumeric.exp(mfpNumImagePart);
            MFPNumeric mfpNumExpMinusImage = MFPNumeric.exp(mfpNumImagePart.negate());
            MFPNumeric mfpNumReturnReal = mfpNumExpImage.add(mfpNumExpMinusImage).multiply(MFPNumeric.sin(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            MFPNumeric mfpNumReturnImage = mfpNumExpImage.subtract(mfpNumExpMinusImage).multiply(MFPNumeric.cos(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            datumReturnNum = new DataClassComplex(mfpNumReturnReal, mfpNumReturnImage);
        }
        return datumReturnNum;
    }
    
    public static DataClass evaluateCos(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClassComplex datum = DCHelper.lightCvtOrRetDCComplex(datumOperand);
        MFPNumeric mfpNumRealPart = datum.getReal();
        MFPNumeric mfpNumImagePart = datum.getImage();
        DataClass datumReturnNum = new DataClassNull();
        if (mfpNumImagePart.isActuallyZero())    {
            // this is a real number.
            datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.cos(mfpNumRealPart));
        } else  {
            // this is a complex number
            MFPNumeric mfpNumExpImage = MFPNumeric.exp(mfpNumImagePart);
            MFPNumeric mfpNumExpMinusImage = MFPNumeric.exp(mfpNumImagePart.negate());
            MFPNumeric mfpNumReturnReal = mfpNumExpImage.add(mfpNumExpMinusImage).multiply(MFPNumeric.cos(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            MFPNumeric mfpNumReturnImage = mfpNumExpMinusImage.subtract(mfpNumExpImage).multiply(MFPNumeric.sin(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            datumReturnNum = new DataClassComplex(mfpNumReturnReal, mfpNumReturnImage);
        }
        return datumReturnNum;
    }
    
    public static DataClass evaluateTan(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClassComplex datum = DCHelper.lightCvtOrRetDCComplex(datumOperand);
        MFPNumeric mfpNumRealPart = datum.getReal();
        MFPNumeric mfpNumImagePart = datum.getImage();
        DataClass datumReturnNum = new DataClassNull();
        if (mfpNumImagePart.isActuallyZero())    {
            // this is a real number.
            datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.tan(mfpNumRealPart));
        } else  {
            // this is a complex number
            MFPNumeric mfpNumExpImage = MFPNumeric.exp(mfpNumImagePart);
            MFPNumeric mfpNumExpMinusImage = MFPNumeric.exp(mfpNumImagePart.negate());
            MFPNumeric mfpNumSinReal = mfpNumExpImage.add(mfpNumExpMinusImage).multiply(MFPNumeric.sin(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            MFPNumeric mfpNumSinImage = mfpNumExpImage.subtract(mfpNumExpMinusImage).multiply(MFPNumeric.cos(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            MFPNumeric mfpNumCosReal = mfpNumExpImage.add(mfpNumExpMinusImage).multiply(MFPNumeric.cos(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            MFPNumeric mfpNumCosImage = mfpNumExpMinusImage.subtract(mfpNumExpImage).multiply(MFPNumeric.sin(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            /* // no longer need divide by zero exception.
             * if (mfpNumCosReal.isActuallyZero() && mfpNumCosImage.isActuallyZero())  {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_DIVISOR_CAN_NOT_BE_ZERO);
            }*/
            MFPNumeric mfpNumDivisor = mfpNumCosReal.multiply(mfpNumCosReal).add(mfpNumCosImage.multiply(mfpNumCosImage));
            MFPNumeric mfpNumReturnReal = mfpNumSinReal.multiply(mfpNumCosReal).add(mfpNumSinImage.multiply(mfpNumCosImage)).divide(mfpNumDivisor);
            MFPNumeric mfpNumReturnImage = mfpNumSinImage.multiply(mfpNumCosReal).subtract(mfpNumSinReal.multiply(mfpNumCosImage)).divide(mfpNumDivisor);
            datumReturnNum = new DataClassComplex(mfpNumReturnReal, mfpNumReturnImage);
        }
        return datumReturnNum;
    }

    public static DataClass evaluateASin(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClassComplex datum = DCHelper.lightCvtOrRetDCComplex(datumOperand);
        MFPNumeric mfpNumRealPart = datum.getReal();
        MFPNumeric mfpNumImagePart = datum.getImage();
        DataClass datumReturnNum = new DataClassNull();
        if (mfpNumImagePart.isActuallyZero() && MFPNumeric.compareTo(mfpNumRealPart, MFPNumeric.MINUS_ONE) >= 0
                && MFPNumeric.compareTo(mfpNumRealPart, MFPNumeric.ONE) <= 0)   {
            // this is a real number between [-1, 1].
            datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.asin(mfpNumRealPart));
        } else  {
            // the result will be a complex value.
            DataClassComplex datumI = new DataClassComplex(MFPNumeric.ZERO, MFPNumeric.ONE);
            DataClassSingleNum datumOne = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE);
            DataClassSingleNum datumZeroPntFive = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.HALF);
            
            DataClass datumTmp = evaluateMultiplication(datumOperand, datumOperand);
            datumTmp = evaluateSubstraction(datumOne, datumTmp);
            datumTmp = evaluatePower(datumTmp, datumZeroPntFive, null);
            DataClass datumTmp1 = evaluateMultiplication(datumOperand, datumI);
            datumTmp = evaluateAdding(datumTmp1, datumTmp);
            datumTmp = evaluateLog(datumTmp);
            datumReturnNum = evaluateDivision(datumTmp, datumI);
        }
        return datumReturnNum;
    }

    public static DataClass evaluateAbs(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        MFPNumeric[] mfpNumRadAng = DCHelper.lightCvtOrRetDCComplex(datumOperand).getComplexRadAngle();
        DataClassSingleNum datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumRadAng[0]);
        if (datumReturnNum.isSingleInteger())    {
            datumReturnNum = DCHelper.lightCvtOrRetDCMFPInt(datumReturnNum);
        }
        return datumReturnNum;
    }
    
    public static DataClass evaluateACos(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClassComplex datum = DCHelper.lightCvtOrRetDCComplex(datumOperand);
        MFPNumeric mfpNumRealPart = datum.getReal();
        MFPNumeric mfpNumImagePart = datum.getImage();
        DataClass datumReturnNum = new DataClassNull();
        if (mfpNumImagePart.isActuallyZero() && MFPNumeric.compareTo(mfpNumRealPart, MFPNumeric.MINUS_ONE) >= 0
                && MFPNumeric.compareTo(mfpNumRealPart, MFPNumeric.ONE) <= 0)    {
            // this is a real number between [-1, 1].
            datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.acos(mfpNumRealPart));
        } else  {
            // the result will be a complex value.
            DataClassComplex datumI = new DataClassComplex(MFPNumeric.ZERO, MFPNumeric.ONE);
            DataClassSingleNum datumOne = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE);
            DataClassSingleNum datumZeroPntFive = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.HALF);
            
            DataClass datumTmp = evaluateMultiplication(datumOperand, datumOperand);
            datumTmp = evaluateSubstraction(datumTmp, datumOne);
            datumTmp = evaluatePower(datumTmp, datumZeroPntFive, null);
            datumTmp = evaluateAdding(datumOperand, datumTmp);
            datumTmp = evaluateLog(datumTmp);
            datumReturnNum = evaluateDivision(datumTmp, datumI);
        }
        return datumReturnNum;
    }
    

    public static DataClass evaluateATan(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClassComplex datum = DCHelper.lightCvtOrRetDCComplex(datumOperand);
        MFPNumeric mfpNumRealPart = datum.getReal();
        MFPNumeric mfpNumImagePart = datum.getImage();
        DataClass datumReturnNum = new DataClassNull();
        if (mfpNumImagePart.isActuallyZero())    {
            // this is a real number.
            datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.atan(mfpNumRealPart));
        } else  {
            // the result will be a complex value.
            DataClassComplex datumI = new DataClassComplex(MFPNumeric.ZERO, MFPNumeric.ONE);
            DataClassSingleNum datumOne = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE);
            DataClassComplex datumTwoI = new DataClassComplex(MFPNumeric.ZERO, MFPNumeric.TWO);
            
            // ToDo if x = i or -i atan(x) has not result.
            DataClass datumTmp = evaluateMultiplication(datumOperand, datumI);
            DataClass datumTmp1 = evaluateAdding(datumOne, datumTmp);
            DataClass datumTmp2 = evaluateSubstraction(datumOne, datumTmp);
            datumTmp = evaluateDivision(datumTmp1, datumTmp2);
            datumTmp = evaluateLog(datumTmp);
            datumReturnNum = evaluateDivision(datumTmp, datumTwoI);
        }
        return datumReturnNum;
    }

    public static DataClass evaluatePower(DataClass datumBaseOperand, DataClass datumPowerOperand, DataClass datumNumOfRootsOperand) throws JFCALCExpErrException    {
        DataClass datumReturnNum = new DataClassNull();
        
        DataClass datumNumOfRoots = new DataClassNull();
        boolean bNotReturnList = false;
        if (datumNumOfRootsOperand == null)    {
            bNotReturnList = true;
            datumNumOfRoots = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
        } else    {
            datumNumOfRoots = datumNumOfRootsOperand.copySelf();
        }
        
        int nNumofReturnedRoots = 1;
        DataClassSingleNum datumNumOfRootsInt = DCHelper.lightCvtOrRetDCMFPInt(datumNumOfRoots);
        if (datumNumOfRootsInt.getDataValue().compareTo(MFPNumeric.ONE) < 0)    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_POWER_SHOULD_RETURN_AT_LEAST_ONE_ROOT);
        } else if (datumNumOfRootsInt.getDataValue().longValue() > 0
                && datumNumOfRootsInt.getDataValue().longValue() != Long.MAX_VALUE)    {
            nNumofReturnedRoots = (int)datumNumOfRootsInt.getDataValue().longValue();
        } else {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);  // number of roots is invalid.
        }

        DataClass datumPower = DCHelper.lightCvtOrRetDCComplex(datumPowerOperand);

        boolean bImagePowerMode = false;
        if (!DCHelper.lightCvtOrRetDCComplex(datumPower).getImage().isActuallyZero())    {
            // image power
            bImagePowerMode = true;
            if (nNumofReturnedRoots != 1)    {
                // if image power, only return one result.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
        } else {
            datumPower = DCHelper.lightCvtOrRetDCMFPDec(datumPower);
        }

        if (datumBaseOperand.getThisOrNull() instanceof DataClassArray) {
            // integer power of a matrix (only support integer now)
            if (bImagePowerMode)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MATRIX_POWER_ONLY_SUPPORT_INTEGER_NOW);
            } else  {
                DataClassSingleNum datumPowerInt = DCHelper.lightCvtOrRetDCMFPInt(datumPower);
                if (datumPower.isEqual(datumPowerInt) == false) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_MATRIX_POWER_ONLY_SUPPORT_INTEGER_NOW);               
                }
                int[] narraySize = datumBaseOperand.recalcDataArraySize();
                if (narraySize.length != 2 || (narraySize.length == 2 && narraySize[0] != narraySize[1]))    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_ONLY_SQUARE_MATRIX_SUPPORTED_BY_POWER);               
                }
                // datumBaseOperand must be an array. And inside fullfillDataArray it will be cloned.
                DataClass datumBase = DCHelper.lightCvtOrRetDCArray(datumBaseOperand).fullfillDataArray(narraySize, false);
                datumReturnNum = createEyeMatrix(narraySize[0], 2);
                if (datumPowerInt.getDataValue().compareTo(MFPNumeric.ZERO) > 0)    {
                    DataClassSingleNum datumIdx = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                    // have to identify if they are equal or not because 0.9999999999999999999 should be equal to 1
                    while (!MFPNumeric.isEqual(datumIdx.getDataValue(), datumPowerInt.getDataValue())
                            && datumIdx.getDataValue().compareTo(datumPowerInt.getDataValue()) < 0) {
                        datumReturnNum = evaluateMultiplication(datumReturnNum, datumBase);
                        datumIdx = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, datumIdx.getDataValue().add(MFPNumeric.ONE));
                    }
                } else  {
                    DataClassSingleNum datumIdx = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                    // have to identify if they are equal or not because 0.9999999999999999999 should be equal to 1
                    while (!MFPNumeric.isEqual(datumIdx.getDataValue(), datumPowerInt.getDataValue())
                            && datumIdx.getDataValue().compareTo(datumPowerInt.getDataValue()) > 0) {
                        datumReturnNum = evaluateDivision(datumReturnNum, datumBase);
                        datumIdx = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, datumIdx.getDataValue().subtract(MFPNumeric.ONE));
                    }
                }
                if (bNotReturnList == false)    {   //number of returned root must be one..
                    DataClass[] arrayRoots = new DataClass[1];
                    arrayRoots[0] = datumReturnNum;
                    datumReturnNum = new DataClassArray(arrayRoots);
                }
            }
        } else  {
            // power of a complex number
            DataClassComplex datumBase = DCHelper.lightCvtOrRetDCComplex(datumBaseOperand);
            MFPNumeric mfpNumRealBase = datumBase.getReal();
            MFPNumeric mfpNumImageBase = datumBase.getImage();
            if (datumPower.isEqual(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO)))    {
                // x**0 is always 1.
                datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                if (bNotReturnList == false)    {   //number of returned root must be one..
                    DataClass[] arrayRoots = new DataClass[1];
                    arrayRoots[0] = datumReturnNum;
                    datumReturnNum = new DataClassArray(arrayRoots);
                }
            } else if (mfpNumRealBase.isActuallyZero() && mfpNumImageBase.isActuallyZero())    {
                // if base is zero
                DataClassComplex datumPowerCplx = DCHelper.lightCvtOrRetDCComplex(datumPower);
                if (!datumPowerCplx.getImage().isActuallyZero()) {
                    datumReturnNum = new DataClassComplex(MFPNumeric.NAN, MFPNumeric.NAN);
                } else if (datumPowerCplx.getReal().isActuallyNegative()) {
                    datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.INF);    // zero to a negative value should be infinite.
                } else {
                    datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                }
                if (bNotReturnList == false)    {   //number of returned root must be one..
                    DataClass[] arrayRoots = new DataClass[1];
                    arrayRoots[0] = datumReturnNum;
                    datumReturnNum = new DataClassArray(arrayRoots);
                }
            } else if (bImagePowerMode)    {
                DataClassComplex datumPowerCplx = DCHelper.lightCvtOrRetDCComplex(datumPower);
                MFPNumeric mfpNumRealLogBase = MFPNumeric.log(MFPNumeric.hypot(mfpNumRealBase, mfpNumImageBase));
                MFPNumeric mfpNumImageLogBase = MFPNumeric.atan2(mfpNumImageBase, mfpNumRealBase);
                MFPNumeric mfpNumRealPower = datumPowerCplx.getReal();
                MFPNumeric mfpNumImagePower = datumPowerCplx.getImage();
                MFPNumeric mfpNumRealPart = mfpNumRealLogBase.multiply(mfpNumRealPower).subtract(mfpNumImageLogBase.multiply(mfpNumImagePower));
                MFPNumeric mfpNumImagePart = mfpNumRealLogBase.multiply(mfpNumImagePower).add(mfpNumImageLogBase.multiply(mfpNumRealPower));
                MFPNumeric mfpNumCoeff = MFPNumeric.exp(mfpNumRealPart);
                datumReturnNum = new DataClassComplex(mfpNumCoeff.multiply(MFPNumeric.cos(mfpNumImagePart)), mfpNumCoeff.multiply(MFPNumeric.sin(mfpNumImagePart)));            
                if (bNotReturnList == false)    {   //number of returned root must be one..
                    DataClass[] arrayRoots = new DataClass[1];
                    arrayRoots[0] = datumReturnNum;
                    datumReturnNum = new DataClassArray(arrayRoots);
                }
            } else    {
                MFPNumeric mfpNum2ndParam = DCHelper.lightCvtOrRetDCMFPDec(datumPower).getDataValue();
                MFPNumeric[] mfpNumListRadAngle = datumBase.getComplexRadAngle();
                if (mfpNumListRadAngle[0].isActuallyZero() && mfpNum2ndParam.isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_ANSWER_OF_POWER_FUNCTION_OPERANDS);
                }
                for (int index = 0; index < nNumofReturnedRoots; index++)    {
                    if ((new MFPNumeric(index)).multiply(mfpNum2ndParam).isActuallyInteger() && index != 0)    {
                        nNumofReturnedRoots = index;
                        break;    // have found all the roots
                    }
                    MFPNumeric[] mfpNumListRadAngleRoot = new MFPNumeric[2];
                    mfpNumListRadAngleRoot[0] = MFPNumeric.pow(mfpNumListRadAngle[0], mfpNum2ndParam);
                    if (mfpNumListRadAngle[1].isActuallyZero() && index == 0)   {
                        // base is a positive real number, and we calculate its first root
                        // this avoid to multiply INF and get NAN if mfpNum2ndParam is inf.
                        mfpNumListRadAngleRoot[1] = MFPNumeric.ZERO;
                    } else {
                        mfpNumListRadAngleRoot[1] = mfpNumListRadAngle[1].add(new MFPNumeric(2 * index).multiply(MFPNumeric.PI)).multiply(mfpNum2ndParam);
                    }
                    
                    boolean bImageIsZero = false;
                    boolean bRealIsZero = false;
                    MFPNumeric mfpNumOverPI = MFPNumeric.divide(mfpNumListRadAngleRoot[1], MFPNumeric.PI);
                    if (mfpNumOverPI.isActuallyInteger())    {
                        bImageIsZero = true;
                    }
                    
                    MFPNumeric mfpNumOverHalfPI = MFPNumeric.divide(mfpNumListRadAngleRoot[1], MFPNumeric.PI_OVER_TWO);
                    if (mfpNumOverHalfPI.isActuallyInteger() && !bImageIsZero)    { // if it is integer times of 1/2 PI but not integer times of PI
                        bRealIsZero = true;
                    }
                    
                    if (nNumofReturnedRoots == 1 && bNotReturnList)    {    //return a single root
                        datumReturnNum = new DataClassComplex(mfpNumListRadAngleRoot);
                        if (bImageIsZero)    {
                            datumReturnNum = new DataClassComplex(DCHelper.lightCvtOrRetDCComplex(datumReturnNum).getReal(), MFPNumeric.ZERO);
                        } else if (bRealIsZero) {
                            datumReturnNum = new DataClassComplex(MFPNumeric.ZERO, DCHelper.lightCvtOrRetDCComplex(datumReturnNum).getImage());
                        } else    {
                            if (MFPNumeric.isEqual(DCHelper.lightCvtOrRetDCComplex(datumReturnNum).getImage(), MFPNumeric.ZERO))    {
                                datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, DCHelper.lightCvtOrRetDCComplex(datumReturnNum).getReal());
                            }
                        }
                        break;
                    } else    {    // three parameters, return a root list
                        DataClass datumRoot = new DataClassComplex(mfpNumListRadAngleRoot);
                        if (bImageIsZero)    {
                            datumRoot = new DataClassComplex(DCHelper.lightCvtOrRetDCComplex(datumRoot).getReal(), MFPNumeric.ZERO);
                        } else if (bRealIsZero) {
                            datumRoot = new DataClassComplex(MFPNumeric.ZERO, DCHelper.lightCvtOrRetDCComplex(datumRoot).getImage());
                        } else    {
                            if (DCHelper.lightCvtOrRetDCComplex(datumRoot).getImage().isActuallyZero())    {
                                datumRoot = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, DCHelper.lightCvtOrRetDCComplex(datumRoot).getReal());
                            }
                        }
                        DataClass datumZero = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                        // note that we assign data at index by ref because datumRoot was newly created and it does not have any
                        // connections with previous data.
                        datumReturnNum = DCHelper.lightCvtOrRetDCArrayNoExcept(datumReturnNum).createDataAt1stLvlIdxByRef(index, datumRoot, datumZero);
                        // no need to validate datumReturnNum to avoid cross reference coz datumRoot is created independently.
                    }
                }
            }
        }
        return datumReturnNum;
    }
    
    public static DataClass createEyeMatrix(int nSize, int nDim) throws JFCALCExpErrException {
        DataClass datumReturn = new DataClassArray(new DataClass[0]);
        if (nSize == 0)    {
            // if size is 0, whatever nDim is, return 1
            datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE);
        } else    {
            int[] narrayDims = new int[nDim];
            for (int idx = 0; idx < nDim; idx ++)   {
                narrayDims[idx] = nSize;
            }
            datumReturn = DCHelper.lightCvtOrRetDCArray(datumReturn)
                    .populateDataArray(narrayDims, false);
            for (int idxSize = 0; idxSize < nSize; idxSize ++)   {
                DataClass datumTmp = datumReturn;
                for (int idxDim = 0; idxDim < nDim; idxDim ++)  {
                    if (!datumTmp.isPrimitiveOrArray()) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_TYPE);
                    }
                    if (idxDim == nDim - 1) {
                        DataClassArray datumTmpArray = DCHelper.lightCvtOrRetDCArray(datumTmp);
                        datumTmpArray.getDataList()[idxSize]
                                = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE);
                        datumTmp = datumTmpArray;
                    } else {
                        datumTmp = DCHelper.lightCvtOrRetDCArray(datumTmp).getDataList()[idxSize];
                    }
                }
            }
        }
        return datumReturn;
    }

    public static DataClass createUniValueMatrix(int[] nlistSizes, DataClass datumUniValue) throws JFCALCExpErrException {
        // assume all nlistSizes members are positive integer.
        DataClass datumReturn = new DataClassArray(new DataClass[0]);
        if (nlistSizes == null || nlistSizes.length == 0)    {
            // return a single value
            datumReturn = datumUniValue.cloneSelf();
        } else    {
            // here we have checked the size of return value and it is an array.
            datumReturn = DCHelper.lightCvtOrRetDCArray(datumReturn).populateDataArray(nlistSizes, false);
            int[] nlistSizesChild = new int[nlistSizes.length - 1];
            for (int idx = 0; idx < nlistSizes.length - 1; idx ++)  {
                nlistSizesChild[idx] = nlistSizes[idx + 1];
            }
            for (int idxSize = 0; idxSize < nlistSizes[0]; idxSize ++)   {
                if (nlistSizes.length > 1)   {
                    DCHelper.lightCvtOrRetDCArray(datumReturn).getDataList()[idxSize] = createUniValueMatrix(nlistSizesChild, datumUniValue);
                } else  {
                    DataClass datumRetValue = datumUniValue.cloneSelf();
                    DCHelper.lightCvtOrRetDCArray(datumReturn).getDataList()[idxSize] = datumRetValue;
                }
            }
        }
        return datumReturn;
    }
    
    public static boolean includesAbnormalValues(DataClass datum, int nSearchMode) throws JFCALCExpErrException {
        // nSearchMode == 1 means search for NULL
        // nSearchMode == 2 means search for NAN
        // nSearchMode == 4 means search for +INF
        // nSearchMode == 8 means search for -INF
        boolean bSearchNULL = (nSearchMode & 1) == 1;
        boolean bSearchNan = (nSearchMode & 2) == 2;
        boolean bSearchPosInf = (nSearchMode & 4) == 4;
        boolean bSearchNegInf = (nSearchMode & 8) == 8;
        
        if (!(bSearchNULL || bSearchNan || bSearchPosInf || bSearchNegInf)) {
            // nothing to look for
            return true;
        }
        boolean bReturn = false;
        if (datum.getThisOrNull() instanceof DataClassComplex || datum.getThisOrNull() instanceof DataClassArray) {
            DataClass[] datumList = new DataClass[0];
            if (datum.getThisOrNull() instanceof DataClassComplex) {
                DataClassComplex datumCplx = DCHelper.lightCvtOrRetDCComplex(datum);
                datumList = new DataClass[] {
                                        datumCplx.getRealDataClass(),
                                        datumCplx.getImageDataClass()
                                        };
            } else { // array
                datumList = DCHelper.lightCvtOrRetDCArray(datum).getDataList();
            }
            if (datumList != null)    {
                for (int idx = 0; idx < datumList.length; idx ++)    {
                    bReturn |= includesAbnormalValues(datumList[idx], nSearchMode);
                    if (bReturn)    {
                        break;  // find!
                    }
                }
            }
        } else if (DCHelper.isDataClassType(datum, DATATYPES.DATUM_MFPDEC, DATATYPES.DATUM_MFPINT)) {
            if (bSearchNan && DCHelper.lightCvtOrRetDCSingleNum(datum).getDataValue().isNan()) {
                bReturn = true;
            }
            if (bSearchPosInf && DCHelper.lightCvtOrRetDCSingleNum(datum).getDataValue().isPosInf()) {
                bReturn = true;
            }
            if (bSearchNegInf && DCHelper.lightCvtOrRetDCSingleNum(datum).getDataValue().isNegInf()) {
                bReturn = true;
            }
        } else if (datum.isNull() && bSearchNULL) {
            bReturn = true;
        }
        return bReturn;
    }
}
    
