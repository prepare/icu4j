/*
 * (C) Copyright IBM Corp. 1998-2004.  All Rights Reserved.
 *
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */
// Requires Java2

package com.ibm.richtext.print;

import com.ibm.richtext.styledtext.MConstText;
import com.ibm.richtext.textlayout.attributes.AttributeMap;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;

final class PrintContext implements Printable {
    
    static final String COPYRIGHT =
                "(C) Copyright IBM Corp. 1998-1999 - All Rights Reserved";

    private MConstTextPrintable fPrintable;
    
    PrintContext(MConstText text, AttributeMap defaultStyles, PageFormat pf) {
        
        int width = (int) Math.round(pf.getImageableWidth());
        int height = (int) Math.round(pf.getImageableHeight());
        int left = (((int)Math.round(pf.getWidth())) - width) / 2;
        int top = (((int)Math.round(pf.getHeight())) - height) / 2;
        
        Rectangle pageRect = new Rectangle(left, top, width, height);
        fPrintable = new MConstTextPrintable(text, defaultStyles, pageRect);
    }
    
    public int print(Graphics graphics,
                     PageFormat format,
                     int pageIndex) throws PrinterException {
        
        if (false)
            throw new PrinterException("save trees");
            
        if (fPrintable.print(graphics, pageIndex) == MConstTextPrintable.PAGE_EXISTS) {
            return PAGE_EXISTS;
        }
        else {
            return NO_SUCH_PAGE;
        }
    }
    
    static void userPrintText(MConstText text,
                              AttributeMap defaultStyles,
                              Frame frame,
                              String jobTitle) {

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName(jobTitle);
        if (job.printDialog()) {
            job.setPrintable(new PrintContext(text, defaultStyles, job.defaultPage()));
            try {
                job.print();
            }
            catch(PrinterException e) {
                System.out.println("Printer exception: " + e);
            }
        }
    }
}
