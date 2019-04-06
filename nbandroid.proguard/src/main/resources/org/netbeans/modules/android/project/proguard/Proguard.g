grammar Proguard;

options{
  k = 3;
}

tokens {
  NEGATOR = '!';
}

@header {
package com.android.build.gradle.shrinker.parser;
import static org.objectweb.asm.Opcodes.*;
}

@lexer::header {
package com.android.build.gradle.shrinker.parser;
}

@members {
  @Override
  public void emitErrorMessage(String msg) {
    throw new RuntimeException(msg);
  }
}

@lexer::members {
  @Override
  public void emitErrorMessage(String msg) {
    throw new RuntimeException(msg);
  }
}

prog [Flags flags, String baseDirectory]
  :
  (
    ('-basedirectory' baseDir=NAME {baseDirectory=$baseDir.text;})
    | ('-include'|'@') proguardFile=NAME {GrammarActions.include($proguardFile.text, baseDirectory, $flags);}
    | ('-keepclassmembers' keepModifier=keepOptionModifier? classSpec=classSpecification {GrammarActions.addKeepClassMembers($flags, $classSpec.classSpec, $keepModifier.modifier);})
    | ('-keepclasseswithmembers' keepModifier=keepOptionModifier? classSpec=classSpecification {GrammarActions.addKeepClassesWithMembers($flags, $classSpec.classSpec, $keepModifier.modifier);})
    | ('-keep' keepModifier=keepOptionModifier? classSpec=classSpecification {GrammarActions.addKeepClassSpecification($flags, $classSpec.classSpec, $keepModifier.modifier);})
    | (igFlag=ignoredFlag {GrammarActions.ignoredFlag($igFlag.text, true);})
    | (nopFlag=noOpFlag {GrammarActions.ignoredFlag($nopFlag.text, false);})
    | (unFlag=unsupportedFlag {GrammarActions.unsupportedFlag($unFlag.text);})
    | ('-dontwarn' {FilterSpecification class_filter = new FilterSpecification();} filter[class_filter] {GrammarActions.dontWarn($flags, class_filter);})
    | ('-ignorewarnings' {GrammarActions.ignoreWarnings($flags);})
  )*
  EOF
  ;
  catch [RecognitionException e] {
    throw e;
  }

private noOpFlag
  :
  (   '-verbose'
    | ('-dontnote' {FilterSpecification class_filter = new FilterSpecification();} filter[class_filter])
    // These flags are used in the default SDK proguard rules, so there's no point warning about them:
    | '-dontusemixedcaseclassnames'
    | '-dontskipnonpubliclibraryclasses'
    | '-dontskipnonpubliclibraryclassmembers'
    | '-skipnonpubliclibraryclasses'
    // Similar flags as above:
    | '-keepparameternames'
    | ('-keepnames' classSpec=classSpecification )
    | ('-keepclassmembernames' classSpec=classSpecification  )
    | ('-keepclasseswithmembernames' classSpec=classSpecification  )
    | ('-keepattributes' {FilterSpecification attribute_filter = new FilterSpecification();} filter[attribute_filter] )
    | ('-keeppackagenames' {FilterSpecification package_filter = new FilterSpecification();} filter[package_filter] )
    | ('-dontshrink' )
    | ('-dontoptimize'  )
    | ('-dontpreverify'  )
    | ('-dontobfuscate' )
  )
  ;

