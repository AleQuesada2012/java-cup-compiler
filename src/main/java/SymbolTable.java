package main.java;

import java.util.*;
import java.io.BufferedWriter;
import java.util.HashMap;

/**
 * Clase
 */
public class SymbolTable {
    // estos tipos es para verificar con lo guardado en la tabla de símbolos
    // por lo tanto deben coincidir con el patrón que producen los literales y los identificadores tipados
    public static final String[] DATA_TYPES = {"INT", "FLOAT", "BOOL", "CHAR", "STRING"};

    private final HashMap<String, String> globalTable;
    private final Stack<HashMap<String, String>> localScopes;
    private String currentFunction;

    public SymbolTable(BufferedWriter symTableOutFile) {
        this.localScopes = new Stack<HashMap<String, String>>();
        this.globalTable = new HashMap<String, String>();
        this.currentFunction = "";
    }


    public void addToGlobalIfAbsent(String currentSymbol, String info) {this.globalTable.putIfAbsent(currentSymbol, info);}

    public void setCurrentFunction(String nameFunction) {this.currentFunction = nameFunction;}

    public String getCurrentFunction() {return this.currentFunction;}

    public String getFuncType() {
        if(globalTable.containsKey(currentFunction)) {
            System.out.println("entramos a sacar el dato");
            String[] data = globalTable.get(currentFunction).split(":");
            return data[0];
        }
        System.out.println("no habia dato :c");
        return "";
    }

    public String getFuncType(String funcId) {
        if(globalTable.containsKey(funcId)) {
            return globalTable.get(funcId).split(":")[0];
        }
        return "";
    }

    public void createScope() {this.localScopes.add(new HashMap<String, String>());}

    public void popScope() {this.localScopes.pop();}

    public void addSymbolToGlobal(String currentHash, String characteristic) {
        if (!this.globalTable.containsKey(currentHash)) {this.globalTable.put(currentHash, characteristic);}
    }

    public boolean addSymbolToScope(String currentHash, String characteristic) {
        if (!this.localScopes.isEmpty() && !this.localScopes.peek().containsKey(currentHash)) {
            this.localScopes.peek().put(currentHash, characteristic);
            return true;
        }
        return false;
    }

    public boolean isInsideLocalScope(String hash) {
        for (HashMap<String, String> localScope : localScopes)
            if (localScope.containsKey(hash)) return true;
        return false;
    }

    // todo
    public boolean isDataType(String data) {
        for (String dataType : DATA_TYPES) {
            if (data.equals(dataType)) return true;
        }
        return false;
    }


    public String getType(String hash) {
        if (isDataType(hash)) {return hash;}
        if (isInGlobalScope(hash)) {
            globalTable.get(hash);
            String[] data = globalTable.get(hash).split(":");
            return data[0];
        }

        for (HashMap<String, String> localScope : localScopes) {
            if (localScope.containsKey(hash)) {
                String[] info = localScope.get(hash).split(":");
                return info[0];
            }
        }
        return "";
    }


    public boolean isValidOperation(String right, String operator, String left) {
        String typeOperandRight = getType(right) == null ? getFuncType() : getType(right);
        String typeOperandLeft = getType(left) == null ? getFuncType() : getType(left);
        System.out.println("tipo izq: " + typeOperandRight + "\ntipo der: " + typeOperandLeft);
        return !(typeOperandRight.equals("STRING") || typeOperandLeft.equals("STRING") ||
                ((typeOperandRight.equals("FLOAT") || typeOperandLeft.equals("FLOAT")) && operator.equals("%"))) &&
                typeOperandRight.equals(typeOperandLeft);
    }

    /**
     *
     * @param calledFunction nombre de la función que se desea verificar
     * @param function_data información que acompaña a la función en el momento que se invoca (sus argumentos)
     * @return casos por valor de entero, dependiendo de por qué la llamada no es válida. 1 si no existe el IDENt, 2 si faltan parámetros, 3 si el tipo de alguno no coincide
     * o 0 si es válida.
     */
    public int functionCallVerification(String calledFunction, String function_data) {
        System.out.println("called func: " + calledFunction);
        if (!globalTable.containsKey(calledFunction)) {return 1;}

        String[] parameters = function_data.split(":");
        String[] finfo = globalTable.get(calledFunction).split(":");
        System.out.println("finfo: " + Arrays.toString(finfo));

        if (finfo.length - 2 != parameters.length) {return 2;}

        for (int i = 2; i < finfo.length; i++) {
            if (!finfo[i].equals(getType(parameters[i - 2]))) {
                return 3;
            }
        }
        return 0;
    }

    public boolean isInGlobalScope(String calledFunction) {
        return globalTable.containsKey(calledFunction);
    }

    /**
     *
     * @param type
     * @param data
     * @return
     */
    public boolean arrayVerification(String type, String data) {
        String[] values = data.split(":");
        for (String value : values)
            if (!type.equals(value)) return false;
        return true;
    }

    /**
     *
     * @param index
     * @return
     */
    public boolean isIntIndex(String index) {
        return getType(index).equals("INT");
    }


    /**
     * Método para mostrar en consola el valor en string de cada scope ants de que se saquen de la pila
     * Por ahora escribe a consola, se debe modificar para que escriban a un archivo.
     */
    public void writeScope() {
        if (!localScopes.empty()) {
            System.out.println(localScopes.peek());
            System.out.println();
        }
    }


    /**
     * Función para imprimir toda la tabla de símbolos
     * Por ahora solo imprime el scope 'global', se debe modificar para que imprima todo
     */
    public void printTableSymbol() {
        System.out.println(globalTable);
        System.out.println();
    }

}