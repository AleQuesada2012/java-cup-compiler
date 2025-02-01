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
    public static final List<String> DATA_TYPES = Arrays.asList("INT", "FLOAT", "CHAR", "BOOL", "STRING");

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

    // todo
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


    public boolean isValidOperation(String right, String operator, String left) {
        String typeOperandRight = getType(right);
        String typeOperandoLeft = getType(left);
        return !(typeOperandRight.equals(DATA_TYPES.get(4)) || typeOperandoLeft.equals(DATA_TYPES.get(4)) ||
                ((typeOperandRight.equals(DATA_TYPES.get(1)) || typeOperandoLeft.equals(DATA_TYPES.get(1))) && operator.equals("%"))) &&
                typeOperandRight.equals(typeOperandoLeft);
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
        return getType(index).equals(DATA_TYPES.getFirst());
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