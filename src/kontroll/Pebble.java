/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;

//********************************************************************

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

//
// Pebble
//
//********************************************************************

class Pebble extends Piece
{
    final private  int OSIZE = board.SIZE/3;  // Size of the Pebble

    final private Keep myKeep = new Keep(true, true, null, board);   // A Keep, just to be able to show an outline when promoting a rubble

   //-------------------------------------------------------------------
   // Construct a pebble: construct a Piece, set its type to pebble, and decrease remaining pebbles
   //-------------------------------------------------------------------

      Pebble(boolean w, boolean my, Board.Position pos, Board board)
    {
      super(w,my,pos,board);
      MAXIMUM=8;
      extent = OSIZE/2;
      if (board.isLegal(pos)) board.decreaseRemaining(w);             // decrease remaining pebbles if on the board
    }                                                                 // (ow it is a selector)


   //---------------------------------------------------------------------
   // Appearance
   //---------------------------------------------------------------------

   Area appearance(Board.Position pos)                      // Appearance is just a circle
    {return new Area(new Ellipse2D.Float(pos.x-OSIZE/2,
                                pos.y-OSIZE/2,
                                OSIZE,
                                OSIZE));}



   //---------------------------------------------------------------------
   // Determine conditions for showing selector pebble
   //---------------------------------------------------------------------

    boolean specificSelector()

     {return  board.howManyOnBoard(Type.RUBBLE) > 0                     // A rubble on the board
              && board.remaining(board.iPlayWhite)  > 0;              // and pebbles remaining to put into play

     }


    //---------------------------------------------------------------------
    // Determine conditions for building a pebble
    //---------------------------------------------------------------------
    @Override
    boolean canBuild(Board.Position p)
    {
       return board.canBuildPebbleOn(p);}  // Builds possible on buildable area


    //----------------------------------------------------------------------
    // Effects of moving a pebble (overrides Piece.moveTo because this can create a keep
    //----------------------------------------------------------------------

    @Override
    Piece moveTo(Board.Position pos)
        {Piece dest = board.find(pos);
         if (dest == null || dest.pieceType != Type.RUBBLE || dest.myside != myside)       // An ordinary move
             return super.moveTo(pos);                                                   // is just as done by super
         else {board.removePiece(dest);                                                  // but a keep-building move
               board.removePiece(this);                                                  // means removing both pebble and rubble
               Piece res = board.newPiece(Type.KEEP,whiteside,myside,pos);                 // and inserting a new keep
               moveTime = res.buildTime;                                                 // and this takes as long as to build a keep!
               return res;                                                               // (no need to reset moveTimet since this pebble disappears)
               }
         }



    @Override
    boolean canMove(Board.Position p)
    {outline = this;                                    // the outline is only changed by canKeep
     return super.canMove(p) || canKeep(p);}            // Either an ordinary move or a keep build


    //---------------------------------------------------------------------
    // Determine conditions for building a keep out of a pebble
    //---------------------------------------------------------------------

    boolean canKeep(Board.Position p)
    {
        if (board.isLite) return false;                                                   // In a lite game you can never build a Keep
        else {Piece inhabitant = board.find(p);
              boolean res =                                                             // Ow you can construct a Keep
                      (
                                        board.isLegal(p)                                // to a place on the board
                                     && inhabitant != null                              // which is inhabited
                                     && inhabitant.pieceType == Type.RUBBLE               // by a rubble
                                     && inhabitant.myside == myside                     // on our side
                                     &&  strideArea.contains(p.x,p.y)                   // not too far away
                                     && board.iControl(p)                  // destination rubble must be controlled
                                     && myKeep.MAXIMUM > board.howManyOnBoard(Type.KEEP)               // not too many keeps already
                      );
              if (res) outline = myKeep;                                                // change outline if I can build a keep
              return res;
             }
      }

}         // End class Pebble