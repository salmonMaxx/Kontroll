/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

 /*
 *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 *Main class in the game of control
 *
 *Serves one player with game logics
 *Reacts to player mouse commands , sends moves to opponent,
 * and reacts to to moves received by opponent
 *
 *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 */



class Player extends JPanel implements ActionListener
{


 //----------- player activity state ---------------------------------------------------------------------

   private Piece currentPiece = null;                             // Piece over which mouse was pressed
   private Piece currentSelection = null;                         // Selection for promotions
   private Board.Position currentPos;                             // where the mouse is
   private boolean waiting;                                       // True if Timer not expired so I must wait
   private boolean clockHasExpired = false;                       // true when game clock has expired and game is over

 //----------- game activity state -----------------------------------------------------------------------

           int clock = 0;                                         // elapsed time in seconds

   private int messageNumber = 0;                                 // number communication messages incrementally
   private int ackedNumber = -1;                                  // last acknowledged message to opponent

 //----------- game parameters (should not change during a game) -----------------------------------------

           boolean playWhite;                                     // true if currently playing white

   private Board board;                                          // the board for this player

   private ControlTimer controlTimer;                            // my Controltimer
   private PlayerInterface myInterface;                          // my Interface (runs the quit button etc)
   private MoveListener moveListener;                            // My listener for moves by opponent

   private Timer timer;                                          // oneSecondTimer for the game clock

   private ObjectOutputStream outgoing;                          // for sending moves to opponent
   private ObjectInputStream incoming;
   private ObjectOutputStream record;                            // stream to recording file

   private int GAME_LENGTH;                                      // Game length in seconds
   private float timeBase;                                       // number of seconds for one time unit (nimbler move)

   boolean isLite;                                              // true if playing controlite
   boolean withHills;                                           // true if hills on the board

   private Semaphore mutex;                              // This protects critical sections updating game states

   private void waitLock()                                        // Semaphore wait
       { mutex.acquireUninterruptibly();}

   private void releaseLock()                                     // Semaphore signal
       {mutex.release();}
   
   ArrayList<String> keyTypedArray = new ArrayList<>();
   private final String password = "stspwns";

//------------------------------------------------------------------------------------------------------------
// Player constructor
//------------------------------------------------------------------------------------------------------------

                                                // Constructor parameters:

     Player(     boolean isLite,                 // true if playing controlite
                 boolean withHills,              // true if playing with Hills
                 boolean playwhite,              // color for this player (true => white)
                 ControlTimer controlTimer,      // my controlTimer
                 ObjectInputStream incoming,     // streams to and from opponent
                 ObjectOutputStream outgoing,
                 ObjectOutputStream record,      // if not null then the stream where games are recorded
                 int graphics,                   // graphic size parameter
                 int shape,                      // shape of controlled areas
                 int boardShape,                 // shape of board
                 int height,                     // Game parameters:
                 int width,                      //   board size
                 int pebbles,                    //   inital number of pebbles
                 int speed,                      // speed choice (1=fast, 2=normal, 3=slow)
                 int length)                     // game length in minutes

   {

    this.controlTimer = controlTimer;   // remember my controlTimer
    this.playWhite = playwhite;         // remember if I play for white
    this.outgoing = outgoing;          // remember my streams to  opponent and to recording file
    this.incoming = incoming;
    this.record = record;
    this.isLite=isLite;
    this.withHills = withHills;

    waiting = true;                    // initially, you cannot move!

    board = new Board(isLite, withHills, graphics, shape, boardShape, height, width, pebbles, playwhite);         // get me a board


    board.allVisible = false;                                      // Initially not all is visible

    GAME_LENGTH = length*60;                                      // GAME_LENGTH is in seconds

    timeBase = (float)((speed == 1) ? 1 : (speed == 2) ? 2 : 4);  // set timeBase based on choice of speed
    if (Control.testSetup) timeBase /= 10;

    add(Box.createRigidArea(new Dimension(board.SIZE*(board.WIDTH+2) -10,   // crazy, but need to tell layout manager how big I am
                                board.SIZE*(board.HEIGHT+2)-10)));


    mutex = new Semaphore(1);                               // Critical sections protects updates of game state

    ControlListener myListener = new ControlListener();  // Add my inner class to the listeners (gets moves from mouse)
      addMouseListener (myListener);
      addMouseMotionListener (myListener);
      addKeyListener(myListener);


    controlTimer.getPlayer(this, board.SIZE, board.WIDTH);       // Tell my Timer who I am
    controlTimer.startIt(2f);                                    // initial countdown: two seconds


    timer = new Timer(1000, this);                                 // Timer for the game clock
    timer.start();

    board.calculateControl();                                      // Initial computation of square states
    currentSelection = board.defaultselector();                    // and of selector
    board.generateBoardGraphics(null);                             // and paint the board



   };


