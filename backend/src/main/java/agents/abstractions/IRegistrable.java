package agents.abstractions;

public interface IRegistrable {
    default void register(Class<?>... types) {
        registerAll(types);
    }

    void registerAll(Class<?>[] types);

    class NotRegisteredException extends RuntimeException {
        public NotRegisteredException(Class<?> type) {
            super("Type '" + type.getName() + "' was not registered");
        }
    }
}
