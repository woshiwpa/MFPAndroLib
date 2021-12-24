// MFP project, AEBitwiseOpt.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.Operators.CalculateOperator;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
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

public class AEBitwiseOpt extends AbstractExpr {

	private static final long serialVersionUID = 1L;

	// here mlistChildren and mlistOpts are not used because it leads to difficulty in merging.
    public AbstractExpr maeLeft = AEInvalid.AEINVALID, maeRight = AEInvalid.AEINVALID;
    
    public OPERATORTYPES moptType = OPERATORTYPES.OPERATOR_BITWISEAND;    // by default it is and.
    
    public AEBitwiseOpt() {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_BITWISE;
        maeLeft = AEInvalid.AEINVALID;
        maeRight = AEInvalid.AEINVALID;
        moptType = OPERATORTYPES.OPERATOR_BITWISEAND;
    }
    
    public AEBitwiseOpt(AbstractExpr aeLeft, OPERATORTYPES optType, AbstractExpr aeRight) throws JSmartMathErrException    {
        setAEBitwiseOpt(aeLeft, optType, aeRight);
    }

    public AEBitwiseOpt(AbstractExpr aeLeft, CalculateOperator opt, AbstractExpr aeRight) throws JSmartMathErrException    {
        setAEBitwiseOpt(aeLeft, opt.getOperatorType(), aeRight);
    }

    public AEBitwiseOpt(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException    {
        copy(aexprOrigin);
    }

    @Override
    public void validateAbstractExpr() throws JSmartMathErrException {
        if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_BITWISE)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
        }
        
