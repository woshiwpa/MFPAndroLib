// MFP project, InterfVarUpdateRunner.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.OSAdapter.ParallelManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jmfp.VariableOperator;

public class InterfVarUpdateRunner extends VariableOperator.VarUpdateRunner {

	protected CallObject callObj;
	protected String varName; // this varName is full shrinked and lowercased var name, like xxx.xxx.xxx
	
	public InterfVarUpdateRunner(CallObject callObject, String variableName) {
		callObj = callObject;
		varName = variableName;
	}
	
	@Override
	public void run() {
		String cmd = CallCommPack.VALUE_COMMAND;
        
        CallCommPack callCommPack = new CallCommPack(callObj.getCallPoint(), callObj.getCallCommPackIndex(), varName, datum2Update);
        String strSerialized = "";
		try {
			strSerialized = FuncEvaluator.msCommMgr.serialize(callCommPack);    // assume msCommMgr is not null.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		Boolean sendResult = callObj.getConnectObject().sendCallRequest(callObj, cmd, "", strSerialized);
	}

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof InterfVarUpdateRunner)) {
            return false;
        }

        if (callObj == ((InterfVarUpdateRunner)obj).callObj
                && (varName == null ? ((InterfVarUpdateRunner)obj).varName == null : varName.equals(((InterfVarUpdateRunner)obj).varName))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.callObj != null ? this.callObj.hashCode() : 0);
        hash = 53 * hash + (this.varName != null ? this.varName.hashCode() : 0);
        return hash;
    }
}
