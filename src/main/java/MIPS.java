package main.java;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;


public class MIPS {

    private String fileName;
    public StringBuilder textSection;
    private StringBuilder dataSection;
    private Hashtable<String, Integer> structCounter = new Hashtable<>();
    private Stack<String> structController;
    private Hashtable<String, String> registerMap;
    private Hashtable<String, Stack<String>> registerHandler;
    private int stackOffset = 0;
    private Stack<String> registerPool = new Stack<>();

    /**
     * Constructor de la clase MIPS.
     *
     * @param outputFileName Nombre del archivo de salida donde se escribirá el código MIPS.
     */
    public MIPS(String outputFileName) {
        this.fileName = outputFileName;
        this.textSection = new StringBuilder();
        this.dataSection = new StringBuilder();
        this.structController = new Stack<>();
        this.registerMap = new Hashtable<>();
        this.registerHandler = new Hashtable<>();

        String[] structures = {"IF", "ELSE", "WHILE", "FOR", "SWITCH", "CASE", "DEFAULT", "STRING"};
        for (String struct : structures) {
            structCounter.put(struct, 0);
        }
        for (int i = 0; i < 10; i++) {
            registerPool.push("$t" + i);
        }
    }

    /**
     * Reserva espacio en la pila.
     *
     * @param space Cantidad de espacio a reservar en la pila.
     */

    public void StackMemory(int space) {
        this.textSection.append("addiu $sp, $sp, -").append(space).append("\n");
        stackOffset += space;
    }

    /**
     * Declara una variable local en la pila.
     *
     * @param varName Nombre de la variable.
     * @param type    Tipo de la variable.
     */
    public void declareLocalVariable(String varName, String type) {
        this.textSection.append("addiu $sp, $sp, -4\n");
        this.textSection.append("sw $zero, 0($sp)\n");
        registerMap.put(varName, "0($sp)");
    }

    /**
     * Genera una operación unaria (incremento o decremento).
     *
     * @param varReg Registro que contiene la variable.
     * @param op     Operador a aplicar ("++" o "--").
     */
    public void generateUnaryOperation(String varReg, String op) {
        if (op.equals("++")) {
            this.textSection.append("addi " + varReg + ", " + varReg + ", 1\n");
        } else if (op.equals("--")) {
            this.textSection.append("addi " + varReg + ", " + varReg + ", -1\n");
        }
    }

    /**
     * Genera la carga de un valor inmediato en un registro.
     *
     * @param value Valor inmediato a cargar.
     * @return Registro que contiene el valor cargado.
     */
    public String generateLoadImmediate(String value) {
        String resultReg = getFreeRegister();
        this.textSection.append("li " + resultReg + ", " + value + "\n");
        return resultReg;
    }

    /**
     * Declara una cadena en la sección de datos.
     *
     * @param value Cadena a declarar.
     */
    public void declareString(String value) {
        String escapedValue = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t");

        String label = "str_" + structCounter.get("STRING");

        this.dataSection.append(label + ": .asciiz \"" + escapedValue + "\"\n");

        this.dataSection.append(".align 2\n");

        structCounter.put("STRING", structCounter.get("STRING") + 1);
    }


    /**
     * Empuja un elemento en la pila.
     *
     * @param element Elemento a empujar en la pila.
     */
    public void StackPush(String element) {
        this.textSection.append("addiu $sp, $sp, -4\n");
        this.textSection.append("sw " + element + ", 0($sp)\n");
    }


    /**
     * Obtiene un registro libre.
     *
     * @return Registro libre.
     */
    public String getFreeRegister() {
        if (!registerHandler.containsKey("TEMP")) {
            registerHandler.put("TEMP", new Stack<>());

            for (int i = 0; i < 10; i++) {registerHandler.get("TEMP").push("$t" + i);}
        }

        if (!registerHandler.get("TEMP").isEmpty()) {return registerHandler.get("TEMP").pop();}

        String registro = "$t0";
        StackPush(registro);
        return registro;
    }


