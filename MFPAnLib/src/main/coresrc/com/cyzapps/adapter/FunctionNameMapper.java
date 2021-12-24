// MFP project, FunctionNameMapper.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.Oomfp.MemberFunction;

public class FunctionNameMapper {
    // invertible -> inverted function name map. Note that this does not include user defined functions.
    static public Map<String, String> msmapSysFuncInvertMap = new HashMap<String, String>();
    static public void loadSysFuncInvertMap()    {
        msmapSysFuncInvertMap.clear();
        List<String[]> lCitingSpaces = MFPAdapter.getSysAddCitingSpaces();
        String[] arrayAllInvertibles = {"acos", "acosd", "acosh", "asin", "asind", "asinh",
                                        "atan", "atand", "atanh", "cos", "cosd", "cosh",
                                        "sin", "sind", "sinh", "tan", "tand", "tanh",
                                        "exp", "invert", "left_recip", "lg", "ln", "log",
                                        "log10", "log2", "loge", "recip", "sqrt", "todeg", "torad"};
        String[] arrayAllInverted = {"cos", "cosd", "cosh", "sin", "sind", "sinh",
                                        "tan", "tand", "tanh", "acos", "acosd", "acosh",
                                        "asin", "asind", "asinh", "atan", "atand", "atanh",
                                        "log", "invert", "recip", "exp", "exp", "exp",
                                        "", "", "exp", "left_recip", "", "torad", "todeg"};
        for (int idx = 0; idx < arrayAllInvertibles.length; idx ++) {
            MemberFunction mfInvertible = CitingSpaceDefinition.locateFunctionCall(arrayAllInvertibles[idx], 1, lCitingSpaces);
            MemberFunction mfInverted = CitingSpaceDefinition.locateFunctionCall(arrayAllInverted[idx], 1, lCitingSpaces);
            if (mfInvertible != null) { // do not worry about inverted.
                String strInvertibleWithCS = mfInvertible.getAbsNameWithCS();
                String strInvertedWithCS = "";
                if (mfInverted != null) {
                    strInvertedWithCS = mfInverted.getAbsNameWithCS();
                } else {
                    strInvertedWithCS = "~" + arrayAllInvertibles[idx]; // ~ cannot be used as function name so that it is safe
                }
                // add an invertible function least.
                msmapSysFuncInvertMap.put(strInvertibleWithCS, strInvertedWithCS);
            }
        }
    }

