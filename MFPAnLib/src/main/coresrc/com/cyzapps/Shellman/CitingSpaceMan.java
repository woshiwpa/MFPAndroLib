/*
 * MFP project, CitingSpaceMan.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Shellman;

import com.cyzapps.adapter.MFPAdapter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author tonyc
 */
public class CitingSpaceMan {
    
    // developer should ensure that all the saved citing spaces are small cases.
    
    static final public List<String[]> PERMENANT_CITING_SPACES = MFPAdapter.getAllCitingSpaces(null);    // the citing spaces cannot be deleted.

    // In a linked list, front ones always have higher priority than later ones
    public LinkedList<String[]> mAllCSes = new LinkedList<String[]>();
    
    public CitingSpaceMan() {
        mAllCSes.addAll(MFPAdapter.getAllCitingSpaces(null));
    }
    
    public String listAllCitingSpaces() {
        
        String strAllCSes = "";
        
        for ( String[] cs : mAllCSes) {
            String strCS = "";
            for (int idx = 0; idx < cs.length; idx ++) {
                if (idx > 0) {
                    strCS += "::";
                }
                strCS += cs[idx];
            }
            boolean bIsPermenant = false;
            for (String[] csPerm : PERMENANT_CITING_SPACES) {
                if (Arrays.equals(cs, csPerm))  { // both of them are small cases. so can compare directly
                    bIsPermenant = true;  // this means it cannot be deleted.
                    break;
                }
            }
            if (strCS.length() == 0) {
                strCS = "(TOP LEVEL Citingspace)";
            }
            strCS = (bIsPermenant?"! ":"  ") + strCS;
            strCS += "\n";
            strAllCSes += strCS;
        }
        return strAllCSes;
    }
    
    // this function adds a new citing space. If the citing space has been existed, push it up to top.
    // also, cs must not include any big letters
    // return 0 if the cs already exists, or 1 if cs will be added.
    protected int addCitingSpace(String[] cs) {
        for ( Iterator<String[]> itrExist = mAllCSes.iterator(); itrExist.hasNext(); ) {
            String[] csExist = itrExist.next();
            if (Arrays.equals(cs, csExist)) {
                itrExist.remove();  // remove all the same cs.
            }
        }
            
        // now cs does not exist, need to add.
        if (mAllCSes.size() == 0) {   // no cs
            mAllCSes.add(0, cs);
        } else if (cs.length == 1 && cs[0].length() == 0) { // cs is top level.
            mAllCSes.add(0, cs);
        } else if (mAllCSes.get(0).length == 1 && mAllCSes.get(0)[0].length() == 0) { // top level is at the first.
            mAllCSes.add(1, cs);
        } else {
            mAllCSes.add(0, cs);    // top level is not at the first.
        }
        return 1;
    }
    
    // string based cs could be relative or absolute, and could include large letters.
    public static String[] getAbsCitingSpace(String strCS, List<String[]> allCSes) {
        String strProcessedCS = strCS.trim().toLowerCase(Locale.US);
        if (strProcessedCS.length() == 0) {
            // ok, add root cs.
            return new String[] {""};
        } else if (strProcessedCS.charAt(strProcessedCS.length() - 1) == ':') {
            return null;  // invalid strCS.
        } else if (strProcessedCS.charAt(0) == ':') {
            // this is an absolute cs.
            String[] cs = strProcessedCS.split("::");
            for (int idx = 0; idx < cs.length; idx ++) {
                cs[idx] = cs[idx].trim();
            }
            return cs;
        } else {
            // this is a relative cs, use highest priority cs as a reference. If priority
            // cs is something like ::...::...::*, then check the one before until we see
            // a cs without wild card char.
            String[] cs = strProcessedCS.split("::");
            String[] csParent = new String[]{""};
            if (allCSes != null) {
                for (String[] csExist : allCSes) {
                    if (csExist.length != 0 && !csExist[csExist.length - 1].equals("*")) {
                        csParent = csExist;   // get it.
                        break;
                    }
                }
            }
            // ok, now lets create a new CS.
            String[] csNew = new String[csParent.length + cs.length];
            for (int idx = 0; idx < csNew.length; idx ++) {
                csNew[idx] = (idx < csParent.length)?csParent[idx]:cs[idx - csParent.length].trim();
            }
            return csNew;
        }
    }
    
    // string based cs could be relative or absolute, and could include large letters.
    public int addCitingSpace(String strCS) {
        String[] cs = getAbsCitingSpace(strCS, mAllCSes);
        if (cs == null || cs.length == 0) {
            return -1;
        } else {
            return addCitingSpace(cs);
        }
    }

    // delete a cs, note that cs must be absolute. But strCS need not to be small letter and not have to be shrinked.
    public boolean deleteCitingSpace(String strCS) {
        String[] cs = strCS.toLowerCase(Locale.US).split("::");
        for (int idx = 0; idx < cs.length; idx ++) {
            cs[idx] = cs[idx].trim();
        }
        if (cs[0].length() == 0) {
            // absolute citing space
            for (String[] csExist : mAllCSes) {
                if (!Arrays.equals(cs, csExist)) {
                    continue;
                }
                for (String[] csPerm : PERMENANT_CITING_SPACES) {
                    if (Arrays.equals(cs, csPerm)) {
                        return false;   // it cannot be deleted.
                    }
                }
                mAllCSes.remove(csExist);
                return true;    // it can be deleted.
            }
        } else {
            // relative citing space.
            String[] csParent = new String[]{""};
            for (String[] csExist : mAllCSes) {
                if (csExist.length != 0 && !csExist[csExist.length - 1].equals("*")) {
                    csParent = csExist;   // get it.
                    break;
                }
            }
            // ok, now lets create a new CS.
            String[] csNew = new String[csParent.length + cs.length];
            for (int idx = 0; idx < csNew.length; idx ++) {
                csNew[idx] = (idx < csParent.length)?csParent[idx]:cs[idx - csParent.length].trim();
            }
            for (String[] csExist : mAllCSes) {
                if (!Arrays.equals(csNew, csExist)) {
                    continue;
                }
                for (String[] csPerm : PERMENANT_CITING_SPACES) {
                    if (Arrays.equals(csNew, csPerm)) {
                        return false;   // it cannot be deleted.
                    }
                }
                mAllCSes.remove(csExist);
                return true;    // it can be deleted.
            }
        }
        // cannot find matched cs
        return false;
    }

    // clear cs space
    public void clearCitingSpace() {
        mAllCSes.clear();
        mAllCSes.addAll(PERMENANT_CITING_SPACES);
    }
}
