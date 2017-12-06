package kontroll;
        
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;


//*************************************************************************
//
// Start Control
//
//  Handles all user interaction to start a game of control
//
//*************************************************************************


  class StartControl implements ActionListener
  {
    // Graphic common variables

    final private JFrame myFrame         = new JFrame("The game of Control v" + Control.VERSION_NUMBER);

    final private Container thePane      = myFrame.getContentPane();

    final private JButton connectButton  = new JButton("Connect");     // The Buttons on the main screen
    final private JButton quitButton     = new JButton("Quit");
    final private JButton listenButton   = new JButton("Listen");
    final private JButton abortButton    = new JButton("Abort");
    final private JButton playButton     = new JButton("Play");
    final private JButton recordButton   = new JButton("Record");
    final private JButton playbackButton = new JButton("Playback");
    final private JButton optionsButton  = new JButton("Options");
    final private JButton defaultsButton = new JButton("Defaults");
    final private JButton returnButton   = new JButton("Return");

    private JTextField adrField;                                // input of opponent host name to connect to
    private JTextField nickField;                               // input of user nick

       // Button explanatory labels

    final private JLabel connectLabel   = new JLabel("   Press Connect to connect to another computer", SwingConstants.RIGHT);
    final private JLabel quitLabel      = new JLabel("   Press Quit to exit this program", SwingConstants.RIGHT);
    final private JLabel listenLabel    = new JLabel("   Press Listen to listen for connections", SwingConstants.RIGHT);
    final private JLabel abortLabel     = new JLabel("   Press Abort to return to main screen", SwingConstants.RIGHT);
    final private JLabel playLabel      = new JLabel("   Press Play to start the game", SwingConstants.RIGHT);
    final private JLabel recordLabel    = new JLabel("   Press Record to choose a file for recording the game", SwingConstants.RIGHT);
    final private JLabel playbackLabel  = new JLabel("   Press Playback to playback a recorded game", SwingConstants.RIGHT);
    final private JLabel optionsLabel   = new JLabel("   Press Options to change options", SwingConstants.RIGHT);
    final private JLabel returnLabel    = new JLabel("   Set options and return to main menu", SwingConstants.RIGHT);
    final private JLabel defaultsLabel  = new JLabel("   Reset to default values", SwingConstants.RIGHT);
    private JLabel ipLabel;


    final private JPanel connectPanel   = new JPanel();   // panels containing buttons and their labels
    final private JPanel quitPanel      = new JPanel();
    final private JPanel listenPanel    = new JPanel();
    final private JPanel abortPanel     = new JPanel();
    final private JPanel playPanel      = new JPanel();
    final private JPanel recordPanel    = new JPanel();
    final private JPanel playbackPanel  = new JPanel();
    final private JPanel optionsPanel   = new JPanel();
    final private JPanel returnPanel    = new JPanel();
    final private JPanel defaultsPanel  = new JPanel();

    // The following is all concerned with the options screen

    final private JRadioButton compactButton = new JRadioButton("Compact");     // graphic option radio buttons
    final private JRadioButton largeButton   = new JRadioButton ("Large");
    final private JRadioButton hugeButton    = new JRadioButton("Huge");

    final private JRadioButton fastButton   = new JRadioButton("Fast");         // speed option radio buttons
    final private JRadioButton normalButton = new JRadioButton ("Normal");
    final private JRadioButton slowButton   = new JRadioButton("Slow");

    final private JRadioButton squareButton = new JRadioButton("Square");       // Area shape radio buttons
    final private JRadioButton hexButton    = new JRadioButton("Hexagonal");
    final private JRadioButton circleButton = new JRadioButton("Circle");

    final private JRadioButton boardCircleButton = new JRadioButton("Round");   // board shape radio buttons
    final private JRadioButton boardSquareButton = new JRadioButton("Rectangular");

    final private JRadioButton liteButton    = new JRadioButton("Controlite (no heavy pieces)");  // Game type radio buttons
    final private JRadioButton controlButton = new JRadioButton("Control (all pieces)");

    final private JRadioButton noHillsButton = new JRadioButton("Flat board");         // Board topography radio buttons
    final private JRadioButton hillsButton   = new JRadioButton("Hills on the board");

    final private int[] sizeChoices       = {8,10,12,14,16};                     // allowed choices of width and height and pebbles

    final private String[] widthChoisesStr   = {"Width:  8","Width: 10","Width: 12","Width: 14","Width: 16"};
    final private String[] heightChoisesStr  = {"Height:  8","Height: 10","Height: 12","Height: 14","Height: 16"};
    final private String[] pebblesChoisesStr = {"Pebbles:  8","Pebbles: 10","Pebbles: 12","Pebbles: 14","Pebbles: 16"};


    final private int[] lengthChoices       = {5,10,15,20,30};                   // allowed choices of max game length
    final private String[] lengthChoicesStr = {" 5 min.","10 min.","15 min.","20 min.","30 min."};

    final private JComboBox widthBox   = new JComboBox(widthChoisesStr);
    final private JComboBox heightBox  = new JComboBox(heightChoisesStr);
    final private JComboBox pebblesBox = new JComboBox(pebblesChoisesStr);
    final private JComboBox lengthBox  = new JComboBox(lengthChoicesStr);



    // ----------------- Texts to inform the user ----------------------------------------

    final private String welcomeStatus         = "         Welcome to the game of Control!\n"+
                             "                          (c) Joachim Parrow 2003,2006,2010\n\n"+
                            "You are currently not connected to another Player. "+
                            "To attempt to connect, press the Connect Button above ("+
                            "you will need to tell me the IP of the computer to connect to). " +
                            "Alternatively, to listen in case someone tries to connect to you " +
                            "press the Listen Button.";

    final private String listenStatus   = "You are currently listening for someone to connect to you.\n\n"+
                            "When this happens I shall notify you and ask if you want to accept the connection. "+
                            "Until then there is nothing to do but wait. "+
                            "Pressing Abort above will mean that you give up listening.";

    final private String connectingStatus = "Please enter either the IP name or the number "+
                              "you want to connect to in the field above (for example 'myhost.edu.com' or '137.0.1.15'. Pressing <return> "+
                              "in this field will make me start connecting to it. "+
                              "Pressing Abort returns to main screen.";

    final private String acceptingstatus = "An opponent at the address given above is ready to play.\n\n"+
                             "Accept the challange by pressing the Play button. "+
                             "If you do not wish to play this opponent press Abort "+
                             "which returns you to the main menu";

    final private String connectedStatus = "You have successfully connected to a server at the address given above. "+
                             "Now you must wait for a user at that address to accept to play with you. "+
                             "When that happens the game will start. "+
                             "Until then you must wait. If you get tired Abort takes you back to the main menu "+
                             "and the connection will be lost.";

    final private String optionsStatus    = "Here you can set options for your game. A nick is optional and will only be used "+
                             "to tell your opponent who you are."+
                             "The other options must be set in the same way by both players.\n\n"+
                             "Game type: Choose controlite (only Pebbles and Squares) or Control (all heavy pieces).\n\n"+
                             "Hills: Hills are randomly distributed; a piece on a hill exerts control over a larger area. "+
                             "Choose to play with hills on the board or not.\n\n"+
                             "Board shape is the shape of the board, while area shape is the shape of the area a piece controls.\n\n"+
                             "The relative board dimensions adjust the size of the board in relation to the size of pieces, while the choice of graphics "+
                             "determines how large it appears on the screen.\n\n"+
                             "Game speed determines how long a player has to wait between moves, and length is the duration of a game.\n\n"+
                             "Finally, Pebbles is the number of Pebbles available to each player (the initial Pebble plus the additional Pebbles that may be built).";



    // Communication  variables

    private Socket              outSocket      = null;          // outgoing communications socket to other player
    private ServerSocket        inSocket       = null;          // incoming communications server from other player
    private Socket              connection     = null;          // incoming communications socket
    private ObjectOutputStream  outgoing;                       // outgoing stream to opponent
    private ObjectInputStream   incoming;                       // incoming stream from opponent

    private GetConnection       getConnection = null;          // threads listening for connections
    private GetAccept           getAccept = null;


    private ObjectOutputStream  record;                         // for recording games
    private ObjectInputStream   playback;                       // for recorded games

    final private static int    PORT_NUMBER =   8888;           // port number that this game uses

    // Game logic  variables

    private boolean playWhite;                              // set if I (randomly) got to play white

    private String  nick =          "";                     // your nickname
    private String  opponentNick =  "";                     // opponent's nickname
    private String  lastAddress =   "";                     // address typed in when connecting

    private int graphics, shape, boardShape, speed, width, height, pebbles, length;      // game params
    private boolean isLite, withHills;

    private int widthIdx, heightIdx, pebblesIdx, lengthIdx; // idx to the options

    // default options

    final  private static int DEFAULT_GRAPHICS = 1, DEFAULT_SHAPE = 3, DEFAULT_BOARDSHAPE = 1,
                              DEFAULT_SPEED = 1, DEFAULT_WIDTH = 8, DEFAULT_HEIGHT = 8,
                              DEFAULT_PEBBLES = 8, DEFAULT_LENGTH = 10;
    final  private static boolean DEFAULT_ISLITE = false;
    final  private static boolean DEFAULT_WITHHILLS = true;

     // test parameters

     private boolean testServer;            // true if testing and I should set up a server
     private boolean testClient;            // true if testing and I should set up a client

    //-------------------------------------------------------------------------------
    // Constructor just initialises first frame
    //-------------------------------------------------------------------------------



     StartControl(boolean testServer, boolean testClient)
    {
       quitButton.setBackground    (Color.red);        // set button colors need to redifne laf to work on a Mac :(
       connectButton.setBackground (Color.green);
       listenButton.setBackground  (Color.green);
       playButton.setBackground    (Color.green);
       abortButton.setBackground   (Color.orange);
       recordButton.setBackground  (Color.yellow);
       playbackButton.setBackground(Color.yellow);
       optionsButton.setBackground (Color.yellow);
       returnButton.setBackground  (Color.green);
       defaultsButton.setBackground(Color.orange);

       Color buttonColor = Board.BACKGROUNDCOLOR.brighter();  // color for buttons in options panel

       controlButton.setBackground (buttonColor);
       liteButton.setBackground    (buttonColor);

       hillsButton.setBackground   (buttonColor);
       noHillsButton.setBackground (buttonColor);

       compactButton.setBackground (buttonColor);
       largeButton.setBackground   (buttonColor);
       hugeButton.setBackground    (buttonColor);

       fastButton.setBackground    (buttonColor);
       normalButton.setBackground  (buttonColor);
       slowButton.setBackground    (buttonColor);

       squareButton.setBackground (buttonColor);
       hexButton.setBackground    (buttonColor);
       circleButton.setBackground (buttonColor);

       boardCircleButton.setBackground (buttonColor);
       boardSquareButton.setBackground (buttonColor);


       widthBox.setBackground   (buttonColor);
       heightBox.setBackground  (buttonColor);
       pebblesBox.setBackground (buttonColor);
       lengthBox.setBackground  (buttonColor);

       quitPanel.add(quitButton);                   // build button panels
       connectPanel.add(connectButton);
       listenPanel.add(listenButton);
       abortPanel.add(abortButton);
       playPanel.add(playButton);
       recordPanel.add(recordButton);
       playbackPanel.add(playbackButton);
       optionsPanel.add(optionsButton);
       returnPanel.add(returnButton);
       defaultsPanel.add(defaultsButton);

       quitPanel.setBackground(Board.BACKGROUNDCOLOR);  // set panel backgrounds
       listenPanel.setBackground(Board.BACKGROUNDCOLOR);
       connectPanel.setBackground(Board.BACKGROUNDCOLOR);
       abortPanel.setBackground(Board.BACKGROUNDCOLOR);
       playPanel.setBackground(Board.BACKGROUNDCOLOR);
       recordPanel.setBackground(Board.BACKGROUNDCOLOR);
       playbackPanel.setBackground(Board.BACKGROUNDCOLOR);
       optionsPanel.setBackground(Board.BACKGROUNDCOLOR);
       returnPanel.setBackground(Board.BACKGROUNDCOLOR);
       defaultsPanel.setBackground(Board.BACKGROUNDCOLOR);

       quitButton.addActionListener(this);              // add me as listener for all buttons
       connectButton.addActionListener(this);
       listenButton.addActionListener(this);
       abortButton.addActionListener(this);
       playButton.addActionListener(this);
       recordButton.addActionListener(this);
       playbackButton.addActionListener(this);
       optionsButton.addActionListener(this);
       returnButton.addActionListener(this);
       defaultsButton.addActionListener(this);

       graphics   = DEFAULT_GRAPHICS;                 // set params to defaults
       speed      = DEFAULT_SPEED;
       length     = DEFAULT_LENGTH;
       height     = DEFAULT_HEIGHT;
       width      = DEFAULT_WIDTH;
       pebbles    = DEFAULT_PEBBLES;
       shape      = DEFAULT_SHAPE;
       isLite     = DEFAULT_ISLITE;
       boardShape = DEFAULT_BOARDSHAPE;
       withHills  = DEFAULT_WITHHILLS;

       heightIdx  = 0;                           // default idx in param choices must also be set
       widthIdx   = 0;
       pebblesIdx = 0;
       lengthIdx  = 1;

       myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // Ow process continues to run when window closes


       try                              // try to get previously saved game parameters from the file options<V>.controloptions
         {
           FileInputStream optionsFile = new FileInputStream("options"+Control.VERSION_NUMBER+".controloptions");
           ObjectInputStream OS = new ObjectInputStream(optionsFile);

           nick         = (String)OS.readObject();
           speed        = OS.readInt();
           graphics     = OS.readInt();
           shape        = OS.readInt();
           boardShape   = OS.readInt();
           widthIdx     = OS.readInt();
           heightIdx    = OS.readInt();
           pebblesIdx   = OS.readInt();
           lengthIdx    = OS.readInt();
           width        = OS.readInt();
           height       = OS.readInt();
           pebbles      = OS.readInt();
           length       = OS.readInt();
           lastAddress  = (String)OS.readObject();
           isLite       = OS.readBoolean();
           withHills    = OS.readBoolean();

           OS.close();
         }
       catch (Exception e) {}      // If it doesn't work then just ignore

      //  Calculate my IP number

      String myIP;
      try
        {
          myIP = InetAddress.getLocalHost().getHostAddress();
      }
      catch (IOException e) {myIP = null;}

      if (myIP != null)
          ipLabel = new JLabel("    your IP number is " + myIP, SwingConstants.RIGHT);


      this.testServer = testServer;
      this.testClient = testClient;

       welcome();                       // go to main screen
    }

    //-------------------------------------------------------------------------------
    // Main screen
    //-------------------------------------------------------------------------------

    private void welcome ()
    {

     thePane.removeAll();                       // clear frame
     thePane.setLayout(new BorderLayout());


     JPanel statusPanel    = new JPanel();      // two new panels
     JPanel buttonsPanel   = new JPanel();

     buttonsPanel.setLayout(new GridLayout(7,2,20,20));   // for buttons and their explanatory labels

     TextArea statusArea = new TextArea(welcomeStatus,7,40,TextArea.SCROLLBARS_VERTICAL_ONLY);  // intro message

       buttonsPanel.add(connectLabel);          // build the buttons panel
       buttonsPanel.add(connectPanel);
       buttonsPanel.add(listenLabel);
       buttonsPanel.add(listenPanel);
       buttonsPanel.add(recordLabel);
       buttonsPanel.add(recordPanel);
       buttonsPanel.add(playbackLabel);
       buttonsPanel.add(playbackPanel);
       buttonsPanel.add(optionsLabel);
       buttonsPanel.add(optionsPanel);
       buttonsPanel.add(quitLabel);
       buttonsPanel.add(quitPanel);


       buttonsPanel.add(Box.createRigidArea(new Dimension(0,20)));

       statusArea.setEditable(false);           // and the status message
       statusPanel.add(statusArea);

       thePane.add(buttonsPanel);               // build the frame
       thePane.add(statusPanel,BorderLayout.SOUTH);
       thePane.add(Box.createRigidArea(new Dimension(0, 40)),BorderLayout.NORTH);
       thePane.add(Box.createRigidArea(new Dimension(50, 0)), BorderLayout.WEST);

       thePane.setBackground(Board.BACKGROUNDCOLOR);  // make sure background is right

       buttonsPanel.setBackground(Board.BACKGROUNDCOLOR);
       statusPanel.setBackground(Board.BACKGROUNDCOLOR);

       myFrame.pack();                          // display the frame
       myFrame.setVisible(true);

       if (testServer) startListening();
       if (testClient) tryConnecting();
    }

  //-------------------------------------------------------------------------------
  // Set game params (aka options)
  //    We come here when user presses "options"
  //-------------------------------------------------------------------------------

    private void options()
    {
     thePane.removeAll();                       // clear frame
     thePane.setLayout(new GridBagLayout());

     Color optionGroupColor = Board.BACKGROUNDCOLOR;    // background for the options group

     JPanel nickPane = new JPanel();                 // the panel where nick should be input
     JLabel nickLabel = new JLabel("Your nickname (optional)");   // its label
     nickField = new JTextField(nick,10);            // and text input field
     nickField.addActionListener(this);
     nickPane.setLayout(new BoxLayout(nickPane, BoxLayout.X_AXIS));  // build this panel
     nickPane.add(Box.createRigidArea(new Dimension(50,0)));
     nickPane.add(nickLabel);
     nickPane.add(Box.createRigidArea(new Dimension(50,0)));
     nickPane.add(nickField);
     nickPane.add(Box.createRigidArea(new Dimension(50,0)));
     nickPane.setBackground(Board.BACKGROUNDCOLOR);


     ButtonGroup typeGroup = new ButtonGroup();     // Group of buttons for game type
     if (isLite) liteButton.setSelected(true);
       else controlButton.setSelected(true);
     typeGroup.add(liteButton);
     typeGroup.add(controlButton);
     JPanel typePanel = new JPanel();
     typePanel.add(liteButton);
     typePanel.add(Box.createRigidArea(new Dimension(30,0)));
     typePanel.add(controlButton);
     typePanel.setBackground(optionGroupColor);
     Border typeBorder = new TitledBorder(new BevelBorder(BevelBorder.RAISED), "Game type", TitledBorder.ABOVE_TOP, TitledBorder.LEFT);
     typePanel.setBorder( typeBorder);

     ButtonGroup hillGroup = new ButtonGroup();     // Group of buttons for board topography
     if (withHills) hillsButton.setSelected(true);
       else noHillsButton.setSelected(true);
     hillGroup.add(hillsButton);
     hillGroup.add(noHillsButton);
     JPanel hillsPanel = new JPanel();
     hillsPanel.add(hillsButton);
     hillsPanel.add(Box.createRigidArea(new Dimension(30,0)));
     hillsPanel.add(noHillsButton);
     hillsPanel.setBackground(optionGroupColor);
     Border hillsBorder = new TitledBorder(new BevelBorder(BevelBorder.RAISED), "Board topography", TitledBorder.ABOVE_TOP, TitledBorder.LEFT);
     hillsPanel.setBorder(hillsBorder);

     ButtonGroup graphicGroup = new ButtonGroup();        // group of radio buttons for graphics
     if (graphics==1) compactButton.setSelected(true);
     else if (graphics==2) largeButton.setSelected(true);
     else hugeButton.setSelected(true);
     graphicGroup.add(compactButton);
     graphicGroup.add(largeButton);
     graphicGroup.add(hugeButton);
     JPanel graphicPanel = new JPanel();
     graphicPanel.add(compactButton);
     graphicPanel.add(Box.createRigidArea(new Dimension(30,0)));
     graphicPanel.add(largeButton);
     graphicPanel.add(Box.createRigidArea(new Dimension(30,0)));
     graphicPanel.add(hugeButton);
     graphicPanel.setBackground(optionGroupColor);
     Border graphicBorder = new TitledBorder(new BevelBorder(BevelBorder.RAISED), "Graphics", TitledBorder.ABOVE_TOP, TitledBorder.LEFT);
     graphicPanel.setBorder(graphicBorder);

     ButtonGroup boardShapeGroup = new ButtonGroup();        // group of radio buttons for board shape
     boardShapeGroup.add(boardSquareButton);
     boardShapeGroup.add(boardCircleButton);
     if (boardShape==1) boardSquareButton.setSelected(true);
     else if (boardShape==2) boardCircleButton.setSelected(true);
     JPanel boardShapePanel = new JPanel();
     boardShapePanel.add(boardSquareButton);
     boardShapePanel.add(Box.createRigidArea(new Dimension(30,0)));
     boardShapePanel.add(boardCircleButton);
     boardShapePanel.setBackground(optionGroupColor);
     Border boardShapeBorder = new TitledBorder(new BevelBorder(BevelBorder.RAISED), "Board shape", TitledBorder.ABOVE_TOP, TitledBorder.LEFT);
     boardShapePanel.setBorder(boardShapeBorder);


     ButtonGroup shapeGroup = new ButtonGroup();        // group of radio buttons for area shape
     if (shape==1) squareButton.setSelected(true);
     else if (shape==2) hexButton.setSelected(true);
     else if (shape==3) circleButton.setSelected(true);
     shapeGroup.add(squareButton);
     shapeGroup.add(hexButton);
     shapeGroup.add(circleButton);
     JPanel shapePanel = new JPanel();
     shapePanel.add(squareButton);
     shapePanel.add(Box.createRigidArea(new Dimension(30,0)));
     shapePanel.add(hexButton);
     shapePanel.add(Box.createRigidArea(new Dimension(30,0)));
     shapePanel.add(circleButton);
     shapePanel.setBackground(optionGroupColor);
     Border shapeBorder = new TitledBorder(new BevelBorder(BevelBorder.RAISED), "Shape of areas", TitledBorder.ABOVE_TOP, TitledBorder.LEFT);
     shapePanel.setBorder(shapeBorder);

     ButtonGroup speedGroup = new ButtonGroup();        // group of radio buttons for speed
     if (speed==1) fastButton.setSelected(true);
     else if (speed==2) normalButton.setSelected(true);
     else slowButton.setSelected(true);
     speedGroup.add(fastButton);
     speedGroup.add(normalButton);
     speedGroup.add(slowButton);
     JPanel speedPanel = new JPanel();
     speedPanel.add(fastButton);
     speedPanel.add(Box.createRigidArea(new Dimension(30,0)));
     speedPanel.add(normalButton);
     speedPanel.add(Box.createRigidArea(new Dimension(30,0)));
     speedPanel.add(slowButton);
     speedPanel.setBackground(optionGroupColor);
     Border speedBorder = new TitledBorder(new BevelBorder(BevelBorder.RAISED), "Game speed", TitledBorder.ABOVE_TOP, TitledBorder.LEFT);
     speedPanel.setBorder(speedBorder);

     JPanel sizePanel = new JPanel();                   // relative board size options
     heightBox.setSelectedIndex(heightIdx);
     widthBox.setSelectedIndex(widthIdx);
     pebblesBox.setSelectedIndex(pebblesIdx);
     sizePanel.add(widthBox);
     sizePanel.add(Box.createRigidArea(new Dimension(30,0)));
     sizePanel.add(heightBox);
     sizePanel.setBorder(new TitledBorder(new BevelBorder(BevelBorder.RAISED), "Relative board dimensions", TitledBorder.ABOVE_TOP, TitledBorder.LEFT));
     sizePanel.setBackground(optionGroupColor);


     JPanel resourcePanel = new JPanel();                   // Pebbles
     pebblesBox.setSelectedIndex(pebblesIdx);
     resourcePanel.add(pebblesBox);
     resourcePanel.setBorder(new TitledBorder(new BevelBorder(BevelBorder.RAISED), "Pebbles", TitledBorder.ABOVE_TOP, TitledBorder.LEFT));
     resourcePanel.setBackground(optionGroupColor);

     JPanel lengthPanel = new JPanel();                 // game max length option
     lengthBox.setSelectedIndex(lengthIdx);
     lengthPanel.add(lengthBox);
     lengthPanel.setBorder(new TitledBorder(new BevelBorder(BevelBorder.RAISED), "Game length", TitledBorder.ABOVE_TOP, TitledBorder.LEFT));
     lengthPanel.setBackground(optionGroupColor);

     JPanel buttonsPanel   = new JPanel();
     buttonsPanel.setLayout(new GridLayout(2,2,0,20));   // for buttons and their explanatory labels
     buttonsPanel.add(defaultsLabel);                    // buttons here are defaults and return
     buttonsPanel.add(defaultsPanel);
     buttonsPanel.add(returnLabel);
     buttonsPanel.add(returnPanel);
     buttonsPanel.setBackground(Board.BACKGROUNDCOLOR);

     JPanel statusPanel    = new JPanel();
     TextArea statusArea = new TextArea(optionsStatus,5,60,TextArea.SCROLLBARS_VERTICAL_ONLY);  // intro message
     statusArea.setEditable(false);
     statusPanel.setBackground(Board.BACKGROUNDCOLOR);
     statusPanel.add(statusArea);

     // finally build the frame

     GridBagConstraints constr = new GridBagConstraints();
     constr.insets = new Insets(10,10,10,10);                // padding
     constr.ipadx = constr.ipady = 20;

     constr.gridx = constr.gridy=0;
     constr.gridwidth=2;
     thePane.add(nickPane,constr);
     constr.gridx = 0; constr.gridy = 1; constr.gridwidth = 1;
       thePane.add(typePanel,constr);
     constr.gridx = 1; constr.gridy = 1;
       thePane.add(hillsPanel,constr);
     constr.gridx = 0; constr.gridy = 2;
       thePane.add(boardShapePanel,constr);
     constr.gridx = 1; constr.gridy = 2;
       thePane.add(shapePanel,constr);
     constr.gridx = 0; constr.gridy = 3;
       thePane.add(sizePanel,constr);
     constr.gridx = 1; constr.gridy = 3;
       thePane.add(graphicPanel,constr);
     constr.gridx = 0; constr.gridy = 4;
       thePane.add(speedPanel,constr);
     constr.gridx = 1; constr.gridy = 4;
       thePane.add(lengthPanel,constr);
     constr.gridx = 0; constr.gridy = 5;
       thePane.add(resourcePanel,constr);
     constr.gridx = 1; constr.gridy = 5;
       thePane.add(buttonsPanel,constr);
     constr.gridx = 0; constr.gridy = 6;  constr.gridwidth=2;
       thePane.add(statusPanel,constr);

     myFrame.pack();                          // display the frame
     myFrame.setVisible(true);

    }

  //-------------------------------------------------------------------------------
  // Exiting options: these will have to be remembered
  //  We come here when user exits the option screen
  //-------------------------------------------------------------------------------

    private void exitOptions()
    {
       nick = nickField.getText();

       if (fastButton.isSelected()) speed = 1;
       else if (normalButton.isSelected()) speed = 2;
       else speed = 3;

       if (compactButton.isSelected()) graphics = 1;
       else if (largeButton.isSelected()) graphics = 2;
       else graphics = 3;

       if (squareButton.isSelected()) shape = 1;
       else if (hexButton.isSelected()) shape = 2;
       else shape = 3;

       if (boardSquareButton.isSelected()) boardShape = 1;
       else boardShape = 2;

       if (liteButton.isSelected()) isLite = true; else isLite = false;
       withHills =  hillsButton.isSelected();

       widthIdx   = widthBox.getSelectedIndex();    // also remember idx in choice lists
       heightIdx  = heightBox.getSelectedIndex();
       pebblesIdx = pebblesBox.getSelectedIndex();
       lengthIdx  = lengthBox.getSelectedIndex();

       width      = sizeChoices[widthIdx];
       height     = sizeChoices[heightIdx];
       pebbles    = sizeChoices[pebblesIdx];
       length     = lengthChoices[lengthIdx];

       writeOptions();                              // save options to disc
       welcome();                                   // return to main screen

    }


    private void writeOptions()
    {
       //---- and write on the options file

       try
       {FileOutputStream optionsFile = new FileOutputStream("options"+Control.VERSION_NUMBER+".controloptions");
       ObjectOutputStream OS = new ObjectOutputStream(optionsFile);


       OS.writeObject(nick);
       OS.writeInt(speed);
       OS.writeInt(graphics);
       OS.writeInt(shape);
       OS.writeInt(boardShape);
       OS.writeInt(widthIdx);
       OS.writeInt(heightIdx);
       OS.writeInt(pebblesIdx);
       OS.writeInt(lengthIdx);
       OS.writeInt(width);
       OS.writeInt(height);
       OS.writeInt(pebbles);
       OS.writeInt(length);
       OS.writeObject(lastAddress);
       OS.writeBoolean(isLite);
       OS.writeBoolean(withHills);

       OS.close();
       }
       catch (Exception e) {}     // If it doesn't work then just ignore

    }


  //-------------------------------------------------------------------------------
  // Reset Options to defaults
  //-------------------------------------------------------------------------------

    private void defaults()
    {  graphics = DEFAULT_GRAPHICS;
       shape = DEFAULT_SHAPE;
       boardShape = DEFAULT_BOARDSHAPE;
       speed   = DEFAULT_SPEED;
       length  = DEFAULT_LENGTH;
       height  = DEFAULT_HEIGHT;
       width   = DEFAULT_WIDTH;
       pebbles = DEFAULT_PEBBLES;
       isLite = DEFAULT_ISLITE;
       withHills = DEFAULT_WITHHILLS;

       heightIdx = 0;   // also reset choice idx:es
       widthIdx  = 0;
       pebblesIdx = 0;
       lengthIdx = 1;

       options();       // go back to options screen
    }

  //-------------------------------------------------------------------------------
  // User wants to record game so choose a file
  //-------------------------------------------------------------------------------

    private void recordGame()

    {
        JFileChooser myFileChooser = new JFileChooser("Choose file where to save the game");  // get the file
        myFileChooser.setBackground(Board.BACKGROUNDCOLOR);
        myFileChooser.setCurrentDirectory(new File("C:/Games/Control"));
        if (myFileChooser.showSaveDialog(myFrame) == JFileChooser.APPROVE_OPTION)
         {try
          {record = new ObjectOutputStream(new FileOutputStream (myFileChooser.getSelectedFile()));
           record.writeObject("Game of Control");                                            // write "Game of Control" and version number on the file
           record.writeObject(Control.VERSION_NUMBER);}
         catch(Exception ex)
          {JOptionPane.showMessageDialog(thePane, "Sorry, unable to open and write on file.\n"+   // tell user if and why it failed
                                               "The reason given by the system is:\n"
                                               + ex.toString(),
                                               "File Error",
                                               JOptionPane.ERROR_MESSAGE);
           record = null;
          }
        }
    }

  //-------------------------------------------------------------------------------
  // User wants to play back a recorded game so let him choose a file
  //-------------------------------------------------------------------------------

    private void playBack()
    {   String reason="";           // to hold reason for a failure

        JFileChooser myFileChooser = new JFileChooser("Choose file to play back");  // get the file
        myFileChooser.setCurrentDirectory(new File("C:/Games/Control"));
        if (myFileChooser.showOpenDialog(myFrame) == JFileChooser.APPROVE_OPTION)
          try
           {playback = new ObjectInputStream(new FileInputStream (myFileChooser.getSelectedFile()));
            String s = (String) playback.readObject();
            String s2 = (String) playback.readObject();
            if (!s.equals("Game of Control")) {reason = "Not a recorded game file"; throw new Exception();}    // check it begins correctly
            if (!s2.equals(Control.VERSION_NUMBER)) {reason = "Wrong version number of the recorded game"; throw new Exception();} // check version number
           }
          catch (Exception ex)
            {
             JOptionPane.showMessageDialog(thePane, "Sorry, unable to open and read from file.\n"+   // tell user if and why it failed
                                                 "The reason is:\n"
                                               + ex.toString() + "  " + reason,
                                               "File Error",
                                               JOptionPane.ERROR_MESSAGE);
             playback = null;
            }
        else                                    // user clicked on cancel
            playback=null;

         if (playback != null)              // if recorded game seems OK
          {thePane.removeAll();             // then make this fram invisible
           myFrame.setVisible(false);
           myFrame.pack();

           new PlayBack(this, playback);    // and start the playback!
          }
      }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //
    //  Client side methods: try to connect to server and set up game
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    //-------------------------------------------------------------------------------
    // Connect screen. User should type in an IP
    //-------------------------------------------------------------------------------

    private void startConnecting()
    {
     thePane.removeAll();       // clear screen
     thePane.setLayout(new BoxLayout(thePane, BoxLayout.Y_AXIS));

     JPanel adrPane = new JPanel();     // the panel where address should be input
     JLabel adrLabel = new JLabel("Type host name or IP");   // its label
     adrField = new JTextField(lastAddress,20);            // and text input field
     adrField.addActionListener(this);
     adrPane.setLayout(new BoxLayout(adrPane, BoxLayout.X_AXIS));  // build this panel
     adrPane.add(Box.createRigidArea(new Dimension(50,0)));
     adrPane.add(adrLabel);
     adrPane.add(Box.createRigidArea(new Dimension(50,0)));
     adrPane.add(adrField);
     adrPane.add(Box.createRigidArea(new Dimension(50,0)));
     adrPane.setBackground(Board.BACKGROUNDCOLOR);

     JPanel buttonPanel = new JPanel();                     // build a button panel with abort and quit
     buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
     buttonPanel.add(abortPanel);
     buttonPanel.add(quitPanel);

     JPanel statusPanel    = new JPanel();                  // build a text area explaining status
     TextArea statusArea = new TextArea(connectingStatus,5,35,TextArea.SCROLLBARS_VERTICAL_ONLY);
     statusArea.setEditable(false);
     statusPanel.setBackground(Board.BACKGROUNDCOLOR);
     statusPanel.add(statusArea);

     thePane.add(Box.createRigidArea(new Dimension(0, 50)));        // build the frame
     thePane.add(adrPane);
     thePane.add(Box.createRigidArea(new Dimension(0, 50)));
     thePane.add(buttonPanel);
     thePane.add(Box.createRigidArea(new Dimension(0, 50)));
     thePane.add(statusPanel);
     thePane.setBackground(Board.BACKGROUNDCOLOR);  // make sure background is right
     buttonPanel.setBackground(Board.BACKGROUNDCOLOR);

     myFrame.pack();                    // show it
     myFrame.setVisible(true);

    }


    //-------------------------------------------------------------------------------
    // We come here when User has typed an IP and now we should try to connect to it
    //-------------------------------------------------------------------------------



   private void tryConnecting()
   {
       if (testClient)
            {lastAddress = "localhost";
             testClient = false;
       }

       else
            {lastAddress = adrField.getText();            //remember what the user typed
             writeOptions();                              // also save it in the options file
             adrField.setEditable(false);
             }

       JLabel label = new JLabel("Trying to connect, please wait...");
       myFrame.repaint();                           // for some reason this never seems to happen

       boolean failed = false;                      // temp status variables
       String reason = "";

       if (outSocket != null) try {outSocket.close();} catch (Exception e){} // close any remaining outsocket
       try{outSocket = new Socket(lastAddress, PORT_NUMBER);                // set up the connection
           outgoing = new ObjectOutputStream (outSocket.getOutputStream());
           incoming = new ObjectInputStream (outSocket.getInputStream());
           }
         catch (Exception e) {failed = true;                        // if setting up connection failed
                              reason = e.toString();                // remember why
                              }

       if (failed)
          {JOptionPane.showMessageDialog(thePane, "Sorry, unable to connect.\n"+   // tell user if and why it failed
                                                   "Try again if you want\n" +
                                                   "The reason given by the network is:\n"
                                                   + reason,
                                                   "Connection Error",
                                                   JOptionPane.ERROR_MESSAGE);

          adrField.setEditable(true);                           // Let the useer try again
          label.setText("Type name or IP");
          myFrame.repaint();
         }

       else                 // connection did not fail
           isConnected();   // so proceed to the state where you are connected
       }

    //-------------------------------------------------------------------------------
    // Connection to opponent server successful. Now negotiate game start
    //-------------------------------------------------------------------------------

   private void isConnected()
     {
         // First show the 'is connected' screen


     thePane.removeAll();       // clear screen
     thePane.setLayout(new BoxLayout(thePane, BoxLayout.Y_AXIS));

     JPanel adrPane = new JPanel();            // the panel where address should be input
     JLabel adrlab = new JLabel("Successfully connected to "+lastAddress);     // its label
     adrPane.setLayout(new BoxLayout(adrPane, BoxLayout.X_AXIS));  // build this panel
     adrPane.add(Box.createRigidArea(new Dimension(50,0)));
     adrPane.add(adrlab);
     adrPane.add(Box.createRigidArea(new Dimension(50,0)));
     adrPane.setBackground(Board.BACKGROUNDCOLOR);

     JPanel buttonPanel = new JPanel();                     // build a button panel with abort and quit
     buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
     buttonPanel.add(abortPanel);
     buttonPanel.add(quitPanel);

     JPanel statusPanel    = new JPanel();                  // build a text area explaining status
     TextArea statusArea = new TextArea(connectedStatus,7,30,TextArea.SCROLLBARS_VERTICAL_ONLY);
     statusArea.setEditable(false);
     statusPanel.add(statusArea);

     thePane.add(Box.createRigidArea(new Dimension(0, 50)));        // build the frame
     thePane.add(adrPane);
     thePane.add(Box.createRigidArea(new Dimension(0, 50)));
     thePane.add(buttonPanel);
     thePane.add(Box.createRigidArea(new Dimension(0, 50)));
     thePane.add(statusPanel);

     thePane.setBackground(Board.BACKGROUNDCOLOR);  // make sure background is right
     buttonPanel.setBackground(Board.BACKGROUNDCOLOR);
     statusPanel.setBackground(Board.BACKGROUNDCOLOR);

     myFrame.pack();                    // show it
     myFrame.setVisible(true);

     // Tell opponent I want to play and my params

       String reason="";            // temp status varaibles

       boolean failed = false;

       playWhite = new java.util.Random().nextFloat() < 0.5;  // randomly select my color for play

       try{
            outgoing.writeObject("Game of control");       // First message to say what is going on
            outgoing.writeObject(                          // then tell my params
              new GameParameters(Control.VERSION_NUMBER, graphics, isLite, withHills, nick, shape, boardShape, speed, width, height, pebbles, length));

            outgoing.writeObject(playWhite ? "I play white" // tell who plays white
                                    : "You play white");

            getAccept = new GetAccept();  // start a new thread to listen for reply
            getAccept.start();
           }
       catch (Exception e){failed = true; reason = e.toString();   // if IOfailure remember why
             }
         if (failed)
           {JOptionPane.showMessageDialog(thePane, "Sorry, unable to start game\n"+    // Tell if and why it failed
                                               "Try again if you want\n" +
                                               "The reason  is:\n"
                                               + reason,
                                               "Connection Error",
                                               JOptionPane.ERROR_MESSAGE);

            welcome();                     // if failed then return to main screen
          }

   }

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //
        // Inner class GetAccept: a thread to get a reply from a challenge
        //
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        private class GetAccept extends Thread
        {
            private boolean aborted = false;
            private boolean failed = false;

            private String reply, reason;


            ///---------------------------------  To make me stop-------------------------------------


            void abort()
              {aborted = true;
              }


             ///--------------------------------- Start of this thread---------------------------------

            @Override
            public void run()

              {try
               {reply = (String)incoming.readObject();}                     // try reading something from opponent
                catch (Exception e){failed = true; reason = e.toString();}  // if it failed remember why

                if (reason == null) reason = reply;                         // default fail reason is a text transmitted by opponent ( eg mismatch params)

                if (!aborted)                                               // only continue if I should
                 if (failed || !reply.equals("OK Game of control"))         // if something amiss then inform user
                    {JOptionPane.showMessageDialog(thePane, "Game refused\n" +
                                                            "The contact would not start the game.\n" +
                                                           "The reason is:\n"+
                                                           reason
                                                        ,
                                                           "Game Error", JOptionPane.ERROR_MESSAGE);
                      abortIt();                                         // and tell my parent to stop this attempt


                     }
                 else // read did not fail and game seems OK

                   {try
                       {opponentNick = (String)incoming.readObject();}    // read opponent's nick
                    catch (Exception e) {failed = true; reason = e.toString();} // if that fails I just ignore it
                    startGame();                                           // in any case tell parent to go ahead with game
                  }  // end else
              }
            }

    //-------------------------------------------------------------------------------
    // All is ready, so start the game. We come here when getAccept invokes the method
    // after having receivved an accept to play from opponent
    //-------------------------------------------------------------------------------

    private void startGame()
    {
     thePane.removeAll();               // get rid of the frame
     myFrame.setVisible(false);
     myFrame.pack();

     ControlTimer controlTimer = new ControlTimer(playWhite);       // get new control timer for the player

     if (record != null)                // if recording then write game params on the record
         try {record.writeObject(
             new RecordedGame(Control.VERSION_NUMBER,  isLite, withHills, graphics, playWhite?nick:opponentNick, playWhite?opponentNick:nick,
                                shape, boardShape, width,  height, pebbles, length));}
          catch (Exception e)
               {JOptionPane.showMessageDialog(thePane, "Sorry, unable to record.\n"+   // if this fail tell user
                                               "The reason  is:\n"
                                               + "no response for five seconds",
                                               "File Error",
                                               JOptionPane.ERROR_MESSAGE);
                record = null;                                                          // and cease trying to record
                }

     Player player = new Player(isLite, withHills, playWhite, controlTimer, incoming, outgoing, record, graphics, shape, boardShape,
                                height, width, pebbles, speed, length);                  // Set up the player


     new PlayerInterface (player, controlTimer, this, nick, opponentNick);              // and the interface

    }


    //-------------------------------------------------------------------------------
    // Come here when a game has ended
    //-------------------------------------------------------------------------------

    void playAgain()
    {
    abortIt();          // release everything and go to main screen
    }

    //-------------------------------------------------------------------------------
    //  Come here when you press "abort" instead of accepting to play
    //-------------------------------------------------------------------------------


    private void decline() {
        try {
            outgoing.writeObject("Opponent declines to play");   // Inform the opponent that you decline
        } catch (Exception ex) {}
        abortIt();
    }



    //-------------------------------------------------------------------------------
    // Release everything and go to main screen
    //-------------------------------------------------------------------------------


    private void abortIt()

        {      if (getAccept != null) getAccept.abort();            // abort any threads listening for communication
               if (getConnection != null) getConnection.abort();
               getAccept = null;
               getConnection = null;

               try{
               if (incoming != null) incoming.close();              // close all streams
               if (inSocket != null) inSocket.close();
               if (outgoing != null) outgoing.close();
               if (connection != null) connection.close();
               if (outSocket != null) outSocket.close();
               } catch (Exception ex){}


               outgoing = null;
               incoming = null;
               connection = null;
               outSocket = null;
               inSocket=null;

               if (record != null) try {record.close();} catch (Exception e){}
               record = null;

               welcome();                                           // go to main screen
           }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //
    //  Server side methods: listen for a connection where to play the game
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++



    //-------------------------------------------------------------------------------
    // Set up listening screen
    //-------------------------------------------------------------------------------


    private void startListening()
    {
      thePane.removeAll();                       // clear screen
      thePane.setLayout(new BoxLayout(thePane, BoxLayout.Y_AXIS));


       JPanel buttonsPanel   = new JPanel();
       buttonsPanel.setLayout(new GridLayout(3,2,20,40));
       buttonsPanel.add(abortLabel);                     // set up the buttons
       buttonsPanel.add(abortPanel);
       buttonsPanel.add(quitLabel);
       buttonsPanel.add(quitPanel);
       if (ipLabel != null) buttonsPanel.add(ipLabel);

       JPanel statusPanel    = new JPanel();
       TextArea statusArea = new TextArea(listenStatus,7,40,TextArea.SCROLLBARS_VERTICAL_ONLY);    // message about listening
       statusArea.setEditable(false);
       statusPanel.add(statusArea);

       thePane.add(Box.createRigidArea(new Dimension(0, 30)));
       thePane.add(buttonsPanel);                                   // set up the screen
       thePane.add(Box.createRigidArea(new Dimension(0, 30)));
       thePane.add(statusPanel);

       thePane.setBackground(Board.BACKGROUNDCOLOR);        // make sure background is OK
       buttonsPanel.setBackground(Board.BACKGROUNDCOLOR);
       statusPanel.setBackground(Board.BACKGROUNDCOLOR);

       myFrame.pack();                  // show screen
       myFrame.setVisible(true);
       myFrame.invalidate();
       myFrame.repaint();


       getConnection = new GetConnection();                        // Listen for connection in a new thread!
       getConnection.start();                                      // The reason is that startListening is invoked by
                                                                   // a button click and therefore runs on the
                                                                   // AWT thread. We must NEVER put this thread to sleep,
                                                                   // so it cannot wait at an input.
    }

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //
        // Inner class GetConnection: a thread which tries to establish connection on the server side
        //
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        private class GetConnection extends Thread

             {

              private boolean aborted = false;  // to kill me off


              /// --------------------------------- Method to stop this thread


              void abort(){aborted=true;}

              ///--------------------------------- Start of this thread

              @Override
              public void run()
              {
                   boolean  failed  = false;            // temp status variables
                            aborted = false;

                   String  reason = "";
                   GameParameters gp = null;

                   if (inSocket == null)                                  // only if we have not done this before
                     {try {inSocket = new ServerSocket(StartControl.PORT_NUMBER);}     // create a server socket
                      catch (Exception e)
                          {failed = true;                                 // if that does not work tell user

                           JOptionPane.showMessageDialog(thePane, "Sorry, unable to open server\n"+
                                                            "It seems you have another server listening at this port\n" +
                                                           "Try again if you want, by pressing Abort\n"+
                                                           "The reason given by the network is:\n"+
                                                           (e.toString()),
                                                           "Server Error", JOptionPane.ERROR_MESSAGE);

                          }
                   }

                    if (!failed & !aborted)   // Server created successfully

                     {
                      try                       // try to set up connection
                        {
                            failed = false;
                            connection = inSocket.accept();         // get connection and streams
                            incoming = new ObjectInputStream(connection.getInputStream());
                            outgoing = new ObjectOutputStream(connection.getOutputStream());

                            if (!((String)incoming.readObject()).equals("Game of control"))  // check if it is someone who wants to play
                                {failed = true; reason = "someone made contact but not to play Control";}

                             gp = (GameParameters)(incoming.readObject());

                             if (!gp.versionNumber.equals(Control.VERSION_NUMBER))
                                 {failed = true; reason = "Wrong version of the game";}   // check that we all agree on game parameters
                             if (gp.graphics != graphics)
                             {failed=true; reason="Different graphic sizes";}
                             if (gp.shape != shape)
                                  {failed = true; reason = "Mismatching area shapes";}
                             if (gp.boardShape != boardShape)
                                  {failed = true; reason = "Mismatching board shape";}
                             if (gp.isLite != isLite)
                                 {failed = true; reason = "Only one of us wants ControLITE";}
                             if (gp.withHills != withHills)
                                 {failed = true; reason = "Only one of us wants to play with hills";}
                             if (gp.width != width)
                                  {failed = true; reason = "Mismatching board width";}
                             if (gp.height != height)
                                  {failed = true; reason = "Mismatching board height";}
                             if (gp.pebbles != pebbles)
                                  {failed = true; reason = "Mismatching number of pebbles";}
                             if (gp.speed != speed)
                                  {failed = true; reason = "Mismatching game speed";}
                             if (gp.length != length)
                                  {failed = true; reason = "Mismatching game length";}

                            Object o = incoming.readObject();                               // opponent decides who should play white

                            if (((String)o).equals("I play white")) playWhite = false;
                                else
                                if (((String)o).equals("You play white")) playWhite = true;
                                    else {failed = true; reason = "someone contacted me but then quit";}

                          }
                       catch (Exception e) {failed = true; reason = e.toString();}      // if IO failed remember why

                     if (failed &!aborted )        // if failed tell opponent why
                        {JOptionPane.showMessageDialog(thePane, "Incoming connection refused\n" +
                                                            "Someone tried to make contact but we could not start the game.\n" +
                                                           "The reason is:\n"+
                                                           reason,
                                                           "Game Error", JOptionPane.ERROR_MESSAGE);
                         try {outgoing.writeObject(reason);} catch (Exception e){}
                         finally {abortIt();}
                         }

                       }

                     if (!failed & !aborted)
                         {opponentNick = gp.nick;
                                                    // all is fine, so
                         checkGame();                // let user decide if user wants to play
                          }
              } // end run


            }


    //-------------------------------------------------------------------------------
    // Screen where user decides whether to play an opponent. We come here when getConnection
    // invokes it after receiving a connect request.
    //-------------------------------------------------------------------------------


    private void checkGame()

     {
       thePane.removeAll();              // clear screen
       thePane.setLayout(new BoxLayout(thePane,BoxLayout.Y_AXIS));

       String hostname = connection.getInetAddress().getHostName();   // host name of remote opponent
       JLabel hostLabel;
       if (opponentNick.equals(""))
             hostLabel = new JLabel("Anonymous opponent wants to play from "+hostname,SwingConstants.CENTER);  // label displaying the name
       else  hostLabel = new JLabel(opponentNick+" wants to play from  "+hostname,SwingConstants.CENTER);  // label displaying the name
       hostLabel.setBackground(Board.BACKGROUNDCOLOR);
       JPanel hostPanel = new JPanel();
       hostPanel.add(hostLabel);
       hostPanel.setBackground(Board.BACKGROUNDCOLOR);

       JPanel buttonsPanel   = new JPanel();
       buttonsPanel.setLayout(new GridLayout(3,2,20,40));
       buttonsPanel.add(playLabel);
       buttonsPanel.add(playPanel);
       buttonsPanel.add(abortLabel);
       buttonsPanel.add(abortPanel);
       buttonsPanel.add(quitLabel);
       buttonsPanel.add(quitPanel);

       JPanel statusPanel    = new JPanel();
       TextArea statusArea = new TextArea(acceptingstatus,5,40,TextArea.SCROLLBARS_VERTICAL_ONLY);
       statusArea.setEditable(false);
       statusPanel.add(statusArea);

       thePane.add(Box.createRigidArea(new Dimension(0, 50)));
       thePane.add(hostPanel);
       thePane.add(Box.createRigidArea(new Dimension(0, 50)));
       thePane.add(buttonsPanel);
       thePane.add(Box.createRigidArea(new Dimension(0, 50)));
       thePane.add(statusPanel);

       thePane.setBackground(Board.BACKGROUNDCOLOR);
       buttonsPanel.setBackground(Board.BACKGROUNDCOLOR);
       statusPanel.setBackground(Board.BACKGROUNDCOLOR);


       myFrame.pack();
       myFrame.setVisible(true);

       if (testServer)
           {testServer = false;
            playGame();
       }
    }

    //-------------------------------------------------------------------------------
    // User has pressed Play, so tell opponent and get going
    //-------------------------------------------------------------------------------

    private void playGame()
    {    boolean failed = false;            //  temp

         try {outgoing.writeObject("OK Game of control");
              outgoing.writeObject(nick);    }   // tell opponent I am willing to play, and my nick
            catch (Exception e)
            {                                       // Oops, opponent dropped out
                failed = true;
                    JOptionPane.showMessageDialog(thePane, "Game start failed\n" +
                                                "Connection to your opponent disappeared\n" +
                                               "The reason given by the network is:\n"+
                                               e.toString() ,
                                               "Game Error", JOptionPane.ERROR_MESSAGE);
                    abortIt();
            }
         if (!failed) startGame();      // all is well so game starts
    }



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //
    //  Action listener
    //
    //    Here is defined what happens when buttons are pressed
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == quitButton)                    // quit: close server and leave
           {if (inSocket != null) try {inSocket.close();}  catch (Exception ex) {}
               System.exit(0);}

        if (e.getSource() == connectButton) startConnecting();

        if (e.getSource() == listenButton) startListening();

        if (e.getSource() == abortButton)  decline();

        if (e.getSource() == recordButton) recordGame();

        if (e.getSource() == playbackButton) playBack();

        if (e.getSource() == optionsButton) options();

        if (e.getSource() == defaultsButton) defaults();

        if (e.getSource() == returnButton) exitOptions();

        if (e.getSource() == playButton) playGame();

        if (e.getSource() == adrField) tryConnecting();

    }

}   // Here ends class StartControl
