package main.java;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private final HashMap<String, ArrayList<String>> symTable;
    private final BufferedWriter symTableOutFile;

    public SymbolTable(BufferedWriter symTableOutFile) {
        this.symTable = new HashMap<>();
        this.symTableOutFile = symTableOutFile;
    }

    public void addTable(String tableName) {
        symTable.putIfAbsent(tableName, new ArrayList<>());
    }

    public void addSymbol(String tableName, String symbol) {
        if (symTable.containsKey(tableName)) {
            symTable.get(tableName).add(symbol);
        } else {
            throw new IllegalStateException("Table " + tableName + " does not exist.");
        }
    }

    public void printSymbolTable() throws IOException {
        for (String functionName : symTable.keySet()) {
            writeSymTable("Tabla: " + functionName);
            ArrayList<String> symbols = symTable.get(functionName);
            writeSymTable("Valores:");
            for (String symbol : symbols) {
                writeSymTable("\t" + symbol);
            }
        }
    }

    private void writeSymTable(String message) throws IOException {
        symTableOutFile.write(message);
        symTableOutFile.newLine();
    }
}
