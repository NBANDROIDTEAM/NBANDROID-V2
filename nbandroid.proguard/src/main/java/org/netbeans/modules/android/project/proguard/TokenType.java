/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.android.project.proguard;

/**
 *
 * @author arsi
 */
public enum TokenType {
    LINE_COMMENT(4, "comment"),
    NAME(5, "character"),
    NEGATOR(6, "character"),
    WS(7, "character"),
    PERCENTAGE(8, "character"),
    BRACES_LEFT(9, "character"),
    BRACES_RIGHT(10, "character"),
    COMMA(11, "character"),
    ADAPTCLASSSTRINGS(12, "keyword"),
    ADAPTRESOURCEFILECONTENTS(13, "keyword"),
    ADAPTRESOURCEFILENAMES(14, "keyword"),
    ALLOWACCESSMODIFICATION(15, "keyword"),
    APPLYMAPPING(16, "keyword"),
    ASSUMENOSIDEEFFECTS(17, "keyword"),
    BASEDIRECTORY(18, "keyword"),
    CLASSOBFUSCATIONDICTIONARY(19, "keyword"),
    DONTNOTE(20, "keyword"),
    DONTOBFUSCATE(21, "keyword"),
    DONTOPTIMIZE(22, "keyword"),
    DONTPREVERIFY(23, "keyword"),
    DONTSHRINK(24, "keyword"),
    DONTSKIPNONPUBLICLIBRARYCLASSES(25, "keyword"),
    DONTSKIPNONPUBLICLIBRARYCLASSMEMBERS(26, "keyword"),
    DONTUSEMIXEDCASECLASSNAMES(27, "keyword"),
    DONTWARN(28, "keyword"),
    DUMP(29, "keyword"),
    FLATTENPACKAGEHIERARCHY(30, "keyword"),
    FORCEPROCESSING(31, "keyword"),
    IGNOREWARNINGS(32, "keyword"),
    INCLUDE(33, "keyword"),
    INJARS(34, "keyword"),
    KEEP(35, "keyword"),
    KEEPATTRIBUTES(36, "keyword"),
    KEEPCLASSESWITHMEMBERNAMES(37, "keyword"),
    KEEPCLASSESWITHMEMBERS(38, "keyword"),
    KEEPCLASSMEMBERNAMES(39, "keyword"),
    KEEPCLASSMEMBERS(40, "keyword"),
    KEEPDIRECTORIES(41, "keyword"),
    KEEPNAMES(42, "keyword"),
    KEEPPACKAGENAMES(43, "keyword"),
    KEEPPARAMETERNAMES(44, "keyword"),
    LIBRARYJARS(45, "keyword"),
    MERGEINTERFACESAGGRESSIVELY(46, "keyword"),
    MICROEDITION(47, "keyword"),
    OBFUSCATIONDICTIONARY(48, "keyword"),
    OPTIMIZATIONPASSES(49, "keyword"),
    OPTIMIZATIONS(50, "keyword"),
    OUTJARS(51, "keyword"),
    OVERLOADAGGRESSIVELY(52, "keyword"),
    PACKAGEOBFUSCATIONDICTIONARY(53, "keyword"),
    PRINTCONFIGURATION(54, "keyword"),
    PRINTMAPPING(55, "keyword"),
    PRINTSEEDS(56, "keyword"),
    PRINTUSAGE(57, "keyword"),
    RENAMESOURCEFILEATTRIBUTE(58, "keyword"),
    REPACKAGECLASSES(59, "keyword"),
    SKIPNONPUBLICLIBRARYCLASSES(60, "keyword"),
    TARGET(61, "keyword"),
    USEUNIQUECLASSMEMBERNAMES(62, "keyword"),
    VERBOSE(63, "keyword"),
    WHYAREYOUKEEPING(64, "keyword"),
    COLON(65, "character"),
    SEMICOLON(66, "character"),
    FIELDS(67, "keyword"),
    INIT(68, "keyword"),
    METHODS(69, "keyword"),
    AT(70, "character"),
    EMPTY_BRACES(71, "character"),
    BACKSLASH(72, "character"),
    ABSTRACT(73, "keyword"),
    ALLOWOBFUSCATION(74, "keyword"),
    ALLOWOPTIMIZATION(75, "keyword"),
    ALLOWSHRINKING(76, "keyword"),
    BRIDGE(77, "keyword"),
    CLASS(78, "keyword"),
    ENUM(79, "keyword"),
    EXTENDS(80, "keyword"),
    FINAL(81, "keyword"),
    IMPLEMENTS(82, "keyword"),
    INTERFACE(83, "keyword"),
    NATIVE(84, "keyword"),
    PRIVATE(85, "keyword"),
    PROTECTED(86, "keyword"),
    PUBLIC(87, "keyword"),
    STATIC(88, "keyword"),
    STRICTFP(89, "keyword"),
    SYNCHRONIZED(90, "keyword"),
    SYNTHETIC(91, "keyword"),
    TRANSIENT(92, "keyword"),
    VARARGS(93, "keyword"),
    VOLATILE(94, "keyword"),
    COMPOUND_BRACES_LEFT(95, "character"),
    COMPOUND_BRACES_RIGHT(96, "character");
    public int id;
    public String category;
    public String text;

    private TokenType(int id, String category) {
        this.id = id;
        this.category = category;
    }

    public static TokenType valueOf(int id) {
        TokenType[] values = values();
        for (TokenType value : values) {
            if (value.id == id) {
                return value;
            }
        }
        throw new IllegalArgumentException("The id " + id + " is not recognized");
    }
}
