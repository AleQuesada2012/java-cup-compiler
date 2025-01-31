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


    public void generateWhileLoop(String conditionLabel) {
        String endLabel = "whileEND" + structCounter.get("WHILE");

        this.textSection.append("j " + conditionLabel + "\n");
        this.textSection.append(endLabel + ":\n");
    }


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

}