package dmncli

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.*

class DeciderTest extends Specification {

    private Logger logger = LoggerFactory.getLogger(this.getClass())
    private JsonSlurper jsonSlurper = new JsonSlurper()
    private CamundaRunner camundaRunner = new CamundaRunner()

    /*
    Pulls out the absolutePath of the given fileName in the test resources folder
     */
    private String getTestResourcePath(String fileName) {
        //just in case you forgot a leading slash
        if (fileName[0] != '/') {
            fileName = "/" + fileName
        }
        def filePath = new File(getClass().getResource(fileName).toURI()).absolutePath
        assert (filePath instanceof String)
        logger.debug("${filePath} fetched from ${fileName}")
        return filePath
    }

    def "good decision key"() {

        setup:
        String dmnFilePath = getTestResourcePath('/dish-decision.dmn11.xml')
        def inputJson = '''  [  {"season": "Spring", "guestCount": 4}  ]  '''

        when:
        def resultJsonString = camundaRunner.decideTable(dmnFilePath, "decision", inputJson)

        then:
        def result = jsonSlurper.parseText(resultJsonString)
        result[0].desiredDish == "Dry Aged Gourmet Steak"
    }

    def "combination of good inputs"() {

        setup:
        String dmnFilePath = getTestResourcePath('/dish-decision.dmn11.xml')

        //building this as list of maps...
        def inputList = [["season": season, "guestCount": guestCount]]
        String variables = new JsonBuilder(inputList).toPrettyString()

        when:
        def resultJsonString = camundaRunner.decideTable(dmnFilePath, "decision", variables)

        then:
        def result = jsonSlurper.parseText(resultJsonString)
        result[0].desiredDish == desiredDish

        where:
        season   | guestCount || desiredDish
        "Spring" | 4          || "Dry Aged Gourmet Steak"
        "Spring" | 8          || "Steak"
        "Spring" | 16         || "Stew"

    }

    def "map-only inputs"() {

        setup:
        String dmnFilePath = getTestResourcePath('/dish-decision.dmn11.xml')

        //this is a map only, not a list of maps... wrong on purpose.
        def inputMap = ["season": "Fall", "guestCount": 3]
        String variables = new JsonBuilder(inputMap).toPrettyString()

        def resultJsonString = camundaRunner.decideTable(dmnFilePath, "decision", variables)
        def result = jsonSlurper.parseText(resultJsonString)

        expect:
        result[0].code == "__MUST_BE_LIST__"

    }

    def "combination of really bad inputs"() {

        setup:
        String dmnFilePath = getTestResourcePath('/dish-decision.dmn11.xml')

        expect:
        def inputMap = [["season": season, "guestCount": guestCount]]
        String variables = new JsonBuilder(inputMap).toPrettyString()

        def resultJsonString = camundaRunner.decideTable(dmnFilePath, "decision", variables)
        def result = jsonSlurper.parseText(resultJsonString)
        if (result.size == 0) {
            //an empty results comes back as just that... and empty ArrayList
            result[0] = ["code":null]
        }
        result[0].code == code

        where:
        season    | guestCount || code
        "Arizona" | 4          || null
        "Spring"  | true       || "__ENGINE_ERROR__"
        99.8      | "four"     || "__ENGINE_ERROR__"

    }

    def "broken Dmn file"() {
        setup:
        String dmnFilePath = getTestResourcePath('/purposefully-bad-xml.dmn')

        when:
        def inputJson = '''[                {"season": "Spring", "guestCount": 4} ]  '''
        def resultJsonString = camundaRunner.decideTable(dmnFilePath, "decision", inputJson)

        then:
        def result = jsonSlurper.parseText(resultJsonString)
        result[0].code == "__NO_DMN_MODEL__"
    }

    def "bad inputs to the model"() {
        setup:
        String dmnFilePath = getTestResourcePath('/dish-decision.dmn11.xml')
        def inputJson = '''[  {"season": "Arizona", "guestCount": true} ]'''

        expect:
        def resultJsonString = camundaRunner.decideTable(dmnFilePath, "decision", inputJson)
        //the result is a JSON array a string
        def result = jsonSlurper.parseText(resultJsonString)
        result[0].code == "__ENGINE_ERROR__"

    }

    def "bad decision key"() {
        setup:
        String dmnFilePath = getTestResourcePath('/dish-decision.dmn11.xml')

        def inputJson = '''[      {"season": "Spring", "guestCount": 4}   ] '''
        def resultJsonString = camundaRunner.decideTable(dmnFilePath, "bad_key", inputJson)

        expect:
        //the result is a JSON array a string
        def result = jsonSlurper.parseText(resultJsonString)
        result[0].code == "__TRANSFORM_ERROR__"
    }

    def "multiple answers on output"() {
        setup:
        String dmnFilePath = getTestResourcePath('/dinnerDecisions.dmn')

        def inputJson = '''[{"season": "Spring", "guestCount": 10, "guestsWithChildren": false}]'''
        def resultJsonString = camundaRunner.decideTable(dmnFilePath, "beverages", inputJson)

        expect:
        def result = jsonSlurper.parseText(resultJsonString)
        result["beverages"].contains("Guiness")
        result["beverages"].contains("Water")
    }

    def "list decisions"() {
        setup:
        String dmnFilePath = getTestResourcePath('/dinnerDecisions.dmn')

        camundaRunner.loadDmnFile(dmnFilePath)
        def resultJsonString = camundaRunner.listDecisions()

        expect:
        def result = jsonSlurper.parseText(resultJsonString)
        result["key"].contains("dish")
        result["key"].contains("beverages")
    }

}