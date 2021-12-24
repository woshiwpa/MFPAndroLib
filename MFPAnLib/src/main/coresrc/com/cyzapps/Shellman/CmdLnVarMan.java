// MFP project, CmdLnVarMan.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Shellman;

import java.util.LinkedList;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;

public class CmdLnVarMan {
    
    // the variables always exist
    public LinkedList<Variable> mPermVars = new LinkedList<Variable>();

    // No priorities between the variables, but we still need to have the right order
    public LinkedList<Variable> mAllVars = new LinkedList<Variable>();
    
    public CmdLnVarMan(LinkedList<Variable> permVars, LinkedList<Variable> allVars) {
        mPermVars = permVars;
        mAllVars = allVars;
    }
    
    public String listAllCmdLnVars() {
        String str = "";
        for (Variable var : mAllVars) {
            str += var.getName() + " : " + var.getValue().toString() + "\n";
        }
        return str;
    }

    /**
     * Add a new variable to command line.
     * @return 0 if the variable has existed, in this case the value is updated, 1 if the variable is new.
     */
    public int addVarIntoCmdLn(Variable var) {
        Variable varExist = VariableOperator.lookUpList(var.getName(), mAllVars);
        if (varExist != null) {
            varExist.setValue(var.getValue());
            return 0;
        } else {
            mAllVars.addLast(var);
            return 1;
        }
    }

    // strVarName must be lowercased and trimmed.
    public int addVarIntoCmdLn(String strVarName) {
        return addVarIntoCmdLn(new Variable(strVarName));
    }

    // true if can be deleted, false if cannot.
    // strVarName is trimmered and lowercased.
    public boolean deleteVarFromCmdLn(String strVarName) {
        for (int idx = 0; idx < mAllVars.size(); idx ++) {
            if (strVarName.equals(mAllVars.get(idx).getName())
                    && VariableOperator.lookUpList(strVarName, mPermVars) == null) {
                // this variable exist and can be deleted.
                mAllVars.remove(idx);
                return true;
            }
        }
        return false;
    }

    public void clearCmdLnVars() {
        mAllVars.clear();
        mAllVars.addAll(mPermVars);
    }
    
}
