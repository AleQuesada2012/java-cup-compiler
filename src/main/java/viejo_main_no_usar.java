package main.java;

import java.io.FileReader;
import java.io.IOException;

public class viejo_main_no_usar {
    public static void main(String[] args) {
        public static void main(String[] args) {
            try {
                // Adjust the file path if needed
                String testFilePath = "resources/prueba1.txt";

                // Create a FileReader for the test file
                FileReader fileReader = new FileReader(testFilePath);

                // Instantiate the lexer
                LexerCupV lexer = new LexerCupV(fileReader);

                // Process tokens until EOF
                Symbol token;
                while ((token = lexer.next_token()).sym != sym.EOF) {
                    System.out.println("Token: " + token.sym + ", Value: " + token.value);
                }

                System.out.println("Lexical analysis completed!");

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Error during parsing: " + e.getMessage());
            }
        }

    }
}



