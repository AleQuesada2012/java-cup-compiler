package main.java;

import java.util.*;
import java.io.BufferedWriter;
import java.util.HashMap;

public class SymbolTable {

    public static final List<String> DATA_TYPES = Arrays.asList("int", "float", "char", "bool", "string");

    private final HashMap<String, String> globalTable;
    private final Stack<HashMap<String, String>> localScopes;
    private String currentFunction;

    public SymbolTable(BufferedWriter symTableOutFile) {
        this.localScopes = new Stack<HashMap<String, String>>();
        this.globalTable = new HashMap<String, String>();
        this.currentFunction = "";
    }


    public boolean addToGlobalIfAbsent(String currentSymbol, String info) {
        if(!this.globalTable.containsKey(currentSymbol)) {
            this.globalTable.put(currentSymbol, info);
            return true;
        }
        return false;
    }

    public void setCurrentFunction(String nameFunction) {this.currentFunction = nameFunction;}

    public String getCurrentFunction() {return this.currentFunction;}

    public void createScope() {this.localScopes.add(new HashMap<String, String>());}

    public void popScope() {this.localScopes.pop();}

    public void addTableSymbol(String currentHash, String characteristic) {
        if (!this.globalTable.containsKey(currentHash)) {this.globalTable.put(currentHash, characteristic);}
    }

    public void addSymbolToScope(String currentHash, String characteristic) {
        if (!this.localScopes.isEmpty() && !this.localScopes.peek().containsKey(currentHash)) {
            this.localScopes.peek().put(currentHash, characteristic);
        }
    }

    public boolean isInsideLocalScope(String hash) {
        for (HashMap<String, String> localScope : localScopes)
            if (localScope.containsKey(hash)) return true;
        return false;
    }


    public boolean isDataType(String data) {return DATA_TYPES.contains(data);}


    public String getType(String hash) {
        if (isDataType(hash)) {return hash;}

        for (HashMap<String, String> localScope : localScopes) {
            if (localScope.containsKey(hash)) {
                String[] info = localScope.get(hash).split(":");
                return info[0];
            }
        }
        return "";
    }


    public boolean typeVerification(String type1, String type2) {return type1.equals(type2);}


    public boolean isValidOperation(String right, String operator, String left) {
        String typeOperandRight = getType(right);
        String typeOperandoLeft = getType(left);
        return !(typeOperandRight.equals(DATA_TYPES.get(4)) || typeOperandoLeft.equals(DATA_TYPES.get(4)) ||
                ((typeOperandRight.equals(DATA_TYPES.get(1)) || typeOperandoLeft.equals(DATA_TYPES.get(1))) && operator.equals("%"))) &&
                typeOperandRight.equals(typeOperandoLeft);
    }


    public boolean functionCallVerification(String calledFunction, String function_data) {
        if (!globalTable.containsKey(calledFunction)) {return false;}

        String[] parameters = function_data.split(":");
        String[] finfo = globalTable.get(calledFunction).split(":");

        if (finfo.length - 2 != parameters.length) {return false;}

        for (int i = 2; i < finfo.length; i++) {
            if (!finfo[i].equals(getType(parameters[i - 2]))) {
                return false;
            }
        }
        return true;
    }


    public boolean arrayVerification(String type, String data) {
        String[] values = data.split(":");
        for (String value : values)
            if (!type.equals(value)) return false;
        return true;
    }


    public boolean isIntIndex(String index) {
        return getType(index).equals(DATA_TYPES.getFirst());
    }


    public void writeScope() {
        if (!localScopes.empty()) {
            System.out.println(localScopes.peek());
            System.out.println();
        }
    }


    public void printTableSymbol() {
        System.out.println(globalTable);
        System.out.println();
    }

}