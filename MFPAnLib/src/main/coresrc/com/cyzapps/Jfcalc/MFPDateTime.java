/*
 * MFP project, MFPDateTime.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.Locale;

/**
 *
 * @author tonyc
 */
public class MFPDateTime {
    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + MFPDateTime.class.getName());
        }
    }
    
    public static class NowFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public NowFunction() {
            mstrProcessedNameWithFullCS = "::mfp::time_date::now";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 0;
            mnMinParamNum = 0;
        }
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() != mnMinParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            long lTS = System.currentTimeMillis();
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(lTS));
        }
    }
    //public final static NowFunction BUILTINFUNC_Now = new NowFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new NowFunction());}
    
    public static class Get_time_stampFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_time_stampFunction() {
            mstrProcessedNameWithFullCS = "::mfp::time_date::get_time_stamp";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 7;
            mnMinParamNum = 0;
        }
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            String strTime = "1970-01-01 00:00:00.0";
            if (listParams.size() == 1 && listParams.getFirst().getThisOrNull() instanceof DataClassString) {
                DataClass datum = listParams.removeLast();
                strTime = DCHelper.lightCvtOrRetDCString(datum).getStringValue();    //datum must be a DataClassBuiltIn. 
            } else {
                int nYear = 1970, nMonth = 1, nDay = 1, nHour = 0, nMinute = 0, nSecond = 0, nMilliSecond = 0;
                if (listParams.size() > 0) {
                    DataClassSingleNum datum = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                    nYear = datum.getDataValue().intValue();
                }
                if (listParams.size() > 0) {
                    DataClassSingleNum datum = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                    nMonth = datum.getDataValue().intValue();
                }
                if (listParams.size() > 0) {
                    DataClassSingleNum datum = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                    nDay = datum.getDataValue().intValue();
                }
                if (listParams.size() > 0) {
                    DataClassSingleNum datum = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                    nHour = datum.getDataValue().intValue();
                }
                if (listParams.size() > 0) {
                    DataClassSingleNum datum = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                    nMinute = datum.getDataValue().intValue();
                }
                if (listParams.size() > 0) {
                    DataClassSingleNum datum = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                    nSecond = datum.getDataValue().intValue();
                }
                if (listParams.size() > 0) {
                    DataClassSingleNum datum = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                    nMilliSecond = datum.getDataValue().intValue();
                }
                Formatter formatter = null;
                try {
                    StringBuilder sb = new StringBuilder();
                    formatter = new Formatter(sb, Locale.US);
                    String strFormat = "%04d-%02d-%02d %02d:%02d:%02d.%03d";
                    formatter.format(strFormat, nYear, nMonth, nDay, nHour, nMinute, nSecond, nMilliSecond);
                    strTime = sb.toString();
                } catch(Exception e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                } finally {
                    if (formatter != null) {
                        formatter.close();
                    }
                }
            }
            try {
                Timestamp ts = Timestamp.valueOf(strTime);
                long lTS = ts.getTime();
                return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(lTS));
            } catch(IllegalArgumentException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
        }
    }
    //public final static Get_time_stampFunction BUILTINFUNC_Get_time_stamp = new Get_time_stampFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_time_stampFunction());}
    
    public static class Get_yearFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_yearFunction() {
            mstrProcessedNameWithFullCS = "::mfp::time_date::get_year";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() != mnMinParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumTS = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
            long lTS = datumTS.getDataValue().longValue();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lTS);
            int nYear = cal.get(Calendar.YEAR);
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nYear));
        }
    }
    //public final static Get_yearFunction BUILTINFUNC_Get_year = new Get_yearFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_yearFunction());}
    
    public static class Get_monthFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_monthFunction() {
            mstrProcessedNameWithFullCS = "::mfp::time_date::get_month";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() != mnMinParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumTS = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
            long lTS = datumTS.getDataValue().longValue();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lTS);
            int nReturn = cal.get(Calendar.MONTH);
            int nMonth = 1;
            switch (nReturn) {
                case (Calendar.JANUARY): {
                    nMonth = 1;
                    break;
                } case (Calendar.FEBRUARY): {
                    nMonth = 2;
                    break;
                } case (Calendar.MARCH): {
                    nMonth = 3;
                    break;
                } case (Calendar.APRIL): {
                    nMonth = 4;
                    break;
                } case (Calendar.MAY): {
                    nMonth = 5;
                    break;
                } case (Calendar.JUNE): {
                    nMonth = 6;
                    break;
                } case (Calendar.JULY): {
                    nMonth = 7;
                    break;
                } case (Calendar.AUGUST): {
                    nMonth = 8;
                    break;
                } case (Calendar.SEPTEMBER): {
                    nMonth = 9;
                    break;
                } case (Calendar.OCTOBER): {
                    nMonth = 10;
                    break;
                } case (Calendar.NOVEMBER): {
                    nMonth = 11;
                    break;
                } case (Calendar.DECEMBER): {
                    nMonth = 12;
                    break;
                }
            }
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nMonth));
        }
    }
    //public final static Get_monthFunction BUILTINFUNC_Get_month = new Get_monthFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_monthFunction());}
    
    public static class Get_day_of_yearFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_day_of_yearFunction() {
            mstrProcessedNameWithFullCS = "::mfp::time_date::get_day_of_year";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() != mnMinParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumTS = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
            long lTS = datumTS.getDataValue().longValue();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lTS);
            int nReturn = cal.get(Calendar.DAY_OF_YEAR);
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nReturn));
        }
    }
    //public final static Get_day_of_yearFunction BUILTINFUNC_Get_day_of_year = new Get_day_of_yearFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_day_of_yearFunction());}
    
    public static class Get_day_of_monthFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_day_of_monthFunction() {
            mstrProcessedNameWithFullCS = "::mfp::time_date::get_day_of_month";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() != mnMinParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumTS = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
            long lTS = datumTS.getDataValue().longValue();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lTS);
            int nReturn = cal.get(Calendar.DATE);
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nReturn));
        }
    }
    //public final static Get_day_of_monthFunction BUILTINFUNC_Get_day_of_month = new Get_day_of_monthFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_day_of_monthFunction());}
    
    public static class Get_day_of_weekFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_day_of_weekFunction() {
            mstrProcessedNameWithFullCS = "::mfp::time_date::get_day_of_week";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() != mnMinParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumTS = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
            long lTS = datumTS.getDataValue().longValue();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lTS);
            int nReturn = cal.get(Calendar.DAY_OF_WEEK);
            int nDayOfWeek = 0;
            switch (nReturn) {
                case (Calendar.SUNDAY): {
                    nDayOfWeek = 0;
                    break;
                } case (Calendar.MONDAY): {
                    nDayOfWeek = 1;
                    break;
                } case (Calendar.TUESDAY): {
                    nDayOfWeek = 2;
                    break;
                } case (Calendar.WEDNESDAY): {
                    nDayOfWeek = 3;
                    break;
                } case (Calendar.THURSDAY): {
                    nDayOfWeek = 4;
                    break;
                } case (Calendar.FRIDAY): {
                    nDayOfWeek = 5;
                    break;
                } case (Calendar.SATURDAY): {
                    nDayOfWeek = 6;
                    break;
                }
            }
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nDayOfWeek));
        }
    }
    //public final static Get_day_of_weekFunction BUILTINFUNC_Get_day_of_week = new Get_day_of_weekFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_day_of_weekFunction());}
    
    public static class Get_hourFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_hourFunction() {
            mstrProcessedNameWithFullCS = "::mfp::time_date::get_hour";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() != mnMinParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumTS = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
            long lTS = datumTS.getDataValue().longValue();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lTS);
            int nReturn = cal.get(Calendar.HOUR_OF_DAY);
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nReturn));
        }
    }
    //public final static Get_hourFunction BUILTINFUNC_Get_hour = new Get_hourFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_hourFunction());}
    
    public static class Get_minuteFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_minuteFunction() {
            mstrProcessedNameWithFullCS = "::mfp::time_date::get_minute";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() != mnMinParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumTS = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
            long lTS = datumTS.getDataValue().longValue();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lTS);
            int nReturn = cal.get(Calendar.MINUTE);
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nReturn));
        }
    }
    //public final static Get_minuteFunction BUILTINFUNC_Get_minute = new Get_minuteFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_minuteFunction());}
    
    public static class Get_secondFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_secondFunction() {
            mstrProcessedNameWithFullCS = "::mfp::time_date::get_second";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() != mnMinParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumTS = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
            long lTS = datumTS.getDataValue().longValue();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lTS);
            int nReturn = cal.get(Calendar.SECOND);
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nReturn));
        }
    }
    //public final static Get_secondFunction BUILTINFUNC_Get_second = new Get_secondFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_secondFunction());}
    
    public static class Get_millisecondFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_millisecondFunction() {
            mstrProcessedNameWithFullCS = "::mfp::time_date::get_millisecond";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() != mnMinParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumTS = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
            long lTS = datumTS.getDataValue().longValue();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lTS);
            int nReturn = cal.get(Calendar.MILLISECOND);
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nReturn));
        }
    }
    //public final static Get_millisecondFunction BUILTINFUNC_Get_millisecond = new Get_millisecondFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_millisecondFunction());}
}
