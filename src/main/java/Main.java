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
            File inputFile = new File("src\\main\\resources\\prueba.txt"); //Aquí va a estar mientras
            File outputFile = new File("src\\main\\resources\\output.txt");

            Reader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            LexerCupV lexer = new LexerCupV(reader);

            Symbol token;
            while (!Objects.isNull(token = lexer.next_token())) {
                if (token.sym == sym.EOF) break;

                writer.write("Línea de aparición: " + (token.left + 1));
                writer.newLine();
                writer.write("Columna: " + (token.right + 1));
                writer.newLine();
                writer.write("----------------------------------");
                writer.newLine();
            }
            System.out.println("Output written to: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
