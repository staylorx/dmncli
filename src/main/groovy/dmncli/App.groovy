/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package dmncli

class App {

    static void main(String[] args) {

        def camundaRunner = new CamundaRunner("testFile.dmn")
        print(camundaRunner.inputFile.getName())

    }

}