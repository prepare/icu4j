/*
 * @(#)$RCSfile: DocumentWindow.java,v $ $Revision: 1.1 $ $Date: 2000/04/20 17:43:09 $
 *
 * (C) Copyright IBM Corp. 1998-1999.  All Rights Reserved.
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
package com.ibm.richtext.demo;

public interface DocumentWindow {

    public void doNew();
    
    public void doOpen();
    
    public boolean doClose();
    
    public boolean doSave();
    
    public boolean doSaveAs(int format);
    
    public void doPrint();
    
    public void setSize(int wd, int ht);
    
    public void show();
}