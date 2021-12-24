// MFP project, AEOnOffUnaryOpt.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.Operators.CalculateOperator;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassNull;
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

public class AEOnOffUnaryOpt extends AbstractExpr {

	private static final long serialVersionUID = 1L;

	public CalculateOperator getOpt() throws JSmartMathErrException    {
        if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FALSE)    {
            return new CalculateOperator(OPERATORTYPES.OPERATOR_FALSE, 1, true);
        } else if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_NOT)    {
            return new CalculateOperator(OPERATORTYPES.OPERATOR_NOT, 1, true);
        } else if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE)    {
            return new CalculateOperator(OPERATORTYPES.OPERATOR_TRANSPOSE, 1, false);
        } else    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
        }
    }
    
    public OPERATORTYPES getOptType() throws JSmartMathErrException    {
        if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FALSE)    {
            return OPERATORTYPES.OPERATOR_FALSE;
        } else if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_NOT)    {
            return OPERATORTYPES.OPERATOR_NOT;
        } else if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE)    {
            return OPERATORTYPES.OPERATOR_TRANSPOSE;
        } else    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
        }
    }
    
    public int mnNumofOpts = 1;
    public AbstractExpr maexprChild = AEInvalid.AEINVALID;
    
    public AEOnOffUnaryOpt() {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FALSE;
        maexprChild = AEInvalid.AEINVALID;
        mnNumofOpts = 1;
    }
    
    public AEOnOffUnaryOpt(ABSTRACTEXPRTYPES aeType, AbstractExpr aexprChild, int nNumOfOpts) throws JSmartMathErrException    {
        setAEOnOffUnaryOpt(aeType, aexprChild, nNumOfOpts);
    }

    public AEOnOffUnaryOpt(CalculateOperator calcOpt, AbstractExpr aexprChild, int nNumOfOpts) throws JSmartMathErrException    {
        if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_FALSE)    {
            setAEOnOffUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FALSE, aexprChild, nNumOfOpts);
        } else if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_NOT)    {
            setAEOnOffUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_NOT, aexprChild, nNumOfOpts);
        } else if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_TRANSPOSE)    {
            setAEOnOffUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE, aexprChild, nNumOfOpts);
        } else    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
        }
    }

    public AEOnOffUnaryOpt(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException    {
        copy(aexprOrigin);
    }

    @Override
    public void validateAbstractExpr() throws JSmartMathErrException    {
        if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FALSE
                && menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_NOT
                && menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
        } else if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_NOT && mnNumofOpts > 1)    {
            // clearly, we cannot not twice to any number because , for example ~1 is -2 while ~-2 is invalid.
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
        }
        
    }
    
    private void setAEOnOffUnaryOpt(ABSTRACTEXPRTYPES aeType, AbstractExpr aexprChild, int nNumOfOpts) throws JSmartMathErrException    {
        menumAEType = aeType;
        maexprChild = aexprChild;
        mnNumofOpts = nNumOfOpts;
        validateAbstractExpr();
    }

    @Override
    protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
            JSmartMathErrException {
        ((AEOnOffUnaryOpt)aexprOrigin).validateAbstractExpr();
        super.copy(aexprOrigin);
        maexprChild = ((AEOnOffUnaryOpt)aexprOrigin).maexprChild;
        mnNumofOpts = ((AEOnOffUnaryOpt)aexprOrigin).mnNumofOpts;
    }

    @Override
    protected void copyDeep(AbstractExpr aexprOrigin)
            throws JFCALCExpErrException, JSmartMathErrException {
        ((AEOnOffUnaryOpt)aexprOrigin).validateAbstractExpr();
        super.copyDeep(aexprOrigin);
        maexprChild = ((AEOnOffUnaryOpt)aexprOrigin).maexprChild.cloneSelf();
        mnNumofOpts = ((AEOnOffUnaryOpt)aexprOrigin).mnNumofOpts;
    }

    @Override
    public AbstractExpr cloneSelf() throws JFCALCExpErrException,
            JSmartMathErrException {
        AbstractExpr aeReturn = new AEOnOffUnaryOpt();
        aeReturn.copyDeep(this);
        return aeReturn;
    }

    @Override
    public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();
        if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FALSE
                || menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_NOT)    {
            return new int[0];    // must be a simple number
        } else    {    // transpose
            int[] narrayDim = maexprChild.recalcAExprDim(bUnknownAsSingle);
            int[] narrayReturn = new int[0];
            if (mnNumofOpts % 2 == 0)    {
                if (narrayDim.length == 1)    {
                    narrayReturn = new int[2];
                    narrayReturn[0] = 1;
                    narrayReturn[1] = narrayDim[0];
                } else    {
                    narrayReturn = narrayDim;
                }
            } else    {
                if (narrayDim.length == 0)    {
                    narrayReturn = narrayDim;
                } else if (narrayDim.length == 1)    {
                    narrayReturn = new int[2];
                    narrayReturn[0] = narrayDim[0];
                    narrayReturn[1] = 1;
                } else    {    // >= 2
                    narrayReturn = new int[narrayDim.length];
                    for (int idx = 0; idx < narrayDim.length; idx ++)    {
                        narrayReturn[narrayDim.length - 1 - idx] = narrayDim[idx];
                    }
                }
            }
            return narrayReturn;
        }
    }

    // note that progContext can be null. If null, it is unconditional equal.
    @Override
    public boolean isEqual(AbstractExpr aexpr, ProgContext progContext) throws JFCALCExpErrException {
        if (menumAEType != aexpr.menumAEType)    {
            return false;
        } else if (mnNumofOpts != ((AEOnOffUnaryOpt)aexpr).mnNumofOpts)    {
            return false;
        } else if (maexprChild.isEqual(((AEOnOffUnaryOpt)aexpr).maexprChild, progContext) == false)    {
            return false;
        } else    {
            return true;
        }
    }
    
    @Override
    public int getHashCode()  throws JFCALCExpErrException {
        int hashRet = menumAEType.hashCode() + 13 * mnNumofOpts + 19 * maexprChild.getHashCode();
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
        if (!(aePattern instanceof AEOnOffUnaryOpt))   {
            return false;
        }
        if (getOptType() != ((AEOnOffUnaryOpt)aePattern).getOptType())  {
            return false;
        }
        if (mnNumofOpts != ((AEOnOffUnaryOpt)aePattern).mnNumofOpts)    {
            return false;
        }
        if (maexprChild.isPatternMatch(((AEOnOffUnaryOpt)aePattern).maexprChild,
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
        AEOnOffUnaryOpt aeReturn = new AEOnOffUnaryOpt();
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
        AEOnOffUnaryOpt aeReturn = new AEOnOffUnaryOpt();
        aeReturn.copy(this);
        for (int idx = 0; idx < listFromToMap.size(); idx ++)    {
            AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
            AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
            if (aeReturn.maexprChild.isEqual(aeFrom, progContext))    {
                aeReturn.maexprChild = aeTo;    // need to clone because will be many aeTo copies. But dont do this here to save time.
                listReplacedChildren.add(aeReturn.maexprChild);
                break;
            }
        }
        return aeReturn;
    }

    // only transpose can be distributed. 
    @Override
    public AbstractExpr distributeAExpr(SimplifyParams simplifyParams, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException    {
        validateAbstractExpr();
        if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE)    {
            if (mnNumofOpts == 0) {
                return maexprChild; // note that x'' may not equal x so cannot use mnNumofOpts % 2 == 0;
            } else if (maexprChild instanceof AEPosNegOpt)    {
                AEPosNegOpt aeChildCpy = new AEPosNegOpt();
                aeChildCpy.copy(maexprChild);
                int nNumOfOpts = mnNumofOpts % 2;
                if (nNumOfOpts == 0) {
                    nNumOfOpts = 2;
                }
                for (int idx = 0; idx < aeChildCpy.mlistChildren.size(); idx ++)    {
                    AbstractExpr aeSubChild = aeChildCpy.mlistChildren.get(idx);
                    aeChildCpy.mlistChildren.set(idx,
                            new AEOnOffUnaryOpt(
                                    ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE,
                                    aeSubChild, nNumOfOpts));
                }
                return aeChildCpy;
            } else if (maexprChild instanceof AEMulDivOpt)    {
                // Note that the calculation only support 2D matrix.
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                for (int idx = 0; idx < ((AEMulDivOpt)maexprChild).mlistChildren.size(); idx ++)    {
                    if (mnNumofOpts %2 == 1) {
                        listChildren.addFirst(new AEOnOffUnaryOpt(
                                ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE,
                                ((AEMulDivOpt)maexprChild).mlistChildren.get(idx), 1));
                        listOpts.addFirst(((AEMulDivOpt)maexprChild).mlistOpts.get(idx));
                    } else {    // mnNumofOpts %2 == 0
                        listChildren.add(new AEOnOffUnaryOpt(
                                ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE,
                                ((AEMulDivOpt)maexprChild).mlistChildren.get(idx), 2));
                        listOpts.add(((AEMulDivOpt)maexprChild).mlistOpts.get(idx));
                    }
                }
                return new AEMulDivOpt(listChildren, listOpts);
            } else if (maexprChild instanceof AELeftDivOpt) {
                if (mnNumofOpts %2 == 1)    {
                    // Note that the calculation only support 2D matrix.
                    AbstractExpr aeLeftChild = ((AELeftDivOpt)maexprChild).maeLeft;
                    aeLeftChild = new AEOnOffUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE,
                                                        aeLeftChild, 1);
                    AbstractExpr aeRightChild = ((AELeftDivOpt)maexprChild).maeRight;
                    aeRightChild = new AEOnOffUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE,
                                                        aeRightChild, 1);
                    LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                    listChildren.add(aeRightChild);
                    listChildren.add(aeLeftChild);
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
                    return new AEMulDivOpt(listChildren, listOpts);    // convert to multiply division.
                } else {
                    AELeftDivOpt aeChildCpy = new AELeftDivOpt();
                    aeChildCpy.copy(maexprChild);
                    aeChildCpy.maeLeft = new AEOnOffUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE,
                                                        aeChildCpy.maeLeft, 2);
                    aeChildCpy.maeRight = new AEOnOffUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE,
                                                        aeChildCpy.maeRight, 2);
                    return aeChildCpy;
                }
            }
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
        if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FALSE)    {
            datumValue = DCHelper.lightCvtOrRetDCMFPBool(datumChild);
            if (mnNumofOpts % 2 == 1)    {
                datumValue = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL,
                        (DCHelper.lightCvtOrRetDCMFPBool(datumValue).getDataValue().isFalse())?  // datumValue has been converted to boolean, so we can use isFalse instead of is Actually false.
                                MFPNumeric.TRUE:MFPNumeric.FALSE);
            }
        } else if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_NOT)    {
            datumValue = DCHelper.lightCvtOrRetDCMFPInt(datumChild);
            if (mnNumofOpts % 2 == 1)    {
                datumValue = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NOT, 1, true), datumValue);
            } else    {    //even number of opts.
                datumValue = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NOT, 1, true), datumValue);
                datumValue = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NOT, 1, true), datumValue);
            }
        } else    {    // transpose
            if (mnNumofOpts % 2 == 1)    {
                datumValue = ExprEvaluator.evaluateOneOperandCell(datumChild, new CalculateOperator(OPERATORTYPES.OPERATOR_TRANSPOSE, 1, false));
            } else if (mnNumofOpts != 0)    {    //even number of opts.
                datumValue = ExprEvaluator.evaluateOneOperandCell(datumChild, new CalculateOperator(OPERATORTYPES.OPERATOR_TRANSPOSE, 1, false));
                datumValue = ExprEvaluator.evaluateOneOperandCell(datumChild, new CalculateOperator(OPERATORTYPES.OPERATOR_TRANSPOSE, 1, false));
            }
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
            DataClass datumChild = ((AEConst)aeChild).getDataClassRef();
            DataClass datumValue = new DataClassNull();
            if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FALSE)    {
                datumValue = DCHelper.lightCvtOrRetDCMFPBool(datumChild);
                if (mnNumofOpts % 2 == 1)    {
                    datumValue = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL,
                            (DCHelper.lightCvtOrRetDCMFPBool(datumValue).getDataValue().isFalse())?  // datumValue has been converted to boolean, so we can use isFalse instead of is Actually false.
                                    MFPNumeric.TRUE:MFPNumeric.FALSE);
                }
            } else if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_NOT)    {
                datumValue = DCHelper.lightCvtOrRetDCMFPInt(datumChild);
                if (mnNumofOpts % 2 == 1)    {
                    datumValue = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NOT, 1, true), datumValue);
                } else    {    //even number of opts.
                    datumValue = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NOT, 1, true), datumValue);
                    datumValue = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NOT, 1, true), datumValue);
                }
            } else    {    // transpose
                if (mnNumofOpts % 2 == 1)    {
                    datumValue = ExprEvaluator.evaluateOneOperandCell(datumChild, new CalculateOperator(OPERATORTYPES.OPERATOR_TRANSPOSE, 1, false));
                } else if (mnNumofOpts != 0)    {    //even number of opts.
                    datumValue = ExprEvaluator.evaluateOneOperandCell(datumChild, new CalculateOperator(OPERATORTYPES.OPERATOR_TRANSPOSE, 1, false));
                    datumValue = ExprEvaluator.evaluateOneOperandCell(datumChild, new CalculateOperator(OPERATORTYPES.OPERATOR_TRANSPOSE, 1, false));
                }
            }

            return new AEConst(datumValue);
        } else {
            return new AEOnOffUnaryOpt(menumAEType, aeChild, mnNumofOpts);
        }
    }
    
    @Override
    public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars, SimplifyParams simplifyParams, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();
        
        AEOnOffUnaryOpt aeCopy = new AEOnOffUnaryOpt();
        aeCopy.copy(this);
        
        // initially simplify, should be before child simplify.
        while (aeCopy.maexprChild.menumAEType == aeCopy.menumAEType)    {
            aeCopy.maexprChild = ((AEOnOffUnaryOpt)aeCopy.maexprChild).maexprChild;
            aeCopy.mnNumofOpts += ((AEOnOffUnaryOpt)aeCopy.maexprChild).mnNumofOpts;
        }

        aeCopy.maexprChild = aeCopy.maexprChild.simplifyAExpr(lUnknownVars, simplifyParams, progContext);
        if (aeCopy.mnNumofOpts == 0) {
            return aeCopy.maexprChild;
        }
        // we dont care the datavalue changed because it will be changed anyway.
        // if unsuccessful, it is an error.
        if (aeCopy.maexprChild instanceof AEConst)    {
            DataClass datumValue = ((AEConst)aeCopy.maexprChild).getDataClassCopy();
            if (aeCopy.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FALSE)    {
                datumValue = DCHelper.lightCvtOrRetDCMFPBool(((AEConst)aeCopy.maexprChild).getDataClassRef());
                if (mnNumofOpts % 2 == 1)    {
                    datumValue = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL,
                            (DCHelper.lightCvtOrRetDCMFPBool(datumValue).getDataValue().isFalse())?  // datumValue has been converted to boolean, so we can use isFalse instead of is Actually false.
                                    MFPNumeric.TRUE:MFPNumeric.FALSE);
                }
            } else if (aeCopy.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_NOT)    {
                datumValue = DCHelper.lightCvtOrRetDCMFPInt(((AEConst)aeCopy.maexprChild).getDataClassRef());
                if (aeCopy.mnNumofOpts % 2 == 1)    {
                    datumValue = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NOT, 1, true), datumValue);
                } else    {    //even number of opts.
                    datumValue = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NOT, 1, true), datumValue);
                    datumValue = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NOT, 1, true), datumValue);
                }
            } else    {    // transpose
                if (aeCopy.mnNumofOpts % 2 == 1)    {
                    datumValue = ExprEvaluator.evaluateOneOperandCell(((AEConst)aeCopy.maexprChild).getDataClassRef(), new CalculateOperator(OPERATORTYPES.OPERATOR_TRANSPOSE, 1, false));
                } else if (aeCopy.mnNumofOpts != 0)    {    //even number of opts.
                    datumValue = ExprEvaluator.evaluateOneOperandCell(((AEConst)aeCopy.maexprChild).getDataClassRef(), new CalculateOperator(OPERATORTYPES.OPERATOR_TRANSPOSE, 1, false));
                    datumValue = ExprEvaluator.evaluateOneOperandCell(((AEConst)aeCopy.maexprChild).getDataClassRef(), new CalculateOperator(OPERATORTYPES.OPERATOR_TRANSPOSE, 1, false));
                }
            }
            return new AEConst(datumValue);
        } else    {
            if (aeCopy.mnNumofOpts == 0)    {
                aeCopy.mnNumofOpts = 0;
            } else if (aeCopy.mnNumofOpts % 2 == 1)    {
                aeCopy.mnNumofOpts = 1;
            } else    {
                aeCopy.mnNumofOpts = 2;    // not 0 because child aexpr value may change after two transpose.
            }
            return aeCopy.distributeAExpr(simplifyParams, progContext);
        }
    }
    
    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        switch (menumAEType)    {
            case ABSTRACTEXPR_UOPT_FALSE:
            case ABSTRACTEXPR_UOPT_NOT:
                if (enumAET.getValue() >= ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL.getValue()
                            && enumAET.getValue() <= ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX.getValue())    {
                    return true;
                }
                return false;
            default: // UOPT: transpose
                return false;
        }
    }
    
    @Override
    public int compareAExpr(AbstractExpr aexpr) throws JSmartMathErrException, JFCALCExpErrException {
        if (menumAEType.getValue() < aexpr.menumAEType.getValue())    {
            return 1;
        } else if (menumAEType.getValue() > aexpr.menumAEType.getValue())    {
            return -1;
        } else    {
            return maexprChild.compareAExpr(((AEOnOffUnaryOpt)aexpr).maexprChild);
        }
    }

    // identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
    @Override
    public boolean isNegligible(ProgContext progContext) throws JSmartMathErrException, JFCALCExpErrException    {
        validateAbstractExpr();
        if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FALSE)    {
            if (mnNumofOpts % 2 == 0)     {
                return maexprChild.isNegligible(progContext);
            } else if (maexprChild instanceof AEConst)    {
                DataClass datum = DCHelper.lightCvtOrRetDCMFPBool(((AEConst)maexprChild).getDataClassRef());
                // datum now is DataClassBuiltIn now.
                if (DCHelper.lightCvtOrRetDCMFPBool(datum).getDataValue().isFalse())    { // datum has been boolean, so use isFalse instead of isActuallyFalse.s
                    return true;
                } else {
                    return false;
                }
            } else    {
                return false;
            }
        } else if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_NOT)    {
            // for not, mnNumofOpts can only be 0 or 1
            if (mnNumofOpts == 0)    {
                return maexprChild.isNegligible(progContext);
            } else    {    //mnNumofOpts == 1
                return false;
            }
        } else    {    // menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE
            return maexprChild.isNegligible(progContext);
        }

    }
    
    // output the string based expression of any abstract expression type.
    @Override
    public String output()    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        String strOutput = "";
        if (mnNumofOpts == 0)    {
            strOutput = maexprChild.output();
        } else    {
            boolean bNeedBracketsWhenToStr = false;
            if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE)   {
                bNeedBracketsWhenToStr = maexprChild.needBracketsWhenToStr(menumAEType, 1);
            } else  {   // FALSE or NOT
                bNeedBracketsWhenToStr = maexprChild.needBracketsWhenToStr(menumAEType, -1);
            }

            if (bNeedBracketsWhenToStr) {
                strOutput = "(" + maexprChild.output() + ")";
            } else  {
                strOutput = maexprChild.output();
            }
        }
        for (int idx = 0; idx < mnNumofOpts; idx ++)    {
            if (getOpt().getLabelPrefix() == false)    {
                strOutput += getOptType().output();
            } else    {
                strOutput = getOptType().output() + strOutput;
            }
        }
        return strOutput;
    }

    @Override
    public String outputWithFlag(int flag, ProgContext progContextNow)    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        String strOutput = "";
        if (mnNumofOpts == 0)    {
            strOutput = maexprChild.outputWithFlag(flag, progContextNow);
        } else    {
            boolean bNeedBracketsWhenToStr = false;
            if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE)   {
                bNeedBracketsWhenToStr = maexprChild.needBracketsWhenToStr(menumAEType, 1);
            } else  {   // FALSE or NOT
                bNeedBracketsWhenToStr = maexprChild.needBracketsWhenToStr(menumAEType, -1);
            }

            if (bNeedBracketsWhenToStr) {
                strOutput = "(" + maexprChild.outputWithFlag(flag, progContextNow) + ")";
            } else  {
                strOutput = maexprChild.outputWithFlag(flag, progContextNow);
            }
        }
        for (int idx = 0; idx < mnNumofOpts; idx ++)    {
            if (getOpt().getLabelPrefix() == false)    {
                strOutput += getOptType().output();
            } else    {
                strOutput = getOptType().output() + strOutput;
            }
        }
        return strOutput;
    }

    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException, JFCALCExpErrException {
        AbstractExpr aeChild = (maexprChild instanceof AEConst)?maexprChild:maexprChild.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        return new AEOnOffUnaryOpt(menumAEType, aeChild, mnNumofOpts);
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        AbstractExpr aeChild = maexprChild;
        if (maexprChild instanceof AEConst
                && DCHelper.isDataClassType(((AEConst)maexprChild).getDataClassRef(), DATATYPES.DATUM_ABSTRACT_EXPR)) {
            try {
                aeChild = DCHelper.lightCvtOrRetDCAExpr(((AEConst)maexprChild).getDataClassRef()).getAExpr();
            } catch (JFCALCExpErrException e) {
                // will not be here because type has been checked that ((AEConst)maexprChild).getDataClassRef() is DataClassAExpr
                e.printStackTrace();
            }
        }
        return new AEOnOffUnaryOpt(menumAEType, aeChild, mnNumofOpts);
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
