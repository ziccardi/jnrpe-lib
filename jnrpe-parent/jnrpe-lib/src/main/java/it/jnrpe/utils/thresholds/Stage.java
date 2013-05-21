package it.jnrpe.utils.thresholds;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class for all the parsing stages. A parsing stage is composed of a name
 * and a set of named transitions. This way we can configure the parser as a
 * state machine.
 *
 * @author Massimiliano Ziccardi
 */
abstract class Stage {
    /**
     * The stage name.
     */
    private final String name;

    /**
     * The sets of stage transitions.
     */
    private Map<String, Stage> nextStagesMap = new HashMap<String, Stage>();

    /**
     * @param stageName
     *            The stage name
     */
    protected Stage(final String stageName) {
        name = stageName;
    }

    /**
     * Adds a possible transition to this stage.
     *
     * @param stage
     *            The transition
     */
    public void addTransition(final Stage stage) {
        nextStagesMap.put(stage.name, stage);
    }

    /**
     * Returns the name of this stage.
     *
     * @return The name of this stage
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a named transition of this stage.
     *
     * @param stageName
     *            The requested transition
     * @return The transition if found. Null otherwise.
     */
    public Stage getTransition(final String stageName) {
        return nextStagesMap.get(stageName);
    }

    /**
     * Returns the list of the possible transitions from this stage.
     *
     * @return the list of the possible transitions from this stage
     */
    public Set<String> getTransitionNames() {
        return nextStagesMap.keySet();
    }

    /**
     * A stage is a leaf if no transition are possible from here..
     *
     * @return <code>true</code> if no transitions are available from here.
     */
    public boolean isLeaf() {
        return this.nextStagesMap.isEmpty();
    }

    /**
     * Consumes a part of the threshold and configure the
     * {@link ThresholdConfig} object according to the swallowed part of the
     * threshold.
     *
     * @param threshold
     *            The threshold to consume.
     * @param tc
     *            The threshold configuration object
     * @return The remaining unparsed part of the threshold
     * @throws BadThresholdSyntaxException
     *             -
     */
    public abstract String parse(final String threshold, ThresholdConfig tc)
            throws BadThresholdSyntaxException;

    public abstract boolean canParse(String threshold);

    public abstract String expects();
}
