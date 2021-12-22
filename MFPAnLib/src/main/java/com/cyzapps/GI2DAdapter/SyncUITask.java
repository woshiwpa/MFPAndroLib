package com.cyzapps.GI2DAdapter;

import android.app.Activity;
import android.os.Looper;

/**
 * Blocking Runnable executing on UI thread
 * @author
 */
public class SyncUITask {
    public static interface SyncUITaskListener    {
        public void onRunOnUIThread(); // Code to execute on UI thread
    }

    // Activity
    private Activity mactivity;

    // Event Listener
    private SyncUITaskListener mlistener;

    // UI runnable
    private Runnable muiRunnable;

    /**
     * Class initialization
     * @param activity Activity
     * @param listener Event listener
     */
    public SyncUITask( Activity activity, SyncUITaskListener listener ) {
        mactivity = activity;
        mlistener = listener;

        muiRunnable = new Runnable()
        {
            public void run()
            {
                // Execute custom code
                if ( SyncUITask.this.mlistener != null )
                    SyncUITask.this.mlistener.onRunOnUIThread();

                synchronized ( this ) {
                    this.notify();
                }
            }
        };
    }

    /**
     * Start runnable on UI thread and wait until finished
     */
    public void startOnUiAndWait() {
	    if ( Looper.myLooper() == Looper.getMainLooper() ) {
			muiRunnable.run();
			return;
		} else synchronized ( muiRunnable ) {
            // Execute code on UI thread
            mactivity.runOnUiThread( muiRunnable );

            // Wait until runnable finished
            try  {
                muiRunnable.wait();
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }

}