// MFP project, ErrorProcessor.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jmfp;

import java.io.Serializable;

public class ErrorProcessor {
    public static enum ERRORTYPES
    {
        /* No error */
        NO_ERROR_FOUND,
        /* Unrecognized statement */
        UNRECOGNIZED_STATEMENT,
        /* Unfinished statement */
        UNFINISHED_STATEMENT,
        /* Bad function name */
        BAD_FUNCTION_NAME,
        /* Bad function defintion */
        BAD_FUNCTION_DEFINITION,
        /* Embedded function definition */
        EMBEDDED_FUNCTION_DEFINITION,
        /* Incorrect number of parameters */
        INCORRECT_NUMBER_OF_PARAMETERS,
        /* Incomplete function */
        INCOMPLETE_FUNCTION,
        /* Incomplete class */
        INCOMPLETE_CLASS,
        /* Incomplete block */
        INCOMPLETE_BLOCK,
        /* No valid definition */
        NO_VALID_DEFINITION,
        /* Cannot find beginning of block */
        CANNOT_FIND_BEGINNING_OF_BLOCK,
        /* Should not after previous statement */
        SHOULD_NOT_AFTER_PREVIOUS_STATEMENT,
        /* Is keyword */
        IS_KEYWORD,
        /* No parameter definition border */
        NO_PARAMETER_DEFINITION_BORDER,
        /* Bad variable name */
        BAD_VARIABLE_NAME,
        /* Duplicate parameter */
        DUPLICATE_PARAMETER,
        /* Lack of return */
        LACK_OF_RETURN,
        /* No variable */
        NO_VARIABLE,
        /* No value obtained from expression (return nothing?) */
        NO_VALUE_OBTAINED_FROM_EXPRESSION,
        /* Invalid value obtained from expression (not a numeric value?) */
        INVALID_VALUE_OBTAINED_FROM_EXPRESSION,        
        /* No condition */
        NO_CONDITION,
        /* Invalid for statement */
        INVALID_FOR_STATEMENT,
        /* Invalid call statement */
        INVALID_CALL_STATEMENT,
        /* Access keyword cannot be here */
        ACCESS_KEYWORD_CANNOT_BE_HERE,
        /* Invalid statement */
        INVALID_STATEMENT,
        /* Invalid annotation input */
        INVALID_ANNOTATION_INPUT,
        /* Invalid catch filter */
        INVALID_CATCH_FILTER,
        /* Need expression */
        NEED_EXPRESSION,
        /* Need constant expression */
        NEED_CONSTANT_EXPRESSION,
        /* Invalid assignment statement */
        INVALID_ASSIGNMENT_STATEMENT,
        /* Invalid expression */
        INVALID_EXPRESSION,
        /* Undefined variable */
        UNDEFINED_VARIABLE,
        /* Redefined variable */
        REDEFINED_VARIABLE,
        /* Wrong variable type */
        WRONG_VARIABLE_TYPE,
        /* No open bracket */
        NO_OPEN_BRACKET,
        /* No close bracket */
        NO_CLOSE_BRACKET,
        /* Cannot find close quatation mark for string */
        CANNOT_FIND_CLOSE_QUATATION_MARK_FOR_STRING,
        /* Invalid solver */
        INVALID_SOLVER,
        /* Invalid call block */
        INVALID_CALL_BLOCK,
        /* Cannot reuse variable for call block */
        CANNOT_REUSE_VARIABLE_FOR_CALL_BLOCK,
        /* Cannot find lib */
        CANNOT_FIND_LIB,
        /* Invalid expression to solve */
        INVALID_EXPRESSION_TO_SOLVE,
        /* Undefined function */
        UNDEFINED_FUNCTION,
        /* Undefined class */
        UNDEFINED_CLASS,
        /* Undefined parent class */
        UNDEFINED_PARENT_CLASS,
        /* Invalid citingspace */
        INVALID_CITINGSPACE,
        /* Invalid class */
        INVALID_CLASS,
        /* Cannot create call object */
        CANNOT_CREATE_CALL_OBJECT,
        /* Cannot serialize stack */
        CANNOT_SERIALIZE_STACK,
        /* Cannot serialize file */
        CANNOT_SERIALIZE_FILE,
        /* Interrupted operation */
        INTERRUPTED_OPERATION,
        /* User defined exception */
        USER_DEFINED_EXCEPTION,
    }
    
    /* Definition of the error structure. */
    public static class StructError implements Serializable
    {
        private static final long serialVersionUID = 1L;
	
