package example;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.stream.Stream;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import static utility.Constant.*;
import utility.Model.TransactionsAndLabels;
import utility.SafeConvert;
import utility.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class TransactionProcedure {

    static final Label ACCOUNT = Label.label("account");
    static final Label TRANSACTION = Label.label("transaction");

    @Context
    public Log log;

    @Context
    public Transaction tx;

    /**
     * Retrieves all transaction nodes associated with a specified account address, filtered by a defined 
     * time range and transaction value limits.
     *
     * @param address   The unique address of the account involved in the transactions.
     * @param startTime The start timestamp (inclusive) for filtering transactions based on their timestamp.
     * @param endTime   The end timestamp (inclusive) for filtering transactions based on their timestamp.
     * @param minValue  The minimum transaction value (inclusive) for filtering transactions based on their value.
     * @param maxValue  The maximum transaction value (inclusive) for filtering transactions based on their value.
     * @return A stream of transaction nodes that match the specified filters.
     */

    @Procedure(name = "chainsecurity.tron.retrieve.transaction", mode = Mode.READ)
    @Description("Retrieve transactions of a given ethereum account.")
    public Stream<TransactionsAndLabels> transactionProcedure(
        @Name("address") String address,
        @Name("startTime") long startTime,
        @Name("endTime") long endTime,
        @Name("minValue") Long minValue,
        @Name("maxValue") Long maxValue
    ) {
        try {
            Map<String, Object> transactionsMap = new HashMap<>();
            Map<String, Object> labelsMap = new HashMap<>();

            System.out.println("Starting transactionProcedure for address: " + address);
            System.out.println("Filtering transactions between " + startTime + " and " + endTime);
            System.out.println("Filtering transactions with value between " + minValue + " and " + maxValue);

            Node account = tx.findNode(ACCOUNT, "address", address);

            if (account == null) {
                System.out.println("Account not found: " + address);
                return Stream.empty();
            }

            System.out.println("Account found: " + account.toString());

            long startDate = Timestamp.calculateDaysBetween(TRON_GENESIS_TIMESTAMP, startTime);
            long endDate = Timestamp.calculateDaysBetween(TRON_GENESIS_TIMESTAMP, endTime);

            System.out.println("Calculated startDate: " + startDate + ", endDate: " + endDate);

            List<RelationshipType> relationshipTypes = new ArrayList<>();
            for (long i = startDate; i <= endDate; i++) {
                relationshipTypes.add(RelationshipType.withName(String.valueOf(i)));
            }


            System.out.println("Generated relationship types for filtering: " + relationshipTypes);

            ResourceIterable<Relationship> relationships = account.getRelationships(
                relationshipTypes.toArray(new RelationshipType[0])
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
                    Long value = SafeConvert.toLong(node.getProperty("value", null), 0L);
                    System.out.println("Filtering node with value: " + value);
                    return minValue <= value && value <= maxValue;
                });

            transactions.forEach(transaction -> {
                System.out.println("Processing transaction: " + transaction.toString());
                Node to = null;
                ResourceIterator<Relationship> relationship = transaction.getRelationships(OUTGOING).iterator();
                while (relationship.hasNext()) {
                    Node potentialFrom = relationship.next().getEndNode();
                    if (potentialFrom.hasLabel(ACCOUNT)) {
                        to = potentialFrom;
                        break;
                    }
                }
                System.out.println("Transaction to: " + to.toString());
                Node from = null;
                relationship = transaction.getRelationships(INCOMING).iterator();
                while (relationship.hasNext()) {
                    Node potentialFrom = relationship.next().getStartNode();
                    if (potentialFrom.hasLabel(ACCOUNT)) {
                        from = potentialFrom;
                        break;
                    }
                }
                System.out.println("Transaction from: " + from.toString());

                getResult(transaction, from, to, transactionsMap, labelsMap);
            });

            return Stream.of(new TransactionsAndLabels(transactionsMap, labelsMap));

        } catch (Exception e) {
            System.out.println("Error in transactionProcedure: " + e.getMessage());
            e.printStackTrace();
            return Stream.empty();
        }
    }


    private void getResult(Node transaction, Node from, Node to, Map<String, Object> transactionsMap, Map<String, Object> labelsMap){
        String hash = SafeConvert.toString(transaction.getProperty("hash", null), "");
        long timestamp = Timestamp.getTimestamp(transaction);
        long fee = SafeConvert.toLong(transaction.getProperty("fee", null), 0L);
        long value = SafeConvert.toLong(transaction.getProperty("value", null), 0L);
        long transaction_index = SafeConvert.toLong(transaction.getProperty("transaction_index", null), 0L);
        String fromLabel = from.getDegree() > EXCHANGE_THRESHOLD ? "deposit" : null;
        String toLabel = to.getDegree() > EXCHANGE_THRESHOLD ? "deposit" : null;
        String fromAddress = SafeConvert.toString(from.getProperty("address", null), "");
        String toAddress = SafeConvert.toString(to.getProperty("address", null), "");

        System.out.println("Transaction hash: " + hash);
        if (fromLabel != null) {
            HashSet<String> existingLabels = (HashSet<String>) labelsMap.get(fromLabel);
            if (existingLabels != null) {
                existingLabels.add(fromLabel);
                labelsMap.put(fromAddress, existingLabels);
            } else {
                HashSet<String> newLabel = new HashSet<String>();
                newLabel.add(fromLabel);
                labelsMap.put(fromAddress, newLabel);
            }
        }
        if (toLabel != null) {
            HashSet<String> existingLabels = (HashSet<String>) labelsMap.get(toLabel);
            if (existingLabels != null) {
                existingLabels.add(fromLabel);
                labelsMap.put(fromAddress, existingLabels);
            } else {
                HashSet<String> newLabel = new HashSet<String>();
                newLabel.add(fromLabel);
                labelsMap.put(fromAddress, newLabel);
            }
        }

        transactionsMap.put(transaction.getElementId(), new HashMap<>() {{
            put("hash", hash);
            put("timestamp", timestamp);
            put("value", value);
            put("fee", fee);
            put("transaction_index", transaction_index);
            put("from", fromAddress);
            put("to", toAddress);
        }});

        System.out.println("Transaction result: " + transactionsMap);

    }
}
