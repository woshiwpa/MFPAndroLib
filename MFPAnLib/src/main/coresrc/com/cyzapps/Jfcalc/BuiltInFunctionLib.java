/*
 * MFP project, BuiltInFunctionLib.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.DCHelper.CurPos;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.AbstractExpr.SimplifyParams;
import com.cyzapps.Jsma.ExprAnalyzer;
import com.cyzapps.Jsma.SMErrProcessor;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.Oomfp.MemberFunction;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author tonyc
 */
public class BuiltInFunctionLib {
    
    
    public static Boolean getBoolean(DataClass datum) {
        // note that this function deep copy datum
        try {
            DataClassSingleNum datumCpy = DCHelper.lightCvtOrRetDCMFPBool(datum);
            return datumCpy.getDataValue().booleanValue();
        } catch (JFCALCExpErrException e) {
            return null;
        }
    }
    
    public static Integer getInteger(DataClass datum) {
        // note that this function deep copy datum
        try {
            DataClassSingleNum datumCpy = DCHelper.lightCvtOrRetDCMFPInt(datum);
            return datumCpy.getDataValue().intValue();
        } catch (JFCALCExpErrException e) {
            return null;
        }
    }
    
    public static Long getLong(DataClass datum) {
        // note that this function deep copy datum
        try {
            DataClassSingleNum datumCpy = DCHelper.lightCvtOrRetDCMFPInt(datum);
            return datumCpy.getDataValue().longValue();
        } catch (JFCALCExpErrException e) {
            return null;
        }
    }
    
