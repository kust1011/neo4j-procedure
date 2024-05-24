package example;

import java.util.stream.Stream;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.procedure.*;

import utility.SafeConvert;
import utility.Utility;
import utility.Model.TransactionsAndLabels;

import java.util.*;

import static utility.Constant.*;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.traversal.Uniqueness.RELATIONSHIP_GLOBAL;

public class QuickProcedure extends GraphProcedure{
    @Procedure(name = "tron.quick.transaction", mode = Mode.READ)
    @Description("Performs quick traversal and return transactions.")
    public Stream<TransactionsAndLabels> getTransactions(
        @Name("address") String address,
        @Name("limit") Long limit,
        @Name("timespan") Long timespan,
        @Name("timestamp") long timestamp
    ) {
        try {
            Node startNode = tx.findNode(ACCOUNT, "address", address);
            logger.info("startNode: " + startNode.toString());

            long finalTimespan = timespan == null ? SENVEN_DAY_RANGE_MILLI : timespan;

            long finalLimit = limit == null ? MAX_BRANCH_NUMBER : limit;

            final TraversalDescription outbound = tx.traversalDescription()
                .breadthFirst()
                .uniqueness(RELATIONSHIP_GLOBAL)
                .expand(createExpander(finalLimit, finalTimespan, timestamp, OUTGOING))
                .evaluator(Evaluators.toDepth(6));

            final TraversalDescription inbound = tx.traversalDescription()
                .breadthFirst()
                .uniqueness(RELATIONSHIP_GLOBAL)
                .expand(createExpander(finalLimit, finalTimespan, timestamp, INCOMING))
                .evaluator(Evaluators.toDepth(2));

            List<Path> allPaths = new ArrayList<>();

            for (Path path : outbound.traverse(startNode)) {
                allPaths.add(path);
            }

            for (Path path : inbound.traverse(startNode)) {
                allPaths.add(path);
            }

            Map<String, Object> transactions = new HashMap<>();
            
            Map<String, Object> labels = new HashMap<>();

            allPaths.forEach(path -> {
                Iterator<Relationship> relationshipIterator = path.relationships().iterator();
                while (relationshipIterator.hasNext()) {
                    Relationship relationshipOne = relationshipIterator.next();
                    System.out.println("\n");
                    System.out.println("relation one" + relationshipOne);
                    System.out.println("startNode: ");
                    System.out.println(relationshipOne.getStartNode().getLabels());
                    System.out.println("address: " + relationshipOne.getStartNode().getProperty("address", null));
                    System.out.println("value: " + relationshipOne.getStartNode().getProperty("value", null));
                    System.out.println("endNode: ");
                    System.out.println(relationshipOne.getEndNode().getLabels());
                    System.out.println("address: " + relationshipOne.getEndNode().getProperty("address", null));
                    System.out.println("value: " + relationshipOne.getEndNode().getProperty("value", null));

                    if (!relationshipIterator.hasNext()) break;

                    Relationship relationshipTwo = relationshipIterator.next();
                    System.out.println("--------------------");
                    System.out.println("relation two" + relationshipTwo);
                    System.out.println("startNode: ");
                    System.out.println(relationshipOne.getStartNode().getLabels());
                    System.out.println("address: " + relationshipTwo.getStartNode().getProperty("address", null));
                    System.out.println("value: " + relationshipTwo.getStartNode().getProperty("value", null));
                    System.out.println("endNode: ");
                    System.out.println(relationshipTwo.getEndNode().getLabels());
                    System.out.println("address: " + relationshipTwo.getEndNode().getProperty("address", null));
                    System.out.println("value: " + relationshipTwo.getEndNode().getProperty("value", null));

                    Node start = relationshipOne.getStartNode();
                    Node transaction = relationshipOne.getEndNode();
                    Node end = relationshipTwo.getEndNode();

                    String blockTimestamp = SafeConvert.toString(Utility.getTimestamp(transaction), "");
                    String hash = Utility.getHash(transaction);
                    String value = SafeConvert.toString(transaction.getProperty("value", null), "");
                    String token = SafeConvert.toString(transaction.getProperty("token_address", null), "native");
                    String from = SafeConvert.toString(start.getProperty("address", null), "");
                    String to = SafeConvert.toString(end.getProperty("address", null), "");

                    transactions.put(transaction.getElementId(), new HashMap<>() {{
                        put("timestamp", blockTimestamp);
                        put("hash", hash);
                        put("value", value);
                        put("token", token);
                        put("from", from);
                        put("to", to);
                    }});
                }
            });

            return Stream.of(new TransactionsAndLabels(transactions, labels));
        } catch (Exception e) {
            logger.severe(e.getMessage());
            return Stream.empty();
        }
    }
}