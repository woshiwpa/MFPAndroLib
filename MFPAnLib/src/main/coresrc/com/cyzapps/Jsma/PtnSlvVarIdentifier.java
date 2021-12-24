/*
 * MFP project, PtnSlvVarIdentifier.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jsma;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DCHelper.CurPos;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.SimplifyParams;
import com.cyzapps.Jsma.PatternManager.ABSTRACTEXPRPATTERNS;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import com.cyzapps.adapter.FunctionNameMapper;
import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public class PtnSlvVarIdentifier {
    // this class is for some very simple patterns. Assume these patterns have only
    // one expression, one variable and only one or two pseudo-constants.
    
       public static final String FUNCTION_SINGLE_VAR_INVERTIBLE = "f_single_var_invertible";
       public static final String FUNCTION_SINGLE_VAR_INVERTED = "f_single_var_inverted";

    static public boolean isSingleVarInvertibleFunc(String strFuncNameWithCS) { // small case full name with CS
        if (null != FunctionNameMapper.msmapSysFuncInvertMap.get(strFuncNameWithCS))   {
            return true;
        }
        return false;
    }
    
    static public AbstractExpr getInvertedAExpr(AEFunction aeInvertible, AbstractExpr aeParameter, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException {
        AbstractExpr aeReturn = new AEFunction();
        if (aeInvertible.getAbsFuncNameWithCS(progContext).length() == 0
                || aeInvertible.mlistChildren.size() != 1) {
            // it has been checked. So no need to check again.
            // throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
            return aeReturn;
        }
        LinkedList<AbstractExpr> listParameter = new LinkedList<AbstractExpr>();
        listParameter.add(aeParameter);
        String strInverted = FunctionNameMapper.msmapSysFuncInvertMap.get(aeInvertible.getAbsFuncNameWithCS(progContext));   // get inverted function.
        if (strInverted == null) {
            // not a inverted function.
            return aeReturn;
        } else if (strInverted.length() > 0 && strInverted.charAt(0) != '~') {
            //this means we have found the inverted function in msmapSysFuncIntertMap.
            // we use default citing spaces because all the invertible functions are
            // sys defined not user defined.
            aeReturn = new AEFunction(strInverted, listParameter, progContext);
        } else if (strInverted.equals("~log10")) {
            AEConst aeTen = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TEN));
            aeReturn = new AEPowerOpt(aeTen, aeParameter);
        } else if (strInverted.equals("~log2")) {
            AEConst aeTwo = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TWO));
            aeReturn = new AEPowerOpt(aeTwo, aeParameter);
        } else if (strInverted.equals("sqrt")) {
            AEConst aeTwo = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TWO));
            aeReturn = new AEPowerOpt(aeParameter, aeTwo);
        }
        return aeReturn;
    }
        
    public ABSTRACTEXPRPATTERNS menumAEPType = ABSTRACTEXPRPATTERNS.AEP_UNRECOGNIZEDPATTERN;
    public String mstrPattern = "";
    public String[] mstrarrayPseudoConsts = new String[0];
    public String[] mstrarrayPCRestricts = new String[0];    //restricts of pseudo constant, if not match, throw exception
    public String[] mstrarrayPCConditions = new String[0];    //conditions of pseudo constant, if not match, return false
    public String mstrToSolve = "";
    public String mstrSolveVarExpr = "";
    
    public AbstractExpr maePattern = AEInvalid.AEINVALID;
    public AEVar[] maearrayPseudoConsts = new AEVar[0];
    public AEVar maeToSolve = new AEVar();
    public AbstractExpr maeSolveVarExpr = AEInvalid.AEINVALID;
    
    public PtnSlvVarIdentifier() {
        
    }
    
    public PtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS enumAEPType, String strPattern, String[] strarrayPseudoConsts,
            String[] strarrayPCRestricts, String[] strarrayPCConditions, String strToSolve, String strSolveVarExpr)
            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        setPtnSlvVarIdentifier(enumAEPType, strPattern, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, strToSolve, strSolveVarExpr);
    }
    
    public void setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS enumAEPType, String strPattern, String[] strarrayPseudoConsts,
            String[] strarrayPCRestricts, String[] strarrayPCConditions, String strToSolve, String strSolveVarExpr)
            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        menumAEPType = enumAEPType;
        mstrPattern = strPattern;
        mstrarrayPseudoConsts = strarrayPseudoConsts;
        mstrarrayPCRestricts = strarrayPCRestricts;
        mstrarrayPCConditions = strarrayPCConditions;
        mstrToSolve = strToSolve;
        mstrSolveVarExpr = strSolveVarExpr;
        maearrayPseudoConsts = new AEVar[mstrarrayPseudoConsts.length];
        LinkedList<UnknownVariable> listUnknowns = new LinkedList<UnknownVariable>();
        LinkedList<Variable> listPseudoConsts = new LinkedList<Variable>();
        for (int idx = 0; idx < mstrarrayPseudoConsts.length; idx ++)   {
            // after ExprAnalyzer.analyseExpression maearrayPseudoConsts[idx].mstrVariableName must be lower case.
            maearrayPseudoConsts[idx] = (AEVar) ExprAnalyzer.analyseExpression(mstrarrayPseudoConsts[idx], new CurPos());
            UnknownVariable var = UnknownVarOperator.lookUpList(maearrayPseudoConsts[idx].mstrVariableName, listUnknowns);
            if (var != null)    {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_UNKNOWN_VARIABLE_REDECLARED);
            }
            listUnknowns.add(new UnknownVariable(maearrayPseudoConsts[idx].mstrVariableName));
            listPseudoConsts.add(new Variable(maearrayPseudoConsts[idx].mstrVariableName));
        }
        maePattern = ExprAnalyzer.analyseExpression(mstrPattern, new CurPos(), listPseudoConsts, new ProgContext());    
        maeToSolve = (AEVar) ExprAnalyzer.analyseExpression(mstrToSolve, new CurPos()); // it must be a variable, so no need to add listPseudoConsts.
        UnknownVariable var = UnknownVarOperator.lookUpList(maeToSolve.mstrVariableName, listUnknowns);
        if (var != null)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_UNKNOWN_VARIABLE_REDECLARED);
        }
        listUnknowns.add(new UnknownVariable(maeToSolve.mstrVariableName));
        
        // simplify most so that can easily compare.
        maePattern = maePattern.simplifyAExprMost(listUnknowns, new SimplifyParams(false, true, false), new ProgContext());
        
        maeSolveVarExpr = ExprAnalyzer.analyseExpression(mstrSolveVarExpr, new CurPos());
    }

    public boolean isPatternMatch(AbstractExpr aeOriginalExpr,  // need not to call replace pattern. Assume it has been simplified most.
                                PatternExprUnitMap peuMap,    // this variable is used to return the map of unknown variable to pattern unit
                                DataClass[] datumarrayValueOfUnknown,    // the data value which is used to return value of unknown variable. This is a 1-elem array.
                                LinkedList<UnknownVariable> listUnknown,    // listUnknown and lVarNameSpaces are original expression's unknown list and namespace, 
                                                                            // they are not solved result's unknown list and name space (which only include psconsts
                                                                            // defined patterns
                                ProgContext progContext)  throws JFCALCExpErrException, JSmartMathErrException, InterruptedException
            // listUnknown and lVarNameSpaces in progContext are original expression's unknown list and namespace, 
            // they are not solved result's unknown list and name space (which only include psconsts
            // defined patterns
    {
        LinkedList<PatternExprUnitMap> listpeuMapPseudoFuncs = new LinkedList<PatternExprUnitMap>();
        LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts = new LinkedList<PatternExprUnitMap>();
        LinkedList<PatternExprUnitMap> listpeuMapUnknowns = new LinkedList<PatternExprUnitMap>();
        // do not allow conversion when matching pattern
        if (aeOriginalExpr.isPatternMatch(maePattern, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, false, progContext))   {
            // get the pattern! Assume there are at most one member in listpeuMapPseudoFuncs which is FUNCTION_SINGLE_VAR_INVERTIBLE
            // also assume there is only one unknown var in listpeuMapUnknowns.
            if (listpeuMapPseudoFuncs.size() > 1 || listpeuMapUnknowns.size() != 1)    {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_UNSUPPORTED_SIMPLE_PATTERN);
            }
            LinkedList<LinkedList<Variable>> lVarSpaces = new LinkedList<LinkedList<Variable>>();
            LinkedList<Variable> listKnownVars = new LinkedList<Variable>();
            lVarSpaces.add(listKnownVars);  // here name space and unknown list are psconst's name space and unknown list.
            for (int idx = 0; idx < listpeuMapPseudoConsts.size(); idx ++)  {
                DataClass datumValue = new DataClassNull();
                if (listpeuMapPseudoConsts.get(idx).maeExprUnit instanceof AEConst) {
                    datumValue = ((AEConst)listpeuMapPseudoConsts.get(idx).maeExprUnit).getDataClass(); // getDataClass will automatically determine when to deep copy.
                    Variable var = new Variable(((AEVar)listpeuMapPseudoConsts.get(idx).maePatternUnit).mstrVariableName,
                                                datumValue);
                    listKnownVars.add(var); // listpeuMapPseudoConsts does not include any duplicated pseudo-consts.
                } else  {
                    throw new JSmartMathErrException(ERRORTYPES.ERROR_UNSUPPORTED_SIMPLE_PATTERN);
                }
            }
            ProgContext progContextTmp = new ProgContext();
            progContextTmp.mdynamicProgContext.mlVarNameSpaces = lVarSpaces;
            progContextTmp.mstaticProgContext.setCitingSpacesExplicitly(progContext.mstaticProgContext.getCitingSpaces());
            for (int idx = 0; idx < mstrarrayPCRestricts.length; idx ++)   {
                ExprEvaluator exprEvaluator = new ExprEvaluator(progContextTmp);
                DataClass datumRestrictResult = exprEvaluator.evaluateExpression(mstrarrayPCRestricts[idx], new CurPos());
                if (DCHelper.isDataClassType(datumRestrictResult, DATATYPES.DATUM_ABSTRACT_EXPR)) {
                    // datumRestrictResult must be a DataClassBuiltIn instance.
                    AbstractExpr aexpr = DCHelper.lightCvtOrRetDCAExpr(datumRestrictResult).getAExpr();
                    aexpr = aexpr.simplifyAExprMost(listUnknown, new SimplifyParams(false, true, false), progContext);
                    if (aexpr instanceof AEConst) {
                        datumRestrictResult = ((AEConst)aexpr).getDataClassRef();
                    } else {
                        return false; // if result of restrict is an expression, we assume it cannot fit.
                    }
                }
                if (!DCHelper.isDataClassType(datumRestrictResult, DATATYPES.DATUM_ABSTRACT_EXPR)) {
                    DataClassSingleNum datumRestrictResultBool = DCHelper.lightCvtOrRetDCMFPBool(datumRestrictResult);
                    // now datumRestrictResult is DataClassBuiltIn
                    if (datumRestrictResultBool.getDataValue().isActuallyZero()) {   // restrict does not fit.
                        return false;
                    }
                } else {
                    return false; // if result of restrict is an expression, we assume it cannot fit.
                }
            }
            for (int idx = 0; idx < mstrarrayPCConditions.length; idx ++)   {
                ExprEvaluator exprEvaluator = new ExprEvaluator(progContextTmp);
                DataClass datumConditionResult = exprEvaluator.evaluateExpression(mstrarrayPCConditions[idx], new CurPos());
                if (DCHelper.isDataClassType(datumConditionResult, DATATYPES.DATUM_ABSTRACT_EXPR)) {
                    // datumConditionResult is DataClassBuiltIn now
                    AbstractExpr aexpr = DCHelper.lightCvtOrRetDCAExpr(datumConditionResult).getAExpr();
                    aexpr = aexpr.simplifyAExprMost(listUnknown, new SimplifyParams(false, true, false), progContext);
                    if (aexpr instanceof AEConst) {
                        datumConditionResult = ((AEConst)aexpr).getDataClassRef();
                    }
                }
                if (!DCHelper.isDataClassType(datumConditionResult, DATATYPES.DATUM_ABSTRACT_EXPR)) {
                    DataClassSingleNum datumConditionResultBool = DCHelper.lightCvtOrRetDCMFPBool(datumConditionResult);
                    // datumConditionResult is DataClassBuiltIn now
                    if (datumConditionResultBool.getDataValue().isActuallyZero()) {   // condition does not fit.
                        return false;
                    }
                }
            }
            peuMap.maeExprUnit = listpeuMapUnknowns.get(0).maeExprUnit;
            peuMap.maePatternUnit = listpeuMapUnknowns.get(0).maePatternUnit;
            AbstractExpr aeToSimplifyMost = maeSolveVarExpr;
            if (menumAEPType == ABSTRACTEXPRPATTERNS.AEP_SIMPLEINVFUNCZERO
                    || menumAEPType == ABSTRACTEXPRPATTERNS.AEP_SIMPLEINVFUNCADD
                    || menumAEPType == ABSTRACTEXPRPATTERNS.AEP_SIMPLEINVFUNCSUB)   {
                if (aeToSimplifyMost instanceof AEFunction)  {
                    if (((AEFunction)aeToSimplifyMost).mlistChildren.size() != 1)   {
                        throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_SIMPLE_PATTERN);
                    }
                    // function name of aeToSimplifyMost has been validated in AEFunction.isPatternMatch
                    // and must be one of the invertible functions.
                    aeToSimplifyMost = getInvertedAExpr((AEFunction)listpeuMapPseudoFuncs.get(0).maeExprUnit,
                            ((AEFunction)aeToSimplifyMost).mlistChildren.get(0), progContext);
                } else  {
                    throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_SIMPLE_PATTERN);
                }
            } 
            // consider a situation aeToSimplifyMost = a where a is a psconst = something like a + 1 where a is an variable,
            // First a and second a are in different name space, have to simplify the expression step by step.
            aeToSimplifyMost = aeToSimplifyMost.simplifyAExprMost(new LinkedList<UnknownVariable>(), new SimplifyParams(false, true, false), progContextTmp);
            AbstractExpr aeReturn = aeToSimplifyMost.simplifyAExprMost(listUnknown, new SimplifyParams(false, false, false), progContext);
            if (aeReturn instanceof AEConst)    {
                datumarrayValueOfUnknown[0] = ((AEConst)aeReturn).getDataClass().copySelf();  // getDataClass will determine whether to deep copy or not.
                return true;
            } else  {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_UNSUPPORTED_SIMPLE_PATTERN);
            }
        }
        return false;
    }
}