    /**
     * Genera una operación aritmética.
     *
     * @param left  Operando izquierdo.
     * @param op    Operador aritmético.
     * @param right Operando derecho.
     * @return Registro que contiene el resultado de la operación.
     */
    public String generateArithmetic(String left, String op, String right) {
        String result = getFreeRegister();


        if (left.matches("\\d+")) {
            String temporalRegister = getFreeRegister();
            this.textSection.append("li " + temporalRegister + ", " + left + "\n");
            left = temporalRegister;
        }
        if (right.matches("\\d+")) {
            String temporalRegister = getFreeRegister();
            this.textSection.append("li " + temporalRegister + ", " + right + "\n");
            right = temporalRegister;
        }

        switch (op) {
            case "+":
                this.textSection.append("add " + result + ", " + left + ", " + right + "\n");
                break;
            case "-":
                this.textSection.append("sub " + result + ", " + left + ", " + right + "\n");
                break;
            case "*":
                this.textSection.append("mul " + result + ", " + left + ", " + right + "\n");
                break;
            case "/":
                this.textSection.append("div " + left + ", " + right + "\n");
                this.textSection.append("mflo " + result + "\n");
                break;
            case "%":
                this.textSection.append("div " + left + ", " + right + "\n");
                this.textSection.append("mfhi " + result + "\n");
                break;
        }
        freeRegister(left);
        freeRegister(right);
        return result;
    }


    /**
     * Genera una asignación de valor a una variable.
     *
     * @param variable     Nombre de la variable.
     * @param registerValue Registro que contiene el valor a asignar.
     */
    public void generateAssignment(String variable, String registerValue) {
        if (!registerMap.containsKey(variable)) {declareLocalVariable(variable, "INT");}
        String variableLocation = registerMap.get(variable);
        this.textSection.append("sw " + registerValue + ", " + variableLocation + "\n");
    }

    /**
     * Genera una condición para una estructura de control.
     *
     * @param left  Operando izquierdo.
     * @param op    Operador de comparación.
     * @param right Operando derecho.
     * @return Etiqueta de fin de la condición.
     */
    public String generateCondition(String left, String op, String right) {
        String condiotionalLabel = "COND" + structCounter.get("IF");
        String endLabel = "COND_END" + structCounter.get("IF");

        structCounter.put("IF", structCounter.get("IF") + 1);

        switch (op) {
            case "==": this.textSection.append("beq " + left + ", " + right + ", " + condiotionalLabel + "\n"); break;
            case "!=": this.textSection.append("bne " + left + ", " + right + ", " + condiotionalLabel + "\n"); break;
            case "<":  this.textSection.append("blt " + left + ", " + right + ", " + condiotionalLabel + "\n"); break;
            case "<=": this.textSection.append("ble " + left + ", " + right + ", " + condiotionalLabel + "\n"); break;
            case ">":  this.textSection.append("bgt " + left + ", " + right + ", " + condiotionalLabel + "\n"); break;
            case ">=": this.textSection.append("bge " + left + ", " + right + ", " + condiotionalLabel + "\n"); break;
        }
        this.textSection.append("j " + endLabel + "\n");
        this.textSection.append(condiotionalLabel + ":\n");
        return endLabel;
    }

    /**
     * Genera el fin de un bucle while.
     *
     * @param conditionLabel Etiqueta de la condición del bucle.
     */
    public void makeWhileLoop(String conditionLabel) {
        String labelFinish = "whileEND" + structCounter.get("WHILE");
        this.textSection.append("j " + conditionLabel + "\n");
        this.textSection.append(labelFinish + ":\n");
    }

    /**
     * Escribe el código generado en un archivo.
     */
    public void writeToFile() {
        try (BufferedWriter buffwriter = new BufferedWriter(new FileWriter(this.fileName))) {
            buffwriter.write(".data\n");
            buffwriter.write(dataSection.toString());
            buffwriter.write("\n.text\n.globl main\nmain:\n");
            buffwriter.write(textSection.toString());
        }
        catch (Exception e) {e.printStackTrace();}
    }

