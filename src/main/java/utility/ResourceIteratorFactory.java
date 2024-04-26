package utility;

import java.util.NoSuchElementException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;

public class ResourceIteratorFactory {
    private static final ResourceIterator<Relationship> EMPTY_ITERATOR = new ResourceIterator<Relationship>() {
        @Override
        public void close() {}

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Relationship next() {
            throw new NoSuchElementException();
        }
    };

    public static ResourceIterator<Relationship> createEmptyIterator() {
        return EMPTY_ITERATOR;
    }
}
