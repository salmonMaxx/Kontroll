/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;

//*******************************************************************

import java.awt.Polygon;
import java.awt.geom.Area;

//
// Keep
//
//*******************************************************************

class Keep extends Piece
    {
      final private int BSIZE = board.SIZE;
      final private int KM =  (BSIZE * 10) / 60;           // sizing constants for graphics
      final private int KW =  (BSIZE * 15) / 60;


                                        // the keep polygon shape

      final private int[] Kx = {KM, BSIZE/2-KW/2, BSIZE/2-KW/2, BSIZE/2+KW/2, BSIZE/2+KW/2, BSIZE-KM,
                         BSIZE-KM, BSIZE/2+KW/2, BSIZE/2+KW/2, BSIZE/2-KW/2, BSIZE/2-KW/2, KM};
      final private int[] Ky =   {BSIZE/2-KW/2, BSIZE/2-KW/2, KM, KM, BSIZE/2-KW/2, BSIZE/2-KW/2,
                         BSIZE/2+KW/2, BSIZE/2+KW/2, BSIZE-KM, BSIZE-KM,BSIZE/2+KW/2, BSIZE/2+KW/2};
      final private int Klength = 12;

      private int kx[] = new int[12];       // temps when drawing
      private int ky[] = new int[12];


    //--------------------------------------------------------------------
    // Keep constructor
    //--------------------------------------------------------------------

     Keep(boolean w, boolean my, Board.Position pos, Board board)
        {
        super(w,my,pos,board);         // construct a piece
        moveable = false;             // not moveable
        stride = 0;
        extent = board.SIZE/3;
        MAXIMUM = 2;
        moveTime = 8;               // delay of a build (because pebble creates a keep through a move)
        }

    //---------------------------------------------------------------------
    // Appearance
    //---------------------------------------------------------------------

       Area appearance(Board.Position pos)
        {for (int i=0; i<Klength; i++)
          {kx[i] = Kx[i] +  pos.x - BSIZE/2;
            ky[i] =Ky[i] +  pos.y - BSIZE/2;}
         return new Area (new Polygon(kx,ky, Klength));
        }


    //---------------------------------------------------------------------
    // Never show selector
    //---------------------------------------------------------------------

    boolean specificSelector()
      {return  false;
      }

    //---------------------------------------------------------------------
    // Never build it (because it is created by a pebble move)
    //---------------------------------------------------------------------

    @Override
    boolean canBuild(Board.Position p)
    {   return false;
    }

}    // End class Keep