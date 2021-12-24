// MFP project, XYChartOperator.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.adapter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.Locale;
import com.cyzapps.VisualMFP.PointStyle.POINTSHAPE;
import com.cyzapps.adapter.MFPAdapter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tony
 * This class is a sub class of ChartOperator. It does not include createChart function.
 * This function will be placed in a separated interface.
 */
public class XYChartOperator extends ChartOperator {
    public String mstrChartType = "multiXY";
    public String mstrChartTitle = "";
    public String mstrXTitle = "";
    public double mdblXMin = 0;
    public double mdblXMax = 1;
    public int mnXLabels = 1;
    public String mstrYTitle = "";
    public double mdblYMin = 0;
    public double mdblYMax = 1;
    public int mnYLabels = 1;
    public String mstrAxesColor = "gray";
    public String mstrLabelsColor = "ltgray";
    public String mstrChartBKColor = "black";
    public boolean mbShowGrid = true;
    public int mnNumofCurves = 0;
    public XYCurve[] mXYCurves = new XYCurve[0];
    
    public XYChartOperator()    {
    }
            
    public class XYCurve    {
        public double[] mdbllistX = new double[0];
        public double[] mdbllistY = new double[0];
        public String mstrCurveLabel = "";
        public String mstrColor = "";
        public String mstrPntColor = "";
        public String mstrPointStyle = "";
        public int mnPntSize = 1;
        public String mstrLnColor = "";
        public String mstrLineStyle = "";
        public int mnLnSize = 1;
    }
    
    public void getChartSettings(String strSettings)    {
        String[] strlistFileSettings = strSettings.split("(?<!\\\\);");
        for (int nIndex = 0; nIndex < strlistFileSettings.length; nIndex ++)    {
            String[] strlistSetting = strlistFileSettings[nIndex].split("(?<!\\\\):");
            if (strlistSetting.length != 2)    {
                continue;
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("chart_type"))    {
                mstrChartType = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("chart_title"))    {
                mstrChartTitle = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("x_title"))    {
                mstrXTitle = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("x_min"))    {
                try    {
                    mdblXMin = Double.parseDouble(removeEscapes(strlistSetting[1]));
                } catch(NumberFormatException e)    {
                    mdblXMin = 0;
                }
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("x_max"))    {
                try    {
                    mdblXMax = Double.parseDouble(removeEscapes(strlistSetting[1]));
                } catch(NumberFormatException e)    {
                    mdblXMax = 1;
                }
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("x_labels"))    {
                try    {
                    mnXLabels = Integer.parseInt(removeEscapes(strlistSetting[1]));
                } catch(NumberFormatException e)    {
                    mnXLabels = 1;
                }
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("y_title"))    {
                mstrYTitle = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("y_min"))    {
                try    {
                    mdblYMin = Double.parseDouble(removeEscapes(strlistSetting[1]));
                } catch(NumberFormatException e)    {
                    mdblYMin = 0;
                }
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("y_max"))    {
                try    {
                    mdblYMax = Double.parseDouble(removeEscapes(strlistSetting[1]));
                } catch(NumberFormatException e)    {
                    mdblYMax = 1;
                }
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("y_labels"))    {
                try    {
                    mnYLabels = Integer.parseInt(removeEscapes(strlistSetting[1]));
                } catch(NumberFormatException e)    {
                    mnYLabels = 1;
                }
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("axes_color"))    {
                mstrAxesColor = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("labels_color"))    {
                mstrLabelsColor = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("background_color"))    {
                mstrChartBKColor = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("show_grid"))    {
                mbShowGrid = removeEscapes(strlistSetting[1]).trim().equalsIgnoreCase("true");
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("number_of_curves"))    {
                try {
                    mnNumofCurves = Integer.parseInt(removeEscapes(strlistSetting[1]));
                    if (mnNumofCurves < 0)    {
                        mnNumofCurves = 0;
                    }
                } catch (NumberFormatException e)    {
                    mnNumofCurves = 0;
                }
            }
        }
    }
    
