// MFP project, DataReferringObj.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.VariableOperator.Variable;

public class DataReferringObj {// the referring object of this dataclass
    
    private DataClassArray mdatumArray = null;
    private int[] mindexList = new int[]{};
    private Variable mvariable = null;
    public DataReferringObj(DataClassArray datumArray, int[] indexList) {
        mdatumArray = datumArray;
        mindexList = indexList;
        mvariable = null;
    }
    public DataReferringObj(Variable variable) {
        mvariable = variable;
        mdatumArray = null;
        mindexList = new int[]{};
    }
    public DataReferringObj() {
        mdatumArray = null;
        mindexList = new int[]{};
        mvariable = null;
    }
    public void setReferringObj(DataClassArray datumArray, int[] indexList) {
        mdatumArray = datumArray;
        mindexList = indexList;
        mvariable = null;
    }
    public void setReferringObj(Variable variable) {
        mvariable = variable;
        mdatumArray = null;
        mindexList = new int[]{};
    }
    public void resetReferringObj() {
        mdatumArray = null;
        mindexList = new int[]{};
        mvariable = null;
    }
    public boolean isEmpty() {
        return mvariable == null && mdatumArray == null;
    }
    public void setNewValue4ReferringObj(DataClass datumNewRef) throws JFCALCExpErrException {
        if (mdatumArray != null) {
            DataClass datumOriginal = mdatumArray.getDataAtIndexByRef(mindexList);
            mdatumArray.setDataAtIndexByRef(mindexList, datumNewRef);
            try {
                mdatumArray.validateDataClass();
            } catch (JFCALCExpErrException e) {
                // if fail to set, restore, otherwise, incorrect dataclass will cause trouble later on.
                // only restore when set data list cause recursive referring may cause stack overflow.
                mdatumArray.setDataAtIndexByRef(mindexList, datumOriginal);    
                mdatumArray.validateDataClass_Step2();  // reset nvalidatedcnt.
                throw e;
            }

        } else if (mvariable != null) {
            mvariable.setValue(datumNewRef);
        } else {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_REFERRING_OBJECT);
        }
    }
}

