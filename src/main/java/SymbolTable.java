package main.java;

import java.util.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {

    public static final List<String> Tipos_Datos = Arrays.asList("int", "float", "char", "bool", "string");

    private HashMap<String, String> symTable;
    private Stack<HashMap<String, String>> Scopeslocales;
    private String currentFunction;

    public SymbolTable(BufferedWriter symTableOutFile) {
        this.Scopeslocales = new Stack<HashMap<String, String>>();
        this.symTable = new HashMap<String, String>();
        this.currentFunction = "";
    }


    public void setCurrentFunction(String nameFunction) {this.currentFunction = nameFunction;}

    public String getCurrentFunction() {return this.currentFunction;}

    public void getInScope() {this.Scopeslocales.add(new HashMap<String, String>());}

    public void getOutScope() {this.Scopeslocales.pop();}

    public void addTableSymbol(String currentHash, String characteristic) {
        if (!this.symTable.containsKey(currentHash)) {this.symTable.put(currentHash, characteristic);}
    }

    public void addSymbolScope(String currentHash, String characteristic) {
        if (!this.Scopeslocales.isEmpty() && !this.Scopeslocales.peek().containsKey(currentHash)) {
            this.Scopeslocales.peek().put(currentHash, characteristic);
        }
    }

    public boolean isInsideLocalScope(String hash) {
        for (int i = 0; i < Scopeslocales.size(); i++) {if (Scopeslocales.get(i).containsKey(hash)) {return true;}}
        return false;
    }


    public boolean isDataType(String data) {return Tipos_Datos.contains(data);}


    public String getType(String hash) {
        if (isDataType(hash)) {return hash;}

        for (int i = 0; i < Scopeslocales.size(); i++) {
            if (Scopeslocales.get(i).containsKey(hash)) {
                String[] info = Scopeslocales.get(i).get(hash).split(":");
                return info[0];
            }
        }
        return "";
    }


    public boolean Typeverification(String type1, String type2) {return type1.equals(type2);}


    public boolean isValidOperation(String right, String operator, String left) {
        String typeOperandRight = getType(right);
        String typeOperandoLeft = getType(left);
        return !(typeOperandRight.equals(Tipos_Datos.get(4)) || typeOperandoLeft.equals(Tipos_Datos.get(4)) ||
                ((typeOperandRight.equals(Tipos_Datos.get(1)) || typeOperandoLeft.equals(Tipos_Datos.get(1))) && operator.equals("%"))) &&
                typeOperandRight.equals(typeOperandoLeft);
    }


    public boolean functionCallVerification(String calledFunction, String function_data) {
        if (!symTable.containsKey(calledFunction)) {return false;}

        String[] parameters = function_data.split(":");
        String[] finfo = symTable.get(calledFunction).split(":");

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
        for (int i = 0; i < values.length; i++) {if (!type.equals(values[i])) {return false;}}
        return true;
    }


    public boolean integrerIndex(String index) {
        return getType(index).equals(Tipos_Datos.get(0));
    }


    public void printingScope() {
        if (!Scopeslocales.empty()) {
            System.out.println(Scopeslocales.peek());
            System.out.println("");
        }
    }


    public void printTableSymbol() {
        System.out.println(symTable);
        System.out.println("");
    }

}