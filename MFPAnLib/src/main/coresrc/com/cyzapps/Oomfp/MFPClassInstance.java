/*
 * MFP project, MFPClassInstance.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Oomfp;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassClass;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jmfp.ErrorProcessor;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.ScriptAnalysisHelper;
import com.cyzapps.Jmfp.Statement;
import com.cyzapps.Jmfp.StatementType;
import com.cyzapps.Jmfp.Statement_function;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tony
 */
public final class MFPClassInstance implements Serializable {
	private static final long serialVersionUID = 1L;
    
    public String classDefFullNameWithCS;
    private transient MFPClassDefinition classDefinition;   // avoid expensive serialization of class definition.
    public MFPClassDefinition getClassDefinition() {
        if (classDefinition == null) {
            classDefinition = MFPClassDefinition.getClassDefinitionMap().get(classDefFullNameWithCS);
        }
        return classDefinition;
    }
    public final MFPClassInstance[] parents;
    public final VariableOperator.Variable[] privateVariables;
    public final VariableOperator.Variable[] publicVariables;

    public MFPClassInstance() { // here, we create an instance of MFP
        classDefinition = MFPClassDefinition.OBJECT;
        classDefFullNameWithCS = classDefinition.selfTypeDef.toString();
        parents = new MFPClassInstance[0];
        privateVariables = new VariableOperator.Variable[0];
        publicVariables = new VariableOperator.Variable[0];
    }
    
    public MFPClassInstance(MFPClassDefinition classDef) throws ErrorProcessor.JMFPCompErrException {
        classDefinition = classDef;
        classDefFullNameWithCS = classDefinition.selfTypeDef.toString();
        parents = new MFPClassInstance[classDefinition.parentClassInfos.length];
        for (int idx = 0; idx < classDefinition.parentClassInfos.length; idx ++) {
            MFPClassDefinition parentClassDef = classDefinition.parentClassInfos[idx].getClassDef();
            if (parentClassDef == null) {
                ErrorProcessor.ERRORTYPES errType = ErrorProcessor.ERRORTYPES.UNDEFINED_PARENT_CLASS;
                Statement clsDeclareStatement = classDefinition.statements[classDefinition.classDeclarePosition];
                throw new ErrorProcessor.JMFPCompErrException(clsDeclareStatement.mstrFilePath, clsDeclareStatement.mnStartLineNo, clsDeclareStatement.mnEndLineNo, errType);
            }
            parents[idx] = new MFPClassInstance(parentClassDef);
        }
        privateVariables = new VariableOperator.Variable[classDefinition.privateVariables.length];
        for (int idx = 0; idx < privateVariables.length; idx ++) {
            ClassVariableInfo clsVarInfo = classDefinition.privateVariables[idx];
            DataClass datumReturn = new DataClassNull();
            if (clsVarInfo.memberInitialValueStr.length() > 0) {
                ProgContext progContext = new ProgContext();
                progContext.mstaticProgContext.setCitingSpacesExplicitly(clsVarInfo.m_lCitingSpaces);
                clsVarInfo.compileDefaultValExpr(progContext, false);
                try {
                    datumReturn = ScriptAnalysisHelper.analyseAExprOrString(clsVarInfo.memberInitialValue,
                                                                            clsVarInfo.memberInitialValueStr,
                                                                            clsVarInfo.variableStatement,
                                                                            ErrorProcessor.ERRORTYPES.INVALID_EXPRESSION,
                                                                            progContext);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MFPClassInstance.class.getName()).log(Level.SEVERE, null, ex);
                    ErrorProcessor.ERRORTYPES errType = ErrorProcessor.ERRORTYPES.INTERRUPTED_OPERATION;
                    throw new ErrorProcessor.JMFPCompErrException(clsVarInfo.variableStatement.mstrFilePath, clsVarInfo.variableStatement.mnStartLineNo, clsVarInfo.variableStatement.mnEndLineNo, errType);
                }
                if (datumReturn == null)    {
                    ErrorProcessor.ERRORTYPES errType = ErrorProcessor.ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                    throw new ErrorProcessor.JMFPCompErrException(clsVarInfo.variableStatement.mstrFilePath, clsVarInfo.variableStatement.mnStartLineNo, clsVarInfo.variableStatement.mnEndLineNo, errType);
                }
            }
            privateVariables[idx] = new VariableOperator.Variable(clsVarInfo.memberName, datumReturn);
        }
        
