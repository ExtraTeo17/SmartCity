package smartcity.config;

class PedestriansConfig extends ObjectsConfig {
    private PedestriansConfig(int number, int testNum) {
        super(number, testNum);
    }

    static PedestriansConfig of(int number, int testId) {
        return new PedestriansConfig(number, testId);
    }
}
