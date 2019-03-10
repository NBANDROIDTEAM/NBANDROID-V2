/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.android.project.proguard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author arsi
 */
public class ProguardLanguageHierarchy extends LanguageHierarchy<ProguardTokenId> {

    private static List<ProguardTokenId> tokens = new ArrayList<ProguardTokenId>();
    private static Map<Integer, ProguardTokenId> idToToken = new HashMap<Integer, ProguardTokenId>();

    static {
        TokenType[] tokenTypes = TokenType.values();
        for (TokenType tokenType : tokenTypes) {
            tokens.add(new ProguardTokenId(tokenType.name(), tokenType.category, tokenType.id));
        }
        for (ProguardTokenId token : tokens) {
            idToToken.put(token.ordinal(), token);
        }
    }

    static synchronized ProguardTokenId getToken(int id) {
        return idToToken.get(id);
    }

    @Override
    protected synchronized Collection<ProguardTokenId> createTokenIds() {
        return tokens;
    }


    @Override
    protected String mimeType() {
        return "text/x-proguard";
    }

    @Override
    protected org.netbeans.spi.lexer.Lexer<ProguardTokenId> createLexer(LexerRestartInfo<ProguardTokenId> lri) {
        return new ProguardLexer(lri);
    }
}
