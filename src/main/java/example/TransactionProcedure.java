package example;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.stream.Stream;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import utility.SafeConvert;

/**
 * This example demonstrates how to return nodes based on a specific label.
 */
public class TransactionProcedure {

    static final Label ACCOUNT = Label.label("account");
    static final Label TRANSACTION = Label.label("transaction");

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
    @Procedure(name = "ethereum.retrieve.transaction", mode = Mode.READ)
    @Description("Retrieve transactions of a given ethereum account.")
    public Stream<NodeResult> transactionProcedure(
        @Name("address") String address,
        @Name("startTime") long startTime,
        @Name("endTime") long endTime
        ) {
            try {

                Node account = tx.findNode(ACCOUNT, "address", address.toLowerCase());

                if (account == null) {
                    log.info("Account not found: " + address);
                    return Stream.empty();
                }

                // find the outgoing node, and check if it is a transaction
                ResourceIterable<Relationship> outgoing = account.getRelationships(OUTGOING);
                Stream<NodeResult> transactions = outgoing.stream()
                    .filter(relationship -> relationship.getEndNode().hasLabel(TRANSACTION))
                    .map(relationship -> new NodeResult(relationship.getEndNode()));

                // (blk:block)-[:includes]->(:transaction)
                // filter the transaction block
                // by checking the include block node where startTime < timestamp(blk) < endTime
                Stream<NodeResult> filteredTransactions = transactions
                    .filter(nodeResult -> {
                        Node transaction = nodeResult.node;
                        ResourceIterable<Relationship> incoming = transaction.getRelationships(INCOMING);
                        return incoming.stream()
                            .filter(relationship -> relationship.isType(RelationshipType.withName("includes")))
                            .filter(relationship -> relationship.getStartNode().hasLabel(Label.label("block")))
                            .anyMatch(relationship -> {
                                Node block = relationship.getStartNode();
                                long timestamp = (long) block.getProperty("timestamp", 0L);
                                // long timestamp = SafeConvert.toLong(block.getProperty("timestamp", null), 0L);
                                return startTime <= timestamp && timestamp <= endTime;
                            });
                    });
                
                return filteredTransactions;
                
            } 
            catch (Exception e) {
                log.error("Error in transactionProcedure: " + e.getMessage());
                return null;
            }
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
