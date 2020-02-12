package dmncli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "dmncli", mixinStandardHelpOptions = true, version = "dmncli 1.0",
        description = "Decides inputs against a DMN decision.")
class CamundaRunnerMain implements Callable<Integer> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Parameters(index = "0", description = "The DMN file.")
    private File file;

    @Option(names = {"-d", "--decision"}, description = "The Decision label")
    private String decision = "";
    @Option(names = {"-i", "--inputVars"}, description = "Input variables in JSON")
    private String inputVars = "";

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
        CamundaRunner camundaRunner = new CamundaRunner(file.getAbsolutePath());
        String resultJsonString = camundaRunner.decideTable(decision, inputVars);
        //TODO Could want an output to a file
        System.out.printf(resultJsonString);
        return 0;
    }
}