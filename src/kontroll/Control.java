package kontroll;
//*******************************************************************
//
// GAME OF CONTROL
//
//                                 (c) Joachim Parrow 2003, 2006, 2010
//
//*******************************************************************

import javax.swing.*;
public class Control

 {

     static final String VERSION_NUMBER = "6.3";              // version number
     static final boolean testSetup = true;                  // If true, start a test session

     public static void main(String[] args)
     {

       // The following hack is needed to make the background colors of buttons
       // show on a mac. If it makes trouble just delete it.

         String laf = UIManager.getCrossPlatformLookAndFeelClassName();
         try {
             UIManager.setLookAndFeel(laf);
         } catch (Exception e) {
             System.err.println("Error loading L&F: " + e.getMessage());
         }


      // main just starts one instance of StartControl - or two instances if testing.

        if (!testSetup) {
            new StartControl(false, false);       // Normal start: Here goes!
        } else {
            new StartControl(true, false);        // Test start: start a listener
            new StartControl(false, true);        // and then start a client on the same thread (OK for test purposes!)
        }
    }
}