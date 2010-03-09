package com.googlecode.groovyhttp

import com.googlecode.groovyhttp.html.Form
import net.htmlparser.jericho.FormControl
import net.htmlparser.jericho.FormFields
import net.htmlparser.jericho.HTMLElementName
import net.htmlparser.jericho.Source
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.http.HttpEntity
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.BufferedHttpEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.protocol.HTTP
import org.apache.http.protocol.HttpContext
import org.apache.http.HttpResponseInterceptor
import org.apache.http.HttpResponse
import org.apache.http.entity.InputStreamEntity

/**
 * This HTTP Client is designed to provide a set of very simple API for use in Groovy.
 *
 * Requirement:
 *  - expose all HTTP Client objects
 *  - support Jericho HTML
 *  - No need to be thread-safe
 *
 * The main action methods allow providing a Closure as an execution context, e.g.
 * new Http().get(url){ get-> xxx }* When a closure is used, the get method return the return value of the get method rather than the Http instance. If
 * you want to return the Http instance, use:
 *  new Http().get(url){ get, client -> xxx; client }*/
//@Grab(group='com.jidesoft', module='jide-oss', version='[2.2.1,2.3.0)')
public class Http{
  static Log log = LogFactory.getLog(Http.class)
  // Firefox 3 on Windows Vista
  static final String USER_AGENT = 'Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.0.1) Gecko/2008070208 Firefox/3.0.1'
  def httpClient;
  def enableBuffer = true, reset = false
  def referer, uri, request, source;// instance of the last fetched HTTP elements
  HttpEntity entity;
  HttpResponse response;

  def Http(){ this(null)}