   //--------------------------- End of Constructor ------------------------------------------

   //--------------------------------------------------------------------
   // Method to accept identity of my interface (only done once, in the setup phase)
   // Here are also generated the Hills, for a white player
   //--------------------------------------------------------------------

    void setInterface(PlayerInterface i)
    {myInterface=i;                                 // save my interface
       if (withHills & playWhite)                   // if hills are used and I play white
          {Board.Hill [] hills = board.generateHills();  // then let my board generate the hills
          try
             {outgoing.writeObject(hills); }      // send them to opponent
          catch (Exception e)                    // if impossible then report an error
              {communicationError(e);}
            recordMessage(hills);                 // anyway, record them on the recording
           }

    moveListener = new MoveListener();               // start listening for moves by opponent
    moveListener.start();

    }


   //-----------------------------------------------------------------
   //  Draws board, and possibly an outline because of a mouse position etc
   //-----------------------------------------------------------------


   @Override
   public void paintComponent (Graphics g) {
      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

   board.paint(g,                                          // Draw the board
                waiting | ackedNumber != messageNumber-1,   // signal move possible if this is true
                (ackedNumber != messageNumber -1 & !waiting) & !board.allVisible,  // Show stall  if oneSecondTimer has expired and unacked messages,
                 GAME_LENGTH - clock                        // remaining time

                 );

     if (currentPiece != null)                               // show if mouse dragged over legal possibility
         if (currentPiece.canMove(currentPos))
            currentPiece.outline.drawOutline(g, currentPos, Color.red); // if so, show a red outline of it there

     if (currentSelection != null && currentPiece == null)        // Highlight current selector if any
        {currentSelection.drawOutline(g, Color.red);

        if (!waiting && currentPos != null && board.isLegal(currentPos) &&
            currentSelection.canBuild(currentPos))              // Highlight mouse position if build possible
                 currentSelection.drawOutline(g, currentPos, Color.red);
     }
   }






   //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
   //
   // Game methods. Note that before calling any of these, the caller must first claim the mutex lock.
   //  That way, there will be no races between the thread that listens to player moves through the mouse
   //  and the thread that listens through opponent moves through the incoming stream.
   //
   //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

   //*************************************************************************
   //
   // Communication
   //
   //*************************************************************************

   //-----------------------------------------------------
   //
   //  getMove: methods to receive moves by opponent
   //
   //-----------------------------------------------------

    private void getMove(Board.Position s, Board.Position p)          // opponent moves from s to p
   {
     board.find(s).moveTo(p);        // just move the piece
     endOpponentMove();
   }


    private void getMove(Piece.Type pt, Board.Position p)         // Overloaded method: a build move at p of type pt
   {
    board.newPiece(pt, !playWhite, false, p);     // so place the new piece there
    endOpponentMove();
   }


   private void endOpponentMove()
   {
       board.calculateControl();      // recalculate and repaint
       board.calculateBuildable();
       appraise();
       checkCurrent();
       board.calculateHighlight(currentPiece);
       repaint();
   }

