# Dependency #
This project requires:
  * Apache HTTP Client
    * [Core](http://hc.apache.org/httpcomponents-core) | [JavaDoc](http://hc.apache.org/httpcomponents-core/httpcore/apidocs/index.html)
    * [Client](http://hc.apache.org/httpcomponents-client/index.html) | [JavaDoc](http://hc.apache.org/httpcomponents-client/httpclient/apidocs/index.html)
    * Apache Commmons Logging
  * [Jericho HTML Parser](http://jerichohtml.sourceforge.net) | [JavaDoc (2.6)](http://jerichohtml.sourceforge.net/doc/api/index.html), only if you use it

Remarks: You are highly recommended to get familiar with the Apache HTTP Client and Core API to get best result.

# Concepts #
  * Http
    * mainly provides post() and get() methods, that takes a URI and optional a closure for execution
    * lifecycle methods: reset(), close(), shutdown()
    * Jericho HTML methods: getSource(), getElement(), and @Delegate HttpEntity
    * Form method: getForm()
    * Getter: httpclient, entity, uri, request, source, referer (notice that only httpclient is always available, the other are available only after relevant first usage)
    * Others: size() - first check contentLength, if is -1, check the actual length of inputStream
  * Form
    * provides submit() method, that utilize Http.post() to do submission
    * Jericho HTML methods: @Delegate of the form element

# Usage #
  * put the main jar and any depending jars in your classpath. The depending jars could be found at [/lib](http://code.google.com/p/groovy-http/source/browse/#svn/trunk/lib).
  * Get a page
    * Sample code:
```
println new Http().get("http://code.google.com/p/groovy-http").text
println new Http().get("http://code.google.com/p/groovy-http").getElement('pname').textExtractor.toString() //print: "groovy-http"
println new Http().get("http://code.google.com/p/groovy-http"){ request, entity, client-> // optionally use 0 to 3 argument(s)
  println entity.contentType
} //print: text/html; charset=UTF-8
println new Http().get("http://code.google.com/p/groovy-http").source //return a Jericho HTML parser Source
```
    * text is getText that print out the input stream with StringWriter
    * Http constructor accepts only one parameter 'buffer' (boolean)
      * by default, HttpEntity is wrapped with a BufferedHttpEntity. `new Http(buffer:false)` disable the buffer. You need to ensure you read the input stream only once if buffer is disabled.
  * get a Form
    * Sample code:
```
def form = new Http().get("http://code.google.com/p/groovy-http/").getForm()

//with constructor
def http = new Http().get("http://code.google.com/p/groovy-http/")
def form = new Form(http, http.getElement("action":"/hosting/search")) //there are many other ways to get the form element
```
    * Http.getForm(params) - if params is null, it get the first form. if params is an Integer, it get the form by index; if it is a string, it get by HTML Element ID
  * Post (without a form)
    * Sample
```
println new Http().post("http://search.yahoo.com/web",[[fr:'sfp'],[p:'groovy']]).text //proper usage, HTTP form support multiple values
println new Http().post("http://search.yahoo.com/web",[fr:'sfp',p:'groovy']).text
```
  * Post (a form)
    * Sample
```
println new Http().get("http://search.yahoo.com").getForm().submit(p: 'Groovy HTTP').text
```
  * Complex example: fetch a page, submit a form, and request another page in the same session ... and do it fluently in a single line
    * sample
```
new Http().get("http://localhost:8080/login").getForm('loginForm').submit(username:'foo',password:'bar').get("http://localhost/securePage").text
```
  * Run the test cases to get to know how they work

# Advanced Concepts #
  * Cookie
    * cookie is enabled by default
  * HTTP Header
    * by default, it simulates FireFox 3 on Windows Vista. It is a more sensible default than most HTTP Client.
    * there are two ways to configure the http header, both requires usage of Apache HTTP Client API
```
new Http(requestInceptor:{HttpRequest request, HttpContext context -> 
  request.setHeader('User-Agent', "bot")
} as HttpRequestInterceptor)
    //for User-Agent, Accept, Accept-Language, and Accept-Encoding
 new Http('User-Agent':'crawler')
```
  * Return value of methods
    * by default, most methods of Http returns 'this' (the Http instance). When a closure is supplied, the return value is determined by the closure. Users could use the third argument which is the Http instance and return it.
    * all getXXX method are for getting something other than the Http instance.
  * Get content as stream
    * Use HttpEntity, e.g.
```
println(new Http().get("http://code.google.com/p/groovy-http").entity.content instanceof InputStream) //println: true
```
# Reference #
  * Please also check the [Groovy HTTP Builder](http://groovy.codehaus.org/modules/http-builder/) project which is more mature and provides similar functionality