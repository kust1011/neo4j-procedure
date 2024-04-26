package utility;

import java.util.Map;
import org.neo4j.graphdb.Path;

public class Model{
    public record PathResult(Path path) {}

    public record BaseResult(Map<String, Object> result) {}

    public record TransactionsAndLabels(Map<String, Object> transactions, Map<String, Object> labels) {
        public TransactionsAndLabels(Map<String, Object> transactions, Map<String, Object> labels) {
            this.transactions = transactions;
            this.labels = labels;
        }
    }

    public record ExchangeDataResult(Map<String, Object> exchanges, Map<String, Object> unknowns) {
        public ExchangeDataResult(Map<String, Object> exchanges, Map<String, Object> unknowns) {
            this.exchanges = exchanges;
            this.unknowns = unknowns;
        }
    }
}