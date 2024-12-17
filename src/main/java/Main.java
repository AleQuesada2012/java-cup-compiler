package main.java;

import java.io.*; // For file handling
import java_cup.runtime.*; // CUP runtime for Symbol
import java.util.Objects;
import main.java.sym;
import main.java.Parser;
import main.java.LexerCupV;


public class Main {

    public static void main(String[] args) {
        try {
            // 1. Open a sample input file (replace "input.txt" with your file path)
            File inputFile = new File("input.txt");
            Reader reader = new BufferedReader(new FileReader(inputFile));

            // 2. Initialize your Lexer
            LexerCupV lexer = new LexerCupV(reader);

            // 3. Process tokens
            Symbol token; // Symbol object to hold the token returned by lexer
            while (!Objects.isNull(token = lexer.next_token())) {
                // Break condition when the lexer encounters EOF
                if (token.sym == sym.EOF) break;

                // Print token details
                System.out.println("Tipo de Token: " + getTokenName(token.sym));
                System.out.println("Lexema: " + token.value);
                System.out.println("Línea de aparición: " + (token.left + 1)); // line starts at 0
                System.out.println("Columna: " + (token.right + 1)); // column starts at 0
                System.out.println("----------------------------------");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Helper function to translate token symbol to its name
    private static String getTokenName(int sym) {
        switch (sym) {
            case sym.INTEGER: return "INTEGER";
            case sym.FLOAT: return "FLOAT";
            case sym.BOOL: return "BOOL";
            case sym.CHAR: return "CHAR";
            case sym.STRING: return "STRING";
            case sym.IDENTIFICADOR: return "IDENTIFICADOR";
            case sym.ERROR: return "ERROR";
            case sym.SUMA: return "SUMA";
            case sym.RESTA: return "RESTA";
            case sym.ASIGNACION: return "ASIGNACION";
            // Add cases for other token types as needed
            default: return "UNKNOWN";
        }
    }
}
