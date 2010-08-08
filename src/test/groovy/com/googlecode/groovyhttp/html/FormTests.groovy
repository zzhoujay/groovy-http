package com.googlecode.groovyhttp.html

import com.googlecode.groovyhttp.Http

public class FormTests extends GroovyTestCase{

  void testGetForm(){
    def form = new Http().get("http://code.google.com/p/groovy-http/").getForm()
    assertEquals "Search projects", form.getAllElements("type", "submit", false)?.getAt(0)?.getAttributeValue("value")
  }

  void testYahooSearch(){
    def form = new Http().get("http://search.yahoo.com/").getForm()
    println "form.element: " +form.element
    def anchors = form.submit(p: 'Groovy HTTP').getElement('web').getAllElements("class", "yschttl spt", false)
    anchors.eachWithIndex {a, i ->
      def text = a.textExtractor.toString()
      def link = a.getAttributeValue("href").with {it.substring(it.lastIndexOf('**') + 2, it.size())}
      //println "$i\t$text\t$link"
    }
    assertEquals 10, anchors.size()

  }

  void testYahooSearchShortForm(){
    assertTrue new Http().get("http://search.yahoo.com").getForm().submit(p: 'Groovy HTTP').size() > 8000
  }


}