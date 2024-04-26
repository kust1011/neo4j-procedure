package example;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.Direction.BOTH;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

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

import utility.Timeout;
import utility.Model.TransactionsAndLabels;
import utility.SafeConvert;
import static utility.Constant.*;

/**
 * This example demonstrates how to return nodes based on a specific label.
 */
public class TokenTransferProcedure {

    static final Label ACCOUNT = Label.label("account");
    static final Label TRANSACTION = Label.label("transaction");
    static final Label TOKEN_TRANSFER = Label.label("token_transfer");

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
    @Procedure(name = "ethereum.retrieve.tokenTransfer", mode = Mode.READ)
    @Description("Retrieve token transfers of a given ethereum account.")
    public Stream<TransactionsAndLabels> getTokenTransfer(
        @Name("address") String address,
        @Name("startTime") long startTime,
        @Name("endTime") long endTime,
        @Name("minValue") Double minValue,
        @Name("maxValue") Double maxValue
    ) {
        final List<TransactionsAndLabels> result = new ArrayList<>();

        Timeout.executeWithTimeout(() -> {
            try {
                Map<String, Object> transactions = new HashMap<>();
                Map<String, Object> labels = new HashMap<>();

                Node account = tx.findNode(ACCOUNT, "address", address.toLowerCase());

                if (account == null) {
                    log.info("Account not found: " + address);
                    return;
                }

                // find the outgoing & ingoing node, and check if it is a token_transfer
                ResourceIterable<Relationship> outgoing = account.getRelationships(OUTGOING);
                Stream<NodeResult> tokenTransferOut = outgoing.stream()
                    .filter(relationship -> relationship.getEndNode().hasLabel(TOKEN_TRANSFER))
                    .map(relationship -> new NodeResult(relationship.getEndNode()));

                ResourceIterable<Relationship> ingoing = account.getRelationships(INCOMING);
                Stream<NodeResult> tokenTransferIn = ingoing.stream()
                    .filter(relationship -> relationship.getStartNode().hasLabel(TOKEN_TRANSFER))
                    .map(relationship -> new NodeResult(relationship.getStartNode()));

                Stream<NodeResult> tokenTransfers = Stream.concat(tokenTransferOut, tokenTransferIn);

                // filter the value of token_transfer
                // by checking the token_transfer node where minValue <= value <= maxValue
                tokenTransfers = tokenTransfers
                    .filter(nodeResult -> {
                        Node tokenTransfer = nodeResult.node;
                        Double value = SafeConvert.toDouble(tokenTransfer.getProperty("value", null), 0.0);
                        return minValue <= value && value <= maxValue;
                    });

                // (blk:block)-[:includes]->(:transaction)-[:includes]->(:token_transfer)
                // filter the token_transfer block
                // by checking the include block node where startTime < timestamp(blk) < endTime
                tokenTransfers.filter(nodeResult -> {
                        Node tokenTransfer = nodeResult.node;
                        ResourceIterable<Relationship> incoming = tokenTransfer.getRelationships(INCOMING);
                        return incoming.stream()
                            .filter(relationship -> relationship.isType(RelationshipType.withName("includes")))
                            .filter(relationship -> relationship.getStartNode().hasLabel(Label.label("transaction")))
                            .anyMatch(relationship -> {
                                Node transaction = relationship.getStartNode();
                                ResourceIterable<Relationship> incomingTransaction = transaction.getRelationships(INCOMING);
                                return incomingTransaction.stream()
                                    .filter(relationship2 -> relationship2.isType(RelationshipType.withName("includes")))
                                    .filter(relationship2 -> relationship2.getStartNode().hasLabel(Label.label("block")))
                                    .anyMatch(relationship2 -> {
                                        Node block = relationship2.getStartNode();
                                        long timestamp = SafeConvert.toLong(block.getProperty("timestamp", null), 0L);
                                        return startTime <= timestamp && timestamp <= endTime;
                                    });
                            });

                    }).forEach(tokenTransfer -> {
                        try {
                            getResult(tokenTransfer.node, transactions, labels);
                        } catch (Exception e) {
                            log.warn("token forEach error" + e.getMessage());
                        }
                    });
                
                result.add(new TransactionsAndLabels(transactions, labels));
            } 
            catch (Exception e) {
                log.error("Error in transactionProcedure: " + e.getMessage());
            }
        }, 10000);

        return result.stream();
    }

