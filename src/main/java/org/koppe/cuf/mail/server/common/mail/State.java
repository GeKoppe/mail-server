package org.koppe.cuf.mail.server.common.mail;

import java.util.List;

/**
 * Interface to be used exclusively on enums.
 */
public interface State {
    /**
     * Returns the value of the state instance.
     * 
     * @return Value of the state instance.
     */
    public int getValue();

    /**
     * List of allowed commands for the current state
     * 
     * @return List of all allowed commands for the current state.
     */
    public List<? extends Command<? extends State>> getAllowedCommands();

    /**
     * Returns the default error state
     * 
     * @return Default error state
     */
    public State getErrorState();

    /**
     * Returns true, if the given command is valid in the given state
     * 
     * @param state   State to verify the command for
     * @param command Command to check
     * @return True, if command is allowed in current state
     */
    public static boolean validCommand(State state, Command<? extends State> command) {
        return state.getAllowedCommands().contains(command);
    }
}
