// MFP project, AEUnaryOpt.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.Operators.CalculateOperator;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.Operators.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.ModuleInfo;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AEUnaryOpt extends AbstractExpr {

	private static final long serialVersionUID = 1L;

	public CalculateOperator getOpt() throws JSmartMathErrException    {
        if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL)    {
            return new CalculateOperator(OPERATORTYPES.OPERATOR_FACTORIAL, 1, false);
        } else if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT)    {
            return new CalculateOperator(OPERATORTYPES.OPERATOR_PERCENT, 1, false);
        } else    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
        }
    }
    
    public OPERATORTYPES getOptType() throws JSmartMathErrException    {
        if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL)    {
            return OPERATORTYPES.OPERATOR_FACTORIAL;
        } else if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT)    {
            return OPERATORTYPES.OPERATOR_PERCENT;
        } else    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
        }
    }
    
    public AbstractExpr maexprChild = AEInvalid.AEINVALID;

    public AEUnaryOpt() {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL;
        maexprChild = AEInvalid.AEINVALID;
    }
    
    public AEUnaryOpt(ABSTRACTEXPRTYPES aeType, AbstractExpr aexprChild) throws JSmartMathErrException    {
        setAEUnaryOpt(aeType, aexprChild);
    }

    public AEUnaryOpt(CalculateOperator calcOpt, AbstractExpr aexprChild) throws JSmartMathErrException    {
        if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_FACTORIAL)    {
            setAEUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL, aexprChild);
        } else if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_PERCENT)    {
            setAEUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT, aexprChild);
        } else    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
        }
    }

    public AEUnaryOpt(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException    {
        copy(aexprOrigin);
    }

    private void setAEUnaryOpt(ABSTRACTEXPRTYPES aeType, AbstractExpr aexprChild) throws JSmartMathErrException    {
        menumAEType = aeType;
        maexprChild = aexprChild;
        validateAbstractExpr();
    }

    @Override
    public void validateAbstractExpr() throws JSmartMathErrException    {
        if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL
                && menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
        }
    }
    
    @Override
    protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
            JSmartMathErrException {
        ((AEUnaryOpt)aexprOrigin).validateAbstractExpr();
        super.copy(aexprOrigin);
        maexprChild = ((AEUnaryOpt)aexprOrigin).maexprChild;
    }

    @Override
    protected void copyDeep(AbstractExpr aexprOrigin)
            throws JFCALCExpErrException, JSmartMathErrException {
        ((AEUnaryOpt)aexprOrigin).validateAbstractExpr();
        super.copyDeep(aexprOrigin);
        maexprChild = ((AEUnaryOpt)aexprOrigin).maexprChild.cloneSelf();
    }

    @Override
    public AbstractExpr cloneSelf() throws JFCALCExpErrException,
            JSmartMathErrException {
        AbstractExpr aeReturn = new AEUnaryOpt();
        aeReturn.copyDeep(this);
        return aeReturn;
    }

    @Override
    public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();
        return new int[0];
    }

    // note that progContext can be null. If null, it is unconditional equal.
    @Override
    public boolean isEqual(AbstractExpr aexpr, ProgContext progContext) throws JFCALCExpErrException {
        if (menumAEType != aexpr.menumAEType)    {
            return false;
        } else if (maexprChild.isEqual(((AEUnaryOpt)aexpr).maexprChild, progContext) == false)    {
            return false;
        } else    {
            return true;
        }
    }
    
    @Override
    public int getHashCode() throws JFCALCExpErrException {
        int hashRet = menumAEType.hashCode()
                + maexprChild.getHashCode() * 13;
        return hashRet;
    }

    @Override
    public boolean isPatternMatch(AbstractExpr aePattern,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoFuncs,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts,
                                LinkedList<PatternExprUnitMap> listpeuMapUnknowns,
                                boolean bAllowConversion, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
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
        if (!(aePattern instanceof AEUnaryOpt))   {
            return false;
        }
        if (getOptType() != ((AEUnaryOpt)aePattern).getOptType())  {
            return false;
        }
        if (maexprChild.isPatternMatch(((AEUnaryOpt)aePattern).maexprChild,
                listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns,
                bAllowConversion, progContext) == false) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean isKnownValOrPseudo() {
        return maexprChild.isKnownValOrPseudo();
    }
    
    // note that the return list should not be changed.
    @Override
    public LinkedList<AbstractExpr> getListOfChildren()    {
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        listChildren.add(maexprChild);
        return listChildren;
    }
    
    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws JFCALCExpErrException, JSmartMathErrException {
        if (listChildren == null || listChildren.size() != 1) {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);            
        }
        AEUnaryOpt aeReturn = new AEUnaryOpt();
        aeReturn.copy(this);
        aeReturn.maexprChild = listChildren.getFirst();
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }

    // this function replaces children who equal aeFrom to aeTo and
    // returns the number of children that are replaced.
    @Override
    public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern,
            LinkedList<AbstractExpr> listReplacedChildren, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException    {
        AEUnaryOpt aeReturn = new AEUnaryOpt();
        aeReturn.copy(this);
        for (int idx = 0; idx < listFromToMap.size(); idx ++)    {
            AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
            AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
            if (aeReturn.maexprChild.isEqual(aeFrom, progContext))    {
                aeReturn.maexprChild = aeTo;    // need to clone because will be many aeTo copies. But to keep performance, dont clone.
                listReplacedChildren.add(aeReturn.maexprChild);
                break;
            }
        }
        return aeReturn;
    }

    // assignment cannot be distributed.
    @Override
    public AbstractExpr distributeAExpr(SimplifyParams simplifyParams, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException    {
        validateAbstractExpr();
        if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT)    {
            AEUnaryOpt aeReturn = this;
            if (aeReturn.maexprChild instanceof AEPosNegOpt)    {
                AEPosNegOpt aeReturnChild = new AEPosNegOpt();
                aeReturnChild.copy((AEPosNegOpt)aeReturn.maexprChild);
                for (int idx = 0; idx < aeReturnChild.mlistChildren.size(); idx ++)    {
                    AbstractExpr aeSubChild = aeReturnChild.mlistChildren.get(idx);
                    aeReturnChild.mlistChildren.set(idx, new AEUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT, aeSubChild));
                }
                aeReturn = new AEUnaryOpt(aeReturnChild);
            } else if (aeReturn.maexprChild instanceof AEMulDivOpt)    {
                AEMulDivOpt aeReturnChild = new AEMulDivOpt();
                aeReturnChild.copy((AEMulDivOpt)aeReturn.maexprChild);
                aeReturnChild.mlistOpts.addFirst(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                aeReturnChild.mlistChildren.addFirst(new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.01, true))));
                aeReturn = new AEUnaryOpt(aeReturnChild);
            } else if (maexprChild instanceof AELeftDivOpt)    {
                AELeftDivOpt aeReturnChild = new AELeftDivOpt();
                aeReturnChild.copy((AELeftDivOpt)aeReturn.maexprChild);
                AbstractExpr aeRightChild = aeReturnChild.maeRight;
                aeReturnChild.maeRight = new AEUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT, aeRightChild);
                aeReturn = new AEUnaryOpt(aeReturnChild);
            }
            return aeReturn;
        }
        return this;
    }

    // avoid to do any overhead work.
    @Override
    public DataClass evaluateAExprQuick(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr(); // still needs to do some basic validation.
        DataClass datumChild = maexprChild.evaluateAExprQuick(lUnknownVars, progContext);
        DataClass datumValue = new DataClassNull();
        if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL)    {
            datumValue = DCHelper.lightCvtOrRetDCMFPInt(datumChild);
            datumValue = ExprEvaluator.evaluateOneOperandCell(datumValue, new CalculateOperator(OPERATORTYPES.OPERATOR_FACTORIAL, 1, false));
        } else {    // percentage
            datumValue = DCHelper.lightCvtOrRetDCMFPDec(datumChild);
            datumValue = ExprEvaluator.evaluateOneOperandCell(datumValue, new CalculateOperator(OPERATORTYPES.OPERATOR_PERCENT, 1, false));
        }

        return datumValue;        
    }
    
    // avoid to do any overhead work.
    @Override
    public AbstractExpr evaluateAExpr(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr(); // still needs to do some basic validation.
        AbstractExpr aeChild = maexprChild.evaluateAExpr(lUnknownVars, progContext);
        if (aeChild instanceof AEConst) {
            DataClass datumValue = new DataClassNull();
            if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL)    {
                datumValue = DCHelper.lightCvtOrRetDCMFPInt(((AEConst)aeChild).getDataClassRef());
                datumValue = ExprEvaluator.evaluateOneOperandCell(datumValue, new CalculateOperator(OPERATORTYPES.OPERATOR_FACTORIAL, 1, false));
            } else {    // percentage
                datumValue = DCHelper.lightCvtOrRetDCMFPDec(((AEConst)aeChild).getDataClassRef());
                datumValue = ExprEvaluator.evaluateOneOperandCell(datumValue, new CalculateOperator(OPERATORTYPES.OPERATOR_PERCENT, 1, false));
            }

            return new AEConst(datumValue);
        } else {
            return new AEUnaryOpt(menumAEType, aeChild);
        }
    }
    
    @Override
    public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars, SimplifyParams simplifyParams, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();
        AbstractExpr aexprChild = maexprChild.simplifyAExpr(lUnknownVars, simplifyParams, progContext);
        // we dont care the datavalue changed because it will be changed anyway.
        // if unsuccessful, it is an error.
        if (aexprChild instanceof AEConst)    {
            DataClass datumValue = new DataClassNull();
            if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL)    {
                datumValue = DCHelper.lightCvtOrRetDCMFPInt(((AEConst)aexprChild).getDataClassCopy());
                datumValue = ExprEvaluator.evaluateOneOperandCell(datumValue, new CalculateOperator(OPERATORTYPES.OPERATOR_FACTORIAL, 1, false));
            } else {    // percentage
                datumValue = DCHelper.lightCvtOrRetDCMFPDec(((AEConst)aexprChild).getDataClassCopy());
                datumValue = ExprEvaluator.evaluateOneOperandCell(datumValue, new CalculateOperator(OPERATORTYPES.OPERATOR_PERCENT, 1, false));
            }
            return new AEConst(datumValue);
        } else    {
            if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT) {
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                listChildren.add(aexprChild);
                listChildren.add(new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(100))));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
                AbstractExpr aeReturn = new AEMulDivOpt(listChildren, listOpts);
                return aeReturn.distributeAExpr(simplifyParams, progContext);
            } else  {
                return distributeAExpr(simplifyParams, progContext);
            }
        }
    }
    
    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        return false;
    }
    
    @Override
    public int compareAExpr(AbstractExpr aexpr) throws JSmartMathErrException, JFCALCExpErrException {
        if (menumAEType.getValue() < aexpr.menumAEType.getValue())    {
            return 1;
        } else if (menumAEType.getValue() > aexpr.menumAEType.getValue())    {
            return -1;
        } else    {
            return maexprChild.compareAExpr(((AEUnaryOpt)aexpr).maexprChild);
        }
    }

    // identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
    @Override
    public boolean isNegligible(ProgContext progContext) throws JSmartMathErrException, JFCALCExpErrException    {
        validateAbstractExpr();
        if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL)    {
            return maexprChild.isNegligible(progContext);
        } else    {    // menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT
            return maexprChild.isNegligible(progContext);
        }
    }
    
    // output the string based expression of any abstract expression type.
    @Override
    public String output()    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        boolean bNeedBracketsWhenToStr = false;
        String strOutput = "";
        if (getOpt().getLabelPrefix())    {
            bNeedBracketsWhenToStr = maexprChild.needBracketsWhenToStr(menumAEType, -1);
            if (bNeedBracketsWhenToStr) {
                strOutput = "(" + maexprChild.output() + ")";
            } else  {
                strOutput = maexprChild.output();
            }
            strOutput = getOptType().output() + strOutput;
        } else    {
            bNeedBracketsWhenToStr = maexprChild.needBracketsWhenToStr(menumAEType, 1);
            if (bNeedBracketsWhenToStr) {
                strOutput = "(" + maexprChild.output() + ")";
            } else  {
                strOutput = maexprChild.output();
            }
            strOutput += getOptType().output();
        }
        return strOutput;
    }

    @Override
    public String outputWithFlag(int flag, ProgContext progContextNow)    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        boolean bNeedBracketsWhenToStr = false;
        String strOutput = "";
        if (getOpt().getLabelPrefix())    {
            bNeedBracketsWhenToStr = maexprChild.needBracketsWhenToStr(menumAEType, -1);
            if (bNeedBracketsWhenToStr) {
                strOutput = "(" + maexprChild.outputWithFlag(flag, progContextNow) + ")";
            } else  {
                strOutput = maexprChild.outputWithFlag(flag, progContextNow);
            }
            strOutput = getOptType().output() + strOutput;
        } else    {
            bNeedBracketsWhenToStr = maexprChild.needBracketsWhenToStr(menumAEType, 1);
            if (bNeedBracketsWhenToStr) {
                strOutput = "(" + maexprChild.outputWithFlag(flag, progContextNow) + ")";
            } else  {
                strOutput = maexprChild.outputWithFlag(flag, progContextNow);
            }
            strOutput += getOptType().output();
        }
        return strOutput;
    }

    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException, JFCALCExpErrException {
        AbstractExpr aeChild = (maexprChild instanceof AEConst)?maexprChild:maexprChild.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        return new AEUnaryOpt(menumAEType, aeChild);
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        AbstractExpr aeChild = maexprChild;
        if (maexprChild instanceof AEConst
                && DCHelper.isDataClassType(((AEConst)maexprChild).getDataClassRef(), DATATYPES.DATUM_ABSTRACT_EXPR)) {
            try {
                aeChild = DCHelper.lightCvtOrRetDCAExpr(((AEConst)maexprChild).getDataClassRef()).getAExpr();
            } catch (JFCALCExpErrException e) {
                // will not be here because ((AEConst)maexprChild).getDataClassRef() must be a DataClassAExpr
                e.printStackTrace();
            }
        }
        return new AEUnaryOpt(menumAEType, aeChild);
    }
    
    @Override
    public int getVarAppearanceCnt(String strVarName) {
        int nCnt = maexprChild.getVarAppearanceCnt(strVarName);
        return nCnt;
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) {
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        listChildren.add(maexprChild);
        return getReferredFunctionsFromAExprs(listChildren, progContext);
    }
    
}
