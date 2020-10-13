package smartcity.config;

class CarsConfig extends ObjectsConfig {
    private CarsConfig(int number, int testNum) {
        super(number, testNum);
    }

    static CarsConfig of(int number, int testId) {
        return new CarsConfig(number, testId);
    }
}
