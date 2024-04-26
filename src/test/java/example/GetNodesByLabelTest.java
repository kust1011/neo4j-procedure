package example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

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
import org.neo4j.driver.Values;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import org.neo4j.driver.types.Node;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetNodesByLabelTest {

    private Driver driver;
    private Neo4j embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withProcedure(GetNodesByLabel.class)
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
     * Test for the GetNodesByLabel procedure.
     */
    @Test
    public void testGetNodesByLabel() {
        try (Session session = driver.session()) {

            //Create our data in the database.
            session.run("CREATE (b:Block {number: 5500000, timestamp: 1524611221})\r\n" + //
                        "CREATE (a1:Account {address: '0xe24397398f125fc64c32a109d15a8ce480f936d9'})\r\n" + //
                        "CREATE (a2:Account {address: '0x6dc06ecb14160c05dc1002b2a6aea3cbb4880b12'})\r\n" + //
                        "CREATE (t:Transaction {hash: '0x2f0d6b5287f4f44bc266508ee383a911ab7b06aac0fd65fb91633051e3e958d7'})\r\n" + //
                        "MERGE (a1)-[:`40`]->(t)\r\n" + //
                        "MERGE (t)-[:`40`]->(a2)\r\n" + //
                        "MERGE (b)-[:includes]->(t)");
            
            //Execute our procedure against it.
            Result result = session.run("CALL example.getNodesByLabel(\"Account\")");
            List<Record> resultList = result.list();
            assertThat(resultList).hasSize(2);
            for (Record record : resultList) {
                Node node = record.get("node").asNode();
                assertThat(node.labels()).containsExactly("Account");
            }
            
            //Execute our procedure against it.
            result = session.run("CALL example.getNodesByLabel(\"Transaction\")");
            resultList = result.list();
            assertThat(resultList).hasSize(1);
            for (Record record : resultList) {
                Node node = record.get("node").asNode();
                assertThat(node.labels()).containsExactly("Transaction");
            }

            //Execute our procedure against it.
            result = session.run("CALL example.getNodesByLabel(\"Block\")");
            resultList = result.list();
            assertThat(resultList).hasSize(1);
            for (Record record : resultList) {
                Node node = record.get("node").asNode();
                assertThat(node.labels()).containsExactly("Block");
            }


            
        }
    }
}
