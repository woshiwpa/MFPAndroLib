/*
 * MFP project, SpaceMember.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Oomfp;

import java.io.Serializable;

/**
 *
 * @author tonyc
 */
public class SpaceMember implements Serializable {
	private static final long serialVersionUID = 1L;

    public static enum AccessRestriction {
        PUBLIC {
            @Override
            public String toString() {
                return "public";
            }
        },
        PRIVATE {
            @Override
            public String toString() {
                return "private";
            }
        }
    }
    public AccessRestriction maccess = AccessRestriction.PUBLIC;  // default is public
}
