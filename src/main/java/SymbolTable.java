package main.java;

import java.util.*;
import java.io.BufferedWriter;
import java.util.HashMap;

/**
 * Clase para delegar la funcionalidad de la tabla de símbolos a un archivo externo, mejorando lebigilidad
 * y el manejo general del CUP.
 */
public class SymbolTable {
    // estos tipos es para verificar con lo guardado en la tabla de símbolos
    // por lo tanto deben coincidir con el patrón que producen los literales y los identificadores tipados
    public static final String[] DATA_TYPES = {"INT", "FLOAT", "BOOL", "CHAR", "STRING"};

    private final Map<String, String> globalTable;
    private final Stack<Scope> localScopes;
    private final BufferedWriter outputFile;
    //private final List<String> switchCaseValues;
    public String switchDataType;
    public String currentFunction;


    /**
     * Constructor de la clase. Inicializa los atributos de la clase como es de costumbre en POO.
     * @param symTableOutFile: una referencia al archivo en el que se escribirá la tabla de símbolos en forma de
     *                       BufferedWriter (no File).
     */
    public SymbolTable(BufferedWriter symTableOutFile) {
        this.outputFile = symTableOutFile;
        this.globalTable = new HashMap<>();
        this.localScopes = new Stack<>();
        //this.switchCaseValues = new ArrayList<>();
        this.currentFunction = "";
        this.switchDataType = "";
    }


    /**
     * Método para revisar la presencia de símbolos en el scope global
     * @param currentSymbol: la función actual en la que se encuentra (función, dado que este lenguaje solo tiene funciones en su scope global)
     * @param info: datos que acompañan al nombre de la función: su tipo y el tipo de sus parámetros.
     * @return: un valor de verdad, true si el símbolo no existía, false si ya estaba presente y no lo agregó.
     */
    public boolean addToGlobalIfAbsent(String currentSymbol, String info) {
        if (!globalTable.containsKey(currentSymbol)) {
            globalTable.put(currentSymbol, info);
            return true;
        }
        return false;
    }


    /**
     * Método para agregar nuevos scopes durante el análisis semántico
     */
    public void createScope(String name) {this.localScopes.add(new Scope(name));}


    /**
     * Método para salirse de un scope al finalizar un bloque o estructura de control
     * Se encarga de sacar de la pila el scope, por lo que este debe escribirse al archivo de salida antes de perder
     * su referencia
     */
    public void popScope() {this.localScopes.pop();}


    /**
     * Método para agregar símbolos al scope local inmediato (en el que se encuentra actualmente la pila)
     * @param symbolName: cadena que representa el identificador del símbolo que se desea agregar
     * @param symbolData: información relevante del símbolo
     * @return: true si se pudo agregar, indicando que no existía en los scopes locales anteriores a este (inclusive) o
     * false si no se pudo, indicando que es un símbolo repetido.
     */
    public boolean addSymbolToScope(String symbolName, String symbolData) {
        if (!this.localScopes.isEmpty() && !this.localScopes.peek().scopeValues.containsKey(symbolName)) {
            this.localScopes.peek().scopeValues.put(symbolName, symbolData);
            return true;
        }
        return false;
    }


    /**
     * Método para verificar si un símbolo está en el scope actual (sin insertarlo a diferencia del anterior)
     * @param symbolName: nombre o identificador del símbolo
     * @return: true si se encuentra, false de lo contrario
     */
    public boolean isInsideLocalScope(String symbolName) {
        for (Scope localScope : localScopes)
            if (localScope.scopeValues.containsKey(symbolName)) return true;
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
     * Método para extraer el tipo de un símbolo que pertenezca a una función, variable o literal
     * @param hash: llave con la que se accede a las estructuras de la clase a revisar el símbolo
     * @return vacío si el símbolo no está presente, o el string de su tipo si lo está
     */
    public String getType(String hash) {
        if (isDataType(hash)) {return hash;}
        if (isInGlobalScope(hash)) {
            globalTable.get(hash);
            // por la forma en la que almacenamos en la tabla de símbolos, el primer valor de este split es el tipo
            return globalTable.get(hash).split(":")[0];
        }

        for (Scope localScope : localScopes) {
            if (localScope.scopeValues.containsKey(hash)) {
                String[] info = localScope.scopeValues.get(hash).split(":");
                return info[0];
            }
        }
        return "";
    }


    /**
     * Función que verifica el tipo de operaciones coincide, se usa en asignaciones, hay otro para aritmética y lógica
     * @param left operando izquierdo de la expresión
     * @param right operando derecho de la expresión
     * @return si coinciden los tipos, un true
     */
    public boolean checkType(String left, String right) {
        String typeLeft = getType(left);
        String typeRight = getType(right);
        return typeLeft.equals(typeRight) && !typeLeft.isBlank();
    }


    /**
     * Función para verificar si una operación es válida conociendo sus operandos y el operador
     * @param right parte derecha de la expresión
     * @param operator símbolo o símbolos que representan el operando (puede ser hasta 2 por los de +=, *=)
     * @param left parte izquierda de la expresión
     * @return true si la operación cumple las restricciones del lenguaje, false si no es válida
     */
    public boolean isValidOperation(String right, String operator, String left) {
        String typeRight = getType(right);
        String typeLeft = getType(left);
        System.out.println("tipo izq: " + typeRight + "\ntipo der: " + typeLeft);
        return !(typeRight.equals("STRING") || typeLeft.equals("STRING") ||
                ((typeRight.equals("FLOAT") || typeLeft.equals("FLOAT")) && operator.equals("%"))) && typeRight.equals(typeLeft);
    }

    /**
     * Función para verificar las restricciones de una llamada a función. Funciona como un switch que el cup interpreta.
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

    /**
     * Función booleana para verificar si una función es parte del scope global
     * @param calledFunction: nombre de la función
     * @return valor de verdad representativo de su existencia en este scope. Una función no llega aquí hasta que termina
     *  su bloque local.
     */
    public boolean isInGlobalScope(String calledFunction) {
        return globalTable.containsKey(calledFunction);
    }

    /**
     * Función booleana para confirmar que la declaración de un arreglo con valores sea del tipo correcto
     * @param type: tipo del que se declaró el arreglo (int, char, string, bool, float)
     * @param data: los valores brindados dentro de un bloque para asignar al arreglo
     * @return falso si en algún momento encuentra un valor que no es del tipo del arreglo, true si todos eran del mismo tipo
     */
    public boolean checkArray(String type, String data) {
        String[] values = data.split(":");
        for (String value : values)
            if (!type.equals(value)) return false;
        return true;
    }

    /**
     * Función para saber si un índice cumple con restricción del lenguaje
     * @param index: el índice pasado al arreglo que se desea verificar
     * @return true si el valor pasado al índice evalúa a entero, false si evalúa a otra cosa
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
     * Método para escribir al archivo el valor en string de cada scope ants de que se saquen de la pila
     * Utiliza el BufferedWriter que se le pasa por parámetro a la clase en su constructor
     */
    public void writeScope() {
        if (!localScopes.empty()) {
            System.out.println(localScopes.peek());
            System.out.println();
        }
    }


    /**
     * Método para imprimir el scope global al archivo de salida
     * Igual que el anterior, este usa el BufferedWriter.
     */
    public void printTableSymbol() {
        System.out.println(globalTable);
        System.out.println();
    }

}