    /**
     * Genera la instrucción para terminar el programa.
     */
    public void exitProgram() {this.textSection.append("li $v0, 10\nsyscall\n");}
    /**
     * Declara un arreglo en la sección de datos.
     *
     * @param NameArray Nombre del arreglo.
     * @param space     Tamaño del arreglo en bytes.
     */
    public void declareArray(String NameArray, int space) {
        this.dataSection.append(NameArray + ": .space " + (space * 4) + "\n");
    }
    /**
     * Genera el acceso a un elemento de un arreglo.
     *
     * @param nameArray    Nombre del arreglo.
     * @param registerIndex Registro que contiene el índice del arreglo.
     * @return Registro que contiene el valor del elemento del arreglo.
     */
    public String generateArrayAccess(String nameArray, String registerIndex) {
        String resultaRegister = getFreeRegister();
        this.textSection.append("sll " + resultaRegister + ", " + registerIndex + ", 2\n");
        this.textSection.append("la $t0, " + nameArray + "\n");
        this.textSection.append("addu " + resultaRegister + ", " + resultaRegister + ", $t0\n");
        this.textSection.append("lw " + resultaRegister + ", 0(" + resultaRegister + ")\n");
        return resultaRegister;
    }

    /**
     * Genera el epílogo de una función.
     */
    public void generateFunctionEpilogue() {
        this.textSection.append("move $sp, $fp\n");
        this.textSection.append("lw $ra, 0($sp)\n");
        this.textSection.append("lw $fp, 4($sp)\n");
        this.textSection.append("addiu $sp, $sp, 8\n");
        this.textSection.append("jr $ra\n");
    }
    /**
     * Genera la llamada a una función.
     *
     * @param nameFunctions Nombre de la función.
     * @param args          Argumentos de la función.
     */
    public void generateFunctionCall(String nameFunctions, String[] args) {
        for (int i = 0; i < args.length; i++) {this.textSection.append("move $a" + i + ", " + args[i] + "\n");}
        this.textSection.append("jal " + nameFunctions + "\n");
    }

    /**
     * Genera una estructura if.
     *
     * @param Regularcondition Condición del if.
     * @return Etiquetas asociadas al if.
     */
    public String generateIf(String Regularcondition) {
        String labelElse = "ELSE" + structCounter.get("IF");
        String labeldEnd = "ENDIF" + structCounter.get("IF");
        structCounter.put("IF", structCounter.get("IF") + 1);
        this.textSection.append("beqz " + Regularcondition + ", " + labelElse + "\n");
        return labelElse + ":" + labeldEnd;
    }

    /**
     * Genera una estructura else.
     *
     * @param labels Etiquetas asociadas al if.
     */
    public void generateElse(String labels) {
        String[] parts = labels.split(":");
        this.textSection.append("j " + parts[1] + "\n");
        this.textSection.append(parts[0] + ":\n");
    }

