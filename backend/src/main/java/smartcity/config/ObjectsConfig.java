package smartcity.config;

import java.util.Objects;

public abstract class ObjectsConfig {
    private int number;
    private int testNumber;

    protected ObjectsConfig(int number, int testNumber) {
        this.number = number;
        this.testNumber = testNumber;
    }

    int getNumber() {
        return number;
    }

    void setNumber(ConfigMutator.Mutation mutation, int newNumber) {
        Objects.requireNonNull(mutation);
        this.number = newNumber;
    }

    int getTestObjectNumber() {
        return testNumber;
    }

    void setTestObjectNumber(ConfigMutator.Mutation mutation, int newTestNumber) {
        Objects.requireNonNull(mutation);
        this.testNumber = newTestNumber;
    }
}
