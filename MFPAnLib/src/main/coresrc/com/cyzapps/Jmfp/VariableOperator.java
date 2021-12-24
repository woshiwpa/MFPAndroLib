// MFP project, VariableOperator.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.DCHelper;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassClass;
import com.cyzapps.Jfcalc.DataClassComplex;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.ElemAnalyzer;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jfcalc.Operators;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import com.cyzapps.Oomfp.MFPClassInstance;
import com.cyzapps.Oomfp.SpaceMember;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VariableOperator {
	public static class VarUpdateRunner implements Runnable {
        //todo: client to server update.
		public DataClass datum2Update;
		@Override
		public void run() {}
	}
	
    // variable name is not case sensitive.
    public static class Variable implements Serializable   {
		private static final long serialVersionUID = 1L;
		private final Lock lock = new ReentrantLock(); // any deserialized lock is always unlocked irrespective of the state when it was serialized.
    	//should we lock and unlock in two different functions?
    	// these two functions have to be public because CallAgent will use them.
    	public void acquireLock(boolean isWrite) {
    		// read / write lock on the same lock.
    		lock.lock();
    	}
    	public void releaseLock() {
    		// should be fine even if it is not locked.
    		lock.unlock();
    	}
        
        public static class InterruptSuspensionCondition {
            public String opt = "";
            public DataClass newValue = null;
            
            public InterruptSuspensionCondition(String operator, DataClass theNewValue) {
                opt = operator;
                if (opt.length() > 0) {
                    newValue = theNewValue;
                } else {
                    newValue = null;
                }
            }
            
            public boolean doInterrupt(DataClass oldValue, DataClass setValue) {
                try {
                    // this means we need to compare datumValue with the variable's value.
                    // however, condition may satisfied before we monitor the variable.
                    switch (opt) {
                        case "":
                            return true;
                        case "==":
                            return setValue.isBitEqual(newValue == null?oldValue:newValue);
                        case "!=":
                            return !setValue.isBitEqual(newValue == null?oldValue:newValue);
                        default:
                            DataClass datum1stOperand = setValue;
                            DataClass datum2ndOperand = (newValue == null)?oldValue:newValue;
                            Boolean retValue = false;
                            switch (opt) {
                                case ">":
                                    retValue = DCHelper.lightCvtOrRetDCMFPDec(datum1stOperand).getDataValue()
                                                .compareTo(DCHelper.lightCvtOrRetDCMFPDec(datum2ndOperand).getDataValue()) > 0;
                                    break;
                                case "<":
                                    retValue = DCHelper.lightCvtOrRetDCMFPDec(datum1stOperand).getDataValue()
                                                .compareTo(DCHelper.lightCvtOrRetDCMFPDec(datum2ndOperand).getDataValue()) < 0;
                                    break;
                                case ">=":
                                    if (datum1stOperand.isBitEqual(datum2ndOperand)) {
                                        retValue = true;
                                    } else {
                                        retValue = DCHelper.lightCvtOrRetDCMFPDec(datum1stOperand).getDataValue()
                                                    .compareTo(DCHelper.lightCvtOrRetDCMFPDec(datum2ndOperand).getDataValue()) > 0;
                                    }
                                    break;
                                default:
                                    // if (opt.equals("<=")) {
                                    if (datum1stOperand.isBitEqual(datum2ndOperand)) {
                                        retValue = true;
                                    } else {
                                        retValue = DCHelper.lightCvtOrRetDCMFPDec(datum1stOperand).getDataValue()
                                                    .compareTo(DCHelper.lightCvtOrRetDCMFPDec(datum2ndOperand).getDataValue()) < 0;
                                    }
                                    break;
                            }
                            return retValue;
                    }
                } catch (JFCALCExpErrException ex) {
                    // may be here. This means the comparison threw exception. So return false.
                    Logger.getLogger(VariableOperator.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
            
            @Override
            public boolean equals(Object o) {
                if (o == this) {
                    return true;
                } else if (o instanceof InterruptSuspensionCondition) {
                    InterruptSuspensionCondition isc = (InterruptSuspensionCondition) o;
                    if (newValue != null && isc.newValue != null) {
                        try {
                            return newValue.isEqual(isc.newValue) && opt.equals(isc.opt);
                        } catch (JFCALCExpErrException ex) {
                            // this actually will not happen.
                            Logger.getLogger(VariableOperator.class.getName()).log(Level.SEVERE, null, ex);
                            return false;
                        }
                    } else if (newValue == null && isc.newValue == null) {
                        return opt.equals(isc.opt);
                    } else {
                        return false;   // one newValue is null, the other newValue is not null
                    }
                } else {
                    return false;
                }
            }

            @Override
            public int hashCode() {
                int hash = 5;
                hash = 71 * hash + Objects.hashCode(this.newValue);
                hash = 71 * hash + Objects.hashCode(this.opt);
                return hash;
            }
        }
        // no need to use concurrent list because it is always protected by lock.
        private final Set<InterruptSuspensionCondition> interruptSuspensionCondCollection = new HashSet<InterruptSuspensionCondition>();
        public InterruptSuspensionCondition addInterruptSuspensionCond(String opt, DataClass theNewValue) {
            InterruptSuspensionCondition interruptSuspensionCond = new InterruptSuspensionCondition(opt, theNewValue);
            for (InterruptSuspensionCondition one : interruptSuspensionCondCollection) {
                if (one.equals(interruptSuspensionCond)) {
                    return one;
                }
            }
            interruptSuspensionCondCollection.add(interruptSuspensionCond);
            return interruptSuspensionCond;
        }
    	
        //runnable will change on the remote side so it needs not serialized.
    	private transient Set<VarUpdateRunner> setVarUpdateRunner = new HashSet<VarUpdateRunner>();
    	public void addWriteUpdateRunner(VarUpdateRunner rNew) {
            acquireLock(true);
            try {
                setVarUpdateRunner.add(rNew);
            } finally {
                releaseLock();
            }
    	}
        
    	public void removeWriteUpdateRunner(VarUpdateRunner rExisting) {
            acquireLock(true);
            try {
                setVarUpdateRunner.remove(rExisting);
            } finally {
                releaseLock();
            }
    	}
    	
        protected String mstrName = "";
        protected DataClass mdatumValue = new DataClassNull();
        
        public Variable() {
        }
        // strName must be trimmed and lowercased.
        public Variable(String strName)    {
            setName(strName);
        }
        public Variable(String strName, DataClass datumValue)    {
            setVariableLockless(strName, datumValue);
        }
        
        // need this function to initialize setVarUpdateRunner.
        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            setVarUpdateRunner = new HashSet<VarUpdateRunner>();
        }
    
        // strName must be trimmed and lowercased.
        public final void setName(String strName)    {
            mstrName = strName;
        }
        
        public String getName()    {
            return mstrName;
        }
        
        public void setValue(DataClass datumValue)    {
            setValueSkipNotice(datumValue, null);
        }
        
        public void setValueSkipNotice(DataClass datumValue, VarUpdateRunner rNotNotice)    {
            acquireLock(true);
            try {
                if (interruptSuspensionCondCollection.size() > 0) {
                    // the following statements are protected by lock so that it is thread safe
                    Set<InterruptSuspensionCondition> iscToRemove = new HashSet<InterruptSuspensionCondition>();
                    for(InterruptSuspensionCondition isc : interruptSuspensionCondCollection) {
                        if (isc.doInterrupt(mdatumValue, datumValue)) {
                            synchronized(isc) {
                                isc.notifyAll();
                            }
                            iscToRemove.add(isc);
                        }
                    }
                    interruptSuspensionCondCollection.removeAll(iscToRemove);
                }
        		mdatumValue = datumValue;
                
                Iterator<VarUpdateRunner> itr = setVarUpdateRunner.iterator(); // traversing over HashSet
                while(itr.hasNext()){
					VarUpdateRunner rRef = itr.next();
                    if (rRef.equals(rNotNotice)) {
                        continue;   // skip this update runner.
                    }
                    rRef.datum2Update = datumValue;
                    rRef.run(); // notify remote that a new value is set.
                }
            } finally {
        		releaseLock();
        	}
        }
        
        public DataClass getValue()    {
        	acquireLock(false);
        	try {
        		return mdatumValue;    // have to use reference, cannot use copy or deep copy for variable values
        	} finally {
        		releaseLock();
        	}
    	}
        
        // public access of this function is not allowed.
        // strName must be trimmed and lowercased.
        protected void setVariable(String strName, DataClass datumValue)    {
        	acquireLock(true);
        	setVariableLockless(strName, datumValue);
        	releaseLock();
        }
        // because it is for constructor to call, we do not acquire lock.
        // strName must be trimmed and lowercased.
        protected final void setVariableLockless(String strName, DataClass datumValue) {
            mstrName = strName;
            mdatumValue = datumValue;
        }
        
        public void clear()    {
        	acquireLock(true);
            // something like free memory although no memory is actually freed.
            mdatumValue = new DataClassNull();
            releaseLock();
        }
        
        public static boolean isValidVarNameWithNoCS(String strName)    {
            if (strName == null)    {
                return false;
            } else if (strName.trim().length() != strName.length())    {
                // blanks before and after
                return false;
            } else if (strName.length() == 0)    {
                return false;
            } else {
                if (ElemAnalyzer.isNameChar(strName, 0) != 1)    {
                    return false;    // first char is wrong.
                }
                for (int idx = 0; idx < strName.length(); idx ++)    {
                    if (ElemAnalyzer.isNameChar(strName, idx) == 0)    {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    // the input must be lowercased and trimmed.
    public static Variable lookUpPreDefined(String strLowerCaseName) throws JFCALCExpErrException    {
        Variable var = null;
        if (strLowerCaseName.equals("null"))    {
            var = new Variable(strLowerCaseName);
            DataClass datumValue = new DataClassNull();    // this is create NULL
            var.setValue(datumValue);
        } else if (strLowerCaseName.equals("true"))    {
            var = new Variable(strLowerCaseName);
            DataClass datumValue = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
            var.setValue(datumValue);
        } else if (strLowerCaseName.equals("false"))    {
            var = new Variable(strLowerCaseName);
            DataClass datumValue = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            var.setValue(datumValue);
        } else if (strLowerCaseName.equals("pi"))    {
            var = new Variable(strLowerCaseName);
            // The accuracy of Math.PI is not high enough because it is a double
            DataClass datumValue = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.PI);
            var.setValue(datumValue);
        } else if (strLowerCaseName.equals("e"))    {
            var = new Variable(strLowerCaseName);
            // The accuracy of Math.E is not high enough because it is a double
            DataClass datumValue = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.E);
            var.setValue(datumValue);
        } else if (strLowerCaseName.equals("inf"))    {
            var = new Variable(strLowerCaseName);
            DataClass datumValue = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.INF);
            var.setValue(datumValue);
        } else if (strLowerCaseName.equals("infi"))    {
            var = new Variable(strLowerCaseName);
            DataClass datumValue = new DataClassComplex(MFPNumeric.ZERO, MFPNumeric.INF);
            var.setValue(datumValue);
        } else if (strLowerCaseName.equals("nan"))    {
            var = new Variable(strLowerCaseName);
            DataClass datumValue = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.NAN);
            var.setValue(datumValue);
        } else if (strLowerCaseName.equals("nani"))    {
            var = new Variable(strLowerCaseName);
            DataClass datumValue = new DataClassComplex(MFPNumeric.ZERO, MFPNumeric.NAN);
            var.setValue(datumValue);
        }
        return var;
    }
    
    // strName must be trimmed and lowercased.
    public static DataClass lookUpPreDefined4Value(String strName) throws JFCALCExpErrException    {
        Variable v = lookUpPreDefined(strName);
        if (v == null)
            return null;
        else
            return v.getValue();
    }
    
    // variable name must be lower cased and trimmed.
    public static Variable lookUpList(String strLowerCaseName, LinkedList<Variable> lVars)    {
        ListIterator<Variable> itr = lVars.listIterator();
        while (itr.hasNext())    {
            Variable var = itr.next();
            if(var.getName().equals(strLowerCaseName))    {
                return var; // same name
            }
        }
        return null;
    }
    // variable name must be lower cased and trimmed.
    public static DataClass lookUpList4Value(String strName, LinkedList<Variable> lVars)    {
        Variable v = lookUpList(strName, lVars);
        if (v == null)
            return null;
        else
            return v.getValue();
    }
    // variable name must be lower cased and trimmed.
    public static Variable lookUpSpaces(String strName, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws JFCALCExpErrException    {
        ListIterator<LinkedList<Variable>> itr = lVarNameSpaces.listIterator();
        while (itr.hasNext())    {
            Variable var = lookUpList(strName, itr.next());
            if(var != null)    {
                return var;
            }
        }
        return lookUpPreDefined(strName);    // if we cannot find the variable in our defined variable space,
                                            // look up the predefined variable space.
    }
    // variable name must be lower cased and trimmed.
    public static DataClass lookUpSpaces4Value(String strName, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws JFCALCExpErrException    {
        Variable v = lookUpSpaces(strName, lVarNameSpaces);
        if (v == null)
            return null;
        else
            return v.getValue();
    }
    
    // varName must be
    // 1. shrinked; 2. lower cased; 3. varName has to be xxx.xxx.xxx ..., all varName parts must be a (member) variable not a function or array.
    // 4. varName must be valid, i.e. like xxx. cannot be here.
    public static Variable lookForVarOrMemberVar(String varName, SpaceMember.AccessRestriction access, ProgContext progContext) throws JFCALCExpErrException {
        String[] varNameParts = varName.split("\\.");
        Variable var = lookUpSpaces(varNameParts[0], progContext.mdynamicProgContext.mlVarNameSpaces);
        int currentStrIdx = varNameParts[0].length();
        for (int idx = 1; idx < varNameParts.length; idx ++) {
            if (var == null) {
                return null;
            }
            DataClassClass datumOwner = DCHelper.lightCvtOrRetDCClass(var.getValue());
            MFPClassInstance clsInstance = datumOwner.getClassInstance();
            var = clsInstance.getMemberVariable(varName.substring(0, currentStrIdx), varNameParts[idx], access, progContext);
            currentStrIdx += (1 + varNameParts[idx].length());
        }
        return var;
    }
    // varName must be
    // 1. shrinked; 2. lower cased; 3. varName has to be xxx.xxx.xxx ..., all varName parts must be a (member) variable not a function or array.
    // 4. varName must be valid, i.e. like xxx. cannot be here.
    public static DataClass lookForVarOrMemberVar4Value(String varName, SpaceMember.AccessRestriction access, ProgContext progContext) throws JFCALCExpErrException {
        Variable v = lookForVarOrMemberVar(varName, access, progContext);
        if (v == null)
            return null;
        else
            return v.getValue();
    }
    
    // variable name must be lower cased and trimmed.
    public static DataClass setValueInList(LinkedList<Variable> lVars, String strName, DataClass datumValue)    {
        Variable v = lookUpList(strName, lVars);
        if (v != null)    {
            v.setValue(datumValue);
            return datumValue;
        } else    {
            return null;
        }
    }
    // variable name must be lower cased and trimmed.
    public static DataClass setValueInSpaces(LinkedList<LinkedList<Variable>> lVarNameSpaces, String strName, DataClass datumValue) throws JFCALCExpErrException    {
        Variable v = lookUpSpaces(strName, lVarNameSpaces);
        if (v != null)    {
            v.setValue(datumValue);
            return datumValue;
        } else    {
            return null;
        }
    }
    
    // varName must be
    // 1. shrinked; 2. lower cased; 3. varName has to be xxx.xxx.xxx ..., all varName parts must be a (member) variable not a function or array.
    // 4. varName must be valid, i.e. like xxx. cannot be here.
    public static DataClass setValue4VarOrMemberVar(String varName, SpaceMember.AccessRestriction access, ProgContext progContext, DataClass datumValue) throws JFCALCExpErrException {
        Variable v = lookForVarOrMemberVar(varName, access, progContext);
        if (v != null)    {
            v.setValue(datumValue);
            return datumValue;
        } else    {
            return null;
        }
    }
    
    public static LinkedList<Variable> cloneVarList(LinkedList<Variable> listVars) throws JFCALCExpErrException   {
        LinkedList<Variable> listVarsNew = new LinkedList<Variable>();
        for (int idx = 0; idx < listVars.size(); idx ++)    {
            if (listVars.get(idx) instanceof UnknownVariable) {
                UnknownVariable var = new UnknownVariable(listVars.get(idx).getName());
                if (((UnknownVariable)listVars.get(idx)).isValueAssigned()) {
                    DataClass datumValue = listVars.get(idx).getValue().cloneSelf();
                    var.setValue(datumValue);
                }
                listVarsNew.add(var);
            } else {    //Variable
                Variable var = new Variable(listVars.get(idx).getName());
                DataClass datumValue = listVars.get(idx).getValue().cloneSelf();
                var.setValue(datumValue);
                listVarsNew.add(var);
            }
        }
        return listVarsNew;
    }
    
    public static LinkedList<LinkedList<Variable>> cloneVarSpaces(LinkedList<LinkedList<Variable>> lVarNameSpaces) throws JFCALCExpErrException  {
        LinkedList<LinkedList<Variable>> lVarNameSpacesNew = new LinkedList<LinkedList<Variable>>();
        for (int idx = 0; idx < lVarNameSpaces.size(); idx ++)  {
            lVarNameSpacesNew.add(cloneVarList(lVarNameSpaces.get(idx)));
        }
        return lVarNameSpacesNew;
    }
    
    // different from clone, copy doesn't reproduce variable.
    public static LinkedList<Variable> copyVarList(LinkedList<Variable> listVars)   {
        LinkedList<Variable> listVarsNew = new LinkedList<Variable>();
        listVarsNew.addAll(listVars);
        return listVarsNew;
    }
    
    public static LinkedList<LinkedList<Variable>> copyVarSpaces(LinkedList<LinkedList<Variable>> lVarNameSpaces)  {
        LinkedList<LinkedList<Variable>> lVarNameSpacesNew = new LinkedList<LinkedList<Variable>>();
        for (int idx = 0; idx < lVarNameSpaces.size(); idx ++)  {
            lVarNameSpacesNew.add(copyVarList(lVarNameSpaces.get(idx)));
        }
        return lVarNameSpacesNew;
    }
}
