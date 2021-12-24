// MFP project, ScriptAnalyzer.java : Designed and developed by Tony Cui in 2021
//:CodeAnalyzer.java analyzes the mfp codes
package com.cyzapps.Jmfp;

import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jfcalc.Operators.CalculateOperator;
import com.cyzapps.Jfcalc.Operators.OPERATORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.ERRORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.ProgContext.StaticProgContext;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.UnknownVarOperator;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import com.cyzapps.OSAdapter.ParallelManager.CallAgent;
import com.cyzapps.Oomfp.SpaceMember.AccessRestriction;
import com.cyzapps.adapter.MFPAdapter;

public class ScriptAnalyzer{
    
    public static ScriptInterrupter msscriptInterrupter = null;
    public static abstract class ScriptInterrupter    {
        public abstract boolean shouldInterrupt();
        public abstract void interrupt() throws InterruptedException;        
    }

    // todo : we should not derive it from exception here because it is too expensive to initialize an exception.    
    public class ScriptStatementException extends Exception    {
        private static final long serialVersionUID = 1L;
        public Statement m_statement;
        public int m_statementPosition;
        public ScriptStatementException()    {
            m_statement = null;
            m_statementPosition = -1;
        }
        public ScriptStatementException(Statement statement, int statementPosition)    {
            m_statement = statement;
            m_statementPosition = statementPosition;
        }
        @Override
        public String getMessage() {
            return "Script statement error!";
        }
        @Override
        public String getLocalizedMessage() {
            return "Script statement error!";
        }
        
        @Override
        public Throwable fillInStackTrace() {
            return this;    // override this to avoid expensive operation.
        }
    }
    
    public class FuncRetException extends ScriptStatementException    {
        private static final long serialVersionUID = 1L;
        public DataClass m_datumReturn;
        public FuncRetException(Statement sFuncRet, int nStatementPosition) {
            super(sFuncRet, nStatementPosition);
            m_datumReturn = null;
        }
        public FuncRetException(Statement sFuncRet, int nStatementPosition, DataClass datumReturn)
                throws JFCALCExpErrException    {
            super(sFuncRet, nStatementPosition);
            m_datumReturn = datumReturn.copySelf();
        }
    }
    
    public class BreakException extends ScriptStatementException    {
        private static final long serialVersionUID = 1L;
        public BreakException()    {
            super();
        }
        public BreakException(Statement sBreak, int nStatementPosition)    {
            super(sBreak, nStatementPosition);
        }
    }
    
    public class ContinueException extends ScriptStatementException    {
        private static final long serialVersionUID = 1L;
        public ContinueException()    {
            super();
        }
        public ContinueException(Statement sContinue, int nStatementPosition)    {
            super(sContinue, nStatementPosition);
        }
    }
    
    public class UntilException extends ScriptStatementException    {
        private static final long serialVersionUID = 1L;
        public UntilException()    {
            super();
        }
        public UntilException(Statement sUntil, int nStatementPosition)    {
            super(sUntil, nStatementPosition);
        }
    }
    
    public static class InFunctionCSManager {
        protected StaticProgContext mstaticProgContext;
        protected boolean mbIsOriginalCSListNull = false;
        protected LinkedList<LinkedList<String[]>> mlistInFunctionCSStack = new LinkedList<LinkedList<String[]>>();
        protected LinkedList<String[]> mlistAllCSes = new LinkedList<String[]>();
        public String[] mbaseCitingSpace = new String[]{""};
        public InFunctionCSManager(StaticProgContext staticProgContext) {
            mstaticProgContext = staticProgContext;
            mbIsOriginalCSListNull = (mstaticProgContext.mexplicitCitingSpaces == null
                                   && mstaticProgContext.mstCallingFunc == null);
            mlistAllCSes = (mstaticProgContext.mexplicitCitingSpaces != null)?
                            mstaticProgContext.mexplicitCitingSpaces
                            :(mstaticProgContext.mstCallingFunc != null)?
                                            mstaticProgContext.mstCallingFunc.m_lCitingSpaces
                                            :null;
            if (mlistAllCSes != null && mlistAllCSes.size() > 0) {
                mbaseCitingSpace = mlistAllCSes.peek();
            }
        }

        public int getNumOfCSStack() {
            return mlistInFunctionCSStack.size();
        }

        public int pushCSStack() {
            // return new CS stack depth
            mlistInFunctionCSStack.push(new LinkedList<String[]>());
            if (mlistAllCSes == null) {
                mlistAllCSes = new LinkedList<String[]>();
                mlistAllCSes.addAll(MFPAdapter.getAllCitingSpaces(null));
                mstaticProgContext.mexplicitCitingSpaces = mlistAllCSes;
            }
            return mlistAllCSes.size();
        }

        public int addNewCS(String[] cs) {
            mlistInFunctionCSStack.getFirst().push(cs);    // mlistInFunctionCSStack's size should > 0
            mlistAllCSes.push(cs);    // mlistAllCSes is not null.
            return mlistAllCSes.size();
        }

        public int popCSStack() {
            // return new CS stack depth
            LinkedList<String[]> csStack = mlistInFunctionCSStack.pop();
            for (int idx = 0; idx < csStack.size(); idx ++) {
                mlistAllCSes.pop();                
            }
            if (mbIsOriginalCSListNull && mlistAllCSes.size() == MFPAdapter.getAllCitingSpaces(null).size()) {
                mstaticProgContext.mexplicitCitingSpaces = mlistAllCSes = null;
            }
            return mlistAllCSes.size();
        }

        public int popAllCSStacks() {
            while (mlistInFunctionCSStack.size() > 0) {
                LinkedList<String[]> csStack = mlistInFunctionCSStack.pop();
                for (int idx = 0; idx < csStack.size(); idx ++) {
                    mlistAllCSes.pop();                
                }
            }
            if (mbIsOriginalCSListNull) {
                mstaticProgContext.mexplicitCitingSpaces = mlistAllCSes = null;
            }
            return mlistAllCSes.size();
        }

    }

    public class NextStatementFilter {
        protected Statement m_sThisStatement = null;
        protected int mnThisStatementIndex =  -1;
        protected String[] m_strCandidates = null;    // null means all the statements can be candidates except the ones in shouldnots
        protected boolean m_bEndofBlock = false;
        protected String[] m_strShouldNots = new String[0];    //new String[0] means no statement should not be candidate except the ones in candidates.
        public class BorderPair {
            public String[] m_strBorders = new String[2];
            public int m_nEmbeddedLevel = 0;
        }
        public BorderPair[] m_borderPairs = new BorderPair[0];
        
        public void clear()    {
            m_sThisStatement = null;
            mnThisStatementIndex = -1;
            m_strCandidates = null;
            m_bEndofBlock = false;
            m_strShouldNots = new String[0];
        }
        
        public void set(Statement sThisStatement, int nThisStatementIndex, String[] strCandidates, boolean bEndofBlock, String[] strShouldNots)    {
            /*
             * we do not check if there is any confliction, e.g. one statement type is both in
             * candidates set and in shouldnots set. Programmer should guarantee this.
             */
            m_sThisStatement = sThisStatement;
            mnThisStatementIndex = nThisStatementIndex;
            m_strCandidates = strCandidates;
            m_bEndofBlock = bEndofBlock;
            m_strShouldNots = strShouldNots;
        }
        
        NextStatementFilter()    {
            m_borderPairs = new BorderPair[8];
            m_borderPairs[0] = new BorderPair();
            m_borderPairs[0].m_strBorders[0] = Statement_function.getTypeStr();
            m_borderPairs[0].m_strBorders[1] = Statement_endf.getTypeStr();
            m_borderPairs[1] = new BorderPair();
            m_borderPairs[1].m_strBorders[0] = Statement_if.getTypeStr();
            m_borderPairs[1].m_strBorders[1] = Statement_endif.getTypeStr();
            m_borderPairs[2] = new BorderPair();
            m_borderPairs[2].m_strBorders[0] = Statement_for.getTypeStr();
            m_borderPairs[2].m_strBorders[1] = Statement_next.getTypeStr();
            m_borderPairs[3] = new BorderPair();
            m_borderPairs[3].m_strBorders[0] = Statement_while.getTypeStr();
            m_borderPairs[3].m_strBorders[1] = Statement_loop.getTypeStr();
            m_borderPairs[4] = new BorderPair();
            m_borderPairs[4].m_strBorders[0] = Statement_do.getTypeStr();
            m_borderPairs[4].m_strBorders[1] = Statement_until.getTypeStr();
            m_borderPairs[5] = new BorderPair();
            m_borderPairs[5].m_strBorders[0] = Statement_select.getTypeStr();
            m_borderPairs[5].m_strBorders[1] = Statement_ends.getTypeStr();            
            m_borderPairs[6] = new BorderPair();
            m_borderPairs[6].m_strBorders[0] = Statement_try.getTypeStr();
            m_borderPairs[6].m_strBorders[1] = Statement_endtry.getTypeStr();            
            m_borderPairs[7] = new BorderPair();
            m_borderPairs[7].m_strBorders[0] = Statement_call.getTypeStr();
            m_borderPairs[7].m_strBorders[1] = Statement_endcall.getTypeStr();            
        }
        