private ignoredFlag
  :
  (   ('-optimizations' {FilterSpecification optimization_filter = new FilterSpecification();} filter[optimization_filter])
    | '-useuniqueclassmembernames'
    | '-allowaccessmodification'
    | ('-optimizationpasses' NAME) //n
    | ('-assumenosideeffects' classSpecification)
    | '-mergeinterfacesaggressively'
    | '-overloadaggressively'
    | ('-renamesourcefileattribute' sourceFile=NAME?)
    | ('-adaptclassstrings' {FilterSpecification filter = new FilterSpecification();} filter[filter])
    | ('-applymapping' mapping=NAME )
    | '-obfuscationdictionary' obfuscationDictionary=NAME
    | '-classobfuscationdictionary' classObfuscationDictionary=NAME
    | '-packageobfuscationdictionary' packageObfuscationDictionary=NAME
    | ('-repackageclasses' ('\'' newPackage=NAME? '\'')? )
    | ('-flattenpackagehierarchy' ('\'' newPackage=NAME? '\'')? )
    | ('-adaptresourcefilenames' {FilterSpecification file_filter = new FilterSpecification();} filter[file_filter] )
    | ('-adaptresourcefilecontents' {FilterSpecification file_filter = new FilterSpecification();} filter[file_filter] )
  )
  ;

private unsupportedFlag
  :
  ( '-injars' inJars=classpath
    | '-outjars' outJars=classpath
    | '-libraryjars' libraryJars=classpath
    | ('-target' NAME) //version
    | '-forceprocessing'
    | ('-printusage' NAME) //[filename]
    | ('-whyareyoukeeping' classSpecification)
    | '-microedition'
    | ('-printconfiguration' NAME?) //[filename]
    | ('-dump' NAME?) //[filename]
    | '-printmapping' outputMapping=NAME?
    | ('-printseeds' seedOutputFile=NAME? )
    | ('-keepdirectories' {FilterSpecification directory_filter = new FilterSpecification();} filter[directory_filter])
  )
  ;

private classpath
  :  NAME ((':'|';') classpath)?
  ;

private filter [FilterSpecification filter]
  :
  nonEmptytFilter[filter]
  | {GrammarActions.filter($filter, false, "**");}
  ;


private nonEmptytFilter [FilterSpecification filter]
@init {
  boolean negator = false;
}
  :
  ((NEGATOR {negator=true;})? NAME {GrammarActions.filter($filter, negator, $NAME.text);} (',' nonEmptytFilter[filter])?)
  ;

private classSpecification returns [ClassSpecification classSpec]
@init{
  ModifierSpecification modifier = new ModifierSpecification();
  boolean hasNameNegator = false;
}
  :
  (annotation)?
  cType=classModifierAndType[modifier]
  (NEGATOR {hasNameNegator = true;})? NAME {classSpec = GrammarActions.classSpec($NAME.text, hasNameNegator, cType, $annotation.annotSpec, modifier);}
  (inheritanceSpec=inheritance {classSpec.setInheritance(inheritanceSpec);})?
  members[classSpec]?
  ;

private classModifierAndType[ModifierSpecification modifier] returns [ClassTypeSpecification cType]
@init{
  boolean hasNegator = false;
}
  :
  (NEGATOR {hasNegator = true;})?
  (
  'public' {GrammarActions.addModifier(modifier, ACC_PUBLIC, hasNegator);} cmat=classModifierAndType[modifier] {cType = $cmat.cType;}
  | 'abstract' {GrammarActions.addModifier(modifier, ACC_ABSTRACT, hasNegator);} cmat=classModifierAndType[modifier] {cType = $cmat.cType;}
  | 'final' {GrammarActions.addModifier(modifier, ACC_FINAL, hasNegator);} cmat=classModifierAndType[modifier] {cType = $cmat.cType;}
  | classType {cType=GrammarActions.classType($classType.type, hasNegator); }
  )
  ;

private classType returns [int type]
@init {
  $type = 0;
}
  :
  ('@' {$type |= ACC_ANNOTATION;})?
  ('interface' {$type |= ACC_INTERFACE;}
  | 'enum' {$type |= ACC_ENUM;}
  | 'class'
  )
  ;

private members [ClassSpecification classSpec]
  :
  '{'
    member[classSpec]*
  '}'
  ;

