CREATE (a1:account {
    address: "THRNXnx6KoFMCzfhDBaUasVFrTJPEeuQ2w",
    is_contract: "False"
})
CREATE (a2:account {
    address: "TFRBDZHogDDpgkx9rGM7QhvGLQNsuVAAAA",
    is_contract: "False"
})
CREATE (a3:account {
    address: "TPh99GxuHsHEjFMmdpGw4aNUGjJ94wBCBC",
    is_contract: "False"
})
CREATE (a4:account {
    address: "TQRP3k9gsuziDKiGEABRziaEAhjzU1tTGZ",
    is_contract: "False"
})


CREATE (tf1:token_transfer {
    token_address: "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
    value: "169000000",
    log_index: "0"
})

CREATE (tf2:token_transfer {
    token_address: "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
    value: "240000000",
    log_index: "0"
})

CREATE (tf3:token_transfer {
    token_address: "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
    value: "170000000",
    log_index: "0"
})

CREATE (tf4:token_transfer {
    token_address: "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
    value: "589960000",
    log_index: "0"
})

CREATE (tf5:token_transfer {
    token_address: "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
    value: "4016000000",
    log_index: "0"
})

CREATE (tf6:token_transfer {
    token_address: "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
    value: "2094000000",
    log_index: "0"
})

CREATE (tf7:token_transfer {
    token_address: "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
    value: "4054000000",
    log_index: "0"
})

CREATE (tf8:token_transfer {
    token_address: "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
    value: "65070000000",
    log_index: "0"
})

CREATE (tf9:token_transfer {
    token_address: "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
    value: "12295080000",
    log_index: "0"
})

CREATE (t0:transaction {
  hash: "ac09137ddbf6f3ffb39e7065376c92884173a432e1ce68e6987358eb1a97b0fa",
  transaction_index: "32",
  value: "1"
})

CREATE (b0:block {
  timestamp: "1672797081000",
  transaction_count: "20060"
})

CREATE (t1:transaction {
  hash: "17f429e7eb0e459480fcb1128993f889771095e89967e0e348f08fcff80acd95",
  transaction_index: "88",
  value: "0"
})

CREATE (b1:block {
  timestamp: "1672883403000",
  transaction_count: "63849"
})

CREATE (t2:transaction {
  hash: "55acf191efdd04f00a631dddaf3143185ea95b0c72dd7de7a9f5c4c74a32cc86",
  transaction_index: "163",
  value: "0"
})

CREATE (b2:block {
  timestamp: "1672969035000",
  transaction_count: "68348"
})

CREATE (t3:transaction {
  hash: "00fbbe324aab7c14398d5e571073f62729ce67f09737c9ab51545c3fc5378729",
  transaction_index: "130",
  value: "0"
})

CREATE (b3:block {
  timestamp: "1672969071000",
  transaction_count: "70314"
})

CREATE (t4:transaction {
  hash: "290305657daae8ceb60ffce40b44089325076d3c8726ea7dade3f6882ab72004",
  transaction_index: "214",
  value: "0"
})

CREATE (b4:block {
  timestamp: "1672882839000",
  transaction_count: "31304"
})

CREATE (t5:transaction {
  hash: "c0e09197dd866580dd032ab8d9af02673860d4b0e8941f14b8b639da4ec6e312",
  transaction_index: "140",
  value: "0"
})

CREATE (b5:block {
  timestamp: "1672904382000",
  transaction_count: "100794"
})

CREATE (t6:transaction {
  hash: "5a4ffb2a2b3ce4ec69643e54132d2b414545b3318760807ad001acc65d23ff92",
  transaction_index: "34",
  value: "0"
})

CREATE (b6:block {
  timestamp: "1672904349000",
  transaction_count: "97557"
})

CREATE (t7:transaction {
  hash: "cd04c829e5f437724a60f68e057cc920cf37bc67c10fcb1dc25d913360eb1c29",
  transaction_index: "184",
  value: "0"
})

CREATE (b7:block {
  timestamp: "1672848003000",
  transaction_count: "20666"
})

CREATE (t8:transaction {
  hash: "310b1bc45d05b937faf6fb48d7ec4fe1e551712de6f9465fd50ab05af100030a",
  transaction_index: "119",
  value: "0"
})

CREATE (b8:block {
  timestamp: "1672797903000",
  transaction_count: "75863"
})

CREATE (t9:transaction {
  hash: "a5afee9093e270116505a5d253c853e9183cdef6d1e9cc0e098dc72ebb19aac7",
  transaction_index: "184",
  value: "0"
})

CREATE (b9:block {
  timestamp: "1672882764000",
  transaction_count: "26975"
})

MERGE (b0)-[:`includes`]->(t0)
MERGE (t0)-[:`1654`]->(a2)


MERGE (a1)-[:`1654`]->(tf1)
MERGE (a1)-[:`1655`]->(tf2)
MERGE (a1)-[:`1655`]->(tf3)
MERGE (tf4)-[:`1654`]->(a1)


MERGE (tf1)-[:`1654`]->(a2)
MERGE (tf2)-[:`1655`]->(a2)
MERGE (tf3)-[:`1655`]->(a2)
// MERGE (a3)-[:`1654`]->(tf4)


MERGE (tf5)-[:`1655`]->(a2)
MERGE (tf6)-[:`1655`]->(a2)
MERGE (a2)-[:`1654`]->(tf7)
MERGE (a2)-[:`1654`]->(tf8)


MERGE (a4)-[:`1655`]->(tf5)
MERGE (a4)-[:`1655`]->(tf6)
MERGE (tf7)-[:`1654`]->(a3)
MERGE (tf8)-[:`1654`]->(a3)

MERGE (tf9)-[:`1654`]->(a4)
// MERGE (a3)-[:`1655`]->(tf9)


MERGE (b1)-[:`includes`]->(t1)
MERGE (t1)-[:`includes`]->(tf1)
MERGE (b2)-[:`includes`]->(t2)
MERGE (t2)-[:`includes`]->(tf2)
MERGE (b3)-[:`includes`]->(t3)
MERGE (t3)-[:`includes`]->(tf3)
MERGE (b4)-[:`includes`]->(t4)
MERGE (t4)-[:`includes`]->(tf4)
MERGE (b5)-[:`includes`]->(t5)
MERGE (t5)-[:`includes`]->(tf5)
MERGE (b6)-[:`includes`]->(t6)
MERGE (t6)-[:`includes`]->(tf6)
MERGE (b7)-[:`includes`]->(t7)
MERGE (t7)-[:`includes`]->(tf7)
MERGE (b8)-[:`includes`]->(t8)
MERGE (t8)-[:`includes`]->(tf8)
MERGE (b9)-[:`includes`]->(t9)
MERGE (t9)-[:`includes`]->(tf9)