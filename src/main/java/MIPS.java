package main.java;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;


public class MIPS {

    private String fileName;
    public StringBuilder textSection;
    private StringBuilder dataSection;
    private Hashtable<String, Integer> structCounter = new Hashtable<>();

    private Stack<String> structController; //--> Como los scopes, saber la estructura en la que voy para saber dónde saltar.
    private HashMap<String,String> registerMap; //sin usar aún --> para saber que variable quedó en qué registro.
    private HashMap<String, Stack<String>> registerHandler; //sin usar aún --> saber que registros están disponibles.

    public MIPS(String outputFileName) {
        this.fileName = outputFileName;
        this.textSection = new StringBuilder();
        this.dataSection = new StringBuilder();
        this.structController = new Stack<>();
        this.registerMap = new HashMap<>();
        this.registerHandler = new HashMap<>();

        String[] structures = {"IF", "ELSE", "WHILE", "FOR", "SWITCH", "CASE", "DEFAULT"};
        for (String struct : structures) {
            structCounter.put(struct, 0);
        }
    }

    public String getAvailableRegister() {
        if (!registerHandler.containsKey("TEMP")) {
            registerHandler.put("TEMP", new Stack<>());
            // Initialize temporary registers
            for (int i = 0; i < 10; i++) {
                registerHandler.get("TEMP").push("$t" + i);
            }
        }
        // Get a free register
        if (!registerHandler.get("TEMP").isEmpty()) {
            return registerHandler.get("TEMP").pop();
        }

        return "$zero";
    }

    public void freeRegister(String reg) {
        if (reg.startsWith("$t")) {
            registerHandler.get("TEMP").push(reg);
        }
    }

    public String generateArithmetic(String left, String op, String right) {
        String resultReg = getAvailableRegister();

        // Handle immediate values (constants)
        if (left.matches("\\d+")) {
            this.textSection.append("li $t9, " + left + "\n"); // Load immediate into a temporary register
            left = "$t9";
        }
        if (right.matches("\\d+")) {
            this.textSection.append("li $t8, " + right + "\n"); // Load immediate into a temporary register
            right = "$t8";
        }

        switch (op) {
            case "+":
                this.textSection.append("add " + resultReg + ", " + left + ", " + right + "\n");
                break;
            case "-":
                this.textSection.append("sub " + resultReg + ", " + left + ", " + right + "\n");
                break;
            case "*":
                this.textSection.append("mul " + resultReg + ", " + left + ", " + right + "\n");
                break;
            case "/":
                this.textSection.append("div " + left + ", " + right + "\n");
                this.textSection.append("mflo " + resultReg + "\n");
                break;

            case "%":
                this.textSection.append("div " + left + ", " + right + "\n");
                this.textSection.append("mfhi " + resultReg + "\n"); // Get remainder
                break;
        }

        return resultReg;
    }


    public void generateAssignment(String var, String valueReg) {
        if (!registerMap.containsKey(var)) {
            registerMap.put(var, "$s" + registerMap.size());
        }
        String varReg = registerMap.get(var);

        this.textSection.append("move " + varReg + ", " + valueReg + "\n");
    }

    public String generateCondition(String left, String op, String right) {
        String condLabel = "COND" + structCounter.get("IF");
        String endLabel = "COND_END" + structCounter.get("IF");

        structCounter.put("IF", structCounter.get("IF") + 1);

        switch (op) {
            case "==": this.textSection.append("beq " + left + ", " + right + ", " + condLabel + "\n"); break;
            case "!=": this.textSection.append("bne " + left + ", " + right + ", " + condLabel + "\n"); break;
            case "<":  this.textSection.append("blt " + left + ", " + right + ", " + condLabel + "\n"); break;
            case "<=": this.textSection.append("ble " + left + ", " + right + ", " + condLabel + "\n"); break;
            case ">":  this.textSection.append("bgt " + left + ", " + right + ", " + condLabel + "\n"); break;
            case ">=": this.textSection.append("bge " + left + ", " + right + ", " + condLabel + "\n"); break;
        }

        this.textSection.append("j " + endLabel + "\n"); // Jump to end if condition is false
        this.textSection.append(condLabel + ":\n"); // Start of true block

        return endLabel;
    }

/*
    public void generateWhileLoop(String conditionLabel) {
        String endLabel = "whileEND" + structCounter.get("WHILE");

        this.textSection.append("j " + conditionLabel + "\n");
        this.textSection.append(endLabel + ":\n");
    }
*/

