// MFP project, AEInvalid.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.ModuleInfo;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AEInvalid extends AbstractExpr {

	private static final long serialVersionUID = 1L;
	public static final AEInvalid AEINVALID = new AEInvalid();
    
    public AEInvalid() {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID;
    }
    
    public AEInvalid(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException    {
        copy(aexprOrigin);
    }

    @Override
    public void validateAbstractExpr() throws JSmartMathErrException    {
        throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
    }
    
    @Override
    protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
            JSmartMathErrException {
        copyDeep(aexprOrigin);
    }

    @Override
    protected void copyDeep(AbstractExpr aexprOrigin)
            throws JFCALCExpErrException, JSmartMathErrException {
        if (aexprOrigin.menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
        }
        super.copyDeep(aexprOrigin);
    }

    @Override
    public AbstractExpr cloneSelf() throws JFCALCExpErrException, JSmartMathErrException    {
        return AEInvalid.AEINVALID;
    }
    
    @Override
    public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
            JFCALCExpErrException {
        throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
    }

    // note that progContext can be null. If null, it is unconditional equal.
    @Override
    public boolean isEqual(AbstractExpr aexpr, ProgContext progContext) throws JFCALCExpErrException {
        if (aexpr.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)    {
            return true;
        } else    {
            return false;
        }
    }

    @Override
    public int getHashCode() throws JFCALCExpErrException {
        return 0;
    }

    @Override
    public boolean isPatternMatch(AbstractExpr aePattern,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoFuncs,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts,
                                LinkedList<PatternExprUnitMap> listpeuMapUnknowns,
                                boolean bAllowConversion, ProgContext progContext) {
        /* do not call isPatternDegrade function because invalid cannot degrade-match a pattern.*/
        if (aePattern.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)    {
            return true;
        } else    {
            return false;
        }
    }

    @Override
    public boolean isKnownValOrPseudo() {
        return false;
    }
    
    // note that the return list should not be changed.
    @Override
    public LinkedList<AbstractExpr> getListOfChildren()    {
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        return listChildren;
    }
    
    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws JFCALCExpErrException, JSmartMathErrException {
        throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);            
    }

    // this function replaces children who equal aeFrom to aeTo and
    // returns the number of children that are replaced.
    @Override
    public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern,
            LinkedList<AbstractExpr> listReplacedChildren, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException    {
        return this;
    }
    
    @Override
    public AbstractExpr distributeAExpr(SimplifyParams simplifyParams, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException    {
        return this;
    }
    
    @Override
    public DataClass evaluateAExprQuick(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws JSmartMathErrException {
        throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
    }
    
    @Override
    public AbstractExpr evaluateAExpr(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws JSmartMathErrException {
        throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
    }
    
    @Override
    public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars, SimplifyParams simplifyParams, ProgContext progContext)
            throws InterruptedException {
        return this;
    }

    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        return false;
    }
    
    @Override
    public int compareAExpr(AbstractExpr aexpr) throws JFCALCExpErrException {
        if (menumAEType.getValue() < aexpr.menumAEType.getValue())    {
            return 1;
        } else if (menumAEType.getValue() > aexpr.menumAEType.getValue())    {
            return -1;
        } else    {    // both of them are AEInvalid
            return 0;
        }
    }
    
    // identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
    @Override
    public boolean isNegligible(ProgContext progContext) throws JSmartMathErrException    {
        throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
    }
    
    // output the string based expression of any abstract expression type.
    @Override
    public String output()    throws JFCALCExpErrException, JSmartMathErrException {
        throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
    }

    @Override
    public String outputWithFlag(int flag, ProgContext progContextNow)    throws JFCALCExpErrException, JSmartMathErrException {
        throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
    }

    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException {
        return this;
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        return this;
    }

    @Override
    public int getVarAppearanceCnt(String strVarName) {
        return 0;
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) {
        return new LinkedList<ModuleInfo>();
    }
}
