/* JF1ex exarnole: partial Java language lexer specification*/
import java_cup.runtime.* ;

    /*
    *   This class is a simple example lexer.
    */

    /*
        Lexer base tomado de la página de Cup que requiere main.java.sym para utilizarse como Lexer
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
        return new Symbol(type, yyline, yycolumn);
    }

    private Symbol symbol(int type, Object value) {
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
<YYINITIAL> "int" { return symbol(main.java.sym.INTEGER); }
<YYINITIAL> "string" { return symbol(main.java.sym.STRING); }
<YYINITIAL> "char" { return symbol(main.java.sym.CHAR); }

<YYINITIAL>{
    /* Identifiers */
    {Identifier}            { return symbol(main.java.sym.IDENTIFICADOR); }

    /* Literals */
    {DecIntergerLiteral}    { return symbol(main.java.sym.L_INTEGER); }

    \"                      {string.setLength(0); yybegin(STRING); }

    /* Operators */

    "="                     { return symbol(main.java.sym.ASIGNA); }
    "=="                    { return symbol(main.java.sym.COMPARACION); }
    "+"                     { return symbol(main.java.sym.SUMA); }

    /* Comments */
    {Comment}               { /* Ignore comments */ }

    /* White space */
    {WhiteSpace}            { /* Ignore white space */ }

}

<STRING>{

    \"                      { yybegin(YYINITIAL); }


    [^\r\n\"\\]+               { string.append(yytext()); }
    \\t                    { string.append("\t"); }
    \\n                    { string.append("\n"); }

    \\r                    { string.append("\r"); }
    \\\"                   { string.append("\""); }
    \\                   { string.append("\\"); }

}

/* Error Fallback */
//[^]                     { throw new Error("Illegal character <" + yytext()+">");}
