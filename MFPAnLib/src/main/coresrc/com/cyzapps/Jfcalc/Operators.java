// MFP project, Operators.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;

import java.io.Serializable;

import com.cyzapps.Jfcalc.ErrProcessor.*;

public class Operators {
    
    public static enum OPERATORTYPES
    {
        /* invalid operator */
        OPERATOR_NOTEXIST(0),  

        /* assign a value to a variable */
        OPERATOR_ASSIGN(1),
        
        /* judge if two operands are equal */
        OPERATOR_EQ(2),
        /* judge if two operands are not equal */
        OPERATOR_NEQ(3),
        /* judge if operand a is larger than b */
        OPERATOR_LARGER(4),
        /* judge if operand a is smaller than b */
        OPERATOR_SMALLER(5),
        /* judge if operand a is no larger than b */
        OPERATOR_NOLARGER(6),
        /* judge if operand a is no smaller than b */
        OPERATOR_NOSMALLER(7),

        /* bit AND */
        OPERATOR_BITWISEAND(8),  
        /* bit OR */
        OPERATOR_BITWISEOR(9),    
        /* bit XOR */
        OPERATOR_BITWISEXOR(10),   

        /* + */
        OPERATOR_ADD(11),
        /* - */
        OPERATOR_SUBTRACT(12),
        /* positive number, only one operand */
        OPERATOR_POSSIGN(13),   
        /* negative number, only one operand */
        OPERATOR_NEGSIGN(14),   


        /* * */
        OPERATOR_MULTIPLY(15),  
        /* / */
        OPERATOR_DIVIDE(16),    
        /* \ (left division for matrix) */
        OPERATOR_LEFTDIVIDE(17),
        
        
        /* **: power */
        OPERATOR_POWER(18), 

        /* equal to FALSE or not, only one operand */
        OPERATOR_FALSE(19),   
        /* bit NOT, only one operand */
        OPERATOR_NOT(20),   
        
        /* factorial */
        OPERATOR_FACTORIAL(21), 
        /* %: percentage */
        OPERATOR_PERCENT(22),
        /* ': transpose of an at most 2-D matrix */
        OPERATOR_TRANSPOSE(23),

        /* ( */
        OPERATOR_LEFTPARENTHESE(24),    
        /* ) */
        OPERATOR_RIGHTPARENTHESE(25),   
        OPERATOR_STARTEND(26);
        
        private int value; 

        private OPERATORTYPES(int i) { 
            value = i; 
        } 

        public int getValue() { 
            return value; 
        } 
        
