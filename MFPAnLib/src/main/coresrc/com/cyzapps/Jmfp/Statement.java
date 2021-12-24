// MFP project, Statement.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jmfp;

import java.io.Serializable;
import java.util.Locale;

import com.cyzapps.Jfcalc.ElemAnalyzer;
import com.cyzapps.Jfcalc.DCHelper.CurPos;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Oomfp.SpaceMember.AccessRestriction;

public class Statement implements Serializable {
	private static final long serialVersionUID = 1L;
	public transient String mstrFilePath = "";	// after serialize and deserialize, m_strFilePath definitely changes
    public int mnStartLineNo = 0;    // which line does this statement start? note that line no starts from 1.
    public int mnEndLineNo = 0;    // which line does this statement end? note that line no starts from 1.
    public Exception meAnalyze = null; // the exception occurred in analyzing the statement when loading the file.
    private String mstrStatement = "";    // trimmed lower-case statement.
    private String mstrHelpTail = "";    // the help information at the tail.
    
    public boolean isFinishedStatement()    {
        // identify if the statement finished or not. Assume m_strStatement has been trimmed.
        // if the last two characters of a statement is " _", the statement is not finished.
        if (mstrStatement.length() > 2
                && mstrStatement.substring(mstrStatement.length() - 2).equals(" _"))    {
            return false;
        }
        return true;
    }
    
    public boolean concatenate(Statement sNext)    {
        if (isFinishedStatement())    {
            return false;    // this statement has finished so that can not be concatenate
        } else if(mnEndLineNo >= sNext.mnStartLineNo)    {
            return false;    // the two statements have overlapped lines, something wrong.
        } else {
            mnEndLineNo = sNext.mnEndLineNo;
            mstrStatement = mstrStatement.substring(0, mstrStatement.length() - 1) + sNext.mstrStatement;
            mstrHelpTail = mstrHelpTail + "\n" + sNext.mstrHelpTail;
            return true;
        }
    }
    
    public Statement(String strStatement, String strFilePath, int nLineNo)    {
        mstrFilePath = strFilePath;
        mnStartLineNo = mnEndLineNo = nLineNo;
        setStatement(strStatement);
        // Log.e("statement", "original is " + strStatement + " statement is " + m_strStatement + " help tail is " + m_strHelpTail);
    }
    
    public final void setStatement(String strStatement) {
        // this function will convert all non-string parts to lower case.
        // it will also trim mstrStatement.
        if (strStatement == null)    {
            mstrStatement = "";
            mstrHelpTail = "";
        } else    {
            int index = 0;
            int lastIdx = 0;
            mstrStatement = "";
            mstrHelpTail = "";
            while(index < strStatement.length())    {
                if (strStatement.charAt(index) == '"')    {
                    // should be the start of a string
                    mstrStatement += strStatement.substring(lastIdx, index).toLowerCase(Locale.US);
                    lastIdx = index;
                	try {
                		CurPos curpos = new CurPos();
                		curpos.m_nPos = index;
                		ElemAnalyzer.getString(strStatement, curpos);
                		index = curpos.m_nPos;
                        mstrStatement += strStatement.substring(lastIdx, index);
                        lastIdx = index;
                	} catch(JFCALCExpErrException ex) {
                		// the only reason is no close quatation.
                        // something wrong with the statement, string has no end.
                        mstrStatement += strStatement.substring(lastIdx);
                        mstrStatement = mstrStatement.trim();
                        mstrHelpTail = "";
                        return;
                    }
                } else if (strStatement.charAt(index) == '/'
                            && index < (strStatement.length() - 1) && strStatement.charAt(index + 1) == '/')    {
                    // comment starts.
                    mstrStatement += strStatement.substring(lastIdx, index).toLowerCase(Locale.US);
                    mstrStatement = mstrStatement.trim();
                    mstrHelpTail = strStatement.substring(index + 2);
                    return;
                } else    {
                    index ++;
                }
            }
            // no // help found.
            mstrStatement += strStatement.substring(lastIdx, index).toLowerCase(Locale.US);
            mstrStatement = mstrStatement.trim();
            mstrHelpTail = "";
        }
    }
    
    public String getStatement()    {
        return mstrStatement;
    }
    
    public Boolean isEmpty() {
        return mstrStatement.length() == 0;
    }
    
    public String getHelpTail()    {
        return mstrHelpTail;
    }
    
