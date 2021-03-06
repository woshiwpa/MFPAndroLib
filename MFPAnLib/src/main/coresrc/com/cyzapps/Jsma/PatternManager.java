// MFP project, PatternManager.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.Operators.CalculateOperator;
import com.cyzapps.Jfcalc.DCHelper.CurPos;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.Operators.OPERATORTYPES;
import com.cyzapps.Jfcalc.BuiltinProcedures;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.AbstractExpr.SimplifyParams;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import com.cyzapps.adapter.FunctionNameMapper;

public class PatternManager {
    static public int enumIdx = 0;
    
    static public enum ABSTRACTEXPRPATTERNS
    {
        AEP_UNRECOGNIZEDPATTERN(0),
        
        AEP_SIMPLEZERO(1),  // pattern identifier x
        AEP_SIMPLEADD(2),   // pattern identifier x + a
        AEP_SIMPLESUB(3),   // pattern identifier x - a
        AEP_SIMPLEMULEDZERO(4),    // pattern identifier x*a
        AEP_SIMPLEMULEDADD(5),    // pattern identifier x*a + b
        AEP_SIMPLEMULEDSUB(6),    // pattern identifier x*a - b
        AEP_SIMPLEMULZERO(7),    // pattern identifier a*x
        AEP_SIMPLEMULADD(8),    // pattern identifier a*x + b
        AEP_SIMPLEMULSUB(9),    // pattern identifier a*x - b
        AEP_SIMPLEDIVEDZERO(10),    // pattern identifier x/a 
        AEP_SIMPLEDIVEDADD(11),    // pattern identifier x/a + b
        AEP_SIMPLEDIVEDSUB(12),    // pattern identifier x/a - b
        AEP_SIMPLEDIVZERO(13),    // pattern identifier a/x
        AEP_SIMPLEDIVADD(14),    // pattern identifier a/x + b
        AEP_SIMPLEDIVSUB(15),    // pattern identifier a/x - b
        AEP_SIMPLELEFTDIVEDZERO(16),    // pattern identifier a\x
        AEP_SIMPLELEFTDIVEDADD(17),    // pattern identifier a\x + b
        AEP_SIMPLELEFTDIVEDSUB(18),    // pattern identifier a\x - b
        AEP_SIMPLELEFTDIVZERO(19),    // pattern identifier x\a
        AEP_SIMPLELEFTDIVADD(20),    // pattern identifier x\a + b
        AEP_SIMPLELEFTDIVSUB(21),    // pattern identifier x\a - b
        AEP_SIMPLEPOWEREDZERO(22),    // pattern identifier x**a
        AEP_SIMPLEPOWEREDADD(23),    // pattern identifier x**a + b
        AEP_SIMPLEPOWEREDSUB(24),    // pattern identifier x**a - b
        AEP_SIMPLEPOWERZERO(25),    // pattern identifier a**x
        AEP_SIMPLEPOWERADD(26),    // pattern identifier a**x + b
        AEP_SIMPLEPOWERSUB(27),    // pattern identifier a**x - b
        AEP_SIMPLENEGZERO(28),    // pattern identifier -x
        AEP_SIMPLENEGADD(29),    // pattern identifier -x + b
        AEP_SIMPLENEGSUB(30),    // pattern identifier -x - b
        AEP_SIMPLETRANSPOSEZERO(31),    // pattern identifier x'
        AEP_SIMPLETRANSPOSEADD(32),    // pattern identifier x' + b
        AEP_SIMPLETRANSPOSESUB(33),    // pattern identifier x' - b
        AEP_SIMPLEPERCENTZERO(34),    // pattern identifier x%
        AEP_SIMPLEPERCENTADD(35),    // pattern identifier x% + b
        AEP_SIMPLEPERCENTSUB(36),    // pattern identifier x% - b
        AEP_SIMPLEINVFUNCZERO(37),    // pattern identifier f_single_var_invertible(x)
        AEP_SIMPLEINVFUNCADD(38),    // pattern identifier f_single_var_invertible(x) + b
        AEP_SIMPLEINVFUNCSUB(39),    // pattern identifier f_single_var_invertible(x) - b
        
        AEP_EXPR_1_OVER_A_PLUS_B_X(501), // pattern 1/(a+b*x)
        AEP_EXPR_X_OVER_A_PLUS_B_X(502), // pattern x/(a+b*x)
        AEP_EXPR_X_SQR_OVER_A_PLUS_B_X(503), // pattern x**2/(a+b*x)
        AEP_EXPR_1_OVER_X_A_PLUS_B_X(504), // pattern 1/(x*(a+b*x))
        AEP_EXPR_1_OVER_X_SQR_A_PLUS_B_X(505), // pattern 1/(x**2*(a+b*x))
        
        AEP_EXPR_SQRT_A_PLUS_B_X(506),    // pattern sqrt(a+b*x)
        AEP_EXPR_X_SQRT_A_PLUS_B_X(507),    // pattern x*sqrt(a+b*x)
        AEP_EXPR_X_TO_N_SQRT_A_PLUS_B_X(508),   // pattern x**n*sqrt(a+b*x)
        AEP_EXPR_SQRT_A_PLUS_B_X_OVER_X(509),   // pattern sqrt(a+b*x)/x
        AEP_EXPR_SQRT_A_PLUS_B_X_OVER_X_TO_N(510),  // pattern sqrt(a+b*x)/x**n
        AEP_EXPR_1_OVER_SQRT_A_PLUS_B_X(511),    // pattern 1/sqrt(a+b*x)
        AEP_EXPR_1_OVER_X_SQRT_A_PLUS_B_X(512), // pattern 1/(x*sqrt(a+b*x))
        AEP_EXPR_1_OVER_X_TO_N_SQRT_A_PLUS_B_X(513), // pattern 1/(x**n*sqrt(a+b*x))
        
        AEP_EXPR_1_OVER_X_SQR_PLUS_A_SQR(514),  // pattern 1/(x**2+a**2)
        AEP_EXPR_1_OVER_X_SQR_MINUS_A_SQR(515), // pattern 1/(x**2-a**2)
        AEP_EXPR_1_OVER_MINUS_X_SQR_PLUS_A_SQR(516),    // pattern 1/(-x**2+a**2)
        AEP_EXPR_1_OVER_MINUS_X_SQR_MINUS_A_SQR(517),    // pattern 1/(-x**2-a**2)
        
        AEP_EXPR_1_OVER_A_X_SQR_PLUS_B(518),    // pattern 1/(a*x**2+b)
        
        AEP_EXPR_SQRT_A_SQR_PLUS_X_SQR(519),    // pattern sqrt(a**2+x**2)
        AEP_EXPR_SQRT_X_SQR_SQRT_A_SQR_PLUS_X_SQR(520),    // pattern x**2*sqrt(a**2+x**2)
        AEP_EXPR_SQRT_A_SQR_PLUS_X_SQR_OVER_X(521), // pattern sqrt(a**2+x**2)/x
        AEP_EXPR_SQRT_A_SQR_PLUS_X_SQR_OVER_X_SQR(522), // pattern sqrt(a**2+x**2)/x**2
        AEP_EXPR_1_OVER_SQRT_A_SQR_PLUS_X_SQR(523), // pattern 1/sqrt(a**2 + x**2)
        AEP_EXPR_X_SQR_OVER_SQRT_A_SQR_PLUS_X_SQR(524), // pattern x**2/sqrt(a**2 + x**2)
        AEP_EXPR_1_OVER_X_SQRT_A_SQR_PLUS_X_SQR(525), // pattern 1/(x*sqrt(a**2 + x**2))
        AEP_EXPR_1_OVER_X_SQR_SQRT_A_SQR_PLUS_X_SQR(526), // pattern 1/(x**2*sqrt(a**2 + x**2))
        
        AEP_EXPR_1_OVER_SQRT_X_SQR_MINUS_A_SQR(527),    // pattern 1/sqrt(x**2-a**2)
        
        AEP_EXPR_1_OVER_SQRT_A_SQR_MINUS_X_SQR(528),    // pattern 1/sqrt(a**2-x**2)
        AEP_EXPR_SQRT_A_SQR_MINUS_X_SQR(529),    // pattern sqrt(a**2-x**2)
        AEP_EXPR_X_SQR_SQRT_A_SQR_MINUS_X_SQR(530), // pattern x**2*sqrt(a**2-x**2)
        AEP_EXPR_SQRT_A_SQR_MINUS_X_SQR_OVER_X(531),    // pattern sqrt(a**2-x**2)/x
        AEP_EXPR_SQRT_A_SQR_MINUS_X_SQR_OVER_X_SQR(532),    // pattern sqrt(a**2-x**2)/x**2
        AEP_EXPR_1_OVER_X_SQRT_A_SQR_MINUS_X_SQR(533),    // pattern 1/x/sqrt(a**2-x**2)
        AEP_EXPR_X_SQR_OVER_SQRT_A_SQR_MINUS_X_SQR(534),    // pattern x**2/sqrt(a**2-x**2)
        AEP_EXPR_1_OVER_X_SQR_SQRT_A_SQR_MINUS_X_SQR(535),    // pattern 1/(x**2*sqrt(a**2-x**2))
        
        AEP_EXPR_1_OVER_SQRT_A_X_SQR_PLUS_B_X_PLUS_C(536),  // pattern 1/sqrt(a*x**2+b*x+c)
        AEP_EXPR_X_OVER_SQRT_A_X_SQR_PLUS_B_X_PLUS_C(537),  // pattern x/sqrt(a*x**2+b*x+c)
        //AEP_EXPR_1_OVER_SQRT_A_X_SQR_PLUS_B_X_PLUS_C_TO_N(538),  // pattern 1/sqrt(a*x**2+b*x+c)**n   // dont know how to calculate integrate
        //AEP_EXPR_X_OVER_SQRT_A_X_SQR_PLUS_B_X_PLUS_C_TO_N(539),  // pattern x/sqrt(a*x**2+b*x+c)**n   // dont know how to calculate integrate
        AEP_EXPR_1_OVER_X_SQRT_A_X_SQR_PLUS_B_X_PLUS_C(540),  // pattern 1/x/sqrt(a*x**2+b*x+c)
        
        AEP_EXPR_SIN_X(541),    // pattern sin(x)
        AEP_EXPR_SIN_X_SQR(542), // pattern sin(x)**2
        AEP_EXPR_SIN_X_N(543),  // pattern sin(x)**n
        
        AEP_EXPR_COS_X(544),    // pattern cos(x)
        AEP_EXPR_COS_X_SQR(545), // pattern cos(x)**2
        AEP_EXPR_COS_X_N(546),  // pattern cos(x)**n
        
        AEP_EXPR_TAN_X(547),    // pattern tan(x)
        AEP_EXPR_TAN_X_SQR(548), // pattern tan(x)**2
        AEP_EXPR_TAN_X_N(549),  // pattern tan(x)**n
        
        AEP_EXPR_COT_X(550),    // pattern 1/tan(x)
        AEP_EXPR_COT_X_SQR(551), // pattern 1/tan(x)**2
        AEP_EXPR_COT_X_N(552),  // pattern 1/tan(x)**n
        
        AEP_EXPR_CSC_X(553),    // pattern 1/sin(x)
        AEP_EXPR_CSC_X_SQR(554), // pattern 1/sin(x)**2
        AEP_EXPR_CSC_X_N(555),  // pattern 1/sin(x)**n
        AEP_EXPR_CSC_X_COT_X(556),  // pattern 1/(sin(x)*tan(x))
        
        AEP_EXPR_SEC_X(557),    // pattern 1/cos(x)
        AEP_EXPR_SEC_X_SQR(558), // pattern 1/cos(x)**2
        AEP_EXPR_SEC_X_N(559),  // pattern 1/cos(x)**n
        AEP_EXPR_SEC_X_TAN_X(560),  // pattern 1/cos(x)*tan(x)
        
        AEP_EXPR_ASIN_X(561),   // pattern asin(x)
        AEP_EXPR_ACOS_X(562),   // pattern acos(x)
        AEP_EXPR_ATAN_X(563),   // pattern atan(x)
 
        AEP_EXPR_E_TO_X(564),   // pattern e**x
        AEP_EXPR_A_TO_X(565),   // pattern a**x
        AEP_EXPR_E_TO_A_X_SQR(566),    // pattern e**(a*x**2)
        AEP_EXPR_E_TO_MINUS_A_X_SQR(567),    // pattern e**(-a*x**2)
        AEP_EXPR_X_E_TO_A_X(568),   // pattern x*e**(a*x)
        AEP_EXPR_X_TO_N_E_TO_A_X(569),   // pattern x**n*e**(a*x)
        AEP_EXPR_E_TO_A_X_SIN_B_X(570),   // pattern e**(a*x)*sin(b*x)
        AEP_EXPR_E_TO_A_X_COS_B_X(571),   // pattern e**(a*x)*cos(b*x)
        
        AEP_EXPR_LOG_X(573),    // pattern log(x)
        AEP_EXPR_X_TO_N_LOG_X(574), // pattern x**n*log(x)
        AEP_EXPR_1_OVER_X_LOG_X(575),   // pattern 1/(x*log(x))
        
