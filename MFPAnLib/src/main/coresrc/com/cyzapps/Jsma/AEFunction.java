// MFP project, AEFunction.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jsma;

import com.cyzapps.Jfcalc.Operators.CalculateOperator;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassAExpr;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.Operators.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.CompileAdditionalInfo;
import com.cyzapps.Jmfp.ModuleInfo;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.Oomfp.MFPClassDefinition;
import com.cyzapps.Oomfp.MemberFunction;
import com.cyzapps.adapter.FunctionNameMapper;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;


public class AEFunction extends AbstractExpr {
	private static final long serialVersionUID = 1L;

	public static class CSMFPair implements Serializable {  //this class is immutible
		private static final long serialVersionUID = 1L;
		private List<String[]> mCSes = new LinkedList<String[]>();
        private String mstrAbsFuncNameWithCS = null;
        private MemberFunction mmf = null;
        public CSMFPair() {}
        public CSMFPair(List<String[]> CSes, String strAbsFuncNameWithCS, MemberFunction mf) {
            mCSes.addAll(CSes);
            mstrAbsFuncNameWithCS = strAbsFuncNameWithCS;
            mmf = mf;
        }
        public Boolean isInitialized() {
            return mstrAbsFuncNameWithCS != null;
        }
        public Boolean isSameCSes(List<String[]> CSes) {
            // we may make an assumption, when an AEFunction has different CSes, it can only
            // stack new CS on existing CSes. So to compare two CSes, we only need to compare
            // stack depth.
            // However, the above assumption is not save, so still need to compare strings
            if (mCSes.size() != CSes.size()) {
                return false;
            } for (int idx = 0; idx < CSes.size(); idx ++) {
                if (!Arrays.equals(mCSes.get(idx), CSes.get(idx))) {
                    return false;
                }
            }
            return true;
        }
        
        public CSMFPair clone() {
            return new CSMFPair(mCSes, mstrAbsFuncNameWithCS, mmf);
        }
    }

    protected LinkedList<CSMFPair> mlistCSMFPairCache = new LinkedList<CSMFPair>();
    public void setCSMFPairCache(LinkedList<CSMFPair> listCSMFPairCache) {
        mlistCSMFPairCache.clear();
        mlistCSMFPairCache.addAll(listCSMFPairCache);
    }
    protected String mstrShrinkedRawFuncName = "";  // shrinked raw function name with only small letters.
    public LinkedList<AbstractExpr> mlistChildren = new LinkedList<AbstractExpr>(); // do not protect it here because useless.
    
    public MemberFunction getMemberFunction(ProgContext progContext) {
        List<String[]> lCitingSpaces = progContext.mstaticProgContext.getCitingSpaces();
        for (CSMFPair csmfPair : mlistCSMFPairCache) {
            if (csmfPair.isSameCSes(lCitingSpaces)) {
                return csmfPair.mmf;
            }
        }
        
        // cannot find it.
        if (mstrShrinkedRawFuncName.equals(PtnSlvVarIdentifier.FUNCTION_SINGLE_VAR_INVERTIBLE)
                || mstrShrinkedRawFuncName.equals(PtnSlvVarIdentifier.FUNCTION_SINGLE_VAR_INVERTED)
                || mstrShrinkedRawFuncName.equals(PtnSlvMultiVarsIdentifier.FUNCTION_ORIGINAL_EXPRESSION)) {
            CSMFPair csmfPair = new CSMFPair(lCitingSpaces,
                                            mstrShrinkedRawFuncName,    // this are special function names.
                                            null);
            mlistCSMFPairCache.addFirst(csmfPair);
            return null;    // reserved function is not a real function so that return null.
        } else {
            MemberFunction mf = CitingSpaceDefinition.locateFunctionCall(mstrShrinkedRawFuncName, mlistChildren.size(), lCitingSpaces);
            if (mf != null) {
                String strFuncNameWithCS = mf.getAbsNameWithCS();
                CSMFPair csmfPair = new CSMFPair(lCitingSpaces, strFuncNameWithCS, mf);
                mlistCSMFPairCache.addFirst(csmfPair);
                return mf;
            } else {
                // invalid mf
                return null;
            }
        }
    }
    
