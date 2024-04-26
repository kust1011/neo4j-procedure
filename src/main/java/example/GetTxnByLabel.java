package example;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

/**
 * This example demonstrates how to return Transaction nodes based on specified labels.
 */
public class GetTxnByLabel {

    @Context
    public Log log;

    @Context
    public Transaction tx;

    /**
     * This procedure returns all Transaction nodes with the specified labels.
     *
     * @param labelNames The label names to search for.
     * @return A stream of Nodes found with the specified labels.
     */
    @Procedure(name = "example.getTxnByLabel")
    @Description("Returns all Transaction nodes with the specified labels.")
    public Stream<NodeResult> getTxnByLabel(@Name("labels") List<String> labelNames) {
        Label transactionLabel = Label.label("Transaction");
        return tx.findNodes(transactionLabel).stream()
                .filter(node -> labelNames.stream().allMatch(labelName -> node.hasLabel(Label.label(labelName))))
                .map(NodeResult::new);
    }

    /**
     * This class defines the output record for our node search procedure.
     * Each node returned by the procedure will be wrapped in a NodeResult object.
     */
    public static class NodeResult {
        public Node node;

        public NodeResult(Node node) {
            this.node = node;
        }
    }
}
