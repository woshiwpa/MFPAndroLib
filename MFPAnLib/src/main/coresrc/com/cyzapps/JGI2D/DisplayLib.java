// MFP project, DisplayLib.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.JGI2D;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.BuiltInFunctionLib;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClassExtObjRef;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Multimedia.ImageLib;
import com.cyzapps.Oomfp.CitingSpaceDefinition;

public class DisplayLib {
    
    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + DisplayLib.class.getName());
        }
    }
    
    public static enum GraphicDisplayType {
        INVALID_DISPLAY,
    	SCREEN_2D_DISPLAY,
    	IMAGE_DISPLAY,
    	IMAGE_PATH_DISPLAY
    }
    
    public static interface IGraphicDisplay {
    	// this is a common super class for all displays.
    	public GraphicDisplayType getDisplayType();	// at this moment only support screen 2D, image and image path displays. All of them are 2D.
    	public void initialize();
        public void update();
    	public void close();
    }
    /**
     * doesn't support image type (or image path type) display
     */
    public static Display2D get2DDisplay(DataClass pathOrHandler, boolean bCanbeImage) throws JFCALCExpErrException {
    	if (bCanbeImage && DCHelper.try2LightCvtOrRetDCString(pathOrHandler) != null) {
    		// OK, this is path of an image file.
    		if (FuncEvaluator.msMultimediaMgr == null) {
    			throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
    		} else {
    			IGraphicDisplay display = FuncEvaluator.msMultimediaMgr.getImageManager().openImageDisplay(pathOrHandler);
    			if (!(display instanceof Display2D)) {
    				throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
    			}
    			return (Display2D)display;
    		}
    	} else if (DCHelper.try2LightCvtOrRetDCExtObjRef(pathOrHandler) != null) {
    		Object handler = DCHelper.lightCvtOrRetDCExtObjRef(pathOrHandler).getExternalObject();
    		if (handler instanceof Display2D) {
    			// ok, this has been a display
    			return (Display2D)handler;
    		} else if (bCanbeImage) {
    			IGraphicDisplay display = FuncEvaluator.msMultimediaMgr.getImageManager().openImageDisplay(pathOrHandler);
    			if (!(display instanceof Display2D)) {
    				throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
    			}
    			return (Display2D)display;
    		} else {
    			throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
    		}
    	} else {
    		throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
    	}
    }
    
    /**
     * doesn't support image type (or image path type) display
     */
    public static class Open_screen_displayFunction extends BaseBuiltInFunction {

        public Open_screen_displayFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::open_screen_display";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            // caption, background color, quit_confirm, size, resizable, orientation.
            // put size, orientation in the tail because they are not platform independent.
            mnMaxParamNum = 6;
            mnMinParamNum = 0;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msGDIMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_GDI_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            String strTitle = "";
            com.cyzapps.VisualMFP.Color clr = new com.cyzapps.VisualMFP.Color();    // by default, it is black.
            boolean bQuitConfirm = false;
            double[] size = new double[] {
                0,0
            };
            boolean bResizable = false;
            int orientation = -1;
            if (listParams.size() > 0) {
                strTitle = DCHelper.lightCvtOrRetDCString(listParams.pollLast()).getStringValue();
            }
            if (listParams.size() > 0) {
                clr = DrawLib.getColor(listParams.pollLast());
            }
            if (listParams.size() > 0) {
                bQuitConfirm = DCHelper.lightCvtOrRetDCMFPBool(listParams.pollLast()).getDataValue().booleanValue();
            }
            if (listParams.size() > 0) {
                size = DrawLib.getCoordinate(listParams.pollLast());
                if (size == null || size.length < 2) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
            }
            if (listParams.size() > 0) {
                bResizable = DCHelper.lightCvtOrRetDCMFPBool(listParams.pollLast()).getDataValue().booleanValue();
            }
            if (listParams.size() > 0) {
                orientation = DCHelper.lightCvtOrRetDCMFPInt(listParams.pollLast()).getDataValue().intValue();
            }
            IGraphicDisplay display = FuncEvaluator.msGDIMgr.openScreenDisplay(strTitle, clr, bQuitConfirm, size, bResizable, orientation);
            if (display == null) {
            	return new DataClassNull();
            } else {
            	return new DataClassExtObjRef(display);
            }
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Open_screen_displayFunction());}
    
    public static class Shutdown_displayFunction extends BaseBuiltInFunction {

        public Shutdown_displayFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::shutdown_display";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2; // if the additional parameter is true, then no confirm dialog pop up.
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Display2D display = get2DDisplay(listParams.pollLast(), false);
            if (!display.isDisplayOnLive()) {
            	// if the display is not on live, no need to close it
            	return null;
            }
            boolean bByForce = false;
            if (listParams.size() > 0) {
                bByForce = DCHelper.lightCvtOrRetDCMFPBool(listParams.pollLast()).getDataValue().booleanValue();
            }
            
            switch (display.getDisplayType())
            {
            case SCREEN_2D_DISPLAY:
	            if (FuncEvaluator.msGDIMgr == null) {
	                throw new JFCALCExpErrException(ERRORTYPES.ERROR_GDI_LIB_IS_NOT_LOADED);
	            } else {
	            	FuncEvaluator.msGDIMgr.shutdownScreenDisplay(display, bByForce);
	            }
	            break;
            case IMAGE_DISPLAY:
            case IMAGE_PATH_DISPLAY:
	        	if (FuncEvaluator.msMultimediaMgr == null) { // if multimedia manager is loaded, image library must be loaded.
	                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
	            } else {
	            	FuncEvaluator.msMultimediaMgr.getImageManager().shutdownImageDisplay(display);
	            }
	        	break;
	        default:
	        	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            }
            return null;    // create a new window instance.
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Shutdown_displayFunction());}

    public static class Is_display_on_liveFunction extends BaseBuiltInFunction {

        public Is_display_on_liveFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::is_display_on_live";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Display2D display = get2DDisplay(listParams.pollLast(), false);
            
            DataClassSingleNum datumReturn = new DataClassSingleNum(
            		DATATYPES.DATUM_MFPBOOL,
            		display.isDisplayOnLive()?MFPNumeric.TRUE:MFPNumeric.FALSE);
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Is_display_on_liveFunction());}

    public static class Update_displayFunction extends BaseBuiltInFunction {

        public Update_displayFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::update_display";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing in Android.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            Display2D display = get2DDisplay(listParams.poll(), false);

            display.update();
            display.repaint();
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Update_displayFunction());}
    
    public static class Set_display_bgrnd_colorFunction extends BaseBuiltInFunction {

        public Set_display_bgrnd_colorFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::set_display_bgrnd_color";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 2;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }

            Display2D display = get2DDisplay(listParams.pollLast(), false);
            
            com.cyzapps.VisualMFP.Color clr = DrawLib.getColor(listParams.pollLast());
            display.setBackgroundColor(clr);
            return null;    // create a new window instance.
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_display_bgrnd_colorFunction());}
    
    public static class Get_display_bgrnd_colorFunction extends BaseBuiltInFunction {

        public Get_display_bgrnd_colorFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::get_display_bgrnd_color";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Display2D display = get2DDisplay(listParams.poll(), false);
                        
            com.cyzapps.VisualMFP.Color clr = display.getBackgroundColor();
            DataClassSingleNum clrAlpha = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(clr.mnAlpha));
            DataClassSingleNum clrR = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(clr.mnR));
            DataClassSingleNum clrG = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(clr.mnG));
            DataClassSingleNum clrB = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(clr.mnB));
            DataClass[] datumList = new DataClass[4];
            datumList[0] = clrAlpha;
            datumList[1] = clrR;
            datumList[2] = clrG;
            datumList[3] = clrB;
            DataClassArray datumReturn = new DataClassArray(datumList);
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_display_bgrnd_colorFunction());}
    
    public static class Set_display_bgrnd_imageFunction extends BaseBuiltInFunction {

        public Set_display_bgrnd_imageFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::set_display_bgrnd_image";
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

            Display2D display = get2DDisplay(listParams.pollLast(), false);
            
            DataClass imgHandlerOrPath = listParams.pollLast();
            DataClassString imgPath = null;
            if (!imgHandlerOrPath.isNull()) {
                imgPath = DCHelper.try2LightCvtOrRetDCString(imgHandlerOrPath);
            }
            DataClassExtObjRef imgHandler = null;
            if (imgPath != null) {
                // this is a path
                ImageLib.ImageManager imgMgr = FuncEvaluator.msMultimediaMgr.getImageManager();
                imgHandler = DCHelper.lightCvtOrRetDCExtObjRef(imgMgr.loadImage(imgPath.getStringValue()));
            } else if (!imgHandlerOrPath.isNull()) {
                // this is a image handler
                imgHandler = DCHelper.lightCvtOrRetDCExtObjRef(imgHandlerOrPath);
                if (!FuncEvaluator.msMultimediaMgr.getImageManager().isValidImageHandle(imgHandler)) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
            }

            int backgroundMode = DCHelper.lightCvtOrRetDCMFPInt(listParams.pollLast()).getDataValue().intValue();
            if (backgroundMode < 0 || backgroundMode > 3) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            
            display.setBackgroundImage(imgHandler, backgroundMode);
            return null;    // create a new window instance.
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_display_bgrnd_imageFunction());}
    
    public static class Get_display_bgrnd_imageFunction extends BaseBuiltInFunction {

        public Get_display_bgrnd_imageFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::get_display_bgrnd_image";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Display2D display = get2DDisplay(listParams.poll(), false);
                        
            DataClass datumReturn = display.getBackgroundImage();
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_display_bgrnd_imageFunction());}
    
    public static class Get_display_bgrnd_image_modeFunction extends BaseBuiltInFunction {

        public Get_display_bgrnd_image_modeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::get_display_bgrnd_image_mode";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Display2D display = get2DDisplay(listParams.poll(), false);
                        
            DataClass datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPINT,
            								new MFPNumeric(display.getBackgroundImageMode()));
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_display_bgrnd_image_modeFunction());}
    
    public static class Set_display_snapshot_as_bgrndFunction extends BaseBuiltInFunction {

        public Set_display_snapshot_as_bgrndFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::set_display_snapshot_as_bgrnd";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3;
            mnMinParamNum = 3;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Display2D display = get2DDisplay(listParams.pollLast(), false);
            
            DataClassSingleNum datumUpdateScreen = DCHelper.lightCvtOrRetDCMFPBool(listParams.pollLast());
            boolean bUpdateScreen = datumUpdateScreen.getDataValue().booleanValue();
            
            DataClassSingleNum datumClearPCB = DCHelper.lightCvtOrRetDCMFPBool(listParams.pollLast());
            boolean bClearPaintingCallbacks = datumClearPCB.getDataValue().booleanValue();
                        
            display.setSnapshotAsBackground(bUpdateScreen, bClearPaintingCallbacks);
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_display_snapshot_as_bgrndFunction());}
    
    public static class Get_display_snapshotFunction extends BaseBuiltInFunction {

        public Get_display_snapshotFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::get_display_snapshot";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 4;	// display, update display, width scaling ratio, height scaling ratio
            mnMinParamNum = 2;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Display2D display = get2DDisplay(listParams.pollLast(), false);
                        
            DataClassSingleNum updateScreen = DCHelper.lightCvtOrRetDCMFPBool(listParams.pollLast());
            
            double dWidthRatio = 1.0, dHeightRatio = 1.0;
            if (listParams.size() > 0) {
            	dWidthRatio = dHeightRatio = DCHelper.lightCvtOrRetDCMFPDec(listParams.pollLast()).getDataValue().doubleValue();
            }
            if (listParams.size() > 0) {
            	dHeightRatio = DCHelper.lightCvtOrRetDCMFPDec(listParams.pollLast()).getDataValue().doubleValue();
            }
            if (dWidthRatio <= 0 || dHeightRatio <= 0) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            DataClass datumReturn = display.getSnapshotImage(updateScreen.getDataValue().booleanValue(), dWidthRatio, dHeightRatio);
            
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_display_snapshotFunction());}

    public static class Set_display_sizeFunction extends BaseBuiltInFunction {

        public Set_display_sizeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::set_display_size";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3;	// window handle, width and height
            mnMinParamNum = 3;
        }

        // this function may do nothing in Android.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Integer nHeight = BuiltInFunctionLib.getInteger(listParams.poll());
            Integer nWidth = BuiltInFunctionLib.getInteger(listParams.poll());
            if (nWidth == null || nHeight == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else if (nWidth <= 0 || nHeight <= 0) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            Display2D display = get2DDisplay(listParams.poll(), false);
            display.setDisplaySize(nWidth, nHeight);
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_display_sizeFunction());}

    public static class Get_display_sizeFunction extends BaseBuiltInFunction {

        public Get_display_sizeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::get_display_size";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing in Android.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Display2D display = get2DDisplay(listParams.poll(), false);
            int[] sizeArray = display.getDisplaySize();

            DataClass datumWidth = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(sizeArray[0]));
            DataClass datumHeight = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(sizeArray[1]));
            DataClass[] datumarrayWH = new DataClass[]{
                datumWidth,
                datumHeight
            };
            DataClass datumWH = new DataClassArray(datumarrayWH);
            return datumWH;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_display_sizeFunction());}

    public static class Set_display_resizableFunction extends BaseBuiltInFunction {

        public Set_display_resizableFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::set_display_resizable";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;	// First parameter is window handle, second is resizable
            mnMinParamNum = 2;
        }

        // this function may do nothing in Android.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Boolean bResizable = BuiltInFunctionLib.getBoolean(listParams.poll());
            if (bResizable == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            Display2D display = get2DDisplay(listParams.poll(), false);
            
            display.setDisplayResizable(bResizable);
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_display_resizableFunction());}

    public static class Get_display_resizableFunction extends BaseBuiltInFunction {

        public Get_display_resizableFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::get_display_resizable";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing in Android.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Display2D display = get2DDisplay(listParams.poll(), false);
            Boolean bResizable = display.getDisplayResizable();
            DataClass datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, new MFPNumeric(bResizable));
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_display_resizableFunction());}

    public static class Set_display_captionFunction extends BaseBuiltInFunction {

        public Set_display_captionFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::set_display_caption";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;	// first parameter is handle of window, second is the caption.
            mnMinParamNum = 2;
        }

        // this function may do nothing in Android.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumCaption = DCHelper.lightCvtOrRetDCString(listParams.poll());
            String strCaption = datumCaption.getStringValue();

            Display2D display = get2DDisplay(listParams.poll(), false);
            display.setDisplayCaption(strCaption);
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_display_captionFunction());}

    public static class Get_display_captionFunction extends BaseBuiltInFunction {

        public Get_display_captionFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::get_display_caption";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing in Android.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Display2D display = get2DDisplay(listParams.poll(), false);
            String strCaption = display.getDisplayCaption();
            DataClass datumReturn = new DataClassString(strCaption);
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_display_captionFunction());}

    public static class Set_display_confirm_closeFunction extends BaseBuiltInFunction {

        public Set_display_confirm_closeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::set_display_confirm_close";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;	// first parameter is handle of window, second is true or false.
            mnMinParamNum = 2;
        }

        // this function may do nothing in Android.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumConfirmOrNot = DCHelper.lightCvtOrRetDCMFPBool(listParams.poll());
            boolean bConfirmOrNot = datumConfirmOrNot.getDataValue().booleanValue();

            Display2D display = get2DDisplay(listParams.poll(), false);
            display.setDisplayConfirmClose(bConfirmOrNot);
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_display_confirm_closeFunction());}

    public static class Get_display_confirm_closeFunction extends BaseBuiltInFunction {

        public Get_display_confirm_closeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::get_display_confirm_close";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing in Android.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Display2D display = get2DDisplay(listParams.poll(), false);
            boolean bConfirmClose = display.getDisplayConfirmClose();
            DataClass datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, new MFPNumeric(bConfirmClose));
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_display_confirm_closeFunction());}


    public static class Set_display_orientationFunction extends BaseBuiltInFunction {

        public Set_display_orientationFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::set_display_orientation";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;	// first parameter is handle of window, second is orientation.
            mnMinParamNum = 2;
        }

        // this function may do nothing in Android.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumOrientation = DCHelper.lightCvtOrRetDCMFPInt(listParams.poll());
            int orientation = datumOrientation.getDataValue().intValue();

            Display2D display = get2DDisplay(listParams.poll(), false);
            display.setDisplayOrientation(orientation);
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_display_orientationFunction());}

    public static class Get_display_orientationFunction extends BaseBuiltInFunction {

        public Get_display_orientationFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::display::get_display_orientation";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing in Android.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Display2D display = get2DDisplay(listParams.poll(), false);
            int orientation = display.getDisplayOrientation();
            DataClass datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(orientation));
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_display_orientationFunction());}

}
