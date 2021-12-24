/*
 * MFP project, ProgContext.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.adapter.MFPAdapter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


/**
 *
 * @author tonyc
 * This class needs to be serialized. However, we cannot use default serializable way because
 * we need to send text based codes and citingspaces to remote. We cannot send binary bytes and
 * deserialize them because sender and receiver may be using different version of class.
 * Also, don't worry too much about AExpr type dataclass. AExpr type dataclass is used in solver,
 * however, call block cannot inside solver so we will not see AExpr type in mlVarNamesSpaces.
 * And call block cannot be called in annotation, so that mstCallingAnnotation must be null.
 * Therefore, it is same to use gson to serialize progcontext.
 */
public class ProgContext implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public class StaticProgContext implements Serializable {    
		private static final long serialVersionUID = 1L;
		protected Statement_function mstCallingFunc = null;
        public void setCallingFunc(Statement_function stCallingFunc) {
            mstCallingFunc = stCallingFunc;
        }
        public Statement_function getCallingFunc() {
            return mstCallingFunc;
        }
        
        // for some functions called by annotation, there is no calling function
        // especially if the annotation is defined out of the scope of any function.
        protected Annotation mstCallingAnnotation = null;
        public void setCallingAnnotation(Annotation stCallingAnnotation) {
            mstCallingAnnotation = stCallingAnnotation;
        }
        public Annotation getCallingAnnotation() {
            return mstCallingAnnotation;
        }
        
        // if explicit cs is set, it overrides mstCallingFunc
        protected LinkedList<String[]> mexplicitCitingSpaces = null;
        public void setCitingSpacesExplicitly(List<String[]> explicitCitingSpaces) {
            if (null == explicitCitingSpaces) {
                mexplicitCitingSpaces = null;
            } else {
                if (null == mexplicitCitingSpaces) {
                    mexplicitCitingSpaces = new LinkedList<String[]>();
                } else {
                    mexplicitCitingSpaces.clear();
                }
                mexplicitCitingSpaces.addAll(explicitCitingSpaces);
            }
        }
        public LinkedList<String[]> getExplicitCitingSpaces() {
            return mexplicitCitingSpaces;
        }
        
        /**
         * This function returns a deep copy of citing space list.
         * @return 
         */
        public List<String[]> getCitingSpaces() {
            if (null != mexplicitCitingSpaces) {
                LinkedList<String[]> listCSes = new LinkedList<String[]>();
                listCSes.addAll(mexplicitCitingSpaces);
                return listCSes;
            } else if (mstCallingFunc != null) {
                return MFPAdapter.getAllCitingSpaces(mstCallingFunc.m_lCitingSpaces);
            } else {
                return MFPAdapter.getAllCitingSpaces(null);
            }
        }
    }
    public StaticProgContext mstaticProgContext = new StaticProgContext();
    
    public class DynamicProgContext implements Serializable {
		private static final long serialVersionUID = 1L;
		public LinkedList<LinkedList<Variable>> mlVarNameSpaces = new LinkedList<LinkedList<Variable>>();   // this one is heritaged from upper level.
    }
    
    public DynamicProgContext mdynamicProgContext = new DynamicProgContext();
    
    public ProgContext() {}
    
    public ProgContext(ProgContext progContext) {
        mstaticProgContext.mstCallingFunc = progContext.mstaticProgContext.mstCallingFunc;
        mstaticProgContext.mstCallingAnnotation = progContext.mstaticProgContext.mstCallingAnnotation;
        mstaticProgContext.setCitingSpacesExplicitly(progContext.mstaticProgContext.mexplicitCitingSpaces);
        
        mdynamicProgContext.mlVarNameSpaces = VariableOperator.copyVarSpaces(progContext.mdynamicProgContext.mlVarNameSpaces);
    }
}
