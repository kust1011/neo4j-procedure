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

public class TokenTransferProcedure {

    static final Label ACCOUNT = Label.label("account");
    static final Label TRANSACTION = Label.label("transaction");
    static final Label TOKEN_TRANSFER = Label.label("token_transfer");

    @Context
    public Log log;

    @Context
    public Transaction tx;

    /**
     * Retrieves all nodes corresponding to token transfers associated with a specified account address
     * within a defined time range and value limits.
     *
     * @param address   The unique address of the account.
     * @param startTime The start timestamp (inclusive) for filtering token transfers.
     * @param endTime   The end timestamp (inclusive) for filtering token transfers.
     * @param minValue  The minimum transaction value (inclusive) for filtering token transfers.
     * @param maxValue  The maximum transaction value (inclusive) for filtering token transfers.
     * @return A stream of nodes representing token transfers that meet the specified criteria.
     */

    @Procedure(name = "chainsecurity.tron.retrieve.tokenTransfer", mode = Mode.READ)
    @Description("Retrieve tokenTransfer of a given tron account.")
    public Stream<TransactionsAndLabels> getTokenTransfer(
        @Name("address") String address,
        @Name("startTime") long startTime,
        @Name("endTime") long endTime,
        @Name("minValue") Long minValue,
        @Name("maxValue") Long maxValue
    ) {
        try {
            Map<String, Object> tokenTransfersMap = new HashMap<>();
            Map<String, Object> labelsMap = new HashMap<>();

            System.out.println("Starting tokenTransferProcedure for address: " + address);
            System.out.println("Filtering tokenTransfers between " + startTime + " and " + endTime);
            System.out.println("Filtering tokenTransfers with value between " + minValue + " and " + maxValue);

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

            Stream<Node> incomingTokenTransfers = relationships.stream()
                .filter(relationship -> relationship.getEndNode().hasLabel(TOKEN_TRANSFER))
                .map(Relationship::getEndNode);

            Stream<Node> outgoingTokenTransfers = relationships.stream()
                .filter(relationship -> relationship.getStartNode().hasLabel(TOKEN_TRANSFER))
                .map(Relationship::getStartNode);

            Stream<Node> tokenTransfers = Stream.concat(incomingTokenTransfers, outgoingTokenTransfers)
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

            
            tokenTransfers.forEach(tokenTransfer -> {
                System.out.println("Processing tokenTransfer: " + tokenTransfer.toString());
                Node to = null;
                ResourceIterator<Relationship> relationship = tokenTransfer.getRelationships(OUTGOING).iterator();
                while (relationship.hasNext()) {
                    Node potentialFrom = relationship.next().getEndNode();
                    if (potentialFrom.hasLabel(ACCOUNT)) {
                        to = potentialFrom;
                        break;
                    }
                }
                System.out.println("TokenTransfer to: " + to.toString());
                Node from = null;
                relationship = tokenTransfer.getRelationships(INCOMING).iterator();
                while (relationship.hasNext()) {
                    Node potentialFrom = relationship.next().getStartNode();
                    if (potentialFrom.hasLabel(ACCOUNT)) {
                        from = potentialFrom;
                        break;
                    }
                }
                System.out.println("TokenTransfer from: " + from.toString());

                getResult(tokenTransfer, from, to, tokenTransfersMap, labelsMap);
            });

            return Stream.of(new TransactionsAndLabels(tokenTransfersMap, labelsMap));

        } catch (Exception e) {
            System.out.println("Error in tokenTransferProcedure: " + e.getMessage());
            e.printStackTrace();
            return Stream.empty();
        }
    }


    private void getResult(Node tokenTransfer, Node from, Node to, Map<String, Object> tokenTransfersMap, Map<String, Object> labelsMap){
        Node transactionNode = tokenTransfer.getSingleRelationship(RelationshipType.withName("includes"), INCOMING).getStartNode();
        String transaction_hash = SafeConvert.toString(transactionNode.getProperty("hash", null), "");
        String token_address = SafeConvert.toString(tokenTransfer.getProperty("token_address", null), "");
        long timestamp = Timestamp.getTimestamp(tokenTransfer);
        long value = SafeConvert.toLong(tokenTransfer.getProperty("value", null), 0L);
        long log_index = SafeConvert.toLong(tokenTransfer.getProperty("log_index", null), 0L);
        String fromLabel = from.getDegree() > EXCHANGE_THRESHOLD ? "deposit" : null;
        String toLabel = to.getDegree() > EXCHANGE_THRESHOLD ? "deposit" : null;
        String fromAddress = SafeConvert.toString(from.getProperty("address", null), "");
        String toAddress = SafeConvert.toString(to.getProperty("address", null), "");

        System.out.println("TokenTransfer hash: " + transaction_hash);
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

        tokenTransfersMap.put(tokenTransfer.getElementId(), new HashMap<>() {{
            put("transaction_hash", transaction_hash);
            put("timestamp", timestamp);
            put("log_index", log_index);
            put("token_address", token_address);
            put("value", value);
            put("from", fromAddress);
            put("to", toAddress);
        }});

        System.out.println("TokenTransfer result: " + tokenTransfersMap);

    }
}
