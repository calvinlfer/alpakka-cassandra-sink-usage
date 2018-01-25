CREATE KEYSPACE example WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 };
USE example;
CREATE TABLE customers_by_customer_id (
  customerId uuid,
  age int,
  pay bigint,
  PRIMARY KEY((customerid))
);
