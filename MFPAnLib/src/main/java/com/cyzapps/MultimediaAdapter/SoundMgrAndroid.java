/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.MultimediaAdapter;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassExtObjRef;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Multimedia.SoundLib.SoundManager;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tony
 */
public class SoundMgrAndroid extends SoundManager{

    public static class MediaPlayerWrapper {
        public MediaPlayer mmediaPlayer = null;
        public Context mcontext = null;
        public String mstrPath = "";
        public double mdVolume = 1.0;
        public boolean mbLooping = false;
        public int mnFileType = 0;
        public String mstrReferencePath = "";
    }

    protected Context mcontext = null;
    public SoundMgrAndroid(Context context) {
        mcontext = context;
    }

    protected static List<MediaPlayerWrapper> mslistMediaPlayers = new LinkedList<MediaPlayerWrapper>();

    public static void clearMediaPlayers(Context context, boolean bRelease) {
        int idx = mslistMediaPlayers.size() - 1;
        for (; idx >= 0; idx --) {
            if (mslistMediaPlayers.get(idx) == null
                    || mslistMediaPlayers.get(idx).mmediaPlayer == null) {
                mslistMediaPlayers.remove(idx);
            } else if (mslistMediaPlayers.get(idx).mcontext != context) {
                mslistMediaPlayers.get(idx).mmediaPlayer.pause();
                if (bRelease) {
                    mslistMediaPlayers.get(idx).mmediaPlayer.release(); // release memory
                    mslistMediaPlayers.remove(idx); // not this activity
                }
            }
        }
    }

    @Override
    public boolean isPlaying(DataClass datumSndHdl) {
        MediaPlayerWrapper mpWrapper = null;
        try {
            Object o = DCHelper.lightCvtOrRetDCExtObjRef(datumSndHdl).getExternalObject();
            if (o instanceof MediaPlayerWrapper) {
                mpWrapper = (MediaPlayerWrapper)o;
            }
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            // will not be here.
            ex.printStackTrace();
        }
        if (mpWrapper != null) {
            try {
                return mpWrapper.mmediaPlayer.isPlaying();
            } catch(IllegalStateException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public DataClass playSound(SoundFileInfo sndFileInfo, boolean bRepeat, double dVolume, boolean bCreateNew) throws IOException {
        MediaPlayerWrapper mpWrapper = null;
        if (!bCreateNew) {
            for (MediaPlayerWrapper mpw : mslistMediaPlayers) {
                if (mpw.mstrReferencePath.equals(sndFileInfo.mstrFileReference)
                        && mpw.mnFileType == sndFileInfo.mnFileType) {
                    // OK, reuse this one
                    mpWrapper = mpw;
                    break;
                }
            }
        }
        try {
            if (mpWrapper == null) {
                mpWrapper = new MediaPlayerWrapper();
                mpWrapper.mmediaPlayer = MediaPlayer.create(mcontext, Uri.parse(sndFileInfo.mstrFilePath));
                mpWrapper.mcontext = mcontext;
                mpWrapper.mstrPath = sndFileInfo.mstrFilePath;
                float fVolume = (float)((dVolume < 0)?0.0:(dVolume > 1)?1.0:dVolume);
                mpWrapper.mmediaPlayer.setVolume(fVolume, fVolume);
                mpWrapper.mdVolume = fVolume;
                mpWrapper.mmediaPlayer.setLooping(bRepeat);
                mpWrapper.mbLooping = bRepeat;
                mpWrapper.mnFileType = sndFileInfo.mnFileType;
                mpWrapper.mstrReferencePath = sndFileInfo.mstrFileReference;
                final MediaPlayerWrapper mpw = mpWrapper;
                mpw.mmediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        mp.stop();  // stop it waiting for next start.
                        mp.release();
                        mslistMediaPlayers.remove(mpw);    // this is a bad media player, so remove it from list.
                        return false;
                    }
                });
                mslistMediaPlayers.add(0, mpWrapper);   // new media player always add first.
            } else {
                float fVolume = (float)((dVolume < 0)?0.0:(dVolume > 1)?1.0:dVolume);
                mpWrapper.mmediaPlayer.setVolume(fVolume, fVolume);
                mpWrapper.mdVolume = fVolume;
                mpWrapper.mmediaPlayer.setLooping(bRepeat);
                mpWrapper.mbLooping = bRepeat;
                // force it to start to play from beginning.
                mpWrapper.mmediaPlayer.seekTo(0);   // reset to 0.
                //mpWrapper.mmediaPlayer.prepare();
            }
            mpWrapper.mmediaPlayer.start();
            return new DataClassExtObjRef(mpWrapper);
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            ex.printStackTrace(); // either JFCALCExpErrException (very unlikely) or IOException
            if (mpWrapper != null) {
                mslistMediaPlayers.remove(mpWrapper);  // bad media player, remove.
            }
            return new DataClassNull();
        }
    }

    @Override
    public DataClass startSound(DataClass datumSndHdl) {
        MediaPlayerWrapper mpWrapper = null;
        try {
            Object o = DCHelper.lightCvtOrRetDCExtObjRef(datumSndHdl).getExternalObject();
            if (o instanceof MediaPlayerWrapper) {
                mpWrapper = (MediaPlayerWrapper)o;
            }
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            // will not be here.
            ex.printStackTrace();
        }
        if (mpWrapper != null) {
            mpWrapper.mmediaPlayer.start(); // will not force it to restart from beginning.
            try {
                return new DataClassExtObjRef(mpWrapper);
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                // will not be here.
                ex.printStackTrace();
                return new DataClassNull();
            }
        }
        return new DataClassNull();
    }

