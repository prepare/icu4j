/*
 *******************************************************************************
 * Copyright (C) 1998-2002, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/tool/layout/ScriptRunModuleWriter.java,v $
 * $Date: 2003/01/14 19:05:23 $
 * $Revision: 1.1 $
 *
 *******************************************************************************
 */
package com.ibm.icu.dev.tool.layout;

import java.util.*;
import com.ibm.icu.impl.Utility;

public class ScriptRunModuleWriter extends ModuleWriter
{
    public ScriptRunModuleWriter(ScriptData theScriptData)
    {
        super(theScriptData);
    }
    
    public void writeScriptRuns(String fileName)
    {
        int minScript   = scriptData.getMinScript();
        int maxScript   = scriptData.getMaxScript();
        int recordCount = scriptData.getRecordCount();
        
        openFile(fileName);
        writeHeader();
        output.println(preamble);
        
        for (int record = 0; record < recordCount; record += 1) {
            int script = scriptData.getRecord(record).scriptCode();
            
            output.print("    {0x");
            output.print(Utility.hex(scriptData.getRecord(record).startChar(), 6));
            output.print(", 0x");
            output.print(Utility.hex(scriptData.getRecord(record).endChar(), 6));
            output.print(", ");
            output.print(scriptData.getScriptTag(script));
            output.print("ScriptCode}");
            output.print((record == recordCount - 1) ? " " : ",");
            output.print(" // ");
            output.println(scriptData.getScriptName(script));
        }
        
        output.println(postamble);
        
        int power = 1 << Utility.highBit(recordCount);
        int extra = recordCount - power;
        
        output.print("le_int32 ScriptRun::scriptRecordsPower = 0x");
        output.print(Utility.hex(power, 4));
        output.println(";");
        
        
        output.print("le_int32 ScriptRun::scriptRecordsExtra = 0x");
        output.print(Utility.hex(extra, 4));
        output.println(";");

        Vector[] scriptRangeOffsets = new Vector[maxScript - minScript + 1];
        
        for (int script = minScript; script <= maxScript; script += 1) {
            scriptRangeOffsets[script - minScript] = new Vector();
        }
        
        for (int record = 0; record < recordCount; record += 1) {
            scriptRangeOffsets[scriptData.getRecord(record).scriptCode() - minScript].addElement(new Integer(record));
        }
        
        output.println();
        
        for (int script = minScript; script <= maxScript; script += 1) {
            Vector offsets = scriptRangeOffsets[script - minScript];
            
            output.print("le_int16 ");
            output.print(scriptData.getScriptTag(script));
            output.println("ScriptRanges[] = {");
            output.print("   ");
            
            for (int offset = 0; offset < offsets.size(); offset += 1) {
                Integer i = (Integer) offsets.elementAt(offset);
                
                output.print(i.intValue());
                output.print(", ");
            }
            
            output.println("-1");
            output.println("};\n");
        }
        
        output.println("le_int16 *ScriptRun::scriptRangeOffsets[] = {");
        
        for (int script = minScript; script <= maxScript; script += 1) {
            output.print("    ");
            output.print(scriptData.getScriptTag(script));
            output.print("ScriptRanges");
            output.print(script == maxScript? "  " : ", ");
            output.print("// ");
            output.println(scriptData.getScriptName(script));
        }
        
        output.println("};");
        
        closeFile();
    }
    
    private static final String preamble = 
    "#include \"LETypes.h\"\n" +
    "#include \"LEScripts.h\"\n" +
    "#include \"ScriptRun.h\"\n" +
    "\n" +
    "ScriptRecord ScriptRun::scriptRecords[] = {";
    
    private static final String postamble =
    "};\n";
}