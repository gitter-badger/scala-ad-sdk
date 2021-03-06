package com.github.emildafinov.ad.sdk.configuration

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Module that resolves and exposes the [[com.typesafe.config.Config]] instance containing
  * the application configuration
  */
private[sdk] trait ApplicationConfigurationModule {
  lazy val config: Config = ConfigFactory.load()
}