  /**
   * Accepted params
   *   requestInterceptor - HttpRequestInterceptor
   */
  def Http(Map params){
    httpClient = params?.'httpClient' ?: new DefaultHttpClient()
    httpClient.addRequestInterceptor(params?.'requestInterceptor' ?: {HttpRequest request, HttpContext context ->
      request.setHeader('User-Agent', params?.'User-Agent' ?: System.getProperty('http.user-agent') ?: USER_AGENT)
      if (params?.containsKey('Accept') || System.getProperty('http.accept'))
        request.setHeader('Accept', params?.'Accept' ?: System.getProperty('http.accept') ?: System.getProperty('http.accept') ?: "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
      if (params?.containsKey('Accept-Language') || System.getProperty('http.accept-language'))
        request.setHeader('Accept-Language', params?.'Accept-Language' ?: System.getProperty('http.accept-language') ?: "en-us,en;q=0.5")
      if (params?.containsKey('Accept-Encoding') || System.getProperty('http.accept-encoding'))
        request.setHeader('Accept-Encoding', params?.'Accept-Encoding' ?: System.getProperty('http.accept-encoding') ?: "ISO-8859-1,utf-8;q=0.7,*;q=0.7")

      params?.get('headers')?.each {k, v -> request.setHeader(k, v)}
    } as HttpRequestInterceptor)
    if (params?.containsKey('buffer')) this.enableBuffer = params.'buffer'
    if (params?.'responseInterceptor' && params.'responseInterceptor' instanceof HttpResponseInterceptor){
      httpClient.addResponseInterceptor(params.'responseInterceptor')
    }
    /* httpClient.addResponseInterceptor(params?.'requestInterceptor' ?: {HttpResponse response, HttpContext context ->
    if (logger.isTraceEnabled()) logger.trace("\t[response] - header.'Set-Cookie': ${response.getHeaders('Set-Cookie')}")
  } as HttpResponseInterceptor)*/

  }

  def post(requestUri, InputStream stream, Closure closure = null){
    reset()
    uri = requestUri instanceof URI ? requestUri : new URI(requestUri)
    request = new HttpPost(uri)
    def result = this;
    request.setEntity(new InputStreamEntity(stream, -1))//TODO see if it is necessary to set contentType and chunked
    if (referer) request.setHeader('Referer', referer)

    response = httpClient.execute(request)
    if (log.isDebugEnabled()) log.debug("post() - uri: $uri, cookies.size(): ${httpClient.cookieStore.cookies?.size()}, stream.class: ${stream.getClass()}")
    entity = enableBuffer ? new BufferedHttpEntity(response.getEntity()) : response.getEntity()
    this.reset = false;
    if (closure){ result = callClosure(closure, [request, entity, this]) }
    //entity?.consumeContent()

    return result;
  }

  def post(requestUri, params = null, Closure closure = null){
    reset()
    uri = requestUri instanceof URI ? requestUri : new URI(requestUri)
    request = new HttpPost(uri)
    def result = this;
    def nameValues = parseNameValues(params)
    request.setEntity(new UrlEncodedFormEntity(nameValues, HTTP.UTF_8))
    if (referer) request.setHeader('Referer', referer)


    response = httpClient.execute(request)
    if (log.isDebugEnabled()) log.debug("post() - uri: $uri, cookies.size(): ${httpClient.cookieStore.cookies?.size()}, nameValues: $nameValues")
    entity = enableBuffer ? new BufferedHttpEntity(response.getEntity()) : response.getEntity()
    this.reset = false;
    if (closure){ result = callClosure(closure, [request, entity, this]) }
    //entity?.consumeContent()

    return result;
  }

  def get(requestUri, Closure closure = null){
    reset()
    uri = requestUri instanceof URI ? requestUri : new URI(requestUri)
    def result = this;
    request = new HttpGet(uri)
    if (referer) request.setHeader('Referer', uri.toString())
    if (log.isDebugEnabled()) log.debug("get() - uri: $uri, cookies.size(): ${httpClient.cookieStore.cookies?.size()}")
    response = httpClient.execute(request)
    entity = enableBuffer ? new BufferedHttpEntity(response.getEntity()) : response.getEntity()
    this.reset = false;
    if (closure){
      result = callClosure(closure, [request, entity, this])
    }
    //entity?.consumeContent()
    return result;
  }


  static List<NameValuePair> parseNameValues(input){
    def result = []
    if (input instanceof NameValuePair){
      result << input;
    } else if (input instanceof FormFields){
      //println "parseNameValues() - FormFields - input: ${input.getClass()}, formControls.size(): ${input.formControls.size()}"
      input.formControls.each { result += parseNameValues(it)}
    } else if (input instanceof FormControl){
      //println "parseNameValues() - FormControl - input: ${input.getClass()}, input: ${input}, input.values: ${input.values}, getAttributesMap() : ${input.getPredefinedValues()  }"
      input.values.each { result << new BasicNameValuePair(input.name, it) }
    } else if (input instanceof Collection){
      input.each {result += parseNameValues(it)}
    } else if (input instanceof Map){
      input.each {k, v -> result << new BasicNameValuePair(k, v)}
    }
    return result;
  }

  /**
   * getContentLength only return the contentLength of HttpEntity. if contentLength is -1, size() will retrieve
   * the entity content inputstream to check the size.
   *
   * <b>Warning</b> this method may retrieve from the Http stream
   */
  def size(){
    def contentLength = entity?.getContentLength();
    if (contentLength > -1){
      return contentLength;
    } else{
      buffer = new ByteArrayOutputStream()
      buffer << entity?.content
      return buffer.size()
    }
  }

  def getText(){
    if (entity) return new StringWriter().with {out -> out << entity.content; out}.toString()
  }

  def getSource(){
    if (!source){
      source = new Source(entity.content)
      entity.consumeContent()
    }
    return source;
  }

  /**
   * if params = null, return the root 'html' element
   */
  def getElement(params = null){
    if (params == null){
      return getSource();
    } else if (params instanceof Collection){
      throw new IllegalArgumentException("getElement() doesn't accept a Collection as argument")
    } else if (params instanceof Map){
      if (params.size() == 1){
        def entries = params.entrySet().toArray(), key = entries[0].key, value = entries[0].value
        return getSource().getAllElements(key, value, false)?.getAt(0)
      } else throw new UnsupportedOperationException("multiple value map is not implemented")
    } else if (params instanceof Closure){
      return callClosure(params, [getSource(), this])
    } else{
      //assume the parameter is an ID
      return getSource()?.getElementById(params)
    }
    def elements = getElements(params)
    if (elements instanceof Collection){ return elements.getAt(0)}
  }

  def getElement(String tag, Map attrs){
    if (attrs?.size() != 1) throw new UnsupportedOperationException("attrs must contain exactly 1 entry")
    def entry = attrs.entrySet().toList()?.getAt(0)
    return getElements(tag).findAll {it.getAttributeValue(entry.key) == entry.value}?.getAt(0)
  }

  def getElements(String tag, Map attrs){
    if (attrs?.size() != 1) throw new UnsupportedOperationException("attrs must contain exactly 1 entry")
    def entry = attrs.entrySet().toList()?.getAt(0)
    return getElements(tag).findAll {it.getAttributeValue(entry.key) == entry.value}
  }

  /**
   * For map and collection, they return the accumulated elements
   */
  def getElements(params = null){
    if (params == null){
      return getSource().getAllElements()
    } else if (params instanceof Integer){
      throw new IllegalArgumentException("invalid argument, params: $params, params.class: ${params?.getClass()}")
    } else if (params instanceof String){
      return getSource().getAllElements(params)
    } else if (params instanceof Map){ // multi-entry map narrow down the scope of result
      def elements = getSource(), result = []
      params.entrySet().each {entry -> result += elements.getAllElements(entry.key, entry.value, false)}
      return result;
    } else if (params instanceof Collection){
      def result = []
      params.each {result += getElements(it)}
      return result;
    } else if (params instanceof Closure){
      return callClosure(params, [getSource(), this])
    } else{
      throw new IllegalArgumentException("getForm() - unknown params - params: $params")
    }
  }

  def getForm(params){
    //TODO consider to delegate this method to Form
    //TODO refactor this to a generic get element
    if (params == null){
      return new Form(this, getSource().getAllElements(HTMLElementName.FORM)?.getAt(0))
    } else if (params instanceof Integer){
      return new Form(this, getSource().getAllElements(HTMLElementName.FORM)?.getAt(params))
    } else if (params instanceof String){
      def source = getSource()
      def formElement = source.getElementById(params) ?: source.getAllElement('name', params, false)?.getAt(0)
      return new Form(this, formElement)
    } else if (params instanceof Map){
      return new Form(this, getElement(params))
    } else{
      throw new IllegalArgumentException("getForm() - unknown params - params: $params")
    }
  }


  def reset(){
    if (reset) return; //skip if reset already
    this.entity?.consumeContent()
    this.entity = null;
    this.referer = uri?.toString();
    this.uri = null;
    this.request = null;
    this.response = null;
    this.source = null;
    this.reset = true
    return this;
  }

  /**
   * getSource() will also trigger close
   */
  def close(){ reset(); return this; }

  def shutdown(){reset(); httpClient.connectionManager.shutdown(); return this}



  static callClosure(Closure closure, List arguments){
    def result;
    switch (closure.parameterTypes.length){
      case 0: result = closure(arguments?.getAt(0)); break;
      case 1: result = closure(arguments?.getAt(0)); break;
      case 2: result = closure(arguments?.getAt(0), arguments?.getAt(1)); break;
      default: result = closure(arguments?.getAt(0), arguments?.getAt(1), arguments?.getAt(2)); break;
    }
    return result;
  }
}