    public void getCurveSettings(String strSettings, XYCurve xyCurve)    {
        String[] strlistCurveSettings = strSettings.split("(?<!\\\\);");
        for (int nIndex = 0; nIndex < strlistCurveSettings.length; nIndex ++)    {
            String[] strlistSetting = strlistCurveSettings[nIndex].split("(?<!\\\\):");
            if (strlistSetting.length != 2)    {
                continue;
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("curve_label"))    {
                xyCurve.mstrCurveLabel = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("color"))    {
                xyCurve.mstrColor = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("point_color"))    {
                xyCurve.mstrPntColor = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("point_style"))    {
                xyCurve.mstrPointStyle = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("point_size"))    {
                try    {
                    xyCurve.mnPntSize = Integer.parseInt(removeEscapes(strlistSetting[1]));
                    if (xyCurve.mnPntSize < 0)    {
                        xyCurve.mnPntSize = 0;
                    }
                } catch(NumberFormatException e)    {
                    xyCurve.mnPntSize = 1;
                }
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("line_color"))    {
                xyCurve.mstrLnColor = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("line_style"))    {
                xyCurve.mstrLineStyle = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("line_size"))    {
                try    {
                    xyCurve.mnLnSize = Integer.parseInt(removeEscapes(strlistSetting[1]));
                    if (xyCurve.mnLnSize < 0)    {
                        xyCurve.mnLnSize = 0;
                    }
                } catch(NumberFormatException e)    {
                    xyCurve.mnLnSize = 1;
                }
            }
        }
    }
    
    public double[] getValueList(String strValueList)    {
        String[] strlistValues = strValueList.split(";");
        double[] dbllistValues = new double[strlistValues.length];
        for (int nIndex = 0; nIndex < strlistValues.length; nIndex ++)    {
            try {
                dbllistValues[nIndex] = Double.parseDouble(strlistValues[nIndex]);
            } catch(NumberFormatException e)    {
                dbllistValues[nIndex] = Double.NaN;
            }
        }
        return dbllistValues;
    }
    
    public static POINTSHAPE cvtStr2PntShape(String str)    {
        if (str.trim().toLowerCase(Locale.US).equals("circle"))    {
            return POINTSHAPE.POINTSHAPE_CIRCLE;
        } else if (str.trim().toLowerCase(Locale.US).equals("triangle"))    {
            return POINTSHAPE.POINTSHAPE_UPTRIANGLE;
        } else if (str.trim().toLowerCase(Locale.US).equals("square"))    {
            return POINTSHAPE.POINTSHAPE_SQUARE;
        } else if (str.trim().toLowerCase(Locale.US).equals("diamond"))    {
            return POINTSHAPE.POINTSHAPE_DIAMOND;
        } else if (str.trim().toLowerCase(Locale.US).equals("x"))    {
            return POINTSHAPE.POINTSHAPE_X;
        } else    {
            return POINTSHAPE.POINTSHAPE_DOT;
        }
    }
        
