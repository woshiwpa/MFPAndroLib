// MFP project, ArrayBasedList.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfdatastruct;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Oomfp.CitingSpaceDefinition;

public class ArrayBasedList {

    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + ArrayBasedList.class.getName());
        }
    }
	
    /** 
     * get reference (not copy) of the value at idx
     * @param list
     * @param idx
     * @return the reference of the element.
     * @throws JFCALCExpErrException : if the idx is invalid
     */
    public static DataClass getElemFromArrayBasedList(DataClass list, int idx) throws JFCALCExpErrException {
    	DataClassArray arrayList = DCHelper.lightCvtOrRetDCArray(list);
    	return arrayList.getDataAtIndexByRef(new int[] {idx});
    }
    
    /** 
     * set reference (not copy) of the value at idx
     * @param list
     * @param idx
     * @param elem
     * @throws JFCALCExpErrException : if the idx is invalid
     */
    public static void setElemInArrayBasedList(DataClass list, int idx, DataClass elem) throws JFCALCExpErrException {
    	DataClassArray arrayDict = DCHelper.lightCvtOrRetDCArray(list);
    	arrayDict.setValidDataAtIndexByRef(new int[] {idx}, elem);
    }
    
    /** 
     * append a reference of the elem to the tail of list
     * @param list
     * @param elem
     * @return a new list which includes the elem.
     * @throws JFCALCExpErrException
     */
    public static DataClassArray appendElemToArrayBasedList(DataClass list, DataClass elem) throws JFCALCExpErrException {
    	DataClassArray arrayDict = DCHelper.lightCvtOrRetDCArray(list);
    	DataClassArray arrayReturn = DCHelper.lightCvtOrRetDCArray(
    			arrayDict.createDataAt1stLvlIdxByRef(arrayDict.getDataListSize(), elem, new DataClassNull())
    			);
    	return arrayReturn;
    }
    
    /** 
     * concatenate two array based lists
     * @param list1
     * @param list2
     * @return a new list which is the concatenation of the two lists.
     * @throws JFCALCExpErrException
     */
    public static DataClassArray concatArrayBasedLists(DataClass list1, DataClass list2) throws JFCALCExpErrException {
    	DataClassArray arrayDict1 = DCHelper.lightCvtOrRetDCArray(list1);
    	DataClassArray arrayDict2 = DCHelper.lightCvtOrRetDCArray(list2);
    	DataClass[] arrayElems1 = arrayDict1.getDataList();
    	DataClass[] arrayElems2 = arrayDict2.getDataList();
    	DataClass[] arrayElemsNew = new DataClass[arrayElems1.length + arrayElems2.length];
        System.arraycopy(arrayElems1, 0, arrayElemsNew, 0, arrayElems1.length);
        System.arraycopy(arrayElems2, 0, arrayElemsNew, arrayElems1.length, arrayElems2.length);
    	DataClassArray arrayReturn = new DataClassArray(arrayElemsNew);
    	return arrayReturn;
    }

    /** 
     * insert reference (not copy) of the value at idx
     * @param list
     * @param idx
     * @param elem
     * @return the reference of the element.
     * @throws JFCALCExpErrException : if the idx is invalid (idx = 0 to list len (not len - 1))
     */
    public static DataClassArray insertElemIntoArrayBasedList(DataClass list, int idx, DataClass elem) throws JFCALCExpErrException {
    	DataClassArray arrayDict = DCHelper.lightCvtOrRetDCArray(list);
    	if (idx < 0 || idx >= arrayDict.getDataListSize()) {
    		throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
    	}
    	DataClass[] arrayElems = arrayDict.getDataList();
    	DataClass[] arrayElemsNew = new DataClass[arrayElems.length + 1];
    	System.arraycopy(arrayElems, 0, arrayElemsNew, 0, idx);
    	System.arraycopy(arrayElems, idx, arrayElemsNew, idx + 1, arrayElems.length - idx);
    	arrayElemsNew[idx] = elem;
    	DataClassArray arrayReturn = new DataClassArray(arrayElemsNew);
    	return arrayReturn;
    }

    /** 
     * remove reference (not copy) of the value at idx
     * @param list
     * @param idx
     * @return the reference of the element.
     * @throws JFCALCExpErrException : if the idx is invalid (idx = 0 to list len - 1)
     */
    public static DataClassArray removeElemFromArrayBasedList(DataClass list, int idx) throws JFCALCExpErrException {
    	DataClassArray arrayDict = DCHelper.lightCvtOrRetDCArray(list);
    	if (idx < 0 || idx > arrayDict.getDataListSize() - 1) {
    		throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
    	}
    	DataClass[] arrayElems = arrayDict.getDataList();
    	DataClass[] arrayElemsNew = new DataClass[arrayElems.length - 1];
        if (idx > 0) {
            System.arraycopy(arrayElems, 0, arrayElemsNew, 0, idx);
        }
        if (idx < arrayElems.length - 1)  {
            System.arraycopy(arrayElems, idx + 1, arrayElemsNew, idx, arrayElems.length - 1 - idx);
        }
    	DataClassArray arrayReturn = new DataClassArray(arrayElemsNew);
    	return arrayReturn;
    }
    
    public static class Get_elem_from_ablistFunction extends BaseBuiltInFunction {

        public Get_elem_from_ablistFunction() {
            mstrProcessedNameWithFullCS = "::mfp::data_struct::array_based::get_elem_from_ablist";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2; // array based list, idx
            mnMinParamNum = 2; // array based list, idx
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumList = listParams.pollLast();
            DataClassSingleNum datumIdx = DCHelper.lightCvtOrRetDCMFPInt(listParams.pollLast());
            DataClass datumReturn = getElemFromArrayBasedList(datumList, datumIdx.getDataValue().intValue());
            
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_elem_from_ablistFunction());}
    
    public static class Set_elem_in_ablistFunction extends BaseBuiltInFunction {

        public Set_elem_in_ablistFunction() {
            mstrProcessedNameWithFullCS = "::mfp::data_struct::array_based::set_elem_in_ablist";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3; // array based list, idx, ref_of_elem
            mnMinParamNum = 3; // array based list, idx, ref_of_elem
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumList = listParams.pollLast();
            DataClassSingleNum datumIdx = DCHelper.lightCvtOrRetDCMFPInt(listParams.pollLast());
            DataClass datum = listParams.pollLast();
            setElemInArrayBasedList(datumList, datumIdx.getDataValue().intValue(), datum);
            
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_elem_in_ablistFunction());}
    
    public static class Append_elem_to_ablistFunction extends BaseBuiltInFunction {

        public Append_elem_to_ablistFunction() {
            mstrProcessedNameWithFullCS = "::mfp::data_struct::array_based::append_elem_to_ablist";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2; // array based list, ref_of_elem
            mnMinParamNum = 2; // array based list, ref_of_elem
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumList = listParams.pollLast();
            DataClass datum = listParams.pollLast();
            DataClass datumReturn = appendElemToArrayBasedList(datumList, datum);
            
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Append_elem_to_ablistFunction());}
    
    public static class Concat_ablistsFunction extends BaseBuiltInFunction {

        public Concat_ablistsFunction() {
            mstrProcessedNameWithFullCS = "::mfp::data_struct::array_based::concat_ablists";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2; // list1, list2
            mnMinParamNum = 2; // list1, list2
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumReturn = concatArrayBasedLists(listParams.pollLast(), listParams.pollLast());
            
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Concat_ablistsFunction());}
    
    public static class Insert_elem_into_ablistFunction extends BaseBuiltInFunction {

        public Insert_elem_into_ablistFunction() {
            mstrProcessedNameWithFullCS = "::mfp::data_struct::array_based::insert_elem_into_ablist";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3; // array based list, idx, ref_of_elem
            mnMinParamNum = 3; // array based list, idx, ref_of_elem
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumList = listParams.pollLast();
            DataClassSingleNum datumIdx = DCHelper.lightCvtOrRetDCMFPInt(listParams.pollLast());
            DataClass datum = listParams.pollLast();
            DataClass datumReturn = insertElemIntoArrayBasedList(datumList, datumIdx.getDataValue().intValue(), datum);
            
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Insert_elem_into_ablistFunction());}
    
    public static class Remove_elem_from_ablistFunction extends BaseBuiltInFunction {

        public Remove_elem_from_ablistFunction() {
            mstrProcessedNameWithFullCS = "::mfp::data_struct::array_based::remove_elem_from_ablist";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2; // array based list, idx
            mnMinParamNum = 2; // array based list, idx
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumList = listParams.pollLast();
            DataClassSingleNum datumIdx = DCHelper.lightCvtOrRetDCMFPInt(listParams.pollLast());
            DataClass datumReturn = removeElemFromArrayBasedList(datumList, datumIdx.getDataValue().intValue());
            
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Remove_elem_from_ablistFunction());}
}
