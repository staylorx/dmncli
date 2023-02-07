# dmncli

A Camunda DMN client

## Example

    java -jar build\install\dmncli-shadow\lib\dmncli-all.jar -d="decision" -i="[{\"season\": \"Spring\", \"guestCount\": 4}]" src\test\resources\dish-decision.dmn11.xml
Note the escaped JSON string. TODO is to accept an "-if --inputFile" so that will make things a bit easier to live with for testing.

Returns a JSON list (always a list) of objects: `[{"desiredDish":"Dry Aged Gourmet Steak"}]`

## Build

    gradle clean installShadowDist