    // note that m_statementType in the derived object should always be no-null.
    public StatementType mstatementType = null;    // this variable stores the type of the statement.
    
    public void analyze() throws JMFPCompErrException    {
    	if (mstrStatement.startsWith("@")) {
    		// annotation
    		String[] strSplits = mstrStatement.split("[:\\s+]");	// annotation may followed by :
            String strLowerCaseStart = strSplits[0];
            mstatementType = new Annotation(this, strLowerCaseStart.substring(1));
    	} else {
	        String[] strSplits = mstrStatement.split("\\s+");
            String strStatementWithoutAccess = mstrStatement;
	        String strLowerCaseStart = strSplits[0];
            String matchDests = AccessRestriction.PUBLIC + "|" + AccessRestriction.PRIVATE;
            if (mstrStatement.matches("^(" + matchDests + ")\\s.*$")) {
                // mstrStatement has been lowercased, so no need to use "^(?i)(public|private)\\s.*$"
                // starts with public or private
                if (strSplits.length > 1) {
                    strLowerCaseStart = strSplits[1];
                    if (!strLowerCaseStart.equals(Statement_function.getTypeStr())
                            //&& !strLowerCaseStart.equals(Statement_class.getTypeStr())    // don't support class at this moment.
                            && !strLowerCaseStart.equals(Statement_variable.getTypeStr())) {
                        ErrorProcessor.ERRORTYPES e = ErrorProcessor.ERRORTYPES.ACCESS_KEYWORD_CANNOT_BE_HERE;   // public and private keywords can only be applied to function and variable
                        throw new JMFPCompErrException(mstrFilePath, mnStartLineNo, mnEndLineNo, e);
                    }
                    strStatementWithoutAccess = mstrStatement.substring(strSplits[0].length()).trim();
                } else {
                    // will not be here as mstrStatement has been trimmed.
                    ErrorProcessor.ERRORTYPES e = ErrorProcessor.ERRORTYPES.ACCESS_KEYWORD_CANNOT_BE_HERE;   // public and private keywords can only be applied to function and variable
                    throw new JMFPCompErrException(mstrFilePath, mnStartLineNo, mnEndLineNo, e);
                }
            }
            if (strLowerCaseStart.equals(Statement_citingspace.getTypeStr()))    {
	            mstatementType = new Statement_citingspace(this);
	        } else if (strLowerCaseStart.equals(Statement_endcs.getTypeStr()))    {
	            mstatementType = new Statement_endcs(this);    // end of citing space
	        } else if (strLowerCaseStart.equals(Statement_using.getTypeStr()))    {
	            mstatementType = new Statement_using(this);    // end of citing space
	        } else if (strLowerCaseStart.equals(Statement_function.getTypeStr()))    {
	            mstatementType = new Statement_function(this);
	        } else if (strLowerCaseStart.equals(Statement_endf.getTypeStr()))    {
	            mstatementType = new Statement_endf(this);    // end of function
	        } else if (strLowerCaseStart.equals(Statement_return.getTypeStr()))    {
	            mstatementType = new Statement_return(this);    // return value
	        } else if (strLowerCaseStart.equals(Statement_variable.getTypeStr()))    {
	            mstatementType = new Statement_variable(this);    // define variables
	        } else if (strLowerCaseStart.equals(Statement_if.getTypeStr()))    {
	            mstatementType = new Statement_if(this);
	        } else if (strLowerCaseStart.equals(Statement_elseif.getTypeStr()))    {
	            mstatementType = new Statement_elseif(this);    // else if
	        } else if (strLowerCaseStart.equals(Statement_else.getTypeStr()))    {
	            mstatementType = new Statement_else(this);
	        } else if (strLowerCaseStart.equals(Statement_endif.getTypeStr()))    {
	            mstatementType = new Statement_endif(this);    // end of if block
	        } else if (strLowerCaseStart.equals(Statement_while.getTypeStr()))    {
	            mstatementType = new Statement_while(this);
	        } else if (strLowerCaseStart.equals(Statement_loop.getTypeStr()))    {
	            mstatementType = new Statement_loop(this);    // end of while ... loop block
	        } else if (strLowerCaseStart.equals(Statement_do.getTypeStr()))    {
	            mstatementType = new Statement_do(this);
	        } else if (strLowerCaseStart.equals(Statement_until.getTypeStr()))    {
	            mstatementType = new Statement_until(this);    // end of do ... until block
	        } else if (strLowerCaseStart.equals(Statement_for.getTypeStr()))    {
	            mstatementType = new Statement_for(this);
	        } else if (strLowerCaseStart.equals(Statement_next.getTypeStr()))    {
	            mstatementType = new Statement_next(this);    // end of for block
	        } else if (strLowerCaseStart.equals(Statement_break.getTypeStr()))    {
	            mstatementType = new Statement_break(this);    // break from a loop
	        } else if (strLowerCaseStart.equals(Statement_continue.getTypeStr()))    {
	            mstatementType = new Statement_continue(this);    // continue a loop without executing the following statements.
	        } else if (strLowerCaseStart.equals(Statement_select.getTypeStr()))    {
	            mstatementType = new Statement_select(this);    // select ... case
	        } else if (strLowerCaseStart.equals(Statement_case.getTypeStr()))    {
	            mstatementType = new Statement_case(this);
	        } else if (strLowerCaseStart.equals(Statement_default.getTypeStr()))    {
	            mstatementType = new Statement_default(this);
	        } else if (strLowerCaseStart.equals(Statement_ends.getTypeStr()))    {
	            mstatementType = new Statement_ends(this);    // end of select
	        } else if (strLowerCaseStart.equals(Statement_try.getTypeStr()))    {
	            mstatementType = new Statement_try(this);
	        } else if (strLowerCaseStart.equals(Statement_throw.getTypeStr()))    {
	            mstatementType = new Statement_throw(this);
	        } else if (strLowerCaseStart.equals(Statement_catch.getTypeStr()))    {
	            mstatementType = new Statement_catch(this);
	        } else if (strLowerCaseStart.equals(Statement_endtry.getTypeStr()))    {
	            mstatementType = new Statement_endtry(this);
	        } else if (strLowerCaseStart.equals(Statement_solve.getTypeStr()))  {
	            mstatementType = new Statement_solve(this);
	        } else if (strLowerCaseStart.equals(Statement_slvreto.getTypeStr()))  {
	            mstatementType = new Statement_slvreto(this);
	        } else if (strLowerCaseStart.equals(Statement_call.getTypeStr()))  {
	            mstatementType = new Statement_call(this);
	        } else if (strLowerCaseStart.equals(Statement_endcall.getTypeStr()))  {
	            mstatementType = new Statement_endcall(this);
	        } else if (strLowerCaseStart.equals(Statement_class.getTypeStr()))  {
	            mstatementType = new Statement_class(this);
	        } else if (strLowerCaseStart.equals(Statement_endclass.getTypeStr()))  {
	            mstatementType = new Statement_endclass(this);
	        } else if (strLowerCaseStart.equals(Statement_help.getTypeStr()))    {
	            mstatementType = new Statement_help(this);    // help
	        } else if (strLowerCaseStart.equals(Statement_endh.getTypeStr()))    {
	            mstatementType = new Statement_endh(this);    // end of help
	        } else {
	            // it is possible that some statement key words followed closely by ( or [ or ", so need to split them by these chars.
	            strSplits = strStatementWithoutAccess.split("[\\(\\[\"]");
	            if (strSplits.length == 0) {    // for case ""
	                mstatementType = new Statement_expression(this);    // a pure expression
	            } else {
	                strLowerCaseStart = strSplits[0];
	                if (strLowerCaseStart.equals(Statement_return.getTypeStr()))    {
	                    mstatementType = new Statement_return(this);
	                } else if (strLowerCaseStart.equals(Statement_if.getTypeStr()))    {
	                    mstatementType = new Statement_if(this);
	                } else if (strLowerCaseStart.equals(Statement_elseif.getTypeStr()))    {
	                    mstatementType = new Statement_elseif(this);
	                } else if (strLowerCaseStart.equals(Statement_while.getTypeStr()))    {
	                    mstatementType = new Statement_while(this);
	                } else if (strLowerCaseStart.equals(Statement_until.getTypeStr()))    {
	                    mstatementType = new Statement_until(this);
	                } else if (strLowerCaseStart.equals(Statement_select.getTypeStr()))    {
	                    mstatementType = new Statement_select(this);
	                } else if (strLowerCaseStart.equals(Statement_case.getTypeStr()))    {
	                    mstatementType = new Statement_case(this);
	                } else if (strLowerCaseStart.equals(Statement_catch.getTypeStr()))    {
	                    mstatementType = new Statement_catch(this);
	                } else if (strLowerCaseStart.equals(Statement_throw.getTypeStr()))    {
	                    mstatementType = new Statement_throw(this);
	                } else  {
	                    mstatementType = new Statement_expression(this);    // a pure expression
	                }
	            }
	        }
    	}
        mstatementType.analyze(mstrStatement);
    }
    
