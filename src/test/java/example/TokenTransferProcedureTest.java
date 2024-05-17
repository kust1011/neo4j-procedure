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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TokenTransferProcedureTest {

    private Driver driver;
    private Neo4j embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withProcedure(TokenTransferProcedure.class)
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

            session.run("CREATE (a1:account {address: \"TQvHKMHC2ytau2N5sFbjQHQyZ6w4itCGJ5\"})\n" + 
                        "CREATE (a2:account {address: \"TGx3Lyc6rR14keStqfAK8u37euNFaq8uZ4\"})\n" + 
                        "CREATE (tt:token_transfer {token_address: \"TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t\", value: 99000000, log_index: 0})\n" + 
                        "CREATE (t:transaction {hash: \"ae6bc3ceecf7a0a2df8719fbbe9c31d66dbc7e9bd74aa083b710ebfbaf880a0a\",transaction_index: 54, value: 0, fee: 345})\n" + 
                        "CREATE (b:block {timestamp: 1650552468000})\n" + 
                        "MERGE (a1)-[:`1396`]->(t)\n" + 
                        "MERGE (a1)-[:`1396`]->(tt)\n" + 
                        "MERGE (tt)-[:`1396`]->(a2)\n" + 
                        "MERGE (t)-[:`includes`]->(tt)\n" + 
                        "MERGE (b)-[:`includes`]->(t)");

            // Execute our procedure against it.
            Result result = session.run("CALL chainsecurity.tron.retrieve.tokenTransfer('TQvHKMHC2ytau2N5sFbjQHQyZ6w4itCGJ5', 1650452468000, 1650652468000, 0, 100000000)");

            
        }
    }
}
