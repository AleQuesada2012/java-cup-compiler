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
        System.out.println("Token identified: " + type);
        return new Symbol(type, yyline, yycolumn);
    }

    private Symbol symbol(int type, Object value) {
         System.out.println("Token identified: " + type + ", Value: " + value);
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace = {LineTerminator} | [ \t\f]

//comentarios
Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment = "\\" [^*] ~"/" | "\\" ""+ "/" //Verificar que se este mapeando correctamente el '\' :c

EndOfLineComment = "#" {InputCharacter}* {LineTerminator}?
//DocumentationComment = "/" {CommentContent} "*"+ "/"
//CommentContent = ([^] | \+ [^ / ])

Identifier = [:jletter:]+[:jletterdigit:]*


digito = [0-9]
digitoNoCero = [1-9]
DecIntergerLiteral = 0 | -?{digitoNoCero}{digito}*




%state STRING

%%


/* Keywords */
<YYINITIAL> "rodolfo" { return symbol(sym.INTEGER); }
<YYINITIAL> "bromista" { return symbol(sym.FLOAT); }
<YYINITIAL> "trueno" { return symbol(sym.BOOL); }
<YYINITIAL> "cupido" { return symbol(sym.CHAR); }
<YYINITIAL> "cometa" { return symbol(sym.STRING); }

/* Bloques de código */
<YYINITIAL> "abrecuento" { return symbol(sym.APERTURA_DE_BLOQUE); }
<YYINITIAL> "cierracuento" { return symbol(sym.CIERRRE_DE_BLOQUE); }

/* Corchetes */
<YYINITIAL> "abreempaque" { return symbol(sym.CORCHETE_APERTURA); }
<YYINITIAL> "cierraempaque" { return symbol(sym.CORCHETE_CIERRE); }

/* Asignación */
<YYINITIAL> "entrega" { return symbol(sym.ASIGNACION); }

/* Paréntesis */
<YYINITIAL> "abreregalo" { return symbol(sym.PARENTESIS_APERTURA); }
<YYINITIAL> "cierraregalo" { return symbol(sym.PARENTESIS_CIERRE); }

/* Operadores */
<YYINITIAL> "navidad" { return symbol(sym.SUMA); }
<YYINITIAL> "intercambio" { return symbol(sym.RESTA); }
<YYINITIAL> "nochebuena" { return symbol(sym.MULTIPLICACION); }
<YYINITIAL> "magos" { return symbol(sym.MODULO); }
<YYINITIAL> "adviento" { return symbol(sym.POTENCIA); }
<YYINITIAL> "quien" { return symbol(sym.INCREMENTO); }
<YYINITIAL> "grinch" { return symbol(sym.DECREMENTO); }

/* Operadores relacionales */
<YYINITIAL> "snowball" { return symbol(sym.MENOR); }
<YYINITIAL> "evergreen" { return symbol(sym.MENOR_IGUAL); }
<YYINITIAL> "minstix" { return symbol(sym.MAYOR); }
<YYINITIAL> "upatree" { return symbol(sym.MAYOR_IGUAL); }
<YYINITIAL> "mary" { return symbol(sym.IGUALDAD); }
<YYINITIAL> "openslae" { return symbol(sym.DIFERENTE); }

/* Operadores lógicos */
<YYINITIAL> "melchor" { return symbol(sym.CONJUNCION); }
<YYINITIAL> "gaspar" { return symbol(sym.DISYUNCION); }
<YYINITIAL> "baltazar" { return symbol(sym.NEGACION); }

/* Estructuras de control */
<YYINITIAL> "elfo" { return symbol(sym.IF); }
<YYINITIAL> "hada" { return symbol(sym.ELSE); }
<YYINITIAL> "envuelve" { return symbol(sym.WHILE); }
<YYINITIAL> "duende" { return symbol(sym.FOR); }
<YYINITIAL> "varios" { return symbol(sym.SWITCH); }
<YYINITIAL> "historia" { return symbol(sym.CASE); }
<YYINITIAL> "ultimo" { return symbol(sym.DEFAULT); }
<YYINITIAL> "corta" { return symbol(sym.BREAK); }
<YYINITIAL> "envia" { return symbol(sym.RETURN); }
<YYINITIAL> "sigue" { return symbol(sym.DOS_PUNTOS); }

/* Funciones */
<YYINITIAL> "narra" { return symbol(sym.PRINT); }
<YYINITIAL> "escucha" { return symbol(sym.READ); }

/* Identificadores */
<YYINITIAL> {Identifier} { return symbol(sym.IDENTIFICADOR); }

/* Literales */
<YYINITIAL> {DecIntergerLiteral} { return symbol(sym.L_INTIGER); }

/* String literals */
<YYINITIAL> "\"" {string.setLength(0); yybegin(STRING); }
<STRING> "\"" { yybegin(YYINITIAL); }
<STRING> [^\r\n\"\\]+ { string.append(yytext()); }
<STRING> \\t { string.append("\t"); }
<STRING> \\n { string.append("\n"); }
<STRING> \\r { string.append("\r"); }
<STRING> \\\" { string.append("\""); }
<STRING> \\\\ { string.append("\\"); }

/* Comentarios */
<YYINITIAL> {Comment} { /* Ignorar comentarios */ }

/* Espacios en blanco */
<YYINITIAL> {WhiteSpace} { /* Ignorar espacios en blanco */ }

/* Error Fallback */
//[^]                     { throw new Error("Illegal character <" + yytext()+">");}