package main.java;

import java_cup.runtime.*;

import java.util.Objects;

import main.java.sym;
import main.java.Parser;
import main.java.LexerCupV;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Ingrese la ruta al archivo .txt de entrada (relativo al directorio actual):");
            String inputPathString = scanner.nextLine();
            //String inputPathString = "src/main/resources/prueba_parser2.txt";
            Path inputPath = Paths.get(inputPathString).toAbsolutePath(); // para que funcione sin importar donde se ejecute
            File inputFile = inputPath.toFile();

            if (!inputFile.exists() || !inputFile.isFile()) {
                System.err.println("Error: El archivo especificado no existe o no es válido.");
                return;
            }
            System.out.println("Archivo de entrada encontrado: " + inputFile.getAbsolutePath());

            // aquí se llaman los métodos para correr cada "etapa" del compilador
            runLexer(inputFile);
            runParser(inputFile);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static void runLexer(File inputSource) {
        try {
            LexerCupV lexer = new LexerCupV(new FileReader(inputSource));
            BufferedWriter writer = new BufferedWriter(new FileWriter("output_lexer.txt"));

            Symbol token;

            while (!Objects.isNull(token = lexer.next_token())) {
                if (token.sym == sym.EOF) break;
                String tokenInfo = lexer.getTokenInfo(token.sym);
                writer.write(tokenInfo);
                writer.newLine();
            }
            writer.close();
            System.out.println("\n\nAnálisis léxico completado. Se escribió la salida al archivo: output_lexer.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runParser(File inputSource) throws Exception {
        // probando el parseo con un lexer nuevo
        System.out.println("--------------------------");
        System.out.println("Iniciando análisis sintáctico:");

        LexerCupV lexer = new LexerCupV(new FileReader(inputSource));
        BufferedWriter writer = new BufferedWriter(new FileWriter("output_parser.txt"));
        BufferedWriter symTable = new BufferedWriter(new FileWriter("output_symTable.txt"));
        BufferedWriter semanticOutput = new BufferedWriter(new FileWriter("output_semantic.txt"));
        Parser parser = new Parser(lexer, writer, symTable, semanticOutput);
        parser.parse();
        writer.close();
        semanticOutput.close();
        //parser.printSymbolTable();
        symTable.close();
        System.out.println("Se escribió la tabla de símbolos al archivo output_symTable.txt y los errores encontrados al output_parser.txt");
        System.out.println();


        File file = new File("output_parser.txt");
        System.out.println((file.length() == 0) ? "sí se puede producir el archivo con la gramática" : "hay errores, no se podría producir el archivo con la gramática.");
    }
}
