/*
 * MFP project, Ptn1VarDeriIdentifierMgr.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jsma;

import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jsma.PatternManager.ABSTRACTEXPRPATTERNS;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;

/**
 *
 * @author tonyc
 */
public class Ptn1VarDeriIdentifierMgr {
    public static Ptn1VarDeriIdentifier createSqrtXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
        Ptn1VarDeriIdentifier pi = new Ptn1VarDeriIdentifier();
        String[] strarrayPatterns = new String[1];
        strarrayPatterns[0] = "::mfp::math::log_exp::sqrt(x)";
        String[] strarrayPatternConditions = new String[0];
        pi.setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_DERI_SQRT_X, strarrayPatterns, strarrayPatternConditions, "x", "0.5*x**(-0.5)");
        return pi;
    }
    
    public static Ptn1VarDeriIdentifier createLogXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
        Ptn1VarDeriIdentifier pi = new Ptn1VarDeriIdentifier();
        String[] strarrayPatterns = new String[4];
        strarrayPatterns[0] = "::mfp::math::log_exp::ln(x)";
        strarrayPatterns[1] = "::mfp::math::log_exp::log(x)";
        strarrayPatterns[2] = "::mfp::math::log_exp::loge(x)";
        strarrayPatterns[3] = "::mfp::math::log_exp::lg(x)";
        String[] strarrayPatternConditions = new String[0];
        pi.setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_DERI_LOG_X, strarrayPatterns, strarrayPatternConditions, "x", "1/x");
        return pi;
    }
    
    public static Ptn1VarDeriIdentifier createSinXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
        Ptn1VarDeriIdentifier pi = new Ptn1VarDeriIdentifier();
        String[] strarrayPatterns = new String[1];
        strarrayPatterns[0] = "::mfp::math::trigon::sin(x)";
        String[] strarrayPatternConditions = new String[0];
        pi.setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_DERI_SIN_X, strarrayPatterns, strarrayPatternConditions, "x", "::mfp::math::trigon::cos(x)");
        return pi;
    }
    
    public static Ptn1VarDeriIdentifier createCosXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
        Ptn1VarDeriIdentifier pi = new Ptn1VarDeriIdentifier();
        String[] strarrayPatterns = new String[1];
        strarrayPatterns[0] = "::mfp::math::trigon::cos(x)";
        String[] strarrayPatternConditions = new String[0];
        pi.setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_DERI_COS_X, strarrayPatterns, strarrayPatternConditions, "x", "-::mfp::math::trigon::sin(x)");
        return pi;
    }
    
    public static Ptn1VarDeriIdentifier createTanXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
        Ptn1VarDeriIdentifier pi = new Ptn1VarDeriIdentifier();
        String[] strarrayPatterns = new String[1];
        strarrayPatterns[0] = "::mfp::math::trigon::tan(x)";
        String[] strarrayPatternConditions = new String[0];
        pi.setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_DERI_TAN_X, strarrayPatterns, strarrayPatternConditions, "x", "1+::mfp::math::trigon::tan(x)**2");
        return pi;
    }
    
    public static Ptn1VarDeriIdentifier createASinXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
        Ptn1VarDeriIdentifier pi = new Ptn1VarDeriIdentifier();
        String[] strarrayPatterns = new String[1];
        strarrayPatterns[0] = "::mfp::math::trigon::asin(x)";
        String[] strarrayPatternConditions = new String[0];
        pi.setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_DERI_ASIN_X, strarrayPatterns, strarrayPatternConditions, "x", "1/(1-x**2)**0.5");
        return pi;
    }
    
    public static Ptn1VarDeriIdentifier createACosXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
        Ptn1VarDeriIdentifier pi = new Ptn1VarDeriIdentifier();
        String[] strarrayPatterns = new String[1];
        strarrayPatterns[0] = "::mfp::math::trigon::acos(x)";
        String[] strarrayPatternConditions = new String[0];
        pi.setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_DERI_ACOS_X, strarrayPatterns, strarrayPatternConditions, "x", "-1/(1-x**2)**0.5");
        return pi;
    }
    
    public static Ptn1VarDeriIdentifier createATanXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
        Ptn1VarDeriIdentifier pi = new Ptn1VarDeriIdentifier();
        String[] strarrayPatterns = new String[1];
        strarrayPatterns[0] = "::mfp::math::trigon::atan(x)";
        String[] strarrayPatternConditions = new String[0];
        pi.setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_DERI_ATAN_X, strarrayPatterns, strarrayPatternConditions, "x", "1/(1+x**2)");
        return pi;
    }
    
    public static Ptn1VarDeriIdentifier createSinhXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
        Ptn1VarDeriIdentifier pi = new Ptn1VarDeriIdentifier();
        String[] strarrayPatterns = new String[1];
        strarrayPatterns[0] = "::mfp::math::trigon::sinh(x)";
        String[] strarrayPatternConditions = new String[0];
        pi.setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_DERI_SINH_X, strarrayPatterns, strarrayPatternConditions, "x", "(::mfp::math::log_exp::exp(x)+::mfp::math::log_exp::exp(-x))/2");
        return pi;
    }
    
    public static Ptn1VarDeriIdentifier createCoshXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
        Ptn1VarDeriIdentifier pi = new Ptn1VarDeriIdentifier();
        String[] strarrayPatterns = new String[1];
        strarrayPatterns[0] = "::mfp::math::trigon::cosh(x)";
        String[] strarrayPatternConditions = new String[0];
        pi.setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_DERI_COSH_X, strarrayPatterns, strarrayPatternConditions, "x", "(::mfp::math::log_exp::exp(x)-::mfp::math::log_exp::exp(-x))/2");
        return pi;
    }
    
    public static Ptn1VarDeriIdentifier createTanhXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
        Ptn1VarDeriIdentifier pi = new Ptn1VarDeriIdentifier();
        String[] strarrayPatterns = new String[1];
        strarrayPatterns[0] = "::mfp::math::trigon::tanh(x)";
        String[] strarrayPatternConditions = new String[0];
        pi.setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_DERI_TANH_X, strarrayPatterns, strarrayPatternConditions, "x", "4/(::mfp::math::log_exp::exp(x)+::mfp::math::log_exp::exp(-x))**2");
        return pi;
    }
    
    public static Ptn1VarDeriIdentifier createASinhXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
        Ptn1VarDeriIdentifier pi = new Ptn1VarDeriIdentifier();
        String[] strarrayPatterns = new String[1];
        strarrayPatterns[0] = "::mfp::math::trigon::asinh(x)";
        String[] strarrayPatternConditions = new String[0];
        pi.setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_DERI_ASINH_X, strarrayPatterns, strarrayPatternConditions, "x", "1/(1+x**2)**0.5");
        return pi;
    }
    
    public static Ptn1VarDeriIdentifier createACoshXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
        Ptn1VarDeriIdentifier pi = new Ptn1VarDeriIdentifier();
        String[] strarrayPatterns = new String[1];
        strarrayPatterns[0] = "::mfp::math::trigon::acosh(x)";
        String[] strarrayPatternConditions = new String[0];
        pi.setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_DERI_ACOSH_X, strarrayPatterns, strarrayPatternConditions, "x", "1/(x**2-1)**0.5");
        return pi;
    }
    
    public static Ptn1VarDeriIdentifier createATanhXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
        Ptn1VarDeriIdentifier pi = new Ptn1VarDeriIdentifier();
        String[] strarrayPatterns = new String[1];
        strarrayPatterns[0] = "::mfp::math::trigon::atanh(x)";
        String[] strarrayPatternConditions = new String[0];
        pi.setPtn1VarDeriIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_DERI_ATANH_X, strarrayPatterns, strarrayPatternConditions, "x", "1/(1-x**2)");
        return pi;
    }
}