    public static Double getDouble(DataClass datum) {
        // note that this function deep copy datum
        try {
            DataClassSingleNum datumCpy = DCHelper.lightCvtOrRetDCMFPDec(datum);
            return datumCpy.getDataValue().doubleValue();
        } catch (JFCALCExpErrException e) {
            return null;
        }
    }

    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + BuiltInFunctionLib.class.getName());
        }
    }
    
    // definition for built in functions.
    public static abstract class BaseBuiltInFunction extends MemberFunction {
		private static final long serialVersionUID = 1L;
        // member variables of BaseBuiltInFunction are all protected so that
        // no way to change out of the class.
		protected String mstrProcessedNameWithFullCS = "";
        protected String[] mstrarrayFullCS = new String[0];
        public String[] getFullCS() { return mstrarrayFullCS; }
        protected int mnMaxParamNum = 0;
        @Override
        public int getMaxNumParam() { return mnMaxParamNum; }
        protected int mnMinParamNum = 0;
        @Override
        public int getMinNumParam() { return mnMinParamNum; }
        @Override
        public boolean matchFunctionCall(String strShrinkedRawName, int nParamNum,   // function 1
                                         List<String[]> lCitingSpaces) {    // citing spaces
            return matchFunctionCall(strShrinkedRawName, nParamNum,
                    mstrProcessedNameWithFullCS, mnMinParamNum, mnMaxParamNum, lCitingSpaces);
        }
        @Override
        public boolean matchFunctionDef(String strShrinkedRawName, int nParamNum, boolean bWithOptionalParam,   // function 1
                                        List<String[]> lCitingSpaces) {    // citing spaces
            return matchFunctionDef(strShrinkedRawName, nParamNum, bWithOptionalParam,
                    mstrProcessedNameWithFullCS, mnMinParamNum, mnMaxParamNum, lCitingSpaces);
        }
        @Override
        public String getAbsNameWithCS() {
            return mstrProcessedNameWithFullCS;
        }
        @Override
        public String getPureNameWithoutCS() {
            return mstrarrayFullCS[mstrarrayFullCS.length - 1];
        }
        @Override
        public boolean isIncludeOptParam() {
            return mnMinParamNum != mnMaxParamNum;
        }

        public BaseBuiltInFunction() {}
        public abstract DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext)
                throws JFCALCExpErrException, InterruptedException;
    }
    
    /* rand */
    public static class RandFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public RandFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::stat_stoch::rand";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 0;
            mnMinParamNum = 0;
        }
        
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (tsParameter.size() != mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            // avoid to use MFPNumeric(double), instead use MFPNumeric(string) because bigDecimal(double) is much slower than bigDecimal(string)
            DataClass datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(Math.random(), true));
            return datumReturnNum;
        }
    }
    //public final static RandFunction BUILTINFUNC_Rand = new RandFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new RandFunction());}
    
    public static class CeilFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public CeilFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::ceil";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 1;
        }
        
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* ceil */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            int nScale = 0;
            if (tsParameter.size() == 2)    {
                DataClassSingleNum dsScale = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());
                nScale = (int)dsScale.getDataValue().longValue();
                if (nScale < 0 || nScale == Long.MAX_VALUE)    {   //nScale == Long.MAX_VALUE means overflow.
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                }
            }
            // make sure parameter variables not changed inside function
            DataClassSingleNum dsTmp = DCHelper.lightCvtOrRetDCMFPDec(tsParameter.poll());
            datumReturnNum =  new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, dsTmp.getDataValue().setScale(nScale, MFPNumeric.ROUND_CEILING));
            return datumReturnNum;
        }
    }
    //public final static CeilFunction BUILTINFUNC_Ceil = new CeilFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new CeilFunction());}
    
    public static class FloorFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public FloorFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::floor";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 1;
        }
        
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* floor */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            int nScale = 0;
            if (tsParameter.size() == 2)    {
                DataClassSingleNum dsScale = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());
                nScale = (int)dsScale.getDataValue().longValue();
                if (nScale < 0 || nScale == Long.MAX_VALUE)    {   //nScale == Long.MAX_VALUE means overflow.
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                }
            }
            // make sure parameter variables not changed inside function
            DataClassSingleNum dsTmp = DCHelper.lightCvtOrRetDCMFPDec(tsParameter.poll());
            datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, dsTmp.getDataValue().setScale(nScale, MFPNumeric.ROUND_FLOOR));
            return datumReturnNum;
        }
    }
    //public final static FloorFunction BUILTINFUNC_Floor = new FloorFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new FloorFunction());}
    
    public static class RoundFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public RoundFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::round";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 1;
        }
        
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* round */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            int nScale = 0;
            if (tsParameter.size() == 2)    {
                DataClassSingleNum dsScale = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());
                nScale = (int)dsScale.getDataValue().longValue();
                if (nScale < 0 || nScale == Long.MAX_VALUE)    {   //nScale == Long.MAX_VALUE means overflow.
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                }
            }
            // make sure parameter variables not changed inside function
            DataClassSingleNum dsTmp = DCHelper.lightCvtOrRetDCMFPDec(tsParameter.poll());
            datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, dsTmp.getDataValue().setScale(nScale, MFPNumeric.ROUND_HALF_UP));
            return datumReturnNum;
        }
    }
    //public final static RoundFunction BUILTINFUNC_Round = new RoundFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new RoundFunction());}
    
    public static class AndFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

		public AndFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::logic::and";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = -1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* logic and */
        {
            if (tsParameter.size() < mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.ONE);
            do
            {
                //process the parameters from first to last
                DataClassSingleNum dsTmp = DCHelper.lightCvtOrRetDCMFPBool(tsParameter.removeLast());
                if (dsTmp.getDataValue().isActuallyZero())    {
                    datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.ZERO);
                    break;
                }
            } while (tsParameter.size() != 0);
            return datumReturnNum;
        }
    }
    //public final static AndFunction BUILTINFUNC_And = new AndFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new AndFunction());}
        
    public static class OrFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

		public OrFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::logic::or";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = -1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* logic or */
        {
            if (tsParameter.size() < mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.ZERO);
            do
            {
                //process the parameters from first to last
                // after change data type to boolean, long value should be 1 or 0.
                DataClassSingleNum dsTmp = DCHelper.lightCvtOrRetDCMFPBool(tsParameter.removeLast());
                if (dsTmp.getDataValue().isTrue())    {   // need not to use is actually true here.
                    datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
                    break;
                }
            } while (tsParameter.size() != 0);
            return datumReturnNum;
        }
    }    
    //public final static OrFunction BUILTINFUNC_Or = new OrFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new OrFunction());}
        
    public static class SinFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public SinFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::trigon::sin";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* sin */
        {
            DataClass datumReturnNum = new DataClassNull();
            // doesn't support complex yet.
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            // need not to copy type and value here because inside evaluateSin parameter will not be changed.
            datumReturnNum = BuiltinProcedures.evaluateSin(tsParameter.poll());
            return datumReturnNum;
        }
    }    
    //public final static SinFunction BUILTINFUNC_Sin = new SinFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new SinFunction());}
        
    public static class CosFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public CosFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::trigon::cos";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* cos */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            // need not to copy type and value here because inside evaluateCos parameter will not be changed.
            datumReturnNum = BuiltinProcedures.evaluateCos(tsParameter.poll());
            return datumReturnNum;
        }
    }    
    //public final static CosFunction BUILTINFUNC_Cos = new CosFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new CosFunction());}
        
    public static class TanFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public TanFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::trigon::tan";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* tan */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            // need not to copy type and value here because inside evaluateTan parameter will not be changed.
            datumReturnNum = BuiltinProcedures.evaluateTan(tsParameter.poll());
            return datumReturnNum;
        }
    }    
    //public final static TanFunction BUILTINFUNC_Tan = new TanFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new TanFunction());}
        
    public static class AsinFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public AsinFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::trigon::asin";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* arcsin */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            // need not to copy type and value here because inside evaluateASin parameter will not be changed.
            datumReturnNum = BuiltinProcedures.evaluateASin(tsParameter.poll());
            return datumReturnNum;
        }
    }    
    //public final static AsinFunction BUILTINFUNC_Asin = new AsinFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new AsinFunction());}
        
    public static class AcosFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public AcosFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::trigon::acos";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* arccos */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            // need not to copy type and value here because inside evaluateACos parameter will not be changed.
            datumReturnNum = BuiltinProcedures.evaluateACos(tsParameter.poll());
            return datumReturnNum;
        }
    }    
    //public final static AcosFunction BUILTINFUNC_Acos = new AcosFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new AcosFunction());}
        
    public static class AtanFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public AtanFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::trigon::atan";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* arctan */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            // need not to copy type and value here because inside evaluateATan parameter will not be changed.
            datumReturnNum = BuiltinProcedures.evaluateATan(tsParameter.poll());
            return datumReturnNum;
        }
    }    
    //public final static AtanFunction BUILTINFUNC_Atan = new AtanFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new AtanFunction());}
        
    public static class LogFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public LogFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::log_exp::log";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* logE */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            // need not to copy type and value here because inside evaluateLog parameter will not be changed.
            datumReturnNum = BuiltinProcedures.evaluateLog(tsParameter.poll());
            return datumReturnNum;
        }
    }    
    //public final static LogFunction BUILTINFUNC_Log = new LogFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new LogFunction());}
        
    public static class ExpFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public ExpFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::log_exp::exp";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* exp */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            // need not to copy type and value here because inside evaluateExp parameter will not be changed.
            datumReturnNum = BuiltinProcedures.evaluateExp(tsParameter.poll());
            return datumReturnNum;
        }
    }    
    //public final static ExpFunction BUILTINFUNC_Exp = new ExpFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new ExpFunction());}
        
    public static class RealFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public RealFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::complex::real";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* real */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassComplex dsTmp = DCHelper.lightCvtOrRetDCComplex(tsParameter.poll());
            // no need to do copy or deep copy coz getRealDataClass will not change dsTmp.
            datumReturnNum = dsTmp.getRealDataClass();            
            return datumReturnNum;
        }
    }    
    //public final static RealFunction BUILTINFUNC_Real = new RealFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new RealFunction());}
        
    public static class ImageFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public ImageFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::complex::image";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* image */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            boolean bReturnI = false;
            if (tsParameter.size() == 2) {
                // now determine return an image or a real value
                DataClassSingleNum dsReturnType = DCHelper.lightCvtOrRetDCMFPBool(tsParameter.poll());
                bReturnI = dsReturnType.getDataValue().booleanValue();
            }
            DataClassComplex dsTmp = DCHelper.lightCvtOrRetDCComplex(tsParameter.poll());
            if (bReturnI) {
                // we need to return something like 3*i
                dsTmp = new DataClassComplex(MFPNumeric.ZERO, dsTmp.getImage());
                return dsTmp;
            } else {
                datumReturnNum = dsTmp.getImageDataClass();
                return datumReturnNum;
            }
        }
    }    
    //public final static ImageFunction BUILTINFUNC_Image = new ImageFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new ImageFunction());}
        
    public static class AbsFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

		public AbsFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::complex::abs";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* abs */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            datumReturnNum = BuiltinProcedures.evaluateAbs(tsParameter.poll());
            return datumReturnNum;
        }
    }    
    //public final static AbsFunction BUILTINFUNC_Abs = new AbsFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new AbsFunction());}
        
    public static class AngleFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public AngleFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::complex::angle";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* angle */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassComplex dsTmp = DCHelper.lightCvtOrRetDCComplex(tsParameter.poll());
            MFPNumeric[] mfpNumRadAng = dsTmp.getComplexRadAngle();
            datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumRadAng[1]);
            return datumReturnNum;
        }
    }    
    //public final static AngleFunction BUILTINFUNC_Angle = new AngleFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new AngleFunction());}
        
    public static class ModFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public ModFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::mod";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* mod */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum dsTmp1 = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());    /* cast to integer */
            DataClassSingleNum dsTmp2 = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());    /* cast to integer */
            if (dsTmp1.getDataValue().compareTo(MFPNumeric.ZERO) <= 0)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            datumReturnNum = new DataClassSingleNum(    // here dsTmp1 and dsTmp2 have been changed to MFP_INTEGER_TYPE, so can use toBigInteger() directly
                DATATYPES.DATUM_MFPINT,
                new MFPNumeric(dsTmp2.getDataValue().toBigInteger().mod(dsTmp1.getDataValue().toBigInteger())));
            return datumReturnNum;
        }
    }    
    //public final static ModFunction BUILTINFUNC_Mod = new ModFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new ModFunction());}
        
    /* convert bin dec hex to another positional notation */
    public static class Conv_bin_dec_hexFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Conv_bin_dec_hexFunction() {
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            DataClass datumReturnNum = new DataClassNull();
            /* num2 <-> num8 <-> num10 <-> num16, support int and double */
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            int nRadixFrom = 10, nRadixTo = 16;
            String strPureFuncName = mstrarrayFullCS[mstrarrayFullCS.length - 1];
            if (strPureFuncName.compareTo("conv_bin_to_dec") == 0)   {
                nRadixFrom = 2;
                nRadixTo = 10;
            } else if (strPureFuncName.compareTo("conv_oct_to_dec") == 0)  {
                nRadixFrom = 8;
                nRadixTo = 10;
            } else if (strPureFuncName.compareTo("conv_hex_to_dec") == 0)  {
                nRadixFrom = 16;
                nRadixTo = 10;
            } else if (strPureFuncName.compareTo("conv_dec_to_bin") == 0)  {
                nRadixFrom = 10;
                nRadixTo = 2;
            } else if (strPureFuncName.compareTo("conv_dec_to_oct") == 0)  {
                nRadixFrom = 10;
                nRadixTo = 8;
            } else if (strPureFuncName.compareTo("conv_dec_to_hex") == 0)  {
                nRadixFrom = 10;
                nRadixTo = 16;
            } else if (strPureFuncName.compareTo("conv_bin_to_hex") == 0)  {
                nRadixFrom = 2;
                nRadixTo = 16;
            } else if (strPureFuncName.compareTo("conv_hex_to_bin") == 0)   {
                nRadixFrom = 16;
                nRadixTo = 2;
            } else if (strPureFuncName.compareTo("conv_bin_to_oct") == 0)    {
                nRadixFrom = 2;
                nRadixTo = 8;
            } else if (strPureFuncName.compareTo("conv_oct_to_bin") == 0)    {
                nRadixFrom = 8;
                nRadixTo = 2;
            } else if (strPureFuncName.compareTo("conv_oct_to_hex") == 0)   {
                nRadixFrom = 8;
                nRadixTo = 16;
            } else if (strPureFuncName.compareTo("conv_hex_to_oct") == 0)   {
                nRadixFrom = 16;
                nRadixTo = 8;
            }
            DataClass dsTmp = tsParameter.poll().cloneSelf();
            // only support 2 input data types: String or binary double
            if (!(dsTmp.getThisOrNull() instanceof DataClassString))    {
                dsTmp = DCHelper.lightCvtOrRetDCMFPDec(dsTmp);
                datumReturnNum = dsTmp;
            } else  {
                // input is a string. Note that if input is a string, we don't allow blank in the head or tail.
                String strInput = DCHelper.lightCvtOrRetDCString(dsTmp).getStringValue();
                if (nRadixFrom == 2)    {
                    strInput = "0b" + strInput;
                } else if (nRadixFrom == 8) {
                    strInput = "00" + strInput; // have to use two 0s to cope with the case like .317
                } else if (nRadixFrom == 16)    {
                    strInput = "0x" + strInput;
                }
                CurPos curPos = new CurPos();
                datumReturnNum = ElemAnalyzer.getNumber(strInput, curPos);
                if (curPos.m_nPos < strInput.length())  {   // only part of the input is a number, which is not accepted.
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_FORMAT);
                }
                datumReturnNum = DCHelper.lightCvtOrRetDCMFPDec(datumReturnNum);
            }
            // now datumReturnNum is a double.
            String strOutput = "";
            if (nRadixTo == 10) {   // if return decimal, return a number value, otherwise return a string.
                if (DCHelper.isSingleInteger(datumReturnNum))   {
                    datumReturnNum = DCHelper.lightCvtOrRetDCMFPInt(datumReturnNum);
                }
            } else {
                if (DCHelper.isSingleInteger(datumReturnNum))   {
                    BigInteger bigIntValue = DCHelper.lightCvtOrRetDCSingleNum(datumReturnNum).getDataValue().toBigInteger();
                    strOutput = bigIntValue.toString(nRadixTo);
                } else  {
                    MFPNumeric mfpNumValue = DCHelper.lightCvtOrRetDCSingleNum(datumReturnNum).getDataValue();
                    BigInteger bigIntValue = mfpNumValue.toBigInteger();
                    strOutput = bigIntValue.toString(nRadixTo) + ".";
                    mfpNumValue = mfpNumValue.subtract(new MFPNumeric(bigIntValue));
                    double dDenominator = 1.0;
                    while (mfpNumValue.abs().isActuallyPositive())  {
                        dDenominator /= nRadixTo;
                        // cannot use MFPNumeric.divide(MFPNumeric, MFPNumeric) here because when dDenominator is very close to zero
                        // it will treated as zero.
                        //int nQuotient = (int)mfpNumValue.divide(new MFPNumeric(dDenominator)).longValue();
                        // also, avoid to use BigDecimal.divide(BigDecimal) to avoid exception.
                        // also because nRadixTo = 2, 8 or 16 and dDenominator's original value is 1.0,
                        // dDenominator is accurate and BigDecimal(dDenominator) == BigDecimal.valueOf(dDenominator)
                        int nQuotient = (int)MFPNumeric.divide(mfpNumValue.toBigDecimal(), new BigDecimal(dDenominator)).longValue();
                        if (nQuotient < 10) {
                            strOutput += nQuotient;
                        } else  {
                            strOutput += (char)('a' + nQuotient - 10);
                        }
                        // do not use MFPNumeric string here because dDenominator can be accurately represented,
                        // using Double.string will round it.
                        mfpNumValue = mfpNumValue.subtract(new MFPNumeric(dDenominator * nQuotient, false));
                    }
                }
                datumReturnNum = new DataClassString(strOutput);
            }
            return datumReturnNum;
        }
    }
    
    public static class Conv_bin_to_decFunction extends Conv_bin_dec_hexFunction {
		private static final long serialVersionUID = 1L;

        public Conv_bin_to_decFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::conv_bin_to_dec";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Conv_bin_to_decFunction BUILTINFUNC_Conv_bin_to_dec = new Conv_bin_to_decFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Conv_bin_to_decFunction());}
    
    public static class Conv_oct_to_decFunction extends Conv_bin_dec_hexFunction {
		private static final long serialVersionUID = 1L;

        public Conv_oct_to_decFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::conv_oct_to_dec";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Conv_oct_to_decFunction BUILTINFUNC_Conv_oct_to_dec = new Conv_oct_to_decFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Conv_oct_to_decFunction());}
    
    public static class Conv_hex_to_decFunction extends Conv_bin_dec_hexFunction {
		private static final long serialVersionUID = 1L;

        public Conv_hex_to_decFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::conv_hex_to_dec";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Conv_hex_to_decFunction BUILTINFUNC_Conv_hex_to_dec = new Conv_hex_to_decFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Conv_hex_to_decFunction());}
    
    public static class Conv_dec_to_binFunction extends Conv_bin_dec_hexFunction {
		private static final long serialVersionUID = 1L;

        public Conv_dec_to_binFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::conv_dec_to_bin";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Conv_dec_to_binFunction BUILTINFUNC_Conv_dec_to_bin = new Conv_dec_to_binFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Conv_dec_to_binFunction());}
    
    public static class Conv_dec_to_octFunction extends Conv_bin_dec_hexFunction {
		private static final long serialVersionUID = 1L;

        public Conv_dec_to_octFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::conv_dec_to_oct";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Conv_dec_to_octFunction BUILTINFUNC_Conv_dec_to_oct = new Conv_dec_to_octFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Conv_dec_to_octFunction());}
    
    public static class Conv_dec_to_hexFunction extends Conv_bin_dec_hexFunction {
		private static final long serialVersionUID = 1L;

        public Conv_dec_to_hexFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::conv_dec_to_hex";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Conv_dec_to_hexFunction BUILTINFUNC_Conv_dec_to_hex = new Conv_dec_to_hexFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Conv_dec_to_hexFunction());}
    
    public static class Conv_bin_to_hexFunction extends Conv_bin_dec_hexFunction {
		private static final long serialVersionUID = 1L;

        public Conv_bin_to_hexFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::conv_bin_to_hex";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Conv_bin_to_hexFunction BUILTINFUNC_Conv_bin_to_hex = new Conv_bin_to_hexFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Conv_bin_to_hexFunction());}
    
    public static class Conv_hex_to_binFunction extends Conv_bin_dec_hexFunction {
		private static final long serialVersionUID = 1L;

        public Conv_hex_to_binFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::conv_hex_to_bin";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Conv_hex_to_binFunction BUILTINFUNC_Conv_hex_to_bin = new Conv_hex_to_binFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Conv_hex_to_binFunction());}
    
    public static class Conv_bin_to_octFunction extends Conv_bin_dec_hexFunction {
		private static final long serialVersionUID = 1L;

        public Conv_bin_to_octFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::conv_bin_to_oct";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Conv_bin_to_octFunction BUILTINFUNC_Conv_bin_to_oct = new Conv_bin_to_octFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Conv_bin_to_octFunction());}
    
    public static class Conv_oct_to_binFunction extends Conv_bin_dec_hexFunction {
		private static final long serialVersionUID = 1L;

        public Conv_oct_to_binFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::conv_oct_to_bin";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Conv_oct_to_binFunction BUILTINFUNC_Conv_oct_to_bin = new Conv_oct_to_binFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Conv_oct_to_binFunction());}
    
    public static class Conv_oct_to_hexFunction extends Conv_bin_dec_hexFunction {
		private static final long serialVersionUID = 1L;

        public Conv_oct_to_hexFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::conv_oct_to_hex";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Conv_oct_to_hexFunction BUILTINFUNC_Conv_oct_to_hex = new Conv_oct_to_hexFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Conv_oct_to_hexFunction());}
    
    public static class Conv_hex_to_octFunction extends Conv_bin_dec_hexFunction {
		private static final long serialVersionUID = 1L;

        public Conv_hex_to_octFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::conv_hex_to_oct";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Conv_hex_to_octFunction BUILTINFUNC_Conv_hex_to_oct = new Conv_hex_to_octFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Conv_hex_to_octFunction());}
        
    public static class PowFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public PowFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::log_exp::pow";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* pow */
        {
            DataClass datumReturnNum = new DataClassNull();
            int nNumofParams = tsParameter.size();
            if (nNumofParams < mnMinParamNum || nNumofParams > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass dsNumOfOperands = null;
            if (nNumofParams == mnMaxParamNum)    {
                dsNumOfOperands = tsParameter.poll();
            }
            DataClass dsPower = tsParameter.poll();
            DataClass dsBase = tsParameter.poll();
            // need not to copy type and value here because inside evaluatePower parameters will not be changed.
            datumReturnNum = BuiltinProcedures.evaluatePower(dsBase, dsPower, dsNumOfOperands);
            
            return datumReturnNum;
        }
    }    
    //public final static PowFunction BUILTINFUNC_Pow = new PowFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new PowFunction());}
        
    public static class Print_file_listFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Print_file_listFunction() {
            mstrProcessedNameWithFullCS = "::mfp::io::file::print_file_list";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 0;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            String strPath = ".";
            if (tsParameter.size() > mnMinParamNum) {
                DataClassString dsParam = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
                strPath = dsParam.getStringValue();
            }
            LinkedList<String> listOutputs = new LinkedList<String>();
            int nReturn = IOLib.outputFileList(strPath, listOutputs);
            
            if (FuncEvaluator.msstreamLogOutput != null)    {
                for (int idx = 0; idx < listOutputs.size(); idx ++) {
                    FuncEvaluator.msstreamLogOutput.outputString(listOutputs.get(idx) + "\n");
                }
            }
            datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nReturn));    // do not return anything.
            return datumReturnNum;
        }
    }    
    //public final static Print_file_listFunction BUILTINFUNC_Print_file_list = new Print_file_listFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Print_file_listFunction());}

    public static class LsFunction extends Print_file_listFunction {
		private static final long serialVersionUID = 1L;

        public LsFunction() {
            mstrProcessedNameWithFullCS = "::mfp::io::file::ls";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 0;
        }
    }    
    //public final static LsFunction BUILTINFUNC_Ls = new LsFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new LsFunction());}

    public static class DirFunction extends Print_file_listFunction {
		private static final long serialVersionUID = 1L;

        public DirFunction() {
            mstrProcessedNameWithFullCS = "::mfp::io::file::dir";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 0;
        }
    }    
    //public final static DirFunction BUILTINFUNC_Dir = new DirFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new DirFunction());}
    
    /* input */
    public static class InputFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public InputFunction() {
            mstrProcessedNameWithFullCS = "::mfp::io::console::input";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3;	// prompt text, flag, default value
            mnMinParamNum = 1;	// prompt text
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum && tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            boolean bInputString = false;
            DataClass datumDefault = null;
            if (tsParameter.size() > mnMinParamNum)    {
            	if (tsParameter.size() == mnMaxParamNum) {
            		datumDefault = tsParameter.poll();
            	}
                DataClass datumInputType = tsParameter.poll();
                if (datumInputType != null && datumInputType.getThisOrNull() instanceof DataClassString) {
                	String strFlag = DCHelper.lightCvtOrRetDCString(datumInputType).getStringValue();
                	if (strFlag.equalsIgnoreCase("s")) {
                		bInputString = true;
                	} else if (!strFlag.equalsIgnoreCase("default"))	{
                		datumDefault = null;	// do not use default.
                	}
                }
            }
            
            DataClass datumPrompt = tsParameter.poll();    // need not to deep copy coz only output value.
            if (datumPrompt == null)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            datumReturnNum = null;    // do not return anything if no valid input.
            if (FuncEvaluator.msstreamConsoleInput != null)    {
                while (true)    {
                    if (FuncEvaluator.msstreamLogOutput != null)    {
                        if (datumPrompt.getThisOrNull() instanceof DataClassString)    {
                            // should not include the double quote if print's parameter is a string.
                            // but if part of the parameter is a string, e.g. [56, "abc"], double quote
                            // should be used.
                            FuncEvaluator.msstreamLogOutput.outputString(DCHelper.lightCvtOrRetDCString(datumPrompt).getStringValue());
                        } else    {
                            FuncEvaluator.msstreamLogOutput.outputString(datumPrompt.output());
                        }
                    }
                    
                    FuncEvaluator.msstreamConsoleInput.doBeforeInput();
                    String strInput = FuncEvaluator.msstreamConsoleInput.inputString();
                    FuncEvaluator.msstreamConsoleInput.doAfterInput();
                    if (strInput != null)    {
                        if (bInputString)    {
                            datumReturnNum = new DataClassString(strInput);
                        } else if (datumDefault != null && strInput.length() == 0) {
                        	// use default value. Note that default here is default reference, not default clone.
                        	datumReturnNum = datumDefault;
                        } else {
                            DataClassString datumStrExpr = new DataClassString(strInput);
                            LinkedList<Variable> l = new LinkedList<Variable>();
                            progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(l);
                            ExprEvaluator exprEvaluator = new ExprEvaluator(progContext);
                            try    {
                                datumReturnNum = exprEvaluator.evaluateExpression(
                                                            datumStrExpr.getStringValue(), new CurPos());
                            } catch (JFCALCExpErrException e)    {
                                continue;    // invalid input.
                            } finally {
                                progContext.mdynamicProgContext.mlVarNameSpaces.poll();    // this will run before try...catch, i.e. before continue
                            }
                        }
                        break;    // valid input
                    } else if (datumDefault != null) {
                    	// use default value. Note that default here is default reference, not default clone.
                    	datumReturnNum = datumDefault; // will not be here actually
                    	break;
                    } else    {
                        continue;    // invalid input. will not be here actually
                    }
                }
            }
            if (datumReturnNum == null)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
            }            
            return datumReturnNum;
        }
    }    
    //public final static InputFunction BUILTINFUNC_Input = new InputFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new InputFunction());}
        
    public static class PauseFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public PauseFunction() {
            mstrProcessedNameWithFullCS = "::mfp::system::pause";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 0;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* input */, InterruptedException
        {
            DataClass datumReturnNum = null;
            if (tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumPrompt = new DataClassString("");
            if (tsParameter.size() == mnMaxParamNum) {
                datumPrompt = tsParameter.poll();    // need not to deep copy coz only output value.
            }
            if (datumPrompt == null || DCHelper.isDataClassType(datumPrompt, DATATYPES.DATUM_BASE_OBJECT))    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            if (FuncEvaluator.msstreamConsoleInput != null)    {
                if (FuncEvaluator.msstreamLogOutput != null)    {
                    if (datumPrompt.getThisOrNull() instanceof DataClassString)    {
                        // should not include the double quote if print's parameter is a string.
                        // but if part of the parameter is a string, e.g. [56, "abc"], double quote
                        // should be used.
                        FuncEvaluator.msstreamLogOutput.outputString(DCHelper.lightCvtOrRetDCString(datumPrompt).getStringValue());
                    } else    {
                        FuncEvaluator.msstreamLogOutput.outputString(datumPrompt.output());
                    }
                }

                FuncEvaluator.msstreamConsoleInput.doBeforeInput();
                String strInput = FuncEvaluator.msstreamConsoleInput.inputString();   // discard input.
                FuncEvaluator.msstreamConsoleInput.doAfterInput();
            }
            return datumReturnNum;
        }
    }    
    //public final static PauseFunction BUILTINFUNC_Pause = new PauseFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new PauseFunction());}
        
    public static class ScanfFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public ScanfFunction() {
            mstrProcessedNameWithFullCS = "::mfp::io::console::scanf";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* input */, InterruptedException
        {
            DataClass datumReturnNum = new DataClassArray(new DataClass[0]);
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumFormat = DCHelper.lightCvtOrRetDCString(tsParameter.poll());    // need not to deep copy coz only output value.
            if (FuncEvaluator.msstreamConsoleInput != null) {
                FuncEvaluator.msstreamConsoleInput.doBeforeInput();
                String strInput = FuncEvaluator.msstreamConsoleInput.inputString();
                FuncEvaluator.msstreamConsoleInput.doAfterInput();
                DataClass datumInput = new DataClassString(strInput);
                datumReturnNum = IOLib.sfScanf(datumInput, datumFormat.getStringValue());
            }
            if (datumReturnNum == null)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
            }            
            return datumReturnNum;
        }
    }    
    //public final static ScanfFunction BUILTINFUNC_Scanf = new ScanfFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new ScanfFunction());}

    /* print */
    public static class PrintFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public PrintFunction() {
            mstrProcessedNameWithFullCS = "::mfp::io::console::print";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass dsParam = tsParameter.poll(); // no need to cloneSelf because no content will change.
            if (FuncEvaluator.msstreamLogOutput != null)    {
                if (dsParam.getThisOrNull() instanceof DataClassString)    {
                    // should not include the double quote if print's parameter is a string.
                    // but if part of the parameter is a string, e.g. [56, "abc"], double quote
                    // should be used.
                    FuncEvaluator.msstreamLogOutput.outputString(DCHelper.lightCvtOrRetDCString(dsParam).getStringValue());
                } else    {
                    FuncEvaluator.msstreamLogOutput.outputString(dsParam.output());
                }
            }
            datumReturnNum = null;    // do not return anything.
            return datumReturnNum;
        }
    }    
    //public final static PrintFunction BUILTINFUNC_Print = new PrintFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new PrintFunction());}

    /* print */
    public static class Print_lineFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Print_lineFunction() {
            mstrProcessedNameWithFullCS = "::mfp::io::console::print_line";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 0;  // if parameter is 0, print "\n"
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass dsParam = new DataClassString("");
            if (tsParameter.size() > 0) {
                // no need to clone self coz if it is a string, its value
                // will not change. Otherwise, it simply output.
                dsParam = tsParameter.poll();
            }
            if (FuncEvaluator.msstreamLogOutput != null)    {
                if (dsParam.getThisOrNull() instanceof DataClassString)    {
                    // should not include the double quote if print's parameter is a string.
                    // but if part of the parameter is a string, e.g. [56, "abc"], double quote
                    // should be used.
                    FuncEvaluator.msstreamLogOutput.outputString(DCHelper.lightCvtOrRetDCString(dsParam).getStringValue() + "\n");
                } else    {
                    FuncEvaluator.msstreamLogOutput.outputString(dsParam.output() + "\n");
                }
            }
            datumReturnNum = null;    // do not return anything.
            return datumReturnNum;
        }
    }    
    //public final static Print_lineFunction BUILTINFUNC_Print_line = new Print_lineFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Print_lineFunction());}
        
    /* printf */
    public static class PrintfFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public PrintfFunction() {
            mstrProcessedNameWithFullCS = "::mfp::io::console::printf";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = -1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            if (tsParameter.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumFormatString = DCHelper.lightCvtOrRetDCString(tsParameter.removeLast());
            String strFormat = datumFormatString.getStringValue();
            String strOutput = IOLib.sPrintf(strFormat, tsParameter);
            FuncEvaluator.msstreamLogOutput.outputString(strOutput);
            DataClass datumReturnNum = null;
            return datumReturnNum;
        }
    }    
    //public final static PrintfFunction BUILTINFUNC_Printf = new PrintfFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new PrintfFunction());}
        
    public static class CloneFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public CloneFunction() {
            mstrProcessedNameWithFullCS = "::mfp::object::clone";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* alloc array */
        {
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumReturnNum = tsParameter.getFirst().cloneSelf();
            return datumReturnNum;
        }
    }    
    //public final static CloneFunction BUILTINFUNC_Clone = new CloneFunction();    
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new CloneFunction());}
    
    public static class Alloc_arrayFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Alloc_arrayFunction() {
            mstrProcessedNameWithFullCS = "::mfp::array::alloc_array";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = -1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* alloc array */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            } else  {
                int[] nListArraySize = new int[0];
                DataClass datumDefault = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO); // default data is ZERO
                if (tsParameter.size() <= 2 && tsParameter.getLast() instanceof DataClassArray)    {
                    // one or two paremeters first of which is a vector
                    if (tsParameter.size() == 2)    {
                        datumDefault = tsParameter.poll().cloneSelf();
                    }
                    DataClass datumParam = tsParameter.poll().cloneSelf();
                    if (!(datumParam instanceof DataClassArray) || datumParam.isNull()) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_TYPE);
                    }
                    DataClassArray datumParamIsArray = DCHelper.lightCvtOrRetDCArray(datumParam);
                    if (datumParamIsArray.getDataListSize() == 0) {
                        return datumDefault;    // datumDefault is a deep copy of parameter so we can return directly.
                    } else if (datumParamIsArray.getDataListSize() > 16) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                    } else {
                        nListArraySize = new int[datumParamIsArray.getDataListSize()];
                        for (int idx = 0; idx < datumParamIsArray.getDataListSize(); idx ++)   {
                            DataClassSingleNum datumSize = DCHelper.lightCvtOrRetDCMFPInt(datumParamIsArray.getDataList()[idx]);
                            if (datumSize.getDataValue().isActuallyNegative() || datumSize.getDataValue().compareTo(new MFPNumeric(DataLimit.ARRAY_INDEX_MAX)) > 0)  {
                                // size should not be greater than DataLimit.ARRAY_INDEX_MAX
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                            }
                            nListArraySize[idx] = (int)datumSize.getDataValue().longValue();
                        }
                    }
                } else  {
                    nListArraySize = new int[tsParameter.size()];
                    for (int index = 0; index < nListArraySize.length; index ++)    {
                        DataClassSingleNum datumParam = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.removeLast());
                        if (datumParam.getDataValue().isActuallyNegative() || datumParam.getDataValue().compareTo(new MFPNumeric(DataLimit.ARRAY_INDEX_MAX)) > 0)    {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                        }
                        nListArraySize[index] = (int)datumParam.getDataValue().longValue();
                    }
                }
                datumReturnNum = DataClassArray.constructDataArray(nListArraySize, datumDefault);
            }
            return datumReturnNum;
        }
    }    
    //public final static Alloc_arrayFunction BUILTINFUNC_Alloc_array = new Alloc_arrayFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Alloc_arrayFunction());}
        
    public static class EyeFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public EyeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::matrix::eye";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* alloc an I array */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumDim = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TWO);
            if (tsParameter.size() == mnMaxParamNum)    {
                datumDim = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());
                if (datumDim.getDataValue().compareTo(MFPNumeric.ONE) < 0 || datumDim.getDataValue().compareTo(new MFPNumeric(16)) > 0)  {
                    // dim should not be greater than 16
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                }
            }
            DataClassSingleNum datumSize = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());
            if (datumSize.getDataValue().isActuallyNegative() || datumSize.getDataValue().compareTo(new MFPNumeric(DataLimit.ARRAY_INDEX_MAX)) > 0)  {
                // size should not be greater than DataLimit.ARRAY_INDEX_MAX
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            
            datumReturnNum = BuiltinProcedures.createEyeMatrix((int)datumSize.getDataValue().longValue(), (int)datumDim.getDataValue().longValue());
            return datumReturnNum;
        }
    }    
    //public final static EyeFunction BUILTINFUNC_Eye = new EyeFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new EyeFunction());}
        
    public static class Ones_zerosFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Ones_zerosFunction() {
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* alloc an zeros or ones array */
        {
            DataClass datumReturnNum = new DataClassNull();
            int [] nlistSizes;
            if (tsParameter.size() < mnMinParamNum)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            } else if (tsParameter.size() == mnMinParamNum && tsParameter.get(0) instanceof DataClassArray)    {
                // a single paremeter which is a vector
                DataClassArray datumParam = DCHelper.lightCvtOrRetDCArray(tsParameter.poll().cloneSelf());
                if (datumParam.getDataListSize() > 16)  {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                } else {
                    nlistSizes = new int[datumParam.getDataListSize()];
                    for (int idx = 0; idx < datumParam.getDataListSize(); idx ++)   {
                        DataClassSingleNum datumSize = DCHelper.lightCvtOrRetDCMFPInt(datumParam.getDataList()[idx]);
                        // here we consider a special case where size(a_simple_value) = [0]
                        // to make zeros(size(a_value)) always work, zeros([0]) should be allowed.
                        if ((/*datumParam.getDataListSize() == 1 && */datumSize.getDataValue().isActuallyNegative())
                                //|| (datumParam.getDataListSize() > 1 && datumSize.getDataValue().compareTo(MFPNumeric.ONE) < 0)
                                || datumSize.getDataValue().compareTo(new MFPNumeric(DataLimit.ARRAY_INDEX_MAX)) > 0)  {
                            // size should not be greater than DataLimit.ARRAY_INDEX_MAX
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                        }
                        nlistSizes[idx] = (int)datumSize.getDataValue().longValue();
                    }
                }
            } else  {
                // multiple parameters and each of them is a positive integer.
                nlistSizes = new int[tsParameter.size()];
                for (int idx = 0; idx < nlistSizes.length; idx ++)   {
                    DataClassSingleNum datumSize = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());
                    // here we consider a special case where size(a_simple_value) = [0]
                    // to make zeros(size(a_value)) always work, zeros([0]) should be allowed.
                    if ((/*nlistSizes.length == 1 && */datumSize.getDataValue().isActuallyNegative())
                            //|| (nlistSizes.length > 1 && datumSize.getDataValue().compareTo(MFPNumeric.ONE) < 0)
                            || datumSize.getDataValue().compareTo(new MFPNumeric(DataLimit.ARRAY_INDEX_MAX)) > 0)  {
                        // size should not be greater than DataLimit.ARRAY_INDEX_MAX
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                    }
                    nlistSizes[nlistSizes.length - 1 - idx] = (int)datumSize.getDataValue().longValue();
                }
            }
            
            DataClass datumUniValue = new DataClassNull();
            if (mstrarrayFullCS[mstrarrayFullCS.length - 1].compareTo("zeros") == 0)   {
                datumUniValue = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
            } else  {   // if (strNameLowCase.compareTo("ones") == 0)
                datumUniValue = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
            }
            datumReturnNum = BuiltinProcedures.createUniValueMatrix(nlistSizes, datumUniValue);
            return datumReturnNum;
        }
    }    

    public static class OnesFunction extends Ones_zerosFunction {
		private static final long serialVersionUID = 1L;

        public OnesFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::matrix::ones";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = -1;
            mnMinParamNum = 1;
        }
    }
    //public final static OnesFunction BUILTINFUNC_Ones = new OnesFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new OnesFunction());}

    public static class ZerosFunction extends Ones_zerosFunction {
		private static final long serialVersionUID = 1L;

        public ZerosFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::matrix::zeros";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = -1;
            mnMinParamNum = 1;
        }
    }
    //public final static ZerosFunction BUILTINFUNC_Zeros = new ZerosFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new ZerosFunction());}
        
    public static class Includes_nan_inf_nullFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Includes_nan_inf_nullFunction() {
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datum = tsParameter.poll();   // need not to deep copy in this function.
            int nSearchMode = 0;
            String strPureFuncName = mstrarrayFullCS[mstrarrayFullCS.length - 1];
            if (strPureFuncName.compareTo("includes_nan_or_inf_or_null") == 0) {
                nSearchMode = 15;
            } else if (strPureFuncName.compareTo("includes_nan_or_inf") == 0) {
                nSearchMode = 14;
            } else if (strPureFuncName.compareTo("includes_nan") == 0)   {
                nSearchMode = 2;
            } else if (strPureFuncName.compareTo("includes_inf") == 0)    {
                nSearchMode = 12;
            } else if (strPureFuncName.compareTo("includes_null") == 0)  {
                nSearchMode = 1;
            }
            boolean bReturn = BuiltinProcedures.includesAbnormalValues(datum, nSearchMode);
            datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.valueOf(bReturn));
            return datumReturnNum;
        }
    }    
    public static class Includes_nan_or_inf_or_nullFunction extends Includes_nan_inf_nullFunction {
		private static final long serialVersionUID = 1L;

        public Includes_nan_or_inf_or_nullFunction() {
            mstrProcessedNameWithFullCS = "::mfp::array::includes_nan_or_inf_or_null";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Includes_nan_or_inf_or_nullFunction BUILTINFUNC_Includes_nan_or_inf_or_null = new Includes_nan_or_inf_or_nullFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Includes_nan_or_inf_or_nullFunction());}
        
    public static class Includes_nan_or_infFunction extends Includes_nan_inf_nullFunction {
		private static final long serialVersionUID = 1L;

        public Includes_nan_or_infFunction() {
            mstrProcessedNameWithFullCS = "::mfp::array::includes_nan_or_inf";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Includes_nan_or_infFunction BUILTINFUNC_Includes_nan_or_inf = new Includes_nan_or_infFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Includes_nan_or_infFunction());}
        
    public static class Includes_nanFunction extends Includes_nan_inf_nullFunction {
		private static final long serialVersionUID = 1L;

        public Includes_nanFunction() {
            mstrProcessedNameWithFullCS = "::mfp::array::includes_nan";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Includes_nanFunction BUILTINFUNC_Includes_nan = new Includes_nanFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Includes_nanFunction());}
        
    public static class Includes_infFunction extends Includes_nan_inf_nullFunction {
		private static final long serialVersionUID = 1L;

        public Includes_infFunction() {
            mstrProcessedNameWithFullCS = "::mfp::array::includes_inf";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Includes_infFunction BUILTINFUNC_Includes_inf = new Includes_infFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Includes_infFunction());}
        
    public static class Includes_nullFunction extends Includes_nan_inf_nullFunction {
		private static final long serialVersionUID = 1L;

        public Includes_nullFunction() {
            mstrProcessedNameWithFullCS = "::mfp::array::includes_null";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Includes_nullFunction BUILTINFUNC_Includes_null = new Includes_nullFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Includes_nullFunction());}
        
    public static class Is_nan_inf_nullFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Is_nan_inf_nullFunction() {
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datum = tsParameter.poll();   // need not to deep copy in this function.
            String strPureFuncName = mstrarrayFullCS[mstrarrayFullCS.length - 1];
            if (strPureFuncName.compareTo("is_nan_or_inf_or_null") == 0
                    && datum.isNull()) {
                datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
            } else if (datum instanceof DataClassComplex || datum instanceof DataClassSingleNum) {
                DataClassComplex datumCplx = DCHelper.lightCvtOrRetDCComplex(datum);
                MFPNumeric mfpNumReal = datumCplx.getReal();
                MFPNumeric mfpNumImage = datumCplx.getImage();
                if (!mfpNumImage.isActuallyZero()) {
                    // has image part, should not be nan or inf or -inf
                    datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
                } else if (mfpNumReal.isInf())  {
                    datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
                } else if (mfpNumReal.isNan() && (strPureFuncName.compareTo("is_nan_or_inf") == 0 || strPureFuncName.compareTo("is_nan_or_inf_or_null") == 0)) {
                    datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
                } else {
                    datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
                }
            } else {
                datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            }
            return datumReturnNum;
        }
    }    
        
    public static class Is_nan_or_infFunction extends Is_nan_inf_nullFunction {
		private static final long serialVersionUID = 1L;

        public Is_nan_or_infFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::is_nan_or_inf";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Is_nan_or_infFunction BUILTINFUNC_Is_nan_or_inf = new Is_nan_or_infFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Is_nan_or_infFunction());}
        
    public static class Is_nan_or_inf_or_nullFunction extends Is_nan_inf_nullFunction {
		private static final long serialVersionUID = 1L;

        public Is_nan_or_inf_or_nullFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::is_nan_or_inf_or_null";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Is_nan_or_inf_or_nullFunction BUILTINFUNC_Is_nan_or_inf_or_null = new Is_nan_or_inf_or_nullFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Is_nan_or_inf_or_nullFunction());}
        
    public static class Is_infFunction extends Is_nan_inf_nullFunction {
		private static final long serialVersionUID = 1L;

        public Is_infFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::number::is_inf";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Is_infFunction BUILTINFUNC_Is_inf = new Is_infFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Is_infFunction());}
        
    public static class Is_aexpr_datumFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Is_aexpr_datumFunction() {
            mstrProcessedNameWithFullCS = "::mfp::statement::is_aexpr_datum";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datum = tsParameter.poll();   // need not to deep copy in this function.
            if (datum.getThisOrNull() instanceof DataClassAExpr) {
                datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
            } else {
                datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            }
            return datumReturnNum;
        }
    }    
    //public final static Is_aexpr_datumFunction BUILTINFUNC_Is_aexpr_datum = new Is_aexpr_datumFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Is_aexpr_datumFunction());}
        
    public static class Get_boolean_aexpr_trueFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_boolean_aexpr_trueFunction() {
            mstrProcessedNameWithFullCS = "::mfp::statement::get_boolean_aexpr_true";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datum = tsParameter.poll();   // need not to deep copy in this function.
            
            if (datum.getThisOrNull() instanceof DataClassAExpr) {
                datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
            } else {
                datumReturnNum = DCHelper.lightCvtOrRetDCMFPBool(datum);
            }
            return datumReturnNum;
        }
    }    
    //public final static Get_boolean_aexpr_trueFunction BUILTINFUNC_Get_boolean_aexpr_true = new Get_boolean_aexpr_trueFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_boolean_aexpr_trueFunction());}
        
    public static class Get_boolean_aexpr_falseFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_boolean_aexpr_falseFunction() {
            mstrProcessedNameWithFullCS = "::mfp::statement::get_boolean_aexpr_false";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datum = tsParameter.poll();   // need not to deep copy in this function.
            
            if (datum.getThisOrNull() instanceof DataClassAExpr) {
                datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            } else {
                datumReturnNum = DCHelper.lightCvtOrRetDCMFPBool(datum);
            }
            return datumReturnNum;
        }
    }    
    //public final static Get_boolean_aexpr_falseFunction BUILTINFUNC_Get_boolean_aexpr_false = new Get_boolean_aexpr_falseFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_boolean_aexpr_falseFunction());}
        
    public static class Is_eye_zerosFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Is_eye_zerosFunction() {
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* is eye (zeros) or not */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumExplicitNullIsZero = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.ZERO);
            if (tsParameter.size() == mnMaxParamNum)    {
                datumExplicitNullIsZero = DCHelper.lightCvtOrRetDCMFPBool(tsParameter.poll());
            }
            boolean bExplicitNullIsZero = true;
            if (datumExplicitNullIsZero.getDataValue().isActuallyZero())    {
                // false;
                bExplicitNullIsZero = false;
            }
            DataClass datum = tsParameter.poll().cloneSelf();
            boolean bReturn = false;
            if (mstrarrayFullCS[mstrarrayFullCS.length - 1].compareTo("is_eye") == 0)    {
                bReturn = DCHelper.isEye(datum, bExplicitNullIsZero);
            } else    {    // is zeros?
                bReturn = DCHelper.isZeros(datum, bExplicitNullIsZero);
            }
            if (bReturn)    {
                datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.ONE);
            } else    {
                datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.ZERO);
            }
            return datumReturnNum;
        }
    }    
    public static class Is_eyeFunction extends Is_eye_zerosFunction {
		private static final long serialVersionUID = 1L;

        public Is_eyeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::matrix::is_eye";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 1;
        }
    }
    //public final static Is_eyeFunction BUILTINFUNC_Is_eye = new Is_eyeFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Is_eyeFunction());}
    
    public static class Is_zerosFunction extends Is_eye_zerosFunction {
		private static final long serialVersionUID = 1L;

        public Is_zerosFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::matrix::is_zeros";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 1;
        }
    }
    //public final static Is_zerosFunction BUILTINFUNC_Is_zeros = new Is_zerosFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Is_zerosFunction());}
        
    public static class SizeFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public SizeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::array::size";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* size */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum dsParamSizeDim = null;
            if (tsParameter.size() == mnMaxParamNum)    {
                dsParamSizeDim = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());
                if (dsParamSizeDim.getDataValue().isActuallyNonPositive())    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                }
            }
            DataClass dsParamArray = tsParameter.poll();// recalcDataArraySize will not change dsParamArray so no need to cloneSelf;
            int[] nArraySize = dsParamArray.recalcDataArraySize();
            int nSizeArrayLen = nArraySize.length;
            if (dsParamSizeDim != null)    {
                nSizeArrayLen = Math.min(nSizeArrayLen, (int)dsParamSizeDim.getDataValue().longValue());
            }
            DataClass[] dataList = new DataClass[nSizeArrayLen];
            for (int index = 0; index < nSizeArrayLen; index ++)    {
                dataList[index] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nArraySize[index]));
            }
            datumReturnNum = new DataClassArray(dataList);
            return datumReturnNum;
        }
    }    
    //public final static SizeFunction BUILTINFUNC_Size = new SizeFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new SizeFunction());}
        
    public static class Set_array_elemFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Set_array_elemFunction() {
            mstrProcessedNameWithFullCS = "::mfp::array::set_array_elem";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 4;
            mnMinParamNum = 3;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* set array element */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum && tsParameter.size() > mnMaxParamNum)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumDefault = new DataClassNull();   // default is NULL
            // datumDefault.SetDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_MFPINT);
            if (tsParameter.size() == mnMaxParamNum)    {
                // the 4th parameter is the default value for the new elements (i.e. elems will be added 
                // into the array besides the set_array_elem function explicitly set.
                datumDefault = tsParameter.poll().cloneSelf();
            } else if (!tsParameter.getFirst().isNull())  {   // tsParameter.size() == mnMinParamNum
                if (tsParameter.getFirst() instanceof DataClassSingleNum)    {
                    datumDefault = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO);
                } else if (tsParameter.getFirst() instanceof DataClassComplex) {
                    datumDefault = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO);
                } else if (tsParameter.getFirst() instanceof DataClassString) {
                    datumDefault = new DataClassString("");
                } else {
                    // datumDefault has been initialized as NULL.
                }
            }
            DataClass datumValue = tsParameter.poll().cloneSelf();
            DataClassArray datumIndex = DCHelper.lightCvtOrRetDCArray(tsParameter.poll().cloneSelf());
            if (datumIndex.getDataListSize() == 0)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
            }
            int[] nListArrayIndex = new int[datumIndex.getDataListSize()];
            for (int index = 0; index < datumIndex.getDataListSize(); index ++)    {
                int[] nListArrayIndex1 = new int[1];
                nListArrayIndex1[0] = index;
                DataClassSingleNum datumTmp = DCHelper.lightCvtOrRetDCMFPInt(datumIndex.getDataAtIndexByRef(nListArrayIndex1));
                if (datumTmp.getDataValue().longValue() < 0)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                }
                nListArrayIndex[index] = (int)datumTmp.getDataValue().longValue();
            }
            // do not deep copy because too expensive to set array elem function.
            DataClassArray datumArray = DCHelper.lightCvtOrRetDCArrayNoExcept(tsParameter.poll());
            // datumValue has been cloned above. We do not need to deep copy again.
            // also, as datumArray should not include any cells that is in datumValue,
            // no validation is required.
            datumReturnNum = datumArray.assignDataAtIndexByRef(nListArrayIndex, datumValue, datumDefault);
            return datumReturnNum;
        }
    }    
    //public final static Set_array_elemFunction BUILTINFUNC_Set_array_elem = new Set_array_elemFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_array_elemFunction());}
        
    public static class RecipFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public RecipFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::matrix::recip";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    // reciprocal of 2D matrix or a number
        {
            DataClass datumReturnNum = new DataClassNull();
            // now only support 2D matrix or a number.
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumOperand = tsParameter.poll();
            if (datumOperand.isNull()) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else if (datumOperand instanceof DataClassArray) {
                int[] narraySize = datumOperand.recalcDataArraySize();
                // no deep copy of datumOperand is required coz fullfillDataArray will deep copy it anyway.
                DataClass datum = DCHelper.lightCvtOrRetDCArray(datumOperand).fullfillDataArray(narraySize, false);
                datumReturnNum = BuiltinProcedures.evaluateReciprocal(datum);
            } else {    // not an array, so light copy should be fine.
                DataClass datum = datumOperand.copySelf();
                datumReturnNum = BuiltinProcedures.evaluateReciprocal(datum);
            }
            return datumReturnNum;
        }
    }    
    //public final static RecipFunction BUILTINFUNC_Recip = new RecipFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new RecipFunction());}
        
    public static class Left_recipFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Left_recipFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::matrix::left_recip";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    // left reciprocal of 2D matrix or a number
        {
            DataClass datumReturnNum = new DataClassNull();
            // now only support 2D matrix or a number.
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumOperand = tsParameter.poll();
            if (datumOperand.isNull()) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else if (datumOperand instanceof DataClassArray) {
                int[] narraySize = datumOperand.recalcDataArraySize();
                // deep copy will be done in fullfillDataArray
                DataClass datum = DCHelper.lightCvtOrRetDCArray(datumOperand).fullfillDataArray(narraySize, false);
                datumReturnNum = BuiltinProcedures.evaluateLeftReciprocal(datum);
            } else {    // not an array, so light copy should be fine.
                DataClass datum = datumOperand.copySelf();
                datumReturnNum = BuiltinProcedures.evaluateLeftReciprocal(datum);
            }
            return datumReturnNum;
        }
    }    
    //public final static Left_recipFunction BUILTINFUNC_Left_recip = new Left_recipFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Left_recipFunction());}
        
    public static class EigenFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public EigenFunction() {
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException    /* calculate eigen values/vectors */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum && tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datum2DSqrA, datum2DSqrB;
            DataClass datumParam2 = null;
            if (tsParameter.size() == mnMaxParamNum)    {
                datumParam2 = tsParameter.poll();
                if (DCHelper.isDataClassType(datumParam2, DATATYPES.DATUM_BASE_OBJECT)
                        || DCHelper.isZeros(datumParam2, true))  {
                    // B cannot be zero or zero matrix.
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
            }
            DataClass datumParam1 = tsParameter.poll();
            int[] narraySize = datumParam1.recalcDataArraySize();
            if (DCHelper.isDataClassType(datumParam1, DATATYPES.DATUM_MFPBOOL)) {
                datum2DSqrA = DCHelper.lightCvtOrRetDCMFPInt(datumParam1);
            } else if (DCHelper.isDataClassType(datumParam1, DATATYPES.DATUM_MFPINT, DATATYPES.DATUM_MFPDEC, DATATYPES.DATUM_COMPLEX)) {
                datum2DSqrA = datumParam1.cloneSelf();
            } else if (datumParam1.getThisOrNull() instanceof DataClassArray) {
                datum2DSqrA = datumParam1;
                if (narraySize.length == 1 && narraySize[0] == 1)    {
                    DataClass[] dataList = new DataClass[1];
                    dataList[0] = datumParam1;
                    datum2DSqrA = new DataClassArray(dataList);
                    narraySize = datum2DSqrA.recalcDataArraySize();
                }
                // datum2DSqrA must be data array after the above conversion. Inside fullfillDataArray,
                // datum2DSqrA is fully copied so no need to explicitly full copy it.
                datum2DSqrA = DCHelper.lightCvtOrRetDCArray(datum2DSqrA).fullfillDataArray(narraySize, false);
                if (narraySize.length != 2 || narraySize[0] == 0 || narraySize[0] != narraySize[1] || narraySize[0] > 12) {
                    // array matrix size shouldn't be greater than 12.
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
                }
            } else {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            }
            if (datumParam2 == null)    {   // don't have the second parameter, so it is I by default.
                if (!(datum2DSqrA instanceof DataClassArray))  {
                    datum2DSqrB = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                } else {
                    DataClass[] datumList = new DataClass[narraySize[0]];
                    for (int idx0 = 0; idx0 < datumList.length; idx0 ++)   {
                        datumList[idx0] = new DataClassNull();
                        DataClass[] datumListChildren = new DataClass[narraySize[1]];
                        for (int idx1 = 0; idx1 < datumListChildren.length; idx1 ++)    {
                            datumListChildren[idx1] = new DataClassNull();
                            if (idx0 == idx1)   {
                                datumListChildren[idx1] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                            } else {
                                datumListChildren[idx1] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                            }
                        }
                        datumList[idx0] = new DataClassArray(datumListChildren);
                    }
                    datum2DSqrB = new DataClassArray(datumList);
                }
            } else  {
                int[] narraySizeB = datumParam2.recalcDataArraySize();
                if (DCHelper.isDataClassType(datumParam2, DATATYPES.DATUM_MFPBOOL)) {
                    datum2DSqrB = DCHelper.lightCvtOrRetDCMFPInt(datumParam2);
                } else if (DCHelper.isDataClassType(datumParam2, DATATYPES.DATUM_MFPINT,
                    DATATYPES.DATUM_MFPDEC, DATATYPES.DATUM_COMPLEX)) {
                    datum2DSqrB = datumParam2.cloneSelf();
                } else if (datumParam2.getThisOrNull() instanceof DataClassArray) {
                    datum2DSqrB = datumParam2;
                    if (narraySizeB.length == 1 && narraySizeB[0] == 1)    {
                        DataClass[] dataList = new DataClass[1];
                        dataList[0] = datumParam2;
                        datum2DSqrB = new DataClassArray(dataList);
                        narraySizeB = datum2DSqrB.recalcDataArraySize();
                    }
                    // datum2DSqrB must be an array. It will be deep copied inside fullfillDataArray.
                    datum2DSqrB = DCHelper.lightCvtOrRetDCArray(datum2DSqrB).fullfillDataArray(narraySizeB, false);
                } else {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
                }
                if (narraySizeB.length != narraySize.length) {  // A is a matrix but B isn't or vise versa
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_PARAMETER_NOT_MATCH);
                } else if (narraySize.length == 2 && (narraySize[0] != narraySizeB[0] || narraySize[1] != narraySizeB[1])) {
                    // A and B sizes are not match.
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_PARAMETER_NOT_MATCH);
                }
            }
            // now parameter A and B are ok.
            String strPureFuncName = mstrarrayFullCS[mstrarrayFullCS.length - 1];
            if (!(datum2DSqrA instanceof DataClassArray))  {
                // inputs are single values
                DataClass datumEigenValue = BuiltinProcedures.evaluateDivision(datum2DSqrA, datum2DSqrB);
                if (strPureFuncName.compareTo("get_eigen_values") == 0)   {   // return eigen values
                    datumReturnNum = datumEigenValue;
                } else {    // return eigen vector and eigen value
                    DataClass datumEigenVector = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                    DataClass[] datumList = new DataClass[2];
                    datumList[0] = datumEigenVector;
                    datumList[1] = datumEigenValue;
                    datumReturnNum = new DataClassArray(datumList);
                }
            } else {
                // inputs are 2d square matrices.
                LinkedList<DataClass> listEigenValues = MatrixLib.calculateEigenValues(datum2DSqrA, datum2DSqrB, FuncEvaluator.msfunctionInterrupter);
                // eigen vectors save all the eigen vectors for each eigen value.
                LinkedList<LinkedList<LinkedList<DataClass>>> listAllEigenVectors = new LinkedList<LinkedList<LinkedList<DataClass>>>();
                if (strPureFuncName.compareTo("get_eigen_values") == 0)   {   // return eigen values
                    datumReturnNum = new DataClassArray(listEigenValues.toArray(new DataClass[narraySize[0]]));
                } else if (narraySize[0] > 1)  {   // return eigen vector matrix and eigen values
                    DataClass[] datumDataVectors = new DataClass[narraySize[0]];
                    for (int idx = 0; idx < listEigenValues.size(); idx ++) {
                        int nExistingEigVal = 0;
                        for (int idx1 = 0; idx1 < idx; idx1 ++) {
                            if (listEigenValues.get(idx).isEqual(listEigenValues.get(idx1))) {
                                nExistingEigVal ++;
                                if (listAllEigenVectors.size() == idx) {
                                    // add its eigen vector set if haven't done this.
                                    listAllEigenVectors.add(listAllEigenVectors.get(idx1));
                                }
                            }
                        }
                        if (listAllEigenVectors.size() == idx) {
                            // its eigen vector set hasn't been added. calculate now.
                            DataClass datumAMinusEigB = BuiltinProcedures.evaluateMultiplication(listEigenValues.get(idx), datum2DSqrB);
                            datumAMinusEigB = BuiltinProcedures.evaluateSubstraction(datum2DSqrA, datumAMinusEigB);
                            LinkedList<LinkedList<DataClass>> listEigenVectors = MatrixLib.calculateZeroVectors(datumAMinusEigB);
                            listAllEigenVectors.add(listEigenVectors);
                        }
                        datumDataVectors[idx] = new DataClassArray(listAllEigenVectors.get(idx).get(nExistingEigVal).toArray(new DataClass[narraySize[1]]));
                    }
                    DataClass datumVectorMatrix = new DataClassArray(datumDataVectors);
                    datumVectorMatrix = BuiltinProcedures.evaluateTransposition(datumVectorMatrix);
                    // calculate eigen value matrix.
                    DataClass datumEigenValues = new DataClassNull();
                    DataClass[] datumListEigVals = new DataClass[narraySize[0]];
                    for (int idx0 = 0; idx0 < narraySize[0]; idx0 ++)  {
                        DataClass[] datumListEigValChildren = new DataClass[narraySize[1]];
                        for (int idx1 = 0; idx1 < narraySize[1]; idx1 ++)   {
                            if (idx1 == idx0)   {
                                datumListEigValChildren[idx1] = listEigenValues.get(idx1);
                            } else {
                                datumListEigValChildren[idx1] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                            }
                        }
                        datumListEigVals[idx0] = new DataClassArray(datumListEigValChildren);
                    }
                    datumEigenValues = new DataClassArray(datumListEigVals);

                    DataClass[] datumReturnList = new DataClass[2];
                    // return value includes two elements, first is eigen vector matrix, each column is an eigen vector,
                    // second is the eigen values list.
                    datumReturnList[0] = datumVectorMatrix;
                    datumReturnList[1] = datumEigenValues;
                    datumReturnNum = new DataClassArray(datumReturnList);
                } else { // it is a 1 * 1 matrix, have to be handled specially.
                    DataClass datumEigenVector = new DataClassNull();
                    DataClass datumEigenVectorChild = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                    DataClass[] datumList = new DataClass[1];
                    datumList[0] = datumEigenVectorChild;
                    DataClass datumTmp = new DataClassArray(datumList);
                    datumList = new DataClass[1];
                    datumList[0] = datumTmp;
                    datumEigenVector = new DataClassArray(datumList);
                    
                    DataClass datumEigenValues = new DataClassNull();
                    // actually there is only one eigen value.
                    DataClass[] datumEigValList = new DataClass[1];
                    datumEigValList[0] = new DataClassArray(listEigenValues.toArray(new DataClass[narraySize[0]]));
                    datumEigenValues = new DataClassArray(datumEigValList);
                    
                    DataClass[] datumReturnList = new DataClass[2];
                    datumReturnList[0] = datumEigenVector;
                    datumReturnList[1] = datumEigenValues;
                    datumReturnNum = new DataClassArray(datumReturnList);
                }
            }
            return datumReturnNum;
        }
    }
    
    public static class EigFunction extends EigenFunction {
		private static final long serialVersionUID = 1L;

        public EigFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::matrix::eig";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 1;
        }
    }
    //public final static EigFunction BUILTINFUNC_Eig = new EigFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new EigFunction());}
    
    public static class Get_eigen_valuesFunction extends EigenFunction {
		private static final long serialVersionUID = 1L;

        public Get_eigen_valuesFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::matrix::get_eigen_values";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 1;
        }
    }
    //public final static Get_eigen_valuesFunction BUILTINFUNC_Get_eigen_values = new Get_eigen_valuesFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_eigen_valuesFunction());}
        
    public static class DeterFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public DeterFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::matrix::deter";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* calculate determinant of a 2D square array */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumOperand = tsParameter.poll();
            if (DCHelper.isDataClassType(datumOperand, DATATYPES.DATUM_MFPBOOL)) {
                datumReturnNum = DCHelper.lightCvtOrRetDCMFPInt(datumOperand);
            } else if (DCHelper.isDataClassType(datumOperand, DATATYPES.DATUM_MFPINT,
                    DATATYPES.DATUM_MFPDEC, DATATYPES.DATUM_COMPLEX)) {
                datumReturnNum = datumOperand.cloneSelf();
            } else if (datumOperand.getThisOrNull() instanceof DataClassArray) {
                int[] narraySize = datumOperand.recalcDataArraySize();
                DataClass datum2DMatrix;
                if (narraySize.length == 1 && narraySize[0] == 1)    {
                    DataClass[] dataList = new DataClass[1];
                    dataList[0] = datumOperand;
                    datum2DMatrix = new DataClassArray(dataList);
                    narraySize = datum2DMatrix.recalcDataArraySize();
                } else {
                    datum2DMatrix = datumOperand;
                }   // deep copy will be done in fullfillDataArray so no deep copy is needed above it.
                datum2DMatrix = DCHelper.lightCvtOrRetDCArray(datum2DMatrix).fullfillDataArray(narraySize, false);
                datumReturnNum = BuiltinProcedures.evaluateDeterminant(datum2DMatrix);
            } else {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            }
            return datumReturnNum;
        }
    }    
    //public final static DeterFunction BUILTINFUNC_Deter = new DeterFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new DeterFunction());}

    public static class DetFunction extends DeterFunction {
		private static final long serialVersionUID = 1L;

        public DetFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::matrix::det";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static DetFunction BUILTINFUNC_Det = new DetFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new DetFunction());}
    
    public static class RankFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public RankFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::matrix::rank";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* calculate rank of a 2D square array */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            datumReturnNum = MatrixLib.calculateMatrixRank(tsParameter.poll());
            return datumReturnNum;
        }
    }    
    //public final static RankFunction BUILTINFUNC_Rank = new RankFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new RankFunction());}
        
    public static class Upper_triangular_matrixFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Upper_triangular_matrixFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::matrix::upper_triangular_matrix";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* calculate upper triangular matrix of a 2D square array */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            datumReturnNum = MatrixLib.calculateUpperTriangularMatrix(tsParameter.poll());
            return datumReturnNum;
        }
    }    
    //public final static Upper_triangular_matrixFunction BUILTINFUNC_Upper_triangular_matrix = new Upper_triangular_matrixFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Upper_triangular_matrixFunction());}
        
    public static class InvertFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public InvertFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::matrix::invert";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* invert a 2D array */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumOperand = tsParameter.poll();
            if (datumOperand.getThisOrNull() instanceof DataClassSingleNum
                    || datumOperand.getThisOrNull() instanceof DataClassComplex)    {
                datumReturnNum = BuiltinProcedures.evaluateDivision(
                                    new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE),
                                    datumOperand);
            } else if (datumOperand.getThisOrNull() instanceof DataClassArray)    {
                int[] narraySize = datumOperand.recalcDataArraySize();
                if (narraySize.length == 1 && narraySize[0] == 1)    {
                    DataClass datumElem = DCHelper.lightCvtOrRetDCArray(datumOperand).getDataList()[0].cloneSelf();
                    DataClass[] dataList = new DataClass[1];
                    dataList[0] = BuiltinProcedures.evaluateDivision(
                                    new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE),
                                    datumElem);
                    datumReturnNum = new DataClassArray(dataList);
                } else    {
                    // deep copy is done in fullfillDataArray
                    DataClass datum2DMatrix = DCHelper.lightCvtOrRetDCArray(datumOperand).fullfillDataArray(narraySize, false);
                    datumReturnNum = BuiltinProcedures.invert2DSquare(datum2DMatrix);
                }
            } else    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            }
            return datumReturnNum;
        }
    }    
    //public final static InvertFunction BUILTINFUNC_Invert = new InvertFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new InvertFunction());}
        
    /* calculate roots by Java code, this function interface is not released to user */
    public static class Roots_internalFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Roots_internalFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::polynomial::roots_internal";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = -1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            DataClass datumReturnNum = new DataClassNull();
            LinkedList<DataClass> listParams = new LinkedList<DataClass>();
            if (tsParameter.size() < mnMinParamNum)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            } else if (tsParameter.size() == mnMinParamNum) {
                if (!(tsParameter.get(0).getThisOrNull() instanceof DataClassArray))   {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
                }
                DataClassArray datumParam = DCHelper.lightCvtOrRetDCArray(tsParameter.get(0).cloneSelf());
                for (int idx = 0; idx < datumParam.getDataListSize(); idx ++)   {
                    listParams.add(datumParam.getDataList()[idx]);
                }
            } else  {
                for (int idx = 0; idx < tsParameter.size(); idx ++)
                {
                    DataClass datumParam = tsParameter.get(idx).cloneSelf();
                    listParams.addFirst(datumParam);
                }
            }
            LinkedList<DataClass> listResults = MathLib.solvePolynomial(listParams, FuncEvaluator.msfunctionInterrupter);
            DataClass[] arraydatumChildren = new DataClass[listResults.size()];
            for (int idx = 0; idx < listResults.size(); idx ++) {
                arraydatumChildren[idx] = listResults.get(idx);
            }
            datumReturnNum = new DataClassArray(arraydatumChildren);            
            return datumReturnNum;
        }
    }    
    //public final static Roots_internalFunction BUILTINFUNC_Roots_internal = new Roots_internalFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Roots_internalFunction());}

    /* this function get the continuous root from root list. This function is useful when plot 3-order polynomial implicit functions */
    public static class Get_continuous_rootFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_continuous_rootFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::polynomial::get_continuous_root";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 4;
            mnMinParamNum = 4;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            DataClass datumReturnNum = new DataClassNull();
            LinkedList<DataClass> listParams = new LinkedList<DataClass>();
            if (tsParameter.size() != mnMinParamNum)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumNext = tsParameter.poll().cloneSelf();
            DataClass datumPrev = tsParameter.poll().cloneSelf();
            DataClassSingleNum datumSuggestedIdx = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());
            int nSuggestedIdx = datumSuggestedIdx.getDataValue().intValue();
            DataClassArray datumRoots = DCHelper.lightCvtOrRetDCArray(tsParameter.poll());
            for (int idx = 0; idx < datumRoots.getDataListSize(); idx ++) {
                datumRoots.getDataList()[idx] = DCHelper.lightCvtOrRetDCComplex(datumRoots.getDataList()[idx]);
            }
            if (datumRoots.getDataListSize() == 0) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else if (datumRoots.getDataListSize() == 1) {
                datumReturnNum = datumRoots.getDataList()[0];
                return datumReturnNum;
            }
            
            boolean bPrevNextAvgValid = false;
            DataClass datumPrevNextAvg = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO);
            if (!datumNext.isNull()) {
                DataClassComplex datumNextCplx = DCHelper.lightCvtOrRetDCComplex(datumNext);
                if (!datumNextCplx.getReal().isNan() && !datumNextCplx.getImage().isNan()) {
                    datumPrevNextAvg = datumNext;
                    bPrevNextAvgValid = true;
                }
            }
            if (!datumPrev.isNull()) {
                DataClassComplex datumPrevCplx = DCHelper.lightCvtOrRetDCComplex(datumPrev);
                if (!datumPrevCplx.getReal().isNan() && !datumPrevCplx.getImage().isNan()) {
                    datumPrevNextAvg = BuiltinProcedures.evaluateAdding(datumPrev, datumPrevNextAvg);
                    bPrevNextAvgValid = true;
                }
            }
            
            if (bPrevNextAvgValid) {
                DataClassComplex datumPrevNextAvgCplx = DCHelper.lightCvtOrRetDCComplex(datumPrevNextAvg);
                MFPNumeric mfpPrevNextAvgReal = datumPrevNextAvgCplx.getReal(),
                        mfpPrevNextAvgImage = datumPrevNextAvgCplx.getImage();
                int nSelectedIdx = 0;
                DataClassComplex dataThisRoot = DCHelper.lightCvtOrRetDCComplex(datumRoots.getDataList()[0]);
                MFPNumeric mfpGapReal = dataThisRoot.getReal().subtract(mfpPrevNextAvgReal);
                MFPNumeric mfpGapImage = dataThisRoot.getImage().subtract(mfpPrevNextAvgImage);
                MFPNumeric mfpMinGap = mfpGapReal.abs().add(mfpGapImage.abs()); // no need to calculate (x1-x2)**2+(y1-y2)**2
                for (int idx = 0; idx < datumRoots.getDataListSize(); idx ++) {
                    dataThisRoot = DCHelper.lightCvtOrRetDCComplex(datumRoots.getDataList()[idx]);
                    mfpGapReal = dataThisRoot.getReal().subtract(mfpPrevNextAvgReal);
                    mfpGapImage = dataThisRoot.getImage().subtract(mfpPrevNextAvgImage);
                    MFPNumeric mfpGap = mfpGapReal.abs().add(mfpGapImage.abs());
                    if (mfpGap.compareTo(mfpMinGap) < 0) {
                        nSelectedIdx = idx;
                        mfpMinGap = mfpGap;
                    }
                }
                datumReturnNum = datumRoots.getDataList()[nSelectedIdx];
            } else if (nSuggestedIdx >= 0 && nSuggestedIdx < datumRoots.getDataListSize()) {
                datumReturnNum = datumRoots.getDataList()[nSuggestedIdx];
            } else {
                datumReturnNum = datumRoots.getDataList()[0];
            }
            return datumReturnNum;
        }
    }    
    //public final static Get_continuous_rootFunction BUILTINFUNC_Get_continuous_root = new Get_continuous_rootFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_continuous_rootFunction());}
        
    /* sum_over or SIGMA and product_over or PI */
    public static class Sum_product_overFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Sum_product_overFunction() {
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            String strPureFuncName = mstrarrayFullCS[mstrarrayFullCS.length - 1];
            DataClassString datumEndRaw = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
            DataClassString datumStartRaw = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
            DataClassString datumStrExpr = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
            
            String strStart = datumStartRaw.getStringValue();
            if (strStart == null)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            String[] strarrayStartParts = strStart.trim().split("=");   // use trim to prevent situation like " x= 8" (variable name becomes " x")
            if (strarrayStartParts.length < 2 || strarrayStartParts[0].length() == 0)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            String strVarName = strarrayStartParts[0].trim().toLowerCase(Locale.US);
            for (int idx = 0; idx < strVarName.length(); idx ++)    {
                int nNameCharType = ElemAnalyzer.isNameChar(strVarName, idx);
                if (idx == 0 && nNameCharType != 1)  {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
                if (idx > 0 && nNameCharType == 0)  {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
            }
            // now variable name is valid
            UnknownVariable var = new UnknownVariable(strVarName);
            LinkedList<UnknownVariable> lUnknown = new LinkedList<UnknownVariable>();
            lUnknown.addFirst(var);
            AbstractExpr aeStrExpr = null;
            try {
                aeStrExpr = ExprAnalyzer.analyseExpression(datumStrExpr.getStringValue(), new CurPos(), new LinkedList<Variable>(), progContext);
            } catch (Exception e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            try {
                // need to evaluate considering second order integral, x+y + 1, y is a constant, so simplifymost will calculate y + 1 directly.
                aeStrExpr = aeStrExpr.evaluateAExpr(lUnknown, progContext); // look on unknown dim as single value dim.
            }catch (Exception e)    {
                // for like x + y + 1, y may not in lUnknown and lVarNameSpaces. This exception may be thrown as cannot get result exception. so catch
                // any exceptions that throw.
            }
            LinkedList<Variable> l = new LinkedList<Variable>();
            l.addFirst(var);
            progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(l);
            ExprEvaluator exprEvaluator = new ExprEvaluator(progContext);
            DataClass datumEnd = null;
            try {
                datumEnd = exprEvaluator.evaluateExpression(datumEndRaw.getStringValue(), new CurPos());    // evaluate data end if it is a string.
            } catch (JFCALCExpErrException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            if (datumEnd == null)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
            }
            DataClassSingleNum datumEndInt = DCHelper.lightCvtOrRetDCMFPInt(datumEnd);
            DataClass datumStart = null;
            try {
                datumStart = exprEvaluator.evaluateExpression(datumStartRaw.getStringValue(), new CurPos());    // evaluate data end if it is a string.
            } catch (JFCALCExpErrException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            if (datumStart == null)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
            }
            DataClassSingleNum datumStartInt = DCHelper.lightCvtOrRetDCMFPInt(datumStart);
            MFPNumeric mfpNumStart = datumStartInt.getDataValue();
            MFPNumeric mfpNumEnd = datumEndInt.getDataValue();
            MFPNumeric mfpNumStep = (mfpNumStart.compareTo(mfpNumEnd) > 0)?MFPNumeric.MINUS_ONE:MFPNumeric.ONE;
            MFPNumeric mfpNumEndPlusStep = mfpNumEnd.add(mfpNumStep);
            MFPNumeric idx = mfpNumStart;
            MFPNumeric mfpNumValueReal = MFPNumeric.ZERO;
            MFPNumeric mfpNumValueImage = MFPNumeric.ZERO;
            MFPNumeric mfpNumReturnReal = (strPureFuncName.compareTo("sum_over") == 0)?MFPNumeric.ZERO:MFPNumeric.ONE;
            MFPNumeric mfpNumReturnImage = MFPNumeric.ZERO;
            while (!idx.isEqual(mfpNumEndPlusStep))   {
                DataClassSingleNum datumIndex = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, idx);
                var.setValue(datumIndex);
                DataClass datumExprValue = null;
                if (aeStrExpr != null) {
                    try {
                        datumExprValue = aeStrExpr.evaluateAExprQuick(lUnknown, progContext);
                    } catch (Exception ex) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                    }
                }
                //DataClass datumExprValue = exprEvaluator.evaluateExpression(datumStrExpr.getStringValue(), new CurPos());
                if (datumExprValue == null)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                }
                DataClassComplex datumExprValueCplx = DCHelper.lightCvtOrRetDCComplex(datumExprValue);
                mfpNumValueReal = datumExprValueCplx.getReal();
                mfpNumValueImage = datumExprValueCplx.getImage();
                if (strPureFuncName.compareTo("sum_over") == 0)  {
                    mfpNumReturnReal = mfpNumReturnReal.add(mfpNumValueReal);
                    if (!mfpNumReturnImage.isActuallyZero() || !mfpNumValueImage.isActuallyZero())  {
                        mfpNumReturnImage = mfpNumReturnImage.add(mfpNumValueImage);
                    }
                } else {
                    mfpNumReturnReal = mfpNumReturnReal.multiply(mfpNumValueReal)
                            .subtract(mfpNumReturnImage.multiply(mfpNumValueImage));
                    if (!mfpNumReturnImage.isActuallyZero() || !mfpNumValueImage.isActuallyZero())  {
                        mfpNumReturnImage = mfpNumReturnReal.multiply(mfpNumValueImage)
                                .add(mfpNumReturnImage.multiply(mfpNumValueReal));
                    }
                }
                idx = idx.add(mfpNumStep);
            }
            datumReturnNum = new DataClassComplex(mfpNumReturnReal, mfpNumReturnImage);
            progContext.mdynamicProgContext.mlVarNameSpaces.poll();
            return datumReturnNum;
        }
    }    
    public static class Sum_overFunction extends Sum_product_overFunction {
		private static final long serialVersionUID = 1L;

        public Sum_overFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::stat_stoch::sum_over";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3;
            mnMinParamNum = 3;
        }
    }
    //public final static Sum_overFunction BUILTINFUNC_Sum_over = new Sum_overFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Sum_overFunction());}
        
    public static class Product_overFunction extends Sum_product_overFunction {
		private static final long serialVersionUID = 1L;

        public Product_overFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::stat_stoch::product_over";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3;
            mnMinParamNum = 3;
        }
    }
    //public final static Product_overFunction BUILTINFUNC_Product_over = new Product_overFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Product_overFunction());}
        
    /* derivative method */
    public static class DerivativeFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public DerivativeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::calculus::derivative";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 4;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            if (tsParameter.size() < 2 && tsParameter.size() > 4)    {    // do not use mnMinParamNum or mnMaxParamNum
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            } else {
                //definite derivative
                boolean bUseNumeric = true;  // by default, using numeric may solve some derivative which cannot be mathematically derived.
                if (tsParameter.size() == 4)    {
                    DataClassSingleNum datumUseNumeric = DCHelper.lightCvtOrRetDCMFPBool(tsParameter.poll());
                    bUseNumeric = datumUseNumeric.getDataValue().booleanValue();
                }
                Double dVarVal = null;
                DataClass datumVarVal = new DataClassNull();
                if (tsParameter.size() == 3)    {
                    // do not support complex or matrix derivative.
                    datumVarVal = tsParameter.poll();   // no need to deep copy as datumVarVal is a double value anyway.
                    dVarVal = DCHelper.lightCvtOrRetDCMFPDec(datumVarVal).getDataValue().doubleValue();
                }
                DataClassString datumVarName = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
                String strVarName = datumVarName.getStringValue().trim().toLowerCase(Locale.US);
                DataClassString datumExpr = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
                String strExpr = datumExpr.getStringValue().trim();
                UnknownVariable var = new UnknownVariable(strVarName);
                LinkedList<UnknownVariable> l = new LinkedList<UnknownVariable>();
                l.addFirst(var);
                AbstractExpr aeExpr = new AEInvalid();
                try {
                    aeExpr = ExprAnalyzer.analyseExpression(strExpr, new CurPos(), new LinkedList<Variable>(), progContext);
                } catch (Exception e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
                try {
                    aeExpr = aeExpr.simplifyAExprMost(l, new SimplifyParams(true, true, true), progContext); // look on unknown dim as single value dim.
                    AbstractExpr aeReturn = FuncEvaluator.mspm.deriInDefByPtn1VarDeriIdentifier(aeExpr, l, progContext);
                    if (aeReturn == null) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                    }
                    DataClass datumReturn;
                    if (dVarVal == null) {
                        // OK, we just need the expression.
                        aeReturn = aeReturn.simplifyAExprMost(l, new SimplifyParams(false, true, true), progContext);
                        datumReturn = new DataClassString(aeReturn.outputWithFlag(1, progContext));    // do not call output coz we want to see most simple format
                    } else {
                        var.setValue(datumVarVal);
                        aeReturn = aeReturn.simplifyAExprMost(l, new SimplifyParams(false, true, true), progContext);
                        if (aeReturn instanceof AEConst) {
                            datumReturn = ((AEConst)aeReturn).getDataClassCopy();
                            if (!DCHelper.isNumericalData(datumReturn, true)) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                            }
                        } else {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                        }
                    }
                    return datumReturn;
                } catch (JSmartMathErrException ex) {
                    if (ex.m_se.m_enumErrorType == SMErrProcessor.ERRORTYPES.ERROR_CANNOT_SOLVE_CALCULATION) {
                        if (bUseNumeric) {
                            // use numeric methods to get value
                            DataClass datumReturn = MathLib.deriRidders(aeExpr, strVarName, datumVarVal,
                                                                        new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE_TENTH),
                                                                        1, progContext);
                            return datumReturn;
                        } else {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                        }
                    } else {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                    }
                }
            } 
        }
    }    
    //public final static DerivativeFunction BUILTINFUNC_Derivative = new DerivativeFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new DerivativeFunction());}

    /* derivative method */
    public static class Deri_RiddersFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Deri_RiddersFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::calculus::deri_ridders";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 4;
            mnMinParamNum = 4;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            if (tsParameter.size() != 4)    {    // do not use mnMinParamNum or mnMaxParamNum
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            } else {
                //definite derivative
                DataClassSingleNum datumDeriOrder = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());
                int nDeriOrder = datumDeriOrder.getDataValue().intValue();
                DataClass datumVarVal = DCHelper.lightCvtOrRetDCMFPDec(tsParameter.poll()); // do not support complex or matrix derivative.
                DataClassString datumVarName = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
                String strVarName = datumVarName.getStringValue().trim().toLowerCase(Locale.US);
                DataClassString datumExpr = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
                String strExpr = datumExpr.getStringValue().trim();
                UnknownVariable var = new UnknownVariable(strVarName);
                LinkedList<UnknownVariable> l = new LinkedList<UnknownVariable>();
                l.addFirst(var);
                AbstractExpr aeInteg = new AEInvalid();
                try {
                    aeInteg = ExprAnalyzer.analyseExpression(strExpr, new CurPos(), new LinkedList<Variable>(), progContext);
                } catch (Exception e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
                DataClass datumReturn = MathLib.deriRidders(aeInteg, strVarName, datumVarVal,
                                                            new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE_TENTH),
                                                            nDeriOrder, progContext);
                return datumReturn;
            } 
        }
    }    
    //public final static DerivativeFunction BUILTINFUNC_Derivative = new DerivativeFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Deri_RiddersFunction());}

    /* integrate adaptive method */
    public static class IntegrateFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public IntegrateFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::calculus::integrate";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 5;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            if (tsParameter.size() != 2 && tsParameter.size() != 4 && tsParameter.size() != 5)    {    // do not use mnMinParamNum or mnMaxParamNum
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            } else if (tsParameter.size() == 2)   {
                // indefinite integral
                DataClassString datumVarName = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
                String strVarName = datumVarName.getStringValue().trim().toLowerCase(Locale.US);
                DataClassString datumExpr = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
                String strExpr = datumExpr.getStringValue().trim();
                UnknownVariable var = new UnknownVariable(strVarName);
                LinkedList<UnknownVariable> l = new LinkedList<UnknownVariable>();
                l.addFirst(var);
                try {
                    AbstractExpr aeInteg = ExprAnalyzer.analyseExpression(strExpr, new CurPos(), new LinkedList<Variable>(), progContext);
                    aeInteg = aeInteg.simplifyAExprMost(l, new SimplifyParams(true, true, true), progContext); // look on unknown dim as single value dim.
                    AbstractExpr aeReturn = FuncEvaluator.mspm.integInDefByPtn1VarIntegIdentifier(aeInteg, l, progContext);
                    if (aeReturn == null) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                    }
                    //String strOutputB4Optimize = aeReturn.output();
                    //aeReturn = ExprAnalyzer.analyseExpression(strOutputB4Optimize, new CurPos(), progContext);
                    aeReturn = aeReturn.simplifyAExprMost(l, new SimplifyParams(false, true, true), progContext);
                    DataClass datumReturn = new DataClassString(aeReturn.outputWithFlag(1, progContext)); // use outputWithFlag instead of output to show simpliest string.
                    return datumReturn;
                } catch (JSmartMathErrException ex) {
                    if (ex.m_se.m_enumErrorType == SMErrProcessor.ERRORTYPES.ERROR_CANNOT_SOLVE_CALCULATION) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                    } else {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                    }
                }
            } else {
                //definite integral
                boolean bUseBasicInteg = true;  // by default, using basic which is much quick.
                DataClassSingleNum datumNumofSteps = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                int nParamIdx = 0;
                if (tsParameter.size() == 5)    {
                    datumNumofSteps = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.peek());
                    nParamIdx ++;
                }
                long nNumOfSteps = datumNumofSteps.getDataValue().longValue();
                if (nNumOfSteps <= 0)    {
                    bUseBasicInteg = false;    // use Gauss-Kronrod.
                }
                DataClassString datumStrDeltaVar = DCHelper.lightCvtOrRetDCString(tsParameter.get(nParamIdx + 2));
                Variable var = new Variable(datumStrDeltaVar.getStringValue().trim().toLowerCase(Locale.US));
                LinkedList<Variable> l = new LinkedList<Variable>();
                l.addFirst(var);
                progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(l);
                ExprEvaluator exprEvaluator = new ExprEvaluator(progContext);
                if (bUseBasicInteg)    {
                    DataClass datumEnd = tsParameter.get(nParamIdx).cloneSelf();
                    if (!(datumEnd.getThisOrNull() instanceof DataClassString))   {
                        datumEnd = DCHelper.lightCvtOrRetDCComplex(datumEnd);
                    } else {
                        try {
                            datumEnd = exprEvaluator.evaluateExpression(DCHelper.lightCvtOrRetDCString(datumEnd).getStringValue(), new CurPos());    // evaluate data end if it is a string.
                        } catch (JFCALCExpErrException e) {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                        }
                        if (datumEnd == null)   {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                        }
                        datumEnd = DCHelper.lightCvtOrRetDCComplex(datumEnd);
                    }
                    if (DCHelper.lightCvtOrRetDCComplex(datumEnd).getReal().isInf() || DCHelper.lightCvtOrRetDCComplex(datumEnd).getImage().isInf()) {
                        bUseBasicInteg = false;
                    }
                }

                if (bUseBasicInteg)    {
                    DataClass datumStart = tsParameter.get(nParamIdx + 1).cloneSelf();
                    if (!(datumStart.getThisOrNull() instanceof DataClassString)) {
                        datumStart = DCHelper.lightCvtOrRetDCComplex(datumStart);
                    } else {
                        try {
                            datumStart = exprEvaluator.evaluateExpression(DCHelper.lightCvtOrRetDCString(datumStart).getStringValue(), new CurPos());    // evaluate data end if it is a string.
                        } catch (JFCALCExpErrException e) {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                        }
                        if (datumStart == null)   {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                        }
                        datumStart = DCHelper.lightCvtOrRetDCComplex(datumStart);
                    }
                    if (DCHelper.lightCvtOrRetDCComplex(datumStart).getReal().isInf() || DCHelper.lightCvtOrRetDCComplex(datumStart).getImage().isInf()) {
                        bUseBasicInteg = false;
                    }
                }
                progContext.mdynamicProgContext.mlVarNameSpaces.poll();      // needs to poll first because it contains variable to integrated.

                if (bUseBasicInteg) {
                    return new Integ_BasicFunction().callAction(tsParameter, listParamRawInputs, progContext);
                } else {
                    if (tsParameter.size() == 5) {
                        tsParameter.poll(); // the number of steps is set to be default.
                        listParamRawInputs.poll();
                    }
                    return new Integ_GKFunction().callAction(tsParameter, listParamRawInputs, progContext);
                }
            }
        }
    }    
    //public final static IntegrateFunction BUILTINFUNC_Integrate = new IntegrateFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new IntegrateFunction());}
        
    /* integrate Gauss-Kronrod method */
    public static class Integ_GKFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Integ_GKFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::calculus::integ_gk";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 8;
            mnMinParamNum = 4;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumCheckFinalResult = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            if (tsParameter.size() == 8) {    // do not use mnMaxParamNum here.
                datumCheckFinalResult = DCHelper.lightCvtOrRetDCMFPBool(tsParameter.poll());
            }
            boolean bCheckFinalResult = datumCheckFinalResult.getDataValue().booleanValue();
            
            DataClassSingleNum datumExceptNotEnoughSteps = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            if (tsParameter.size() == 7) {
                datumExceptNotEnoughSteps = DCHelper.lightCvtOrRetDCMFPBool(tsParameter.poll());
            }
            boolean bExceptNotEnoughSteps = datumExceptNotEnoughSteps.getDataValue().booleanValue();
            
            DataClassSingleNum datumCheckConverge = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
            if (tsParameter.size() == 6) {
                datumCheckConverge = DCHelper.lightCvtOrRetDCMFPBool(tsParameter.poll());
            }
            boolean bCheckConverge = datumCheckConverge.getDataValue().booleanValue();
            
            DataClassSingleNum datumNumofSteps = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
            if (tsParameter.size() == 5)    {
                datumNumofSteps = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());
                if (datumNumofSteps.getDataValue().isActuallyNegative())    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);  // if number of steps is a parameter, it must be positive or 0.
                }
            }
            long nNumOfSteps = datumNumofSteps.getDataValue().longValue();
            if (nNumOfSteps > 1024)    {
                nNumOfSteps = 1024;    // number of steps should be no more than 1024.
            }
            DataClass datumEndRaw = tsParameter.poll().cloneSelf();
            if (!(datumEndRaw.getThisOrNull() instanceof DataClassString))   {
                datumEndRaw = DCHelper.lightCvtOrRetDCComplex(datumEndRaw);
            }
            DataClass datumStartRaw = tsParameter.poll().cloneSelf();
            if (!(datumStartRaw.getThisOrNull() instanceof DataClassString)) {
                datumStartRaw = DCHelper.lightCvtOrRetDCComplex(datumStartRaw);
            }
            DataClassString datumStrDeltaVar = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
            String strDeltaVar = datumStrDeltaVar.getStringValue().trim().toLowerCase(Locale.US);
            DataClassString datumStrExpr = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
            UnknownVariable var = new UnknownVariable(strDeltaVar);
            LinkedList<UnknownVariable> lUnknown = new LinkedList<UnknownVariable>();
            lUnknown.addFirst(var);
            AbstractExpr aeStrExpr = null;
            try {
                aeStrExpr = ExprAnalyzer.analyseExpression(datumStrExpr.getStringValue(), new CurPos(), new LinkedList<Variable>(), progContext);
            } catch (Exception e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            try {
                // need to evaluate considering second order integral, x+y + 1, y is a constant, so simplifymost will calculate y + 1 directly.
                aeStrExpr = aeStrExpr.evaluateAExpr(lUnknown, progContext); // look on unknown dim as single value dim.
            }catch (Exception e)    {
                // for like x + y + 1, y may not in lUnknown and lVarNameSpaces. This exception may be thrown as cannot get result exception. so catch
                // any exceptions that throw.
            }
            // variable list has to be added after aeStrExpr is constructed considering x + y + 1, x is in lUnknown but y is not, if l is added
            // aeStrExpr is constructed then both x and y are in lVarNameSpaces. Then y is treated as variable NULL.
            LinkedList<Variable> l = new LinkedList<Variable>();
            l.addFirst(var);
            progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(l);
            ExprEvaluator exprEvaluator = new ExprEvaluator(progContext);
            DataClass datumEnd = datumEndRaw;
            if (datumEndRaw.getThisOrNull() instanceof DataClassString)   {
                try {
                    datumEnd = exprEvaluator.evaluateExpression(DCHelper.lightCvtOrRetDCString(datumEndRaw).getStringValue(), new CurPos());    // evaluate data end if it is a string.
                } catch (JFCALCExpErrException e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
                if (datumEnd == null)   {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                }
                datumEnd = DCHelper.lightCvtOrRetDCComplex(datumEnd);
            }
            DataClass datumStart = datumStartRaw;
            if (datumStartRaw.getThisOrNull() instanceof DataClassString)   {
                try {
                    datumStart = exprEvaluator.evaluateExpression(DCHelper.lightCvtOrRetDCString(datumStartRaw).getStringValue(), new CurPos());    // evaluate data start if it is a string.
                } catch (JFCALCExpErrException e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
                if (datumStart == null)   {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                }
                datumStart = DCHelper.lightCvtOrRetDCComplex(datumStart);
            }
            
            DataClass[] darrayResults = MathLib.integByGaussKronrod(aeStrExpr/*datumStrExpr.getStringValue()*/, strDeltaVar,
                                                                    datumStart, datumEnd, (int)nNumOfSteps,
                                                                    bCheckConverge, bExceptNotEnoughSteps, bCheckFinalResult,
                                                                    progContext, FuncEvaluator.msfunctionInterrupter);
            progContext.mdynamicProgContext.mlVarNameSpaces.poll();  // dont forget to pop-up stack.
            return darrayResults[0];
        }
    }    
    //public final static Integ_GKFunction BUILTINFUNC_Integ_GK = new Integ_GKFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Integ_GKFunction());}

    /* integrate basic method */
    public static class Integ_BasicFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Integ_BasicFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::calculus::integ_basic";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 5;
            mnMinParamNum = 4;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumNumofSteps = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
            if (tsParameter.size() == mnMaxParamNum)    {
                datumNumofSteps = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());
            }
            if (datumNumofSteps.getDataValue().compareTo(MFPNumeric.ONE) < 0)    {
                datumNumofSteps = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
            }
            long nNumOfSteps = datumNumofSteps.getDataValue().longValue();
            if (nNumOfSteps > 65536)    {
                nNumOfSteps = 65536;    // number of steps should be no more than 65536.
            }
            DataClass datumEndRaw = tsParameter.poll().cloneSelf();
            if (!(datumEndRaw.getThisOrNull() instanceof DataClassString))   {
                datumEndRaw = DCHelper.lightCvtOrRetDCComplex(datumEndRaw);
            }
            DataClass datumStartRaw = tsParameter.poll().cloneSelf();
            if (!(datumStartRaw.getThisOrNull() instanceof DataClassString)) {
                datumStartRaw = DCHelper.lightCvtOrRetDCComplex(datumStartRaw);
            }
            DataClassString datumStrDeltaVar = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
            DataClassString datumStrExpr = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
            UnknownVariable var = new UnknownVariable(datumStrDeltaVar.getStringValue().trim().toLowerCase(Locale.US));
            LinkedList<UnknownVariable> lUnknown = new LinkedList<UnknownVariable>();
            lUnknown.addFirst(var);
            AbstractExpr aeStrExpr = null;
            try {
                aeStrExpr = ExprAnalyzer.analyseExpression(datumStrExpr.getStringValue(), new CurPos(), new LinkedList<Variable>(), progContext);
            } catch (Exception e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            try {
                // need to evaluate considering second order integral, x+y + 1, y is a constant, so simplifymost will calculate y + 1 directly.
                aeStrExpr = aeStrExpr.evaluateAExpr(lUnknown, progContext); // look on unknown dim as single value dim.
            }catch (Exception e)    {
                // for like x + y + 1, y may not in lUnknown and lVarNameSpaces. This exception may be thrown as cannot get result exception. so catch
                // any exceptions that throw.
            }
            // variable list has to be added after aeStrExpr is constructed considering x + y + 1, x is in lUnknown but y is not, if l is added
            // aeStrExpr is constructed then both x and y are in lVarNameSpaces. Then y is treated as variable NULL.
            LinkedList<Variable> l = new LinkedList<Variable>();
            l.addFirst(var);
            progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(l);
            ExprEvaluator exprEvaluator = new ExprEvaluator(progContext);
            DataClass datumEnd = datumEndRaw;
            if (datumEndRaw.getThisOrNull() instanceof DataClassString)   {
                try {
                    datumEnd = exprEvaluator.evaluateExpression(DCHelper.lightCvtOrRetDCString(datumEndRaw).getStringValue(), new CurPos());    // evaluate data end if it is a string.
                } catch (JFCALCExpErrException e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
                if (datumEnd == null)   {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                }
            }
            DataClassComplex datumEndCplx = DCHelper.lightCvtOrRetDCComplex(datumEnd);
            DataClass datumStart = datumStartRaw;
            if (datumStartRaw.getThisOrNull() instanceof DataClassString)   {
                try {
                    datumStart = exprEvaluator.evaluateExpression(DCHelper.lightCvtOrRetDCString(datumStartRaw).getStringValue(), new CurPos());    // evaluate data start if it is a string.
                } catch (JFCALCExpErrException e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
                if (datumStart == null)   {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                }
            }
            DataClassComplex datumStartCplx = DCHelper.lightCvtOrRetDCComplex(datumStart);
            
            MFPNumeric mfpNumStartReal = datumStartCplx.getReal();
            MFPNumeric mfpNumStartImage = datumStartCplx.getImage();
            MFPNumeric mfpNumEndReal = datumEndCplx.getReal();
            MFPNumeric mfpNumEndImage = datumEndCplx.getImage();
            MFPNumeric mfpNumStepReal = MFPNumeric.ZERO;
            MFPNumeric mfpNumStepImage = MFPNumeric.ZERO;
            boolean bStartEndInfOrNan = mfpNumStartReal.isNanOrInf() || mfpNumStartImage.isNanOrInf() || mfpNumEndReal.isNanOrInf() || mfpNumEndImage.isNanOrInf();
            boolean bConsiderImage = !mfpNumStartImage.isActuallyZero() || !mfpNumEndImage.isActuallyZero();
            if (!bStartEndInfOrNan) {
                // save time because no need to create MFPNumeric
                BigDecimal bigDecStepReal = mfpNumEndReal.toBigDecimal().subtract(mfpNumStartReal.toBigDecimal())
                        .divide(new BigDecimal(nNumOfSteps), MFPNumeric.THE_MAX_ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP);
                mfpNumStepReal = new MFPNumeric(bigDecStepReal);
                BigDecimal bigDecStepImage = mfpNumEndImage.toBigDecimal().subtract(mfpNumStartImage.toBigDecimal())
                        .divide(new BigDecimal(nNumOfSteps), MFPNumeric.THE_MAX_ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP);
                mfpNumStepImage = new MFPNumeric(bigDecStepImage);
            } else {
                MFPNumeric mfpNumOfSteps = new MFPNumeric(nNumOfSteps);
                MFPNumeric mfpNumRealDistance = mfpNumEndReal.subtract(mfpNumStartReal);
                if (!mfpNumRealDistance.isActuallyZero()) {
                    mfpNumStepReal = MFPNumeric.divide(mfpNumRealDistance, mfpNumOfSteps);
                }
                MFPNumeric mfpNumImageDistance = mfpNumEndImage.subtract(mfpNumStartImage);
                if (!mfpNumImageDistance.isActuallyZero()) {
                    mfpNumStepImage = MFPNumeric.divide(mfpNumImageDistance, mfpNumOfSteps);
                }
            }
            DataClass datumIndex = new DataClassNull();
            MFPNumeric mfpNumReturnReal = MFPNumeric.ZERO;
            BigDecimal bigDecReturnReal = BigDecimal.ZERO;
            MFPNumeric mfpNumReturnImage = MFPNumeric.ZERO;
            BigDecimal bigDecReturnImage = BigDecimal.ZERO;
            MFPNumeric mfpNumIdxReal = mfpNumStartReal;
            MFPNumeric mfpNumIdxImage = mfpNumStartImage;
            boolean bUseBigDec2Calc = !bStartEndInfOrNan;
            BigDecimal bigDecHalf = BigDecimal.valueOf(0.5);
            for (int index = 0; index <= nNumOfSteps; index++)
            {
                if (index == nNumOfSteps) {
                    mfpNumIdxReal = mfpNumEndReal;
                    mfpNumIdxImage = mfpNumEndImage;
                } else if (index != 0) {
                    mfpNumIdxReal = mfpNumIdxReal.add(mfpNumStepReal);
                    mfpNumIdxImage = mfpNumIdxImage.add(mfpNumStepImage);
                }
                if (mfpNumIdxImage.isActuallyZero()) {
                    datumIndex = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumIdxReal);
                } else {
                    datumIndex = new DataClassComplex(mfpNumIdxReal, mfpNumIdxImage);
                }
                var.setValue(datumIndex);
                
                DataClass datumExprValue = null;
                if (aeStrExpr != null) {
                    try {
                        datumExprValue = aeStrExpr.evaluateAExprQuick(lUnknown, progContext);
                    } catch (Exception ex) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                    }
                }
                /*
                DataClass datumExprValue = exprEvaluator.evaluateExpression(datumStrExpr.getStringValue(), new CurPos());
                
                if (datumExprValue == null)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                }*/
                DataClassComplex datumExprValueCplx = DCHelper.lightCvtOrRetDCComplex(datumExprValue);
                MFPNumeric mfpNumValueReal = datumExprValueCplx.getReal();
                MFPNumeric mfpNumValueImage = datumExprValueCplx.getImage();
                if (bUseBigDec2Calc && (mfpNumValueReal.isNanOrInf() || mfpNumValueImage.isNanOrInf())) {
                    // we were using big decimal to do calculation to make it faster. Now we cannot because we see inf or nan.
                    mfpNumReturnReal = new MFPNumeric(bigDecReturnReal);
                    mfpNumReturnImage = new MFPNumeric(bigDecReturnImage);
                    bUseBigDec2Calc = false;
                }
                if (!bConsiderImage && !mfpNumValueImage.isActuallyZero()) {
                    bConsiderImage = true;  // now we need to consider image.
                }
                boolean bHeadOrTail = (index == 0 || index == nNumOfSteps);
                if (bUseBigDec2Calc) {
                    if (!mfpNumValueReal.isActuallyZero() && !mfpNumStepReal.isActuallyZero()) {
                        BigDecimal bigDecThis = mfpNumValueReal.toBigDecimal().multiply(mfpNumStepReal.toBigDecimal());
                        bigDecThis = bigDecThis.setScale(MFPNumeric.THE_MAX_ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP);
                        if (bHeadOrTail)    {
                            bigDecThis = bigDecThis.multiply(bigDecHalf);
                        }
                        bigDecReturnReal = bigDecReturnReal.add(bigDecThis);
                    }
                    if (!mfpNumValueImage.isActuallyZero() && !mfpNumStepImage.isActuallyZero()) {
                        BigDecimal bigDecThis = mfpNumValueImage.toBigDecimal().multiply(mfpNumStepImage.toBigDecimal());
                        bigDecThis = bigDecThis.setScale(MFPNumeric.THE_MAX_ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP);
                        if (bHeadOrTail)    {
                            bigDecThis = bigDecThis.multiply(bigDecHalf);
                        }
                        bigDecReturnReal = bigDecReturnReal.subtract(bigDecThis);
                    }
                    if (!mfpNumValueReal.isActuallyZero() && !mfpNumStepImage.isActuallyZero()) {
                        BigDecimal bigDecThis = mfpNumValueReal.toBigDecimal().multiply(mfpNumStepImage.toBigDecimal());
                        bigDecThis = bigDecThis.setScale(MFPNumeric.THE_MAX_ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP);
                        if (bHeadOrTail)    {
                            bigDecThis = bigDecThis.multiply(bigDecHalf);
                        }
                        bigDecReturnImage = bigDecReturnImage.add(bigDecThis);
                    }
                    if (!mfpNumValueImage.isActuallyZero() && !mfpNumStepReal.isActuallyZero()) {
                        BigDecimal bigDecThis = mfpNumValueImage.toBigDecimal().multiply(mfpNumStepReal.toBigDecimal());
                        bigDecThis = bigDecThis.setScale(MFPNumeric.THE_MAX_ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP);
                        if (bHeadOrTail)    {
                            bigDecThis = bigDecThis.multiply(bigDecHalf);
                        }
                        bigDecReturnImage = bigDecReturnImage.add(bigDecThis);
                    }
                } else {
                    MFPNumeric mfpNumThis = mfpNumValueReal.multiply(mfpNumStepReal);
                    if (bHeadOrTail)    {
                        mfpNumThis = mfpNumThis.multiply(MFPNumeric.HALF);
                    }
                    mfpNumReturnReal = mfpNumReturnReal.add(mfpNumThis);
                    mfpNumThis = mfpNumValueImage.multiply(mfpNumStepImage);
                    if (bHeadOrTail)    {
                        mfpNumThis = mfpNumThis.multiply(MFPNumeric.HALF);
                    }
                    mfpNumReturnReal = mfpNumReturnReal.subtract(mfpNumThis);
                    if (bConsiderImage) {
                        mfpNumThis = mfpNumValueReal.multiply(mfpNumStepImage);
                        if (bHeadOrTail)    {
                            mfpNumThis = mfpNumThis.multiply(MFPNumeric.HALF);
                        }
                        mfpNumReturnImage = mfpNumReturnImage.add(mfpNumThis);
                        mfpNumThis = mfpNumValueImage.multiply(mfpNumStepReal);
                        if (bHeadOrTail)    {
                            mfpNumThis = mfpNumThis.multiply(MFPNumeric.HALF);
                        }
                        mfpNumReturnImage = mfpNumReturnImage.add(mfpNumThis);
                    }
                }
            }
            if (bUseBigDec2Calc)    {
                mfpNumReturnReal = new MFPNumeric(bigDecReturnReal);
                mfpNumReturnImage = new MFPNumeric(bigDecReturnImage);
            }
            if (mfpNumReturnImage.isActuallyZero()) {
                datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumReturnReal);
            } else {
                datumReturnNum = new DataClassComplex(mfpNumReturnReal, mfpNumReturnImage);
            }
            progContext.mdynamicProgContext.mlVarNameSpaces.poll();
            return datumReturnNum;
        }
    }    
    //public final static Integ_BasicFunction BUILTINFUNC_Integ_Basic = new Integ_BasicFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Integ_BasicFunction());}

    /* evaluate a lim expression */
    public static class LimFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public LimFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::calculus::lim";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 4;
            mnMinParamNum = 3;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            if (tsParameter.size() != mnMinParamNum && tsParameter.size() != mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumOneTenth = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE_TENTH);
            DataClass datumLimDirection = new DataClassNull();
            if (tsParameter.size() == mnMaxParamNum) {
                datumLimDirection = tsParameter.poll();
                DataClassComplex datumLimDirectionCplx = DCHelper.lightCvtOrRetDCComplex(datumLimDirection);
                if (datumLimDirectionCplx.getImage().isActuallyZero()) {    // double.
                    if (datumLimDirectionCplx.getReal().isActuallyNegative()) {
                        datumLimDirection = BuiltinProcedures.evaluateNegSign(datumOneTenth);
                    } else {
                        // positive or 0 datumLimDirection is 0.1
                        datumLimDirection = datumOneTenth;
                    }
                } else {    // complex.
                    datumLimDirection = BuiltinProcedures.evaluateDivision(datumLimDirection,
                                            BuiltinProcedures.evaluateAbs(datumLimDirection));
                    datumLimDirection = BuiltinProcedures.evaluateMultiplication(datumLimDirection, datumOneTenth);
                }
            }
            
            DataClassComplex datumX0Expr = DCHelper.lightCvtOrRetDCComplex(tsParameter.poll());
            DataClassString datumStrXVarName = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
            String strVarName = datumStrXVarName.getStringValue().trim().toLowerCase(Locale.US);
            DataClassString datumStrExpr = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
            String strExpr = datumStrExpr.getStringValue().trim();

            UnknownVariable var = new UnknownVariable(strVarName);
            LinkedList<UnknownVariable> l = new LinkedList<UnknownVariable>();
            l.addFirst(var);
            AbstractExpr aeExpr = new AEInvalid();
            try {
                aeExpr = ExprAnalyzer.analyseExpression(strExpr, new CurPos(), new LinkedList<Variable>(), progContext);
            } catch (Exception e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            boolean bIsDerivable = true;
            try {
                aeExpr = aeExpr.simplifyAExprMost(l, new SimplifyParams(true, true, true), progContext); // look on unknown dim as single value dim.
                AbstractExpr aeReturn = FuncEvaluator.mspm.deriInDefByPtn1VarDeriIdentifier(aeExpr, l, progContext);
                if (aeReturn == null) {
                    bIsDerivable = false;
                }
            } catch (JSmartMathErrException ex) {
                bIsDerivable = false;
            }
            
            if (bIsDerivable) {
                try {
                    // for lim calculation
                    DataClass datumReturn = MathLib.deriFormula(aeExpr, strVarName, datumX0Expr,
                                new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO), 0, progContext);
                    return datumReturn;
                } catch (Exception ex) {
                    
                }
            }
            // todo : still cannot handle infinite.
            DataClass datumReturn = MathLib.deriRidders(aeExpr, strVarName, datumX0Expr, datumLimDirection, 0, progContext);
            return datumReturn;
        }
    }    
    //public final static LimFunction BUILTINFUNC_Lim = new LimFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new LimFunction());}
        
    /* evaluate a string expression */
    public static class EvaluateFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public EvaluateFunction() {
            mstrProcessedNameWithFullCS = "::mfp::statement::evaluate";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = -1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() % 2 != 1)    //number of parameters must be odd.
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            LinkedList<Variable> l = new LinkedList<Variable>();
            progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(l);
            int nNumOfDeltaVars = (tsParameter.size() - 1)/2;
            for (int idx = 0; idx < nNumOfDeltaVars; idx ++) {
                DataClass datumValue = tsParameter.poll().cloneSelf();
                DataClass datumStrDeltaVar = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
                Variable var = new Variable(DCHelper.lightCvtOrRetDCString(datumStrDeltaVar).getStringValue().trim().toLowerCase(Locale.US));
                var.setValue(datumValue);
                l.addFirst(var);
                
            }
            DataClassString datumStrExpr = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
            
            // here out-variable name spaces (lVarNameSpaces) must be heritated from upper level because of the following case:
            // evaluate("evaluate(\"x+y+3\",\"x\",4)", "y", 1)
            ExprEvaluator exprEvaluator = new ExprEvaluator(progContext);
            try {
                datumReturnNum = exprEvaluator.evaluateExpression(
                                            datumStrExpr.getStringValue(), new CurPos());
            } catch (JFCALCExpErrException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            progContext.mdynamicProgContext.mlVarNameSpaces.poll();
            if (datumReturnNum == null)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
            }
            return datumReturnNum;
        }
    }    
    //public final static EvaluateFunction BUILTINFUNC_Evaluate = new EvaluateFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new EvaluateFunction());}
        
    public static class SystemFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public SystemFunction() {
            mstrProcessedNameWithFullCS = "::mfp::system::system";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            DataClass datumReturnNum = new DataClassNull();
            boolean bSingleParam = true;
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            } else if (tsParameter.getFirst().getThisOrNull() instanceof DataClassArray) {
                bSingleParam = false;
            }
            
            LinkedList<String> listExecParams = new LinkedList<String>();
            if (bSingleParam) {
                DataClassString datumSystemCmd = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
                String strSysCmd = datumSystemCmd.getStringValue();
                String[] strarray = strSysCmd.split("\\s+");
                listExecParams.addAll(Arrays.asList(strarray));
            } else {
                // because convert to an array, I have to clone self.
                DataClassArray datumCmdArray = DCHelper.lightCvtOrRetDCArray(tsParameter.poll().cloneSelf());
                for (int idx = 0; idx < datumCmdArray.getDataListSize(); idx ++) {
                    DataClassString datumElem = DCHelper.lightCvtOrRetDCString(datumCmdArray.getDataList()[idx]);
                    String strSysCmd = datumElem.getStringValue();
                    listExecParams.add(strSysCmd);
                }
            }

            Thread threadExecI = null, threadExecO = null, threadExecE = null;
            Process p = null;
            try {
                ProcessBuilder procBlder;
                //p = Runtime.getRuntime().exec(listExecParams.toArray(new String[0]), null, new File(IOLib.getWorkingDir()));
                procBlder = new ProcessBuilder(listExecParams);
                procBlder.directory(new File(IOLib.getWorkingDir()));
                procBlder.redirectErrorStream(true);
                p = procBlder.start();
                // at this moment, only support output.
                SysExecProcIOE sysExecI = new SysExecProcIOE(p, 1, FuncEvaluator.msstreamConsoleInput, FuncEvaluator.msstreamLogOutput, FuncEvaluator.msstreamLogOutput);
                //SysExecProcIOE sysExecO = new SysExecProcIOE(p, 0, msstreamConsoleInput, msstreamLogOutput, msstreamLogOutput);
                //SysExecProcIOE sysExecE = new SysExecProcIOE(p, 2, msstreamConsoleInput, msstreamLogOutput, msstreamLogOutput);

                threadExecI = new Thread(sysExecI);
                //Thread threadExecO = new Thread(sysExecO);
                //threadExecE = new Thread(sysExecE);
                threadExecI.start();
                //threadExecO.start();
                //threadExecE.start();
                int exitVal = p.waitFor();
                threadExecI.interrupt();
                //threadExecO.interrupt();
                //threadExecE.interrupt();
                datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(exitVal));
            } catch (Throwable t) {
                if (threadExecI != null && threadExecI.isAlive()) {
                    threadExecI.interrupt();
                }
                if (threadExecO != null && threadExecO.isAlive()) {
                    threadExecO.interrupt();
                }
                if (threadExecE != null && threadExecE.isAlive()) {
                    threadExecE.interrupt();
                }

                if (p != null) {
                    p.destroy();
                }
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_RUNTIME_ERROR);
            }
            return datumReturnNum;
        }
    }    
    //public final static SystemFunction BUILTINFUNC_System = new SystemFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new SystemFunction());}
        
    public static class SleepFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public SleepFunction() {
            mstrProcessedNameWithFullCS = "::mfp::system::sleep";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumSleepMilliSeconds = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());
            Thread.sleep((int)datumSleepMilliSeconds.getDataValue().longValue());
            datumReturnNum = null;
            return datumReturnNum;
        }
    }    
    //public final static SleepFunction BUILTINFUNC_Sleep = new SleepFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new SleepFunction());}
        
    public static class Conv_str_to_intsFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Conv_str_to_intsFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::conv_str_to_ints";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* string length */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumString = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
            String str = datumString.getStringValue();
            if (str == null) {
                str = "";
            }
            DataClass[] datumarray = new DataClass[str.length()];
            for (int idx = 0; idx < str.length(); idx ++) {
                DataClass datum = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(str.codePointAt(idx)));
                datumarray[idx] = datum;
            }
            datumReturnNum = new DataClassArray(datumarray);
            return datumReturnNum;
        }
    }    
    //public final static Conv_str_to_intsFunction BUILTINFUNC_Conv_str_to_ints = new Conv_str_to_intsFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Conv_str_to_intsFunction());}
        
    public static class Conv_ints_to_strFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Conv_ints_to_strFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::conv_ints_to_str";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* string length */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumInput = tsParameter.poll().cloneSelf();
            String str = "";
            if (!(datumInput instanceof DataClassArray)) {
                int nValue = DCHelper.lightCvtOrRetDCMFPInt(datumInput).getDataValue().intValue();
                try {
                    char[] chars = Character.toChars(nValue);
                    for (char c : chars) {
                        str += c;
                    }
                } catch (IllegalArgumentException e) {
                    char c = 0; // invalid char.
                    str += c;
                }
            } else {
                int[] narrayValues = new int[DCHelper.lightCvtOrRetDCArray(datumInput).getDataListSize()];
                for (int idx = 0; idx < DCHelper.lightCvtOrRetDCArray(datumInput).getDataListSize(); idx ++) {
                    DataClassSingleNum datum = DCHelper.lightCvtOrRetDCMFPInt(DCHelper.lightCvtOrRetDCArray(datumInput).getDataList()[idx]);
                    narrayValues[idx] = datum.getDataValue().intValue();
                    try {
                        char[] chars = Character.toChars(narrayValues[idx]);
                        for (char c : chars) {
                            str += c;
                        }
                    } catch (IllegalArgumentException e) {
                        char c = 0; // invalid char.
                        str += c;
                    }
                }
            }
            datumReturnNum = new DataClassString(str);
            return datumReturnNum;
        }
    }    
    //public final static Conv_ints_to_strFunction BUILTINFUNC_Conv_ints_to_str = new Conv_ints_to_strFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Conv_ints_to_strFunction());}
        
    public static class StrlenFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public StrlenFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::strlen";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* string length */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumString = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
            MFPNumeric mfpNumStrLen = new MFPNumeric(datumString.getStringValue().length());
            datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, mfpNumStrLen);
            return datumReturnNum;
        }
    }    
    //public final static StrlenFunction BUILTINFUNC_Strlen = new StrlenFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new StrlenFunction());}
        
    /* string copy, string index must be integer so convert MFPNumeric to long then to int */
    public static class StrcpyFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public StrcpyFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::strcpy";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 6;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumStrSrc = DCHelper.lightCvtOrRetDCString(tsParameter.removeLast());
            DataClassString datumStrDest = DCHelper.lightCvtOrRetDCString(tsParameter.removeLast());
            DataClass[] datumlistParams = new DataClass[4];
            int nIndex = 0;
            for (; nIndex < 4; nIndex ++)    {
                datumlistParams[nIndex] = new DataClassNull();
                if (nIndex == 0)    {
                    // src start
                    datumlistParams[nIndex] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                } else if (nIndex == 1)    {
                    // src end (one character passed the last character)
                    datumlistParams[nIndex] = new DataClassSingleNum(
                            DATATYPES.DATUM_MFPINT,
                            new MFPNumeric(datumStrSrc.getStringValue().length()));
                } else if (nIndex == 2)    {
                    // dest start
                    datumlistParams[nIndex] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                } else if (nIndex == 3)    {
                    // dest end (one character passed the last character)
                    datumlistParams[nIndex] = new DataClassSingleNum(
                            DATATYPES.DATUM_MFPINT,
                            new MFPNumeric(datumStrDest.getStringValue().length()));
                }
            }
            nIndex = 0;
            while(tsParameter.size() > 0)    {
                datumlistParams[nIndex] = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.removeLast());
                nIndex ++;
            }
            String strSrc = datumStrSrc.getStringValue();
            String strDest = datumStrDest.getStringValue();
            int nSrcStart = (int)DCHelper.lightCvtOrRetDCMFPInt(datumlistParams[0]).getDataValue().longValue();
            if (nSrcStart > strSrc.length() || nSrcStart < 0)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            int nSrcEnd = (int)DCHelper.lightCvtOrRetDCMFPInt(datumlistParams[1]).getDataValue().longValue();
            if (nSrcEnd > strSrc.length() || nSrcEnd < nSrcStart)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            int nDestStart = (int)DCHelper.lightCvtOrRetDCMFPInt(datumlistParams[2]).getDataValue().longValue();
            if (nDestStart > strDest.length() || nDestStart < 0)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            int nDestEnd = (int)DCHelper.lightCvtOrRetDCMFPInt(datumlistParams[3]).getDataValue().longValue();
            if (nDestEnd > strDest.length() || nDestEnd < nDestStart)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            String strReturn = strDest.substring(0, nDestStart)
                    + strSrc.substring(nSrcStart, nSrcEnd)
                    + strDest.substring(nDestEnd);
            datumReturnNum = new DataClassString(strReturn);
            return datumReturnNum;
        }
    }    
    //public final static StrcpyFunction BUILTINFUNC_Strcpy = new StrcpyFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new StrcpyFunction());}
        
    public static class StrcatFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public StrcatFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::strcat";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = -1;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* string catenate */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            datumReturnNum = new DataClassString("");
            while (tsParameter.size() > 0)    {
                DataClassString datumStr = DCHelper.lightCvtOrRetDCString(tsParameter.poll());
                datumReturnNum = new DataClassString(datumStr.getStringValue() + DCHelper.lightCvtOrRetDCString(datumReturnNum).getStringValue());
            }
            return datumReturnNum;
        }
    }    
    //public final static StrcatFunction BUILTINFUNC_Strcat = new StrcatFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new StrcatFunction());}
        
    /* string compare (case sensative or ignore case, string index must be integer so convert MFPNumeric to long then to int */
    public static class StrcmpFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public StrcmpFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::strcmp";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 6;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumStrSrc = DCHelper.lightCvtOrRetDCString(tsParameter.removeLast());
            DataClassString datumStrDest = DCHelper.lightCvtOrRetDCString(tsParameter.removeLast());
            DataClass[] datumlistParams = new DataClass[4];
            int nIndex = 0;
            for (; nIndex < 4; nIndex ++)    {
                datumlistParams[nIndex] = new DataClassNull();
                if (nIndex == 0)    {
                    // src start
                    datumlistParams[nIndex] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                } else if (nIndex == 1)    {
                    // src end (one character passed the last character)
                    datumlistParams[nIndex] = new DataClassSingleNum(
                            DATATYPES.DATUM_MFPINT,
                            new MFPNumeric(datumStrSrc.getStringValue().length()));
                } else if (nIndex == 2)    {
                    // dest start
                    datumlistParams[nIndex] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                } else if (nIndex == 3)    {
                    // dest end (one character passed the last character)
                    datumlistParams[nIndex] = new DataClassSingleNum(
                            DATATYPES.DATUM_MFPINT,
                            new MFPNumeric(datumStrDest.getStringValue().length()));
                }
            }
            nIndex = 0;
            while(tsParameter.size() > 0)    {
                datumlistParams[nIndex] = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.removeLast());
                nIndex++;
            }
            String strSrc = datumStrSrc.getStringValue();
            String strDest = datumStrDest.getStringValue();
            int nSrcStart = (int)DCHelper.lightCvtOrRetDCMFPInt(datumlistParams[0]).getDataValue().longValue();
            if (nSrcStart > strSrc.length() || nSrcStart < 0)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            int nSrcEnd = (int)DCHelper.lightCvtOrRetDCMFPInt(datumlistParams[1]).getDataValue().longValue();
            if (nSrcEnd > strSrc.length() || nSrcEnd < nSrcStart)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            int nDestStart = (int)DCHelper.lightCvtOrRetDCMFPInt(datumlistParams[2]).getDataValue().longValue();
            if (nDestStart > strDest.length() || nDestStart < 0)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            int nDestEnd = (int)DCHelper.lightCvtOrRetDCMFPInt(datumlistParams[3]).getDataValue().longValue();
            if (nDestEnd > strDest.length() || nDestEnd < nDestStart)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            int nReturn = 0;
            if (mstrarrayFullCS[mstrarrayFullCS.length - 1].compareTo("strcmp") == 0) {
                nReturn = strSrc.substring(nSrcStart, nSrcEnd)
                            .compareTo(strDest.substring(nDestStart, nDestEnd));
            } else {    // stricmp
                nReturn = strSrc.substring(nSrcStart, nSrcEnd)
                            .compareToIgnoreCase(strDest.substring(nDestStart, nDestEnd));
            }
            datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nReturn));
            return datumReturnNum;
        }
    }    
    //public final static StrcmpFunction BUILTINFUNC_Strcmp = new StrcmpFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new StrcmpFunction());}

    public static class StricmpFunction extends StrcmpFunction {
		private static final long serialVersionUID = 1L;

        public StricmpFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::stricmp";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 6;
            mnMinParamNum = 2;
        }
    }
    //public final static StricmpFunction BUILTINFUNC_Stricmp = new StricmpFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new StricmpFunction());}
    
    /* sub-string, string index must be integer so convert MFPNumeric to long then to int */
    public static class StrsubFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public StrsubFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::strsub";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumStr = DCHelper.lightCvtOrRetDCString(tsParameter.removeLast());
            DataClassSingleNum datumStart = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.removeLast());
            int nStart = (int)datumStart.getDataValue().longValue();
            if (nStart < 0 || nStart > datumStr.getStringValue().length())    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            int nEnd = datumStr.getStringValue().length();
            DataClassSingleNum datumEnd = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nEnd));
            if (tsParameter.size() > 0)    {
                datumEnd = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.removeLast());
                if (datumEnd.getDataValue().longValue() < nStart || datumEnd.getDataValue().longValue() > datumStr.getStringValue().length())    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                }
                nEnd = (int)datumEnd.getDataValue().longValue();
            }
            datumReturnNum = new DataClassString(datumStr.getStringValue().substring(nStart, nEnd));
            return datumReturnNum;
        }
    }    
    //public final static StrsubFunction BUILTINFUNC_Strsub = new StrsubFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new StrsubFunction());}
        
    public static class Break_lineFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Break_lineFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::break_line";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 0;
            mnMinParamNum = 0;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* string length */
        {
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumReturnNum = new DataClassString("\n");
            return datumReturnNum;
        }
    }    
    //public final static Break_lineFunction BUILTINFUNC_Break_line = new Break_lineFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Break_lineFunction());}
        
    /* convert to string, to lower case string or to upper case string */
    public static class To_stringFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public To_stringFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::to_string";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumValue = tsParameter.poll();    // no need deep copy here because we will not change datumValue.
            String strValue = "";
            if (datumValue.getThisOrNull() instanceof DataClassString) {
                strValue = DCHelper.lightCvtOrRetDCString(datumValue).getStringValue();
            } else  {
                strValue = datumValue.output();
            }
            String strPureFuncName = mstrarrayFullCS[mstrarrayFullCS.length - 1];
            if (strPureFuncName.compareTo("to_lowercase_string") == 0)    {
                datumReturnNum = new DataClassString(strValue.toLowerCase(Locale.US));
            } else if (strPureFuncName.compareTo("to_uppercase_string") == 0)    {
                datumReturnNum = new DataClassString(strValue.toUpperCase(Locale.US));
            } else  {   // to_string or tostring
                datumReturnNum = new DataClassString(strValue);
            }
            return datumReturnNum;
        }
    }    
    //public final static To_stringFunction BUILTINFUNC_To_string = new To_stringFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new To_stringFunction());}
    
    public static class TostringFunction extends To_stringFunction {
		private static final long serialVersionUID = 1L;

        public TostringFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::tostring";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static TostringFunction BUILTINFUNC_Tostring = new TostringFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new TostringFunction());}
    
    public static class To_lowercase_stringFunction extends To_stringFunction {
		private static final long serialVersionUID = 1L;

        public To_lowercase_stringFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::to_lowercase_string";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static To_lowercase_stringFunction BUILTINFUNC_To_lowercase_string = new To_lowercase_stringFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new To_lowercase_stringFunction());}
    
    public static class To_uppercase_stringFunction extends To_stringFunction {
		private static final long serialVersionUID = 1L;

        public To_uppercase_stringFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::to_uppercase_string";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static To_uppercase_stringFunction BUILTINFUNC_To_uppercase_string = new To_uppercase_stringFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new To_uppercase_stringFunction());}
        
    /* convert an expr to string for testing purpose */
    public static class Expr_to_stringFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Expr_to_stringFunction() {
            mstrProcessedNameWithFullCS = "::mfp::statement::expr_to_string";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumValue = tsParameter.poll().cloneSelf();
            if (!(datumValue.getThisOrNull() instanceof DataClassString)) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            } else  {
                String strExpr = DCHelper.lightCvtOrRetDCString(datumValue).getStringValue();
                String strOutput;
                AbstractExpr aexpr = AEInvalid.AEINVALID;
                try {
                    aexpr = ExprAnalyzer.analyseExpression(strExpr, new CurPos(), new LinkedList<Variable>(), progContext);
                    strOutput = aexpr.output();
                } catch (JSmartMathErrException ex) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_FORMAT);
                }
                datumReturnNum = new DataClassString(strOutput);
            }
            return datumReturnNum;
        }
    }    
    //public final static Expr_to_stringFunction BUILTINFUNC_Expr_to_string = new Expr_to_stringFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Expr_to_stringFunction());}
        
    /* trim string */
    public static class TrimFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public TrimFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::trim";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumValue = tsParameter.poll().cloneSelf();
            if (!(datumValue.getThisOrNull() instanceof DataClassString)) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            }
            String strInput = DCHelper.lightCvtOrRetDCString(datumValue).getStringValue();
            String strOutput = strInput;
            String strPureFuncName = mstrarrayFullCS[mstrarrayFullCS.length - 1];
            if (strPureFuncName.equals("trim_left")) {
                // trim left
                int i = 0;
                while (i < strInput.length() && strInput.charAt(i) <= ' ') {
                    i++;
                }
                strOutput = strInput.substring(i);
            } else if (strPureFuncName.equals("trim_right")) {
                // trim right
                int i = strInput.length()-1;
                while (i >= 0 && Character.isWhitespace(strInput.charAt(i))) {
                    i--;
                }
                strOutput = strInput.substring(0,i+1);
            } else  {   // nTrimSide == 0
                strOutput = strInput.trim();
            }
            datumReturnNum = new DataClassString(strOutput);
            return datumReturnNum;
        }
    }    
    //public final static TrimFunction BUILTINFUNC_Trim = new TrimFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new TrimFunction());}

    public static class Trim_leftFunction extends TrimFunction {
		private static final long serialVersionUID = 1L;

        public Trim_leftFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::trim_left";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Trim_leftFunction BUILTINFUNC_Trim_left = new Trim_leftFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Trim_leftFunction());}

    public static class Trim_rightFunction extends TrimFunction {
		private static final long serialVersionUID = 1L;

        public Trim_rightFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::trim_right";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
    }
    //public final static Trim_rightFunction BUILTINFUNC_Trim_right = new Trim_rightFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Trim_rightFunction());}
    
    /* split string */
    public static class SplitFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public SplitFunction() {
            mstrProcessedNameWithFullCS = "::mfp::string::split";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumValue = tsParameter.getLast().cloneSelf();
            if (!(datumValue.getThisOrNull() instanceof DataClassString)) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            }
            String strInput = DCHelper.lightCvtOrRetDCString(datumValue).getStringValue();
            DataClass datumReg = tsParameter.getFirst().cloneSelf();
            if (!(datumReg.getThisOrNull() instanceof DataClassString)) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            }
            String strReg = DCHelper.lightCvtOrRetDCString(datumReg).getStringValue();
            String[] strarrayOutputs = new String[0];
            try {
                strarrayOutputs = strInput.split(strReg);
            } catch (PatternSyntaxException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_FORMAT);
            }
            DataClass[] dataarrayOutputs = new DataClass[strarrayOutputs.length];
            for (int idx = 0; idx < strarrayOutputs.length; idx ++) {
                DataClass datumReturn = new DataClassString(strarrayOutputs[idx]);
                dataarrayOutputs[idx] = datumReturn;
            }
            datumReturnNum = new DataClassArray(dataarrayOutputs);
            return datumReturnNum;
        }
    }    
    //public final static SplitFunction BUILTINFUNC_Split = new SplitFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new SplitFunction());}
        
    public static class Get_num_of_results_setsFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_num_of_results_setsFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::polynomial::get_num_of_results_sets";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* get number of solved result sets*/
        {
            DataClass datumReturnNum = new DataClassNull();
            // a read only function. Will not change parameter.
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumValue = tsParameter.poll(); // need not to do deep copy coz it is read only function
            //datumValue.copyTypeValueDeep(tsParameter.poll());
            if (!(datumValue.getThisOrNull() instanceof DataClassArray)) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            } else  {
                int nNumOfResultSets = DCHelper.lightCvtOrRetDCArray(datumValue).getDataListSize();
                datumReturnNum = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nNumOfResultSets));
            }
            return datumReturnNum;
        }
    }    
    //public final static Get_num_of_results_setsFunction BUILTINFUNC_Get_num_of_results_sets = new Get_num_of_results_setsFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_num_of_results_setsFunction());}
        
    public static class Get_solved_results_setFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_solved_results_setFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::polynomial::get_solved_results_set";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* get one solved results set */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumIndex = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());  // index should not be too large. it must be an integer.
            DataClass datumResultSets = tsParameter.poll().cloneSelf();
            if (!(datumResultSets.getThisOrNull() instanceof DataClassArray)) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            }
            if (datumIndex.getDataValue().longValue() < 0
                    || datumIndex.getDataValue().longValue() >= DCHelper.lightCvtOrRetDCArray(datumResultSets).getDataListSize())   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            
            datumReturnNum = DCHelper.lightCvtOrRetDCArray(datumResultSets).getDataList()[(int)datumIndex.getDataValue().longValue()];
            return datumReturnNum;
        }
    }    
    //public final static Get_solved_results_setFunction BUILTINFUNC_Get_solved_results_set = new Get_solved_results_setFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_solved_results_setFunction());}
        
    public static class Get_variable_resultsFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_variable_resultsFunction() {
            mstrProcessedNameWithFullCS = "::mfp::math::polynomial::get_variable_results";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException    /* get all results of a variable */
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() != mnMinParamNum)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassSingleNum datumVarIndex = DCHelper.lightCvtOrRetDCMFPInt(tsParameter.poll());   // variable index shouldn't be too large. It should be an integer.
            if (datumVarIndex.getDataValue().longValue() < 0)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            DataClass datumResultSets = tsParameter.poll().cloneSelf();
            if (!(datumResultSets.getThisOrNull() instanceof DataClassArray)) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            }
            DataClass[] arrayDataValues = new DataClass[DCHelper.lightCvtOrRetDCArray(datumResultSets).getDataListSize()];
            for (int idx = 0; idx < DCHelper.lightCvtOrRetDCArray(datumResultSets).getDataListSize(); idx ++)  {
                DataClassArray datumResultElement = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datumResultSets).getDataList()[idx]);
                if (datumVarIndex.getDataValue().longValue() >= datumResultElement.getDataListSize())   {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                }
                arrayDataValues[idx]
                        = datumResultElement.getDataList()[(int)datumVarIndex.getDataValue().longValue()];
            }
            datumReturnNum = new DataClassArray(arrayDataValues);
            return datumReturnNum;
        }
    }    
    //public final static Get_variable_resultsFunction BUILTINFUNC_Get_variable_results = new Get_variable_resultsFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_variable_resultsFunction());}
        
    /* if (condition1, trueresult1, condition2, trueresult2, ... falseresult) */
    public static class IffFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public IffFunction() {
            mstrProcessedNameWithFullCS = "::mfp::command::iff";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = -1;
            mnMinParamNum = 3;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            DataClass datumReturnNum = new DataClassNull();
            if (tsParameter.size() < mnMinParamNum || (tsParameter.size() - 1) % 2 != 0)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            // keep in mind that in tsParameter the order of params is from last to first.
            int idx = 0;
            boolean bGetValue = false;
            while (idx <= tsParameter.size() - 2)    {
                DataClass datumIfCondition = DCHelper.lightCvtOrRetDCMFPBool(tsParameter.get(tsParameter.size() - 1 - idx));
                if (datumIfCondition.isEqual(new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.ZERO)))    {
                    // condition == false
                    idx += 2;
                } else    {
                    // condition == true
                    datumReturnNum = tsParameter.get(tsParameter.size() - 2 - idx).cloneSelf();
                    bGetValue = true;
                    break;
                }
            }
            if (bGetValue == false) {
                datumReturnNum = tsParameter.getFirst().cloneSelf();
            }
            return datumReturnNum;
        }
    }    
    //public final static IffFunction BUILTINFUNC_Iff = new IffFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new IffFunction());}
        
    /* hash_code function */
    public static class Hash_codeFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Hash_codeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::object::hash_code";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> tsParameter, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (tsParameter.size() < mnMinParamNum || tsParameter.size() > mnMaxParamNum)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass param = tsParameter.getLast();
            int hashCode = param.getHashCode();
            
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(hashCode));
        }
    }    
    //public final static IffFunction BUILTINFUNC_Iff = new IffFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Hash_codeFunction());}
}