        public String output() throws JFCALCExpErrException {
            if (value == OPERATOR_NOTEXIST.getValue())    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_NON_EXISTING_OPERATOR_CANNOT_OUTPUT);
            } else if (value == OPERATOR_ASSIGN.getValue())    {
                return "=";
            } else if (value == OPERATOR_EQ.getValue())    {
                return "==";
            } else if (value == OPERATOR_NEQ.getValue())    {
                return "!=";
            } else if (value == OPERATOR_LARGER.getValue())    {
                return ">";
            } else if (value == OPERATOR_SMALLER.getValue())    {
                return "<";
            } else if (value == OPERATOR_NOLARGER.getValue())    {
                return "<=";
            } else if (value == OPERATOR_NOSMALLER.getValue())    {
                return ">=";
            } else if (value == OPERATOR_ADD.getValue())    {
                return "+";
            } else if (value == OPERATOR_SUBTRACT.getValue())    {
                return "-";
            } else if (value == OPERATOR_MULTIPLY.getValue())    {
                return "*";
            } else if (value == OPERATOR_DIVIDE.getValue())    {
                return "/";
            } else if (value == OPERATOR_LEFTDIVIDE.getValue())    {
                return "\\";
            } else if (value == OPERATOR_BITWISEAND.getValue())    {
                return "&";
            } else if (value == OPERATOR_BITWISEOR.getValue())    {
                return "|";
            } else if (value == OPERATOR_BITWISEXOR.getValue())    {
                return "^";
            } else if (value == OPERATOR_POWER.getValue())    {
                return "**";
            } else if (value == OPERATOR_POSSIGN.getValue())    {
                return "+";
            } else if (value == OPERATOR_NEGSIGN.getValue())    {
                return "-";
            } else if (value == OPERATOR_FALSE.getValue())    {
                return "!";
            } else if (value == OPERATOR_NOT.getValue())    {
                return "~";
            } else if (value == OPERATOR_FACTORIAL.getValue())    {
                return "!";
            } else if (value == OPERATOR_PERCENT.getValue())    {
                return "%";
            } else if (value == OPERATOR_TRANSPOSE.getValue())    {
                return "'";
            } else if (value == OPERATOR_LEFTPARENTHESE.getValue())    {
                return "(";
            } else if (value == OPERATOR_RIGHTPARENTHESE.getValue())    {
                return ")";
            } else if (value == OPERATOR_STARTEND.getValue())    {
                return ";";
            } else    {   // highest priority
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_NON_EXISTING_OPERATOR_CANNOT_OUTPUT);
            }
        }
    };  

    public static class BoundOperator implements Serializable
    {
		private static final long serialVersionUID = 1L;
		private OPERATORTYPES m_OPTRBoundOperator;  
        private int m_nOperatorLevel;  
        public BoundOperator() 
        {
            m_OPTRBoundOperator = OPERATORTYPES.OPERATOR_NOTEXIST;
            m_nOperatorLevel = 0;
        }
        public BoundOperator(int nStartPosition,  
                           int nEndPosition,    
                           OPERATORTYPES OPTRBoundOperator, 
                           int nOperatorLevel)   
        {
            setBoundOperator(nStartPosition, nEndPosition, OPTRBoundOperator, nOperatorLevel);
        }    
            
        private void setBoundOperator(int nStartPosition,  
                                   int nEndPosition,    
                                   OPERATORTYPES OPTRBoundOperator, 
                                   int nOperatorLevel)  
        {
            m_OPTRBoundOperator = OPTRBoundOperator;
            m_nOperatorLevel = nOperatorLevel;
        }
        public boolean isEqual(OPERATORTYPES OPTRBoundOperator) 
        {
            if(m_OPTRBoundOperator == OPTRBoundOperator)
                return true;
            return false;
        }    
        public boolean isEqual(int nOperatorLevel)  
        {
            if(m_nOperatorLevel == nOperatorLevel)
                return true;
            return false;
        }    
        public OPERATORTYPES getOperatorType()
        {
            return m_OPTRBoundOperator;
        }
        public int getOperatorLevel()
        {
            return m_nOperatorLevel;
        }
    }

    public static class CalculateOperator implements Serializable   // this class should be immutable.
    {
		private static final long serialVersionUID = 1L;
		private OPERATORTYPES m_OPTRCalcOperator;   
        private int m_nOperandNum;  
        private boolean m_boolLabelPrefix; 
        public CalculateOperator()
        {
            m_OPTRCalcOperator = OPERATORTYPES.OPERATOR_NOTEXIST;
            m_nOperandNum = -1;
            m_boolLabelPrefix = true;
        }
        public CalculateOperator(OPERATORTYPES OPTRCalcOperator,  
                                int nOperandNum, 
                                boolean boolLabelPrefix)  
        {
            setCalculateOperator(OPTRCalcOperator, nOperandNum, boolLabelPrefix);
        }    
        public CalculateOperator(OPERATORTYPES OPTRCalcOperator,  
                                int nOperandNum)  
        {
            setCalculateOperator(OPTRCalcOperator, nOperandNum, true);
        }    
            
        private void setCalculateOperatorValue(OPERATORTYPES OPTRCalcOperator,
                                            int nOperandNum,
                                            boolean boolLabelPrefix)
        {
            m_OPTRCalcOperator = OPTRCalcOperator;
            m_nOperandNum = nOperandNum;
            m_boolLabelPrefix = boolLabelPrefix;
        }    
        private void setCalculateOperatorValue(OPERATORTYPES OPTRCalcOperator,
                                            int nOperandNum)
        {
            m_OPTRCalcOperator = OPTRCalcOperator;
            m_nOperandNum = nOperandNum;
            m_boolLabelPrefix = true;
        }

        private void setCalculateOperator(OPERATORTYPES OPTRCalcOperator,
                                        int nOperandNum,
                                        boolean boolLabelPrefix)
        {
            setCalculateOperatorValue(OPTRCalcOperator, nOperandNum, boolLabelPrefix);
        }    
            
        private void setCalculateOperator(OPERATORTYPES OPTRCalcOperator,  
                                       int nOperandNum)
        {
            setCalculateOperatorValue(OPTRCalcOperator, nOperandNum, true);
            
        }
            
        public boolean isEqual(OPERATORTYPES OPTRCalcOperator)  
        {
            if(m_OPTRCalcOperator == OPTRCalcOperator)
                return true;
            return false;
        }
        
        public OPERATORTYPES getOperatorType()
        {
            return m_OPTRCalcOperator;
        }
        public int getOperandNum()
        {
            return m_nOperandNum;
        }   
        public boolean getLabelPrefix()
        {
            return m_boolLabelPrefix;
        }
    }
    
}
