package agents.utilities;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WrapperBehaviour;

import java.util.function.Consumer;

import static smartcity.config.StaticConfig.HANDLE_UNPREDICTED_ERRORS;

public class BehaviourWrapper {

	/**
	 * Construct a behaviour, which will perform a given function
	 * if any exception happens and is supposed not to throw
	 * the exception
	 *
	 * @param behaviour The behaviour to wrap over
	 * @param onError The function to call upon exception in underlying behaviour
	 * @return The constructed, wrapped behaviour
	 */
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
