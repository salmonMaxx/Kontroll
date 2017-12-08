/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;

//*******************************************************************

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

//
// A piece
//
//*******************************************************************

abstract class Piece

{
    // ----------- The following parameters uniquely determine the piece type. Change them when devising new pieces.

  Type   pieceType;                    // Type of piece
  int    range;                        // Range  (how far control extends)
  int    currentRange;                 // range at this position (may be increased by a hill)
  int    power;                        // Power  (how much control)
  int    stride;                       // stride (how far it can move)
  int    extent;                       // extent (how much space it occupies )
  int    moveTime;                      // delay after piece moved (units of Nimbler move)
  int    buildTime;                     // delay after piece built
  int    MAXIMUM;                       // Max number of pieces allowed simultaneously on the board of a type

  Area   rangeArea,                     // Area that piece controls
         strideArea,                    // Area it can move to
         extentArea,                    // Area it occupies (used to determine when you click on the piece)
         nearArea,                      // only for rubbles, used to determine buildable area
         avoidBuildArea;                // Area too close to permit builds
  boolean moveable;                     // true if can move

  // ------------ Fields used for game state---------------------------------------------------------------------------

  boolean whiteside;                  // true if white
  boolean myside;                     // true if my, false if opponent's

  Board.Position pos;                 // where it sits on the board
  boolean underAttack;                // true if it is under enemy control

  Board  board;                        // the board where the piece sits

  Piece outline = this;                // The outline to show when dragging it (usually itself)

  private    int   nPoly;                        // graphics for range areas etc
  private    float[] xPoly;
  private    float[] yPoly;



  final static private Color WHITE_NEUTRAL = new Color(210,210,210);     // Various colors of pieces when not controlled by owner
  final static private Color WHITE_OPPONENT = new Color (180,210,250);
  final static private Color BLACK_NEUTRAL = new Color (80,80,80);
  final static private Color BLACK_OPPONENT = new Color (130,80,80);

  enum Type
      {PEBBLE(0), RUBBLE(1), KEEP(2), BOUNCER(3), QUORUM(4), NIMBLER(5), TRIPPLE_DUDE(6);          // The six kinds of pieces
       int index;                                                       // each kind has an integer index
       Type(int idx) {
           index = idx;}
    }


  static Piece  newPiece(Type pt, boolean w, boolean my, Board.Position p, Board b)              // create new piece of right type
  {
      Piece res = null;           // ow compiler warns it may be undefined
       switch (pt)
          {case PEBBLE:  res= new Pebble( w, my, p, b); break;
           case RUBBLE:  res= new Rubble( w, my, p, b); break;
           case QUORUM:  res= new Quorum( w, my, p, b); break;
           case BOUNCER: res= new Bouncer(w, my, p, b); break;
           case NIMBLER: res= new Nimbler(w, my, p, b); break;
           case KEEP:    res= new Keep(   w, my, p, b); break;
           case TRIPPLE_DUDE: res= new TrippleDude(w, my, p, b); break;
          }
       res.pieceType = pt;
       return res;
}
//--------------------------------------------------------------------
// General Piece constructor
//--------------------------------------------------------------------

   Piece(boolean w, boolean my, Board.Position pos, Board board)
   {
        stride = board.SIZE;               // set default values
        range  = board.SIZE;
        power  = 1;
        extent = board.SIZE/2 ;
        moveable = true;
        moveTime  = 2;
        buildTime = 6;

        whiteside = w;                          // set values from params
        this.pos = pos;
        this.board = board;
        myside = my;

     // Set a polygon to approximate a circle

        if (board.shape == 2) nPoly = 6; else nPoly = 28;   // Determine polygon degree from shape param

        if (board.shape ==2 | board.shape ==3)              // define a regular nPoly-polygon
          {xPoly = new float[nPoly];
           yPoly = new float[nPoly];
           for (int i=0; i<nPoly; i++)
            {xPoly[i] = (float)Math.sin(2*Math.PI*(i+0.5)/nPoly);
             yPoly[i] = (float)Math.cos(2*Math.PI*(i+0.5)/nPoly);
            }

          }

   }

 //----------------------------------------------------------------------------------
 // circArea: generate an area around pos with given radius. if isCirc it is a perfect circle, OW as determined by shape
 //----------------------------------------------------------------------------------

  private Area circArea (int radius, boolean isCirc)
     {
      Shape circ = null;

      // isCirc: generate circle

      if (isCirc) circ = new Ellipse2D.Float((int)(pos.x-radius), (int)(pos.y-radius),
                                (int)(2*radius),(int)(2*radius));

      // shape == 1: generate square

      else if (board.shape == 1) circ = new Rectangle((int)(pos.x-radius/Math.sqrt(2)), (int)(pos.y-radius/Math.sqrt(2)),
                                (int)(radius*Math.sqrt(2)),(int)(radius*Math.sqrt(2)));

      // OW generate nPoly-polygon

      else if (board.shape == 2 | board.shape==3)
      {
       int [] x = new int [nPoly];
       int [] y = new int [nPoly];
       for (int i=0;i<nPoly;i++)
         {x[i] = (int)(pos.x + radius*xPoly[i]);
          y[i] = (int)(pos.y + radius*yPoly[i]);
         }
       circ = new Polygon(x,y,nPoly);
      }

      return new Area(circ);
    }

    //--------------------------------------------------------------------------------
    // Area functions
    //--------------------------------------------------------------------------------

    // setAreas just defines the Area fields, typically after the piece has moved

    void setAreas()

