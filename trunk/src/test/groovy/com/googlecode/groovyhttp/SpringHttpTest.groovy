package com.googlecode.groovyhttp

import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This is an example of how to define and get Http in Spring
 */
class SpringHttpTest{
  def context = new ClassPathXmlApplicationContext("spring-context.xml")

  @Test void testSpringGetBean(){
    def http = context.getBean("http", Http.class)
    assertNotNull http
    assertEquals context.getBean("httpClient"), http.httpClient
  }
}
