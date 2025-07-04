package org.Tweakio.WhatsappWeb;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class Reloader {
    public static void hardRestart()  {
        try {


            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            String mainClass = getMainClassName();
            List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
            List<String> command = new ArrayList<>();
            command.add(javaBin);
            command.addAll(vmArguments);
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
            command.add(mainClass);
            System.out.println("Restarting with command: " + String.join(" ", command));
            new ProcessBuilder(command)
                    .inheritIO()
                    .start();
            System.exit(0);
        }catch (Exception e){
            Extras.logwriter("Error ---> Restarting with command // reloader // hard restart: " + e.getMessage());
        }
    }

    private static String getMainClassName() {
        String command = System.getProperty("sun.java.command");
        if (command != null) {
            return command.split(" ")[0];
        }
        throw new IllegalStateException("Cannot determine main class name.");
    }
}