        public boolean isNextStatement(Statement sStatement) throws JMFPCompErrException    {
            /*
             * we do not check if there is any confliction, e.g. one statement type is both in
             * candidates set and in shouldnots set. Programmer should guarantee this.
             */
            if (m_strShouldNots != null)    {
                for (int index = 0; index < m_strShouldNots.length; index ++)    {
                    if (m_strShouldNots[index].equals(sStatement.mstatementType.mstrType))    {
                        ERRORTYPES e = ERRORTYPES.SHOULD_NOT_AFTER_PREVIOUS_STATEMENT;
                        throw new JMFPCompErrException(sStatement.mstrFilePath, sStatement.mnStartLineNo, sStatement.mnEndLineNo, e);    
                    }
                }
            }

            if (m_strCandidates == null)    {
                // this implies that every statement can be candidate.
                return true;
            } else    {
                for (int index1 = 0; index1 < m_borderPairs.length; index1 ++)    {
                    if (sStatement.mstatementType.getType().equals(m_borderPairs[index1].m_strBorders[0]))    {
                        m_borderPairs[index1].m_nEmbeddedLevel ++;
                    } else if (sStatement.mstatementType.getType().equals(m_borderPairs[index1].m_strBorders[1]))    {
                        int nMatchedCandidate = 0;
                        for (nMatchedCandidate = 0; nMatchedCandidate < m_strCandidates.length; nMatchedCandidate ++)    {
                            if (m_strCandidates[nMatchedCandidate].equals(m_borderPairs[index1].m_strBorders[1]))    {
                                break;
                            }
                        }
                        if (nMatchedCandidate >= m_strCandidates.length)    {
                            // not next statement candidate
                            m_borderPairs[index1].m_nEmbeddedLevel --;
                            if (m_borderPairs[index1].m_nEmbeddedLevel < 0)    {
                                ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
                                throw new JMFPCompErrException(sStatement.mstrFilePath, sStatement.mnStartLineNo, sStatement.mnEndLineNo, e);    
                            }
                        } else    {
                            if (m_borderPairs[index1].m_nEmbeddedLevel > 0)    {
                                // next statement candidate, but in a sub-block
                                m_borderPairs[index1].m_nEmbeddedLevel --;
                                return false;
                            } else    {
                                return true;    // we are not in a sub-block of pair index1, match.
                            }
                        }
                    }
                }
                for (int index1 = 0; index1 < m_borderPairs.length; index1 ++)    {
                    if (m_borderPairs[index1].m_nEmbeddedLevel > 0)    {
                        return false;    // we are in a sub-block.
                    }
                }
                for (int index = 0; index < m_strCandidates.length; index ++)    {
                    if (m_strCandidates[index].equals(sStatement.mstatementType.mstrType))    {
                        return true;    // we are not in a sub-block and we find a match here
                    }
                }
                return false;
            }
        }
        
        public boolean isEndofBlock()    {
            return m_bEndofBlock;
        }
    }
    
