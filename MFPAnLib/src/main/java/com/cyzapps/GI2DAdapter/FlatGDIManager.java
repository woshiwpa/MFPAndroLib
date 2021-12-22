/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.GI2DAdapter;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import com.cyzapps.Jfcalc.FuncEvaluator.GraphicDisplayInterfaceManager;
import com.cyzapps.JGI2D.DisplayLib.IGraphicDisplay;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author tony
 */
public class FlatGDIManager extends GraphicDisplayInterfaceManager {

    public static final String FLATGDI_ID = "flatGDI_index";
    public static final String INITIAL_ORIENTATION = "initial_orientation";
    // never remove flatGDI from this list.
    public static List<FlatGDI> mslistFlatGDI = Collections.synchronizedList(new ArrayList<FlatGDI>());

    protected Context mcontext = null; // the context (activity) that calls FlatGDIManager

    public FlatGDIManager(Context context) {
        mcontext = context;
    }

    /**
     * keep in mind that openNewDisplay function should be running in working thread, not main ui thread.
     * although this function provide caters working in ui thread, this mode should avoid to use except
     * in some extreme case (like manually start by pushing a button).
     * @param strTitle
     * @param clr
     * @param bConfirmClose
     * @param size
     * @return null if GDI activity hasn't finish onCreate in 4 s, otherwise GDI.
     */
    @Override
    public IGraphicDisplay openScreenDisplay(String strTitle, com.cyzapps.VisualMFP.Color clr, boolean bConfirmClose,
                                          double[] size, boolean bResizable, int orientation) {
        //
        // although this function provide
        final FlatGDI flatGDI = new FlatGDI();
        flatGDI.mstrTitle = strTitle;
        flatGDI.mcolorBkGrnd = clr;
        flatGDI.mbConfirmClose = bConfirmClose;
        flatGDI.mnDisplayOrientation = FlatGDI.validateDisplayOrientation(orientation);
        mslistFlatGDI.add(flatGDI);
        // startActivity can be called in either main ui thread or a background working thread.
        Intent intent = new Intent(mcontext, ActivityGDIDaemon.class);
        intent.putExtra(FLATGDI_ID, flatGDI.getId());
        intent.putExtra(INITIAL_ORIENTATION, flatGDI.mnDisplayOrientation);
        mcontext.startActivity(intent);
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            // we are not in the ui thread.
            int waitSec = 10;
            for (int cnt = 0; cnt < waitSec; cnt++) {
                if (!flatGDI.isDisplayOnLive()) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        break;
                    }
                } else {
                    break;
                }
            }
            // If flatGDI is on live, it is good. However, if we have waited for a few seconds, but
            // flat GDI is still not on live. we do not return null. This is to cater some extreme case
            // where phone is so slow that it takes ages to start flatGDI.
            // return null;
            return flatGDI;
        } else { // we are in the ui thread. This mode should not be used except in some very special mode.
            return flatGDI;
        }
    }

    @Override
    public void shutdownScreenDisplay(IGraphicDisplay gdi, boolean bByForce) {
        FlatGDI[] allDisplays = mslistFlatGDI.toArray(new FlatGDI[0]);  // convert to an array, this is an atomic operation.
        for (int idx = 0; idx < allDisplays.length; idx ++) {
            if (allDisplays[idx] == gdi) {
                // send a close event.
                if (bByForce) {
                    ((FlatGDI)gdi).setDisplayConfirmClose(false);
                }
                if (((FlatGDI)gdi).getGDIView() != null) {
                    // because closeGDI will call finish() and/or start an alertDialog, it can only run in ui thread,
                    // however, shutdownDisplay generally is not run in UI thread.
                    if (!((FlatGDI)gdi).getDisplayConfirmClose()) {
                        // the status should be shutdown if we do not need to confirm close.
                        ((FlatGDI)gdi).mbHasBeenShutdown = true;
                    }
                    final ActivityGDIDaemon activityGDIDaemon = ((ActivityGDIDaemon) ((FlatGDI) gdi).getGDIView().getGDIActivity());
                    activityGDIDaemon.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activityGDIDaemon.closeGDI();
                        }
                    });
                    if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                        // we are not in the ui thread.
                        int waitSec = 10; // open screen we try 10 times, shutdown only try 10 times because user may choose not to close.
                        for (int cnt = 0; cnt < waitSec; cnt++) {
                            if (((FlatGDI) gdi).isDisplayOnLive()) {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                        // If flatGDI is not on live, it is good. However, if we have waited for a few seconds, but
                        // flat GDI is still on live. we just return. This is to cater some extreme case where phone
                        // is so slow that it takes ages to shutdown flatGDI. This is also to cater the situation when
                        // user doesn't want to shutdown the screen.
                    } // if we are in the ui thread. This mode should not be used except in some very special mode.
                }
            }
        }
    }
    
}
