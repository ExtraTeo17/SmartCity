package agents.utilities;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WrapperBehaviour;

import java.util.function.Consumer;

import static smartcity.config.StaticConfig.HANDLE_UNPREDICTED_ERRORS;

public class BehaviourWrapper {
    //TODO:dokumentacja

    public static Behaviour wrapErrors(Behaviour behaviour, Consumer<Exception> onError) {
        return new WrapperBehaviour(behaviour) {
            @Override
            public void action() {
                try {
                    super.action();
                } catch (Exception e) {
                    if (!HANDLE_UNPREDICTED_ERRORS) {
                        throw e;
                    }

                    onError.accept(e);
                }
            }
        };
    }
}
