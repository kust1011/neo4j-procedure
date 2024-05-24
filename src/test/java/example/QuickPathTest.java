package example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Path;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QuickPathTest {

    private Driver driver;
    private Neo4j embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withProcedure(QuickPath.class)
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
    public void QuickPath() {
        try (Session session = driver.session()) {

            String query;
            try {
                query = new String(Files.readAllBytes(Paths.get("src/test/resources/graph.cypher")));
                // System.out.println("Query: " + query);
                session.run(query);
                System.out.println("Query executed successfully.");

                Result result = session.run("call tron.quick.path('TFRBDZHogDDpgkx9rGM7QhvGLQNsuVAAAA', 2, 86400, 1672827342)");

                while (result.hasNext()) {
                    Record record = result.next();
                    System.out.println(record.asMap());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            
            
        }
    }
}
