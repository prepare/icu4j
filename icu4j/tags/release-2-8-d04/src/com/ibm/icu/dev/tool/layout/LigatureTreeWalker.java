/*
 *******************************************************************************
 * Copyright (C) 1998-2003, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * Created on Dec 3, 2003
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/tool/layout/LigatureTreeWalker.java,v $
 * $Date: 2003/12/17 04:54:40 $
 * $Revision: 1.2 $
 * 
 *******************************************************************************
 */
package com.ibm.icu.dev.tool.layout;

import java.util.Vector;

import com.ibm.icu.impl.Utility;

public class LigatureTreeWalker extends TreeWalker implements LookupSubtable
{
    protected int[] componentChars;
    protected int componentCount;
    protected int lastFirstComponent;

    protected Vector ligatureSets;
    protected Vector ligatureSet;
    
    public LigatureTreeWalker()
    {
        componentChars = new int[30];
        componentCount = 0;
        lastFirstComponent = -1;
        ligatureSet = null;
        ligatureSets = new Vector();
    }

    public void down(int ch)
    {
        componentChars[componentCount] = ch;
        componentCount += 1;
    }

    public void up()
    {
        if (componentCount > 0) {
            componentCount -= 1;
        }
    }

    public void ligature(int lig)
    {
        int firstComponent = componentChars[0];

        if (lastFirstComponent != firstComponent) {
            if (ligatureSet != null) {
                ligatureSets.addElement(ligatureSet);
            }

            ligatureSet = new Vector();
            lastFirstComponent = firstComponent;
        }

        ligatureSet.addElement(new LigatureEntry(lig, componentChars, componentCount));
    }

    public void done()
    {
        if (ligatureSet != null) {
            ligatureSets.addElement(ligatureSet);
        }
    }
    protected int firstComponentChar(int ligatureSetIndex)
    {
        Vector ligatureSet = (Vector) ligatureSets.elementAt(ligatureSetIndex);
        LigatureEntry firstEntry = (LigatureEntry) ligatureSet.elementAt(0);

        return firstEntry.getComponentChar(0);
    }

    protected void writeCoverageTable(OpenTypeTableWriter writer)
    {
        int ligatureSetCount = ligatureSets.size();

        writer.writeData(1);
        writer.writeData(ligatureSetCount);

        for (int set = 0; set < ligatureSetCount; set += 1) {
            writer.writeData(firstComponentChar(set));
        }
    }
    
    public void writeLookupSubtable(OpenTypeTableWriter writer)
    {
        int coverageOffset, ligatureSetOffset, ligatureTableOffset;
        int ligatureSubstitutionBase = writer.getOutputIndex();
        int ligatureSetCount = ligatureSets.size();
        
        //System.out.println("Writing " + tableName + "...");

        writer.writeData(1);   // substFormat
        
        coverageOffset = writer.getOutputIndex();
        writer.writeData(0);   // coverageTableOffset (will fix later)
        
        writer.writeData(ligatureSetCount);

        ligatureSetOffset = writer.getOutputIndex();
        for (int set = 0; set < ligatureSetCount; set += 1) {
            writer.writeData(0);  // ligatureSet offset - will fix later
        }
        
        for (int set = 0; set < ligatureSetCount; set += 1) {
            System.out.print(Utility.hex(firstComponentChar(set), 6) + ": ");
            
            Vector ligatureSet = (Vector) ligatureSets.elementAt(set);
            int ligatureCount = ligatureSet.size();
            int ligatureSetAddress = writer.getOutputIndex();
            
            System.out.println(ligatureCount + " ligatures.");
            
            writer.fixOffset(ligatureSetOffset++, ligatureSubstitutionBase);
            writer.writeData(ligatureCount);
            
            ligatureTableOffset = writer.getOutputIndex();
            for (int lig = 0; lig < ligatureCount; lig += 1) {
                writer.writeData(0);  // ligatureTableOffset (will fix later)
            }
            
            for (int lig = 0; lig < ligatureCount; lig += 1) {
                LigatureEntry entry = (LigatureEntry) ligatureSet.elementAt(lig);
                int componentCount = entry.getComponentCount();
                
                writer.fixOffset(ligatureTableOffset++, ligatureSetAddress);
                writer.writeData(entry.getLigature());
                writer.writeData(componentCount);
                
                for (int comp = 1; comp < componentCount; comp += 1) {
                    writer.writeData(entry.getComponentChar(comp));
                }
            }
        }
        
        writer.fixOffset(coverageOffset, ligatureSubstitutionBase); 
        writeCoverageTable(writer);
    }
}
