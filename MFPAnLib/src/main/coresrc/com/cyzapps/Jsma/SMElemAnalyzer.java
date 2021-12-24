// MFP project, SMElemAnalyzer.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jsma;

import java.util.LinkedList;
import com.cyzapps.Jfcalc.ElemAnalyzer;
import com.cyzapps.Jfcalc.DCHelper.CurPos;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import java.util.Locale;

public class SMElemAnalyzer extends ElemAnalyzer    {
    /***************************************************************************\
     use boolean ElemAnalyzer.IsBlankChar(String strExpression, int nCurPos)
    \***************************************************************************/

    /***************************************************************************\
     use boolean ElemAnalyzer.IsBoundOperatorChar(String strExpression, int nCurPos)
    \***************************************************************************/

    /***************************************************************************\
     use BoundOperator ElemAnalyzer.GetBoundOperator(String strExpression, CurPos curpos) throws JFCALCExpErrException
    \***************************************************************************/
    
    /***************************************************************************\
     use boolean ElemAnalyzer.IsCalcOperatorChar(String strExpression, int nCurPos)
    \***************************************************************************/

    /***************************************************************************\
     use CalculateOperator ElemAnalyzer.GetCalcOperator(String strExpression,
                                                          CurPos curpos,
                                                          int nLastPushedCellType) throws JFCALCExpErrException
    \***************************************************************************/
    
    /***************************************************************************\
     use boolean ElemAnalyzer.Is2ndOPTRHaveHighLevel(CalculateOperator COPTR1st,
                                                    CalculateOperator COPTR2nd)
    \***************************************************************************/
    
    /***************************************************************************\
     use int ElemAnalyzer.IsStartNumberChar(String strExpression, int nCurPos)
    \***************************************************************************/
    
    /***************************************************************************\
     use int ElemAnalyzer.IsDigitChar(String strExpression, int nCurPos, int nPN)
    \***************************************************************************/
    
    /***************************************************************************\
     use NUMBERCHARTYPES ElemAnalyzer.getDecimalCharType(String strExpression, int nCurPos)
    \***************************************************************************/
    
    /***************************************************************************\
     getNumberAExpr:
       This function is used to get a number constant AbstractExpr.
       Input:
         char *: The expression string.
         unsigned int &: The initial position of the number in the expression.
       Output:
         The number constant AbstractExpr.
    \
     * @throws JFCALCExpErrException, JSmartMathErrException ***************************************************************************/
    public AbstractExpr getNumberAExpr(String strExpression, CurPos curpos) throws JFCALCExpErrException, JSmartMathErrException
    {
        DataClass datumNumber = getNumber(strExpression, curpos);
        return new AEConst(datumNumber);
    }
    
    /***************************************************************************\
     use boolean ElemAnalyzer.IsDataRefChar(String strExpression, int nCurPos)
    \***************************************************************************/

