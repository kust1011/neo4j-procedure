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

    /**
     * Test for the GetTxnByLabel procedure.
     */
    @Test
    public void testGetTxnByLabel() {
        try (Session session = driver.session()) {

            // Create our data in the database.
            session.run("CREATE (a1:account {address: \"0xd1220a0cf47c7b9be7a2e6ba89f429762e7b9adb\"})\r\n" + //
                        "CREATE (a2:account {address: \"0xcda0ad7542e30bf520652a05056ebe0105c7e49a\"})\r\n" + //
                        "CREATE (tf:token_transfer)\r\n" + //
                        "CREATE (t:transaction {hash: \"0x5bea7acec667ba815637088b00f8369d74fd9008cb6e494ffbb042f55b20d666\",gas_used: 50853})\r\n" + //
                        "CREATE (b:block {timestamp: 1446553206})\r\n" + //
                        "MERGE (a1)-[:`95`]->(tf)\r\n" + //
                        "MERGE (a1)-[:`95`]->(t)\r\n" + //
                        "MERGE (tf)-[:`95`]->(a2)\r\n" + //
                        "MERGE (t)-[:`includes`]->(tf)\r\n" + //
                        "MERGE (b)-[:`includes`]->(t)");

            // Execute our procedure against it.
            Result result = session.run("CALL ethereum.retrieve.transaction('0xd1220a0cf47c7b9be7a2e6ba89f429762e7b9adb', 1246553206, 1625130800)\r\n" + //
                                "YIELD node\r\n" + //
                                "RETURN node;\r\n");

            // Verify the transaction node
            Record record = result.single();
            Value node = record.get("node");
            assertThat(node.get("hash").asString()).isEqualTo("0x5bea7acec667ba815637088b00f8369d74fd9008cb6e494ffbb042f55b20d666");

            
        }
    }
}
