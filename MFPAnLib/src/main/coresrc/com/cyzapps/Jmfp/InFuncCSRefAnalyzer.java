// MFP project, InFuncCSRefAnalyzer.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jmfp;

import java.util.LinkedList;

import com.cyzapps.Jmfp.ScriptAnalyzer.InFunctionCSManager;

/**
 * 
 * @author tony
 * Some assumptions made here:
 * 1. all blocks, except help block end at endf and its block end statement(s);
 * 2. all pieces end at endf and its piece end statement(s);
 * 3. a statement can be both piece start and piece end statement. However, a statement cannot
 * be be both piece start and piece end of the same piece;
 * 4. a block start statement cannot be a block end or a piece end statement;
 * 5. a block end statement cannot be a block start or a piece start statement.
 */
public class InFuncCSRefAnalyzer {
    public static final InFuncCSRefAnalyzer HELP_ENDH = new InFuncCSRefAnalyzer(Statement_help.getTypeStr(), new String[] {Statement_endh.getTypeStr()},
            new String[] {},
            new String[] {}, false);
    public static final InFuncCSRefAnalyzer FUNCTION_ENDF = new InFuncCSRefAnalyzer(Statement_function.getTypeStr(), new String[] {Statement_endf.getTypeStr()},
            new String[] {Statement_function.getTypeStr()},
            new String[] {Statement_endf.getTypeStr(), Statement_throw.getTypeStr(), Statement_return.getTypeStr()}, false);
    public static final InFuncCSRefAnalyzer CALL_ENDCALL = new InFuncCSRefAnalyzer(Statement_call.getTypeStr(), new String[] {Statement_endf.getTypeStr(), Statement_endcall.getTypeStr()},
            new String[] {Statement_call.getTypeStr()}, new String[] {Statement_endf.getTypeStr(), Statement_endcall.getTypeStr(), Statement_return.getTypeStr(), Statement_throw.getTypeStr()}, false);
    public static final InFuncCSRefAnalyzer IF_ENDIF = new InFuncCSRefAnalyzer(Statement_if.getTypeStr(), new String[] {Statement_endf.getTypeStr(), Statement_endif.getTypeStr()},
            new String[] {Statement_if.getTypeStr(), Statement_elseif.getTypeStr(), Statement_else.getTypeStr()},
            new String[] {Statement_endf.getTypeStr(), Statement_elseif.getTypeStr(), Statement_else.getTypeStr(), Statement_endif.getTypeStr(), Statement_break.getTypeStr(), Statement_continue.getTypeStr(), Statement_throw.getTypeStr(), Statement_return.getTypeStr()}, false);
    public static final InFuncCSRefAnalyzer SELECT_ENDS = new InFuncCSRefAnalyzer(Statement_select.getTypeStr(), new String[] {Statement_endf.getTypeStr(), Statement_ends.getTypeStr()},
            new String[] {Statement_case.getTypeStr(), Statement_default.getTypeStr()},
            new String[] {Statement_endf.getTypeStr(), Statement_ends.getTypeStr(), Statement_break.getTypeStr(), Statement_continue.getTypeStr(), Statement_throw.getTypeStr(), Statement_return.getTypeStr()}, true);
    public static final InFuncCSRefAnalyzer FOR_NEXT = new InFuncCSRefAnalyzer(Statement_for.getTypeStr(), new String[] {Statement_endf.getTypeStr(), Statement_next.getTypeStr()},
            new String[] {Statement_for.getTypeStr()}, new String[] {Statement_endf.getTypeStr(), Statement_next.getTypeStr(), Statement_break.getTypeStr(), Statement_continue.getTypeStr(), Statement_throw.getTypeStr(), Statement_return.getTypeStr()}, false);
    public static final InFuncCSRefAnalyzer WHILE_LOOP = new InFuncCSRefAnalyzer(Statement_while.getTypeStr(), new String[] {Statement_endf.getTypeStr(), Statement_loop.getTypeStr()},
            new String[] {Statement_while.getTypeStr()}, new String[] {Statement_endf.getTypeStr(), Statement_loop.getTypeStr(), Statement_break.getTypeStr(), Statement_continue.getTypeStr(), Statement_throw.getTypeStr(), Statement_return.getTypeStr()}, false);
    public static final InFuncCSRefAnalyzer DO_UNTIL = new InFuncCSRefAnalyzer(Statement_do.getTypeStr(), new String[] {Statement_endf.getTypeStr(), Statement_until.getTypeStr()},
            new String[] {Statement_do.getTypeStr()}, new String[] {Statement_endf.getTypeStr(), Statement_until.getTypeStr(), Statement_break.getTypeStr(), Statement_continue.getTypeStr(), Statement_throw.getTypeStr(), Statement_return.getTypeStr()}, false);
    public static final InFuncCSRefAnalyzer TRY_ENDTRY = new InFuncCSRefAnalyzer(Statement_try.getTypeStr(), new String[] {Statement_endf.getTypeStr(), Statement_endtry.getTypeStr()},
            new String[] {Statement_try.getTypeStr(), Statement_catch.getTypeStr()},
            new String[] {Statement_endf.getTypeStr(), Statement_catch.getTypeStr(), Statement_endtry.getTypeStr(), Statement_break.getTypeStr(), Statement_continue.getTypeStr(), Statement_throw.getTypeStr(), Statement_return.getTypeStr()}, false);
    
