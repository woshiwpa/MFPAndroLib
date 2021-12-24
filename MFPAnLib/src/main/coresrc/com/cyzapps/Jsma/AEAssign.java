// MFP project, AEAssign.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.ModuleInfo;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import com.cyzapps.Oomfp.SpaceMember;

public class AEAssign extends AbstractExpr {

	private static final long serialVersionUID = 1L;
	public LinkedList<AbstractExpr> mlistChildren = new LinkedList<AbstractExpr>();

    public AEAssign() {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_ASSIGN;
        mlistChildren = new LinkedList<AbstractExpr>();
    }
    
    public AEAssign(LinkedList<AbstractExpr> listChildren) throws JSmartMathErrException    {
        setAEAssign(listChildren);
    }

    public AEAssign(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException    {
        copy(aexprOrigin);
    }

    @Override
    public void validateAbstractExpr() throws JSmartMathErrException    {
        if (mlistChildren.size() < 2)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_OPERATOR_SHOULD_HAVE_AT_LEAST_TWO_OPERANDS);
        }
        for (int idx = 0; idx < mlistChildren.size() - 1; idx ++)    {
            if (mlistChildren.get(idx).isVariable() == false)    {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_CAN_ONLY_ASSIGN_TO_A_VARIABLE);
            }
        }
    }
    
    private void setAEAssign(LinkedList<AbstractExpr> listChildren) throws JSmartMathErrException    {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_ASSIGN;
        mlistChildren = (listChildren == null)?new LinkedList<AbstractExpr>():listChildren;
        validateAbstractExpr();
    }
    
    @Override
    protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
            JSmartMathErrException {
        ((AEAssign)aexprOrigin).validateAbstractExpr();
        super.copy(aexprOrigin);
        mlistChildren = new LinkedList<AbstractExpr>();
        if (((AEAssign)aexprOrigin).mlistChildren != null)    {
            mlistChildren.addAll(((AEAssign)aexprOrigin).mlistChildren);
        }
    }

    @Override
    protected void copyDeep(AbstractExpr aexprOrigin)
            throws JFCALCExpErrException, JSmartMathErrException {
        ((AEAssign)aexprOrigin).validateAbstractExpr();
        super.copyDeep(aexprOrigin);
        mlistChildren = new LinkedList<AbstractExpr>();
        if (((AEAssign)aexprOrigin).mlistChildren != null)    {
            for (int idx = 0; idx < ((AEAssign)aexprOrigin).mlistChildren.size(); idx ++)    {
                AbstractExpr aexprChild = ((AEAssign)aexprOrigin).mlistChildren.get(idx).cloneSelf();
                mlistChildren.add(aexprChild);
            }
        }
    }

    @Override
    public AbstractExpr cloneSelf() throws JFCALCExpErrException, JSmartMathErrException    {
        AbstractExpr aeReturn = new AEAssign();
        aeReturn.copyDeep(this);
        return aeReturn;
    }
    
    @Override
    public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();

        return mlistChildren.getLast().recalcAExprDim(bUnknownAsSingle);
    }

    // note that progContext can be null. If null, it is unconditional equal.
    @Override
    public boolean isEqual(AbstractExpr aexpr, ProgContext progContext) throws JFCALCExpErrException {
        if (menumAEType != aexpr.menumAEType)    {
            return false;
        } else if (mlistChildren.size() != ((AEAssign)aexpr).mlistChildren.size())    {
            return false;
        } else {
            for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
                if (mlistChildren.get(idx).isEqual(((AEAssign)aexpr).mlistChildren.get(idx), progContext) == false)    {
                    return false;
                }
            }
            return true;
        }
    }
    
    @Override
    public int getHashCode()  throws JFCALCExpErrException {
        int hashRet = menumAEType.hashCode();
        for (int idx = 0; idx < mlistChildren.size(); idx ++) {
            hashRet = hashRet * 13 + mlistChildren.get(idx).getHashCode();
        }
        return hashRet;        
    }
    
    @Override
    public boolean isPatternMatch(AbstractExpr aePattern,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoFuncs,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts,
                                LinkedList<PatternExprUnitMap> listpeuMapUnknowns,
                                boolean bAllowConversion, ProgContext progContext)  throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
        /* do not call isPatternDegrade function because generally assign expression cannot degrade-match a pattern.*/
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
        if (!(aePattern instanceof AEAssign))   {
            return false;
        }
        if (mlistChildren.size() != ((AEAssign)aePattern).mlistChildren.size()) {
            return false;
        }
        for (int idx = 0; idx < mlistChildren.size(); idx ++)   {
            if (mlistChildren.get(idx).isPatternMatch(((AEAssign)aePattern).mlistChildren.get(idx),
                                                    listpeuMapPseudoFuncs,
                                                    listpeuMapPseudoConsts,
                                                    listpeuMapUnknowns,
                                                    bAllowConversion,
                                                    progContext) == false)   {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isKnownValOrPseudo() {
        for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
            if ( mlistChildren.get(idx).isKnownValOrPseudo() == false)    {
                return false;
            }
        }
        return true;
    }
    
    // note that the return list should not be changed.
    @Override
    public LinkedList<AbstractExpr> getListOfChildren()    {
        return mlistChildren;
    }
    
    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws JFCALCExpErrException, JSmartMathErrException {
        AEAssign aeReturn = new AEAssign();
        aeReturn.copy(this);
        aeReturn.mlistChildren = listChildren == null?new LinkedList<AbstractExpr>():listChildren;
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }
    
    // this function replaces children who equal aeFrom to aeTo and
    // returns the number of children that are replaced.
    @Override
    public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern,
            LinkedList<AbstractExpr> listReplacedChildren, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException    {
        AEAssign aeReturn = new AEAssign();
        aeReturn.copy(this);
        for (int idx = 0; idx < aeReturn.mlistChildren.size(); idx ++)    {
            for (int idx1 = 0; idx1 < listFromToMap.size(); idx1 ++)    {
                if (bExpr2Pattern && aeReturn.mlistChildren.get(idx).isEqual(listFromToMap.get(idx1).maeExprUnit, progContext))    {
                    aeReturn.mlistChildren.set(idx, listFromToMap.get(idx1).maePatternUnit);
                    listReplacedChildren.add(aeReturn.mlistChildren.get(idx));
                    break;
                } else if ((!bExpr2Pattern) && aeReturn.mlistChildren.get(idx).isEqual(listFromToMap.get(idx1).maePatternUnit, progContext))    {
                    aeReturn.mlistChildren.set(idx, listFromToMap.get(idx1).maeExprUnit);
                    listReplacedChildren.add(aeReturn.mlistChildren.get(idx));
                    break;
                }
            }
        }
        return aeReturn;
    }
    
    // assignment cannot be distributed.
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
        DataClass datumReturn = mlistChildren.getLast().evaluateAExprQuick(lUnknownVars, progContext);
        for (int idx = mlistChildren.size() - 2; idx >= 0 ; idx --)    {    // have to go through from last to first consider the following case:
                                                                        // x[y] = y = 3. If y is not known, x[y] cannot be evaluated.
            // if child idx hasn't been simplified, child idx - 1 cannot be simplified considering the situation y = -1; x[y] = y = 3
            if (mlistChildren.get(idx).isVariable())    {
                if (mlistChildren.get(idx) instanceof AEVar)    {
                    String strVariableName = ((AEVar)mlistChildren.get(idx)).mstrVariableName;
                    Variable var = UnknownVarOperator.lookUpList(strVariableName, lUnknownVars);
                    // first lookup unknown variable list, then lookup known variable space.
                    if (var == null)    {
                        var = VariableOperator.lookUpSpaces(strVariableName, progContext.mdynamicProgContext.mlVarNameSpaces);
                        if (var == null)    {
                            throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_UNDEFINED_VARIABLE);
                        }
                    }
                    var.setValue(datumReturn);  // datumReturn should be a deep copy of single type or a shallow copy of a matrix.
                } else if (mlistChildren.get(idx) instanceof AEObjMember)    {
                    DataClass datumOwner = ((AEObjMember)mlistChildren.get(idx)).maeOwner.evaluateAExprQuick(lUnknownVars, progContext);
                    AEVar aeMemberVariable = (AEVar)((AEObjMember)mlistChildren.get(idx)).maeMember;
                    String varName = aeMemberVariable.mstrVariableName;
                    Variable var = DCHelper.lightCvtOrRetDCClass(datumOwner).getClassInstance()
                            .getMemberVariable(((AEObjMember)mlistChildren.get(idx)).mstrOwnerName, varName,
                                    SpaceMember.AccessRestriction.PRIVATE, progContext);
                    // first lookup unknown variable list, then lookup known variable space.
                    if (var == null)    {
                        throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_MEMBER_NOT_EXIST);
                    }
                    var.setValue(datumReturn);  // datumReturn should be a deep copy of single type or a shallow copy of a matrix.
                } else    {    // AEIndex with base is a variable.
                    AbstractExpr aeBase = ((AEIndex)mlistChildren.get(idx)).maeBase;
                    AbstractExpr aeIndex = ((AEIndex)mlistChildren.get(idx)).maeIndex;
                    AbstractExpr aeMerged = mlistChildren.get(idx);
                    try    {
                        aeMerged = AEIndex.mergeIndex(aeBase, aeIndex);
                    } catch (JSmartMathErrException e)    {
                        if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)    {
                            throw e;
                        }
                    }
                    DataClass datumNewIndex = ((AEIndex)aeMerged).maeIndex.evaluateAExprQuick(lUnknownVars, progContext);
                    if (!DCHelper.isDataClassType(datumNewIndex, DATATYPES.DATUM_REF_DATA)) {
                        // if a constant AEIndex cannot be converted to an array, it must be an
                        // invalid AEIndex.
                        throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_DATA_TYPE);
                    }
                    DataClass[] datumIndexList = DCHelper.lightCvtOrRetDCArray(datumNewIndex).getDataList();
                    int[] nIndexArray = new int[datumIndexList.length];
                    for (int idx1 = 0; idx1 < datumIndexList.length; idx1 ++)    {
                        DataClassSingleNum datumThisIdx = DCHelper.lightCvtOrRetDCMFPInt(datumIndexList[idx1]);
                        // now datumThisIdx must be a DataClassBuiltIn type.
                        nIndexArray[idx1] = (int)datumThisIdx.getDataValue().longValue();
                    }
                    // now aeMerged must be an AEIndex type, and it's base must be a member variable or a variable.
                    Variable var = null;
                    if (((AEIndex)aeMerged).maeBase instanceof AEObjMember) {
                        DataClass datumOwner = ((AEObjMember)((AEIndex)aeMerged).maeBase).maeOwner.evaluateAExprQuick(lUnknownVars, progContext);
                        AEVar aeMemberVariable = (AEVar)((AEObjMember)((AEIndex)aeMerged).maeBase).maeMember;
                        String varName = aeMemberVariable.mstrVariableName;
                        var = DCHelper.lightCvtOrRetDCClass(datumOwner).getClassInstance()
                                .getMemberVariable(((AEObjMember)((AEIndex)aeMerged).maeBase).mstrOwnerName, varName,
                                        SpaceMember.AccessRestriction.PRIVATE, progContext);
                        // first lookup unknown variable list, then lookup known variable space.
                        if (var == null)    {
                            throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_MEMBER_NOT_EXIST);
                        }
                    } else {                        
                        AEVar aeNewBase = (AEVar)((AEIndex)aeMerged).maeBase;
                        String strVariableName = aeNewBase.mstrVariableName;
                        var = UnknownVarOperator.lookUpList(strVariableName, lUnknownVars);
                        // first lookup unknown variable list, then lookup known variable space.
                        if (var == null)    {
                            var = VariableOperator.lookUpSpaces(strVariableName, progContext.mdynamicProgContext.mlVarNameSpaces);
                            if (var == null)    {
                                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_UNDEFINED_VARIABLE);
                            }
                        }
                    }
                    DataClass datumValue = new DataClassNull();
                    if ((var instanceof UnknownVariable && ((UnknownVariable)var).isValueAssigned())
                            || var instanceof Variable)    {
                        datumValue = var.getValue();
                        if (!DCHelper.isDataClassType(datumValue, DATATYPES.DATUM_REF_DATA)) {
                            // this means datumValue cannot be an object.
                            throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_DATA_TYPE);
                        }
                    }
                    DataClassArray datumArrayVal = DCHelper.lightCvtOrRetDCArrayNoExcept(datumValue);
                    datumArrayVal.setValidDataAtIndexByRef(nIndexArray, datumReturn);
                    // no need to validate because setValidDataAtIndexByRef has done this; // prevent refer to itself.
                    var.setValue(datumArrayVal);
                }
            } else    {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CANNOT_ASSIGN_VALUE_TO_ANYTHING_EXCEPT_VARIALBE_USE_EQUAL_INSTEAD_AT_RUN_TIME);
            }
        }
        return datumReturn;
    }

    // avoid to do any overhead work.
    @Override
    public AbstractExpr evaluateAExpr(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr(); // still needs to do some basic validation.
        LinkedList<AbstractExpr> listNewChildren = new LinkedList<AbstractExpr>();
        AbstractExpr aeReturn = mlistChildren.getLast().evaluateAExpr(lUnknownVars, progContext);
        DataClass datumReturn = (aeReturn instanceof AEConst)?((AEConst)aeReturn).getDataClass():null;   // use data ref coz aeReturn may be a matrix.
        listNewChildren.addFirst(aeReturn);
        for (int idx = mlistChildren.size() - 2; idx >= 0 ; idx --)    {    // have to go through from last to first consider the following case:
                                                                        // x[y] = y = 3. If y is not known, x[y] cannot be evaluated.
            // if child idx hasn't been simplified, child idx - 1 cannot be simplified considering the situation y = -1; x[y] = y = 3
            if (mlistChildren.get(idx).isVariable())    {
                if (mlistChildren.get(idx) instanceof AEVar)    {
                    if (aeReturn instanceof AEConst) {
                        String strVariableName = ((AEVar)mlistChildren.get(idx)).mstrVariableName;
                        Variable var = UnknownVarOperator.lookUpList(strVariableName, lUnknownVars);
                        // first lookup unknown variable list, then lookup known variable space.
                        if (var == null)    {
                            var = VariableOperator.lookUpSpaces(strVariableName, progContext.mdynamicProgContext.mlVarNameSpaces);
                            if (var == null)    {
                                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_UNDEFINED_VARIABLE);
                            }
                        }
                        DataClass datumValue = datumReturn.copySelf();  // do not deep copy considering a matrix (reference copy)
                        var.setValue(datumValue);
                    } else {
                        listNewChildren.addFirst(mlistChildren.get(idx));
                    }
                } else if (mlistChildren.get(idx) instanceof AEObjMember)    {
                    if (aeReturn instanceof AEConst) {
                        AbstractExpr aeOwner = ((AEObjMember)mlistChildren.get(idx)).maeOwner.evaluateAExpr(lUnknownVars, progContext);
                        if (aeOwner instanceof AEConst) {
                            DataClass datumOwner = ((AEConst)aeOwner).getDataClassRef();
                            AEVar aeMemberVariable = (AEVar)((AEObjMember)mlistChildren.get(idx)).maeMember;
                            String varName = aeMemberVariable.mstrVariableName;
                            Variable var = DCHelper.lightCvtOrRetDCClass(datumOwner).getClassInstance()
                                    .getMemberVariable(((AEObjMember)mlistChildren.get(idx)).mstrOwnerName, varName,
                                            SpaceMember.AccessRestriction.PRIVATE, progContext);
                            // first lookup unknown variable list, then lookup known variable space.
                            if (var == null)    {
                                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_MEMBER_NOT_EXIST);
                            }
                            var.setValue(datumReturn);  // datumReturn should be a deep copy of single type or a shallow copy of a matrix.
                        } else {
                            listNewChildren.addFirst(mlistChildren.get(idx));
                        }
                    } else {
                        listNewChildren.addFirst(mlistChildren.get(idx));
                    }
                } else    {    // AEIndex with base is a variable.
                    AbstractExpr aeBase = ((AEIndex)mlistChildren.get(idx)).maeBase;
                    AbstractExpr aeIndex = ((AEIndex)mlistChildren.get(idx)).maeIndex;
                    AbstractExpr aeMerged = mlistChildren.get(idx);
                    try    {
                        aeMerged = AEIndex.mergeIndex(aeBase, aeIndex);
                    } catch (JSmartMathErrException e)    {
                        if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)    {
                            throw e;
                        }
                    }
                    // now aeMerged must be an AEIndex type, and it's base must be a variable.
                    AbstractExpr aeNewIndex = ((AEIndex)aeMerged).maeIndex.evaluateAExpr(lUnknownVars, progContext);
                    if (aeReturn instanceof AEConst && aeNewIndex instanceof AEConst) {
                        DataClass datumNewIndex = ((AEConst)aeNewIndex).getDataClassRef();
                        if (!DCHelper.isDataClassType(datumNewIndex, DATATYPES.DATUM_REF_DATA)) {
                            throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_DATA_TYPE);
                        }
                        DataClass[] datumIndexList = DCHelper.lightCvtOrRetDCArray(datumNewIndex).getDataList();
                        int[] nIndexArray = new int[datumIndexList.length];
                        for (int idx1 = 0; idx1 < datumIndexList.length; idx1 ++)    {
                            DataClassSingleNum datumThisIdx = DCHelper.lightCvtOrRetDCMFPInt(datumIndexList[idx1]);
                            // now datumThisIdx must be a DataClassBuiltIn
                            nIndexArray[idx1] = (int)datumThisIdx.getDataValue().longValue();
                        }
                        // now aeMerged must be an AEIndex type, and it's base must be a member variable or a variable.
                        Variable var = null;
                        Boolean baseIsKnown = true;  // true means ((AEIndex)aeMerged).maeBase is known value so that we can assign a new value to it. Otherwise false.
                        if (((AEIndex)aeMerged).maeBase instanceof AEObjMember) {
                            AbstractExpr aeOwner = ((AEObjMember)((AEIndex)aeMerged).maeBase).maeOwner.evaluateAExpr(lUnknownVars, progContext);
                            if (aeOwner instanceof AEConst) {
                                DataClass datumOwner = ((AEConst)aeOwner).getDataClassRef();
                                AEVar aeMemberVariable = (AEVar)((AEObjMember)((AEIndex)aeMerged).maeBase).maeMember;
                                String varName = aeMemberVariable.mstrVariableName;
                                var = DCHelper.lightCvtOrRetDCClass(datumOwner).getClassInstance()
                                        .getMemberVariable(((AEObjMember)((AEIndex)aeMerged).maeBase).mstrOwnerName, varName,
                                                SpaceMember.AccessRestriction.PRIVATE, progContext);
                                // first lookup unknown variable list, then lookup known variable space.
                                if (var == null)    {
                                    throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_MEMBER_NOT_EXIST);
                                }
                            } else {
                                baseIsKnown = false;
                            }
                        } else {                        
                            AEVar aeNewBase = (AEVar)((AEIndex)aeMerged).maeBase;
                            String strVariableName = aeNewBase.mstrVariableName;
                            var = UnknownVarOperator.lookUpList(strVariableName, lUnknownVars);
                            // first lookup unknown variable list, then lookup known variable space.
                            if (var == null)    {
                                var = VariableOperator.lookUpSpaces(strVariableName, progContext.mdynamicProgContext.mlVarNameSpaces);
                                if (var == null)    {
                                    throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_UNDEFINED_VARIABLE);
                                }
                            }
                        }
                        if (baseIsKnown) {
                            DataClass datumValue = new DataClassNull();
                            if ((var instanceof UnknownVariable && ((UnknownVariable)var).isValueAssigned())
                                    || var instanceof Variable)    {
                                datumValue = var.getValue();
                                if (!DCHelper.isDataClassType(datumValue, DATATYPES.DATUM_REF_DATA)) {
                                    // this means datumValue cannot be an object.
                                    throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_DATA_TYPE);
                                }
                            }
                            DataClass datumReturnCp = datumReturn.copySelf();  // do not deep copy considering a matrix (reference copy).
                            DataClassArray datumArrayVal = DCHelper.lightCvtOrRetDCArrayNoExcept(datumValue);
                            datumArrayVal.setValidDataAtIndexByRef(nIndexArray, datumReturnCp);
                            // no need to validate because setValidDataAtIndexByRef has done this // prevent refer to itself.
                            var.setValue(datumArrayVal);
                        } else {
                            AEIndex aeNewChild = new AEIndex(((AEIndex)aeMerged).maeBase, aeNewIndex);
                            listNewChildren.addFirst(aeNewChild);
                        }
                    } else {
                        AEIndex aeNewChild = new AEIndex(((AEIndex)aeMerged).maeBase, aeNewIndex);
                        listNewChildren.addFirst(aeNewChild);
                    }
                }
            } else    {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CANNOT_ASSIGN_VALUE_TO_ANYTHING_EXCEPT_VARIALBE_USE_EQUAL_INSTEAD_AT_RUN_TIME);
            }
        }
        if (listNewChildren.size() == 1) {
            return aeReturn;
        } else {
            return new AEAssign(listNewChildren);
        }        
    }
    // exceptions like cannot merge or unknown variable should be handled internally in this function.
    // if an exception is thrown out, something is wrong.
    @Override
    public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars, SimplifyParams simplifyParams, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr();

        // ensure that this will not be changed.
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        listChildren.addAll(mlistChildren);
        
        // only simplify the last one, other children must be variables and should
        // be simplified gradually.
        AbstractExpr aexpr = listChildren.getLast().simplifyAExpr(lUnknownVars, simplifyParams, progContext);
        listChildren.set(listChildren.size() - 1, aexpr);
        
        // merge children.
        for (int idx = listChildren.size() - 1; idx >= 1; idx --)    {
            try {
                AbstractExpr aeMerged = mergeAssign(listChildren.get(idx - 1), listChildren.get(idx));
                listChildren.remove(idx - 1);
                listChildren.remove(idx - 1);
                listChildren.add(idx - 1, aeMerged);
            } catch (JSmartMathErrException e)    {
                if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)    {
                    throw e;
                }
            }
        }
        
        if (listChildren.getLast() instanceof AEConst)    {
            DataClass datumReturn = ((AEConst)listChildren.getLast()).getDataClass();    // getDataClass will automatically determine whether to deepcopy or not.
            
            for (int idx = listChildren.size() - 2; idx >= 0 ; idx --)    {    // have to go through from last to first consider the following case:
                                                                            // x[y] = y = 3. If y is not known, x[y] cannot be evaluated.
                // if child idx hasn't been simplified, child idx - 1 cannot be simplified considering the situation y = -1; x[y] = y = 3
                boolean bChildCanBeRemoved = true;
                if (listChildren.get(idx).isVariable())    {
                    if (listChildren.get(idx) instanceof AEVar)    {
                        String strVariableName = ((AEVar)listChildren.get(idx)).mstrVariableName;
                        Variable var = UnknownVarOperator.lookUpList(strVariableName, lUnknownVars);
                        // first lookup unknown variable list, then lookup known variable space.
                        if (var == null)    {
                            var = VariableOperator.lookUpSpaces(strVariableName, progContext.mdynamicProgContext.mlVarNameSpaces);
                            if (var == null)    {
                                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_UNDEFINED_VARIABLE);
                            }
                        }
                        DataClass datumValue = datumReturn.copySelf();  // do not deep copy considering a matrix (reference copy).
                        var.setValue(datumValue);
                    } else if (listChildren.get(idx) instanceof AEObjMember) {
                        AbstractExpr aeOwner = ((AEObjMember)mlistChildren.get(idx)).maeOwner.simplifyAExpr(lUnknownVars, simplifyParams, progContext);
                        if (aeOwner instanceof AEConst) {
                            DataClass datumOwner = ((AEConst) aeOwner).getDataClassRef();   // no need to clone owner's dataclass.
                            AEVar aeMemberVariable = (AEVar)((AEObjMember)mlistChildren.get(idx)).maeMember;
                            String varName = aeMemberVariable.mstrVariableName;
                            Variable var = DCHelper.lightCvtOrRetDCClass(datumOwner).getClassInstance()
                                    .getMemberVariable(((AEObjMember)mlistChildren.get(idx)).mstrOwnerName, varName,
                                            SpaceMember.AccessRestriction.PRIVATE, progContext);
                            // first lookup unknown variable list, then lookup known variable space.
                            if (var == null)    {
                                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_MEMBER_NOT_EXIST);
                            }
                            var.setValue(datumReturn);  // datumReturn should be a deep copy of single type or a shallow copy of a matrix.
                        } else {
                            bChildCanBeRemoved = false;
                        }
                    } else    {    // AEIndex with base is a variable.
                        AbstractExpr aeBase = ((AEIndex)listChildren.get(idx)).maeBase;
                        AbstractExpr aeIndex = ((AEIndex)listChildren.get(idx)).maeIndex;
                        AbstractExpr aeMerged = listChildren.get(idx);
                        try    {
                            aeMerged = AEIndex.mergeIndex(aeBase, aeIndex);
                        } catch (JSmartMathErrException e)    {
                            if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)    {
                                throw e;
                            }
                        }
                        // now aeMerged must be an AEIndex type, and it's base must be a variable.
                        AbstractExpr aeNewIndex = ((AEIndex)aeMerged).maeIndex.simplifyAExpr(lUnknownVars, simplifyParams, progContext);
                        if (!(aeNewIndex instanceof AEConst))    {
                            // some indices are still unknown.
                            listChildren.set(idx, aeMerged);
                            bChildCanBeRemoved = false;
                        } else    {
                            DataClassArray datumIndexCpy = DCHelper.lightCvtOrRetDCArray(((AEConst)aeNewIndex).getDataClassCopy());
                            /*// no need to check type because lightCvtOrRetDCArray has done it.
                             * if (!(datumIndexCpy instanceof DataClassArray))    {
                                throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
                            }*/
                            DataClass[] datumIndexList = datumIndexCpy.getDataList();
                            int[] nIndexArray = new int[datumIndexList.length];
                            for (int idx1 = 0; idx1 < datumIndexList.length; idx1 ++)    {
                                DataClassSingleNum datumThisIndex = DCHelper.lightCvtOrRetDCMFPInt(datumIndexList[idx1]);
                                // now datumThisIndex must be a DataClassBuiltIn type.
                                nIndexArray[idx1] = (int)datumThisIndex.getDataValue().longValue();
                            }
                            Variable var = null;
                            if (((AEIndex)aeMerged).maeBase instanceof AEObjMember) {
                                AbstractExpr aeOwner = ((AEObjMember)((AEIndex)aeMerged).maeBase).maeOwner.simplifyAExpr(lUnknownVars, simplifyParams, progContext);
                                if (aeOwner instanceof AEConst) {
                                    DataClass datumOwner = ((AEConst) aeOwner).getDataClassRef();   // no need to clone owner's dataclass.
                                    AEVar aeMemberVariable = (AEVar)((AEObjMember)((AEIndex)aeMerged).maeBase).maeMember;
                                    String varName = aeMemberVariable.mstrVariableName;
                                    var = DCHelper.lightCvtOrRetDCClass(datumOwner).getClassInstance()
                                            .getMemberVariable(((AEObjMember)((AEIndex)aeMerged).maeBase).mstrOwnerName, varName,
                                                    SpaceMember.AccessRestriction.PRIVATE, progContext);
                                    // first lookup unknown variable list, then lookup known variable space.
                                    if (var == null)    {
                                        throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_MEMBER_NOT_EXIST);
                                    }
                                } else {
                                    bChildCanBeRemoved = false;
                                }
                            } else {
                                AEVar aeNewBase = (AEVar)((AEIndex)aeMerged).maeBase;
                                String strVariableName = aeNewBase.mstrVariableName;
                                var = UnknownVarOperator.lookUpList(strVariableName, lUnknownVars);
                                // first lookup unknown variable list, then lookup known variable space.
                                if (var == null)    {
                                    var = VariableOperator.lookUpSpaces(strVariableName, progContext.mdynamicProgContext.mlVarNameSpaces);
                                    if (var == null)    {
                                        throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_UNDEFINED_VARIABLE);
                                    }
                                }
                            }
                            if (bChildCanBeRemoved) {
                                DataClass datumValue = new DataClassNull();
                                if ((var instanceof UnknownVariable && ((UnknownVariable)var).isValueAssigned())
                                        || var instanceof Variable)    {
                                    datumValue = var.getValue();
                                    if (!DCHelper.isDataClassType(datumValue, DATATYPES.DATUM_REF_DATA)) {
                                        // this means datumValue cannot be an object.
                                        throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_DATA_TYPE);
                                    }
                                }
                                DataClass datumReturnCp = datumReturn.copySelf();  // do not deep copy considering a matrix (reference copy).
                                DataClassArray datumArrayVal = DCHelper.lightCvtOrRetDCArrayNoExcept(datumValue);
                                datumArrayVal.setValidDataAtIndexByRef(nIndexArray, datumReturnCp);
                                // no need to validate because setValidDataAtIndexByRef has done this // prevent refer to itself.
                                var.setValue(datumArrayVal);
                            }
                        }
                    }
                } else    {
                    throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CANNOT_ASSIGN_VALUE_TO_ANYTHING_EXCEPT_VARIALBE_USE_EQUAL_INSTEAD_AT_RUN_TIME);
                }
                if (bChildCanBeRemoved)    {
                    // the variable has been known, remove it from child list.
                    // or if it is not a variable, remove it anyway.
                    listChildren.remove(idx);
                    idx --;
                }
            }
        }
        if (listChildren.size() == 1)    {
            return listChildren.get(0);    // if a variable aexpr, it has been converted to const in its simplifyexpr function.
        } else    {
            return new AEAssign(listChildren);
        }
    }
    
    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        if (enumAET == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_ASSIGN
                && (nLeftOrRight >= 0)) {   // like (a = b) = c or d = (a = b) = c
            return true;
        } else if (enumAET.getValue() > ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV.getValue()
                    && enumAET.getValue() <= ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX.getValue())    {
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
            int nChildrenListSize1 = mlistChildren.size();
            int nChildrenListSize2 = ((AEAssign)aexpr).mlistChildren.size();
            for (int idx = Math.min(nChildrenListSize1, nChildrenListSize2) - 1; idx >= 0; idx --)    {
                int nCompareChildReturn = mlistChildren.get(idx).compareAExpr(((AEAssign)aexpr).mlistChildren.get(idx));
                if (nCompareChildReturn != 0)    {
                    return nCompareChildReturn;
                }
            }
            if (nChildrenListSize1 > nChildrenListSize2)    {
                return 1;
            } else if (nChildrenListSize1 < nChildrenListSize2)    {
                return -1;
            } else    {
                return 0;
            }
        }
    }
    
    // identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
    @Override
    public boolean isNegligible(ProgContext progContext) throws JSmartMathErrException, JFCALCExpErrException    {
        validateAbstractExpr();
        return mlistChildren.getLast().isNegligible(progContext);
    }
    
    // output the string based expression of any abstract expression type.
    @Override
    public String output()    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        String strOutput = "";
        for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
            boolean bNeedBracketsWhenToStr = false;
            if (idx > 0 && idx < mlistChildren.size() - 1)  {
                bNeedBracketsWhenToStr = mlistChildren.get(idx).needBracketsWhenToStr(menumAEType, 0);
            } else if (idx == 0 && idx < mlistChildren.size() - 1)  {
                bNeedBracketsWhenToStr = mlistChildren.get(idx).needBracketsWhenToStr(menumAEType, 1);
            } else if (idx > 0 && idx == mlistChildren.size() - 1)  {
                bNeedBracketsWhenToStr = mlistChildren.get(idx).needBracketsWhenToStr(menumAEType, -1);
            }
            if (bNeedBracketsWhenToStr) {
                strOutput += "(" + mlistChildren.get(idx).output() + ")";
            } else  {
                strOutput += mlistChildren.get(idx).output();
            }
            if (idx != mlistChildren.size() - 1)    {
                strOutput += "=";
            }
        }
        return strOutput;
    }
    
    @Override
    public String outputWithFlag(int flag, ProgContext progContextNow)    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        String strOutput = "";
        for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
            boolean bNeedBracketsWhenToStr = false;
            if (idx > 0 && idx < mlistChildren.size() - 1)  {
                bNeedBracketsWhenToStr = mlistChildren.get(idx).needBracketsWhenToStr(menumAEType, 0);
            } else if (idx == 0 && idx < mlistChildren.size() - 1)  {
                bNeedBracketsWhenToStr = mlistChildren.get(idx).needBracketsWhenToStr(menumAEType, 1);
            } else if (idx > 0 && idx == mlistChildren.size() - 1)  {
                bNeedBracketsWhenToStr = mlistChildren.get(idx).needBracketsWhenToStr(menumAEType, -1);
            }
            if (bNeedBracketsWhenToStr) {
                strOutput += "(" + mlistChildren.get(idx).outputWithFlag(flag, progContextNow) + ")";
            } else  {
                strOutput += mlistChildren.get(idx).outputWithFlag(flag, progContextNow);
            }
            if (idx != mlistChildren.size() - 1)    {
                strOutput += "=";
            }
        }
        return strOutput;
    }
    
    // note that parameters of this function will be changed inside.
    public static AbstractExpr mergeAssign(AbstractExpr aeLeft, AbstractExpr aeRight) throws JFCALCExpErrException, JSmartMathErrException    {
        if (aeRight instanceof AEAssign)    {
            AEAssign aeReturn = new AEAssign();
            aeReturn.copy(aeRight);
            aeReturn.mlistChildren.addFirst(aeLeft);
            return aeReturn;
        }
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }
    
    public AEAssign[] splitIntoExprs() throws JSmartMathErrException, JFCALCExpErrException {
        /*
         * convert like y[x] = x = 5 to x = 5 and y[x] = x
         */
        validateAbstractExpr();

        AEAssign[] aearrayExprs = new AEAssign[mlistChildren.size() - 1];
        for (int idx = mlistChildren.size() - 1, idx1 = 0; idx >= 1; idx --, idx1 ++)    {
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            AbstractExpr child1 = mlistChildren.get(idx - 1);
            AbstractExpr child2 = mlistChildren.get(idx);
            listChildren.add(child1);
            listChildren.add(child2);
            aearrayExprs[idx1] = new AEAssign(listChildren);
        }
        return aearrayExprs;
    }

    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException, JFCALCExpErrException {
        LinkedList<AbstractExpr> listChildrenCvted = new LinkedList<AbstractExpr>();
        for (int idx = 0; idx < mlistChildren.size(); idx ++) {
            if (mlistChildren.get(idx) instanceof AEConst) {
                listChildrenCvted.add(mlistChildren.get(idx));
            } else {
                listChildrenCvted.add(mlistChildren.get(idx).convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars));
            }
        }
        return new AEAssign(listChildrenCvted);
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        LinkedList<AbstractExpr> listChildrenCvted = new LinkedList<AbstractExpr>();
        for (int idx = 0; idx < mlistChildren.size(); idx ++) {
            if (mlistChildren.get(idx) instanceof AEConst
                    && DCHelper.isDataClassType(((AEConst)mlistChildren.get(idx)).getDataClassRef(), DATATYPES.DATUM_ABSTRACT_EXPR)) {
                try {
                    listChildrenCvted.add(DCHelper.lightCvtOrRetDCAExpr(((AEConst)mlistChildren.get(idx)).getDataClassRef()).getAExpr());
                } catch (JFCALCExpErrException e) {
                    // Will not be here anyway because the type of ((AEConst)mlistChildren.get(idx)).getDataClassRef() has been checked.
                    e.printStackTrace();
                }
            } else {
                listChildrenCvted.add(mlistChildren.get(idx));
            }
        }
        return new AEAssign(listChildrenCvted);
    }

    @Override
    public int getVarAppearanceCnt(String strVarName) {
        int nCnt = 0;
        for (int idx = 0; idx < mlistChildren.size(); idx ++) {
            nCnt += mlistChildren.get(idx).getVarAppearanceCnt(strVarName);
        }
        return nCnt;
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) {
        return getReferredFunctionsFromAExprs(mlistChildren, progContext);
    }
    
}