    private void getResult(Node tokenTransfer, Map<String, Object> transactions, Map<String, Object> labels) {
        // (:transaction)-[:includes]->(:token_transfer)
        // find the hash value in transaction node
        String hash = SafeConvert.toString(tokenTransfer.getSingleRelationship(RelationshipType.withName("includes"), INCOMING).getStartNode().getProperty("hash", null), "");
        // (blk:block)-[:includes]->(:transaction)-[:includes]->(:token_transfer)
        // find the timestamp value in block node
        long timestamp = SafeConvert.toLong(tokenTransfer.getSingleRelationship(RelationshipType.withName("includes"), INCOMING).getStartNode().getSingleRelationship(RelationshipType.withName("includes"), INCOMING).getStartNode().getProperty("timestamp", null), 0L);
        long value = SafeConvert.toLong(tokenTransfer.getProperty("value", null), 0L);
        // (from:account)-->(:token_transfer)-->(to:account)
        // get incoming node (relation name is unknown)
        Node from = null;
        Iterable<Relationship> incomingRelationships = tokenTransfer.getRelationships(INCOMING);
        for (Relationship relationship : incomingRelationships) {
            Node incomingNode = relationship.getStartNode();
            from = incomingNode.hasLabel(ACCOUNT) ? incomingNode : from;
        }

        Node to = null;
        Iterable<Relationship> outgoingRelationships = tokenTransfer.getRelationships(OUTGOING);
        for (Relationship relationship : outgoingRelationships) {
            Node outgoingNode = relationship.getEndNode();
            to = outgoingNode.hasLabel(ACCOUNT) ? outgoingNode : to;
        }
        String fromLabel = from.getDegree() > EXCHANGE_THRESHOLD ? "deposit" : null;
        String toLabel = to.getDegree() > EXCHANGE_THRESHOLD ? "deposit" : null;
        String fromAddress = SafeConvert.toString(from.getProperty("address", null), "");
        String toAddress = SafeConvert.toString(to.getProperty("address", null), "");
        // String testAddress = SafeConvert.toString(test.getProperty("address", null), "");
        // String contract = SafeConvert.toString(tokenTransfer.getProperty("token_address", null), USDT_CONTRACT);
        // String token = contract.equals(USDT_CONTRACT) ? "usdt" : "usdc";

        if (fromLabel != null) {
            HashSet<String> existingLabels = (HashSet<String>) labels.get(fromLabel);
            if (existingLabels != null) {
                existingLabels.add(fromLabel);
                labels.put(fromAddress, existingLabels);
            } else {
                HashSet<String> newLabel = new HashSet<String>();
                newLabel.add(fromLabel);
                labels.put(fromAddress, newLabel);
            }
        }
        if (toLabel != null) {
            HashSet<String> existingLabels = (HashSet<String>) labels.get(toLabel);
            if (existingLabels != null) {
                existingLabels.add(fromLabel);
                labels.put(fromAddress, existingLabels);
            } else {
                HashSet<String> newLabel = new HashSet<String>();
                newLabel.add(fromLabel);
                labels.put(fromAddress, newLabel);
            }
        }

        transactions.put(tokenTransfer.getElementId(), new HashMap<>() {{
            put("hash", hash);
            put("timestamp", timestamp);
            put("value", value);
            put("from", fromAddress);
            put("to", toAddress);
            // put("test", testAddress);
            // put("contract", contract);
            // put("token", token);
        }});
    }

    public static class NodeResult {
        public Node node;

        public NodeResult(Node node) {
            this.node = node;
        }
    }
}
