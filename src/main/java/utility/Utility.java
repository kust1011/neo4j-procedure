package utility;

import org.neo4j.graphdb.*;
import static org.neo4j.graphdb.Direction.INCOMING;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Utility {
    static final Label ACCOUNT = Label.label("account");
    static final RelationshipType INCLUDES = RelationshipType.withName("includes");

    private static long toEpochSecond(long timestamp) {
        if (timestamp > 1e12) {
            return timestamp / 1000;
        } else {
            return timestamp;
        }
    }

    public static long getTimestamp(Node node) {
        if (node.hasLabel(ACCOUNT)) {
            return 0L;
        }
        
        try {
            if (node.hasProperty("timestamp")) {
                return toEpochSecond(SafeConvert.toLong(node.getProperty("timestamp"), 0L));
            } else {
                return getTimestamp(node.getSingleRelationship(INCLUDES, INCOMING).getStartNode());
            }
        } catch (Exception e) {
            return 0L;
        }
    }

    public static RelationshipType getRelationshipType(long timestamp, long genesisTimestamp) {
        if (timestamp == 0L) {
            return null;
        }

        try {
            LocalDate genesisDate = Instant.ofEpochSecond(genesisTimestamp).atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate date = Instant.ofEpochSecond(timestamp).atZone(ZoneOffset.UTC).toLocalDate();
            return RelationshipType.withName(SafeConvert.toString(ChronoUnit.DAYS.between(genesisDate, date), null));
        } catch (Exception e) {
            return null;
        }
    }

    public static RelationshipType[] getRelationshipTypes(long startTimestamp, long endTimestamp, long genesisTimestamp) {
        ArrayList<RelationshipType> relationshipTypes = new ArrayList<RelationshipType>();
        if (startTimestamp == 0L) {
            return relationshipTypes.toArray(RelationshipType[]::new);
        }

        try {
            LocalDate genesisDate = Instant.ofEpochSecond(genesisTimestamp).atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate startDate = Instant.ofEpochSecond(startTimestamp).atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate endDate = Instant.ofEpochSecond(endTimestamp).atZone(ZoneOffset.UTC).toLocalDate();
            long startRelationshipType = ChronoUnit.DAYS.between(genesisDate, startDate);
            long endRelationshipType = ChronoUnit.DAYS.between(genesisDate, endDate);

            for (long i = startRelationshipType; i <= endRelationshipType; i++) {
                relationshipTypes.add(RelationshipType.withName(SafeConvert.toString(i, null)));
            }

            return relationshipTypes.toArray(RelationshipType[]::new);
        } catch (Exception e) {
            return relationshipTypes.toArray(RelationshipType[]::new);
        }
    }

    public static String getHash(Node node) {
        if (node.hasLabel(ACCOUNT)) {
            return "";
        }

        try {
            if (node.hasProperty("hash")) {
                return SafeConvert.toString(node.getProperty("hash", null), "");
            } else {
                return getHash(node.getSingleRelationship(INCLUDES, INCOMING).getStartNode());
            }
        } catch (Exception e) {
            return "";
        }
    }
}