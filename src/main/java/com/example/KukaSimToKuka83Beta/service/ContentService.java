package com.example.KukaSimToKuka83Beta.service;


import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ContentService {

    public String transformSrc(String input) {
        String[] lines = input.split("\\R"); // podział po liniach
        List<String> output = new ArrayList<>();

        // Regex do wykrycia nagłówków bloków PTP/LIN/CIRC
        Pattern foldPattern = Pattern.compile("^;FOLD\\s+(PTP|LIN|CIRC).*%\\{PE\\}.*");
        Pattern endFoldPattern = Pattern.compile("^;ENDFOLD");

        List<String> currentBlock = null;

        for (String line : lines) {
            String trimmed = line.trim();

            // Usuń SET_CD_PARAMS (0)
            if (trimmed.equals("SET_CD_PARAMS (0)")) continue;

            // Start nowego bloku
            if (foldPattern.matcher(trimmed).find()) {
                currentBlock = new ArrayList<>();
                currentBlock.add(line);
                System.out.println("Start block detected: " + trimmed);
                continue;
            }

            // Jeśli jesteśmy w bloku
            if (currentBlock != null) {
                currentBlock.add(line);

                // Koniec bloku
                if (endFoldPattern.matcher(trimmed).find()) {
                    System.out.println("End block detected");
                    // Dodajemy cały blok do output
                    output.addAll(currentBlock);
                    currentBlock = null;
                }
                continue;
            }

            // Linie poza blokiem
            output.add(line);
        }

        return String.join(System.lineSeparator(), output);
    }

    public static void main(String[] args) {
        String testInput = """
                ;FOLD PTP HOME Vel=100 % DEFAULT Tool[1]:TOOL_DATA[1] Base[0] ;%{PE}
                    ;FOLD Parameters ;%{h}
                       ;Params IlfProvider=kukaroboter.basistech.inlineforms.movement.old; Kuka.IsGlobalPoint=False; Kuka.PointName=HOME; Kuka.BlendingEnabled=False; Kuka.APXEnabled=False; Kuka.MoveDataPtpName=DEFAULT; Kuka.VelocityPtp=100; Kuka.CurrentCDSetIndex=0; Kuka.MovementParameterFieldEnabled=True; IlfCommand=PTP; SimId=
                    ;ENDFOLD
                    $BWDSTART = FALSE
                    PDAT_ACT = PDEFAULT
                    FDAT_ACT = FHOME
                    BAS(#PTP_PARAMS, 100.0)
                    SET_CD_PARAMS (0)
                    PTP XHOME
                 ;ENDFOLD
                """;

        ContentService service = new ContentService();
        String result = service.transformSrc(testInput);
        System.out.println("\n--- Transformed content ---\n" + result);
    }


    public String transformDat(String input) {
        String[] lines = input.split("\\R");
        StringBuilder output = new StringBuilder();
        Pattern e6Pattern = Pattern.compile("(E1\\s+)([-\\d\\.Ee]+)(,\\s*E2\\s+)([-\\d\\.Ee]+)(,\\s*E3\\s+)([-\\d\\.Ee]+)");

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