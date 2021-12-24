// MFP project, DataClassArray.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DCHelper.DCUncopibleFeatures;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import java.util.Arrays;

/**
 * This class caters reference of array, and of course null.
 * Note that the reference itself is immutable. However, array's elements may change.
 * A design standard is, for extended class type, it is treated as array if DATATYPE
 * is unknown.
 * TODO: should this class be final?
 * @author tony
 *
 */
public class DataClassArray extends DataClass {
	private static final long serialVersionUID = 1L;
	private DATATYPES menumDataType = DATATYPES.DATUM_NULL;   
    private DataClass[] mdataList = new DataClass[0];    // this is for ref_data
    private String melemFullTypeName = "::mfp::lang::element";

    public DCUncopibleFeatures mdcUncopibleFeature = new DCUncopibleFeatures();
    
    @Override
    public String getTypeName() {
        if (DCHelper.isDataClassType(getDataClassType(), DATATYPES.DATUM_NULL)) {
            return "null";
        } else if (DCHelper.isDataClassType(getDataClassType(), DATATYPES.DATUM_REF_DATA)) {
            return "array[" + mdataList.length + "](" + melemFullTypeName.substring(melemFullTypeName.lastIndexOf(":")) + ")";
        } else {
            return "array[" + mdataList.length + "](" + melemFullTypeName.substring(melemFullTypeName.lastIndexOf(":")) + ")";
        }
    }
    
    @Override
    public String getTypeFullName() {
        if (DCHelper.isDataClassType(getDataClassType(), DATATYPES.DATUM_NULL)) {
            return "::mfp::lang::null";
        } else if (DCHelper.isDataClassType(getDataClassType(), DATATYPES.DATUM_REF_DATA)) {
            return "::mfp::lang::array[" + mdataList.length + "](" + melemFullTypeName + ")";
        } else {
            return "::mfp::lang::array[" + mdataList.length + "](" + melemFullTypeName + ")";
        }
    }
        
    public DataClassArray()   {
        super();
    }    

    public DataClassArray(DataClass[] dataList) throws JFCALCExpErrException    {
        super();
        setDataList(dataList);
    }
    
    private void setDataList(DataClass[] dataList) throws JFCALCExpErrException    {
        menumDataType = DATATYPES.DATUM_REF_DATA;
        if (dataList == null)    {
            dataList = new DataClass[0];
        }
        mdataList = dataList;
        validateDataClass();
    }

