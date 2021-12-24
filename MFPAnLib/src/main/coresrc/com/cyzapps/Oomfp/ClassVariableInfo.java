/*
 * MFP project, ClassVariableInfo.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Oomfp;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jmfp.CompileAdditionalInfo;
import com.cyzapps.Jmfp.ModuleInfo;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.Statement;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.ExprAnalyzer;
import com.cyzapps.adapter.MFPAdapter;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tony
 */
public class ClassVariableInfo implements Serializable {
	private static final long serialVersionUID = 1L;

    // this is a function name or a variable name (trimmed and without big letters)
    // like "abc", default is "", which means invalid.
    public final String memberName;
    // this is a function or variable type (full type name, trimmed, shrunk and without big letters)
    // like "::mfp::lang::string", "::mfp::lang::array[3]()", "::mfp::lang::array[4](object)",
    // "::mfp::lang::function(::mfp::lang::object, ::mfp::lang::int, ::mfp::abcd::efgh)->::mfp::lang::float" etc.
    public final MFPDataTypeDef memberType;   // default is MFPDataTypeDef::OBJECT
    public final String memberInitialValueStr;  // the string of member initial value.
    public AbstractExpr memberInitialValue; // initial value, it can be a function or an expression, as long as it can be evaluated before initializing object from class.
    public final Statement variableStatement;   // the statement includes the variable declaration.
    // citing spaces. The first one is current citingspace. make sure they are small case. citing spaces are needed to evaluate initial value.
    public final LinkedList<String[]> m_lCitingSpaces = new LinkedList<>();
    
    public ClassVariableInfo(String name, MFPDataTypeDef type, String initialValueStr, AbstractExpr initialValue,
                            Statement sLine, LinkedList<String[]> lCitingSpaceStack,
                            LinkedList<LinkedList<String[]>> lUsingCitingSpacesStack) {
        // this is for member variable.
        memberName = name;
        memberType = (type == null) ? MFPDataTypeDef.OBJECT : type;
        memberInitialValueStr = initialValueStr;
        memberInitialValue = initialValue;
        variableStatement = sLine;
        List<String[]> lAllCSs = MFPAdapter.getReferredCitingSpaces(lCitingSpaceStack, lUsingCitingSpacesStack);
        m_lCitingSpaces.addAll(lAllCSs);
    }

    public void compileDefaultValExpr(ProgContext progContext, boolean bForceReanalyse) {
        if (bForceReanalyse || memberInitialValue == null || memberInitialValue.menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)  {
            if (memberInitialValueStr != null && memberInitialValueStr.length() > 0) {
                try {
                    memberInitialValue = ExprAnalyzer.analyseExpression(memberInitialValueStr, new DCHelper.CurPos(),
                            new LinkedList<VariableOperator.Variable>(), progContext);
                } catch (Exception ex) {
                    // do not throw exception here. Otherwise, the statement may not execute.
                    memberInitialValue = new AEInvalid();
                }
            }
        }           
    }
    
    public LinkedList<ModuleInfo> getReferredModules(CompileAdditionalInfo cai) {
        LinkedList<ModuleInfo> listModules = new LinkedList<ModuleInfo>();
        if (memberInitialValueStr != null && memberInitialValueStr.length() > 0) {
            ProgContext progContext = new ProgContext();
            progContext.mstaticProgContext.setCitingSpacesExplicitly(m_lCitingSpaces);
            try {
                listModules = ModuleInfo.getReferredModulesFromString(memberInitialValueStr, progContext);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClassVariableInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            return new LinkedList<ModuleInfo>();
        }
        return listModules;
    }
}
    
