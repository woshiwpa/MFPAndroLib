/*
 * MFP project, ClassAnalyzer.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jsma.SMErrProcessor;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.Oomfp.ClassVariableInfo;
import com.cyzapps.Oomfp.MFPClassDefinition;
import com.cyzapps.Oomfp.OoErrProcessor;
import com.cyzapps.Oomfp.SpaceMember;
import com.cyzapps.Oomfp.SpaceMember.AccessRestriction;
import com.cyzapps.adapter.MFPAdapter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author youxi
 */
public class ClassAnalyzer {
    
    public static MFPClassDefinition analyzeClass(String[] strarrayPathSpace, // path space of the source file
                                                Statement[] sarrayLines,    // statements of this script (until endclass)
                                                String[] strLines,  // all the lines of this script
                                                int[] narrayDeclarationStartEnd,    // The position of class/endclass declaration in sarrayLines 
                                                LinkedList<String[]> lCitingSpaceStack,    // citingspace statements above class declaration
                                                LinkedList<LinkedList<String[]>> lUsingCitingSpacesStack)    // using citingspace statements above class declaration
    {
        int nDeclarationStart = narrayDeclarationStartEnd[0];
        Statement_class clsDeclaration = (Statement_class)sarrayLines[nDeclarationStart].mstatementType;
        clsDeclaration.setCitingSpaces(lCitingSpaceStack, lUsingCitingSpacesStack); // set full citing space for the class.
        String[] thisClassCS = clsDeclaration.getClassNameWithCS().split("::");
        lCitingSpaceStack.push(thisClassCS);
        lUsingCitingSpacesStack.push(new LinkedList<String[]>());
        int[] helpBlock = null;
        if (nDeclarationStart > 1 && (sarrayLines[nDeclarationStart - 2].mstatementType instanceof Statement_help)) {
            // sarrayLines[nDeclarationStart - 1].m_statementType must be Statement_endh
            helpBlock = new int[2];
            helpBlock[0] = sarrayLines[nDeclarationStart - 2].mnEndLineNo;
            helpBlock[1] = sarrayLines[nDeclarationStart - 1].mnStartLineNo;
        }
        LinkedList<ClassVariableInfo> publicVariables = new LinkedList<ClassVariableInfo>();
        LinkedList<ClassVariableInfo> privateVariables = new LinkedList<ClassVariableInfo>();        
        LinkedList<FunctionEntry> publicFunctions = new LinkedList<FunctionEntry>();
        LinkedList<FunctionEntry> privateFunctions = new LinkedList<FunctionEntry>();
        LinkedList<MFPClassDefinition> publicInnerClasses = new LinkedList<MFPClassDefinition>();
        LinkedList<MFPClassDefinition> privateInnerClasses = new LinkedList<MFPClassDefinition>();
        LinkedList<Statement[]> lFuncEndf = new LinkedList<Statement[]>();
        LinkedList<Integer[]> lFuncEndfPos = new LinkedList<Integer[]>();
        for (int idx = nDeclarationStart + 1; idx < sarrayLines.length; idx ++) {
            StatementType thisStatementType = sarrayLines[idx].mstatementType;
            if (thisStatementType instanceof Statement_variable) {
                // this is class variable.
                Statement_variable statementVar = (Statement_variable)thisStatementType;
                if (!statementVar.misSelf) {
                    // static member variable is not supported
                    continue;
                }
                for (int idxVar = 0; idxVar < statementVar.mstrVariables.length; idxVar++) {
                    ClassVariableInfo clsVarInfo = new ClassVariableInfo(statementVar.mstrVariables[idxVar],
                                                                        null, statementVar.mstrVarValues[idxVar],
                                                                        statementVar.maexprVarValues[idxVar],
                                                                        sarrayLines[idx],
                                                                        lCitingSpaceStack, lUsingCitingSpacesStack);
                    if (statementVar.access == SpaceMember.AccessRestriction.PRIVATE) {
                        privateVariables.add(clsVarInfo);
                    } else {
                        publicVariables.add(clsVarInfo);
                    }
                }
            } else if (thisStatementType instanceof Statement_function) {
                // this is a memeber function.
                // the function defined in this file
                ((Statement_function)thisStatementType).setCitingSpaces(lCitingSpaceStack, lUsingCitingSpacesStack);
                Statement[] funcEndfPair = new Statement[2];
                funcEndfPair[0] = sarrayLines[idx];
                funcEndfPair[1] = null;	// means endf hasn't been found.
                lFuncEndf.addLast(funcEndfPair);
                Integer[] funcEndfPosPair = new Integer[2];
                funcEndfPosPair[0] = idx;
                funcEndfPosPair[1] = -1;	// means invalid position.
                lFuncEndfPos.addLast(funcEndfPosPair);
            } else if (thisStatementType instanceof Statement_endf)    {
                if (lFuncEndf.size() > 0) {	//we try to be forward compatible, to allow function nested in function.
                    Statement[] funcEndfPair = lFuncEndf.removeLast();
                    Integer[] funcEndfPosPair = lFuncEndfPos.removeLast();
                    funcEndfPair[1] = sarrayLines[idx];
                    funcEndfPosPair[1] = idx;
                    int[] hb = null;
                    if (funcEndfPosPair[0] > 1 && (sarrayLines[funcEndfPosPair[0] - 2].mstatementType instanceof Statement_help)) {
                        // sarrayLines[nDeclarationStart - 1].m_statementType must be Statement_endh
                        hb = new int[2];
                        hb[0] = sarrayLines[funcEndfPosPair[0] - 2].mnEndLineNo;
                        hb[1] = sarrayLines[funcEndfPosPair[0] - 1].mnStartLineNo;
                    }
                    FunctionEntry functionEntry = new FunctionEntry(strarrayPathSpace, (Statement_function)(funcEndfPair[0].mstatementType), funcEndfPosPair[0],
                                                                    (Statement_endf)(funcEndfPair[1].mstatementType), funcEndfPosPair[1],
                                                                    hb, strLines, sarrayLines);
                    if (lFuncEndf.isEmpty()) {
                        // this is not an embedded function, so we add it into class memeber list.
                        if (((Statement_function)funcEndfPair[0].mstatementType).access == AccessRestriction.PRIVATE) {
                            privateFunctions.add(functionEntry);
                        } else {
                            publicFunctions.add(functionEntry);
                        }
                    }
                }
            } else if (thisStatementType instanceof Statement_class) {
                int[] classStartEnd = new int[2];
                classStartEnd[0] = idx;
                classStartEnd[1] = idx; // set class end position same as class start position to handle a situation where there is no endclass statement.
                MFPClassDefinition innerClass = analyzeClass(strarrayPathSpace, sarrayLines, strLines, classStartEnd, lCitingSpaceStack, lUsingCitingSpacesStack);
                if (((Statement_class)thisStatementType).access == AccessRestriction.PRIVATE) {
                    privateInnerClasses.add(innerClass);
                } else {
                    publicInnerClasses.add(innerClass);
                }
                idx = classStartEnd[1]; // ok, now set idx to the endclass statement postion.
            } else if (thisStatementType instanceof Statement_endclass) {
                narrayDeclarationStartEnd[1] = idx; //endclass statement position.
                break;
            } else if (thisStatementType instanceof Statement_using) {
                Statement_using usingStatementType = (Statement_using)thisStatementType;
                usingStatementType.convertRelativeCS2Absolute(thisClassCS);
                lUsingCitingSpacesStack.getFirst().add(usingStatementType.m_strarrayCitingSpace);
            }   // just ignore other statements, we do not throw exception
        }
        
        MFPClassDefinition mfpClassDef =
                MFPClassDefinition.createClassDefinition(nDeclarationStart, strarrayPathSpace,
                                                        lCitingSpaceStack, lUsingCitingSpacesStack,
                                                        publicInnerClasses, privateInnerClasses,
                                                        publicVariables, privateVariables,
                                                        publicFunctions, privateFunctions,
                                                        helpBlock, strLines, sarrayLines);
        // pop out citing space stack.
        lUsingCitingSpacesStack.poll();
        lCitingSpaceStack.poll();
        return mfpClassDef;
    }
    
    public static void addClass2CitingSpace(MFPClassDefinition mfpClassDef) {
        try {
            CitingSpaceDefinition.getTopCSD().addMember(mfpClassDef);
            for (MFPClassDefinition innerClass : mfpClassDef.publicInnerClasses) {
                addClass2CitingSpace(innerClass);
            }
        } catch (OoErrProcessor.JOoMFPErrException ex) {
            // shouldn't throw the exception.
            Logger.getLogger(MFPAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
