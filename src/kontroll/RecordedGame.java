/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;

import java.io.Serializable;


//---------------------------------------------------------------------------------
// Wrapper for game parameters on a recorded game
//---------------------------------------------------------------------------------

  class RecordedGame implements Serializable

      {String versionNumber;        // game version number
       boolean isLite;              // true if controlite, false if control
       boolean withHills;           // with hills on board
       int graphics;                // graphics size
       String whiteNick;            // white's nick
       String blackNick;            // black's nick
       int shape;                   // area shape
       int boardShape;              // board shape
       int width;                   // game params
       int height;                  // board relative height
       int pebbles;                 // pebbles per player
       int length;                  // game length

        //---------------- constructor just sets the fields-------------------------------

       public  RecordedGame (String v, boolean il, boolean wH, int gr, String w, String b,
                             int g, int bS, int wi, int h, int p, int l)
       {versionNumber = v;  isLite=il; withHills = wH; graphics = gr; whiteNick = w; blackNick = b;
        shape = g; boardShape = bS; width = wi; height = h; pebbles = p; length = l;}
      }