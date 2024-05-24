package example;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.procedure.*;

import utility.Model.PathResult;

import static utility.Constant.*;
import static org.neo4j.graphdb.traversal.Uniqueness.RELATIONSHIP_GLOBAL;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import java.util.*;
import java.util.stream.Stream;

public class QuickPath extends GraphProcedure{
    @Procedure(name = "tron.quick.path", mode = Mode.READ)
    @Description("Performs quick traversal and return paths.")
    public Stream<PathResult> getPaths(
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

            return allPaths.stream().map(PathResult::new);

            // List<Path> uniqueLongestPaths = getUniqueLongestPaths(allPaths);

            // return uniqueLongestPaths.stream().map(PathResult::new);
        } catch (Exception e) {
            logger.severe(e.getMessage());
            return Stream.empty();
        }
    }
}