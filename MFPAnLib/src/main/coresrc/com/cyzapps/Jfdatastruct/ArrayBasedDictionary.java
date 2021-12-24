// MFP project, ArrayBasedDictionary.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfdatastruct;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Oomfp.CitingSpaceDefinition;

public class ArrayBasedDictionary {

    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + ArrayBasedDictionary.class.getName());
        }
    }
    
    /**
     * Create a new array based dictionary
     * @return array based dictionary
     */
    public static DataClassArray createArrayBasedDict() {
    	try {
			return new DataClassArray(new DataClass[0]);
		} catch (JFCALCExpErrException e) {
			// will not be here.
			e.printStackTrace();
			return null;
		}
    }
    
    /** 
     * get reference (not copy) of the key value pair from arrayDictionary
     * @param dict
     * @param key
     * @return key value pair if the key exist, or null if not.
     * @throws JFCALCExpErrException
     */
    protected static DataClassArray getArrayBasedDictKeyValuePair(DataClass dict, String key) throws JFCALCExpErrException {
    	DataClassArray arrayDict = DCHelper.lightCvtOrRetDCArray(dict);
    	DataClass[] keyValPairs = arrayDict.getDataList();
    	for (DataClass keyValPair : keyValPairs) {
    		DataClassArray keyVal = null;
    		if (keyValPair != null && (keyVal = DCHelper.try2LightCvtOrRetDCArray(keyValPair)) != null) {
    			DataClassString datumKey = null;
    			if (keyVal.getDataListSize() == 2
    					&& keyVal.getDataList()[0] != null
    					&& (datumKey = DCHelper.try2LightCvtOrRetDCString(keyVal.getDataAtIndexByRef(new int[] {0}))) != null) {
    				// ok, this is a keyval pair
    				String strKey = datumKey.getStringValue();	// assume strKey is not null.
    				if (strKey.equals(key)) {
    					// find it.
    					return keyVal;
    				}
    			}
    		}
    	}
    	// unfortunately, the value doesn't exist.
    	return null;
    }
    
    /**
     * add (key, value) into dictionary. Note that reference, not copy of the value will be set.
     * @param dict : original dictionary,
     * @param key
     * @param value
     * @return new dictionary.
     * @throws JFCALCExpErrException 
     */
    public static DataClassArray setArrayBasedDictValue(DataClass dict, String key, DataClass value) throws JFCALCExpErrException {
    	DataClassArray arrayDict = DCHelper.lightCvtOrRetDCArray(dict);
    	DataClassArray keyVal = getArrayBasedDictKeyValuePair(dict, key);
    	if (keyVal != null) {
    		// the key does exist
    		keyVal.setValidDataAtIndexByRef(new int[] {1}, value);
    		return arrayDict;
    	} else {
	    	// unfortunately, the key doesn't exist.
	    	int dataSize = arrayDict.getDataListSize();
                DataClass datumStrKey = new DataClassString(key);
                DataClass[] datumElemList = new DataClass[] {datumStrKey, value};
                DataClass datumNewElem = new DataClassArray(datumElemList);
	    	return DCHelper.lightCvtOrRetDCArray(
	    			arrayDict.createDataAt1stLvlIdxByRef(dataSize, datumNewElem, new DataClassNull())
	    			);
    	}
    }
    
    /**
     * get value for key
     * @param dict : array based dictionary
     * @param key : string based key
     * @return : if the key exist, return the reference of the value, otherwise, return null.
     * @throws JFCALCExpErrException 
     */
    public static DataClass getArrayBasedDictValue(DataClass dict, String key) throws JFCALCExpErrException {
    	DataClassArray keyVal = getArrayBasedDictKeyValuePair(dict, key);
    	if (keyVal != null) {
    		// the key does exist
    		return keyVal.getDataAtIndexByRef(new int[] {1});
    	} else {
	    	// unfortunately, the value doesn't exist.
	    	return null;
    	}
    }

    
    public static class Create_abdictFunction extends BaseBuiltInFunction {

        public Create_abdictFunction() {
            mstrProcessedNameWithFullCS = "::mfp::data_struct::array_based::create_abdict";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 0;
            mnMinParamNum = 0;
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumReturn = createArrayBasedDict();
            
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Create_abdictFunction());}
    
    public static class Set_value_in_abdictFunction extends BaseBuiltInFunction {

        public Set_value_in_abdictFunction() {
            mstrProcessedNameWithFullCS = "::mfp::data_struct::array_based::set_value_in_abdict";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3; // array based dict, key, value
            mnMinParamNum = 3; // array based list, key, value
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumDict = listParams.pollLast();
            DataClassString datumKey = DCHelper.lightCvtOrRetDCString(listParams.pollLast());
            DataClass datumVal = listParams.pollLast();
            DataClass datumReturn = setArrayBasedDictValue(datumDict, datumKey.getStringValue(), datumVal);
            
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_value_in_abdictFunction());}
    
    public static class Get_value_from_abdictFunction extends BaseBuiltInFunction {

        public Get_value_from_abdictFunction() {
            mstrProcessedNameWithFullCS = "::mfp::data_struct::array_based::get_value_from_abdict";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2; // array based dict, key
            mnMinParamNum = 2; // array based dict, key
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumDict = listParams.pollLast();
            DataClassString datumKey = DCHelper.lightCvtOrRetDCString(listParams.pollLast());
            DataClass datumReturn = getArrayBasedDictValue(datumDict, datumKey.getStringValue());
            
            if (datumReturn == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_KEY_NOT_EXIST);
            }
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_value_from_abdictFunction());}

}
