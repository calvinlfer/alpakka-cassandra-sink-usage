# Using the Alpakka Cassandra Sink #
The Cassandra `Sink` provided by [alpakka](https://developer.lightbend.com/docs/alpakka/current/cassandra.html) is meant
to be used when you want to push elements from your stream into a Cassandra table. The example on the website shows a 
simple use case which persists an integer to a table. Here we look at how we can persist a `case class` to a target table.

```scala
case class Customer(customerId: UUID, age: Int, pay: Long)
```

One big gotcha is figuring out how to write the `BoundStatement` in Scala which involves performing type conversions to 
Java since the underlying Datastax driver that is used to communicate with Cassandra is Java based. 

```scala
  val cassandraSink: Sink[Customer, Future[Done]] = {
    implicit val session: Session = ...

    val insertPreparedStatement: PreparedStatement = session.prepare(s"INSERT INTO customers_by_customer_id(customerId, age, pay) VALUES (?, ?, ?)")

    // you need to convert each Scala type to a Java type when binding values to the prepared statement in order to create the bound statement
    // we deconstruct our Scala case class and explicitly use the Java type instead of the Scala type on the destructured fields
    val statementBinder: (Customer, PreparedStatement) => BoundStatement = (c, ps) => ps.bind(c.customerId: java.util.UUID, c.age: java.lang.Integer, c.pay: java.lang.Long)

    CassandraSink[Customer](parallelism = 10, statement = insertPreparedStatement, statementBinder = statementBinder)
  }
```

## Cassandra Table ##
The Cassandra table schema is provided here:
```cql
CREATE TABLE customers_by_customer_id (
  customerId uuid,
  age int,
  pay bigint,
  PRIMARY KEY((customerid))
);
```


## Local development ##
If you have Docker and Docker Compose installed, just run `docker-compose up` to bring up Cassandra in a Docker 
container.