    /**
     * Genera el fin de una estructura if.
     *
     * @param labels Etiquetas asociadas al if.
     */
    public void generateEndIf(String labels) {
        String[] parts = labels.split(":");
        this.textSection.append(parts[1] + ":\n");
    }
    /**
     * Genera el inicio de un bucle while.
     *
     * @return Etiquetas asociadas al bucle while.
     */
    public String generateWhileStart() {
        String labelStart = "WHILE" + structCounter.get("WHILE");
        String labelEnd = "ENDWHILE" + structCounter.get("WHILE");
        structCounter.put("WHILE", structCounter.get("WHILE") + 1);
        this.textSection.append(labelStart + ":\n");
        return labelStart + ":" + labelEnd;
    }
    /**
     * Genera la condición de un bucle while.
     *
     * @param regularCondition Condición del bucle while.
     * @param labels           Etiquetas asociadas al bucle while.
     */
    public void generateWhileCondition(String regularCondition, String labels) {
        String[] parts = labels.split(":");
        this.textSection.append("beqz " + regularCondition + ", " + parts[1] + "\n");
    }
    /**
     * Genera el fin de un bucle while.
     *
     * @param labels Etiquetas asociadas al bucle while.
     */
    public void generateWhileEnd(String labels) {
        String[] parts = labels.split(":");
        this.textSection.append("j " + parts[0] + "\n");
        this.textSection.append(parts[1] + ":\n");
    }
    /**
     * Genera la asignación de un valor a un elemento de un arreglo.
     *
     * @param nameArray      Nombre del arreglo.
     * @param indexRegister  Registro que contiene el índice del arreglo.
     * @param regularValue   Valor a asignar.
     */
    public void generateArrayAssignment(String nameArray, String indexRegister, String regularValue) {
        String tempReg = getFreeRegister();
        this.textSection.append("sll " + tempReg + ", " + indexRegister + ", 2\n");
        this.textSection.append("la $t0, " + nameArray + "\n");
        this.textSection.append("addu " + tempReg + ", " + tempReg + ", $t0\n");
        this.textSection.append("sw " + regularValue + ", 0(" + tempReg + ")\n");
        freeRegister(tempReg);
    }
    /**
     * Genera la impresión de un valor entero.
     *
     * @param regularValue Valor a imprimir.
     */
    public void generatePrint(String regularValue) {
        this.textSection.append("li $v0, 1\n");
        this.textSection.append("move $a0, " + regularValue + "\n");
        this.textSection.append("syscall\n");
    }
    /**
     * Genera la lectura de un valor entero desde la entrada estándar.
     *
     * @return Registro que contiene el valor leído.
     */
    public String generateRead() {
        String resultRegister = getFreeRegister();
        this.textSection.append("li $v0, 5\n");
        this.textSection.append("syscall\n");
        this.textSection.append("move " + resultRegister + ", $v0\n");
        return resultRegister;
    }
    /**
     * Genera una operación lógica AND.
     *
     * @param leftRegister  Registro que contiene el operando izquierdo.
     * @param rightRegister Registro que contiene el operando derecho.
     * @return Registro que contiene el resultado de la operación.
     */
    public String generateLogicalAnd(String leftRegister, String rightRegister) {
        String resultRegister = getFreeRegister();
        this.textSection.append("and " + resultRegister + ", " + leftRegister + ", " + rightRegister + "\n");
        return resultRegister;
    }
    /**
     * Genera una operación lógica OR.
     *
     * @param leftRegister  Registro que contiene el operando izquierdo.
     * @param rightRegister Registro que contiene el operando derecho.
     * @return Registro que contiene el resultado de la operación.
     */
    public String generateLogicalOr(String leftRegister, String rightRegister) {
        String resultRegister = getFreeRegister();
        this.textSection.append("or " + resultRegister + ", " + leftRegister + ", " + rightRegister + "\n");
        return resultRegister;
    }
    /**
     * Genera una operación lógica NOT.
     *
     * @param registerExpression Registro que contiene el operando.
     * @return Registro que contiene el resultado de la operación.
     */
    public String generateLogicalNot(String registerExpression) {
        String resultRegister = getFreeRegister();
        this.textSection.append("seq " + resultRegister + ", " + registerExpression + ", $zero\n");
        return resultRegister;
    }
    /**
     * Libera un registro para su reutilización.
     *
     * @param register Registro a liberar.
     */

    public void freeRegister(String register) {
        if (register.startsWith("$t")) {
            if (!registerHandler.containsKey("TEMP")) {registerHandler.put("TEMP", new Stack<>());}
            registerHandler.get("TEMP").push(register);
        }
        else if (register.startsWith("$s")) {
            if (!registerHandler.containsKey("SAVED")) {registerHandler.put("SAVED", new Stack<>());}
            registerHandler.get("SAVED").push(register);
        }
    }



