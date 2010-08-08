package com.googlecode.groovyhttp.html

import net.htmlparser.jericho.Element
import net.htmlparser.jericho.HTMLElementName
import com.googlecode.groovyhttp.Http
import net.htmlparser.jericho.FormField
import org.apache.http.message.BasicNameValuePair
import net.htmlparser.jericho.FormFields
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log

public class Form {
  static Log logger = LogFactory.getLog(Form.class)
  @Delegate Element element;
  def http, base, parameters;

  def Form(Http http, Element element) {
    this.element = element
    this.base = http.'uri' instanceof String ? new URI(http.'uri') : http.'uri'
    this.http = http;
    if (this.element?.name != HTMLElementName.FORM) throw new IllegalArgumentException("Form constructor takes a Form element only, element.name: ${element?.name}")
  }

  def submit(nameValues, Closure closure = null) {
    def result = http, action = base?.resolve(element.getAttributeValue("action"))
    if (!action) new IllegalStateException("form has no valid action attribute")
    FormFields fields = fill(element.getFormFields(), nameValues)
    if (logger.isTraceEnabled()) logger.trace("submit() - action: $action, nameValues: $nameValues")
    return http.post(action, fields, closure);
  }

  static fill(FormFields fields, input) {
    if (input instanceof Collection) {
      input.each { fill(fields, it)}
    } else if (input instanceof Map) {
      input.each {k, v -> fields.setValue(k, String.valueOf(v))}
    } else {
      throw new UnsupportedOperationException("only Collection and Map are supported")
    }
    return fields;
  }
}