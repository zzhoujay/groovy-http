package com.googlecode.groovyhttp.util;

import net.htmlparser.jericho.FormControl;
import net.htmlparser.jericho.FormFields;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.*;

public class HttpPostUtil{
    static Log log = LogFactory.getLog(HttpPostUtil.class);

    public static List<NameValuePair> parseNameValues(Object input){
        if (log.isTraceEnabled())
            log.trace("parseNameValues() - input: " + input + ", input.class: " + input.getClass());

        List<NameValuePair> result = new ArrayList<NameValuePair>();
        if (input instanceof NameValuePair){
            result.add((NameValuePair) input);
        } else if (input instanceof FormFields){
            //println "parseNameValues() - FormFields - input: ${input.getClass()}, formControls.size(): ${input.formControls.size()}"
            FormFields formFields = (FormFields) input;
            for (Object formControl : formFields.getFormControls()){
                result.addAll(parseNameValues(formControl));
            }

        } else if (input instanceof FormControl){
            //println "parseNameValues() - FormControl - input: ${input.getClass()}, input: ${input}, input.values: ${input.values}, getAttributesMap() : ${input.getPredefinedValues()  }"
            FormControl control = (FormControl) input;
            for (String value : control.getValues()){
                result.add(new BasicNameValuePair(control.getName(), value));
            }
        } else if (input instanceof Collection){
            for (Object pair : (Collection) input){
                result.addAll(parseNameValues(input));
            }
        } else if (input instanceof Map){
            Map map = (Map) input;
            for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()){
                result.add(new BasicNameValuePair(String.valueOf(entry.getKey()), String.valueOf(entry.getValue())));
            }
        }
        return result;
    }

}
