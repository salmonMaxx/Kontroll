/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 *
 * @author maxxw
 */
public class TrippleDude extends HeavyPiece{
    int OSIZE = board.SIZE / 3;
    TrippleDude(boolean w, boolean my, Board.Position pos, Board board){
        super(w,my,pos,board);
        power = 3;
        extent = 3*OSIZE/4;
    }
    
    @Override
    boolean canBuild(Board.Position p){
        return super.canBuild(pos) && board.relativeNumberOfControlledSquares < 0;
    }    
    //trippel kontroll och kan byggas om man ligger under
        @Override
        Area appearance(Board.Position pos) {
            Area app = new Area(new Ellipse2D.Float(pos.x-OSIZE/3,
                                    pos.y-OSIZE/2+OSIZE/4,
                                    OSIZE,
                                    OSIZE));
            app.add(new Area(new Ellipse2D.Float(pos.x-OSIZE/3,
                                   pos.y-OSIZE/2-OSIZE/4,
                                   OSIZE,
                                   OSIZE)));
            app.add(new Area(new Ellipse2D.Float(pos.x-OSIZE,
                                   pos.y-(OSIZE/2+OSIZE/4)/2,
                                   OSIZE,
                                   OSIZE)));
         return app;
        }
        
//        @Override
//        protected void drawOutline(Graphics page, Board.Position pos, Color color){
//            super.drawOutline(page,  pos,  color);
//            page.drawOval(pos.x-OSIZE/3,
//                                       pos.y-OSIZE/2-OSIZE/4,
//                                       OSIZE,
//                                       OSIZE);
//            page.drawOval(pos.x-OSIZE,
//                                   pos.y-(OSIZE/2+OSIZE/4)/2,
//                                   OSIZE,
//                                   OSIZE);
//            page.fillOval(pos.x-OSIZE/3,
//                                   pos.y-OSIZE/2-OSIZE/4,
//                                   OSIZE,
//                                   OSIZE);
//    }
}

    
