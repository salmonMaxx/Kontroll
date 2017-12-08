/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;
//************************************************************************
//

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

// Board
//
//************************************************************************

  class Board
{

 //---------------------
 // The state of the game: the pieces on the board
 //---------------------

            ArrayList<Piece>    pieces;              // All pieces on the board. This defines the state of the game.
    private Hill []             hills = {};          // The hills. This defines the board topography


 //----------------------
 // The areas of the board
 //----------------------

     private Area myArea;                              // area under my control
     private Area opponentArea;                        // area under opponent's control
     private Area opponentVisibleArea;                 // area under opponent's control that I can see
     private Area neutralArea;                         // neutral area that I can see
             Area boardArea;                           // area containing all of the board
     private Area highlightArea;                       // area where the currently grabbed piece may go
     private Area buildableArea;                       // area where pebbles may be built

     private Position topLeft, bottomRight;            // initial piece positions

    // Variuos other status variables

             boolean isLite;                          // true if playing lite version (no Keeps)
     private boolean circleBoard;
     private boolean withHills;

     private  Piece [] initialselectors;                // selector pieces

             int relativeNumberOfControlledSquares=0;   // Number of "squares" (of SIZE x SIZE) I control minus opponent control

             int SIZE   ;                          // Number of pixels to the side of a hypothetical square
                                                   // (default = 1/8 of the side of the board)
             int WIDTH  ;                          // Width of the board in squares
             int HEIGHT ;                          // Height of the board in squares

             int shape;                            // shape parameter for areas of pieces

             boolean allVisible = false;           // true if board should be displayed without fog of war
             boolean playBackBoard = false;        // true if board is used to display a playback of a recorded game

             boolean iPlayWhite;                   // true iff I play white

    private static final int FRAMESIZE   = 4;      // frame width of board

    private int selectorIdx = 2;                   // number of current selector

    private final Pebble myPebble;                 // For drawing remaining pebbles

    private  int whiteremaining;                   // Number of remaining Pebbles
    private  int blackremaining;

    private int [] piecesOnBoard = new int[7];     // stores how many of each type of piece I have on board

      private void initPiecesOnBoard ()                         // Resets number of all pieces. Used by calculateControl
              {for (int i=0; i<7; i++) piecesOnBoard[i]=0;
      }

      private void incrementPieceOnBoard(Piece.Type pt) {         // To increment the number of a piece type
          piecesOnBoard[pt.index]++;
      }

      int howManyOnBoard(Piece.Type pt) {         // This is used by Piece to calculate what selectors should be shown
          return piecesOnBoard[pt.index];
      }

    private static final int numberOfSamples = 50;     // Sqrt of number of sample points for determining who controls most area

    private static final int minHills = 5,             // minimum number of hills
                             maxHills = 8;             // maximum number of hills
    private static final float minHillSize = 0.25f,    // possible hill size (in board squares)
                               maxHillSize = 0.66f;



    // Various colors

    private static final Color FRAMECOLOR =         Color.RED;             // Color of frame


            static final Color BACKGROUNDCOLOR =    new Color(180,180,120);  // Background colour

    private static final Color NEUTRAL =            new Color(145,145,145);  // Neutral area

    private static final Color WHITECONTROLLED =   new Color(210,160,160);  // Area controlled by white
    private static final Color WHITECONTROLLED_2 = new Color(205,155,155);
            static final Color WHITEHIGHLIGHT =    new Color(250,120,120);
    private static final Color WHITEBUILD =        new Color(190,140,140);

    private static final Color BLACKCONTROLLED =   new Color(150,150,240);  // Area controlled by black
    private static final Color BLACKCONTROLLED_2 = new Color(145,145,235);
            static final Color BLACKHIGHLIGHT =    new Color(100,100,250);
    private static final Color BLACKBUILD =        new Color(130,130,220);

    private static final Color darkInvisible =     Color.black;             // Area beyond my seeing range


    // Other graphics stuff

    private BufferedImage   whiteBuild, blackBuild;      // for rendering special areas

    private Rectangle textureRect;

    private BufferedImage boardImage;                   // An image of the board

    private Graphics2D boardGraphics;                   // The graphics used to paint the board image


 // Fonts

    private static final Font myFont = new Font("SansSerif", Font.BOLD, 24);      // Font for displaying number of controlled squares
    private static final Font clockFont = new Font("SansSerif", Font.BOLD, 18);   // Font for the clock
    private static final Font stallFont = new Font("SansSerif", Font.BOLD, 36);   // Font for the stall message



 // Debugging things

    private static final boolean debug = false;                                 // The following only used for debugging the computation
    private ArrayList<Region> debugRegions;                                     //   of controlled areas. Turn on at own risk.
    private int numberOfRegions,  numberOfBox, numberOfIntersection, numberOfUncontested,  // debug tracers, some currently ununsed
                timeCalculate, timeHighlight, timeBuildable, timeGraphics,
                timeInit, timeMy, timeOpponents;




        //----------------------------------------------------------------------------
        //
        // A position on the board
        //
        //----------------------------------------------------------------------------


            static class Position implements Serializable
        {
            int x;                   // a position is just a pair of int's.
            int y;

            Position(int x, int y)   // constructor just remembers the parameters
             {this.x = x; this.y = y;};

         //-------------------distance to another position or piece

