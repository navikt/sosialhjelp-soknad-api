package no.nav.sbl.dialogarena.soknadinnsending.business.service;


import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.OS;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Callable;

public class ScriptRunner implements Callable<byte[]> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptRunner.class);

    public static final int TIMEOUT_PAA_GENERERING = 20000;
    public static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"));

    private static boolean enableGhostscript = true;
    public static final boolean GS_EXISTS = checkIfExists(decideGSCommand());
    public static final boolean IM_EXISTS = checkIfExists(decideIMCommand());
    private final Type command;

    public enum Type {
        GS {
            @Override
            String command() {
                return decideGSCommand();
            }
        }, IM {
            @Override
            String command() {
                return decideIMCommand();
            }
        };

        abstract String command();
    }


    private final String args;
    private final Map<String, String> substitutionMap;
    private final InputStream inputStream;

    public ScriptRunner(Type command, String args, Map<String, String> substitutionMap, InputStream inputStream) {
        this.command = command;
        this.args = args;
        this.substitutionMap = substitutionMap;
        this.inputStream = inputStream;
    }

    @Override
    public byte[] call() throws Exception {

        ExecuteWatchdog watchdog = new ExecuteWatchdog(TIMEOUT_PAA_GENERERING);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CommandLine cmdLine = new CommandLine(command.command());
            cmdLine.addArguments(args);
            cmdLine.setSubstitutionMap(substitutionMap);

            DefaultExecutor executor = new DefaultExecutor();
            executor.setWorkingDirectory(TMP_DIR);

            executor.setStreamHandler(new PumpStreamHandler(out, System.err, inputStream));
            executor.setWatchdog(watchdog);
            System.out.println(cmdLine.toString());

            executor.execute(cmdLine);

            return out.toByteArray();
        } catch (ExecuteException e) {
            if (watchdog.killedProcess()) {
                LOGGER.info("[PDF] Timet ut mens en lagde preview.");
                throw new RuntimeException("Prosessen brukte for lang tid", e);
            } else {
                LOGGER.info("[PDF] Noe gikk galt i GhostScript under generering av preview. ", e);
                throw new RuntimeException("Noe annet gikk galt: " + e, e);
            }
        } catch (IOException e) {
            LOGGER.info("[PDF] Noe gikk galt i GhostScript under generering av preview. ", e);
            throw new RuntimeException("Feil ved preview-generering", e);
        }
    }


    private static String decideGSCommand() {
        return OS.isFamilyWindows() ? "gswin32c" : "gs";
    }

    private static String decideIMCommand() {
        return OS.isFamilyWindows() ? "c:\\apps\\im\\convert.exe" : "convert";
    }

    private static boolean checkIfExists(String executable) {
        int exitCode = -1;
        try {
            CommandLine cmdLine = new CommandLine(executable);
            cmdLine.addArgument("--version");
            DefaultExecutor executor = new DefaultExecutor();
            executor.setWorkingDirectory(TMP_DIR);
            exitCode = executor.execute(cmdLine);
        } catch (IOException e) {
            LOGGER.warn("[PDF] Could not find " + executable + " (" + e.getMessage() + ")");
            LOGGER.debug("[PDF] " + e, e);
        }
        boolean exists = (exitCode == 0);
        if (exists) {
            LOGGER.info("[PDF] Bruker GhostScript som rendring-motor for thumbnails");
        } else {
            LOGGER.warn("[PDF] Fant ikke GhostScript. Bruker PdfBox som rendring-motor for thumbnails (exitcode: {})", exitCode);
        }
        return exists;
    }
}
