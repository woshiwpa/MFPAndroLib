// MFP project, PtnSlvVarMultiRootsIdentifier.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.Operators.CalculateOperator;
import com.cyzapps.Jfcalc.DCHelper.CurPos;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassAExpr;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.Operators.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.AbstractExpr.SimplifyParams;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.PatternManager.ABSTRACTEXPRPATTERNS;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class PtnSlvVarMultiRootsIdentifier {
    
    public static final String FUNCTION_ORIGINAL_EXPRESSION = "f_aexpr_to_analyze";
            
    static public class PatternSetByString
    {
        public String mstrPattern = "";
        public String[] mstrarrayPseudoConsts = new String[0];
        public String[] mstrarrayPConExprs = new String[0];
        public String[] mstrarrayPConRestricts = new String[0];
        public String[] mstrarrayPConConditions = new String[0];
    }
    
    static public class PatternSetByAExpr
    {
        public AbstractExpr maePattern = AEInvalid.AEINVALID;
        public AEVar[] maePseudoConsts = new AEVar[0];
        public AbstractExpr[] maePConExprs = new AbstractExpr[0];
    }

    public ABSTRACTEXPRPATTERNS menumAEPType = ABSTRACTEXPRPATTERNS.AEP_UNRECOGNIZEDPATTERN;
    
    public PatternSetByString mpss = new PatternSetByString();
    public String mstrVariable = "";    // variable name
    public String mstrSolveVarExpr = "";    // solved variable expression
    public String mstrRootNumber = "0";    // number of roots.
    protected int mnRootNumber = 0; /// number of roots.
    
    public PatternSetByAExpr mpsa = new PatternSetByAExpr();
    public AEVar maeVariable = new AEVar();
    public AbstractExpr maeSolveVarExpr = new AEInvalid();
    
    public PtnSlvVarMultiRootsIdentifier()    {
        
    }
    
    public PtnSlvVarMultiRootsIdentifier(ABSTRACTEXPRPATTERNS enumAEPType,
                    PatternSetByString pss,
                    String strVariable,
                    String strSolveVarExpr,
                    String strNumOfRoots)
                    throws JSmartMathErrException, JFCALCExpErrException, InterruptedException    {
        setPtnSlvVarMultiRootsIdentifier(enumAEPType, pss, strVariable, strSolveVarExpr, strNumOfRoots);
    }
    
    public void setPtnSlvVarMultiRootsIdentifier(ABSTRACTEXPRPATTERNS enumAEPType,
                    PatternSetByString pss,
                    String strVariable,
                    String strSolveVarExpr,
                    String strNumOfRoots)
                    throws JSmartMathErrException, JFCALCExpErrException, InterruptedException    {
        menumAEPType = enumAEPType;
        
        mpss = pss;
        mstrVariable = strVariable;
        mstrSolveVarExpr = strSolveVarExpr;
        mstrRootNumber = strNumOfRoots;
        
        if (mpss.mstrarrayPConExprs.length != mpss.mstrarrayPseudoConsts.length)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPRPATTERN);
        }
        mpsa.maePConExprs = new AbstractExpr[mpss.mstrarrayPConExprs.length];
        mpsa.maePseudoConsts = new AEVar[mpss.mstrarrayPseudoConsts.length];
        
        LinkedList<Variable> listPseudoConsts = new LinkedList<Variable>();
        for (int idx1 = 0; idx1 < mpss.mstrarrayPseudoConsts.length; idx1 ++)    {
            // after ExprAnalyzer.analyseExpression maearrayPseudoConsts[idx].mstrVariableName must be lower case.
            AbstractExpr aexprTmp = ExprAnalyzer.analyseExpression(mpss.mstrarrayPseudoConsts[idx1], new CurPos());
            if (!(aexprTmp instanceof AEVar))    {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPRPATTERN);
            }
            mpsa.maePseudoConsts[idx1] = (AEVar) aexprTmp;
            mpsa.maePseudoConsts[idx1].menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_PSEUDOCONST;
            if (VariableOperator.lookUpList(
                    ((AEVar)mpsa.maePseudoConsts[idx1]).mstrVariableName,
                    listPseudoConsts) == null)    {
                listPseudoConsts.add(new Variable(((AEVar)mpsa.maePseudoConsts[idx1]).mstrVariableName));
            }
            mpsa.maePConExprs[idx1] = ExprAnalyzer.analyseExpression(mpss.mstrarrayPConExprs[idx1], new CurPos());        
        }
        
        AbstractExpr aexprTmp = ExprAnalyzer.analyseExpression(mstrVariable, new CurPos());
        if (!(aexprTmp instanceof AEVar))    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPRPATTERN);
        }
        maeVariable = (AEVar) aexprTmp;
        maeSolveVarExpr = ExprAnalyzer.analyseExpression(mstrSolveVarExpr, new CurPos());
        mpsa.maePattern = ExprAnalyzer.analyseExpression(mpss.mstrPattern, new CurPos(), listPseudoConsts, new ProgContext());
    }
    
    // length of aePatterns should be equal to length of datumValues. Also, parameter datumValues should not be changed inside the function.
    public static AbstractExpr calcExprValue(AbstractExpr aeOriginalExpr, AbstractExpr[] aeExprUnits, DataClass[] datumValues, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException    {
        LinkedList<PatternExprUnitMap> listFromToMap = new LinkedList<PatternExprUnitMap>();
        for (int idx = 0; idx < aeExprUnits.length; idx ++)    {
            AEConst aeTo = new AEConst(datumValues[idx].cloneSelf());
            PatternExprUnitMap pi = new PatternExprUnitMap(aeExprUnits[idx], aeTo);
            listFromToMap.add(pi);
        }
        AbstractExpr aeProcessedExpr = PatternManager.replaceExprPattern(aeOriginalExpr, listFromToMap, true, progContext);
        return aeProcessedExpr;
    }
    
    /*
     * aeToSolve: the expression to solve pseudo constant, like f_aexpr_to_analyze(1, 2, -i) + f_aexpr_to_analyze(3, 1, 5) - 8;
     * aeOriginalExpr: the expression f_aexpr_to_analyze(...) represents;
     * listPEUMap: includes all the expression units which are looked on as variables and can be patterned.
     */
    public static AbstractExpr replaceExprFunc4PCon(AbstractExpr aeToSolve, AbstractExpr aeOriginalExpr, LinkedList<PatternExprUnitMap> listPEUMap, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
        if (aeToSolve instanceof AEFunction
                && ((AEFunction)aeToSolve).isSameAbsFuncNameWithCS(FUNCTION_ORIGINAL_EXPRESSION, progContext))    {
            if (((AEFunction)aeToSolve).mlistChildren.size() != listPEUMap.size())    {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_NUMBER_OF_VARIABLES_NOT_MATCH);
            }
            AbstractExpr[] aeExprUnits = new AbstractExpr[((AEFunction)aeToSolve).mlistChildren.size()];
            DataClass[] datumValues = new DataClass[((AEFunction)aeToSolve).mlistChildren.size()];
            for (int idx = 0; idx < ((AEFunction)aeToSolve).mlistChildren.size(); idx ++)    {
                // simplify it most to a constant.
                AbstractExpr aexprSimplified = ((AEFunction)aeToSolve).mlistChildren.get(idx).simplifyAExprMost(new LinkedList<UnknownVariable>(), new SimplifyParams(false, true, false), progContext);
                if (!(aexprSimplified instanceof AEConst))    {
                    // currently only support constant parameters for f_aexpr_to_analyze
                    throw new JSmartMathErrException(ERRORTYPES.ERROR_NOT_CONSTANT_ABSTRACTEXPR);
                }
                aeExprUnits[idx] = listPEUMap.get(idx).maeExprUnit;
                datumValues[idx] = ((AEConst) aexprSimplified).getDataClassRef();  // datumValues are only used in calcExprValue and they will not be changed inside the function.
            }
            aeToSolve = calcExprValue(aeOriginalExpr, aeExprUnits, datumValues, progContext);
        } else    {
            LinkedList<PatternExprUnitMap> listFromToMap = new LinkedList<PatternExprUnitMap>();
            LinkedList<AbstractExpr> listChildren = aeToSolve.getListOfChildren();
            for (int idx = 0; idx < listChildren.size(); idx ++)    {
                // does not matter if two children are the same. We just replace them together.
                PatternExprUnitMap pi = new PatternExprUnitMap(listChildren.get(idx),
                        replaceExprFunc4PCon(listChildren.get(idx), aeOriginalExpr, listPEUMap, progContext));
                listFromToMap.add(pi);
            }
            aeToSolve = aeToSolve.replaceChildren(listFromToMap, true, new LinkedList<AbstractExpr>(), progContext);
        }
        return aeToSolve;
    }
    
    public static boolean canSingleExprMatchBackPattern(AbstractExpr aeOriginalExpr,
                                                AbstractExpr aePatternExpr,
                                                LinkedList<UnknownVariable> listPseudoConstVars,    // pseudo constants name and value list of pattern
                                                LinkedList<PatternExprUnitMap> listPEUMap,
                                                LinkedList<UnknownVariable> listUnknown,    // unknown list of original expr
                                                ProgContext progContext)    // unknown list of original expr + name spaces of original expr
                                throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
        // unknown2pattern is the unknown variable list specifically for pattern
        LinkedList<UnknownVariable> listUnknown2Pattern = new LinkedList<UnknownVariable>();
        listUnknown2Pattern.addAll(listUnknown);

        // have to convert aepatternExpr instead of aeOriginalExpr coz aeOriginalExpr may includes
        // some aexpr data which includes var's name some as pattern variable name. This will lead
        // to confusion later on.
        //aeOriginalExpr = PatternManager.replaceExprPattern(aeOriginalExpr, listPEUMap, true);
        aePatternExpr = PatternManager.replaceExprPattern(aePatternExpr, listPEUMap, false, progContext);
        // first, aeOriginalExpr seems to be simplified before so no need to simplify it further. Second, even if it is not simplified, we can still use it. 
        // pseudo const vars should be added in unknown var list coz aexpr might be simplified b4 pseudo const has value.
        listUnknown2Pattern.addAll(listPseudoConstVars);
        aePatternExpr = aePatternExpr.simplifyAExprMost(listUnknown2Pattern, new SimplifyParams(false, true, false), progContext);
        
        // simplify aeOriginalExpr - aePattern to see if it is zero or not.
        LinkedList<AbstractExpr> listAEChildren = new LinkedList<AbstractExpr>();
        listAEChildren.add(aeOriginalExpr);
        listAEChildren.add(aePatternExpr);
        LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));        
        AbstractExpr aeOrginalMinusPattern = new AEPosNegOpt(listAEChildren, listOpts);
        aeOrginalMinusPattern = aeOrginalMinusPattern.simplifyAExprMost(listUnknown, new SimplifyParams(false, false, false), progContext);// WHEN match back, need to simplify aexpr datum.
        if (aeOrginalMinusPattern.isNegligible(progContext))    {
            return true;
        } else    {
            return false;
        }
    }

    public boolean isPatternMatch(AbstractExpr aeOriginalExpr,  // original expr hasn't been mostly simplifed coz need first call replace pattern then simplify most.
                                LinkedList<AbstractExpr> listOriginalExprVars,
                                LinkedList<PatternExprUnitMap> listPEUMap,    // the map of unknown variables to pattern units
                                LinkedList<UnknownVariable> listPseudoConstVars,    // this list returns pseudo consts used in this pattern identifier.
                                LinkedList<UnknownVariable> listUnknown,
                                ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException
    {
        AbstractExpr aePattern = mpsa.maePattern;
        
        LinkedList<AbstractExpr> listPatternVars = new LinkedList<AbstractExpr>();
        PtnSlvMultiVarsIdentifier.lookupToSolveVariables(aePattern, listPatternVars, new LinkedList<AbstractExpr>(), progContext);
        
        if (listOriginalExprVars.size() != listPatternVars.size())    {
            return false;    // original expression has not got same number of unknown variables as pattern
        } else    {
            listPEUMap.clear();
            for (int idx = 0; idx < listOriginalExprVars.size(); idx ++)    {
                listPEUMap.add(new PatternExprUnitMap(listOriginalExprVars.get(idx), listPatternVars.get(idx)));
            }
        }
        
        LinkedList<UnknownVariable> listLocalPConVars = new LinkedList<UnknownVariable>();
        boolean bPConValuesGot = true;
        for (int idx2 = 0; idx2 < mpsa.maePConExprs.length; idx2 ++)    {
            AbstractExpr aePConToSolve = replaceExprFunc4PCon(mpsa.maePConExprs[idx2], aeOriginalExpr, listPEUMap, progContext);
            try {
                aePConToSolve = aePConToSolve.simplifyAExprMost(listUnknown, new SimplifyParams(false, false, false), progContext);  // should be false here to simplfiy datum expr.
            } catch (JFCALCExpErrException e)  {
                // it is possible that some parameters have incompatiable value type with the function.
                // e.g. when try to match sind(x) to a polynomial, it may need to calculate sind(i) which
                // is invalid because i cannot be parameter of sind.
                return false;
            }
            if (!(aePConToSolve instanceof AEConst)) {  // some function cannot be simplified to AEConst data expr.
                aePConToSolve = new AEConst(new DataClassAExpr(aePConToSolve));
            }
            
            UnknownVariable varPCon = UnknownVarOperator.lookUpList(((AEVar)mpsa.maePseudoConsts[idx2]).mstrVariableName, listPseudoConstVars);
            if (varPCon == null)    {
                listLocalPConVars.add(new UnknownVariable(
                                    ((AEVar)mpsa.maePseudoConsts[idx2]).mstrVariableName,
                                    ((AEConst)aePConToSolve).getDataClass()));  // getDataClass will automatically determine deep copy or not.
            } else if (((AEConst)aePConToSolve).getDataClassRef().isEqual(varPCon.getSolvedValue()) == false)    {
                bPConValuesGot = false;
                break;
            }
        }
        if (!bPConValuesGot)    {
            return false;   // some of the pseudo const values cannot be obtained.
        }
        LinkedList<LinkedList<Variable>> lVarSpaces = new LinkedList<LinkedList<Variable>>();
        LinkedList<Variable> listKnownVars = new LinkedList<Variable>();
        lVarSpaces.add(listKnownVars);
        listKnownVars.addAll(listLocalPConVars);
        ProgContext progContextTmp = new ProgContext();
        progContextTmp.mdynamicProgContext.mlVarNameSpaces = lVarSpaces;
        progContextTmp.mstaticProgContext.setCitingSpacesExplicitly(progContext.mstaticProgContext.getCitingSpaces());
        ExprEvaluator exprEvaluator = new ExprEvaluator(progContextTmp);
        for (int idx2 = 0; idx2 < mpss.mstrarrayPConRestricts.length; idx2 ++)    {
            DataClass datumRestrictResult = exprEvaluator.evaluateExpression(mpss.mstrarrayPConRestricts[idx2], new CurPos());
            if (DCHelper.isDataClassType(datumRestrictResult, DATATYPES.DATUM_ABSTRACT_EXPR)) {
                // datumRestrictResult must be a DataClassBuiltIn
                AbstractExpr aexpr = DCHelper.lightCvtOrRetDCAExpr(datumRestrictResult).getAExpr();
                aexpr = aexpr.simplifyAExprMost(listUnknown, new SimplifyParams(false, true, false), progContext);
                if (aexpr instanceof AEConst) {
                    datumRestrictResult = ((AEConst)aexpr).getDataClassRef();
                } else {
                    return false; // if result of restrict is an expression, we assume it cannot fit.
                }
            }
            if (!DCHelper.isDataClassType(datumRestrictResult, DATATYPES.DATUM_ABSTRACT_EXPR)) {
                // ok, datumRestrictResult is DataClassBuiltIn now.
                if (DCHelper.lightCvtOrRetDCMFPBool(datumRestrictResult).getDataValue().isActuallyZero()) {   // restrict does not fit.
                    return false;
                }
            } else {
                return false; // if result of restrict is an expression, we assume it cannot fit.
            }
        }
        
        for (int idx2 = 0; idx2 < mpss.mstrarrayPConConditions.length; idx2 ++)    {
            DataClass datumConditionResult = exprEvaluator.evaluateExpression(mpss.mstrarrayPConConditions[idx2], new CurPos());
            if (DCHelper.isDataClassType(datumConditionResult, DATATYPES.DATUM_ABSTRACT_EXPR)) {
                // datumConditionResult must be DataClassBuiltIn
                AbstractExpr aexpr = DCHelper.lightCvtOrRetDCAExpr(datumConditionResult).getAExpr();
                aexpr = aexpr.simplifyAExprMost(listUnknown, new SimplifyParams(false, true, false), progContext);
                if (aexpr instanceof AEConst) {
                    datumConditionResult = ((AEConst)aexpr).getDataClassRef();
                }
            }
            if (!DCHelper.isDataClassType(datumConditionResult, DATATYPES.DATUM_ABSTRACT_EXPR)) {
                // datumConditionResult must be DataClassBuiltIn now
                if (DCHelper.lightCvtOrRetDCMFPBool(datumConditionResult).getDataValue().isActuallyZero()) {   // condition does not fit.
                    return false;
                }
            }
        }
        
        mnRootNumber = -1;
        DataClass datumNumOfRoots = exprEvaluator.evaluateExpression(mstrRootNumber, new CurPos());
        if (DCHelper.isDataClassType(datumNumOfRoots, DATATYPES.DATUM_ABSTRACT_EXPR)) {
            // datumNumOfRoots is DataClassBuiltIn
            AbstractExpr aexpr = DCHelper.lightCvtOrRetDCAExpr(datumNumOfRoots).getAExpr();
            aexpr = aexpr.simplifyAExprMost(listLocalPConVars, new SimplifyParams(false, true, false), progContextTmp);
            if (aexpr instanceof AEConst) {
                datumNumOfRoots = ((AEConst)aexpr).getDataClassRef();
            }
        }
        if (!DCHelper.isDataClassType(datumNumOfRoots, DATATYPES.DATUM_ABSTRACT_EXPR)) {
            // now datumNumOfRoots is DataClassBuiltIn
            if (DCHelper.lightCvtOrRetDCMFPInt(datumNumOfRoots).getDataValue().isActuallyPositive()) {   // no result.
                mnRootNumber = DCHelper.lightCvtOrRetDCMFPInt(datumNumOfRoots).getDataValue().intValue();
            }
        }
        if (mnRootNumber < 1) {
            return false;   // no root or number of roots is not sure.
        }
        
        // all the pseudo constant values obtained without problem, now verify back
        boolean bcanSingleExprMatchBackPattern = canSingleExprMatchBackPattern(aeOriginalExpr, aePattern,
                                                                listLocalPConVars, listPEUMap, listUnknown, progContext);
        if (bcanSingleExprMatchBackPattern)    {
            // match! Now add all variables in the variable list only after we believe the single expr matches the pattern.
            for (int idx3 = 0; idx3 < listLocalPConVars.size(); idx3 ++)    {
                // pseudo constants should have been solved anyway
                DataClass datumValuePCon = listLocalPConVars.get(idx3).getSolvedValue();
                if (DCHelper.isDataClassType(datumValuePCon, DATATYPES.DATUM_ABSTRACT_EXPR)) {
                    // datumValuePCon must be DataClassBuiltIn
                    AbstractExpr aexprData = DCHelper.lightCvtOrRetDCAExpr(datumValuePCon).getAExpr().simplifyAExprMost(listUnknown, new SimplifyParams(false, true, false), progContext);
                    if (aexprData instanceof AEConst && !DCHelper.isDataClassType(((AEConst)aexprData).getDataClassRef(), DATATYPES.DATUM_ABSTRACT_EXPR)) {
                        datumValuePCon = ((AEConst)aexprData).getDataClassRef().copySelf();
                    } else {
                        datumValuePCon = new DataClassAExpr(aexprData);
                    }
                    listLocalPConVars.get(idx3).setValue(datumValuePCon);
                }
                // if a pseudo constant is very very close to zero. We believe it is zero otherwise,
                // we may see more roots than we expect, considering a situation 0.000000000000000000000000000000000001 * x**6 + x+ 3 == 0,
                // where x**6 should be multiplied by zero.
                if (DCHelper.isZeros(datumValuePCon, false))    {
                    if (datumValuePCon.getThisOrNull() instanceof DataClassArray) {
                        DataClass datumZero = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                        DataClassArray datumValuePConArray = DCHelper.lightCvtOrRetDCArray(datumValuePCon);
                        datumValuePConArray.setArrayAllLeafChildren(datumZero);
                    }
                    listLocalPConVars.get(idx3).setValue(datumValuePCon);
                }
            }
            listPseudoConstVars.addAll(listLocalPConVars);
            return true;
        }
        return false;
    }
    
    public DataClass[] solveOriginalExprUnit(PatternExprUnitMap peuMap, LinkedList<UnknownVariable> listPseudoConstVars, ProgContext progContext)
            throws JFCALCExpErrException, InterruptedException, JSmartMathErrException    {
        AbstractExpr aePatternUnit = peuMap.maePatternUnit;
        if (maeVariable.isEqual(aePatternUnit, progContext))    {
            // find!
            AbstractExpr aeReturn = maeSolveVarExpr.simplifyAExprMost(listPseudoConstVars, new SimplifyParams(false, true, false), progContext);
            if (!(aeReturn instanceof AEConst))    {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_CANNOT_BE_SOLVED);
            }
            DataClass[] datumValueList;
            if (mnRootNumber > 1) {
                if (((AEConst)aeReturn).getDataClassRef().getThisOrNull() instanceof DataClassArray)    {
                    datumValueList = DCHelper.lightCvtOrRetDCArray(((AEConst)aeReturn).getDataClassRef()).getDataList();   // because it is a matrix, so need not to call getDataClass
                } else if (DCHelper.isDataClassType(((AEConst)aeReturn).getDataClassRef(), DATATYPES.DATUM_ABSTRACT_EXPR))    {    // for situation that data type is aexpr
                    datumValueList = new DataClass[mnRootNumber];
                    for (int idx = 0; idx < mnRootNumber; idx ++) {
                        AbstractExpr aexpr = DCHelper.lightCvtOrRetDCAExpr(((AEConst)aeReturn).getDataClassRef()).getAExpr();
                        DataClass datumIndexValue = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(idx));
                        DataClass[] arrayIdx = new DataClass[1];
                        arrayIdx[0] = datumIndexValue;
                        DataClass datumIdx = new DataClassArray(arrayIdx);
                        AEIndex aeIndexValue = new AEIndex(aexpr, new AEConst(datumIdx));
                        datumValueList[idx] = new DataClassAExpr(aeIndexValue);
                    }
                } else {
                    throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_RESULT);
                }
            } else    {
                datumValueList = new DataClass[1];
                datumValueList[0] = ((AEConst)aeReturn).getDataClass(); // getDataClass will automatically determine when to deep copy.
            }
            return datumValueList;
        }
        throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_CANNOT_BE_SOLVED);
    }
}
