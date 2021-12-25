package com.cyzapps.mfpanlib;

import android.content.Context;
import android.content.Intent;

import com.cyzapps.AdvRtc.RtcAppClient;
import com.cyzapps.GI2DAdapter.FlatGDIManager;
import com.cyzapps.GraphDaemon.ActivityChartDaemon;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jmfp.ErrorProcessor;
import com.cyzapps.Jmfp.FunctionEntry;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.ScriptAnalyzer;
import com.cyzapps.Jmfp.Statement;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.PatternManager;
import com.cyzapps.Multimedia.MultimediaManager;
import com.cyzapps.MultimediaAdapter.ImageMgrAndroid;
import com.cyzapps.MultimediaAdapter.SoundMgrAndroid;
import com.cyzapps.OSAdapter.AndroidRtcMMediaMan;
import com.cyzapps.OSAdapter.LangFileManager;
import com.cyzapps.OSAdapter.ParallelManager.MFP4AndroidCommMan;
import com.cyzapps.PlotAdapter.ChartOperator;
import com.cyzapps.adapter.AndroidStorageOptions;
import com.cyzapps.adapter.MFPAdapter;

import java.util.LinkedList;

public class MFPAndroidLib {
    public static final int RTC_STATE_CHANGE_INFO = 1;
    public static final int EMAIL_SEND_ERROR_INFO = 2;
    public static final int EMAIL_FETCH_STALE_INFO = 3;
    public static final int EMAIL_FETCH_ERROR_INFO = 4;
    public static final int EMAIL_SEND_RECV_STATE_INFO = 5;

    private static MFPAndroidLib singleObject;
    private void MFPAndroidLib() {}


    private static Context context;
    private static String settingsConfig;
    private static boolean readScriptsResFromAsset;
    public boolean initialize(Context c, String settingsCfg, boolean readCodesResFromAsset) {
        if (context == null) {
            context = c;
            settingsConfig = settingsCfg;
            readScriptsResFromAsset = readCodesResFromAsset;
            return true;
        }
        return false;
    }
    public boolean initialize(Context c, String settingsCfg) {
        if (context == null) {
            context = c;
            settingsConfig = settingsCfg;
            readScriptsResFromAsset = false;
            return true;
        }
        return false;
    }

    public static MFPAndroidLib getInstance() {
        if (singleObject == null) {
            singleObject = new MFPAndroidLib();
        }
        return singleObject;
    }


    public static Context getContext() {
        return context;
    }
    public static String getSettingsConfig() { return settingsConfig; }
    public static boolean getReadScriptsResFromAsset() { return readScriptsResFromAsset; }

    private static RtcAppClient rtcAppClient = null;    // cannot initialize it here because it needs App's context so it has to be initalized after App is initialized.

    public static RtcAppClient getRtcAppClient() {
        if (null == rtcAppClient) {
            rtcAppClient = new RtcAppClient(false);
        }
        return rtcAppClient;
    }


    public class CmdLineFunctionInterrupter extends FuncEvaluator.FunctionInterrupter {

        @Override
        public boolean shouldInterrupt() {
            return Thread.currentThread().isInterrupted();
        }

        @Override
        public void interrupt() throws InterruptedException {
            throw new InterruptedException();
        }

    }

    public class CmdLineScriptInterrupter extends ScriptAnalyzer.ScriptInterrupter {

        @Override
        public boolean shouldInterrupt() {
            return Thread.currentThread().isInterrupted();
        }

        @Override
        public void interrupt() throws InterruptedException {
            throw new InterruptedException();
        }

    }

    public class CmdLineAbstractExprInterrupter extends AbstractExpr.AbstractExprInterrupter {

        @Override
        public boolean shouldInterrupt() {
            return Thread.currentThread().isInterrupted();
        }

        @Override
        public void interrupt() throws InterruptedException {
            throw new InterruptedException();
        }

    }

    public class PlotGraphPlotter extends FuncEvaluator.GraphPlotter {
        public PlotGraphPlotter()	{}
        public PlotGraphPlotter(Context context)	{
            mcontext = context;
        }

        public boolean mbOK = false;
        public Context mcontext = null;

        @Override
        public boolean plotGraph(String strGraphInfo) {
            Intent intent = new Intent(mcontext, ActivityChartDaemon.class);
            intent.putExtra(ChartOperator.VMFPChart, strGraphInfo);
            mcontext.startActivity(intent);
            mbOK = true;
            return true;
        }

    }

