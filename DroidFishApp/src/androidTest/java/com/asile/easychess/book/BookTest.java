/*
    DroidFish - An Android chess program.
    Copyright (C) 2011  Peter Österlund, peterosterlund2@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.asile.easychess.book;


import java.util.ArrayList;

import junit.framework.TestCase;

import com.asile.easychess.book.DroidBook;
import com.asile.easychess.book.IOpeningBook.BookPosInput;
import com.asile.easychess.gamelogic.ChessParseError;
import com.asile.easychess.gamelogic.Move;
import com.asile.easychess.gamelogic.MoveGen;
import com.asile.easychess.gamelogic.Position;
import com.asile.easychess.gamelogic.TextIO;

public class BookTest extends TestCase {

    public BookTest() {
    }

    public void testGetBookMove() throws ChessParseError {
        Position pos = TextIO.readFEN(TextIO.startPosFEN);
        DroidBook book = DroidBook.getInstance();
        BookPosInput posInput = new BookPosInput(pos, null, null);
        Move move = book.getBookMove(posInput);
        checkValid(pos, move);

        // Test "out of book" condition
        pos.setCastleMask(0);
        move = book.getBookMove(posInput);
        assertEquals(null, move);
    }

    public void testGetAllBookMoves() throws ChessParseError {
        Position pos = TextIO.readFEN(TextIO.startPosFEN);
        DroidBook book = DroidBook.getInstance();
        BookPosInput posInput = new BookPosInput(pos, null, null);
        ArrayList<Move> moves = book.getAllBookMoves(posInput, false).second;
        assertTrue(moves.size() > 1);
        for (Move m : moves) {
            checkValid(pos, m);
        }
    }

    /** Check that move is a legal move in position pos. */
    private void checkValid(Position pos, Move move) {
        assertTrue(move != null);
        ArrayList<Move> moveList = new MoveGen().legalMoves(pos);
        assertTrue(moveList.contains(move));
    }
}
