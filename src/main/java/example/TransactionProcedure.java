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
import utility.Timestamp;

import java.util.ArrayList;
import java.util.List;

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
     * @param address   The account address.
     * @param startTime The start time for filtering transactions.
     * @param endTime   The end time for filtering transactions.
     * @param minValue  The minimum value for filtering transactions.
     * @param maxValue  The maximum value for filtering transactions.
     * @return A stream of Nodes found with the specified label.
     */
    @Procedure(name = "ethereum.retrieve.transaction", mode = Mode.READ)
    @Description("Retrieve transactions of a given ethereum account.")
    public Stream<NodeResult> transactionProcedure(
        @Name("address") String address,
        @Name("startTime") long startTime,
        @Name("endTime") long endTime,
        @Name("minValue") Double minValue,
        @Name("maxValue") Double maxValue
    ) {
        try {
            System.out.println("Starting transactionProcedure for address: " + address);
            System.out.println("Filtering transactions between " + startTime + " and " + endTime);
            System.out.println("Filtering transactions with value between " + minValue + " and " + maxValue);

            Node account = tx.findNode(ACCOUNT, "address", address);

            if (account == null) {
                System.out.println("Account not found: " + address);
                return Stream.empty();
            }

            System.out.println("Account found: " + account.toString());

            long startDate = Timestamp.calculateDaysBetween(1529891469000L, startTime);
            long endDate = Timestamp.calculateDaysBetween(1529891469000L, endTime);

            System.out.println("Calculated startDate: " + startDate + ", endDate: " + endDate);

            List<RelationshipType> relationshipTypes = new ArrayList<>();
            // for (long i = startDate; i <= endDate; i++) {
            //     relationshipTypes.add(RelationshipType.withName(String.valueOf(i)));
            // }
            relationshipTypes.add(RelationshipType.withName(String.valueOf(1396)));


            System.out.println("Generated relationship types for filtering: " + relationshipTypes);

            // ResourceIterable<Relationship> relationships = account.getRelationships(
            //     relationshipTypes.toArray(new RelationshipType[0])
            // );
            ResourceIterable<Relationship> relationships = account.getRelationships(
                RelationshipType.withName(String.valueOf(1396))
            );

            // print all information about the relationships
            relationships.forEach(relationship -> {
                System.out.println("Relationship found: " + relationship.toString());
                System.out.println("Start node: " + relationship.getStartNode().toString());
                System.out.println("End node: " + relationship.getEndNode().toString());
            });

            Stream<Node> incomingTransactions = relationships.stream()
                .filter(relationship -> relationship.getEndNode().hasLabel(TRANSACTION))
                .map(Relationship::getEndNode);

            Stream<Node> outgoingTransactions = relationships.stream()
                .filter(relationship -> relationship.getStartNode().hasLabel(TRANSACTION))
                .map(Relationship::getStartNode);

            Stream<Node> transactions = Stream.concat(incomingTransactions, outgoingTransactions)
                .filter(node -> {
                    long timestamp = Timestamp.getTimestamp(node);
                    System.out.println("Filtering node with timestamp: " + timestamp);
                    return startTime <= timestamp && timestamp <= endTime;
                })
                .filter(node -> {
                    Double value = SafeConvert.toDouble(node.getProperty("value", null), 0.0);
                    System.out.println("Filtering node with value: " + value);
                    return minValue <= value && value <= maxValue;
                });

            long transactionCount = transactions.count();
            System.out.println("Transactions found: " + transactionCount);

            // Recreate stream as count operation consumes it
            incomingTransactions = relationships.stream()
                .filter(relationship -> relationship.getEndNode().hasLabel(TRANSACTION))
                .map(Relationship::getEndNode);

            outgoingTransactions = relationships.stream()
                .filter(relationship -> relationship.getStartNode().hasLabel(TRANSACTION))
                .map(Relationship::getStartNode);

            transactions = Stream.concat(incomingTransactions, outgoingTransactions)
                .filter(node -> {
                    long timestamp = Timestamp.getTimestamp(node);
                    return startTime <= timestamp && timestamp <= endTime;
                })
                .filter(node -> {
                    Double value = SafeConvert.toDouble(node.getProperty("value", null), 0.0);
                    return minValue <= value && value <= maxValue;
                });

            return transactions.map(NodeResult::new);

        } catch (Exception e) {
            System.out.println("Error in transactionProcedure: " + e.getMessage());
            e.printStackTrace();
            return Stream.empty();
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
