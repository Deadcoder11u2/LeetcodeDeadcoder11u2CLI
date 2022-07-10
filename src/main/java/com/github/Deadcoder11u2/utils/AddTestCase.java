package com.github.Deadcoder11u2.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "tc", description = "Adding testcase for the problem")
public class AddTestCase implements Runnable{
    static HashMap<String, String> paramMapper;
    static {
        paramMapper = new HashMap<>();
        paramMapper.put("int", "int");
        paramMapper.put("I", "int");
        paramMapper.put("J", "long");
        paramMapper.put("F", "float");
        paramMapper.put("S", "short");
        paramMapper.put("Z", "boolean");
        paramMapper.put("C", "char");
        paramMapper.put("B", "byte");
        paramMapper.put("String", "String");
        paramMapper.put("Long", "Long");
        paramMapper.put("Integer", "Integer");
        paramMapper.put("Double", "Double");
        paramMapper.put("Character", "Character");
        paramMapper.put("Byte", "Byte");
    }
    final    String HEADER = "\033[95m";
    final    String OKBLUE = "\033[94m";
    final    String OKCYAN = "\033[96m";
    final    String OKGREEN = "\033[92m";
    final    String WARNING = "\033[93m";
    final    String FAIL = "\033[91m";
    final    String ENDC = "\033[0m";
    final    String BOLD = "\033[1m";
    final    String UNDERLINE = "\033[4m";
    final    String code =
        "import java.util.*;"+
        ""+
        "public class Main {" +
        "public static void main(String args[]) {"+
        "   Solution sol = new Solution();"+
        "   %s"+
        "}"+
        "}";
    @Option(names = {"-m", "--method"}, description = "Method number from the Soltion class file")
    int method = 0;

    @Option(names = {"-args", "-a"}, description = "Number of arguments of the method")
    int arguments = 0;

    @Option(names = {"-v", "--verbose"}, description = "Show all the scripts")
    boolean verbose;

    @Option(names = {"-t", "--test-cases"}, description = "Number of testcases")
    int tc = 1;
    @Override
    public void run() {
        String args[];
        String OPERATING_SYSTEM = System.getProperty("os.name");
        if(OPERATING_SYSTEM.startsWith("Windows")) {
            args = new String[]{"cmd", "/c"};
        }
        else {
            args = new String[]{"bash", "-c"};
        }

        File testFile = new File("methods" + method + ".test");
        Scanner fs = new Scanner(System.in);
        try {
            testFile.createNewFile();
            PrintWriter pw = new PrintWriter(testFile);
            for(int i = 0 ; i <  tc; i++) {
                for(int j = 0 ; j < arguments ; j++) {
                    pw.write(fs.nextLine());
                    pw.write("\n");
                }
            }
            pw.close();
        }
        catch(Exception e) {
        }
        fs.close();
        File mainFile = new File("Main.java");
        try {
            mainFile.createNewFile();
            StringBuilder sb = new StringBuilder();
            HashMap<String, String> arg = fetchMethod();
            for(String key: arg.keySet()) {
                if(key.equals("methodName")) continue;
                sb.append(arg.get(key) + key + ";");
            }
            BufferedReader br = new BufferedReader(new FileReader(testFile));
            for(int test= 0 ; test < tc ; test++) {
                for(int i = 0; i < arguments ; i++) {
                    String s = br.readLine().replace("[", "{").replace("]", "}");
                    if(isArray.get("arg"+i)) {
                        sb.append("arg" + i  + "="+ "new " + arg.get("arg" + i) +s + ";");
                    }else {
                        sb.append("arg" + i + "=" + s + ";") ;
                    }
                }
                String call = "System.out.println(sol." + arg.get("methodName") + "(";
                for(int i = 0 ; i < arguments ; i++) {
                    call += "arg" + i;
                    if(i != arguments - 1) {
                        call += ",";
                    }
                }
                call += "));";
                sb.append(call);
                String code_final = code;
                code_final = String.format(code_final, sb.toString());
                PrintWriter pw = new PrintWriter(mainFile);
                pw.write(code_final);
                pw.close();
            }
            br.close();
            Process process = new ProcessBuilder(args[0], args[1], "javac Main.java").start();
            process.waitFor();
            if(process.exitValue() != 0) throw new RuntimeException();
            System.out.println(OKBLUE + "Use the run to test solution against the testcases" + ENDC);
        }
        catch(Exception e) {
            System.out.println(FAIL + "Something went wrong" + ENDC);
        }
    }

    HashMap<String, Boolean> isArray = new HashMap<>();

    HashMap<String, String> fetchMethod() {
        HashMap<String, String> mp = new HashMap<>();
        File f = new File("methods.leetcode");
        String str = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            for(int i = 0 ; i < method; i++) {
                str = br.readLine();
            }
            br.close();
            String splits[] = str.split("->");
            mp.put("methodName", splits[0]);
            String param = splits[1];
            param = param.replace("{", "").replace("}", "").replace("Ljava.lang.", "").replace("class ", "").replace(";", "");
            String params[] = param.split(",");
            int paramCount = 0;
            for(String par: params) {
                int dimension = 0;
                int i;
                for(i = 0 ; i < par.length() ; i++) {
                    if(par.charAt(i) == '[') dimension++;
                    else break;
                }
                par = par.substring(i);
                String dataType = paramMapper.get(par);
                dataType += " ";
                dataType += "[]".repeat(dimension);
                mp.put("arg"+paramCount, dataType);
                if(dimension > 0) {
                    isArray.put("arg" + paramCount, true);
                }
                else {
                    isArray.put("arg" + paramCount, false);
                }
                paramCount++;
            }
        }
        catch(Exception e) {
            System.out.println(FAIL + "Method doesn't exist" + ENDC);
            System.exit(-1);
        }
        return mp;
    }
}
