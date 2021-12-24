/*
 * MFP project, MultimediaManager.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Multimedia;

import com.cyzapps.Jfcalc.IOLib;
import com.cyzapps.Multimedia.SoundLib.SoundManager;
import com.cyzapps.Multimedia.ImageLib.ImageManager;
import java.io.File;
import java.net.URI;

/**
 *
 * @author tony
 */
public class MultimediaManager { // it is not abstract class.
    protected ImageManager mimageManager = null;
    protected SoundManager msoundManager = null;
    
    public MultimediaManager(ImageManager imgMgr, SoundManager sndMgr) {
        mimageManager = imgMgr;
        msoundManager = sndMgr;
    }
    
    public ImageManager getImageManager() {
        return mimageManager;
    }
    
    public SoundManager getSoundManager() {
        return msoundManager;
    }
    
    public static boolean isValidURL(String urlStr) {
        try {
          URI uri = new URI(urlStr);
          return uri.getScheme().equals("http") || uri.getScheme().equals("https");
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isFileURL(String urlStr) {
        try {
          URI uri = new URI(urlStr);
          return uri.getScheme().equals("file");
        } catch (Exception e) {
            return false;
        }
    }
    
    public static String convert2Url(String strPathOrUrl) {
        String path = "";
        if (isValidURL(strPathOrUrl)) {
            try {
                // is it url?
                URI uri = new URI(strPathOrUrl);
                path = uri.toString();
            } catch (Exception e) {
                // ok, it is a path.
                File f = IOLib.getFileFromCurrentDir(strPathOrUrl);
                URI uri = f.toURI();
                path = uri.toString();
            }
        } else if (isFileURL(strPathOrUrl)) {
            try {
                // is it a file url? (i.e. file:/...)
                URI uri = new URI(strPathOrUrl);
                path = uri.toString();
            } catch (Exception e) {
                // ok, it is a path.
                File f = IOLib.getFileFromCurrentDir(strPathOrUrl);
                URI uri = f.toURI();
                path = uri.toString();
            }
        } else {
            // it is a anyway path.
            File f = IOLib.getFileFromCurrentDir(strPathOrUrl);
            URI uri = f.toURI();
            path = uri.toString();
        }
        return path;
    }
}
