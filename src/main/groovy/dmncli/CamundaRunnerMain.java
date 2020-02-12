package dmncli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;

@Command(name = "dmncli", mixinStandardHelpOptions = true, version = "dmncli 1.0",
        description = "Decides inputs against a DMN decision.")
class CamundaRunnerMain implements Callable<Integer> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Parameters(index = "0", description = "The DMN file.")
    private File file;

    @Option(names = {"-d", "--decision"}, description = "The Decision label")
    private String decision = "";
    @Option(names = {"-i", "--inputVars"}, description = "Input variables JSON string")
    private String inputVars = "";
    @Option(names = {"-if", "--inputVarsFile"}, description = "Input variable file")
    private File inputVarFile;
    @Option(names = {"-of", "--outputVarsFile"}, description = "Output variable file")
    private File outputVarFile;

    // this example implements Callable, so parsing, error handling and handling user
    // requests for usage help or version help can be done with one line of code.
    public static void main(String... args) {
        int exitCode = new CommandLine(new CamundaRunnerMain()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        logger.debug("DMN File: " + file.getAbsolutePath());
        logger.debug("Decision: " + decision);
        logger.debug("InputVar: " + inputVars);
        logger.debug("InputVarFile: " + inputVarFile.getAbsolutePath());

        CamundaRunner camundaRunner = new CamundaRunner(file.getAbsolutePath());

        if (inputVarFile != null) {
            try {
                inputVars = FileUtils.readFileToString(inputVarFile, StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage());
                return 1;
            }
        }

        String resultJsonString = camundaRunner.decideTable(decision, inputVars);

        if (outputVarFile != null) {
            try {
                FileUtils.writeStringToFile(outputVarFile, resultJsonString);
                logger.info("Output written to " + outputVarFile.getAbsolutePath());
            } catch (Exception e) {
                logger.error(e.getMessage());
                return 1;
            }
        } else {
            System.out.printf(resultJsonString);
        }
        return 0;
    }
}