    /**
     * Genera una instrucción de salto para salir de un bucle o estructura de control.
     */
    public void generateBreak() {
        if (!structController.isEmpty()) {
            String[] generalData = structController.peek().split(":");
            String structType = generalData[0].toUpperCase();
            if (structType.equals("WHILE") || structType.equals("FOR") || structType.equals("SWITCH")) {
                this.textSection.append("j " + structType + "END" + generalData[1] + "\n");
            }
        }
    }

    /**
     * Genera un valor booleano (1 para true, 0 para false) y lo almacena en un registro.
     *
     * @param value Valor booleano ("true" o "false").
     * @return Registro que contiene el valor booleano.
     */
    public String generateBoolean(String value) {
        String resultRegister = getFreeRegister();
        this.textSection.append("li " + resultRegister + ", " + (value.equals("true") ? "1" : "0") + "\n");
        return resultRegister;
    }

    /**
     * Declara una variable global en la sección de datos.
     *
     * @param varName Nombre de la variable global.
     * @param type    Tipo de la variable.
     */
    public void declareGlobalVariable(String varName, String type) {this.dataSection.append(varName + ": .word 0\n");}

    /**
     * Extrae un valor de la pila y lo almacena en un registro.
     *
     * @return Registro que contiene el valor extraído de la pila.
     */
    public String popFromStack() {
        String resultRegister = getFreeRegister();
        this.textSection.append("lw " + resultRegister + ", 0($sp)\n");
        this.textSection.append("addiu $sp, $sp, 4\n");
        return resultRegister;
    }

    /**
     * Libera espacio en la pila.
     *
     * @param space Cantidad de espacio a liberar en la pila.
     */
    public void FreeEspace(int space) {
        this.textSection.append("addiu $sp, $sp, ").append(space).append("\n");
        stackOffset -= space;
    }

    /**
     * Reserva espacio en la pila para una variable y la asocia con un registro.
     *
     * @param variableName Nombre de la variable.
     * @param type         Tipo de la variable.
     * @return Ubicación de la variable en la pila.
     */
    public String spaceforVariable(String variableName, String type) {
        StackMemory(4);
        registerMap.put(variableName, stackOffset + "($sp)");
        return registerMap.get(variableName);
    }
    /**
     * Genera una instrucción de retorno para una función.
     *
     * @param returnValue Valor a retornar.
     */
    public void generateReturn(String returnValue) {
        this.textSection.append("move $v0, " + returnValue + "\n");
        generateFunctionEpilogue();
    }

    /**
     * Genera una instrucción de incremento para una variable.
     *
     * @param variableReg Registro que contiene la variable.
     */
    public void generateIncrement(String variableReg) {this.textSection.append("addi " + variableReg + ", " + variableReg + ", 1\n");}
    /**
     * Genera una instrucción de decremento para una variable.
     *
     * @param varReg Registro que contiene la variable.
     */
    public void generateDecrement(String varReg) {this.textSection.append("addi " + varReg + ", " + varReg + ", -1\n");}
    /**
     * Genera una estructura switch.
     *
     * @param exprRegister  Registro que contiene la expresión a evaluar.
     * @param cases         Casos del switch.
     * @param standardLabel Etiqueta para el caso por defecto.
     */
    public void generateSwitch(String exprRegister, String[] cases, String standardLabel) {
        for (String caseLabel : cases) {this.textSection.append("beq " + exprRegister + ", " + caseLabel + "\n");}
        this.textSection.append("j " + standardLabel + "\n");
    }
    /**
     * Declara una cadena en la sección de datos.
     *
     * @param variableName Nombre de la variable de cadena.
     * @param value        Valor de la cadena.
     */
    public void declareString(String variableName, String value) {this.dataSection.append(variableName + ": .asciiz \"" + value + "\"\n");}
    /**
     * Genera la impresión de una cadena.
     *
     * @param labelString Etiqueta de la cadena a imprimir.
     */
    public void generatePrintString(String labelString) {
        this.textSection.append("li $v0, 4\n");
        this.textSection.append("la $a0, " + labelString + "\n");
        this.textSection.append("syscall\n");
    }