   //----------------------------------------------------------------
   //   set Current piece to null if it is not in control
   //----------------------------------------------------------------

   private void checkCurrent()
   {
       if (currentPiece != null && ! board.iControl(currentPiece))
           currentPiece = null;
       if (currentPos != null && ! board.iControl(currentPos))
           currentPos = null;
   }



   //----------------------------------------------------------
   //
   // Methods for sending move to opponent
   //
   //----------------------------------------------------------

    private synchronized void sendMessage(Message message)    // actually "synchronized" is probably not needed here bc of the mutex
   {

        try {
             outgoing.writeObject(message);      // send it to opponent
             }
        catch (Exception e)                // if impossible then report an error (unless I already quit)
          {if (message.messageType != Message.MessageType.QUIT) communicationError(e);}

   }


   //--------------------- Report a communication error -------------------------------------

   private void communicationError(Exception e)
    {
        myInterface.endGame("Lost Contact with Opponent",       // end of game because communication broken
                         "Contact between players broken");
      }



   //-----------------------------------------------------------------------------------
   // SendMove: builds a message out of the info in a move, and sends it using sendMessage
   //-----------------------------------------------------------------------------------

   private void sendMove(Message.MessageType mt,       // message type
                 Board.Position s,                  // source position
                 Board.Position p,                  // destination position
                 Piece.Type pt                  // piece type of build
                 )
   {
       Message message = new Message (mt,                   // message type
                                      clock,                // game clock at time of sending
                                      messageNumber++,      // attach and increase message number
                                      s,
                                      p,
                                      pt,
                                      playWhite,            // true if move done by white
                                      "");                  // unused string parameter
       sendMessage(message);
       if (message.messageType != Message.MessageType.QUIT) recordMessage(message);    // record the message (QUIT messages are recorded at another place)

   }


   //-----------------------------------------------------
   //
   //  tellMove: methods to send moves to opponent (uses sendMove)
   //
   //-----------------------------------------------------

   private void tellMove(Piece.Type pt, Board.Position p)             // tell a build move
   {sendMove(Message.MessageType.BUILD, null, p, pt);
   }

   private void tellClockHasExpired()                               // tell that the game clock expired
   {sendMove(Message.MessageType.EXPIRED,null,null,null);}

   private void tellMove(Board.Position s, Board.Position p)     // overloaded: tell a  move
   {sendMove(Message.MessageType.MOVE, s, p, null);
   }




   //------------------------------------------------------------------------
   //
   // Record a move if recording is on
   //
   //------------------------------------------------------------------------

    private void recordMessage(Object message)
   {
       if (record != null)                      // if recording is on:
           try
             {record.writeObject(message);}     // record the message
           catch
              (Exception e)                     // if this fails:
                {try {record.close();}          // close the file if I can
                     catch (Exception e2){}
                 record = null;                 // turn recording off
                                                // and inform user
                 JOptionPane.showMessageDialog(this,
                                                "Recording ceased.\n"+
                                               "The reason given by the system is:\n" +
                                               e.toString(),
                                               "File Error",
                                               JOptionPane.ERROR_MESSAGE);
                 }
   }




  //**************************************************************************
  //
  //    Methods to serve my user in making moves: determining effects of mouse actions
  //
  //**************************************************************************


   //-------------------------------------------------------------------------
   // test if piece at p can be set as current piece
   //-------------------------------------------------------------------------

   private boolean canSetCurrentPiece(Board.Position p)
   {Piece candidate = board.isLegal(p) ?       // get the candidate piece if on board
                         board.find(p)
                       : null;
    return (
           !waiting                             // if Timer still runs no piece can be set as current
        && candidate != null                    // x,y must be at a piece
        && candidate.whiteside == playWhite     // of my color
        && candidate.moveable                   // and moveable
        && board.iControl(p)                    // and under my control

           );
   }

   //-------------------------------------------------------------------------
   // set piece at p as current, if possible
   //-------------------------------------------------------------------------

