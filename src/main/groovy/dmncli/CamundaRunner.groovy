package dmncli

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.camunda.bpm.dmn.engine.DmnDecisionRequirementsGraph
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration
import org.camunda.bpm.dmn.engine.DmnEngineException
import org.camunda.bpm.dmn.engine.impl.DmnEvaluationException
import org.camunda.bpm.dmn.engine.impl.transform.DmnTransformException
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.model.dmn.Dmn
import org.camunda.bpm.model.dmn.DmnModelException
import org.camunda.bpm.model.dmn.DmnModelInstance
import org.camunda.bpm.model.xml.ModelValidationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/*
TODO implement graph to check for valid decisionKeys => __BAD_DECISION_KEY__
TODO process entire array of inputs instead of just the first one
 */
class CamundaRunner {

    private Logger logger = LoggerFactory.getLogger(this.getClass())
    private jsonSlurper = new JsonSlurper()
    private dmnEngine = DmnEngineConfiguration
            .createDefaultDmnEngineConfiguration()
            .buildEngine()
    private DmnModelInstance dmnModelInstance
    private DmnDecisionRequirementsGraph decisionRequirementsGraph

    /*
        @param[dmnFilePath] The complete path to the DMN/XML file containing the decision tables and maps.
    */
    CamundaRunner(String dmnFilePath) {
        logger.debug("Class created with optional constructor {dmnFilePath:${dmnFilePath}}.")
        loadDmnFile(dmnFilePath)
    }

    /*
    Default empty constructor
     */
    CamundaRunner() {
        logger.debug("Class created with default constructor.")
    }

    def loadDmnFile(String dmnFilePath) {
        logger.debug("Setting DMN file to ${dmnFilePath}")
        new File(dmnFilePath).withInputStream { stream ->
            try {
                this.dmnModelInstance = Dmn.readModelFromStream(stream)
                try {
                    Dmn.validateModel(dmnModelInstance)
                    this.decisionRequirementsGraph = dmnEngine.parseDecisionRequirementsGraph(dmnModelInstance)
                }
                catch (ModelValidationException e) {
                    logger.error("Model won't validated: ${e.message}")
                    return createErrorJson("__WONT_VALIDATE_", "${e.message}")
                }
            }
            catch (DmnModelException ex) {
                logger.error("Model can't be opened from file: ${ex.message}")
                return createErrorJson("__BAD_MODEL_READ_", "${ex.message}")
            }

        }
    }

    private static String createErrorJson(code, message) {

        def map = [:]
        map['code'] = code
        map['message'] = message

        def list = []
        list.add(map)

        return JsonOutput.toJson(list)
    }

    String decideTable(String dmnFilePath, String decisionKey, String variablesJson) {
        loadDmnFile(dmnFilePath)
        return decideTable(decisionKey,variablesJson)
    }

    /*
        @param[decisionKey] The id of the decision table to evaluate
        @param[variables] Variables (in JSON) to be evaluated against the table
        @return An ArrayList of Maps, in the form of JSON of the evaluation(s) of the decision table
     */
    String decideTable(String decisionKey, String variablesJson) {

        if (dmnModelInstance == null) {
            logger.error("Need a DMN model.")
            return createErrorJson("__NO_DMN_MODEL__","No model loaded.")
        }

        def inputList = jsonSlurper.parseText(variablesJson)

        //TODO build an exception class and move these out
        if (inputList instanceof List) {
            //fine... move along quietly
        } else {
            logger.error("JSON must be a list of maps.")
            return createErrorJson("__MUST_BE_LIST__","The input structure must be a List<Map> is JSON")
        }

        if (inputList.size == 0) {
            logger.error("Empty input variables.")
            return createErrorJson("__EMPTY_INPUT__","The input JSON has an zero length.")
        }

        //we're only trying a single table... so we look to the first row only
        def variableMap = Variables.fromMap(inputList[0])

        try {
            def decision = dmnEngine.parseDecision(decisionKey, dmnModelInstance)
            def decisionTableResult = dmnEngine.evaluateDecisionTable(decision, variableMap)
            return JsonOutput.toJson(decisionTableResult.resultList)
        }
        catch (DmnTransformException e) {
            logger.error("Unable to parse or transform the decision: ${e.message}")
            return createErrorJson("__TRANSFORM_ERROR__","${e.message}")
        }
        catch (DmnEvaluationException e) {
            logger.error("Cannot evaluate: ${e.message}")
            return createErrorJson("__EVALUATION_ERROR__","${e.message}")
        }
        catch (DmnEngineException e) {
            logger.error("Engine failure: ${e.message}")
            return createErrorJson("__ENGINE_ERROR__","${e.message}")
        }

    }

    /*
    Very simple dump of decisions
     */
    String listDecisions() {

        if (decisionRequirementsGraph == null) {
            logger.error("Need a requirements graph.")
            return createErrorJson("__NO_REQ_GRAPH__","No model loaded.")
        }

        List output = []
        decisionRequirementsGraph.decisions.each { decision ->
            Map decisionMap = [:]
            decisionMap["key"] = decision.key
            decisionMap["name"] = decision.name
            decisionMap["isTable"] = decision.decisionTable
            logger.debug(decisionMap.toString())
            output.add(decisionMap)
        }

        return JsonOutput.toJson(output)
    }

}