    /**
     * Genera la impresión de un carácter.
     *
     * @param registerChar Registro que contiene el carácter a imprimir.
     */
    public void generatePrintChar(String registerChar) {
        this.textSection.append("li $v0, 11\n");
        this.textSection.append("move $a0, " + registerChar + "\n");
        this.textSection.append("syscall\n");
    }

    /**
     * Genera el inicio de un bucle for.
     *
     * @return Etiquetas asociadas al bucle for.
     */
    public String generateForStart() {
        String startLabel = "FOR" + structCounter.get("FOR");
        String endLabel = "ENDFOR" + structCounter.get("FOR");
        structCounter.put("FOR", structCounter.get("FOR") + 1);
        this.textSection.append(startLabel + ":\n");
        return startLabel + ":" + endLabel;
    }
    /**
     * Genera la condición de un bucle for.
     *
     * @param conditionRegisters Condición del bucle for.
     * @param labels             Etiquetas asociadas al bucle for.
     */
    public void generateForCondition(String conditionRegisters, String labels) {
        String[] partss = labels.split(":");
        this.textSection.append("beqz " + conditionRegisters + ", " + partss[1] + "\n");
    }

    /**
     * Genera la impresión de un valor entero.
     *
     * @param valuePrint Valor entero a imprimir.
     */
    public void printInt(int valuePrint) {this.textSection.append("li $v0, 1\nli $a0, " + valuePrint + "\nsyscall\n");}

    /**
     * Genera el prólogo de una función.
     *
     * @param nameFunction Nombre de la función.
     */
    public void generateFunctionPrologue(String nameFunction) {
        this.textSection.append(nameFunction + ":\n");
        this.textSection.append("addiu $sp, $sp, -8\n");
        this.textSection.append("sw $fp, 4($sp)\n");
        this.textSection.append("sw $ra, 0($sp)\n");
        this.textSection.append("move $fp, $sp\n");
    }

    /**
     * Obtiene el contador de una estructura específica.
     *
     * @param structKey Nombre de la estructura.
     * @return Contador de la estructura o -1 si no existe.
     */
    private int getStruct(String structKey) {return structCounter.getOrDefault(structKey.toUpperCase(), -1);}
    /**
     * Crea una etiqueta de inicio para una estructura de control.
     *
     * @param keyStruct Nombre de la estructura de control.
     */
    public void createStartLabel(String keyStruct) {
        keyStruct = keyStruct.toUpperCase();
        int counterStruct = structCounter.get(keyStruct);
        this.textSection.append(keyStruct + "Inicio" + counterStruct + ":\n");
        this.structController.add(keyStruct + ":" + counterStruct);
        structCounter.put(keyStruct, counterStruct + 1);
    }
    /**
     * Crea una etiqueta de fin para la última estructura de control abierta.
     */

    public void createEndLabel() {
        if (!structController.empty()) {
            String[] dataGeneral = this.structController.pop().split(":");
            this.textSection.append(dataGeneral[0] + "FIN" + dataGeneral[1] + ":\n");
        }
    }

    /**
     * Genera una instrucción de salto para salir del bucle más reciente.
     */
    public void breakLastestLoop() {
        for (int i = structController.size() - 1; i >= 0; i--) {
            String[] dataGeneral = this.structController.get(i).split(":");
            String structType = dataGeneral[0].toUpperCase();
            if (structType.equals("SWITCH") || structType.equals("FOR") || structType.equals("WHILE")) {
                this.textSection.append("j " + structType + "END" + dataGeneral[1] + "\n");
            }
        }
    }

    /**
     * Genera la actualización de un bucle for.
     *
     * @param codeUpdate Código de actualización del bucle for.
     * @param labels     Etiquetas asociadas al bucle for.
     */
    public void generateForUpdate(String codeUpdate, String labels) {
        String[] parts = labels.split(":");
        this.textSection.append(codeUpdate + "\n");
        this.textSection.append("j " + parts[0] + "\n");
        this.textSection.append(parts[1] + ":\n");
    }
}