        publicVariables = new VariableOperator.Variable[classDefinition.publicVariables.length];
        for (int idx = 0; idx < publicVariables.length; idx ++) {
            ClassVariableInfo clsVarInfo = classDefinition.publicVariables[idx];
            DataClass datumReturn = new DataClassNull();
            if (clsVarInfo.memberInitialValueStr.length() > 0) {
                ProgContext progContext = new ProgContext();
                progContext.mstaticProgContext.setCitingSpacesExplicitly(clsVarInfo.m_lCitingSpaces);
                clsVarInfo.compileDefaultValExpr(progContext, false);
                try {
                    datumReturn = ScriptAnalysisHelper.analyseAExprOrString(clsVarInfo.memberInitialValue,
                                                                            clsVarInfo.memberInitialValueStr,
                                                                            clsVarInfo.variableStatement,
                                                                            ErrorProcessor.ERRORTYPES.INVALID_EXPRESSION,
                                                                            progContext);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MFPClassInstance.class.getName()).log(Level.SEVERE, null, ex);
                    ErrorProcessor.ERRORTYPES errType = ErrorProcessor.ERRORTYPES.INTERRUPTED_OPERATION;
                    throw new ErrorProcessor.JMFPCompErrException(clsVarInfo.variableStatement.mstrFilePath, clsVarInfo.variableStatement.mnStartLineNo, clsVarInfo.variableStatement.mnEndLineNo, errType);
                }
                if (datumReturn == null)    {
                    ErrorProcessor.ERRORTYPES errType = ErrorProcessor.ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                    throw new ErrorProcessor.JMFPCompErrException(clsVarInfo.variableStatement.mstrFilePath, clsVarInfo.variableStatement.mnStartLineNo, clsVarInfo.variableStatement.mnEndLineNo, errType);
                }
            }
            publicVariables[idx] = new VariableOperator.Variable(clsVarInfo.memberName, datumReturn);
        }
    }
    
    public Boolean isBitEqual(MFPClassInstance instance) throws ErrProcessor.JFCALCExpErrException {
        if (this == instance) {
            return true;
        } else if (classDefFullNameWithCS.equals(instance.classDefFullNameWithCS)) {
            // same class type. Note that we do not know class definition at this moment.
            // so we compare publicVariables, privateVariables and finally parents.
            // note that variable/parent order in the above lists should match class definition
            // which means that we do not need to search the list to find a matched name.
            for (int idx = 0; idx < publicVariables.length; idx ++) {
                Boolean isEqual = publicVariables[idx].getValue().isBitEqual(instance.publicVariables[idx].getValue());
                if (!isEqual) {
                    return false;
                }
            }
            for (int idx = 0; idx < privateVariables.length; idx ++) {
                Boolean isEqual = privateVariables[idx].getValue().isBitEqual(instance.privateVariables[idx].getValue());
                if (!isEqual) {
                    return false;
                }
            }
            for (int idx = 0; idx < parents.length; idx ++) {
                Boolean isEqual = parents[idx].isBitEqual(instance.parents[idx]);
                if (!isEqual) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + ((getClassDefinition() == null)?0 : getClassDefinition().hashCode());
        hash = 31 * hash + Arrays.hashCode(parents);
        hash = 31 * hash + Arrays.hashCode(privateVariables);
        hash = 31 * hash + Arrays.hashCode(publicVariables);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj;
    }
            
    @Override
    public String toString() {
        return classDefFullNameWithCS + "@{" + hashCode() + "}";
    }
    
    public MFPClassInstance superInstance(int idx) throws ErrProcessor.JFCALCExpErrException {
        if (idx < 0 || idx >= parents.length) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        return parents[idx];
    }
    
    private void copyFrom(MFPClassInstance src) throws ErrProcessor.JFCALCExpErrException {
        if (getClassDefinition() == null || getClassDefinition() != src.getClassDefinition()) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_MFP_CLASS_TYPE);
        }
        for (int idx = 0; idx < getClassDefinition().parentClassInfos.length; idx ++) {
            parents[idx].copyFrom(src.parents[idx]);
        }
        for (int idx = 0; idx < privateVariables.length; idx ++) {
            privateVariables[idx].setValue(src.privateVariables[idx].getValue());   // as this is first level copy, I don't think we need to shallow-copy dataclass.
        }
        for (int idx = 0; idx < publicVariables.length; idx ++) {
            publicVariables[idx].setValue(src.publicVariables[idx].getValue());
        }
    }
    
    public MFPClassInstance copySelf() {
        MFPClassInstance cpy = null;
        try {
            // although we have a try block here. No excetion will be thrown
            // because this object (self) has been initialized successfully,
            // so that new MFPClassInstance(classDefinition) and copyFrom will
            // never throw exception.
            // also, copySelf function is used only in defaultCopy function.
            // defaultCopy has already checked if getClassDefinition returns null.
            cpy = new MFPClassInstance(getClassDefinition());
            cpy.copyFrom(this);
        } catch (ErrorProcessor.JMFPCompErrException ex) {
            Logger.getLogger(MFPClassInstance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            Logger.getLogger(MFPClassInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cpy;
    }
    
    private void cloneFrom(MFPClassInstance src) throws ErrProcessor.JFCALCExpErrException {
        if (getClassDefinition() == null || getClassDefinition() != src.getClassDefinition()) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_MFP_CLASS_TYPE);
        }
        for (int idx = 0; idx < getClassDefinition().parentClassInfos.length; idx ++) {
            parents[idx].cloneFrom(src.parents[idx]);
        }
        for (int idx = 0; idx < privateVariables.length; idx ++) {
            privateVariables[idx].setValue(src.privateVariables[idx].getValue().cloneSelf());
        }
        for (int idx = 0; idx < publicVariables.length; idx ++) {
            publicVariables[idx].setValue(src.publicVariables[idx].getValue().cloneSelf());
        }
    }
    
    public MFPClassInstance cloneSelf() throws ErrProcessor.JFCALCExpErrException {
        MFPClassInstance clone = null;
        try {
            // although we have a try block here. No excetion will be thrown
            // because this object (self) has been initialized successfully,
            // so that new MFPClassInstance(classDefinition) and copyFrom will
            // never throw exception.
            // also, cloneSelf function is used only in defaultClone function.
            // defaultClone has already checked if getClassDefinition returns null.
            clone = new MFPClassInstance(getClassDefinition());
            clone.cloneFrom(this);  // cloneFrom may still throw JFCALCExpErrException
        } catch (ErrorProcessor.JMFPCompErrException ex) {
            Logger.getLogger(MFPClassInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return clone;
    }
    
    // this function returns an MFPClassInstance which is either itself or its parent
    public MFPClassInstance getInstanceFromDef(MFPClassDefinition classDef) {
        if (getClassDefinition() == classDef) {
            return this;
        } else {
            for (MFPClassInstance parent: parents) {
                MFPClassInstance clsInstance = parent.getInstanceFromDef(classDef);
                if (clsInstance != null) {
                    return clsInstance;
                }
            }
        }
        return null;
    }
    
    // varName has been trimmed uncapitalized. But ownerName is only trimmed. But we only need to compare it to self,
    // and create a temp variable, so no need to lowercase it. Simply compare owner name to self ignore case.
    public Variable getMemberVariable(String ownerName, String varName, SpaceMember.AccessRestriction access, ProgContext progContext) {
        // there are two special read only variables, this and super. This is private, while super is public.
        // here, varName has been small letters.
        if (varName.equals("this")) {
            Statement_function sf = progContext.mstaticProgContext.getCallingFunc();
            // sf.getOwnerClassDef() will never return null because if it returns null, satatement_function will never be analysed.
            MFPClassDefinition funcOwnerClassDef = sf == null ? null:sf.getOwnerClassDef();
            if (sf == null) {
                return null;    // we are not in a member function call, as this is private, it means it is invisible.
            } else if (ownerName.equalsIgnoreCase("self") || getClassDefinition() == funcOwnerClassDef) {
                MFPClassInstance thisInstance = getInstanceFromDef(funcOwnerClassDef);
                if (thisInstance == null) {
                    // this only appears when we call memberfunction(classObj, ...) instead of classObj.memberfunction(...)
                    return null;
                }
                try {
                    return new Variable(ownerName + "." + varName, new DataClassClass(thisInstance));
                } catch (ErrProcessor.JFCALCExpErrException ex) {
                    // will not be here.
                    Logger.getLogger(MFPClassInstance.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            } else {
                return null;
            }
        } else if (varName.equals("super")) {
            try {
                DataClass[] parentInstanceArray = new DataClass[parents.length];
                for (int idx = 0; idx < parents.length; idx ++) {
                    parentInstanceArray[idx] = new DataClassClass(parents[idx]);
                }
                return new Variable(ownerName + "." + varName, new DataClassArray(parentInstanceArray));
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                Logger.getLogger(MFPClassInstance.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        // OK, now varName must be a normal variable.
        // first, let's look at public variables.
        for (Variable var: publicVariables) {
            if (var.getName().equals(varName)) {
                return var;
            }
        }
        // ok, cannot find the member variable from public list, so we need to search parent classes
        for (MFPClassInstance parentCls : parents) {
            Variable var = parentCls.getMemberVariable(ownerName, varName, SpaceMember.AccessRestriction.PUBLIC, progContext);
            if (var != null) {
                return var;
            }
        }
        // if we are allowed to search private.
        if (access == SpaceMember.AccessRestriction.PRIVATE) {
            // no luck in the public searching, let's look at private function list only if the owner is
            // self or the owner is the same class as ownerClassDef. Note that private function cannot be
            // overridden. 
            Statement_function sf = progContext.mstaticProgContext.getCallingFunc();
            // sf.getOwnerClassDef() will never return null because if it returns null, satatement_function will never be analysed.
            MFPClassDefinition funcOwnerClassDef = sf == null ? null:sf.getOwnerClassDef();
            if (getClassDefinition() == funcOwnerClassDef) {
                // same class definition.
                for (Variable var: privateVariables) {
                    if (var.getName().equals(varName)) {
                        return var;
                    }
                }
            } else if (ownerName.equalsIgnoreCase("self")) {
                MFPClassInstance parentInstance = getInstanceFromDef(funcOwnerClassDef);
                if (parentInstance != null) {
                    for (Variable var: parentInstance.privateVariables) {
                        if (var.getName().equals(varName)) {
                            return var;
                        }
                    }
                }
            }
        }
        return null;
    }

    // there is a same name function in MFPClassDefinition. However, that function could be slower because
    // it needs to find super class from class map.
    // functionName has been uncapitalized. But ownerName is only trimmed. But we only need to compare it to self,
    // and create a temp variable, so no need to lowercase it. Simply compare owner name to self ignore case.
    public MemberFunction getMemberFunction(String ownerName, String functionName, int numOfParams, SpaceMember.AccessRestriction access, ProgContext progContext) {
        // first, let's look at public functions.
        MFPClassDefinition clsDef = getClassDefinition();
        for (MemberFunction mf: clsDef.mlistFuncMembers) {
            if (mf.getPureNameWithoutCS().equals(functionName)) {
                int feMaxNumParam = mf.getMaxNumParam();
                int feMinNumParam = mf.getMinNumParam();
                if ((numOfParams >= feMinNumParam) && (feMaxNumParam == -1 || numOfParams <= feMaxNumParam)) {
                    return mf;
                }
            }
        }
        // ok, cannot find the member function from public function list, so we need to search parent classes
        for (MFPClassInstance parentClsInstance : parents) {
            // only search public functions of parent.
            MemberFunction mf = parentClsInstance.getMemberFunction(ownerName,
                    functionName, numOfParams, SpaceMember.AccessRestriction.PUBLIC,
                    progContext);
            if (mf != null) {
                return mf;
            }
        }
        // if we are allowed to search private.
        if (access == SpaceMember.AccessRestriction.PRIVATE) {
            // no luck in the public searching, let's look at private function list only if the owner is
            // self or the owner is the same class as ownerClassDef. Note that private function cannot be
            // overridden.
            Statement_function sf = progContext.mstaticProgContext.getCallingFunc();
            MFPClassDefinition funcOwnerClassDef = sf == null ? null:sf.getOwnerClassDef();
            if (ownerName.equalsIgnoreCase("self") || clsDef == funcOwnerClassDef) {
                // this is self.xxxx. This implies that ownerClassDef cannot be null.
                for (MemberFunction mf: funcOwnerClassDef.privateFunctions) {
                    if (mf.getPureNameWithoutCS().equals(functionName)) {
                        int feMaxNumParam = mf.getMaxNumParam();
                        int feMinNumParam = mf.getMinNumParam();
                        if ((numOfParams >= feMinNumParam) && (feMaxNumParam == -1 || numOfParams <= feMaxNumParam)) {
                            return mf;
                        }
                    }
                }
            }
        }
        return null;
    }
}
