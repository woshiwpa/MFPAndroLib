/*
 * MFP project, MemberVariable.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Oomfp;

/**
 *
 * @author tonyc
 */
public class MemberVariable extends SpaceMember {
	private static final long serialVersionUID = 1L;

	public String mstrTypeName = ""; // type name with (partial CS). Note that this name is shrinked and small lettered.
    
    protected String mstrTypeNameWithFullCS = null;    // this is type name with full CS. If it is null, it hasn't been initialized
    
    protected String[] mstrarrayTypeCS = null;
    public String getTypeNameWithFullCS() {
        //TODO
        throw new java.lang.IllegalStateException("This method hasn't been implemented yet!");
    }
    
    protected String[] getTypeCitingSpaces()  {
        //TODO
        throw new java.lang.IllegalStateException("This method hasn't been implemented yet!");
    }
    
    public String[] mstrarrayFullCS = new String[0];

    public String[] mstrarrayPathSpace = new String[0];    //String[0] means a built-in path space.

}