        public String m_strSrcFile = "";
        public int m_nStartLineNo = 0;
        public int m_nEndLineNo = 0;
        public ERRORTYPES m_enumErrorType = ERRORTYPES.NO_ERROR_FOUND;
        public String m_strUserDefMsg = "";
        
        public String getErrorType()    {
            String strErrorType = "";
            switch(m_enumErrorType)    {
            case NO_ERROR_FOUND:
                strErrorType = "NO_EXCEPTION";
                break;
            case UNRECOGNIZED_STATEMENT:
                strErrorType = "UNRECOGNIZED_STATEMENT_EXCEPTION";
                break;
            case UNFINISHED_STATEMENT:
                strErrorType = "UNFINISHED_STATEMENT_EXCEPTION";
                break;
            case BAD_FUNCTION_NAME:
                strErrorType = "BAD_FUNCTION_NAME_EXCEPTION";
                break;
            case BAD_FUNCTION_DEFINITION:
                strErrorType = "BAD_FUNCTION_DEFINITION_EXCEPTION";
                break;
            case EMBEDDED_FUNCTION_DEFINITION:
                strErrorType = "EMBEDDED_FUNCTION_DEFINITION_EXCEPTION";
                break;
            case INCORRECT_NUMBER_OF_PARAMETERS:
                strErrorType = "INCORRECT_NUMBER_OF_PARAMETERS_EXCEPTION";
                break;
            case INCOMPLETE_FUNCTION:
                strErrorType = "INCOMPLETE_FUNCTION_EXCEPTION";
                break;
            case INCOMPLETE_CLASS:
                strErrorType = "INCOMPLETE_CLASS_EXCEPTION";
                break;
            case INCOMPLETE_BLOCK:
                strErrorType = "INCOMPLETE_BLOCK_EXCEPTION";
                break;
            case NO_VALID_DEFINITION:
                strErrorType = "NO_VALID_DEFINITION_EXCEPTION";
                break;
            case CANNOT_FIND_BEGINNING_OF_BLOCK:
                strErrorType = "CANNOT_FIND_BEGINNING_OF_BLOCK_EXCEPTION";
                break;
            case SHOULD_NOT_AFTER_PREVIOUS_STATEMENT:
                strErrorType = "SHOULD_NOT_AFTER_PREVIOUS_STATEMENT_EXCEPTION";
                break;
            case IS_KEYWORD:
                strErrorType = "IS_KEYWORD_EXCEPTION";
                break;
            case NO_PARAMETER_DEFINITION_BORDER:
                strErrorType = "NO_PARAMETER_DEFINITION_BORDER_EXCEPTION";
                break;
            case BAD_VARIABLE_NAME:
                strErrorType = "BAD_VARIABLE_NAME_EXCEPTION";
                break;
            case DUPLICATE_PARAMETER:
                strErrorType = "DUPLICATE_PARAMETER_EXCEPTION";
                break;
            case LACK_OF_RETURN:
                strErrorType = "LACK_OF_RETURN_EXCEPTION";
                break;
            case NO_VARIABLE:
                strErrorType = "NO_VARIABLE_EXCEPTION";
                break;
            case NO_VALUE_OBTAINED_FROM_EXPRESSION:
                strErrorType = "NO_VALUE_OBTAINED_FROM_EXPRESSION_EXCEPTION";
                break;
            case INVALID_VALUE_OBTAINED_FROM_EXPRESSION:
                strErrorType = "INVALID_VALUE_OBTAINED_FROM_EXPRESSION_EXCEPTION";
                break;
            case NO_CONDITION:
                strErrorType = "NO_CONDITION_EXCEPTION";
                break;
            case INVALID_FOR_STATEMENT:
                strErrorType = "INVALID_FOR_STATEMENT_EXCEPTION";
                break;
            case INVALID_CALL_STATEMENT:
                strErrorType = "INVALID_CALL_STATEMENT_EXCEPTION";
                break;
            case ACCESS_KEYWORD_CANNOT_BE_HERE:
                strErrorType = "ACCESS_KEYWORD_CANNOT_BE_HERE";
                break;
            case INVALID_STATEMENT:
                strErrorType = "INVALID_STATEMENT_EXCEPTION";
                break;
            case INVALID_ANNOTATION_INPUT:
                strErrorType = "INVALID_ANNOTATION_INPUT_EXCEPTION";
                break;
            case INVALID_CATCH_FILTER:
                strErrorType = "INVALID_CATCH_FILTER_EXCEPTION";
                break;
            case NEED_EXPRESSION:
                strErrorType = "NEED_EXPRESSION_EXCEPTION";
                break;
            case NEED_CONSTANT_EXPRESSION:
                strErrorType = "NEED_CONSTANT_EXPRESSION_EXCEPTION";
                break;
            case INVALID_ASSIGNMENT_STATEMENT:
                strErrorType = "INVALID_ASSIGNMENT_STATEMENT_EXCEPTION";
                break;
            case INVALID_EXPRESSION:
                strErrorType = "INVALID_EXPRESSION_EXCEPTION";
                break;
            case UNDEFINED_VARIABLE:
                strErrorType = "UNDEFINED_VARIABLE_EXCEPTION";
                break;
            case REDEFINED_VARIABLE:
                strErrorType = "REDEFINED_VARIABLE_EXCEPTION";
                break;
            case WRONG_VARIABLE_TYPE:
                strErrorType = "WRONG_VARIABLE_TYPE_EXCEPTION";
                break;
            case NO_OPEN_BRACKET:
                strErrorType = "NO_OPEN_BRACKET_EXCEPTION";
                break;
            case NO_CLOSE_BRACKET:
                strErrorType = "NO_CLOSE_BRACKET_EXCEPTION";
                break;
            case CANNOT_FIND_CLOSE_QUATATION_MARK_FOR_STRING:
                strErrorType = "CANNOT_FIND_CLOSE_QUATATION_MARK_FOR_STRING_EXCEPTION";
                break;
            case INVALID_SOLVER:
                strErrorType = "INVALID_SOLVER_EXCEPTION";
                break;
            case INVALID_CALL_BLOCK:
                strErrorType = "INVALID_CALL_BLOCK_EXCEPTION";
                break;
            case CANNOT_REUSE_VARIABLE_FOR_CALL_BLOCK:
            	strErrorType = "CANNOT_REUSE_VARIABLE_FOR_CALL_BLOCK_EXCEPTION";
            	break;
            case CANNOT_FIND_LIB:
            	strErrorType = "CANNOT_FIND_LIB_EXCEPTION";
            	break;
            case INVALID_EXPRESSION_TO_SOLVE:
                strErrorType = "INVALID_EXPRESSION_TO_SOLVE_EXCEPTION";
                break;
            case UNDEFINED_FUNCTION:
                strErrorType = "UNDEFINED_FUNCTION_EXCEPTION";
                break;
            case UNDEFINED_CLASS:
                strErrorType = "UNDEFINED_CLASS_EXCEPTION";
                break;
            case UNDEFINED_PARENT_CLASS:
                strErrorType = "UNDEFINED_PARENT_CLASS_EXCEPTION";
                break;
            case INVALID_CITINGSPACE:
                strErrorType = "INVALID_CITINGSPACE_EXCEPTION";
                break;
            case INVALID_CLASS:
                strErrorType = "INVALID_CLASS_EXCEPTION";
                break;
            case CANNOT_CREATE_CALL_OBJECT:
                strErrorType = "CANNOT_CREATE_CALL_OBJECT_EXCEPTION";
                break;
            case CANNOT_SERIALIZE_STACK:
                strErrorType = "CANNOT_SERIALIZE_STACK_EXCEPTION";
                break;
            case CANNOT_SERIALIZE_FILE:
                strErrorType = "CANNOT_SERIALIZE_FILE_EXCEPTION";
                break;
            case INTERRUPTED_OPERATION:
                strErrorType = "INTERRUPTED_OPERATION_EXCEPTION";
                break;
            case USER_DEFINED_EXCEPTION:
                strErrorType = "USER_DEFINED_EXCEPTION";
                break;
            default:
                strErrorType = "UNRECOGNIZED_EXCEPTION";
            }
            return strErrorType;
        }
        
