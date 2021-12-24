// MFP project, AEConst.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassAExpr;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassComplex;
import com.cyzapps.Jfcalc.DataClassExtObjRef;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.ModuleInfo;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AEConst extends AbstractExpr {
	private static final long serialVersionUID = 1L;
	public String mstrKnownVarName = "";
    private DataClass mdatumValue = new DataClassNull();    // declare it to private is the best way to avoid change outof scope.
    
    public AEConst() {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE;
        mdatumValue = new DataClassNull();
    }
    
    public AEConst(DataClass datum) throws JFCALCExpErrException, JSmartMathErrException    {
        setAEConst(datum);
    }
    
    public AEConst(String strName, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws JSmartMathErrException, JFCALCExpErrException    {
        setAEConst(strName, lVarNameSpaces);
    }

    public AEConst(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException    {
        copy(aexprOrigin);  // no need to deep copy because data is a constant.
    }

    @Override
    public void validateAbstractExpr() throws JSmartMathErrException    {
        if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE && menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_DATAREFVALUE
                && menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_KNOWNVAR)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
        }
    }
    
    private void setAEConst(DataClass datum) throws JFCALCExpErrException, JSmartMathErrException {
        if (datum == null)    {
            mdatumValue = new DataClassNull();
            menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE;
        } else if (DCHelper.isDataClassType(datum, DATATYPES.DATUM_REF_DATA))    {
            mdatumValue = new DataClassNull();
            mdatumValue = datum;    // no need to deep copy i.e. copyTypeValueDeep(datum). We need to ensure that datum will not change.
            menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_DATAREFVALUE;
        } else    {
            mdatumValue = new DataClassNull();
            mdatumValue = datum;    // no need to deep copy i.e. copyTypeValueDeep(datum). We need to ensure that datum will not change.
            menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE;
        }
        validateAbstractExpr();
    }

    private void setAEConst(String strName, LinkedList<LinkedList<Variable>> lVarNameSpaces)
            throws JSmartMathErrException, JFCALCExpErrException    {
        DataClass datum = VariableOperator.lookUpSpaces4Value(strName, lVarNameSpaces);
        if (datum == null)    {
            // the variable value is not found
            throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_UNDEFINED_VARIABLE);
        }
        mdatumValue = datum.cloneSelf();   // need to deep copy here because value in lVarNameSpaces may change in a function.
        mstrKnownVarName = strName;
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_KNOWNVAR;
        validateAbstractExpr();
    }
    
    public DataClass getDataClassCopy() throws JFCALCExpErrException {
        DataClass datumReturn = mdatumValue.cloneSelf();
        return datumReturn;
    }
    
    public DataClass getDataClassRef() {
        return mdatumValue; // this means returned datum value will not be changed.
    }
    
    // this function returns a deep copy of mdatumValue if it is a simple data type (bool, int, double and complex).
    // if it is a string or matrix, returns a reference.
    public DataClass getDataClass() throws JFCALCExpErrException {
        if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_MFPBOOL, DATATYPES.DATUM_MFPINT, DATATYPES.DATUM_MFPDEC, DATATYPES.DATUM_COMPLEX)) {
            DataClass datumReturn = mdatumValue.cloneSelf();
            return datumReturn;
        } else {
            return mdatumValue;
        }
    }

    @Override
    protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException {
        ((AEConst)aexprOrigin).validateAbstractExpr();

        super.copy(aexprOrigin);
        mdatumValue = new DataClassNull();
        if (((AEConst)aexprOrigin).mdatumValue != null)    {
            mdatumValue = ((AEConst)aexprOrigin).mdatumValue;   // no need to deep copy coz AbstractExpr is immutable.
        }
    }

    @Override
    protected void copyDeep(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException {
        ((AEConst)aexprOrigin).validateAbstractExpr();

        super.copyDeep(aexprOrigin);
        mdatumValue = new DataClassNull();
        if (((AEConst)aexprOrigin).mdatumValue != null)    {
            // do not deep copy because AEConst is immutable means AEConst always refer to the same dataclass although dataclass itself can change..
            // another reason do not deep copy is, if deep copy datumValue, and if datumValue is DATUM_ABSTRACT_EXPR type, and DATUM_ABSTRACT_EXPR
            // type refer to this AEConst, then we will deep copy infinitely.
            mdatumValue = ((AEConst)aexprOrigin).mdatumValue;
        }
    }
    
    @Override
    public AbstractExpr cloneSelf() throws JFCALCExpErrException, JSmartMathErrException    {
        AbstractExpr aeReturn = new AEConst();
        aeReturn.copyDeep(this);
        return aeReturn;
    }
    
    @Override
    public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();
        return mdatumValue.recalcDataArraySize();
    }

    // note that progContext can be null. If null, it is unconditional equal.
    @Override
    public boolean isEqual(AbstractExpr aexpr, ProgContext progContext) throws JFCALCExpErrException {
        if (menumAEType != aexpr.menumAEType)    {
            return false;    // this has been able to confirm that aexpr must be a constant.
        } else if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_KNOWNVAR)    {
            return mdatumValue.isEqual(((AEConst)aexpr).mdatumValue);
        } else {
            return mdatumValue.isEqual(((AEConst)aexpr).mdatumValue)
                && mstrKnownVarName.equals(((AEConst)aexpr).mstrKnownVarName);
        }
    }
    
    @Override
    public int getHashCode()  throws JFCALCExpErrException {
        int hashRet = menumAEType.hashCode()
                + mdatumValue.getHashCode() * 13
                + mstrKnownVarName.hashCode() * 17;
        return hashRet;
    }

    // this function compare two abstract expr bit by bit. Note that it is only
    // used in suspend_until_cond function, which is called when a variable is
    // updated from a different call object. Because comparison between old
    // value and new value is NOT carried out in a call thread, class definition
    // and stack is invisible. As such, we cannot use user defined __equal__
    // function. We have to compare bit by bit. Generally this function only calls
    // isEqual. However, if AbstractExpr is an AEConst, we have to override it.
    @Override
    public boolean isBitEqual(AbstractExpr aexpr, ProgContext progContext) throws JFCALCExpErrException {
        if (menumAEType != aexpr.menumAEType)    {
            return false;    // this has been able to confirm that aexpr must be a constant.
        } else if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_KNOWNVAR)    {
            return mdatumValue.isBitEqual(((AEConst)aexpr).mdatumValue);
        } else {
            return mdatumValue.isBitEqual(((AEConst)aexpr).mdatumValue)
                && mstrKnownVarName.equals(((AEConst)aexpr).mstrKnownVarName);
        }
    }

    @Override
    public boolean isPatternMatch(AbstractExpr aePattern,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoFuncs,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts,
                                LinkedList<PatternExprUnitMap> listpeuMapUnknowns,
                                boolean bAllowConversion, ProgContext progContext) throws JFCALCExpErrException  {
        /* do not call isPatternDegrade function because we don't allow const abstractexpr degrade-matchs a pattern. Otherwise will be a lot of troubles.*/
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
        if (aePattern instanceof AEConst)   {
            if (((AEConst)aePattern).mdatumValue.isEqual(mdatumValue))  {
                return true;
            }
        } else if (aePattern.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_PSEUDOCONST) {
            // pseudo constant can pattern any constant.
            for (int idx = 0; idx < listpeuMapPseudoConsts.size(); idx ++)  {
                if (listpeuMapPseudoConsts.get(idx).maePatternUnit.isEqual(aePattern, progContext))    {
                    if (isEqual(listpeuMapPseudoConsts.get(idx).maeExprUnit, progContext))   {
                        // this pseudo-const variable has been mapped to a const and the const is the same as this
                        return true;
                    } else  {
                        // this pseudo-const variable has been mapped to a const and the const is not the same as this
                        return false;
                    }
                }
            }
            // the aePattern is an unknown variable and it hasn't been mapped to some expressions before.
            PatternExprUnitMap peuMap = new PatternExprUnitMap(this, aePattern);
            listpeuMapPseudoConsts.add(peuMap);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean isKnownValOrPseudo() {
        return true;
    }
    
    // note that the return list should not be changed.
    @Override
    public LinkedList<AbstractExpr> getListOfChildren()    {
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        return listChildren;
    }
    
    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws JFCALCExpErrException, JSmartMathErrException {
        if (listChildren != null && listChildren.size() != 0) {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);            
        }
        return this;    // AEConst does not have any child.
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
        validateAbstractExpr();
        return this;
    }
    
    // avoid to do any overhead work.
    // avoid to do any overhead work.
    @Override
    public DataClass evaluateAExprQuick(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr(); // still needs to do some basic validation.
        
        return getDataClass();          // need to do deep copy if not a matrix.
    }

    // avoid to do any overhead work.
    @Override
    public AbstractExpr evaluateAExpr(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr(); // still needs to do some basic validation.
        
        return this;        
    }
    
    @Override
    public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars, SimplifyParams simplifyParams, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr();
        // if the type was AEConst known var, it is now changed to AEConst value or AEConst dataref value.
        DataClass datumValue = mdatumValue;
        if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_ABSTRACT_EXPR) && !simplifyParams.mbNotSimplifyAExprDatum) {
            AbstractExpr aexpr = DCHelper.lightCvtOrRetDCAExpr(mdatumValue).getAExpr();    // mdatumValue is DataClassAExpr anyway.
            aexpr = aexpr.simplifyAExpr(lUnknownVars, simplifyParams, progContext);
            if (aexpr instanceof AEConst) {
                datumValue = ((AEConst)aexpr).mdatumValue;
            } else {
                datumValue = new DataClassAExpr(aexpr); // cannot simply use mdatumValue.setAexpr(aexpr) otherwise this is changed
            }
        }
        AEConst aexprReturn = new AEConst(datumValue);
        return aexprReturn;
    }

    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight) throws JFCALCExpErrException {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        switch (menumAEType)    {
            case ABSTRACTEXPR_VALUE:
            case ABSTRACTEXPR_KNOWNVAR:
                boolean bHasPosNegOpt = false;
                if (DCHelper.isDataClassType(((AEConst)this).mdatumValue, DATATYPES.DATUM_MFPINT, DATATYPES.DATUM_MFPDEC)
                        && DCHelper.lightCvtOrRetDCSingleNum(((AEConst)this).mdatumValue).getDataValue().isActuallyNegative()) {
                    bHasPosNegOpt = true;
                } else if (DCHelper.isDataClassType(((AEConst)this).mdatumValue, DATATYPES.DATUM_COMPLEX))   {
                    DataClassComplex datumConstValue = DCHelper.lightCvtOrRetDCComplex(((AEConst)this).mdatumValue);    // cast will be ok.
                    if ((datumConstValue.getReal().isActuallyZero() == false && datumConstValue.getImage().isActuallyZero() == false)
                            || datumConstValue.getReal().isActuallyNegative()
                            || datumConstValue.getImage().isActuallyNegative())   {
                        // means + or - operator is used. Note that when output 0 + 3*i  the output string is 3i
                        bHasPosNegOpt = true;
                    }
                }
                if (bHasPosNegOpt)  {
                    if ((enumAET.getValue() > ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POSNEG.getValue()
                                && enumAET.getValue() <= ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX.getValue())
                            || (enumAET.getValue() == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POSNEG.getValue() && nLeftOrRight <= 0))    {
                        return true;
                    }
                }
                if (DCHelper.isDataClassType(((AEConst)this).mdatumValue, DATATYPES.DATUM_ABSTRACT_EXPR)
                        && enumAET.getValue() <= ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX.getValue()
                        && DCHelper.lightCvtOrRetDCAExpr(((AEConst)this).mdatumValue).getAExpr().menumAEType.getValue() <= enumAET.getValue()) {
                    return true;    // need brackets anyway.
                }
                return false;
            default: // ABSTRACTEXPR_DATAREFVALUE: // no matter what operator on left or right, I do not need () coz I have [] already.
                return false;
        }
    }
    
    @Override
    public int compareAExpr(AbstractExpr aexpr) throws JSmartMathErrException, JFCALCExpErrException {
        if (menumAEType.getValue() < aexpr.menumAEType.getValue())    {
            return 1;
        } else if (menumAEType.getValue() > aexpr.menumAEType.getValue())    {
            return -1;
        } else if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_BASE_OBJECT)
                && DCHelper.isDataClassType(((AEConst)aexpr).mdatumValue, DATATYPES.DATUM_BASE_OBJECT)) {
            return 0;
        } else if (DCHelper.isDataClassType(((AEConst)aexpr).mdatumValue, DATATYPES.DATUM_BASE_OBJECT)) {
            return 1;
        } else if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_BASE_OBJECT)) {
            return -1;
        } else    {    // both of them are not pure objects.
            int[] nDim1 = mdatumValue.recalcDataArraySize();
            int[] nDim2 = ((AEConst)aexpr).mdatumValue.recalcDataArraySize();
            if (nDim1.length > nDim2.length)    {
                return 1;
            } else if (nDim1.length < nDim2.length)    {
                return -1;
            } else    {
                int idx;
                for (idx = 0; idx < nDim1.length; idx ++)    {
                    if (nDim1[idx] > nDim2[idx])    {
                        return 1;
                    } else if (nDim1[idx] < nDim2[idx])    {
                        return -1;
                    }
                }

                // dimensions are exactly the same.
                int nAExpr1DataType = 0, nAExpr2DataType = 0;
                if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_REF_EXTOBJ))    {
                    nAExpr1DataType = 4;
                } else if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_REF_FUNC))    {
                    nAExpr1DataType = 3;
                } else if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_ABSTRACT_EXPR))    {
                    nAExpr1DataType = 2;
                } else if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_STRING))    {
                    nAExpr1DataType = 1;
                } else    {
                    nAExpr1DataType = 0;
                }
                if (DCHelper.isDataClassType(((AEConst)aexpr).mdatumValue, DATATYPES.DATUM_REF_EXTOBJ))    {
                    nAExpr2DataType = 4;
                } else if (DCHelper.isDataClassType(((AEConst)aexpr).mdatumValue, DATATYPES.DATUM_REF_FUNC))    {
                    nAExpr2DataType = 3;
                } else if (DCHelper.isDataClassType(((AEConst)aexpr).mdatumValue, DATATYPES.DATUM_ABSTRACT_EXPR))    {
                    nAExpr2DataType = 2;
                } else if (DCHelper.isDataClassType(((AEConst)aexpr).mdatumValue, DATATYPES.DATUM_STRING))    {
                    nAExpr2DataType = 1;
                } else    {
                    nAExpr2DataType = 0;
                }
                if (nAExpr1DataType < nAExpr2DataType)    {
                    return 1;
                } else if (nAExpr1DataType > nAExpr2DataType)    {
                    return -1;
                } else if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_REF_EXTOBJ))    {
                    // aexpr.mdatumValue.GetDataType() is also DATATYPES.DATUM_REF_EXTOBJ, we can cast both to DataClassBuiltIn
                    return DataClassExtObjRef.compareObjs(
                    		DCHelper.lightCvtOrRetDCExtObjRef(mdatumValue).getExternalObject(),
                    		DCHelper.lightCvtOrRetDCExtObjRef(((AEConst)aexpr).mdatumValue).getExternalObject()
                    		);
                } else if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_REF_FUNC))    {
                    // aexpr.mdatumValue.GetDataType() is also DATATYPES.DATUM_REF_FUNC, we can cast both to DataClassBuiltIn
                    return DCHelper.lightCvtOrRetDCFuncRef(mdatumValue).getFunctionName().compareTo(DCHelper.lightCvtOrRetDCFuncRef(((AEConst)aexpr).mdatumValue).getFunctionName());
                } else if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_ABSTRACT_EXPR))    {
                    // aexpr.mdatumValue.GetDataType() is also DATATYPES.DATUM_ABSTRACT_EXPR, we can cast both to DataClassBuiltIn
                    return DCHelper.lightCvtOrRetDCAExpr(mdatumValue).getAExpr().compareAExpr(DCHelper.lightCvtOrRetDCAExpr(((AEConst)aexpr).mdatumValue).getAExpr());
                } else if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_STRING))    {
                    // aexpr.mdatumValue.GetDataType() is also DATATYPES.DATUM_REF_FUNC, we can cast both to DataClassBuiltIn
                    return DCHelper.lightCvtOrRetDCString(mdatumValue).getStringValue().compareTo(DCHelper.lightCvtOrRetDCString(((AEConst)aexpr).mdatumValue).getStringValue());
                } else if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_REF_DATA))    {
                    // we can cast both of them can be converted to array because they both are either array or single num or complex.
                    // also, do not populateDataArray because we want to compare original data.
                    DataClassArray datumValue1 = DCHelper.lightCvtOrRetDCArray(mdatumValue.cloneSelf());
                    //datumValue1.populateDataArray(nDim1, false);
                    DataClassArray datumValue2 = DCHelper.lightCvtOrRetDCArray(((AEConst)aexpr).mdatumValue.cloneSelf());
                    //datumValue2.populateDataArray(nDim2, false);
                    if (datumValue1.getDataListSize() > datumValue2.getDataListSize()) {
                        return 1;
                    } else if (datumValue1.getDataListSize() < datumValue2.getDataListSize()) {
                        return -1;
                    } else {
                        for (int idx1 = 0; idx1 < datumValue1.getDataListSize(); idx1 ++)   {
                            DataClass datumChild1 = datumValue1.getDataList()[idx1];
                            DataClass datumChild2 = datumValue2.getDataList()[idx1];
                            if (datumChild1 == null && datumChild2 == null) {
                                continue;
                            } else if (datumChild1 != null) {
                                return 1;
                            } else if (datumChild2 != null) {
                                return -1;
                            } else {    // datumChild1 and 2 are both not null.
                                AEConst aeChild1 = new AEConst(datumChild1);
                                AEConst aeChild2 = new AEConst(datumChild2);
                                int nComparedResult = aeChild1.compareAExpr(aeChild2);
                                if (nComparedResult != 0)   {
                                    return nComparedResult;
                                }
                            }
                        }
                        return 0;
                    }
                } else if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_MFPBOOL, DATATYPES.DATUM_MFPINT,
                                                    DATATYPES.DATUM_MFPDEC, DATATYPES.DATUM_COMPLEX)
                        && DCHelper.isDataClassType(((AEConst)aexpr).mdatumValue, DATATYPES.DATUM_MFPBOOL,
                                                    DATATYPES.DATUM_MFPINT, DATATYPES.DATUM_MFPDEC, DATATYPES.DATUM_COMPLEX))  {
                    // the two values are real or image values. They can be converted to DataClassBuiltIn
                    MFPNumeric mfpNum1 = DCHelper.lightCvtOrRetDCComplex(mdatumValue).getReal();
                    MFPNumeric mfpNum2 = DCHelper.lightCvtOrRetDCComplex(((AEConst)aexpr).mdatumValue).getReal();
                    double dComparedResult = mfpNum2.compareTo(mfpNum1);
                    if (dComparedResult == 0)   {
                        mfpNum1 = DCHelper.lightCvtOrRetDCComplex(mdatumValue).getImage();
                        mfpNum2 = DCHelper.lightCvtOrRetDCComplex(((AEConst)aexpr).mdatumValue).getImage();
                        dComparedResult = mfpNum2.compareTo(mfpNum1);
                    }
                    return (int)dComparedResult;
                } else  {
                    return 0;   // NULL or invalid
                }
            }
        }
    }
    
    // identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
    @Override
    public boolean isNegligible(ProgContext progContext) throws JSmartMathErrException, JFCALCExpErrException    {
        validateAbstractExpr();
        // do not look on NULL as zero here.
        return DCHelper.isZeros(mdatumValue, false);
    }
    
    // output the string based expression of any abstract expression type.
    @Override
    public String output()    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        String strOutput = mdatumValue.output();
        return strOutput;
    }

    @Override
    public String outputWithFlag(int flag, ProgContext progContextNow)    throws JFCALCExpErrException, JSmartMathErrException {
        return output();
    }
    
    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException {
        return this;
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        if (DCHelper.isDataClassType(mdatumValue, DATATYPES.DATUM_ABSTRACT_EXPR)) {
            try {
                return DCHelper.lightCvtOrRetDCAExpr(mdatumValue).getAExpr();
            } catch (JFCALCExpErrException e) {
                // will not be here because mdatumValue's type must be a DataClassAExpr
                e.printStackTrace();
                return null;
            }
        } else {
            return this;
        }
    }

    @Override
    public int getVarAppearanceCnt(String strVarName) {
        return 0;   // datum aexpr is not considered.
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) {
        return new LinkedList<ModuleInfo>();
    }
}
