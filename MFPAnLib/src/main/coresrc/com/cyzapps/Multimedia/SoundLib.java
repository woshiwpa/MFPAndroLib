/*
 * MFP project, SoundLib.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Multimedia;

import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassExtObjRef;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Multimedia.SoundLib.SoundManager.SoundFileInfo;
import com.cyzapps.OSAdapter.LangFileManager;
import com.cyzapps.Oomfp.CitingSpaceDefinition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

/**
 *
 * @author tony
 */
public class SoundLib {
    public static abstract class SoundManager {
        public static class SoundFileInfo {
            public String mstrFilePath = "";
            public int mnFileType = 0; // 0 means normal file
            public String mstrFileReference = "";   // for zipped file, it is zipped entry path
        }
        public abstract boolean isPlaying(DataClass sndHandle);
        public abstract DataClass playSound(SoundFileInfo sndFileInfo, boolean bRepeat, double dVolume, boolean bCreateNew) throws IOException;
        public abstract DataClass startSound(DataClass sndHandle) throws IOException;
        public abstract String getSoundPath(DataClass sndHandle);
        public abstract String getSoundReferencePath(DataClass sndHandle);
        public abstract int getSoundSourceType(DataClass sndHandle);
        public abstract void setSoundRepeat(DataClass sndHandle, boolean bRepeat);
        public abstract boolean getSoundRepeat(DataClass sndHandle);
        public abstract void setSoundVolume(DataClass sndHandle, double dVolume);
        public abstract double getSoundVolume(DataClass sndHandle);
        public abstract void stopSound(DataClass sndHandle);
        public abstract void stopAllSounds();
    }
    
    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + SoundLib.class.getName());
        }
    }
    
    public static class Is_playingFunction extends BaseBuiltInFunction {

        public Is_playingFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::audio_lib::is_playing";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1; // sound handle.
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassExtObjRef datumSndHdl = DCHelper.lightCvtOrRetDCExtObjRef(listParams.poll());
            
            SoundManager sndMgr = FuncEvaluator.msMultimediaMgr.getSoundManager();
            boolean bRet = sndMgr.isPlaying(datumSndHdl);
            DataClassSingleNum datumReturn = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, bRet?MFPNumeric.TRUE:MFPNumeric.FALSE);
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Is_playingFunction());}
    
    
    public static class Play_soundFunction extends BaseBuiltInFunction {

        public Play_soundFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::audio_lib::play_sound";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 4; // source path, repeat or not, volume, create new or not.
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumPath = DCHelper.lightCvtOrRetDCString(listParams.pollLast());
            DataClassSingleNum datumRepeat = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            if (listParams.size() > 0) {
                datumRepeat = DCHelper.lightCvtOrRetDCMFPBool(listParams.pollLast());
            }
            DataClassSingleNum datumVolume = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE);
            if (listParams.size() > 0) {
                datumVolume = DCHelper.lightCvtOrRetDCMFPDec(listParams.pollLast());
            }
            DataClassSingleNum datumCreateNew = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            if (listParams.size() > 0) {
                datumCreateNew = DCHelper.lightCvtOrRetDCMFPBool(listParams.pollLast());
            }
            
            SoundManager sndMgr = FuncEvaluator.msMultimediaMgr.getSoundManager();
            String path = datumPath.getStringValue();
            boolean bRepeat = datumRepeat.getDataValue().booleanValue();
            double dVolume = datumVolume.getDataValue().doubleValue();
            boolean bCreateNew = datumCreateNew.getDataValue().booleanValue();
            DataClass datumReturn = new DataClassNull();
            try {
                SoundFileInfo sndFileInfo = new SoundFileInfo();
                sndFileInfo.mstrFilePath = MultimediaManager.convert2Url(path);
                sndFileInfo.mnFileType = 0;
                sndFileInfo.mstrFileReference = sndFileInfo.mstrFilePath;
                datumReturn = sndMgr.playSound(sndFileInfo, bRepeat, dVolume, bCreateNew);
            } catch (IOException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_SOUND_CANNOT_BE_PLAYED);
            } catch (Exception e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_SOUND_CANNOT_BE_PLAYED);
            }
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Play_soundFunction());}
    
    public static class Play_sound_from_zipFunction extends BaseBuiltInFunction {

        public Play_sound_from_zipFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::audio_lib::play_sound_from_zip";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 6; // source zip file path, zipped entry path, zip file type, repeat or not, volume, create new or not.
            mnMinParamNum = 3;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
        	if (FuncEvaluator.msPlatformHWMgr == null) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_PLATFORM_HARDWARE_LIB_IS_NOT_LOADED);
            } else if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumZipFilePath = DCHelper.lightCvtOrRetDCString(listParams.pollLast());
            DataClassString datumZippedFileEntryPath = DCHelper.lightCvtOrRetDCString(listParams.pollLast());
            DataClassSingleNum datumZipFileMode = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast());
            DataClassSingleNum datumRepeat = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            if (listParams.size() > 0) {
                datumRepeat = DCHelper.lightCvtOrRetDCMFPBool(listParams.pollLast());
            }
            DataClassSingleNum datumVolume = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, MFPNumeric.ONE);
            if (listParams.size() > 0) {
                datumVolume = DCHelper.lightCvtOrRetDCMFPDec(listParams.pollLast());
            }
            DataClassSingleNum datumCreateNew = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            if (listParams.size() > 0) {
                datumCreateNew = DCHelper.lightCvtOrRetDCMFPBool(listParams.pollLast());
            }
            
            SoundManager sndMgr = FuncEvaluator.msMultimediaMgr.getSoundManager();
            String strZipFilePath = datumZipFilePath.getStringValue();
            String strFileEntryPath = datumZippedFileEntryPath.getStringValue();
            int zipFileType = datumZipFileMode.getDataValue().intValue();
            
            LangFileManager langFileMgr = FuncEvaluator.msPlatformHWMgr.getLangFileManager();
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            String strTmpFilePath = "";
			try {
				// first read the zip entry
				inputStream = langFileMgr.openZippedFileEntry(strZipFilePath, strFileEntryPath, zipFileType==1);
				if (inputStream == null) {
					throw new IOException();
				}
	
				// then write the zip entry to a temp file.
	            File tmpFile = File.createTempFile("SoundFile", ".dat", null);
	            outputStream = new FileOutputStream(tmpFile);
	
	            byte buffer[] = new byte[16384];
	            int length = 0;
	            while ( (length = inputStream.read(buffer)) != -1 ) 
	            {
	            	outputStream.write(buffer,0, length);
	            }
	
	            // dont forget to schedule file deletion when exiting jvm.
	            tmpFile.deleteOnExit();
	            strTmpFilePath = tmpFile.getCanonicalPath();
			} catch (IOException e) {
				throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_OPEN_FILE);
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						// do not do anything but print stack.
						e.printStackTrace();
					}
				}
				
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						// do not do anything but print stack.
						e.printStackTrace();
					}
				}
			}
            
            boolean bRepeat = datumRepeat.getDataValue().booleanValue();
            double dVolume = datumVolume.getDataValue().doubleValue();
            boolean bCreateNew = datumCreateNew.getDataValue().booleanValue();
            DataClass datumReturn = new DataClassNull();
            try {
                SoundFileInfo sndFileInfo = new SoundFileInfo();
                sndFileInfo.mstrFilePath = MultimediaManager.convert2Url(strTmpFilePath);
                sndFileInfo.mnFileType = (zipFileType==1)?2:1; // 2 means Android asset zip, 1 means other zips
                sndFileInfo.mstrFileReference = strZipFilePath + LangFileManager.STRING_PATH_DIVISOR
                        + strFileEntryPath;
                datumReturn = sndMgr.playSound(sndFileInfo, bRepeat, dVolume, bCreateNew);
            } catch (IOException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_SOUND_CANNOT_BE_PLAYED);
            } catch (Exception e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_SOUND_CANNOT_BE_PLAYED);
            }
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Play_sound_from_zipFunction());}
    
    public static class Start_soundFunction extends BaseBuiltInFunction {

        public Start_soundFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::audio_lib::start_sound";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassExtObjRef datumSndHdl = DCHelper.lightCvtOrRetDCExtObjRef(listParams.poll());
            
            SoundManager sndMgr = FuncEvaluator.msMultimediaMgr.getSoundManager();
            DataClass datumReturn = new DataClassNull();
            try {
                datumReturn = sndMgr.startSound(datumSndHdl);
            } catch (IOException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_SOUND_CANNOT_BE_PLAYED);
            } catch (Exception e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_SOUND_CANNOT_BE_PLAYED);
            }
           return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Start_soundFunction());}
    
    public static class Get_sound_pathFunction extends BaseBuiltInFunction {

        public Get_sound_pathFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::audio_lib::get_sound_path";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassExtObjRef datumSndHdl = DCHelper.lightCvtOrRetDCExtObjRef(listParams.poll());
            
            SoundManager sndMgr = FuncEvaluator.msMultimediaMgr.getSoundManager();
            String strPath = sndMgr.getSoundPath(datumSndHdl);
            
            if (strPath != null) {
                return new DataClassString(strPath);
            } else {
                return new DataClassNull();
            }
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_sound_pathFunction());}
    
    public static class Get_sound_reference_pathFunction extends BaseBuiltInFunction {

        public Get_sound_reference_pathFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::audio_lib::get_sound_reference_path";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassExtObjRef datumSndHdl = DCHelper.lightCvtOrRetDCExtObjRef(listParams.poll());
            
            SoundManager sndMgr = FuncEvaluator.msMultimediaMgr.getSoundManager();
            String strPath = sndMgr.getSoundReferencePath(datumSndHdl);
            
            if (strPath != null) {
                return new DataClassString(strPath);
            } else {
                return new DataClassNull();
            }
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_sound_reference_pathFunction());}
    
    public static class Get_sound_source_typeFunction extends BaseBuiltInFunction {

        public Get_sound_source_typeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::audio_lib::get_sound_source_type";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassExtObjRef datumSndHdl = DCHelper.lightCvtOrRetDCExtObjRef(listParams.poll());
            
            SoundManager sndMgr = FuncEvaluator.msMultimediaMgr.getSoundManager();
            int nType = sndMgr.getSoundSourceType(datumSndHdl);
            
            return new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nType));
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_sound_source_typeFunction());}
    
    public static class Set_sound_repeatFunction extends BaseBuiltInFunction {

        public Set_sound_repeatFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::audio_lib::set_sound_repeat";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 2;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassExtObjRef datumSndHdl = DCHelper.lightCvtOrRetDCExtObjRef(listParams.pollLast());
            DataClassSingleNum datumRepeat = DCHelper.lightCvtOrRetDCMFPBool(listParams.pollLast());
            
            SoundManager sndMgr = FuncEvaluator.msMultimediaMgr.getSoundManager();
            sndMgr.setSoundRepeat(datumSndHdl, datumRepeat.getDataValue().booleanValue());
            
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_sound_repeatFunction());}
    
    public static class Get_sound_repeatFunction extends BaseBuiltInFunction {

        public Get_sound_repeatFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::audio_lib::get_sound_repeat";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassExtObjRef datumSndHdl = DCHelper.lightCvtOrRetDCExtObjRef(listParams.poll());
            
            SoundManager sndMgr = FuncEvaluator.msMultimediaMgr.getSoundManager();
            boolean bRepeat = sndMgr.getSoundRepeat(datumSndHdl);
            
            return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, new MFPNumeric(bRepeat));
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_sound_repeatFunction());}
    
    public static class Set_sound_volumeFunction extends BaseBuiltInFunction {

        public Set_sound_volumeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::audio_lib::set_sound_volume";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 2;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassExtObjRef datumSndHdl = DCHelper.lightCvtOrRetDCExtObjRef(listParams.pollLast());
            DataClassSingleNum datumVolume = DCHelper.lightCvtOrRetDCMFPDec(listParams.pollLast());
            
            SoundManager sndMgr = FuncEvaluator.msMultimediaMgr.getSoundManager();
            sndMgr.setSoundVolume(datumSndHdl, datumVolume.getDataValue().doubleValue());
            
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_sound_volumeFunction());}
    
    public static class Get_sound_volumeFunction extends BaseBuiltInFunction {

        public Get_sound_volumeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::audio_lib::get_sound_volume";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassExtObjRef datumSndHdl = DCHelper.lightCvtOrRetDCExtObjRef(listParams.poll());
            
            SoundManager sndMgr = FuncEvaluator.msMultimediaMgr.getSoundManager();
            double dVolume = sndMgr.getSoundVolume(datumSndHdl);
            
            return new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dVolume, true));
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_sound_volumeFunction());}
    
    public static class Stop_soundFunction extends BaseBuiltInFunction {

        public Stop_soundFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::audio_lib::stop_sound";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassExtObjRef datumSndHdl = DCHelper.lightCvtOrRetDCExtObjRef(listParams.poll());
            
            SoundManager sndMgr = FuncEvaluator.msMultimediaMgr.getSoundManager();
            sndMgr.stopSound(datumSndHdl);
            
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Stop_soundFunction());}
    
    public static class Stop_all_soundsFunction extends BaseBuiltInFunction {

        public Stop_all_soundsFunction() {
            mstrProcessedNameWithFullCS = "::mfp::multimedia::audio_lib::stop_all_sounds";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 0;
            mnMinParamNum = 0;
        }

        // this function may do nothing, depends on the operation system.
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (FuncEvaluator.msMultimediaMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_MULTIMEDIA_LIB_IS_NOT_LOADED);
            } else if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            SoundManager sndMgr = FuncEvaluator.msMultimediaMgr.getSoundManager();
            sndMgr.stopAllSounds();
            
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Stop_all_soundsFunction());}
}
