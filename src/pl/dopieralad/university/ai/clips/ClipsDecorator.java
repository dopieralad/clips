package pl.dopieralad.university.ai.clips;

import CLIPSJNI.Environment;
import CLIPSJNI.FactAddressValue;
import CLIPSJNI.PrimitiveValue;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public class ClipsDecorator extends Environment implements Closeable {

    private static final String GET_CURRENT_ID_QUERY = "(find-all-facts ((?f state-list)) TRUE)";
    private static final String GET_CURRENT_STATE_QUERY = "(find-all-facts ((?f UI-state)) (eq ?f:id %s))";

    public CompletableFuture<Void> runAsync() {
        return CompletableFuture.runAsync(this::run);
    }

    public PrimitiveValue getCurrentState() throws Exception {
        final String getCurrentStateQuery = String.format(GET_CURRENT_STATE_QUERY, getCurrentId());

        final PrimitiveValue currentState = eval(getCurrentStateQuery).get(0);

        System.out.println("Current state:");
        System.out.printf("\tID: '%s'.\n", currentState.getFactSlot("id"));
        System.out.printf("\tDisplay: '%s'.\n", currentState.getFactSlot("display"));
        System.out.printf("\tRelation asserted: '%s'.\n", currentState.getFactSlot("relation-asserted"));
        System.out.printf("\tResponse: '%s'.\n", currentState.getFactSlot("response"));
        System.out.printf("\tValid answers: '%s'.\n", currentState.getFactSlot("valid-answers"));
        System.out.printf("\tState: '%s'.\n", currentState.getFactSlot("state"));

        return currentState;
    }

    public String getCurrentId() throws Exception {
        final PrimitiveValue value = eval(GET_CURRENT_ID_QUERY).get(0);
        final String currentId = value.getFactSlot("current").toString();

        System.out.printf("Current ID: '%s'.\n", currentId);

        return currentId;
    }

    @Override
    public FactAddressValue assertString(String string) {
        System.out.printf("Asserting: '%s'.\n", string);

        return super.assertString(string);
    }

    @Override
    public void close() {
        destroy();
    }
}
