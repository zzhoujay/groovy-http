package com.googlecode.groovyhttp

import org.apache.http.client.methods.HttpGet
import org.apache.http.HttpEntity
import net.htmlparser.jericho.HTMLElementName

public class HttpTests extends GroovyTestCase {
  static TEST_URL = "http://code.google.com/p/groovy-http/"

  void testGetReturnType() {
    assertEquals Http.class, new Http()?.getClass()
    assertEquals Http.class, new Http().get(TEST_URL)?.getClass()
    new Http().get(TEST_URL) {
      delegate.assertTrue(it instanceof HttpGet)
    }
    new Http().get(TEST_URL) {get ->
      delegate.assertTrue(get instanceof HttpGet)
    }
    new Http().get(TEST_URL) {get, entity ->
      delegate.assertTrue(get instanceof HttpGet)
      delegate.assertTrue(entity instanceof HttpEntity)
    }
    new Http().get(TEST_URL) {get, entity, client ->
      delegate.assertTrue(get instanceof HttpGet)
      delegate.assertTrue(entity instanceof HttpEntity)
      delegate.assertTrue(client instanceof Http)
    }
    assertTrue new Http().get(TEST_URL) {it} instanceof HttpGet
    assertTrue new Http().get(TEST_URL) {g, e, c -> c} instanceof Http
  }


  void testGetSize() {
    assertTrue new Http().get(TEST_URL).size() > 8000
  }

  void testPost() {
    //println new Http().post(TEST_URL, [[test: 'test']]).text
    assertTrue new Http().post("http://search.yahoo.com/web",[fr:'sfp',p:'groovy']).size() > 8000
  }

  void testGetForm() {
    def form = new Http().get(TEST_URL).getForm()
    assertNotNull(form)
    assertEquals TEST_URL, form.base.toString()
  }


  void testVariables() {
    println Http.class.metaClass.properties.collect {it.name}
  }

  void testDebug() {
    new Http().get(TEST_URL).source.getAllElements(HTMLElementName.FORM).eachWithIndex {form, i ->
      println "$i\t$form"
    }
  }
}