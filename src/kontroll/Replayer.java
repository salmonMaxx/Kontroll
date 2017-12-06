/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;
//***************************************************************************
 //
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;


 //  Replayer: class to replay a game
 //
 //***************************************************************************


   class Replayer extends JPanel implements ActionListener {

      private final  PlayBack     parent;                     // The guy who started me
      private final  ObjectInputStream playback;              // where I get the game
      private final  Board        board;                      // where I show the game
      private final  JFrame       myFrame;                    // where the board sits
      private final  Timer oneSecondTimer, phaseTwoTimer, endMoveTimer, endBuildTimer;   // various timers used in replay

      private  boolean aborted = false;                 // set to true if I should abort
               boolean paused = false;                  // set to true when paused
               boolean ffw = false;                     // set true if fast forward
      private  boolean terminated = false;              // set to true when recording ends

      private  String reason = "End of Recording";      // reason for abortion

      private  Message message;                         // current message being replayed
      private  int     clock = 0;                       // elapsed game time
      private  long    startTime;                       // system time when a move starts to be shown
      private  Piece   currentPiece;                    // highlighted piece during a move
      private  Piece   outlinePiece;                    // outline shown during a build


      private  Board.Position outline, sourcePos, destPos; // position of outline (if any) and of move to be played

      private  boolean isWhite;                           // color of player who sent last message


  //--------------------------------------------------------------------------------
  // Constructor just sets things up
  //--------------------------------------------------------------------------------

       public Replayer(StartControl startControl, ObjectInputStream playback, PlayBack parent)

       {                                      // remember parameters
            this.playback = playback;
            myFrame=parent.myFrame;
            this.parent = parent;
                                                                                            // get me a board of the right size
            board = new Board(parent.rg.isLite, parent.rg.withHills, parent.rg.graphics, parent.rg.shape,
                              parent.rg.boardShape, parent.rg.height, parent.rg.width, parent.rg.pebbles, true);

            board.allVisible = true;                                                // during playback all is visible
            board.playBackBoard = true;

           add(Box.createRigidArea(new Dimension(board.SIZE*(board.WIDTH+2) -10,   // crazy, but need to tell layout manager how big I am
                                    board.SIZE*(board.HEIGHT+2)-10)));

            oneSecondTimer    = new Timer(1000, this);                          // create oneSecondTimer for one second delay
            oneSecondTimer.setRepeats(false);

            phaseTwoTimer = new Timer( 400, this);                              // this fires 0.4s delay for showing red piece during move
            phaseTwoTimer.setRepeats(false);
             endMoveTimer = new Timer( 400, this);                              // ditto, showing piece after it moved
            endMoveTimer.setRepeats(false);
             endBuildTimer = new Timer( 400, this);                             // ditto, showing piece being built
            endBuildTimer.setRepeats(false);
        }


   //-------------------------------------------------------------------
   // Here we paint the replay
   //-------------------------------------------------------------------

   @Override
   public void paintComponent (Graphics g)
   {  Graphics2D g2 = (Graphics2D)g;
   g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

     board.paint(g2,                                          // All we have to do is to draw the board
                 false,                                         // not waiting for the oneSecondTimer
                 false,                                         // no stall message
                 parent.rg.length*60 - clock                    // remaining time in seconds
                 );

       if (outlinePiece != null)                                // possibly, show outline of a build in progress
            outlinePiece.drawOutline(g2, outline, Color.red);
   };

 //-----------------------------------------------------------------------------
 //  Here we go: replay starts the replay
 //-----------------------------------------------------------------------------


       void replay()
       {
        if (parent.rg.withHills)                               // if the game is with hills
            try                                                // then first read the hills from the recording
              {Board.Hill [] hills = (Board.Hill []) playback.readObject();
               board.setHills(hills);                          // and tell my board to use these hills
               }
            catch (Exception e){aborted = true; reason = e.toString();}  // if that fails then abort
        myFrame.pack();                                             // set up my frame for display
        myFrame.setVisible(true);

        board.calculateControl();                                   // initial control calculation, for white and with all visible
        board.generateBoardGraphics(null);                          // generate board graphics
        message = null;                                             // yet no message read
        oneSecondTimer.start();                                     // start the oneSecondTimer who calls "playMoves" every second"
       }

   //---------------------------------------------------------------------------------
   //  Actionlistener methods: just go to the right places on timeouts
   //---------------------------------------------------------------------------------

     public void actionPerformed(ActionEvent e)
   {
     if (e.getSource()==oneSecondTimer)             //  oneSecondTimer:
            { if (!paused & !terminated & !aborted)  // If I am supposed to continue to replay every second
                  {startTime = System.currentTimeMillis();    // record the time
                  clock++;                     // increase displayed game time
                  playMoves();                 // and go see if there are any moves to play
                  }
              else if (ffw) playMoves();
              else if (paused)
                  sleepAWhile(1000);       // else, if on fast forward or paused keep the oneSecondTimer alive
            }                             // if aborted or terminated don't restart the oneSecondTimer

     if  (e.getSource()==phaseTwoTimer)      // phaseTwoTimer take us to phase two of a move
     {playMove2();}

     if (e.getSource()==endMoveTimer)       // endMoveTimer takes us to phase three of a move
     {playMove3();}

     if (e.getSource() == endBuildTimer)     // and endBuildTimer to phase two of a build
     {playBuild2();}
   }


 //-----------------------------------------------------------------------------
 //  Playmoves: executes every second. Reads a recorded move if necessary, and replays it
 //-----------------------------------------------------------------------------

     private void playMoves()

     {

        {if (message == null)                               // if I have no current move
        try {message = (Message)playback.readObject();}     //   then read one
          catch (Exception e){aborted = true; reason = e.toString();}  // if that does not work I must exit

           if (!aborted & !terminated)
               {if (ffw | message.time <= clock)            // if the time of the message is now (or before)
                 {clock = message.time;                     // then let displayed time be that of the message
                  playBackMove(message);                    // and play it back
                  message = null;}                          // and consume it
                else                                        // if time of curent message is later
                    {repaint();                                 // then just wait and display new clock
                    sleepAWhile(1000);
                    }
               }
           else                                             // if I must exit then tell user
             {JOptionPane.showMessageDialog(this, "Recording ended\n" +
                                               "The reason is:\n"+
                                               reason,
                                               "Recording ended ", JOptionPane.INFORMATION_MESSAGE);

             }
         }
     }



  //------------------------------------------------------------------------------------
  // Play back one move
  //------------------------------------------------------------------------------------


      private void playBackMove(Message message)      // message is the encoded record of one move
       {

          sourcePos = message.s;  // unpack move information
          destPos = message.p;
          isWhite = message.white;

          switch (message.messageType)
              {case MOVE:    playMove(); break;             // an ordinary move displayed by playMove
               case BUILD:   playBuild(); break;            // a build displayed by playBuild
               case QUIT:    terminated = true;             // a quit: tell my parent and stop playback
                             parent.endGame(message.text);
                             break;
              case EXPIRED:  oneSecondTimer.setDelay(1); oneSecondTimer.start(); break; // EXPIRED message, just get the next one

              default:       message = null;                 // no recognizable move: tell parent
                             parent.endGame("Recording broken");
              }

       }

    //--------------------------------------------------------------------------
    // ordinary move phase one
    //--------------------------------------------------------------------------

   private void playMove()                     //  moves from source to dest
   {
       currentPiece=board.find(sourcePos);    // so highlight the piece at source
       board.generateBoardGraphics(currentPiece);
       repaint();
       phaseTwoTimer.start();                      // and wait some time for phase two
   }


    //--------------------------------------------------------------------------
    // ordinary move phase two
    //--------------------------------------------------------------------------


   private void playMove2()
   {  currentPiece = currentPiece.moveTo(destPos);

      board.generateBoardGraphics(currentPiece);                    // highlight current piece
      repaint();
      endMoveTimer.start();                                             // and wait some time for phase three
   }

    //--------------------------------------------------------------------------
    // any move final phase
    //--------------------------------------------------------------------------

   private void playMove3()
   {
    currentPiece=null;              // take away highlighting
     board.calculateControl();      // recalculation of control needed now
     board.generateBoardGraphics(currentPiece);
    repaint();                       // and repaint

    if (ffw & !terminated & !aborted) playMoves();   // if on fast forward go immediately to play next move
    else sleepAWhile((int)(System.currentTimeMillis() - startTime)); // else wait what is left of the second
   }


   private void sleepAWhile(int delay)       // wait the specified number of milliseconds
   {
             oneSecondTimer.setDelay(Math.max(delay,1));
             oneSecondTimer.start();
   }



    //--------------------------------------------------------------------------
    // build move first phase
    //--------------------------------------------------------------------------


   private void playBuild()                  // a build move at dest of type sx
   {
     outlinePiece = board.newPiece(message.pt, message.white, message.white, null);         // the new piece is first present as an outline
     if (message.pt == Piece.Type.PEBBLE) board.decreaseRemaining(isWhite); // needed since we created the pebble at -1,-1 (ow board would do this automatically)
     outline = destPos;
     board.generateBoardGraphics(currentPiece);
     repaint();                                        // repaint
     endBuildTimer.start();                            // and wait some time for next build phase
   }

    //--------------------------------------------------------------------------
    // build move second phase
    //--------------------------------------------------------------------------

   private void playBuild2()
   {
   //
     currentPiece =  outlinePiece;                          // place the new piece at dest and highlight it
     currentPiece.pos = outline;

     board.addPiece(currentPiece);
     outlinePiece = null;                                   // get rid of the outline
     endMoveTimer.start();                                      // and wait for final phase

   }





 }   //---------------------------- end of replayer ---------------------------