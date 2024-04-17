package com.ethan.legacy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class Compile {

    final String VAR_REGEX = "setv: 0x[0-9a-fA-F]+ ([0-9]+|#add|#mult|#div|#sub)$";
    final String SET_ALIAS_REGEX = "seta: 0[xX][0-9a-fA-F]+ ([a-zA-Z])+";
    final String IF_REGEX = "if: ((0x)?[0-9]+) (-gt|-lt|-eq|-neq|-ltq|-gtq) ((0x)?[0-9]+)";
    final String[] fns = { "out:", "seta:", "setv:" };


    public Compile() throws IOException, FunctionConstantNotFoundException {
        BufferedReader r = new BufferedReader(new FileReader("main.llm"));
        String l;
        HashMap<String, Long> alias = new HashMap<>();
        HashMap<Long, Integer> vars = new HashMap<>();

        int add = 0;
        int sub = 0;
        long mult = 0;
        int div = 0;


        while((l = r.readLine()) != null) {
            if(l.isEmpty()) continue;
            String function = l.substring(0, l.indexOf(' '));
            String[] args = l.substring(l.indexOf(' ')).trim().split(" ");

            switch (function) {
                case "if:": {
                    assert l.matches(IF_REGEX);
                    String operator = args[1];
                    Long lhs;
                    if(args[0].contains("0x")) {
                        lhs = Long.parseLong(args[0].replace("0x", ""), 16);
                        lhs = (long)vars.get(lhs);
                    }
                    else lhs = (long)Integer.parseInt(args[0]);

                    int rhs = Integer.parseInt(args[2]);

                    switch(args[1]) {
                        case "-gt": {
                            if(!(lhs > rhs)) {
                                while((l = r.readLine()) != null) {
                                    if(l.equals(":end")) break;
                                }
                                continue;
                            }
                        } break;
                        case "-lt": {
                            if(!(lhs < rhs)) {
                                while((l = r.readLine()) != null) {
                                    if(l.equals(":end")) break;
                                }
                                continue;
                            }
                        } break;
                        case "-eq": {
                            if(!(lhs == rhs)) {
                                while((l = r.readLine()) != null) {
                                    if(l.equals(":end")) break;
                                }
                                continue;
                            }
                        } break;
                        case "-neq": {
                            if(!(lhs != rhs)) {
                                while((l = r.readLine()) != null) {
                                    if(l.equals(":end")) break;
                                }
                                continue;
                            }
                        } break;
                        case "-ltq": {
                            if(!(lhs <= rhs)) {
                                while((l = r.readLine()) != null) {
                                    if(l.equals(":end")) break;
                                }
                                continue;
                            }
                        } break;
                        case "-gtq": {
                            if(!(lhs >= rhs)) {
                                while((l = r.readLine()) != null) {
                                    if(l.equals(":end")) break;
                                }
                                continue;
                            }
                        } break;
                    }

                    while((l = r.readLine()) != null) {
                        if(l.equals(":end")) break;

                        System.out.println("In if: " + l);
                    }

                } break;

                case "add:": {
                    int lhs = Integer.parseInt(args[0]);
                    int rhs = Integer.parseInt(args[1]);
                    add = (lhs + rhs);
                } break;

                case "mult:": {
                    int lhs = Integer.parseInt(args[0]);
                    int rhs = Integer.parseInt(args[1]);
                    mult = (lhs * rhs);
                } break;

                case "div:": {
                    int lhs = Integer.parseInt(args[0]);
                    int rhs = Integer.parseInt(args[1]);
                    div = (lhs / rhs);
                } break;

                case "sub:": {
                    int lhs = Integer.parseInt(args[0]);
                    int rhs = Integer.parseInt(args[1]);
                    sub = (lhs - rhs);
                } break;

                case "setv:": {
                    assert l.matches(VAR_REGEX);
                    if(!args[1].matches("[0-9]+")) {
                        long addr = Long.parseLong(args[0].replace("0x", ""), 16);
                        long value = switch(args[1]) {
                            case "#add" -> add;
                            case "#sub" -> sub;
                            case "#mult" -> mult;
                            case "#div" -> div;
                            default -> {
                                System.err.println("ERROR: Unknown function output constant " + args[1]);
                                throw new FunctionConstantNotFoundException();
                            }
                        };

                        vars.put(addr, (int)value);
                        continue;
                    }


//                System.out.println("Indexing address " + l.substring(l.indexOf(':') + 2, l.lastIndexOf(' ')) + " with value " + l.substring(l.lastIndexOf(' ') + 1));
                    long currAddr = Long.parseLong(l.substring(l.indexOf(' ') + 1, l.lastIndexOf(' ')).replace("0x", ""), 16);
                    int addrVal = Integer.parseInt(l.substring(l.lastIndexOf(' ')).trim());

                    if (vars.containsKey(currAddr)) {
                        vars.replace(currAddr, addrVal);
                    }

                    vars.put(currAddr, addrVal);
                } break;
                case "seta:": {
                    if (!l.matches(SET_ALIAS_REGEX)) {
                        System.out.println("ERROR(s): Unknown error at: '" + l + "'");
                        System.exit(1);
                    } else if (Arrays.asList(fns).contains(args[1])) {
                        System.err.println("ERROR(s): Alias name is preset function");
                        System.exit(1);
                    } else if (alias.containsKey(args[1])) {
                        // set to diff address
                        System.out.println("redirect variable");
                    }

                    alias.put(args[1], Long.parseLong(args[0].replace("0x", ""), 16));
                } break;
                case "out:": {
                    String out;

                    if(!l.matches("(.*)\\{[a-zA-z]}(.*)")) {
                        out = String.join(" ", args);
                        String printOut = out.replaceFirst("\\{(.+)}", "__VAR__").replace("\"", "");
                        out = out.substring(out.indexOf('{') + 1, out.indexOf('}'));
                        if(out.matches("(.*)0x[a-fA-F0-9](.*)")) out = String.valueOf(vars.get(Long.parseLong(out.replace("0x", "").trim())));
                        else out = String.valueOf(vars.get(alias.get(out)));

                        System.out.println(printOut.replace("__VAR__", out));
                    } else {
                        System.out.println(args[0].replace("\"", ""));
                    }
                }break;
            }
        }
    }

    public static void main(String[] args) throws IOException, FunctionConstantNotFoundException {
        new Compile();
    }
}
