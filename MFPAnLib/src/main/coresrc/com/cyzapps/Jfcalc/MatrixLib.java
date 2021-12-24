/*
 * MFP project, MatrixLib.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator.FunctionInterrupter;
import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public class MatrixLib {
    public static LinkedList<DataClass> calculateEigenValues(DataClass datum2DSqrA, DataClass datum2DSqrB, FunctionInterrupter functionInterrupter)  throws JFCALCExpErrException, InterruptedException {
        // assume that datum2DSquare is always a 2D square matrix and it has been fully populated.
        // Moreover, the parameter datum2DSqrA and datum2DSqrB will be not be modified inside the function.
        // nor the return value will refer to any part of the parameter.

        // do not deep copy input because unecessary (we do not generate another matrix).
        int[] narraySquareSize = datum2DSqrA.recalcDataArraySize();
        if (narraySquareSize.length != 2
                || narraySquareSize[0] != narraySquareSize[1]
                || narraySquareSize[0] == 0)    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
        }
        // assume Matrix has been fully populated. datum2DSqrA must be a 2D data reference type.
        //datum2DSquare.PopulateDataArray(narraySquareSize, true);
        
        LinkedList<Long> listReversedPairCnts = new LinkedList<>();
        int[][] narrayPrLists = MathLib.getPermutations(narraySquareSize[0], listReversedPairCnts, functionInterrupter);
        DataClass[] datumArrayAddSub = new DataClass[1];
        datumArrayAddSub[0] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
        for (int idx = 0; idx < narrayPrLists.length; idx ++)    {
            DataClass[] datumArrayMultip = new DataClass[1];
            datumArrayMultip[0] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
            for (int index0 = 0; index0 < narrayPrLists[idx].length; index0 ++) {
                int index1 = narrayPrLists[idx][index0];
                DataClass datumArrayThis[] = new DataClass[2];
                datumArrayThis[0] = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSqrA).getDataList()[index0]).getDataList()[index1];
                datumArrayThis[1] = BuiltinProcedures.evaluateNegSign(DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSqrB).getDataList()[index0]).getDataList()[index1]);
                datumArrayMultip = MathLib.multiplyPolynomial(datumArrayMultip, datumArrayThis, functionInterrupter);
            }
            datumArrayAddSub = MathLib.addSubPolynomial(datumArrayAddSub, datumArrayMultip, (listReversedPairCnts.get(idx) % 2 == 0), functionInterrupter);
        }
        
        // calculate roots of polynomial to get the eigen values.
        LinkedList<DataClass> listParams = new LinkedList<DataClass>();
        for (int idx = 0; idx < datumArrayAddSub.length; idx ++)    {
            listParams.addFirst(datumArrayAddSub[idx]);
        }
        return MathLib.solvePolynomial(listParams, functionInterrupter);        
    }

    public static LinkedList<LinkedList<DataClass>> calculateZeroVectors(DataClass datum2DSquare)   throws JFCALCExpErrException   {
        // this function calculate vectors which satisfy input A * one_of_vectors = 0 and v is a not all zero vector
        // if A is an invertible matrix, return value includes a single linkedlist<DataClass>. Otherwise, returned
        // value includes multiple linkedlist<DataClass>.
        // note that function will not change input datum2DSquare.
        
        // do not deep copy input because unecessary (we do not generate another matrix).
        int[] narraySquareSize = datum2DSquare.recalcDataArraySize();
        if (narraySquareSize.length != 2
                || narraySquareSize[0] != narraySquareSize[1]
                || narraySquareSize[0] <= 1)    {   // if narraySquareSize[0] == 1, sub 2d matrix will be empty which cannot be handled.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
        }
        // assume Matrix has been fully populated
        //datum2DSquare.PopulateDataArray(narraySquareSize, true);
        
        // step 1, find out the child matrix whose abs(det) is smallest. If it's zero, then look for the next one.
        DataClass datumZero = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
        
        DataClass[] datumArrayAddSub = new DataClass[1];
        datumArrayAddSub[0] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);

        DataClass[] datumVector = new DataClass[narraySquareSize[0]];
        int nMinAbsDetIdx = -1;
        DataClass datumMinAbsDet = null;
        for (int index = 0; index < narraySquareSize[0]; index ++)    {
            // now reconstruct a sub matrix and calculate abs(det(sub matrix))
            DataClass[] datumList = new DataClass[narraySquareSize[0] - 1];
            for (int idx0 = 0; idx0 < datumList.length; idx0++) {
                DataClass[] datumListChildren = new DataClass[narraySquareSize[1] - 1];
                for (int idx1 = 0; idx1 < datumListChildren.length; idx1 ++)    {
                    if (idx0 < index && idx1 < index)   {
                        datumListChildren[idx1] = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx0]).getDataList()[idx1];
                    } else if (idx0 >= index && idx1 < index)   {
                        datumListChildren[idx1] = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx0 + 1]).getDataList()[idx1];
                    } else if (idx0 < index && idx1 >= index)   {
                        datumListChildren[idx1] = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx0]).getDataList()[idx1 + 1];
                    } else {
                        datumListChildren[idx1] = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx0 + 1]).getDataList()[idx1 + 1];
                    }
                }
                datumList[idx0] = new DataClassArray(datumListChildren);
            }
            DataClassArray datumSub2DMatrix = new DataClassArray(datumList);
            DataClassComplex datumDet = DCHelper.lightCvtOrRetDCComplex(BuiltinProcedures.evaluateDeterminant(datumSub2DMatrix));
            MFPNumeric[] mfpNumRadAngle = datumDet.getComplexRadAngle();
            DataClassSingleNum datumAbsDet = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumRadAngle[0]);
            if (datumAbsDet.isEqual(datumZero)) {
                continue;   // abs(det) of child matrix is 0, we go to next one.
            } else if (datumMinAbsDet == null || MFPNumeric.compareTo(datumAbsDet.getDataValue(), DCHelper.lightCvtOrRetDCSingleNum(datumMinAbsDet).getDataValue()) < 0) {
                nMinAbsDetIdx = index;
                datumMinAbsDet = datumAbsDet;
            }
        }
        
        if (nMinAbsDetIdx == -1)    {
            // all the Abs dets are zeros which means matrix not invertible.
            LinkedList<LinkedList<DataClass>> listReturn = new LinkedList<LinkedList<DataClass>>();
            for (int index = 0; index < narraySquareSize[0]; index ++)    {
                // now reconstruct a sub matrix and calculate abs(det(sub matrix))
                DataClass[] datumList = new DataClass[narraySquareSize[0] - 1];
                for (int idx0 = 0; idx0 < datumList.length; idx0++) {
                    datumList[idx0] = new DataClassNull();
                    DataClass[] datumListChildren = new DataClass[narraySquareSize[1] - 1];
                    for (int idx1 = 0; idx1 < datumListChildren.length; idx1 ++)    {
                        if (idx0 < index && idx1 < index)   {
                            datumListChildren[idx1] = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx0]).getDataList()[idx1];
                        } else if (idx0 >= index && idx1 < index)   {
                            datumListChildren[idx1] = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx0 + 1]).getDataList()[idx1];
                        } else if (idx0 < index && idx1 >= index)   {
                            datumListChildren[idx1] = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx0]).getDataList()[idx1 + 1];
                        } else {
                            datumListChildren[idx1] = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx0 + 1]).getDataList()[idx1 + 1];
                        }
                    }
                    datumList[idx0] = new DataClassArray(datumListChildren);
                }
                LinkedList<LinkedList<DataClass>> listSubVectors = new LinkedList<LinkedList<DataClass>>();
                if (datumList.length == 1) {
                    // not a matrix but a single element so calculateZeroVectors cannot be called
                    LinkedList<DataClass> listSubVector = new LinkedList<DataClass>();
                    DataClass datumOneElem = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                    listSubVector.add(datumOneElem);
                    listSubVectors.add(listSubVector);
                } else {
                    DataClass datumSub2DMatrix = new DataClassArray(datumList);
                    listSubVectors = calculateZeroVectors(datumSub2DMatrix);
                    if (listSubVectors.size() == 0) {
                        continue;   // no vector solved
                    }
                }
                
                DataClass datumZeroElem = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                listSubVectors.get(0).add(index, datumZero); // insert zero at index
                listReturn.add(listSubVectors.get(0));
            }
            
            return listReturn;
        } else  {
            // now reconstruct a sub child matrix and calculate the sub zero vector
            DataClass[] datumList = new DataClass[narraySquareSize[0] - 1];
            for (int idx0 = 0; idx0 < narraySquareSize[0]; idx0 ++)   {
                int idxNew0 = idx0;
                if (idx0 == nMinAbsDetIdx)  {
                    continue;
                } else if (idx0 > nMinAbsDetIdx)    {
                    idxNew0 = idx0 - 1;
                }
                DataClass[] datumListOrigChildren = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx0]).getDataList();
                DataClass[] datumListChildren = new DataClass[narraySquareSize[1] - 1];
                for (int idx1 = 0; idx1 < narraySquareSize[1]; idx1 ++) {
                    int idxNew1 = idx1;
                    if (idx1 == nMinAbsDetIdx)  {
                        continue;
                    } else if (idx1 > nMinAbsDetIdx)    {
                        idxNew1 = idx1 - 1;
                    }
                    datumListChildren[idxNew1] = datumListOrigChildren[idx1];
                }
                datumList[idxNew0] = new DataClassArray(datumListChildren);
            }
            DataClassArray datumSubMatrix = new DataClassArray(datumList);
            // now sub matrix is ready, calculate its inverted matrix
            DataClass datumSubMatrixInv = BuiltinProcedures.evaluateReciprocal(datumSubMatrix);
            datumList = new DataClass[narraySquareSize[0] - 1];
            for (int idx = 0; idx < narraySquareSize[0]; idx ++)    {
                int idxNew = idx;
                if (idx == nMinAbsDetIdx) {
                    continue;
                } else if (idxNew > nMinAbsDetIdx)  {
                    idxNew --;
                }
                DataClass[] datumListChildren = new DataClass[1];
                datumListChildren[0] = BuiltinProcedures.evaluateNegSign(DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx]).getDataList()[nMinAbsDetIdx]);
                datumList[idxNew] = new DataClassArray(datumListChildren);
            }
            DataClassArray datum2MultiplyVector = new DataClassArray(datumList);
            DataClass datumSubZeroVector = BuiltinProcedures.evaluateMultiplication(datumSubMatrixInv, datum2MultiplyVector);
            // datumSubZeroVector must be a 2D matrix.
            LinkedList<DataClass> listSingleVector = new LinkedList<DataClass>();
            for (int idx = 0; idx < narraySquareSize[0]; idx ++)    {
                if (idx < nMinAbsDetIdx)    {
                    listSingleVector.add(DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datumSubZeroVector).getDataList()[idx]).getDataList()[0]);
                } else if (idx == nMinAbsDetIdx)    {
                    DataClass datumOne = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                    listSingleVector.add(datumOne);
                } else {    // idx > nMinAbsDetIdx
                    listSingleVector.add(DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datumSubZeroVector).getDataList()[idx - 1]).getDataList()[0]);
                }
            }
            LinkedList<LinkedList<DataClass>> listReturn = new LinkedList<LinkedList<DataClass>>();
            listReturn.add(listSingleVector);
            return listReturn;
        }
    }
    
    public static LinkedList<DataClass> calculateZeroVector(DataClass datum2DSquare)   throws JFCALCExpErrException   {
        // this function calculate a vector v which satisfy input A * v = 0 and v is not all zero vector.
        // if A is an invertible matrix, return value includes a not all zero linkedlist<DataClass>. Otherwise, returned
        // value is an all 0 vector.
        // note that function will not change input datum2DSquare.
        
        // do not deep copy input because unecessary (we do not generate another matrix).
        int[] narraySquareSize = datum2DSquare.recalcDataArraySize();
        if (narraySquareSize.length != 2
                || narraySquareSize[0] != narraySquareSize[1]
                || narraySquareSize[0] <= 1)    {   // if narraySquareSize[0] == 1, sub 2d matrix will be empty which cannot be handled.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
        }
        // assume Matrix has been fully populated
        //datum2DSquare.PopulateDataArray(narraySquareSize, true);
        
        // step 1, find out the child matrix whose abs(det) is smallest. If it's zero, then look for the next one.
        DataClass datumZero = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
        
        DataClass[] datumArrayAddSub = new DataClass[1];
        datumArrayAddSub[0] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);

        DataClass[] datumVector = new DataClass[narraySquareSize[0]];
        int nMinAbsDetIdx = -1;
        DataClass datumMinAbsDet = null;
        for (int index = 0; index < narraySquareSize[0]; index ++)    {
            // now reconstruct a sub matrix and calculate abs(det(sub matrix))
            DataClass[] datumList = new DataClass[narraySquareSize[0] - 1];
            for (int idx0 = 0; idx0 < datumList.length; idx0++) {
                DataClass[] datumListChildren = new DataClass[narraySquareSize[1] - 1];
                for (int idx1 = 0; idx1 < datumListChildren.length; idx1 ++)    {
                    if (idx0 < index && idx1 < index)   {
                        datumListChildren[idx1] = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx0]).getDataList()[idx1];
                    } else if (idx0 >= index && idx1 < index)   {
                        datumListChildren[idx1] = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx0 + 1]).getDataList()[idx1];
                    } else if (idx0 < index && idx1 >= index)   {
                        datumListChildren[idx1] = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx0]).getDataList()[idx1 + 1];
                    } else {
                        datumListChildren[idx1] = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx0 + 1]).getDataList()[idx1 + 1];
                    }
                }
                datumList[idx0] = new DataClassArray(datumListChildren);
            }
            DataClassArray datumSub2DMatrix = new DataClassArray(datumList);
            DataClassComplex datumDet = DCHelper.lightCvtOrRetDCComplex(BuiltinProcedures.evaluateDeterminant(datumSub2DMatrix));
            MFPNumeric[] mfpNumRadAngle = datumDet.getComplexRadAngle();
            DataClassSingleNum datumAbsDet = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumRadAngle[0]);
            if (datumAbsDet.isEqual(datumZero)) {
                continue;   // abs(det) of child matrix is 0, we go to next one.
            } else if (datumMinAbsDet == null || MFPNumeric.compareTo(datumAbsDet.getDataValue(), DCHelper.lightCvtOrRetDCSingleNum(datumMinAbsDet).getDataValue()) < 0) {
                nMinAbsDetIdx = index;
                datumMinAbsDet = datumAbsDet;
            }
        }
        
        if (nMinAbsDetIdx == -1)    {
            // all the Abs dets are zeros
            LinkedList<DataClass> listReturn = new LinkedList<DataClass>();
            for (int idx = 0; idx < narraySquareSize[0]; idx ++)    {
                DataClass datumZeroElem = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                listReturn.add(datumZeroElem);
            }
            return listReturn;
        } else  {
            // now reconstruct a sub child matrix and calculate the sub zero vector
            DataClass[] datumList = new DataClass[narraySquareSize[0] - 1];
            for (int idx0 = 0; idx0 < narraySquareSize[0]; idx0 ++)   {
                int idxNew0 = idx0;
                if (idx0 == nMinAbsDetIdx)  {
                    continue;
                } else if (idx0 > nMinAbsDetIdx)    {
                    idxNew0 = idx0 - 1;
                }
                DataClass[] datumListOrigChildren = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx0]).getDataList();
                DataClass[] datumListChildren = new DataClass[narraySquareSize[1] - 1];
                for (int idx1 = 0; idx1 < narraySquareSize[1]; idx1 ++) {
                    int idxNew1 = idx1;
                    if (idx1 == nMinAbsDetIdx)  {
                        continue;
                    } else if (idx1 > nMinAbsDetIdx)    {
                        idxNew1 = idx1 - 1;
                    }
                    datumListChildren[idxNew1] = datumListOrigChildren[idx1];
                }
                datumList[idxNew0] = new DataClassArray(datumListChildren);
            }
            DataClassArray datumSubMatrix = new DataClassArray(datumList);
            // now sub matrix is ready, calculate its inverted matrix
            DataClass datumSubMatrixInv = BuiltinProcedures.evaluateReciprocal(datumSubMatrix);
            datumList = new DataClass[narraySquareSize[0] - 1];
            for (int idx = 0; idx < narraySquareSize[0]; idx ++)    {
                int idxNew = idx;
                if (idx == nMinAbsDetIdx) {
                    continue;
                } else if (idxNew > nMinAbsDetIdx)  {
                    idxNew --;
                }
                DataClass[] datumListChildren = new DataClass[1];
                datumListChildren[0] = BuiltinProcedures.evaluateNegSign(DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datum2DSquare).getDataList()[idx]).getDataList()[nMinAbsDetIdx]);
                datumList[idxNew] = new DataClassArray(datumListChildren);
            }
            DataClassArray datum2MultiplyVector = new DataClassArray(datumList);
            DataClassArray datumSubZeroVector = DCHelper.lightCvtOrRetDCArray(BuiltinProcedures.evaluateMultiplication(datumSubMatrixInv, datum2MultiplyVector));
            LinkedList<DataClass> listReturn = new LinkedList<DataClass>();
            for (int idx = 0; idx < narraySquareSize[0]; idx ++)    {
                if (idx < nMinAbsDetIdx)    {
                    listReturn.add(DCHelper.lightCvtOrRetDCArray(datumSubZeroVector.getDataList()[idx]).getDataList()[0]);
                } else if (idx == nMinAbsDetIdx)    {
                    DataClass datumOne = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                    listReturn.add(datumOne);
                } else {    // idx > nMinAbsDetIdx
                    listReturn.add(DCHelper.lightCvtOrRetDCArray(datumSubZeroVector.getDataList()[idx - 1]).getDataList()[0]);
                }
            }
            return listReturn;
        }
    }
    
    // this function will not change the value of datumInput and will assume this array (datumOperand) has been fully populated.
    public static DataClass calculateUpperTriangularMatrix(DataClass datumInput)   throws JFCALCExpErrException   {
        int[] narrayLen = new int[0];
        if (!(datumInput.getThisOrNull() instanceof DataClassArray)) {
            // it is not a matrix, return a copy of itself.
            return datumInput.cloneSelf();
        }
        
        narrayLen = datumInput.recalcDataArraySize();
        if (narrayLen.length != 2 || narrayLen[0] == 0 || narrayLen[1] == 0) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
        }
                
        // now it is a 2D matrix.
        DataClassArray datumReturn = DCHelper.lightCvtOrRetDCArray(datumInput.cloneSelf());
        int nShorterSize = Math.min(narrayLen[0], narrayLen[1]);
        for (int idx1 = 0; idx1 < nShorterSize; idx1 ++) {
            MFPNumeric mfpMaxAbs = MFPNumeric.ZERO;
            int nMaxAbsIdx = 0;
            for (int idx0 = idx1; idx0 < narrayLen[0]; idx0 ++) {
                int[] narrayIdx = new int[2];
                narrayIdx[0] = idx0;
                narrayIdx[1] = idx1;
                DataClass datumThisElem = datumReturn.getDataAtIndexByRef(narrayIdx); // deep copy here
                // datumThisElem and datumAbs must be both DataClassBuiltIn
                DataClass datumAbs = BuiltinProcedures.evaluateAbs(datumThisElem);
                MFPNumeric mfpAbs = DCHelper.lightCvtOrRetDCSingleNum(datumAbs).getDataValue();
                if (mfpAbs.compareTo(mfpMaxAbs) > 0) {
                    mfpMaxAbs = mfpAbs;
                    nMaxAbsIdx = idx0;
                }
            }
            if (!mfpMaxAbs.isActuallyZero()) {
                for (int idx11 = idx1; idx11 < narrayLen[0]; idx11 ++) {
                    if (idx11 != nMaxAbsIdx) {
                        int[] narrayIdx = new int[2];
                        narrayIdx[0] = nMaxAbsIdx;
                        narrayIdx[1] = idx1;
                        DataClass datumMaxAbs = datumReturn.getDataAtIndexByRef(narrayIdx);
                        narrayIdx[0] = idx11;
                        DataClass datum2Divided = datumReturn.getDataAtIndexByRef(narrayIdx);
                        DataClass datumCoeff = BuiltinProcedures.evaluateDivision(datum2Divided, datumMaxAbs);
                        DataClass datumMinus = BuiltinProcedures.evaluateSubstraction(datumReturn.getDataList()[idx11],
                                                BuiltinProcedures.evaluateMultiplication(datumCoeff, datumReturn.getDataList()[nMaxAbsIdx]));
                        for (int idxTmp = 0; idxTmp <= idx1; idxTmp++) {
                            narrayIdx = new int[1];
                            narrayIdx[0] = idxTmp;
                            // datumMinus must be a DataClassBuiltIn type so conversion is alright.
                            DataClassArray datumMinusArray = DCHelper.lightCvtOrRetDCArray(datumMinus);
                            datumMinusArray.setDataAtIndexByRef(narrayIdx, new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO));
                            datumMinus = datumMinusArray;
                        }
                        narrayIdx = new int[1];
                        narrayIdx[0] = idx11;
                        datumReturn.setDataAtIndexByRef(narrayIdx, datumMinus);
                    }
                }
                if (nMaxAbsIdx != idx1) {
                    // swap nMaxAbsIdx and idx1. We need not to worry about invalid index here.
                    int[] narrayIdx = new int[1];
                    narrayIdx[0] = nMaxAbsIdx;
                    DataClass datumMaxAbsIdx = datumReturn.getDataAtIndexByRef(narrayIdx);
                    narrayIdx[0] = idx1;
                    DataClass datumIdx1 = datumReturn.getDataAtIndexByRef(narrayIdx);
                    datumReturn.setDataAtIndexByRef(narrayIdx, datumMaxAbsIdx);
                    narrayIdx[0] = nMaxAbsIdx;
                    datumReturn.setDataAtIndexByRef(narrayIdx, datumIdx1);
                }
            }
        }
        return datumReturn;
    }
    
    public static DataClass calculateMatrixRank(DataClass datumInput)   throws JFCALCExpErrException   {
        int[] narrayLen = new int[0];
        if (!(datumInput.getThisOrNull() instanceof DataClassArray)) {
            // it is not a matrix, return a copy of itself.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
        }
        
        narrayLen = datumInput.recalcDataArraySize();
        if (narrayLen.length != 2) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
        }
        
        if (narrayLen[0] == 0 || narrayLen[1] == 0) {
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
        }
           
        DataClass datumUpperTriangularMatrix;    // this should be a data array
        if (narrayLen[0] < narrayLen[1]) {
            // this is necessary otherwise, rank may be lower estimated.
            datumUpperTriangularMatrix = calculateUpperTriangularMatrix(BuiltinProcedures.evaluateTransposition(datumInput));
        } else {
            datumUpperTriangularMatrix = calculateUpperTriangularMatrix(datumInput);
        }
        int nShorterSize = Math.min(narrayLen[0], narrayLen[1]);
        int nRank = nShorterSize;
        for (int idx0 = 0; idx0 < nShorterSize; idx0 ++) {
            int[] narrayIdx = new int[2];
            narrayIdx[0] = narrayIdx[1] = idx0;
            DataClass datumDiagonal = DCHelper.lightCvtOrRetDCArray(datumUpperTriangularMatrix).getDataAtIndexByRef(narrayIdx);
            MFPNumeric mfpAbs = DCHelper.lightCvtOrRetDCSingleNum(BuiltinProcedures.evaluateAbs(datumDiagonal)).getDataValue();
            if (mfpAbs.isActuallyZero()) {
                nRank --;
            }
        }
        return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nRank));
    }
}
