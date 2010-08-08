package com.googlecode.groovyhttp

import org.apache.http.client.methods.HttpGet
import org.apache.http.HttpEntity
import net.htmlparser.jericho.HTMLElementName
import net.htmlparser.jericho.Source
import org.apache.log4j.BasicConfigurator
import org.apache.http.entity.BufferedHttpEntity

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
    assertTrue new Http().post("http://search.yahoo.com/web", [fr: 'sfp', p: 'groovy']).size() > 8000
  }

  void testGetForm() {
    def form = new Http().get(TEST_URL).getForm()
    assertNotNull(form)
    assertEquals TEST_URL, form.base.toString()

    assertEquals TEST_URL, new Http().get(TEST_URL).getForm(action: '/hosting/search').base.toString()
  }


  void testVariables() {
    println Http.class.metaClass.properties.collect {it.name}
  }

  void testDebug() {
    new Http().get(TEST_URL).source.getAllElements(HTMLElementName.FORM).eachWithIndex {form, i ->
      println "$i\t$form"
    }
  }

  void testGetElement() {
    assertTrue new Http().get(TEST_URL).getElement() instanceof Source
    assertEquals 'gaia', new Http().get(TEST_URL).getElement('gaia').getAttributeValue('id')
    assertEquals 'gbh', new Http().get(TEST_URL).getElement('class': 'gbh').getAttributeValue('class')
    try {
      assertEquals 'gbh', new Http().get(TEST_URL).getElement(['class', 'gbh'])
      fail("pass in a collection should throw exception")
    } catch (e) {}
    assertEquals 'gaia', new Http().get(TEST_URL).getElement {source -> source.getElementById('gaia')}.getAttributeValue('id')
  }

  void testGetElementTwoArgs() {
    assertTrue new Http().get(TEST_URL).getElement('td', ['id': 'project_labels'])?.textExtractor.toString().contains("groovy")
  }

  void testGetElementsTwoArgs() {
    assertTrue new Http().get(TEST_URL).getElements('td', ['id': 'project_labels'])?.getAt(0).textExtractor.toString().contains("groovy")
  }

  void testGetElements() {
    assertTrue new Http().get(TEST_URL).getElements().size() > 150
    assertEquals 'div', new Http().get(TEST_URL).getElements('div').getAt(0).name
    assertEquals 'gbh', new Http().get(TEST_URL).getElements('class': 'gbh').getAt(0).getAttributeValue('class')
    def http = new Http().get(TEST_URL)
    assertEquals http.source.getAllElements().findAll {it.name in ['a', 'div']}.size(), http.getElements(['a', 'div']).size()
  }

  void testClose(){
    def http = new Http(enableBuffer:true).get(TEST_URL)
    assertNotNull http.entity
    assertEquals BufferedHttpEntity.class, http.entity.getClass()
    assertNotNull http.entity.content
    http.close()
    assertNull http.entity
  }


  void XtestCustomHeader(){
    BasicConfigurator.configure()
    def http = new Http(headers:['foo':'bar'])
    http.get(TEST_URL)    
  }
}