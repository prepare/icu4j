/*
 *******************************************************************************
 * Copyright (C) 1997-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/demo/impl/DemoApplet.java,v $ 
 * $Date: 2000/03/10 03:47:42 $ 
 * $Revision: 1.3 $
 *
 *****************************************************************************************
 */

package com.ibm.demo;

import java.applet.Applet;
import java.util.Locale;
import java.awt.*;
import java.awt.event.*;

public abstract class DemoApplet extends java.applet.Applet {
    private Button   demoButton;
    private Frame    demoFrame;
	private static int demoFrameCount = 0;

    protected abstract Frame createDemoFrame(DemoApplet applet);
    protected Dimension getDefaultFrameSize(DemoApplet applet, Frame f) {
    	return new Dimension(700, 550);
    }

    //Create a button that will display the demo
    public void init()
    {
        setBackground(Color.white);
        demoButton = new Button("Demo");
        demoButton.setBackground(Color.yellow);
        add( demoButton );

        demoButton.addActionListener( new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                if (e.getID() == ActionEvent.ACTION_PERFORMED) {
                    demoButton.setLabel("loading");

                    if (demoFrame == null) {
                       demoFrame = createDemoFrame(DemoApplet.this);
                       showDemo();
                    }

                    demoButton.setLabel("Demo");
                }
             }
        } );
    }

    public void showDemo()
    {
    	demoFrame = createDemoFrame(this);
        demoFrame.layout();
        Dimension d = getDefaultFrameSize(this, demoFrame);
        demoFrame.resize(d.width, d.height);
        demoFrame.show();
		demoFrameOpened();
    }

    public void demoClosed()
    {
        demoFrame = null;
		demoFrameClosed();
    }

	protected static void demoFrameOpened() {
		demoFrameCount++;
    }
	protected static void demoFrameClosed() {
		if (--demoFrameCount == 0) {
			System.exit(0);
		}
    }
}