    // built-in and predefined (not user defined) function name to full cs map.
    static public Map<String, String> msmapSysFunc2FullCSMap = new HashMap<String, String>();
    static public void loadSysFunc2FullCSMap()    {
        msmapSysFunc2FullCSMap.clear();
        List<String[]> lCitingSpaces = MFPAdapter.getSysAddCitingSpaces();
        String[] arrayAllBuiltinFunctions = {
                "rand", "sum_over", "product_over",
                "ceil",
                    "floor",
                    "round",
                    "mod",
                    "conv_bin_to_dec",
                    "conv_oct_to_dec",
                    "conv_hex_to_dec",
                    "conv_dec_to_bin",
                    "conv_dec_to_oct",
                    "conv_dec_to_hex",
                    "conv_bin_to_hex",
                    "conv_hex_to_bin",
                    "conv_bin_to_oct",
                    "conv_oct_to_bin",
                    "conv_oct_to_hex",
                    "conv_hex_to_oct",
                    "is_nan_or_inf",
                    "is_nan_or_inf_or_null",
                    "is_inf",
                "and", "or",
                "sin", "cos", "tan", "asin", "acos", "atan",
                "log", "exp", "pow",
                "real", "image", "abs", "angle",
                "eye",
                    "ones",
                    "zeros",
                    "is_eye",
                    "is_zeros",
                    "recip",
                    "left_recip",
                    "eig",
                    "get_eigen_values",
                    "deter",
                    "det",
                    "rank",
                    "upper_triangular_matrix",
                    "invert",
                "roots_internal", "get_continuous_root", "get_num_of_results_sets", "get_variable_results",
                "integrate", "integ_gk", "integ_basic", "derivative", "deri_ridders", "lim",
                "copy_file", "move_file",
                    "create_file",
                    "delete_file",
                    "list_files",
                    "print_file_list",
                    "ls",
                    "dir",
                    "is_file_existing",
                    "is_directory",
                    "is_file_executable",
                    "is_file_hidden",
                    "is_file_readable",
                    "is_file_writable",
                    "is_file_normal",
                    "is_symbol_link",
                    "is_path_absolute",
                    "is_path_parent",
                    "is_path_same",
                    "get_file_separator",
                    "get_file_path",
                    "get_absolute_path",
                    "get_canonical_path",
                    "change_dir",
                    "cd",
                    "get_working_dir",
                    "pwd",
                    "get_file_size",
                    "get_file_last_modified_time",
                    "set_file_last_modified_time",
                    "fopen",
                    "fclose",
                    "feof",
                    "fread",
                    "fwrite",
                    "fscanf",
                    "freadline",
                    "fprintf",
                "input", "scanf", "print", "printf",
                "pause", "system", "sleep",
                "sscanf", "sprintf",
                    "conv_str_to_ints",
                    "conv_ints_to_str",
                    "strlen",
                    "strcpy",
                    "strcat",
                    "strcmp",
                    "stricmp",
                    "strsub",
                    "tostring",
                    "to_string",
                    "to_lowercase_string",
                    "to_uppercase_string",
                    "trim",
                    "trim_left",
                    "trim_right",
                    "split",
                "now", "get_time_stamp",
                    "get_year",
                    "get_month",
                    "get_day_of_year",
                    "get_day_of_month",
                    "get_day_of_week",
                    "get_hour",
                    "get_minute",
                    "get_second",
                    "get_millisecond",
                "clone",
                "alloc_array", "includes_nan_or_inf_or_null",
                    "includes_nan_or_inf",
                    "includes_nan",
                    "includes_inf",
                    "includes_null",
                    "size",
                    "set_array_elem",
                "is_aexpr_datum", "get_boolean_aexpr_true", "get_boolean_aexpr_false", "evaluate", "expr_to_string",
                "plot_2d_curves", "plot_multi_xy",
                    "plot_2d_expressions",
                    "plot_polar_curves",
                    "plot_multi_rangle",
                    "plot_polar_expressions",
                    "plot_3d_surfaces",
                    "plot_multi_xyz",
                    "plot_3d_expressions",
                    "plot_expressions",
                "iff"
                };
        for (int idx = 0; idx < arrayAllBuiltinFunctions.length; idx ++) {
            MemberFunction mf = CitingSpaceDefinition.locateFunctionCall(arrayAllBuiltinFunctions[idx], -1, lCitingSpaces);
            if (mf != null) { // do not worry about inverted.
                String strNameWithCS = mf.getAbsNameWithCS();
                // add a function into list.
                msmapSysFunc2FullCSMap.put(arrayAllBuiltinFunctions[idx], strNameWithCS);
            }
        }
        // selected predefined functions. Note that now predefined function should been loaded before the following calls
        String[] arrayAllPredefinedFunctions = {
                "sqrt",
                "ln",
                "lg",
                "loge",
                "sinh",
                "cosh",
                "tanh",
                "asinh",
                "acosh",
                "atanh",
                "sind",
                "cosd",
                "tand",
                "asind",
                "acosd",
                "atand",
        };
        for (int idx = 0; idx < arrayAllPredefinedFunctions.length; idx ++) {
            MemberFunction mf = CitingSpaceDefinition.locateFunctionCall(arrayAllPredefinedFunctions[idx], -1, lCitingSpaces);
            if (mf != null) { // do not worry about inverted.
                String strNameWithCS = mf.getAbsNameWithCS();
                // add a function into list.
                msmapSysFunc2FullCSMap.put(arrayAllPredefinedFunctions[idx], strNameWithCS);
            }
        }
    }
    
}
