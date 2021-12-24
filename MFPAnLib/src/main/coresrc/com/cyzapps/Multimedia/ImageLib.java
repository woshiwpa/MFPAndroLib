/*
 * MFP project, ImageLib.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Multimedia;

import com.cyzapps.JGI2D.DisplayLib.IGraphicDisplay;
import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassExtObjRef;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.OSAdapter.LangFileManager;
import com.cyzapps.Oomfp.CitingSpaceDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

/**
 *
 * @author tony
 */
public class ImageLib {
    public static abstract class ImageManager {
    	public abstract IGraphicDisplay openImageDisplay(DataClass pathOrImgHandler);
    	public abstract void shutdownImageDisplay(IGraphicDisplay display); // do not need a Shutdown_image_displayFunction. Shutdown_displayFunction will handle it.
        public abstract DataClass loadImage(String strImagePath);
        public abstract DataClass loadImage(InputStream inputStream);
        public abstract int[] getImageSize(DataClassExtObjRef img);
        public abstract boolean isValidImageHandle(DataClassExtObjRef img);
        public abstract DataClass createImage(int w, int h);
        public abstract DataClass cloneImage(DataClassExtObjRef img);
        public abstract DataClass cloneImage(DataClassExtObjRef img, int x1, int y1, int x2, int y2, int destW, int destH);
        public abstract boolean saveImage(DataClassExtObjRef img, String imgFormat, String strImagePath);
    }
    
    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + ImageLib.class.getName());
        }
    }
    
    public static class Open_image_displayFunction extends BaseBuiltInFunction {

        public Open_image_displayFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::image_lib::open_image_display";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumImgPathOrHandler = listParams.poll();
            
            if (!datumImgPathOrHandler.isNull() && DCHelper.try2LightCvtOrRetDCString(datumImgPathOrHandler) == null) {
            	// must be a handler
            	DataClassExtObjRef datumHandler = DCHelper.lightCvtOrRetDCExtObjRef(datumImgPathOrHandler);
            	if (!FuncEvaluator.msMultimediaMgr.getImageManager().isValidImageHandle(datumHandler)) {
            		throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            	}
            }
            
            IGraphicDisplay display = FuncEvaluator.msMultimediaMgr.getImageManager().openImageDisplay(datumImgPathOrHandler);
            if (display == null) {
            	return new DataClassNull();
            } else {
            	return new DataClassExtObjRef(display);
            }
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Open_image_displayFunction());}
    
    // do not need a Shutdown_image_displayFunction. Shutdown_displayFunction will handle it.

    public static class Load_imageFunction extends BaseBuiltInFunction {

        public Load_imageFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::image_lib::load_image";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumPath = DCHelper.lightCvtOrRetDCString(listParams.poll());
            
            ImageManager imgMgr = FuncEvaluator.msMultimediaMgr.getImageManager();
            DataClass datumReturn = imgMgr.loadImage(datumPath.getStringValue());
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Load_imageFunction());}

    public static class Load_image_from_zipFunction extends BaseBuiltInFunction {

        public Load_image_from_zipFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::image_lib::load_image_from_zip";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3; // first is zip file name, second is zipped file entry, third is zip file type (1 for android asset, 0 for plain zip)
            mnMinParamNum = 3;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msPlatformHWMgr == null) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_PLATFORM_HARDWARE_LIB_IS_NOT_LOADED);
            } else if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            int zipFileType = DCHelper.lightCvtOrRetDCMFPInt(listParams.poll()).getDataValue().intValue();
            String strFileEntryPath = DCHelper.lightCvtOrRetDCString(listParams.poll()).getStringValue();
            String strZipFilePath = DCHelper.lightCvtOrRetDCString(listParams.poll()).getStringValue();
            
            LangFileManager langFileMgr = FuncEvaluator.msPlatformHWMgr.getLangFileManager();
            InputStream inputStream = null;
			try {
				inputStream = langFileMgr.openZippedFileEntry(strZipFilePath, strFileEntryPath, zipFileType==1);
				if (inputStream == null) {
					throw new IOException();
				}
			} catch (IOException e) {
				throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_OPEN_FILE);
			}
            ImageManager imgMgr = FuncEvaluator.msMultimediaMgr.getImageManager();
            DataClass datumReturn = imgMgr.loadImage(inputStream);
            try {
            	// inputStream must not be null now.
				inputStream.close();
			} catch (IOException e) {
				// do nothing, only print stack.
				e.printStackTrace();
			}
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Load_image_from_zipFunction());}
        
    public static class Get_image_sizeFunction extends BaseBuiltInFunction {

        public Get_image_sizeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::image_lib::get_image_size";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassExtObjRef datumImage = DCHelper.lightCvtOrRetDCExtObjRef(listParams.poll());
            ImageManager imgMgr = FuncEvaluator.msMultimediaMgr.getImageManager();
            if (!imgMgr.isValidImageHandle(datumImage)) {
        		throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
        	}
            int[] size = imgMgr.getImageSize(datumImage);
            if (size == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_IMAGE);
            }
            DataClassSingleNum datum0 = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(size[0]));
            DataClassSingleNum datum1 = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(size[1]));
            DataClass[] sizeArray = new DataClass[] {datum0, datum1};
            DataClassArray datumReturn = new DataClassArray(sizeArray);
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_image_sizeFunction());}
    
    public static class Is_valid_image_handleFunction extends BaseBuiltInFunction {

        public Is_valid_image_handleFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::image_lib::is_valid_image_handle";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassExtObjRef datumImage = DCHelper.lightCvtOrRetDCExtObjRef(listParams.poll());
            Boolean bIsValid = FuncEvaluator.msMultimediaMgr.getImageManager().isValidImageHandle(datumImage);
            return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, new MFPNumeric(bIsValid));
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Is_valid_image_handleFunction());}
    
    public static class Create_imageFunction extends BaseBuiltInFunction {

        public Create_imageFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::image_lib::create_image";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2; // width, height
            mnMinParamNum = 2;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum
            		&& listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            int w = DCHelper.lightCvtOrRetDCMFPInt(listParams.pollLast()).getDataValue().intValue();
            int h = DCHelper.lightCvtOrRetDCMFPInt(listParams.pollLast()).getDataValue().intValue();
            if (w <= 0 || h <= 0) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            ImageManager imgMgr = FuncEvaluator.msMultimediaMgr.getImageManager();
            DataClass datumReturn = imgMgr.createImage(w, h);
        	return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Create_imageFunction());}
    
    public static class Clone_imageFunction extends BaseBuiltInFunction {

        public Clone_imageFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::image_lib::clone_image";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 7; // img, srcleft, srctop, srcright, srcbottom, destwidth, destheight
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() != mnMaxParamNum
            		&& listParams.size() != 5
            		&& listParams.size() != mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassExtObjRef datumImage = DCHelper.lightCvtOrRetDCExtObjRef(listParams.pollLast());
            ImageManager imgMgr = FuncEvaluator.msMultimediaMgr.getImageManager();
            if (!imgMgr.isValidImageHandle(datumImage)) {
        		throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
        	}
            if (listParams.size() > 0) {
            	int srcLeft = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast()).getDataValue().intValue();
            	int srcTop = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast()).getDataValue().intValue();
            	int srcRight = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast()).getDataValue().intValue();
            	int srcBottom = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast()).getDataValue().intValue();
            	if (srcRight <= 0 || srcBottom <= 0 || srcLeft >= srcRight || srcTop >= srcBottom) {
            		throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            	}
            	int destWidth = srcRight - srcLeft;
            	int destHeight = srcBottom - srcTop;
            	if (listParams.size() > 0) {
                	destWidth = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast()).getDataValue().intValue();
                	destHeight = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast()).getDataValue().intValue();
                	if (destWidth <= 0 || destHeight <= 0) {
                		throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                	}
            	}
            	DataClass datumReturn = imgMgr.cloneImage(datumImage, srcLeft, srcTop, srcRight, srcBottom, destWidth, destHeight);
            	return datumReturn;
            } else {
            	DataClass datumReturn = imgMgr.cloneImage(datumImage);
            	return datumReturn;
            }
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Clone_imageFunction());}
    
    public static class Save_imageFunction extends BaseBuiltInFunction {

        public Save_imageFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::image_lib::save_image";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3;
            mnMinParamNum = 3;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            String strPath = DCHelper.lightCvtOrRetDCString(listParams.poll()).getStringValue();
            String strFormat = DCHelper.lightCvtOrRetDCString(listParams.poll()).getStringValue();
            DataClassExtObjRef datumImage = DCHelper.lightCvtOrRetDCExtObjRef(listParams.poll());
            ImageManager imgMgr = FuncEvaluator.msMultimediaMgr.getImageManager();
            if (!imgMgr.isValidImageHandle(datumImage)) {
        		throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
        	}
            Boolean bResult = imgMgr.saveImage(datumImage, strFormat, strPath);
            return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, new MFPNumeric(bResult));
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Save_imageFunction());}
    
}