    public void initializeMFPInterpreterEnv(Context activity, FuncEvaluator.ConsoleInputStream input, FuncEvaluator.LogOutputStream output) {
        // initialize MFP interpreter environment
        FuncEvaluator.msstreamLogOutput = output;
        FuncEvaluator.msstreamConsoleInput = input;
        FuncEvaluator.msfunctionInterrupter = new CmdLineFunctionInterrupter();
        FuncEvaluator.msfileOperator = null;	// this means do not save generated MFP graph files.
        FuncEvaluator.msgraphPlotter = new PlotGraphPlotter(activity);
        FuncEvaluator.msgraphPlotter3D = new PlotGraphPlotter(activity);
        FuncEvaluator.msGDIMgr = new FlatGDIManager(activity);
        FuncEvaluator.msMultimediaMgr = new MultimediaManager(
                new ImageMgrAndroid(activity),
                new SoundMgrAndroid(activity)
        );
					/*FuncEvaluator.msPlatformHWMgr = new PlatformHWManager(
							new MFP4AndroidFileMan(getAssets())
					);*/  // msPlatformHWMgr is loaded early to analyze anotation at loading stage.
        if (FuncEvaluator.mspm == null) {
            FuncEvaluator.mspm = new PatternManager();
            try {
                FuncEvaluator.mspm.loadPatterns(2);	// load pattern is a very time consuming work. So only do this if needed.
            } catch (Exception e) {
                // load all integration patterns. Assume load patterns will not throw any exceptions.
            }
        }
        if (FuncEvaluator.msCommMgr == null) {
            FuncEvaluator.msCommMgr = new MFP4AndroidCommMan();
        }
        if (FuncEvaluator.msRtcMMediaManager == null) {
            FuncEvaluator.msRtcMMediaManager = new AndroidRtcMMediaMan(activity);
        }
        ScriptAnalyzer.msscriptInterrupter = new CmdLineScriptInterrupter();
        AbstractExpr.msaexprInterrupter = new CmdLineAbstractExprInterrupter();
    }

    public static String processMFPStatement(String strCmd, LinkedList<VariableOperator.Variable> lCmdLineLocalVars, VariableOperator.Variable varAns) throws InterruptedException	{
        if (strCmd.trim().equals(""))	{
            // empty cmd
            return "";
        } else	{
            String strReturn, strOutput = "";
            Statement sCmd = new Statement(strCmd, "", 1);  // because it is in command line, line number is 1.
            try {
                sCmd.analyze();
                if (sCmd.mstatementType.getType().equals("variable"))  {
                    strReturn = "";
                    strOutput = "";	// do not support variable.
                } else  {   // normal expression
                    // clear variable name spaces
                    LinkedList<LinkedList<VariableOperator.Variable>> lVarNameSpaces = new LinkedList<LinkedList<VariableOperator.Variable>>();
                    lVarNameSpaces.add(lCmdLineLocalVars);
                    ProgContext progContext = new ProgContext();
                    progContext.mstaticProgContext.setCitingSpacesExplicitly(MFPAdapter.getAllCitingSpaces(null));
                    progContext.mdynamicProgContext.mlVarNameSpaces = lVarNameSpaces;
                    ExprEvaluator exprEvaluator = new ExprEvaluator(progContext);

                    /* evaluate the expression */
                    DCHelper.CurPos curpos = new DCHelper.CurPos();
                    curpos.m_nPos = 0;
                    DataClass datumAnswer = null;
                    String strarrayAnswer[] = new String[2];
                    datumAnswer = exprEvaluator.evaluateExpression(strCmd, curpos);
                    if (datumAnswer != null)	{
                        strarrayAnswer = MFPAdapter.outputDatum(datumAnswer);
                        strReturn = strarrayAnswer[0];
                        varAns.setValue(datumAnswer);  // assign answer to "ans" variable.
                    } else	{
                        strReturn = "returns nothing";
                    }
                /*strOutput = strCmd
                        + ((datumAnswer == null)?(" " + getString(R.string.return_nothing_answer_shown)):("\n= " + strarrayAnswer[1]))
                        + "\n";*/
                    strOutput = ((datumAnswer == null)?(""):(strCmd + "\n= " + strarrayAnswer[1]))
                            + "\n";
                }
            } catch(ErrorProcessor.JMFPCompErrException e) {
                strReturn = "Error";
                strOutput = MFPAdapter.outputException(e);
            } catch (ErrProcessor.JFCALCExpErrException e) {
                strReturn = "Error";
                strOutput = MFPAdapter.outputException(e);
            }
            return strOutput;
        }
    }

