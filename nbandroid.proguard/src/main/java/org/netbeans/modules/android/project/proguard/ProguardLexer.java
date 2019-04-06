/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.android.project.proguard;

import org.antlr.runtime.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author arsi
 */
public class ProguardLexer implements Lexer<ProguardTokenId> {

    private LexerRestartInfo<ProguardTokenId> info;

    private ProguardLexerAntlr oracleLexer;

    public ProguardLexer(LexerRestartInfo<ProguardTokenId> info) {
        this.info = info;

        AntlrCharStream charStream = new AntlrCharStream(info.input(), "ProguardEditor");
        oracleLexer = new ProguardLexerAntlr(charStream);
    }

    @Override
    public org.netbeans.api.lexer.Token<ProguardTokenId> nextToken() {
        Token token = oracleLexer.nextToken();
        if (token.getType() != ProguardLexerAntlr.EOF) {
            ProguardTokenId tokenId = ProguardLanguageHierarchy.getToken(token.getType());
            return info.tokenFactory().createToken(tokenId);
        }
        return null;
    }

    public Object state() {
        return null;
    }

    public void release() {
    }
}
