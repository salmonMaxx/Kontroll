/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kontroll;
//---------------------------------------------------------------------------------
// Message is just a wrapper for data being sent.

import java.io.Serializable;

//   It is used by both Player and Replayer.
//---------------------------------------------------------------------------------

     class Message implements Serializable
   {
      enum MessageType {ACK, MOVE, BUILD, QUIT, EXPIRED};   // The different types of messages

      MessageType  messageType;      // type of message
      int time;             // time it is sent (seconds)
      int number;           // message sequence number
      Piece.Type pt;          // type of piece in a build
      Board.Position s;     // source position of a move
      Board.Position p;     // destination position of move, or position of build
      boolean white;        // color of sender (only relevant for recordings)
      String text;          // other info, such as reasons for quits

   // The constructor just sets the fields from the parameters

    public Message(MessageType mt,  int ti, int nu, Board.Position s, Board.Position p, Piece.Type pt,  boolean w, String te)
    { messageType = mt;
      time = ti;
      number = nu;
      this.s = s;
      this.p = p;
      this.pt = pt;
      this.white = w;
      text = te;
    }
   }