   private void setCurrentPiece(Board.Position p)
       {if (canSetCurrentPiece(p))               // can you set it?
         currentPiece = board.find(p);            // if so set it
         else currentPiece = null;
       };



  //-----------------------------------------------------------------
  // Makes a move of currentPiece to p if possible (mutex claimed by caller)
  //-----------------------------------------------------------------

  private void attemptToMove(Board.Position p)
  {
       if (currentPiece != null && currentPiece.canMove(p))             // is it legal other move?
           {                                                            // yes, then do it:
            tellMove(currentPiece.pos, p);                              //   tell opponent my move
            currentPiece = currentPiece.moveTo(p);                      //   move it. Note the piece may change!
            endMove(currentPiece.moveTime*timeBase);                    //   end of move (note moveTo may determine move time)
           }
                                                                  // in any case, even if not legal move
        currentPiece = null;                                     // drop current piece and repaint
        board.calculateHighlight(null);

        repaint();

  }


  //-----------------------------------------------------------------
  // Executes a build of type currentSelection at p if possible.
  //-----------------------------------------------------------------

private void attemptToBuild(Board.Position p)
  {
      if (board.isLegal(p) && currentSelection.canBuild(p))              // Check it is on the board and ok to build
         {Piece newPiece = board.newPiece(currentSelection.pieceType, playWhite,true, p);  // build it
          tellMove(currentSelection.pieceType, p);                                // tell opponent what my move is
          endMove(newPiece.buildTime*timeBase);                                  // end of build move.
      }

     repaint();

   };


   //-----------------------------------------------------------------
   // End of move. StarttTimer to wait for specified time, recalculate etc
   //-----------------------------------------------------------------


   private void endMove (float delay)
      {
              waiting  = true;                                  // now I am waiting
              currentPiece = null;                              // reset current piece
              board.calculateControl();                         // Recalculate control
              board.calculateBuildable();
              currentSelection = board.defaultselector();       // recalculate default selector
              board.calculateHighlight(null);                   // and reset highlighted squares

              controlTimer.startIt(delay);                      // start my controlTimer

       }

   //-----------------------------------------------------------------
   // appraise: check if someone has won or if it is a draw. If so report it to my interface
   //-----------------------------------------------------------------

                                                            // Note: this is only done when receiving messages from opponent
                                                            // (either his moves or acks of my moves)


    private void appraise()
      {
          boolean iHaveControl = false;                       // will become true if I have control of a piece
          boolean opponentHasControl = false;                 // will become true if opponent has control of a piece


          for (Piece thePiece : board.pieces)     // for all pieces, check if controlled by owner
          {
             if (thePiece.myside && board.iControl(thePiece)) iHaveControl = true; // and record this
             if (!thePiece.myside && board.opponentControls(thePiece)) opponentHasControl = true;
          }

           if (!opponentHasControl & !iHaveControl)                        // report a draw because no one has control
                 myInterface.endGame("DRAW: No one controls  pieces",
                                     "DRAW: No one controls  pieces");

           else if (!opponentHasControl)                                // report a win because opponent cannot move
               myInterface.endGame("You win: opponent has no control",                         // (note: second param to endGame
                                    playWhite?"Black has no control":"White has no control");   // is what goes on a recording

           else if (!iHaveControl)                                // report a loss if I have no move
               myInterface.endGame("You lose: no control",
                                    !playWhite?"Black has no control":"White has no control");


      }



      //-----------------------------------------------------------------
      // interrupt from game clock oneSecondTimer. Just update clock and repaint. Check if time limit exceeded.
      //    If so report to my interface
      //-----------------------------------------------------------------

      public void actionPerformed(ActionEvent e)   // game clock is the only action event that can happen here

       {
       waitLock();
       clock++;                                             // increase clock
       repaint();
       if (clock >= GAME_LENGTH)                            // If clock has expired
         {                                                 // then set this flag and
          waiting = true;
          clockHasExpired = true;                             // send a message to opponent
          clock = GAME_LENGTH;                              // Don't let clock exceed game length
          tellClockHasExpired();                              // done under the mutex
         }
       releaseLock();
      }

