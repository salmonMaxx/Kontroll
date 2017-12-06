/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.Timer;


/**
 *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * ControlTimer
 *
 * This is the timer for determining when a player is allowed to move
 *
 *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

 class ControlTimer extends JPanel implements ActionListener

{

   private static final Color BACKGROUNDCOLOR =    new Color(160,180,120);   // Background color for the timer (currently same as for board)

    // Various sizing constants for graphics


   private static final int TOP_OFFSET = 0;              // Offset from top border
           static final int BAR_HEIGHT = 25;             // timer height
   private int LEFT_OFFSET;                              // Offset from left border
   private int BAR_LENGTH;                               // timer bar length

   private final int UPDATE_FREQUENCY = 20;                    // how often to update in milliseconds


   private Timer timer;                                                            // The timer

   private Player player;                                                          // Whom to tell when timer expires

   private boolean whiteColor;                                                     // true if timer should be painted white

   private float remainingFraction = 0f;                                           // remaining fraction of time

   private long startTime;                                                         // Epoch time in ms when timer started

   private int totalTime;                                                          // total  time in ms

   private int size, width;                                                        // board parameters (needed so the timer will fit nicely)


 //------------------------------------------------------------------------------------
 //  Create a controlTimer. Just done once for each player.
 //------------------------------------------------------------------------------------

     ControlTimer(boolean whiteColor) {
        super();                            // this is also a JPanel
        this.whiteColor = whiteColor;
    }

 //------------------------------------------------------------------------------------
 //  Accept a message from my Player to initiate variables
 //------------------------------------------------------------------------------------

     void getPlayer(Player player,    // my player
                          int size,         // size of one board square in pixels
                          int width)        // board width in number of squares
    {                                       // This is how the player tells me who he is so I can report timeouts
      this.player = player;
      this.size   = size;                       // he also tells me how big the board is
      this.width  = width;                     // so I set the sizing constants from this
      LEFT_OFFSET = size;
      BAR_LENGTH  = size*width;
    }

 //------------------------------------------------------------------------------------
 //  Paint the timer
 //------------------------------------------------------------------------------------

    @Override
    public void paintComponent(Graphics page)
    {super.paintComponent(page);                // This is also a JPanel

     page.setColor(BACKGROUNDCOLOR);        // Paint background
     page.fillRect(0,0, size*(width+2), 30);

                          // Paint timer bar

     if (whiteColor) page.setColor(Color.white); else page.setColor(Color.black);
     page.fillRect(LEFT_OFFSET, TOP_OFFSET,
              (int)(remainingFraction*BAR_LENGTH),
              BAR_HEIGHT);
    }

//----------------------------------------------------------------------
// Start timer with totalTime in seconds
//-----------------------------------------------------------------------


    void startIt(float totalTime)
    {
    startTime = System.currentTimeMillis();                         // remember when it started
    remainingFraction = 1f;                                         // at start all the time is remaining
    this.totalTime = (int)(1000*totalTime);                         // total time in ms
    timer = new Timer(UPDATE_FREQUENCY, this);                      // create a new Timer reporting to me
    repaint();                                                      // draw me, for the first time

    timer.start();                                                  // start the timer
    }

//----------------------------------------------------------------------
// Interrupt from the timer
//-----------------------------------------------------------------------

    public void actionPerformed(ActionEvent e)

    {
     remainingFraction = 1 - (float)(System.currentTimeMillis()- startTime)/(float)totalTime; // Fraction of total time that remains
     if (remainingFraction < 0) remainingFraction = (float)0;                                 // (should neve be negative)

     if (remainingFraction<=0) {                                        // If finished then stop timer and notify player
         timer.stop();
         repaint();
         player.timeOut();
     }
     else repaint();                                                    // OW just repaint the timer bar


    }


//----------------------------------------------------------------------
// Stop the timer
//-----------------------------------------------------------------------

    void stopIt()
    {if (timer != null) timer.stop();
    }


}