    public void writeToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.fileName))) {
            writer.write(".data\n");
            writer.write(dataSection.toString());
            writer.write("\n.text\n.globl main\nmain:\n");
            writer.write(textSection.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private int getStruct(String structKey) {
        return structCounter.getOrDefault(structKey.toUpperCase(), -1);
    } //la idea es que cuando pide algo del Struct counter funcione como un secuence de sql

    public void createStartLabel(String structKey) {
        structKey = structKey.toUpperCase(); // Convertir a mayúsculas
        int structCount = structCounter.get(structKey);

        this.textSection.append(structKey + "Inicio" + structCount + ":\n");
        this.structController.add(structKey + ":" + structCount);
        structCounter.put(structKey, structCount + 1);
    }

    public void createEndLabel() {
        if (!structController.empty()) {
            String[] data = this.structController.pop().split(":");
            this.textSection.append(data[0] + "FIN" + data[1] + ":\n");
        }
    }


    public void breakLastestLoop() {
        for (int i = structController.size() - 1; i >= 0; i--) {
            String[] data = this.structController.get(i).split(":");
            String structType = data[0].toUpperCase();

            if (structType.equals("SWITCH") || structType.equals("FOR") || structType.equals("WHILE")) {
                this.textSection.append("j " + structType + "END" + data[1] + "\n");
            }
        }
    }

    public void exitProgram() {
        this.textSection.append("li $v0, 10\nsyscall\n");
    }

    public void declareArray(String arrayName, int size) {
        this.dataSection.append(arrayName + ": .space " + (size * 4) + "\n"); // Allocate space for the array
    }

    public String generateArrayAccess(String arrayName, String indexReg) {
        String resultReg = getAvailableRegister();
        this.textSection.append("mul " + indexReg + ", " + indexReg + ", 4\n"); // Multiply index by 4 (assuming int size = 4 bytes)
        this.textSection.append("la $t0, " + arrayName + "\n"); // Load base address of array
        this.textSection.append("add $t0, $t0, " + indexReg + "\n"); // Get element address
        this.textSection.append("lw " + resultReg + ", 0($t0)\n"); // Load array element into register
        return resultReg;
    }


    public void printInt(int print_value) {
        this.textSection.append("li $v0, 1\nli $a0, " + print_value + "\nsyscall\n");
    }

    public void generateFunctionPrologue(String funcName) {
        this.textSection.append(funcName + ":\n");
        this.textSection.append("addiu $sp, $sp, -8\n"); // Adjust stack pointer
        this.textSection.append("sw $fp, 4($sp)\n");     // Save frame pointer
        this.textSection.append("sw $ra, 0($sp)\n");     // Save return address
        this.textSection.append("move $fp, $sp\n");      // Set frame pointer
    }

    public void generateFunctionEpilogue() {
        this.textSection.append("move $sp, $fp\n");      // Restore stack pointer
        this.textSection.append("lw $ra, 0($sp)\n");     // Restore return address
        this.textSection.append("lw $fp, 4($sp)\n");     // Restore frame pointer
        this.textSection.append("addiu $sp, $sp, 8\n");  // Adjust stack pointer
        this.textSection.append("jr $ra\n");             // Jump to return address
    }

    public void generateFunctionCall(String funcName, String[] args) {
        for (int i = 0; i < args.length; i++) {
            this.textSection.append("move $a" + i + ", " + args[i] + "\n"); // Pass arguments
        }
        this.textSection.append("jal " + funcName + "\n"); // Call function
    }

    public void generateReturn(String returnValue) {
        this.textSection.append("move $v0, " + returnValue + "\n"); // Set return value
        generateFunctionEpilogue();
    }

    public String generateIf(String conditionReg) {
        String elseLabel = "ELSE" + structCounter.get("IF");
        String endLabel = "ENDIF" + structCounter.get("IF");
        structCounter.put("IF", structCounter.get("IF") + 1);

        this.textSection.append("beqz " + conditionReg + ", " + elseLabel + "\n"); // Branch to else if false
        return elseLabel + ":" + endLabel;
    }

    public void generateElse(String labels) {
        String[] parts = labels.split(":");
        this.textSection.append("j " + parts[1] + "\n"); // Jump to end of if
        this.textSection.append(parts[0] + ":\n");       // Else label
    }

    public void generateEndIf(String labels) {
        String[] parts = labels.split(":");
        this.textSection.append(parts[1] + ":\n");       // End of if label
    }

    public String generateWhileStart() {
        String startLabel = "WHILE" + structCounter.get("WHILE");
        String endLabel = "ENDWHILE" + structCounter.get("WHILE");
        structCounter.put("WHILE", structCounter.get("WHILE") + 1);

        this.textSection.append(startLabel + ":\n");
        return startLabel + ":" + endLabel;
    }

    public void generateWhileCondition(String conditionReg, String labels) {
        String[] parts = labels.split(":");
        this.textSection.append("beqz " + conditionReg + ", " + parts[1] + "\n"); // Branch to end if false
    }

    public void generateWhileEnd(String labels) {
        String[] parts = labels.split(":");
        this.textSection.append("j " + parts[0] + "\n"); // Jump back to start
        this.textSection.append(parts[1] + ":\n");       // End of while label
    }

    public String generateForStart() {
        String startLabel = "FOR" + structCounter.get("FOR");
        String endLabel = "ENDFOR" + structCounter.get("FOR");
        structCounter.put("FOR", structCounter.get("FOR") + 1);

        this.textSection.append(startLabel + ":\n");
        return startLabel + ":" + endLabel;
    }

    public void generateForCondition(String conditionReg, String labels) {
        String[] parts = labels.split(":");
        this.textSection.append("beqz " + conditionReg + ", " + parts[1] + "\n"); // Branch to end if false
    }

    public void generateForUpdate(String updateCode, String labels) {
        String[] parts = labels.split(":");
        this.textSection.append(updateCode + "\n"); // Execute update code
        this.textSection.append("j " + parts[0] + "\n"); // Jump back to start
        this.textSection.append(parts[1] + ":\n");       // End of for label
    }

    public void generateArrayAssignment(String arrayName, String indexReg, String valueReg) {
        this.textSection.append("mul " + indexReg + ", " + indexReg + ", 4\n"); // Multiply index by 4
        this.textSection.append("la $t0, " + arrayName + "\n"); // Load base address
        this.textSection.append("add $t0, $t0, " + indexReg + "\n"); // Calculate element address
        this.textSection.append("sw " + valueReg + ", 0($t0)\n"); // Store value
    }

    public void generatePrint(String valueReg) {
        this.textSection.append("li $v0, 1\n"); // Print integer
        this.textSection.append("move $a0, " + valueReg + "\n");
        this.textSection.append("syscall\n");
    }

    public String generateRead() {
        String resultReg = getAvailableRegister();
        this.textSection.append("li $v0, 5\n"); // Read integer
        this.textSection.append("syscall\n");
        this.textSection.append("move " + resultReg + ", $v0\n");
        return resultReg;
    }

    public void generateIncrement(String varReg) {
        this.textSection.append("addi " + varReg + ", " + varReg + ", 1\n");
    }

    public void generateDecrement(String varReg) {
        this.textSection.append("addi " + varReg + ", " + varReg + ", -1\n");
    }
    public String generateLogicalAnd(String leftReg, String rightReg) {
        String resultReg = getAvailableRegister();
        this.textSection.append("and " + resultReg + ", " + leftReg + ", " + rightReg + "\n");
        return resultReg;
    }

    public String generateLogicalOr(String leftReg, String rightReg) {
        String resultReg = getAvailableRegister();
        this.textSection.append("or " + resultReg + ", " + leftReg + ", " + rightReg + "\n");
        return resultReg;
    }
    public void generateSwitch(String exprReg, String[] cases, String defaultLabel) {
        for (String caseLabel : cases) {
            this.textSection.append("beq " + exprReg + ", " + caseLabel + "\n"); // Compare with case value
        }
        this.textSection.append("j " + defaultLabel + "\n"); // Jump to default case
    }

    public void declareString(String varName, String value) {
        this.dataSection.append(varName + ": .asciiz \"" + value + "\"\n");
    }

    public void generatePrintString(String stringLabel) {
        this.textSection.append("li $v0, 4\n"); // Print string
        this.textSection.append("la $a0, " + stringLabel + "\n");
        this.textSection.append("syscall\n");
    }

    public void generatePrintChar(String charReg) {
        this.textSection.append("li $v0, 11\n"); // Print character
        this.textSection.append("move $a0, " + charReg + "\n");
        this.textSection.append("syscall\n");
    }

    public String generateBoolean(String value) {
        String resultReg = getAvailableRegister();
        this.textSection.append("li " + resultReg + ", " + (value.equals("true") ? "1" : "0") + "\n");
        return resultReg;
    }

    public String generatePower(String baseReg, String exponentReg) {
        String resultReg = getAvailableRegister();
        String tempReg = getAvailableRegister();
        String loopLabel = "POW_LOOP" + structCounter.get("POW");
        String endLabel = "POW_END" + structCounter.get("POW");
        structCounter.put("POW", structCounter.get("POW") + 1);

        this.textSection.append("li " + resultReg + ", 1\n"); // Initialize result to 1
        this.textSection.append("move " + tempReg + ", " + exponentReg + "\n"); // Copy exponent
        this.textSection.append(loopLabel + ":\n");
        this.textSection.append("beqz " + tempReg + ", " + endLabel + "\n"); // Exit if exponent is 0
        this.textSection.append("mul " + resultReg + ", " + resultReg + ", " + baseReg + "\n"); // Multiply base
        this.textSection.append("addi " + tempReg + ", " + tempReg + ", -1\n"); // Decrement exponent
        this.textSection.append("j " + loopLabel + "\n");
        this.textSection.append(endLabel + ":\n");

        freeRegister(tempReg); // Free temporary register
        return resultReg;
    }

    public void generateBreak() {
        if (!structController.isEmpty()) {
            String[] data = structController.peek().split(":");
            String structType = data[0].toUpperCase();
            if (structType.equals("WHILE") || structType.equals("FOR") || structType.equals("SWITCH")) {
                this.textSection.append("j " + structType + "END" + data[1] + "\n");
            }
        }
    }

    public void declareGlobalVariable(String varName, String type) {
        this.dataSection.append(varName + ": .word 0\n"); // Initialize global variable
    }

    public void declareLocalVariable(String varName, String type) {
        this.textSection.append("addiu $sp, $sp, -4\n"); // Allocate space on stack
        this.textSection.append("sw $zero, 0($sp)\n");   // Initialize to 0
    }
}