    public void analyze2(FunctionEntry fe)    {
    	if (mstatementType != null && fe != null) {
            mstatementType.analyze2(fe);
    	}
    }

    private static final String[] MFP_KEY_WORDS = new String[]{
                                        Statement_citingspace.getTypeStr(),
                                        Statement_endcs.getTypeStr(),
                                        Statement_using.getTypeStr(),
                                        Statement_function.getTypeStr(),
                                        Statement_endf.getTypeStr(),
                                        Statement_return.getTypeStr(),
                                        Statement_variable.getTypeStr(),
                                        Statement_if.getTypeStr(),
                                        Statement_elseif.getTypeStr(),
                                        Statement_else.getTypeStr(),
                                        Statement_endif.getTypeStr(),
                                        Statement_while.getTypeStr(),
                                        Statement_loop.getTypeStr(),
                                        Statement_do.getTypeStr(),
                                        Statement_until.getTypeStr(),
                                        Statement_for.getTypeStr(),
                                        "to",
                                        "step",
                                        Statement_next.getTypeStr(),
                                        Statement_break.getTypeStr(),
                                        Statement_continue.getTypeStr(),
                                        Statement_select.getTypeStr(),
                                        Statement_case.getTypeStr(),
                                        Statement_default.getTypeStr(),
                                        Statement_ends.getTypeStr(),
                                        Statement_help.getTypeStr(),
                                        Statement_endh.getTypeStr(),
                                        "opt_argv",
                                        "opt_argc",
                                        Statement_throw.getTypeStr(),
                                        Statement_try.getTypeStr(),
                                        Statement_catch.getTypeStr(),
                                        Statement_endtry.getTypeStr(),
                                        Statement_solve.getTypeStr(),
                                        Statement_slvreto.getTypeStr(),
                                        Statement_call.getTypeStr(),
                                        "local",
                                        "on",
                                        "with",
                                        Statement_endcall.getTypeStr(),
                                        Statement_update.getTypeStr(),
                                        Statement_class.getTypeStr(),
                                        "self",
                                        "this",
                                        "super",
                                        "public",
                                        "private",
                                        Statement_endclass.getTypeStr(),
                                        };
    public static String[] getMFPKeyWords()    {
        // all the key words must be small letters.
        // because the key words are read only, copy and return.
        return MFP_KEY_WORDS.clone();
    }
    