    /***************************************************************************\
     getDataRefAExpr:
       This function is used to get a data reference abstract expr.
       Input:
         strExpression: The expression string.
         CurPos: The initial position of the name in the expression.
         listPseudoConsts: pseudo-constants.
       Output:
         The data reference AbstractExpr.
       Note that here variable name space is not used because variable name will be looked up when we further-analyze
       the abstract expression.
    \
     * @throws JFCALCExpErrException 
     * @throws InterruptedException 
     * @throws JSmartMathErrException ***************************************************************************/
    public AbstractExpr getDataRefAExpr(String strExpression, CurPos curpos, LinkedList<Variable> listPseudoConsts, ProgContext progContext) throws JFCALCExpErrException, InterruptedException, JSmartMathErrException
    {
        int nEndofVariablePos = curpos.m_nPos;
        if (strExpression.charAt(nEndofVariablePos) == '[')    {
            int nBracketLevel = 1;
            int i;
            /* find out the close bracket position */
            for (i = nEndofVariablePos + 1; i < strExpression.length(); i++)
            {
                if (strExpression.charAt(i) == ']')
                    nBracketLevel --;
                else if (strExpression.charAt(i) == '[')
                    nBracketLevel ++;
                if (nBracketLevel == 0)
                {
                    nEndofVariablePos = i;
                    break;
                }
            }
    
            if (nBracketLevel != 0)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_UNMATCHED_LEFTPARENTHESE);
            }
        }
        else
        {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_REFERENCE);
        }

        LinkedList<AbstractExpr> listAExprRef = new LinkedList<AbstractExpr>();  /* data reference list */
        /* get a string of index list */
        int nIndexStart = curpos.m_nPos + 1;
        boolean bNoIndex = true;  /* if there is no Index */
        for (int j = nIndexStart; j < nEndofVariablePos; j++)
        {
            if (!isBlankChar(strExpression, j))
            {
                bNoIndex = false;
                break;
            }
        }
        if (bNoIndex == true)
        {
            // throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_REFERENCE);     // do not throw exception as [] should be allowed
        }
        else
        {
            while(nIndexStart < nEndofVariablePos)
            {
                String strDataList = strExpression.substring(nIndexStart, nEndofVariablePos);
                CurPos curposSubExpr = new CurPos();
                curposSubExpr.m_nPos = 0;
                AbstractExpr aeIndex = ExprAnalyzer.analyseExpression(strDataList, curposSubExpr, listPseudoConsts, progContext);
                if (aeIndex == null)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                }
                /* if the parameter expression's value is invalid, error will be generated. */
                listAExprRef.addLast(aeIndex);
                nIndexStart += curposSubExpr.m_nPos + 1;
                if ((strDataList.length() == curposSubExpr.m_nPos + 1)
                    && (strDataList.charAt(curposSubExpr.m_nPos) == ','))
                {
                    /* this is to handle a situation like [-1,], where there is a , unnecessary */
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_EXPRESSION);
                }                            
            }
        }
        // need not to think about if aeReturn is a constant or not.
        AbstractExpr aeReturn = new AEDataRef(listAExprRef);
        curpos.m_nPos = nEndofVariablePos + 1;
        return aeReturn;        
    }

    /***************************************************************************\
     use int ElemAnalyzer.IsNameChar(String strExpression, int nCurPos)
    \***************************************************************************/
    
    /***************************************************************************\
     GetExprName:
       This function is used to get the name of a variable or a function.
       Input:
         AbstractExpr exprNameOwner: the owner of the exprName, which can be null.
         String: The expression string.
         CurPos: The initial position of the name in the expression.
         LinkedList<Variable> : pseudo variable name list
       Output:
         The value of the name.
       Note that here variable name space is not used because variable name will be looked up when we further-analyse
       the abstract expression.
    \
     * @throws JFCALCExpErrException 
     * @throws InterruptedException 
     * @throws JSmartMathErrException ***************************************************************************/
    public AbstractExpr getExprNameAExpr(AbstractExpr aeOwner, String strExpression, CurPos curpos, LinkedList<Variable> listPseudoConsts, ProgContext progContext) throws JFCALCExpErrException, InterruptedException, JSmartMathErrException
    {
        /* get the function or variable name */
        String strName = getShrinkedFuncNameStr(strExpression, curpos, "").toLowerCase(Locale.US);
    
        AbstractExpr aeReturn = AEInvalid.AEINVALID;
        int nCurNameCharPos = curpos.m_nPos;

        /* identify if it is function or variable */
        int nCurFurtherCharPos = nCurNameCharPos;
        while ((strExpression.length() > nCurFurtherCharPos) && isBlankChar(strExpression, nCurFurtherCharPos))
            nCurFurtherCharPos ++;
        
    
        if ((strExpression.length() > nCurFurtherCharPos) && (strExpression.charAt(nCurFurtherCharPos) == '(')) /* function */
        {
            int nBracketLevel = 1;
            int nSqareBracketLevel = 0;
            int nCurlyBraceLevel = 0;
            int nLastDelimiterPlace = nCurFurtherCharPos;
            LinkedList<String> listParamRawInputs = new LinkedList<String>();
            int nCloseBracketPos = nCurFurtherCharPos;
            int i;
            /* find out the close bracket position */
            for (i = nCurFurtherCharPos + 1; i < strExpression.length(); i++)
            {
                if (isStringStartChar(strExpression, i))
                {
                    CurPos curpos1 = new CurPos();
                    curpos1.m_nPos = i;
                    getString(strExpression, curpos1);
                    i = curpos1.m_nPos;
                }
                
                if (i >= strExpression.length())    {
                    // ok, we have arrived at the end of the expression, but we cannot find close bracket.
                    // jump out and throw exception
                    break;
                } else {
                    switch (strExpression.charAt(i)) {
                        case ')':    {
                            nBracketLevel --;
                            break;
                        } case '(':    {
                            nBracketLevel ++;
                            break;
                        } case '[':    {
                            nSqareBracketLevel ++;
                            break;
                        } case ']':    {
                            nSqareBracketLevel --;
                            if (nSqareBracketLevel < 0) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_CHARACTER);
                            }
                            break;
                        } case '{':    {
                            nCurlyBraceLevel ++;
                            break;
                        } case '}':    {
                            nCurlyBraceLevel --;
                            if (nCurlyBraceLevel < 0) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_CHARACTER);
                            }
                            break;
                        } case ',': {
                            if (nBracketLevel == 1 && nSqareBracketLevel == 0 && nCurlyBraceLevel == 0) {
                                // ok, this is a new delimiter
                                String param = strExpression.substring(nLastDelimiterPlace + 1, i).trim();
                                if (param.length() == 0) {
                                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_EXPRESSION);
                                }
                                listParamRawInputs.addFirst(param);
                                nLastDelimiterPlace = i;
                            }
                        }
                    }
                }
                
                if (nBracketLevel == 0)
                {
                    String param = strExpression.substring(nLastDelimiterPlace + 1, i).trim();
                    if (param.length() > 0) {
                        listParamRawInputs.addFirst(param);
                    } else if (nLastDelimiterPlace != nCurFurtherCharPos) {
                        /* this is to handle a situation like abs(-1,), where there is a , unecessary */
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_EXPRESSION);
                    }
                    nCloseBracketPos = i;
                    break;
                }
            }
    
            if (nBracketLevel != 0)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_UNMATCHED_LEFTPARENTHESE);
            }
    
            LinkedList<AbstractExpr> listParams = new LinkedList<AbstractExpr>();  /* parameter stack */
            for (String param : listParamRawInputs) {
                AbstractExpr aeParam = ExprAnalyzer.analyseExpression(param, new CurPos(), listPseudoConsts, progContext);
                if (aeParam == null)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                }
                /* push the parameter */
                listParams.addFirst(aeParam);    // AEFunction stores parameters in an opposite way to string pased function.
            }
            if (aeOwner == null) {
                aeReturn = new AEFunction(strName, listParams, progContext);
            } else {
                AEFunction aeFunctionMember = new AEFunction(strName, listParams, progContext);
                aeReturn = new AEObjMember(aeOwner.output(), aeOwner, aeFunctionMember);    // aeOwner hasn't been simplified so we can use its output string.
            }
            /* tell the EveluateExpression function to identify next character. */
            curpos.m_nPos = nCloseBracketPos + 1;
            return aeReturn;
        }
        else if (strExpression.length() == nCurFurtherCharPos
                || (strExpression.length() > nCurFurtherCharPos
                        && (strExpression.charAt(nCurFurtherCharPos) == ',' ||
                        strExpression.charAt(nCurFurtherCharPos) == '[' ||
                        strExpression.charAt(nCurFurtherCharPos) == ')' ||
                        strExpression.charAt(nCurFurtherCharPos) == '.' ||
                        isCalcOperatorChar(strExpression, nCurFurtherCharPos))))
        {
            /*
             * str length <= further char pos means we are at the end of string, this means it
             * is a variable. It may also be a function name. JMAnalyzer will first look up
             * variable name space then look up function name space for it.
             * if str length > further char pos and the further char is ',', it is a parameter
             * which could be either a variable or a function name.
             * if str length > further char pos and the further char is '[', the follows are a
             * data index.
             * if str length > further char pos and the further char is ')', means it is the
             * end of bracketed expression.
             * if str length > further char pos and the further char is ')', means it is the
             * end of bracketed expression.
             * if str length > further char pos and the further char is '.', means it is a
             * class instance followed by a member variable or a member function.
             * if str length > further char pos and the further char is an operator, means it
             * is an operand.
             */
            aeReturn = new AEVar(strName, new LinkedList<AbstractExpr>());    // the variable can be either a defined variable or a variable to be resolved.
            if (aeOwner != null) { // this is a member variable.
                aeReturn = new AEObjMember(aeOwner.output(), aeOwner, aeReturn);    // aeOwner hasn't been simplified so we can use its output string.
                return aeReturn;
            } else {
                if (VariableOperator.lookUpList(strName, listPseudoConsts) != null)    {
                    aeReturn.menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_PSEUDOCONST;    // this is a pseudo-constant.
                }
                curpos.m_nPos = nCurFurtherCharPos;
                return aeReturn;
            }
        }
        else /* neither function nor variable */
        {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS);
        }
    }
    
    /***************************************************************************\
     use boolean ElemAnalyzer.IsStringStartChar(String strExpression, int nCurPos)
    \***************************************************************************/
    
    /***************************************************************************\
     getStringAExpr:
       This function is used to get a string constant AbstractExpr from expression.
       Input:
         String: The expression string.
         CurPos: The initial position of the name in the expression.
       Output:
         An AbstractExpr which is the string constant.
    \
     * @throws JFCALCExpErrException, JSmartMathErrException ***************************************************************************/
    public AbstractExpr getStringAExpr(String strExpression, CurPos curpos) throws JFCALCExpErrException, JSmartMathErrException
    {
        DataClass datumString = getString(strExpression, curpos);
        return new AEConst(datumString);
    }
}
