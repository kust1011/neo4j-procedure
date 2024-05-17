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

    /**
     * Test for the GetTxnByLabel procedure.
     */
    @Test
    public void testGetTxnByLabel() {
        try (Session session = driver.session()) {

            // Create our data in the database.
            // session.run("CREATE (a1:account {address: \"0xd2220a0cf47c7b9be7a2e6ba89f429762e7b9adb\"})\r\n" + //
            //             "CREATE (a2:account {address: \"0xcda1ad7542e30bf520652a05056ebe0105c7e49a\"})\r\n" + //
            //             "CREATE (a3:account {address: \"0xaf880fc7567d5595cacce15c3fc14c8742c26c9e\"})\r\n" + //
            //             "CREATE (a4:account {address: \"0xf86a3ea8071f7095c7db8a05ae507a8929dbb876\"})\r\n" + //
            //             "CREATE (tf1:token_transfer {token_address: \"0xf5eced2f682ce333f96f2d8966c613ded8fc95dd\",value: 200000})\r\n" + //
            //             "CREATE (t1:transaction {hash: \"0x5bea8acec667ba815637088b00f8369d74fd9008cb6e494ffbb042f55b20d666\",gas_used: 50853})\r\n" + //
            //             "CREATE (b1:block {timestamp: 1446553206})\r\n" + //
            //             "CREATE (tf2:token_transfer {token_address: \"0x27cb40ce7eb4d078196923d608eb903a17e0c0ed\",value: 3000})\r\n" + //
            //             "CREATE (t2:transaction {hash: \"0x19f1df2c7ee6b464720ad28e903aeda1a5ad8780afc22f0b960827bd4fcf656d\",gas_used: 21000})\r\n" + //
            //             "CREATE (b2:block {timestamp: 1438270048})\r\n" + //
            //             "MERGE (a1)-[:`95`]->(tf1)\r\n" + //
            //             "MERGE (a1)-[:`95`]->(t1)\r\n" + //
            //             "MERGE (tf1)-[:`95`]->(a2)\r\n" + //
            //             "MERGE (t1)-[:`includes`]->(tf1)\r\n" + //
            //             "MERGE (b1)-[:`includes`]->(t1)\r\n" + //
            //             "MERGE (a3)-[:`95`]->(tf2)\r\n" + //
            //             "MERGE (a3)-[:`95`]->(t2)\r\n" + //
            //             "MERGE (tf2)-[:`95`]->(a4)\r\n" + //
            //             "MERGE (t2)-[:`includes`]->(tf2)\r\n" + //
            //             "MERGE (b2)-[:`includes`]->(t2)");
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

            // // Verify the data
            // result.list().forEach(record -> {
            //     Map<String, Object> transactions = record.get("transactions").asMap();
            //     Map<String, Object> labels = record.get("labels").asMap();
            
            //     transactions.forEach((key, value) -> System.out.println(key + ": " + value));
            
            //     labels.forEach((key, value) -> System.out.println(key + ": " + value));
            // });
            

            
        }
    }
}