        AEP_EXPR_SINH_X(576),   // pattern sinh(x)
        AEP_EXPR_COSH_X(577),   // pattern cosh(x)
        AEP_EXPR_TANH_X(578),   // pattern tanh(x)
        AEP_EXPR_SINH_X_SQR(579),   // pattern sinh(x)**2
        AEP_EXPR_COSH_X_SQR(580),   // pattern cosh(x)**2
        AEP_EXPR_TANH_X_SQR(581),   // pattern tanh(x)**2
        
        AEP_EXPR_CSCH_X(582),   // pattern 1/sinh(x)
        AEP_EXPR_SECH_X(583),   // pattern 1/cosh(x)
        AEP_EXPR_COTH_X(584),   // pattern 1/tanh(x)
        AEP_EXPR_CSCH_X_SQR(585),   // pattern 1/sinh(x)**2
        AEP_EXPR_SECH_X_SQR(586),   // pattern 1/cosh(x)**2
        AEP_EXPR_COTH_X_SQR(587),   // pattern 1/tanh(x)**2
        
        AEP_EXPR_DERI_SQRT_X(801),  // pattern for derivative of sqrt(x)
        AEP_EXPR_DERI_LOG_X(802),  // pattern for derivative of log(x)
        AEP_EXPR_DERI_SIN_X(803),  // pattern for derivative of sin(x)
        AEP_EXPR_DERI_COS_X(804),  // pattern for derivative of cos(x)
        AEP_EXPR_DERI_TAN_X(805),  // pattern for derivative of tan(x)
        AEP_EXPR_DERI_ASIN_X(806),  // pattern for derivative of asin(x)
        AEP_EXPR_DERI_ACOS_X(807),  // pattern for derivative of acos(x)
        AEP_EXPR_DERI_ATAN_X(808),  // pattern for derivative of atan(x)
        AEP_EXPR_DERI_SINH_X(809),  // pattern for derivative of sinh(x)
        AEP_EXPR_DERI_COSH_X(810),  // pattern for derivative of cosh(x)
        AEP_EXPR_DERI_TANH_X(811),  // pattern for derivative of tanh(x)
        AEP_EXPR_DERI_ASINH_X(812),  // pattern for derivative of asinh(x)
        AEP_EXPR_DERI_ACOSH_X(813),  // pattern for derivative of acosh(x)
        AEP_EXPR_DERI_ATANH_X(814),  // pattern for derivative of atanh(x)
        
        AEP_SINGLEVARINTPOW(1001),    // a * x**b + c
        AEP_SINGLEVARPOLYNORMIAL(1002),    // Polynomial with single variable (6-order at most)
        
        AEP_SINGLEVARPOSINTPOW(1101),   // a*x**b + c, b is a positive integer
        AEP_SINGLEVARNEGINTPOW(1102),   // a*x**b + c, b is a negative integer
        
        AEP_SINGLEVAR2ORDERPOLYNORMIAL(1103), // a*x**2+b*x+c, a is not zero
        AEP_SINGLEVAR3ORDERPOLYNORMIAL(1104), // a*x**3+b*x**2+c*x+d, a is not zero
        AEP_SINGLEVAR4ORDERPOLYNORMIAL(1105), // a*x**4+b*x**3+c*x**2+d*x+e, a is not zero
        AEP_SINGLEVAR5ORDERPOLYNORMIAL(1106), // a*x**5+b*x**4+c*x**3+d*x**2+e*x+f, a is not zero
        AEP_SINGLEVAR6ORDERPOLYNORMIAL(1107), // a*x**6+b*x**5+c*x**4+d*x*83+e*x**2+f*x+g, a is not zero
        
        
        //AEP_SINGLEVARPOWER(1003),    // a ** x + b
        //AEP_SIND(1004),    // sin(x) + a
        
        
        AEP_FIRSTORDER2VAREQUATIONS(10000),    // first order 2 variable equations
        AEP_FIRSTORDER3VAREQUATIONS(10001),    // first order 3 variable equations
        AEP_FIRSTORDER4VAREQUATIONS(10002),    // first order 4 variable equations
        AEP_FIRSTORDER5VAREQUATIONS(10003),    // first order 5 variable equations
        AEP_FIRSTORDER6VAREQUATIONS(10004);    // first order 6 variable equations
        private int value; 

        private ABSTRACTEXPRPATTERNS(int i) { 
            value = i; 
        } 

        public int getValue() { 
            return value; 
        } 
    };
    
        
    static public class PatternExprUnitMap {
        public PatternExprUnitMap() {}
        
        public PatternExprUnitMap(AbstractExpr aeExprUnit, AbstractExpr aePatternUnit) {
            maePatternUnit = aePatternUnit;
            maeExprUnit = aeExprUnit;
        }
        public AbstractExpr maePatternUnit = AEInvalid.AEINVALID;
        public AbstractExpr maeExprUnit = AEInvalid.AEINVALID;
    }
            
    
    // content of aeExpression will not be changed in this function
    public static AbstractExpr replaceExprPattern(AbstractExpr aeExpression, LinkedList<PatternExprUnitMap> listFromToMap,
            boolean bExpr2Pattern, ProgContext progContext) throws JFCALCExpErrException, JSmartMathErrException    {
        for (int idx = 0; idx < listFromToMap.size(); idx ++)    {
            if (bExpr2Pattern && aeExpression.isEqual(listFromToMap.get(idx).maeExprUnit, progContext))    {
                return listFromToMap.get(idx).maePatternUnit;
            } else if ((!bExpr2Pattern) && aeExpression.isEqual(listFromToMap.get(idx).maePatternUnit, progContext)) {
                return listFromToMap.get(idx).maeExprUnit;
            }
        }
        LinkedList<AbstractExpr> listReplacedChildren = new LinkedList<AbstractExpr>();
        aeExpression = aeExpression.replaceChildren(listFromToMap, bExpr2Pattern, listReplacedChildren, progContext);
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        listChildren.addAll(aeExpression.getListOfChildren());
        for (int idx = 0; idx < listChildren.size(); idx ++)    {
            boolean bIsReplacedChild = false;
            for (int idx1 = 0; idx1 < listReplacedChildren.size(); idx1 ++)    {
                if (listChildren.get(idx) == listReplacedChildren.get(idx1))    {
                    bIsReplacedChild = true;
                    break;
                }
            }
            if (bIsReplacedChild == false)    {
                // this child hasn't been replaced
                listChildren.set(idx, replaceExprPattern(listChildren.get(idx),listFromToMap, bExpr2Pattern, progContext));
            }
        }
        aeExpression = aeExpression.copySetListOfChildren(listChildren);
        return aeExpression;
    }

    public LinkedList<PtnSlvVarIdentifier> mlistSimpleVar2SlvPatterns = new LinkedList<PtnSlvVarIdentifier>();
    public LinkedList<Ptn1VarIntegIdentifier> mlist1VarIntegPatterns = new LinkedList<Ptn1VarIntegIdentifier>();
    public LinkedList<Ptn1VarDeriIdentifier> mlist1VarDeriPatterns = new LinkedList<Ptn1VarDeriIdentifier>();
    public LinkedList<PtnSlvVarMultiRootsIdentifier> mlistMultiRoot2SlvPatterns = new LinkedList<PtnSlvVarMultiRootsIdentifier>();
    public LinkedList<PtnSlvMultiVarsIdentifier> mlistSingleVar2SlvPatterns = new LinkedList<PtnSlvMultiVarsIdentifier>();
    public LinkedList<PtnSlvMultiVarsIdentifier> mlistMultiVar2SlvPatterns = new LinkedList<PtnSlvMultiVarsIdentifier>();
    
    
    public void loadPatterns(int nFilter) throws JSmartMathErrException, JFCALCExpErrException, InterruptedException    {
        if ((nFilter & 1) == 1) {
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createZeroPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createAddPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createSubPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createDivAddPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createDivPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createDivSubPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createDivedAddPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createDivedPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createDivedSubPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createInvFuncAddPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createInvFuncPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createInvFuncSubPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createLeftDivAddPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createLeftDivPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createLeftDivSubPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createLeftDivedAddPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createLeftDivedPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createLeftDivedSubPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createMulAddPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createMulPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createMulSubPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createMuledAddPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createMuledPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createMuledSubPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createNegAddPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createNegPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createNegSubPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createPercentAddPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createPercentPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createPercentSubPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createPowerAddPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createPowerPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createPowerSubPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createPoweredAddPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createPoweredPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createPoweredSubPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createTransposeAddPatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createTransposePatternIdentifer());
            mlistSimpleVar2SlvPatterns.add(PtnSlvVarIdentifierMgr.createTransposeSubPatternIdentifer());
        }
        
