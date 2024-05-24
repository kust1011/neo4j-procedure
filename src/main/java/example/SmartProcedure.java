// package example;

// import org.neo4j.graphdb.*;
// import org.neo4j.graphdb.traversal.Evaluators;
// import org.neo4j.graphdb.traversal.PathEvaluator;
// import org.neo4j.graphdb.traversal.TraversalDescription;
// import org.neo4j.procedure.*;
// import utility.SafeConvert;
// import utility.Model.TransactionsAndLabels;
// import java.util.*;
// import java.util.stream.Stream;
// import static org.neo4j.graphdb.traversal.Uniqueness.RELATIONSHIP_GLOBAL;
// import static utility.Constant.*;

// public class SmartProcedure extends GraphProcedure {
//     @Procedure(name = "tron.graph.transaction", mode = Mode.READ)
//     @Description("Performs a custom traversal on the graph and return the transactions.")
//     public Stream<TransactionsAndLabels> getTransactions(
//         @Name("startNodeAddress") String startNodeAddress,
//         @Name("timespan") Long timespan,
//         @Name("maxRelationshipCount") Long maxRelationshipCount,
//         @Name("minTimestamp") long minTimestamp,
//         @Name("minValue") Double minValue,
//         @Name("maxValue") Double maxValue,
//         @Name("reverse") boolean reverse
//     ) {
//         try {
//             Node startNode = tx.findNode(ACCOUNT, "address", startNodeAddress);

//             PathEvaluator<Object> depthEvaluator = (reverse) ? Evaluators.toDepth(1) : Evaluators.includingDepths(1, 7);
            
//             long timeout = System.currentTimeMillis() + MAX_SEARCH_TIME;

//             long finalTimespan = timespan == null ? SENVEN_DAY_RANGE_MILLI : timespan;

//             long finalMaxRelationshipCount = maxRelationshipCount == null ? MAX_BRANCH_NUMBER : maxRelationshipCount;

//             double finalMinValue = minValue == null ? 0 : minValue;

//             double finalMaxValue = maxValue == null ? Double.MAX_VALUE : maxValue;

//             final TraversalDescription traversalDescription = tx.traversalDescription()
//                 .breadthFirst()
//                 .uniqueness(RELATIONSHIP_GLOBAL)
//                 .expand(createExpander(finalTimespan, finalMaxRelationshipCount, minTimestamp, finalMinValue, finalMaxValue, reverse, timeout))
//                 .evaluator(depthEvaluator);

//             List<Path> allPaths = new ArrayList<>();

//             for (Path path : traversalDescription.traverse(startNode)) {
//                 allPaths.add(path);
//             }

//             List<Path> uniqueLongestPaths = getUniqueLongestPaths(allPaths);

//             Map<String, Object> transactions = new HashMap<>();
//             Map<String, Object> labels = new HashMap<>();

//             uniqueLongestPaths.forEach(path -> {
//                 Iterator<Node> nodeIterator = path.nodes().iterator();
//                 while(nodeIterator.hasNext()) {
//                     Node node = nodeIterator.next();
//                     String address = SafeConvert.toString(node.getProperty("address", null), "");
//                     HashSet<String> existingLabels = (HashSet<String>) labels.get(address);
//                     String label = node.getDegree() > EXCHANGE_THRESHOLD ? "deposit" : null;
//                     if (label == null) continue;

//                     if (existingLabels != null) {
//                         existingLabels.add(label);
//                         labels.put(address, existingLabels);
//                     } else {
//                         HashSet<String> newLabel = new HashSet<String>();
//                         newLabel.add(label);
//                         labels.put(address, newLabel);
//                     }
//                 }

//                 Iterator<Relationship> relationshipIterator = path.relationships().iterator();
//                 while (relationshipIterator.hasNext()) {
//                     Relationship relationship = relationshipIterator.next();
//                     if (relationship != null) {
//                         long timestamp = SafeConvert.toLong(relationship.getProperty("timestamp", null), 0L);
//                         long value = SafeConvert.toLong(relationship.getProperty("value", null), 0L);
//                         String from = SafeConvert.toString(relationship.getStartNode().getProperty("address", null), "");
//                         String to = SafeConvert.toString(relationship.getEndNode().getProperty("address", null), "");
//                         String hash;
//                         String contract;
//                         String token;
//                         if (relationship.isType(TOKEN_TRANSFER)) {
//                             hash = SafeConvert.toString(relationship.getProperty("transaction_hash", null), "");
//                             contract = SafeConvert.toString(relationship.getProperty("token_address", null), "");
//                             token = contract.equals(USDT_CONTRACT) ? "usdt" : contract.equals(USDC_CONTRACT) ? "usdc" : "";
//                         } else {
//                             hash = SafeConvert.toString(relationship.getProperty("hash", null), "");
//                             contract = null;
//                             token = null;
//                         }

//                         transactions.put(relationship.getElementId(), new HashMap<>() {{
//                             put("hash", hash);
//                             put("timestamp", timestamp);
//                             put("value", value);
//                             put("from", from);
//                             put("to", to);
//                             if (contract != null) {
//                                 put("contract", contract); 
//                                 put("token", token);
//                             }
//                         }});
//                     }
//                 }
//             });

//             return Stream.of(new TransactionsAndLabels(transactions, labels));
//         } catch (Exception e) {
//             logger.severe(e.getMessage());
//             return Stream.empty();
//         }
//     }
// }