    public static InFuncCSRefAnalyzer[] msarrayCSRefAnalyzers = new InFuncCSRefAnalyzer[] {
            CALL_ENDCALL, IF_ENDIF, SELECT_ENDS, FOR_NEXT, WHILE_LOOP, DO_UNTIL, TRY_ENDTRY
    };

    public String mstrBlockStart;
    public String[] mstrarrayBlockEnd;
    public String[] mstrarrayPieceStarts;
    public String[] mstrarrayPieceEnds;
    public boolean mbAllowParallelPieces;    //allow multi pieces cover same area of code
    
    public InFuncCSRefAnalyzer(String strBlockStart, String[] strarrayBlockEnd,
            String[] strarrayPieceStarts, String[] strarrayPieceEnds, boolean bAllowParallel) {
        mstrBlockStart = strBlockStart;
        mstrarrayBlockEnd = strarrayBlockEnd;
        mstrarrayPieceStarts = strarrayPieceStarts;
        mstrarrayPieceEnds = strarrayPieceEnds;
        mbAllowParallelPieces = bAllowParallel;
    }
    
    // this function return all the referred function modules INSIDE a function block (not including
    // function declaration) or other blocks like for, do etc (not including block starting statement,
    // i.e. for, do, select etc., but including block end statement i.e. until, endtry etc.).
    // Note that returned ListedList<ModuleInfo> may include duplicates.
    public static int getAllReferredModules(int nStartLine, Statement[] statements,
            InFuncCSRefAnalyzer csAnalyzer, LinkedList<ModuleInfo> listAllReferredModules, CompileAdditionalInfo cai,
            ProgContext progContext, InFunctionCSManager inFuncCSMgr) throws InterruptedException {
        int idx = nStartLine;
        while (idx < statements.length) {
            // The following code is to check if the code is redundant.
            // now probe new block which out of executable code piece, for example:
            // if ...
            //     throw ...
            //     for ...
            //        ...
            //     next
            // endif
            // the for ... next block is a new block but it is dead code, it is out of any
            // executable code pieces.
            // another example is
            // select ...
            // if ...
            //     ...
            // endif
            // case ...
            //     ...
            // ends
            // the if ... endif block between select and its first case is useless code.
            int nStartNewBlock = 0;
            InFuncCSRefAnalyzer newBlockAnalyzer = null;
            if (idx > nStartLine) {
                // ok, this could be the start of a new block.
                for (InFuncCSRefAnalyzer aCSAnalyzer : InFuncCSRefAnalyzer.msarrayCSRefAnalyzers) {
                    if (statements[idx].mstatementType.mstrType.equals(aCSAnalyzer.mstrBlockStart)) {
                        // it is the start of a new block.
                        nStartNewBlock++;
                        newBlockAnalyzer = aCSAnalyzer;
                        break;
                    }
                }
            }
            while (nStartNewBlock > 0 && idx < statements.length) { // nStartNewBlock is 0 means we are at the end of the redundant block.
                idx ++;
                if (statements[idx].mstatementType.mstrType.equals(newBlockAnalyzer.mstrBlockStart)) {
                    nStartNewBlock ++;
                }
                for(String strBlockEnd : newBlockAnalyzer.mstrarrayBlockEnd)  {
                    if (statements[idx].mstatementType.mstrType.equals(strBlockEnd)) {
                        nStartNewBlock --;
                        break;    // finish.
                    }
                }
            }
            // the above code is to check if the code is redundant.
            
            if (idx >= statements.length) {
                // ok, we arrive at the bottom of the file, break and exit.
                break;
            }
            
            boolean bStartNewPiece = false;
            for (String strPieceStart : csAnalyzer.mstrarrayPieceStarts) {
                if (statements[idx].mstatementType.mstrType.equals(strPieceStart)) {
                    bStartNewPiece = true;
                    break;
                }
            }
            if (bStartNewPiece) {
                int nPieceStartLn = idx;
                // cannot use idx ++; for a piece start but not block start statement here
                // because start of a new piece statement, like case, can also have function calls
                if (idx == nStartLine) {
                    idx ++;
                }
                inFuncCSMgr.pushCSStack();
                while (idx < statements.length) {    // check if idx out of scope.
                    if (statements[idx].mstatementType.mstrType.equals(Statement_using.getTypeStr())) {
                        Statement_using su = (Statement_using)(statements[idx].mstatementType);
                        su.convertRelativeCS2Absolute(inFuncCSMgr.mbaseCitingSpace);
                        inFuncCSMgr.addNewCS(su.m_strarrayCitingSpace);
                        idx ++;
                    } else {
                        LinkedList<ModuleInfo> listThisStatementReferred
                            = statements[idx].mstatementType.getReferredModules(progContext);
                        listAllReferredModules.addAll(listThisStatementReferred);
                        if (cai != null) {
                            cai.addAssetCopyCmd(statements[idx].mstatementType);
                        }
                        boolean bEndThisPiece = false;
                        if (idx > nPieceStartLn) {
                            // in some situation, like elseif, piece start statement can also be piece end
                            // statement. However, a statement cannot be both piece start and piece end of
                            // the same piece.
                            for (String strPieceEnd : csAnalyzer.mstrarrayPieceEnds) {
                                if (statements[idx].mstatementType.mstrType.equals(strPieceEnd)) {
                                    bEndThisPiece = true;
                                    break;
                                }
                            }
                        }
                        if (bEndThisPiece) { // until statement may include some functions, so has to check referred modules for piece end statement
                            break;
                        }
                        
                        // here we assume that a block starter cannot be end of a piece.
                        boolean bStartInsideBlock = false;
                        InFuncCSRefAnalyzer insideCSAnalyzer = null;
                        // ok, this could be the start of a new inside block.
                        for (InFuncCSRefAnalyzer aCSAnalyzer : InFuncCSRefAnalyzer.msarrayCSRefAnalyzers) {
                            if (statements[idx].mstatementType.mstrType.equals(aCSAnalyzer.mstrBlockStart)) {
                                // it is the start of a new inside block.
                                bStartInsideBlock = true;
                                insideCSAnalyzer = aCSAnalyzer;
                                break;
                            }
                        }
                        if (bStartInsideBlock) {
                            idx = getAllReferredModules(idx, statements, insideCSAnalyzer, listAllReferredModules, cai, progContext, inFuncCSMgr);
                        } else {
                            idx ++;
                        }
                        if (idx >= statements.length) {
                            break;    // end of statement.
                        }
                    }
                }
                inFuncCSMgr.popCSStack();
                if (statements[nPieceStartLn].mstatementType.mstrType.equals(Statement_case.getTypeStr())) {
                	idx = nPieceStartLn + 1;    // move to next line of the piece start line if this is "case" statement.
                } // if not case statement, do not adjust idx.
            } else {    // is it the end of the block, if yes, go to next line and break. otherwise, go to next line and loop.
                boolean bFinished = false;
                for(String strBlockEnd : csAnalyzer.mstrarrayBlockEnd)  {
                    if (statements[idx].mstatementType.mstrType.equals(strBlockEnd)) {
                        bFinished = true;
                        break;    // finish.
                    }
                }
                idx ++;
                if (bFinished)  {
                    break;
                }
            }
        }
        return idx;
    }
}
