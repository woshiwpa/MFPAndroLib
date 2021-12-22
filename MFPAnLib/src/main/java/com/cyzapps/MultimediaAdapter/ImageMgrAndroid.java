/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.MultimediaAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.cyzapps.JGI2D.Display2D;
import com.cyzapps.JGI2D.DisplayLib;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassExtObjRef;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.IOLib;
import com.cyzapps.Multimedia.ImageLib.ImageManager;
import com.cyzapps.Multimedia.MultimediaManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author tony
 */
public class ImageMgrAndroid extends ImageManager {
    public static class ImageWrapper {
        private Bitmap mbitmap;
        public ImageWrapper(Bitmap bitmap) {
            mbitmap = bitmap;
        }
        public Bitmap getImageFromWrapper() {
            return mbitmap;
        }
    }
    protected Context mcontext = null;
    public ImageMgrAndroid(Context context) {
        mcontext = context;
    }

    @Override
    public DataClass loadImage(String strImagePath) {
        // strImagePath is either file path or url.
        InputStream input = null;
        if (!MultimediaManager.isValidURL(strImagePath)) {
            File f = IOLib.getFileFromCurrentDir(strImagePath);
            try {
                input = new FileInputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                URL url = new URL(strImagePath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        Bitmap bitmap = null;
        if (input != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(input, null, options);
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (bitmap != null) {
            try {
                return new DataClassExtObjRef(new ImageWrapper(bitmap));
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                // will not be here
            }
        }
        return new DataClassNull();
    }

    @Override
    public DataClass loadImage(InputStream input) {
        Bitmap bitmap = null;
        if (input != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(input, null, options);
            // no need to close input. The function calling loadImage will close it.
        }
        if (bitmap != null) {
            try {
                return new DataClassExtObjRef(new ImageWrapper(bitmap));
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                // will not be here
            }
        }
        return new DataClassNull();
    }

    @Override
    public int[] getImageSize(DataClassExtObjRef img) {
        try {
            if (img.getExternalObject() instanceof ImageWrapper
                    && ((ImageWrapper)img.getExternalObject()).getImageFromWrapper().isRecycled() == false) {
                Bitmap bitmap = ((ImageWrapper)img.getExternalObject()).getImageFromWrapper();
                int[] size = new int[2];
                size[0] = bitmap.getWidth();
                size[1] = bitmap.getHeight();
                return size;
            }
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            // do nothing here
        }
        return null;
    }

    @Override
    public Display2D openImageDisplay(DataClass pathOrImgHandler) {
        if (pathOrImgHandler.isNull()) {
            // if parameter is explicitly null, it means we just want an empty display
            return new ImageDisplay();
        } else {
            // if parameter is not explicity null, will return a display with background image,
            // however, if pathOrImgHandler is invalid, will return null;
            DataClassString imgPath = DCHelper.try2LightCvtOrRetDCString(pathOrImgHandler);
            String path = null;
            ImageWrapper img = null;
            if (imgPath == null) {
                DataClassExtObjRef imgHandler = DCHelper.try2LightCvtOrRetDCExtObjRef(pathOrImgHandler);
                try {
                    if (imgHandler == null || !(imgHandler.getExternalObject() instanceof ImageWrapper)
                            || ((ImageWrapper)imgHandler.getExternalObject()).getImageFromWrapper().isRecycled()) {
                        return null;    // invalid path or image handler.
                    } else {
                        img = (ImageWrapper)imgHandler.getExternalObject();
                    }
                } catch (ErrProcessor.JFCALCExpErrException ex) {
                    ex.printStackTrace();
                    return null;
                }
                // now buffered image must not be null.
            } else {
                try {
                    path = imgPath.getStringValue();
                    if (path == null) {
                        return null;
                    }
                } catch (ErrProcessor.JFCALCExpErrException ex) {
                    ex.printStackTrace();
                    return null;
                }
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    Bitmap bmp = null;
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    if (MultimediaManager.isValidURL(path)) {
                        URL url = new URL(path);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        bmp = BitmapFactory.decodeStream(input);
                    } else {
                        bmp = BitmapFactory.decodeFile(IOLib.getFileFromCurrentDir(path).getAbsolutePath());
                    }
                    if (bmp != null) {
                        img = new ImageWrapper(bmp);
                    }
                } catch (IOException e) {
                    // failed to load image.
                    return null;
                }
            }
            if (img == null) {
                return null; // invalid image.
            }
            // now we have non-null img.
            ImageDisplay imgDisplay = new ImageDisplay();
            imgDisplay.setDisplaySize(img.getImageFromWrapper().getWidth(), img.getImageFromWrapper().getHeight());
            try {
                imgDisplay.setBackgroundImage(new DataClassExtObjRef(img), 0);
                if (path != null) {
                    imgDisplay.mstrFilePath = path;
                }
                return imgDisplay;
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                ex.printStackTrace();
                return null;    // will not be here.
            }
        }
    }

    @Override
    public void shutdownImageDisplay(DisplayLib.IGraphicDisplay display) {
        if (display != null) {
            display.close();
        }
    }

    @Override
    public boolean isValidImageHandle(DataClassExtObjRef img) {
        try {
            return img.getExternalObject() instanceof ImageWrapper
                    && !((ImageWrapper)img.getExternalObject()).getImageFromWrapper().isRecycled();
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            return false;   // will not be here.
        }
    }

    @Override
    public DataClass createImage(int w, int h) {
        try {
            if (w <= 0 || h <= 0) {
                return new DataClassNull();
            } else {
                Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                return new DataClassExtObjRef(new ImageWrapper(bitmap));
            }
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            ex.printStackTrace();
            return new DataClassNull();
        }
    }

    @Override
    public DataClass cloneImage(DataClassExtObjRef img) {
        try {
            if (img == null || !(img.getExternalObject() instanceof ImageWrapper)
                    || ((ImageWrapper)img.getExternalObject()).getImageFromWrapper().isRecycled()) {
                return new DataClassNull();
            } else {
                Bitmap imgBmp = ((ImageWrapper)img.getExternalObject()).getImageFromWrapper();
                Bitmap imgCopy = imgBmp.copy(imgBmp.getConfig(), true);
                return new DataClassExtObjRef(new ImageWrapper(imgCopy));
            }
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            ex.printStackTrace();
            return new DataClassNull();
        }
    }

    @Override
    public DataClass cloneImage(DataClassExtObjRef img, int x1, int y1, int x2, int y2, int destW, int destH) {
        try {
            if (img == null || !(img.getExternalObject() instanceof ImageWrapper)
                    || ((ImageWrapper)img.getExternalObject()).getImageFromWrapper().isRecycled()
                    || x1 >= x2 || y1 >= y2
                    || x1 >= ((ImageWrapper)img.getExternalObject()).getImageFromWrapper().getWidth() || x2 <= 0
                    || y1 >= ((ImageWrapper)img.getExternalObject()).getImageFromWrapper().getHeight() || y2 <= 0
                    || destW <= 0 || destH <= 0) {
                // invalid bitmap or copy range
                return new DataClassNull();
            } else {
                Bitmap bitmap = ((ImageWrapper)img.getExternalObject()).getImageFromWrapper();
                x1 = Math.max(0, Math.min(x1, bitmap.getWidth()));
                x2 = Math.max(0, Math.min(x2, bitmap.getWidth()));
                y1 = Math.max(0, Math.min(y1, bitmap.getHeight()));
                y2 = Math.max(0, Math.min(y2, bitmap.getHeight()));
                Bitmap bmpCopy = Bitmap.createBitmap(destW, destH, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bmpCopy);
                canvas.drawBitmap(bitmap, new Rect(x1, y1, x2, y2), new Rect(0, 0, destW, destH), null);
                return new DataClassExtObjRef(new ImageWrapper(bmpCopy));
            }
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            ex.printStackTrace();
            return new DataClassNull();
        }
    }

    @Override
    public boolean saveImage(DataClassExtObjRef img, String imgFormat, String strImagePath) {
        try {
            // retrieve image
            if (img == null || !(img.getExternalObject() instanceof ImageWrapper)
                    || ((ImageWrapper)img.getExternalObject()).getImageFromWrapper().isRecycled()) {
                return false;
            }
            Bitmap imgHandler = ((ImageWrapper)img.getExternalObject()).getImageFromWrapper();
            File outputfile = IOLib.getFileFromCurrentDir(strImagePath);
            if (outputfile.getParentFile() != null) {
                // create parent folders
                outputfile.getParentFile().mkdirs();
            }
            FileOutputStream out = null;
            boolean bSaveFileOK = false;
            try {
                out = new FileOutputStream(outputfile);
                switch(imgFormat.toUpperCase())
                {
                    case "PNG":
                        // PNG is a lossless format, the compression factor (100) is ignored
                        imgHandler.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                        bSaveFileOK = true;
                        break;
                    case "JPG":
                    case "JPEG":
                        imgHandler.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                        bSaveFileOK = true;
                        break;
                    default: // bmp
                        AndroidBmpUtil bmpUtil = new AndroidBmpUtil();
                        bSaveFileOK = bmpUtil.save(imgHandler, outputfile.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bSaveFileOK;
        } catch (Exception e) {
            return false;
        }
    }
}
