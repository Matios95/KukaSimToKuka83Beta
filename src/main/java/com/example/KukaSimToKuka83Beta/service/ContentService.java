package com.example.KukaSimToKuka83Beta.service;


import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ContentService {

    public static String transformSrc(String input) {
        final String foldMove = ";%{PE}%R";
        final String foldParametersMove = ";FOLD Parameters ;%{h}";
        final String endFold = ";ENDFOLD";
        final String setCdParams = "SET_CD_PARAMS (0)";
        boolean inParametersBlock = false;
        boolean inMoveBlock = false;
        List<String> lines = List.of(input.split("\\R")); // podział po liniach
        Matcher m;
        List<String> output = new ArrayList<>();
        Pattern p = Pattern.compile(
                ";FOLD\\s+(PTP|LIN|CIRC)\\s(\\S+)\\s?(\\S+|)\\s+(CONT|)?\\s*Vel=([0-9]+(?:\\.[0-9]+)?)\\s*(?:%|m/s)?\\s*(\\S+).",
                Pattern.CASE_INSENSITIVE);
        for (String line : lines) {
            m = p.matcher(line);
            if (m.find() && !line.contains(foldMove)) {
                line = line.replace(" ;%{PE}", ";%{PE}");
                output.add(generateLineKuka(line, m));
                inMoveBlock = true;
                continue;
            }
            if (line.contains(setCdParams)) continue;
            if (line.contains(foldParametersMove) && inMoveBlock) {
                inParametersBlock = true; // start usuwania
                continue; // nie dodajemy tej linii
            }
            if (inParametersBlock) {
                if (line.contains(endFold)) {
                    inParametersBlock = false; // koniec bloku Parameters
                }
                continue;
            }
            if (inMoveBlock && line.contains(endFold)) inMoveBlock = false;
            output.add(line);
        }
        return String.join(System.lineSeparator(), output);

    }

    private static String generateLineKuka(String line, Matcher m) {
        String rTag = "";
        String cParams = "";
        String type = m.group(1).toUpperCase();    //PTP, LIN, CIRC
        String point = m.group(2);                 //p1, p2, home
        String point2 = m.group(3);                //p1, p2, home w przypadku CIRC!
        String cdis = m.group(4);                  //CONT
        String vel = m.group(5);                   //40%, 1.0 m/s
        String dat = m.group(6);                   //PDAT1, CPDAT1
        rTag = switch (type) {
            case "PTP" -> {
                if (cdis.contains("CONT")) cParams = "C_PTP";
                yield String.format("%%R 8.3.22,%%MKUKATPBASIS,%%CMOVE,%%VPTP,%%P 1:PTP, 2:%s, 3:%s, 5:%s, 7:%s",
                        point, cParams, vel, dat);
            }
            case "LIN" -> {
                if (cdis.contains("CONT")) cParams = "C_DIS C_DIS";
                yield String.format("%%R 8.3.22,%%MKUKATPBASIS,%%CMOVE,%%VLIN,%%P 1:LIN, 2:%s, 3:%s, 5:%s, 7:%s",
                        point, cParams, vel, dat);
            }
            case "CIRC" -> {
                // dla CIRC potrzebujemy dwóch punktów w %P
                if (cdis.contains("CONT")) cParams = "C_DIS C_DIS";
                yield String.format("%%R 8.3.22,%%MKUKATPBASIS,%%CMOVE,%%VCIRC,%%P 1:CIRC, 2:%s, 3:%s, 4:%s, 6:%s, 8:%s",
                        point, point2, cParams, vel, dat);
            }
            default -> rTag;
        };
        System.out.println(rTag);
        return line + rTag;
    }


    public String transformDat(String input) {
        String[] lines = input.split("\\R");
        StringBuilder output = new StringBuilder();
        Pattern e6Pattern = Pattern.compile("(E1\\s+)([-\\d.Ee]+)(,\\s*E2\\s+)([-\\d.Ee]+)(,\\s*E3\\s+)([-\\d.Ee]+)");

        for (String line : lines) {
            Matcher m = e6Pattern.matcher(line);
            if (m.find()) {
                String e2 = m.group(4);
                String newLine = m.replaceAll("E1 180.0,E2 " + e2 + ",E3 " + e2);
                output.append(newLine).append(System.lineSeparator());
            } else {
                output.append(line).append(System.lineSeparator());
            }
        }

        return output.toString();
    }
}