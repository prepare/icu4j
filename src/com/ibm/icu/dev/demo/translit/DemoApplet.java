/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/demo/translit/DemoApplet.java,v $ 
 * $Date: 2001/11/28 19:27:09 $ 
 * $Revision: 1.5 $
 *
 *****************************************************************************************
 */
package com.ibm.demo.translit;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import com.ibm.icu.demo.components.AppletFrame;

/**
 * A simple Applet that shows a button.  When pressed, the button
 * shows the DemoAppletFrame.  This Applet is meant to be embedded
 * in a web page.
 *
 * <p>Copyright (c) IBM Corporation 1999.  All rights reserved.
 *
 * @author Alan Liu
 * @version $RCSfile: DemoApplet.java,v $ $Revision: 1.5 $ $Date: 2001/11/28 19:27:09 $
 */
public class DemoApplet extends Applet {

    Demo frame = null;
    
    private static final String COPYRIGHT =
        "\u00A9 IBM Corporation 1999. All rights reserved.";

    public static void main(String args[]) {
        final DemoApplet applet = new DemoApplet();
        new AppletFrame("Transliteration Demo", applet, 640, 480);
    }

	public void init() {

		Button button = new Button("Transliteration Demo");
		button.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        if (frame == null) {
                    frame = new Demo(600, 200);
                    frame.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent we) {
                            frame = null;
                        }
                    });
                }
                frame.setVisible(true);
                frame.toFront();
		    }
		});

		add(button);

        Dimension size = button.getPreferredSize();
        size.width += 10;
        size.height += 10;

		resize(size);
	}
	
    public void stop() {
        if (frame != null) {
            frame.dispose();
        }
        frame = null;
    }
}
