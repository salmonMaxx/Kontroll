/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;

  //***************************************************************
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


  //
  // "PlayBack" is akin to "PlayerInterface": set up frame for playback
  //
  //****************************************************************


   class PlayBack extends JPanel implements ActionListener

  {
            JFrame myFrame;                                         // my Frame
    private final JPanel tops   = new JPanel();                           // for buttons etc

    private final JButton quitButton = new JButton("Quit");               // the quit button
    private final JButton pauseButton = new JButton("Pause");
    private final JButton playButton = new JButton("Play");
    private final JButton ffwButton = new JButton("Fast Fwd");

    private final StartControl startControl;                              // points to the object who started me
    private Replayer replayer;                                      // points to the object I'll use to replay the game

    RecordedGame rg;                                        // holds game parameters (NOT the entire game!)

    private String whiteNick, blackNick;                            // nicknames to display


    //--------------------------------------------------------
    // The constructor does most of the work in this class, to set things up
    //--------------------------------------------------------


    PlayBack(StartControl startControl,                 //  who started me
                    ObjectInputStream playback)         // from where I get the game
    {
       this.startControl = startControl;                 // remember parameter

       myFrame = new JFrame("Replaying a game of Control");          // Frame for one player
       myFrame.getContentPane().setLayout(new BorderLayout());       // with borderlayout manager
       myFrame.getContentPane().setBackground(Board.BACKGROUNDCOLOR);// and appropriate background

       boolean failed = false;                          // becomes true when I cannot read from the stream

       try
         {rg = (RecordedGame)playback.readObject();}    // get game parameters
          catch (Exception e)
            {JOptionPane.showMessageDialog(this, "Not a recorded game\n" +      // if impossible tell user and exit
                                               "The reason is:\n"+
                                               e.toString(),
                                               "Recording ended ", JOptionPane.INFORMATION_MESSAGE);
             failed = true;
             }

       if (!failed)                                                // if game parameters are OK:
       {
           myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);      // Ow process continues when user closes window

           replayer = new Replayer (startControl, playback, this);      // get someone to replay a game

           myFrame.getContentPane().add (replayer);                     // and put it on display in the center

           JPanel quitPanel = new JPanel();                             // panel for the buttons
           quitButton.setBackground(Color.red);
           pauseButton.setBackground(Color.orange);
           playButton.setBackground(Color.LIGHT_GRAY);
           ffwButton.setBackground(Color.green);
           quitPanel.add(quitButton);
           quitPanel.add(Box.createRigidArea(new Dimension(20, 0)));
           quitPanel.add(pauseButton);
           quitPanel.add(Box.createRigidArea(new Dimension(20, 0)));
           quitPanel.add(playButton);
           quitPanel.add(Box.createRigidArea(new Dimension(20, 0)));
           quitPanel.add(ffwButton);
           quitPanel.setBackground(Board.BACKGROUNDCOLOR);

           tops.setLayout(new BoxLayout(tops,BoxLayout.Y_AXIS));        // top area
           tops.add(quitPanel);                                         //  holds the buttons, and...
           tops.add(Box.createRigidArea(new Dimension(0, 20)));

           boolean noids;                                               //   This is true if nicks not avialble

           if ((rg.whiteNick.equals(""))&(rg.blackNick.equals(""))) noids=true; else noids=false;  // set noids and nicks for white and black
           if (rg.whiteNick.equals("")) whiteNick="White"; else whiteNick = rg.whiteNick;
           if (rg.blackNick.equals("")) blackNick="Black"; else blackNick = rg.blackNick;

           if (!noids)                                                  // if at least one nick available
           {                                                            // tops will additionally display nicks
                JPanel idPanel = new JPanel();
                idPanel.setLayout(new BoxLayout(idPanel, BoxLayout.X_AXIS));

                JLabel whiteLabel = new JLabel(whiteNick);
                whiteLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
                whiteLabel.setForeground(Color.white);
                whiteLabel.setBackground(Board.BACKGROUNDCOLOR);

                JLabel blackLabel = new JLabel(blackNick);
                blackLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
                blackLabel.setForeground(Color.black);
                blackLabel.setBackground(Board.BACKGROUNDCOLOR);

                JLabel vsLabel = new JLabel("     vs      ");
                vsLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                vsLabel.setForeground(Color.orange);
                blackLabel.setBackground(Board.BACKGROUNDCOLOR);

                idPanel.add(whiteLabel);
                idPanel.add(vsLabel);
                idPanel.add(blackLabel);
                idPanel.setBackground(Board.BACKGROUNDCOLOR);

                tops.add(idPanel);
           } // end of (!noids)

           tops.setBackground(Board.BACKGROUNDCOLOR);
           myFrame.getContentPane().add(tops,BorderLayout.NORTH);

           quitButton.addActionListener(this);                              // I listen for buttons myself
           pauseButton.addActionListener(this);
           playButton.addActionListener(this);
           ffwButton.addActionListener(this);

           myFrame.pack();
           myFrame.setVisible(true);
           replayer.replay();                  // starts the replayer in this thread

       }                                // here ends !failed

       else startControl.playAgain();  // if failed to read game parameters, let the guy who called me know
    }                                  // end of constructor



   //------------------------------------------------------------------------
   // Here is where the rePlayer reports a recording ended
   //------------------------------------------------------------------------

     void endGame(String reason)                         // parameter is reason to be displayed
     {
       JLabel reasonLabel = new JLabel(reason);                 // put it in a label
       reasonLabel.setBackground(Color.orange);
       reasonLabel.setFont(new Font("SansSerif", Font.BOLD, 24));

       myFrame.getContentPane().remove(tops);                // perhaps not necessary because myFrame uses borderlayout

       tops.add(Box.createRigidArea(new Dimension(0,20)));
       tops.add(reasonLabel);                                // add the reason (tops already holds the buttons)
       tops.add(Box.createRigidArea(new Dimension(0,20)));

       myFrame.getContentPane().add(tops,BorderLayout.NORTH);

       myFrame.pack();
       myFrame.repaint();

     }

  //----------------------------------------------------------------------------
  // Here we come when replayer user presses a button
  //----------------------------------------------------------------------------

       private void quit()
       {   myFrame.setVisible(false);           // Kill this frame
           myFrame.dispose();

          startControl.playAgain();             // and tell the guy who started me
       }



  //------------------------------------------------------------------------

       private void pause()
       {    replayer.paused = true;             // remember we are paused
            replayer.ffw = false;
            pauseButton.setBackground(Color.LIGHT_GRAY); // change button colors
            playButton.setBackground(Color.green);
            ffwButton.setBackground(Color.green);

            repaint();
       }

  //------------------------------------------------------------------------
       private void play()
       {    replayer.paused = false;            // remember ordinary replay
            replayer.ffw = false;
            pauseButton.setBackground(Color.orange);      // change button colors
            playButton.setBackground(Color.LIGHT_GRAY);
            ffwButton.setBackground(Color.green);
            repaint();
       }

  //------------------------------------------------------------------------
       private void ffw()
       {    replayer.paused = false;            // remember fast forward replay
            replayer.ffw = true;
            pauseButton.setBackground(Color.orange);     // change button colors
            playButton.setBackground(Color.green);
            ffwButton.setBackground(Color.LIGHT_GRAY);
            repaint();

       }
  // ----------------------  button pressed: -------------------------------

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == quitButton)    // if quit button is pressed
          quit();
        if (e.getSource() == pauseButton)    // if pause button is pressed
          pause();
        if (e.getSource() == playButton)    // if play button is pressed
          play();
         if (e.getSource() == ffwButton)    // if ffw button is pressed
          ffw();
    }

   }        // end class PlayBack