    /**
     * This function will not return null.
     * @param progContext
     * @return 
     */
    public String getAbsFuncNameWithCS(ProgContext progContext) {
        List<String[]> lCitingSpaces = progContext.mstaticProgContext.getCitingSpaces();
        for (CSMFPair csmfPair : mlistCSMFPairCache) {
            if (csmfPair.isSameCSes(lCitingSpaces)) {
                return csmfPair.mstrAbsFuncNameWithCS;  // in mlistCSMFPairCache, any csmfPair's mstrAbsFuncNameWithCS cannot be null.
            }
        }
        
        // cannot find it.
        if (mstrShrinkedRawFuncName.equals(PtnSlvVarIdentifier.FUNCTION_SINGLE_VAR_INVERTIBLE)
                || mstrShrinkedRawFuncName.equals(PtnSlvVarIdentifier.FUNCTION_SINGLE_VAR_INVERTED)
                || mstrShrinkedRawFuncName.equals(PtnSlvMultiVarsIdentifier.FUNCTION_ORIGINAL_EXPRESSION)) {
            CSMFPair csmfPair = new CSMFPair(lCitingSpaces,
                                            mstrShrinkedRawFuncName,    // this are special function names.
                                            null);
            mlistCSMFPairCache.addFirst(csmfPair);
            return mstrShrinkedRawFuncName;
        } else {
            MemberFunction mf = CitingSpaceDefinition.locateFunctionCall(mstrShrinkedRawFuncName, mlistChildren.size(), lCitingSpaces);
            if (mf != null) {
                String strFuncNameWithCS = mf.getAbsNameWithCS();
                CSMFPair csmfPair = new CSMFPair(lCitingSpaces, strFuncNameWithCS, mf);
                mlistCSMFPairCache.addFirst(csmfPair);
                return strFuncNameWithCS;
            } else {
                // invalid mf
                return mstrShrinkedRawFuncName;
            }
        }
    }
    
    public String getShrinkedRawFuncName() {
        return mstrShrinkedRawFuncName;
    }
    
    public boolean isSameAbsFuncNameWithCS(String strAnotherName2Comp, ProgContext progContext) {
        return getAbsFuncNameWithCS(progContext).equals(strAnotherName2Comp);
    }

    public AEFunction() {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_FUNCTION;
        mstrShrinkedRawFuncName = "";
        mlistChildren = new LinkedList<AbstractExpr>();
    }
    
    public AEFunction(String strName, LinkedList<AbstractExpr> listChildren) throws JSmartMathErrException    {
        setAEFunction(strName, listChildren, new ProgContext());
    }
    
    public AEFunction(String strName, LinkedList<AbstractExpr> listChildren, ProgContext progContext) throws JSmartMathErrException    {
        // strName is always shrinked raw name. (could include big letters).
        setAEFunction(strName, listChildren, progContext);
    }

