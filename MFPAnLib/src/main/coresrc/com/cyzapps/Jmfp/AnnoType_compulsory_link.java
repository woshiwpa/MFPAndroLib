/*
 * MFP project, AnnoType_compulsory_link.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.Annotation.AnnotationType;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import java.util.LinkedList;
import java.util.Locale;

/**
 *
 * @author tonyc
 */
public class AnnoType_compulsory_link extends AnnotationType {

	private static final long serialVersionUID = 1L;
	public LinkedList<ModuleInfo>  mlistModuleInfo;
    AnnoType_compulsory_link(StatementType st)    {
        mstatementType = st;
        mlistModuleInfo = new LinkedList<>();
    }

    public static String getCmdTypeStr() {
        return "compulsory_link";
    }

    @Override
    public String getCmdTypeString() {
        return getCmdTypeStr();
    }
    
    @Override
    public void analyze(String strDetails) throws JMFPCompErrException { // this parameter has been trimmed and converted to lower case.
        // strDetails should have been lower cased so no need to do it again.
        DataClassArray datumInfos = null;
        DataClass datumReturn = null;
        
        Statement sLine = mstatementType.mstatement;
        try {
            // note that we must use .toLowerCase(Locale.US) here because parameters of compulsory_link
            // annotation are string based function names. when a statement is constructed, string is not
            // converted to lowercase. But function names have to be lower case. So we do it here.
            datumReturn = Annotation.executeDetails(this, strDetails.toLowerCase(Locale.US));
        } catch (JFCALCExpErrException ex) {
            ErrorProcessor.ERRORTYPES e = ErrorProcessor.ERRORTYPES.INVALID_STATEMENT;
            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e, ex);
        } catch (Exception ex) {
            ErrorProcessor.ERRORTYPES e = ErrorProcessor.ERRORTYPES.INVALID_STATEMENT;
            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e, ex);
        }
        
        try {
            datumInfos = DCHelper.lightCvtOrRetDCArray(datumReturn);
            DataClass[] infos = datumInfos.getDataList();
            for (DataClass info: infos) {
                ModuleInfo moduleInfo = (ModuleInfo)DCHelper.lightCvtOrRetDCExtObjRef(info).getExternalObject();
                mlistModuleInfo.add(moduleInfo);
            }
        } catch (Exception e) {
            throw new JMFPCompErrException(sLine.mstrFilePath,
                    sLine.mnStartLineNo, sLine.mnEndLineNo,
                    ErrorProcessor.ERRORTYPES.INVALID_ANNOTATION_INPUT);
        }
    }
}
