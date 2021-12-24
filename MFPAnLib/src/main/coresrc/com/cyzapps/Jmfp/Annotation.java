/*
 * MFP project, Annotation.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jmfp.ErrorProcessor.ERRORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.Oomfp.MFPClassDefinition;
import com.cyzapps.Oomfp.MemberFunction;
import com.cyzapps.adapter.MFPAdapter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author tonyc
 */
public class Annotation extends StatementType {
	private static final long serialVersionUID = 1L;

	public String mstrInstructionCmd;
    
    public static abstract class AnnotationType implements Serializable {
		private static final long serialVersionUID = 1L;
		public StatementType mstatementType;
        
        public abstract String getCmdTypeString();
        public abstract void analyze(String strInstructionDetails) throws ErrorProcessor.JMFPCompErrException;
    }
    public AnnotationType mannoType;
    
    public String mstrParameter;
    
    Annotation(Statement s, String strInstructionCmd)    {
        mstatement = s;
        mstrInstructionCmd = strInstructionCmd; // have been lower cased so no need to do it again.
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "_compile_instruction_";
    }

    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {
        if (strStatement.length() < 1 + mstrInstructionCmd.length() || strStatement.charAt(0) != '@') {
            ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
        
        if (!mstrInstructionCmd.equals(AnnoType_compulsory_link.getCmdTypeStr())
                && !mstrInstructionCmd.equals(AnnoType_build_asset.getCmdTypeStr())) {
            return; // this statement is not supported. so ignore it.
        } else {
        	String strDetails = strStatement.substring(mstrInstructionCmd.length() + 1);
        	if (strDetails.startsWith(":")) {
        		strDetails = strDetails.substring(1); // if starts with :, remove :
        	}
            strDetails = strDetails.trim(); // cannot call toLowerCase() because like build_asset may include file name which is case sensative 
            if (mstrInstructionCmd.equals(AnnoType_compulsory_link.getCmdTypeStr())) {
                mannoType = new AnnoType_compulsory_link(this);
                mannoType.analyze(strDetails);
            } else {
                mannoType = new AnnoType_build_asset(this);
                mannoType.analyze(strDetails);
            }
        }
    }
    
    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) throws InterruptedException {
        if (!mstrInstructionCmd.equals(AnnoType_compulsory_link.getCmdTypeStr())) {
            return new LinkedList<>(); // only compulsory_link has referred modules.
        } else {
            List<String[]> lCitingSpaces = progContext.mstaticProgContext.getCitingSpaces();
            LinkedList<ModuleInfo> lNewModuleInfo = new LinkedList<>();
            for (int idx = 0; idx < ((AnnoType_compulsory_link)mannoType).mlistModuleInfo.size(); idx ++) {
                // note that strName has been small letter. Now shrink it.
                ModuleInfo moduleInfo = ((AnnoType_compulsory_link)mannoType).mlistModuleInfo.get(idx);
                String strShrinkedRawName = FunctionEntry.getShrinkedName(moduleInfo.mstrModuleName);
                if (strShrinkedRawName.equals("*")) {
                    lNewModuleInfo.add(moduleInfo);
                } else {
                    LinkedList<MemberFunction> listMf;
                    if (moduleInfo.mnModuleParam1 < 0) {
                        listMf = CitingSpaceDefinition.lookupFunctionDef(strShrinkedRawName, -1, true, lCitingSpaces);
                    } else {
                        MemberFunction mf = CitingSpaceDefinition.locateFunctionCall(strShrinkedRawName, moduleInfo.mnModuleParam1, lCitingSpaces);
                        if (mf == null) {
                            continue;
                        }
                        listMf = new LinkedList<>();
                        listMf.add(mf);
                    }
                    for (int idx1 = 0; idx1 < listMf.size(); idx1 ++ ) {
                        ModuleInfo newModuleInfo = new ModuleInfo();
                        newModuleInfo.mnModuleType = moduleInfo.mnModuleType;
                        newModuleInfo.mstrModuleName = listMf.get(idx1).getAbsNameWithCS();
                        newModuleInfo.mnModuleParam1 = moduleInfo.mnModuleParam1;

                        MFPClassDefinition mfpClsDef = null;
                        if (newModuleInfo.mnModuleParam1 == 0) {            // might be a constructor.
                            mfpClsDef = MFPClassDefinition.getClassDefinitionMap().get(newModuleInfo.mstrModuleName);
                        }
                        if (mfpClsDef == null) {
                            // normal function.
                            boolean bAdd = true;
                            for (int idx2 = 0; idx2 < lNewModuleInfo.size(); idx2 ++) {
                                if (lNewModuleInfo.get(idx2).mstrModuleName.equals(newModuleInfo.mstrModuleName)
                                        && lNewModuleInfo.get(idx2).mnModuleParam1 == newModuleInfo.mnModuleParam1) {
                                    bAdd = false;
                                    break;
                                }
                            }
                            if (bAdd) {
                                // add it.
                                lNewModuleInfo.add(newModuleInfo);
                            }
                        } else {
                            // constructor
                            CompileAdditionalInfo cai = new CompileAdditionalInfo();
                            // this function will return a class module itself.
                            LinkedList<ModuleInfo> listModules = mfpClsDef.getReferredModules(cai);
                            ModuleInfo.mergeIntoList(listModules, lNewModuleInfo);
                        }
                    }
                }
            }
            return lNewModuleInfo;
        }
    }

    @Override
    protected void analyze2(FunctionEntry fe) {
    }

    @Override
    protected void analyze2(ProgContext progContext, boolean bForceReanalyse) {
    }
    
    /**
     * execute the function defined for this annotation. The result of the function will the input
     * of the annotation.
     * @param annotationType : this cannot be null.
     * @param strDetails : the detail of the annotation statement
     * @return
     * @throws com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException
     * @throws InterruptedException 
     */
    public static DataClass executeDetails(AnnotationType annotationType, String strDetails) throws ErrProcessor.JFCALCExpErrException, InterruptedException {
        LinkedList<LinkedList<VariableOperator.Variable>> lVarNameSpaces = new LinkedList<>();
        ProgContext progContext = new ProgContext();
        List<String[]> citingSpaces = new LinkedList<>();
        citingSpaces.add(new String[] {"", "mfp_compiler", "annotation", annotationType.getCmdTypeString()});
        citingSpaces.add(new String[] {"", "mfp_compiler", "annotation"});
        progContext.mstaticProgContext.setCitingSpacesExplicitly(MFPAdapter.getAllCitingSpaces(citingSpaces));
        progContext.mstaticProgContext.setCallingAnnotation((Annotation) annotationType.mstatementType);
        progContext.mdynamicProgContext.mlVarNameSpaces = lVarNameSpaces;
        ExprEvaluator exprEvaluator = new ExprEvaluator(progContext);
        DCHelper.CurPos c = new DCHelper.CurPos();
        c.m_nPos = 0;
        DataClass datumValue = exprEvaluator.evaluateExpression(strDetails, c);
        return datumValue;
    }
}