        public String getErrorInfo()    {
            String strErrorInfo = "";
            switch(m_enumErrorType)    {
            case NO_ERROR_FOUND:
                strErrorInfo = "No error found";
                break;
            case UNRECOGNIZED_STATEMENT:
                strErrorInfo = "Unrecognized statement";
                break;
            case UNFINISHED_STATEMENT:
                strErrorInfo = "Unfinished statement";
                break;
            case BAD_FUNCTION_NAME:
                strErrorInfo = "Function name should start from a letter, and only include letters, digits and '_'";
                break;
            case BAD_FUNCTION_DEFINITION:
                strErrorInfo = "Function defintion is wrong";
                break;
            case EMBEDDED_FUNCTION_DEFINITION:
                strErrorInfo = "Function defintion should not be embedded";
                break;
            case INCORRECT_NUMBER_OF_PARAMETERS:
                strErrorInfo = "Incorrect number of parameters";
                break;
            case INCOMPLETE_FUNCTION:
                strErrorInfo = "Function is incomplete";
                break;
            case INCOMPLETE_CLASS:
                strErrorInfo = "Class is incomplete";
                break;
            case INCOMPLETE_BLOCK:
                strErrorInfo = "Block is incomplete, e.g. no next after for";
                break;
            case NO_VALID_DEFINITION:
                strErrorInfo = "No valid definition, e.g. class or function";
                break;
            case CANNOT_FIND_BEGINNING_OF_BLOCK:
                strErrorInfo = "Cannot find beginning of block, e.g. no endif after if";
                break;
            case SHOULD_NOT_AFTER_PREVIOUS_STATEMENT:
                strErrorInfo = "This statement should not be after its previous statement";
                break;
            case IS_KEYWORD:
                strErrorInfo = "Function or variable name should not be a language keyword";
                break;
            case NO_PARAMETER_DEFINITION_BORDER:
                strErrorInfo = "Cannot find parameter definition border";
                break;
            case BAD_VARIABLE_NAME:
                strErrorInfo = "Variable name should start from a letter or '_', and only include letters, digits and '_'";
                break;
            case DUPLICATE_PARAMETER:
                strErrorInfo = "Duplicate parameter found";
                break;
            case LACK_OF_RETURN:
                strErrorInfo = "A value, a variable or an expression should be returned";
                break;
            case NO_VARIABLE:
                strErrorInfo = "No variable defined";
                break;
            case NO_VALUE_OBTAINED_FROM_EXPRESSION:
                strErrorInfo = "No value obtained from expression";
                break;
            case INVALID_VALUE_OBTAINED_FROM_EXPRESSION:
                strErrorInfo = "Invalid value obtained from expression";
                break;
            case NO_CONDITION:
                strErrorInfo = "No condition statement after if, elseif, while or until keyword";
                break;
            case INVALID_FOR_STATEMENT:
                strErrorInfo = "Invalid for statement";
                break;
            case INVALID_CALL_STATEMENT:
                strErrorInfo = "Invalid call statement";
                break;
            case ACCESS_KEYWORD_CANNOT_BE_HERE:
                strErrorInfo = "Access keyword cannot be here";
                break;
            case INVALID_STATEMENT:
                strErrorInfo = "Invalid statement";
                break;
            case INVALID_ANNOTATION_INPUT:
                strErrorInfo = "Invalid annotation input";
                break;
            case INVALID_CATCH_FILTER:
                strErrorInfo = "Invalid filter for catch statement";
                break;
            case NEED_EXPRESSION:
                strErrorInfo = "A expression is needed";
                break;
            case NEED_CONSTANT_EXPRESSION:
                strErrorInfo = "A constant expression is needed after case keyword";
                break;
            case INVALID_ASSIGNMENT_STATEMENT:
                strErrorInfo = "Invalid assignment statement";
                break;
            case INVALID_EXPRESSION:
                strErrorInfo = "Invalid expression";
                break;
            case UNDEFINED_VARIABLE:
                strErrorInfo = "Undefined variable";
                break;
            case REDEFINED_VARIABLE:
                strErrorInfo = "Redefined variable";
                break;
            case WRONG_VARIABLE_TYPE:
                strErrorInfo = "Wrong variable type";
                break;
            case NO_OPEN_BRACKET:
                strErrorInfo = "No open bracket";
                break;
            case NO_CLOSE_BRACKET:
                strErrorInfo = "No close bracket";
                break;
            case CANNOT_FIND_CLOSE_QUATATION_MARK_FOR_STRING:
                strErrorInfo = "Cannot find close quatation mark for string";
                break;
            case INVALID_SOLVER:
                strErrorInfo = "Invalid solver";
                break;
            case INVALID_CALL_BLOCK:
                strErrorInfo = "Invalid call block";
                break;
            case CANNOT_REUSE_VARIABLE_FOR_CALL_BLOCK:
            	strErrorInfo = "Cannot reuse variable for call block";
            	break;
            case CANNOT_FIND_LIB:
            	strErrorInfo = "Cannot find lib";
            	break;
            case INVALID_EXPRESSION_TO_SOLVE:
                strErrorInfo = "Invalid expression to solve";
                break;
            case UNDEFINED_FUNCTION:
                strErrorInfo = "Undefined function";
                break;
            case UNDEFINED_CLASS:
                strErrorInfo = "Undefined class";
                break;
            case UNDEFINED_PARENT_CLASS:
                strErrorInfo = "Undefined parent class";
                break;
            case INVALID_CITINGSPACE:
                strErrorInfo = "Invalid citingspace";
                break;
            case INVALID_CLASS:
                strErrorInfo = "Invalid class";
                break;
            case CANNOT_CREATE_CALL_OBJECT:
                strErrorInfo = "Cannot create call object";
                break;
            case CANNOT_SERIALIZE_STACK:
                strErrorInfo = "Cannot serialize stack";
                break;
            case CANNOT_SERIALIZE_FILE:
                strErrorInfo = "Cannot serialize file";
                break;
            case INTERRUPTED_OPERATION:
                strErrorInfo = "Interrupted operation";
                break;
            case USER_DEFINED_EXCEPTION:
                strErrorInfo = m_strUserDefMsg;
                break;
            default:
                strErrorInfo = "Unrecognized error";
            }
            return strErrorInfo;
        }
    }
    
