// package example;

// import org.junit.jupiter.api.AfterAll;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.TestInstance;
// import org.neo4j.driver.AuthTokens;
// import org.neo4j.driver.Driver;
// import org.neo4j.driver.GraphDatabase;
// import org.neo4j.driver.Session;
// import org.neo4j.driver.SessionConfig;
// import org.neo4j.driver.Config;

// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.io.IOException;

// import static org.junit.jupiter.api.Assertions.assertTrue;

// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
// public class WriteTest {

//     private Driver driver;

//     @BeforeAll
//     void initializeNeo4j() {
//         // 連接到本地運行的Neo4j實例，不使用認證
//         driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.none(), Config.defaultConfig());
//     }

//     @Test
//     void writeToTestDatabase() {
//         try (Session session = driver.session(SessionConfig.forDatabase("test"))) {
            
//             String cypherQuery = new String(Files.readAllBytes(Paths.get("C:\\Users\\user\\Desktop\\n" + //
//                                 "eo4j-procedure-template\\src\\test\\java\\example\\cypher\\createData.cql")));


//             var result = session.run(cypherQuery);
//             // assertTrue(result.hasNext(), "No node was created.");
//         }
//         catch (IOException e) {
//             e.printStackTrace();
//         }
//     }

//     @AfterAll
//     void closeDriver() {
//         if (driver != null) {
//             driver.close();
//         }
//     }
// }