            int distance (Position p)    // distance is geometric
            {return
              (int)Math.sqrt((x-p.x)*(x-p.x) + (y-p.y)*(y-p.y));
                }

           int distance (Piece p)        // distance to a piece
           {return distance(p.pos);}

        //-------------------- equals

           boolean equals (Position p) {return p != null && p.x == x && p.y == y;}

        }  // end class Position


        //------------------------------------------------------------------------------
        //
        //  inner class Hill: a hill on the board
        //
        //------------------------------------------------------------------------------

            static class Hill implements Serializable       // must be serializable: sent to opponent and written on recording
         {
             private Position pos, center;                          // its position (upper left corner of bounding box) and center
             private int diameter, radius;                          // diameter and radius
             private float height;                                  // height
             private int rings;                             // elevation rings when displayed

        //--------------- Constructor: just set the fields

             Hill (Position pos, int diameter, int rings)
             {this.pos=pos;
              this.diameter=diameter;
              this.rings = rings;
              radius = diameter/2;                          // radius is half diameter
              height = 1.1f + ((float)rings)/10;            // height is 1.1 + 0.1*number of rings
              center = new Position(pos.x+radius, pos.y+radius); // calculate center
             }

        //-------------- invalid: returns true if the hill is invalid

             boolean invalid (Hill [] hills, Area boardArea)     // params are the hills generated so far
             {
               boolean r = false;
               if (!boardArea.contains(pos.x,pos.y,diameter,diameter))  // If the board are does not contain the bounding box, then true
                        return true;
               for (Hill otherHill : hills)                             // Ow, for all hills generated so far
                 {if (otherHill != null & otherHill != this)
                   r |= otherHill.center.distance(center)               // Check that the distance between centers is larger than hill extents
                          < radius + otherHill.radius +1;
                 }
                return r;
             }

        // ------------- onIt: returns true if the position is on the hill

             boolean onIt(Position p)
             {return p.distance(center) <= radius;}                     // p is closer than radius to center


         // ------------ draw: draw me

              private void draw (Graphics2D g, int SIZE)                        // param SIZE needed to compute ring distance
              {
               final int distCirc = SIZE/12;                            // ring distance is 1/12 of SIZE
               g.setColor(Color.BLACK);                                 // draw with black color
               int d = 0;
               int i=0;
               while (i<rings & diameter > d)                           // for each ring
                 {g.drawOval(pos.x+d/2, pos.y+d/2,diameter-d,diameter-d);  // draw it
                  i++;
                  d += distCirc;}
              }
         }   // end class Hill

 //---------------------------------------------------------------------------
 // Board Constructor just sets things up
 //---------------------------------------------------------------------------

