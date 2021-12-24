/*
 * MFP project, MFPDataTypeDef.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Oomfp;

import com.cyzapps.OSAdapter.ParallelManager.CallObject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This class defines type of MFP data types. It is immutable.
 * And every type corresponds to a unique MFPDataTypeDef. All the types are stored
 * in a static HashMap<String, MFPDataTypeDef>, which is for the whole MFP session
 * i.e. sandbox or main entity.
 * @author Tony
 */
public final class MFPDataTypeDef implements Serializable {
	private static final long serialVersionUID = 1L;

    public static final Map<String, MFPDataTypeDef> DATATYPE_DEF_SYS_MAP = new HashMap<String, MFPDataTypeDef>();;   // system level MFPDataTypeDef map (i.e. maximum subset of main entity and a sandbox).
    
    // main entity data type definition map. Initialize it to null as zero size data type definition full map is a
    // valid state.
	public static final Map<String, MFPDataTypeDef> msdataTypeDefFullMap = null;
            
    public static final MFPDataTypeDef OBJECT = new MFPDataTypeDef("::mfp::lang::object");
    public static final MFPDataTypeDef TYPE = new MFPDataTypeDef("::mfp::lang::type");
    public static final MFPDataTypeDef NULL = new MFPDataTypeDef("::mfp::lang::null");
    public static final MFPDataTypeDef NUMERIC = new MFPDataTypeDef("::mfp::lang::numeric");
    public static final MFPDataTypeDef STRING = new MFPDataTypeDef("::mfp::lang::string");
    public static final MFPDataTypeDef EXPRESSION = new MFPDataTypeDef("::mfp::lang::expression");
    
    public static Map<String, MFPDataTypeDef> getDataTypeDefMap() {   // find right MFPDataTypeDef for this thread.
        Long currentThreadId = Thread.currentThread().getId();
        if (!CallObject.msmapThreadId2SessionInfo.containsKey(currentThreadId)) {
            if (msdataTypeDefFullMap == null) {
            return DATATYPE_DEF_SYS_MAP;
            } else {
                return msdataTypeDefFullMap;
            }
        } else {
            return CallObject.msmapThreadId2SessionInfo.get(currentThreadId).getDataTypeDefMap();
        }
    }
    
    public static MFPDataTypeDef addDataTypeDef2Map(MFPDataTypeDef dataTypeDef) {
        Map<String, MFPDataTypeDef> dataTypeDefMap = getDataTypeDefMap();
        if (dataTypeDefMap.containsKey(dataTypeDef.toString())) {
            return dataTypeDefMap.get(dataTypeDef.toString());
        } else {
            dataTypeDefMap.put(dataTypeDef.toString(), dataTypeDef);
            return dataTypeDef;
        }        
    }
       
    static {
        DATATYPE_DEF_SYS_MAP.put(OBJECT.toString(), OBJECT);
        DATATYPE_DEF_SYS_MAP.put(TYPE.toString(), TYPE);
        DATATYPE_DEF_SYS_MAP.put(NULL.toString(), NULL);
        DATATYPE_DEF_SYS_MAP.put(NUMERIC.toString(), NUMERIC);
        DATATYPE_DEF_SYS_MAP.put(STRING.toString(), STRING);
        DATATYPE_DEF_SYS_MAP.put(EXPRESSION.toString(), EXPRESSION);
    }
    
    public final String typeName;   // typeName here must be a small letter and shrunk full path name.
    public final int size;
    private final MFPDataTypeDef[] paramTypes;    // if it is an array, size must be 1.
    public final MFPDataTypeDef returnType;
    
    private String fullType = "";
    
    /**
     * Initialize simple data type, e.g. int, string, ::mfp::abc::a_class etc.
     * @param name name must be a small letter and shrunk full path name, e.g. array and ::mfp::abc::def.
     */
    private MFPDataTypeDef(String name) {
        typeName = name;
        size = 0;
        paramTypes = new MFPDataTypeDef[0];
        returnType = null;
        
        fullType = getFullType();
    }
    
    public static MFPDataTypeDef createDataTypeDef(String name) {
        MFPDataTypeDef mfpDataTypeDef = new MFPDataTypeDef(name);
        return addDataTypeDef2Map(mfpDataTypeDef);
    }
    
    /**
     * Initialize function type
     * @param allParams function parameter types.
     * @param retType function return type. null if return void
     */
    private MFPDataTypeDef(LinkedList<MFPDataTypeDef> allParams, MFPDataTypeDef retType) {
        typeName = "::mfp::lang::function";
        size = 0;
        paramTypes = allParams.toArray(new MFPDataTypeDef[0]);
        returnType = retType;
        
        fullType = getFullType();
    }
    
    public static MFPDataTypeDef createDataTypeDef(LinkedList<MFPDataTypeDef> allParams, MFPDataTypeDef retType) {
        MFPDataTypeDef mfpDataTypeDef = new MFPDataTypeDef(allParams, retType);
        return addDataTypeDef2Map(mfpDataTypeDef);
    }
    
    /**
     * Initialize array type
     * @param sizeParam array size, must be a positive integer. For other types, it is ignored.
     * @param paramType array element param type.
     */
    private MFPDataTypeDef(int sizeParam, MFPDataTypeDef paramType) {
        typeName = "::mfp::lang::array";
        size = sizeParam;
        paramTypes = new MFPDataTypeDef[] {paramType};
        returnType = null;
        
        fullType = getFullType();
    }
    
    public static MFPDataTypeDef createDataTypeDef(int sizeParam, MFPDataTypeDef paramType) {
        MFPDataTypeDef mfpDataTypeDef = new MFPDataTypeDef(sizeParam, paramType);
        return addDataTypeDef2Map(mfpDataTypeDef);
    }
    
    public int getParamCount() {
        return paramTypes.length;
    }
    
    public MFPDataTypeDef getParamAt(int idx) {
        if (idx >= 0 && idx < paramTypes.length) {
            return paramTypes[idx];
        } else {
            return null;
        }
    }
    
    public MFPDataTypeDef[] getAllParams() {
        MFPDataTypeDef[] allParams = new MFPDataTypeDef[paramTypes.length];
        System.arraycopy(paramTypes, 0, allParams, 0, paramTypes.length);
        return allParams;
    }
    
    public Boolean isArray() {
        return typeName.equals("::mfp::lang::array");
    }
    
    public Boolean isFunction() {
        return typeName.equals("::mfp::lang::function");
    }
    
    @Override
    public String toString() {
        return fullType;
    }
    
    private String getFullType() {
        String fullType = "";
        if (isFunction()) {
            fullType = typeName + "(";
            for (int idx = 0; idx < paramTypes.length; idx ++) {
                if (idx > 0) {
                    fullType += ",";
                }
                fullType += paramTypes[idx].getFullType();
            }
            fullType += ")";
            if (null != returnType) {
                fullType += "->" + returnType.getFullType();
            }
        } else if (isArray()) {
            fullType = typeName + "[" + size + "]" + "(" + paramTypes[0].getFullType() + ")";
        } else {
            fullType = typeName;
        }
        return fullType;
    }
}
