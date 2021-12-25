package com.cyzapps.AnMFPApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.cyzapps.GI2DAdapter.FlatGDIManager;
import com.cyzapps.GraphDaemon.ActivityChartDaemon;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.ElemAnalyzer;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jfcalc.FuncEvaluator.ConsoleInputStream;
import com.cyzapps.Jfcalc.FuncEvaluator.FileOperator;
import com.cyzapps.Jfcalc.FuncEvaluator.FunctionInterrupter;
import com.cyzapps.Jfcalc.FuncEvaluator.GraphPlotter;
import com.cyzapps.Jfcalc.FuncEvaluator.LogOutputStream;
import com.cyzapps.Jfcalc.IOLib;
import com.cyzapps.Jmfp.ErrorProcessor;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.FunctionEntry;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.ScriptAnalyzer;
import com.cyzapps.Jmfp.ScriptAnalyzer.ScriptInterrupter;
import com.cyzapps.Jmfp.Statement;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.AbstractExpr.AbstractExprInterrupter;
import com.cyzapps.Jsma.PatternManager;
import com.cyzapps.Multimedia.MultimediaManager;
import com.cyzapps.MultimediaAdapter.ImageMgrAndroid;
import com.cyzapps.MultimediaAdapter.SoundMgrAndroid;
import com.cyzapps.OSAdapter.AndroidRtcMMediaMan;
import com.cyzapps.OSAdapter.LangFileManager;
import com.cyzapps.OSAdapter.MFP4AndroidFileMan;
import com.cyzapps.OSAdapter.ParallelManager.MFP4AndroidCommMan;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.PlotAdapter.ChartOperator;
import com.cyzapps.VisualMFP.Color;
import com.cyzapps.adapter.AndroidStorageOptions;
import com.cyzapps.adapter.MFPAdapter;
import com.cyzapps.MFPLibTester.R;
import com.cyzapps.mfpanlib.MFPAndroidLib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class ActivityAnMFPMain extends Activity {
	protected static final int ITEM0 = 1;
	protected static final int ITEM1 = 2;
	protected static final int ITEM2 = 3;
	protected static final int ITEM3 = 4;
	protected static final int ITEM4 = 5;
	protected static final int ITEM5 = 6;
	protected static final int ITEM6 = 7;
	protected static final int ITEM7 = 8;
	protected static final int ITEM8 = 9;
	protected static final int ITEM9 = 10;
		
	public EditText medtCmdLineEdtBox = null;
    											// 1 means use but hide softkeyboard, 2 means use and show soft keyboard.
	protected int mnEditableTextStart = 0;
	// Need handler for callbacks to the UI thread
	protected Handler mHandler = new Handler();
    // Create runnable for posting
	protected Runnable mUpdateResults = new Runnable() {
        public void run() {
       		outputString2Screen(null);
        }
    };
    
    protected final ReentrantReadWriteLock mreadWriteLock = new ReentrantReadWriteLock();
	
	// just for convenience, should be replaced by settings.
    protected int mnCmdLineBufSize = 8388608;	// characters.
    protected int mnNumOfRemovedChars = 0;	// the number of removed chars because of buffer overflow.
	// just for convenience, should be replaced by settings.
	
    protected Variable mvarAns = new Variable("ans", new DataClassNull());
    protected LinkedList<Variable> mlCmdLineLocalVars = new LinkedList<Variable>();
    protected Thread mthreadCmd = null;

    protected Object mobjInputSync = new Object();
    protected LinkedList<String> mlistOutputStrings = new LinkedList<String>();
    protected InputFilter mfilter = null;

    protected View mvAds = null;
    
    protected boolean mbDblBack2ExitPressedOnce = false;
	
	protected ProgressDialog mdlgInitProgress = null;

	public class CmdLineConsoleInput extends ConsoleInputStream	{
		@Override
    	public void doBeforeInput() {

        }

        @Override
    	public String inputString() throws InterruptedException	{
     		if (Thread.currentThread().isInterrupted())	{
    			// make sure that there is no log output after exception.
    			throw new InterruptedException();
    		}
    		
    		final Object objInsureInputAfterOutput = new Object();
    		synchronized(objInsureInputAfterOutput)	{
	   			mHandler.post(new Runnable()	{
	
					@Override
					public void run() {
						synchronized(objInsureInputAfterOutput)	{
							objInsureInputAfterOutput.notify(); // because in synchronized block, notify should be always after wait.
						}
					}
	   				
	   			});
	   			objInsureInputAfterOutput.wait();
    		}

   			if (Thread.currentThread().isInterrupted())	{
    			// make sure that there is no log output after exception.
    			throw new InterruptedException();
    		}
     		if (medtCmdLineEdtBox != null)	{
    			while(true)	{
		    		synchronized(mobjInputSync)	{
		    			mobjInputSync.wait();
		    		}
	    			String strAllTxt = medtCmdLineEdtBox.getEditableText().toString();
	    			int nEditableTextStart = mnEditableTextStart;
	    			String strInput = "";
	    			if (nEditableTextStart <= strAllTxt.length())	{
	    				strInput = strAllTxt.substring(nEditableTextStart);
	    			}
	    			int nReturnPosition = strInput.indexOf("\n");
	    			if (nReturnPosition != -1)	{
	    				mnEditableTextStart = strAllTxt.length();	//finish this input.
	                    String strReturn = strInput.substring(0, nReturnPosition);
	                    return strReturn;
	    			}
    			}
    		}
    		return null;	// cannot reach here.
    	}

    	@Override
    	public void doAfterInput()  {
            
        }
    }
    
    public class CmdLineLogOutput extends LogOutputStream	{
		@Override
    	public void outputString(String str) throws InterruptedException	{
    		if (Thread.currentThread().isInterrupted())	{
    			// make sure that there is no log output after exception.
    			throw new InterruptedException();
    		}
    		mreadWriteLock.writeLock().lockInterruptibly();
    		mlistOutputStrings.addLast(str);
    		mreadWriteLock.writeLock().unlock();
   			mHandler.post(mUpdateResults);
    	}
    }

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		asyncInit();	// asyncronously initialize.
		
        setTitle(getString(R.string.app_name).trim() + ": " + getString(R.string.cmd_line_title));
		int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		int nScreenOrientation = getResources().getConfiguration().orientation;
		// make sure command line always turns on the window so that cpu will not stop.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.cmd_line_gui);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
        medtCmdLineEdtBox = (EditText)findViewById(R.id.command_line_edit_box);
        medtCmdLineEdtBox.setSelection(medtCmdLineEdtBox.length());
        if (mfilter == null)	{
	        mfilter = new InputFilter() { 
	 			@Override
				public CharSequence filter(CharSequence source, int start, int end,
						Spanned dest, int dstart, int dend) {
					int nB4EditableStart = (dstart < mnEditableTextStart)?dstart:-1;
					int nB4EditableEnd = (dend <= mnEditableTextStart)?dend:mnEditableTextStart;
					String strInput = source.toString().substring(start, end);
					String strReturn = "";
					if (nB4EditableStart == -1)	{
						// all the destination text is editable.
						strReturn = strInput;
					} else if (nB4EditableEnd == dend)	{
						// all of the destination text is not editable
						strReturn = dest.toString().substring(dstart, dend);
					} else	{
						// part of the destination text is editable
						strReturn =  dest.toString().substring(nB4EditableStart, mnEditableTextStart)
									+ strInput;
					}
					return strReturn;
				} 
	        };
        }
        mnEditableTextStart = medtCmdLineEdtBox.length();
        setEdtInputFilter(medtCmdLineEdtBox.getEditableText());
        medtCmdLineEdtBox.addTextChangedListener(new TextWatcher()	{

        	
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > mnCmdLineBufSize)	{
					// have to remove filter twice because in s.delete afterTextChanged is called again.
					removeEdtInputFilter(s);
					// remove very old text from screen.
					int nNumofChars2BeReomoved = s.length() - mnCmdLineBufSize;
					mnEditableTextStart -= nNumofChars2BeReomoved;
					if (mnEditableTextStart < 0)	{
						mnEditableTextStart = 0;
					}
					s.delete(0, nNumofChars2BeReomoved);
					mnNumOfRemovedChars += nNumofChars2BeReomoved;
				}
				// have to remove filter twice because in s.delete afterTextChanged is called again.
				removeEdtInputFilter(s);
				setEdtInputFilter(s);
	    		synchronized(mobjInputSync)	{
	    			mobjInputSync.notifyAll();
	    		}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
        	
        });
        
        mlCmdLineLocalVars.addLast(mvarAns);
    }
    
	@Override
	public void onResume()	{
		super.onResume();
	}

    public void setEdtInputFilter(Editable s)	{
        InputFilter[] inputFilters = s.getFilters();
        int nOldInputFilterLen = 0;
        if (inputFilters != null)	{
        	nOldInputFilterLen = inputFilters.length;
	        for (int nIndex = 0; nIndex < inputFilters.length; nIndex ++)	{
	        	if (inputFilters[nIndex] == null)	{
	        		nOldInputFilterLen --;
	        	}
	        }
        }
        
        InputFilter[] inputFiltersNew = new InputFilter[nOldInputFilterLen + 1];
        inputFiltersNew[nOldInputFilterLen] = mfilter;
        if (inputFilters != null)	{
        	for (int nIndex = 0, nIndexNew = 0; nIndex < inputFilters.length; nIndex ++)	{
        		if (inputFilters[nIndex] != null)	{
        			inputFiltersNew[nIndexNew] = inputFilters[nIndex];
        			nIndexNew ++;
        		}
        	}
        }
        s.setFilters(inputFiltersNew);
    }

    public void removeEdtInputFilter(Editable s)	{
        InputFilter[] inputFilters = s.getFilters();
        if (inputFilters == null)	{
        	inputFilters = new InputFilter[0];
        }
        int nNewLen = 0;
        for (int nIndex = 0; nIndex < inputFilters.length; nIndex ++)	{
        	if (inputFilters[nIndex] == mfilter)	{
        		inputFilters[nIndex] = null;
        	}
        	if (inputFilters[nIndex] != null)	{
        		nNewLen ++;
        	}
        }
        InputFilter[] inputFiltersNew = new InputFilter[nNewLen];
        for (int nIndex = 0, nIndexNew = 0; nIndex < inputFilters.length; nIndex ++)	{
        	if (inputFilters[nIndex] != null)	{
        		inputFiltersNew[nIndexNew] = inputFilters[nIndex];
        		nIndexNew ++;
        	}
        }
        s.setFilters(inputFiltersNew);    	
    }
    
	public boolean startCalc(final String str2Run)	{
		medtCmdLineEdtBox.append("\n");	// mstr2BProcessedInput may change during this command.
		if (str2Run != null)	{
			mthreadCmd = new Thread(new Runnable() {
		        public void run() { 
					mnEditableTextStart += medtCmdLineEdtBox.getText().length();
		        	try	{
		        		String[] statements = str2Run.trim().split("\\\n");
		        		String strOutput;
		        		if (statements.length == 1) {
		        			if (str2Run.matches("(?i)\\s*help\\s+.*") || str2Run.matches("(?i)\\s*help\\s*")) {	// this is help command
		        				strOutput = quickhelp(str2Run);
							} else {	// run single line
								strOutput = MFPAndroidLib.processMFPStatement(str2Run.trim(), new LinkedList<Variable>(), mvarAns);
							}
						} else { // run multiple lines
							strOutput = MFPAndroidLib.processMFPSession(statements, new LinkedList<Variable>(), mvarAns);
						}
						// don't add this line if developer doesn't want to show the final output in app
						new CmdLineLogOutput().outputString(strOutput);
		        	} catch(InterruptedException e)	{
		    			Log.e("multithread", "Thread receive exception : " + e.toString());
		        	}
					mHandler.post(new Runnable()	{

						@Override
						public void run() {
							/* use message to tell the main thread that I finish.
							 * Do not set mthreadCmd directly coz there might be some
							 * delayed output message which changes GUI after directly
							 * set mthreadCmd to null.
							 */
							mthreadCmd = null;
							outputString2Screen(getString(R.string.command_finished));
						}
						
					});
		        }
			});
			mthreadCmd.start();
			return true;	// means thread started.
		}
		return false;	// means thread cannot be started.
    }

	public String quickhelp(String strExpression) {
		String strOutput = "";
		strExpression = strExpression.trim();
		if ((strExpression.length() >= 5 && strExpression.substring(0, 5).toLowerCase(Locale.US).equals("help ") == false)
				|| (strExpression.length() == 4 && strExpression.substring(0, 4).toLowerCase(Locale.US).equals("help") == false)
				|| (strExpression.length() < 4)) {
			strOutput = "Error in analyzing help requirement!\n";
		} else if (strExpression.toLowerCase(Locale.US).trim().equals("help")) {
			strOutput = getString(R.string.cmd_line_welcome_message) + "\n";
		} else {
			// the length of the string must be larger than 4 now.
			String strHelpReq = strExpression.substring(4).trim().toLowerCase(Locale.US);

			if (strHelpReq.equals("")) {
				strOutput = getString(R.string.cmd_line_welcome_message);
			} else if (strHelpReq.equals("=")) {
				strOutput = strHelpReq + " : " + getString(R.string.assign_help_info);
			} else if (strHelpReq.equals("==")) {
				strOutput = strHelpReq + " : " + getString(R.string.equal_help_info);
			} else if (strHelpReq.equals("(")) {
				strOutput = strHelpReq + " : " + getString(R.string.parenthesis_help_info);
			} else if (strHelpReq.equals(")")) {
				strOutput = strHelpReq + " : " + getString(R.string.closeparenthesis_help_info);
			} else if (strHelpReq.equals("[")) {
				strOutput = strHelpReq + " : " + getString(R.string.squarebracket_help_info);
			} else if (strHelpReq.equals("]")) {
				strOutput = strHelpReq + " : " + getString(R.string.closesquarebracket_help_info);
			} else if (strHelpReq.equals(",")) {
				strOutput = strHelpReq + " : " + getString(R.string.comma_help_info);
			} else if (strHelpReq.equals("+")) {
				strOutput = strHelpReq + " : " + getString(R.string.plus_help_info);
			} else if (strHelpReq.equals("-")) {
				strOutput = strHelpReq + " : " + getString(R.string.minus_help_info);
			} else if (strHelpReq.equals("*")) {
				strOutput = strHelpReq + " : " + getString(R.string.multiplication_help_info);
			} else if (strHelpReq.equals("/")) {
				strOutput = strHelpReq + " : " + getString(R.string.division_help_info);
			} else if (strHelpReq.equals("\\")) {
				strOutput = strHelpReq + " : " + getString(R.string.leftdivision_help_info);
			} else if (strHelpReq.equals("**")) {
				strOutput = strHelpReq + " : " + getString(R.string.power_help_info);
			} else if (strHelpReq.equals("'")) {
				strOutput = strHelpReq + " : " + getString(R.string.transpose_help_info);
			} else if (strHelpReq.equals("\"")) {
				strOutput = strHelpReq + " : " + getString(R.string.doublequote_help_info);
			} else if (strHelpReq.equals("!")) {
				strOutput = strHelpReq + " : " + getString(R.string.exclaimation_help_info);
			} else if (strHelpReq.equals("%")) {
				strOutput = strHelpReq + " : " + getString(R.string.percentage_help_info);
			} else if (strHelpReq.equals("&")) {
				strOutput = strHelpReq + " : " + getString(R.string.bit_and_help_info);
			} else if (strHelpReq.equals("|")) {
				strOutput = strHelpReq + " : " + getString(R.string.bit_or_help_info);
			} else if (strHelpReq.equals("^")) {
				strOutput = strHelpReq + " : " + getString(R.string.bit_xor_help_info);
			} else if (strHelpReq.equals("~")) {
				strOutput = strHelpReq + " : " + getString(R.string.bit_not_help_info);
			} else if (strHelpReq.equals("i")) {
				strOutput = strHelpReq + " : " + getString(R.string.image_i_help_info);
			} else if (strHelpReq.equals("pi")) {
				strOutput = strHelpReq + " : " + getString(R.string.pi_constant_help_info);
			} else if (strHelpReq.equals("e")) {
				strOutput = strHelpReq + " : " + getString(R.string.e_constant_help_info);
			} else if (strHelpReq.equals("null")) {
				strOutput = strHelpReq + " : " + getString(R.string.null_constant_help_info);
			} else if (strHelpReq.equals("true")) {
				strOutput = strHelpReq + " : " + getString(R.string.true_constant_help_info);
			} else if (strHelpReq.equals("false")) {
				strOutput = strHelpReq + " : " + getString(R.string.false_constant_help_info);
			} else if (strHelpReq.equals("inf")) {
				strOutput = strHelpReq + " : " + getString(R.string.inf_constant_help_info);
			} else if (strHelpReq.equals("infi")) {
				strOutput = strHelpReq + " : " + getString(R.string.infi_constant_help_info);
			} else if (strHelpReq.equals("nan")) {
				strOutput = strHelpReq + " : " + getString(R.string.nan_constant_help_info);
			} else if (strHelpReq.equals("nani")) {
				strOutput = strHelpReq + " : " + getString(R.string.nani_constant_help_info);
			} else if ((strOutput = MFPAdapter.getMFPKeyWordHelp(strHelpReq, AppAnMFP.getLocalLanguage())) != null) {
				// is key word.
			} else {
				strOutput = "";
				String[] strHelpReqParts = strHelpReq.split("\\(");
				if (strHelpReqParts.length == 2) {
					boolean bRightFormat = true;
					String strFuncName = strHelpReqParts[0].trim();
					String strNumofParams = "";
					int nNumofParams = 0;
					boolean bIncludeOptionParam = false;
					if (strHelpReqParts[1].trim().matches("[0-9]*\\s*\\.\\.\\.\\s*\\)")) {
						strNumofParams = strHelpReqParts[1].split("\\.\\.\\.")[0].trim();
						bIncludeOptionParam = true;
					} else if (strHelpReqParts[1].trim().matches("[0-9]*\\s*\\)")) {
						String[] strParts = strHelpReqParts[1].split("\\)");
						if (strParts.length > 0) {   // if it is ")", then strParts.length = 0
							strNumofParams = strParts[0].trim();
						} else {
							strNumofParams = "";
						}
					} else {
						bRightFormat = false;
					}
					if (strNumofParams.trim().equals("")) {
						nNumofParams = 0;
					} else {
						try {
							nNumofParams = Integer.parseInt(strNumofParams.trim());
						} catch (NumberFormatException e) {
							bRightFormat = false;
						}
					}
					if (bRightFormat) {
						try {
							// make it shrinked name
							strFuncName = ElemAnalyzer.getShrinkedFuncNameStr(strFuncName, new DCHelper.CurPos(), "");
						} catch (JFCALCExpErrException ex) {
							Log.e("ActivityAMCmdLine", "Invalid function name : " + strFuncName);
						}
						strOutput = MFPAdapter.getFunctionHelp(strFuncName,
								nNumofParams,
								bIncludeOptionParam,
								AppAnMFP.getLocalLanguage(),
								MFPAdapter.getAllCitingSpaces(null));
					}
					if (strOutput == null || strOutput.trim().equals("")) {
						strOutput = getString(R.string.no_quick_help_info) + " " + strHelpReq;
					}
				} else if (strHelpReqParts.length == 1) {
					try {
						// make it shrinked name
						strHelpReq = ElemAnalyzer.getShrinkedFuncNameStr(strHelpReq, new DCHelper.CurPos(), "");
					} catch (JFCALCExpErrException ex) {
						Log.e("ActivityAMCmdLine", "Invalid help request : " + strHelpReq);
					}
					strOutput = MFPAdapter.getFunctionHelp(strHelpReq, AppAnMFP.getLocalLanguage(), MFPAdapter.getAllCitingSpaces(null));
					if (strOutput.equals("")) {
						strOutput = getString(R.string.no_quick_help_info) + " " + strHelpReq;
					}
				}
			}
		}
		return strOutput;
	}

	private void outputString2Screen(String str)	{
		// always output to the tail.
		if (medtCmdLineEdtBox != null)	{
			if (str == null)	{
                LinkedList<String> listNewOutputStrings = new LinkedList<String>();
                try {
					mreadWriteLock.writeLock().lockInterruptibly();
	                if (mlistOutputStrings.size() > 0) {
	                    listNewOutputStrings.addAll(mlistOutputStrings);
	                    mlistOutputStrings.clear();
	                }
	                mreadWriteLock.writeLock().unlock();
	                String strNewOutput = "";
	                boolean bUpdated = false;
	                while (listNewOutputStrings.size() > 0)    {
	                    strNewOutput += listNewOutputStrings.poll();
	                    bUpdated = true;
	                }
					if(bUpdated)	{
						removeEdtInputFilter(medtCmdLineEdtBox.getEditableText());
			    		medtCmdLineEdtBox.setText(medtCmdLineEdtBox.getText().toString() + strNewOutput);
			    		mnEditableTextStart = medtCmdLineEdtBox.length();
			    		medtCmdLineEdtBox.setSelection(mnEditableTextStart);
					}
				} catch (InterruptedException e) {
					// do not update edit box if it is interrupted.
				}
			} else	{
	    		removeEdtInputFilter(medtCmdLineEdtBox.getEditableText());
	    		medtCmdLineEdtBox.setText(medtCmdLineEdtBox.getText().toString() + str);
	    		mnEditableTextStart = medtCmdLineEdtBox.length();
	    		medtCmdLineEdtBox.setSelection(mnEditableTextStart);
			}
		}
	}
		
	public boolean isIdle()	{
		return (mthreadCmd == null);
	}
	
	public boolean isWaiting()	{
		return mthreadCmd != null && mthreadCmd.getState() == Thread.State.WAITING;
	}

    //Dynamically create context Menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	// use generate menu first time here because since Android 11 there is a bug, onPrepareOptionsMenu
    	// not called when press menu button because there is a menu buffered after onCreateOptionsMenu.
		menu.clear();
		menu.add(0, ITEM0, 0, getString(R.string.menu_run_again));
		menu.add(0, ITEM1, 0, getString(R.string.menu_help));
		return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	   	Bundle bundle = new Bundle();
		switch (item.getItemId()) {
		case ITEM0:
			interruptCmd();	// first interrupt running cmd.
			String str2Run = AppAnMFP.STRING_COMMANDS_TO_RUN;
			medtCmdLineEdtBox.setText(getString(R.string.command_prompt) + str2Run);
			mnEditableTextStart = medtCmdLineEdtBox.getText().length();
			medtCmdLineEdtBox.setSelection(mnEditableTextStart);
			startCalc(str2Run);
			break;
		case ITEM1:
		   	Intent intentHelp = new Intent(this, ActivityShowHelp.class);
		   	bundle.putString("HELP_CONTENT", "cmdline");
		   	//Add this bundle to the intent
		   	intentHelp.putExtras(bundle);
		   	startActivity(intentHelp);
		   	break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onPause()	{
		if (mdlgInitProgress != null) {
			mdlgInitProgress.dismiss();
			mdlgInitProgress = null;
		}
    	super.onPause();
	}
	
	/** Called when the activity is finally destroyed. */
    @Override
    public void onDestroy()	{
    	if (isFinishing())	{
			interruptCmd();
    	}
    	// stop signal service here to prevent the service competing with other webrtc apps' services
		MFPAndroidLib.getRtcAppClient().stopSignalService();
    	super.onDestroy();
    }
    
    private void interruptCmd()	{
        // this function terminate a running cmd.
        if (!isIdle())	{
            if (mthreadCmd.isAlive())	{
                mthreadCmd.interrupt();
            }
            mthreadCmd = null;
        }
    }
    
    /* we don't want the command line show the text since last session so do not call super.onRestoreInstanceState(bundle);.
     * (non-Javadoc)
     * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
     */
	@Override
	public void onRestoreInstanceState(Bundle bundle) {
		// super.onRestoreInstanceState(bundle); do not restore the text in the command line box.
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		// super.onSaveInstanceState(bundle);
	}

	protected void asyncInit() {
		mdlgInitProgress = ProgressDialog.show(this, getString(R.string.please_wait),
				getString(R.string.loading_config_file), true);
		final Handler handler = new Handler();

		Thread threadInit = new Thread(new Runnable()	{
		
			@Override
			public void run() {
				AssetManager am = getAssets();
				// Now start to load functions
				MFP4AndroidFileMan mfp4AnFileMan = new MFP4AndroidFileMan(am);
				// if we repeatedly run this function, we have to call the following statement to ensure
				// MFP citingspace is clear. However, if we only run this function once, MFP citingspace
				// is clear anyway so the following line can be removed.
				// MFPAdapter.clear(CitingSpaceDefinition.CheckMFPSLibMode.CHECK_USER_DEFINED_ONLY);
				handler.post(new Runnable()	{
					@Override
					public void run() {
						if (mdlgInitProgress != null) {
							mdlgInitProgress.setMessage(getString(R.string.loading_user_defined_libs));
						}
					}
				});

				if (mfp4AnFileMan.isMFPApp()) {
					MFP4AndroidFileMan.loadZippedUsrDefLib(MFP4AndroidFileMan.STRING_ASSET_USER_SCRIPT_LIB_ZIP, mfp4AnFileMan);    // load user defined lib.
				} else {
					// use the following line if you put your MFP scripts in local storage
					MFP4AndroidFileMan.reloadAllUsrLibs(ActivityAnMFPMain.this, -1, null);
				}
				// ok, now the libs are loaded.
				// set the initial working directory in local storage. Not essential.
				// If not called, initial working directory will be app's default storage directory
				IOLib.msstrWorkingDir = mfp4AnFileMan.getAppBaseFullPath();

				// now initialize MFP interpreter environment
				MFPAndroidLib.getInstance().initializeMFPInterpreterEnv(ActivityAnMFPMain.this, new CmdLineConsoleInput(), new CmdLineLogOutput());

				handler.post(new Runnable()	{
					@Override
					public void run() {
						if (mdlgInitProgress != null) {
							mdlgInitProgress.dismiss();
							mdlgInitProgress = null;
						}
						String str2Run = AppAnMFP.STRING_COMMANDS_TO_RUN;
						medtCmdLineEdtBox.setText(getString(R.string.command_prompt) + str2Run);
						mnEditableTextStart = medtCmdLineEdtBox.getText().length();
						medtCmdLineEdtBox.setSelection(mnEditableTextStart);
						startCalc(str2Run);
					}
				});
			}
		});
		threadInit.start();
	}

	@Override
    public void onBackPressed() {
        if (mbDblBack2ExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        mbDblBack2ExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
            	mbDblBack2ExitPressedOnce = false;   

            }
        }, 2000);
    } 
	
}