package example;

import static org.junit.Assert.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionProcedureTest {

    private Driver driver;
    private Neo4j embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withProcedure(TransactionProcedure.class)
                .build();

        this.driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());
    }

    @AfterAll
    void closeDriver() {
        this.driver.close();
        this.embeddedDatabaseServer.close();
    }

    @AfterEach
    void cleanDb() {
        try (Session session = driver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
        }
    }

    @Test
    public void testGetTxnByLabel() {
        try (Session session = driver.session()) {

            session.run("CREATE (a1:account {address: \"TFAxtnX2z1GkmB1RKGajJB9pyHGG7nxuEY\"})\n" + 
                        "CREATE (a2:account {address: \"TNvc55H6ua3YoDazg5UKLK8wFo8bSRRF28\"})\n" + 
                        "CREATE (t:transaction {hash: \"5159fa1d1232a53bbd2fc7699039e1880a96f8fc107e34a98fd9cb4a4fbab465\",transaction_index: 24, value: 16000000})\n" + 
                        "CREATE (b:block {timestamp: 1650552468000})\n" + 
                        "MERGE (a1)-[:`1396`]->(t)\n" + 
                        "MERGE (t)-[:`1396`]->(a2)\n" + 
                        "MERGE (b)-[:`includes`]->(t)");

            // Execute our procedure against it.
            Result result = session.run("CALL chainsecurity.tron.retrieve.transaction('TFAxtnX2z1GkmB1RKGajJB9pyHGG7nxuEY', 1650452468000, 1650652468000, 0, 17000000)");

        }
    }
}