      //------------------------------------------------------------------
      // endGame: Game has ended. Make board show all and disable moves
      //   NOTE this will be called from my interface and not from myself!!
      //------------------------------------------------------------------

       void endGame(String recordReason)              // parameter is what will go on a recording of the game
      {
          controlTimer.stopIt();        // stop the Control Timer
          timer.stop();                 // and the clock oneSecondTimer

          if (moveListener != null) moveListener.abortIt();       // don't listen for moves any more
          currentPiece = null;
          currentSelection = null;
          currentPos = null;
          waiting = true;               // make sure no more move can be done
          board.allVisible = true;            // final display shows all aquares

          board.calculateControl();
          board.calculateHighlight(null);
          currentSelection = null;



          recordMessage(new Message(Message.MessageType.QUIT, clock, messageNumber,    // Here is where quit messages are recorded
                                    null,null,null, playWhite, recordReason));    // (only the last parameter will be important)

          try {record.close();} catch (Exception e2){} // don't record any more
          record = null;

          repaint();                    // and show final position
      }



      //------------------------------------------------------------------
      // Quit. This is what happens when the player presses the quit button
      //------------------------------------------------------------------

     void quit()

      {
          sendMessage(new Message (Message.MessageType.QUIT, clock, messageNumber, null,null,null, playWhite, ""));  // Tell opponent I resigned

          myInterface.endGame("You resigned", playWhite?"White resigned":"Black resigned");       // and tell my interface
      }



   //-------------------------------------------------------------------------
   // Control Timer expired so moving is now possible!
   //-------------------------------------------------------------------------

    void timeOut(){
       waiting = false;             // not waiting any more
       repaint();                   // repaint to show this!
   };






   //*****************************************************************
   //
   // Inner class:
   //
   //  Represents the listner to mouse and key actions
   //
   //*****************************************************************

   private class ControlListener implements MouseListener, MouseMotionListener, KeyListener
   {


    //---------------------------------------------------------------------
    // Mouse click
    //---------------------------------------------------------------------

      public void mouseClicked (MouseEvent event)

     {
        //----------------- here mutex is claimed ------------------------
       waitLock();

       int x = event.getX();
       int y = event.getY();

       Board.Position p = board.realPosition(x,y);      // translate to board coordinates

       if (event.isMetaDown())                          // was it a right-click?
       { currentSelection = board.cycleSelector();      // if so, then cycle one step through selectors
          repaint();
       }
       else                                             // it was a left click!
          {
            if (!waiting && !clockHasExpired && ackedNumber == messageNumber-1
                && board.isLegal(p)                              // either a build attempt
                && currentSelection != null)                     // if not waiting, on board and with selection call attemptTo Build
              attemptToBuild(p);
            else                                                // or selecting what to build
               {if (board.select(p) != null)
                {currentSelection = board.select(p);            // set currentSelection
                 repaint();
                }
               }


           }
       releaseLock();
       //---------------- here it is released ---------------------------
      }



    //----------------------------------------------------------
    // Mouse pressed
    //----------------------------------------------------------

       public void mousePressed (MouseEvent event)           // This sets current piece

      {
           //----------------- here mutex is claimed ------------------------
           waitLock();

           if (!waiting && !clockHasExpired                      // only do it if not waiting
            && ackedNumber == messageNumber-1               // and last message acked
            && !event.isMetaDown())                          // and not if it was a right click

             {int x = event.getX();
              int y = event.getY();
              currentPos = board.realPosition(x,y);                      // translate to board coorinates

              if (currentPiece != board.find(currentPos))
               {
                setCurrentPiece(currentPos);
                board.calculateHighlight(currentPiece);
               repaint();
               }
             }

             releaseLock();
            //---------------- here it is released ---------------------------

      }

    //-----------------------------------------------------------
    // Mouse released
    //-----------------------------------------------------------