    public static String processMFPSession(String[] strlistInputSession, LinkedList<VariableOperator.Variable> lCmdLineLocalVars, VariableOperator.Variable varAns) throws InterruptedException {
        // first step: scan strlistInputSession to find out if it is a session or it is a lib.
        String firstStatement = "", strReturn = "", strOutput = "";
        try {
            Boolean isInHelp = false;
            for (int idx = 0; idx < strlistInputSession.length; idx ++) {
                if (strlistInputSession[idx].matches("\\s*") || strlistInputSession[idx].matches("\\s*\\/\\/.*")) {
                    continue;   // if it is an empty line or a line starting with //
                } else if (strlistInputSession[idx].matches("\\s*(?i)help(?-i)\\s+.*")) {
                    isInHelp = true;
                } else if (strlistInputSession[idx].matches("\\s*(?i)endh(?-i)\\s+.*")) {
                    isInHelp = false;
                } else if (!isInHelp) {
                    if (strlistInputSession[idx].matches("\\s*(?i)public(?-i)\\s+.*")
                            || strlistInputSession[idx].matches("\\s*(?i)private(?-i)\\s+.*")) {
                        ErrorProcessor.ERRORTYPES e = ErrorProcessor.ERRORTYPES.ACCESS_KEYWORD_CANNOT_BE_HERE;   // public and private keywords can only be applied to function and variable
                        throw new ErrorProcessor.JMFPCompErrException("", idx, idx, e);
                    } else if (strlistInputSession[idx].matches("\\s*(?i)citingspace(?-i)\\s+.*")) {
                        firstStatement = "citingspace";
                        break;
                    } else if (strlistInputSession[idx].matches("\\s*(?i)class(?-i)\\s+.*")) {
                        firstStatement = "class";
                        break;
                    } else if (strlistInputSession[idx].matches("\\s*(?i)function(?-i)\\s+.*")) {
                        firstStatement = "function";
                        break;
                    } else {
                        firstStatement = "";
                        break;
                    }
                }
            }
            if (!firstStatement.equals("")) { // this is a not session.
                MFPAdapter.loadLibCodeString(strlistInputSession, "", new String[] {"\u0000" + LangFileManager.STRING_COMMAND_INPUT_FILE_PATH});
            } else {    // this is a session.
                try {
                    FunctionEntry fe = MFPAdapter.loadSession(strlistInputSession);
                    fe.getStatementFunction().m_lCitingSpaces.addAll(MFPAdapter.getAllCitingSpaces(null));
                    for (Statement s : fe.getStatementLines()) {
                        s.analyze2(fe);     // analyse statement to use aexpr to replace string.
                    }
                    // Different from a function, an input session should not be able to read namespaces outside.
                    LinkedList<LinkedList<VariableOperator.Variable>> lVarNameSpaces = new LinkedList<LinkedList<VariableOperator.Variable>>();
                    lVarNameSpaces.add(lCmdLineLocalVars);
                    ProgContext progContext = new ProgContext();
                    progContext.mstaticProgContext.setCallingFunc(fe.getStatementFunction());
                    progContext.mdynamicProgContext.mlVarNameSpaces = lVarNameSpaces;
                    ScriptAnalyzer.InFunctionCSManager inFuncCSMgr = new ScriptAnalyzer.InFunctionCSManager(progContext.mstaticProgContext);
                    ScriptAnalyzer sa = new ScriptAnalyzer();
                    sa.analyzeBlock(fe.getStatementLines(), fe.getStartStatementPos(), new LinkedList<VariableOperator.Variable>(), inFuncCSMgr, progContext);
                } catch (ScriptAnalyzer.FuncRetException e) {
                    String strarrayAnswer[] = new String[2];
                    if (e.m_datumReturn != null) {
                        try {
                            strarrayAnswer = MFPAdapter.outputDatum(e.m_datumReturn);
                            strReturn = strarrayAnswer[0];
                            strOutput = "----> " + strarrayAnswer[1] + "\n";
                            varAns.setValue(e.m_datumReturn);  // assign answer to "ans" variable.
                        } catch (ErrProcessor.JFCALCExpErrException ex) {
                            strReturn = "Error";
                            strOutput = MFPAdapter.outputException(ex);
                        }
                    } else {
                        strReturn = "returns nothing";
                        strOutput = "";
                    }
                } catch (ScriptAnalyzer.ScriptStatementException e) {
                    strReturn = "Error";
                    strOutput = MFPAdapter.outputException(e);
                }
            }
        } catch (ErrorProcessor.JMFPCompErrException e) {
            strReturn = "Error";
            strOutput = MFPAdapter.outputException(e);
        } catch (Exception e) {
            // unexcepted exception
            strReturn = "Error";
            strOutput = MFPAdapter.outputException(e);
        }

        strOutput += "\n";
        return strOutput;
    }
}
