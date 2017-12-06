/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;


//*******************************************************************
//
// Bouncer
//
//*******************************************************************

class Bouncer extends HeavyPiece
{
   final private int OSIZE =  board.SIZE/3;       // various constants for the graphic representation, in pixels
   final private int BWIDTH =(8 * board.SIZE) / 60 ;
   final private int BSIZE =  board.SIZE*3/4;




    //--------------------------------------------------------------------
    // Bouncer constructor
    //--------------------------------------------------------------------

    Bouncer(boolean w, boolean my, Board.Position pos, Board board)
    {
        super(w,my,pos,board);       // construct a piece
        range =2*board.SIZE;         // with range 2
        extent = board.SIZE/3;        // The bouncer is large!
        }

    //---------------------------------------------------------------------
    // Appearance
    //---------------------------------------------------------------------

     Area appearance(Board.Position pos)
        {   Area app =
                      new Area(new Ellipse2D.Float(pos.x-OSIZE/2,           // Appearance is a circle plus two polygons
                                                   pos.y-OSIZE/2,
                                                   OSIZE,
                                                   OSIZE));
             int[] Bx1 = {pos.x-BSIZE/2,  pos.x+BSIZE/2 - BWIDTH/2,  pos.x+BSIZE/2,  pos.x-BSIZE/2 + BWIDTH/2};
             int[] By1 = {pos.y-BSIZE/2 + BWIDTH/2,  pos.y+BSIZE/2,  pos.y+BSIZE/2 - BWIDTH/2,  pos.y-BSIZE/2};
             app.add(new Area (new Polygon(Bx1,By1,4)));
             int [] Bx2 = {pos.x+BSIZE/2 - BWIDTH/2,  pos.x-BSIZE/2, pos.x-BSIZE/2 + BWIDTH/2,  pos.x+BSIZE/2};
             int [] By2 = {pos.y-BSIZE/2,  pos.y+BSIZE/2 - BWIDTH/2,  pos.y+BSIZE/2, pos.y-BSIZE/2 + BWIDTH/2};
             app.add(new Area (new Polygon(Bx2,By2,4)));
             return app;
     }

}    // End class Bouncer