private member [ClassSpecification classSpec]
  :
    annotation? modifiers
    (
      (typeSig=type)? name=(NAME|'<init>') (signature=arguments {GrammarActions.method(classSpec, $annotation.annotSpec, typeSig, $name.text, signature, $modifiers.modifiers);}
                  | {GrammarActions.fieldOrAnyMember(classSpec, $annotation.annotSpec, typeSig, $name.text, $modifiers.modifiers);})
      | '<methods>' {GrammarActions.method(classSpec, $annotation.annotSpec,
          GrammarActions.getSignature("***", 0), "*", "("+ GrammarActions.getSignature("...", 0) + ")",
          $modifiers.modifiers);}
      | '<fields>' {GrammarActions.field(classSpec, $annotation.annotSpec, null, "*", $modifiers.modifiers);}
    ) ';'
  ;

private annotation returns [AnnotationSpecification annotSpec]
@init{
  boolean hasNameNegator = false;
}
  :  '@' (NEGATOR {hasNameNegator = true;})? NAME {$annotSpec = GrammarActions.annotation($NAME.text, hasNameNegator);};

private modifiers returns [ModifierSpecification modifiers]
@init{
  modifiers = new ModifierSpecification();
}
  :
  modifier[modifiers]*
  ;

private modifier [ModifierSpecification modifiers]
@init{
  boolean hasNegator = false;
}
  :
  (NEGATOR {hasNegator = true;})?
  (
    'public' {modifiers.addModifier(ACC_PUBLIC, hasNegator);}
    | 'private' {modifiers.addModifier(ACC_PRIVATE, hasNegator);}
    | 'protected' {modifiers.addModifier(ACC_PROTECTED, hasNegator);}
    | 'static' {modifiers.addModifier(ACC_STATIC, hasNegator);}
    | 'synchronized' {modifiers.addModifier(ACC_SYNCHRONIZED, hasNegator);}
    | 'volatile' {modifiers.addModifier(ACC_VOLATILE, hasNegator);}
    | 'native' {modifiers.addModifier(ACC_NATIVE, hasNegator);}
    | 'abstract' {modifiers.addModifier(ACC_ABSTRACT, hasNegator);}
    | 'strictfp' {modifiers.addModifier(ACC_STRICT, hasNegator);}
    | 'final' {modifiers.addModifier(ACC_FINAL, hasNegator);}
    | 'transient' {modifiers.addModifier(ACC_TRANSIENT, hasNegator);}
    | 'synthetic' {modifiers.addModifier(ACC_SYNTHETIC, hasNegator);}
    | 'bridge' {modifiers.addModifier(ACC_BRIDGE, hasNegator);}
    | 'varargs' {modifiers.addModifier(ACC_VARARGS, hasNegator);}
  )
  ;

private inheritance returns [InheritanceSpecification inheritanceSpec]
@init{
  boolean hasNameNegator = false;
}
  :
  ('extends' | 'implements')
  annotation? (NEGATOR {hasNameNegator = true;})? NAME {inheritanceSpec = GrammarActions.createInheritance($NAME.text, hasNameNegator, $annotation.annotSpec);};

private arguments returns [String signature]
  :
  '(' {signature = "(";}
    (
      (
        parameterSig=type {signature += parameterSig;}
        (',' parameterSig=type {signature += parameterSig;})*
        )?
      )
    ')' {signature += ")";}
  ;

private type returns [String signature]
@init {
  int dim = 0;
}
  :
  (
    typeName='%' {String sig = $typeName.text; signature = GrammarActions.getSignature(sig == null ? "" : sig, 0);}
    |
    (typeName=NAME ('[]' {dim++;})*  {String sig = $typeName.text; signature = GrammarActions.getSignature(sig == null ? "" : sig, dim);})
  )
  ;

private keepOptionModifier returns [KeepModifier modifier]
  : ','
  ('allowshrinking' {modifier = KeepModifier.ALLOW_SHRINKING;}
  | 'allowoptimization' // Optimizations not supported
  | 'allowobfuscation' {modifier = KeepModifier.ALLOW_OBFUSCATION;})
  ;

private NAME  : ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'?'|'$'|'.'|'*'|'/'|'\\'|'-'|'<'|'>')+ ;

LINE_COMMENT
  :  '#' ~( '\r' | '\n' )* {$channel=HIDDEN;}
  ;

private WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;