    public boolean isNumericalData(boolean bLookOnNullAsZero) throws JFCALCExpErrException {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return bLookOnNullAsZero;
        } else  {
            // data_ref type
            for (int idx = 0; idx < getDataListSize(); idx ++)  {
                if (getDataList()[idx] == null)    {
                    getDataList()[idx] = new DataClassNull();  // we initialize it to null.
                }
                if (!DCHelper.isNumericalData(getDataList()[idx], bLookOnNullAsZero)) {
                    return false;
                }
            }
            return true;
        }
    }
    
    @Override
    public void validateDataClass_Step1() throws JFCALCExpErrException    {
        super.validateDataClass_Step1();
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            mdataList = new DataClass[0];
        } else    {
            if (mdcUncopibleFeature.mnValidatedCnt > DCHelper.MAX_REPEATING_VALIDATION_CNT_ALLOWED) {
                //it has been validated many times. This implies a recursive reference. So throw an error.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MATRIX_CANNOT_RECURSIVELY_REFERRED);
            }
            mdcUncopibleFeature.mnValidatedCnt ++;
            if (mdataList != null)    {
                for (int i = 0; i < mdataList.length; i++)    {
                    if (mdataList[i] != null)    {
                        mdataList[i].validateDataClass_Step1();    // recursive calling
                    }
                }
            } else    {
                mdataList = new DataClass[0];
            }
        }
    }
    
    @Override
    public void validateDataClass_Step2()    {
        super.validateDataClass_Step2();
        if (!DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            mdcUncopibleFeature.mnValidatedCnt = 0;
            if (mdataList != null)    {
                for (int i = 0; i < mdataList.length; i++)    {
                    if (mdataList[i] instanceof DataClassArray)    {    
                        mdataList[i].validateDataClass_Step2();    // recursive calling
                    }
                }
            }
        }
    }
    
    @Override
    public DATATYPES getDataClassType()    {
        return menumDataType;
    }
    
    public DataClass[] getDataList() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)){
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        }
        return mdataList;
    }

    /**
     * AllocDataArray resets this dataclass and allocates a new data array for this.
     * Everything is initialized to null. This function is final private which means
     * it works like a constructor but without validation. Therefore, It will access
     * mdataList directly.
     * @param nListArraySize
     * @param datumDefault
     * @throws JFCALCExpErrException
     */
    private void allocDataArray(int[] nListArraySize, DataClass datumDefault) throws JFCALCExpErrException    {
        if (nListArraySize == null || nListArraySize.length == 0 || nListArraySize[0] < 0)    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
        } else    {
            menumDataType = DATATYPES.DATUM_REF_DATA;
            mdataList = new DataClass[nListArraySize[0]];
            if (nListArraySize.length == 1)    {
                for (int index = 0; index < nListArraySize[0]; index ++)    {
                    mdataList[index] = datumDefault.cloneSelf();
                }
            } else {
                for (int index = 0; index < nListArraySize[0]; index ++)    {
                    mdataList[index] = new DataClassArray();
                    int[] nListSubArraySize = new int[nListArraySize.length-1];
                    for (int index1 = 1; index1 < nListArraySize.length; index1 ++)    {
                        nListSubArraySize[index1 - 1] = nListArraySize[index1];
                    }
                    ((DataClassArray)mdataList[index]).allocDataArray(nListSubArraySize, datumDefault);
                }
            }
        }
    }

    /**
     * This function is the external version of allocDataArray, note that it is static.
     * @param nListArraySize
     * @param datumDefault
     * @return new data class.
     * @throws com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException 
     */
    public static DataClass constructDataArray(int[] nListArraySize, DataClass datumDefault) throws JFCALCExpErrException  
    {
        DataClassArray datum = new DataClassArray();
        datum.allocDataArray(nListArraySize, datumDefault);   // the content of datum will be destroyed anyway.
        return datum;
    }
    
    public int getDataListSize() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);
        } else    {
            return mdataList.length;
        }
    }
    
    @Override
    public int[] recalcDataArraySize() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            // if it is not an array, but a null;
            return new int[0];
        }
        int nVectorLength = mdataList.length;
        int[][] vectorLens = new int[nVectorLength][];
        int nMaxDim = 0;
        for (int index = 0; index < nVectorLength; index ++)    {
            if (mdataList[index] instanceof DataClassArray)    {
                vectorLens[index] = ((DataClassArray)mdataList[index]).recalcDataArraySize();
            } else {
                vectorLens[index] = new int[0];
            }
            if (nMaxDim < vectorLens[index].length)    {
                nMaxDim = vectorLens[index].length;
            }
        }
        int[] nArraySize = new int[nMaxDim + 1];
        nArraySize[0] = nVectorLength;
        for (int index = 1; index < nMaxDim + 1; index++)    {
            int nMaxVectorLen = 0;
            for (int index1 = 0; index1 < nVectorLength; index1 ++)    {
                if (vectorLens[index1].length >= index)    {
                    if (nMaxVectorLen < vectorLens[index1][index - 1])    {
                        nMaxVectorLen = vectorLens[index1][index - 1];
                    }
                }
            }
            nArraySize[index] = nMaxVectorLen;
        }
        return nArraySize;
    }
    
    /**
     * This function wont change the content of a DataClassArray. It likes an array copy and
     * it will access mdataList directly. note that this function will return a dataclass with
     * a different mdataList but elements inside mdataList wont change. Also, no validation is
     * required after calling this function because it is simply populate zeros. The original
     * array will not be affected but the returned array may share elements with the original
     * array.
     * @param narrayDims
     * @param bCvtNull2Zero
     * @throws JFCALCExpErrException
     * @return DataClass : an array-copy and populated reference of this.
     */
    public DataClass populateDataArray(int[] narrayDims, boolean bCvtNull2Zero) throws JFCALCExpErrException    {
        // populate a data array. If no value or null value in a cell, put 0 in it if bCvtNull2Zero is true.
        // assume the parameter narrayDims is not null.
        if (narrayDims == null)    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
        } else if (narrayDims.length == 0)    {
            if (!DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {    // if it is an array, it cannot be populated to single number
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
            } else {    // if it is a single value, and we want it be populated to a single value, just return itself.
                return this;
            }
        } else if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) { // if this is null, and we want to populate it.
        	DataClassArray datum = new DataClassArray();
        	// the content of datum will be destroyed anyway.
            datum.allocDataArray(narrayDims, new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO));
            // now datum has been fully populated and every element is 0.
            if (!bCvtNull2Zero) {
            	// we don't what to convert null to zero, so revert the first 0 to null.
            	DataClassArray datum2Revert = datum;
            	int idx = 0;
            	for (; idx < narrayDims.length - 1; idx ++) {
            		if (narrayDims[idx] > 0) {
            			// datum2Revert.mdataList[0] must be an array, so we simply convert to itself.
            			datum2Revert = DCHelper.lightCvtOrRetDCArray(datum2Revert.mdataList[0]);
            		}
            	}
            	if (idx == narrayDims.length - 1 && narrayDims[idx] > 0) {
            		datum2Revert.mdataList[0] = new DataClassNull();
            	}
            }
            return datum;
        }
        
        int nThisDimSize = getDataListSize();
        if (nThisDimSize > narrayDims[0])    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
        }
        
        if (narrayDims[0] == 0) {
        	// This original array is an empty array so we return an empty array.
            return new DataClassArray(new DataClass[0]);
        } else {    //we have checked narrayDims.length == 0 before, so here narrayDims.length >= 1
            int[] narrayDimsNext = new int[narrayDims.length - 1];
            for (int index = 0; index < narrayDims.length - 1; index ++)    {
                narrayDimsNext[index] = narrayDims[index + 1];
            }
            
            DataClass[] dataList = new DataClass[narrayDims[0]];
            for (int index = 0; index < narrayDims[0]; index ++)    {
                boolean bIsGeneratedValue = true;
                dataList[index] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO);
                if (index < nThisDimSize)    {
                    if ((!bCvtNull2Zero) || !DCHelper.isDataClassType(mdataList[index], DATATYPES.DATUM_NULL))    {
                        dataList[index] = mdataList[index];
                        bIsGeneratedValue = false; // this value is copied from original value. It is not just generated.
                    }
                }
                if (narrayDimsNext.length > 0)    {
                    if (!(dataList[index] instanceof DataClassArray)) {
                        // convert dataList[index] to an array if it is not an array.
                        if (bIsGeneratedValue) {
                            // if it is just generated value, we convert it to a zero size array because the next
                            // dim size can be zero. If the next dim size is zero, we cannot convert it to a non-
                            // zero size array.
                            dataList[index] = new DataClassArray(new DataClass[0]);
                        } else {
                            dataList[index] = new DataClassArray(new DataClass[] {dataList[index]});
                        }
                    }
                    dataList[index] = ((DataClassArray)dataList[index]).populateDataArray(narrayDimsNext, bCvtNull2Zero);
                }
            }
            return new DataClassArray(dataList);
        }
    }

    /**
     * deep copy version of populateDataArray. It returns a new reference of fullfilled data array.
     * @param narrayDims
     * @param bCvtNull2Zero
     * @return
     * @throws JFCALCExpErrException
     */
    public DataClass fullfillDataArray(int[] narrayDims, boolean bCvtNull2Zero) throws JFCALCExpErrException  
    {
        DataClassArray datumCp = (DataClassArray)cloneSelf();
        return datumCp.populateDataArray(narrayDims, bCvtNull2Zero);
    }
    
    /**
     * return DataClass reference
     * @param indexList
     * @return
     * @throws JFCALCExpErrException
     */
    public DataClass getDataAtIndexByRef(int[] indexList) throws JFCALCExpErrException    {
        DataClass datumCurrent = this;
        for (int nIndex = 0; nIndex < indexList.length; nIndex ++)    {
            if (indexList[nIndex] < 0)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
            } else if (!(datumCurrent instanceof DataClassArray)
                    || DCHelper.isDataClassType(datumCurrent, DATATYPES.DATUM_NULL))    {
                // a non-array data cannot be converted to x[0,0,0,0,0...] implicitly.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
            } else    {    // must be an array now.
                if (indexList[nIndex] >= ((DataClassArray)datumCurrent).mdataList.length)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
                } else    {
                    datumCurrent = ((DataClassArray)datumCurrent).mdataList[indexList[nIndex]];
                }
            }
        }
        return datumCurrent;
    }
    
    /**
     * return DataClass value
     * @param indexList
     * @return
     * @throws JFCALCExpErrException
     */
    public DataClass getDataAtIndex(int[] indexList) throws JFCALCExpErrException    {
        DataClass datumReturn = getDataAtIndexByRef(indexList).cloneSelf();
        return datumReturn;
    }
    
    /**
     * assign data class reference, not data value. Note that this class is protected because
     * the dataclass must be datum_ref_data, so no data type change is needed.
     * and this function will not resize the mdataList.
     * Note that this function does not validate data class after setting. This is the reason
     * the function is PROTECTED not public (because the function is only called by setDataAtIndex
     * which use a deep copy of datum, no validation is required).
     * @param indexList
     * @param datum
     * @throws JFCALCExpErrException
     */
    protected void setDataAtIndexByRef(int[] indexList, DataClass datum) throws JFCALCExpErrException    {
    	// Note that this function is PROTECTED because there is no validation!!!!
        DataClass datumCurrent = this;
        for (int nIndex = 0; nIndex < indexList.length; nIndex ++)    {
            if (indexList[nIndex] < 0)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
            } else if (!(datumCurrent instanceof DataClassArray)
                    || DCHelper.isDataClassType(datumCurrent, DATATYPES.DATUM_NULL))    {
                // a non-array data cannot be converted to x[0,0,0,0,0...] implicitly.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
            } else    {
                if (indexList[nIndex] >= ((DataClassArray)datumCurrent).mdataList.length)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
                } else if (nIndex < indexList.length - 1)    {
                    datumCurrent = ((DataClassArray)datumCurrent).mdataList[indexList[nIndex]];
                } else    {
                    // assign data reference
                    ((DataClassArray)datumCurrent).mdataList[indexList[nIndex]] = datum;
                }
            }
        }
        // do not validate because this function is mainly used for internal calculation
        // like swap rows or columns will throw error during swapping.
        // validateDataClass();
    }
    
    /**
     * This is public version of setValidDataAtIndexByRef. It is with validation. If validation fails,
     * it first roll back and then throws an exception.
     * @param indexList
     * @param datum
     * @throws JFCALCExpErrException
     */
    public void setValidDataAtIndexByRef(int[] indexList, DataClass datum) throws JFCALCExpErrException    {
        DataClass datumOriginal = getDataAtIndexByRef(indexList);
        setDataAtIndexByRef(indexList, datum);
        try {
            validateDataClass();
        } catch (JFCALCExpErrException e) {
            // if fail to set, restore, otherwise, incorrect dataclass will cause trouble later on.
            // only restore when set data list cause recursive referring may cause stack overflow.
            setDataAtIndexByRef(indexList, datumOriginal);    
            validateDataClass_Step2();  // reset nvalidatedcnt.
            throw e;
        }
        setDataAtIndexByRef(indexList, datum);
        validateDataClass();
    }
    
    /**
     * different from SetDataAtIndexByRef, this function will create data
     * if the array index is beyond its range.
     * This function do not validate the dataclass. It returns an array copy of this.
     * The original array will not be affected although it shares some elements with
     * the returned array.
     * @param indexList
     * @param datum : the data to be set at index
     * @param datumNewByDefault : in order to set datum, we may need to create some elements to fill
     * the array, the value of the elements would be datumNewByDefault.
     * @throws JFCALCExpErrException
     */
    protected DataClass createDataAtIndexByRef(int[] indexList, DataClass datum, DataClass datumNewByDefault) throws JFCALCExpErrException    {
        // zero dim means doing nothing.
    	for (int nIndex = 0; nIndex < indexList.length; nIndex ++)    {
            if (indexList[nIndex] < 0)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
            }
        }
        DataClassArray datum2Return = this;	// we may return this or an array-copied reference of this.
        DataClassArray datumCurrent = this;    // now datumCurrent is a DataClassArray
        DataClassArray datumLast = null;
        if (isNull()) {
        	// if it is null, turn it to an empty array. This is consistent with function
            // DCHelper.lightCvtOrRetDCArrayNoExcept which also returns an empty array when convert a
            // null to an array.
        	datum2Return = datumCurrent = new DataClassArray(new DataClass[0]);
        }
        for (int nIndex = 0; nIndex < indexList.length; nIndex ++)    {
            if (datumCurrent.mdataList.length <= indexList[nIndex]) {
                DataClass[] dataList = new DataClass[indexList[nIndex] + 1];
                for (int nIndex2 = 0; nIndex2 <= indexList[nIndex]; nIndex2 ++)    {
                    if (nIndex2 < datumCurrent.mdataList.length)    {
                        dataList[nIndex2] = datumCurrent.mdataList[nIndex2];
                    } else if (nIndex2 < indexList[nIndex])    {
                        dataList[nIndex2] = datumNewByDefault.cloneSelf();// Have to initialize the data to datumNewByDefault, otherwise, null will leads to problem in PopulateDataArray.
                    } else    {
                        dataList[nIndex2] = new DataClassArray(new DataClass[0]);
                    }
                }
                datumCurrent = new DataClassArray(dataList);
                if (nIndex == 0) {
                	datum2Return = datumCurrent;
                } else {
                	datumLast.mdataList[indexList[nIndex - 1]] = datumCurrent;	// update reference.
                }
            }
            // the index now is in the valid range.
            datumLast = datumCurrent;
            if (nIndex < indexList.length - 1)    {
                if (datumCurrent.mdataList[indexList[nIndex]] != null
                        && datumCurrent.mdataList[indexList[nIndex]].getThisOrNull() instanceof DataClassArray) {
                    datumCurrent = (DataClassArray)datumCurrent.mdataList[indexList[nIndex]];
                } else if (datumCurrent.mdataList[indexList[nIndex]] != null
                        && !datumCurrent.mdataList[indexList[nIndex]].isNull()) {
                    // this is not a DataClassArray, and it is not the last index. Note that datumCurrent is
                    // set a new value and we have to change the original value to a one element dataClassArray.
                    // and the first element is its original value. Note that condition is its original value
                    // is not Null. If it is null, turn it to an empty array.
                	datumLast.mdataList[indexList[nIndex]] = datumCurrent
                            = new DataClassArray(new DataClass[] {
                                    datumCurrent.mdataList[indexList[nIndex]]
                            });
                } else {
                    // if it is null, turn it to an empty array. This is consistent with function
                    // DCHelper.lightCvtOrRetDCArrayNoExcept which also returns an empty array when convert a
                    // null to an array.
                	datumLast.mdataList[indexList[nIndex]] = datumCurrent = new DataClassArray(new DataClass[0]);
                }
            } else    {
                // now we have arrived at the last index.
                datumCurrent.mdataList[indexList[nIndex]] = datum;
                // break the for loop and exit the function here.
            }
        }
        
        return datum2Return;
    }
    
    /**
     * This is the public version of createDataAtIndexByRef. It will validate the returned value.
     * @param indexList
     * @param datum
     * @param datumNewByDefault
     * @return
     * @throws JFCALCExpErrException
     */
    public DataClass createValidDataAtIndexByRef(int[] indexList, DataClass datum, DataClass datumNewByDefault) throws JFCALCExpErrException    {
        DataClass datum2Return = createDataAtIndexByRef(indexList, datum, datumNewByDefault);
        datum2Return.validateDataClass();
        return datum2Return;
    }

    /**
     * this function is the public version of createDataAtIndexByRef. This function will not change this.
     * @param indexList
     * @param datum
     * @param datumNewByDefault
     * @return
     * @throws JFCALCExpErrException
     */
    public DataClass assignDataAtIndexByRef(int[] indexList, DataClass datum, DataClass datumNewByDefault) throws JFCALCExpErrException    {
        DataClassArray datumCp = (DataClassArray)cloneSelf();   // have to deep copy.
        return datumCp.createDataAtIndexByRef(indexList, datum, datumNewByDefault);
        // no need to validate datumCp because datumCp is a deep copied version, it should not refer to
        // any element of datum, and datum should not refer to any element of datumCp.
    }
    
    /**
     * This function set/append data to a new index and create a new dataclass array. Note that
     * 1. the appended data is not a clone but a reference;
     * 2. the returned dataclass array is different from this, however, they share elements except that the added element;
     * 3. the index is a single value index;
     * 4. if index is less than this.length, replacing the exising element, if index larger or equal to this.length,
     * append datum to the returned dataclass array. The empty elements between index and this.length will be set as
     * datumNewByDefault.
     * 5. this function will validataDataClass
     * @param index
     * @param datum
     * @param datumNewByDefault
     * @return 
     */
    public DataClass createDataAt1stLvlIdxByRef(int index, DataClass datum, DataClass datumNewByDefault) throws JFCALCExpErrException {
        DataClass datumReturn = null;
        if (index < 0)    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
        } else if (isNull()) {
            // if it is null, turn it to an empty array. This is consistent with function
            // DCHelper.lightCvtOrRetDCArrayNoExcept which also returns an empty array when convert a
            // null to an array.
            DataClass[] dataList = new DataClass[index + 1];
            for (int idx = 0; idx < index; idx ++) {
                dataList[idx] = datumNewByDefault.cloneSelf();
            }
            dataList[index] = datum;    // using reference.
            datumReturn = new DataClassArray(dataList);
        } else {
            // mdataList shouldn't be null.
            if (mdataList.length <= index) {
                // ok, this is a new element.
                DataClass[] dataList = new DataClass[index + 1];
                System.arraycopy(mdataList, 0, dataList, 0, mdataList.length);
                for (int idx = mdataList.length; idx < index; idx ++) {
                    dataList[idx] = datumNewByDefault.cloneSelf();
                }
                dataList[index] = datum;    // using reference.
                datumReturn = new DataClassArray(dataList);
            } else {
                // this is replacing existing element
                DataClass[] dataList = new DataClass[mdataList.length];
                System.arraycopy(mdataList, 0, dataList, 0, mdataList.length);
                dataList[index] = datum;
                datumReturn = new DataClassArray(dataList);
            }
        }
        datumReturn.validateDataClass();
        return datumReturn;
    }

    /**
     * set datumValue to each LEAF child.
     * @param datumValue
     * @throws JFCALCExpErrException
     */
    public void setArrayAllLeafChildren(DataClass datumValue) throws JFCALCExpErrException    {
        if (isNull()) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ASSIGN_VALUE_TO_A_NULL_DATA_REFERENCE);
        } else if (mdataList != null)    {
            for (int idx = 0; idx < mdataList.length; idx ++)    {
                if (mdataList[idx] != null && mdataList[idx].getThisOrNull() instanceof DataClassArray) {
                    DataClassArray datumElem = DCHelper.lightCvtOrRetDCArray(mdataList[idx]);
                    datumElem.setArrayAllLeafChildren(datumValue);
                    mdataList[idx] = datumElem;
                } else {
                    mdataList[idx] = datumValue.cloneSelf();
                }
            }
        }
    }

    /**
     * convert data to a new type and returns a new data reference which is a light copy.
     * of the original data. Note that this function guarantees that, for a data array, its
     * original elements will not change.
     */
    @Override
    public DataClass convertData2NewType(DATATYPES enumNewDataType) throws JFCALCExpErrException
    {
        if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_BASE_OBJECT)) {
            return copySelf();    // we just return a light copy of itself, even for an array.
        } else if (DCHelper.isDataClassType(this, enumNewDataType)) {    // src data type is the same as dest
            return copySelf();    // we just return a light copy of itself, even for an array.
        } else if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_NULL)) {
            // source data type must be different from null, so throw exception.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_ANY_OTHER_DATATYPE_TO_NONEXIST);
        } else if (!DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            // it is array.
            // note that we allow the conversion from a multi-dimentional 1 element array to a
            // single value only if the single value is numerical. This is to support matrix
            // multiplication. In otherwords, both [x,x,x,x] *[x] and [x,x,x,x] * [[x]] are valid.
            if (DCHelper.isDataClassType(enumNewDataType, DATATYPES.DATUM_MFPBOOL,
                    DATATYPES.DATUM_MFPINT, DATATYPES.DATUM_MFPDEC, DATATYPES.DATUM_COMPLEX)) {
                /* complex should not be looked on as a 2-element data reference. complex number is a single number */
                DataClass datum2Check = this;
                while (datum2Check instanceof DataClassArray) {
                    if (DCHelper.isDataClassType(datum2Check, DATATYPES.DATUM_NULL)
                            || ((DataClassArray)datum2Check).getDataList() == null
                            || ((DataClassArray)datum2Check).getDataListSize() != 1)    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_DATA_REFERENCE);
                    }
                    datum2Check = ((DataClassArray)datum2Check).getDataList()[0];
                }
                // ok, now we have arrived at the deepest level.
                DataClass datumCpy = datum2Check.convertData2NewType(enumNewDataType);    // recursive calling.
                return datumCpy;
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
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_DATA_REFERENCE);    // will not be here.
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
            if (datum instanceof DataClassArray && ((DataClassArray)datum).getDataListSize() == getDataListSize())    {
                if (getDataListSize() == 0)    {
                    return true;    // both of the data lists are empty.
                }
                boolean bEqualDataList = true;
                for (int index = 0; index < getDataListSize(); index++)    {
                    // recursive compare
                    if (getDataList()[index] == null && ((DataClassArray)datum).getDataList()[index] == null) {
                        continue;
                    } else if (getDataList()[index] == null) {
                        if (!((DataClassArray)datum).getDataList()[index].isNull()) {
                            bEqualDataList = false;
                            break;
                        }
                    } else if (((DataClassArray)datum).getDataList()[index] == null) {
                        if (!getDataList()[index].isNull()) {
                            bEqualDataList = false;
                            break;
                        }
                    } else if (!DCHelper.isEqual(getDataList()[index], ((DataClassArray)datum).getDataList()[index], errorScale))    {
                        bEqualDataList = false;
                        break;
                    }
                }
                return bEqualDataList;
            }
        }
        return false;
    }
    
    @Override
    public boolean isEqual(DataClass datum) throws JFCALCExpErrException
    {
        return isEqual(datum, 1);
    }
    
    @Override
    public boolean isBitEqual(DataClass datum) throws JFCALCExpErrException {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            if (DCHelper.isDataClassType(datum, DATATYPES.DATUM_NULL))    {
                return true;
            }
        } else if (!DCHelper.isDataClassType(datum, DATATYPES.DATUM_NULL))   {
            if (datum instanceof DataClassArray && ((DataClassArray)datum).getDataListSize() == getDataListSize())    {
                if (getDataListSize() == 0)    {
                    return true;    // both of the data lists are empty.
                }
                boolean bEqualDataList = true;
                for (int index = 0; index < getDataListSize(); index++)    {
                    // recursive compare
                    if (getDataList()[index] == null && ((DataClassArray)datum).getDataList()[index] == null) {
                        continue;
                    } else if (getDataList()[index] == null) {
                        if (!((DataClassArray)datum).getDataList()[index].isNull()) {
                            bEqualDataList = false;
                            break;
                        }
                    } else if (((DataClassArray)datum).getDataList()[index] == null) {
                        if (!getDataList()[index].isNull()) {
                            bEqualDataList = false;
                            break;
                        }
                    } else if (!DCHelper.isBitEqual(getDataList()[index], ((DataClassArray)datum).getDataList()[index]))    {
                        bEqualDataList = false;
                        break;
                    }
                }
                return bEqualDataList;
            }
        }
        return false;
    }
    
    /**
     * This function identify if a data value is zero (false is zero) or a data array is full-of-zero array
     * @param bExplicitNullIsZero
     * @return
     * @throws JFCALCExpErrException
     */
    public boolean isZeros(boolean bExplicitNullIsZero) throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL))    {
            return bExplicitNullIsZero;
        } else    {
            if (mdataList == null)    {
                mdataList = new DataClass[0];
            }
            for (int idx = 0; idx < getDataListSize(); idx ++)    {
                if (getDataList()[idx] == null)    {
                    continue;
                } else if (!DCHelper.isZeros(getDataList()[idx], bExplicitNullIsZero))    {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * This function identify if a data value is I or 1 or [1].
     * @param bExplicitNullIsZero
     * @return
     * @throws JFCALCExpErrException
     */
    public boolean isEye(boolean bExplicitNullIsZero) throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return false;
        } else {
            DataClassSingleNum datumOne = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
            int[] narrayDims = recalcDataArraySize();
            if (narrayDims.length == 0)    {
                return isEqual(datumOne);    // this actually will not happen.
            } else     {
                for (int idx = 0; idx < narrayDims.length; idx ++)    {
                    if (narrayDims[idx] != narrayDims[0])    {
                        return false;
                    }
                }
                
                // check if all the should be one elements are one. We have to use fullfillDataArray coz we may change its content.
                DataClassArray datumCopy = (DataClassArray)fullfillDataArray(narrayDims, bExplicitNullIsZero);
                for (int idxSize = 0; idxSize < narrayDims[0]; idxSize ++)   {
                    DataClass datumTmp = datumCopy;
                    DataClass datumIElement = null;
                    for (int idxDim = 0; idxDim < narrayDims.length; idxDim ++)  {
                        if (!(datumTmp instanceof DataClassArray)
                                || DCHelper.isDataClassType(datumTmp, DATATYPES.DATUM_NULL)) {
                            // if it is not DataClassArray, definitely it is not Eye. Actually this will not
                            // happen because the array has been fully populated.
                            return false;
                        } else if (((DataClassArray)datumTmp).getDataList() == null
                                || ((DataClassArray)datumTmp).getDataList().length <= idxSize)    {
                            return false;    // does not include the element.
                        } else if (idxDim < narrayDims.length - 1){
                            datumTmp = ((DataClassArray)datumTmp).getDataList()[idxSize];
                        } else {    // now get I element.
                            datumIElement = ((DataClassArray)datumTmp).getDataList()[idxSize];
                        }
                    }
                    if (datumIElement.isEqual(datumOne) == false)    {
                        return false;
                    } else    {
                        ((DataClassArray)datumTmp).getDataList()[idxSize]
                                = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);    // convert 1 to zero
                    }
                }
                // after convert all one to zero, it should be a zero array
                return datumCopy.isZeros(bExplicitNullIsZero);
            }
        }
    }
    
    /**
     * copy itself (light copy) and return a new reference.
     * To enable extended type compatibility, use
     * new DataClassArray(getDataList());
     * instead of
     * new DataClassArray(mDataList);
     */
    @Override
    public DataClass copySelf() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return new DataClassArray();
        } else {
            DataClassArray datumReturn = new DataClassArray(getDataList());
            return datumReturn;
        }
    }
    
    /**
     * This function returns an arraycopied reference. Its elements will not change.
     * @return
     * @throws JFCALCExpErrException
     */
    public DataClass copyArray() throws JFCALCExpErrException {
    	if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
    		// note that we cannot return DataClassNull because copyArray needs to return a DataClassArray.
            return new DataClassArray();
        } else {
        	DataClass[] datumList = new DataClass[getDataListSize()];
        	DataClass[] datumOriginalList = getDataList();
        	System.arraycopy(datumOriginalList, 0, datumList, 0, datumList.length);
            DataClassArray datumReturn = new DataClassArray(datumList);
            return datumReturn;
        }
    }
    
    /**
     * copy itself (deep copy) and return a new reference.
     * To enable extended type compatibility, use
     * new DataClassArray(getDataList());
     * instead of
     * new DataClassArray(mDataList);
     */
    @Override
    public DataClass cloneSelf() throws JFCALCExpErrException    {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return new DataClassArray();
        } else {
            if (getDataList() == null)    {
                return new DataClassArray(new DataClass[0]);
            } else {
                DataClass[] dataList = new DataClass[getDataListSize()];
                for (int index0 = 0; index0 < dataList.length; index0 ++)    {
                    dataList[index0] = new DataClassNull();    // set default value to null.
                    if (getDataList()[index0] != null)    {
                        dataList[index0] = getDataList()[index0].cloneSelf();
                    }
                }
                return new DataClassArray(dataList);
            }
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
            strOutput = "[";
            for (int index = 0; index < getDataListSize(); index ++) {
                if (index == (getDataListSize() - 1)) {
                    strOutput += (getDataList()[index] == null)?
                            (new DataClassNull()).output():getDataList()[index].output();
                } else {
                    strOutput += ((getDataList()[index] == null)?
                            (new DataClassNull()).output():getDataList()[index].output()) + ", ";
                }
            }
            strOutput += "]";
        }
        return strOutput;
    }
    
    @Override
    public int getHashCode() throws JFCALCExpErrException {
        if (DCHelper.isDataClassType(this, DATATYPES.DATUM_NULL)) {
            return 0;
        }
        int hashRet = 0;
        for (int idx = 0; idx < mdataList.length; idx ++) {
            hashRet = hashRet * 13 + mdataList[idx].getHashCode();
        }
        return hashRet;
    }
}
