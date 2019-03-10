/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.android.project.proguard;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;

/**
 *
 * @author arsi
 */
public class ProguardTokenId implements TokenId {

    private static final Language<ProguardTokenId> language = new ProguardLanguageHierarchy().language();
    private final String name;
    private final String primaryCategory;
    private final int id;

    public ProguardTokenId(String name, String primaryCategory, int id) {
        this.name = name;
        this.primaryCategory = primaryCategory;
        this.id = id;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    @Override
    public int ordinal() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    public static final Language<ProguardTokenId> getLanguage() {
        return language;
    }
}
