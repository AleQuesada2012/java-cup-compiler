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
    private int stackOffset = 0; // Track stack offset
    private Stack<String> registerPool = new Stack<>();


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



    public void StackMemory(int space) {
        this.textSection.append("addiu $sp, $sp, -").append(space).append("\n");
        stackOffset += space;
    }

    public void declareLocalVariable(String varName, String type) {
        this.textSection.append("addiu $sp, $sp, -4\n");
        this.textSection.append("sw $zero, 0($sp)\n");
        registerMap.put(varName, "0($sp)");
    }


    public void generateUnaryOperation(String varReg, String op) {
        if (op.equals("++")) {
            this.textSection.append("addi " + varReg + ", " + varReg + ", 1\n");
        } else if (op.equals("--")) {
            this.textSection.append("addi " + varReg + ", " + varReg + ", -1\n");
        }
    }

    public String generateLoadImmediate(String value) {
        String resultReg = getFreeRegister();
        this.textSection.append("li " + resultReg + ", " + value + "\n");
        return resultReg;
    }

    public void declareString(String value) {
        String escapedValue = value
                .replace("\\", "\\\\")  // Escape backslashes
                .replace("\"", "\\\"")  // Escape double quotes
                .replace("\n", "\\n")   // Escape newlines
                .replace("\t", "\\t");  // Escape tabs

        String label = "str_" + structCounter.get("STRING");

        this.dataSection.append(label + ": .asciiz \"" + escapedValue + "\"\n");

        this.dataSection.append(".align 2\n");

        structCounter.put("STRING", structCounter.get("STRING") + 1);
    }

    public void StackPush(String element) {
        this.textSection.append("addiu $sp, $sp, -4\n");
        this.textSection.append("sw " + element + ", 0($sp)\n");
    }



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

    public String generateArithmetic(String left, String op, String right) {
        String result = getFreeRegister();

        // Handle immediate values (constants)
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

    public void generateAssignment(String variable, String registerValue) {
        if (!registerMap.containsKey(variable)) {declareLocalVariable(variable, "INT");}
        String variableLocation = registerMap.get(variable);
        this.textSection.append("sw " + registerValue + ", " + variableLocation + "\n"); // Store value in the stack
    }

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


    public void makeWhileLoop(String conditionLabel) {
        String labelFinish = "whileEND" + structCounter.get("WHILE");
        this.textSection.append("j " + conditionLabel + "\n");
        this.textSection.append(labelFinish + ":\n");
    }


    public void writeToFile() {
        try (BufferedWriter buffwriter = new BufferedWriter(new FileWriter(this.fileName))) {
            buffwriter.write(".data\n");
            buffwriter.write(dataSection.toString());
            buffwriter.write("\n.text\n.globl main\nmain:\n");
            buffwriter.write(textSection.toString());
        }
        catch (Exception e) {e.printStackTrace();}
    }


    public void exitProgram() {this.textSection.append("li $v0, 10\nsyscall\n");}

    public void declareArray(String NameArray, int space) {
        this.dataSection.append(NameArray + ": .space " + (space * 4) + "\n"); // Allocate space for the array
    }

    public String generateArrayAccess(String nameArray, String registerIndex) {
        String resultaRegister = getFreeRegister();
        this.textSection.append("sll " + resultaRegister + ", " + registerIndex + ", 2\n"); // Multiply index by 4
        this.textSection.append("la $t0, " + nameArray + "\n"); // Load base address
        this.textSection.append("addu " + resultaRegister + ", " + resultaRegister + ", $t0\n"); // Calculate element address
        this.textSection.append("lw " + resultaRegister + ", 0(" + resultaRegister + ")\n"); // Load array element
        return resultaRegister;
    }

    public void generateFunctionEpilogue() {
        this.textSection.append("move $sp, $fp\n");
        this.textSection.append("lw $ra, 0($sp)\n");
        this.textSection.append("lw $fp, 4($sp)\n");
        this.textSection.append("addiu $sp, $sp, 8\n");
        this.textSection.append("jr $ra\n");
    }

    public void generateFunctionCall(String nameFunctions, String[] args) {
        for (int i = 0; i < args.length; i++) {this.textSection.append("move $a" + i + ", " + args[i] + "\n");}
        this.textSection.append("jal " + nameFunctions + "\n"); // Call function
    }

    public String generateIf(String Regularcondition) {
        String labelElse = "ELSE" + structCounter.get("IF");
        String labeldEnd = "ENDIF" + structCounter.get("IF");
        structCounter.put("IF", structCounter.get("IF") + 1);
        this.textSection.append("beqz " + Regularcondition + ", " + labelElse + "\n");
        return labelElse + ":" + labeldEnd;
    }

    public void generateElse(String labels) {
        String[] parts = labels.split(":");
        this.textSection.append("j " + parts[1] + "\n");
        this.textSection.append(parts[0] + ":\n");
    }

    public void generateEndIf(String labels) {
        String[] parts = labels.split(":");
        this.textSection.append(parts[1] + ":\n");
    }

    public String generateWhileStart() {
        String labelStart = "WHILE" + structCounter.get("WHILE");
        String labelEnd = "ENDWHILE" + structCounter.get("WHILE");
        structCounter.put("WHILE", structCounter.get("WHILE") + 1);
        this.textSection.append(labelStart + ":\n");
        return labelStart + ":" + labelEnd;
    }

    public void generateWhileCondition(String regularCondition, String labels) {
        String[] parts = labels.split(":");
        this.textSection.append("beqz " + regularCondition + ", " + parts[1] + "\n"); // Branch to end if false
    }

    public void generateWhileEnd(String labels) {
        String[] parts = labels.split(":");
        this.textSection.append("j " + parts[0] + "\n");
        this.textSection.append(parts[1] + ":\n");
    }



    public void generateArrayAssignment(String nameArray, String indexRegister, String regularValue) {
        String tempReg = getFreeRegister();
        this.textSection.append("sll " + tempReg + ", " + indexRegister + ", 2\n");
        this.textSection.append("la $t0, " + nameArray + "\n");
        this.textSection.append("addu " + tempReg + ", " + tempReg + ", $t0\n");
        this.textSection.append("sw " + regularValue + ", 0(" + tempReg + ")\n");
        freeRegister(tempReg);
    }

    public void generatePrint(String regularValue) {
        this.textSection.append("li $v0, 1\n");
        this.textSection.append("move $a0, " + regularValue + "\n");
        this.textSection.append("syscall\n");
    }

    public String generateRead() {
        String resultRegister = getFreeRegister();
        this.textSection.append("li $v0, 5\n");
        this.textSection.append("syscall\n");
        this.textSection.append("move " + resultRegister + ", $v0\n");
        return resultRegister;
    }

    public String generateLogicalAnd(String leftRegister, String rightRegister) {
        String resultRegister = getFreeRegister();
        this.textSection.append("and " + resultRegister + ", " + leftRegister + ", " + rightRegister + "\n");
        return resultRegister;
    }

    public String generateLogicalOr(String leftRegister, String rightRegister) {
        String resultRegister = getFreeRegister();
        this.textSection.append("or " + resultRegister + ", " + leftRegister + ", " + rightRegister + "\n");
        return resultRegister;
    }

    public String generateLogicalNot(String exprReg) {
        String resultRegister = getFreeRegister();
        this.textSection.append("seq " + resultRegister + ", " + exprReg + ", $zero\n"); // NOT operation: resultReg = (exprReg == 0)
        return resultRegister;
    }


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




    public void generateBreak() {
        if (!structController.isEmpty()) {
            String[] generalData = structController.peek().split(":");
            String structType = generalData[0].toUpperCase();
            if (structType.equals("WHILE") || structType.equals("FOR") || structType.equals("SWITCH")) {
                this.textSection.append("j " + structType + "END" + generalData[1] + "\n");
            }
        }
    }

    public String generateBoolean(String value) {
        String resultRegister = getFreeRegister();
        this.textSection.append("li " + resultRegister + ", " + (value.equals("true") ? "1" : "0") + "\n");
        return resultRegister;
    }

    public void declareGlobalVariable(String varName, String type) {this.dataSection.append(varName + ": .word 0\n");}


    public String popFromStack() {
        String resultRegister = getFreeRegister();
        this.textSection.append("lw " + resultRegister + ", 0($sp)\n");
        this.textSection.append("addiu $sp, $sp, 4\n");
        return resultRegister;
    }


    public void FreeEspace(int space) {
        this.textSection.append("addiu $sp, $sp, ").append(space).append("\n");
        stackOffset -= space;
    }

    public String spaceforVariable(String variableName, String type) {
        StackMemory(4);
        registerMap.put(variableName, stackOffset + "($sp)");
        return registerMap.get(variableName);
    }

    public void generateReturn(String returnValue) {
        this.textSection.append("move $v0, " + returnValue + "\n"); // Set return value
        generateFunctionEpilogue();
    }


    public void generateIncrement(String variableReg) {this.textSection.append("addi " + variableReg + ", " + variableReg + ", 1\n");}

    public void generateDecrement(String varReg) {this.textSection.append("addi " + varReg + ", " + varReg + ", -1\n");}

    public void generateSwitch(String exprRegister, String[] cases, String standardLabel) {
        for (String caseLabel : cases) {this.textSection.append("beq " + exprRegister + ", " + caseLabel + "\n");}
        this.textSection.append("j " + standardLabel + "\n"); // Jump to default case
    }

    public void declareString(String variableName, String value) {this.dataSection.append(variableName + ": .asciiz \"" + value + "\"\n");}

    public void generatePrintString(String labelString) {
        this.textSection.append("li $v0, 4\n");
        this.textSection.append("la $a0, " + labelString + "\n");
        this.textSection.append("syscall\n");
    }

    public void generatePrintChar(String registerChar) {
        this.textSection.append("li $v0, 11\n");
        this.textSection.append("move $a0, " + registerChar + "\n");
        this.textSection.append("syscall\n");
    }

    public String generateForStart() {
        String startLabel = "FOR" + structCounter.get("FOR");
        String endLabel = "ENDFOR" + structCounter.get("FOR");
        structCounter.put("FOR", structCounter.get("FOR") + 1);
        this.textSection.append(startLabel + ":\n");
        return startLabel + ":" + endLabel;
    }

    public void generateForCondition(String conditionRegisters, String labels) {
        String[] partss = labels.split(":");
        this.textSection.append("beqz " + conditionRegisters + ", " + partss[1] + "\n");
    }

    public void printInt(int valuePrint) {this.textSection.append("li $v0, 1\nli $a0, " + valuePrint + "\nsyscall\n");}

    public void generateFunctionPrologue(String nameFunction) {
        this.textSection.append(nameFunction + ":\n");
        this.textSection.append("addiu $sp, $sp, -8\n");
        this.textSection.append("sw $fp, 4($sp)\n");
        this.textSection.append("sw $ra, 0($sp)\n");
        this.textSection.append("move $fp, $sp\n");
    }

    private int getStruct(String structKey) {return structCounter.getOrDefault(structKey.toUpperCase(), -1);}

    public void createStartLabel(String keyStruct) {
        keyStruct = keyStruct.toUpperCase();
        int counterStruct = structCounter.get(keyStruct);
        this.textSection.append(keyStruct + "Inicio" + counterStruct + ":\n");
        this.structController.add(keyStruct + ":" + counterStruct);
        structCounter.put(keyStruct, counterStruct + 1);
    }

    public void createEndLabel() {
        if (!structController.empty()) {
            String[] dataGeneral = this.structController.pop().split(":");
            this.textSection.append(dataGeneral[0] + "FIN" + dataGeneral[1] + ":\n");
        }
    }


    public void breakLastestLoop() {
        for (int i = structController.size() - 1; i >= 0; i--) {
            String[] dataGeneral = this.structController.get(i).split(":");
            String structType = dataGeneral[0].toUpperCase();
            if (structType.equals("SWITCH") || structType.equals("FOR") || structType.equals("WHILE")) {
                this.textSection.append("j " + structType + "END" + dataGeneral[1] + "\n");
            }
        }
    }

    public void generateForUpdate(String codeUpdate, String labels) {
        String[] parts = labels.split(":");
        this.textSection.append(codeUpdate + "\n");
        this.textSection.append("j " + parts[0] + "\n");
        this.textSection.append(parts[1] + ":\n");
    }
}