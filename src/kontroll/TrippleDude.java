/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;

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
    //trippel kontroll och kan byggas om man ligger under
        @Override
        Area appearance(Board.Position pos) {
            Area app = new Area(new Ellipse2D.Float(pos.x-OSIZE/2,
                                    pos.y-OSIZE/2+OSIZE/4,
                                    OSIZE,
                                    OSIZE));
            app.add(new Area(new Ellipse2D.Float(pos.x-OSIZE/2,
                                   pos.y-OSIZE/2-OSIZE/4,
                                   OSIZE,
                                   OSIZE)));
            app.add(new Area(new Ellipse2D.Float(pos.x-OSIZE/2,
                                   pos.y+OSIZE/2+OSIZE/4,
                                   OSIZE,
                                   OSIZE)));
         return app;
        }
}

    
