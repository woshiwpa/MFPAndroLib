/*
 * MFP project, AnnoType_build_asset.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jmfp.Annotation.AnnotationType;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;

import java.io.File;
import java.util.Locale;

/**
 *
 * @author tony
 */
public class AnnoType_build_asset extends AnnotationType {
	private static final long serialVersionUID = 1L;
	public static final String ASSET_RESOURCE_TARGET = "resource";
    public String mstrSrcPath = "";
    public int mnSrcZipType = 0;   // 1 means Android asset zip, 0 otherwise
    public String mstrSrcZipPath = "";
    public String mstrSrcZipEntry = "";
    public String mstrDestTarget = ASSET_RESOURCE_TARGET;
    public String mstrDestPath = "";
	
    AnnoType_build_asset(StatementType st)    {
        mstatementType = st;
    }

    public static String getCmdTypeStr() {
        return "build_asset";
    }
    
    @Override
    public String getCmdTypeString() {
        return getCmdTypeStr();
    }
    
    @Override
    public void analyze(String strInstructionDetails) throws JMFPCompErrException { // this parameter has been trimmed and converted to lower case.
        // cannot call toLowerCase(Locale.US) because it may include file path which is case sensative.
        DataClass datumReturn = null;
        Statement sLine = mstatementType.mstatement;
        try {
            datumReturn = Annotation.executeDetails(this, strInstructionDetails);
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            ErrorProcessor.ERRORTYPES e = ErrorProcessor.ERRORTYPES.INVALID_STATEMENT;
            throw new ErrorProcessor.JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e, ex);
        } catch (Exception ex) {
            ErrorProcessor.ERRORTYPES e = ErrorProcessor.ERRORTYPES.INVALID_STATEMENT;
            throw new ErrorProcessor.JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e, ex);
        }
        
        try {
            DataClassArray datumInfos = DCHelper.lightCvtOrRetDCArray(datumReturn);
            DataClass[] infos = datumInfos.getDataList();
            if (infos.length < 3) {
                throw new JMFPCompErrException(sLine.mstrFilePath,
                        sLine.mnStartLineNo, sLine.mnEndLineNo,
                        ErrorProcessor.ERRORTYPES.INVALID_ANNOTATION_INPUT);
            }
            DataClass datumSrc = datumInfos.getDataAtIndexByRef(new int[]{0});
            if (DCHelper.isDataClassType(datumSrc, DCHelper.DATATYPES.DATUM_STRING)) {
                mstrSrcPath = DCHelper.lightCvtOrRetDCString(datumSrc).getStringValue();
            } else {
                datumSrc = DCHelper.lightCvtOrRetDCArray(datumSrc);
                if (((DataClassArray)datumSrc).getDataListSize() != 3) {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
                DataClassSingleNum datumSrcType = DCHelper.lightCvtOrRetDCMFPInt(((DataClassArray)datumSrc).getDataAtIndexByRef(new int[]{0}));
                mnSrcZipType = datumSrcType.getDataValue().intValue();
                DataClassString datumZipPath = DCHelper.lightCvtOrRetDCString(((DataClassArray)datumSrc).getDataAtIndexByRef(new int[]{1}));
                mstrSrcZipPath = datumZipPath.getStringValue();
                DataClassString datumZipEntry = DCHelper.lightCvtOrRetDCString(((DataClassArray)datumSrc).getDataAtIndexByRef(new int[]{2}));
                mstrSrcZipEntry = datumZipEntry.getStringValue();
                if (File.separator.equals("\\")) {
                    // if we are in windows system, some people my use \ as file separator in zip entry
                    // which is not accepted by zip. So we have to convert
                    mstrSrcZipEntry = mstrSrcZipEntry.replace('\\', '/');
                }
            }
            // convert destination to small case to avoid conflict in Windows whose file name is case insensative.
            mstrDestTarget = DCHelper.lightCvtOrRetDCString(datumInfos.getDataAtIndexByRef(new int[]{1})).getStringValue().toLowerCase(Locale.US);
            if (!mstrDestTarget.equalsIgnoreCase(ASSET_RESOURCE_TARGET)) {
                throw new JMFPCompErrException(sLine.mstrFilePath,
                        sLine.mnStartLineNo, sLine.mnEndLineNo,
                        ErrorProcessor.ERRORTYPES.INVALID_ANNOTATION_INPUT);
            }
            mstrDestPath = DCHelper.lightCvtOrRetDCString(datumInfos.getDataAtIndexByRef(new int[]{2})).getStringValue();
            // it is safe to always use / as file separator in JAVA
            // as mstrDestPath is a cross platform destination path, we have to explicitly convert \ to /.
            mstrDestPath = mstrDestPath.replace('\\', '/');
        } catch (JMFPCompErrException e) {
        	throw e;
        } catch (Exception e) {
            throw new JMFPCompErrException(sLine.mstrFilePath,
                    sLine.mnStartLineNo, sLine.mnEndLineNo,
                    ErrorProcessor.ERRORTYPES.INVALID_ANNOTATION_INPUT);
        }
    }
}