     Board (boolean isLite,
            boolean withHills,
            int     graphics,
            int     shape,
            int     boardS,
            int     height,                   // params are board dimensions
            int     width,
            int     pebbles,
            boolean iPlayWhite)          // and initial number of pebbles
    {

  //------------- Set various parameters

        WIDTH = width;                          // remember params from invocation
        HEIGHT = height;
        this.shape = shape;
        this.iPlayWhite=iPlayWhite;
        this.isLite = isLite;
        this.withHills = withHills;
        circleBoard = boardS == 2;
        whiteremaining = blackremaining = pebbles;  // Set initial number of available Pebbles

        if (graphics==1) SIZE = 60;             // set the SIZE according to graphics parameter
          else if (graphics==2) SIZE = 80;
          else SIZE=100;

        SIZE = (SIZE * 8) / Math.max(WIDTH, HEIGHT);  // calculate  square size in pixels

        if (circleBoard)                                              // If a circular board
           {Ellipse2D.Float boardShape = new Ellipse2D.Float();       //   then declare a shape for it
            boardShape.setFrame(SIZE, SIZE, SIZE*WIDTH, SIZE*HEIGHT); //   set its position and size parameters
            boardArea = new Area(boardShape);                         //   and define the area for this shape

            topLeft = new Position(SIZE*(WIDTH+2)/2,SIZE+1);          //   the initial piece positions are here
            bottomRight = new Position(SIZE *(WIDTH+2)/2,             //   the top and bottom positions of the ellipse
                                                 SIZE*(HEIGHT+1)-1);
           }
        else                                                          // If a rectangular board
           {Rectangle2D.Float boardShape = new Rectangle2D.Float();   //   then declare a shape for it
            boardShape.setFrame(SIZE, SIZE, SIZE*WIDTH, SIZE*HEIGHT); //   set its position and size parameters
            boardArea = new Area(boardShape);                        //    and define the area for this shape

            topLeft = new Position(SIZE,SIZE);                       //    initial positions are
            bottomRight = new Position(SIZE *(WIDTH+1)-1,            //    opposite corners of the board
                                                 SIZE*(HEIGHT+1)-1);}


   //--------------- initialise various variables

        myArea = new Area();
        opponentArea = new Area();
        opponentVisibleArea = new Area();
        neutralArea = new Area();
        debugRegions = new ArrayList();
        highlightArea = new Area();
        buildableArea = new Area();

        pieces = new ArrayList();

        newPiece (Piece.Type.PEBBLE, true, iPlayWhite,  topLeft);                          // initial white piece
        newPiece (Piece.Type.PEBBLE, false,!iPlayWhite, bottomRight);                      // initial black piece

        initialselectors = new Piece[6];                       // set up selector pieces
        int selectorPosX = SIZE/2;                            // x-coordinate of selector pieces

        initialselectors[0] =  newPiece (Piece.Type.PEBBLE,true,true, new Position(selectorPosX,2*SIZE));
        initialselectors[1] =  newPiece (Piece.Type.RUBBLE,true,true ,new Position(selectorPosX,3*SIZE));
        initialselectors[2] =  newPiece (Piece.Type.NIMBLER,true,true ,new Position(selectorPosX,4*SIZE));
        initialselectors[3] =  newPiece (Piece.Type.QUORUM,true,true ,new Position(selectorPosX,5*SIZE));
        initialselectors[4] =  newPiece (Piece.Type.BOUNCER,true,true ,new Position(selectorPosX,6*SIZE));
        initialselectors[5] =  newPiece (Piece.Type.TRIPPLE_DUDE,true,true ,new Position(selectorPosX,7*SIZE));

        myPebble   = (Pebble)initialselectors[0];     // For drawing remaining pebbles use the pebble selector!

        for (Piece selector : initialselectors) selector.setAreas();

  //-------------- Set up textures for the graphics


        textureRect = new Rectangle(0,0,10,10);            // 10x10 texture rectangle, used for buildable areas


 //-------------- Texture for buildableArea controlled by white

         whiteBuild = new BufferedImage(10, 10,
                                BufferedImage.TYPE_INT_RGB);
         Graphics2D bigWb = whiteBuild.createGraphics();
         bigWb.setColor(WHITECONTROLLED_2);
         bigWb.fill(textureRect);
         bigWb.setStroke(new BasicStroke(1));
         bigWb.setColor(WHITEBUILD);
         bigWb.drawOval(0,0,10,10);

//-------------- Texture for buildableArea controlled by black

         blackBuild = new BufferedImage(10, 10,
                                BufferedImage.TYPE_INT_RGB);
         Graphics2D bigBb = blackBuild.createGraphics();
         bigBb.setColor(BLACKCONTROLLED_2);
         bigBb.fill(textureRect);
         bigBb.setStroke(new BasicStroke(1));
         bigBb.setColor(BLACKBUILD);
         bigBb.drawOval(0,0,10,10);


 //-------------- The buffered image for the board (note it includes the margins)

         boardImage = new BufferedImage((WIDTH+2)*SIZE, (HEIGHT+2)*SIZE,
                                BufferedImage.TYPE_INT_RGB);
         boardGraphics = boardImage.createGraphics();

    }

//------------------- end of board constructor ----------------------------------




//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Utility functions: check if position is on the board.
// Determine top active selector
// methods to access and check remaining pebbles
// methods to manipulate pieces
// etc....
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

//--------- generate the Hills --------------------------------------------




     // -------------- Generate all the hills ------------------------------------------

     Hill []  generateHills()
       { Random rand = new Random();                        // get me a new random number generator
         Hill hill;                                         // local hill. I'll generate a hill there and
                                                            // use it only if it doesn't collide with other hills
                                                            // or the border
         int numberOfHills = (rand.nextInt(maxHills-minHills+1)+minHills)  // determine the number of Hills
                                * WIDTH*HEIGHT/64,          // scaled to board size in squares
              sizeMin = (int) (SIZE*minHillSize),           // minimum hill size
              sizeMax = (int) (SIZE*maxHillSize);           // maximum hill size

         hills = new Hill [numberOfHills];                  // get the array for hills
         for (int i = 0; i<numberOfHills; i++)              // for each element in the array,
         {do
          {int x = 2*SIZE + rand.nextInt(SIZE*(WIDTH-2)),   //    determine the position
              y = 2*SIZE + rand.nextInt(SIZE*(HEIGHT-2)),
              d = rand.nextInt(sizeMax - sizeMin) + sizeMin, //   and diameter
              rings = rand.nextInt(5)+1;                       //   and height
              hill = new Hill(new Position(x,y),d,rings);    //   and create this hill
          }
          while (hill.invalid(hills, boardArea));              //   repeat this until the hill checks out
          hills[i]=hill;                                     //   then save it in the array
         }
         return hills;                                       // return the result (also saved in hills)
       }


    //----------- let someone else tell me what the hills should be

       void setHills (Hill [] hills)                            // just store the parameter in hills
       {this.hills = hills;}

    //----------------Checks if position is on the board----------------------------------

      boolean isLegal(Position p)
        {return (p!= null) && boardArea.contains(p.x, p.y);}    // A position is legal if non null and on the board

    //----------------Elevation: more than 1 if on a hill ----------------------------------

    float elevation(Position pos)
     {for (Hill hill : hills)                           // for every hill
        if (hill.onIt(pos)) return hill.height;         // if on it then return the hill height
      return 1f;                                        // if not on any hill return 1
    }


    //----------------Set the default selector-----------------------------------------

      Piece defaultselector()                              // default selector is topmost available selector
        {for (int y=0; y<5; y++)                                 // y runs through selector idx
            if (initialselectors[y].shouldShowSelector()) // if selector y is available
              {selectorIdx = y;                                  // then this is it!
               return initialselectors[y];}
        return null;};                                           // nothing available

    //----------------Cycle through selectors-----------------------------------------