    public int analyzeBlock(Statement[] sarrayLines,    // statements of this script
            int nDeclarationPosition,    // The position of block declaration in sarrayLines 
            LinkedList<Variable> lParams,    // parameter value list used by function or for block, empty if other blocks
            InFunctionCSManager inFuncCSMgr,    // in function CS manager
            ProgContext progContext)    // variable name space
            throws
            ErrorProcessor.JMFPCompErrException,    // compiling exception
            FuncRetException,    // function return by throwing FuncRetException
            BreakException,        // break statement throws break exception
            ContinueException,        // continue statement throws continue exception
            UntilException, JFCALCExpErrException,    // until statement throws until exception
            InterruptedException    {    // sleep function may throw Interrupted Exception
        
        // in this function we make an assumption that every element in sarrayLines cannot be null.
        // and all the statements have been analyzed.
        
        if (sarrayLines.length == 0)    {
            ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
            throw new JMFPCompErrException("", 1, 1, e);
        } else if (nDeclarationPosition >= sarrayLines.length || nDeclarationPosition < 0)    {
            ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
            Statement sLast = sarrayLines[sarrayLines.length - 1];
            throw new JMFPCompErrException(sLast.mstrFilePath, 1, sLast.mnEndLineNo, e);
        } else if (nDeclarationPosition >= sarrayLines.length - 1)    {
            ERRORTYPES e = ERRORTYPES.INCOMPLETE_BLOCK;
            Statement sLast = sarrayLines[sarrayLines.length - 1];
            throw new JMFPCompErrException(sLast.mstrFilePath, sLast.mnEndLineNo, sLast.mnEndLineNo, e);
        }
        Statement sDeclaration = sarrayLines[nDeclarationPosition];
        if (sDeclaration.meAnalyze != null)    {
            if (sDeclaration.meAnalyze instanceof JMFPCompErrException) {
                throw new JMFPCompErrException(sDeclaration.mstrFilePath, sDeclaration.mnStartLineNo, sDeclaration.mnEndLineNo,
                        ((JMFPCompErrException)sDeclaration.meAnalyze).m_se.m_enumErrorType);
            } else  {
                throw new JMFPCompErrException(sDeclaration.mstrFilePath, sDeclaration.mnStartLineNo, sDeclaration.mnEndLineNo,
                        ERRORTYPES.INVALID_EXPRESSION, sDeclaration.meAnalyze);
            }
        }
        
        JMFPCompErrException jmfpeCaught = null; 
        
        if (sDeclaration.mstatementType.mstrType.equals(Statement_function.getTypeStr()))    {
            Statement_function sf = (Statement_function)(sDeclaration.mstatementType);
            boolean bOptParams = false;
            if (sf.m_strParams.length != 0 && sf.m_strParams[sf.m_strParams.length - 1].equals("opt_argv"))    {
                bOptParams = true;
            }
            if ((bOptParams == false && sf.m_strParams.length != lParams.size())
                    || (bOptParams == true && sf.m_strParams.length > (lParams.size() + 1)))    {
                // check if the number of parameters match definition.
                ERRORTYPES e = ERRORTYPES.INCORRECT_NUMBER_OF_PARAMETERS;
                throw new JMFPCompErrException(sDeclaration.mstrFilePath, sDeclaration.mnStartLineNo, sDeclaration.mnEndLineNo, e);
            } else if (bOptParams == true)    {
                // optional parameters
                Variable vOptArgC = new Variable();
                vOptArgC.setName("opt_argc");
                DataClassArray datumAllOptArgs = DCHelper.lightCvtOrRetDCArray(lParams.getLast().getValue());
                int nOptArgC = datumAllOptArgs.getDataListSize();
                DataClass datumOptArgC = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nOptArgC));
                vOptArgC.setValue(datumOptArgC);
                lParams.addLast(vOptArgC);
            }
        }
    
        // use a try ... finally block here to catch all exceptions, reset var name spaces and
        // citing spaces, then throw them out again.
        int nVarNameSpacesDepthAtEntrance = progContext.mdynamicProgContext.mlVarNameSpaces.size();
        int nCSStackDepthAtEntrance = inFuncCSMgr.getNumOfCSStack();
        try {
            
            LinkedList<LinkedList<Variable>> lVarNameSpaces = progContext.mdynamicProgContext.mlVarNameSpaces;
            LinkedList<Variable> lLocalVars = lParams;    // initialize local name space
            lVarNameSpaces.addFirst(lLocalVars);    // push local variable list into name space.

            // now need to get reference of citing spaces.
            inFuncCSMgr.pushCSStack();    // push a new empty CS stack in.

            if (msscriptInterrupter != null)    {
                if (msscriptInterrupter.shouldInterrupt())    {
                    msscriptInterrupter.interrupt();
                }
            }

            int index;
            NextStatementFilter nsf = new NextStatementFilter();    // the next statement to find and execute
            for (index = nDeclarationPosition + 1; index < sarrayLines.length; index ++)    {

                Statement sLine = sarrayLines[index];
                if (nsf.isNextStatement(sLine) == false)    {
                    continue;    // not next statement candidate
                }
                // if sLine is not annotation and its analysis was unsuccessful, throw exception.
                if (sLine.meAnalyze != null && !sLine.mstatementType.mstrType.equals(Annotation.getTypeStr()))    {
                    if (sLine.meAnalyze instanceof JMFPCompErrException) {
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo,
                                ((JMFPCompErrException)sLine.meAnalyze).m_se.m_enumErrorType);
                    } else  {
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo,
                                ERRORTYPES.INVALID_EXPRESSION, sLine.meAnalyze);
                    }
                }

                // debug output
                //System.out.printf("%d:%s\n", sLine.m_nLineNo - 1, strLines[sLine.m_nLineNo - 1]);
                sLine.mstatementType.analyze2(progContext, false); // analyzing statement on the fly.
                if (sLine.mstatementType.mstrType.equals(Statement_function.getTypeStr()))    {
                    // multiple function keywords.
                    ERRORTYPES e = ERRORTYPES.EMBEDDED_FUNCTION_DEFINITION;
                    throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);
                } else if (sLine.mstatementType.mstrType.equals(Statement_endf.getTypeStr()))    {
                    if (sDeclaration.mstatementType.mstrType.equals(Statement_function.getTypeStr()))    {
                        /*
                         *  arriving at endf means the function does not return anything, throw an exception
                         *  which includes a null datumReturn
                         */
                        throw new FuncRetException(sLine, index);
                    } else {
                        // incomplete block
                        ERRORTYPES e = ERRORTYPES.INCOMPLETE_BLOCK;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_return.getTypeStr()))    {
                    Statement_return sr = (Statement_return)(sLine.mstatementType);
                    if (sr.m_strReturnedExpr.length() > 0)    {
                        // do return something.
                        DataClass datumReturn = ScriptAnalysisHelper.analyseAExprOrString(sr.maexprReturnedExpr, sr.m_strReturnedExpr, sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                        if (datumReturn == null) {
                            throw new FuncRetException(sLine, index);
                        } else {
                            throw new FuncRetException(sLine, index, datumReturn);
                        }
                    } else {
                        throw new FuncRetException(sLine, index);
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_using.getTypeStr()))    {
                    Statement_using su = (Statement_using)(sLine.mstatementType);
                    su.convertRelativeCS2Absolute(inFuncCSMgr.mbaseCitingSpace);
                    inFuncCSMgr.addNewCS(su.m_strarrayCitingSpace);
                    nsf.clear();    // no filter applied
                } else if (sLine.mstatementType.mstrType.equals(Statement_variable.getTypeStr()))    {
                    LinkedList<Variable> listVar = new LinkedList<Variable>();
                    Statement_variable sv = (Statement_variable)(sLine.mstatementType);
                    for (int index1 = 0; index1 < sv.mstrVariables.length; index1 ++)    {
                        String strVarName = sv.mstrVariables[index1];
                        if (VariableOperator.lookUpList(strVarName, lLocalVars) != null)    {
                            ERRORTYPES e = ERRORTYPES.REDEFINED_VARIABLE;
                            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);                                                
                        }
                        Variable var = new Variable(sv.mstrVariables[index1], new DataClassNull());
                        listVar.add(var);
                    }
                    // after verify all the variables, then assign initial values and add them into namespace.
                    for (int index1 = 0; index1 < listVar.size(); index1 ++)    {
                        if (sv.mstrVarValues[index1].length() != 0)    {
                            try    {
                                DataClass datumReturn = ScriptAnalysisHelper.analyseAExprOrString(sv.maexprVarValues[index1],
                                		sv.mstrVarValues[index1], sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                                if (datumReturn == null)    {
                                    ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                                    throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                                }
                                DataClass datumNewValue = ExprEvaluator.evaluateTwoOperandCell(listVar.get(index1).getValue(),
                                                                    new CalculateOperator(OPERATORTYPES.OPERATOR_ASSIGN, 2),
                                                                    datumReturn);
                                listVar.get(index1).setValue(datumNewValue);
                            } catch(JFCALCExpErrException e)    {
                                ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
                                throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, e);                                                
                            }
                        }
                        lLocalVars.addFirst(listVar.get(index1));
                    }
                    nsf.clear();    // no filter applied
                } else if (sLine.mstatementType.mstrType.equals(Statement_if.getTypeStr()))    {
                    Statement_if sif = (Statement_if)(sLine.mstatementType);
                    DataClass datumCondition;
                    try    {
                        datumCondition = ScriptAnalysisHelper.analyseAExprOrString(sif.maexprCondition, sif.m_strCondition, sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                        if (datumCondition == null)    {
                            ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                        }
                        datumCondition = DCHelper.lightCvtOrRetDCMFPBool(datumCondition);
                    } catch(JFCALCExpErrException e)    {
                        ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, e);                                                
                    }
                    String[] strCandidates;
                    if (DCHelper.isDataClassType(datumCondition, DATATYPES.DATUM_MFPBOOL)
                            && DCHelper.lightCvtOrRetDCMFPBool(datumCondition).getDataValue().isActuallyTrue())    {
                        /*
                         * condition is true
                         */
                        // empty parameter list
                        LinkedList<Variable> l = new LinkedList<Variable>();
                        /*
                         * index is the next line of elseif, else or endif.
                         */
                        // m_nLineNo starts from 1, and AnalyzeLock needs first body line number
                        // (excluding declaration) starting from 0.
                        index = analyzeBlock(sarrayLines, index, l, inFuncCSMgr, progContext); 
                        nsf.clear();    // jump out of analyze block when arrive at endif. after for loop increase
                                        // index by one, the next statement should be the statement below endif.
                                        // Thus, nsf should be cleared.
                    } else {
                        /*
                         * condition is false
                         */
                        strCandidates = new String[3];
                        strCandidates[0] = Statement_elseif.getTypeStr();
                        strCandidates[1] = Statement_else.getTypeStr();
                        strCandidates[2] = Statement_endif.getTypeStr();
                        // this means all statements can be after if but the next step is to jump to elseif, else or endif.
                        String[] strShouldNots = new String[0];
                        nsf.set(sLine, index, strCandidates, false, strShouldNots);
                    }

                } else if (sLine.mstatementType.mstrType.equals(Statement_elseif.getTypeStr()))    {
                    if (nsf.m_sThisStatement != null
                            && (nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_if.getTypeStr())
                            || nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_elseif.getTypeStr())))    {
                        // previous if / elseif conditions are false
                        // else if should not be the end of if/elseif/else/endif block anyway.
                        Statement_elseif selseif = (Statement_elseif)(sLine.mstatementType);
                        DataClass datumCondition;
                        try    {
                        	datumCondition = ScriptAnalysisHelper.analyseAExprOrString(selseif.maexprCondition, selseif.m_strCondition, sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                            if (datumCondition == null)    {
                                ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                                throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                            }
                            datumCondition = DCHelper.lightCvtOrRetDCMFPBool(datumCondition);
                        } catch(JFCALCExpErrException e)    {	// this catch is required because lightCvtOrRetDCMFPBool may throw.
                            ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
                            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, e);                                                
                        }
                        String[] strCandidates;
                        if (DCHelper.isDataClassType(datumCondition, DATATYPES.DATUM_MFPBOOL)
                                && DCHelper.lightCvtOrRetDCMFPBool(datumCondition).getDataValue().isActuallyTrue())    {
                            /*
                             * condition is true
                             */
                            // empty parameter list
                            LinkedList<Variable> l = new LinkedList<Variable>();
                            /*
                             * index is the next line of elseif, else or endif.
                             */
                            // m_nLineNo starts from 1, and AnalyzeLock needs first body line number
                            // (excluding declaration) starting from 0.
                            index = analyzeBlock(sarrayLines, index, l, inFuncCSMgr, progContext);
                            nsf.clear();    // jump out of analyze block when arrive at endif. after for loop increase
                                            // index by one, the next statement should be the statement below endif.
                                            // Thus, nsf should be cleared.
                        } else {
                            /*
                             * condition is false
                             */
                            strCandidates = new String[3];
                            strCandidates[0] = Statement_elseif.getTypeStr();
                            strCandidates[1] = Statement_else.getTypeStr();
                            strCandidates[2] = Statement_endif.getTypeStr();
                            // all statements can be after this statement but the next step is to jump to one of the candidates
                            String[] strShouldNots = new String[0];
                            nsf.set(sLine, index, strCandidates, false, strShouldNots);
                        }
                    } else if (sDeclaration.mstatementType.mstrType.equals(Statement_if.getTypeStr())
                            || sDeclaration.mstatementType.mstrType.equals(Statement_elseif.getTypeStr()))    {
                        /*
                         * the end of if or else branch block. Exit the analyzeBlock function. However,
                         * index should not be added because index will be automatically added by one
                         * later on.
                         */
                        String[] strCandidates = new String[1];
                        strCandidates[0] = Statement_endif.getTypeStr();
                        // this means all statements can be after if but the next step is to jump to elseif, else or endif.
                        String[] strShouldNots = new String[0];
                        nsf.set(sLine, index, strCandidates, true, strShouldNots);
                    } else {
                        // it is not in a if/elseif block and there is no if/elseif before it.
                        ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);                    
                    }

                } else if (sLine.mstatementType.mstrType.equals(Statement_else.getTypeStr()))    {
                    if (nsf.m_sThisStatement != null
                            && (nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_if.getTypeStr())
                            || nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_elseif.getTypeStr())))    {
                        // previous if / elseif conditions are false
                        /*
                         * if else is not the end of a if/elseif branch, it should be the beginning of 
                         * else branch
                         */
                        // empty parameter list
                        LinkedList<Variable> l = new LinkedList<Variable>();
                        /*
                         * index is the next line of elseif, else or endif.
                         */
                        // m_nLineNo starts from 1, and AnalyzeLock needs first body line number
                        // (excluding declaration) starting from 0.
                        index = analyzeBlock(sarrayLines, index, l, inFuncCSMgr, progContext);
                        nsf.clear();    // jump out of analyze block when arrive at endif. after for loop increase
                                        // index by one, the next statement should be the statement below endif.
                                        // Thus, nsf should be cleared.
                    } else if (sDeclaration.mstatementType.mstrType.equals(Statement_if.getTypeStr())
                            || sDeclaration.mstatementType.mstrType.equals(Statement_elseif.getTypeStr()))    {
                        /*
                         * the end of if or else branch block. Exit the analyzeBlock function. However,
                         * index should not be added because index will be automatically added by one
                         * later on.
                         */
                        //index ++;
                        String[] strCandidates = new String[1];
                        strCandidates[0] = Statement_endif.getTypeStr();
                        // this means all statements can be after if but the next step is to jump to elseif, else or endif.
                        String[] strShouldNots = new String[0];
                        nsf.set(sLine, index, strCandidates, true, strShouldNots);
                    } else {
                        // it is not in a if/elseif block and there is no if/elseif before it.
                        ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);                    
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_endif.getTypeStr()))    {
                    if (nsf.m_sThisStatement != null
                            && (nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_if.getTypeStr())
                            || nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_elseif.getTypeStr())
                            || nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_else.getTypeStr())))    {
                        // if there is if/elseif/else before it, clear next statement filter structure.
                        if (nsf.isEndofBlock())    {
                            break;
                        } else {
                            nsf.clear();                    
                        }
                    } else if (sDeclaration.mstatementType.mstrType.equals(Statement_if.getTypeStr())
                            || sDeclaration.mstatementType.mstrType.equals(Statement_elseif.getTypeStr())
                            || sDeclaration.mstatementType.mstrType.equals(Statement_else.getTypeStr()))    {
                        /*
                         * the end of if or else branch block and exit the analyzeBlock function. However
                         * we should not go to the next line because index will be automatically added by
                         * one later on.
                         */
                        //index ++;
                        break;
                    } else    {
                        // it is not in a if/elseif/else block and there is no if/elseif/else before it.
                        ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);                    
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_while.getTypeStr()))    {
                    try    {
                        int nWhileStatementPos = index;
                        while(true)    {
                            Statement_while swhile = (Statement_while)(sLine.mstatementType);
                            DataClass datumCondition;
                            try    {
                                datumCondition = ScriptAnalysisHelper.analyseAExprOrString(swhile.maexprCondition,
                                		swhile.m_strCondition, sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                                if (datumCondition == null)    {
                                    ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                                    throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                                }
                                datumCondition = DCHelper.lightCvtOrRetDCMFPBool(datumCondition);
                            } catch(JFCALCExpErrException e)    {
                                ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
                                throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, e);                                                
                            }
                            if (DCHelper.isDataClassType(datumCondition, DATATYPES.DATUM_MFPBOOL)
                                    && DCHelper.lightCvtOrRetDCMFPBool(datumCondition).getDataValue().isActuallyTrue())    {
                                /*
                                 * condition is true
                                 */
                                // empty parameter list
                                LinkedList<Variable> l = new LinkedList<Variable>();
                                /*
                                 * index is the next line of loop.
                                 */
                                try    {
                                    // m_nLineNo starts from 1, and AnalyzeBlock needs first body line number
                                    // (excluding declaration) starting from 0.
                                    index = analyzeBlock(sarrayLines, nWhileStatementPos, l, inFuncCSMgr, progContext);
                                } catch(ContinueException e)    {
                                    continue;
                                }
                            } else    {
                                String[] strCandidates = new String[1];
                                strCandidates[0] = Statement_loop.getTypeStr();
                                // all statements can be after this statement but the next step is to jump to one of the
                                // candidate.
                                String[] strShouldNots = new String[0];
                                // in this case we are not in AnlyzeBlock caused by while. As such isendofblock should be false.
                                nsf.set(sLine, nWhileStatementPos, strCandidates, false, strShouldNots);
                                break;    // while condition is false, jump out.
                            }
                        }
                    } catch(BreakException e)    {
                        // break exception
                        String[] strCandidates = new String[1];
                        strCandidates[0] = Statement_loop.getTypeStr();
                        // all statements can be after this statement but the next step is to jump to one of the candidates
                        String[] strShouldNots = new String[0];
                        // Statement_while.getTypeStr() AnalyzeBlock returns the line of while statement so that loop here is not end of block.
                        nsf.set(e.m_statement, e.m_statementPosition, strCandidates, false, strShouldNots);
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_loop.getTypeStr()))    {
                    if (nsf.m_sThisStatement != null
                            && nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_while.getTypeStr()))    {
                        // just finish a while-loop block
                        // we always need not to identify isendofblock because while block's exit point is while, not loop.
                        nsf.clear();
                        continue;
                    } else if (nsf.m_sThisStatement != null
                            && nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_break.getTypeStr()))    {
                        // previously exited a while-loop block by break
                        // we always need not to identify isendofblock because while block's normal exit point is while, not break.
                        nsf.clear();
                        continue;
                    } else if (sDeclaration.mstatementType.mstrType.equals(Statement_while.getTypeStr()))    {
                        /*
                         * This is the end of while-loop block function. Exit this analyzeBlock
                         * function here but should not go to next line because index should go
                         * to the beginning of the loop. If this loop is never entered, we will
                         * jump from the beginning of the loop to the end of the loop.
                         */
                        index = nDeclarationPosition;
                        break;
                    } else    {
                        // something wrong here. It is not in a select/case/default/ends block and there is no case/default before it.
                        ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);                    
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_do.getTypeStr()))    {
                    try    {
                        int nDoStatementPos = index;
                        while(true)    {
                            LinkedList<Variable> l = new LinkedList<Variable>();
                            /*
                             * index is the next line of until.
                             */
                            try {
                                // m_nLineNo starts from 1, and AnalyzeLock needs first body line number
                                // (excluding declaration) starting from 0.
                                index = analyzeBlock(sarrayLines, nDoStatementPos, l, inFuncCSMgr, progContext);
                            } catch(ContinueException e)    {
                                continue;
                            }
                        }
                    } catch(BreakException e)    {
                        // break exception
                        String[] strCandidates = new String[1];
                        strCandidates[0] = Statement_until.getTypeStr();
                        // all statements can be after this statement but the next step is to jump to one of the candidates
                        String[] strShouldNots = new String[0];
                        //break here is not the end of Statement_do.getTypeStr() AnalyzeBlock because AnalyzeBlock returns to the line of do statment.
                        nsf.set(e.m_statement,  e.m_statementPosition, strCandidates, false, strShouldNots);
                    } catch(UntilException e)    {
                        // set the index to the until statement line so that the next step
                        // we can go the statement after until.
                        index = e.m_statementPosition;
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_until.getTypeStr()))    {
                    if (nsf.m_sThisStatement != null
                            && nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_break.getTypeStr()))    {
                        // previously exited a do-until block by break
                        nsf.clear();
                        // we always need not to identify isendofblock because do block's normal exit point is do, not break.
                        continue;
                    } else if (sDeclaration.mstatementType.mstrType.equals(Statement_do.getTypeStr()))    {
                        // this is the end of do-until block function
                        Statement_until suntil = (Statement_until)(sLine.mstatementType);
                        DataClass datumCondition;
                        try    {
                            datumCondition = ScriptAnalysisHelper.analyseAExprOrString(suntil.maexprCondition, suntil.m_strCondition, sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                            if (datumCondition == null)    {
                                ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                                throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                            }
                            datumCondition = DCHelper.lightCvtOrRetDCMFPBool(datumCondition);
                        } catch(JFCALCExpErrException e)    {
                            ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
                            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, e);                                                
                        }
                        if (DCHelper.isDataClassType(datumCondition, DATATYPES.DATUM_MFPBOOL)
                                && DCHelper.lightCvtOrRetDCMFPBool(datumCondition).getDataValue().isActuallyTrue())    {
                            throw new UntilException(sLine, index);
                        }
                        /*
                         * This is the end of do-until block function. Exit this analyzeBlock
                         * function here but should not go to next line because index should go
                         * to the beginning of the loop. If this loop is never entered, we will
                         * jump from the beginning of the loop to the end of the loop.
                         */
                        index = nDeclarationPosition;
                        nsf.clear();
                        break;
                    } else    {
                        // something wrong here. It is not in a select/case/default/ends block and there is no case/default before it.
                        ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);                    
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_for.getTypeStr()))    {
                    Statement_for sfor = (Statement_for)(sLine.mstatementType);
                    // calculate start, end and step value
                    DataClass datumIndex;
                    try    {
                    	DataClass datumStart = ScriptAnalysisHelper.analyseAExprOrString(sfor.maexprStart, sfor.mstrStart, sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                        if (datumStart == null)    {
                            ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                        }
                        datumIndex = ExprEvaluator.evaluateTwoOperandCell(new DataClassNull(), new CalculateOperator(OPERATORTYPES.OPERATOR_ASSIGN, 2), datumStart);
                        if (DCHelper.isSingleInteger(datumIndex))    {
                            datumIndex = DCHelper.lightCvtOrRetDCMFPInt(datumIndex);
                        } else    {
                            datumIndex = DCHelper.lightCvtOrRetDCMFPDec(datumIndex);
                        }
                    } catch(JFCALCExpErrException e)    {
                        ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, e);                                                
                    }
                    DataClass datumStep = ScriptAnalysisHelper.analyseAExprOrString(sfor.maexprStep, sfor.mstrStep, sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                    if (datumStep == null)    {
                        ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                    }
                    DataClass datumEnd = null;
                    try    {
                        int nForStatementPos = index;
                        while(true)    {
                            DataClassSingleNum datumIdx, datumStp, datumEd; // they are same as datumIndex, datumStep and datumEnd.
                            try    {
                                datumEnd = ScriptAnalysisHelper.analyseAExprOrString(sfor.maexprEnd, sfor.mstrEnd, sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                                if (datumEnd == null)    {
                                    ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                                    throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                                }
                                if (DCHelper.isSingleInteger(datumIndex))    {
                                    datumIndex = datumIdx = DCHelper.lightCvtOrRetDCMFPInt(datumIndex);
                                } else    {
                                    datumIndex = datumIdx = DCHelper.lightCvtOrRetDCMFPDec(datumIndex);
                                }
                                if (DCHelper.isSingleInteger(datumStep))    {
                                    datumStep = datumStp = DCHelper.lightCvtOrRetDCMFPInt(datumStep);
                                } else    {
                                    datumStep = datumStp = DCHelper.lightCvtOrRetDCMFPDec(datumStep);
                                }
                                if (DCHelper.isSingleInteger(datumEnd))    {
                                    datumEnd = datumEd = DCHelper.lightCvtOrRetDCMFPInt(datumEnd);
                                } else    {
                                    datumEnd = datumEd = DCHelper.lightCvtOrRetDCMFPDec(datumEnd);
                                }
                            } catch(JFCALCExpErrException e)    {
                                ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
                                throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, e);                                                
                            }
                            // OK, now datumStep, datumIndex and datumEnd are all DataClassBuiltIn types.
                            if ((datumStp.getDataValue().isActuallyPositive()
                                        && datumIdx.getDataValue().compareTo(datumEd.getDataValue()) <= 0)
                                    || (datumStp.getDataValue().isActuallyNegative()
                                        && datumIdx.getDataValue().compareTo(datumEd.getDataValue()) >= 0)
                                    || (datumStp.getDataValue().isActuallyZero()
                                        && datumIdx.getDataValue().isEqual(datumEd.getDataValue())))    {
                                /*
                                 * index matches the range.
                                 */
                                try    {
                                    Variable var = null;
                                    // empty parameter list
                                    LinkedList<Variable> l = new LinkedList<Variable>();
                                    if (sfor.mbLocalDefIndex)    {
                                        var = new Variable(sfor.mstrIndexName, datumIndex);
                                        l.addFirst(var);
                                    } else    {
                                        // originally it was 
                                        // if (VariableOperator.setValueInSpaces(lVarNameSpaces, sfor.mstrIndexName, datumIndex) == null) 
                                        // however, to support oo programming and member variables, I changed it.
                                        if (VariableOperator.setValue4VarOrMemberVar(sfor.mstrIndexName, AccessRestriction.PRIVATE, progContext, datumIndex) == null)    {
                                            ERRORTYPES e = ERRORTYPES.UNDEFINED_VARIABLE;
                                            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);                                                
                                        }
                                    }
                                    // m_nLineNo starts from 1, and AnalyzeLock needs first body line number
                                    // (excluding declaration) starting from 0.
                                    index = analyzeBlock(sarrayLines, nForStatementPos, l, inFuncCSMgr, progContext);
                                    try    {
                                    	datumStep = ScriptAnalysisHelper.analyseAExprOrString(sfor.maexprStep, sfor.mstrStep, sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                                        if (datumStep == null)    {
                                            ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                                            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                                        /*} else if (!DCHelper.isPrimitiveOrArray(datumStep)) {
                                            // no need to check isPrimitiveOrArray as will convert datumStep to number in the next statement.
                                            ERRORTYPES errType = ERRORTYPES.INVALID_VALUE_OBTAINED_FROM_EXPRESSION;
                                            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                                        */}
                                        datumIndex = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, 
                                                DCHelper.lightCvtOrRetDCSingleNum(datumIndex).getDataValue()
                                                .add(DCHelper.lightCvtOrRetDCSingleNum(datumStep).getDataValue()));
                                        if (DCHelper.isSingleInteger(datumIndex))    {
                                            datumIndex = DCHelper.lightCvtOrRetDCMFPInt(datumIndex);
                                        }
                                    } catch(JFCALCExpErrException eExpression)    {
                                        ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
                                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, eExpression);                                                
                                    }
                                } catch(ContinueException e)    {
                                    try {
                                        datumStep = ScriptAnalysisHelper.analyseAExprOrString(sfor.maexprStep, sfor.mstrStep, sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                                        if (datumStep == null)    {
                                            ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                                            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                                        /*} else if (!DCHelper.isPrimitiveOrArray(datumStep)) {
                                            // no need to check isPrimitiveOrArray as will convert datumStep to number in the next statement.
                                            ERRORTYPES errType = ERRORTYPES.INVALID_VALUE_OBTAINED_FROM_EXPRESSION;
                                            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                                        */}
                                        datumIndex = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC,
                                                DCHelper.lightCvtOrRetDCSingleNum(datumIndex).getDataValue()
                                                    .add(DCHelper.lightCvtOrRetDCSingleNum(datumStep).getDataValue()));
                                        if (DCHelper.isSingleInteger(datumIndex))    {
                                            datumIndex = DCHelper.lightCvtOrRetDCMFPInt(datumIndex);
                                        }
                                    } catch(JFCALCExpErrException eExpression)    {
                                        ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
                                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, eExpression);                                                
                                    }
                                    continue;
                                }
                            } else    {
                                String[] strCandidates = new String[1];
                                strCandidates[0] = Statement_next.getTypeStr();
                                // all statements can be after this statement but the next step is to jump to one of the
                                // candidate.
                                String[] strShouldNots = new String[0];
                                // next here is not end of Statement_for.getTypeStr() AnalyzeBlock coz Statement_for.getTypeStr() AnalyzeBlock returns to for statement line.
                                nsf.set(sLine, nForStatementPos, strCandidates, false, strShouldNots);
                                break;    // finish for - next loop in normal.
                            }
                        }
                    } catch(BreakException e)    {
                        // break exception
                        String[] strCandidates = new String[1];
                        strCandidates[0] = Statement_next.getTypeStr();
                        // all statements can be after this statement but the next step is to jump to one of the candidates
                        String[] strShouldNots = new String[0];
                        // break here is not end of Statement_for.getTypeStr() AnalyzeBlock coz Statement_for.getTypeStr() AnalyzeBlock returns to for statement line.
                        nsf.set(e.m_statement, e.m_statementPosition, strCandidates, false, strShouldNots);
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_next.getTypeStr()))    {
                    if (nsf.m_sThisStatement != null
                            && nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_for.getTypeStr()))    {
                        // just finish a for-next loop block
                        nsf.clear();
                        // we always need not to identify isendofblock because for block's exit point is for, not next.
                        continue;
                    } else if (nsf.m_sThisStatement != null
                            && nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_break.getTypeStr()))    {
                        // previously exited a for-next block by break
                        nsf.clear();
                        // we always need not to identify isendofblock because for block's normal exit point is for, not break.
                        continue;
                    } else if (sDeclaration.mstatementType.mstrType.equals(Statement_for.getTypeStr()))    {
                        /*
                         * This is the end of for-next block function. Exit this analyzeBlock
                         * function here but should not go to next line because index should go
                         * to the beginning of the loop. If this loop is never entered, we will
                         * jump from the beginning of the loop to the end of the loop.
                         */
                        index = nDeclarationPosition;
                        break;
                    } else    {
                        // something wrong here. It is not in a select/case/default/ends block and there is no case/default before it.
                        ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);                    
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_break.getTypeStr()))    {
                    // pop local variable list out from name space
                    // but should not do it here coz we don't know how many
                    // stack levels we are gonna exit.
                    throw new BreakException(sLine, index);
                } else if (sLine.mstatementType.mstrType.equals(Statement_continue.getTypeStr()))    {
                    // pop local variable list out from name space
                    // but should not do it here coz we don't know how many
                    // stack levels we are gonna exit.
                    throw new ContinueException(sLine, index);
                } else if (sLine.mstatementType.mstrType.equals(Statement_select.getTypeStr()))    {
                    String[] strCandidates = new String[3];
                    strCandidates[0] = Statement_case.getTypeStr();
                    strCandidates[1] = Statement_default.getTypeStr();
                    strCandidates[2] = Statement_ends.getTypeStr();
                    String[] strShouldNots = null;    //all strings should not be after select except case or default.
                    nsf.set(sLine, index, strCandidates, false, strShouldNots);
                } else if (sLine.mstatementType.mstrType.equals(Statement_case.getTypeStr()))    {
                    if (nsf.m_sThisStatement != null && nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_select.getTypeStr()))    {
                        // this is the case statement following a select or previous unhit case
                        // case should not be the normal exit point of select block.
                        Statement_select sselect = (Statement_select)(nsf.m_sThisStatement.mstatementType);
                        DataClass datumSelect = ScriptAnalysisHelper.analyseAExprOrString(sselect.maexprSelectedExpr,
                                sselect.m_strSelectedExpr, nsf.m_sThisStatement, ERRORTYPES.INVALID_EXPRESSION, progContext);
                        if (datumSelect == null)    {
                            ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                            throw new JMFPCompErrException(nsf.m_sThisStatement.mstrFilePath,
                                    nsf.m_sThisStatement.mnStartLineNo,
                                    nsf.m_sThisStatement.mnEndLineNo, errType);
                        }
                        Statement_case scase = (Statement_case)(sLine.mstatementType);
                        DataClass datumCase = ScriptAnalysisHelper.analyseAExprOrString(scase.maexprCaseExpr,
                                scase.m_strCaseExpr, sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                        if (datumCase == null)    {
                            ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                        }
                        if (datumSelect.isEqual(datumCase))    {    // so that we can compare string, array, complex
                            // this is the case!
                            // empty parameter list
                            LinkedList<Variable> l = new LinkedList<Variable>();
                            try    {
                                /*
                                 * index is the next line of ends.
                                 */
                                // m_nLineNo starts from 1, and AnalyzeLock needs first body line number
                                // (excluding declaration) starting from 0.
                                index = analyzeBlock(sarrayLines, index, l, inFuncCSMgr, progContext);
                                // we have arrived at ends so that clear nsf here.
                                nsf.clear();
                            } catch (BreakException e)    {
                                // break exception
                                String[] strCandidates = new String[1];
                                strCandidates[0] = Statement_ends.getTypeStr();
                                // all strings can be after this statement but the next step is jump to ends.
                                String[] strShouldNots = new String[0];
                                // we have already been out of Statement_case.getTypeStr() analyzeblock 
                                nsf.set(e.m_statement, e.m_statementPosition, strCandidates, false, strShouldNots);
                            }
                        } else {
                            // this is not the case
                            continue;
                        }
                    } else if (sDeclaration.mstatementType.mstrType.equals(Statement_case.getTypeStr()))    {
                        // this is another case. If no break before, ignore it.
                        continue;
                    } else     {
                        ERRORTYPES e = ERRORTYPES.SHOULD_NOT_AFTER_PREVIOUS_STATEMENT;
                        throw new JMFPCompErrException(nsf.m_sThisStatement.mstrFilePath, nsf.m_sThisStatement.mnStartLineNo,
                                                                        nsf.m_sThisStatement.mnEndLineNo, e);                                                
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_default.getTypeStr()))    {
                    if (sDeclaration.mstatementType.mstrType.equals(Statement_case.getTypeStr()))    {
                        // this is default case. If no break before, ignore it.
                        continue;
                    } else    {
                        // this is the default statement following a select or previous unhit case
                        // this is the case!
                        // empty parameter list
                        LinkedList<Variable> l = new LinkedList<Variable>();
                        try    {
                            /*
                             * index is the next line of ends.
                             */
                            // m_nLineNo starts from 1, and AnalyzeLock needs first body line number
                            // (excluding declaration) starting from 0.
                            index = analyzeBlock(sarrayLines, index, l, inFuncCSMgr, progContext);
                            // we have arrived at ends so that we should reset next statement filter here.
                            nsf.clear();
                        } catch (BreakException e)    {
                            // break exception
                            String[] strCandidates = new String[1];
                            strCandidates[0] = Statement_ends.getTypeStr();
                            // all strings can be after this statement but the next step is jumping to ends
                            String[] strShouldNots = new String[0];
                            // we have already been out of Statement_default.getTypeStr() analyzeblock.
                            nsf.set(e.m_statement, e.m_statementPosition, strCandidates, false, strShouldNots);
                        }
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_ends.getTypeStr()))    {
                    if (nsf.m_sThisStatement != null
                            && (nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_break.getTypeStr())
                                    || nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_select.getTypeStr())))    {
                        // exited a select-case blog by break previously or no case nor default is hit so exit anyway.
                        nsf.clear();
                        // we have previously exited case or default analyzeBlock so need not exit again.
                        continue;
                    } else if (sDeclaration.mstatementType.mstrType.equals(Statement_select.getTypeStr())
                            || sDeclaration.mstatementType.mstrType.equals(Statement_case.getTypeStr())
                            || sDeclaration.mstatementType.mstrType.equals(Statement_default.getTypeStr()))    {
                        /*
                         * this is the end of select-case blog function, exit this analyzeBlock function
                         * index should not be added by one because it will be automatically added later
                         * on
                         */
                        //index ++;
                        break;
                    } else    {
                        // something wrong here. It is not in a select/case/default/ends block and there is no case/default before it.
                        ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);                    
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_try.getTypeStr()))    {
                    // empty parameter list
                    LinkedList<Variable> l = new LinkedList<Variable>();
                    /*
                     * index is the line of try.
                     */
                    // m_nLineNo starts from 1, and AnalyzeLock needs first body line number
                    // (excluding declaration) starting from 0.
                    int nTryStatementPos = index;
                    try    {
                        jmfpeCaught = null;
                        index = analyzeBlock(sarrayLines, nTryStatementPos, l, inFuncCSMgr, progContext);
                    } catch (JMFPCompErrException e)    {
                        // only JMFPCompErrException is captured.
                        jmfpeCaught = e;

                        String[] strCandidates = new String[2];
                        strCandidates[0] = Statement_catch.getTypeStr();
                        strCandidates[1] = Statement_endtry.getTypeStr();
                        // this means all statements can be after try but the next step is to jump to catch or endtry.
                        String[] strShouldNots = new String[0];
                        nsf.set(sLine, nTryStatementPos, strCandidates, false, strShouldNots);
                        continue;
                    }
                    nsf.clear();    // jump out of analyze block when arrive at endtry. after for loop increase
                                    // index by one, the next statement should be the statement below endtry.
                                    // Thus, nsf should be cleared.
                } else if (sLine.mstatementType.mstrType.equals(Statement_throw.getTypeStr()))    {
                    Statement_throw sthrow = (Statement_throw)sLine.mstatementType;
                    DataClass datum = ScriptAnalysisHelper.analyseAExprOrString(sthrow.maexprThrownExpr,
                    		sthrow.m_strThrownExpr, sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                    if (datum == null)    {
                        ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);                                                
                    } 
                    try {
                        datum = DCHelper.lightCvtOrRetDCString(datum);        
                    } catch(JFCALCExpErrException e)    {
                        ERRORTYPES errType = ERRORTYPES.WRONG_VARIABLE_TYPE;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);                                                
                    }
                    ERRORTYPES errType = ERRORTYPES.USER_DEFINED_EXCEPTION;
                    // datum is DataClassBuiltIn now
                    throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, DCHelper.lightCvtOrRetDCString(datum).getStringValue());                    
                } else if (sLine.mstatementType.mstrType.equals(Statement_catch.getTypeStr()))    {
                    if (jmfpeCaught == null)    {
                        // this means the exception has been processed or no exception was thrown in try block.
                        String[] strCandidates = new String[1];
                        strCandidates[0] = Statement_endtry.getTypeStr();
                        // this means all statements can be after catch but the next step is to jump to endtry.
                        String[] strShouldNots = new String[0];
                        nsf.set(sLine, index, strCandidates, false, strShouldNots);
                    } else    {
                        // assume all the exceptions (including lower level exceptions) caught here are eighter JFCALCExpErrException or JMFPCompErrException
                        String strExceptionLevel = "EXPRESSION";
                        String strExceptionType = "UNKNOWN_EXCEPTION";
                        String strExceptionInfo = "Unknown exception";
                        Exception eThisLevel = jmfpeCaught;
                        while (eThisLevel != null)    {
                            if (eThisLevel instanceof JMFPCompErrException) {
                                JMFPCompErrException jmfpeThisLevel = (JMFPCompErrException)eThisLevel;
                                if (jmfpeThisLevel.m_se.m_enumErrorType != ErrorProcessor.ERRORTYPES.INVALID_EXPRESSION
                                        || jmfpeThisLevel.m_exceptionLowerLevel == null)   {
                                    strExceptionLevel = "LANGUAGE";
                                    strExceptionType = jmfpeThisLevel.m_se.getErrorType();
                                    strExceptionInfo = jmfpeThisLevel.m_se.getErrorInfo();
                                    break;  // definitely not "EXPRESSION" level.
                                } else  {
                                    eThisLevel = jmfpeThisLevel.m_exceptionLowerLevel;
                                }
                            } else if (eThisLevel instanceof JFCALCExpErrException) {
                                JFCALCExpErrException jfcalceThisLevel = (JFCALCExpErrException)eThisLevel;
                                if (jfcalceThisLevel.m_se.m_enumErrorType != ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION
                                        || jfcalceThisLevel.m_exceptionLowerLevel == null)  {
                                    strExceptionLevel = "EXPRESSION";
                                    strExceptionType = jfcalceThisLevel.m_se.getErrorType();
                                    strExceptionInfo = jfcalceThisLevel.m_se.getErrorInfo();
                                    break;
                                } else  {
                                    // function evaluation exception, should have deeper exceptions
                                    eThisLevel = jfcalceThisLevel.m_exceptionLowerLevel;
                                }
                            } else if (eThisLevel instanceof JSmartMathErrException) {
                                JSmartMathErrException jfcalceThisLevel = (JSmartMathErrException)eThisLevel;
                                if (jfcalceThisLevel.m_exceptionLowerLevel == null)  {
                                    strExceptionLevel = "EXPRESSION";
                                    strExceptionType = jfcalceThisLevel.m_se.getErrorType();
                                    strExceptionInfo = jfcalceThisLevel.m_se.getErrorInfo();
                                    break;
                                } else  {
                                    // function evaluation exception, should have deeper exceptions
                                    eThisLevel = jfcalceThisLevel.m_exceptionLowerLevel;
                                }
                            } else  {
                                // unknown exception
                                strExceptionLevel = "EXPRESSION";
                                strExceptionType = "UNKNOWN_EXCEPTION";
                                strExceptionInfo = "Unknown exception";
                                break;
                            }                        
                        }
                        DataClass datumExceptionLevel = new DataClassString(strExceptionLevel);

                        DataClass datumExceptionType = new DataClassString(strExceptionType);

                        DataClass datumExceptionInfo = new DataClassString(strExceptionInfo);

                        LinkedList<Variable> lCatchFilterVars = new LinkedList<Variable>();
                        Variable vCatchFilterArgLevel = new Variable();
                        vCatchFilterArgLevel.setName("level");
                        vCatchFilterArgLevel.setValue(datumExceptionLevel);
                        lCatchFilterVars.addLast(vCatchFilterArgLevel);
                        Variable vCatchFilterArgType = new Variable();
                        vCatchFilterArgType.setName("type");
                        vCatchFilterArgType.setValue(datumExceptionType);
                        lCatchFilterVars.addLast(vCatchFilterArgType);
                        Variable vCatchFilterArgInfo = new Variable();
                        vCatchFilterArgInfo.setName("info");
                        vCatchFilterArgInfo.setValue(datumExceptionInfo);
                        lCatchFilterVars.addLast(vCatchFilterArgInfo);

                        lVarNameSpaces.addFirst(lCatchFilterVars);
                        inFuncCSMgr.pushCSStack();  // now enter catch, needs to push a cs stack
                        DataClass datum = new DataClassNull();
                        Statement_catch scatch = (Statement_catch)sLine.mstatementType;
                        try {
                            if (scatch.m_strFilter.trim().equals(""))    {
                                datum = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);    // catch any exception
                            } else    {
                                // return a boolean.
                                datum = ScriptAnalysisHelper.analyseAExprOrString(scatch.maexprFilter, scatch.m_strFilter, sLine, ERRORTYPES.INVALID_CATCH_FILTER, progContext);
                                if (datum == null)    {
                                	// actually shouldn't be here. If it is here, it is very wrong.
                                    ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                                    throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                                }
                                datum = DCHelper.lightCvtOrRetDCMFPBool(datum);
                            }
                        } catch(JFCALCExpErrException e)    { // this try catch is still needed because DCHelper.lightCvtOrRetDCMFPBool(datum) may throw.
                            ERRORTYPES errType = ERRORTYPES.INVALID_CATCH_FILTER;
                            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, e);                                                
                        }
                        // datum has been converted to boolean, MFP type must be MFP_BOOLEAN_TYPE so that we can
                        // use isFalse() instead of isActuallyFalse()
                        if (DCHelper.lightCvtOrRetDCSingleNum(datum).getDataValue().isFalse())    {
                            // false, go to next catch
                            String[] strCandidates = new String[2];
                            strCandidates[0] = Statement_catch.getTypeStr();
                            strCandidates[1] = Statement_endtry.getTypeStr();
                            // this means all statements can be after try but the next step is to jump to catch or endtry.
                            String[] strShouldNots = new String[0];
                            nsf.set(sLine, index, strCandidates, false, strShouldNots);
                        } else    {
                            // true, go to next statement in this catch block
                            jmfpeCaught = null;    // so that next catch knows that no exception needs to catch.
                            // empty parameter list
                            LinkedList<Variable> l = new LinkedList<Variable>();
                            index = analyzeBlock(sarrayLines, index, l, inFuncCSMgr, progContext);
                            nsf.clear();
                        }
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_endtry.getTypeStr()))    {
                    if (jmfpeCaught != null
                            && (nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_try.getTypeStr())
                            || nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_catch.getTypeStr())))    {
                        // the exception in try block is not handled. throw the exception out.
                        throw jmfpeCaught;                    
                    } else if (jmfpeCaught == null
                            && (sDeclaration.mstatementType.mstrType.equals(Statement_try.getTypeStr())    // no exception thrown
                                    || sDeclaration.mstatementType.mstrType.equals(Statement_catch.getTypeStr())))    {    // the exception is handled.
                        /*
                         * the end of try/catch branch block and exit the analyzeBlock function. However
                         * we should not go to the next line because index will be automatically added by
                         * one later on.
                         */
                        //index ++;
                        break;
                    } else    {
                        // it is not in a try/catch/endtry block and there is no try or catch before it.
                        ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);                    
                    }
                } else if (sLine.mstatementType.mstrType.equals(Statement_solve.getTypeStr()))    {
                    LinkedList<UnknownVariable> listVarUnknown = new LinkedList<UnknownVariable>();
                    Statement_solve ss = (Statement_solve)(sLine.mstatementType);
                    for (int index1 = 0; index1 < ss.m_strVariables.length; index1 ++)    {
                        String strVarName = ss.m_strVariables[index1];
                        Variable varFromOutside;
                        if ((varFromOutside = VariableOperator.lookUpSpaces(strVarName, lVarNameSpaces)) == null)    {
                            ERRORTYPES e = ERRORTYPES.UNDEFINED_VARIABLE;
                            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);                                                
                        }
                        UnknownVariable varUnknown = new UnknownVariable(ss.m_strVariables[index1]);
                        varUnknown.setValue(varFromOutside.getValue()); // do not deep copy the value coz it might be referred by something
                        // although unknown variable var has been assigned a value, the state of it is still unassigned.
                        varUnknown.setValueAssigned(false);
                        listVarUnknown.add(varUnknown);
                    }
                    // ensure that all the vars in unknown list are actually in lVarNameSpaces
                    UnknownVarOperator.mergeUnknowns2VarSpaces(listVarUnknown, lVarNameSpaces);
                    try {
                        SolveAnalyzer solveAnalyzer = new SolveAnalyzer();
                        index = solveAnalyzer.analyzeSolve(sarrayLines, index, listVarUnknown, progContext);
                    } catch (JSmartMathErrException e1) {
                        ERRORTYPES e = ERRORTYPES.INVALID_SOLVER;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e, e1);                                                
                    } catch (JFCALCExpErrException e1) {
                        ERRORTYPES e = ERRORTYPES.INVALID_SOLVER;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e, e1);
                    } catch (JMFPCompErrException e1)   {
                        ERRORTYPES e = ERRORTYPES.INVALID_SOLVER;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e, e1);
                    }
                    nsf.clear();    // no filter applied
                } else if (sLine.mstatementType.mstrType.equals(Statement_call.getTypeStr()))    { // TODO what to do for on received call?
                    String[] strCandidates = new String[1];
                    strCandidates[0] = Statement_endcall.getTypeStr();	// we will jump to the endcall statement
                    // this means all statements can be after call but the next step is to jump to endcall.
                    String[] strShouldNots = new String[0];
                    nsf.set(sLine, index, strCandidates, false, strShouldNots);
                } else if (sLine.mstatementType.mstrType.equals(Statement_endcall.getTypeStr()))    {
                    if (nsf.m_sThisStatement != null && nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_call.getTypeStr()))    {
                        /*
                         *  arriving at endcall from a call statement. This means we are in the client.
                         */
                        
                        // in the submitCall function, we will validate variable return.
                        
                        Statement_call sc = (Statement_call)(nsf.m_sThisStatement.mstatementType);
                        
                        DataClass datumConnect = null;
                        if (!sc.mbIsCallLocal) {  // connect to remote.
                            datumConnect = ScriptAnalysisHelper.analyseAExprOrString(sc.maexprConnect,
                                    sc.mstrConnect, sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                            if (datumConnect == null)    {
                                ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                                throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                            }
                        }
                        DataClass datumSettings = null;
                        if (sc.mstrSettings.trim().length() > 0) {
                            datumSettings = ScriptAnalysisHelper.analyseAExprOrString(sc.maexprSettings,
                                    sc.mstrSettings, sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                            if (datumSettings == null)    {
                                ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                                throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType);
                            }
                        }

                        try {
                            CallAgent callAgent = new CallAgent();
                            callAgent.submitCall(sc.mbIsCallLocal, datumConnect, datumSettings, sarrayLines,
                                    nsf.mnThisStatementIndex, index, new LinkedList(sc.msetInterfParams), progContext);
                        } catch (IOException e1) {
                            ERRORTYPES e = ERRORTYPES.INVALID_CALL_BLOCK;
                            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e, e1);                                                
                        }
                    } else if (sDeclaration.mstatementType.mstrType.equals(Statement_call.getTypeStr()))    {
                        // this is the server side. We have finished the call block, so just return.
                        throw new FuncRetException(sLine, index);
                    } else {
                        // incomplete block
                        ERRORTYPES e = ERRORTYPES.INCOMPLETE_BLOCK;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);
                    }
                    nsf.clear();    // no filter applied
                } else if (sLine.mstatementType.mstrType.equals(Statement_help.getTypeStr()))    {
                    String[] strCandidates = new String[1];
                    strCandidates[0] = Statement_endh.getTypeStr();
                    // all statements can be after help but the next step is to jump to endh.
                    String[] strShouldNots = new String[0];
                    nsf.set(sLine, index, strCandidates, false, strShouldNots);
                } else if (sLine.mstatementType.mstrType.equals(Statement_endh.getTypeStr()))    {
                    if (nsf.m_sThisStatement == null
                            || nsf.m_sThisStatement.mstatementType.mstrType.equals(Statement_help.getTypeStr()) == false)    {
                        ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);                    
                    }
                    nsf.clear();
                } else if (sLine.mstatementType.mstrType.equals(Annotation.getTypeStr()))  {
                    // compile instruction is not executed.
                    continue;
                } else if (sLine.getStatement().compareTo("") == 0)    {
                    // empty statement. just //...
                    continue;
                } else if (sLine.mstatementType.mstrType.equals(Statement_expression.getTypeStr()))    {
                    // this should be a single expression
                    AbstractExpr aexpr = ((Statement_expression)sLine.mstatementType).maexprStatement;
                    ScriptAnalysisHelper.analyseAExprOrString(aexpr, sLine.getStatement(), sLine, ERRORTYPES.INVALID_EXPRESSION, progContext);
                } else {
                    ERRORTYPES e = ERRORTYPES.INVALID_EXPRESSION; // this should be an expression but it is not.
                    throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);                
                }
            }

            if (index >= sarrayLines.length)    {
                // we've arrived at the bottom of the script but no return nor endf found.
                ERRORTYPES e = ERRORTYPES.INCOMPLETE_BLOCK;
                /*
                 *  remember that the line seen by user starts from 1 so that the line no
                 *  should be strLines.length not strLines.length - 1.
                 */
                Statement sLast = sarrayLines[sarrayLines.length - 1];
                throw new JMFPCompErrException(sLast.mstrFilePath, sLast.mnEndLineNo, sLast.mnEndLineNo, e);
            }

            return index;    // return the line No that to be processed

            // use a try ... finally block here to catch all exceptions, reset var name spaces and
            // citing spaces, then throw them out again.
        } finally {
            while (progContext.mdynamicProgContext.mlVarNameSpaces.size() > nVarNameSpacesDepthAtEntrance) {
                progContext.mdynamicProgContext.mlVarNameSpaces.poll();
            }
            while (inFuncCSMgr.getNumOfCSStack() > nCSStackDepthAtEntrance) {
                inFuncCSMgr.popCSStack();
            }
        }
    }
}
