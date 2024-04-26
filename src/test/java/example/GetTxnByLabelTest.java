package example;

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
public class GetTxnByLabelTest {

    private Driver driver;
    private Neo4j embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withProcedure(GetTxnByLabel.class)
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
            session.run("CREATE (t1:Transaction:Y2019 {hash: '0x2f0d6b5287f4f44bc266508ee383a911ab7b06aac0fd65fb91633051e3e958d7'})\r\n" +
                        "CREATE (t2:Transaction:Y2019:M05 {hash: '0x2f0d6b5287f4f44bc266508ee383a911ab7b06aac0fd65fb91633051e3e958d7'})\r\n" +
                        "CREATE (t3:Transaction:Y2019:M05:D15 {hash: '0x2f0d6b5287f4f44bc266508ee383a911ab7b06aac0fd65fb91633051e3e958d7'})\r\n" +
                        "CREATE (t4:Transaction:Y2020 {hash: '0x2f0d6b5287f4f44bc266508ee383a911ab7b06aac0fd65fb91633051e3e958d7'})\r\n" +
                        "CREATE (t5:Transaction:Y2020:M08 {hash: '0x2f0d6b5287f4f44bc266508ee383a911ab7b06aac0fd65fb91633051e3e958d7'})\r\n" +
                        "CREATE (t6:Transaction:Y2020:M08:D03 {hash: '0x2f0d6b5287f4f44bc266508ee383a911ab7b06aac0fd65fb91633051e3e958d7'})\r\n" +
                        "CREATE (t7:Transaction:Y2021 {hash: '0x2f0d6b5287f4f44bc266508ee383a911ab7b06aac0fd65fb91633051e3e958d7'})\r\n" +
                        "CREATE (t8:Transaction:Y2021:M12 {hash: '0x2f0d6b5287f4f44bc266508ee383a911ab7b06aac0fd65fb91633051e3e958d7'})\r\n" +
                        "CREATE (t9:Transaction:Y2021:M12:D31 {hash: '0x2f0d6b5287f4f44bc266508ee383a911ab7b06aac0fd65fb91633051e3e958d7'})");

            // Execute our procedure against it.
            Result result = session.run("CALL example.getTxnByLabel(['Y2019', 'M05']) YIELD node");

            // Verify the number
            assertThat(result.stream()).hasSize(2);
        }
    }
}