     Piece cycleSelector()
        {int y;                                                  // y cycles from selectorIdx+1 to selectorIdx
         if (selectorIdx==4) y=0; else y = selectorIdx+1;        // going from 2 to 6
         while (y != selectorIdx)
            {if (initialselectors[y].shouldShowSelector())  // if y is available
               {selectorIdx = y; return initialselectors[y];}       // then this is it!
             if (y==4) y=0; else y++;
             }
         if (initialselectors[y].shouldShowSelector())      // nothing found on one trip
           return initialselectors[y]; else return null;            // so let previous selector remain if possible
        }

    //----------------Find selector at position p-----------------------------------------

     Piece select(Position p)
    { Piece result = null;
        for (int y=0; y<5 ; y++)                     // y runs through selector idx
          {   Piece piece = initialselectors[y];
              if (piece.shouldShowSelector()               // if the selector is shown
                 && piece.on(p))                                   // and is located on p
              {selectorIdx = y;                                    // then it is it.
               result = piece;}
          }
          return result;


    }


    //-------------------Remaining number of pebbles---------------------------------

      int remaining(boolean whiteside)
      {return (whiteside ? whiteremaining : blackremaining) ;}

    //-------------------Decrease remaining number of pebbles------------------------

      void decreaseRemaining (boolean whiteside)
       {if (whiteside) whiteremaining--; else blackremaining--;}

    //-------------------Determine what position a mouse event refers to ------------------------


    // We also check if the position is very near a piece
    // If so, we return the position of that piece. This means when you click a piece you don't have to
    // hit the exact center. An exception is when moving a piece only a very short
    // distance, then the mouse release will happen when it is still very close to itself.
    // Therefore an extra parameter of type Piece tells what piece should be exempt from
    // this proximity check.

      Position realPosition (int x, int y, Piece exemptPiece)           // params are pixel coordinates and
    {                                                                         // the piece exempt from proximity check
        Position res =  new Position(x,y);                                    // First create the proper position
        {int min = 10000;                                                     // (min is initially bigger than any possible distance)
        Piece closestPiece = null;
            for (Piece thePiece : pieces)
           {                                                    // It may be "on" several pieces
               if (thePiece.on(res)                             // if so we must find the closest to res
                     &  ! (thePiece == exemptPiece)
                     &  res.distance(thePiece) < min)
               {min = res.distance(thePiece);                   // found a new minimum, remember it
                closestPiece = thePiece;}
            }
         if (closestPiece != null) return closestPiece.pos;
         else return res;
        }}


     Position realPosition(int x, int y) {return (realPosition(x, y, (Piece)null));}   // Ditto, without info about exempt piece

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // create a new piece and add it to the board if it is on a legal board position
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    final Piece  newPiece(Piece.Type pt, boolean w, boolean my, Position p)
          {Piece res = Piece.newPiece(pt,w,my,p,this);
            if (isLegal(p))
                addPiece(res);      // put it on the board, if on a legal position
           return res;
          }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Add a piece to the board
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

      Piece addPiece(Piece p)
      {
         Piece victim = find(p.pos);        // If that spot is occupied
         if (victim != null) removePiece(victim); // then remove the piece there
         pieces.add(p);             //  add it to the list of pieces
         p.setAreas();                     // and make sure you calculate its areas
          return p;
     }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Remove a piece from the board
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

      void removePiece(Piece p)
      {
          pieces.remove(p);          // just remove it from the list of pieces
      }



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Find the piece at position x,y
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

     Piece find(Position pos)
    {
        Piece result = null;
        for (Piece piece : pieces)          // For all pieces
           if (piece.pos.equals(pos)) result = piece;                 // if its position is pos then it is it.
        return result;
    }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Check if a controlled piece of certain type is near
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    // This only applies to Pebbles and Keeps. Rubbles instead use buildableArea


    boolean near(Piece.Type pt, Position pos)
    {boolean found = false;
     for (int i=0; i<pieces.size() & !found; i++)                 // for all pieces
         {Piece piece = (Piece)pieces.get(i);
          found =    piece.pieceType == pt                         // is it of right type?
                  && piece.myside                                   // on my side?
                  && (pos.distance(piece) > SIZE*2/3 || pt != Piece.Type.PEBBLE) // not too close (pebble only)
                  && pos.distance(piece) < SIZE                     // not too far away
                  && myArea.contains(piece.pos.x, piece.pos.y);     // piece controlled by me
         }

     if (found & pt == Piece.Type.PEBBLE)                                   // for pebble, check that other pieces are not too close
     for (int i=0; i<pieces.size() & found; i++)
         {Piece piece = (Piece)pieces.get(i);
          found = pos.distance(piece) > SIZE*2/3;
     }

     return found;
    }

    //  Various checks on the properties of a position


    boolean iControl(Position pos)
    {return myArea.contains(pos.x, pos.y);}

    boolean opponentControls(Position pos)
    {return opponentArea.contains(pos.x, pos.y);}

    boolean iControl(Piece p)
    {return iControl(p.pos);}

    boolean opponentControls(Piece p)
    {return opponentControls(p.pos);}

    boolean canMoveTo(Position pos)
    {return highlightArea.contains(pos.x,pos.y);}