    @Override
    public boolean loadFromFile(String strFilePath)    {
        FileInputStream fis = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(strFilePath);
            br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            int nNumofLines = 0;
            // use BufferedReader.readLine instead of scanner because scanner in Samsung Galaxy Pad A
            // long line will block at scanner.hasNextLine(). OGLChart and XYChart generally have very long line.
            String strLine = br.readLine();
            while (strLine != null){
                nNumofLines ++;
                if (nNumofLines == 1)    {
                    getChartSettings(strLine);
                    double dDefaultHalfRange = (MFPAdapter.getPlotChartVariableTo() - MFPAdapter.getPlotChartVariableFrom())/2.0;
                    if (mdblXMin == mdblXMax
                            || (this instanceof PolarChartOperator 
                                && Math.abs(mdblXMax - mdblXMin) < 0.00001)) {
                        // polar chart is a bit special. For some reason, if xmin = xmax = like 2.67,
                        // mdblXMax and mdblXMin are not exactly the same, their difference is so small
                        // that R step length will be too small and there will be so many points to draw
                        // that final results in outofmemory error.
                        mdblXMin -= dDefaultHalfRange;
                        mdblXMax += dDefaultHalfRange;
                    }
                    if (mdblYMin == mdblYMax) {
                        mdblYMin -= dDefaultHalfRange;
                        mdblYMax += dDefaultHalfRange;
                    }
                    if (mdblXMin >= mdblXMax || mdblYMin >= mdblYMax
                            || mnXLabels <= 0 || mnYLabels <= 0)    {
                        // something wrong with file format
                        return false;
                    }
                    if (mnNumofCurves <= 0)    {
                        return false;
                    }
                    mXYCurves = new XYCurve[mnNumofCurves];
                    for (int nIndex = 0; nIndex < mnNumofCurves; nIndex ++)    {
                        mXYCurves[nIndex] = new XYCurve();
                    }
                } else {
                    int nCurveId = (int)Math.floor((double)(nNumofLines - 2)/3.0);
                    if (nCurveId >= mnNumofCurves)    {
                        return false;
                    }
                    if (nNumofLines % 3 == 2)    {
                        // X
                        mXYCurves[nCurveId].mdbllistX = getValueList(strLine);
                    } else if (nNumofLines % 3 == 0)    {
                        // Y
                        mXYCurves[nCurveId].mdbllistY = getValueList(strLine);
                        if (mXYCurves[nCurveId].mdbllistX.length != mXYCurves[nCurveId].mdbllistY.length)    {
                            // something wrong
                            return false;
                        }
                    } else    {
                        // curve settings
                        getCurveSettings(strLine, mXYCurves[nCurveId]);
                    }
                }
                strLine = br.readLine();
            }
        } catch(FileNotFoundException ex)    {
            // cannot open the file.
            Logger.getLogger(XYChartOperator.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(XYChartOperator.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(XYChartOperator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return true;
    }
    
    @Override
    public boolean loadFromString(String strSettings)    {
        if (strSettings == null)    {
            return false;
        }
        String[] strlistLines = strSettings.split("\n");
        int nNumofLines = 0;
        for (int idxLn = 0; idxLn < strlistLines.length; idxLn ++){
            String strLine = strlistLines[idxLn];
            nNumofLines ++;
            if (nNumofLines == 1)    {
                getChartSettings(strLine);
                double dDefaultHalfRange = (MFPAdapter.getPlotChartVariableTo() - MFPAdapter.getPlotChartVariableFrom())/2.0;
                if (mdblXMin == mdblXMax
                        || (this instanceof PolarChartOperator 
                            && Math.abs(mdblXMax - mdblXMin) < 0.00001)) {
                    // polar chart is a bit special. For some reason, if xmin = xmax = like 2.67,
                    // mdblXMax and mdblXMin are not exactly the same, their difference is so small
                    // that R step length will be too small and there will be so many points to draw
                    // that final results in outofmemory error.
                    mdblXMin -= dDefaultHalfRange;
                    mdblXMax += dDefaultHalfRange;
                }
                if (mdblYMin == mdblYMax) {
                    mdblYMin -= dDefaultHalfRange;
                    mdblYMax += dDefaultHalfRange;
                }
                if (mdblXMin >= mdblXMax || mdblYMin >= mdblYMax
                        || mnXLabels <= 0 || mnYLabels <= 0)    {
                    // something wrong with file format
                    return false;
                }
                if (mnNumofCurves <= 0)    {
                    return false;
                }
                if (strlistLines.length != (1 + 3 * mnNumofCurves))    {
                    // something wrong
                    return false;
                }
                mXYCurves = new XYCurve[mnNumofCurves];
                for (int nIndex = 0; nIndex < mnNumofCurves; nIndex ++)    {
                    mXYCurves[nIndex] = new XYCurve();
                }
            } else {
                int nCurveId = (int)Math.floor((double)(nNumofLines - 2)/3.0);
                if (nNumofLines % 3 == 2)    {
                    // X
                    mXYCurves[nCurveId].mdbllistX = getValueList(strLine);
                } else if (nNumofLines % 3 == 0)    {
                    // Y
                    mXYCurves[nCurveId].mdbllistY = getValueList(strLine);
                    if (mXYCurves[nCurveId].mdbllistX.length != mXYCurves[nCurveId].mdbllistY.length)    {
                        // something wrong
                        return false;
                    }
                } else    {
                    // curve settings
                    getCurveSettings(strLine, mXYCurves[nCurveId]);
                }
            }
        }
        return true;
    }
}
