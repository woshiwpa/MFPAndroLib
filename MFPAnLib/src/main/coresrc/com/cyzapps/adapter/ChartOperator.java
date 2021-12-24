// MFP project, ChartOperator.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.adapter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tony
 * com.cyzapps.adapter.ChartOperator does not include createChart function. This function
 * will be placed in a separated interface.
 */
public class ChartOperator {
    
    public final static String VMFPChart = "VMFPChart";
    public final static String VMFPChartPath = "VMFPChartPath";
    
    //add Escapes in chart view works differently from same name function in ActivityPlotXYGraph.
    public static String addEscapes(String strInput)    {
        String strOutput = "";
        if (strInput != null)    {
            for (int i = 0; i < strInput.length(); i++)    {
                char cCurrent = strInput.charAt(i);
                if (cCurrent == ';')    {
                    strOutput += "\\;";
                } else if (cCurrent == ':')    {
                    strOutput += "\\:";
                } else    {
                    strOutput += cCurrent;
                }
            }
        }
        return strOutput;
    }
    
    public static String removeEscapes(String strInput)    {
        String strOutput = "";
        if (strInput != null)    {
            for (int i = 0; i < strInput.length() - 1; i++)    {
                char cCurrent = strInput.charAt(i);
                char cNext = strInput.charAt(i + 1);
                if (cCurrent == '\\' && (cNext == ';' || cNext == ':'))    {
                    continue;
                } else    {
                    strOutput += cCurrent;
                }
            }
            if (strInput.length() > 0)    {
                // Add the last char.
                strOutput += strInput.charAt(strInput.length() - 1);
            }
        }
        return strOutput;
    }
    
    public static com.cyzapps.VisualMFP.Color cvtStr2VMFPColor(String str)    {
        if (str.trim().toLowerCase(Locale.US).equals("blue"))    {
            return com.cyzapps.VisualMFP.Color.BLUE;     
        } else if (str.trim().toLowerCase(Locale.US).equals("brown"))    {
            return com.cyzapps.VisualMFP.Color.BROWN;     
        } else if (str.trim().toLowerCase(Locale.US).equals("cyan"))    {
            return com.cyzapps.VisualMFP.Color.CYAN;     
        } else if (str.trim().toLowerCase(Locale.US).equals("dkgray"))    {
            return com.cyzapps.VisualMFP.Color.DKGRAY;     
        } else if (str.trim().toLowerCase(Locale.US).equals("gray"))    {
            return com.cyzapps.VisualMFP.Color.GRAY;     
        } else if (str.trim().toLowerCase(Locale.US).equals("green"))    {
            return com.cyzapps.VisualMFP.Color.GREEN;     
        } else if (str.trim().toLowerCase(Locale.US).equals("ltgray"))    {
            return com.cyzapps.VisualMFP.Color.LTGRAY;     
        } else if (str.trim().toLowerCase(Locale.US).equals("magenta"))    {
            return com.cyzapps.VisualMFP.Color.MAGENTA;     
        } else if (str.trim().toLowerCase(Locale.US).equals("orange"))    {
            return com.cyzapps.VisualMFP.Color.ORANGE;     
        } else if (str.trim().toLowerCase(Locale.US).equals("pink"))    {
            return com.cyzapps.VisualMFP.Color.PINK;     
        } else if (str.trim().toLowerCase(Locale.US).equals("purple"))    {
            return com.cyzapps.VisualMFP.Color.PURPLE;     
        } else if (str.trim().toLowerCase(Locale.US).equals("red"))    {
            return com.cyzapps.VisualMFP.Color.RED;     
        } else if (str.trim().toLowerCase(Locale.US).equals("transparent"))    {
            return com.cyzapps.VisualMFP.Color.TRANSPARENT;     
        } else if (str.trim().toLowerCase(Locale.US).equals("violet"))    {
            return com.cyzapps.VisualMFP.Color.VIOLET;     
        } else if (str.trim().toLowerCase(Locale.US).equals("white"))    {
            return com.cyzapps.VisualMFP.Color.WHITE;
        } else if (str.trim().toLowerCase(Locale.US).equals("yellow"))    {
            return com.cyzapps.VisualMFP.Color.YELLOW;
        } else    {
            return com.cyzapps.VisualMFP.Color.BLACK;
        }
    }

    public boolean loadFromFile(String strFilePath)    {
        return false;
    }
    
    public boolean loadFromString(String strSettings)    {
        return false;
    }
    
    public static String getChartTypeFromFile(String strFilePath)    {
        // identify chart type
        String strChartType = "Invalid";

        String strFirstLine = "";
        FileInputStream fis = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(strFilePath);
            br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            // use BufferedReader.readLine instead of scanner because scanner in Samsung Galaxy Pad A
            // long line will block at scanner.hasNextLine(). OGLChart and XYChart generally have very long line.
            String strLine = br.readLine();
            while (strLine != null){
                if (strLine.trim().length() > 0) {
                    strFirstLine = strLine.trim();
                    break;
                }
                strLine = br.readLine();
            }
        } catch(FileNotFoundException ex)    {
            // cannot open the file.
            Logger.getLogger(ChartOperator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ChartOperator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ChartOperator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        String[] strlistFileSettings = strFirstLine.split("(?<!\\\\);");
        for (int nIndex = 0; nIndex < strlistFileSettings.length; nIndex ++)    {
            String[] strlistSetting = strlistFileSettings[nIndex].split("(?<!\\\\):");
            if (strlistSetting.length != 2)    {
                continue;
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("chart_type"))    {
                strChartType = ChartOperator.removeEscapes(strlistSetting[1]);
                break;
            }
        }
        return strChartType;        
    }
    
    public static String getChartTypeFromString(String strSettings)    {
        String[] strlistLines = strSettings.split("\n");
        String[] strlistFileSettings = strlistLines[0].split("(?<!\\\\);");
        String strChartType = "Invalid";
        for (int nIndex = 0; nIndex < strlistFileSettings.length; nIndex ++)    {
            String[] strlistSetting = strlistFileSettings[nIndex].split("(?<!\\\\):");
            if (strlistSetting.length != 2)    {
                continue;
            } else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("chart_type"))    {
                strChartType = ChartOperator.removeEscapes(strlistSetting[1]);
                break;
            }
        }
        return strChartType;        
    }
        
    public static class ChartCreationParam    {
        public int mnRecommendedNumOfMarksPerAxis = 10;
        public String toString()    {
            return Integer.toString(mnRecommendedNumOfMarksPerAxis);
        }
        public boolean readFromString(String strParams)   {
            // read and validate input parameters.
            try {
                mnRecommendedNumOfMarksPerAxis = Integer.parseInt(strParams);
                if (mnRecommendedNumOfMarksPerAxis <= 0)    {
                    return false;
                } else  {
                    return true;
                }
            } catch (NumberFormatException e)   {
                return false;
            }
        }
    }
    
    public String writeToString()    {
        return "";
    }
    
    public boolean writeToFile(String strPath)    {
        return false;
    }
}