    private static final String[] MFP_RESERVED_WORDS = new String[]{
                                        "quit",
                                        "argc",
                                        "argv",
                                        "f_aexpr_to_analyze",
                                        "f_single_var_invertible",
                                        "f_single_var_inverted",
                                        "internal_var*",
                                        /*"a_pcon",
                                        "b_pcon",
                                        "c_pcon",
                                        "d_pcon",
                                        "e_pcon",
                                        "f_pcon",
                                        "g_pcon",
                                        "h_pcon",
                                        "i_pcon",
                                        "j_pcon",
                                        "k_pcon",
                                        "l_pcon",
                                        "m_pcon",
                                        "n_pcon",
                                        "o_pcon",
                                        "p_pcon",
                                        "q_pcon",
                                        "r_pcon",
                                        "s_pcon",
                                        "t_pcon",
                                        "u_pcon",
                                        "v_pcon",
                                        "w_pcon",
                                        "x_pcon",
                                        "y_pcon",
                                        "z_pcon",*/
                                        "enum",
                                        "type",
                                        "typeof",
                                        "export",
                                        "import",
                                        "include",
                                        "dict",
                                        "list",
                                        "internal",
                                        "external",
                                        "end",
                                        "protected",
                                        "virtual",
                                        "extends",
                                        "finally",
                                        "declare",
                                        "define",
                                        "long",
                                        "int",
                                        "float",
                                        "double",
                                        "numeric",
                                        "string",
                                        "function",
                                        "array",
                                        "solver",
                                        "caught_expt",
                                        "true",
                                        "false",
                                        "i",
                                        "null",
                                        "inf",
                                        "infi",
                                        "nan",
                                        "nani",
                                        "at",
                                        "for_each",
                                        "start"
                                        };
    
