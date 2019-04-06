/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.android.project.proguard;
import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.CharStream;
import org.netbeans.spi.lexer.LexerInput;
/**
 *
 * @author arsi
 */
public class AntlrCharStream implements CharStream {

    private class CharStreamState {

        int index;
        int line;
        int charPositionInLine;
    }

    private int line = 1;
    private int charPositionInLine = 0;
    private LexerInput input;
    private String name;
    private int index = 0;
    private List<CharStreamState> markers;
    private int markDepth = 0;
    private int lastMarker;

    public AntlrCharStream(LexerInput input, String name) {
        this.input = input;
        this.name = name;
    }

    public String substring(int start, int stop) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int LT(int i) {
        return LA(i);
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setCharPositionInLine(int pos) {
        this.charPositionInLine = pos;
    }

    public int getCharPositionInLine() {
        return charPositionInLine;
    }

    public void consume() {
        int c = input.read();
        index++;
        charPositionInLine++;

        if (c == '\n') {
            line++;
            charPositionInLine = 0;
        }
    }

    public int LA(int i) {
        if (i == 0) {
            return 0; // undefined
        }

        int c = 0;
        for (int j = 0; j < i; j++) {
            c = read();
        }
        backup(i);
        return c;
    }

    public int mark() {
        if (markers == null) {
            markers = new ArrayList<CharStreamState>();
            markers.add(null); // depth 0 means no backtracking, leave blank
        }
        markDepth++;
        CharStreamState state = null;
        if (markDepth >= markers.size()) {
            state = new CharStreamState();
            markers.add(state);
        } else {
            state = (CharStreamState) markers.get(markDepth);
        }
        state.index = index;
        state.line = line;
        state.charPositionInLine = charPositionInLine;
        lastMarker = markDepth;

        return markDepth;
    }

    public void rewind() {
        rewind(lastMarker);
    }

    public void rewind(int marker) {
        CharStreamState state = (CharStreamState) markers.get(marker);
        // restore stream state
        seek(state.index);
        line = state.line;
        charPositionInLine = state.charPositionInLine;
        release(marker);
    }

    public void release(int marker) {
        // unwind any other markers made after m and release m
        markDepth = marker;
        // release this marker
        markDepth--;
    }

    public void seek(int index) {
        if (index < this.index) {
            backup(this.index - index);
            this.index = index; // just jump; don't update stream state (line, ...)
            return;
        }

        // seek forward, consume until p hits index
        while (this.index < index) {
            consume();
        }
    }

    public int index() {
        return index;
    }

    public int size() {
        return -1; //unknown...
    }

    public String getSourceName() {
        return name;
    }

    private int read() {
        int result = input.read();
        if (result == LexerInput.EOF) {
            result = CharStream.EOF;
        }

        return result;
    }

    private void backup(int count) {
        input.backup(count);
    }
}