        if ((nFilter & 2) == 2) {
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverAPlusBXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createXOverAPlusBXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createXSqrOverAPlusBXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverXAPlusBXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverXSqrAPlusBXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSqrtAPlusBXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createXSqrtAPlusBXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createXToNSqrtAPlusBXPatternIdentifier()); // integrate answer is integrate
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSqrtAPlusBXOverXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSqrtAPlusBXOverXToNPatternIdentifier());    // integrate answer is integrate
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverSqrtAPlusBXPatternIdentifier());    // integrate answer is integrate
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverXSqrtAPlusBXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverXToNSqrtAPlusBXPatternIdentifier());    // integrate answer is integrate
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverXSqrPlusASqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverXSqrMinusASqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverMinusXSqrPlusASqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverMinusXSqrMinusASqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverAXSqrPlusBPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSqrtASqrPlusXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSqrtXSqrSqrtASqrPlusXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSqrtASqrPlusXSqrOverXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSqrtASqrPlusXSqrOverXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverSqrtASqrPlusXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createXSqrOverSqrtASqrPlusXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverXSqrtASqrPlusXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverXSqrSqrtASqrPlusXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverSqrtXSqrMinusASqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverSqrtASqrMinusXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSqrtASqrMinusXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createXSqrSqrtASqrMinusXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSqrtASqrMinusXSqrOverXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSqrtASqrMinusXSqrOverXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverXSqrtASqrMinusXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createXSqrOverSqrtASqrMinusXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverXSqrSqrtASqrMinusXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverSqrtAXSqrPlusBXPlusCPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createXOverSqrtAXSqrPlusBXPlusCPatternIdentifier());
            //mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverSqrtAXSqrPlusBXPlusCToNPatternIdentifier());    // pattern hasn't been constructed yet
            //mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createXOverSqrtAXSqrPlusBXPlusCToNPatternIdentifier());    // pattern hasn't been constructed yet
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverXSqrtAXSqrPlusBXPlusCPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSinXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSinXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSinXNPatternIdentifier());    // integrate answer is integrate
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCosXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCosXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCosXNPatternIdentifier());    // integrate answer is integrate
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createTanXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createTanXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createTanXNPatternIdentifier());    // integrate answer is integrate
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCotXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCotXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCotXNPatternIdentifier());    // integrate answer is integrate
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCscXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCscXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCscXNPatternIdentifier());    // integrate answer is integrate
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCscXCotXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSecXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSecXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSecXNPatternIdentifier());    // integrate answer is integrate
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSecXTanXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createAsinXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createAcosXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createAtanXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createEToXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createAToXPatternIdentifier());
            /*mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createEToAXSqrPatternIdentifier());*/    // definite integrate
            /*mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createEToMinusAXSqrPatternIdentifier());*/    // definite integrate
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createXEToAXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createXToNEToAXPatternIdentifier());    // integrate answer is integrate
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createEToAXSinBXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createEToAXCosBXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createLogXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createXToNLogXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.create1OverXLogXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSinhXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCoshXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createTanhXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSinhXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCoshXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createTanhXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCschXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSechXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCothXPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCschXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createSechXSqrPatternIdentifier());
            mlist1VarIntegPatterns.add(Ptn1VarIntegIdentifierMgr.createCothXSqrPatternIdentifier());
            
            // now add all derivative patterns:
            mlist1VarDeriPatterns.add(Ptn1VarDeriIdentifierMgr.createSqrtXPatternIdentifier());
            mlist1VarDeriPatterns.add(Ptn1VarDeriIdentifierMgr.createLogXPatternIdentifier());
            mlist1VarDeriPatterns.add(Ptn1VarDeriIdentifierMgr.createSinXPatternIdentifier());
            mlist1VarDeriPatterns.add(Ptn1VarDeriIdentifierMgr.createCosXPatternIdentifier());
            mlist1VarDeriPatterns.add(Ptn1VarDeriIdentifierMgr.createTanXPatternIdentifier());
            mlist1VarDeriPatterns.add(Ptn1VarDeriIdentifierMgr.createASinXPatternIdentifier());
            mlist1VarDeriPatterns.add(Ptn1VarDeriIdentifierMgr.createACosXPatternIdentifier());
            mlist1VarDeriPatterns.add(Ptn1VarDeriIdentifierMgr.createATanXPatternIdentifier());
            mlist1VarDeriPatterns.add(Ptn1VarDeriIdentifierMgr.createSinhXPatternIdentifier());
            mlist1VarDeriPatterns.add(Ptn1VarDeriIdentifierMgr.createCoshXPatternIdentifier());
            mlist1VarDeriPatterns.add(Ptn1VarDeriIdentifierMgr.createTanhXPatternIdentifier());
            mlist1VarDeriPatterns.add(Ptn1VarDeriIdentifierMgr.createASinhXPatternIdentifier());
            mlist1VarDeriPatterns.add(Ptn1VarDeriIdentifierMgr.createACoshXPatternIdentifier());
            mlist1VarDeriPatterns.add(Ptn1VarDeriIdentifierMgr.createATanhXPatternIdentifier());
        }

        if ((nFilter & 4) == 4) {
            PtnSlvMultiVarsIdentifier paIntPow = PtnSlvMultiVarsIdentifierMgr.createIntPowPtnSlvMultiVarsIdentifier();
            PtnSlvMultiVarsIdentifier paPolynomail = PtnSlvMultiVarsIdentifierMgr.createPolynomialPtnSlvMultiVarsIdentifier();

            mlistSingleVar2SlvPatterns.add(paIntPow);
            mlistSingleVar2SlvPatterns.add(paPolynomail);
            
            PtnSlvVarMultiRootsIdentifier paPosIntPow = PtnSlvVarMultiRootsIdentifierMgr.createPosIntPowPtnSlvVarMultiRootsIdentifier();
            PtnSlvVarMultiRootsIdentifier paNegIntPow = PtnSlvVarMultiRootsIdentifierMgr.createNegIntPowPtnSlvVarMultiRootsIdentifier();
            PtnSlvVarMultiRootsIdentifier pa3OrderPolynomail = PtnSlvVarMultiRootsIdentifierMgr.create3OrderPolynomialPtnSlvVarMultiRootsIdentifier();
            //PtnSlvVarMultiRootsIdentifier pa6OrderPolynomail = PtnSlvVarMultiRootsIdentifierMgr.create6OrderPolynomialPtnSlvVarMultiRootsIdentifier();
            
            mlistMultiRoot2SlvPatterns.add(paPosIntPow);
            mlistMultiRoot2SlvPatterns.add(paNegIntPow);
            mlistMultiRoot2SlvPatterns.add(pa3OrderPolynomail);
        }
        
        if ((nFilter & 8) == 8) {
            PtnSlvMultiVarsIdentifier pa1stOrderEqs2Vars = PtnSlvMultiVarsIdentifierMgr.create1stOrderEqs2VarsPtnSlvMultiVarsIdentifier();
            PtnSlvMultiVarsIdentifier pa1stOrderEqs3Vars = PtnSlvMultiVarsIdentifierMgr.create1stOrderEqs3VarsPtnSlvMultiVarsIdentifier();
            PtnSlvMultiVarsIdentifier pa1stOrderEqs4Vars = PtnSlvMultiVarsIdentifierMgr.create1stOrderEqs4VarsPtnSlvMultiVarsIdentifier();
            PtnSlvMultiVarsIdentifier pa1stOrderEqs5Vars = PtnSlvMultiVarsIdentifierMgr.create1stOrderEqs5VarsPtnSlvMultiVarsIdentifier();
            PtnSlvMultiVarsIdentifier pa1stOrderEqs6Vars = PtnSlvMultiVarsIdentifierMgr.create1stOrderEqs6VarsPtnSlvMultiVarsIdentifier();

            mlistMultiVar2SlvPatterns.add(pa1stOrderEqs2Vars);
            mlistMultiVar2SlvPatterns.add(pa1stOrderEqs3Vars);
            mlistMultiVar2SlvPatterns.add(pa1stOrderEqs4Vars);
            mlistMultiVar2SlvPatterns.add(pa1stOrderEqs5Vars);
            mlistMultiVar2SlvPatterns.add(pa1stOrderEqs6Vars);
        }
    }
    
    public AbstractExpr solveVarByPtnSlvVarIdentifier(AbstractExpr aeExpr2Solve,  //aeExpr2Solve will not be changed in this function
                                                UnknownVariable varSolved,    // this parameter returns solved variable and value
                                                LinkedList<UnknownVariable> listUnknown,
                                                ProgContext progContext)
                            throws JFCALCExpErrException, InterruptedException, JSmartMathErrException    {
        LinkedList<AbstractExpr> listSimplifyRecord = new LinkedList<AbstractExpr>();
        listSimplifyRecord.add(aeExpr2Solve);
        AbstractExpr aeSimplified = aeExpr2Solve;
        do {
            boolean bMatch = false;
            for (int idx = 0; idx < mlistSimpleVar2SlvPatterns.size(); idx ++)  {
                PatternExprUnitMap peuMap = new PatternExprUnitMap();
                AEConst aeValueOfUnknown = new AEConst();
                try {
                    DataClass[] datumarrayValueOfUnknown = new DataClass[] {new DataClassNull()};
                    bMatch = mlistSimpleVar2SlvPatterns.get(idx).isPatternMatch(aeSimplified, peuMap, datumarrayValueOfUnknown, listUnknown, progContext);
                    aeValueOfUnknown = new AEConst(datumarrayValueOfUnknown[0]);
                } catch (JSmartMathErrException e)  {
                    if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_UNSUPPORTED_SIMPLE_PATTERN
                            && e.m_se.m_enumErrorType != ERRORTYPES.ERROR_INVALID_SIMPLE_PATTERN)   {
                        throw e;
                    } else  {
                        bMatch = false;
                    }
                }
                if (bMatch) {
                    LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                    listChildren.add(peuMap.maeExprUnit);
                    listChildren.add(aeValueOfUnknown);
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
                    aeSimplified = new AEPosNegOpt(listChildren, listOpts).simplifyAExprMost(listUnknown, new SimplifyParams(false, false, false), progContext);
                    if (peuMap.maeExprUnit instanceof AEVar)    {
                        String strUnknownName = ((AEVar)peuMap.maeExprUnit).mstrVariableName;
                        varSolved.setVariable(strUnknownName, aeValueOfUnknown.getDataClass()); // getDataClass will determine when to deep copy.
                        UnknownVariable var = UnknownVarOperator.lookUpList(strUnknownName, listUnknown);
                        if (var != null)    {
                            var.setValue(aeValueOfUnknown.getDataClass());
                        }
                        return aeSimplified;
                    }
                }
            }
            for (int idx = 0; idx < listSimplifyRecord.size(); idx ++)  {
                if (aeSimplified.isEqual(listSimplifyRecord.get(idx), progContext))  {
                    // this means we cannot further simplify it.
                    varSolved.setName("");
                    varSolved.setValueAssigned(false);
                    return aeSimplified;
                }
            }
            listSimplifyRecord.add(aeSimplified);
        } while (true);
    }
    
    // return expressions need to be solved.
    public AbstractExpr[] simplifyByPtnSlvVarIdentifier(AbstractExpr[] aeOriginalExprs,   // individual aexpr will not be changed in this function.
                                                        LinkedList<UnknownVariable> listUnknown,                                    
                                                        ProgContext progContext)
                            throws InterruptedException, JFCALCExpErrException, JSmartMathErrException    {
        AbstractExpr[] aeProcessedExprs = new AbstractExpr[aeOriginalExprs.length];
        boolean[] bSingleVarSolveds = new boolean[aeOriginalExprs.length];
        int nNumofUnsolvedExprs = aeOriginalExprs.length;
        for (int idx = 0; idx < aeOriginalExprs.length; idx ++) {
            bSingleVarSolveds[idx] = false;
            aeProcessedExprs[idx] = aeOriginalExprs[idx].simplifyAExprMost(listUnknown, new SimplifyParams(false, true, false), progContext);
        }
        LinkedList<AbstractExpr[]> listExprsSimplifyRecord = new LinkedList<AbstractExpr[]>();
        listExprsSimplifyRecord.add(aeProcessedExprs);
        do {
            for (int idx = 0; idx < aeProcessedExprs.length; idx ++) {
                if (bSingleVarSolveds[idx]) {
                    continue;
                }
                UnknownVariable varSolved = new UnknownVariable();
                aeProcessedExprs[idx] = solveVarByPtnSlvVarIdentifier(aeProcessedExprs[idx], varSolved, listUnknown, progContext);
                if (varSolved.isValueAssigned())    {
                    bSingleVarSolveds[idx] = true;
                    nNumofUnsolvedExprs --;
                }
            }
            if (nNumofUnsolvedExprs == 0)   {   // all the expressions have been solved.
                return new AbstractExpr[0];
            } else  {
                for (int idx = 0; idx < listExprsSimplifyRecord.size(); idx ++) {
                    boolean bAllEqual = true;
                    for (int idx1 = 0; idx1 < aeProcessedExprs.length; idx1 ++) {
                        if (aeProcessedExprs[idx1].isEqual(listExprsSimplifyRecord.get(idx)[idx1], progContext) == false)    {
                            bAllEqual = false;
                            break;
                        }
                    }
                    if (bAllEqual)  {
                        // the expressions cannot be further simplified.
                        AbstractExpr[] aearrayReturn = new AbstractExpr[nNumofUnsolvedExprs];
                        for (int idx1 = 0, idx2 = 0; idx1 < aeProcessedExprs.length; idx1 ++) {
                            if (bSingleVarSolveds[idx1] == false)   {
                                aearrayReturn[idx2] = aeProcessedExprs[idx1];
                                idx2 ++;
                            }
                        }
                        return aearrayReturn;
                    }
                }
                // ok, the expressions can be further simplified
                AbstractExpr[] aeProcessedExprsCpy = new AbstractExpr[aeProcessedExprs.length];
                System.arraycopy(aeProcessedExprs, 0, aeProcessedExprsCpy, 0, aeProcessedExprs.length);
                listExprsSimplifyRecord.add(aeProcessedExprsCpy);
            }
        } while (true);
        
    }
            
    
    public PtnSlvVarMultiRootsIdentifier findPtnSlvVarMultiRootsIdentifierToMatch(AbstractExpr aeOriginalExpr,
                                                LinkedList<PatternExprUnitMap> listPEUMap,    // transfer a map which includes last list of mapped variable-units and will returns a list of mapped variable-units
                                                LinkedList<UnknownVariable> listPseudoConstVars,    // this list will return the pseudo constant variables in the Pattern identifier.
                                                LinkedList<UnknownVariable> listUnknown,
                                                ProgContext progContext)
                            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
        LinkedList<PtnSlvVarMultiRootsIdentifier> listAllPatterns = new LinkedList<PtnSlvVarMultiRootsIdentifier>();
        listAllPatterns.addAll(mlistMultiRoot2SlvPatterns);
        
        LinkedList<AbstractExpr> listOriginalExprVars = new LinkedList<AbstractExpr>();
        do    {
            LinkedList<AbstractExpr> listOldOriginalExprVars = listOriginalExprVars;
            listOriginalExprVars = new LinkedList<AbstractExpr>();
            PtnSlvMultiVarsIdentifier.lookupToSolveVariables(aeOriginalExpr, listOriginalExprVars, listOldOriginalExprVars, progContext);
            AbstractExpr[] aeOriginalExprs = new AbstractExpr[1];
            aeOriginalExprs[0] = aeOriginalExpr;
            if (listOriginalExprVars.size() == 0)    {
                break;    // no variable found or more than 1 variables found
            } else if (AbstractExpr.isExprsEqual(aeOriginalExprs, listOriginalExprVars.toArray(new AbstractExpr[0]), progContext))    {
                break;  // this means the "variables" we found are actually aeOriginalExprs, ie, we cannot simplify aeOriginalExprs after apply pattern.
            } else if (listOldOriginalExprVars.size() != 0)  {
                // we have done at list one testing round but cannot find the pattern, now start the 2nd or 3rd or ... round.
                boolean bAllAEVar = true;
                for (int idx = 0; idx < listOriginalExprVars.size(); idx ++)    {
                    if (!(listOriginalExprVars.get(idx) instanceof AEVar))  {
                        bAllAEVar = false;
                        break;
                    }
                }
                if (bAllAEVar)  {
                    // the case that all unknowns are AEVar has been tested in the very beginning so that should not be retested.
                    break;
                } else if (listOriginalExprVars.size() == listOldOriginalExprVars.size())    {
                    // check if the variables are actually the same as last pattern
                    boolean bAllTheSame = true;
                    for (int idx = 0; idx < listOriginalExprVars.size(); idx ++)    {
                        boolean bFoundThisVar = false;
                        for (int idx1 = 0; idx1 < listOldOriginalExprVars.size(); idx1 ++)    {
                            if (listOriginalExprVars.get(idx).isEqual(listOldOriginalExprVars.get(idx1), progContext))    {
                                bFoundThisVar = true;
                                break;
                            }
                        }
                        if (bFoundThisVar == false)    {
                            bAllTheSame = false;
                            break;
                        }
                    }
                    if (bAllTheSame)    {
                        break;    // cannot be further patterned.
                    }
                } else if ( listOriginalExprVars.size() > 1) {
                    // we find more than one variables, should not be tested so look up their parents.
                    continue;
                }
            }
            for (int idx = 0; idx <listAllPatterns.size(); idx ++)    {
                listPseudoConstVars.clear();    // psedo const list should be empty at this moment.
                // unknown list should be deep-copied and save the original copy.
                LinkedList<UnknownVariable> listUnknownCpy = new LinkedList<UnknownVariable>();
                for (int idx1 = 0; idx1 < listUnknown.size(); idx1 ++)    {
                    UnknownVariable var = new UnknownVariable(listUnknown.get(idx1).getName());
                    if (listUnknown.get(idx1).isValueAssigned())    {
                        DataClass datumValue = listUnknown.get(idx1).getSolvedValue().cloneSelf();
                        var.setValue(datumValue);
                    }
                    listUnknownCpy.add(var);
                }
                // during pattern matching, some variable values may change.
                LinkedList<LinkedList<Variable>> lVarNameSpacesCpy = new LinkedList<LinkedList<Variable>>();
                for (int idx1 = 0; idx1 < progContext.mdynamicProgContext.mlVarNameSpaces.size(); idx1 ++)    {
                    LinkedList<Variable> lVarListCpy = new LinkedList<Variable>();
                    for (int idx2 = 0; idx2 < progContext.mdynamicProgContext.mlVarNameSpaces.get(idx1).size(); idx2 ++)    {
                        Variable var = new Variable(progContext.mdynamicProgContext.mlVarNameSpaces.get(idx1).get(idx2).getName());
                        DataClass datumValue = progContext.mdynamicProgContext.mlVarNameSpaces.get(idx1).get(idx2).getValue().cloneSelf();
                        var.setValue(datumValue);
                        lVarListCpy.add(var);
                    }
                    lVarNameSpacesCpy.add(lVarListCpy);
                }
                
                listPEUMap.clear();
                
                ProgContext progContextCpy = new ProgContext();
                progContextCpy.mdynamicProgContext.mlVarNameSpaces = lVarNameSpacesCpy;
                progContextCpy.mstaticProgContext.setCitingSpacesExplicitly(progContext.mstaticProgContext.getCitingSpaces());
                boolean bMatch = listAllPatterns.get(idx).isPatternMatch(aeOriginalExpr,
                                                                        listOriginalExprVars,
                                                                        listPEUMap,
                                                                        listPseudoConstVars,
                                                                        listUnknownCpy,
                                                                        progContextCpy);
                if (bMatch)    {
                    return listAllPatterns.get(idx);
                }
            }
        } while (true);
        return new PtnSlvVarMultiRootsIdentifier();    // unrecognized pattern
    }
        
    public PtnSlvMultiVarsIdentifier findPtnSlvMultiVarsIdentifierToMatch(AbstractExpr[] aeOriginalExprs,
                                                LinkedList<PatternExprUnitMap> listPEUMap,    // transfer a map which includes last list of mapped variable-units and will returns a list of mapped variable-units
                                                LinkedList<UnknownVariable> listPseudoConstVars,    // this list will return the pseudo constant variables in the Pattern identifier.
                                                LinkedList<UnknownVariable> listUnknown,
                                                ProgContext progContext)
                            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
        LinkedList<PtnSlvMultiVarsIdentifier> listAllPatterns = new LinkedList<PtnSlvMultiVarsIdentifier>();
        listAllPatterns.addAll(mlistSingleVar2SlvPatterns);
        listAllPatterns.addAll(mlistMultiVar2SlvPatterns);
        
        LinkedList<AbstractExpr> listOriginalExprVars = new LinkedList<AbstractExpr>();
        do    {
            LinkedList<AbstractExpr> listOldOriginalExprVars = listOriginalExprVars;
            listOriginalExprVars = new LinkedList<AbstractExpr>();
            PtnSlvMultiVarsIdentifier.lookupToSolveVarsInExprs(aeOriginalExprs, listOriginalExprVars, listOldOriginalExprVars, progContext);
            if (listOriginalExprVars.size() == 0)    {
                break;    // no variable found.
            } else if (AbstractExpr.isExprsEqual(aeOriginalExprs, listOriginalExprVars.toArray(new AbstractExpr[0]), progContext))    {
                break;  // this means the "variables" we found are actually aeOriginalExprs, ie, we cannot simplify aeOriginalExprs after apply pattern.
            } else if (listOldOriginalExprVars.size() != 0)  {
                // we have done at list one testing round but cannot find the pattern, now start the 2nd or 3rd or ... round.
                boolean bAllAEVar = true;
                for (int idx = 0; idx < listOriginalExprVars.size(); idx ++)    {
                    if (!(listOriginalExprVars.get(idx) instanceof AEVar))  {
                        bAllAEVar = false;
                        break;
                    }
                }
                if (bAllAEVar)  {
                    // the case that all unknowns are AEVar has been tested in the very beginning so that should not be retested.
                    break;
                } else if (listOriginalExprVars.size() == listOldOriginalExprVars.size())    {
                    // check if the variables are actually the same as last pattern
                    boolean bAllTheSame = true;
                    for (int idx = 0; idx < listOriginalExprVars.size(); idx ++)    {
                        boolean bFoundThisVar = false;
                        for (int idx1 = 0; idx1 < listOldOriginalExprVars.size(); idx1 ++)    {
                            if (listOriginalExprVars.get(idx).isEqual(listOldOriginalExprVars.get(idx1), progContext))    {
                                bFoundThisVar = true;
                                break;
                            }
                        }
                        if (bFoundThisVar == false)    {
                            bAllTheSame = false;
                            break;
                        }
                    }
                    if (bAllTheSame)    {
                        break;    // cannot be further patterned.
                    }
                } else if ( listOriginalExprVars.size() > 1) {
                    // we find more than one variables, should not be tested so look up their parents.
                    continue;
                }
            } 
            for (int idx = 0; idx <listAllPatterns.size(); idx ++)    {
                listPseudoConstVars.clear();    // psedo const list should be empty at this moment.
                // unknown list should be deep-copied and save the original copy.
                LinkedList<UnknownVariable> listUnknownCpy = new LinkedList<UnknownVariable>();
                for (int idx1 = 0; idx1 < listUnknown.size(); idx1 ++)    {
                    UnknownVariable var = new UnknownVariable(listUnknown.get(idx1).getName());
                    if (listUnknown.get(idx1).isValueAssigned())    {
                        DataClass datumValue = listUnknown.get(idx1).getSolvedValue().cloneSelf();
                        var.setValue(datumValue);
                    }
                    listUnknownCpy.add(var);
                }
                // during pattern matching, some variable values may change.
                LinkedList<LinkedList<Variable>> lVarNameSpacesCpy = new LinkedList<LinkedList<Variable>>();
                for (int idx1 = 0; idx1 < progContext.mdynamicProgContext.mlVarNameSpaces.size(); idx1 ++)    {
                    LinkedList<Variable> lVarListCpy = new LinkedList<Variable>();
                    for (int idx2 = 0; idx2 < progContext.mdynamicProgContext.mlVarNameSpaces.get(idx1).size(); idx2 ++)    {
                        Variable var = new Variable(progContext.mdynamicProgContext.mlVarNameSpaces.get(idx1).get(idx2).getName());
                        DataClass datumValue = progContext.mdynamicProgContext.mlVarNameSpaces.get(idx1).get(idx2).getValue().cloneSelf();
                        var.setValue(datumValue);
                        lVarListCpy.add(var);
                    }
                    lVarNameSpacesCpy.add(lVarListCpy);
                }
                
                listPEUMap.clear();
                ProgContext progContextTmp = new ProgContext();
                progContextTmp.mdynamicProgContext.mlVarNameSpaces = lVarNameSpacesCpy;
                progContextTmp.mstaticProgContext.setCitingSpacesExplicitly(progContext.mstaticProgContext.getCitingSpaces());
                boolean bMatch = listAllPatterns.get(idx).isPatternMatch(aeOriginalExprs,
                                                                        listOriginalExprVars,
                                                                        listPEUMap,
                                                                        listPseudoConstVars,
                                                                        listUnknownCpy,
                                                                        progContextTmp);
                if (bMatch)    {
                    return listAllPatterns.get(idx);
                }
            }
        } while (true);
        return new PtnSlvMultiVarsIdentifier();    // unrecognized pattern
    }
    
    // this function will not change aeExpr2Integ. Its return will not include any part of aeExpr2Integ.
    public AbstractExpr integInDefByPtn1VarIntegIdentifier(AbstractExpr aeExpr2Integ,   // it has been simplified most. so need not to do it in the function
                                                        LinkedList<UnknownVariable> listUnknown,
                                                        ProgContext progContext)
                            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
        // This function only support indefinite integrate
        
        // during integration, some variable values may change. However, for integration, variable
        // should change to fixed value no matter how many integration steps we have. Moreover, the
        // variable value change should not affect following calculation. Otherwise, integration
        // result would not be indefinite.
        
        // do not do namespace copy here. If a variable's value changes, let it be.
        
        if (listUnknown.size() != 1)  {   // high order indefinite integration is still not supported.
            throw new JSmartMathErrException(ERRORTYPES.ERROR_UNSUPPORTED_INTEGRATION_TYPE);
        }
        
        String strVarName = listUnknown.getFirst().getName();
                
        if (aeExpr2Integ instanceof AEConst) {  // integrate a constant.
            AEVar aeUnknown = new AEVar(strVarName, ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE);
            DataClass datumExpr2Integ = DCHelper.lightCvtOrRetDCComplex(((AEConst)aeExpr2Integ).getDataClassRef());
            AbstractExpr aeExpr2IntegCpy = new AEConst(datumExpr2Integ);  // constant must be a single value.
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            listChildren.add(aeExpr2IntegCpy);
            listChildren.add(aeUnknown);
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            AEMulDivOpt aeReturn = new AEMulDivOpt(listChildren, listOpts);
            return aeReturn;
        } else if ((aeExpr2Integ instanceof AEVar) && ((AEVar)aeExpr2Integ).mstrVariableName.equals(strVarName)) {  // integrate x. need not to worry about -x because -x is a AEPosNegOpt.
            AEConst aeTwo = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TWO));
            AEConst aeHalf = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.HALF));
            AEPowerOpt aeXTo2 = new AEPowerOpt(aeExpr2Integ, aeTwo);
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            listChildren.add(aeHalf);
            listChildren.add(aeXTo2);
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            AEMulDivOpt aeReturn = new AEMulDivOpt(listChildren, listOpts);
            return aeReturn;
        } else if ((aeExpr2Integ instanceof AEPowerOpt)
                && (((AEPowerOpt)aeExpr2Integ).maeLeft instanceof AEVar)
                && (((AEPowerOpt)aeExpr2Integ).maeRight instanceof AEConst)
                && ((AEVar)((AEPowerOpt)aeExpr2Integ).maeLeft).mstrVariableName.equals(strVarName)) { // integrate x**a
            AEVar aePowerBase = (AEVar)((AEPowerOpt)aeExpr2Integ).maeLeft;
            AEConst aePowerTo = (AEConst)((AEPowerOpt)aeExpr2Integ).maeRight;
            DataClassSingleNum datumPowerTo = DCHelper.lightCvtOrRetDCMFPDec(aePowerTo.getDataClassRef());    // power to (a) must be a real value, otherwise throw exception.
            // now datumPowerTo must be a DataClassSingleNum
            if (datumPowerTo.getDataValue().isEqual(MFPNumeric.MINUS_ONE)) {
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                listChildren.add(aePowerBase);
                String strLogNameWithCs = FunctionNameMapper.msmapSysFunc2FullCSMap.get("log");
                if (strLogNameWithCs == null) {
                    strLogNameWithCs = "log";
                }
                AEFunction aeReturn = new AEFunction(strLogNameWithCs, listChildren, progContext);
                return aeReturn;
            } else {
                DataClass datumOne = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                DataClass datumIntegrated = BuiltinProcedures.evaluateAdding(datumPowerTo, datumOne);
                DataClass datum1OverInteg = BuiltinProcedures.evaluateDivision(datumOne, datumIntegrated);
                AEConst aeIntegrated = new AEConst(datumIntegrated);
                AEConst ae1OverInteg = new AEConst(datum1OverInteg);
                AEPowerOpt aeXToInteg = new AEPowerOpt(aePowerBase, aeIntegrated);
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                listChildren.add(ae1OverInteg);
                listChildren.add(aeXToInteg);
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                AEMulDivOpt aeReturn = new AEMulDivOpt(listChildren, listOpts);
                return aeReturn;
            }
        } else if (aeExpr2Integ instanceof AEMulDivOpt && ((AEMulDivOpt)aeExpr2Integ).mlistChildren.size() == 2
                && ((AEMulDivOpt)aeExpr2Integ).mlistChildren.getFirst() instanceof AEConst
                && ((AEMulDivOpt)aeExpr2Integ).mlistChildren.getLast() instanceof AEVar
                && ((AEMulDivOpt)aeExpr2Integ).mlistOpts.getLast().getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE   // must be divide
                && ((AEVar)((AEMulDivOpt)aeExpr2Integ).mlistChildren.getLast()).mstrVariableName.equals(strVarName)) {    // integrate a/x
            AEConst aeCoeff = (AEConst)((AEMulDivOpt)aeExpr2Integ).mlistChildren.getFirst();
            DataClass datumCoeff = DCHelper.lightCvtOrRetDCComplex(aeCoeff.getDataClassRef()); // a has to be a single value.
            aeCoeff = new AEConst(datumCoeff);
            AEVar aeUnknown = (AEVar)((AEMulDivOpt)aeExpr2Integ).mlistChildren.getLast();
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            listChildren.add(aeUnknown);
            String strLogNameWithCs = FunctionNameMapper.msmapSysFunc2FullCSMap.get("log");
            if (strLogNameWithCs == null) {
                strLogNameWithCs = "log";
            }
            AEFunction aeLogX = new AEFunction(strLogNameWithCs, listChildren, progContext);
            listChildren = new LinkedList<AbstractExpr>();
            listChildren.add(aeCoeff);
            listChildren.add(aeLogX);
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.add(new CalculateOperator(((AEMulDivOpt)aeExpr2Integ).mlistOpts.getFirst().getOperatorType(), 2));
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            AEMulDivOpt aeReturn = new AEMulDivOpt(listChildren, listOpts);
            return aeReturn;
        } else if (aeExpr2Integ instanceof AEMulDivOpt && ((AEMulDivOpt)aeExpr2Integ).mlistChildren.size() == 2
                && ((AEMulDivOpt)aeExpr2Integ).mlistChildren.getFirst() instanceof AEConst
                && ((AEMulDivOpt)aeExpr2Integ).mlistChildren.getLast() instanceof AEPowerOpt
                && ((AEPowerOpt)((AEMulDivOpt)aeExpr2Integ).mlistChildren.getLast()).maeLeft instanceof AEVar
                && ((AEPowerOpt)((AEMulDivOpt)aeExpr2Integ).mlistChildren.getLast()).maeRight instanceof AEConst
                && ((AEVar)((AEPowerOpt)((AEMulDivOpt)aeExpr2Integ).mlistChildren.getLast()).maeLeft).mstrVariableName.equals(strVarName)
                ) {    // integrate a/x**b
            AEConst aeCoeff = (AEConst)((AEMulDivOpt)aeExpr2Integ).mlistChildren.getFirst();
            DataClass datumCoeff = DCHelper.lightCvtOrRetDCComplex(aeCoeff.getDataClassRef());
            aeCoeff = new AEConst(datumCoeff); // a has to be a single value.
            AEVar aeUnknown = (AEVar)((AEPowerOpt)((AEMulDivOpt)aeExpr2Integ).mlistChildren.getLast()).maeLeft;
            AEConst aePowerTo = (AEConst)((AEPowerOpt)((AEMulDivOpt)aeExpr2Integ).mlistChildren.getLast()).maeRight;
            DataClass datumPowerTo = DCHelper.lightCvtOrRetDCMFPDec(aePowerTo.getDataClassRef()); // b has to be a real single value.
            if (((AEMulDivOpt)aeExpr2Integ).mlistOpts.getLast().getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE) {
                datumPowerTo = BuiltinProcedures.evaluateNegSign(datumPowerTo);
            }
            // datumPowerTo must be a DataClassBuiltIn coz it is returned from convertData2NewType
            if (DCHelper.lightCvtOrRetDCSingleNum(datumPowerTo).getDataValue().isEqual(MFPNumeric.MINUS_ONE)) { // a/x
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                listChildren.add(aeUnknown);
                String strLogNameWithCs = FunctionNameMapper.msmapSysFunc2FullCSMap.get("log");
                if (strLogNameWithCs == null) {
                    strLogNameWithCs = "log";
                }
                AEFunction aeLogX = new AEFunction(strLogNameWithCs, listChildren, progContext);
                listChildren = new LinkedList<AbstractExpr>();
                listChildren.add(aeCoeff);
                listChildren.add(aeLogX);
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                listOpts.add(new CalculateOperator(((AEMulDivOpt)aeExpr2Integ).mlistOpts.getFirst().getOperatorType(), 2));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                AEMulDivOpt aeReturn = new AEMulDivOpt(listChildren, listOpts);
                return aeReturn;
            } else {
                DataClass datumOne = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                DataClass datumIntegrated = BuiltinProcedures.evaluateAdding(datumPowerTo, datumOne);
                if (((AEMulDivOpt)aeExpr2Integ).mlistOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY) {
                    datumCoeff = BuiltinProcedures.evaluateDivision(datumCoeff, datumIntegrated);
                } else {
                    datumCoeff = BuiltinProcedures.evaluateMultiplication(datumCoeff, datumIntegrated);
                }
                AEConst aeIntegrated = new AEConst(datumIntegrated);
                AEConst aeIntegMultiply = new AEConst(datumCoeff);
                AEPowerOpt aeXToInteg = new AEPowerOpt(aeUnknown, aeIntegrated);
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                listChildren.add(aeIntegMultiply);
                listChildren.add(aeXToInteg);
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                listOpts.add(new CalculateOperator(((AEMulDivOpt)aeExpr2Integ).mlistOpts.getFirst().getOperatorType(), 2));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                AEMulDivOpt aeReturn = new AEMulDivOpt(listChildren, listOpts);
                return aeReturn;
            }
        } else if (aeExpr2Integ instanceof AEPosNegOpt) {
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            int idxChild = 0;
            for (AbstractExpr aeChild : ((AEPosNegOpt)aeExpr2Integ).mlistChildren) {
                // we need not to simplify aeChild most here because aeChild, as part of aeExpr2Integ, has been simplified.
                AbstractExpr aeChildInteg = integInDefByPtn1VarIntegIdentifier(
                                                    aeChild,
                                                    listUnknown,
                                                    progContext);
                listChildren.add(aeChildInteg);
                listOpts.add(new CalculateOperator(((AEPosNegOpt)aeExpr2Integ).mlistOpts.get(idxChild).getOperatorType(),
                        ((AEPosNegOpt)aeExpr2Integ).mlistOpts.get(idxChild).getOperandNum(),
                        ((AEPosNegOpt)aeExpr2Integ).mlistOpts.get(idxChild).getLabelPrefix()));
                idxChild ++;
            }
            AEPosNegOpt aeReturn = new AEPosNegOpt(listChildren, listOpts);
            return aeReturn;
        } else if (aeExpr2Integ instanceof AEMulDivOpt
                && aeExpr2Integ.getListOfChildren().getFirst() instanceof AEConst
                && DCHelper.isPrimitiveOrArray(((AEConst)aeExpr2Integ.getListOfChildren().getFirst()).getDataClassRef())
                && DCHelper.isEye(((AEConst)aeExpr2Integ.getListOfChildren().getFirst()).getDataClassRef(), true) == false) {
            if (aeExpr2Integ.getListOfChildren().size() == 2)   {   // size cannot be smaller than 2.
                // consider 2 cases: multiply or div, aeConst multiply or div.
                if (((AEMulDivOpt)aeExpr2Integ).mlistOpts.getLast().getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE) {
                    DataClass datumOne = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                    AEConst aeOne = new AEConst(datumOne);
                    LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                    listChildren.add(aeOne);
                    listChildren.add(((AEMulDivOpt)aeExpr2Integ).mlistChildren.getLast());
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
                    AbstractExpr aeNewExpr2Integ = new AEMulDivOpt(listChildren, listOpts);
                    // need not to simplify aeNewExpr2Integ most because it is 1/..., while original aeExpr2Integ is a/... and aeExpr2Integ has been simplified most.
                    AEConst aeCoeff = (AEConst)((AEMulDivOpt)aeExpr2Integ).mlistChildren.getFirst();
                    DataClass datumCoeff = DCHelper.lightCvtOrRetDCComplex(aeCoeff.getDataClassRef()); // coefficient can only be a complex.
                    aeCoeff = new AEConst(datumCoeff);
                    AbstractExpr aeNewIntegResult = integInDefByPtn1VarIntegIdentifier(
                                                    aeNewExpr2Integ,
                                                    listUnknown,
                                                    progContext);
                    listChildren = new LinkedList<AbstractExpr>();
                    listChildren.add(aeCoeff);
                    listChildren.add(aeNewIntegResult);
                    listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(new CalculateOperator(((AEMulDivOpt)aeExpr2Integ).mlistOpts.getFirst().getOperatorType(),
                            ((AEMulDivOpt)aeExpr2Integ).mlistOpts.getFirst().getOperandNum(),
                            ((AEMulDivOpt)aeExpr2Integ).mlistOpts.getFirst().getLabelPrefix()));
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                    AEMulDivOpt aeReturn = new AEMulDivOpt(listChildren, listOpts);
                    return aeReturn;
                } else {    // this is like a * x
                    DataClass datumCoeff = DCHelper.lightCvtOrRetDCComplex(((AEConst)((AEMulDivOpt)aeExpr2Integ).mlistChildren.getFirst())
                            .getDataClassRef()); // coefficient can only be a complex.
                    AEConst aeCoeff = new AEConst(datumCoeff);
                    AbstractExpr aeNewExpr2Integ = ((AEMulDivOpt)aeExpr2Integ).mlistChildren.getLast();
                    // need not to simplify aeNewExpr2Integ most because it is a part of aeExpr2Integ which has been simplified most.
                    AbstractExpr aeNewIntegResult = integInDefByPtn1VarIntegIdentifier(
                                                    aeNewExpr2Integ,
                                                    listUnknown,
                                                    progContext);
                    LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                    listChildren.add(aeCoeff);
                    listChildren.add(aeNewIntegResult);
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(new CalculateOperator(((AEMulDivOpt)aeExpr2Integ).mlistOpts.getFirst().getOperatorType(),
                            ((AEMulDivOpt)aeExpr2Integ).mlistOpts.getFirst().getOperandNum(),
                            ((AEMulDivOpt)aeExpr2Integ).mlistOpts.getFirst().getLabelPrefix()));
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                    AEMulDivOpt aeReturn = new AEMulDivOpt(listChildren, listOpts);
                    return aeReturn;
                }
            } else {    // number of children is greater than 2.
                AEMulDivOpt aeExpr2IntegRef = (AEMulDivOpt)aeExpr2Integ;
                AEConst aeCoeff = (AEConst)aeExpr2IntegRef.mlistChildren.getFirst();
                DataClass datumCoeff = DCHelper.lightCvtOrRetDCComplex(aeCoeff.getDataClassRef()); // coefficient can only be a complex.
                aeCoeff = new AEConst(datumCoeff);
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                listChildren.addAll(aeExpr2IntegRef.mlistChildren);
                listChildren.removeFirst();
                AEConst aeOne = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE));
                listChildren.addFirst(aeOne);   // need to add 1 because like 1/x*sqrt(...) will be simplified as /x*sqrt(...)
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                listOpts.addAll(aeExpr2IntegRef.mlistOpts);
                AbstractExpr aeNewExpr2Integ = new AEMulDivOpt(listChildren, listOpts);
                aeNewExpr2Integ = aeNewExpr2Integ.simplifyAExprMost(listUnknown, new SimplifyParams(true, true, true), progContext); // look on unknown dim as single value dim.
                AbstractExpr aeNewIntegResult = integInDefByPtn1VarIntegIdentifier(
                                                aeNewExpr2Integ,
                                                listUnknown,
                                                progContext);
                listChildren = new LinkedList<AbstractExpr>();
                listChildren.add(aeCoeff);
                listChildren.add(aeNewIntegResult);
                listOpts = new LinkedList<CalculateOperator>();
                listOpts.add(aeExpr2IntegRef.mlistOpts.getFirst());
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                AEMulDivOpt aeReturn = new AEMulDivOpt(listChildren, listOpts);
                return aeReturn;
            }
        } else {    // now it is time to check patterns.
            LinkedList<Ptn1VarIntegIdentifier> listAllPatterns = new LinkedList<Ptn1VarIntegIdentifier>();
            listAllPatterns.addAll(mlist1VarIntegPatterns);

            LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts = new LinkedList<PatternExprUnitMap>();
            LinkedList<PatternExprUnitMap> listpeuMapUnknowns = new LinkedList<PatternExprUnitMap>();        
            for (Ptn1VarIntegIdentifier pvii : listAllPatterns) {
                for (int idxPattern = 0; idxPattern < pvii.maearrayPatterns.length; idxPattern ++) {
                    listpeuMapPseudoConsts.clear();
                    listpeuMapUnknowns.clear();
                    // allow conversion here.
                    boolean bMatch = aeExpr2Integ.isPatternMatch(pvii.maearrayPatterns[idxPattern],
                            new LinkedList<PatternExprUnitMap>(), listpeuMapPseudoConsts, listpeuMapUnknowns,
                            true, progContext);
                    if (bMatch) {
                        // get it
                        AEConst aeIntegCoeff = null;
                        CalculateOperator coptCoeff = null;
                        if (listpeuMapUnknowns.size() != 1 || listpeuMapPseudoConsts.size() > pvii.maearrayPseudoConsts.length)    {
                            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_INTEGRATION);
                        }
                        // ok, now assign values to other pseudo consts
                        LinkedList<Variable> listPseudoConsts = new LinkedList<Variable>();
                        for (int idx = 0; idx < pvii.maearrayPseudoConsts.length; idx ++)   {
                            String strPseudoConstName = pvii.maearrayPseudoConsts[idx].mstrVariableName;
                            listPseudoConsts.add(new Variable(strPseudoConstName, new DataClassNull()));
                        }
                        ProgContext progContextTmp = new ProgContext();
                        progContextTmp.mdynamicProgContext.mlVarNameSpaces = new LinkedList<LinkedList<Variable>>();
                        progContextTmp.mdynamicProgContext.mlVarNameSpaces.add(listPseudoConsts);
                        progContextTmp.mstaticProgContext.setCitingSpacesExplicitly(progContext.mstaticProgContext.getCitingSpaces());
                        ExprEvaluator exprEvaluator = new ExprEvaluator(progContextTmp);
                        if (pvii.mstrarrayPatternConditions[idxPattern].trim().length() > 0) {
                            // we have pattern condition to satisfy.
                            exprEvaluator.evaluateExpression(pvii.mstrarrayPatternConditions[idxPattern], new CurPos());
                        }
                        boolean bMatchPseudo = true;
                        for (int idx = 0; idx < pvii.maearrayPseudoConsts.length; idx ++)   {
                            String strPseudoConstName = pvii.maearrayPseudoConsts[idx].mstrVariableName;
                            DataClass datumPatternCond = VariableOperator.lookUpList4Value(strPseudoConstName, listPseudoConsts);
                            DataClass datumMatched = new DataClassNull();
                            for (int idx1 = 0; idx1 < listpeuMapPseudoConsts.size(); idx1 ++)  {
                                if (((AEVar)listpeuMapPseudoConsts.get(idx1).maePatternUnit).mstrVariableName.equals(strPseudoConstName)) {
                                    // find the pseudo constant in the matched list.
                                    if (listpeuMapPseudoConsts.get(idx1).maeExprUnit.menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE
                                            && listpeuMapPseudoConsts.get(idx1).maeExprUnit.menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_DATAREFVALUE) {
                                        //throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_INTEGRATION);
                                        bMatchPseudo = false;
                                    } else {
                                        datumMatched = ((AEConst)listpeuMapPseudoConsts.get(idx1).maeExprUnit).getDataClass();  // getDataClass will automatically determine when to deep copy.
                                    }
                                    break;
                                }
                            }
                            if (!bMatchPseudo) {
                                break;
                            } else if (datumPatternCond.isNull()) {
                                // in the pattern condition the pseudo constant's value is not set.
                                if (datumMatched.isNull()) {
                                    // the pseudo const doesn't have a valid value
                                    bMatchPseudo = false;
                                    break;
                                } else {
                                    // assign the new value to the variable
                                    VariableOperator.setValueInList(listPseudoConsts, strPseudoConstName, datumMatched);
                                }
                                
                            } else if (!datumMatched.isNull() && !datumPatternCond.isEqual(datumMatched)) {
                                // the pseudo constant has been matched. But pattern condition conflicts the matched pseudo value, something wrong.
                                // note that here datumPatternCond must be not null.
                                bMatchPseudo = false;
                                break;
                            } else if (datumMatched.isNull()) {
                                listpeuMapPseudoConsts.add(new PatternExprUnitMap(new AEConst(datumPatternCond.cloneSelf()), new AEVar(strPseudoConstName, ABSTRACTEXPRTYPES.ABSTRACTEXPR_PSEUDOCONST)));
                            }   // otherwise ok. need not reset pseudo constant value or add listpeuMapPseudoConsts
                        }
                        if (!bMatchPseudo) {
                            continue;   // the pseudo const values are not correct, go to next pattern.
                        }

                        // now the pseudo constants have values, need to validate that the values match the pseudo const conditions.
                        for (int idx = 0; idx < pvii.mstrarrayPCConditions.length; idx ++) {
                            if (pvii.mstrarrayPCConditions[idx].trim().length() == 0) {
                                // do not have pseudo constant condition
                                continue;
                            }
                            DataClass datumPCCondition = exprEvaluator.evaluateExpression(pvii.mstrarrayPCConditions[idx], new CurPos());
                            if (!DCHelper.lightCvtOrRetDCMFPBool(datumPCCondition).getDataValue().booleanValue()) {
                                // unfortunately the condition cannot match
                                bMatchPseudo = false;
                                break;
                            }
                        }
                        if (!bMatchPseudo) {
                            continue;   // the pseudo const conditions cannot match, go to next pattern.
                        }
                        AbstractExpr aeUnknownExprUnit = listpeuMapUnknowns.getFirst().maeExprUnit;
                        if (aeUnknownExprUnit.menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE) {
                            if (aeUnknownExprUnit instanceof AEMulDivOpt && ((AEMulDivOpt)aeUnknownExprUnit).mlistChildren.size() == 2
                                    && aeUnknownExprUnit.getListOfChildren().getFirst().menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE
                                    && aeUnknownExprUnit.getListOfChildren().getLast().menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE
                                    && ((AEVar)aeUnknownExprUnit.getListOfChildren().getLast()).mstrVariableName.equals(strVarName)
                                    && ((AEMulDivOpt)aeUnknownExprUnit).mlistOpts.getLast().getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY) {
                                // it is something like x is mapped to 2*y. Note that we need not to worry about 2 is after y because it has been
                                // fully simplified with matrix dim ignored.
                                aeIntegCoeff = (AEConst)aeUnknownExprUnit.getListOfChildren().getFirst();
                                coptCoeff = (((AEMulDivOpt)aeUnknownExprUnit).mlistOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY)
                                        ?new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2)
                                        :new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
                            } else if (aeUnknownExprUnit instanceof AELeftDivOpt
                                    && ((AELeftDivOpt)aeUnknownExprUnit).maeRight.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE
                                    && ((AEVar)((AELeftDivOpt)aeUnknownExprUnit).maeRight).mstrVariableName.equals(strVarName)
                                    && ((AELeftDivOpt)aeUnknownExprUnit).maeLeft.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE) {
                                // it is something like x is mapped to 2\y
                                aeIntegCoeff = (AEConst)((AELeftDivOpt)aeUnknownExprUnit).maeRight;
                                coptCoeff = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
                            } else if (aeUnknownExprUnit instanceof AEPosNegOpt && aeUnknownExprUnit.getListOfChildren().size() == 1
                                    && aeUnknownExprUnit.getListOfChildren().getFirst().menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE) {
                                // it is something like x is mapped to -y
                                boolean bNeg = ((AEPosNegOpt)aeUnknownExprUnit).mlistOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_NEGSIGN
                                        || ((AEPosNegOpt)aeUnknownExprUnit).mlistOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_SUBTRACT;
                                if (bNeg) {
                                    aeIntegCoeff = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.MINUS_ONE));
                                } else {
                                    aeIntegCoeff = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE));
                                }
                                coptCoeff = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
                            } else {
                                //throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_SOLVE_CALCULATION);
                                continue;   // do not throw at this moment, we may find another pattern which matches this.
                            }
                        }
                        AbstractExpr aeIntegrated = ExprAnalyzer.analyseExpression(pvii.mstrIntegrated, new CurPos(), listPseudoConsts, progContext);
                        // have to replace pseudo constants here.
                        aeIntegrated = replaceExprPattern(aeIntegrated, listpeuMapPseudoConsts, false, progContext);
                        if (aeIntegCoeff != null)   {
                            // need to multiply a factor
                            LinkedList<AbstractExpr> listMulDivChildren = new LinkedList<AbstractExpr>();
                            listMulDivChildren.add(aeIntegCoeff);
                            listMulDivChildren.add(aeIntegrated);
                            LinkedList<CalculateOperator> listMulDivOpts = new LinkedList<CalculateOperator>();
                            listMulDivOpts.add(coptCoeff);
                            listMulDivOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                            aeIntegrated = new AEMulDivOpt(listMulDivChildren, listMulDivOpts);
                        }
                        if (listPseudoConsts.size() > 0) {
                            progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(listPseudoConsts);
                            aeIntegrated = integRecurChildren(aeIntegrated, progContext);   // process recursive integration.
                            progContext.mdynamicProgContext.mlVarNameSpaces.poll();
                        } else {
                            aeIntegrated = integRecurChildren(aeIntegrated, progContext);   // process recursive integration.
                        }
                        // have to replace unknowns here considering that aeIntegrated is
                        // f(x) + integrate("a+b*x","x") as in recursive integration x hasn't been replaced to z yet.
                        aeIntegrated = replaceExprPattern(aeIntegrated, listpeuMapUnknowns, false, progContext);
                        return aeIntegrated;
                    }
                }
            }
            
            // now seems that it does not match any pattern, could it be like integrate("f'(g(x))*g'(x)","x") == f(g(x))?
            if (aeExpr2Integ instanceof AEMulDivOpt) {
                AEConst aeOne = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE));
                String strNewVarName = "internal_var0";
                AEVar aeNewVar = new AEVar(strNewVarName, ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE);
                for (int idx = 0; idx < ((AEMulDivOpt)aeExpr2Integ).mlistChildren.size(); idx ++) {
                    AbstractExpr aeChild = ((AEMulDivOpt)aeExpr2Integ).mlistChildren.get(idx);
                    if (aeChild instanceof AEConst) {
                        continue;
                    }
                    if (((AEMulDivOpt)aeExpr2Integ).mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE) {
                        LinkedList<AbstractExpr> listGrandChildren = new LinkedList<AbstractExpr>();
                        listGrandChildren.add(aeOne);
                        listGrandChildren.add(aeChild);
                        LinkedList<CalculateOperator> listGrandOpts = new LinkedList<CalculateOperator>();
                        listGrandOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                        listGrandOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
                        aeChild = new AEMulDivOpt(listGrandChildren, listGrandOpts);
                        aeChild = aeChild.simplifyAExprMost(listUnknown, new SimplifyParams(true, true, true), progContext);
                        if (aeChild.isEqual(aeExpr2Integ, progContext)) {
                            continue;   // for example, if aeExpr2Integ is 1/(-x**2-x**3), aeChild will still be 1/(-x**2-x**3).
                        }
                    }
                    // now try to integrate aeChild
                    AbstractExpr aeChildIntegrated = null;
                    try {
                        aeChildIntegrated = integInDefByPtn1VarIntegIdentifier(aeChild, listUnknown, progContext);
                    } catch (JSmartMathErrException ex) {
                        continue;   // this child cannot be integrated.
                    }
                    aeChildIntegrated = aeChildIntegrated.simplifyAExprMost(listUnknown, new SimplifyParams(true, true, true), progContext); // prevent case like asin(x/9**0.5)
                    
                    // now remove all the constant part of aeChildIntegrated
                    AEConst aeFactor = aeOne;
                    if (aeChildIntegrated instanceof AEPosNegOpt && aeChildIntegrated.getListOfChildren().size() == 1
                            && (((AEPosNegOpt)aeChildIntegrated).mlistOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_NEGSIGN
                                || ((AEPosNegOpt)aeChildIntegrated).mlistOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_SUBTRACT)) {
                        // this is like - cos(x)... don't worry about + cos(x) coz it has been fully simplified.
                        aeFactor = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.MINUS_ONE));
                        aeChildIntegrated = aeChildIntegrated.getListOfChildren().getFirst();
                    } else if (aeChildIntegrated instanceof AEMulDivOpt && aeChildIntegrated.getListOfChildren().getFirst() instanceof AEConst) {
                        // even aeChildIntegrated is something like 1/..., we should still remove 1 except that one child after 1.
                        aeFactor = (AEConst)aeChildIntegrated.getListOfChildren().getFirst();
                        if (((AEMulDivOpt)aeChildIntegrated).mlistOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE) {
                            aeFactor = new AEConst(BuiltinProcedures.evaluateReciprocal(aeFactor.getDataClassRef()));  // no need to deep copy.
                        }
                        if (aeChildIntegrated.getListOfChildren().size() == 2) {
                            if (((AEMulDivOpt)aeChildIntegrated).mlistOpts.getLast().getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE) {
                                LinkedList<AbstractExpr> listGrandChildren = new LinkedList<AbstractExpr>();
                                listGrandChildren.add(aeOne);
                                listGrandChildren.add(((AEMulDivOpt)aeChildIntegrated).mlistChildren.getLast());
                                LinkedList<CalculateOperator> listGrandOpts = new LinkedList<CalculateOperator>();
                                listGrandOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                                listGrandOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
                                aeChildIntegrated = new AEMulDivOpt(listGrandChildren, listGrandOpts);
                                aeChildIntegrated = aeChildIntegrated.simplifyAExprMost(listUnknown, new SimplifyParams(true, true, true), progContext); // need to simplify most again
                            } else {
                                // multiply needs no simplify most
                                aeChildIntegrated = ((AEMulDivOpt)aeChildIntegrated).mlistChildren.getLast();
                            }
                        } else {
                            // remove the first constant and simplify most again
                            ((AEMulDivOpt)aeChildIntegrated).mlistChildren.removeFirst();
                            ((AEMulDivOpt)aeChildIntegrated).mlistOpts.removeFirst();
                            aeChildIntegrated = aeChildIntegrated.simplifyAExprMost(listUnknown, new SimplifyParams(true, true, true), progContext); // need to simplify most again
                        }
                    }
                    
                    LinkedList<AbstractExpr> listModifiedChildren = new LinkedList<AbstractExpr>();
                    listModifiedChildren.addAll(aeExpr2Integ.getListOfChildren());
                    LinkedList<CalculateOperator> listModifiedCalcOpt = new LinkedList<CalculateOperator>();
                    listModifiedCalcOpt.addAll(((AEMulDivOpt)aeExpr2Integ).mlistOpts);
                    listModifiedChildren.set(idx, aeFactor);
                    listModifiedCalcOpt.set(idx, new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));                    
                    LinkedList<PatternExprUnitMap> listpeuMapNewUnknown = new LinkedList<PatternExprUnitMap>();
                    AbstractExpr aeChildExcluded = new AEMulDivOpt(listModifiedChildren, listModifiedCalcOpt);
                    PatternExprUnitMap peum = new PatternExprUnitMap(aeChildIntegrated, aeNewVar);
                    listpeuMapNewUnknown.add(peum);
                    aeChildExcluded = replaceExprPattern(aeChildExcluded, listpeuMapNewUnknown, true, progContext);
                    LinkedList<UnknownVariable> listNewUnknown = new LinkedList<UnknownVariable>();
                    listNewUnknown.add(new UnknownVariable(strNewVarName));
                    AbstractExpr aeIntegrated = null;
                    try {
                        aeChildExcluded = aeChildExcluded.simplifyAExprMost(listNewUnknown, new SimplifyParams(true, true, true), progContext); // put simplifyAExprMost in the try catch because there maybe two unknowns
                        aeIntegrated = integInDefByPtn1VarIntegIdentifier(aeChildExcluded, listNewUnknown, progContext);
                    } catch (JSmartMathErrException ex) {
                        continue;   // this cannot be integrated.
                    }
                    
                    // ok, now aeIntegrated is ready,
                    aeIntegrated = replaceExprPattern(aeIntegrated, listpeuMapNewUnknown, false, progContext);
                    return aeIntegrated;
                }
            }
        }
        
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_SOLVE_CALCULATION);
    }
    
    // note that aeIntegrated will not be changed inside this function
    public AbstractExpr integRecurChildren(AbstractExpr aeIntegrated, ProgContext progContext)
            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
        LinkedList<AbstractExpr> listOfChildren = new LinkedList<AbstractExpr>();
        listOfChildren.addAll(aeIntegrated.getListOfChildren());
        String strIntegrateNameWithCs = FunctionNameMapper.msmapSysFunc2FullCSMap.get("integrate");
        if (strIntegrateNameWithCs == null) {
            strIntegrateNameWithCs = "integrate";
        }
        // first go through all the non-function children to ensure all the grand-children and grand-grand-children etc are processed.
        for (int idx = 0; idx < listOfChildren.size(); idx ++) {
            if (!(listOfChildren.get(idx) instanceof AEFunction)
                    || !((AEFunction)listOfChildren.get(idx)).isSameAbsFuncNameWithCS(strIntegrateNameWithCs, progContext)) {
                // only if it is not integrate function we recursively process its children.
                listOfChildren.set(idx, integRecurChildren(listOfChildren.get(idx), progContext));
            }
        }
        aeIntegrated = aeIntegrated.copySetListOfChildren(listOfChildren);
        
        LinkedList<PatternExprUnitMap> listFromToMap = new LinkedList<PatternExprUnitMap>();
        for (int idx = 0; idx < listOfChildren.size(); idx ++) {
            if (listOfChildren.get(idx) instanceof AEFunction
                    && ((AEFunction)listOfChildren.get(idx)).isSameAbsFuncNameWithCS(strIntegrateNameWithCs, progContext)) {
                // should only be integrate function. If throw an undefined variable exception, then there must be an error in the expression.
                // using evaluateAExprQuick guarantees it will always return something.
                DataClass datumChildReturn = listOfChildren.get(idx).evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                AbstractExpr aeChildReturn = null;
                if (DCHelper.isDataClassType(datumChildReturn, DATATYPES.DATUM_STRING)) {
                    // seems to return an expression. Note that the returned expression should not include any pseudo const in the parameter.
                    aeChildReturn = ExprAnalyzer.analyseExpression(DCHelper.lightCvtOrRetDCString(datumChildReturn).getStringValue(), new CurPos(), new LinkedList<Variable>(), progContext);
                } else if (DCHelper.isNumericalData(datumChildReturn, false)) {
                    // return a value
                    aeChildReturn = new AEConst(datumChildReturn);
                } else {
                    throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_SOLVE_CALCULATION);
                }
                listFromToMap.add(new PatternExprUnitMap(listOfChildren.get(idx), aeChildReturn));
            }
        }
        if (listFromToMap.size() > 0) {
            aeIntegrated = aeIntegrated.replaceChildren(listFromToMap, true, new LinkedList<AbstractExpr>(), progContext);
        }
        return aeIntegrated;
    }
    
    // this function will not change aeExpr2Deri. Its return will not include any part of aeExpr2Deri.
    // this function doesn't support complex derivative or array derivative.
    public AbstractExpr deriInDefByPtn1VarDeriIdentifier(AbstractExpr aeExpr2Deri,   // it has been simplified most. so need not to do it in the function
                                                        LinkedList<UnknownVariable> listUnknown,
                                                        ProgContext progContext)
                            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
        // This function is for derivative calculation
        // note that we do not need to use cloneSelf because generally AbstractExpr is immutable except a
        // special case for AEConst where the data is an array constant. However, we don't support array derivative.
        // so if aeExpr2Deri does not include any array, it is fine. Even if it includes an array constant, because
        // it is AEConst, in general it will be ok. But we still need to guarantee that AEConst parts in aeExpr2Deri
        // is deep copied before call top level of this function.
        
        // We should guarantee that the output of top level of this function do not change.
        
        if (listUnknown.size() != 1)  {   // multi-variable derivative is still not supported.
            throw new JSmartMathErrException(ERRORTYPES.ERROR_UNSUPPORTED_INTEGRATION_TYPE);
        }
        
        String strVarName = listUnknown.getFirst().getName();
                
        if (aeExpr2Deri instanceof AEConst) {  // derivative of a constant.
            AEConst aeReturn = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO));
            return aeReturn;
        } else if ((aeExpr2Deri instanceof AEVar) && ((AEVar)aeExpr2Deri).mstrVariableName.equals(strVarName)) {  // derivative of x. need not to worry about -x because -x is a AEPosNegOpt.
            AEConst aeReturn = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE));
            return aeReturn;
        } else if ((aeExpr2Deri instanceof AEPowerOpt)
                && (((AEPowerOpt)aeExpr2Deri).maeLeft instanceof AEConst)
                ) { // derivative of a**x or a**f(x)
            AEConst aePowerBase = (AEConst)((AEPowerOpt)aeExpr2Deri).maeLeft;
            AbstractExpr aePowerTo = ((AEPowerOpt)aeExpr2Deri).maeRight;
            AbstractExpr aePowerToDeri = deriInDefByPtn1VarDeriIdentifier(aePowerTo, listUnknown, progContext);
            DataClass datumLogConst = BuiltinProcedures.evaluateLog(aePowerBase.getDataClassRef()); // no need deep copy
            AEConst aeLogConst = new AEConst(datumLogConst);
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            listChildren.add(aeLogConst);
            listChildren.add(aeExpr2Deri); // Do not clone self.
            listChildren.add(aePowerToDeri);
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            AEMulDivOpt aeReturn = new AEMulDivOpt(listChildren, listOpts);
            return aeReturn;
        } else if ((aeExpr2Deri instanceof AEPowerOpt)
                && (((AEPowerOpt)aeExpr2Deri).maeRight instanceof AEConst)
                ) { // derivative of x**a or f(x)**a
            AbstractExpr aePowerBase = ((AEPowerOpt)aeExpr2Deri).maeLeft;
            AEConst aePowerTo = (AEConst)((AEPowerOpt)aeExpr2Deri).maeRight;
            DataClass datumPowerTo = aePowerTo.getDataClassCopy();
            AbstractExpr aeReturn = null;
            if (datumPowerTo.isEqual(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO))) {
                aeReturn = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO));
                return aeReturn;
            } else if (datumPowerTo.isEqual(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE))) {
                aeReturn = new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE));
                // cannot return aeReturn now, need to multiply it by aePowerBaseDeri
            } else if (datumPowerTo.isEqual(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TWO))) {
                AEConst aeCoeff = new AEConst(datumPowerTo);
                // power base can be a single variable or an expression. Note that we dont clone aePowerBase.
                AbstractExpr aeUnknown = aePowerBase;
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                listChildren.add(aeCoeff);
                listChildren.add(aeUnknown);
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                aeReturn = new AEMulDivOpt(listChildren, listOpts);
                // cannot return aeReturn now, need to multiply it by aePowerBaseDeri
            } else {
                AEConst aeCoeff = new AEConst(datumPowerTo);
                AbstractExpr aeUnknown = aePowerBase;  // power base can be a single variable or an expression, but we do not clone.
                DataClass datumOne = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                DataClass datumNewPowerTo = BuiltinProcedures.evaluateSubstraction(datumPowerTo, datumOne);
                AEConst aeNewPowerTo = new AEConst(datumNewPowerTo);
                AEPowerOpt aeNewPower = new AEPowerOpt(aeUnknown, aeNewPowerTo);
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                listChildren.add(aeCoeff);
                listChildren.add(aeNewPower);
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                aeReturn = new AEMulDivOpt(listChildren, listOpts);
                // cannot return aeReturn now, need to multiply it by aePowerBaseDeri
            }
            // now multiply it by aePowerBaseDeri
            AbstractExpr aePowerBaseDeri = deriInDefByPtn1VarDeriIdentifier(aePowerBase, listUnknown, progContext);
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            listChildren.add(aeReturn);
            listChildren.add(aePowerBaseDeri);
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            return new AEMulDivOpt(listChildren, listOpts);
        } else if (aeExpr2Deri instanceof AEPowerOpt) { // derivative of f(x)**g(x)
            // derivative of f(x)**g(x) should be equal to g(x)*f(x)**(g(x)-1)*f'(x) + f(x)**g(x)*log(f(x))*g'(x)
            AbstractExpr aePowerBase = ((AEPowerOpt)aeExpr2Deri).maeLeft;
            AbstractExpr aePowerTo = ((AEPowerOpt)aeExpr2Deri).maeRight;
            AbstractExpr aePowerBaseDeri = deriInDefByPtn1VarDeriIdentifier(aePowerBase, listUnknown, progContext);
            AbstractExpr aePowerToDeri = deriInDefByPtn1VarDeriIdentifier(aePowerTo, listUnknown, progContext);
            
            LinkedList<AbstractExpr> listLogParam = new LinkedList<AbstractExpr>();
            listLogParam.add(aePowerBase);  // do not cloneself
            AEFunction aeLogPowerBase = new AEFunction(FunctionNameMapper.msmapSysFunc2FullCSMap.get("log"), listLogParam, progContext);
            
            LinkedList<AbstractExpr> listPowerToMinus1 = new LinkedList<AbstractExpr>();
            listPowerToMinus1.add(aePowerTo);
            listPowerToMinus1.add(new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE)));
            LinkedList<CalculateOperator> listOptsPowerToMinus1 = new LinkedList<CalculateOperator>();
            listOptsPowerToMinus1.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1));
            listOptsPowerToMinus1.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
            AEPosNegOpt aePowerToMinus1 = new AEPosNegOpt(listPowerToMinus1, listOptsPowerToMinus1);
            AEPowerOpt aeBasePowerToMinus1 = new AEPowerOpt(aePowerBase, aePowerToMinus1);
            
            LinkedList<AbstractExpr> listNewLeft = new LinkedList<AbstractExpr>();
            listNewLeft.add(aePowerTo);
            listNewLeft.add(aeBasePowerToMinus1);
            listNewLeft.add(aePowerBaseDeri);
            LinkedList<CalculateOperator> listOptsNewLeft = new LinkedList<CalculateOperator>();
            listOptsNewLeft.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            listOptsNewLeft.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            listOptsNewLeft.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            AEMulDivOpt aeNewLeft = new AEMulDivOpt(listNewLeft, listOptsNewLeft);
            
            LinkedList<AbstractExpr> listNewRight = new LinkedList<AbstractExpr>();
            listNewRight.add(aeExpr2Deri);
            listNewRight.add(aeLogPowerBase);
            listNewRight.add(aePowerToDeri);
            LinkedList<CalculateOperator> listOptsNewRight = new LinkedList<CalculateOperator>();
            listOptsNewRight.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            listOptsNewRight.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            listOptsNewRight.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            AEMulDivOpt aeNewRight = new AEMulDivOpt(listNewRight, listOptsNewRight);
            
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            listChildren.add(aeNewLeft);
            listChildren.add(aeNewRight);
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1));
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2));
            AEPosNegOpt aeReturn = new AEPosNegOpt(listChildren, listOpts);
            return aeReturn;
        } else if (aeExpr2Deri instanceof AEMulDivOpt && ((AEMulDivOpt)aeExpr2Deri).mlistChildren.size() == 2
                && ((AEMulDivOpt)aeExpr2Deri).mlistOpts.getLast().getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY) {
            // f(x) * g(x)
            if (((AEMulDivOpt)aeExpr2Deri).mlistChildren.getFirst() instanceof AEConst
                    && !(((AEMulDivOpt)aeExpr2Deri).mlistChildren.getLast() instanceof AEConst)
                    ) {    // derivative of a*x or 1/a * x, note that const is always the first because aeExpr2Deri has been fully simplified
                AEConst aeCoeff = (AEConst)((AEMulDivOpt)aeExpr2Deri).mlistChildren.getFirst();
                DataClass datumCoeff = aeCoeff.getDataClassCopy();
                aeCoeff = new AEConst(datumCoeff);
                // aeNonConst may not be immutable. However, we only need its derivative so no need to clone
                AbstractExpr aeNonConst = ((AEMulDivOpt)aeExpr2Deri).mlistChildren.getLast();
                AbstractExpr aeNonConstDeri = deriInDefByPtn1VarDeriIdentifier(aeNonConst, listUnknown, progContext);
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                listChildren.add(aeCoeff);
                listChildren.add(aeNonConstDeri);
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                listOpts.add(new CalculateOperator(((AEMulDivOpt)aeExpr2Deri).mlistOpts.getFirst().getOperatorType(), 2));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                AEMulDivOpt aeReturn = new AEMulDivOpt(listChildren, listOpts);
                return aeReturn;
            } else {
                AbstractExpr aeFirst = ((AEMulDivOpt)aeExpr2Deri).mlistChildren.getFirst();
                AbstractExpr aeSecond = ((AEMulDivOpt)aeExpr2Deri).mlistChildren.getLast();
                if (((AEMulDivOpt)aeExpr2Deri).mlistOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE) {
                    DataClass datumOne = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                    AEConst aeOne = new AEConst(datumOne);
                    LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                    listChildren.add(aeOne);
                    listChildren.add(aeFirst);
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
                    aeFirst = new AEMulDivOpt(listChildren, listOpts);
                }
                AbstractExpr aeFirstDeri = deriInDefByPtn1VarDeriIdentifier(aeFirst, listUnknown, progContext);
                AbstractExpr aeSecondDeri = deriInDefByPtn1VarDeriIdentifier(aeSecond, listUnknown, progContext);
                // return f(x) * g'(x) + g(x) * f'(x)
                LinkedList<AbstractExpr> listChildren1 = new LinkedList<AbstractExpr>();
                listChildren1.add(aeFirst);
                listChildren1.add(aeSecondDeri);
                LinkedList<CalculateOperator> listOpts1 = new LinkedList<CalculateOperator>();
                listOpts1.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listOpts1.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                AEMulDivOpt aePart1 = new AEMulDivOpt(listChildren1, listOpts1);
                
                LinkedList<AbstractExpr> listChildren2 = new LinkedList<AbstractExpr>();
                listChildren2.add(aeSecond);
                listChildren2.add(aeFirstDeri);
                LinkedList<CalculateOperator> listOpts2 = new LinkedList<CalculateOperator>();
                listOpts2.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listOpts2.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                AEMulDivOpt aePart2 = new AEMulDivOpt(listChildren2, listOpts2);
                
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                listChildren.add(aePart1);
                listChildren.add(aePart2);
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2));
                AEPosNegOpt aeReturn = new AEPosNegOpt(listChildren, listOpts);
                return aeReturn;
            }
        } else if (aeExpr2Deri instanceof AEMulDivOpt && ((AEMulDivOpt)aeExpr2Deri).mlistChildren.size() == 2
                && ((AEMulDivOpt)aeExpr2Deri).mlistOpts.getLast().getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE) {
            // f(x) / g(x)
            if (((AEMulDivOpt)aeExpr2Deri).mlistChildren.getFirst() instanceof AEConst
                    && !(((AEMulDivOpt)aeExpr2Deri).mlistChildren.getLast() instanceof AEConst)
                    ) {    // derivative of a/x or 1/a / x, note that const is always the first because aeExpr2Deri has been fully simplified
                AEConst aeCoeff = (AEConst)((AEMulDivOpt)aeExpr2Deri).mlistChildren.getFirst();
                DataClass datumCoeff = aeCoeff.getDataClassRef();   // no need deep copy because BuiltinProcedures.evaluateNegSign will create a new dataclass.
                aeCoeff = new AEConst(BuiltinProcedures.evaluateNegSign(datumCoeff));   // multiply by -1 so that daumCoeff needs no deep copy.
                // aeNonConst may not be immutable. However, we only need its derivative so no need to clone
                AbstractExpr aeNonConst = ((AEMulDivOpt)aeExpr2Deri).mlistChildren.getLast();
                AbstractExpr aeNonConstDeri = deriInDefByPtn1VarDeriIdentifier(aeNonConst, listUnknown, progContext);
                AEPowerOpt aeNonConstTo2 = new AEPowerOpt(aeNonConst, new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TWO)));
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                listChildren.add(aeCoeff);
                listChildren.add(aeNonConstDeri);
                listChildren.add(aeNonConstTo2);
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                listOpts.add(new CalculateOperator(((AEMulDivOpt)aeExpr2Deri).mlistOpts.getFirst().getOperatorType(), 2));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
                // result is -a * f'(x)/f(x)**2 or 1/(-a) * f'(x)/f(x)**2.
                AEMulDivOpt aeReturn = new AEMulDivOpt(listChildren, listOpts);
                return aeReturn;
            } else {
                AbstractExpr aeFirst = ((AEMulDivOpt)aeExpr2Deri).mlistChildren.getFirst();
                AbstractExpr aeSecond = ((AEMulDivOpt)aeExpr2Deri).mlistChildren.getLast();
                if (((AEMulDivOpt)aeExpr2Deri).mlistOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE) {
                    DataClass datumOne = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                    AEConst aeOne = new AEConst(datumOne);
                    LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                    listChildren.add(aeOne);
                    listChildren.add(aeFirst);
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
                    aeFirst = new AEMulDivOpt(listChildren, listOpts);
                }
                AbstractExpr aeFirstDeri = deriInDefByPtn1VarDeriIdentifier(aeFirst, listUnknown, progContext);
                AbstractExpr aeSecondDeri = deriInDefByPtn1VarDeriIdentifier(aeSecond, listUnknown, progContext);
                AEPowerOpt aeSecondTo2 = new AEPowerOpt(((AEMulDivOpt)aeExpr2Deri).mlistChildren.getLast(),
                                                    new AEConst(new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.TWO)));
                // return (g(x) * f'(x) - f(x) * g'(x))/g(x)**2
                LinkedList<AbstractExpr> listChildren1 = new LinkedList<AbstractExpr>();
                listChildren1.add(aeFirstDeri);
                listChildren1.add(aeSecond);
                LinkedList<CalculateOperator> listOpts1 = new LinkedList<CalculateOperator>();
                listOpts1.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listOpts1.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                AEMulDivOpt aePart1 = new AEMulDivOpt(listChildren1, listOpts1);
                
                LinkedList<AbstractExpr> listChildren2 = new LinkedList<AbstractExpr>();
                listChildren2.add(aeSecondDeri);
                listChildren2.add(aeFirst);
                LinkedList<CalculateOperator> listOpts2 = new LinkedList<CalculateOperator>();
                listOpts2.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listOpts2.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                AEMulDivOpt aePart2 = new AEMulDivOpt(listChildren2, listOpts2);
                
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                listChildren.add(aePart1);
                listChildren.add(aePart2);
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
                AEPosNegOpt aeNumerator = new AEPosNegOpt(listChildren, listOpts);
                
                LinkedList<AbstractExpr> listChildrenFinal = new LinkedList<AbstractExpr>();
                listChildrenFinal.add(aeNumerator);
                listChildrenFinal.add(aeSecondTo2);
                LinkedList<CalculateOperator> listOptsFinal = new LinkedList<CalculateOperator>();
                listOptsFinal.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listOptsFinal.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
                AEMulDivOpt aeReturn = new AEMulDivOpt(listChildrenFinal, listOptsFinal);
                return aeReturn;
            }
        } else if (aeExpr2Deri instanceof AEMulDivOpt && ((AEMulDivOpt)aeExpr2Deri).mlistChildren.size() > 2) {
            LinkedList<AbstractExpr> listChildren  = new LinkedList<AbstractExpr>();
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            for (int idx = 1; idx < ((AEMulDivOpt)aeExpr2Deri).mlistChildren.size(); idx ++) {
                AbstractExpr aeChild = ((AEMulDivOpt)aeExpr2Deri).mlistChildren.get(idx);   // no need cloneself because we will calc derivative of them latter on.
                listChildren.add(aeChild);
                listOpts.add(new CalculateOperator(((AEMulDivOpt)aeExpr2Deri).mlistOpts.get(idx).getOperatorType(),
                                                    ((AEMulDivOpt)aeExpr2Deri).mlistOpts.get(idx).getOperandNum()));
            }
            AEMulDivOpt aeExceptFirst = new AEMulDivOpt(listChildren, listOpts);
            LinkedList<AbstractExpr> listChildrenNew  = new LinkedList<AbstractExpr>();
            listChildrenNew.add(((AEMulDivOpt)aeExpr2Deri).mlistChildren.getFirst());
            listChildrenNew.add(aeExceptFirst);
            LinkedList<CalculateOperator> listOptsNew = new LinkedList<CalculateOperator>();
            listOptsNew.add(((AEMulDivOpt)aeExpr2Deri).mlistOpts.getFirst());
            listOptsNew.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            AEMulDivOpt aeExpr2DeriNew = new AEMulDivOpt(listChildrenNew, listOptsNew);
            AbstractExpr aeReturn = deriInDefByPtn1VarDeriIdentifier(aeExpr2DeriNew, listUnknown, progContext);
            return aeReturn;
        } else if (aeExpr2Deri instanceof AEPosNegOpt) {
            LinkedList<AbstractExpr> listChildren  = new LinkedList<AbstractExpr>();
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            for (int idx = 0; idx < ((AEPosNegOpt)aeExpr2Deri).mlistChildren.size(); idx ++) {
                AbstractExpr aeChild = deriInDefByPtn1VarDeriIdentifier(((AEPosNegOpt)aeExpr2Deri).mlistChildren.get(idx),
                                                                        listUnknown, progContext);
                listChildren.add(aeChild);
                listOpts.add(new CalculateOperator(((AEPosNegOpt)aeExpr2Deri).mlistOpts.get(idx).getOperatorType(),
                                                    ((AEPosNegOpt)aeExpr2Deri).mlistOpts.get(idx).getOperandNum()));
            }
            AEPosNegOpt aeReturn = new AEPosNegOpt(listChildren, listOpts);
            return aeReturn;
        } else if (aeExpr2Deri instanceof AEFunction && ((AEFunction)aeExpr2Deri).mlistChildren.size() == 1) {
            // f(g(x)), f should match a pattern.
            LinkedList<Ptn1VarDeriIdentifier> listAllPatterns = new LinkedList<Ptn1VarDeriIdentifier>();
            listAllPatterns.addAll(mlist1VarDeriPatterns);
            String strFuncFullNameWithCS = ((AEFunction)aeExpr2Deri).getAbsFuncNameWithCS(progContext);
            AbstractExpr aeFuncParam = ((AEFunction)aeExpr2Deri).mlistChildren.getFirst();
            for (Ptn1VarDeriIdentifier pvdi : listAllPatterns) {
                for (AbstractExpr aePattern : pvdi.maearrayPatterns) {
                    if (aePattern instanceof AEFunction) {
                        String strPatternFuncName = ((AEFunction)aePattern).getAbsFuncNameWithCS(progContext);
                        if (strPatternFuncName.equals(strFuncFullNameWithCS)) {
                            // ok, find the pattern.
                            AbstractExpr aeFuncParamDeri = deriInDefByPtn1VarDeriIdentifier(aeFuncParam, listUnknown, progContext);
                            LinkedList<PatternExprUnitMap> listpeuMap = new LinkedList<PatternExprUnitMap>();
                            listpeuMap.add(
                                    new PatternExprUnitMap(
                                            new AEVar(pvdi.mstrVariable, ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE),
                                            aeFuncParam));
                            AbstractExpr aeReplacedDeri = replaceExprPattern(pvdi.maeDerivative, listpeuMap, true, progContext);
                            LinkedList<AbstractExpr> listChildren  = new LinkedList<AbstractExpr>();
                            listChildren.add(aeReplacedDeri);
                            listChildren.add(aeFuncParamDeri);
                            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                            AEMulDivOpt aeReturn = new AEMulDivOpt(listChildren, listOpts);
                            return aeReturn;
                        }
                    }
                }
            }
            // cannot find a pattern to match.
            throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_SOLVE_CALCULATION);    
        } else {    // no match to anything.
            throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_SOLVE_CALCULATION);
        }
    }
}
