package utility;

import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;

public class PathExpanderFactory {
    private static final ResourceIterable<Relationship> EMPTY_ITERABLE = new ResourceIterable<Relationship>() {
        @Override
        public void close() {}

        @Override
        public ResourceIterator<Relationship> iterator() {
            return ResourceIteratorFactory.createEmptyIterator();
        }
    };

    public static ResourceIterable<Relationship> createEmptyIterable() {
        return EMPTY_ITERABLE;
    }
}
