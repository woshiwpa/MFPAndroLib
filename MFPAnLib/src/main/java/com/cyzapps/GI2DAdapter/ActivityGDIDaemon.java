package com.cyzapps.GI2DAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.cyzapps.mfpanlib.R;
import com.cyzapps.JGI2D.DisplayLib;

/**
 * Created by tony on 18/02/2017.
 */

public class ActivityGDIDaemon extends Activity {
    /** The encapsulated GDI view. */
    public FlatGDIView mflatGDIView = null;
    /** The GDI class. */
    public FlatGDI mflatGDI = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        int nflatGDIId = extras.getInt(FlatGDIManager.FLATGDI_ID);
        int nOrientation = extras.getInt(FlatGDIManager.INITIAL_ORIENTATION);
        // because mslistFlatGDI never shrink, mflatGDI can be stored there.
        FlatGDI[] allDisplays = FlatGDIManager.mslistFlatGDI.toArray(new FlatGDI[0]);  // convert to an array, this is an atomic operation.
        mflatGDI = null;
        for (int idx = 0; idx < allDisplays.length; idx ++) {
            if (allDisplays[idx] != null && allDisplays[idx].getId() == nflatGDIId) {
                // get it
                mflatGDI = allDisplays[idx];
                break;
            }
        }
        if (mflatGDI == null) {
            return; // flatGDI is not found.
        }
        mflatGDIView = new FlatGDIView(this, mflatGDI);
        mflatGDIView.setBackgroundColor(Color.argb(
                mflatGDI.getBackgroundColor().mnAlpha,
                mflatGDI.getBackgroundColor().mnR,
                mflatGDI.getBackgroundColor().mnG,
                mflatGDI.getBackgroundColor().mnB)
        );
        mflatGDI.setGDIView(mflatGDIView);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //noinspection WrongConstant
        setRequestedOrientation(nOrientation);  // set orientation before setContentView.
        setContentView(mflatGDIView);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public void showMsgBox(String strMsg, String strTitle, final boolean bExitActivity)	{
        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(ActivityGDIDaemon.this)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {
                        if (bExitActivity)	{
                            finish();	// exit
                        }
                    }

                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {
                        // do nothing
                    }

                }).setTitle(strTitle)
                .setMessage(strMsg)
                .create();
        alertDialog.show();
    }

    public void closeGDI() {
        if (mflatGDI != null && mflatGDI.getDisplayConfirmClose()) {
            showMsgBox(getResources().getString(R.string.do_you_want_to_exit), mflatGDI.mstrTitle, true);
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (mflatGDI != null && mflatGDI.getDisplayConfirmClose()) {
            showMsgBox(getResources().getString(R.string.do_you_want_to_exit), mflatGDI.mstrTitle, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish()	{
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        super.finish();
        if (mflatGDI != null) {
            mflatGDI.mbHasBeenShutdown = true;
            mflatGDI.close();
        }
    }
}
