// MFP project, ExprEvaluator.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import java.util.*;

import com.cyzapps.Jfcalc.Operators.BoundOperator;
import com.cyzapps.Jfcalc.Operators.CalculateOperator;
import com.cyzapps.Jfcalc.DCHelper.CurPos;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.Operators.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ErrProcessor.*;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jsma.AEAssign;
import com.cyzapps.Jsma.AEBitwiseOpt;
import com.cyzapps.Jsma.AECompare;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEIndex;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AELeftDivOpt;
import com.cyzapps.Jsma.AEMulDivOpt;
import com.cyzapps.Jsma.AEOnOffUnaryOpt;
import com.cyzapps.Jsma.AEPosNegOpt;
import com.cyzapps.Jsma.AEPowerOpt;
import com.cyzapps.Jsma.AEUnaryOpt;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;

public class ExprEvaluator    {

    public ProgContext mprogContext;

    public ExprEvaluator() {
        mprogContext = new ProgContext();
    }

    public ExprEvaluator(ProgContext progContext) {
        mprogContext = progContext; // initialize mprogContext
    }

    // note that for OPERATOR_ASSIGN, datumFirstOperand has no effect.
    // also note that inside the function, except OPERATOR_ASSIGN, parameter value will not be changed
    // and returned result will not refer to any element of the parameter. So use populateDataArray
    // instead of fullfillDataArray to avoid deep copy.
    public static DataClass evaluateTwoOperandCell(DataClass datumFirstOperand,
            CalculateOperator COPTROperator, DataClass datumSecondOperand) throws JFCALCExpErrException {

        DataClass datumReturn = new DataClassNull();
        
        // no need to use getThisOrNull() because Null can be handled in getAExpr function.
        if (!(datumFirstOperand instanceof DataClassAExpr)
                && !(datumSecondOperand instanceof DataClassAExpr)) {
            switch (COPTROperator.getOperatorType()) {
            case OPERATOR_ASSIGN:
                // before 1.7.1.58, the first operand dataclass will be changed by copying the
                // second dataclass into it. However, after 1.7.1.58, all the dataclass are a reference
                // of memory structure. The reference of memory structure should be constant once
                // initialized. Assigning simply creates another memory structure reference which is,
                // in this case, datumReturn. So the following statement was commented.
                //datumFirstOperand.copyTypeValue(datumSecondOperand);    // not deep copy
                datumReturn = datumSecondOperand.copySelf();    // not deep copy
                break;
            case OPERATOR_EQ:
                if (datumFirstOperand.isEqual(datumSecondOperand)) {
                    datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
                } else {
                    datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
                }
                break;
            case OPERATOR_NEQ:
                if (datumFirstOperand.isEqual(datumSecondOperand)) {
                    datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
                } else {
                    datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
                }
                break;
            case OPERATOR_LARGER:
                DataClass datum1stOperand = datumFirstOperand.copySelf();
                DataClass datum2ndOperand = datumSecondOperand.copySelf();
                if (datum1stOperand.isEqual(datum2ndOperand))   {
                    datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
                } else  {            
                    if (DCHelper.lightCvtOrRetDCMFPDec(datum1stOperand).getDataValue()
                            .compareTo(DCHelper.lightCvtOrRetDCMFPDec(datum2ndOperand).getDataValue()) > 0) {
                        datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
                    } else {
                        datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
                    }
                }
                break;
            case OPERATOR_SMALLER:
                datum1stOperand = datumFirstOperand.copySelf();
                datum2ndOperand = datumSecondOperand.copySelf();
                if (datum1stOperand.isEqual(datum2ndOperand))   {
                    datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
                } else  {            
                    if (DCHelper.lightCvtOrRetDCMFPDec(datum1stOperand).getDataValue()
                            .compareTo(DCHelper.lightCvtOrRetDCMFPDec(datum2ndOperand).getDataValue()) < 0) {
                        datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
                    } else {
                        datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
                    }
                }
                break;
            case OPERATOR_NOLARGER:
                datum1stOperand = datumFirstOperand.copySelf();
                datum2ndOperand = datumSecondOperand.copySelf();
                if (datum1stOperand.isEqual(datum2ndOperand))   {
                    datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
                } else  {            
                    if (DCHelper.lightCvtOrRetDCMFPDec(datum1stOperand).getDataValue()
                            .compareTo(DCHelper.lightCvtOrRetDCMFPDec(datum2ndOperand).getDataValue()) <= 0) {
                        datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
                    } else {
                        datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
                    }
                }
                break;
            case OPERATOR_NOSMALLER:
                datum1stOperand = datumFirstOperand.copySelf();
                datum2ndOperand = datumSecondOperand.copySelf();
                if (datum1stOperand.isEqual(datum2ndOperand))   {
                    datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
                } else  {            
                    if (DCHelper.lightCvtOrRetDCMFPDec(datum1stOperand).getDataValue()
                            .compareTo(DCHelper.lightCvtOrRetDCMFPDec(datum2ndOperand).getDataValue()) >= 0) {
                        datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
                    } else {
                        datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
                    }
                }
                break;
            case OPERATOR_ADD:
                if (datumFirstOperand.getThisOrNull() instanceof DataClassArray
                    && datumSecondOperand.getThisOrNull() instanceof DataClassArray)    {
                    int[] narrayFirstDims = datumFirstOperand.recalcDataArraySize();
                    datumFirstOperand = DCHelper.lightCvtOrRetDCArray(datumFirstOperand).populateDataArray(narrayFirstDims, false);
                    int[] narraySecondDims = datumSecondOperand.recalcDataArraySize();
                    datumSecondOperand = DCHelper.lightCvtOrRetDCArray(datumSecondOperand).populateDataArray(narraySecondDims, false);
                }
                datumReturn = BuiltinProcedures.evaluateAdding(datumFirstOperand, datumSecondOperand);
                break;
            case OPERATOR_SUBTRACT:
                if (datumFirstOperand.getThisOrNull() instanceof DataClassArray
                    && datumSecondOperand.getThisOrNull() instanceof DataClassArray)    {
                    int[] narrayFirstDims = datumFirstOperand.recalcDataArraySize();
                    datumFirstOperand = DCHelper.lightCvtOrRetDCArray(datumFirstOperand).populateDataArray(narrayFirstDims, false);
                    int[] narraySecondDims = datumSecondOperand.recalcDataArraySize();
                    datumSecondOperand = DCHelper.lightCvtOrRetDCArray(datumSecondOperand).populateDataArray(narraySecondDims, false);
                }
                datumReturn = BuiltinProcedures.evaluateSubstraction(datumFirstOperand, datumSecondOperand);
                break;
            case OPERATOR_MULTIPLY:
                if (datumFirstOperand.getThisOrNull() instanceof DataClassArray)    {
                    int[] narrayFirstDims = datumFirstOperand.recalcDataArraySize();
                    datumFirstOperand = DCHelper.lightCvtOrRetDCArray(datumFirstOperand).populateDataArray(narrayFirstDims, false);
                }
                if (datumSecondOperand.getThisOrNull() instanceof DataClassArray)    {
                    int[] narraySecondDims = datumSecondOperand.recalcDataArraySize();
                    datumSecondOperand = DCHelper.lightCvtOrRetDCArray(datumSecondOperand).populateDataArray(narraySecondDims, false);
                }
                datumReturn = BuiltinProcedures.evaluateMultiplication(datumFirstOperand, datumSecondOperand);
                break;
            case OPERATOR_DIVIDE:
                if (datumFirstOperand.getThisOrNull() instanceof DataClassArray)    {
                    int[] narrayFirstDims = datumFirstOperand.recalcDataArraySize();
                    datumFirstOperand = DCHelper.lightCvtOrRetDCArray(datumFirstOperand).populateDataArray(narrayFirstDims, false);
                }
                if (datumSecondOperand.getThisOrNull() instanceof DataClassArray)    {
                    int[] narraySecondDims = datumSecondOperand.recalcDataArraySize();
                    datumSecondOperand = DCHelper.lightCvtOrRetDCArray(datumSecondOperand).populateDataArray(narraySecondDims, false);
                }
                datumReturn = BuiltinProcedures.evaluateDivision(datumFirstOperand, datumSecondOperand);
                break;
            case OPERATOR_LEFTDIVIDE:
                if (datumFirstOperand.getThisOrNull() instanceof DataClassArray)    {
                    int[] narrayFirstDims = datumFirstOperand.recalcDataArraySize();
                    datumFirstOperand = DCHelper.lightCvtOrRetDCArray(datumFirstOperand).populateDataArray(narrayFirstDims, false);
                }
                if (datumSecondOperand.getThisOrNull() instanceof DataClassArray)    {
                    int[] narraySecondDims = datumSecondOperand.recalcDataArraySize();
                    datumSecondOperand = DCHelper.lightCvtOrRetDCArray(datumSecondOperand).populateDataArray(narraySecondDims, false);
                }
                datumReturn = BuiltinProcedures.evaluateLeftDivision(datumFirstOperand, datumSecondOperand);
                break;
            case OPERATOR_BITWISEAND:
                DATATYPES enumDataType = DATATYPES.DATUM_MFPBOOL;
                if (!DCHelper.isDataClassType(datumFirstOperand, DATATYPES.DATUM_MFPBOOL)
                        || !DCHelper.isDataClassType(datumSecondOperand, DATATYPES.DATUM_MFPBOOL))    {
                    enumDataType = DATATYPES.DATUM_MFPINT;
                }

                DataClassSingleNum datum1stOperandInt = DCHelper.lightCvtOrRetDCMFPInt(datumFirstOperand);
                DataClassSingleNum datum2ndOperandInt = DCHelper.lightCvtOrRetDCMFPInt(datumSecondOperand);

                if (datum1stOperandInt.getDataValue().isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO);
                }
                if (datum2ndOperandInt.getDataValue().isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO);
                }
                datumReturn = new DataClassSingleNum(enumDataType,  // we can use toBigInteger directly because 1st and 2nd operands have been converted to MFP_INTEGER_TYPE
                        new MFPNumeric(datum1stOperandInt.getDataValue().toBigInteger().and(datum2ndOperandInt.getDataValue().toBigInteger())));
                break;
            case OPERATOR_BITWISEOR:
                enumDataType = DATATYPES.DATUM_MFPBOOL;
                if (!DCHelper.isDataClassType(datumFirstOperand, DATATYPES.DATUM_MFPBOOL)
                        || !DCHelper.isDataClassType(datumSecondOperand, DATATYPES.DATUM_MFPBOOL))    {
                    enumDataType = DATATYPES.DATUM_MFPINT;
                }

                datum1stOperandInt = DCHelper.lightCvtOrRetDCMFPInt(datumFirstOperand);
                datum2ndOperandInt = DCHelper.lightCvtOrRetDCMFPInt(datumSecondOperand);

                if (datum1stOperandInt.getDataValue().isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO);
                }
                if (datum2ndOperandInt.getDataValue().isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO);
                }
                datumReturn = new DataClassSingleNum(enumDataType,  // we can use toBigInteger directly because 1st and 2nd operands have been converted to MFP_INTEGER_TYPE
                        new MFPNumeric(datum1stOperandInt.getDataValue().toBigInteger().or(datum2ndOperandInt.getDataValue().toBigInteger())));
                break;
            case OPERATOR_BITWISEXOR:
                enumDataType = DATATYPES.DATUM_MFPBOOL;
                if (!DCHelper.isDataClassType(datumFirstOperand, DATATYPES.DATUM_MFPBOOL)
                        || !DCHelper.isDataClassType(datumSecondOperand, DATATYPES.DATUM_MFPBOOL))    {
                    enumDataType = DATATYPES.DATUM_MFPINT;
                }

                datum1stOperandInt = DCHelper.lightCvtOrRetDCMFPInt(datumFirstOperand);
                datum2ndOperandInt = DCHelper.lightCvtOrRetDCMFPInt(datumSecondOperand);

                if (datum1stOperandInt.getDataValue().isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO);
                }
                if (datum2ndOperandInt.getDataValue().isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO);
                }
                datumReturn = new DataClassSingleNum(enumDataType,  // we can use toBigInteger directly because 1st and 2nd operands have been converted to MFP_INTEGER_TYPE
                        new MFPNumeric(datum1stOperandInt.getDataValue().toBigInteger().xor(datum2ndOperandInt.getDataValue().toBigInteger())));
                break;
            case OPERATOR_POWER:
                // operator power only returns one root of the operand. To return all the roots, use pow function.
                // note that parameter values will not be changed in evaluatePower function so no copytypevalue
                // needed.
                datumReturn = BuiltinProcedures.evaluatePower(datumFirstOperand, datumSecondOperand, null);            
                break;
            default:
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_BINARY_OPERATOR);
            }
        } else {
            try {
                // no need to use getThisOrNull in the following statement because Null can be handled
                // by getAExpr() function and Null needs no deep copy.
                AbstractExpr aexpr1stOperand = (datumFirstOperand instanceof DataClassAExpr)?
                                        DCHelper.lightCvtOrRetDCAExpr(datumFirstOperand).getAExpr()
                                        :new AEConst(datumFirstOperand instanceof DataClassArray?
                                            datumFirstOperand:datumFirstOperand.cloneSelf());
                AbstractExpr aexpr2ndOperand = (datumSecondOperand instanceof DataClassAExpr)?
                                        DCHelper.lightCvtOrRetDCAExpr(datumSecondOperand).getAExpr()
                                        :new AEConst(datumSecondOperand instanceof DataClassArray?
                                            datumSecondOperand:datumSecondOperand.cloneSelf());
                switch (COPTROperator.getOperatorType()) {
                case OPERATOR_ASSIGN: {
                    LinkedList<AbstractExpr> listAExprs = new LinkedList<AbstractExpr>();
                    listAExprs.add(aexpr1stOperand);
                    listAExprs.add(aexpr2ndOperand);
                    datumReturn = new DataClassAExpr(new AEAssign(listAExprs));
                    break;
                } case OPERATOR_EQ:
                case OPERATOR_NEQ:
                case OPERATOR_LARGER:
                case OPERATOR_SMALLER:
                case OPERATOR_NOLARGER:
                case OPERATOR_NOSMALLER: {
                    datumReturn = new DataClassAExpr(new AECompare(aexpr1stOperand, COPTROperator, aexpr2ndOperand));
                    break;
                } case OPERATOR_ADD:
                case OPERATOR_SUBTRACT: {
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
                    listOpts.add(COPTROperator);
                    LinkedList<AbstractExpr> listAExprs = new LinkedList<AbstractExpr>();
                    listAExprs.add(aexpr1stOperand);
                    listAExprs.add(aexpr2ndOperand);
                    datumReturn = new DataClassAExpr(new AEPosNegOpt(listAExprs, listOpts));
                    break;
                } case OPERATOR_MULTIPLY:
                case OPERATOR_DIVIDE: {
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                    listOpts.add(COPTROperator);
                    LinkedList<AbstractExpr> listAExprs = new LinkedList<AbstractExpr>();
                    listAExprs.add(aexpr1stOperand);
                    listAExprs.add(aexpr2ndOperand);
                    datumReturn = new DataClassAExpr(new AEMulDivOpt(listAExprs, listOpts));
                    break;
                } case OPERATOR_LEFTDIVIDE: {
                    datumReturn = new DataClassAExpr(new AELeftDivOpt(aexpr1stOperand, aexpr2ndOperand));
                    break;
                } case OPERATOR_BITWISEAND:
                case OPERATOR_BITWISEOR:
                case OPERATOR_BITWISEXOR: {
                    datumReturn = new DataClassAExpr(new AEBitwiseOpt(aexpr1stOperand, COPTROperator, aexpr2ndOperand));
                    break;
                } case OPERATOR_POWER: {
                    datumReturn = new DataClassAExpr(new AEPowerOpt(aexpr1stOperand, aexpr2ndOperand));            
                    break;
                } default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_BINARY_OPERATOR);
                }
            } catch (JSmartMathErrException ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);    // will not arrive here.
            }
        }
        return datumReturn;
    }

    public static DataClass evaluateOneOperandCell(DataClass datumOperand,
            CalculateOperator COPTROperator) throws JFCALCExpErrException {
        OPERATORTYPES enumOperatorType = COPTROperator.getOperatorType();
        DataClass datumReturn = new DataClassNull();
        // no need to call getThisOrNull() function coz getAExpr can handle Null
        if (!(datumOperand instanceof DataClassAExpr)) {
            switch (enumOperatorType) {
            case OPERATOR_PERCENT: {
                // % is always treated as divided by 100. Note that if it is matrix, it has to be fully populated first.
                DataClass datumOprndCpy;
                if (datumOperand.getThisOrNull() instanceof DataClassArray) {
                    int[] narrayDims = datumOperand.recalcDataArraySize();
                    // no need to call fullfillDataArray because no need to clone datumOperand
                    datumOprndCpy = DCHelper.lightCvtOrRetDCArray(datumOperand).populateDataArray(narrayDims, false);
                } else {
                    datumOprndCpy = datumOperand;
                }
                // datumOprndCpy will not be changed inside evaluateDivision and return value (datumReturn) will not
                // refer to any element of parameter datumOprndCpy.
                datumReturn = BuiltinProcedures.evaluateDivision(datumOprndCpy, new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(100)));
                break;
            }
            case OPERATOR_FACTORIAL: {
                if (!(datumOperand.getThisOrNull() instanceof DataClassSingleNum))    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_OPERAND_TYPE);
                }
                MFPNumeric mfpNumValue = DCHelper.lightCvtOrRetDCSingleNum(datumOperand).getDataValue();
                if (mfpNumValue.isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_FACTORIAL_CAN_NOT_LESS_THAN_ZERO);
                }
                // need not to worry about too large factorial because MFPNumeric support any large value.
                if (DCHelper.lightCvtOrRetDCMFPInt(datumOperand).getDataValue().isActuallyZero())
                    mfpNumValue = MFPNumeric.ONE;
                else {
                    MFPNumeric mfpNumMultiplyNum = mfpNumValue;
                    mfpNumValue = MFPNumeric.ONE;
                    for (MFPNumeric i = MFPNumeric.ONE; i.compareTo(mfpNumMultiplyNum) <= 0; i = i.add(MFPNumeric.ONE)) {
                        mfpNumValue = mfpNumValue.multiply(i);
                    }
                }
                datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, mfpNumValue);    // need not to worry about integer overflow
                                                                                // now coz there will be auto-conversion
                                                                                // later on.
                break;
            }
            case OPERATOR_TRANSPOSE:    {
                int[] narrayDims = null;
                if (datumOperand.getThisOrNull() instanceof DataClassArray)    {
                    narrayDims = datumOperand.recalcDataArraySize();
                    // no need to call fulfillDataArray because evaluateTransposition doesn't change value of parameter
                    // and returned value doesn't refer to any element of parameter.
                    datumOperand = DCHelper.lightCvtOrRetDCArray(datumOperand).populateDataArray(narrayDims, false);
                }
                datumReturn = BuiltinProcedures.evaluateTransposition(datumOperand, narrayDims);
                break;
            }
            default:
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_MONADIC_OPERATOR);
            }
        } else {
            try {
                // it is an abstractexpr
                AbstractExpr aexpr = DCHelper.lightCvtOrRetDCAExpr(datumOperand).getAExpr();
                switch (enumOperatorType) {
                case OPERATOR_PERCENT:
                case OPERATOR_FACTORIAL:    {
                    datumReturn = new DataClassAExpr(new AEUnaryOpt(COPTROperator, aexpr));
                    break;
                }
                case OPERATOR_TRANSPOSE:    {
                    datumReturn = new DataClassAExpr(new AEOnOffUnaryOpt(COPTROperator, aexpr, 1));
                    break;
                }
                default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_MONADIC_OPERATOR);
                }
            } catch (JSmartMathErrException ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);    // will not arrive here.
            }
        }
        return datumReturn;
    }

    public static DataClass evaluateOneOperandCell(CalculateOperator COPTROperator,
            DataClass datumOperand) throws JFCALCExpErrException {
        OPERATORTYPES enumOperatorType = COPTROperator.getOperatorType();
        DataClass datumReturn = new DataClassNull();

        if (!(datumOperand instanceof DataClassAExpr)) {    // no need to check if datumOperand is Null, getAExpr will handle.
            switch (enumOperatorType) {
            case OPERATOR_FALSE:
                if (DCHelper.lightCvtOrRetDCMFPBool(datumOperand).getDataValue().isActuallyZero()) { /* boolean : FALSE */
                    datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
                } else {
                    datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
                }
                break;
            case OPERATOR_NOT:
                DATATYPES enumDataType = DATATYPES.DATUM_MFPBOOL;
                if (!DCHelper.isDataClassType(datumOperand, DATATYPES.DATUM_MFPBOOL))    {
                    enumDataType = DATATYPES.DATUM_MFPINT;
                }

                if (DCHelper.lightCvtOrRetDCMFPInt(datumOperand).getDataValue().isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO);
                }
                // datum return has been converted to MFP_INTEGER_TYPE so that we can use toBigInteger() directly.
                datumReturn = new DataClassSingleNum(enumDataType, new MFPNumeric(DCHelper.lightCvtOrRetDCMFPInt(datumOperand).getDataValue().toBigInteger().not()));
                break;
            case OPERATOR_POSSIGN:
                // if data type does not match, the following two statements
                // will throw exceptions.
                if (DCHelper.isDataClassType(datumOperand, DATATYPES.DATUM_MFPBOOL)) {
                    datumReturn = DCHelper.lightCvtOrRetDCMFPInt(datumOperand);
                } else if (DCHelper.isDataClassType(datumOperand, DATATYPES.DATUM_MFPINT, DATATYPES.DATUM_MFPDEC, DATATYPES.DATUM_COMPLEX)) {
                    datumReturn = datumOperand.copySelf();
                } else if (DCHelper.isDataClassType(datumOperand, DATATYPES.DATUM_REF_DATA)) {
                    if (!DCHelper.isNumericalData(datumOperand, true))    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
                    }
                    datumReturn = datumOperand.copySelf();
                } else {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
                }
                break;
            case OPERATOR_NEGSIGN:
                datumReturn = BuiltinProcedures.evaluateNegSign(datumOperand);
                break;
            default:
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_MONADIC_OPERATOR);
            }
        } else {
            try {
                // it is an abstractexpr
                AbstractExpr aexpr = DCHelper.lightCvtOrRetDCAExpr(datumOperand).getAExpr();
                switch (enumOperatorType) {
                case OPERATOR_FALSE:
                case OPERATOR_NOT:
                    datumReturn = new DataClassAExpr(new AEOnOffUnaryOpt(COPTROperator, aexpr, 1));
                    break;
                case OPERATOR_POSSIGN:
                    datumReturn = datumOperand.cloneSelf();
                    break;
                case OPERATOR_NEGSIGN:
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(COPTROperator);
                    LinkedList<AbstractExpr> listAExprs = new LinkedList<AbstractExpr>();
                    listAExprs.add(aexpr);
                    AbstractExpr aexprReturn = new AEPosNegOpt(listAExprs, listOpts);
                    datumReturn = new DataClassAExpr(aexprReturn);
                    break;
                default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_MONADIC_OPERATOR);
                }
            } catch (JSmartMathErrException ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);    // will not arrive here.
            }
        }
        return datumReturn;
    }

    /**
     * This function evaluate an array index. It has an output parameter referringObj
     * @param datumToBeIndex
     * @param datumIndex
     * @param referringObj which is the data array which refers to the returned value. Note that it is
     * used to output orginal array for assignment.
     * @return the evaluated value
     * @throws JFCALCExpErrException
     */
    public static DataClass evaluateIndex(DataClass datumToBeIndex, DataClass datumIndex, DataReferringObj referringObj) throws JFCALCExpErrException    {
        
        if (!(datumToBeIndex.getThisOrNull() instanceof DataClassAExpr)
                && !(datumIndex.getThisOrNull() instanceof DataClassAExpr)) {
            if (!(datumToBeIndex.getThisOrNull() instanceof DataClassArray)
                    || !(datumIndex.getThisOrNull() instanceof DataClassArray)) {
                // if the parameters are not abstract exprs, they must be arrays.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_TYPE);
            }
            // dont deep copy datumIndex coz it is just index, we do not need to change index's value.
            DataClassArray datumIdx = DCHelper.lightCvtOrRetDCArray(datumIndex);
            int[] nIndices = new int[datumIdx.getDataListSize()];
            for (int idx = 0; idx < datumIdx.getDataListSize(); idx ++)    {
                // index can be integer. don't worry its overflow
                nIndices[idx] = (int)DCHelper.lightCvtOrRetDCMFPInt(datumIdx.getDataList()[idx]).getDataValue().longValue();
            }
            /*
             * We do not use datumReturnNum = getDataAtIndex(indexArray)
             * (which includes a deep copy of the returned data at index)
             * here because when we assign a new value to a data class,
             * we need the data class reference.
             */
            DataClassArray datumToBeIdx = DCHelper.lightCvtOrRetDCArray(datumToBeIndex);
            DataClass datumReturnNum = datumToBeIdx.getDataAtIndexByRef(nIndices);
            // we need to set referring object of datumReturnNum so that later on when we assign new value
            // to datumReturnNum, we can update referring object.
            referringObj.setReferringObj(datumToBeIdx, nIndices);
            return datumReturnNum;
        } else {    // at least one of the parameters is AbstractExpr.
            AbstractExpr aeReturn = AEInvalid.AEINVALID;
            try {
                AbstractExpr aexpr2BeIndex = (datumToBeIndex.getThisOrNull() instanceof DataClassAExpr)?
                        DCHelper.lightCvtOrRetDCAExpr(datumToBeIndex).getAExpr():new AEConst(datumToBeIndex);
                AbstractExpr aexprIndex = (datumIndex.getThisOrNull() instanceof DataClassAExpr)?
                        DCHelper.lightCvtOrRetDCAExpr(datumToBeIndex).getAExpr():new AEConst(datumIndex.cloneSelf());
                aeReturn = new AEIndex(aexpr2BeIndex, aexprIndex);
            } catch (JSmartMathErrException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);    // will not arrive here.
            }
            return new DataClassAExpr(aeReturn);
        }
    }
    
    private class ExprElement
    {
        protected int mnStart;
        public int getStart() {
            return mnStart;
        }
        protected int mnEnd;
        public int getEnd() {
            return mnEnd;
        }
        public ExprElement(int start, int end) {
            mnStart = start;
            mnEnd = end;
        }
    }
    private class ExprListValue extends ExprElement
    {
        private DataClass mdatumValue;
        public DataClass getDatum() {
            return mdatumValue;
        }
        private DataReferringObj mreferringObject;
        public DataReferringObj getDataReferringObj() {
            return mreferringObject;
        }
        public ExprListValue(int start, int end, DataClass datum) {
            super(start, end);
            mdatumValue = datum;
            mreferringObject = new DataReferringObj();
        }
        public ExprListValue(int start, int end, DataClass datum, DataReferringObj referringObj) {
            super(start, end);
            mdatumValue = datum;
            mreferringObject = referringObj;
        }
    }
    
    private class ExprOperator extends ExprElement
    {
        private CalculateOperator mOperator;
        public CalculateOperator getCalcOperator() {
            return mOperator;
        }
        
        public ExprOperator(int start, int end, CalculateOperator oper) {
            super(start, end);
            mOperator = oper;
        }
    }
    
    public DataClass evaluateExpression(String strExpression, CurPos curpos) throws JFCALCExpErrException, InterruptedException {
        LinkedList<ExprListValue> listValues = new LinkedList<ExprListValue>();
        LinkedList<ExprOperator> listCalcOpts = new LinkedList<ExprOperator>();

        BoundOperator BOPTRCurboptr;
        CalculateOperator COPTRStackTopcoptr;
        CalculateOperator COPTRCurcoptr = new CalculateOperator();
        DataClass datumCurNum = new DataClassNull();

        int nLastPushedCellType = 0; /* 1 for number, 2 for calculation operator */
        int nRecursionStartPosition = curpos.m_nPos;

        while (curpos.m_nPos < strExpression.length()) {

            /* A bound char */
            if (ElemAnalyzer.isBoundOperatorChar(strExpression, curpos.m_nPos)) {
                BOPTRCurboptr = ElemAnalyzer.getBoundOperator(strExpression, curpos);
                // need not to worry about OPERATOR_NOTEXIST because it has been handled
                // in GetBoundOperator
                if (BOPTRCurboptr.isEqual(OPERATORTYPES.OPERATOR_LEFTPARENTHESE)) {
                    int nStart = curpos.m_nPos;
                    datumCurNum = evaluateExpression(strExpression, curpos);

                    if (nLastPushedCellType == 1) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS);
                    }

                    listValues.addFirst(new ExprListValue(nStart, curpos.m_nPos, datumCurNum));
                    nLastPushedCellType = 1;
                }

                else if (BOPTRCurboptr.isEqual(OPERATORTYPES.OPERATOR_RIGHTPARENTHESE)) {
                    if (nRecursionStartPosition == 0) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_UNMATCHED_RIGHTPARENTHESE);
                    }

                    if ((listCalcOpts.size() == 0) && (listValues.size() == 0)) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_EXPRESSION);
                    }

                    if (nLastPushedCellType != 1) {
                        throw new JFCALCExpErrException(
                                ERRORTYPES.ERROR_LACK_OPERAND);
                    }

                    ExprListValue elv = listValues.poll();
                    datumCurNum = (elv == null)?null:elv.getDatum();
                    ExprOperator eo = listCalcOpts.poll();
                    COPTRStackTopcoptr = (eo == null)?null:eo.getCalcOperator();
                    while (COPTRStackTopcoptr != null) {
                        if (COPTRStackTopcoptr.getOperandNum() == 1) {
                            if (datumCurNum == null) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                            }
                            datumCurNum = evaluateOneOperandCell(COPTRStackTopcoptr, datumCurNum);
                        } else {    // COPTRStackTopcoptr.getOperandNum() == 2
                            elv = listValues.poll();
                            DataClass datumFirstOperand = (elv == null)?null:elv.getDatum();
                            DataReferringObj referringObj = (elv == null)?null:elv.getDataReferringObj();
                            if (datumFirstOperand == null || datumCurNum == null) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                            }
                            datumCurNum = evaluateTwoOperandCell(
                                    datumFirstOperand, COPTRStackTopcoptr,
                                    datumCurNum);
                            if (COPTRStackTopcoptr.getOperatorType() == OPERATORTYPES.OPERATOR_ASSIGN) {
                                if (referringObj.isEmpty()) {
                                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ASSIGN_VALUE_TO_ANYTHING_EXCEPT_VARIALBE_USE_EQUAL_INSTEAD);
                                } else {
                                    // set value back if assignment.
                                    referringObj.setNewValue4ReferringObj(datumCurNum);
                                }
                            }
                        }
                        /* need not to worry about non exist data return now because an exception will be thrown
                        if (datumCurNum.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                            return datumCurNum;*/
                        eo = listCalcOpts.poll();
                        COPTRStackTopcoptr = (eo == null)?null:eo.getCalcOperator();
                    }
                    return datumCurNum;
                } else {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERATOR_NOT_EXIST);
                }

            }

            /* an operator char */
            else if (ElemAnalyzer.isCalcOperatorChar(strExpression,
                    curpos.m_nPos)) {
                int nCurCoptrStart = strExpression.length();
                int nCurCoptrEnd = strExpression.length();
                while ((strExpression.length() > curpos.m_nPos)
                        && (ElemAnalyzer.isCalcOperatorChar(strExpression, curpos.m_nPos)
                                || ElemAnalyzer.isBlankChar(strExpression, curpos.m_nPos))) {
                    if (ElemAnalyzer.isBlankChar(strExpression, curpos.m_nPos))
                        curpos.m_nPos++;
                    else {
                        nCurCoptrStart = curpos.m_nPos;
                        COPTRCurcoptr = ElemAnalyzer.getCalcOperator(strExpression, curpos, nLastPushedCellType);
                        nCurCoptrEnd = curpos.m_nPos;
                        // need not worry about OPERATOR_NOTEXIST here because OPERATOR_NOTEXIST has been handled
                        // in elemAnalyzer.GetCalcOperator.
                        if (COPTRCurcoptr.getLabelPrefix()) {
                            if (COPTRCurcoptr.getOperandNum() == 2
                                    && nLastPushedCellType != 1) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                            }
                            if (COPTRCurcoptr.getOperandNum() == 1
                                    && nLastPushedCellType == 1) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                            }
                            break;
                        } else {
                            ExprListValue elv = listValues.poll();
                            datumCurNum = (elv == null)?null:elv.getDatum();
                            int nStart = (elv == null)?null:elv.getStart();
                            if (nLastPushedCellType != 1 || datumCurNum == null) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                            }
                            datumCurNum = evaluateOneOperandCell(datumCurNum, COPTRCurcoptr);
                            /* need not to worry about non-exist data because it is handled
                             * in EvaluateOneOperandCell
                            if (datumCurNum.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                                return datumCurNum;*/
                            listValues.addFirst(new ExprListValue(nStart, curpos.m_nPos, datumCurNum));
                            nLastPushedCellType = 1;
                        }
                    }
                }

                if (COPTRCurcoptr.getLabelPrefix()) {
                    if (COPTRCurcoptr.getOperandNum() == 2) {
                        boolean bHaveBOptHigherLevelLeft = false;
                        for (int idx = 0; idx < listCalcOpts.size(); idx ++) {
                            if (listCalcOpts.get(idx).getCalcOperator().getOperandNum() == 2) {
                                if (!ElemAnalyzer.is2ndOPTRHaveHighLevel(listCalcOpts.get(idx).getCalcOperator(), COPTRCurcoptr)) {
                                    bHaveBOptHigherLevelLeft = true;
                                }
                                break;
                            }
                        }
                        ExprListValue elv = listValues.poll();
                        datumCurNum = (elv == null)?null:elv.getDatum();
                        DataReferringObj referringObj = (elv == null)?null:elv.getDataReferringObj();
                        int nEnd = (elv == null)?-1:elv.getEnd();
                        ExprOperator eo = listCalcOpts.poll();
                        COPTRStackTopcoptr = (eo == null)?null:eo.getCalcOperator();
                        int nStart = (eo == null)?-1:eo.getStart();
                        while (COPTRStackTopcoptr != null
                                && ((bHaveBOptHigherLevelLeft && COPTRStackTopcoptr.getOperandNum() == 1)
                                    || !ElemAnalyzer.is2ndOPTRHaveHighLevel(COPTRStackTopcoptr, COPTRCurcoptr))) {
                            if (COPTRStackTopcoptr.getOperandNum() == 1) {
                                if (datumCurNum == null)    {
                                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                                }
                                datumCurNum = evaluateOneOperandCell(COPTRStackTopcoptr, datumCurNum);
                                referringObj.resetReferringObj(); // now datumCurNum is not referred by any obj.
                            } else {    // COPTRStackTopcoptr.getOperandNum() == 2
                                elv = listValues.poll();
                                DataClass datumFirstOperand = (elv == null)?null:elv.getDatum();
                                referringObj = (elv == null)?null:elv.getDataReferringObj();
                                nStart = (elv == null)?-1:elv.getStart();
                                if (datumFirstOperand == null || datumCurNum == null) {
                                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                                }
                                datumCurNum = evaluateTwoOperandCell(datumFirstOperand, COPTRStackTopcoptr, datumCurNum);
                                if (COPTRStackTopcoptr.getOperatorType() == OPERATORTYPES.OPERATOR_ASSIGN) {
                                    if (referringObj.isEmpty()) {
                                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ASSIGN_VALUE_TO_ANYTHING_EXCEPT_VARIALBE_USE_EQUAL_INSTEAD);
                                    } else {
                                        // set value back if assignment.
                                        referringObj.setNewValue4ReferringObj(datumCurNum);
                                    }
                                } else {
                                    referringObj.resetReferringObj(); // datumCurNum is not referred by any obj.
                                }
                            }
                            /* need not to worry about non-exist data because it is handled in
                             * EvaluateOneOperandCell
                            if (datumCurNum.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                                return datumCurNum;*/

                            eo = listCalcOpts.poll();
                            COPTRStackTopcoptr = (eo == null)?null:eo.getCalcOperator();
                            nStart = (eo == null)?-1:eo.getStart();
                        }
                        if (COPTRStackTopcoptr != null) {
                            listCalcOpts.addFirst(eo);
                        }
                        listValues.addFirst(new ExprListValue(nStart, nEnd, datumCurNum, referringObj));
                    }

                    listCalcOpts.addFirst(new ExprOperator(nCurCoptrStart, nCurCoptrEnd, COPTRCurcoptr));
                    nLastPushedCellType = 2;
                }
            }

            /* The new cell seems to be a number */
            else if (ElemAnalyzer.isStartNumberChar(strExpression, curpos.m_nPos)) {
                int nStart = curpos.m_nPos;
                datumCurNum = ElemAnalyzer.getNumber(strExpression, curpos);
                /* we need not to worry about Not exist data here because error has been handled before
                 * if (datumCurNum.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                    return datumCurNum;*/
                if (nLastPushedCellType == 1) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS);
                }

                listValues.addFirst(new ExprListValue(nStart, curpos.m_nPos, datumCurNum));
                nLastPushedCellType = 1;
            }

            /* The new cell seems to be a data reference */
            else if (ElemAnalyzer.isDataRefChar(strExpression, curpos.m_nPos)) {
                int nStart = curpos.m_nPos;
                datumCurNum = ElemAnalyzer.getDataRef(strExpression, curpos, mprogContext);
                DataReferringObj referringObj = new DataReferringObj();
                if (nLastPushedCellType == 1)    {    // last time a value was pushed, not an operator, this data reference should be an index.
                    ExprListValue elv = listValues.poll();
                    DataClass datumToBeIndexed = (elv == null)?null:elv.getDatum();
                    nStart = (elv == null)?-1:elv.getStart();
                    if (datumToBeIndexed == null || datumCurNum == null)    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                    }
                    datumCurNum = evaluateIndex(datumToBeIndexed, datumCurNum, referringObj);
                }
                listValues.addFirst(new ExprListValue(nStart, curpos.m_nPos, datumCurNum, referringObj));
                nLastPushedCellType = 1;
            }


            /* a member indicator char */
            else if (nLastPushedCellType == 1 && strExpression.charAt(curpos.m_nPos) == '.') {
				do {
					curpos.m_nPos++;
				} while (ElemAnalyzer.isBlankChar(strExpression, curpos.m_nPos));
                ExprListValue evl = listValues.poll();
				datumCurNum = evl.getDatum();
                int nStart = evl.getStart();
                DataReferringObj referringObj = new DataReferringObj();
                String strDatumCurNum = strExpression.substring(nStart, evl.getEnd());
                datumCurNum = ElemAnalyzer.getExprName(strDatumCurNum, datumCurNum, strExpression, curpos, referringObj, mprogContext);
                /* A function might return NULL point. A NULL point is DATATYPES.DATUM_NOTEXIST
                if (datumCurNum.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                    return datumCurNum;*/
                listValues.addFirst(new ExprListValue(nStart, curpos.m_nPos, datumCurNum, referringObj));
                nLastPushedCellType = 1;
            }
            /*
             * The new cell seems to be the beginning of a variable or
             * function's name
             */
            else if (ElemAnalyzer.isNameChar(strExpression, curpos.m_nPos) == 1
                    || strExpression.charAt(curpos.m_nPos) == ':') {
                DataReferringObj referringObj = new DataReferringObj();
                int nNameStart = curpos.m_nPos;
                datumCurNum = ElemAnalyzer.getExprName(null, null, strExpression, curpos, referringObj, mprogContext);
                /* A function might return NULL point. A NULL point is DATATYPES.DATUM_NOTEXIST
                if (datumCurNum.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                    return datumCurNum;*/
                if (nLastPushedCellType == 1) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS);
                }

                listValues.addFirst(new ExprListValue(nNameStart, curpos.m_nPos, datumCurNum, referringObj));
                nLastPushedCellType = 1;
            }

            /* A blank char */
            else if (ElemAnalyzer.isBlankChar(strExpression, curpos.m_nPos))
                curpos.m_nPos++;
            /* A "," */
            else if (strExpression.charAt(curpos.m_nPos) == ',') {
                /*
                 * we do not add curpos.m_nPos++; here because curpos stops at
                 * the first char which does not belong to the expression.
                 */
                break; /* ignore part of the expression after "," */
            }
            else if (ElemAnalyzer.isStringStartChar(strExpression, curpos.m_nPos))    {
                int nStart = curpos.m_nPos;
                datumCurNum = ElemAnalyzer.getString(strExpression, curpos);
                if (nLastPushedCellType == 1) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS);
                }
                listValues.addFirst(new ExprListValue(nStart, curpos.m_nPos, datumCurNum));
                nLastPushedCellType = 1;
            } else {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_CHARACTER);
            }

        }
        /* arriving here means we are in the top level of expression */
        if (nRecursionStartPosition != 0) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_UNMATCHED_LEFTPARENTHESE);
        }

        if ((listCalcOpts.size() == 0) && (listValues.size() == 0)) {
            if (curpos.m_nPos == 0)
                curpos.m_nPos = 1;
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_EXPRESSION);
        }

        if (nLastPushedCellType != 1) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
        }

        ExprListValue elv = listValues.poll();
        datumCurNum = (elv == null)?null:elv.getDatum();
        ExprOperator eo = listCalcOpts.poll();
        COPTRStackTopcoptr = (eo == null)?null:eo.getCalcOperator();
        while (COPTRStackTopcoptr != null) {
            if (COPTRStackTopcoptr.getOperandNum() == 1) {
                if (datumCurNum == null) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                }
                datumCurNum = evaluateOneOperandCell(COPTRStackTopcoptr, datumCurNum);
            } else {    // COPTRStackTopcoptr.getOperandNum() == 2
                elv = listValues.poll();
                DataClass datumFirstOperand = (elv == null)?null:elv.getDatum();
                DataReferringObj referringObj = (elv == null)?null:elv.getDataReferringObj();
                if (datumFirstOperand == null || datumCurNum == null) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                }
                datumCurNum = evaluateTwoOperandCell(datumFirstOperand, COPTRStackTopcoptr, datumCurNum);
                if (COPTRStackTopcoptr.getOperatorType() == OPERATORTYPES.OPERATOR_ASSIGN) {
                    if (referringObj.isEmpty()) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ASSIGN_VALUE_TO_ANYTHING_EXCEPT_VARIALBE_USE_EQUAL_INSTEAD);
                    } else {
                        // set value back if assignment.
                        referringObj.setNewValue4ReferringObj(datumCurNum);
                    }
                }
            }
            /* need not to worry about non-exist data because it is handled in
             * EvaluateOneOperandCell
            if (datumCurNum.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                return datumCurNum;*/
            eo = listCalcOpts.poll();
            COPTRStackTopcoptr = (eo == null)?null:eo.getCalcOperator();
        }

        return datumCurNum;
    }
}