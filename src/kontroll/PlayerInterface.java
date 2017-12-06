/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

 class PlayerInterface implements ActionListener

{
   private StartControl startControl;                                    // Who started me and to whom I report termination

   private JFrame             myFrame;                                   // my Frame
   private final JPanel       tops           = new JPanel();             // top panel will hold the quit button and player ids
   private final JPanel       quitPanel      = new JPanel();             // part of topside to hold quit button
   private final JPanel       bottoms        = new JPanel();             // bottom panel will hold the control timer
   private final JButton      quitButton     = new JButton("Quit");      // the quit button

   private Player       player;                                    // my player

                                                        // various temp variables.
   private boolean running = true;                                 //  true if player is running.
   private boolean noids;                                          //  true if neither player has a nick

   private String whiteNick, blackNick;                            //  nick of white and of black resp.

    //----------------------------------------------------------------------------------------------------------------------
    // Constructor does most of the work to set things up
    //----------------------------------------------------------------------------------------------------------------------

   PlayerInterface(Player       player,         // Who will actually play the game
                   ControlTimer timer,          // and its control timer. These are the ones I should display properly.
                   StartControl startControl,   // Parent to which I report termination
                   String       nick,           // nick my player uses
                   String       opponentNick)   // nick the opponent uses
    {

       this.player = player;                    // begin by remembering parameters
       this.startControl = startControl;

       myFrame = new JFrame("Playing the game of Control" +
                                       (player.isLite ? "ite" : ""));


       myFrame.getContentPane().setLayout(new BorderLayout());       // with borderlayout manager
       myFrame.getContentPane().setBackground(Board.BACKGROUNDCOLOR);// and proper background

       myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);       // Ow process continues when window closes

       bottoms.setLayout(new BoxLayout(bottoms,  BoxLayout.X_AXIS));     // Bottom region reserved for the timer
       bottoms.add(Box.createRigidArea(new Dimension(0,ControlTimer.BAR_HEIGHT)));   // (ow the layout manager will reserve no space)
       bottoms.add(timer);

       myFrame.getContentPane().add (player);                           // central region for the board
       myFrame.getContentPane().add(bottoms,BorderLayout.SOUTH);

       quitButton.setBackground(Color.red);                             // put the quit button topside
       quitPanel.add(quitButton);
       quitPanel.setBackground(Board.BACKGROUNDCOLOR);
       tops.setLayout(new BoxLayout(tops,BoxLayout.Y_AXIS));
       tops.add(quitPanel);
                                                                        // topside also get to print player nicks
                                                                        // unless neither player has a nick
       tops.add(Box.createRigidArea(new Dimension(0, 20)));

       if (player.playWhite)                                            // set whitenick and blacknick
         {whiteNick = nick; blackNick = opponentNick;}
       else
         {whiteNick = opponentNick; blackNick = nick;}

       noids =  ((whiteNick.equals(""))&(blackNick.equals("")));        // set noids true iff neither player has a nick

       if (whiteNick.equals("")) whiteNick="White";                     // default nicks for white and black
       if (blackNick.equals("")) blackNick="Black";

       if (!noids) tops.add(buildIdPanel());     // display the nicks in idPanel unless both empty

       quitButton.addActionListener(this);                              // I listen for quit button myself
       myFrame.getContentPane().add(tops,BorderLayout.NORTH);
       tops.setBackground(Board.BACKGROUNDCOLOR);

       myFrame.pack();
       myFrame.setVisible(true);
       focus();

       player.setInterface(this);        // tell my player who I am, so it can report game termination properly

    }

    //------------------------------------------------------------------------
    // Strangely, this is necessary in order to let the player listen for key actions
    //------------------------------------------------------------------------

    private void focus()
    { player.requestFocus();
    }

     //------------------------------------------------------------------------
    // Build a panel displaying player nicks and add it to tops
    //------------------------------------------------------------------------

    private JPanel buildIdPanel()
    {
            JPanel idPanel = new JPanel();
            idPanel.setLayout(new BoxLayout(idPanel, BoxLayout.X_AXIS));

            JLabel whiteLabel = new JLabel(whiteNick);
            whiteLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
            whiteLabel.setForeground(Color.white);
            whiteLabel.setBackground(Board.BACKGROUNDCOLOR);

            JLabel vsLabel = new JLabel("     vs      ");
            vsLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            vsLabel.setForeground(Color.orange);
            vsLabel.setBackground(Board.BACKGROUNDCOLOR);

            JLabel blackLabel = new JLabel(blackNick);
            blackLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
            blackLabel.setForeground(Color.black);
            blackLabel.setBackground(Board.BACKGROUNDCOLOR);

            idPanel.add(whiteLabel);
            idPanel.add(vsLabel);
            idPanel.add(blackLabel);
            idPanel.setBackground(Board.BACKGROUNDCOLOR);

            return idPanel;

    }
    //-----------------------------------------------------------------------
    // When game has ended the display needs change
    //-----------------------------------------------------------------------

    void endGame(String reason,               // reason to present to the user
                       String recordReason)         // reason to record on file if game is recorded

   { //  Player.releaseLock();
       myFrame.getContentPane().remove(tops);       // perhaps unnecessary since borderlayout is used in myFrame
       tops.removeAll();                            //  but the topside area needs to be redone
       tops.setLayout(new BoxLayout(tops,  BoxLayout.Y_AXIS));

       JLabel reasonLabel = new JLabel(reason, SwingConstants.CENTER);     // message to user upon termination
       reasonLabel.setBackground(Color.orange);
       reasonLabel.setFont(new Font("SansSerif", Font.BOLD, 24));


       tops.add(Box.createRigidArea(new Dimension(0,20)));   // display this message and the quit button topside
       tops.add(quitPanel);
       tops.add(Box.createRigidArea(new Dimension(0,20)));
       tops.add(reasonLabel);
       tops.add(Box.createRigidArea(new Dimension(0,20)));

       if (!noids) tops.add(buildIdPanel());        // display player nicks

       myFrame.getContentPane().add(tops,BorderLayout.NORTH);   // put on display

       player.endGame(recordReason);                // tell my player to clean up

       myFrame.pack();                              // show it
       myFrame.repaint();
       running = false;
   }

   //---------------------------------------------------------------------------------
   // Termination. get rid of the frame and tell my parent
   //---------------------------------------------------------------------------------

     private  void quit()
       {   myFrame.dispose();
           startControl.playAgain();
       }


    //--------------------------------------------------------------------------------
    // I am listening for actions myself
    //---------------------------------------------------------------------------------


    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == quitButton)    // if quit button is pressed
        {if (running) player.quit();        // if player is running then player has to quit
         else quit();}                      // ow I have to quit
    }

}    //--------------------------- end of player interface -------------------------------------------------