    public static String[] getMFPReservedWords()    {
        // all the reserved words must be small letters.
        // because the reserved words are read only, copy and return.
        return MFP_RESERVED_WORDS.clone();
    }
    
    // strName must be lower case
    public static boolean isKeyword(String strName)    {
        strName = strName.trim();
        for (String strKeyWords : MFP_KEY_WORDS)    {
            if (strName.equals(strKeyWords))    {
                return true;
            }
        }
        
        for (String strReservedWords : MFP_RESERVED_WORDS)    {
            if (strReservedWords.charAt(strReservedWords.length() - 1) != '*') {
                if (strName.equals(strReservedWords))    {
                    return true;
                }
            } else {
                if (strName.length() >= strReservedWords.length() - 1
                        && strName.substring(0, strReservedWords.length() - 1).equals(strReservedWords.substring(0, strReservedWords.length() - 1))) {
                    // wide card character match
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * 
     * this function returns a shrinked name of a member variable or a member function.
     * However, the string may not be all small case.
    */
    public static String getShrinkedMemberNameStr(String strName) {
        Boolean bEndsWithDot = strName.endsWith(".");
        String[] nameParts = strName.split("\\.");
        String shrinkedName = "";
        for (String namePart : nameParts) {
            shrinkedName += namePart.trim() + ".";
        }
        if (!bEndsWithDot) {
            shrinkedName = shrinkedName.substring(0, shrinkedName.length() - 1);
        }
        return shrinkedName;
    }
    
    // strName must be lower case and shrinked. Not that shrinked doesn't mean
    // blank inside a name is removed.
    public static int validateTypeOrVarOrFuncName(String strName)    {
        if (strName.length() == 0)    {   // var name should not be 0.
            return 1;   // ERRORTYPES.BAD_VARIABLE_NAME or BAD_FUNCTION_NAME
        } else if ((strName.charAt(0) > 'z' || strName.charAt(0) < 'a') && strName.charAt(0) != '_')    {
            // variable name should start from a letter or _.
            return 1;   // ERRORTYPES.BAD_VARIABLE_NAME or BAD_FUNCTION_NAME
        } else if (strName.endsWith("."))    {
            // variable name should not end with '.'
            return 1;   // ERRORTYPES.BAD_VARIABLE_NAME or BAD_FUNCTION_NAME
        } else if (isKeyword(strName))    {
            return 2;   // ERRORTYPES.IS_KEYWORD
        } else {
            // strName not starts or ends with '.' . So if we divide it by '.', and if one part,
            // it means there is no .
            String[] nameParts = strName.split("\\.");
            if (nameParts.length == 1) {    // this implies that there is no '.'
                for (int index1 = 0; index1 < strName.length(); index1 ++)    {
                    if ((strName.charAt(index1) > 'z' || strName.charAt(index1) < 'a')
                        && (strName.charAt(index1) > '9' || strName.charAt(index1) < '0')
                        && strName.charAt(index1) != '_')    {
                        // variable name should only include 'a' to 'z', '0' to '9' and '_'
                        return 1;   // ERRORTYPES.BAD_VARIABLE_NAME or BAD_FUNCTION_NAME
                    }
                }
            } else {
                for (int index1 = 0; index1 < nameParts.length; index1 ++) {
                    if (index1 == 0) {
                        if (nameParts[index1].equals("self")) {
                            continue;   // if a variable name is like self.xxxx, 
                        }
                    } else if (index1 < nameParts.length - 1) {
                        if (nameParts[index1].startsWith("super[") && nameParts[index1].endsWith("]")) {
                            continue;
                        }
                    }
                    
                    int nRet = validateTypeOrVarOrFuncName(nameParts[index1]);
                    if (nRet > 0) {
                        return nRet;
                    }
                }
                return -1;  // return -1 means it is a member variable
            }
        }
        return 0;
    }
    
    public static String[] getMFPAnnotation() {
        // annotation starts with @ and is a special type of keywords. However, do not
        // add it in keyword list otherwise it will be too long and takes too much time
        // to identify it is a keyword. Also, function and variable should starts with
        // a letter so should have no confliction with annotations anyway.
        return new String[] {
                    "@language:",
                    "@end",
                    "@compulsory_link",
                    "@execution_entry"
                };
    }
}