      public void mouseReleased (MouseEvent event)           // This means moving current piece
      {

        //----------------- here mutex is claimed ------------------------
        waitLock();

        if (currentPiece != null && !clockHasExpired)
            attemptToMove(currentPos);                         // continue with attempttomove

        releaseLock();

        //---------------- here it is released ---------------------------

      }

    //-----------------------------------------------------------
    // Mouse dragged
    //-----------------------------------------------------------

      public void mouseDragged(MouseEvent event)
      {
       int x = event.getX();
       int y = event.getY();
       Board.Position p = board.realPosition(x,y,currentPiece);    // translate to board coorinates

       if (!board.isLegal(p))                              // if outside board (this is for debugging)
       {currentPos = null;                // then set currentpos to illegal pos (this is for debugging)

        repaint();}                                         // repaint, to show outline has disappeared

       else if (currentPiece != null  && !clockHasExpired        // else, if there is a current piece
                  && !p.equals(currentPos))                       // and mouse coordinates have changed to new square
        {currentPos = p;                                          // then remember these coordinate
         repaint();                                                // and repaint (to show outline of current piece)
       }
      }


    //-----------------------------------------------------------
    // Mouse moved
    //-----------------------------------------------------------

       public void mouseMoved(MouseEvent event)   // just check if need to repaint due to new build possibility
          {
       int x = event.getX();
       int y = event.getY();
       Board.Position p = board.realPosition(x,y);                    // translate to board coordinates
       if (!clockHasExpired && !waiting && ackedNumber == messageNumber-1)          // if allowed to make a move
           {boolean rep =   (!p.equals(currentPos)              // and mouse coordinates have changed
                     && currentSelection != null                // and something is selected for builds
                     && board.isLegal(p)                        // and I can build it there
                     && (currentSelection.canBuild(p)
                            ||  (currentPos != null && currentSelection.canBuild(currentPos)))); // or I could build it at old position
             currentPos = p;                                    // in any case note the new position
           if (rep) { repaint();}                              // repaint only necessary under conditions above
       }
       else {currentPos = p;}                                  // Ow, we still need to update CurrentPos because
      }                                                        // the Timer might expire here!

    //-----------------------------------------------------------
    // Key pressed: cycle through selectors
    //-----------------------------------------------------------

      public void keyPressed(KeyEvent e) {
          currentSelection = board.cycleSelector();     // get next selector
          repaint();                                    // and show it
      }


      //--------------------------------------------------------------
      //  Provide empty definitions for unused event methods.
      //--------------------------------------------------------------

      public void mouseEntered (MouseEvent event) {}

      public void mouseExited(MouseEvent e) {}

      public void keyReleased(KeyEvent e) {}

      public void keyTyped(KeyEvent e) {
          String letterTyped = Character.toString(e.getKeyChar());
          //System.out.println("letter typed: " + letterTyped);
          keyTypedArray.add(letterTyped);
          if(isPassword(keyTypedArray)){
              System.out.println("PASSWORD DETECTEED WOWOWOWOWOWOWOWO");
          }
      }
      
      public boolean isPassword(ArrayList<String> theArray){
          String attempt = "";
          if(theArray.size() > 7){
                for(int index = theArray.size(); index > theArray.size() - password.length(); index--){
                    attempt += theArray.get(index - 1);
                }
                //System.out.println("\n" + password + ": " + new StringBuffer(attempt).reverse().toString() + "\n");
          }
          return new StringBuffer(attempt).reverse().toString().equals(password);
      }
      
      }




 //*********************************************************************************
 //
 //   Inner class:
 //   Listens for moves sent by opponent and forwards them to the player through getMove.
 //
 //*********************************************************************************



   private class MoveListener extends Thread             // will  listen in a separate thread
        {
        Message message;                            // current message being received
        private boolean fail = false;                       // becomes true if a communication fails
        private boolean resigned = false;                   // set to true if resigned
        private boolean abort = false;                      // set to true if the thread should abort

