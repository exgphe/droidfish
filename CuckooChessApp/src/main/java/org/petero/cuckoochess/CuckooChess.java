/*
    CuckooChess - A java chess program.
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

package org.petero.cuckoochess;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import org.petero.cuckoochess.databinding.MainBinding;
import org.petero.cuckoochess.databinding.MainContentBinding;

import java.util.ArrayList;
import java.util.List;

import chess.ChessParseError;
import chess.Move;
import chess.Position;
import chess.TextIO;
import guibase.ChessController;
import guibase.GUIInterface;

public class CuckooChess extends Activity implements GUIInterface {
    ChessController ctrl;
    boolean mShowThinking;
    int mTimeLimit;
    boolean playerWhite;
    static final int ttLogSize = 16; // Use 2^ttLogSize hash entries.

    SharedPreferences settings;

    MainContentBinding binding;

    private void readPrefs() {
        mShowThinking = settings.getBoolean("showThinking", false);
        String timeLimitStr = settings.getString("timeLimit", "5000");
        mTimeLimit = Integer.parseInt(timeLimitStr);
        playerWhite = settings.getBoolean("playerWhite", true);
        boolean boardFlipped = settings.getBoolean("boardFlipped", false);
        binding.chessboard.setFlipped(boardFlipped);
        ctrl.setTimeLimit();
        String fontSizeStr = settings.getString("fontSize", "12");
        int fontSize = Integer.parseInt(fontSizeStr);
        binding.status.setTextSize(fontSize);
        binding.moveList.setTextSize(fontSize);
        binding.thinking.setTextSize(fontSize);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            readPrefs();
            ctrl.setHumanWhite(playerWhite);
        });

        binding = ((MainBinding) DataBindingUtil.setContentView(this, R.layout.main)).content;
        binding.status.setFocusable(false);
        binding.moveListScroll.setFocusable(false);
        binding.moveList.setFocusable(false);
        binding.thinking.setFocusable(false);
        ctrl = new ChessController(this);
        ctrl.setThreadStackSize(32768);
        readPrefs();

        Typeface chessFont = Typeface.createFromAsset(getAssets(), "casefont.ttf");
        binding.chessboard.setFont(chessFont);
        binding.chessboard.setFocusable(true);
        binding.chessboard.requestFocus();
        binding.chessboard.setClickable(true);

        ctrl.newGame(playerWhite, ttLogSize, false);
        {
            String fen = "";
            String moves = "";
            String numUndo = "0";
            String tmp;
            if (savedInstanceState != null) {
                tmp = savedInstanceState.getString("startFEN");
                if (tmp != null) fen = tmp;
                tmp = savedInstanceState.getString("moves");
                if (tmp != null) moves = tmp;
                tmp = savedInstanceState.getString("numUndo");
                if (tmp != null) numUndo = tmp;
            } else {
                tmp = settings.getString("startFEN", null);
                if (tmp != null) fen = tmp;
                tmp = settings.getString("moves", null);
                if (tmp != null) moves = tmp;
                tmp = settings.getString("numUndo", null);
                if (tmp != null) numUndo = tmp;
            }
            List<String> posHistStr = new ArrayList<>();
            posHistStr.add(fen);
            posHistStr.add(moves);
            posHistStr.add(numUndo);
            ctrl.setPosHistory(posHistStr);
        }
        ctrl.startGame();

        binding.chessboard.setOnTouchListener((v, event) -> {
            if (ctrl.humansTurn() && (event.getAction() == MotionEvent.ACTION_UP)) {
                int sq = binding.chessboard.eventToSquare(event);
                Move m = binding.chessboard.mousePressed(sq);
                if (m != null) {
                    ctrl.humanMove(m);
                }
                return false;
            }
            return false;
        });

        binding.chessboard.setOnTrackballListener(new ChessBoard.OnTrackballListener() {
            public void onTrackballEvent(MotionEvent event) {
                if (ctrl.humansTurn()) {
                    Move m = binding.chessboard.handleTrackballEvent(event);
                    if (m != null) {
                        ctrl.humanMove(m);
                    }
                }
            }
        });
        binding.chessboard.setOnLongClickListener(v -> {
            if (!ctrl.computerThinking())
                showDialog(CLIPBOARD_DIALOG);
            return true;
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        List<String> posHistStr = ctrl.getPosHistory();
        outState.putString("startFEN", posHistStr.get(0));
        outState.putString("moves", posHistStr.get(1));
        outState.putString("numUndo", posHistStr.get(2));
    }

    @Override
    protected void onPause() {
        List<String> posHistStr = ctrl.getPosHistory();
        Editor editor = settings.edit();
        editor.putString("startFEN", posHistStr.get(0));
        editor.putString("moves", posHistStr.get(1));
        editor.putString("numUndo", posHistStr.get(2));
        editor.apply();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ctrl.stopComputerThinking();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_new_game:
                ctrl.newGame(playerWhite, ttLogSize, false);
                ctrl.startGame();
                return true;
            case R.id.item_undo:
                ctrl.takeBackMove();
                return true;
            case R.id.item_redo:
                ctrl.redoMove();
                return true;
            case R.id.item_settings: {
                Intent i = new Intent(CuckooChess.this, Preferences.class);
                startActivityForResult(i, 0);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            readPrefs();
            ctrl.setHumanWhite(playerWhite);
        }
    }

    @Override
    public void setPosition(Position pos) {
        binding.chessboard.setPosition(pos);
        ctrl.setHumanWhite(playerWhite);
    }

    @Override
    public void setSelection(int sq) {
        binding.chessboard.setSelection(sq);
    }

    @Override
    public void setStatusString(String str) {
        binding.status.setText(str);
    }

    @Override
    public void setMoveListString(String str) {
        binding.moveList.setText(str);
        binding.moveListScroll.fullScroll(ScrollView.FOCUS_DOWN);
    }

    @Override
    public void setThinkingString(String str) {
        binding.thinking.setText(str);
    }

    @Override
    public int timeLimit() {
        return mTimeLimit;
    }

    @Override
    public boolean randomMode() {
        return mTimeLimit == -1;
    }

    @Override
    public boolean showThinking() {
        return mShowThinking;
    }

    static final int PROMOTE_DIALOG = 0;
    static final int CLIPBOARD_DIALOG = 1;

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROMOTE_DIALOG: {
                final CharSequence[] items = {"Queen", "Rook", "Bishop", "Knight"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Promote pawn to?");
                builder.setItems(items, (dialog, item) -> ctrl.reportPromotePiece(item));
                return builder.create();
            }
            case CLIPBOARD_DIALOG: {
                final CharSequence[] items = {"Copy Game", "Copy Position", "Paste"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Clipboard");
                builder.setItems(items, (dialog, item) -> {
                    switch (item) {
                        case 0: {
                            String pgn = ctrl.getPGN();
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            clipboard.setText(pgn);
                            break;
                        }
                        case 1: {
                            String fen = ctrl.getFEN() + "\n";
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            clipboard.setText(fen);
                            break;
                        }
                        case 2: {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            if (clipboard.hasText()) {
                                String fenPgn = clipboard.getText().toString();
                                try {
                                    ctrl.setFENOrPGN(fenPgn);
                                } catch (ChessParseError e) {
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            break;
                        }
                    }
                });
                return builder.create();
            }
        }
        return null;
    }

    @Override
    public void requestPromotePiece() {
        runOnUIThread(() -> showDialog(PROMOTE_DIALOG));
    }

    @Override
    public void reportInvalidMove(Move m) {
        String msg = String.format("Invalid move %s-%s", TextIO.squareToString(m.from), TextIO.squareToString(m.to));
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void runOnUIThread(Runnable runnable) {
        runOnUiThread(runnable);
    }
}
