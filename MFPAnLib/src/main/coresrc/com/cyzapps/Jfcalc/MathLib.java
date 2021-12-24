/*
 * MFP project, MathLib.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator.FunctionInterrupter;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public class MathLib {
    public static LinkedList<DataClass> solvePolynomial(LinkedList<DataClass> listParams, FunctionInterrupter functionInterrupter) throws JFCALCExpErrException, InterruptedException    {
        if (functionInterrupter != null)    {
            // for debug or killing a background thread. Every heavy functions need to have this check.
            if (functionInterrupter.shouldInterrupt())    {
                functionInterrupter.interrupt();
            }
        }
        if (listParams.size() < 2)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        int nParamNumber = listParams.size(), nNonZeroStart = 0;
        DataClassSingleNum datumZero = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO);
        DataClassSingleNum datumHalf = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.HALF);
        DataClassSingleNum datumOne = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE);
        DataClassSingleNum datumMinus1 = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.MINUS_ONE);
        DataClassSingleNum datumTwo = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.TWO);
        DataClassSingleNum datumThree = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(3));
        DataClassSingleNum datum1Over3 = DCHelper.lightCvtOrRetDCSingleNum(BuiltinProcedures.evaluateDivision(datumOne, datumThree));
        DataClassComplex datumi = new DataClassComplex(MFPNumeric.ZERO, MFPNumeric.ONE);
        
        for (int idx = 0; idx < nParamNumber; idx ++)   {
            if (listParams.get(idx).isEqual(datumZero)) {
                nNonZeroStart = idx + 1;
            } else  {
                break;
            }
        }
        if (nNonZeroStart >= nParamNumber - 1)  {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        
        LinkedList<DataClass> listRoots = new LinkedList<DataClass>();
        switch (nParamNumber - 1 - nNonZeroStart)   {
            case 1:
            {
                // ax + b == 0
                DataClass datumParam1 = listParams.get(nParamNumber - 2);
                DataClass datumParam2 = listParams.get(nParamNumber - 1);
                DataClass datumRoot = BuiltinProcedures.evaluateDivision(datumParam2, datumParam1);
                datumRoot = BuiltinProcedures.evaluateMultiplication(datumMinus1, datumRoot);
                listRoots.add(datumRoot);
                break;
            }
            case 2:
            {
                // ax**2 + bx + c = 0
                DataClass a = new DataClassNull(), b = new DataClassNull(), c = new DataClassNull(), sqrt_b2_4ac = new DataClassNull();
                a = listParams.get(nParamNumber - 3);
                b = listParams.get(nParamNumber - 2);
                c = listParams.get(nParamNumber - 1);
                DataClass bsqr = BuiltinProcedures.evaluateMultiplication(b, b);
                DataClass fourac = BuiltinProcedures.evaluateMultiplication(a, c);
                fourac = BuiltinProcedures.evaluateMultiplication(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(4)), fourac);
                if (c.isEqual(datumZero))   {
                    listRoots.add(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO));
                    DataClass datumRoot2 = BuiltinProcedures.evaluateDivision(b, a);
                    datumRoot2 = BuiltinProcedures.evaluateMultiplication(datumMinus1, datumRoot2);
                    listRoots.add(datumRoot2);
                } else if (BuiltinProcedures.evaluateSubstraction(bsqr, fourac).isEqual(datumZero)) {   // b**2-4ac == 0
                    DataClass datumRoot = BuiltinProcedures.evaluateDivision(b, a);
                    datumRoot = BuiltinProcedures.evaluateMultiplication(datumMinus1, datumRoot);
                    datumRoot = BuiltinProcedures.evaluateDivision(datumRoot, datumTwo);
                    listRoots.add(datumRoot);
                    listRoots.add(datumRoot.cloneSelf());
                } else  {
                    sqrt_b2_4ac = BuiltinProcedures.evaluatePower(
                            BuiltinProcedures.evaluateSubstraction(bsqr, fourac), datumHalf,
                            null);
                    DataClass minusb = BuiltinProcedures.evaluateMultiplication(datumMinus1, b);
                    DataClass twoa = BuiltinProcedures.evaluateMultiplication(datumTwo, a);
                    DataClass datumRoot1 = BuiltinProcedures.evaluateAdding(minusb, sqrt_b2_4ac);
                    DataClass datumRoot2 = BuiltinProcedures.evaluateSubstraction(minusb, sqrt_b2_4ac);
                    listRoots.add(BuiltinProcedures.evaluateDivision(datumRoot1, twoa));
                    listRoots.add(BuiltinProcedures.evaluateDivision(datumRoot2, twoa));
                }
                break;
            }
            case 3:
            {
                // ax**3 + bx**2 + cx + d = 0. Note that the parameters a, b, c, d should be able to convert to complex.
                DataClassComplex a = DCHelper.lightCvtOrRetDCComplex(listParams.get(nParamNumber - 4));
                DataClassComplex b = DCHelper.lightCvtOrRetDCComplex(listParams.get(nParamNumber - 3));
                DataClassComplex c = DCHelper.lightCvtOrRetDCComplex(listParams.get(nParamNumber - 2));
                DataClassComplex d = DCHelper.lightCvtOrRetDCComplex(listParams.get(nParamNumber - 1));
                DataClass delta = new DataClassNull(), big_A = new DataClassNull(), big_B = new DataClassNull(), big_C = new DataClassNull();
                if (d.isEqual(datumZero))   {
                    // now we get one root which is 0
                    DataClass x_value = datumZero.cloneSelf();
                    listRoots.addFirst(x_value);
                    LinkedList<DataClass> listNewParams = new LinkedList<DataClass>();
                    listNewParams.add(a);
                    listNewParams.add(b);
                    listNewParams.add(c);
                    LinkedList<DataClass> listNewRoots = solvePolynomial(listNewParams, functionInterrupter);
                    for (int idx = 0; idx < listNewRoots.size(); idx ++)    {
                        listRoots.add(listNewRoots.get(idx));
                    }
                } else  {
                    DataClass bsqr = BuiltinProcedures.evaluateMultiplication(b, b);
                    DataClass threeac = BuiltinProcedures.evaluateMultiplication(a, c);
                    threeac = BuiltinProcedures.evaluateMultiplication(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(3)), threeac);
                    big_A = BuiltinProcedures.evaluateSubstraction(bsqr, threeac);
                    DataClass bc = BuiltinProcedures.evaluateMultiplication(b, c);
                    DataClass ninead = BuiltinProcedures.evaluateMultiplication(a, d);
                    ninead = BuiltinProcedures.evaluateMultiplication(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(9)), ninead);
                    big_B = BuiltinProcedures.evaluateSubstraction(bc, ninead);
                    DataClass csqr = BuiltinProcedures.evaluateMultiplication(c, c);
                    DataClass threebd = BuiltinProcedures.evaluateMultiplication(b, d);
                    threebd = BuiltinProcedures.evaluateMultiplication(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(3)), threebd);
                    big_C = BuiltinProcedures.evaluateSubstraction(csqr, threebd);
                    DataClass big_Bsqr = BuiltinProcedures.evaluateMultiplication(big_B, big_B);
                    DataClass fourbig_Abig_C = BuiltinProcedures.evaluateMultiplication(big_A, big_C);
                    fourbig_Abig_C = BuiltinProcedures.evaluateMultiplication(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(4)), fourbig_Abig_C);
                    delta = BuiltinProcedures.evaluateSubstraction(big_Bsqr, fourbig_Abig_C);
                    if (big_A.isEqual(big_B) && big_A.isEqual(datumZero))   {   // big_B == big_A == 0
                        // three same roots
                        DataClass minusbover3overa = BuiltinProcedures.evaluateDivision(b, new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(3)));
                        minusbover3overa = BuiltinProcedures.evaluateDivision(minusbover3overa, a);
                        minusbover3overa = BuiltinProcedures.evaluateMultiplication(datumMinus1, minusbover3overa);
                        listRoots.add(minusbover3overa);
                        DataClass root2 = minusbover3overa.cloneSelf();
                        DataClass root3 = minusbover3overa.cloneSelf();
                        listRoots.add(root2);
                        listRoots.add(root3);               
                    } else if (delta.isEqual(datumZero))    {
                        // two same roots
                        DataClass minusbovera = BuiltinProcedures.evaluateDivision(b, a);
                        minusbovera = BuiltinProcedures.evaluateMultiplication(datumMinus1, minusbovera);
                        DataClass big_Boverbig_A = BuiltinProcedures.evaluateDivision(big_B, big_A);
                        DataClass root1 = BuiltinProcedures.evaluateAdding(minusbovera, big_Boverbig_A);
                        DataClass root2 = BuiltinProcedures.evaluateMultiplication(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.HALF.negate()),
                                                            big_Boverbig_A);
                        DataClass root3 = BuiltinProcedures.evaluateMultiplication(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.HALF.negate()),
                                                            big_Boverbig_A);
                        listRoots.add(root1);
                        listRoots.add(root2);
                        listRoots.add(root3);
                    } else if (a.getImage().isActuallyZero() && b.getImage().isActuallyZero()
                            && c.getImage().isActuallyZero() && d.getImage().isActuallyZero()) {
                        //all parameters are real value
                        MFPNumeric mfpSqrt3 = MFPNumeric.sqrt(new MFPNumeric(3));
                        DataClass big_Ab = BuiltinProcedures.evaluateMultiplication(big_A, b);
                        DataClass threeaover2 = BuiltinProcedures.evaluateMultiplication(datumThree, a);
                        threeaover2 = BuiltinProcedures.evaluateDivision(threeaover2, datumTwo);
                        DataClass sqrtdelta = BuiltinProcedures.evaluatePower(delta, datumHalf, null);
                        DataClass Y1 = BuiltinProcedures.evaluateSubstraction(sqrtdelta, big_B);
                        Y1 = BuiltinProcedures.evaluateMultiplication(threeaover2, Y1);
                        Y1 = BuiltinProcedures.evaluateAdding(big_Ab, Y1);
                        if (DCHelper.lightCvtOrRetDCComplex(Y1).getImage().isActuallyZero()) {
                            MFPNumeric mfpY1 = (Y1 instanceof DataClassComplex)?
                                    DCHelper.lightCvtOrRetDCComplex(Y1).getReal():DCHelper.lightCvtOrRetDCSingleNum(Y1).getDataValue();
                            if (mfpY1.isActuallyNegative()) {
                                MFPNumeric mfpY1Real = MFPNumeric.pow(mfpY1.negate(), datum1Over3.getDataValue()).divide(MFPNumeric.TWO);
                                MFPNumeric mfpY1Img = mfpY1Real.multiply(mfpSqrt3);
                                Y1 = new DataClassComplex(mfpY1Real, mfpY1Img);
                            } else {
                                mfpY1 = MFPNumeric.pow(mfpY1, datum1Over3.getDataValue());
                                Y1 = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpY1);  // ensure that we alsways get real Y1.
                            }
                        } else {
                            DataClassArray datumY1s = DCHelper.lightCvtOrRetDCArray(BuiltinProcedures.evaluatePower(Y1, datum1Over3, datumThree));
                            if (datumY1s.getDataListSize() != 3) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_REFERENCE); 
                            }
                            MFPNumeric mfpLargestReal = DCHelper.lightCvtOrRetDCComplex(datumY1s.getDataList()[0]).getReal();
                            int nLargestRealIdx = 0;
                            for (int idx = 1; idx < 3; idx ++) {
                                if (DCHelper.lightCvtOrRetDCComplex(datumY1s.getDataList()[idx]).getReal().compareTo(mfpLargestReal) > 0) {
                                    mfpLargestReal = DCHelper.lightCvtOrRetDCComplex(datumY1s.getDataList()[idx]).getReal();
                                    nLargestRealIdx = idx;
                                }
                            }
                            Y1 = datumY1s.getDataList()[nLargestRealIdx];   // return Y1 with largest real.
                        }
                        DataClass Y2 = BuiltinProcedures.evaluateAdding(sqrtdelta, big_B);
                        Y2 = BuiltinProcedures.evaluateMultiplication(threeaover2, Y2);
                        Y2 = BuiltinProcedures.evaluateSubstraction(big_Ab, Y2);
                        if (DCHelper.lightCvtOrRetDCComplex(Y1).getImage().isActuallyZero()) {   // note: here use Y1 not Y2 to identify if we should return real value in **1/3
                            MFPNumeric mfpY2 = (Y2 instanceof DataClassComplex)?
                                    DCHelper.lightCvtOrRetDCComplex(Y2).getReal():DCHelper.lightCvtOrRetDCSingleNum(Y2).getDataValue();
                            if (mfpY2.isActuallyNegative()) {
                                mfpY2 = MFPNumeric.pow(mfpY2.negate(), datum1Over3.getDataValue()).negate();
                            } else {
                                mfpY2 = MFPNumeric.pow(mfpY2, datum1Over3.getDataValue());
                            }
                            Y2 = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpY2);  // ensure that we alsways get real Y1.
                        } else if (DCHelper.lightCvtOrRetDCComplex(Y2).getImage().isActuallyZero()) {    // Y1 ** 1/3 does not return real
                            MFPNumeric mfpY2 = (Y2 instanceof DataClassComplex)?
                                    DCHelper.lightCvtOrRetDCComplex(Y2).getReal():DCHelper.lightCvtOrRetDCSingleNum(Y2).getDataValue();
                            if (mfpY2.isActuallyNegative()) {   // if Y2 is negative same as Y1, select the conjugate direction.
                                MFPNumeric mfpY2Real = MFPNumeric.pow(mfpY2.negate(), datum1Over3.getDataValue()).divide(MFPNumeric.TWO);
                                MFPNumeric mfpY2Img = mfpY2Real.multiply(mfpSqrt3).negate();
                                Y2 = new DataClassComplex(mfpY2Real, mfpY2Img);
                            } else {   // if Y2 is positive different from Y1, select the real-conjugate direction.
                                MFPNumeric mfpY2Real = MFPNumeric.pow(mfpY2, datum1Over3.getDataValue()).divide(MFPNumeric.TWO).negate();
                                MFPNumeric mfpY2Img = mfpY2Real.multiply(mfpSqrt3).negate();
                                Y2 = new DataClassComplex(mfpY2Real, mfpY2Img);
                            }
                        } else {
                            Y2 = new DataClassComplex(DCHelper.lightCvtOrRetDCComplex(Y1).getReal(),
                                    DCHelper.lightCvtOrRetDCComplex(Y1).getImage().negate());    // Y1 and Y2 must be conjugate.
                        }
                        DataClass tmpDatum = BuiltinProcedures.evaluatePower(datumThree, datumHalf, null);
                        tmpDatum = BuiltinProcedures.evaluateMultiplication(tmpDatum, datumHalf);
                        DataClass sqrt3Halfi = new DataClassComplex(MFPNumeric.ZERO, DCHelper.lightCvtOrRetDCSingleNum(tmpDatum).getDataValue());
                        DataClass Threea = BuiltinProcedures.evaluateMultiplication(datumThree, a);
                        DataClass Y1plusY2 = BuiltinProcedures.evaluateAdding(Y1, Y2);
                        DataClass Y1minusY2 = BuiltinProcedures.evaluateSubstraction(Y1, Y2);
                        DataClass X1 = BuiltinProcedures.evaluateAdding(b, Y1plusY2);
                        X1 = BuiltinProcedures.evaluateMultiplication(datumMinus1, X1);
                        X1 = BuiltinProcedures.evaluateDivision(X1, Threea);
                        DataClass MinusbplusHalfY1Y2 = BuiltinProcedures.evaluateMultiplication(datumHalf, Y1plusY2);
                        MinusbplusHalfY1Y2 = BuiltinProcedures.evaluateSubstraction(MinusbplusHalfY1Y2, b);
                        tmpDatum = BuiltinProcedures.evaluateMultiplication(sqrt3Halfi, Y1minusY2);
                        DataClass X2 = BuiltinProcedures.evaluateAdding(MinusbplusHalfY1Y2, tmpDatum);
                        X2 = BuiltinProcedures.evaluateDivision(X2, Threea);
                        DataClass X3 = BuiltinProcedures.evaluateSubstraction(MinusbplusHalfY1Y2, tmpDatum);
                        X3 = BuiltinProcedures.evaluateDivision(X3, Threea);
                        listRoots.add(X1);
                        listRoots.add(X2);
                        listRoots.add(X3);
                    } else  {
                        DataClass big_Ab = BuiltinProcedures.evaluateMultiplication(big_A, b);
                        DataClass threeaover2 = BuiltinProcedures.evaluateMultiplication(datumThree, a);
                        threeaover2 = BuiltinProcedures.evaluateDivision(threeaover2, datumTwo);
                        DataClass sqrtdelta = BuiltinProcedures.evaluatePower(delta, datumHalf, null);
                        DataClass Y1 = BuiltinProcedures.evaluateSubstraction(sqrtdelta, big_B);
                        Y1 = BuiltinProcedures.evaluateMultiplication(threeaover2, Y1);
                        Y1 = BuiltinProcedures.evaluateAdding(big_Ab, Y1);
                        if (DCHelper.lightCvtOrRetDCComplex(Y1).getImage().isActuallyZero()) {
                            MFPNumeric mfpY1 = (Y1.getThisOrNull() instanceof DataClassComplex)?
                                    DCHelper.lightCvtOrRetDCComplex(Y1).getReal():DCHelper.lightCvtOrRetDCSingleNum(Y1).getDataValue();
                            if (mfpY1.isActuallyNegative()) {
                                mfpY1 = MFPNumeric.pow(mfpY1.negate(), datum1Over3.getDataValue()).negate();
                            } else {
                                mfpY1 = MFPNumeric.pow(mfpY1, datum1Over3.getDataValue());
                            }
                            Y1 = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpY1);  // ensure that we alsways get real Y1.
                        } else {
                            Y1 = BuiltinProcedures.evaluatePower(Y1, datum1Over3, null);
                        }
                        DataClass Y2 = BuiltinProcedures.evaluateAdding(sqrtdelta, big_B);
                        Y2 = BuiltinProcedures.evaluateMultiplication(threeaover2, Y2);
                        Y2 = BuiltinProcedures.evaluateSubstraction(big_Ab, Y2);
                        if (DCHelper.lightCvtOrRetDCComplex(Y2).getImage().isActuallyZero()) {
                            MFPNumeric mfpY2 = (Y2.getThisOrNull() instanceof DataClassComplex)?
                                    DCHelper.lightCvtOrRetDCComplex(Y2).getReal():DCHelper.lightCvtOrRetDCSingleNum(Y2).getDataValue();
                            if (mfpY2.isActuallyNegative()) {
                                mfpY2 = MFPNumeric.pow(mfpY2.negate(), datum1Over3.getDataValue()).negate();
                            } else {
                                mfpY2 = MFPNumeric.pow(mfpY2, datum1Over3.getDataValue());
                            }
                            Y2 = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpY2);  // ensure that we alsways get real Y1.
                        } else {
                            Y2 = BuiltinProcedures.evaluatePower(Y2, datum1Over3, null);
                        }
                        
                        DataClass selected_X1 = new DataClassNull(), selected_X2 = new DataClassNull(), selected_X3 = new DataClassNull();
                        MFPNumeric mfpNumMinabs_YX = MFPNumeric.MINUS_ONE;
                        DataClass tmpDatum = BuiltinProcedures.evaluatePower(datumThree, datumHalf, null);
                        tmpDatum = BuiltinProcedures.evaluateMultiplication(tmpDatum, datumHalf);
                        DataClass sqrt3Halfi = new DataClassComplex(MFPNumeric.ZERO, DCHelper.lightCvtOrRetDCSingleNum(tmpDatum).getDataValue());
                        // if do not consider the 9 rotating cases, we may not get right roots.
                        for (int idxY1 = 0; idxY1 <= 2; idxY1 ++)    {
                            for (int idxY2 = 0; idxY2 <= 2; idxY2 ++)   {
                                DataClass Y1_rotated = new DataClassNull(), Y2_rotated = new DataClassNull(),
                                        YX1 = new DataClassNull(), YX2 = new DataClassNull(), YX3 = new DataClassNull();
                                if (idxY1 == 0) {
                                    Y1_rotated = Y1.cloneSelf();
                                } else if (idxY1 == 1)  {
                                    Y1_rotated = BuiltinProcedures.evaluateSubstraction(sqrt3Halfi, datumHalf);
                                    Y1_rotated = BuiltinProcedures.evaluateMultiplication(Y1, Y1_rotated);
                                } else  {
                                    Y1_rotated = BuiltinProcedures.evaluateAdding(sqrt3Halfi, datumHalf);
                                    Y1_rotated = BuiltinProcedures.evaluateMultiplication(Y1, Y1_rotated);
                                    Y1_rotated = BuiltinProcedures.evaluateMultiplication(datumMinus1, Y1_rotated);
                                }
                                if (idxY2 == 0) {
                                    Y2_rotated = Y2.cloneSelf();
                                } else if (idxY2 == 1)  {
                                    Y2_rotated = BuiltinProcedures.evaluateSubstraction(sqrt3Halfi, datumHalf);
                                    Y2_rotated = BuiltinProcedures.evaluateMultiplication(Y2, Y2_rotated);
                                } else  {
                                    Y2_rotated = BuiltinProcedures.evaluateAdding(sqrt3Halfi, datumHalf);
                                    Y2_rotated = BuiltinProcedures.evaluateMultiplication(Y2, Y2_rotated);
                                    Y2_rotated = BuiltinProcedures.evaluateMultiplication(datumMinus1, Y2_rotated);
                                }
                                DataClass Threea = BuiltinProcedures.evaluateMultiplication(datumThree, a);
                                DataClass Y1_rplusY2_r = BuiltinProcedures.evaluateAdding(Y1_rotated, Y2_rotated);
                                DataClass Y1_rminusY2_r = BuiltinProcedures.evaluateSubstraction(Y1_rotated, Y2_rotated);
                                DataClass X1 = BuiltinProcedures.evaluateAdding(b, Y1_rplusY2_r);
                                X1 = BuiltinProcedures.evaluateMultiplication(datumMinus1, X1);
                                X1 = BuiltinProcedures.evaluateDivision(X1, Threea);
                                DataClass MinusbplusHalfY1rY2r = BuiltinProcedures.evaluateMultiplication(datumHalf, Y1_rplusY2_r);
                                MinusbplusHalfY1rY2r = BuiltinProcedures.evaluateSubstraction(MinusbplusHalfY1rY2r, b);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(sqrt3Halfi, Y1_rminusY2_r);
                                DataClass X2 = BuiltinProcedures.evaluateAdding(MinusbplusHalfY1rY2r, tmpDatum);
                                X2 = BuiltinProcedures.evaluateDivision(X2, Threea);
                                DataClass X3 = BuiltinProcedures.evaluateSubstraction(MinusbplusHalfY1rY2r, tmpDatum);
                                X3 = BuiltinProcedures.evaluateDivision(X3, Threea);

                                tmpDatum = BuiltinProcedures.evaluatePower(X1, datumThree, null);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(a, tmpDatum);
                                YX1 = tmpDatum.cloneSelf();
                                tmpDatum = BuiltinProcedures.evaluatePower(X1, datumTwo, null);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(b, tmpDatum);
                                YX1 = BuiltinProcedures.evaluateAdding(YX1, tmpDatum);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(c, X1);
                                YX1 = BuiltinProcedures.evaluateAdding(YX1, tmpDatum);
                                YX1 = BuiltinProcedures.evaluateAdding(YX1, d);
                                tmpDatum = BuiltinProcedures.evaluatePower(X2, datumThree, null);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(a, tmpDatum);
                                YX2 = tmpDatum.cloneSelf();
                                tmpDatum = BuiltinProcedures.evaluatePower(X2, datumTwo, null);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(b, tmpDatum);
                                YX2 = BuiltinProcedures.evaluateAdding(YX2, tmpDatum);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(c, X2);
                                YX2 = BuiltinProcedures.evaluateAdding(YX2, tmpDatum);
                                YX2 = BuiltinProcedures.evaluateAdding(YX2, d);
                                tmpDatum = BuiltinProcedures.evaluatePower(X3, datumThree, null);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(a, tmpDatum);
                                YX3 = tmpDatum.cloneSelf();
                                tmpDatum = BuiltinProcedures.evaluatePower(X3, datumTwo, null);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(b, tmpDatum);
                                YX3 = BuiltinProcedures.evaluateAdding(YX3, tmpDatum);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(c, X3);
                                YX3 = BuiltinProcedures.evaluateAdding(YX3, tmpDatum);
                                YX3 = BuiltinProcedures.evaluateAdding(YX3, d);
                                // YX1, YX2 and YX3 must be DataClassBuiltIn
                                MFPNumeric mfpNumabs_YX = DCHelper.lightCvtOrRetDCComplex(YX1).getComplexRadAngle()[0]
                                        .add(DCHelper.lightCvtOrRetDCComplex(YX2).getComplexRadAngle()[0])
                                        .add(DCHelper.lightCvtOrRetDCComplex(YX3).getComplexRadAngle()[0]);
                                if (mfpNumMinabs_YX.isActuallyNegative()
                                        || mfpNumabs_YX.compareTo(mfpNumMinabs_YX) < 0) {
                                    mfpNumMinabs_YX = mfpNumabs_YX;
                                    selected_X1 = X1;
                                    selected_X2 = X2;
                                    selected_X3 = X3;
                                }
                            }
                        }
                        listRoots.add(selected_X1);
                        listRoots.add(selected_X2);
                        listRoots.add(selected_X3);
                    }
                }
                break;
            }
            default:
            {
                DataClass x_real = new DataClassNull(), x_image = new DataClassNull(), y_value = new DataClassNull(), stop_threshold = new DataClassNull();
                int num_tried_pnts, max_starting_pnts_try, max_iteration_steps;
                boolean root_found = false;
                if (listParams.getLast().isEqual(datumZero))    {
                    x_real = datumZero.cloneSelf();
                    x_image = datumZero.cloneSelf();
                } else  {
                    max_starting_pnts_try = 8;
                    max_iteration_steps = 100;
                    stop_threshold = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.00000001, true));
                    root_found = false;
                    DataClass root_avg = BuiltinProcedures.evaluateDivision(listParams.get(nNonZeroStart + 1), listParams.get(nNonZeroStart));
                    root_avg = BuiltinProcedures.evaluateDivision(root_avg, new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(nParamNumber - 1)));
                    DataClass root_abs_avg = BuiltinProcedures.evaluateDivision(listParams.get(nParamNumber - 1), listParams.get(nNonZeroStart));
                    if ((nParamNumber % 2 == 0) && (nParamNumber > 2)   // root_abs_avg ** 1/odd value.
                            && (DCHelper.isSingleDouble(root_abs_avg) || DCHelper.isSingleInteger(root_abs_avg) || DCHelper.isSingleBoolean(root_abs_avg)
                                || (root_abs_avg.getThisOrNull() instanceof DataClassComplex && DCHelper.lightCvtOrRetDCComplex(root_abs_avg).getImage().isActuallyZero()))) {
                        MFPNumeric mfproot_abs_avg = (root_abs_avg.getThisOrNull() instanceof DataClassComplex)?
                                                    DCHelper.lightCvtOrRetDCComplex(root_abs_avg).getReal()
                                                    :DCHelper.lightCvtOrRetDCSingleNum(root_abs_avg).getDataValue();
                        if (mfproot_abs_avg.isActuallyNegative()) {
                            mfproot_abs_avg = MFPNumeric.pow(mfproot_abs_avg.negate(), new MFPNumeric(1.0/(nParamNumber - 1.0), true)).negate();
                        } else {
                            mfproot_abs_avg = MFPNumeric.pow(mfproot_abs_avg, new MFPNumeric(1.0/(nParamNumber - 1.0), true));
                        }
                        root_abs_avg = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfproot_abs_avg);  // ensure that we alsways get real Y1.
                    } else {
                        root_abs_avg = BuiltinProcedures.evaluatePower(root_abs_avg,
                            new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(1.0/(nParamNumber - 1.0), true)),
                            null);
                    }
                    MFPNumeric mfpNumRootAbsAvg = DCHelper.lightCvtOrRetDCComplex(root_abs_avg).getComplexRadAngle()[0],
                            mfpNumRootAvg = DCHelper.lightCvtOrRetDCComplex(root_avg).getComplexRadAngle()[0];
                    if (mfpNumRootAbsAvg.compareTo(mfpNumRootAvg) > 0)  {
                        root_abs_avg = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumRootAbsAvg);
                    } else  {
                        root_abs_avg = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumRootAvg);
                    }
                    x_real = DCHelper.lightCvtOrRetDCComplex(root_avg).getRealDataClass();
                    x_image = DCHelper.lightCvtOrRetDCComplex(root_avg).getImageDataClass();
                    num_tried_pnts = 0;
                    do {
                        // try to use Newton Raphson method to find roots
                        for (int idx = 0; idx <= max_iteration_steps; idx ++)   {
                            y_value = datumZero.cloneSelf();
                            DataClass x_real_image = new DataClassNull();
                            x_real_image = x_image.cloneSelf();
                            x_real_image = BuiltinProcedures.evaluateMultiplication(datumi, x_real_image);
                            x_real_image = BuiltinProcedures.evaluateAdding(x_real, x_real_image);
                            for (int idx1 = nNonZeroStart; idx1 < nParamNumber; idx1 ++)    {
                                DataClass tmpDatum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(nParamNumber - 1 - idx1));
                                tmpDatum = BuiltinProcedures.evaluatePower(x_real_image, tmpDatum, null);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(listParams.get(idx1), tmpDatum);
                                y_value = BuiltinProcedures.evaluateAdding(y_value, tmpDatum);
                            }
                            if (DCHelper.lightCvtOrRetDCComplex(y_value).getComplexRadAngle()[0]
                                    .compareTo(DCHelper.lightCvtOrRetDCSingleNum(stop_threshold).getDataValue()) < 0)   {   // find root
                                root_found = true;
                                break;
                            } else  {
                                DataClass y_dash_value = datumZero.cloneSelf();
                                x_real_image = x_image.cloneSelf();
                                x_real_image = BuiltinProcedures.evaluateMultiplication(datumi, x_real_image);
                                x_real_image = BuiltinProcedures.evaluateAdding(x_real, x_real_image);
                                for (int idx1 = nNonZeroStart; idx1 < nParamNumber - 1; idx1 ++)    {
                                    DataClass tmpDatum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(nParamNumber - 2 - idx1));
                                    tmpDatum = BuiltinProcedures.evaluatePower(x_real_image, tmpDatum, null);
                                    tmpDatum = BuiltinProcedures
                                                .evaluateMultiplication(
                                                                new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(nParamNumber - 1 - idx1)),
                                                                tmpDatum);
                                    tmpDatum = BuiltinProcedures.evaluateMultiplication(listParams.get(idx1), tmpDatum);
                                    y_dash_value = BuiltinProcedures.evaluateAdding(y_dash_value, tmpDatum);
                                }
                                if (y_dash_value.isEqual(datumZero) == false)  {
                                    DataClass tmpDatum = new DataClassNull();
                                    tmpDatum = BuiltinProcedures.evaluateDivision(y_value, y_dash_value);
                                    if (idx % 2 == 0)   {
                                        x_real = BuiltinProcedures.evaluateSubstraction(x_real, tmpDatum);
                                    } else  {
                                        x_image = BuiltinProcedures.evaluateAdding(x_image, tmpDatum);
                                    }
                                } else  {
                                    break;  // have to change to another starting point
                                }
                                DataClass new_x = x_image.cloneSelf();
                                new_x = BuiltinProcedures.evaluateMultiplication(datumi, new_x);
                                new_x = BuiltinProcedures.evaluateAdding(x_real, new_x);
                                x_real = DCHelper.lightCvtOrRetDCComplex(new_x).getRealDataClass();
                                x_image = DCHelper.lightCvtOrRetDCComplex(new_x).getImageDataClass();
                            }
                        }
                        if (root_found) {
                            break;
                        }
                        DataClass datumRand1 = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(Math.random(), true)),
                                datumRand2 = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(Math.random(), true));
                        DataClass radius_4_new_x = BuiltinProcedures.evaluateMultiplication(datumRand1, root_abs_avg);
                        DataClass degree_4_new_x = BuiltinProcedures.evaluateMultiplication(datumRand2,
                                                                        new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.PI));
                        degree_4_new_x = BuiltinProcedures.evaluateMultiplication(datumTwo, degree_4_new_x);
                        DataClass tmpDatum = BuiltinProcedures.evaluateMultiplication(radius_4_new_x,
                                                    BuiltinProcedures.evaluateCos(degree_4_new_x));
                        x_real = BuiltinProcedures.evaluateAdding(DCHelper.lightCvtOrRetDCComplex(root_avg).getRealDataClass(), tmpDatum);
                        x_image = BuiltinProcedures.evaluateAdding(DCHelper.lightCvtOrRetDCComplex(root_avg).getImageDataClass(), tmpDatum);
                        num_tried_pnts = num_tried_pnts + 1;
                    } while (num_tried_pnts < max_starting_pnts_try);
                    if (!root_found)    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                    }
                }
                // now we get one root which is x_value
                DataClass x_value = new DataClassNull(), last_param = new DataClassNull();
                LinkedList<DataClass> listNewParams = new LinkedList<DataClass>();
                x_value = x_image.cloneSelf();
                x_value = BuiltinProcedures.evaluateMultiplication(datumi, x_value);
                x_value = BuiltinProcedures.evaluateAdding(x_real, x_value);
                last_param = datumZero.cloneSelf();
                for (int idx = nNonZeroStart; idx < nParamNumber - 1; idx ++)   {
                    DataClass datumNewParam = BuiltinProcedures.evaluateMultiplication(x_value, last_param);
                    datumNewParam = BuiltinProcedures.evaluateAdding(listParams.get(idx), datumNewParam);
                    listNewParams.add(datumNewParam);
                    last_param = listNewParams.get(idx - nNonZeroStart);
                }
                listRoots.addFirst(x_value);
                LinkedList<DataClass> listNewRoots = solvePolynomial(listNewParams, functionInterrupter);
                for (int idx = 0; idx < listNewRoots.size(); idx ++)    {
                    listRoots.add(listNewRoots.get(idx));
                }
                break;
            }
        }
        return listRoots;
    }
 
    public static int[][] getPermutations(int n, LinkedList<Long> listReversedPairCnts, FunctionInterrupter functionInterrupter) throws JFCALCExpErrException, InterruptedException   {
        if (functionInterrupter != null)    {
            // for debug or killing a background thread. Every heavy functions need to have this check.
            if (functionInterrupter.shouldInterrupt())    {
                functionInterrupter.interrupt();
            }
        }
        if (n == 1)  {
            listReversedPairCnts.clear();
            listReversedPairCnts.add(0l);
            int[][] narrayReturn = new int[1][1];
            narrayReturn[0][0] = 0;
            return narrayReturn;
        } else if (n > 12 || n <= 0)    {
            //13! > 2**32, overflow
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        } else {
            int[][] narrayLastReturn = getPermutations(n - 1, listReversedPairCnts, functionInterrupter);
            int[][] narrayReturn = new int[narrayLastReturn.length * n][n];
            LinkedList<Long> listReversedPairCntsNew = new LinkedList<Long>();
            int idxNew0 = 0;
            for (int idx0 = 0; idx0 < narrayLastReturn.length; idx0 ++) {
                for (int idx = 0; idx < n; idx ++)  {
                    int[] narrayThisList = new int[n];
                    long nThisReversedCnt = listReversedPairCnts.get(idx0);
                    // need not to worry that narrayLastReturn[idx0][idx1] > n if idx1 < idx because n should be the largest one
                    if (idx > 0)    {
                        System.arraycopy(narrayLastReturn[idx0], 0, narrayThisList, 0, idx); // copy the first idx elements
                    }
                    narrayThisList[idx] = n - 1;
                    if (idx < n - 1)    {
                        nThisReversedCnt += n - 1 - idx;
                        System.arraycopy(narrayLastReturn[idx0], idx, narrayThisList, idx + 1, n - 1 - idx); // copy the last n - 1 - idx elements
                    }
                    idxNew0 = idx0 * n + idx;
                    narrayReturn[idxNew0] = narrayThisList;
                    listReversedPairCntsNew.add(nThisReversedCnt);
                }
            }
            listReversedPairCnts.clear();
            listReversedPairCnts.addAll(listReversedPairCntsNew);
            return narrayReturn;
        }
    }
    
    public static DataClass[] multiplyPolynomial(DataClass[] datumParams1, DataClass[] datumParams2, FunctionInterrupter functionInterrupter)  throws JFCALCExpErrException, InterruptedException   {
        if (functionInterrupter != null)    {
            // for debug or killing a background thread. Every heavy functions need to have this check.
            if (functionInterrupter.shouldInterrupt())    {
                functionInterrupter.interrupt();
            }
        }
        // this function will not change the contents in datumParams1 and datumParams2.
        if (datumParams1.length == 0 || datumParams2.length == 0) {
            return new DataClass[0];
        }
        DataClass datumParamsRet[] = new DataClass[datumParams1.length + datumParams2.length - 1];
        for (int idx = 0; idx < datumParamsRet.length; idx ++) {
            datumParamsRet[idx] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
        }
        for (int idx1 = 0; idx1 < datumParams1.length; idx1 ++)   {
            for (int idx2 = 0; idx2 < datumParams2.length; idx2 ++)   {
                datumParamsRet[idx1 + idx2] = BuiltinProcedures.evaluateAdding(datumParamsRet[idx1 + idx2],
                                                    BuiltinProcedures.evaluateMultiplication(datumParams1[idx1], datumParams2[idx2]));
            }
        }
        // remove initial zeros
        int idx = datumParamsRet.length - 1;
        for (; idx >= 1; idx --)    {
            if (!DCHelper.isZeros(datumParamsRet[idx], false))  {
                break;
            }
        }
        DataClass datumParamsRetShrinked[] = new DataClass[idx + 1];
        for (idx = 0; idx < datumParamsRetShrinked.length; idx ++) {
            datumParamsRetShrinked[idx] = datumParamsRet[idx];
        }
        return datumParamsRetShrinked;
    }
 
    public static DataClass[] addSubPolynomial(DataClass[] datumParams1, DataClass[] datumParams2, boolean bIsAdd, FunctionInterrupter functionInterrupter)  throws JFCALCExpErrException, InterruptedException   {
        if (functionInterrupter != null)    {
            // for debug or killing a background thread. Every heavy functions need to have this check.
            if (functionInterrupter.shouldInterrupt())    {
                functionInterrupter.interrupt();
            }
        }
        // this function will not change the contents in datumParams1 and datumParams2.
        DataClass datumParamsRet[] = new DataClass[Math.max(datumParams1.length, datumParams2.length)];
        for (int idx = 0; idx < datumParamsRet.length; idx ++)   {
            DataClass datumParam1, datumParam2;
            if (idx >= datumParams1.length) {
                datumParam1 = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
            } else {
                datumParam1 = datumParams1[idx];
            }
            if (idx >= datumParams2.length) {
                datumParam2 = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
            } else {
                datumParam2 = datumParams2[idx];
            }
            if (bIsAdd) {
                datumParamsRet[idx] = BuiltinProcedures.evaluateAdding(datumParam1, datumParam2);
            } else  {
                datumParamsRet[idx] = BuiltinProcedures.evaluateSubstraction(datumParam1, datumParam2);
            }
        }
        // remove initial zeros
        int idx = datumParamsRet.length - 1;
        for (; idx >= 1; idx --)    {
            if (!DCHelper.isZeros(datumParamsRet[idx], false))  {
                break;
            }
        }
        DataClass datumParamsRetShrinked[] = new DataClass[idx + 1];
        for (idx = 0; idx < datumParamsRetShrinked.length; idx ++) {
            datumParamsRetShrinked[idx] = datumParamsRet[idx];
        }
        return datumParamsRetShrinked;
    }
    
    // initialize some constants matrices for integration.
    static final DataClass msdarrayAbsCissa[] = initGaussKronrodMatrices(0);
    static final DataClass msdarrayWeights7[] = initGaussKronrodMatrices(1);
    static final DataClass msdarrayWeights15[] = initGaussKronrodMatrices(2);

    // strVariable must have been lowercased and trimmed.
    public static DataClass[] integByGaussKronrod(AbstractExpr aeStrExpr/*String strIntegExpr*/, String strVariable, DataClass datumValueFrom, DataClass datumValueTo,
            int nMaxSteps, boolean bCheckConverge, boolean bExceptNotEnoughSteps, boolean bCheckFinalResult,
            ProgContext progContext, FunctionInterrupter functionInterrupter) throws JFCALCExpErrException, InterruptedException {

        // first of all, need to validate parameters.
        // from and to must be complex.
        DataClassComplex datumFrom = DCHelper.lightCvtOrRetDCComplex(datumValueFrom);
        DataClassComplex datumTo = DCHelper.lightCvtOrRetDCComplex(datumValueTo);
        
        MFPNumeric mfpFromReal = datumFrom.getReal();
        MFPNumeric mfpFromImage = datumFrom.getImage();
        MFPNumeric mfpToReal = datumTo.getReal();
        MFPNumeric mfpToImage = datumTo.getImage();
        
        if (mfpFromReal.isNan() || mfpFromImage.isNan() || mfpToReal.isNan() || mfpToImage.isNan()) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
        } else if ((mfpFromReal.isInf() && !mfpFromImage.isActuallyZero()) || (mfpToReal.isInf() && !mfpToImage.isActuallyZero())
                || (!mfpFromReal.isActuallyZero() && mfpFromImage.isInf()) || (!mfpToReal.isActuallyZero() && mfpToImage.isInf())) {
            // only support integrate from -inf to inf, any real value to inf, -inf to any real value or
            // -inf * i to inf * i, any image value to inf * i and -inf * i to any image value.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
        } else if ((mfpFromReal.isInf() && !mfpToImage.isActuallyZero()) || (mfpFromImage.isInf() && !mfpToReal.isActuallyZero())
                || (mfpToReal.isInf() && !mfpFromImage.isActuallyZero()) || (mfpToImage.isInf() && !mfpFromReal.isActuallyZero())) {
            // if one is inf, the other 's real (if inf image) or image (if inf real) must be 0.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
        } else if (mfpFromReal.isEqual(mfpToReal) && mfpFromImage.isEqual(mfpToImage)) {
            // from to are equal, the integrated result is always zero.
            return new DataClass[] {
                new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO),
                new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO)
            };
        }
        
        // now integrate:
        boolean bNeed2MultiplyMinus1 = false;
        // initialize some constants
        DataClass datumTwo = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TWO);
        double abstol = 1e-10;
        double reltol = 1e-5;
        DataClass[] darraySubs = new DataClass[11];
        int nDefaultMaxCalcIntervals = 256;  // matlab use 650 as default nMaxSteps. I select 256.
        int nNumOfErrIncreases = 0;
        int nMaxNumOfErrIncreases = 6;
        if (nMaxSteps <= 0) {
            nMaxSteps = nDefaultMaxCalcIntervals;
        } else if (nMaxSteps > nDefaultMaxCalcIntervals) {
            nMaxSteps = nDefaultMaxCalcIntervals; 
        }
        boolean bNotConvergeExit = false, bNotEnoughStepsExit = false;
        
        DataClass datumH = new DataClassNull();
        //String strCvtVarName = "internal_var0"; // cannot simply use strVariable + "1" because it may be another variable's name.
        //String strTransFunc = "";
        //String strCvtIntegExpr = strIntegExpr;
        int nIntegMode = -4; // 1 means -inf to inf, -1 means -infi to infi, 2 means -inf to something, -2 means -infi to something,
                            // 3 means something to inf, -3 means something to -infi, 4 means real to real, -4 means complex to complex.

        if (!mfpFromImage.isActuallyZero() || !mfpToImage.isActuallyZero()) { // from or to is a complex value.
            if ((mfpFromImage.isInf() || mfpToImage.isInf()) && mfpFromImage.compareTo(mfpToImage) > 0) {
                // Swap from and to if to and from are simple image value and to image < from image.
                // Note that only support simple image to from if one of them is (negative) inf i.
                bNeed2MultiplyMinus1 = true;
                MFPNumeric mfpSwapTmp = mfpFromImage;
                mfpFromImage = mfpToImage;
                mfpToImage = mfpSwapTmp;
                DataClassComplex datumSwapTmp = datumFrom;
                datumFrom = datumTo;
                datumTo = datumSwapTmp;
            }
            if (mfpFromImage.isInf() && mfpToImage.isInf()) {    // both from and to are inf
                initGaussKronrodSubRanges(-1.0, 1.0, darraySubs);
                datumH = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TWO);
                //strTransFunc = "i*" + strCvtVarName + "/(1-" + strCvtVarName + "**2)";
                //strCvtIntegExpr = "i*(" + strIntegExpr + ")*(1+" + strCvtVarName + "**2)/((1-" + strCvtVarName + "**2)**2)";
                nIntegMode = -1;
            } else if (mfpFromImage.isInf()) {   // only from is inf
                initGaussKronrodSubRanges(-1.0, 0.0, darraySubs);
                datumH = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                //strTransFunc = datumTo.output() + "-i*(" + strCvtVarName + "/(1+" + strCvtVarName + "))**2";
                //strCvtIntegExpr = "-2i*" + strCvtVarName + "*(" + strIntegExpr + ")/(1+" + strCvtVarName + ")**3"; 
                nIntegMode = -2;
            } else if (mfpToImage.isInf()) { // only to is inf
                initGaussKronrodSubRanges(0.0, 1.0, darraySubs);
                datumH = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                //strTransFunc = datumFrom.output() + "+i*(" + strCvtVarName + "/(1-" + strCvtVarName + "))**2";
                //strCvtIntegExpr = "2i*" + strCvtVarName + "*(" + strIntegExpr + ")/(1-" + strCvtVarName + ")**3"; 
                nIntegMode = -3;
            } else {    // from and to are not inf
                darraySubs = new DataClass[2];
                darraySubs[0] = datumFrom;
                darraySubs[1] = datumTo;
                datumH = BuiltinProcedures.evaluateSubstraction(datumFrom, datumTo);
                datumH = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, DCHelper.lightCvtOrRetDCComplex(datumH).getComplexRadAngle()[0]); // datumH has to be a real value.
                //strTransFunc = strCvtVarName;
                //strCvtIntegExpr = strIntegExpr;
                nIntegMode = -4;
            }
        } else {    // integrate from and to are both real values
            if (mfpFromReal.compareTo(mfpToReal) > 0) {
                // swap from and to.
                bNeed2MultiplyMinus1 = true;
                MFPNumeric mfpSwapTmp = mfpFromReal;
                mfpFromReal = mfpToReal;
                mfpToReal = mfpSwapTmp;
                DataClassComplex datumSwapTmp = datumFrom;
                datumFrom = datumTo;
                datumTo = datumSwapTmp;
            }
            if (mfpFromReal.isInf()&& mfpToReal.isInf()) {    // both from and to are inf
                initGaussKronrodSubRanges(-1.0, 1.0, darraySubs);
                datumH = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TWO);
                //strTransFunc = strCvtVarName + "/(1-" + strCvtVarName + "**2)";
                //strCvtIntegExpr = "(" + strIntegExpr + ")*(1+" + strCvtVarName + "**2)/((1-" + strCvtVarName + "**2)**2)";
                nIntegMode = 1;
            } else if (mfpFromReal.isInf()) {   // only from is inf
                initGaussKronrodSubRanges(-1.0, 0.0, darraySubs);
                datumH = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                //strTransFunc = datumTo.output() + "-(" + strCvtVarName + "/(1+" + strCvtVarName + "))**2";
                //strCvtIntegExpr = "-2*" + strCvtVarName + "*(" + strIntegExpr + ")/(1+" + strCvtVarName + ")**3"; 
                nIntegMode = 2;
            } else if (mfpToReal.isInf()) { // only to is inf
                initGaussKronrodSubRanges(0.0, 1.0, darraySubs);
                datumH = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                //strTransFunc = datumFrom.output() + "+(" + strCvtVarName + "/(1-" + strCvtVarName + "))**2";
                //strCvtIntegExpr = "2*" + strCvtVarName + "*(" + strIntegExpr + ")/(1-" + strCvtVarName + ")**3"; 
                nIntegMode = 3;
            } else {    // from and to are not inf
                initGaussKronrodSubRanges(-1.0, 1.0, darraySubs);
                datumH = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TWO);
                //strTransFunc = "((" + datumTo.output() + "-" + datumFrom.output() + ")/4)*" + strCvtVarName + "*(3-" + strCvtVarName + "**2)+(" + datumTo.output() + "+" + datumFrom.output() + ")/2";
                //strCvtIntegExpr = "(" + strIntegExpr + ")*3*(" + datumTo.output() + "-" + datumFrom.output() + ")/4*(1-" + strCvtVarName + "**2)";
                nIntegMode = 4;
            }
        }

        // Split interval into at least 10 sub-interval with a 15 point
        // Gauss-Kronrod rule giving a minimum of 150 function evaluations
        while (darraySubs.length < 11) {
            DataClass[] darraySubsNew = new DataClass[darraySubs.length * 2 - 1];
            darraySubsNew[0] = darraySubs[0];
            for (int idx = 1; idx < darraySubs.length; idx ++) {
                darraySubsNew[idx*2 - 1] = BuiltinProcedures.evaluateAdding(darraySubs[idx], darraySubs[idx - 1]);
                darraySubsNew[idx*2 - 1] = BuiltinProcedures.evaluateDivision(darraySubsNew[idx*2 - 1], datumTwo);
                darraySubsNew[idx*2] = darraySubs[idx]; // dont think we need to clone.
            }
            darraySubs = darraySubsNew;
        }
        DataClass[][] darraySubIntervals = new DataClass[darraySubs.length - 1][2];
        for (int idx = 1; idx < darraySubs.length; idx ++) {
            darraySubIntervals[idx - 1][0] = darraySubs[idx - 1];
            darraySubIntervals[idx - 1][1] = darraySubs[idx];
        }
    
        DataClass[][] darrayResult = integByGaussKronrodCore(aeStrExpr/*strIntegExpr*/, strVariable, datumFrom, datumTo, nIntegMode,
                                                    darraySubIntervals, msdarrayAbsCissa, msdarrayWeights15, msdarrayWeights7,
                                                    progContext, functionInterrupter);
        DataClassComplex datumQ0 = new DataClassComplex(MFPNumeric.ZERO, MFPNumeric.ZERO);
        DataClassSingleNum datumErr0 = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
        DataClassSingleNum datumErr0Last = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.INF);
        for (int idx = 0; idx < darraySubIntervals.length; idx ++) {
            datumQ0 = DCHelper.lightCvtOrRetDCComplex(BuiltinProcedures.evaluateAdding(datumQ0, darrayResult[0][idx]));
            datumErr0 = DCHelper.lightCvtOrRetDCSingleNum(BuiltinProcedures.evaluateAdding(datumErr0, darrayResult[1][idx]));
        }
        
        DataClassComplex datumQ = new DataClassComplex(MFPNumeric.ZERO, MFPNumeric.ZERO);
        DataClassSingleNum datumErr = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
        
        // now start to do the calculation.
        double dULP = Math.ulp(1.0);
        MFPNumeric mfpIntervalThresh = new MFPNumeric(dULP*100, true);
        int nTotalCalcSubIntervals = darraySubIntervals.length;
        while (true) {
            // Check for sub-intervals that are too small. Test must be
            // performed in untransformed sub-intervals. What is a good
            // value for this test. Shampine suggests 100*eps
            // Check for infinite sub-interval integration result. If there
            // is, exit.
            DataClass datumFrom2To = BuiltinProcedures.evaluateSubstraction(datumTo, datumFrom);
            boolean bFoundSmallInterval = false;
            boolean bFoundInfQSub = false;
            for (int idx = 0; idx < darraySubIntervals.length; idx ++) {
                DataClass datumItvlEnd1 = integGKTransF(darraySubIntervals[idx][1], datumFrom, datumTo, nIntegMode);
                DataClass datumItvlEnd0 = integGKTransF(darraySubIntervals[idx][0], datumFrom, datumTo, nIntegMode);
                DataClass datumRelInterval = BuiltinProcedures.evaluateSubstraction(datumItvlEnd1, datumItvlEnd0);
                datumRelInterval = BuiltinProcedures.evaluateDivision(datumRelInterval, datumFrom2To);
                if (!(mfpFromReal.isInf() || mfpToReal.isInf() || mfpFromImage.isInf() || mfpToImage.isInf())   // if to or from is inf do not check small interval
                        && DCHelper.lightCvtOrRetDCComplex(datumRelInterval).getComplexRadAngle()[0].compareTo(mfpIntervalThresh) <= 0) {
                    bFoundSmallInterval = true;
                    break;
                }
                if (DCHelper.lightCvtOrRetDCComplex(darrayResult[0][idx]).getReal().isInf() || DCHelper.lightCvtOrRetDCComplex(darrayResult[0][idx]).getImage().isInf()) {
                    bFoundInfQSub = true;
                    break;
                }
            }
            if (bFoundSmallInterval || bFoundInfQSub) {
                datumQ = datumQ0;
                datumErr = datumErr0;
                /*if ((mfpFromReal.isInf() || mfpToReal.isInf() || mfpFromImage.isInf() || mfpToImage.isInf())
                        && bFoundSmallInterval) {
                    // do not check converge if one of to and from is infinite.
                    bCheckConverge = false;
                }*/
                break;
            }
            
            // If the global error estimate is meet exit
            MFPNumeric mfpTolerance = DCHelper.lightCvtOrRetDCComplex(datumQ0).getComplexRadAngle()[0].multiply(new MFPNumeric(reltol, true));
            if (mfpTolerance.compareTo(new MFPNumeric(abstol, true)) < 0) {
                mfpTolerance  = new MFPNumeric(abstol, true);
            }
            
            if (datumErr0.getDataValue().compareTo(mfpTolerance) < 0) {
                datumQ = datumQ0;
                datumErr = datumErr0;
                break;
            }
        
            datumQ0 = datumQ;
            datumErr0Last = datumErr0;
            datumErr0 = datumErr;
            LinkedList<Integer> listRemainingSubs = new LinkedList<Integer>();
            // accept the sub-intervals that meet convergence criteria
            for (int idx = 0; idx < darraySubIntervals.length; idx ++) {
                DataClass datumTmp = BuiltinProcedures.evaluateSubstraction(darraySubIntervals[idx][1], darraySubIntervals[idx][0]);
                datumTmp = BuiltinProcedures.evaluateDivision(datumTmp, datumH);
                datumTmp = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpTolerance.multiply(DCHelper.lightCvtOrRetDCComplex(datumTmp).getComplexRadAngle()[0]));
                if (DCHelper.lightCvtOrRetDCSingleNum(darrayResult[1][idx]).getDataValue()
                        .compareTo(DCHelper.lightCvtOrRetDCSingleNum(datumTmp).getDataValue()) < 0) {
                    datumQ = DCHelper.lightCvtOrRetDCComplex(BuiltinProcedures.evaluateAdding(datumQ, darrayResult[0][idx]));
                    datumErr = DCHelper.lightCvtOrRetDCSingleNum(BuiltinProcedures.evaluateAdding(datumErr, darrayResult[1][idx]));
                } else {
                    listRemainingSubs.add(idx);
                }
                datumQ0 = DCHelper.lightCvtOrRetDCComplex(BuiltinProcedures.evaluateAdding(datumQ0, darrayResult[0][idx]));
                datumErr0 = DCHelper.lightCvtOrRetDCSingleNum(BuiltinProcedures.evaluateAdding(datumErr0, darrayResult[1][idx]));
            }

            // if no remaining sub-intervals, exit
            if (listRemainingSubs.size() == 0) {
                break;
            }
            
            // If the maximum sub-interval count is met accept remaining sub-interval and exit
            if (nTotalCalcSubIntervals > nMaxSteps) {
                datumQ = datumQ0;
                datumErr = datumErr0;
                bNotEnoughStepsExit = true;
                break;
            }
            nTotalCalcSubIntervals += listRemainingSubs.size() * 2;
            
            // If error increase more than max error increase number, exit
            if (datumErr0Last.getDataValue().compareTo(datumErr0.getDataValue()) < 0) {
                nNumOfErrIncreases ++;
                if (nNumOfErrIncreases > nMaxNumOfErrIncreases) {
                    datumQ = datumQ0;
                    datumErr = datumErr0;
                    bNotConvergeExit = true;
                    break;
                }
            }
            
            // now split the remaining sub-intervals into two
            DataClass[][] darrayNewSubIntervals = new DataClass[listRemainingSubs.size()*2][2];
            int idx1 = 0;
            for (int idx = 0; idx < darraySubIntervals.length; idx ++) {
                if (listRemainingSubs.getFirst() == idx) {
                    listRemainingSubs.removeFirst();
                    darrayNewSubIntervals[idx1][0] = darraySubIntervals[idx][0];
                    darrayNewSubIntervals[idx1][1] = BuiltinProcedures.evaluateAdding(darraySubIntervals[idx][0], darraySubIntervals[idx][1]);
                    darrayNewSubIntervals[idx1][1] = BuiltinProcedures.evaluateDivision(darrayNewSubIntervals[idx1][1], datumTwo);
                    idx1 ++;
                    darrayNewSubIntervals[idx1][0] = darrayNewSubIntervals[idx1 - 1][1];
                    darrayNewSubIntervals[idx1][1] = darraySubIntervals[idx][1];
                    idx1 ++;
                }
                if (listRemainingSubs.size() == 0)  {
                    break;
                }
            }
            darraySubIntervals = darrayNewSubIntervals;
            
            // Evaluation of the integrand on the remaining sub-intervals
            darrayResult = integByGaussKronrodCore(aeStrExpr/*strIntegExpr*/, strVariable, datumFrom, datumTo, nIntegMode,
                                                    darraySubIntervals, msdarrayAbsCissa, msdarrayWeights15, msdarrayWeights7,
                                                    progContext, functionInterrupter);
        }
        
        MFPNumeric mfpQAbs = DCHelper.lightCvtOrRetDCComplex(datumQ).getComplexRadAngle()[0];
        MFPNumeric mfpTolerance = mfpQAbs.multiply(new MFPNumeric(reltol, true));
        if (mfpTolerance.compareTo(new MFPNumeric(abstol, true)) < 0) {
            mfpTolerance  = new MFPNumeric(abstol, true);
        }

        if (bCheckConverge && bNotConvergeExit) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CALCULATION_CANNOT_CONVERGE); // not converge like 1/x from -inf to inf
        }
        if (bExceptNotEnoughSteps && bNotEnoughStepsExit) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CALCULATION_CANNOT_CONVERGE); // the calculation steps are not enough to see a converged result.
        }
        if (bCheckFinalResult && (mfpQAbs.isNan() || datumErr.getDataValue().isNan()  //we can still get Nan when cvtvar is very close to 1.
                    || datumErr.getDataValue().compareTo(mfpTolerance) > 0)) {
            // Error tolerance not met. Estimated error
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CALCULATION_CANNOT_CONVERGE);
        }
        DataClass[] darrayQAndErr = new DataClass[2];
        if (bNeed2MultiplyMinus1) {
            DataClass datumMinus1 = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.MINUS_ONE);
            darrayQAndErr[0] = BuiltinProcedures.evaluateMultiplication(datumMinus1, datumQ);
        } else {
            darrayQAndErr[0] = datumQ;
        }
        darrayQAndErr[1] = datumErr;
        return darrayQAndErr;
    }

    protected static DataClass[] initGaussKronrodMatrices(int nReturnMode) {
         /* nReturnMode is 0 for darrayAbsCissa[], 1 for darrayWeights7[], 2 for darrayWeights15[] */
        try {
        switch (nReturnMode) {
            case (1): {
                DataClass[] darrayWeights7 = new DataClass[7];
                darrayWeights7[0] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.1294849661688697, true));
                darrayWeights7[1] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.2797053914892767, true));
                darrayWeights7[2] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.3818300505051889, true));
                darrayWeights7[3] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.4179591836734694, true));
                darrayWeights7[4] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.3818300505051889, true));
                darrayWeights7[5] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.2797053914892767, true));
                darrayWeights7[6] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.1294849661688697, true));
                return darrayWeights7;
            } case (2): {
                DataClass[] darrayWeights15 = new DataClass[15];
                darrayWeights15[0] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.2293532201052922e-01, true));
                darrayWeights15[1] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.6309209262997855e-01, true));
                darrayWeights15[2] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.1047900103222502, true));
                darrayWeights15[3] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.1406532597155259, true));
                darrayWeights15[4] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.1690047266392679, true));
                darrayWeights15[5] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.1903505780647854, true));
                darrayWeights15[6] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.2044329400752989, true));
                darrayWeights15[7] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.2094821410847278, true));
                darrayWeights15[8] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.2044329400752989, true));
                darrayWeights15[9] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.1903505780647854, true));
                darrayWeights15[10] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.1690047266392679, true));
                darrayWeights15[11] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.1406532597155259, true));
                darrayWeights15[12] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.1047900103222502, true));
                darrayWeights15[13] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.6309209262997855e-01, true));
                darrayWeights15[14] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.2293532201052922e-01, true));
                return darrayWeights15;
            } default: {
                DataClass[] darrayAbsCissa = new DataClass[15];
                darrayAbsCissa[0] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(-0.9914553711208126, true));
                darrayAbsCissa[1] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(-0.9491079123427585, true));
                darrayAbsCissa[2] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(-0.8648644233597691, true));
                darrayAbsCissa[3] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(-0.7415311855993944, true));
                darrayAbsCissa[4] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(-0.5860872354676911, true));
                darrayAbsCissa[5] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(-0.4058451513773972, true));
                darrayAbsCissa[6] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(-0.2077849550078985, true));
                darrayAbsCissa[7] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
                darrayAbsCissa[8] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.2077849550078985, true));
                darrayAbsCissa[9] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.4058451513773972, true));
                darrayAbsCissa[10] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.5860872354676911, true));
                darrayAbsCissa[11] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.7415311855993944, true));
                darrayAbsCissa[12] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.8648644233597691, true));
                darrayAbsCissa[13] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.9491079123427585, true));
                darrayAbsCissa[14] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(0.9914553711208126, true));
                return darrayAbsCissa;
            }
        }
        } catch (JFCALCExpErrException e) {
            throw new Error(e); //shouldn't arrive here.
        }
    }
    
    protected static void initGaussKronrodSubRanges(double dFrom, double dTo, DataClass[] darraySubs) throws JFCALCExpErrException   {
        double dAllRange = dTo - dFrom;
        darraySubs[0] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dFrom, true));
        darraySubs[1] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dFrom + dAllRange * 0.1, true));
        darraySubs[2] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dFrom + dAllRange * 0.2, true));
        darraySubs[3] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dFrom + dAllRange * 0.3, true));
        darraySubs[4] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dFrom + dAllRange * 0.4, true));
        darraySubs[5] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dFrom + dAllRange * 0.5, true));
        darraySubs[6] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dFrom + dAllRange * 0.6, true));
        darraySubs[7] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dFrom + dAllRange * 0.7, true));
        darraySubs[8] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dFrom + dAllRange * 0.8, true));
        darraySubs[9] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dFrom + dAllRange * 0.9, true));
        darraySubs[10] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dTo, true));
    }
    
    protected static DataClass[][] integByGaussKronrodCore(AbstractExpr aeStrExpr/*String strIntegExpr*/,
                                                        String strVariable,
                                                        DataClass datumFrom,
                                                        DataClass datumTo,
                                                        int nIntegMode,
                                                        DataClass[][] darraySubIntervals,
                                                        DataClass[] darrayAbsCissa,
                                                        DataClass[] darrayWeights15,
                                                        DataClass[] darrayWeights7,
                                                        ProgContext progContext,
                                                        FunctionInterrupter functionInterrupter
                                                        ) throws JFCALCExpErrException, InterruptedException {
        DataClass[] darrayHalfWidth = new DataClass[darraySubIntervals.length];
        DataClass[] darrayCenter = new DataClass[darraySubIntervals.length];
        DataClass datumTwo = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.TWO);
        for (int idx = 0; idx < darraySubIntervals.length; idx ++) {
            if (functionInterrupter != null)    {
                // for debug or killing a background thread. Every heavy functions need to have this check.
                if (functionInterrupter.shouldInterrupt())    {
                    functionInterrupter.interrupt();
                }
            }
            darrayHalfWidth[idx] = BuiltinProcedures.evaluateSubstraction(darraySubIntervals[idx][1], darraySubIntervals[idx][0]);
            darrayHalfWidth[idx] = BuiltinProcedures.evaluateDivision(darrayHalfWidth[idx], datumTwo);
            darrayCenter[idx] = BuiltinProcedures.evaluateAdding(darraySubIntervals[idx][1], darraySubIntervals[idx][0]);
            darrayCenter[idx] = BuiltinProcedures.evaluateDivision(darrayCenter[idx], datumTwo);
        }
        
        DataClass[][] datumX = new DataClass[darrayHalfWidth.length][darrayAbsCissa.length];
        DataClass[][] datumY = new DataClass[darrayHalfWidth.length][darrayAbsCissa.length];
        Variable varVar = new Variable(strVariable);
        LinkedList<Variable> l = new LinkedList<Variable>();
        l.addFirst(varVar);
        progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(l);
        ExprEvaluator exprEvaluator = new ExprEvaluator(progContext);
        for (int idx0 = 0; idx0 < darrayHalfWidth.length; idx0 ++) {
            for (int idx1 = 0; idx1 < darrayAbsCissa.length; idx1 ++)   {
                if (functionInterrupter != null)    {
                    // for debug or killing a background thread. Every heavy functions need to have this check.
                    if (functionInterrupter.shouldInterrupt())    {
                        functionInterrupter.interrupt();
                    }
                }
                datumX[idx0][idx1] = BuiltinProcedures.evaluateMultiplication(darrayHalfWidth[idx0], darrayAbsCissa[idx1]);
                datumX[idx0][idx1] = BuiltinProcedures.evaluateAdding(datumX[idx0][idx1], darrayCenter[idx0]);
                DataClass datumTrans = integGKTransF(datumX[idx0][idx1], datumFrom, datumTo, nIntegMode);
                varVar.setValue(datumTrans);
                DataClass datumInteg = null;
                if (aeStrExpr != null) {
                    try {
                        datumInteg = aeStrExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);    // do not use lUnknown coz lVarNameSpaces has included var.
                    } catch (Exception ex) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                    }
                }
                //DataClass datumInteg = exprEvaluator.evaluateExpression(strIntegExpr, new CurPos());
                datumY[idx0][idx1] = integGKExprF(datumX[idx0][idx1], datumInteg, datumFrom, datumTo, nIntegMode);
            }
        }
        progContext.mdynamicProgContext.mlVarNameSpaces.pollFirst();
        
        DataClass[] darrayQs = new DataClass[darrayHalfWidth.length];
        for (int idx0 = 0; idx0 < darrayHalfWidth.length; idx0 ++) {
            DataClass datumQElem = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
            for (int idx1 = 0; idx1 < darrayAbsCissa.length; idx1 ++)   {
                if (functionInterrupter != null)    {
                    // for debug or killing a background thread. Every heavy functions need to have this check.
                    if (functionInterrupter.shouldInterrupt())    {
                        functionInterrupter.interrupt();
                    }
                }
                DataClass datumTmp = BuiltinProcedures.evaluateMultiplication(datumY[idx0][idx1], darrayWeights15[idx1]);
                datumQElem = BuiltinProcedures.evaluateAdding(datumQElem, datumTmp);
            }
            darrayQs[idx0] = BuiltinProcedures.evaluateMultiplication(datumQElem, darrayHalfWidth[idx0]);
        }
        DataClass[] darrayErrs = new DataClass[darrayHalfWidth.length];
        for (int idx0 = 0; idx0 < darrayHalfWidth.length; idx0 ++) {
            DataClass datumErrElem = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO);
            for (int idx1 = 0; idx1 < darrayWeights7.length; idx1 ++)   {
                if (functionInterrupter != null)    {
                    // for debug or killing a background thread. Every heavy functions need to have this check.
                    if (functionInterrupter.shouldInterrupt())    {
                        functionInterrupter.interrupt();
                    }
                }
                DataClass datumTmp = BuiltinProcedures.evaluateMultiplication(datumY[idx0][idx1*2+1], darrayWeights7[idx1]);
                datumErrElem = BuiltinProcedures.evaluateAdding(datumErrElem, datumTmp);
            }
            datumErrElem = BuiltinProcedures.evaluateMultiplication(datumErrElem, darrayHalfWidth[idx0]);
            datumErrElem = BuiltinProcedures.evaluateSubstraction(datumErrElem, darrayQs[idx0]);
            darrayErrs[idx0] = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, DCHelper.lightCvtOrRetDCComplex(datumErrElem).getComplexRadAngle()[0]);
        }
        DataClass[][] darrayReturn = new DataClass[2][];
        darrayReturn[0] = darrayQs;
        darrayReturn[1] = darrayErrs;
        return darrayReturn;
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKTransFInf2Inf(DataClass datumCvtVar, boolean bImage) throws JFCALCExpErrException {
        // if bImage is true, then i * cvtvar / (1 - cvtvar**2)
        // else cvtvar / (1 - cvtvar**2), here cvtvar must be a real value.
        MFPNumeric mfpNumCvtVar = DCHelper.lightCvtOrRetDCComplex(datumCvtVar).getReal();
        MFPNumeric mfpReturn = mfpNumCvtVar.divide(MFPNumeric.ONE.subtract(mfpNumCvtVar.multiply(mfpNumCvtVar)));
        DataClass datum = new DataClassNull();
        if (bImage) {
            datum = new DataClassComplex(MFPNumeric.ZERO, mfpReturn);
        } else {
            datum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpReturn);
        }
        return datum;
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKExprFInf2Inf(DataClass datumCvtVar, DataClass datumInteg, boolean bImage) throws JFCALCExpErrException {
        // if bImage is true, then i * integ * (1 + cvtvar ** 2)/((1 - cvtvar ** 2)**2)
        // else integ * (1 + cvtvar ** 2)/((1 - cvtvar ** 2)**2), here cvtvar must be a real value.
        MFPNumeric mfpNumCvtVar = DCHelper.lightCvtOrRetDCComplex(datumCvtVar).getReal();
        MFPNumeric mfpNumCvtVarSqr = mfpNumCvtVar.multiply(mfpNumCvtVar);
        MFPNumeric mfpNum1MinusSqr = MFPNumeric.ONE.subtract(mfpNumCvtVarSqr);
        MFPNumeric mfpNumExceptInteg = MFPNumeric.ONE.add(mfpNumCvtVarSqr).divide(mfpNum1MinusSqr.multiply(mfpNum1MinusSqr));
        
        MFPNumeric mfpNumReal = DCHelper.lightCvtOrRetDCComplex(datumInteg).getReal();
        mfpNumReal = mfpNumReal.multiply(mfpNumExceptInteg);
        MFPNumeric mfpNumImage = DCHelper.lightCvtOrRetDCComplex(datumInteg).getImage();
        mfpNumImage = mfpNumImage.multiply(mfpNumExceptInteg);
        
        DataClass datum = new DataClassNull();
        if (bImage) {
            datum = new DataClassComplex(mfpNumImage.negate(), mfpNumReal);
        } else {
            datum = new DataClassComplex(mfpNumReal, mfpNumImage);
        }
        return datum;
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKTransFFromInf(DataClass datumCvtVar, DataClass datumTo, boolean bImage) throws JFCALCExpErrException {
        // if bImage is true, then to - i*(cvtvar/(1+cvtvar))**2
        // else to - (cvtvar/(1+cvtvar))**2, here cvtvar must be a real value.
        MFPNumeric mfpNumCvtVar = DCHelper.lightCvtOrRetDCComplex(datumCvtVar).getReal();
        MFPNumeric mfpNumCvtVarOver1PlusCvtVar = mfpNumCvtVar.divide(MFPNumeric.ONE.add(mfpNumCvtVar));
        MFPNumeric mfpNumExceptI = mfpNumCvtVarOver1PlusCvtVar.multiply(mfpNumCvtVarOver1PlusCvtVar);
        
        MFPNumeric mfpNumReal = DCHelper.lightCvtOrRetDCComplex(datumTo).getReal();
        MFPNumeric mfpNumImage = DCHelper.lightCvtOrRetDCComplex(datumTo).getImage();
        
        if (bImage) {
            mfpNumImage = mfpNumImage.subtract(mfpNumExceptI);
        } else {
            mfpNumReal = mfpNumReal.subtract(mfpNumExceptI);
        }
        DataClass datum = new DataClassComplex(mfpNumReal, mfpNumImage);
        return datum;
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKExprFFromInf(DataClass datumCvtVar, DataClass datumInteg, boolean bImage) throws JFCALCExpErrException {
        // if bImage is true, then -2i * cvtvar * integ / (1 + cvtvar)**3 ----------> i * integ * (-2) * cvtvar/ (1 + cvtvar)**3
        // else -2 * cvtvar * integ / (1 + cvtvar)**3 ------------> integ * (-2) * cvtvar/ (1 + cvtvar)**3, here cvtvar must be a real value.
        MFPNumeric mfpNumCvtVar = DCHelper.lightCvtOrRetDCComplex(datumCvtVar).getReal();
        MFPNumeric mfpNum1PlusCvtVar = MFPNumeric.ONE.add(mfpNumCvtVar);
        MFPNumeric mfpNumExceptInteg = mfpNumCvtVar.divide(mfpNum1PlusCvtVar.multiply(mfpNum1PlusCvtVar).multiply(mfpNum1PlusCvtVar)).multiply(MFPNumeric.TWO.negate());
        
        MFPNumeric mfpNumReal = DCHelper.lightCvtOrRetDCComplex(datumInteg).getReal();
        mfpNumReal = mfpNumReal.multiply(mfpNumExceptInteg);
        MFPNumeric mfpNumImage = DCHelper.lightCvtOrRetDCComplex(datumInteg).getImage();
        mfpNumImage = mfpNumImage.multiply(mfpNumExceptInteg);
        
        DataClass datum = new DataClassNull();
        if (bImage) {
            datum = new DataClassComplex(mfpNumImage.negate(), mfpNumReal);
        } else {
            datum = new DataClassComplex(mfpNumReal, mfpNumImage);
        }
        return datum;
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKTransFToInf(DataClass datumCvtVar, DataClass datumFrom, boolean bImage) throws JFCALCExpErrException {
        // if bImage is true, then from + i*(cvtvar/(1-cvtvar))**2
        // else from + (cvtvar/(1-cvtvar))**2, here cvtvar must be a real value.
        MFPNumeric mfpNumCvtVar = DCHelper.lightCvtOrRetDCComplex(datumCvtVar).getReal();
        MFPNumeric mfpNumCvtVarOver1MinusCvtVar = mfpNumCvtVar.divide(MFPNumeric.ONE.subtract(mfpNumCvtVar));
        MFPNumeric mfpNumExceptI = mfpNumCvtVarOver1MinusCvtVar.multiply(mfpNumCvtVarOver1MinusCvtVar);
        
        MFPNumeric mfpNumReal = DCHelper.lightCvtOrRetDCComplex(datumFrom).getReal();
        MFPNumeric mfpNumImage = DCHelper.lightCvtOrRetDCComplex(datumFrom).getImage();
        
        if (bImage) {
            mfpNumImage = mfpNumImage.add(mfpNumExceptI);
        } else {
            mfpNumReal = mfpNumReal.add(mfpNumExceptI);
        }
        DataClassComplex datum = new DataClassComplex(mfpNumReal, mfpNumImage);
        return datum;       
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKExprFToInf(DataClass datumCvtVar, DataClass datumInteg, boolean bImage) throws JFCALCExpErrException {
        // if bImage is true, then 2i * cvtvar * integ / (1 - cvtvar)**3 ----------> i * integ * 2 * cvtvar/ (1 - cvtvar)**3
        // else 2 * cvtvar * integ / (1 - cvtvar)**3 ------------> integ * 2 * cvtvar/ (1 - cvtvar)**3, here cvtvar must be a real value.
        MFPNumeric mfpNumCvtVar = DCHelper.lightCvtOrRetDCComplex(datumCvtVar).getReal();
        MFPNumeric mfpNum1MinusCvtVar = MFPNumeric.ONE.subtract(mfpNumCvtVar);
        MFPNumeric mfpNumExceptInteg = mfpNumCvtVar.divide(mfpNum1MinusCvtVar.multiply(mfpNum1MinusCvtVar).multiply(mfpNum1MinusCvtVar)).multiply(MFPNumeric.TWO);
        
        MFPNumeric mfpNumReal = DCHelper.lightCvtOrRetDCComplex(datumInteg).getReal();
        mfpNumReal = mfpNumReal.multiply(mfpNumExceptInteg);
        MFPNumeric mfpNumImage = DCHelper.lightCvtOrRetDCComplex(datumInteg).getImage();
        mfpNumImage = mfpNumImage.multiply(mfpNumExceptInteg);
        
        DataClass datum = new DataClassNull();
        if (bImage) {
            datum = new DataClassComplex(mfpNumImage.negate(), mfpNumReal);
        } else {
            datum = new DataClassComplex(mfpNumReal, mfpNumImage);
        }
        return datum;        
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKTransFGeneral(DataClass datumCvtVar, DataClass datumFrom, DataClass datumTo, boolean bImage) throws JFCALCExpErrException {
        if (bImage) {
            return datumCvtVar.cloneSelf();
        } else {
            // transf = ((to - from)/4)*cvtvar*(3-cvtvar**2)+(to+from)/2 where cvtvar, to and from are reals
            MFPNumeric mfpNumCvtVar = DCHelper.lightCvtOrRetDCComplex(datumCvtVar).getReal();
            MFPNumeric mfpNumThree = new MFPNumeric(3);
            MFPNumeric mfpNumExceptI = mfpNumCvtVar.multiply(mfpNumThree.subtract(mfpNumCvtVar.multiply(mfpNumCvtVar)));
            
            MFPNumeric mfpNumToValue = DCHelper.lightCvtOrRetDCComplex(datumTo).getReal();
            MFPNumeric mfpNumFromValue = DCHelper.lightCvtOrRetDCComplex(datumFrom).getReal();
            
            MFPNumeric mfpNumFour = new MFPNumeric(4);
            
            MFPNumeric mfpNumValue = mfpNumToValue.subtract(mfpNumFromValue).divide(mfpNumFour).multiply(mfpNumExceptI);
            mfpNumValue = mfpNumValue.add(mfpNumToValue.add(mfpNumFromValue).divide(MFPNumeric.TWO));
            /* to and from are both real. */
            
            DataClass datum = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpNumValue);
            return datum;
        }
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKExprFGeneral(DataClass datumCvtVar, DataClass datumInteg, DataClass datumFrom, DataClass datumTo, boolean bImage) throws JFCALCExpErrException {
        if (bImage) {
            return datumInteg.cloneSelf();
        } else {
            // integ * 3*(to - from)/4*(1-cvtvar**2) where cvtvar, to and from are reals --------> integ * (to - from) * 0.75 * (1-cvtvar**2)
            MFPNumeric mfpNumCvtVar = DCHelper.lightCvtOrRetDCComplex(datumCvtVar).getReal();
            MFPNumeric mfpNum0Pnt75 = new MFPNumeric(0.75, true);
            MFPNumeric mfpNumExceptI = mfpNum0Pnt75.multiply(MFPNumeric.ONE.subtract(mfpNumCvtVar.multiply(mfpNumCvtVar)));
            
            MFPNumeric mfpNumToMinusFrom = DCHelper.lightCvtOrRetDCComplex(datumTo).getReal().subtract(DCHelper.lightCvtOrRetDCComplex(datumFrom).getReal());
            mfpNumExceptI = mfpNumExceptI.multiply(mfpNumToMinusFrom);
            MFPNumeric mfpNumReal = DCHelper.lightCvtOrRetDCComplex(datumInteg).getReal().multiply(mfpNumExceptI);
            MFPNumeric mfpNumImage = DCHelper.lightCvtOrRetDCComplex(datumInteg).getImage().multiply(mfpNumExceptI);
            
            DataClass datum = new DataClassComplex(mfpNumReal, mfpNumImage);
            return datum;
        }
    }
    
    protected static DataClass integGKTransF(DataClass datumCvtVar, DataClass datumFrom, DataClass datumTo, int nIntegMode) throws JFCALCExpErrException {
        switch(nIntegMode) {
            case 1:
                return integGKTransFInf2Inf(datumCvtVar, false);
            case -1:
                return integGKTransFInf2Inf(datumCvtVar, true);
            case 2:
                return integGKTransFFromInf(datumCvtVar, datumTo, false);
            case -2:
                return integGKTransFFromInf(datumCvtVar, datumTo, true);
            case 3:
                return integGKTransFToInf(datumCvtVar, datumFrom, false);
            case -3:
                return integGKTransFToInf(datumCvtVar, datumFrom, true);
            case 4:
                return integGKTransFGeneral(datumCvtVar, datumFrom, datumTo, false);
            default:    // -4
                return integGKTransFGeneral(datumCvtVar, datumFrom, datumTo, true);
        }
    }
    
    protected static DataClass integGKExprF(DataClass datumCvtVar, DataClass datumInteg, DataClass datumFrom, DataClass datumTo, int nIntegMode) throws JFCALCExpErrException {
        switch(nIntegMode) {
            case 1:
                return integGKExprFInf2Inf(datumCvtVar, datumInteg, false);
            case -1:
                return integGKExprFInf2Inf(datumCvtVar, datumInteg, true);
            case 2:
                return integGKExprFFromInf(datumCvtVar, datumInteg, false);
            case -2:
                return integGKExprFFromInf(datumCvtVar, datumInteg, true);
            case 3:
                return integGKExprFToInf(datumCvtVar, datumInteg, false);
            case -3:
                return integGKExprFToInf(datumCvtVar, datumInteg, true);
            case 4:
                return integGKExprFGeneral(datumCvtVar, datumInteg, datumFrom, datumTo, false);
            default:    // -4
                return integGKExprFGeneral(datumCvtVar, datumInteg, datumFrom, datumTo, true);
        }
    }    
    
    // strVariable must have been trimmed and lowercased.
    public static DataClass deriRidders(AbstractExpr aeStrExpr, String strVariable, DataClass datumValue, DataClass datumHValue, int nDeriOrder, ProgContext progContext)
            throws JFCALCExpErrException, InterruptedException   {
        DataClass datumZERO = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ZERO);
        DataClass datumFinalResult = new DataClassNull();
        if (nDeriOrder < 0 || nDeriOrder > 3) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        } else {
            final int MAX_STEPS = 16;   // maximum 16 steps.
            final MFPNumeric TOLERANCE = new MFPNumeric(1.0e-8, true);  // error tolerance.
            DataClass[][] datumarrayResults = new DataClass[MAX_STEPS][MAX_STEPS];
            DataClass[] datumarrayH = new DataClass[MAX_STEPS];
            MFPNumeric[] mfparrayErrors = new MFPNumeric[MAX_STEPS - 1];
            DataClass datumTEN = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.TEN);
            datumarrayH[0] = datumHValue;  // initial value of \delta H.
            boolean bErrorSmallEnough = false;
            for (int i = 0; i < MAX_STEPS; i ++) {
                if (i > 0) {
                    datumarrayH[i] = BuiltinProcedures.evaluateDivision(datumarrayH[i - 1], datumTEN);
                }
                datumarrayResults[i][0] = deriFormula(aeStrExpr, strVariable, datumValue, datumarrayH[i], nDeriOrder, progContext);
                for (int j = 0; j < i; j ++) {
                    // result[i][j+1] = ((x - x_i) * result[i][j] - (x - x_{i-j-1}) * result[i-1][j])/(x_{i-j-1} - x_i)
                    DataClass datumXMinusXi = BuiltinProcedures.evaluateSubstraction(datumZERO, datumarrayH[i]);
                    DataClass datumXMinusXij1 = BuiltinProcedures.evaluateSubstraction(datumZERO, datumarrayH[i-j-1]);
                    DataClass datumXij1MinusXi = BuiltinProcedures.evaluateSubstraction(datumarrayH[i-j-1], datumarrayH[i]);

                    if (DCHelper.lightCvtOrRetDCComplex(datumarrayResults[i-1][j]).getReal().isNanOrInf()
                            || DCHelper.lightCvtOrRetDCComplex(datumarrayResults[i-1][j]).getImage().isNanOrInf()) {
                        datumarrayResults[i][j + 1] = datumarrayResults[i][j];  // otherwise datumarrayResults[i][j + 1] will be nan or infinite.
                    } else {
                        datumarrayResults[i][j + 1] = BuiltinProcedures.evaluateDivision(
                                                        BuiltinProcedures.evaluateSubstraction(
                                                            BuiltinProcedures.evaluateMultiplication(datumXMinusXi, datumarrayResults[i-1][j]),
                                                            BuiltinProcedures.evaluateMultiplication(datumXMinusXij1, datumarrayResults[i][j])),
                                                        datumXij1MinusXi);
                    }
                }
                datumFinalResult = datumarrayResults[i][i];
                if (i > 0) {
                    /* do not use relative error.
                    DataClass datumRelError = BuiltinProcedures.evaluateDivision(datumAbsError, datumarrayResults[i][i]);
                    datumRelError.changeDataType(DATATYPES.DATUM_MFPDEC);
                    MFPNumeric mfpRelError = datumRelError.getDataValue().abs();*/
                    DataClass datumError = BuiltinProcedures.evaluateSubstraction(datumarrayResults[i][i], datumarrayResults[i - 1][i - 1]);
                    // datumError.changeDataType(DATATYPES.DATUM_MFPDEC); // can be complex.
                    MFPNumeric mfpAbsError = DCHelper.lightCvtOrRetDCSingleNum(BuiltinProcedures.evaluateAbs(datumError)).getDataValue();
                    if (mfpAbsError.compareTo(TOLERANCE) <= 0) {  // do 
                        // ok, we get it.
                        bErrorSmallEnough = true;
                        break;
                    }
                    mfparrayErrors[i - 1] = mfpAbsError;
                }
            }
            boolean bErroMonoIncrease = true;
            if (!bErrorSmallEnough) {
                // error is still very big. So find the min error.
                MFPNumeric mfpMinError = MFPNumeric.INF;
                datumFinalResult = datumarrayResults[0][0];
                for (int i = 0; i < MAX_STEPS - 1; i ++) {
                    if (mfpMinError.compareTo(mfparrayErrors[i]) > 0) {
                        mfpMinError = mfparrayErrors[i];
                        datumFinalResult = datumarrayResults[i+1][i+1];
                    }
                    if (i > 0 && mfparrayErrors[i-1].compareTo(mfparrayErrors[i]) > 0) {
                        bErroMonoIncrease = false;
                    }
                }
                
                if (bErroMonoIncrease) {
                    // this means it cannot converge.
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                }
            }
        }
        return datumFinalResult;
    }
    
    // strVariable must have been trimmed and lowercased.
    public static DataClass deriFormula(AbstractExpr aeStrExpr, String strVariable, DataClass datumValue, DataClass datumHValue, int nDeriOrder, ProgContext progContext)
            throws InterruptedException, JFCALCExpErrException   {
        Variable varVar = new Variable(strVariable);
        LinkedList<Variable> l = new LinkedList<Variable>();
        l.addFirst(varVar);
        progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(l);
        
        DataClass datumResult = new DataClassNull();
        if (nDeriOrder == 0) {  // 0 order derivative is itself.
            try {
                // for lim calculation
                varVar.setValue(BuiltinProcedures.evaluateSubstraction(datumValue, datumHValue));
                datumResult = aeStrExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);    // do not use lUnknown coz lVarNameSpaces has included var.
            } catch (Exception ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
            }
        } else if (nDeriOrder == 1) {
            try {
                varVar.setValue(BuiltinProcedures.evaluateAdding(datumValue, datumHValue));
                DataClass datumResult1 = aeStrExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                varVar.setValue(BuiltinProcedures.evaluateSubstraction(datumValue, datumHValue));
                DataClass datumResult2 = aeStrExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                datumResult = BuiltinProcedures.evaluateSubstraction(datumResult1, datumResult2);
                datumResult = BuiltinProcedures.evaluateDivision(datumResult,
                                BuiltinProcedures.evaluateMultiplication(datumHValue,
                                    new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TWO)));
            } catch (Exception ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
            }
        } else if (nDeriOrder == 2) {
            try {
                varVar.setValue(BuiltinProcedures.evaluateAdding(datumValue, datumHValue));
                DataClass datumXPlusH = aeStrExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                varVar.setValue(datumValue);
                DataClass datumX = aeStrExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                varVar.setValue(BuiltinProcedures.evaluateSubstraction(datumValue, datumHValue));
                DataClass datumXMinusH = aeStrExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                DataClass datumTWO = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TWO);
                datumResult = BuiltinProcedures.evaluateAdding(datumXPlusH, datumXMinusH);
                datumResult = BuiltinProcedures.evaluateSubstraction(datumResult,
                                BuiltinProcedures.evaluateMultiplication(datumTWO, datumX));
                datumResult = BuiltinProcedures.evaluateDivision(datumResult,
                                BuiltinProcedures.evaluateMultiplication(datumHValue, datumHValue));
            } catch (Exception ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
            }
        } else if (nDeriOrder == 3) {
            try {
                DataClass datumTWO = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TWO);
                varVar.setValue(BuiltinProcedures.evaluateAdding(datumValue,
                                    BuiltinProcedures.evaluateMultiplication(datumTWO, datumHValue)));
                DataClass datumXPlus2H = aeStrExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                varVar.setValue(BuiltinProcedures.evaluateAdding(datumValue, datumHValue));
                DataClass datumXPlusH = aeStrExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                varVar.setValue(datumValue);
                DataClass datumX = aeStrExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                varVar.setValue(BuiltinProcedures.evaluateSubstraction(datumValue, datumHValue));
                DataClass datumXMinusH = aeStrExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                varVar.setValue(BuiltinProcedures.evaluateSubstraction(datumValue,
                                    BuiltinProcedures.evaluateMultiplication(datumTWO, datumHValue)));
                DataClass datumXMinus2H = aeStrExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                datumResult = BuiltinProcedures.evaluateAdding(datumXPlus2H, 
                                BuiltinProcedures.evaluateMultiplication(datumTWO, datumXMinusH));
                datumResult = BuiltinProcedures.evaluateSubstraction(datumResult,
                                BuiltinProcedures.evaluateMultiplication(datumTWO, datumXPlusH));
                datumResult = BuiltinProcedures.evaluateSubstraction(datumResult, datumXMinus2H);
                datumResult = BuiltinProcedures.evaluateDivision(datumResult,
                                BuiltinProcedures.evaluateMultiplication(datumHValue, datumHValue));
                datumResult = BuiltinProcedures.evaluateDivision(datumResult,
                                BuiltinProcedures.evaluateMultiplication(datumTWO, datumHValue));
            } catch (Exception ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
            }
        } else {    // derivative order is at most 3.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        progContext.mdynamicProgContext.mlVarNameSpaces.pollFirst();
        return datumResult;
    }
    
}
