// MFP project, OoErrProcessor.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Oomfp;

public class OoErrProcessor {
    /* Definition of error types. */
    public static enum ERRORTYPES
    {
        /* No error */
        NO_ERROR_STATE,
        /* Invalid class or citing space */
        ERROR_INVALID_CLASS_OR_CITINGSPACE,
        /* space names don't match */
        ERROR_SPACE_NAMES_DONT_MATCH,
        /* space name conflict */
        ERROR_SPACE_NAME_CONFLICT,
    }

    /* Definition of the error structure. */
    public static class StructError
    {
        public ERRORTYPES m_enumErrorType;
        public String m_strUserDefMsg;
        
        public String getErrorType()    {
            String strErrorType = "NO_EXCEPTION";
            switch(m_enumErrorType)    /* Find the corresponding error type. */
            {
            case ERROR_INVALID_CLASS_OR_CITINGSPACE:
                strErrorType = "INVALID_CLASS_OR_CITINGSPACE";
                break;
            case ERROR_SPACE_NAMES_DONT_MATCH:
                strErrorType = "SPACE_NAMES_DONT_MATCH";
                break;
            case ERROR_SPACE_NAME_CONFLICT:
                strErrorType = "SPACE_NAME_CONFLICT";
                break;
            default:
                ;    /*NO_ERROR_STATE returns "NO_EXCEPTION"*/
            }
        
            return strErrorType;            
        }
        
        public String getErrorInfo()    {
            /* Handle error. */
            String strErrorMsg = "";    /* The string to save error
                                                    information. */
        
            switch(m_enumErrorType)    /* Find the corresponding error type. */
            {
            case ERROR_INVALID_CLASS_OR_CITINGSPACE:
                strErrorMsg = "Invalid class or citingspace!";
                break;
            case ERROR_SPACE_NAMES_DONT_MATCH:
                strErrorMsg = "Space names don't match!";
                break;
            case ERROR_SPACE_NAME_CONFLICT:
                strErrorMsg = "Space name conflict!";
                break;
            default:
                ;    /*NO_ERROR_STATE returns null*/
            };
        
            return strErrorMsg;
        }
    }
    
    public static class JOoMFPErrException extends Exception    {
        private static final long serialVersionUID = 1L;
        public StructError m_se = new StructError();
        public String m_strBlockName = null;
        public Exception m_exceptionLowerLevel = null;
        public JOoMFPErrException()    {
            m_se.m_enumErrorType = ERRORTYPES.NO_ERROR_STATE;
            m_se.m_strUserDefMsg = "";
            m_strBlockName = "";
            m_exceptionLowerLevel = null;
        }
        public JOoMFPErrException(ERRORTYPES e)    {
            m_se.m_enumErrorType = e;
            m_se.m_strUserDefMsg = "";
            m_strBlockName = null;
            m_exceptionLowerLevel = null;
        }
        public JOoMFPErrException(ERRORTYPES e, String strBlockName, Exception exceptionLowerLevel)    {
            m_se.m_enumErrorType = e;
            m_se.m_strUserDefMsg ="";
            m_strBlockName = strBlockName;
            m_exceptionLowerLevel = exceptionLowerLevel;
        }
        public JOoMFPErrException(ERRORTYPES e, String strUserDefMsg)    {
            m_se.m_enumErrorType = e;
            m_se.m_strUserDefMsg = strUserDefMsg;
            m_strBlockName = null;
            m_exceptionLowerLevel = null;
        }
        public JOoMFPErrException(ERRORTYPES e, String strUserDefMsg,
                String strBlockName, Exception exceptionLowerLevel)    {
            m_se.m_enumErrorType = e;
            m_se.m_strUserDefMsg =  strUserDefMsg;
            m_strBlockName = strBlockName;
            m_exceptionLowerLevel = exceptionLowerLevel;
        }
        public JOoMFPErrException(StructError se)    {
            m_se.m_enumErrorType = se.m_enumErrorType;
            m_se.m_strUserDefMsg = se.m_strUserDefMsg;
            m_strBlockName = "";
            m_exceptionLowerLevel = null;
        }
        public JOoMFPErrException(StructError se,
                String strBlockName, Exception exceptionLowerLevel)    {
            m_se.m_enumErrorType = se.m_enumErrorType;
            m_se.m_strUserDefMsg = se.m_strUserDefMsg;
            m_strBlockName = strBlockName;
            m_exceptionLowerLevel = exceptionLowerLevel;
        }
    }
}
