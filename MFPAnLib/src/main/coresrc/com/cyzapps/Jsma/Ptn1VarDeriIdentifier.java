/*
 * MFP project, Ptn1VarDeriIdentifier.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jsma;

import com.cyzapps.Jfcalc.DCHelper.CurPos;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.SimplifyParams;
import com.cyzapps.Jsma.PatternManager.ABSTRACTEXPRPATTERNS;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public class Ptn1VarDeriIdentifier {
    // this class is for normal expression patterns. Assume these patterns have only
    // one expression, one variable and limited number of pseudo-constants.
    public ABSTRACTEXPRPATTERNS menumAEPType = ABSTRACTEXPRPATTERNS.AEP_UNRECOGNIZEDPATTERN;
    public String[] mstrarrayPatterns = new String[0]; // if there are several different way to express, then use alternative patterns, like sqrt(x) can be written as x**0.5, a+b*x can be written as a+x when b = 1
    public String[] mstrarrayPatternConditions = new String[0];
    public String mstrVariable = "";
    
    public String mstrDerivative = "";
    
    public AbstractExpr[] maearrayPatterns = new AbstractExpr[0];
    public AbstractExpr[] maearrayPatternConds = new AbstractExpr[0];
    public AEVar maeVariable = new AEVar();
    
    public AbstractExpr maeDerivative = new AEInvalid();
    
    public Ptn1VarDeriIdentifier() {
        
    }
    
    public Ptn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS enumAEPType, String[] strarrayPatterns, String[] strarrayPatternConditions, String strVariable, String strDerivative)
            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        setPtn1VarDeriIdentifier(enumAEPType, strarrayPatterns, strarrayPatternConditions, strVariable, strDerivative);
    }
    
    public final void setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS enumAEPType, String[] strarrayPatterns,
            String[] strarrayPatternConditions, String strVariable, String strDerivative)
            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        menumAEPType = enumAEPType;
        mstrarrayPatterns = strarrayPatterns;
        mstrarrayPatternConditions = strarrayPatternConditions;
        mstrVariable = strVariable;
        mstrDerivative = strDerivative;
        maearrayPatterns = new AbstractExpr[mstrarrayPatterns.length];
        for (int idx = 0; idx < maearrayPatterns.length; idx ++) {
            maearrayPatterns[idx] = ExprAnalyzer.analyseExpression(mstrarrayPatterns[idx], new CurPos(), new LinkedList<Variable>(), new ProgContext());
        }
        maeVariable = (AEVar) ExprAnalyzer.analyseExpression(mstrVariable, new CurPos()); // it must be a variable, so no need to add listPseudoConsts.
        LinkedList<UnknownVariable> listUnknowns = new LinkedList<UnknownVariable>();
        listUnknowns.add(new UnknownVariable(maeVariable.mstrVariableName));
        ProgContext progContext = new ProgContext();
        // simplify most so that can easily compare.
        for (int idx = 0; idx < maearrayPatterns.length; idx ++) {
            maearrayPatterns[idx] = maearrayPatterns[idx].simplifyAExprMost(listUnknowns, new SimplifyParams(true, true, true), progContext);  // treat all unknowns as single value
        }
        maeDerivative = ExprAnalyzer.analyseExpression(strDerivative, new CurPos(), new LinkedList<Variable>(), progContext);
    }
}
