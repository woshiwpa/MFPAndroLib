// MFP project, AEObjMember.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassAExpr;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassClass;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jmfp.ModuleInfo;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.StatementType;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import com.cyzapps.Oomfp.MFPClassInstance;
import com.cyzapps.Oomfp.SpaceMember;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AEObjMember extends AbstractExpr {

	private static final long serialVersionUID = 1L;
    public String mstrOwnerName = "";   // owner name can include big letters and small letters, like constructor_cls("ABC") is totally valid
	public AbstractExpr maeOwner = AEInvalid.AEINVALID;
    public AbstractExpr maeMember = AEInvalid.AEINVALID;
    
    public AEObjMember() {
        menumAEType = AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_MEMBERVARIABLE;
        maeOwner = AEInvalid.AEINVALID;
        maeMember = AEInvalid.AEINVALID;
    }
    
    public AEObjMember(String strOwnerName, AbstractExpr aeOwner, AbstractExpr aeMember) throws JSmartMathErrException    {
        setAEObjMember(strOwnerName, aeOwner, aeMember);
    }

    public AEObjMember(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException    {
        copy(aexprOrigin);
    }

    @Override
    public void validateAbstractExpr() throws JSmartMathErrException {
        switch (menumAEType) {
            case ABSTRACTEXPR_MEMBERFUNCTION:
                if (!(maeMember instanceof AEFunction)) {
                    throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
                }   break;
            case ABSTRACTEXPR_MEMBERVARIABLE:
                if (!(maeMember instanceof AEVar)) {
                    throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
                }   break;
            default:
                throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
        }
    }
    
    private void setAEObjMember(String strOwnerName, AbstractExpr aeOwner, AbstractExpr aeMember) throws JSmartMathErrException    {
        if (aeMember instanceof AEVar) {
            menumAEType = AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_MEMBERVARIABLE;
        } else {
            menumAEType = AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_MEMBERFUNCTION;
        }
        mstrOwnerName = strOwnerName;
        maeOwner = aeOwner;
        maeMember = aeMember;
        validateAbstractExpr();
    }
    
    @Override
    protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
            JSmartMathErrException {
        ((AEObjMember)aexprOrigin).validateAbstractExpr();
        super.copy(aexprOrigin);
        mstrOwnerName = ((AEObjMember)aexprOrigin).mstrOwnerName;
        maeOwner = ((AEObjMember)aexprOrigin).maeOwner;
        maeMember = ((AEObjMember)aexprOrigin).maeMember;
    }

    @Override
    protected void copyDeep(AbstractExpr aexprOrigin)
            throws JFCALCExpErrException, JSmartMathErrException {
        ((AEObjMember)aexprOrigin).validateAbstractExpr();
        super.copy(aexprOrigin);
        mstrOwnerName = ((AEObjMember)aexprOrigin).mstrOwnerName;
        maeOwner = ((AEObjMember)aexprOrigin).maeOwner.cloneSelf();
        maeMember = ((AEObjMember)aexprOrigin).maeMember.cloneSelf();
    }

    @Override
    public AbstractExpr cloneSelf() throws JFCALCExpErrException,
            JSmartMathErrException {
        AbstractExpr aeReturn = new AEObjMember();
        aeReturn.copyDeep(this);
        return aeReturn;
    }

    @Override
    public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
            JFCALCExpErrException {
        if (bUnknownAsSingle) {
            return new int[0];
        }
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION);
    }

    // note that progContext can be null. If null, it is unconditional equal.
    @Override
    public boolean isEqual(AbstractExpr aexpr, ProgContext progContext) throws JFCALCExpErrException {
        if (menumAEType != aexpr.menumAEType)    {
            return false;
        } else if (mstrOwnerName.equalsIgnoreCase("self") != ((AEObjMember)aexpr).mstrOwnerName.equalsIgnoreCase("self")) { // use equalsIgnoreCase coz owner name may include big letters.
            // if one is self, the other is not, they may access different scope
            return false;
        } else if (maeOwner.isEqual(((AEObjMember)aexpr).maeOwner, progContext) == false)    {
            return false;
        } else {
            if (menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_MEMBERVARIABLE) {
                // isEqual function of AEVar only compares name and condition list. When comparing name,
                // progContext is not used. progContext is only used to compare condition. So we can simply
                // use isEqual function here.
                if (maeMember.isEqual(((AEObjMember)aexpr).maeMember, progContext) == false) {
                    return false;
                }
            } else {    // member function. It is different from member variable. We shouldn't get full cs name.
                AEFunction aeMember = (AEFunction)maeMember;
                AEFunction aeMember2Comp = (AEFunction)((AEObjMember)aexpr).maeMember;
                if (!aeMember.equals(aeMember2Comp.mstrShrinkedRawFuncName))    {
                    return false;
                } else if (aeMember.mlistChildren.size() != aeMember2Comp.mlistChildren.size())    {
                    return false;
                } else {
                    for (int idx = 0; idx < aeMember.mlistChildren.size(); idx ++)    {
                        if (aeMember.mlistChildren.get(idx).isEqual(aeMember2Comp.mlistChildren.get(idx), progContext) == false)    {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return true;
        }
    }
    
    @Override
    public int getHashCode()  throws JFCALCExpErrException {
        int hashRet = menumAEType.hashCode() + 7 * mstrOwnerName.hashCode() + 13 * maeOwner.getHashCode();
        hashRet = hashRet * 19 + maeMember.getHashCode();
        return hashRet;        
    }

    @Override
    public boolean isPatternMatch(AbstractExpr aePattern,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoFuncs,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts,
                                LinkedList<PatternExprUnitMap> listpeuMapUnknowns,
                                boolean bAllowConversion, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        /* do not call isPatternDegrade function because generally index expression cannot degrade-match a pattern.*/
        if (aePattern.menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE)   {
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
        if (!(aePattern instanceof AEObjMember))  {
            return false;
        }
        if (menumAEType != aePattern.menumAEType) {
            return false;
        }
        if (maeOwner.isPatternMatch(((AEObjMember)aePattern).maeOwner, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion, progContext) == false)  {
            return false;
        }
        if (menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_MEMBERVARIABLE) {
            // member variable.
            if (maeMember.isPatternMatch(((AEObjMember)aePattern).maeMember, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion, progContext) == false)  {
                return false;
            } 
        } else {
            // member function. We have to do it in a different way.
            AEFunction thisMember = (AEFunction)maeMember;
            AEFunction patternMember = (AEFunction)((AEObjMember)aePattern).maeMember;
            if (!thisMember.mstrShrinkedRawFuncName.equals(patternMember.mstrShrinkedRawFuncName)) {
                return false;
            } else if (thisMember.mlistChildren.size() != patternMember.mlistChildren.size()) {
                return false;
            }
            for (int idx = 0; idx < thisMember.mlistChildren.size(); idx ++) {
                if (thisMember.mlistChildren.get(idx).isPatternMatch(patternMember.mlistChildren.get(idx),
                        listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion, progContext) == false) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    @Override
    public boolean isKnownValOrPseudo() {
        if (!maeOwner.isKnownValOrPseudo())    {
            return false;
        }
        if (!maeMember.isKnownValOrPseudo())    {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean isVariable() {
        if (menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_MEMBERFUNCTION) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public LinkedList<AbstractExpr> getListOfChildren() {
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        listChildren.add(maeOwner);
        listChildren.add(maeMember);
        return listChildren;
    }
    
    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws JFCALCExpErrException, JSmartMathErrException {
        if (listChildren == null || listChildren.size() != 2) {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);            
        }
        AEObjMember aeReturn = new AEObjMember();
        aeReturn.copy(this);
        aeReturn.maeOwner = listChildren.getFirst();
        aeReturn.maeMember = listChildren.getLast();
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }
    
    @Override
    public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern,
            LinkedList<AbstractExpr> listReplacedChildren, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException    {
        return this;
    }

    @Override
    public AbstractExpr distributeAExpr(AbstractExpr.SimplifyParams simplifyParams, ProgContext progContext) throws JFCALCExpErrException,
            JSmartMathErrException {
        validateAbstractExpr();
        
        return this;
    }

    // avoid to do any overhead work.
    @Override
    public DataClass evaluateAExprQuick(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr(); // still needs to do some basic validation.
        DataClass datumOwner = maeOwner.evaluateAExprQuick(lUnknownVars, progContext);
        if (menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_MEMBERVARIABLE) {
            // member variable
            DataClassClass memberVariableOwner = DCHelper.lightCvtOrRetDCClass(datumOwner);
            MFPClassInstance ownerInstance = memberVariableOwner.getClassInstance();
            String strVariableName = ((AEVar)maeMember).mstrVariableName;
            Variable var = ownerInstance.getMemberVariable(mstrOwnerName, strVariableName, SpaceMember.AccessRestriction.PRIVATE, progContext);
            if (var != null) {
                // ok, we found it
                DataClass datumReturnNum = var.getValue();
                return datumReturnNum;
            } else {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_MEMBER_NOT_EXIST);
            }
        } else {
            // member function
            AEFunction aeMember = (AEFunction)maeMember;
            LinkedList<AbstractExpr> listParamAExprs = new LinkedList<AbstractExpr>();
            for (AbstractExpr ae: aeMember.mlistChildren) {
                listParamAExprs.addFirst(ae);   // listParamAExprs should be in a reversed order.
            }
            DataClass datumValue = FuncAnalyzer.evaluateFunction(mstrOwnerName, datumOwner, aeMember.mstrShrinkedRawFuncName, listParamAExprs, lUnknownVars, progContext);
            return datumValue;
        }
    }

    // avoid to do any overhead work.
    @Override
    public AbstractExpr evaluateAExpr(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr(); // still needs to do some basic validation.
        AbstractExpr aeOwner = maeOwner.evaluateAExpr(lUnknownVars, progContext);
        if (menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_MEMBERVARIABLE) {
            // member variable
            if (aeOwner instanceof AEConst) {
                // the class variable's value is known.
                DataClassClass memberVariableOwner = DCHelper.lightCvtOrRetDCClass(((AEConst)aeOwner).getDataClass());
                MFPClassInstance ownerInstance = memberVariableOwner.getClassInstance();
                String strVariableName = ((AEVar)maeMember).mstrVariableName;
                Variable var = ownerInstance.getMemberVariable(mstrOwnerName, strVariableName, SpaceMember.AccessRestriction.PRIVATE, progContext);
                if (var != null) {
                    // ok, we found it
                    DataClass datumReturnNum = var.getValue();
                    return new AEConst(datumReturnNum);
                } else {
                    throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_MEMBER_NOT_EXIST);
                }
            } else {
                return new AEObjMember(mstrOwnerName, aeOwner, maeMember); // maeMember is AEVar which is immutable. So we can reuse it here.
            }
        } else {
            // member function
            AEFunction aeMember = (AEFunction)maeMember;
            LinkedList<AbstractExpr> listNewChildren4AEFunction = new LinkedList<AbstractExpr>();
            LinkedList<AbstractExpr> listNewChildren2EvaluateFunction = new LinkedList<AbstractExpr>();
            boolean bAllChildrenKnownValues = true;
            for (int idx = 0; idx < aeMember.mlistChildren.size(); idx ++)    {
                // here we do not use try catch because evaluateAExpr analyse an expression and
                // convert it to AbstractExpr in case there are unknown variables. We cannot garantee
                // all parameters are known so that it doesn't make sense for and/or functions to do
                // quick judgement.
                AbstractExpr aeParameter = aeMember.mlistChildren.get(idx).evaluateAExpr(lUnknownVars, progContext);
                if (!(aeParameter instanceof AEConst)) {
                    bAllChildrenKnownValues = false;
                }
                // AEFunction's parameter order is different from FuncAnalyzer.evaluateFunction
                listNewChildren4AEFunction.add(aeParameter);
                listNewChildren2EvaluateFunction.addFirst(aeParameter);
            }
            if (aeOwner instanceof AEConst && bAllChildrenKnownValues) {
                DataClass datumValue = FuncAnalyzer.evaluateFunction(mstrOwnerName, ((AEConst)aeOwner).getDataClass(), aeMember.mstrShrinkedRawFuncName, listNewChildren2EvaluateFunction, lUnknownVars, progContext);
                return new AEConst(datumValue);
            } else {
                AEFunction aeEvalMember = new AEFunction(aeMember.mstrShrinkedRawFuncName, listNewChildren4AEFunction);
                return new AEObjMember(mstrOwnerName, aeOwner, aeEvalMember);
            }
        }
    }

    @Override
    public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars, AbstractExpr.SimplifyParams simplifyParams, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();
        AbstractExpr aeOwner = maeOwner.simplifyAExpr(lUnknownVars, simplifyParams, progContext);
        if (menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_MEMBERVARIABLE) {
            // member variable
            if (aeOwner instanceof AEConst) {
                // the class variable's value is known.
                DataClassClass memberVariableOwner = DCHelper.lightCvtOrRetDCClass(((AEConst)aeOwner).getDataClass());
                MFPClassInstance ownerInstance = memberVariableOwner.getClassInstance();
                String strVariableName = ((AEVar)maeMember).mstrVariableName;
                Variable var = ownerInstance.getMemberVariable(mstrOwnerName, strVariableName, SpaceMember.AccessRestriction.PRIVATE, progContext);
                if (var != null) {
                    // ok, we found it
                    DataClass datumReturnNum = var.getValue();
                    return new AEConst(datumReturnNum);
                } else {
                    throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_MEMBER_NOT_EXIST);
                }
            } else {
                return new AEObjMember(mstrOwnerName, aeOwner, maeMember); // maeMember is AEVar which is immutable. So we can reuse it here.
            }
        } else {
            // member function
            AEFunction aeMember = (AEFunction)maeMember;
            LinkedList<AbstractExpr> listNewChildren4AEFunction = new LinkedList<AbstractExpr>();
            LinkedList<AbstractExpr> listNewChildren2EvaluateFunction = new LinkedList<AbstractExpr>();
            boolean bAllChildrenKnownValues = true;
            for (int idx = 0; idx < aeMember.mlistChildren.size(); idx ++)    {
                // here we do not use try catch because evaluateAExpr analyse an expression and
                // convert it to AbstractExpr in case there are unknown variables. We cannot garantee
                // all parameters are known so that it doesn't make sense for and/or functions to do
                // quick judgement.
                AbstractExpr aeParameter = aeMember.mlistChildren.get(idx).simplifyAExpr(lUnknownVars, simplifyParams, progContext);
                if (!(aeParameter instanceof AEConst)) {
                    bAllChildrenKnownValues = false;
                }
                // AEFunction's parameter order is different from FuncAnalyzer.evaluateFunction
                listNewChildren4AEFunction.add(aeParameter);
                listNewChildren2EvaluateFunction.addFirst(aeParameter);
            }
            if (aeOwner instanceof AEConst && bAllChildrenKnownValues) {
                DataClass datumValue = FuncAnalyzer.evaluateFunction(mstrOwnerName, ((AEConst)aeOwner).getDataClass(), aeMember.mstrShrinkedRawFuncName, listNewChildren2EvaluateFunction, lUnknownVars, progContext);
                return new AEConst(datumValue);
            } else {
                AEFunction aeEvalMember = new AEFunction(aeMember.mstrShrinkedRawFuncName, listNewChildren4AEFunction);
                return new AEObjMember(mstrOwnerName, aeOwner, aeEvalMember);
                
            }
        }
    }

    @Override
    public boolean needBracketsWhenToStr(AbstractExpr.ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        return false;
    }
    
    @Override
    public int compareAExpr(AbstractExpr aexpr) throws JSmartMathErrException, JFCALCExpErrException {
        if (menumAEType.getValue() < aexpr.menumAEType.getValue())    {
            return 1;
        } else if (menumAEType.getValue() > aexpr.menumAEType.getValue())    {
            return -1;
        } else    {
            int nReturn = maeOwner.compareAExpr(((AEObjMember)aexpr).maeOwner);
            if (nReturn == 0)    {
                // use equalsIgnoreCase here coz owner name may have big letters.
                Boolean isLeftSelf = mstrOwnerName.equalsIgnoreCase("self");
                Boolean isRightSelf = ((AEObjMember)aexpr).mstrOwnerName.equalsIgnoreCase("self");
                if (isLeftSelf && !isRightSelf) {
                    nReturn = 1;
                } else if (isRightSelf && !isLeftSelf) {
                    nReturn = -1;
                } else {
                    nReturn = maeMember.compareAExpr(((AEObjMember)aexpr).maeMember);
                }
            }
            return nReturn;
        }
    }

    // identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
    @Override
    public boolean isNegligible(ProgContext progContext) throws JSmartMathErrException, JFCALCExpErrException    {
        validateAbstractExpr();
        return false;    // because this aexpr has been simplified most, but it is
                        // still a AEObjMember. This implies that it is not a constant.
    }
    
    // output the string based expression of any abstract expression type.
    @Override
    public String output()    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        boolean bOwnerNeedBracketsWhenToStr = false;
        bOwnerNeedBracketsWhenToStr = maeOwner.needBracketsWhenToStr(menumAEType, 1);
        
        String strOutput = "";
        if (bOwnerNeedBracketsWhenToStr) {
            strOutput += "(" + maeOwner.output() + ")";
        } else  {
            strOutput += maeOwner.output();
        }
        strOutput += "." + maeMember.output();
        return strOutput;
    }
    
    @Override
    public String outputWithFlag(int flag, ProgContext progContextNow)    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        boolean bOwnerNeedBracketsWhenToStr = false;
        bOwnerNeedBracketsWhenToStr = maeOwner.needBracketsWhenToStr(menumAEType, 1);
        
        String strOutput = "";
        if (bOwnerNeedBracketsWhenToStr) {
            strOutput += "(" + maeOwner.outputWithFlag(flag, progContextNow) + ")";
        } else  {
            strOutput += maeOwner.outputWithFlag(flag, progContextNow);
        }
        strOutput += "." + maeMember.outputWithFlag(flag, progContextNow);
        return strOutput;
    }
    
    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException, JFCALCExpErrException {
        AbstractExpr aeOwner = (maeOwner instanceof AEConst)?maeOwner:maeOwner.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        // maeMember.convertAEVar2AExprDatum will only operate on parameters.
        return new AEObjMember(mstrOwnerName, aeOwner, maeMember.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars));
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        // maeMember.convertAExprDatum2AExpr will only operate on parameters.
        return new AEObjMember(mstrOwnerName, maeOwner.convertAExprDatum2AExpr(), maeMember.convertAExprDatum2AExpr());
    }

    @Override
    public int getVarAppearanceCnt(String strVarName) {
        int nCnt = maeOwner.getVarAppearanceCnt(strVarName);
        if (menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_MEMBERFUNCTION) {
            nCnt += maeMember.getVarAppearanceCnt(strVarName);
        }   // if it is member variable, member variable's variable name cannot be counted in.
        return nCnt;
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) {
        LinkedList<AbstractExpr> listAEs = new LinkedList<AbstractExpr>();
        listAEs.add(maeOwner);
        if (menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_MEMBERFUNCTION) {
            listAEs.addAll(((AEFunction)maeMember).mlistChildren);
        }
        LinkedList<ModuleInfo> listFunctions = getReferredFunctionsFromAExprs(listAEs, progContext);
        if (menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_MEMBERFUNCTION) {
            if (maeOwner instanceof AEConst) {
                try {
                    DataClassClass datumOwner = DCHelper.lightCvtOrRetDCClass(((AEConst)maeOwner).getDataClass());
                    String strFullFunctionNameWithCS = datumOwner.getClassInstance().classDefFullNameWithCS
                                                    + "::" + ((AEFunction)maeMember).mstrShrinkedRawFuncName;
                    ModuleInfo moduleInfo = new ModuleInfo();
                    moduleInfo.mnModuleType = ModuleInfo.FUNCTION_MODULE;
                    moduleInfo.mstrModuleName = strFullFunctionNameWithCS;
                    moduleInfo.mnModuleParam1 = ((AEFunction)maeMember).mlistChildren.size();
                    ModuleInfo.mergeModuleInfo2List(moduleInfo, listFunctions);
                } catch (JFCALCExpErrException ex) {
                    // an exception is thrown, ignore this function module.
                    Logger.getLogger(AEObjMember.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return listFunctions;
    }
    
}
