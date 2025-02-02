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

    private final Map<String, String> globalTable;
    private final Stack<HashMap<String, String>> localScopes;
    //private final List<String> switchCaseValues;
    public String switchDataType;
    public String currentFunction;


    /**
     *
     * @param symTableOutFile
     */
    public SymbolTable(BufferedWriter symTableOutFile) {
        this.globalTable = new HashMap<>();
        this.localScopes = new Stack<>();
        //this.switchCaseValues = new ArrayList<>();
        this.currentFunction = "";
        this.switchDataType = "";
    }


    /**
     *
     * @param currentSymbol
     * @param info
     * @return
     */
    public boolean addToGlobalIfAbsent(String currentSymbol, String info) {
        if (!globalTable.containsKey(currentSymbol)) {
            globalTable.put(currentSymbol, info);
            return true;
        }
        return false;
    }


    /**
     *
     */
    public void createScope() {this.localScopes.add(new HashMap<String, String>());}


    /**
     *
     */
    public void popScope() {this.localScopes.pop();}


    /**
     *
     * @param currentHash
     * @param characteristic
     * @return
     */
    public boolean addSymbolToScope(String currentHash, String characteristic) {
        if (!this.localScopes.isEmpty() && !this.localScopes.peek().containsKey(currentHash)) {
            this.localScopes.peek().put(currentHash, characteristic);
            return true;
        }
        return false;
    }


    /**
     *
     * @param hash
     * @return
     */
    public boolean isInsideLocalScope(String hash) {
        for (HashMap<String, String> localScope : localScopes)
            if (localScope.containsKey(hash)) return true;
        return false;
    }


    /**
     *
     * @param data
     * @return
     */
    public boolean isDataType(String data) {
        for (String dataType : DATA_TYPES) {
            if (data.equals(dataType)) return true;
        }
        return false;
    }


    /**
     *
     * @param hash
     * @return
     */
    public String getType(String hash) {
        if (isDataType(hash)) {return hash;}
        if (isInGlobalScope(hash)) {
            globalTable.get(hash);
            // por la forma en la que almacenamos en la tabla de símbolos, el primer valor de este split es el tipo
            return globalTable.get(hash).split(":")[0];
        }

        for (HashMap<String, String> localScope : localScopes) {
            if (localScope.containsKey(hash)) {
                String[] info = localScope.get(hash).split(":");
                return info[0];
            }
        }
        return "";
    }


    /**
     *
     * @param left
     * @param right
     * @return
     */
    public boolean checkType(String left, String right) {
        String typeLeft = getType(left);
        String typeRight = getType(right);
        return typeLeft.equals(typeRight) && !typeLeft.isBlank();
    }


    public boolean isValidOperation(String right, String operator, String left) {
        String typeRight = getType(right);
        String typeLeft = getType(left);
        System.out.println("tipo izq: " + typeRight + "\ntipo der: " + typeLeft);
        return !(typeRight.equals("STRING") || typeLeft.equals("STRING") ||
                ((typeRight.equals("FLOAT") || typeLeft.equals("FLOAT")) && operator.equals("%"))) && typeRight.equals(typeLeft);
    }

    /**
     *
     * @param f nombre de la función que se desea verificar
     * @param fData información que acompaña a la función en el momento que se invoca (sus argumentos)
     * @return casos por valor de entero, dependiendo de por qué la llamada no es válida. 1 si no existe el IDENt, 2 si faltan parámetros, 3 si el tipo de alguno no coincide
     * o 0 si es válida.
     */
    public int checkFunctionCall(String f, String fData) {
        System.out.println("called func: " + f);
        if (!globalTable.containsKey(f)) {return 1;}

        String[] parameters = fData.split(":");
        String[] fInfo = globalTable.get(f).split(":");
        System.out.println("finfo: " + Arrays.toString(fInfo));

        if (fInfo.length - 2 != parameters.length) {return 2;}

        for (int i = 2; i < fInfo.length; i++) {
            if (!fInfo[i].equals(getType(parameters[i - 2]))) {
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
     * @return falso si en algún momento encuentra un valor que no es del tipo del arreglo, true si todos eran del mismo tipo
     */
    public boolean checkArray(String type, String data) {
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


/*    public boolean isDuplicateCase(String caseValue) {
        if(!switchCaseValues.contains(caseValue)) {
            switchCaseValues.add(caseValue);
            return false;
        }
        return true;
    }


    public void clearCaseValues() {
        System.out.println(switchCaseValues);
        switchCaseValues.clear();
    }*/


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