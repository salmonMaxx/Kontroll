/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;

import java.io.Serializable;


//---------------------------------------------------------------------------------
// Wrapper for game parameters transmitted between players
//---------------------------------------------------------------------------------


class GameParameters implements Serializable {

       String versionNumber;        // game version number

       // game params chosen in options

       boolean isLite;              // true if controlite, false if control
       boolean withHills;           // with hills on board
       int graphics;                // graphics size
       String nick;                 // nick of sender
       int shape;                   // area shape
       int boardShape;              // board shape
       int speed;                   // game speed
       int width;                   // board relative width
       int height;                  // board relative height
       int pebbles;                 // pebbles per player
       int length;                  // game length

   //---------------- constructor just sets the fields-------------------------------


       public GameParameters(String v, int gr, boolean il, boolean wH, String n, int g,
                             int bS, int s, int w, int h, int p, int l)
           {versionNumber=v; graphics=gr; isLite=il; withHills = wH; nick=n; shape = g;
            boardShape = bS; speed=s; width=w; height=h; pebbles=p; length=l;
           }
    }