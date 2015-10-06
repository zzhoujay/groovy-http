# System Properties for HTTP Protocol #
If params is provided in the Http constructor, it will override any system properties.

For User-Agent, it overrides the system's default value. For the other system properties, if specified, the HTTP Header will be added using the specified value, otherwise, the HTTP header will not be added.
  * http.user-agent
  * http.accept
  * http.accept-language
  * http.accept-encoding