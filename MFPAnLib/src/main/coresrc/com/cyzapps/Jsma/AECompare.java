// MFP project, AECompare.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.Operators.CalculateOperator;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.Operators.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.ModuleInfo;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AECompare extends AbstractExpr {

	private static final long serialVersionUID = 1L;
	public AbstractExpr maeLeft = AEInvalid.AEINVALID;
    public AbstractExpr maeRight = AEInvalid.AEINVALID;
    public OPERATORTYPES moptType = OPERATORTYPES.OPERATOR_EQ;    // by default it is ==.
    
    public AECompare() {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_COMPARE;
        maeLeft = AEInvalid.AEINVALID;
        maeRight = AEInvalid.AEINVALID;
        moptType = OPERATORTYPES.OPERATOR_EQ;    // by default it is ==.
    }
    
    public AECompare(AbstractExpr aeLeft, OPERATORTYPES optType, AbstractExpr aeRight) throws JSmartMathErrException    {
        setAECompare(aeLeft, optType, aeRight);
    }

    public AECompare(AbstractExpr aeLeft, CalculateOperator opt, AbstractExpr aeRight) throws JSmartMathErrException    {
        setAECompare(aeLeft, opt.getOperatorType(), aeRight);
    }

    public AECompare(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException    {
        copy(aexprOrigin);
    }

    @Override
    public void validateAbstractExpr() throws JSmartMathErrException    {
        if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_COMPARE)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
        }
        
        if (moptType != OPERATORTYPES.OPERATOR_EQ && moptType != OPERATORTYPES.OPERATOR_NEQ
                    && moptType != OPERATORTYPES.OPERATOR_LARGER
                    && moptType != OPERATORTYPES.OPERATOR_SMALLER
                    && moptType != OPERATORTYPES.OPERATOR_NOLARGER
                    && moptType != OPERATORTYPES.OPERATOR_NOSMALLER)    {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);                
        }
    }
    
    private void setAECompare(AbstractExpr aeLeft, OPERATORTYPES optType, AbstractExpr aeRight) throws JSmartMathErrException    {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_COMPARE;
        maeLeft = aeLeft;
        moptType = optType;
        maeRight = aeRight;
        validateAbstractExpr();
    }
    
    @Override
    protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
            JSmartMathErrException {
        ((AECompare)aexprOrigin).validateAbstractExpr();
        super.copy(aexprOrigin);
        maeLeft = ((AECompare)aexprOrigin).maeLeft;
        maeRight = ((AECompare)aexprOrigin).maeRight;
        moptType = ((AECompare)aexprOrigin).moptType;
    }

    @Override
    protected void copyDeep(AbstractExpr aexprOrigin)
            throws JFCALCExpErrException, JSmartMathErrException {
        ((AECompare)aexprOrigin).validateAbstractExpr();
        super.copyDeep(aexprOrigin);
        maeLeft = ((AECompare)aexprOrigin).maeLeft.cloneSelf();
        maeRight = ((AECompare)aexprOrigin).maeRight.cloneSelf();
        moptType = ((AECompare)aexprOrigin).moptType;
    }

    @Override
    public AbstractExpr cloneSelf() throws JFCALCExpErrException,
            JSmartMathErrException {
        AbstractExpr aeReturn = new AECompare();
        aeReturn.copyDeep(this);
        return aeReturn;
    }

    @Override
    public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();
        // should always return true or false;
        return new int[0];
    }

    // note that progContext can be null. If null, it is unconditional equal.
    @Override
    public boolean isEqual(AbstractExpr aexpr, ProgContext progContext) throws JFCALCExpErrException {
        if (menumAEType != aexpr.menumAEType)    {
            return false;
        } else if (moptType != ((AECompare)aexpr).moptType)    {
            return false;
        } else if (!maeLeft.isEqual(((AECompare)aexpr).maeLeft, progContext))    {
            return false;
        } else if (!maeRight.isEqual(((AECompare)aexpr).maeRight, progContext))    {
            return false;
        } else    {
            return true;
        }
    }
    
    @Override
    public int getHashCode()  throws JFCALCExpErrException {
        int hashRet = menumAEType.hashCode()
                + moptType.hashCode() * 13
                + maeLeft.getHashCode() * 17 + maeRight.getHashCode() * 19;
        return hashRet;
    }

    @Override
    public boolean isPatternMatch(AbstractExpr aePattern,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoFuncs,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts,
                                LinkedList<PatternExprUnitMap> listpeuMapUnknowns,
                                boolean bAllowConversion, ProgContext progContext)  throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
        /* do not call isPatternDegrade function because generally compare expression cannot degrade-match a pattern.*/
        if (aePattern.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE)   {
            // unknown variable
            for (int idx = 0; idx < listpeuMapUnknowns.size(); idx ++)  {
                if (listpeuMapUnknowns.get(idx).maePatternUnit.isEqual(aePattern, progContext))    {
                    if (isEqual(listpeuMapUnknowns.get(idx).maeExprUnit, progContext))   {
                        // this unknown variable has been mapped to an expression and the expression is the same as this
                        return true;
                    } else  {
                        // this unknown variable has been mapped to an expression but the expression is not the same as this
                        return false;
                    }
                }
            }
            // the aePattern is an unknown variable and it hasn't been mapped to some expressions before.
            PatternExprUnitMap peuMap = new PatternExprUnitMap(this, aePattern);
            listpeuMapUnknowns.add(peuMap);
            return true;
        }
        if (!(aePattern instanceof AECompare))   {
            return false;
        }
        if (moptType != ((AECompare)aePattern).moptType) {
            return false;
        }
        if (maeLeft.isPatternMatch(((AECompare)aePattern).maeLeft, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion, progContext) == false) {
            return false;
        }
        if (maeRight.isPatternMatch(((AECompare)aePattern).maeRight, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion, progContext) == false) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean isKnownValOrPseudo() {
        if (!maeLeft.isKnownValOrPseudo())    {
            return false;
        }
        if (!maeRight.isKnownValOrPseudo())    {
            return false;
        }
        return true;
    }
    
    // note that the return list should not be changed.
    @Override
    public LinkedList<AbstractExpr> getListOfChildren()    {
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        listChildren.add(maeLeft);
        listChildren.add(maeRight);
        return listChildren;
    }
    
    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws JFCALCExpErrException, JSmartMathErrException {
        if (listChildren == null || listChildren.size() != 2) {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);            
        }
        AECompare aeReturn = new AECompare();
        aeReturn.copy(this);
        aeReturn.maeLeft = listChildren.getFirst();
        aeReturn.maeRight = listChildren.getLast();
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }
    
    // this function replaces children who equal aeFrom to aeTo and
    // returns the number of children that are replaced.
    @Override
    public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern,
            LinkedList<AbstractExpr> listReplacedChildren, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException    {
        AECompare aeReturn = new AECompare();
        aeReturn.copy(this);
        for (int idx = 0; idx < listFromToMap.size(); idx ++)    {
            AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
            AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
            if (aeReturn.maeLeft.isEqual(aeFrom, progContext))    {
                aeReturn.maeLeft = aeTo;
                listReplacedChildren.add(aeReturn.maeLeft);
                break;
            }
        }
        
        for (int idx = 0; idx < listFromToMap.size(); idx ++)    {
            AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
            AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
            if (aeReturn.maeRight.isEqual(aeFrom, progContext))    {
                aeReturn.maeRight = aeTo;
                listReplacedChildren.add(aeReturn.maeRight);
                break;
            }
        }
        return aeReturn;
    }

    @Override
    public AbstractExpr distributeAExpr(SimplifyParams simplifyParams, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException    {
        validateAbstractExpr();
        return this;
    }
    
    // avoid to do any overhead work.
    @Override
    public DataClass evaluateAExprQuick(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr(); // still needs to do some basic validation.
        DataClass datumLeft = maeLeft.evaluateAExprQuick(lUnknownVars, progContext);
        DataClass datumRight = maeRight.evaluateAExprQuick(lUnknownVars, progContext);
        DataClass datum = ExprEvaluator.evaluateTwoOperandCell(datumLeft, new CalculateOperator(moptType, 2), datumRight);
        return datum;        
    }

    // avoid to do any overhead work.
    @Override
    public AbstractExpr evaluateAExpr(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr(); // still needs to do some basic validation.
        AbstractExpr aeLeft = maeLeft.evaluateAExpr(lUnknownVars, progContext);
        AbstractExpr aeRight = maeRight.evaluateAExpr(lUnknownVars, progContext);
        if (aeLeft instanceof AEConst && aeRight instanceof AEConst) {
            DataClass datum = ExprEvaluator.evaluateTwoOperandCell(((AEConst)aeLeft).getDataClassRef(), new CalculateOperator(moptType, 2), ((AEConst)aeRight).getDataClassRef());
            return new AEConst(datum);
        } else {
            return new AECompare(aeLeft, moptType, aeRight);
        }
    }

    // exceptions like cannot merge or unknown variable should be handled internally in this function.
    // if an exception is thrown out, something is wrong.
    @Override
    public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars, SimplifyParams simplifyParams, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr();
        AbstractExpr aeLeft = maeLeft.simplifyAExpr(lUnknownVars, simplifyParams, progContext);
        AbstractExpr aeRight = maeRight.simplifyAExpr(lUnknownVars, simplifyParams, progContext);
        
        AbstractExpr aeReturn = new AECompare(aeLeft, moptType, aeRight);
        try    {
            // have to clone the parameters coz they may be changed inside.
            aeReturn = mergeCompare(aeLeft, aeRight, moptType, lUnknownVars, simplifyParams, progContext);
        } catch (JSmartMathErrException e)    {
            if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)    {
                throw e;
            }
        }
        
        aeReturn = aeReturn.distributeAExpr(simplifyParams, progContext);
        return aeReturn;
    }

    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        if ((enumAET.getValue() > menumAEType.getValue()
                    && enumAET.getValue() <= ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX.getValue())
                || (enumAET.getValue() == menumAEType.getValue() && nLeftOrRight <= 0))    {
            return true;
        }
        return false;
    }
    
    @Override
    public int compareAExpr(AbstractExpr aexpr) throws JSmartMathErrException, JFCALCExpErrException {
        if (menumAEType.getValue() < aexpr.menumAEType.getValue())    {
            return 1;
        } else if (menumAEType.getValue() > aexpr.menumAEType.getValue())    {
            return -1;
        } else    {
            int nReturn = maeLeft.compareAExpr(((AECompare)aexpr).maeLeft);
            if (nReturn == 0)    {
                nReturn = maeRight.compareAExpr(((AECompare)aexpr).maeRight);
            }
            return nReturn;
        }
    }

    // identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
    @Override
    public boolean isNegligible(ProgContext progContext) throws JSmartMathErrException    {
        validateAbstractExpr();
        return false;    // return false because this aexpr has been mostly simplified
                        // but it is still not an AEConst. This means the data value
                        // of this aexpr is still not know. Thus return false.
    }
    
    // output the string based expression of any abstract expression type.
    @Override
    public String output()    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        boolean bLeftNeedBracketsWhenToStr = false, bRightNeedBracketsWhenToStr = false;
        bLeftNeedBracketsWhenToStr = maeLeft.needBracketsWhenToStr(menumAEType, 1);
        bRightNeedBracketsWhenToStr = maeRight.needBracketsWhenToStr(menumAEType, -1);
        
        String strOutput = "";
        if (bLeftNeedBracketsWhenToStr) {
            strOutput += "(" + maeLeft.output() + ")";
        } else  {
            strOutput += maeLeft.output();
        }
        strOutput += moptType.output();
        if (bRightNeedBracketsWhenToStr)    {
            strOutput += "(" + maeRight.output() + ")";
        } else  {
            strOutput += maeRight.output();
        }
        return strOutput;
    }
    
    @Override
    public String outputWithFlag(int flag, ProgContext progContextNow)    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        boolean bLeftNeedBracketsWhenToStr = false, bRightNeedBracketsWhenToStr = false;
        bLeftNeedBracketsWhenToStr = maeLeft.needBracketsWhenToStr(menumAEType, 1);
        bRightNeedBracketsWhenToStr = maeRight.needBracketsWhenToStr(menumAEType, -1);
        
        String strOutput = "";
        if (bLeftNeedBracketsWhenToStr) {
            strOutput += "(" + maeLeft.outputWithFlag(flag, progContextNow) + ")";
        } else  {
            strOutput += maeLeft.outputWithFlag(flag, progContextNow);
        }
        strOutput += moptType.output();
        if (bRightNeedBracketsWhenToStr)    {
            strOutput += "(" + maeRight.outputWithFlag(flag, progContextNow) + ")";
        } else  {
            strOutput += maeRight.outputWithFlag(flag, progContextNow);
        }
        return strOutput;
    }
    
    // note that parameters of this function will be changed inside.
    public static AbstractExpr mergeCompare(AbstractExpr aeLeft, AbstractExpr aeRight, OPERATORTYPES optType,
            LinkedList<UnknownVariable> lUnknownVars, SimplifyParams simplifyParams, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
        if (aeLeft instanceof AEConst && aeRight instanceof AEConst)    {
            // both aeInput1 and aeInput2 are constants.
            DataClass datumLeft = ((AEConst)aeLeft).getDataClassRef(),
            datumRight = ((AEConst)aeRight).getDataClassRef();   // evaluateTwoOperandCell will not change datumLeft or datumRight.

            CalculateOperator calcOpt = new CalculateOperator(optType, 2);
            DataClass datum = ExprEvaluator.evaluateTwoOperandCell(datumLeft, calcOpt, datumRight);
            AEConst aexprReturn = new AEConst(datum);
            return aexprReturn;
        } else if (aeLeft.isEqual(aeRight, progContext)) {
            // left and right are the same thing.
            if (optType == OPERATORTYPES.OPERATOR_EQ  || optType == OPERATORTYPES.OPERATOR_NOSMALLER
                    || optType == OPERATORTYPES.OPERATOR_NOLARGER)    {
                // ==, >=, <=.
                return new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE));    // cast to DataClassBuiltIn will be ok.
            } else    {
                // !=, >, <
                return new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE));    // cast to DataClassBuiltIn will be ok.
            }
        } else  {   // if (!(aeLeft.isEqual(aeRight)))
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            listChildren.add(aeLeft);
            listChildren.add(aeRight);
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
            AbstractExpr aeLeftMinusRight = new AEPosNegOpt(listChildren, listOpts);
            // we need not to worry about variable name space because aeLeft and aeRight have been
            // simplified most using name space.
            aeLeftMinusRight = aeLeftMinusRight.simplifyAExprMost(lUnknownVars, simplifyParams, progContext);
            if (aeLeftMinusRight instanceof AEConst)    {
                // if not a constant, we cannot compare coz variable's values are unknown
                DataClass datumLeftMinusRight = ((AEConst)aeLeftMinusRight).getDataClassRef();   // datumLeftMinusRight will not be modified.
                if (DCHelper.isDataClassType(datumLeftMinusRight, DATATYPES.DATUM_REF_DATA))  {
                    if (DCHelper.isZeros(datumLeftMinusRight, false)) { // left minus right is a zero matrix.
                        if (optType == OPERATORTYPES.OPERATOR_EQ  || optType == OPERATORTYPES.OPERATOR_NOSMALLER
                                || optType == OPERATORTYPES.OPERATOR_NOLARGER)    {
                            // ==, >=, <=.
                            return new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE));
                        } else    {
                            // >, <, !=
                            return new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE));
                        }
                    } else  { // left minus right is not a zero matrix.
                        if (optType == OPERATORTYPES.OPERATOR_EQ)   {
                            return new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE));
                        } else if (optType == OPERATORTYPES.OPERATOR_NEQ) {
                            return new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE));
                        }
                    }
                } else  {
                    DataClass datumZero = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO);
                    CalculateOperator calcOpt = new CalculateOperator(optType, 2);
                    DataClass datum = ExprEvaluator.evaluateTwoOperandCell(datumLeftMinusRight, calcOpt, datumZero);
                    AEConst aexprReturn = new AEConst(datum);
                    return aexprReturn;
                }
            }
        }
        
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }

    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException, JFCALCExpErrException {
        AbstractExpr aeLeft = (maeLeft instanceof AEConst)?maeLeft:maeLeft.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        AbstractExpr aeRight = (maeRight instanceof AEConst)?maeRight:maeRight.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        return new AECompare(aeLeft, moptType, aeRight);
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        AbstractExpr aeLeft = maeLeft, aeRight = maeRight;
        if (maeLeft instanceof AEConst
                && DCHelper.isDataClassType(((AEConst)maeLeft).getDataClassRef(), DATATYPES.DATUM_ABSTRACT_EXPR)) {
            try {
                aeLeft = DCHelper.lightCvtOrRetDCAExpr(((AEConst)maeLeft).getDataClassRef()).getAExpr();
            } catch (JFCALCExpErrException e) {
                // cast to DataClassAExpr will be ok coz the type has been checked. So will not be here.
                e.printStackTrace();
            }
        }
        if (maeRight instanceof AEConst
                && DCHelper.isDataClassType(((AEConst)maeRight).getDataClassRef(), DATATYPES.DATUM_ABSTRACT_EXPR)) {
            try {
                aeRight = DCHelper.lightCvtOrRetDCAExpr(((AEConst)maeRight).getDataClassRef()).getAExpr();
            } catch (JFCALCExpErrException e) {
                // cast to DataClassAExpr will be ok coz the type has been checked. So will not be here.
                e.printStackTrace();
            }
        }
        return new AECompare(aeLeft, moptType, aeRight);
    }

    @Override
    public int getVarAppearanceCnt(String strVarName) {
        int nCnt = maeLeft.getVarAppearanceCnt(strVarName);
        nCnt += maeRight.getVarAppearanceCnt(strVarName);
        return nCnt;
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) {
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        listChildren.add(maeLeft);
        listChildren.add(maeRight);
        return getReferredFunctionsFromAExprs(listChildren, progContext);
    }
}
