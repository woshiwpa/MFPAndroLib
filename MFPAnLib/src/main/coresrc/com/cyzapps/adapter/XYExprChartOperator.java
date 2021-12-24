// MFP project, XYExprChartOperator.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.adapter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.Locale;
import com.cyzapps.Jfcalc.PlotLib.TwoDExprCurve;
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
public class XYExprChartOperator extends XYChartOperator {
    public TwoDExprCurve[] m2DExprCurves = new TwoDExprCurve[0];
    
    public XYExprChartOperator()    {
        
    }

    public static final int DEFAULT_NUM_OF_STEPS = 100;
    public void getCurveSettings(String strSettings, TwoDExprCurve twoDExprCurve)    {
        String[] strlistCurveSettings = strSettings.split("(?<!\\\\);");
        for (int nIndex = 0; nIndex < strlistCurveSettings.length; nIndex ++)    {
            String[] strlistSetting = strlistCurveSettings[nIndex].split("(?<!\\\\):");
            if (strlistSetting.length != 2)    {
                continue;
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("curve_label"))    {
                twoDExprCurve.mstrCurveTitle = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("color"))    {    // this is not a member of TwoDExprCurve
                twoDExprCurve.mstrLnColor = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("point_color"))    {
                twoDExprCurve.mstrPntColor = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("point_style"))    {
                twoDExprCurve.mstrPntStyle = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("point_size"))    {
                try    {
                    twoDExprCurve.mnPntSize = Integer.parseInt(removeEscapes(strlistSetting[1]));
                    if (twoDExprCurve.mnPntSize < 0)    {
                        twoDExprCurve.mnPntSize = 0;
                    }
                } catch(NumberFormatException e)    {
                    twoDExprCurve.mnPntSize = 1;
                }
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("line_color"))    {
                twoDExprCurve.mstrLnColor = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("line_style"))    {
                twoDExprCurve.mstrLnStyle = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("line_size"))    {
                try    {
                    twoDExprCurve.mnLnSize = Integer.parseInt(removeEscapes(strlistSetting[1]));
                    if (twoDExprCurve.mnLnSize < 0)    {
                        twoDExprCurve.mnLnSize = 0;
                    }
                } catch(NumberFormatException e)    {
                    twoDExprCurve.mnLnSize = 1;
                }
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("function_variable"))    {
                try    {
                    twoDExprCurve.mnFunctionVar = Integer.parseInt(removeEscapes(strlistSetting[1]));
                } catch(NumberFormatException e)    {
                    twoDExprCurve.mnFunctionVar = 1;
                }
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("x_expr"))    {
                twoDExprCurve.mstrXExpr = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("y_expr"))    {
                twoDExprCurve.mstrYExpr = removeEscapes(strlistSetting[1]);
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("number_of_steps"))    {
                try    {
                    twoDExprCurve.mnNumOfSteps = Integer.parseInt(removeEscapes(strlistSetting[1]));
                } catch(NumberFormatException e)    {
                    twoDExprCurve.mnNumOfSteps = DEFAULT_NUM_OF_STEPS;
                }
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("auto_step"))    {
                try    {
                    twoDExprCurve.mbAutoStep = Boolean.parseBoolean(removeEscapes(strlistSetting[1]));
                } catch(NumberFormatException e)    {
                    twoDExprCurve.mbAutoStep = true;
                }
            }
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
            // long line will block at scanner.hasNextLine(). OGLChart and XYChart generally have very lone line.
            String strLine = br.readLine();
            while (strLine != null){
                nNumofLines ++;
                if (nNumofLines == 1)    {
                    getChartSettings(strLine);
                    if (mdblXMin >= mdblXMax || mdblYMin >= mdblYMax || mnXLabels <= 0 || mnYLabels <= 0)    {
                        // something wrong with file format
                        return false;
                    }
                    if (mnNumofCurves <= 0)    {
                        return false;
                    }
                    m2DExprCurves = new TwoDExprCurve[mnNumofCurves];
                    for (int nIndex = 0; nIndex < mnNumofCurves; nIndex ++)    {
                        m2DExprCurves[nIndex] = new TwoDExprCurve();
                    }
                } else {
                    int nCurveId = nNumofLines - 2;
                    if (nCurveId >= mnNumofCurves)    {
                        return false;
                    }
                    // curve settings
                    getCurveSettings(strLine, m2DExprCurves[nCurveId]);
                }
                strLine = br.readLine();
            }
        } catch(FileNotFoundException ex)    {
            // cannot open the file.
            Logger.getLogger(XYExprChartOperator.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(XYExprChartOperator.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(XYExprChartOperator.class.getName()).log(Level.SEVERE, null, ex);
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
        for (int idxLn = 0; idxLn < strlistLines.length; idxLn ++){
            String strLine = strlistLines[idxLn];
            if (idxLn == 0)    {
                getChartSettings(strLine);
                if (mdblXMin >= mdblXMax || mdblYMin >= mdblYMax || mnXLabels <= 0 || mnYLabels <= 0)    {
                    // something wrong with file format
                    return false;
                }
                if (mnNumofCurves <= 0)    {
                    return false;
                }
                if (strlistLines.length != mnNumofCurves + 1)    {
                    return false;
                }
                m2DExprCurves = new TwoDExprCurve[mnNumofCurves];
                for (int nIndex = 0; nIndex < mnNumofCurves; nIndex ++)    {
                    m2DExprCurves[nIndex] = new TwoDExprCurve();
                }
            } else {
                int nCurveId = idxLn - 1;
                // curve settings
                getCurveSettings(strLine, m2DExprCurves[nCurveId]);
            }
        }
        return true;
    }
}