    boolean canBuildPebbleOn(Position pos)
    {return buildableArea.contains(pos.x,pos.y);}






//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// Methods for painting the board
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// generateBoardGraphics: Genereate a buffered image of the board
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


 void generateBoardGraphics(Piece currentPiece)   // selected piece (will be highlighted)

{                           long startTime = System.currentTimeMillis();   // timing only for debugging purposes

   // This will paint the buffered image of the board, omitting current position marker, clock, and selectors


         BufferedImage bi = new BufferedImage((WIDTH+2)*SIZE, (HEIGHT+2)*SIZE,          // get a new temporary image to draw on
                                BufferedImage.TYPE_INT_RGB);
         Graphics2D g = bi.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);


       // Begin by drawing the background, a board where everything is invisible, and frame


         g.setColor(BACKGROUNDCOLOR);
         g.fillRect(0,0, SIZE*(WIDTH+2), SIZE*(HEIGHT+2));

         g.setColor(darkInvisible);
         g.fill(boardArea);

         if (circleBoard)
             {g.setColor(FRAMECOLOR);
              g.setStroke(new BasicStroke(FRAMESIZE));
              g.draw(boardArea);
              g.setStroke(new BasicStroke(1));
             }
         else
             {
             g.setColor(FRAMECOLOR);
             g.fillRect(SIZE-FRAMESIZE, SIZE-FRAMESIZE, SIZE*WIDTH+2*FRAMESIZE, FRAMESIZE);
             g.fillRect(SIZE*(WIDTH+1), SIZE, FRAMESIZE, SIZE*HEIGHT);
             g.fillRect(SIZE-FRAMESIZE, SIZE*(HEIGHT+1), SIZE*WIDTH+2*FRAMESIZE, FRAMESIZE);
             g.fillRect(SIZE-FRAMESIZE, SIZE, FRAMESIZE, SIZE*HEIGHT);
             }

  //--------  Now generate areas for white, black and on

           Area whiteArea = iPlayWhite ? myArea : opponentVisibleArea;
           Area blackArea = iPlayWhite ? opponentVisibleArea : myArea;


  //--------  Paint the areas: my, opponent's and neutral (visible)


           g.setColor(NEUTRAL);
           if (allVisible) g.fill(boardArea); else g.fill(neutralArea);

           g.setColor(BLACKCONTROLLED);
           g.fill(blackArea);

           g.setColor(WHITECONTROLLED);
           g.fill(whiteArea);



 //--------- Paint the buildable area

           g.setColor(iPlayWhite ? WHITEBUILD : BLACKBUILD);
            g.setPaint(new TexturePaint(iPlayWhite ? whiteBuild : blackBuild, textureRect));
           g.fill(buildableArea);

 //---------- Paint the highlighted area

           g.setColor(iPlayWhite ? WHITEHIGHLIGHT : BLACKHIGHLIGHT);
           g.fill(highlightArea);

 //---------- Paint the hills

           if (withHills)
               for (Hill hill : hills) hill.draw(g,SIZE);

 //----------- Now paint all pieces

       for (Piece thePiece : pieces)                                     // For all pieces
       {
           if (thePiece == currentPiece) thePiece.draw(g, Color.red);             //Currently grabbed piece is red
           else if (thePiece.myside ||                                              // OW, determine if it is visible
                    myArea.contains(thePiece.pos.x, thePiece.pos.y) ||
                    neutralArea.contains(thePiece.pos.x, thePiece.pos.y) ||
                    opponentVisibleArea.contains(thePiece.pos.x, thePiece.pos.y)
                   ) thePiece.draw(g);                                           // if it is then draw it

           }

 //----------- Paint line of remaining pebbles

        if (allVisible)                                                        // if allVisible then draw both white and black Pebbles
        {for (int i=1; i<= remaining(true); i++)
              myPebble.draw(g, new Position(i*SIZE+SIZE/2,SIZE/2), Color.white);
         for (int i=1; i<= remaining(false); i++)
              myPebble.draw(g,new Position( i*SIZE+SIZE/2, SIZE*(HEIGHT +1) + SIZE/2), Color.black);}
        else                                                                  // else only show my Pebbles
         for (int i = 1; i<=remaining(iPlayWhite); i++)
             myPebble.draw(g, new Position(i*SIZE+SIZE/2, SIZE/2), iPlayWhite ? Color.white : Color.black);



//------------  Show who controls most area

      if (iPlayWhite) g.setColor(Color.white); else g.setColor(Color.black);
      g.setFont(myFont);
      if (relativeNumberOfControlledSquares<0) g.setColor(Color.red);
      g.drawString(relativeNumberOfControlledSquares+" ",SIZE/2,SIZE/2);

//--------------- show selection pieces

       if (!playBackBoard)          // In a playback no selectors are shown.

       for (Piece selector : initialselectors) {
           if (selector.shouldShowSelector())
               selector.drawOutline(g, (iPlayWhite ? Color.white : Color.black));
       }

 //--------------debug: show regions

         if (debug)
         { g.setColor(Color.green);
             for (Region region : debugRegions)
                 g.draw(region.area);
         }


 // -------------- Finally, paint all this on the proper board image

