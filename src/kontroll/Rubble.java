/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;
//*****************************************************************
//

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

// Rubble
//
//*****************************************************************

 class Rubble extends Piece
{

     final private int RSIZE = (board.SIZE * 25)/60;         // graphical size

    //--------------------------------------------------------------------
    // Rubble constructor
    //--------------------------------------------------------------------

     Rubble (boolean w, boolean my, Board.Position pos, Board board)
    {
        super(w,my,pos, board);       // Construct a piece
        range = (int)board.SIZE/3;    // with very small range
        moveable = false;             // not moveable
        stride = 0;
        MAXIMUM = 2;                  // max 2 allowed on board
        extent = board.SIZE/3;        // large extent because this may become a Keep

        }

    //---------------------------------------------------------------------
    // Appearance
    //---------------------------------------------------------------------

     Area appearance(Board.Position pos)                 // Appearance is a square
        {return new Area(new Rectangle2D.Float(pos.x-RSIZE/2,
                                    pos.y-RSIZE/2,
                                    RSIZE,
                                    RSIZE));}




    //---------------------------------------------------------------------
    // Determine when selector should be shown
    //---------------------------------------------------------------------

    boolean specificSelector()

      {return  board.howManyOnBoard(Type.PEBBLE) > 0;}     // Pebbles on the board

    //---------------------------------------------------------------------
    // Determine when rubble can be built
    //---------------------------------------------------------------------


    @Override
    boolean canBuild(Board.Position p)
    {
        boolean res;
        res = super.canBuild(p)               // Can build piece
        && board.near(Type.PEBBLE, p);    // and near a pebble
        return res;
    }
}     // End class Rubble