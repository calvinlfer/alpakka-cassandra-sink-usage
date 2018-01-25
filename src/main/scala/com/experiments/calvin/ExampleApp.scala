package com.experiments.calvin

import java.util.UUID

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.stream.scaladsl._
import com.datastax.driver.core.{BoundStatement, PreparedStatement, Session}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object ExampleApp extends App {
  implicit val system: ActorSystem = ActorSystem("sample-system")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  val settings = Settings(system)

  case class Customer(customerId: UUID, age: Int, pay: Long)

  val customers: Source[Customer, NotUsed] = Source.fromIterator(() => Iterator.continually(Customer(UUID.randomUUID(), Random.nextInt(50), Math.abs(Random.nextLong()))))

  val cassandraSink: Sink[Customer, Future[Done]] = {
    implicit val session: Session = CassandraSessionProvider(settings).get()
    val insertPreparedStatement: PreparedStatement = session.prepare(s"INSERT INTO customers_by_customer_id(customerId, age, pay) VALUES (?, ?, ?)")
    val statementBinder: (Customer, PreparedStatement) => BoundStatement = (c, ps) => ps.bind(c.customerId: java.util.UUID, c.age: java.lang.Integer, c.pay: java.lang.Long)
    CassandraSink[Customer](parallelism = 10, statement = insertPreparedStatement, statementBinder = statementBinder)
  }

  customers
    .toMat(cassandraSink)(Keep.right)
    .run()
    .onComplete(_ => system.terminate())
}