    public AEFunction(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException    {
        copy(aexprOrigin);
    }

    @Override
    public void validateAbstractExpr() throws JSmartMathErrException    {
        if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_FUNCTION)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
        }
    }
    
    private void setAEFunction(String strName, LinkedList<AbstractExpr> listChildren, ProgContext progContext) throws JSmartMathErrException    {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_FUNCTION;
        mstrShrinkedRawFuncName = strName;
        mlistCSMFPairCache.clear();
        if (mstrShrinkedRawFuncName.equals(PtnSlvVarIdentifier.FUNCTION_SINGLE_VAR_INVERTIBLE)
                || mstrShrinkedRawFuncName.equals(PtnSlvVarIdentifier.FUNCTION_SINGLE_VAR_INVERTED)
                || mstrShrinkedRawFuncName.equals(PtnSlvMultiVarsIdentifier.FUNCTION_ORIGINAL_EXPRESSION)) {
            CSMFPair csmfPair = new CSMFPair(progContext.mstaticProgContext.getCitingSpaces(),
                                            mstrShrinkedRawFuncName,    // this are special function names.
                                            null);
            mlistCSMFPairCache.add(csmfPair);
        } else {
            List<String[]> lCitingSpaces = progContext.mstaticProgContext.getCitingSpaces();
            MemberFunction mf = CitingSpaceDefinition.locateFunctionCall(mstrShrinkedRawFuncName, listChildren.size(), lCitingSpaces);
            if (mf != null) {
                CSMFPair csmfPair = new CSMFPair(lCitingSpaces, mf.getAbsNameWithCS(), mf);
                mlistCSMFPairCache.add(csmfPair);
            } // do not throw exception even if mf is null as AEFunction() will also set invalid names.
        }
        mlistChildren = (listChildren == null)?new LinkedList<AbstractExpr>():listChildren;
        validateAbstractExpr();
    }
    
    @Override
    protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
            JSmartMathErrException {
        ((AEFunction)aexprOrigin).validateAbstractExpr();
        super.copy(aexprOrigin);
        mstrShrinkedRawFuncName = ((AEFunction)aexprOrigin).mstrShrinkedRawFuncName;
        setCSMFPairCache(((AEFunction)aexprOrigin).mlistCSMFPairCache);
        mlistChildren = new LinkedList<AbstractExpr>();
        if (((AEFunction)aexprOrigin).mlistChildren != null)    {
            for (int idx = 0; idx < ((AEFunction)aexprOrigin).mlistChildren.size(); idx ++)    {
                mlistChildren.add(((AEFunction)aexprOrigin).mlistChildren.get(idx));
            }
        }
    }

    @Override
    protected void copyDeep(AbstractExpr aexprOrigin)
            throws JFCALCExpErrException, JSmartMathErrException {
        ((AEFunction)aexprOrigin).validateAbstractExpr();
        super.copyDeep(aexprOrigin);
        mstrShrinkedRawFuncName = ((AEFunction)aexprOrigin).mstrShrinkedRawFuncName;
        setCSMFPairCache(((AEFunction)aexprOrigin).mlistCSMFPairCache);
        mlistChildren = new LinkedList<AbstractExpr>();
        if (((AEFunction)aexprOrigin).mlistChildren != null)    {
            for (int idx = 0; idx < ((AEFunction)aexprOrigin).mlistChildren.size(); idx ++)    {
                AbstractExpr aexprChild = ((AEFunction)aexprOrigin).mlistChildren.get(idx).cloneSelf();
                mlistChildren.add(aexprChild);
            }
        }
    }

    @Override
    public AbstractExpr cloneSelf() throws JFCALCExpErrException,
            JSmartMathErrException {
        AbstractExpr aeReturn = new AEFunction();
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
        } else if (progContext != null) { // if progContext is not null, we compare Abs name with CS.
            String strAbsFuncNameWithCS = getAbsFuncNameWithCS(progContext);
            String strAbsFuncNameWithCS1 = ((AEFunction)aexpr).getAbsFuncNameWithCS(progContext);
            if (!strAbsFuncNameWithCS.equals(strAbsFuncNameWithCS1))    {
                return false;
            } else if (mlistChildren.size() != ((AEFunction)aexpr).mlistChildren.size())    {
                return false;
            } else {
                for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
                    if (mlistChildren.get(idx).isEqual(((AEFunction)aexpr).mlistChildren.get(idx), progContext) == false)    {
                        return false;
                    }
                }
                return true;
            }
        } else {    // if progContext is null, we compare shrinked raw name.
            if (!mstrShrinkedRawFuncName.equals(((AEFunction)aexpr).mstrShrinkedRawFuncName))    {
                return false;
            } else if (mlistChildren.size() != ((AEFunction)aexpr).mlistChildren.size())    {
                return false;
            } else {
                for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
                    if (mlistChildren.get(idx).isEqual(((AEFunction)aexpr).mlistChildren.get(idx), progContext) == false)    {
                        return false;
                    }
                }
                return true;
            }
        }
    }
    
    @Override
    public int getHashCode()  throws JFCALCExpErrException {
        int hashRet = menumAEType.hashCode() + mstrShrinkedRawFuncName.hashCode() * 7;
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
                                boolean bAllowConversion, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        /* do not call isPatternDegrade function because generally function expression cannot degrade-match a pattern.*/
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
        if (!(aePattern instanceof AEFunction))   {
            return false;
        }
        if (mlistChildren.size() != ((AEFunction)aePattern).mlistChildren.size()) {
            return false;
        }
        for (int idx = 0; idx < mlistChildren.size(); idx ++) {
            if (mlistChildren.get(idx).isPatternMatch(((AEFunction)aePattern).mlistChildren.get(idx),
                    listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion, progContext) == false) {
                return false;
            }
        }
        String strAbsFuncNameWithCS = getAbsFuncNameWithCS(progContext);
        if (strAbsFuncNameWithCS.equals(((AEFunction)aePattern).getAbsFuncNameWithCS(progContext)))   {
            // this is not a pattern so that mstrFuncName should not be something like f_single_var_invertible
            return true;
        } else if (((AEFunction)aePattern).getAbsFuncNameWithCS(progContext).equals(PtnSlvVarIdentifier.FUNCTION_SINGLE_VAR_INVERTIBLE)
                && PtnSlvVarIdentifier.isSingleVarInvertibleFunc(strAbsFuncNameWithCS)
                && mlistChildren.size() == 1)    {
            // assume there is always only one invertible single var function in the pattern.
            for (int idx = 0; idx < listpeuMapPseudoFuncs.size(); idx ++)  {
                if (listpeuMapPseudoFuncs.get(idx).maePatternUnit.isEqual(aePattern, progContext))    {
                    if (isEqual(listpeuMapPseudoFuncs.get(idx).maeExprUnit, progContext))   {
                        // this pseudo function has been mapped to a function and the function is the same as this
                        return true;
                    } else  {
                        // this pseudo function has been mapped to a function but the function is not the same as this
                        return false;
                    }
                }
            }
            // the aePattern hasn't been mapped to any function before.
            PatternExprUnitMap peuMap = new PatternExprUnitMap(this, aePattern);
            listpeuMapPseudoFuncs.add(peuMap);
            return true;
        } else  {
            return false;
        }
    }
    
    @Override
    public boolean isKnownValOrPseudo() {
        for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
            if (!mlistChildren.get(idx).isKnownValOrPseudo())    {
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
        AEFunction aeReturn = new AEFunction();
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
        AEFunction aeReturn = new AEFunction();
        aeReturn.copy(this);
        for (int idx = 0; idx < aeReturn.mlistChildren.size(); idx ++)    {
            for (int idx1 = 0; idx1 < listFromToMap.size(); idx1 ++)    {
                if (bExpr2Pattern && aeReturn.mlistChildren.get(idx).isEqual(listFromToMap.get(idx1).maeExprUnit, progContext))    {
                    aeReturn.mlistChildren.set(idx, listFromToMap.get(idx1).maePatternUnit);    // actually need to clone because will be many aeTo copies. Otherwise parent cannot be identified. However, at this moment don't care.
                    listReplacedChildren.add(aeReturn.mlistChildren.get(idx));
                    break;
                } else if ((!bExpr2Pattern) && aeReturn.mlistChildren.get(idx).isEqual(listFromToMap.get(idx1).maePatternUnit, progContext))    {
                    aeReturn.mlistChildren.set(idx, listFromToMap.get(idx1).maeExprUnit);    // actually need to clone because will be many aeTo copies. Otherwise parent cannot be identified. However, at this moment don't care.
                    listReplacedChildren.add(aeReturn.mlistChildren.get(idx));
                    break;
                }
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
        LinkedList<String> listParamRawInputs = new LinkedList<String>();
        LinkedList<DataClass> listParamValues = new LinkedList<DataClass>();
        LinkedList<JFCALCExpErrException> listParamExceptions = new LinkedList<JFCALCExpErrException>();
        for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
            DataClass datumParameter = null;
            JFCALCExpErrException eParam = null;
            try {
                datumParameter = mlistChildren.get(idx).evaluateAExprQuick(lUnknownVars, progContext);
                if (datumParameter == null) {
                    throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
                }
            } catch (JFCALCExpErrException e) {
                eParam = FuncEvaluator.rethrowParamException(e);
            }
            listParamRawInputs.addFirst(mlistChildren.get(idx).output());
            listParamValues.addFirst(datumParameter);    // no need to deep copy. also note that last parameer is the first in the param list.
            listParamExceptions.addFirst(eParam);
        }
        MemberFunction mf = getMemberFunction(progContext);
        if (mf == null) {
            throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_UNDEFINED_FUNCTION);
        }
        DataClass datumReturn = FuncEvaluator.evaluateFunction(mf, mstrShrinkedRawFuncName, listParamRawInputs, listParamValues, listParamExceptions, progContext);
        if (datumReturn == null)    {
            // a function can return nothing, but this function is not supported by smart math.
            throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
        }
        return datumReturn;
    }

    /**
     * Different from evaluateAExprQuick for AEFunction, this function doesn't throw exception if it returns
     * nothing.
     * @param lUnknownVars
     * @param progContext
     * @return
     * @throws InterruptedException
     * @throws com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException
     * @throws com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException 
     */
    public DataClass evaluateAEFunctionQuick(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr(); // still needs to do some basic validation.
        LinkedList<String> listParamRawInputs = new LinkedList<String>();
        LinkedList<DataClass> listParamValues = new LinkedList<DataClass>();
        LinkedList<JFCALCExpErrException> listParamExceptions = new LinkedList<JFCALCExpErrException>();
        for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
            DataClass datumParameter = null;
            JFCALCExpErrException eParam = null;
            try {
                datumParameter = mlistChildren.get(idx).evaluateAExprQuick(lUnknownVars, progContext);
                if (datumParameter == null) {
                    throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
                }
            } catch (JFCALCExpErrException e) {
                eParam = FuncEvaluator.rethrowParamException(e);
            }
            listParamRawInputs.addFirst(mlistChildren.get(idx).output());
            listParamValues.addFirst(datumParameter);    // no need to deep copy. also note that last parameer is the first in the param list.
            listParamExceptions.addFirst(eParam);
        }
        MemberFunction mf = getMemberFunction(progContext);
        if (mf == null) {
            throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_UNDEFINED_FUNCTION);
        }
        DataClass datumReturn = FuncEvaluator.evaluateFunction(mf, mstrShrinkedRawFuncName, listParamRawInputs, listParamValues, listParamExceptions, progContext);
        return datumReturn;
    }
    
    // avoid to do any overhead work.
    @Override
    public AbstractExpr evaluateAExpr(LinkedList<UnknownVariable> lUnknownVars, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
        validateAbstractExpr(); // still needs to do some basic validation.
        LinkedList<String> listParamRawInputs = new LinkedList<String>();
        LinkedList<DataClass> listParamValues = new LinkedList<DataClass>();
        LinkedList<JFCALCExpErrException> listParamExceptions = new LinkedList<JFCALCExpErrException>();
        LinkedList<AbstractExpr> listNewChildren = new LinkedList<AbstractExpr>();
        boolean bAllChildrenKnownValues = true;
        for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
            // here we do not use try catch because evaluateAExpr analyse an expression and
            // convert it to AbstractExpr in case there are unknown variables. We cannot garantee
            // all parameters are known so that it doesn't make sense for and/or functions to do
            // quick judgement.
            AbstractExpr aeParameter = mlistChildren.get(idx).evaluateAExpr(lUnknownVars, progContext);
            if (aeParameter instanceof AEConst) {
                listParamRawInputs.addFirst(mlistChildren.get(idx).output());
                listParamValues.addFirst(((AEConst)aeParameter).getDataClass()); // let getDataClass decides when to deep copy
                listParamExceptions.addFirst(null); // if it can arrive at here, it means no exception thrown.
            } else {
                bAllChildrenKnownValues = false;
            }
            listNewChildren.add(aeParameter);
        }
        if (bAllChildrenKnownValues) {
            MemberFunction mf = getMemberFunction(progContext);
            if (mf == null) {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_UNDEFINED_FUNCTION);
            }
            DataClass datumReturn = FuncEvaluator.evaluateFunction(mf, mstrShrinkedRawFuncName, listParamRawInputs, listParamValues, listParamExceptions, progContext);
            if (datumReturn == null)    {
                // a function can return nothing, but this function is not supported by smart math.
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
            }
            return new AEConst(datumReturn);
        } else {
            return cloneSelf();
        }
    }
    
    @Override
    public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars,
                                    SimplifyParams simplifyParams, ProgContext progContext)
            throws InterruptedException, JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();
        AEFunction aeReturn = new AEFunction();
        aeReturn.copy(this);
        boolean bAllParamSolved = true;
        LinkedList<String> listParamRawInputs = new LinkedList<String>();
        LinkedList<DataClass> listParamValues = new LinkedList<DataClass>();
        LinkedList<JFCALCExpErrException> listParamExceptions = new LinkedList<JFCALCExpErrException>();
        for (int idx = 0; idx < aeReturn.mlistChildren.size(); idx ++)    {
            // here we do not use try catch because simplifyAExpr simplify an expression and
            // convert it to AbstractExpr in case there are unknown variables. We cannot garantee
            // all parameters are known so that it doesn't make sense for and/or functions to do
            // quick judgement.
            AbstractExpr aeChild = aeReturn.mlistChildren.get(idx).simplifyAExpr(lUnknownVars, simplifyParams, progContext);
            if (aeChild instanceof AEConst)    {
                // aeChild refers to same data value in this. So have to call getDatumValue() instead of getFinalDatumValue().
                listParamValues.addFirst(((AEConst) aeChild).getDataClass());   // have to be addFirst because listParams will be read from tail.
            } else    {
                listParamValues.addFirst(new DataClassAExpr(aeChild));
                bAllParamSolved = false;
            }
            listParamRawInputs.addFirst(aeReturn.mlistChildren.get(idx).output());
            listParamExceptions.addFirst(null);
            aeReturn.mlistChildren.set(idx, aeChild);
        }
        String strAbsFuncNameWithCS = aeReturn.getAbsFuncNameWithCS(progContext);
        if (bAllParamSolved)    {
            MemberFunction mf = getMemberFunction(progContext);
            if (mf == null) {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_UNDEFINED_FUNCTION);
            }
            DataClass datumReturn = FuncEvaluator.evaluateFunction(mf, mstrShrinkedRawFuncName, listParamRawInputs, listParamValues, listParamExceptions, progContext);
            if (datumReturn == null)    {
                // a function can return nothing, but this function is not supported by smart math.
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
            }
            AEConst aexprReturn = new AEConst(datumReturn);
            return aexprReturn;
        } else if (strAbsFuncNameWithCS.equals(FunctionNameMapper.msmapSysFunc2FullCSMap.get("pow")) && aeReturn.mlistChildren.size() == 2) { // pow(x,y) converted to x**y
            AEPowerOpt aexprReturn = new AEPowerOpt(aeReturn.mlistChildren.getFirst(), aeReturn.mlistChildren.getLast());
            return aexprReturn;
        } else if (strAbsFuncNameWithCS.equals(FunctionNameMapper.msmapSysFunc2FullCSMap.get("exp")) && aeReturn.mlistChildren.size() == 1) { // exp(x) converted to e**x
            AEConst aexprE = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.E));
            AEPowerOpt aexprReturn = new AEPowerOpt(aexprE, aeReturn.mlistChildren.getLast());
            return aexprReturn;
        } else if (strAbsFuncNameWithCS.equals(FunctionNameMapper.msmapSysFunc2FullCSMap.get("invert")) && aeReturn.mlistChildren.size() == 1) { // invert(x) converted to 1/x
            AEConst aexprOne = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE));
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
            LinkedList<AbstractExpr> listAEChildren = new LinkedList<AbstractExpr>();
            listAEChildren.add(aexprOne);
            listAEChildren.add(aeReturn.mlistChildren.getLast());
            AEMulDivOpt aexprReturn = new AEMulDivOpt(listAEChildren, listOpts);
            return aexprReturn;
        } else if (strAbsFuncNameWithCS.equals(FunctionNameMapper.msmapSysFunc2FullCSMap.get("sqrt")) && aeReturn.mlistChildren.size() == 1) { // sqrt(x) converted to x**0.5
            AEConst aexprHalf = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.HALF));
            AEPowerOpt aexprReturn = new AEPowerOpt(aeReturn.mlistChildren.getFirst(), aexprHalf);
            return aexprReturn;
        } else if (strAbsFuncNameWithCS.equals(FunctionNameMapper.msmapSysFunc2FullCSMap.get("ln")) && aeReturn.mlistChildren.size() == 1) { // ln(x) converted to log(x)
            AEFunction aexprReturn = new AEFunction(FunctionNameMapper.msmapSysFunc2FullCSMap.get("log"), aeReturn.mlistChildren, progContext);
            return aexprReturn;
        } else if (strAbsFuncNameWithCS.equals(FunctionNameMapper.msmapSysFunc2FullCSMap.get("lg")) && aeReturn.mlistChildren.size() == 1) { // lg(x) converted to log(x)
            AEFunction aexprReturn = new AEFunction(FunctionNameMapper.msmapSysFunc2FullCSMap.get("log"), aeReturn.mlistChildren, progContext);
            return aexprReturn;
        } else if (strAbsFuncNameWithCS.equals(FunctionNameMapper.msmapSysFunc2FullCSMap.get("loge")) && aeReturn.mlistChildren.size() == 1) { // loge(x) converted to log(x)
            AEFunction aexprReturn = new AEFunction(FunctionNameMapper.msmapSysFunc2FullCSMap.get("log"), aeReturn.mlistChildren, progContext);
            return aexprReturn;
        } else if (simplifyParams.mbAllowCvtFunc2MoreThan1Funcs
                && strAbsFuncNameWithCS.equals(FunctionNameMapper.msmapSysFunc2FullCSMap.get("tan")) && aeReturn.mlistChildren.size() == 1) { // tan(x) converted to sin(x)/cos(x)
            AEFunction aexprSin = new AEFunction(FunctionNameMapper.msmapSysFunc2FullCSMap.get("sin"), aeReturn.mlistChildren, progContext);
            AbstractExpr aexprX = aeReturn.mlistChildren.getFirst();
            LinkedList<AbstractExpr> listNewChildren = new LinkedList<AbstractExpr>();
            listNewChildren.add(aexprX);
            AEFunction aexprCos = new AEFunction(FunctionNameMapper.msmapSysFunc2FullCSMap.get("cos"), listNewChildren, progContext);
            LinkedList<AbstractExpr> listCnvtChildren = new LinkedList<AbstractExpr>();
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listCnvtChildren.add(aexprSin);
            listCnvtChildren.add(aexprCos);
            CalculateOperator coMultiply = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
            CalculateOperator coDiv = new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2);
            listOpts.add(coMultiply);
            listOpts.add(coDiv);
            AEMulDivOpt aexprReturn = new AEMulDivOpt(listCnvtChildren, listOpts);
            return aexprReturn;
        } else if (simplifyParams.mbAllowCvtFunc2MoreThan1Funcs
                && strAbsFuncNameWithCS.equals(FunctionNameMapper.msmapSysFunc2FullCSMap.get("tanh")) && aeReturn.mlistChildren.size() == 1) { // tanh(x) converted to sinh(x)/cosh(x)
            AEFunction aexprSinh = new AEFunction(FunctionNameMapper.msmapSysFunc2FullCSMap.get("sinh"), aeReturn.mlistChildren, progContext);
            AbstractExpr aexprX = aeReturn.mlistChildren.getFirst();
            LinkedList<AbstractExpr> listNewChildren = new LinkedList<AbstractExpr>();
            listNewChildren.add(aexprX);
            AEFunction aexprCosh = new AEFunction(FunctionNameMapper.msmapSysFunc2FullCSMap.get("cosh"), listNewChildren, progContext);
            LinkedList<AbstractExpr> listCnvtChildren = new LinkedList<AbstractExpr>();
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listCnvtChildren.add(aexprSinh);
            listCnvtChildren.add(aexprCosh);
            CalculateOperator coMultiply = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
            CalculateOperator coDiv = new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2);
            listOpts.add(coMultiply);
            listOpts.add(coDiv);
            AEMulDivOpt aexprReturn = new AEMulDivOpt(listCnvtChildren, listOpts);
            return aexprReturn;
        } else    {
            return aeReturn;
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
            int nReturn = mstrShrinkedRawFuncName.compareTo(((AEFunction)aexpr).mstrShrinkedRawFuncName);
            if (nReturn == 0)    {
                // when comparing children, function parameter is a bit different from posneg sign
                int nChildrenListSize1 = mlistChildren.size();
                int nChildrenListSize2 = ((AEFunction)aexpr).mlistChildren.size();
                if (nChildrenListSize1 > nChildrenListSize2)    {
                    return 1;
                } else if (nChildrenListSize1 < nChildrenListSize2)    {
                    return -1;
                } else    {
                    for (int idx = 0; idx < nChildrenListSize1; idx ++)    {
                        int nCompareChildReturn = mlistChildren.get(idx).compareAExpr(((AEFunction)aexpr).mlistChildren.get(idx));
                        if (nCompareChildReturn != 0)    {
                            return nCompareChildReturn;
                        }
                    }
                    return 0;
                }                
            } else    {
                return nReturn;
            }
        }
    }
    
    // identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
    @Override
    public boolean isNegligible(ProgContext progContext) throws JSmartMathErrException    {
        validateAbstractExpr();
        return false;
    }
    
    // output the string based expression of any abstract expression type.
    @Override
    public String output()    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        String strOutput = mstrShrinkedRawFuncName + "(";
        for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
            // we do not need to consider to use ()
            strOutput += mlistChildren.get(idx).output();
            if (idx < mlistChildren.size() - 1)    {
                strOutput += ",";
            }
        }
        strOutput += ")";
        return strOutput;
    }
    
    // output the string based expression of any abstract expression type with specific flag
    // note that the ProgContext parameter is the ProgContext when the function is called. It
    // is not necessarily the same as the mprogContext of this AEFunction.
    @Override
    public String outputWithFlag(int flag, ProgContext progContextNow)    throws JFCALCExpErrException, JSmartMathErrException {
        if ((flag & 1) == 0) {
            return output();
        } else {
            validateAbstractExpr();
            String strAbsFuncNameWithCS = getAbsFuncNameWithCS(progContextNow);
            int nDblColonPlace = strAbsFuncNameWithCS.length();    // this variable shouldn't be zero
            String strMinifiedName = null;
            String strPartAbsNameWithCs = "";
            while(nDblColonPlace > 0) {
                nDblColonPlace = strAbsFuncNameWithCS.lastIndexOf("::", nDblColonPlace - 1);
                if (nDblColonPlace >= 0) {
                    // ok, now get part of abs func name with cs
                    strPartAbsNameWithCs = strAbsFuncNameWithCS.substring(nDblColonPlace + 2);
                    List<String[]> lCitingSpaces = progContextNow.mstaticProgContext.getCitingSpaces();
                    MemberFunction mf = CitingSpaceDefinition.locateFunctionCall(strPartAbsNameWithCs, mlistChildren.size(), lCitingSpaces);
                    if (mf == null) {
                        continue;    // no such a function.
                    } else if (!mf.getAbsNameWithCS().equals(strAbsFuncNameWithCS)) {
                        continue;    // not the same function.
                    } else {
                        strMinifiedName = strPartAbsNameWithCs;    // it is the same function
                        break;
                    }
                }
            }
            if (strMinifiedName == null) {
                strMinifiedName = strAbsFuncNameWithCS;
            }
            String strOutput = strMinifiedName + "(";
            for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
                // we do not need to consider to use ()
                strOutput += mlistChildren.get(idx).outputWithFlag(flag, progContextNow);
                if (idx < mlistChildren.size() - 1)    {
                    strOutput += ",";
                } else    {
                    strOutput += ")";
                }
            }
            return strOutput;
        }
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
        AEFunction aeFunc = new AEFunction(mstrShrinkedRawFuncName, listChildrenCvted);
        aeFunc.setCSMFPairCache(mlistCSMFPairCache);
        return aeFunc;
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
                    // Will not be here coz the type has been checked.
                    e.printStackTrace();
                }
            } else {
                listChildrenCvted.add(mlistChildren.get(idx));
            }
        }
        AEFunction aeFunc = new AEFunction(mstrShrinkedRawFuncName, listChildrenCvted);
        aeFunc.setCSMFPairCache(mlistCSMFPairCache);
        return aeFunc;
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
        LinkedList<ModuleInfo> listModules = getReferredFunctionsFromAExprs(mlistChildren, progContext);
        String absFuncNameWithCS = getAbsFuncNameWithCS(progContext);
        MFPClassDefinition mfpClsDef = null;
        if (mlistChildren.isEmpty()) {            // might be a constructor.
            mfpClsDef = MFPClassDefinition.getClassDefinitionMap().get(absFuncNameWithCS);
        }
        if (mfpClsDef == null) {
            // normal function.
            ModuleInfo moduleInfo = new ModuleInfo();
            moduleInfo.mnModuleType = ModuleInfo.FUNCTION_MODULE;
            moduleInfo.mstrModuleName = absFuncNameWithCS;
            moduleInfo.mnModuleParam1 = mlistChildren.size();
            ModuleInfo.mergeModuleInfo2List(moduleInfo, listModules);
            return listModules;
        } else {
            // constructor
            ModuleInfo moduleInfo = new ModuleInfo();
            moduleInfo.mnModuleType = ModuleInfo.CLASS_MODULE;
            moduleInfo.mstrModuleName = mfpClsDef.mstrFullNameWithCS;
            moduleInfo.mnModuleParam1 = mlistChildren.size();   // number of parameters is still needed as CLASS_MODULE will also be treated as a function.
            moduleInfo.setClassDef(mfpClsDef);
            ModuleInfo.mergeModuleInfo2List(moduleInfo, listModules);
            return listModules;
        }
    }
}
