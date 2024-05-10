package utility;

import org.neo4j.graphdb.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class Timestamp {

    static final Label TRANSACTION = Label.label("transaction");
    static final Label TOKEN_TRANSFER = Label.label("token_transfer");

    /**
     * Retrieve the timestamp of the given node.
     * @param node the node to retrieve the timestamp from
     * @return the timestamp of the block the node belongs to
     */
    public static Long getTimestamp(Node node) {
        // Retrieve the timestamp of "(blk:block)"
        try {
            if (node.hasLabel(TRANSACTION)) {
                Node blockNode = node.getSingleRelationship(RelationshipType.withName("includes"), Direction.INCOMING).getStartNode();
                return SafeConvert.toLong(blockNode.getProperty("timestamp", null), 0L);
            } else if (node.hasLabel(TOKEN_TRANSFER)) {
                Node transactionNode = node.getSingleRelationship(RelationshipType.withName("includes"), Direction.INCOMING).getStartNode();
                Node blockNode = transactionNode.getSingleRelationship(RelationshipType.withName("includes"), Direction.INCOMING).getStartNode();
                return SafeConvert.toLong(blockNode.getProperty("timestamp", null), 0L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1L;
    }

    /**
     * Calculate the number of days between two timestamps.
     * @param timestamp1 the first timestamp
     * @param timestamp2 the second timestamp
     * @return the number of days between the two timestamps
     */
    public static long calculateDaysBetween(long timestamp1, long timestamp2) {
        // Convert timestamp to LocalDate
        LocalDate date1 = Instant.ofEpochMilli(timestamp1).atOffset(ZoneOffset.UTC).toLocalDate();
        LocalDate date2 = Instant.ofEpochMilli(timestamp2).atOffset(ZoneOffset.UTC).toLocalDate();

        // Calculate the number of days between the two dates
        return ChronoUnit.DAYS.between(date1, date2);
    }

    public static void main(String[] args) {
        Timestamp calculator = new Timestamp();

        // Example timestamps
        long timestamp1 = 1529891469000L;
        long timestamp2 = 1650552468000L;


        // Calculate the difference in days
        long daysBetween = calculator.calculateDaysBetween(timestamp1, timestamp2);
        System.out.println("Days between: " + daysBetween);
    }
}
