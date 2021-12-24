// MFP project, EventLib.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.JGI2D;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClassExtObjRef;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Oomfp.CitingSpaceDefinition;

/**
 * Event defintion: At this moment (before oop is fully supported), Event is a
 * 2-element array. The first element is an integer which defines event type.
 * The second element is a dictionary implemented by a n*2 array. The key of the
 * dictionary is a string and the value is the field info of the event.
 *
 * @author tony
 *
 */
public class EventLib {
    
    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + EventLib.class.getName());
        }
    }
    
    public static class Pull_eventFunction extends BaseBuiltInFunction {

        public Pull_eventFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::event::pull_event";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;  // need display 2D as the sole parameter
            mnMinParamNum = 1;
        }

        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }

            Display2D display = DisplayLib.get2DDisplay(listParams.pollLast(), false);
            
            GIEvent giEvent = display.pullGIEvent();
            DataClass datumReturn = (giEvent==null)?new DataClassNull():new DataClassExtObjRef(giEvent);
            // return an array, [0] is event type, [1] is event info. And the array format should be hidden from user.
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Pull_eventFunction());}

    public static class Get_event_typeFunction extends BaseBuiltInFunction {

        public Get_event_typeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::event::get_event_type";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function read the type of an event from return of pull_event..
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }

            DataClassExtObjRef datumEvent = DCHelper.lightCvtOrRetDCExtObjRef(listParams.poll());	// no need to deep copy coz no change in parameter.
            if (!(datumEvent.getExternalObject() instanceof GIEvent)) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            }
            GIEvent giEvent = (GIEvent)datumEvent.getExternalObject();
            int typeValue = giEvent.menumEventType.getValue();
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(typeValue));
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_event_typeFunction());}

    public static class Get_event_type_nameFunction extends BaseBuiltInFunction {

        public Get_event_type_nameFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::event::get_event_type_name";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function read the type of an event from return of pull_event..
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }

            DataClassExtObjRef datumEvent = DCHelper.lightCvtOrRetDCExtObjRef(listParams.poll());	// no need to deep copy coz no change in parameter.
            if (!(datumEvent.getExternalObject() instanceof GIEvent)) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            }
            GIEvent giEvent = (GIEvent)datumEvent.getExternalObject();
            String typeName = giEvent.menumEventType.toString();
            return new DataClassString(typeName);
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_event_type_nameFunction());}

    public static class Get_event_infoFunction extends BaseBuiltInFunction {

        public Get_event_infoFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::event::get_event_info";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 2;
        }

        // this function read the type of an event from return of pull_event..
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumPropertyName = DCHelper.lightCvtOrRetDCString(listParams.poll());
            String strPropertyName = datumPropertyName.getStringValue();
            DataClassExtObjRef datumEvent = DCHelper.lightCvtOrRetDCExtObjRef(listParams.poll());	// no need to deep copy coz no change in parameter.
            if (!(datumEvent.getExternalObject() instanceof GIEvent)) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            }

            DataClass datumReturn;
            GIEvent giEvent = (GIEvent)datumEvent.getExternalObject();
            if (strPropertyName.equalsIgnoreCase("type")) {
                datumReturn = new DataClassSingleNum(
                        DATATYPES.DATUM_MFPINT,
                        new MFPNumeric(giEvent.menumEventType.getValue())
                );
            } else if (strPropertyName.equalsIgnoreCase("type_name")) {
                datumReturn = new DataClassString(giEvent.menumEventType.toString());
            } else {
                // do not deep copy coz info may be used transfer some data to modify.
                datumReturn = giEvent.getInfo(strPropertyName);
            }
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_event_infoFunction());}
}