        if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_BITWISE)    {
            if (moptType != OPERATORTYPES.OPERATOR_BITWISEAND && moptType != OPERATORTYPES.OPERATOR_BITWISEOR
                    && moptType != OPERATORTYPES.OPERATOR_BITWISEXOR)    {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);                
            }
        }
    }

    private void setAEBitwiseOpt(AbstractExpr aeLeft, OPERATORTYPES optType, AbstractExpr aeRight) throws JSmartMathErrException    {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_BITWISE;
        maeLeft = aeLeft;
        moptType = optType;
        maeRight = aeRight;
        validateAbstractExpr();
    }

    @Override
    protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
            JSmartMathErrException {
        ((AEBitwiseOpt)aexprOrigin).validateAbstractExpr();
        super.copy(aexprOrigin);
        maeLeft = ((AEBitwiseOpt)aexprOrigin).maeLeft;
        maeRight = ((AEBitwiseOpt)aexprOrigin).maeRight;
        moptType = ((AEBitwiseOpt)aexprOrigin).moptType;
    }

    @Override
    protected void copyDeep(AbstractExpr aexprOrigin)
            throws JFCALCExpErrException, JSmartMathErrException {
        ((AEBitwiseOpt)aexprOrigin).validateAbstractExpr();
        super.copyDeep(aexprOrigin);
        maeLeft = ((AEBitwiseOpt)aexprOrigin).maeLeft.cloneSelf();
        maeRight = ((AEBitwiseOpt)aexprOrigin).maeRight.cloneSelf();
        moptType = ((AEBitwiseOpt)aexprOrigin).moptType;
    }

    @Override
    public AbstractExpr cloneSelf() throws JFCALCExpErrException,
            JSmartMathErrException {
        AbstractExpr aeReturn = new AEBitwiseOpt();
        aeReturn.copyDeep(this);
        return aeReturn;
    }

    @Override
    public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();

        return new int[0];    // bitwise operator always return a single value.
    }

    // note that progContext can be null. If null, it is unconditional equal.
    @Override
    public boolean isEqual(AbstractExpr aexpr, ProgContext progContext) throws JFCALCExpErrException {
        if (menumAEType != aexpr.menumAEType)    {
            return false;
        } else if (moptType != ((AEBitwiseOpt)aexpr).moptType)    {
            return false;
        } else if (!maeLeft.isEqual(((AEBitwiseOpt)aexpr).maeLeft, progContext))    {
            return false;
        } else if (!maeRight.isEqual(((AEBitwiseOpt)aexpr).maeRight, progContext))    {
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
        if (!(aePattern instanceof AEBitwiseOpt))   {
            return false;
        }
        if (moptType != ((AEBitwiseOpt)aePattern).moptType) {
            return false;
        }
        if (maeLeft.isPatternMatch(((AEBitwiseOpt)aePattern).maeLeft, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion, progContext) == false) {
            return false;
        }
        if (maeRight.isPatternMatch(((AEBitwiseOpt)aePattern).maeRight, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion, progContext) == false) {
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
        AEBitwiseOpt aeReturn = new AEBitwiseOpt();
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
        AEBitwiseOpt aeReturn = new AEBitwiseOpt();
        aeReturn.copy(this);
        for (int idx = 0; idx < listFromToMap.size(); idx ++)    {
            AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
            AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
            if (aeReturn.maeLeft.isEqual(aeFrom, progContext))    {
                aeReturn.maeLeft = aeTo;    // no need to cloneSelf coz aeTo will not be changed.
                listReplacedChildren.add(aeReturn.maeLeft);
                break;
            }
        }
        
        for (int idx = 0; idx < listFromToMap.size(); idx ++)    {
            AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
            AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
            if (aeReturn.maeRight.isEqual(aeFrom, progContext))    {
                aeReturn.maeRight = aeTo;    // no need to cloneSelf coz aeTo will not be changed.
                listReplacedChildren.add(aeReturn.maeRight);
                break;
            }
        }
        return aeReturn;
    }

    @Override
    public AbstractExpr distributeAExpr(SimplifyParams simplifyParams, ProgContext progContext) throws JFCALCExpErrException,
            JSmartMathErrException {
        // Distribute can be degraded to the same level of aexpr, but not higher level.
        if ((maeLeft instanceof AEBitwiseOpt || maeRight instanceof AEBitwiseOpt)
                && !(maeLeft instanceof AEBitwiseOpt && maeRight instanceof AEBitwiseOpt))    {
            AEBitwiseOpt aeBitwiseChild = (AEBitwiseOpt) ((maeLeft instanceof AEBitwiseOpt)?maeLeft:maeRight);
            AbstractExpr aeNonBitwiseChild = (maeLeft instanceof AEBitwiseOpt)?maeRight:maeLeft;
            if (moptType != OPERATORTYPES.OPERATOR_BITWISEXOR
                    && aeBitwiseChild.moptType != OPERATORTYPES.OPERATOR_BITWISEXOR)    {
                AEBitwiseOpt aeNewLeft = new AEBitwiseOpt(aeNonBitwiseChild,    // no need to clone self
                        moptType, aeBitwiseChild.maeLeft);
                AEBitwiseOpt aeNewRight = new AEBitwiseOpt(aeNonBitwiseChild,    // no need to clone self
                        moptType, aeBitwiseChild.maeRight);
                return new AEBitwiseOpt(aeNewLeft, aeBitwiseChild.moptType, aeNewRight);
            }
        }
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
            return new AEBitwiseOpt(aeLeft, moptType, aeRight);
        }
    }

    @Override
    public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars,
                                      SimplifyParams simplifyParams, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();
        
        AbstractExpr aeLeft = maeLeft.simplifyAExpr(lUnknownVars, simplifyParams, progContext);
        AbstractExpr aeRight = maeRight.simplifyAExpr(lUnknownVars, simplifyParams, progContext);
        
        AbstractExpr aeReturn = new AEBitwiseOpt(aeLeft, moptType, aeRight);
        try    {
            // have to clone the parameters coz they may be changed inside.
            aeReturn = mergeBitwise(aeLeft, aeRight, moptType, progContext);
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
            int nReturn = maeLeft.compareAExpr(((AEBitwiseOpt)aexpr).maeLeft);
            if (nReturn == 0)    {
                nReturn = maeRight.compareAExpr(((AEBitwiseOpt)aexpr).maeRight);
            }
            return nReturn;
        }
    }

    // identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
    @Override
    public boolean isNegligible(ProgContext progContext) throws JSmartMathErrException, JFCALCExpErrException    {
        validateAbstractExpr();
        if (moptType == OPERATORTYPES.OPERATOR_BITWISEAND)    {
            return maeLeft.isNegligible(progContext) && maeRight.isNegligible(progContext);
        } else if (moptType == OPERATORTYPES.OPERATOR_BITWISEOR)    {
            return maeLeft.isNegligible(progContext) || maeRight.isNegligible(progContext);
        } else    {    //moptType == OPERATORTYPES.OPERATOR_XOR
            if (maeLeft.isNegligible(progContext) && maeRight.isNegligible(progContext))    {
                return true;
            } else if (maeLeft.isEqual(maeRight, progContext))    {
                return true;
            } else    {
                return false;
            }
        }
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
    public static AbstractExpr mergeBitwise(AbstractExpr aeLeft, AbstractExpr aeRight, OPERATORTYPES optType, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException    {
        if (aeLeft instanceof AEConst && aeRight instanceof AEConst)    {
            // both aeInput1 and aeInput2 are constants.
            DataClass datum1 = ((AEConst)aeLeft).getDataClassRef(),
            datum2 = ((AEConst)aeRight).getDataClassRef();

            CalculateOperator calcOpt = new CalculateOperator(optType, 2);
            DataClass datum = ExprEvaluator.evaluateTwoOperandCell(datum1, calcOpt, datum2); // evaluateTwoOperandCell will not change datum1 or datum2 so use getFinalDatumValue
            AEConst aexprReturn = new AEConst(datum);
            return aexprReturn;
        } else if (aeLeft.isEqual(aeRight, progContext))    {
            // left and right are the same thing.
            if (optType == OPERATORTYPES.OPERATOR_BITWISEAND || optType == OPERATORTYPES.OPERATOR_BITWISEOR)    {
                return aeLeft;
            } else if (optType == OPERATORTYPES.OPERATOR_BITWISEXOR)    {
                return new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO));
            }
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_OPERATOR);
        } else if (aeLeft instanceof AEBitwiseOpt && aeRight instanceof AEBitwiseOpt)    {
            if (((AEBitwiseOpt)aeLeft).moptType == ((AEBitwiseOpt)aeRight).moptType
                    && ((AEBitwiseOpt)aeLeft).moptType != OPERATORTYPES.OPERATOR_BITWISEXOR
                    && optType != OPERATORTYPES.OPERATOR_BITWISEXOR)    {
                if (((AEBitwiseOpt)aeLeft).maeLeft.isEqual(((AEBitwiseOpt)aeRight).maeLeft, progContext))    {
                    AEBitwiseOpt aeNewChild = new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeRight,
                                                            optType, ((AEBitwiseOpt)aeRight).maeRight);
                    return new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeLeft,
                                            ((AEBitwiseOpt)aeLeft).moptType,
                                            aeNewChild);
                } else if (((AEBitwiseOpt)aeLeft).maeLeft.isEqual(((AEBitwiseOpt)aeRight).maeRight, progContext))    {
                    AEBitwiseOpt aeNewChild = new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeRight,
                                                            optType, ((AEBitwiseOpt)aeRight).maeLeft);
                    return new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeLeft,
                                ((AEBitwiseOpt)aeLeft).moptType,
                                aeNewChild);
                } else if (((AEBitwiseOpt)aeLeft).maeRight.isEqual(((AEBitwiseOpt)aeRight).maeLeft, progContext))    {
                    AEBitwiseOpt aeNewChild = new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeLeft,
                                                            optType, ((AEBitwiseOpt)aeRight).maeRight);
                    return new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeRight,
                                            ((AEBitwiseOpt)aeLeft).moptType,
                                            aeNewChild);
                } else if (((AEBitwiseOpt)aeLeft).maeRight.isEqual(((AEBitwiseOpt)aeRight).maeRight, progContext))    {
                    AEBitwiseOpt aeNewChild = new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeLeft,
                                                            optType, ((AEBitwiseOpt)aeRight).maeLeft);
                    return new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeRight,
                                            ((AEBitwiseOpt)aeLeft).moptType,
                                            aeNewChild);
                }
            }
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_OPERATOR);
        } else if (aeLeft instanceof AEBitwiseOpt || aeRight instanceof AEBitwiseOpt)    {
            // one of aeLeft and aeRight is not AEBitwiseOpt.
            AEBitwiseOpt aeBitwiseChild = (AEBitwiseOpt) ((aeLeft instanceof AEBitwiseOpt)?aeLeft:aeRight);
            AbstractExpr aeNonBitwiseChild = (aeLeft instanceof AEBitwiseOpt)?aeRight:aeLeft;
            if (optType == aeBitwiseChild.moptType)    {
                if (aeBitwiseChild.maeLeft.isEqual(aeNonBitwiseChild, progContext))    {
                    if (optType == OPERATORTYPES.OPERATOR_BITWISEXOR)    {
                        return aeBitwiseChild.maeRight;
                    } else    {
                        return aeBitwiseChild;
                    }
                } else if (aeBitwiseChild.maeRight.isEqual(aeNonBitwiseChild, progContext))    {
                    if (optType == OPERATORTYPES.OPERATOR_BITWISEXOR)    {
                        return aeBitwiseChild.maeLeft;
                    } else    {
                        return aeBitwiseChild;
                    }
                }                
            }
            throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
        }
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }

    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException, JFCALCExpErrException {
        AbstractExpr aeLeft = (maeLeft instanceof AEConst)?maeLeft:maeLeft.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        AbstractExpr aeRight = (maeRight instanceof AEConst)?maeRight:maeRight.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        return new AEBitwiseOpt(aeLeft, moptType, aeRight);
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
        return new AEBitwiseOpt(aeLeft, moptType, aeRight);
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
