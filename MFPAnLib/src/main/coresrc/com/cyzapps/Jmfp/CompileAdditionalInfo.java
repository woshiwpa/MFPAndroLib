/*
 * MFP project, CompileAdditionalInfo.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jmfp.Annotation.AnnotationType;
import java.util.LinkedList;

/**
 *
 * @author tony
 */
public class CompileAdditionalInfo {
    public static class AssetCopyCmd {
        public String mstrSrcPath = "";
        public int mnSrcZipType = 0;
        public String mstrSrcZipPath = "";
        public String mstrSrcZipEntry = "";
        public String mstrDestTarget = AnnoType_build_asset.ASSET_RESOURCE_TARGET;
        public String mstrDestPath = "";
        
        public Boolean isPlainPath() {
            return mstrSrcPath.trim().length() > 0;
        }
        
        public Boolean isAndroidAssetZip() {
            return mnSrcZipType == 1;
        }
        
        public Boolean isNormalZip() {
            return mnSrcZipType != 1;
        }
    }
    public LinkedList<AssetCopyCmd> mbuildAssetCopyCmds = new LinkedList<>();
    
    public void addAssetCopyCmd(StatementType statementType) {
        if (statementType != null && statementType.getType().equals(Annotation.getTypeStr())) {
            AnnotationType annotationType = ((Annotation)statementType).mannoType;
            if (annotationType.getCmdTypeString().equals(AnnoType_build_asset.getCmdTypeStr())) {
                // ok, right type
                AnnoType_build_asset annoTypeBuildAsset = (AnnoType_build_asset)annotationType;
                AssetCopyCmd assetCopyCmd = new AssetCopyCmd();
                assetCopyCmd.mstrSrcPath = annoTypeBuildAsset.mstrSrcPath;
                assetCopyCmd.mnSrcZipType = annoTypeBuildAsset.mnSrcZipType;
                assetCopyCmd.mstrSrcZipPath = annoTypeBuildAsset.mstrSrcZipPath;
                assetCopyCmd.mstrSrcZipEntry = annoTypeBuildAsset.mstrSrcZipEntry;
                assetCopyCmd.mstrDestTarget = annoTypeBuildAsset.mstrDestTarget;
                assetCopyCmd.mstrDestPath = annoTypeBuildAsset.mstrDestPath;
                mbuildAssetCopyCmds.add(assetCopyCmd);
            }
        }
    }
}