    public static class JMFPCompErrException extends Exception    {
        private static final long serialVersionUID = 1L;
        public StructError m_se = new StructError();
        public Exception m_exceptionLowerLevel = null;

        public JMFPCompErrException()    {
            m_se.m_strSrcFile = "";
            m_se.m_nStartLineNo = m_se.m_nEndLineNo = 0;
            m_se.m_enumErrorType = ERRORTYPES.NO_ERROR_FOUND;
            m_se.m_strUserDefMsg = "";
            m_exceptionLowerLevel = null;
        }
        public JMFPCompErrException(String strSrcFile, int nStartLineNo, int nEndLineNo, ERRORTYPES e)    {
            m_se.m_strSrcFile = strSrcFile;
            m_se.m_nStartLineNo = nStartLineNo;
            m_se.m_nEndLineNo = nEndLineNo;
            m_se.m_enumErrorType = e;
            m_se.m_strUserDefMsg = "";
            m_exceptionLowerLevel = null;
        }
        public JMFPCompErrException(String strSrcFile, int nStartLineNo, int nEndLineNo, ERRORTYPES e, String strUserDefMsg)    {
            m_se.m_strSrcFile = strSrcFile;
            m_se.m_nStartLineNo = nStartLineNo;
            m_se.m_nEndLineNo = nEndLineNo;
            m_se.m_enumErrorType = e;
            m_se.m_strUserDefMsg = strUserDefMsg;
            m_exceptionLowerLevel = null;
        }
        public JMFPCompErrException(StructError se)    {
            m_se.m_strSrcFile = se.m_strSrcFile;
            m_se.m_nStartLineNo = se.m_nStartLineNo;
            m_se.m_nEndLineNo = se.m_nEndLineNo;
            m_se.m_enumErrorType = se.m_enumErrorType;
            m_se.m_strUserDefMsg = se.m_strUserDefMsg;
            m_exceptionLowerLevel = null;
        }
        public JMFPCompErrException(String strSrcFile, int nStartLineNo, int nEndLineNo, ERRORTYPES e, Exception exceptionLowerLevel)    {
            m_se.m_strSrcFile = strSrcFile;
            m_se.m_nStartLineNo = nStartLineNo;
            m_se.m_nEndLineNo = nEndLineNo;
            m_se.m_enumErrorType = e;
            m_se.m_strUserDefMsg = "";
            m_exceptionLowerLevel = exceptionLowerLevel;
        }
        public JMFPCompErrException(String strSrcFile, int nStartLineNo, int nEndLineNo, ERRORTYPES e, String strUserDefMsg, Exception exceptionLowerLevel)    {
            m_se.m_strSrcFile = strSrcFile;
            m_se.m_nStartLineNo = nStartLineNo;
            m_se.m_nEndLineNo = nEndLineNo;
            m_se.m_enumErrorType = e;
            m_se.m_strUserDefMsg = strUserDefMsg;
            m_exceptionLowerLevel = exceptionLowerLevel;
        }
        public JMFPCompErrException(StructError se, Exception exceptionLowerLevel)    {
            m_se.m_strSrcFile = se.m_strSrcFile;
            m_se.m_nStartLineNo = se.m_nStartLineNo;
            m_se.m_nEndLineNo = se.m_nEndLineNo;
            m_se.m_enumErrorType = se.m_enumErrorType;
            m_se.m_strUserDefMsg = se.m_strUserDefMsg;
            m_exceptionLowerLevel = exceptionLowerLevel;
        }
        
        @Override
        public String getMessage() {
            return m_se.getErrorInfo();
        }
        @Override
        public String getLocalizedMessage() {
            return m_se.getErrorInfo();
        }
    }
}
