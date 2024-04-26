package example;

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
 * This example demonstrates how to return nodes based on a specific label.
 */
public class GetNodesByLabel {

    @Context
    public Log log;

    @Context
    public Transaction tx;

    /**
     * This procedure returns all nodes with the specified label.
     *
     * @param labelName The label name to search for.
     * @return A stream of Nodes found with the specified label.
     */
    @Procedure(name = "example.getNodesByLabel")
    @Description("Returns all nodes with the specified label.")
    public Stream<NodeResult> getNodesByLabel(@Name("label") String labelName) {
        Label label = Label.label(labelName);
        return tx.findNodes(label).stream().map(NodeResult::new);
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