    { currentRange = (int)( range * board.elevation(pos));   // adjust the range: if on a hill multiply with its height
      rangeArea = circArea(currentRange,false);
      rangeArea.intersect(board.boardArea);        // range only on board
      extentArea = circArea(extent, true);
      strideArea = circArea(stride,false);
      avoidBuildArea = circArea(board.SIZE*2/3,true);
      nearArea = circArea(board.SIZE,true);


    }

    // Generate area clones

    Area rangeArea ()
    {return (Area)rangeArea.clone();}

    Area strideArea()
    {return (Area)strideArea.clone();}

    Area nearArea()
    {return (Area)nearArea.clone();}

    // Generate avoidarea: where a piece of type p may not come

    Area avoidArea(Piece p)
    {return circArea(p.extent + extent, true);}




    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //
    //  Graphic display methods
    //
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    abstract Area appearance(Board.Position pos);          // Each piece must define an appearance


    //---------------------------------------------------------------------
    // Draw an outline of a piece type with specified color at specified position
    //---------------------------------------------------------------------


    void drawOutline(Graphics g, Board.Position pos, Color color)
                {Graphics2D page2 = (Graphics2D)g;
                 g.setColor(color);
                 if (color == Color.red) page2.setStroke(new BasicStroke(2));
                 page2.draw(appearance(pos));
                 page2.setStroke(new BasicStroke(1));
                }



    //---------------------------------------------------------------------
    // Draw a piece type with specified color, including a bounding outline of opposite color
    //---------------------------------------------------------------------

     void draw(Graphics g,  Board.Position pos, Color color)
                {Graphics2D page2 = (Graphics2D)g;
                 g.setColor(color);
                 page2.fill(appearance(pos));
                  drawOutline(g, pos, flipColor(color));
                }




    //--------------------------------------------------------------
    //  overloaded methods for drawing this instance of a piece.
    //  only draw it if it is on a visible square or outside the board.
    //--------------------------------------------------------------

    void draw(Graphics g)     // Determine the correct color and then draw the piece

    {   boolean whitecontrolled =    (whiteside == myside) ? board.iControl(this) :  board.opponentControls(this);
        boolean blackcontrolled =    (whiteside != myside) ? board.iControl(this) :  board.opponentControls(this);

        Color color = whiteside ?   (whitecontrolled ? Color.white : (blackcontrolled ? WHITE_OPPONENT : WHITE_NEUTRAL))
                                :   (blackcontrolled ? Color.black : (whitecontrolled ? BLACK_OPPONENT : BLACK_NEUTRAL));
        if (underAttack)
             color = whitecontrolled ? Board.WHITEHIGHLIGHT : Board.BLACKHIGHLIGHT;
             draw(g,  pos, color);}


    //-----------------------------------------------------------------

    void draw(Graphics g, Color altcolor)   // Draw the piece with a specified color
    {
                draw(g, pos, altcolor);
    }

    void drawOutline(Graphics g, Color altcolor)  // Draw an outline with specified color
    {
                drawOutline(g,  pos, altcolor);
    }

    //-----------------------------------------------------------------

    private Color flipColor(Color color)             // used to determine border of filled piece
         {return (whiteside & !(color == Color.black) ? Color.black : Color.white);}


    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //
    //   Other utility methods
    //
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    //---------------------------------------------------------------------
    // Determine when a selector should be shown (this is refined by subclasses)
    //---------------------------------------------------------------------

     boolean shouldShowSelector ()
     {return (MAXIMUM > board.howManyOnBoard(pieceType) && specificSelector());}

     abstract boolean specificSelector();     // a subclass should define this to refine shouldShowSelector

    //------------------------------------------------------------------------------
    // Default canBuild, determines if a piece of this type can be built at position p.
    //   refined by subclasses. side = true means it is me who builds, ow opponent builds
    //-------------------------------------------------------------------------------

    boolean canBuild(Board.Position p)
    { return (board.isLegal(p)                                    // must be on the board
              &&  board.iControl(p)                               // on a controlled square
              && MAXIMUM > board.howManyOnBoard(pieceType));      // and not exceeding max allowed of piece type

    }


    //-------------------------------------------------------------------------------
    // Default canMove, determines if this piece can move to p
    //-------------------------------------------------------------------------------

    boolean canMove (Board.Position p)
    {   if (p != null && board.canMoveTo(p))              // Can only move to highlighted area
           return !p.equals(pos);                         // but not to the same position!
        else
        {Piece inhabitant = board.find(p);                // or to enemy piece under attack
         return inhabitant != null && inhabitant.myside != myside && inhabitant.underAttack;
        }


    }

    //--------------------------------------------------------------
    // Move the piece to another place on the board
    //--------------------------------------------------------------

     Piece moveTo(Board.Position pos)
     {Piece victim = board.find(pos);                 // check if there is a victim on the destination position
      if (victim != null) board.removePiece(victim);  // if so remove it
       this.pos = pos;                               // then adjust the position of the piece
       setAreas();                                  // and recalculate its areas
       return this;
     }



    //--------------------------------------------------------------
    //  Check if a particular position is near the piece
    //--------------------------------------------------------------

    boolean near (Board.Position pos)
     {return  nearArea.contains(pos.x,pos.y);}


    //--------------------------------------------------------------
    //  Check if a particular position is on the piece
    //--------------------------------------------------------------

    boolean on (Board.Position pos)                                 // If the position is closer than the extent of the piece
    {return extentArea.contains(pos.x,pos.y);}

}        // End class Piece
