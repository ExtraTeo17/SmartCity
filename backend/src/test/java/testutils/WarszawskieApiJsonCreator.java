package testutils;

public class WarszawskieApiJsonCreator extends JsonCreator {

    public static String createResultFromManyOrderedKeyValues(String... orderedKeyValues) {
        return createResultFromManyOrderedKeyValues(6, orderedKeyValues);
    }

    public static String createResultFromManyOrderedKeyValues(int fieldsCount, String... orderedKeyValues) {
        int pairLength = fieldsCount * 2;
        String[] valuesObjects = new String[orderedKeyValues.length / pairLength];
        for (int arraysIter = 0; arraysIter < valuesObjects.length; ++arraysIter) {
            var currArray = new String[fieldsCount];
            int propStart = arraysIter * pairLength ;
            int propEnd = propStart + pairLength;
            int currArrayIter = 0;
            for (int propIter = propStart; propIter < propEnd; propIter += 2) {
                currArray[currArrayIter++] =
                        createKeyValueObj(orderedKeyValues[propIter], orderedKeyValues[propIter + 1]);
            }

            valuesObjects[arraysIter] = createValuesObj(currArray);
        }

        return createResultObj(valuesObjects);
    }

    private static String createResultObj(String... valuesObjects) {
        return createObject(createResultArr(valuesObjects));
    }

    private static String createValuesObj(String... keyValues) {
        return createObject(createValuesArr(keyValues));
    }

    private static String createResultArr(String... valuesObjects) {
        return createArray("result", valuesObjects);
    }

    private static String createValuesArr(String... keyValues) {
        return createArray("values", keyValues);
    }

    private static String createKeyValueObj(String key, String value) {
        return createObject(
                createProperty("value", value),
                createProperty("key", key)
        );
    }
}
