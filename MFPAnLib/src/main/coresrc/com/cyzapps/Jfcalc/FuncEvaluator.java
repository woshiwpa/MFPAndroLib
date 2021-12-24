// MFP project, FuncEvaluator.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;

import java.io.IOException;
import java.util.*;

import com.cyzapps.JGI2D.DisplayLib.IGraphicDisplay;
import com.cyzapps.JPlatformHW.PlatformHWManager;
import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.ScriptAnalyzer;
import com.cyzapps.Jmfp.ScriptAnalyzer.FuncRetException;
import com.cyzapps.Jmfp.ScriptAnalyzer.ScriptStatementException;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jmfp.FunctionEntry;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.ScriptAnalyzer.InFunctionCSManager;
import com.cyzapps.Jmfp.StatementType;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEFunction;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.ExprAnalyzer;

import com.cyzapps.Jsma.PatternManager;
import com.cyzapps.Multimedia.MultimediaManager;
import com.cyzapps.OSAdapter.ParallelManager.CommunicationManager;
import com.cyzapps.OSAdapter.RtcMMediaManager;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.Oomfp.MFPClassDefinition;
import com.cyzapps.Oomfp.MFPClassInstance;
import com.cyzapps.Oomfp.MemberFunction;
import com.cyzapps.Oomfp.SpaceMember.AccessRestriction;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FuncEvaluator    {

    public static abstract class ConsoleInputStream    {
        public abstract void doBeforeInput();
        public abstract String inputString() throws InterruptedException;
        public abstract void doAfterInput();
    }
    public static ConsoleInputStream msstreamConsoleInput = null;
    
    public static abstract class LogOutputStream {
        public abstract void outputString(String str) throws InterruptedException;
    }
    public static LogOutputStream msstreamLogOutput = null;

    public static abstract class FunctionInterrupter    {
        public abstract boolean shouldInterrupt();
        public abstract void interrupt() throws InterruptedException;        
    }
    public static FunctionInterrupter msfunctionInterrupter = null;

    public static abstract class GraphPlotter    {
        public abstract boolean plotGraph(String strGraphInfo);
    }
    public static GraphPlotter msgraphPlotter = null;
    public static GraphPlotter msgraphPlotter3D = null;
    
    public static abstract class GraphicDisplayInterfaceManager {
    	public abstract IGraphicDisplay openScreenDisplay(String strTitle, com.cyzapps.VisualMFP.Color clr, boolean bConfirmClose,
                double[] size, boolean bResizable, int orientation);
        public abstract void shutdownScreenDisplay(IGraphicDisplay gdi, boolean bByForce);
    }
    public static GraphicDisplayInterfaceManager msGDIMgr = null;
    
    public static MultimediaManager msMultimediaMgr = null;
    
    public static PlatformHWManager msPlatformHWMgr = null;
    
    public static PatternManager mspm = null;    
    
    public static CommunicationManager msCommMgr = null;

    public static RtcMMediaManager msRtcMMediaManager = null;
    
    public static abstract class FileOperator    {
        public abstract boolean outputGraphFile(String strFileName, String strFileContent) throws IOException;
    }
    public static FileOperator msfileOperator = null;

    // strAbsNameWithCS is small letter function name with full CS path
    public static boolean isAExprDatumFunction(String strAbsNameWithCS, int nParamNum) {
        if (nParamNum != 1) {
            return false;
        } else if (!strAbsNameWithCS.equals("::mfp::statement::is_aexpr_datum")
                && !strAbsNameWithCS.equals("::mfp::statement::get_boolean_aexpr_true")
                && !strAbsNameWithCS.equals("::mfp::statement::get_boolean_aexpr_false")) {
            return false;
        } else {
            return true;
        }
    }
    
    public static JFCALCExpErrException rethrowParamException(JFCALCExpErrException e) throws JFCALCExpErrException {
        // only throw syntax exceptions
        switch (e.m_se.m_enumErrorType) {
            case ERROR_NO_EXPRESSION:
            case ERROR_INVALID_EXPRESSION_SYNTAX:
            case ERROR_MULTIPLE_EXPRESSIONS:
            case ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS:
            case ERROR_NUM_CAN_NOT_HAVE_TWO_DECIMAL_POINT:
            case ERROR_SCIENTIFIC_NOTATION_FORMAT_WRONG:
            case ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD:
            case ERROR_OPERATOR_NOT_EXIST:
            case ERROR_UNMATCHED_RIGHTPARENTHESE:
            case ERROR_UNMATCHED_LEFTPARENTHESE:
            case ERROR_INCORRECT_BINARY_OPERATOR:
            case ERROR_INCORRECT_MONADIC_OPERATOR:
            case ERROR_LACK_OPERAND:
            case ERROR_INVALID_CHARACTER:
            case ERROR_CANNOT_ASSIGN_VALUE_TO_ANYTHING_EXCEPT_VARIALBE_USE_EQUAL_INSTEAD:
            case ERROR_NOT_A_STRING:
            case ERROR_CANNOT_FIND_CLOSE_QUATATION_MARK_FOR_STRING:
            case ERROR_LACK_OF_INDEX:
            case ERROR_INVALID_NAME:
                throw e;
            default:
                return e;
        }
    }
    
    // strShrinkedRawName must be small case.
    public static DataClass evaluateFunction(ExprEvaluator exprEvaluator, String strFuncOwnerName, DataClass funcOwner, String strShrinkedRawName, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
    {
        LinkedList<DataClass> listParamValues = new LinkedList<DataClass>();  /* parameter stack */
        LinkedList<JFCALCExpErrException> listExceptions = new LinkedList<JFCALCExpErrException>(); /* exception list */
        for (String param : listParamRawInputs) {
            DataClass datumParam = null;
            JFCALCExpErrException eParam = null;
            try {
                datumParam = exprEvaluator.evaluateExpression(param, new DCHelper.CurPos());
                if (datumParam == null)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                }
            } catch (JFCALCExpErrException e) {
                eParam = rethrowParamException(e);
            }
            /* push the parameter and exception */
            listParamValues.add(datumParam);
            listExceptions.add(eParam);
        }
        List<String[]> lCitingSpaces = progContext.mstaticProgContext.getCitingSpaces();
        MemberFunction mf = null;
        if (funcOwner != null) {
            // this means it is a member function
            listParamRawInputs.add(strFuncOwnerName);
            listParamValues.add(funcOwner);
            listExceptions.add(null);
            // get funcOwner's class definition.
            DataClassClass funcOwnerObj = DCHelper.lightCvtOrRetDCClass(funcOwner);
            // search both public and private functions. get function directly from instance to avoid class definition search
            mf = funcOwnerObj.minstance.getMemberFunction(strFuncOwnerName, strShrinkedRawName, listParamRawInputs.size(), AccessRestriction.PRIVATE, progContext);
        } else {
            // it is not a member function
            mf = CitingSpaceDefinition.locateFunctionCall(strShrinkedRawName, listParamValues.size(), lCitingSpaces);
        }
        if (mf == null) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_UNDEFINED_FUNCTION);
        }
        return evaluateFunction(mf, strShrinkedRawName, listParamRawInputs, listParamValues, listExceptions, progContext);
    }
    
    public static DataClass evaluateFunction(MemberFunction mf, String strShrinkedRawName, LinkedList<String> listParamRawInputs, LinkedList<DataClass> listParamValues, LinkedList<JFCALCExpErrException> listParamExceptions, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
    {
        if (msfunctionInterrupter != null)    {
            // for debug or killing a background thread.
            if (msfunctionInterrupter.shouldInterrupt())    {
                msfunctionInterrupter.interrupt();
            }
        }

        String strAbsNameWithCS = mf.getAbsNameWithCS();
        
        // and/or functions are very special, we need not to evaluate all their parameters in some cases.
        if (mf.getAbsNameWithCS().equals("::mfp::math::logic::and")
                || mf.getAbsNameWithCS().equals("::mfp::math::logic::or")) {
            if (listParamValues.isEmpty()) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            for (int idx = listParamValues.size() - 1; idx >= 0; idx --) {
                DataClass datumParam = listParamValues.get(idx);
                if (datumParam == null) {
                    if (listParamExceptions.get(idx) == null) {
                        // this implies that a parameter is returned from a void function,
                        // which means the parameter is invalid.
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                    } else {
                        throw listParamExceptions.get(idx);
                    }
                } else {
                    MFPNumeric mfpValue = DCHelper.lightCvtOrRetDCMFPBool(listParamValues.get(idx)).getDataValue();
                    if ((mf.getAbsNameWithCS().equals("::mfp::math::logic::and") && !mfpValue.booleanValue())
                            || (mf.getAbsNameWithCS().equals("::mfp::math::logic::or") && mfpValue.booleanValue())){
                        return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, mfpValue);
                    }
                }
            }

            if (mf.getAbsNameWithCS().equals("::mfp::math::logic::and")) {
                return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
            } else {    // ::mfp::math::logic::or
                return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            }
        } else { // if not and/or, we have to throw exception if there is an exception.
            // if data value is null, the corresponding exception must not be null.
            // cannot happen that both data value and exception are null because data
            // value and exception are only processed in evaluateFunction, evaluateAExprQuick
            // and evaluateAEFunctionQuick.
            // evaluateFunction will throw Data not exist exception is it find a parameter
            // is null and evaluateAExprQuick ensures that parameter can never be null
            // because evaluateAExprQuick only calls evaluateAExprQuick which cannot return
            // null. evaluateAEFunctionQuick is a top level function and it always call 
            // evaluateAExprQuick so that it is fine.
            int firstExceptionPlace = listParamValues.indexOf(null);
            if (firstExceptionPlace != -1) {
                throw listParamExceptions.get(firstExceptionPlace);
            }
        }
        
        boolean bHasAExprData = false;
        for (int idx = 0; idx < listParamValues.size(); idx ++) {
            if (listParamValues.get(idx).getThisOrNull() instanceof DataClassAExpr)  {
                bHasAExprData = true;
                break;
            }
        }
        
        if (bHasAExprData && !isAExprDatumFunction(strAbsNameWithCS, listParamValues.size())) {    // this is evaluate aexpr.
            LinkedList<AbstractExpr> listFuncChildren = new LinkedList<AbstractExpr>();
            try {
                for (int idx = 0; idx < listParamValues.size(); idx ++) {
                    if (listParamValues.get(idx).getThisOrNull() instanceof DataClassAExpr)  {
                        // tsParameter.get(idx) must be a DataClassBuiltIn type.
                        listFuncChildren.addFirst(DCHelper.lightCvtOrRetDCAExpr(listParamValues.get(idx)).getAExpr()); // the order of the parameters is from last to first, so need addFirst instead of add.
                    } else {
                        DataClass datumChild = listParamValues.get(idx).copySelf();
                        listFuncChildren.addFirst(new AEConst(datumChild)); // the order of the parameters is from last to first, so need addFirst instead of add.
                    }
                }
                return new DataClassAExpr(new AEFunction(strAbsNameWithCS, listFuncChildren, progContext));
            } catch (JSmartMathErrException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
            }
        }
        
        DataClass datumReturnNum = new DataClassNull();
        if (mf instanceof BaseBuiltInFunction) {    // built in
            datumReturnNum = ((BaseBuiltInFunction)mf).callAction(listParamValues, listParamRawInputs, progContext);
        } else {    // predefined or user defined.
            datumReturnNum = evaluateMFPDefinedFunction((FunctionEntry) mf, strShrinkedRawName, listParamValues);
        }
        return datumReturnNum;
    }
    
    public static DataClass evaluateMFPDefinedFunction(FunctionEntry fe, String strShrinkedRawName, LinkedList<DataClass> listParamValues) throws JFCALCExpErrException {
        DataClass datumReturnNum = new DataClassNull();
        ScriptAnalyzer sa = new ScriptAnalyzer();
        //Statement sCurrent = fe.m_sLines[fe.m_nStartStatementPos];
        LinkedList<Variable> lParams = new LinkedList<Variable>();
        ListIterator<DataClass> itrDatum = listParamValues.listIterator(listParamValues.size());
        int nVariableIndex = 0;
        /*
         * note that the parameter values in tsParameter are pushed in (stack) while the parameter
         * variable names in fe.m_sf.m_strParams are appended in (queue).
         */
        DataClass datumValue;
        while(itrDatum.hasPrevious())    {
            if (!(fe.getStatementFunction().m_bIncludeOptParam) || nVariableIndex <= (fe.getStatementFunction().m_strParams.length - 2))    {
                // we do not use deep copy here because reference type parameter should be able to change inside a function
                datumValue = itrDatum.previous().copySelf();    // avoid change of parameter inside a function.
            } else    {
                // the optional parameters.
                DataClass[] dataList = new DataClass[itrDatum.previousIndex() + 1];
                int nOptVarIndex = 0;
                while(itrDatum.hasPrevious())    {
                    // avoid change of parameter (except reference type parameter) inside a function.
                    dataList[nOptVarIndex] = itrDatum.previous().copySelf();
                    nOptVarIndex ++;
                }
                datumValue = new DataClassArray(dataList);
            }
            Variable var = new Variable(fe.getStatementFunction().m_strParams[nVariableIndex], datumValue);
            lParams.addLast(var);
            nVariableIndex ++;
        }
        if (fe.getStatementFunction().m_bIncludeOptParam && nVariableIndex == fe.getStatementFunction().m_strParams.length - 1)    {
            // opt arg list is empty
            DataClass[] dataList = new DataClass[0];
            datumValue = new DataClassArray(dataList);
            Variable var = new Variable(fe.getStatementFunction().m_strParams[nVariableIndex], datumValue);
            lParams.addLast(var);
        }
        try {
            // sCurrent.analyze();  // no need to analyze again? to save time I comment it.
            // a function should not be able to read namespaces outside.
            ProgContext progContextNew = new ProgContext();
            progContextNew.mdynamicProgContext.mlVarNameSpaces = new LinkedList<LinkedList<Variable>>();
            progContextNew.mstaticProgContext.setCallingFunc(fe.getStatementFunction());
            InFunctionCSManager inFuncCSMgr = new InFunctionCSManager(progContextNew.mstaticProgContext);
            sa.analyzeBlock(fe.getStatementLines(), fe.getStartStatementPos(), lParams, inFuncCSMgr, progContextNew);
        } catch(FuncRetException e)    {
            datumReturnNum = e.m_datumReturn;
        } catch(ScriptStatementException e)    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                    strShrinkedRawName, e);
        } catch(JMFPCompErrException e)    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                    strShrinkedRawName, e);
        } catch(Exception e)    {
            // unexcepted exception
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                    strShrinkedRawName);   // unexcepted exception does not append to lower level
        }
        return datumReturnNum;
    }
}