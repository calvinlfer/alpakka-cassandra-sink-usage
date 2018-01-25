package com.experiments.calvin

import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.{SSLContext, TrustManagerFactory}

import com.datastax.driver.core._

class CassandraSessionProvider(settings: Settings) {
  private val config = settings.cassandra

  def get(): Session = {
    def configureSSL(): Option[JdkSSLOptions] =
      if (config.trustStorePath.isEmpty) None
      else {
        // Set up trust-store
        val trustStore = KeyStore.getInstance("JKS")
        val trustStoreFile = new FileInputStream(config.trustStorePath)
        trustStore.load(trustStoreFile, config.trustStorePass.toCharArray)
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
        tmf.init(trustStore)

        // Set up SSL context and feed in the trust-store
        val context = SSLContext.getInstance("TLS")
        context.init(null, tmf.getTrustManagers, null)
        Some(JdkSSLOptions.builder().withSSLContext(context).build())
      }

    def configurePlaintextAuth(): Option[AuthProvider] =
      for {
        user <- config.username
        pass <- config.password
      } yield new PlainTextAuthProvider(user, pass)

    val minimal = Cluster.builder().addContactPoint(config.host).withPort(config.port)
    val withSSL = configureSSL().map(sslOptions => minimal.withSSL(sslOptions)).getOrElse(minimal)
    val withAuth = configurePlaintextAuth().map(provider => withSSL.withAuthProvider(provider)).getOrElse(withSSL)

    withAuth
      .build()
      .connect(config.keyspace)
  }
}

object CassandraSessionProvider {
  def apply(settings: Settings): CassandraSessionProvider = new CassandraSessionProvider(settings)
}