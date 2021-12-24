// MFP project, AEIndex.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassAExpr;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.ModuleInfo;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AEIndex extends AbstractExpr {

	private static final long serialVersionUID = 1L;
	public AbstractExpr maeBase = AEInvalid.AEINVALID;
    public AbstractExpr maeIndex = AEInvalid.AEINVALID;
    
    public AEIndex() {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX;
        maeBase = AEInvalid.AEINVALID;
        maeIndex = AEInvalid.AEINVALID;
    }
    
    public AEIndex(AbstractExpr aeBase, AbstractExpr aeIndex) throws JSmartMathErrException    {
        setAEIndex(aeBase, aeIndex);
    }

    public AEIndex(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException    {
        copy(aexprOrigin);
    }

    @Override
    public void validateAbstractExpr() throws JSmartMathErrException {
        if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
        }
        if (maeIndex instanceof AEConst)    {
            if (!DCHelper.isDataClassType(((AEConst)maeIndex).getDataClassRef(), DATATYPES.DATUM_REF_DATA))    {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
            }
        } else if (!(maeIndex instanceof AEDataRef))    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
        }
        // test if indices are integers will be left in the simplify
    }
    
    private void setAEIndex(AbstractExpr aeBase, AbstractExpr aeIndex) throws JSmartMathErrException    {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX;
        maeBase = aeBase;
        maeIndex = aeIndex;
        validateAbstractExpr();
    }
    
    @Override
    protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
            JSmartMathErrException {
        ((AEIndex)aexprOrigin).validateAbstractExpr();
        super.copy(aexprOrigin);
        
        maeBase = ((AEIndex)aexprOrigin).maeBase;
        maeIndex = ((AEIndex)aexprOrigin).maeIndex;
    }

    @Override
    protected void copyDeep(AbstractExpr aexprOrigin)
            throws JFCALCExpErrException, JSmartMathErrException {
        ((AEIndex)aexprOrigin).validateAbstractExpr();
        super.copyDeep(aexprOrigin);
        
        maeBase = ((AEIndex)aexprOrigin).maeBase.cloneSelf();
        maeIndex = ((AEIndex)aexprOrigin).maeIndex.cloneSelf();
    }

    @Override
    public AbstractExpr cloneSelf() throws JFCALCExpErrException,
            JSmartMathErrException {
        AbstractExpr aeReturn = new AEIndex();
        aeReturn.copyDeep(this);
        return aeReturn;
    }

    @Override
    public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();
        
        int[] nIndexArray = new int[0];
        if (maeIndex instanceof AEConst)    {
            // this dataclass has been validated, it must be a data reference
            DataClass[] datumIndexList = DCHelper.lightCvtOrRetDCArray(((AEConst)maeIndex).getDataClassCopy()).getDataList();
            nIndexArray = new int[datumIndexList.length];
            for (int idx = 0; idx < datumIndexList.length; idx ++)    {
                DataClassSingleNum datumThisIdx = DCHelper.lightCvtOrRetDCMFPInt(datumIndexList[idx]);
                nIndexArray[idx] = (int)datumThisIdx.getDataValue().longValue();
            }
        } else    {
            // must be AEDATAREF
            nIndexArray = new int[((AEDataRef)maeIndex).mlistChildren.size()];
            for (int idx = 0; idx < nIndexArray.length; idx ++)    {
                if (!(((AEDataRef)maeIndex).mlistChildren.get(idx) instanceof AEConst))    {   // the index is not know
                    if (bUnknownAsSingle)   {
                        return new int[0];  // index is unknown, so we assume the indexed value is a single value if bUnknownAsSingle is flagged
                    } else {
                        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION);
                    }
                } else    {
                    // need not to getDatumValue cause it has to be changed to integer anyway.
                    DataClass datumIdx = ((AEConst)((AEDataRef)maeIndex).mlistChildren.get(idx)).getDataClassCopy();
                    nIndexArray[idx] = (int)DCHelper.lightCvtOrRetDCMFPInt(datumIdx).getDataValue().longValue();
                }
            }
        }
        if (!(maeBase instanceof AEConst))    {
            if (bUnknownAsSingle)   {
                return new int[0];
            } else {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION);
            }
        } else    {
            DataClass datumBase = ((AEConst)maeBase).getDataClassRef();
            if (!DCHelper.isDataClassType(datumBase, DATATYPES.DATUM_REF_DATA)) {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_DATA_TYPE);
            }
            DataClass datumChild = DCHelper.lightCvtOrRetDCArray(datumBase).getDataAtIndexByRef(nIndexArray);   // because it is a matrix, so use getDataClassRef() instead of getDataClass()
            return datumChild.recalcDataArraySize();
        }
    }

    // note that progContext can be null. If null, it is unconditional equal.
    @Override
    public boolean isEqual(AbstractExpr aexpr, ProgContext progContext) throws JFCALCExpErrException {
        if (menumAEType != aexpr.menumAEType)    {
            return false;
        } else if (maeBase.isEqual(((AEIndex)aexpr).maeBase, progContext) == false)    {
            return false;
        } else if (maeIndex.isEqual(((AEIndex)aexpr).maeIndex, progContext) == false)    {
            return false;
        } else {
            return true;
        }
    }
    
    @Override
    public int getHashCode()  throws JFCALCExpErrException {
        int hashRet = menumAEType.hashCode() + maeBase.getHashCode() * 13 + maeIndex.getHashCode() * 19;
        return hashRet;
    }

    @Override
    public boolean isPatternMatch(AbstractExpr aePattern,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoFuncs,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts,
                                LinkedList<PatternExprUnitMap> listpeuMapUnknowns,
                                boolean bAllowConversion, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        /* do not call isPatternDegrade function because generally index expression cannot degrade-match a pattern.*/
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
        if (!(aePattern instanceof AEIndex))  { // was if (!(aePattern instanceof AEDataRef)), but I think it is wrong.
            return false;
        }
        if (maeBase.isPatternMatch(((AEIndex)aePattern).maeBase, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion, progContext) == false)  {
            return false;
        }
        if (maeIndex.isPatternMatch(((AEIndex)aePattern).maeIndex, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion, progContext) == false)  {
            return false;
        } 
        
        return true;
    }
    
    @Override
    public boolean isKnownValOrPseudo() {
        if (!maeBase.isKnownValOrPseudo())    {
            return false;
        }
        if (!maeIndex.isKnownValOrPseudo())    {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean isVariable() {
        return maeBase.isVariable();
    }

    @Override
    public LinkedList<AbstractExpr> getListOfChildren() {
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        listChildren.add(maeBase);
        listChildren.add(maeIndex);
        return listChildren;
    }
    
    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws JFCALCExpErrException, JSmartMathErrException {
        if (listChildren == null || listChildren.size() != 2) {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);            
        }
        AEIndex aeReturn = new AEIndex();
        aeReturn.copy(this);
        aeReturn.maeBase = listChildren.getFirst();
        aeReturn.maeIndex = listChildren.getLast();
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }
    
    @Override
    public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern,
            LinkedList<AbstractExpr> listReplacedChildren, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException    {
        AEIndex aeReturn = new AEIndex();
        aeReturn.copy(this);
        for (int idx = 0; idx < listFromToMap.size(); idx ++)    {
            AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
            AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
            if (aeReturn.maeBase.isEqual(aeFrom, progContext))    {
                aeReturn.maeBase = aeTo;
                listReplacedChildren.add(aeReturn.maeBase);
                break;
            }
        }
        
        for (int idx = 0; idx < listFromToMap.size(); idx ++)    {
            AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
            AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
            if (aeReturn.maeIndex.isEqual(aeFrom, progContext))    {
                aeReturn.maeIndex = aeTo;
                listReplacedChildren.add(aeReturn.maeIndex);
                break;
            }
        }
        return aeReturn;
    }

    @Override
    public AbstractExpr distributeAExpr(SimplifyParams simplifyParams, ProgContext progContext) throws JFCALCExpErrException,
            JSmartMathErrException {
        validateAbstractExpr();
        
        return this;
    }

    // avoid to do any overhead work.
    @Override
    public DataClass evaluateAExprQuick(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr(); // still needs to do some basic validation.
        DataClass datumBase = maeBase.evaluateAExprQuick(lUnknownVars, progContext);
        DataClass datumIndex = maeIndex.evaluateAExprQuick(lUnknownVars, progContext);
        if (!DCHelper.isDataClassType(datumBase, DATATYPES.DATUM_REF_DATA)
                || !DCHelper.isDataClassType(datumIndex, DATATYPES.DATUM_REF_DATA))    {
            throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_DATA_TYPE);
        }
        DataClass[] datumIndexList = DCHelper.lightCvtOrRetDCArray(datumIndex).getDataList();
        int[] nIndexArray = new int[datumIndexList.length];
        for (int idx = 0; idx < datumIndexList.length; idx ++)    {
            nIndexArray[idx] = (int)DCHelper.lightCvtOrRetDCMFPInt(datumIndexList[idx]).getDataValue().longValue();
        }
        return DCHelper.lightCvtOrRetDCArray(datumBase).getDataAtIndexByRef(nIndexArray);
    }

    // avoid to do any overhead work.
    @Override
    public AbstractExpr evaluateAExpr(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr(); // still needs to do some basic validation.
        AbstractExpr aeBase = maeBase.evaluateAExpr(lUnknownVars, progContext);
        AbstractExpr aeIndex = maeIndex.evaluateAExpr(lUnknownVars, progContext);
        if (aeBase instanceof AEConst && aeIndex instanceof AEConst) {
            DataClass datumBase = ((AEConst)aeBase).getDataClass();
            DataClass datumIndex = ((AEConst)aeIndex).getDataClass();
            if (!DCHelper.isDataClassType(datumBase, DATATYPES.DATUM_REF_DATA)
                    || !DCHelper.isDataClassType(datumIndex, DATATYPES.DATUM_REF_DATA))    {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_DATA_TYPE);
            }
            DataClass[] datumIndexList = DCHelper.lightCvtOrRetDCArray(datumIndex).getDataList();
            int[] nIndexArray = new int[datumIndexList.length];
            for (int idx = 0; idx < datumIndexList.length; idx ++)    {
                nIndexArray[idx] = (int)DCHelper.lightCvtOrRetDCMFPInt(datumIndexList[idx]).getDataValue().longValue();
            }
            return new AEConst(DCHelper.lightCvtOrRetDCArray(datumBase).getDataAtIndexByRef(nIndexArray));
        } else {
            return new AEIndex(aeBase, aeIndex);
        }
    }

    @Override
    public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars, SimplifyParams simplifyParams, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();
        
        AbstractExpr aeBase = maeBase.simplifyAExpr(lUnknownVars, simplifyParams, progContext);
        AbstractExpr aeIndex = maeIndex.simplifyAExpr(lUnknownVars, simplifyParams, progContext);
        
        AbstractExpr aeReturn = this;
        try    {
            // different from merge power, need not use cloneSelf in mergeIndex
            // Although maeIndex's children may be converted to integer, this is
            // what we desire.
            aeReturn = mergeIndex(aeBase, aeIndex);
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
        if (enumAET == ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX && nLeftOrRight <= 0)   { // like [[1,2],[3,4]]([1,[0]][1])
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
            int nReturn = maeBase.compareAExpr(((AEIndex)aexpr).maeBase);
            if (nReturn == 0)    {
                nReturn = maeIndex.compareAExpr(((AEIndex)aexpr).maeIndex);
            }
            return nReturn;
        }
    }

    // identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
    @Override
    public boolean isNegligible(ProgContext progContext) throws JSmartMathErrException, JFCALCExpErrException    {
        validateAbstractExpr();
        if (maeBase.isNegligible(progContext))    {
            return true;
        }
        return false;    // because this aexpr has been simplified most, but it is
                        // still a AEIndex. This implies that maeIndex is also not
                        // a constant as maeBase. This also means the value of this
                        // aexpr is not known so far.
    }
    
    // output the string based expression of any abstract expression type.
    @Override
    public String output()    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        boolean bBaseNeedBracketsWhenToStr = false;
        bBaseNeedBracketsWhenToStr = maeBase.needBracketsWhenToStr(menumAEType, 1);
        
        String strOutput = "";
        if (bBaseNeedBracketsWhenToStr) {
            strOutput += "(" + maeBase.output() + ")";
        } else  {
            strOutput += maeBase.output();
        }
        strOutput += maeIndex.output();
        return strOutput;
    }
    
    @Override
    public String outputWithFlag(int flag, ProgContext progContextNow)    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        boolean bBaseNeedBracketsWhenToStr = false;
        bBaseNeedBracketsWhenToStr = maeBase.needBracketsWhenToStr(menumAEType, 1);
        
        String strOutput = "";
        if (bBaseNeedBracketsWhenToStr) {
            strOutput += "(" + maeBase.outputWithFlag(flag, progContextNow) + ")";
        } else  {
            strOutput += maeBase.outputWithFlag(flag, progContextNow);
        }
        strOutput += maeIndex.outputWithFlag(flag, progContextNow);
        return strOutput;
    }
    
    public static AbstractExpr mergeIndex(AbstractExpr aeBase, AbstractExpr aeIndex) throws JFCALCExpErrException, JSmartMathErrException    {
        if (aeBase instanceof AEConst && aeIndex instanceof AEConst)    {
            // this dataclass has been validated, it must be a data reference
            if (!DCHelper.isDataClassType(((AEConst)aeIndex).getDataClassRef(), DATATYPES.DATUM_REF_DATA)
                    && (!DCHelper.isDataClassType(((AEConst)aeIndex).getDataClassRef(), DATATYPES.DATUM_ABSTRACT_EXPR)
                        || !(DCHelper.lightCvtOrRetDCAExpr(((AEConst)aeIndex).getDataClassRef()).getAExpr() instanceof AEDataRef)))    {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_DATA_TYPE);
            }
            boolean bIsIdxAExpr = DCHelper.isDataClassType(((AEConst)aeIndex).getDataClassRef(), DATATYPES.DATUM_ABSTRACT_EXPR);
            AbstractExpr aeNewIndex = aeIndex;
            if (bIsIdxAExpr) {
                aeNewIndex = DCHelper.lightCvtOrRetDCAExpr(((AEConst)aeIndex).getDataClassRef()).getAExpr();   // if index is an aexpr data.
            } /*
             * // aexpr data inside a data reference data is not allowed. Otherwise, evaluate2OperandCalc, evaluate1OperandCalc will fail. too complicated.
             * else if (((AEConst)aeIndex).getDataClassRef().getDataType() == DATATYPES.DATUM_REF_DATA) {
                DataClass[] arrayDataChildren = ((AEConst)aeIndex).getDataClassRef().getDataList();
                LinkedList<AbstractExpr> listAEChildren = new LinkedList<AbstractExpr>();
                for (int idx = 0; idx < arrayDataChildren.length; idx ++) {
                    if (arrayDataChildren[idx].getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR) {
                        listAEChildren.add(new AEConst(arrayDataChildren[idx]));
                    } else {
                        bIsIdxAExpr = true;
                        listAEChildren.add(arrayDataChildren[idx].getAExpr());
                    }
                }
                if (bIsIdxAExpr) {  //if index is a data array but includes aexpr children.
                    aeNewIndex = new AEDataRef(listAEChildren);
                }
            }*/
            
            if (DCHelper.isDataClassType(((AEConst)aeBase).getDataClassRef(), DATATYPES.DATUM_ABSTRACT_EXPR)
                    || DCHelper.isDataClassType(((AEConst)aeIndex).getDataClassRef(), DATATYPES.DATUM_ABSTRACT_EXPR)) {
                // we assume aeIndex has been converted to an aexpr datum, i.e. situation like [datum, datum, aexpr datum] does not exist
                AbstractExpr aeNewBase = DCHelper.isDataClassType(((AEConst)aeBase).getDataClassRef(), DATATYPES.DATUM_ABSTRACT_EXPR)?
                                            DCHelper.lightCvtOrRetDCAExpr(((AEConst)aeBase).getDataClassRef()).getAExpr():aeBase;
                AbstractExpr aeMerged = new AEIndex(aeNewBase, aeNewIndex);
                return new AEConst(new DataClassAExpr(aeMerged));
            } else {
                // in the validateAbstractExpr, we have validated that if aeIndex is an AEConst, its dataclass must be an array, i.e. DataClassBuiltIn
                DataClass[] datumIndexList = DCHelper.lightCvtOrRetDCArray(((AEConst)aeIndex).getDataClassCopy()).getDataList();
                int[] nIndexArray = new int[datumIndexList.length];
                for (int idx = 0; idx < datumIndexList.length; idx ++)    {
                    nIndexArray[idx] = (int)DCHelper.lightCvtOrRetDCMFPInt(datumIndexList[idx]).getDataValue().longValue();
                }
                DataClass datumBase = ((AEConst)aeBase).getDataClassRef();    //    // because it is a matrix, so use getDataClassRef() instead of getDataClass()
                /* // we do not need to check if datumBase is primitive or array as we will convert it to array in the next statement anyway.
                if (!DCHelper.isPrimitiveOrArray(datumBase))    {
                    throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_DATA_TYPE);
                }*/
                return new AEConst(DCHelper.lightCvtOrRetDCArray(datumBase).getDataAtIndexByRef(nIndexArray));  // data in AEConst is not immutable.
            }
        }
        AbstractExpr aeReturn = new AEIndex(aeBase, aeIndex);
        if (aeBase instanceof AEIndex)    {
            AbstractExpr aeNewBase = ((AEIndex)aeBase).maeBase;
            AbstractExpr aeLowerLevelIndex = ((AEIndex)aeBase).maeIndex;
            if (aeLowerLevelIndex instanceof AEConst && aeIndex instanceof AEConst)    {
                if (!DCHelper.isDataClassType(((AEConst)aeLowerLevelIndex).getDataClassRef(), DATATYPES.DATUM_REF_DATA)
                        || !DCHelper.isDataClassType(((AEConst)aeIndex).getDataClassRef(), DATATYPES.DATUM_REF_DATA))    {
                    throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_DATA_TYPE);
                }
                DataClass[] datumLowerIndexList = DCHelper.lightCvtOrRetDCArray(((AEConst)aeLowerLevelIndex).getDataClassCopy()).getDataList();
                DataClass[] datumIndexList = DCHelper.lightCvtOrRetDCArray(((AEConst)aeIndex).getDataClassCopy()).getDataList();
                DataClass[] datumNewIndexList = new DataClass[datumIndexList.length + datumLowerIndexList.length];
                for (int idx = 0; idx < datumNewIndexList.length; idx ++)    {
                    if (idx < datumLowerIndexList.length)    {
                        datumNewIndexList[idx] = DCHelper.lightCvtOrRetDCMFPInt(datumLowerIndexList[idx]);
                    } else    {
                        datumNewIndexList[idx] = DCHelper.lightCvtOrRetDCMFPInt(datumIndexList[idx - datumLowerIndexList.length]);
                    }
                }
                DataClass datumNewIndex = new DataClassArray(datumNewIndexList);
                AbstractExpr aeNewIndex = new AEConst(datumNewIndex);
                aeReturn = new AEIndex(aeNewBase, aeNewIndex);                
            } else if (aeLowerLevelIndex instanceof AEDataRef && aeIndex instanceof AEConst)    {
                if (!DCHelper.isDataClassType(((AEConst)aeIndex).getDataClassRef(), DATATYPES.DATUM_REF_DATA))    {
                    throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_DATA_TYPE);
                }
                LinkedList<AbstractExpr> listNewIndexChildren = new LinkedList<AbstractExpr>();
                listNewIndexChildren.addAll(((AEDataRef)aeLowerLevelIndex).mlistChildren);
                DataClass[] datumIndexList = DCHelper.lightCvtOrRetDCArray(((AEConst)aeIndex).getDataClassCopy()).getDataList();
                for (int idx = 0; idx < datumIndexList.length; idx ++)    {
                    listNewIndexChildren.add(new AEConst(DCHelper.lightCvtOrRetDCMFPInt(datumIndexList[idx])));
                }
                AbstractExpr aeNewIndex = new AEDataRef(listNewIndexChildren);
                aeReturn = new AEIndex(aeNewBase, aeNewIndex);                
            } else if (aeLowerLevelIndex instanceof AEConst && aeIndex instanceof AEDataRef)    {
                if (!DCHelper.isDataClassType(((AEConst)aeLowerLevelIndex).getDataClassRef(), DATATYPES.DATUM_REF_DATA))    {
                    throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_DATA_TYPE);
                }
                LinkedList<AbstractExpr> listNewIndexChildren = new LinkedList<AbstractExpr>();
                DataClass[] datumIndexList = DCHelper.lightCvtOrRetDCArray(((AEConst)aeLowerLevelIndex).getDataClassCopy()).getDataList();
                for (int idx = 0; idx < datumIndexList.length; idx ++)    {
                    listNewIndexChildren.add(new AEConst(DCHelper.lightCvtOrRetDCMFPInt(datumIndexList[idx])));
                }
                listNewIndexChildren.addAll(((AEDataRef)aeIndex).mlistChildren);
                AbstractExpr aeNewIndex = new AEDataRef(listNewIndexChildren);
                aeReturn = new AEIndex(aeNewBase, aeNewIndex);                
            } else if (aeLowerLevelIndex instanceof AEDataRef && aeIndex instanceof AEDataRef)    {
                LinkedList<AbstractExpr> listNewIndexChildren = new LinkedList<AbstractExpr>();
                listNewIndexChildren.addAll(((AEDataRef)aeLowerLevelIndex).mlistChildren);
                listNewIndexChildren.addAll(((AEDataRef)aeIndex).mlistChildren);
                AbstractExpr aeNewIndex = new AEDataRef(listNewIndexChildren);
                aeReturn = new AEIndex(aeNewBase, aeNewIndex);                
            } else    {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
            }
            
            if (((AEIndex)aeReturn).maeBase instanceof AEIndex)    {
                // if the newly merged AExpr's base is still an AEIndex, merge again.
                aeReturn = mergeIndex(((AEIndex)aeReturn).maeBase, ((AEIndex)aeReturn).maeIndex);
            }
        }
        return aeReturn;    // do not throw cannot merge exception here.
    }
    
    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException, JFCALCExpErrException {
        AbstractExpr aeBase = (maeBase instanceof AEConst)?maeBase:maeBase.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        AbstractExpr aeIndex = (maeIndex instanceof AEConst)?maeIndex:maeIndex.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        return new AEIndex(aeBase, aeIndex);
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        AbstractExpr aeBase = maeBase, aeIndex = maeIndex;
        if (maeBase instanceof AEConst
                && DCHelper.isDataClassType(((AEConst)maeBase).getDataClassRef(), DATATYPES.DATUM_ABSTRACT_EXPR)) {
            try {
                aeBase = DCHelper.lightCvtOrRetDCAExpr(((AEConst)maeBase).getDataClassRef()).getAExpr();
            } catch (JFCALCExpErrException e) {
                // Will not happen because type has been checked.
                e.printStackTrace();
            }
        }
        if (maeIndex instanceof AEConst
                && DCHelper.isDataClassType(((AEConst)maeIndex).getDataClassRef(), DATATYPES.DATUM_ABSTRACT_EXPR)) {
            try {
                aeIndex = DCHelper.lightCvtOrRetDCAExpr(((AEConst)maeIndex).getDataClassRef()).getAExpr();
            } catch (JFCALCExpErrException e) {
                // Will not happen because type has been checked.
                e.printStackTrace();
            }
        }
        return new AEIndex(aeBase, aeIndex);
    }

    @Override
    public int getVarAppearanceCnt(String strVarName) {
        int nCnt = maeBase.getVarAppearanceCnt(strVarName);
        nCnt += maeIndex.getVarAppearanceCnt(strVarName);
        return nCnt;
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) {
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        listChildren.add(maeBase);
        listChildren.add(maeIndex);
        return getReferredFunctionsFromAExprs(listChildren, progContext);
    }
    
}
