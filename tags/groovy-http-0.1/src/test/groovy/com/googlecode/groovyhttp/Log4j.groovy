package com.googlecode.groovyhttp

import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.LogManager

public class Log4j {
  static trace() {
    LogManager.resetConfiguration()
    BasicConfigurator.configure()
    Logger.getRootLogger().setLevel(Level.INFO);
    //Logger.getLogger("com.googlecode.groovyhttp").setAdditivity(false);

    Logger.getLogger("com.googlecode.groovyhttp").setLevel(Level.TRACE);
  }

  static httpDebug() {
    Logger.getLogger("org.apache.http").setLevel(Level.DEBUG);
  }

  static set(String logger, String level) {
    Logger.getLogger(logger).setLevel(Level.toLevel(level))
  }
}