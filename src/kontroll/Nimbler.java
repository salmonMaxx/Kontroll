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
// Nimbler
//
//*******************************************************************

class Nimbler extends HeavyPiece
{

    final private int NxSIZE = (board.SIZE * 5)/60;                 // graphic size
    final private int NySIZE = (board.SIZE * 20)/60;
    final private int OSIZE =  (board.SIZE * 15)/60;

    final private int[] Nx = {0,  NxSIZE, -NxSIZE, 0, NxSIZE, -NxSIZE};  // Nimbler polygon shape
    final private int[] Ny = {0, -NySIZE, -NySIZE, 0, NySIZE,  NySIZE};

    private int[] Nxx = new int[6];  // temps
    private int[] Nyy = new int[6];




    //--------------------------------------------------------------------
    // Nimbler constructor
    //--------------------------------------------------------------------

     Nimbler(boolean w, boolean my, Board.Position pos, Board board)
    {
        super(w,my,pos, board);          // construct a piece
        stride = board.SIZE*2;           // with stride 2
        moveTime = 1;                    // with a short move time
        extent = NySIZE;
        }

    //---------------------------------------------------------------------
    // Appearance
    //---------------------------------------------------------------------
     Area appearance(Board.Position pos)
        {Area app = new Area(new Ellipse2D.Float(pos.x-OSIZE/2,
                                    pos.y-OSIZE/2,
                                    OSIZE,
                                    OSIZE));
           for (int i=0; i<=5; i++) {Nxx[i] = Nx[i] + pos.x; Nyy[i] = Ny[i] + pos.y;}
           app.add (new Area(new Polygon(Nxx, Nyy, 6)));
           return app;

     }



}  // End class Nimbler
