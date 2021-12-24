/*
 * MFP project, PlatformHWManager.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.JPlatformHW;

import com.cyzapps.OSAdapter.ParallelManager.CommunicationManager;
import com.cyzapps.OSAdapter.LangFileManager;

/**
 *
 * @author tony
 */
public class PlatformHWManager {  // it is not an abstract class.
    protected LangFileManager mlangFileManager = null;
    
    /**
     * Assume the sub managers cannot be null.
     * @param langFileManager 
     */
    public PlatformHWManager(LangFileManager langFileManager) {
        mlangFileManager = langFileManager;
    }
    
    public LangFileManager getLangFileManager() {
    	return mlangFileManager;
    }
}
