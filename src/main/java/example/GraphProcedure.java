package example;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.procedure.*;
import utility.PathExpanderFactory;
import utility.SafeConvert;
import utility.Utility;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.sql.rowset.spi.SyncFactory;

import java.math.BigInteger;
import static utility.Constant.*;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

public class GraphProcedure {
    static final Label ACCOUNT = Label.label("account");

    static final Label TRANSACTION = Label.label("transaction");

    static final Label TOKEN_TRANSFER = Label.label("token_transfer");

    static final Label INTERNAL_TRANSACTION = Label.label("internal_transaction");

    static final RelationshipType INCLUDES = RelationshipType.withName("includes");

    static final String USDT_CONTRACT = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";

    static final String USDC_CONTRACT = "TEkxiTehnzSmSe2XqrBj4w32RUN966rdz8";

    static final Logger logger = Logger.getLogger(GraphProcedure.class.getName());

    @Context
    public Transaction tx;

    private Stream<Relationship> getRelationships(
        long limit, 
        long timespan, 
        Node last, 
        Node current, 
        Direction direction, 
        RelationshipType[] relationshipTypes
    ) {
        try {
            if (SafeConvert.toBoolean(current.getProperty("is_contract", null), false)) return Stream.empty();

            if (last == null) {
                return current.getRelationships(direction, relationshipTypes).stream().limit(limit);
            }

            Label label = last.hasLabel(TOKEN_TRANSFER) ? TOKEN_TRANSFER : last.hasLabel(INTERNAL_TRANSACTION) ? INTERNAL_TRANSACTION : TRANSACTION;
            BigInteger value = SafeConvert.toBigInteger(last.getProperty("value", null), BigInteger.ZERO).divide(BigInteger.TEN);
            long timestamp = Utility.getTimestamp(last);

            return current.getRelationships(direction, relationshipTypes).stream()
                .sorted(Comparator.comparingLong(relationship -> {
                    return SafeConvert.toLong(relationship.getOtherNode(current).getProperty("timestamp", null), 0L);
                }))
                .filter(relationship -> {
                    Node next = relationship.getOtherNode(current);
                    long nextTimestamp = Utility.getTimestamp(next);
                    BigInteger nextValue = SafeConvert.toBigInteger(next.getProperty("value", null), BigInteger.ZERO);
                    return next.hasLabel(label) 
                        && nextValue.compareTo(value) >= 0
                        && nextTimestamp <= timestamp
                        && nextTimestamp >= timestamp - timespan;
                }).limit(limit);
        } catch (Exception e) {
            logger.warning("inbound" + current.toString() + e.getMessage());
            return Stream.empty();
        }
    }

    private Stream<Relationship> outbound(
        long limit, 
        long timespan,
        Node last,
        Node current, 
        RelationshipType[] relationshipTypes
    ) {
        try {
            if (SafeConvert.toBoolean(current.getProperty("is_contract", null), false)) return Stream.empty();

            if (last == null) {
                return current.getRelationships(OUTGOING, relationshipTypes).stream().limit(limit);
            }

            Label label = last.hasLabel(TOKEN_TRANSFER) ? TOKEN_TRANSFER : last.hasLabel(INTERNAL_TRANSACTION) ? INTERNAL_TRANSACTION : TRANSACTION;
            BigInteger value = SafeConvert.toBigInteger(last.getProperty("value", null), BigInteger.ZERO).divide(BigInteger.TEN);
            long timestamp = Utility.getTimestamp(last);

            return current.getRelationships(OUTGOING, relationshipTypes).stream()
                .sorted(Comparator.comparingLong(relationship -> {
                    return SafeConvert.toLong(relationship.getOtherNode(current).getProperty("timestamp", null), 0L);
                }))
                .filter(relationship -> {
                    Node next = relationship.getOtherNode(current);
                    long nextTimestamp = Utility.getTimestamp(next);
                    BigInteger nextValue = SafeConvert.toBigInteger(next.getProperty("value", null), BigInteger.ZERO);
                    return next.hasLabel(label) 
                        && nextValue.compareTo(value) >= 0
                        && nextTimestamp >= timestamp
                        && nextTimestamp <= timestamp + timespan;
                }).limit(limit);        
        } catch (Exception e) {
            logger.warning("inbound" + current.toString() + e.getMessage());
            return Stream.empty();
        }
    }

    PathExpander<Void> createExpander(
        long limit,
        long timespan, 
        long initialTimestamp,
        Direction direction
    ) {
        return new PathExpander<>() {
            @Override
            public ResourceIterable<Relationship> expand(Path path, BranchState<Void> state) {
                try {
                    Node current;
                    RelationshipType[] relationshipTypes;
                    Iterator<Relationship> relationships;

                    if (path.length() == 0) {
                        current = path.startNode();
                        relationshipTypes = Utility.getRelationshipTypes(initialTimestamp, initialTimestamp + timespan, TRON_GENESIS_TIMESTAMP);
                        relationships = getRelationships(limit, timespan, null, current, direction, relationshipTypes).iterator();
                    } else {
                        current = path.endNode();
                        if (current.hasLabel(ACCOUNT)) {
                            Node last = path.lastRelationship().getOtherNode(current);
                            long timestamp = Utility.getTimestamp(last);
                            relationshipTypes = Utility.getRelationshipTypes(timestamp, timestamp + timespan, TRON_GENESIS_TIMESTAMP);
                            // relationships = outbound(limit, timespan, last, current, relationshipTypes).iterator();
                            relationships = getRelationships(limit, timespan, last, current, direction, relationshipTypes).iterator();
                        } else {
                            relationships = current.getRelationships(direction).iterator();
                        }
                    }

                    return new ResourceIterable<Relationship>() {
                        @Override
                        public void close() {}

                        @Override
                        public ResourceIterator<Relationship> iterator() {
                            return new ResourceIterator<Relationship>() {
                                private final Iterator<Relationship> iterator = relationships;

                                @Override
                                public void close() {}

                                @Override
                                public boolean hasNext() {
                                    return iterator.hasNext();
                                }

                                @Override
                                public Relationship next() {
                                    return iterator.next();
                                }
                            };
                        }
                    };
                } catch (Exception e) {
                    logger.warning(path.length() + "error: " + e.getMessage());
                    return PathExpanderFactory.createEmptyIterable();
                }
            }

            @Override
            public PathExpander<Void> reverse() {
                return this.reverse();
            }
        };
    }

    List<Path> getUniqueLongestPaths(List<Path> allPaths) {
        List<Path> uniqueLongestPaths = new ArrayList<>();

        for (Path thisPath : allPaths) {
            if (thisPath.startNode() == null || thisPath.endNode() == null) continue;

            boolean isSubpath = false;

            for (Path otherPath : allPaths) {
                if (thisPath == otherPath) continue;

                if (otherPath.length() < thisPath.length()) continue;

                if (otherPath.toString().contains(thisPath.toString())) {
                    isSubpath = true;
                    break;
                }
            }

            if (!isSubpath) uniqueLongestPaths.add(thisPath);
        }

        return uniqueLongestPaths;
    }
}