    @Override
    public String getSoundPath(DataClass datumSndHdl) {
        MediaPlayerWrapper mpWrapper = null;
        try {
            Object o = DCHelper.lightCvtOrRetDCExtObjRef(datumSndHdl).getExternalObject();
            if (o instanceof MediaPlayerWrapper) {
                mpWrapper = (MediaPlayerWrapper)o;
            }
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            // will not be here.
            ex.printStackTrace();
        }
        if (mpWrapper != null) {
            return mpWrapper.mstrPath;
        } else {
            return null;
        }
    }

    @Override
    public String getSoundReferencePath(DataClass datumSndHdl) {
        MediaPlayerWrapper mpWrapper = null;
        try {
            Object o = DCHelper.lightCvtOrRetDCExtObjRef(datumSndHdl).getExternalObject();
            if (o instanceof MediaPlayerWrapper) {
                mpWrapper = (MediaPlayerWrapper)o;
            }
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            // will not be here.
            ex.printStackTrace();
        }
        if (mpWrapper != null) {
            return mpWrapper.mstrReferencePath;
        } else {
            return null;
        }
    }

    @Override
    public int getSoundSourceType(DataClass datumSndHdl) {
        MediaPlayerWrapper mpWrapper = null;
        try {
            Object o = DCHelper.lightCvtOrRetDCExtObjRef(datumSndHdl).getExternalObject();
            if (o instanceof MediaPlayerWrapper) {
                mpWrapper = (MediaPlayerWrapper)o;
            }
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            // will not be here.
            ex.printStackTrace();
        }
        if (mpWrapper != null) {
            return mpWrapper.mnFileType;
        } else {
            return 0;
        }
    }

    @Override
    public boolean getSoundRepeat(DataClass datumSndHdl) {
        MediaPlayerWrapper mpWrapper = null;
        try {
            Object o = DCHelper.lightCvtOrRetDCExtObjRef(datumSndHdl).getExternalObject();
            if (o instanceof MediaPlayerWrapper) {
                mpWrapper = (MediaPlayerWrapper)o;
            }
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            // will not be here.
            ex.printStackTrace();
        }
        if (mpWrapper != null) {
            return mpWrapper.mbLooping;
        } else {
            return false;
        }
    }

    @Override
    public void setSoundRepeat(DataClass datumSndHdl, boolean bRepeat) {
        MediaPlayerWrapper mpWrapper = null;
        try {
            Object o = DCHelper.lightCvtOrRetDCExtObjRef(datumSndHdl).getExternalObject();
            if (o instanceof MediaPlayerWrapper) {
                mpWrapper = (MediaPlayerWrapper)o;
            }
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            // will not be here.
            ex.printStackTrace();
        }
        if (mpWrapper != null) {
            mpWrapper.mbLooping = bRepeat;
            mpWrapper.mmediaPlayer.setLooping(bRepeat);
        }
    }

    @Override
    public double getSoundVolume(DataClass datumSndHdl) {
        // note that getSoundVolume is getting maxium sound relative to system max sound.
        MediaPlayerWrapper mpWrapper = null;
        try {
            Object o = DCHelper.lightCvtOrRetDCExtObjRef(datumSndHdl).getExternalObject();
            if (o instanceof MediaPlayerWrapper) {
                mpWrapper = (MediaPlayerWrapper)o;
            }
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            // will not be here.
            ex.printStackTrace();
        }
        if (mpWrapper != null) {
            return mpWrapper.mdVolume;
        } else {
            return 0;
        }
    }

    @Override
    public void setSoundVolume(DataClass datumSndHdl, double dVolume) {
        // note that setSoundVolume is setting maxium sound relative to system max sound.
        MediaPlayerWrapper mpWrapper = null;
        try {
            Object o = DCHelper.lightCvtOrRetDCExtObjRef(datumSndHdl).getExternalObject();
            if (o instanceof MediaPlayerWrapper) {
                mpWrapper = (MediaPlayerWrapper)o;
            }
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            // will not be here.
            ex.printStackTrace();
        }
        if (mpWrapper != null) {
            float fVolume = (float)((dVolume < 0)?0.0:(dVolume > 1)?1.0:dVolume);
            mpWrapper.mdVolume = fVolume;
            mpWrapper.mmediaPlayer.setVolume(fVolume, fVolume);
        }
    }

    @Override
    public void stopSound(DataClass datumSndHdl) {
        MediaPlayerWrapper mpWrapper = null;
        try {
            Object o = DCHelper.lightCvtOrRetDCExtObjRef(datumSndHdl).getExternalObject();
            if (o instanceof MediaPlayerWrapper) {
                mpWrapper = (MediaPlayerWrapper)o;
            }
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            // will not be here.
            ex.printStackTrace();
        }
        if (mpWrapper != null) {
            mpWrapper.mmediaPlayer.pause();
        }
    }

    @Override
    public void stopAllSounds() {
        for (MediaPlayerWrapper mp : mslistMediaPlayers) {
            if (mp != null) {
                mp.mmediaPlayer.pause();
            }
        }
    }
}