          boardGraphics.drawImage(bi,null,0,0);
          timeGraphics = (int)(System.currentTimeMillis() - startTime);
}

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// paint: paints the board,using the buffered image
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  void paint (Graphics g,             // Where to paint it
                    boolean  waiting,       // true if timer not expired, so player cannot move
                    boolean  stalling,      // true if "Stalling" should be shown
                    int      clock         // number of seconds remaining
                   )


{
         Graphics2D page = (Graphics2D)g;
         page.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
         page.drawImage(boardImage,null,0,0);       // render the buffered image

// --------------- Show if I can move

       if (!waiting & !playBackBoard) myPebble.draw(page,(new Position(SIZE/2,SIZE*(HEIGHT+1))), Color.red);

// ---------------- Show clock

       page.setColor(Color.black);
       page.setFont(clockFont);
       if (clock <= 10)                      // close to timout: clock is red and larger
        {page.setColor(Color.red);
         page.setFont(myFont);}
       page.drawString(clock/60 + ":" + (clock%60)/10 + (clock%60)%10,
                  (SIZE*WIDTH+SIZE/2),SIZE/2);

// ----------------- Some diagnostics and debug info
       if (debug)
       {page.drawString("U: "+numberOfUncontested+" ", SIZE*(WIDTH+1), SIZE*(HEIGHT-1)/2);
       page.drawString("R: "+numberOfRegions+" ", SIZE*(WIDTH+1), SIZE*HEIGHT/2);

       page.drawString("B: " + numberOfBox, SIZE*(WIDTH+1), SIZE*(HEIGHT+1)/2) ;
       page.drawString("I: " + numberOfIntersection, SIZE*(WIDTH+1), SIZE*(HEIGHT+2)/2) ;
       page.drawString("Ct: "+ timeCalculate,  SIZE*(WIDTH+1),SIZE*(HEIGHT+4)/2);
       page.drawString("It: "+ timeInit,  SIZE*(WIDTH+1),SIZE*(HEIGHT+5)/2);
       page.drawString("Mt: "+ timeMy,  SIZE*(WIDTH+1),SIZE*(HEIGHT+6)/2);
       page.drawString("Ht: "+ timeOpponents,  SIZE*(WIDTH+1),SIZE*(HEIGHT+7)/2);
       }

// ------------------ show stall message

       if (stalling)
       {page.setColor(Color.red);
         page.setFont(stallFont);
         page.drawString("S T A L L I N G",SIZE*3,SIZE/2);
       }
 }



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Recalculate control of each area
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    //-------------------------------------------------
    //  A piece is uncontested if its range does not intersect the range of any opposite piece
    //-------------------------------------------------

     private boolean uncontested(Piece p)
     {boolean res = true;                       // result will be true unless we find a
        for (Piece other : pieces)              // piece, call it other
          {if ((p.myside != other.myside)       // playing for the opposite side
             && p.currentRange + other.currentRange >= p.pos.distance(other)) // and so close that the range areas overlap
           res = false;
          }
      return res;
     };


     //-----------------------------------------------
     //  Inner class Region: a part of the board
     //-----------------------------------------------

     // During the recalculation of control areas the board is partitioned into regions. The regions are formed
     // by intersections of the piece range areas.
     // Each region has an Area which it occupies, an integer control value which is the amount of control
     // exerted on it so far (positive for my side, negative for opponent, zero for neutral), a boolean visibility
     // indicator which tells if that region should be painted on the board, and Rectangle bounding box.
     // The reason for the latter is that it is more efficient to perform some of the computations on
     // bounding boxes before emabarking on more time consuming calculations on Areas.


     private class Region
     { Area area;                // the area on the board of the region
       int control;              // control degree measured so far (negative = opponent)
       boolean visible;          // true if visible
       Rectangle boundingBox;    // bounding box of the area
       Position center;          // center of the bounding box
       float radius;             // radius of the bounding box. So all positions in the area is within radius of the center.

     //----- Constructor just sets things up and calculates the bounding box

       private  Region (Area a, int c, boolean v)
       {area = a; control = c; visible = v;  setBounds();}



     //------ (re)calculate the bounding box, its center and radius

       private void setBounds()
       {boundingBox = area.getBounds();
        center = new Position (boundingBox.x + boundingBox.width/2, boundingBox.y+boundingBox.height/2);
        radius = center.distance(new Position(boundingBox.x, boundingBox.y));
       }


     //------------ decided: check if my control can possibly fall to zero or below


       private boolean decided (ArrayList<Piece> pieces)                          // parameter is opponent pieces to take into consideration
       {
        int c = control;                                                            // let c be the lowest control I can possibly reach
        for (Piece inspected : pieces) {                                           // as long as c is positive, and for all pieces
            if (center.distance(inspected) <= inspected.currentRange + radius)    // if it is so close that it might have an influence on my area
                c -= inspected.power;                                             // decrease c by its power
        }
        return c>0;                                                               // return true iff c is still positive
       }

     //------------- addMe: add me to a list of regions unless my fate can already be inferred

     // The list of regions is the list of still undecided regions who need further examination
     //
     // If my control is negative it can only decrease, so add me to his area
     // else if it is decided (cannot become non-positive) then add me to my area
     // else keep me in the list of regions

       private void addMe(ArrayList<Region> regList, ArrayList<Piece> pieces)
       {   if (control < 0)                                           // if control <0 then
              {opponentArea.add(area);                                     // it can only decrease further so
              if (debug) debugRegions.add(this);                      // add it to his area and
              if (visible) opponentVisibleArea.add(area);}                 // not to the region list
           else                                                       // if control >= 0
           if (decided(pieces))                                       // and the area is deceidedly mine
              {myArea.add(area);if (debug) debugRegions.add(this);}   // then add it to my area
           else regList.add(this);                                    // else add it to the list of regions
       }

     //------------- checkPiece--------------------------
     //
     // This determines what happens to the me when a Piece is taken into consideration.
     // If the range area completely encloses me, then update the control value with the amount
     // exerted by the piece. If the range area is disjoint from me then do nothing. If it intersects me
     // then create a new region corresponding to the intersection, and subtract it from my area. For
     // efficiency the predicates are first tested on my bounding box.
     // The resulting  regions are accumulated in the second parameter. The third
     // parameter is a list of enemy pieces to consider when determining if the region is decided.

       private void checkPiece(Piece p, ArrayList<Region> newRegions, ArrayList<Piece> pieces)
        { if (center.distance(p)  >  radius + p.currentRange) newRegions.add(this);  // area completely out of range: just forward the area to newRegions
          else
             if
             (p.rangeArea.contains(boundingBox))                    // If my bounding box lies completely in range
               {control += p.power * (p.myside?1:-1); numberOfBox++;// update my control value with the power of the piece
                addMe(newRegions, pieces);}                         // and add me to the new regions
             else if ( p.rangeArea.intersects(boundingBox))        // else, if the bounding box intersects the range
                 {Area newArea = p.rangeArea();                       // create a new area that is the intersection
                  newArea.intersect(area);                            // with the range area
                  numberOfIntersection++;                             // (debug)
                  if (newArea.equals(area))                           // if that turns out to be the whole area
                       {control += p.power * (p.myside?1:-1);         // just update my control with the piece power
                        addMe(newRegions, pieces);}                   // and add me to the new regions
                  else if (!newArea.isEmpty())                        //  else, if that turns out to be nonempty
                       {area.subtract(newArea);                        // subtract it from my area
                        setBounds();                                   // recalulate my bounding box
                        addMe(newRegions, pieces);                     // and add me to newRegions
                        Region newRegion = (new Region(newArea,        // and create a new region for the intersection
                                  control + p.power * (p.myside?1:-1),// with updated power
                                  p.myside | visible));                // and visibility the same as before, or true if piece on my side
                        newRegion.addMe(newRegions,pieces);            // and invoke its addMe for adding it to newRrgions
                       }                                               // if the intersection is empty
                   else addMe(newRegions, pieces);                    // then just forward me to new Regions
                  }                                                    // also if intersection of bounding box is empty
             else addMe(newRegions, pieces);
             numberOfBox++;
       }
     }

    //-----------------end class Region


    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // calculateControl: determine myArea, opponentArea, opponentVisibleArea, onAreas. Also count number of pieces on board
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

     void calculateControl()

    //----------  first reset and initialise variables

    {           long startTime = System.currentTimeMillis();



       initPiecesOnBoard();             // reset variables and areas
       myArea.reset();
       opponentArea.reset();
       opponentVisibleArea.reset();
                numberOfBox=0;
                numberOfIntersection=0;
       neutralArea.reset();
                numberOfUncontested=0;
                debugRegions.clear();

       ArrayList<Piece> myPieces = new ArrayList();                                // the subset of pieces that are mine
       ArrayList<Piece> opponentPieces = new ArrayList();                          // the subset of pieces that are the opponent's

       ArrayList<Region> regions = new ArrayList();                                 // initialise the "regions" to contain just one,
       regions.add(new Region((Area)boardArea.clone(),0,allVisible));               // the entire board with control 0

     // Sort pieces into mine and opponent's, update number records,
     // Let uncontested pieces exert directly (these will not need to generate regions)

        for (Piece inspected : pieces)
        {
         if (inspected.myside)                                     // if it is my piece
                {
                 incrementPieceOnBoard(inspected.pieceType);                    // update the record of number of pieces
                 {if (!uncontested(inspected)) myPieces.add(inspected);  // add it to the list of my pieces
                 else {myArea.add(inspected.rangeArea);                // or, if uncontested, add the range area directly
                       numberOfUncontested++;}
                 }
               }
              else                                                    // if it is opponent's piece
                {
                 if (!uncontested(inspected))opponentPieces.add(inspected); // and add it to the list of opponent pieces
                  else                                                  // or, if uncontested
                  {opponentArea.add(inspected.rangeArea); numberOfUncontested++;
                  if (allVisible) opponentVisibleArea.add(inspected.rangeArea); // add it to opponentArea or opponentVisibleArea
                  }
                }
        }                                                            // end for each piece

                    timeInit = (int)(System.currentTimeMillis() - startTime);

     //----------- check the influence of all my contested pieces on the control areas
     //
     // This will generate regions of increasing positive control, as the range areas intersect.

                    long s2 = System.currentTimeMillis();

        for (Piece inspected : myPieces)          // for each of my pieces
         {
          ArrayList<Region> newRegions = new ArrayList();                           //  the  regions that need to be examined after this
          for (Region thisRegion : regions)      // for each existing region
            thisRegion.checkPiece(inspected, newRegions, opponentPieces); // let the region check the piece for possible influence on its area and control                                                             // end for all regions
          regions = newRegions;                                      // continue with the newly generated regions
          }                                                          // end for each of my pieces

                    timeMy = (int)(System.currentTimeMillis() - s2);

     //--------- check the influence of opponent pieces on the control areas
     //
     // This will generate regions with decreasing control.

                     s2 = System.currentTimeMillis();
        while (!opponentPieces.isEmpty())                   // for each of opponent's pieces
        {Piece inspected = (Piece)opponentPieces.get(0);              // call it inspected
         opponentPieces.remove(inspected);                                   // remove it from opponentPieces (so it cannot unduly affect the screening by "decided")
         ArrayList<Region> newRegions = new ArrayList();                             // reset the new regions to be generated
         for (Region thisRegion : regions)                        // for each region
           thisRegion.checkPiece(inspected, newRegions, opponentPieces);  // let the region check the piece for possible influence on its area and control
        regions = newRegions;                                       // continue with the newly generated regions
        }                                                              // end for each of his pieces
                    if (debug) debugRegions.addAll(regions);
                    numberOfRegions = debugRegions.size();
                    timeOpponents = (int)(System.currentTimeMillis() - s2);
                    s2 = System.currentTimeMillis();

     //--------  collect the regions into the main variables

        for (Region thisRegion : regions)
           {
            if (thisRegion.control>0) myArea.add(thisRegion.area);
            if (thisRegion.control == 0 & thisRegion.visible) neutralArea.add(thisRegion.area);
            if (thisRegion.control < 0 & thisRegion.visible) opponentVisibleArea.add(thisRegion.area);
            if (thisRegion.control < 0) opponentArea.add(thisRegion.area);
           }

                 s2 = System.currentTimeMillis();

        calculateRelativeNumberOfSquares();                       // finally calculate who controls most territory

                timeInit  = (int)(System.currentTimeMillis() - s2);
                timeCalculate = (int)(System.currentTimeMillis() - startTime);

    }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // calculateRelativeNumberOfSquares: check who controls most territory
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    // result is stored in the global variable relativeNumberOfControlledSquares.
    // the algorithm is to generate a grid of points (of cardinality numberOfSamples^2)
    // and for each point check membership in myArea or hisArea.




     private void calculateRelativeNumberOfSquares()

     {final float sampleIncrementX = (SIZE*WIDTH)/numberOfSamples;              // distance between sample points
      final float sampleIncrementY = (SIZE*HEIGHT)/numberOfSamples;
      final float scaleFactor = numberOfSamples*numberOfSamples/HEIGHT/WIDTH;   // to scale the result down to something meaningful
      relativeNumberOfControlledSquares=0;
      for (float i=SIZE; i<=SIZE*(WIDTH+1); i=i+sampleIncrementX)               // so just check all sample points
             for (float j=SIZE; j<=SIZE*(HEIGHT+1); j=j+sampleIncrementY)
                 {if (myArea.contains(i,j)) relativeNumberOfControlledSquares++;
                  else if (opponentArea.contains(i,j)) relativeNumberOfControlledSquares--;
                 }
        relativeNumberOfControlledSquares = Math.round(((float)(relativeNumberOfControlledSquares)/(float)scaleFactor));
     }



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Recalculate buildble squares
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

     void calculateBuildable()


      {         long startTime = System.currentTimeMillis();
       buildableArea.reset();
       if (myPebble.shouldShowSelector())                         // buildable only if I can build a Pebble
       {for (Piece inspected : pieces)                           //   For each piece
           {
            if (    inspected.pieceType == Piece.Type.RUBBLE                // if it is a Rubble
                  & inspected.myside                                      // on my side
                  & myArea.contains(inspected.pos.x,inspected.pos.y)      // and controlled by me
                ) buildableArea.add(inspected.nearArea());                // add its near area to buildable
            }
         buildableArea.intersect(myArea);                                 // but buildable area must be controlled
         for (Piece inspected : pieces)                           //   For each piece
             buildableArea.subtract(inspected.avoidBuildArea);
                timeBuildable = (int)(System.currentTimeMillis() - startTime);
         }
       }



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Recalculate highlighted area
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

     void calculateHighlight(Piece currentPiece)
    {       long startTime = System.currentTimeMillis();
     for (Piece inspected : pieces)                           //   For each piece
           inspected.underAttack = false;                       // set its "underAttack" to false


       if (currentPiece != null)                                        // only if there is a current piece
         {highlightArea = currentPiece.strideArea();                    // begin by highlighting its stride area
          highlightArea.intersect(myArea);                              // that is in my control

         for (Piece inspected : pieces)                           //   For each enemy piece
            if (!inspected.myside & highlightArea.contains(inspected.pos.x,inspected.pos.y))  // that sits in this area
                inspected.underAttack=true;                              //set the underAttack flag


          for (Piece inspected : pieces)                           //   For each piece except the currentPiece
               if (inspected != currentPiece) highlightArea.subtract(inspected.avoidArea(currentPiece)); // subtract the avoidArea.
         }
       else highlightArea.reset();                                        // no current piece: higlightArea is null
                timeHighlight = (int)(System.currentTimeMillis() - startTime);
       generateBoardGraphics(currentPiece);                             // this always prompts a regeneration of board graphics

      }



     }// end class Board
