/* JF1ex exarnole: partial Java language lexer specification*/
package main.java;
import java_cup.runtime.* ;

    /*
    *   This class is a simple example lexer.
    */

    /*
        Lexer base tomado de la página de Cup que requiere sym para utilizarse como Lexer
        Este lexer es utilizado por por el parser generado por BasicLexerCup (parser.java que se genera)
    */

%%
%class LexerCupV
%public
%unicode
%cup
%line
%column

%{
    StringBuffer string = new StringBuffer();

    private Symbol symbol(int type) {
        // este es el autogenerado en flex pero no lo utilizamos
        System.out.println("Token identified: " + type + ", Value: " + sym.terminalNames[type]);
        return new Symbol(type, yyline, yycolumn);
    }
    // nuevo procedimiento para pasar la información al main
    public String getTokenInfo(int type) {
        String text = "Token:   " + sym.terminalNames[type] + ",    Lexema: " + yytext();
        if (type == sym.ERROR)
            text += " (Error léxico por patrón no reconocido)";
            return text;
    }

    private Symbol symbol(int type, Object value) {
        String text = "Token:    " + sym.terminalNames[type] + ",    Lexema:    " + value;
         if (type == sym.ERROR)
             text += " (Error léxico por patrón no reconocido)";
         System.out.println(text);
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n] // significa "cualquiera menos estos"
WhiteSpace = {LineTerminator} | [ \t\f]

// Comentarios
Comment = {TraditionalComment} | {EOLComment}

TraditionalComment = "\\_" ([^_] | "_" [^/] | "\n")* "_/"
EOLComment = "@" {InputCharacter}* {LineTerminator}?

Identifier = _[a-zA-Z][a-zA-Z0-9]*_

// expresiones regulares para literales (según el documento del proyecto sí pueden venir y están en el código)
digito = [0-9]
digitoNoCero = [1-9]
DecIntegerLiteral = 0 | -?{digitoNoCero}{digito}*
floatLiteral = "0\.0" | -?{digito}+"\."{digito}*{digitoNoCero}

booleanLiteral = "true"|"false"

charLiteral = \'.\'





%state STRING

%%


/* Keywords */
<YYINITIAL> "rodolfo" { return symbol(sym.INTEGER, yytext()); }
<YYINITIAL> "bromista" { return symbol(sym.FLOAT, yytext()); }
<YYINITIAL> "trueno" { return symbol(sym.BOOL, yytext()); }
<YYINITIAL> "cupido" { return symbol(sym.CHAR, yytext()); }
<YYINITIAL> "cometa" { return symbol(sym.STRING, yytext()); }

/* Bloques de código */
<YYINITIAL> "abrecuento" { return symbol(sym.APERTURA_DE_BLOQUE, yytext()); }
<YYINITIAL> "cierracuento" { return symbol(sym.CIERRRE_DE_BLOQUE, yytext()); }

/* Corchetes */
<YYINITIAL> "abreempaque" { return symbol(sym.CORCHETE_APERTURA, yytext()); }
<YYINITIAL> "cierraempaque" { return symbol(sym.CORCHETE_CIERRE, yytext()); }

/* Separadores */
<YYINITIAL> "," { return symbol(sym.SEPARADOR_PARAMS, yytext()); }

/* Asignación */
<YYINITIAL> "entrega" { return symbol(sym.ASIGNACION, yytext()); }

/* Paréntesis */
<YYINITIAL> "abreregalo" { return symbol(sym.PARENTESIS_APERTURA, yytext()); }
<YYINITIAL> "cierraregalo" { return symbol(sym.PARENTESIS_CIERRE, yytext()); }

// fin de expresion
<YYINITIAL> "finregalo" { return symbol(sym.FIN_EXPRESION, yytext()); }

/* Operadores */
<YYINITIAL> "quien" { return symbol(sym.INCREMENTO, yytext()); }
<YYINITIAL> "grinch" { return symbol(sym.DECREMENTO, yytext()); }
<YYINITIAL> "navidad" { return symbol(sym.SUMA,yytext()); }
<YYINITIAL> "intercambio" { return symbol(sym.RESTA, yytext()); }
<YYINITIAL> "nochebuena" { return symbol(sym.MULTIPLICACION, yytext()); }
<YYINITIAL> "magos" { return symbol(sym.MODULO, yytext()); }
<YYINITIAL> "adviento" { return symbol(sym.POTENCIA, yytext()); }

/* Operadores relacionales */
<YYINITIAL> "snowball" { return symbol(sym.MENOR, yytext()); }
<YYINITIAL> "evergreen" { return symbol(sym.MENOR_IGUAL, yytext()); }
<YYINITIAL> "minstix" { return symbol(sym.MAYOR,yytext()); }
<YYINITIAL> "upatree" { return symbol(sym.MAYOR_IGUAL,yytext()); }
<YYINITIAL> "mary" { return symbol(sym.IGUALDAD,yytext()); }
<YYINITIAL> "openslae" { return symbol(sym.DIFERENTE,yytext()); }

/* Operadores lógicos */
<YYINITIAL> "melchor" { return symbol(sym.CONJUNCION,yytext()); }
<YYINITIAL> "gaspar" { return symbol(sym.DISYUNCION,yytext()); }
<YYINITIAL> "baltazar" { return symbol(sym.NEGACION,yytext()); }

/* Estructuras de control */
<YYINITIAL> "elfo" { return symbol(sym.IF,yytext()); }
<YYINITIAL> "hada" { return symbol(sym.ELSE, yytext()); }
<YYINITIAL> "envuelve" { return symbol(sym.WHILE, yytext()); }
<YYINITIAL> "duende" { return symbol(sym.FOR, yytext()); }
<YYINITIAL> "varios" { return symbol(sym.SWITCH, yytext()); }
<YYINITIAL> "historia" { return symbol(sym.CASE, yytext()); }
<YYINITIAL> "ultimo" { return symbol(sym.DEFAULT, yytext()); }
<YYINITIAL> "corta" { return symbol(sym.BREAK, yytext()); }
<YYINITIAL> "envia" { return symbol(sym.RETURN, yytext()); }
<YYINITIAL> "sigue" { return symbol(sym.DOS_PUNTOS,yytext()); }

/* Funciones */
<YYINITIAL> "narra" { return symbol(sym.PRINT, yytext()); }
<YYINITIAL> "escucha" { return symbol(sym.READ, yytext()); }

/* Identificadores */
<YYINITIAL> "_verano_" { return symbol(sym.MAIN, yytext()); }
<YYINITIAL> {Identifier} { return symbol(sym.IDENTIFICADOR, yytext()); }

/* Literales */
<YYINITIAL> {DecIntegerLiteral} { return symbol(sym.L_INTEGER, yytext()); }
<YYINITIAL> {floatLiteral} { return symbol(sym.L_FLOAT, yytext()); }
<YYINITIAL> {charLiteral} { return symbol(sym.L_CHAR, yytext()); }
<YYINITIAL> {booleanLiteral} { return symbol(sym.L_BOOL, yytext()); }

/* String literals */
<YYINITIAL> \" { string.setLength(0); yybegin(STRING); }

<STRING> {
    \" { yybegin(YYINITIAL); return symbol(sym.L_STRING, string.toString()); }

    [^\r\n\"\\]+ { string.append(yytext()); }

    \\t { string.append("\t"); }

    \\n { string.append("\n"); }

    \\r { string.append("\r"); }

    \\\" { string.append("\""); }

    \\\\ { string.append("\\"); }
}


/* Comentarios */
<YYINITIAL> {Comment} { /* Se ignoran los comentarios aunque se reconocen */}


/* Espacios en blanco */
<YYINITIAL> {WhiteSpace} { /* Ignorar espacios en blanco */ }

/* Error Fallback */
// TODO: implementar el manejo de errores
[^]                     { return symbol(sym.ERROR, yytext());}