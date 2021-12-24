package com.cyzapps.Jfcalc;

import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Oomfp.CitingSpaceDefinition;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import org.json.JSONArray;

public class ExDataLib {

    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + ExDataLib.class.getName());
        }
    }

    public static class Get_json_fieldFunction extends BuiltInFunctionLib.BaseBuiltInFunction {
        private static final long serialVersionUID = 1L;

        public Get_json_fieldFunction() {
            mstrProcessedNameWithFullCS = "::mfp::exdata::json::get_json_field";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3;
            mnMinParamNum = 2;
        }
        
        public DataClass getJSONArrayField(JSONArray array, int idx, String type) throws ErrProcessor.JFCALCExpErrException {
            try {
                if (array.length() <= idx || idx < 0) {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
                } else if (array.isNull(idx)) {
                    return new DataClassNull();
                } else if (type.equals("b")) {
                    Boolean val = array.getBoolean(idx);
                    return new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPBOOL, val?MFPNumeric.TRUE:MFPNumeric.FALSE);
                } else if (type.equals("d")) {
                    Long val = array.getLong(idx);
                    return new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPINT, new MFPNumeric(val));
                } else if (type.equals("f")) {
                    Double val = array.getDouble(idx);
                    return new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(val, true));
                } else if (type.equals("a")) {
                    JSONArray val = array.getJSONArray(idx);
                    DataClass[] datumArray = new DataClass[val.length()];
                    for (int idx1 = 0; idx1 < val.length(); idx1 ++) {
                        DataClass datumElem = getJSONArrayField(val, idx1, "");
                        datumArray[idx1] = datumElem;
                    }
                    return new DataClassArray(datumArray);
                } else if (type.equals("j")) {
                    JSONObject val = array.getJSONObject(idx);
                    DataClass[] datumArray = new DataClass[val.length()];
                    int idx1 = 0;
                    Iterator<String> keys = val.keys();
                    while (keys.hasNext()) {
                        String keySub = (String) keys.next();
                        DataClass valueSub = getJSONField(val, keySub, "");
                        DataClass[] datumKeyValueSubArray = new DataClass[2];
                        datumKeyValueSubArray[0] = new DataClassString(keySub);
                        datumKeyValueSubArray[1] = valueSub;
                        datumArray[val.length() - 1 - idx1] = new DataClassArray(datumKeyValueSubArray);
                        idx1 ++;
                    }
                    return new DataClassArray(datumArray);
                } else if (type.equals("s")) {
                    String val = array.getString(idx);
                    return new DataClassString(val);
                } else {    // auto detect
                    try {
                        Boolean val0 = array.getBoolean(idx);
                        return new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPBOOL, val0?MFPNumeric.TRUE:MFPNumeric.FALSE);
                    } catch (JSONException e0) {
                        try {
                            Double val1 = array.getDouble(idx);
                            return new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(val1, true));
                        } catch (JSONException e1) {
                            try {
                                Long val2 = array.getLong(idx);
                                return new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPINT, new MFPNumeric(val2));
                            } catch (JSONException e2) {
                                try {
                                    JSONArray val3 = array.getJSONArray(idx);
                                    DataClass[] datumArray = new DataClass[val3.length()];
                                    for (int idx1 = 0; idx1 < val3.length(); idx1 ++) {
                                        DataClass datumElem = getJSONArrayField(val3, idx1, "");
                                        datumArray[idx1] = datumElem;
                                    }
                                    return new DataClassArray(datumArray);
                                } catch (JSONException e3) {
                                    try {
                                        JSONObject val4 = array.getJSONObject(idx);
                                        DataClass[] datumArray = new DataClass[val4.length()];
                                        int idx1 = 0;
                                        Iterator<String> keys = val4.keys();
                                        while (keys.hasNext()) {
                                            String keySub = (String) keys.next();
                                            DataClass valueSub = getJSONField(val4, keySub, "");
                                            DataClass[] datumKeyValueSubArray = new DataClass[2];
                                            datumKeyValueSubArray[0] = new DataClassString(keySub);
                                            datumKeyValueSubArray[1] = valueSub;
                                            datumArray[val4.length() - 1 - idx1] = new DataClassArray(datumKeyValueSubArray);
                                            idx1 ++;
                                        }
                                        return new DataClassArray(datumArray);
                                    } catch (JSONException e4) {
                                        // no need to throw exception for the last one
                                        String val5 = array.getString(idx);
                                        return new DataClassString(val5);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
        }
        
        public DataClass getJSONField(JSONObject obj, String key, String type) throws ErrProcessor.JFCALCExpErrException {
            try {
                if (!obj.has(key)) {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
                } else if (obj.isNull(key)) {
                    return new DataClassNull();
                } else if (type.equals("b")) {
                    Boolean val = obj.getBoolean(key);
                    return new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPBOOL, val?MFPNumeric.TRUE:MFPNumeric.FALSE);
                } else if (type.equals("d")) {
                    Long val = obj.getLong(key);
                    return new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPINT, new MFPNumeric(val));
                } else if (type.equals("f")) {
                    Double val = obj.getDouble(key);
                    return new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(val, true));
                } else if (type.equals("a")) {
                    JSONArray val = obj.getJSONArray(key);
                    DataClass[] datumArray = new DataClass[val.length()];
                    for (int idx = 0; idx < val.length(); idx ++) {
                        DataClass datumElem = getJSONArrayField(val, idx, "");
                        datumArray[idx] = datumElem;
                    }
                    return new DataClassArray(datumArray);
                } else if (type.equals("j")) {
                    JSONObject val = obj.getJSONObject(key);
                    DataClass[] datumArray = new DataClass[val.length()];
                    int idx = 0;
                    Iterator<String> keys = val.keys();
                    while (keys.hasNext()) {
                        String keySub = (String) keys.next();
                        DataClass valueSub = getJSONField(val, keySub, "");
                        DataClass[] datumKeyValueSubArray = new DataClass[2];
                        datumKeyValueSubArray[0] = new DataClassString(keySub);
                        datumKeyValueSubArray[1] = valueSub;
                        datumArray[val.length() - 1 - idx] = new DataClassArray(datumKeyValueSubArray);
                        idx ++;
                    }
                    return new DataClassArray(datumArray);
                } else if (type.equals("s")) {
                    String val = obj.getString(key);
                    return new DataClassString(val);
                } else {    // auto detect
                    try {
                        Boolean val0 = obj.getBoolean(key);
                        return new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPBOOL, val0?MFPNumeric.TRUE:MFPNumeric.FALSE);
                    } catch (JSONException e0) {
                        try {
                            Double val1 = obj.getDouble(key);
                            return new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(val1, true));
                        } catch (JSONException e1) {
                            try {
                                Long val2 = obj.getLong(key);
                                return new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPINT, new MFPNumeric(val2));
                            } catch (JSONException e2) {
                                try {
                                    JSONArray val3 = obj.getJSONArray(key);
                                    DataClass[] datumArray = new DataClass[val3.length()];
                                    for (int idx = 0; idx < val3.length(); idx ++) {
                                        DataClass datumElem = getJSONArrayField(val3, idx, "");
                                        datumArray[idx] = datumElem;
                                    }
                                    return new DataClassArray(datumArray);
                                } catch (JSONException e3) {
                                    try {
                                        JSONObject val4 = obj.getJSONObject(key);
                                        DataClass[] datumArray = new DataClass[val4.length()];
                                        int idx = 0;
                                        Iterator<String> keys = val4.keys();
                                        while (keys.hasNext()) {
                                            String keySub = (String) keys.next();
                                            DataClass valueSub = getJSONField(val4, keySub, "");
                                            DataClass[] datumKeyValueSubArray = new DataClass[2];
                                            datumKeyValueSubArray[0] = new DataClassString(keySub);
                                            datumKeyValueSubArray[1] = valueSub;
                                            datumArray[val4.length() - 1 - idx] = new DataClassArray(datumKeyValueSubArray);
                                            idx ++;
                                        }
                                        return new DataClassArray(datumArray);
                                    } catch (JSONException e4) {
                                        // no need to throw exception for the last one
                                        String val5 = obj.getString(key);
                                        return new DataClassString(val5);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
        }
        
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException, InterruptedException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumJSON = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String json = datumJSON.getStringValue();
            DataClassString datumKey = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String key = datumKey.getStringValue();
            String type = "";  // s means string, b means boolean, f means double, d means int and others means let program decide
            if (listParams.size() > 0) {
                DataClass datumType = listParams.removeLast();
                if (DCHelper.isDataClassType(datumType, DCHelper.DATATYPES.DATUM_STRING)) {
                    type = DCHelper.lightCvtOrRetDCString(datumType).getStringValue().toLowerCase(Locale.US);                    
                }
            }

            DataClass datumRet = null;
            try {
                JSONObject obj = new JSONObject(json);
                datumRet = getJSONField(obj, key, type);
            } catch (JSONException e) {
                e.printStackTrace();
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            return datumRet;
        }
    }
    static {
        CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_json_fieldFunction());}

    public static class Set_json_fieldFunction extends BuiltInFunctionLib.BaseBuiltInFunction {
        private static final long serialVersionUID = 1L;

        public Set_json_fieldFunction() {
            mstrProcessedNameWithFullCS = "::mfp::exdata::json::set_json_field";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3;
            mnMinParamNum = 3;
        }
        
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException, InterruptedException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumJSON = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String json = datumJSON.getStringValue();
            DataClassString datumKey = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String key = datumKey.getStringValue();
            DataClass datumValue = listParams.removeLast();

            DataClass datumRet = null;
            try {
                JSONObject obj = new JSONObject(json);
                if (datumValue.isNull()) {
                    obj.put(key, JSONObject.NULL);
                } else switch (datumValue.getDataClassType()) {
                    case DATUM_NULL:
                        obj.put(key, JSONObject.NULL);
                        break;
                    case DATUM_MFPBOOL:
                        obj.put(key, DCHelper.lightCvtOrRetDCMFPBool(datumValue).getDataValue().booleanValue());
                        break;
                    case DATUM_MFPINT:
                        obj.put(key, DCHelper.lightCvtOrRetDCMFPInt(datumValue).getDataValue().longValue());
                        break;
                    case DATUM_MFPDEC:
                        obj.put(key, DCHelper.lightCvtOrRetDCMFPDec(datumValue).getDataValue().doubleValue());
                        break;
                    case DATUM_REF_DATA:
                        obj.put(key, new JSONArray(datumValue.toString()));
                        break;
                    case DATUM_STRING:
                        obj.put(key, DCHelper.lightCvtOrRetDCString(datumValue).getStringValue());
                        break;
                    default:
                        obj.put(key, datumValue.toString());
                }
                datumRet = new DataClassString(obj.toString());
                return datumRet;
            } catch (JSONException e) {
                e.printStackTrace();
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
        }
    }
    static {
        CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_json_fieldFunction());}
}
