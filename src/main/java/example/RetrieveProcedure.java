package example;

import java.util.logging.Logger;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.procedure.Context;
import org.neo4j.graphdb.Transaction;

public class RetrieveProcedure {
    static final Label ACCOUNT = Label.label("account");

  static final RelationshipType TRANSACTION = RelationshipType.withName("transaction");

  static final RelationshipType TOKEN_TRANSFER = RelationshipType.withName("token_transfer");

  static final String USDT_CONTRACT = "0xdac17f958d2ee523a2206206994597c13d831ec7";

  static final String USDC_CONTRACT = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48";

  static final Logger logger = Logger.getLogger(RetrieveProcedure.class.getName());

  @Context
  public Transaction tx;
}

