# dmncli

A Camunda DMN client

## Example

    java -jar build\install\dmncli-shadow\lib\dmncli-all.jar -d="decision" -i="[{\"season\": \"Spring\", \"guestCount\": 4}]" src\test\resources\dish-decision.dmn11.xml
Note the escaped JSON string. TODO is to accept an "-if --inputFile" so that will make things a bit easier to live with for testing.

Returns a JSON list (always a list) of objects: `[{"desiredDish":"Dry Aged Gourmet Steak"}]`

## Build

    gradle clean installShadowDist

## Reflection Error

This is still a thing with Groovy and, in the case of my builds, JDK 11. It can be safely ignored, but it is a hassle and messes with the stdout return of the decision.

```
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by org.codehaus.groovy.reflection.ReflectionUtils (file:/C:/Work/dmncli/build/install/dmncli-shadow/lib/dmncli-all.jar) to field java.util.ArrayList.size
WARNING: Please consider reporting this to the maintainers of org.codehaus.groovy.reflection.ReflectionUtils
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
```