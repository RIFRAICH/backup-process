package fr.rifraich.backup.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandUtils {

    public static void runCommand(String command) {
        String s = "";
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            p.waitFor();

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            while ((s = stdError.readLine()) != null) {
                System.err.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