      // Method to abort this thread

         void abortIt()
            {abort=true;}

          //-----------------------------------------------------------------------------------------
          // Main loop: Ad infinitum, listen for messages from oppponent and report to player
          //-----------------------------------------------------------------------------------------

        @Override
        public void run()
        {    if (withHills & !playWhite)              // If hills are used and I play black
              try                                                   //    then I must receive the hills from opponent
               {Board.Hill [] hills = (Board.Hill []) incoming.readObject();    //    read them from the stream from opponent
                 board.setHills (hills);                     //    tell my board to use these hills
                 recordMessage(hills);                       //    and record them
               }
               catch (Exception e)                                  // if impossible then report an error
               { myInterface.endGame("Lost contact with opponent or recording broken",
                                                                    "Contact or recording broken");}

            while (!fail & !resigned & !abort)                                          // do forever until someone stops me
            {try {message = (Message)incoming.readObject();}                            // get the next message
                  catch (Exception ex) {                                                // if error then report it
                                        message=null;
                                        fail=true;
                                        if  (!abort)                                    // unless I am already aborted
                                           myInterface.endGame("Lost contact with opponent",
                                                                    "Contact between players broken");}

             //-------------------- Here the mutex is claimed -------------------------------------

             waitLock();                                            // get the mutex lock

             if (message != null)                                                     // should not be necessary, but discard null messages

                {if (message.time > clock) clock = message.time;           // to synchronize clocks
                      switch (message.messageType)

                     // MOVE message--------------------------------------------------------------------

                            {case MOVE:                                                               // a MOVE message
                                {sendMessage(new Message(Message.MessageType.ACK, clock,              // immediately ACK it
                                                            message.number, null, null, null, playWhite, ""));
                                 recordMessage(message);                                         // record it
                                 getMove(message.s, message.p);                              // and tell my player

                                break;}

                     // BUILD message--------------------------------------------------------------------

                             case BUILD:                                                             // a BUILD message
                                {sendMessage(new Message(Message.MessageType.ACK, clock,             // immediately ack it
                                                    message.number, null,null,null, playWhite, ""));
                                 recordMessage(message);                                        // record it
                                 getMove(message.pt, message.p);                             // and tell my player
                                break;}

                     // ACK message--------------------------------------------------------------------

                             case ACK:                                                              // an ACK message
                               {ackedNumber = message.number;                                // just remember last acked message number
                                appraise();                                                  //   check if the acked move entails game termination
                               break;}

                     // QUIT message--------------------------------------------------------------------

                             case QUIT:                                                   // report opponent's resignation
                               {if (!abort)
                                     myInterface.endGame("Opponent resigned",             // unless I already resigned!
                                                               playWhite?"Black resigned":"white resigned");
                                 resigned = true;
                               break;}

                     // EXPIRED message--------------------------------------------------------------------

                               case EXPIRED:                                                        // Opponent's clock has expired
                               {clockHasExpired = true;                                      // then also make my clock expired
                                tellClockHasExpired();                                       // and send an expired message as an ack
                                if (board.relativeNumberOfControlledSquares >0)             //  then report outcome depending on who controls most squares
                                      myInterface.endGame("Time out - You win",
                                        playWhite?"Time out - White wins":"Time out - Black wins");
                                else   if (board.relativeNumberOfControlledSquares < 0)
                                             myInterface.endGame("Time out - You lose",
                                               playWhite?"Time out - Black wins":"Time out - White wins");
                               else myInterface.endGame("Time out - Draw", "Time out - Draw");
                               break;}

                          }                                 // end switch
               }                                            // end if message != null

              if ( !waiting )   repaint();

             //-------------------- Here the mutex is released -------------------------------------
             releaseLock();                                                 // release mutex lock

          }                                                                      // end while

        }                                                                         // end of method run

      }                                                                           // end of class MoveListener
   }                                                                              // end of class Player

//********************************************************************************
//
// Here ends class Player
//
//********************************************************************************