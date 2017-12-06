/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;

//*******************************************************************
//
// Heavy Piece: What is common for Quorum, Bouncer and Nimbler
//
//*******************************************************************

 abstract class HeavyPiece extends Piece
 {

     HeavyPiece(boolean w, boolean my, Board.Position pos, Board board)
            {super(w,my,pos,board);       // construct a piece
             MAXIMUM =2;                  // Heavy pieces have maximum = 2

    }

    //---------------------------------------------------------------------
    // Determine when heavy piece selector should be shown
    //---------------------------------------------------------------------

    boolean specificSelector()
          {return board.howManyOnBoard(Type.KEEP) > 0 && board.howManyOnBoard(Type.PEBBLE) > 0;}   // same for all heavy pieces


    //---------------------------------------------------------------------
    // Determine when heavy piece can be built
    //---------------------------------------------------------------------

    @Override
    boolean canBuild(Board.Position pos)
    {   boolean res;
        Piece inhabitant = board.find(pos);
        res = board.near(Type.KEEP, pos)               // near a keep
            && inhabitant != null                 // on a pebble
            && inhabitant.pieceType == Type.PEBBLE;
        return super.canBuild(pos) && res;
    }
}