/*
 * MFP project, GIEvent.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.JGI2D;

import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author tony
 */
public class GIEvent {
    public static enum EVENTTYPE {
        GDI_INITIALIZE(1),
        GDI_CLOSE(10),
        
        WINDOW_RESIZED(21),
        
        POINTER_TAP(101),
        POINTER_DOWN(102),
        POINTER_UP(103),
        POINTER_CLICKED(104),
        POINTER_DRAGGED(105),   // the difference between dragged and slided is, slided is once-off, dragged continously triggered
        POINTER_SLIDED(106),
        POINTER_MOVE(107),
        
        POINTER_PINCHED(201),
        
        INVALID_TYPE(0);
        
        private int value; 

        private EVENTTYPE(int i) { 
            value = i; 
        } 

        public int getValue() { 
            return value; 
        }
    }
    
    public EVENTTYPE menumEventType = EVENTTYPE.INVALID_TYPE;
    
    protected Map<String, DataClass> mInfo = new HashMap<>();
    public Map<String, DataClass> getInfoMap() {
        return mInfo;
    }
    
    public DataClassArray getInfoMapDataClass() throws JFCALCExpErrException {
        LinkedList<DataClass> listInfo = new LinkedList<>();
        for (Map.Entry<String, DataClass> entry : mInfo.entrySet()) {
            listInfo.add(new DataClassArray (
                    new DataClass[] {
                        new DataClassString(entry.getKey()),
                        entry.getValue() // do not use clone, deep or shallow copy here coz we may need to use some reference at the scene.
                    }
            ));
        }
        DataClass[] arrayInfo = listInfo.toArray(new DataClass[0]);
        return new DataClassArray(arrayInfo);
    }
    
    /**
     * get info using strKey
     * @param strKey
     * @return : null if the key doesn't exist.
     */
    public DataClass getInfo(String strKey) {
        return mInfo.get(strKey);
    }
    /**
     * set the datum value for strKey
     * @param strKey
     * @param datum : shouldn't be null.
     */
    public void setInfo(String strKey, DataClass datum) {
        mInfo.put(strKey, datum);
    }
    
    /**
     * clear all the info map.
     */
    public void clearInfo() {
        mInfo.clear();
    }
}
