package agents.abstractions;

public interface IRegistrable {
    void register(Class<?>... types);

    void registerAll(Class<?>[] types);

    class NotRegisteredException extends RuntimeException {
        NotRegisteredException(Class<?> type) {
            super("Type '" + type.getName() + "' was not registered");
        }
    }
}
