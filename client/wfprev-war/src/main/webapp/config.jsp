<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="java.util.Properties" %>

<%
  ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(application);
  Properties properties = (Properties) context.getBean("applicationProperties");

  if (properties != null) {
    String baseUrl = properties.getProperty("base.url", "");

    String json = "{";

    // General Application Section
    json = json.concat("\"application\":{");
    json = json.concat("\"acronym\":\"").concat(properties.getProperty("project.acronym", "")).concat("\"").concat(",");
    json = json.concat("\"version\":\"").concat(properties.getProperty("application.version", "")).concat("\"").concat(",");
    json = json.concat("\"buildNumber\":\"").concat(properties.getProperty("build.number", "")).concat("\"").concat(",");
    json = json.concat("\"environment\":\"").concat(properties.getProperty("default.application.environment", "")).concat("\"").concat(",");
    json = json.concat("\"baseUrl\":\"").concat(baseUrl).concat("\"");
    json = json.concat("},");

    // REST API Section (assuming you still want to keep the REST API properties)
    json = json.concat("\"rest\":{");
    json = json.concat("},");

    // WebADE OAuth Section (if still needed)
    json = json.concat("\"webade\":{");
    json = json.concat("\"oauth2Url\":\"").concat(properties.getProperty("webade-oauth2.authorize.url", "")).concat("\"").concat(",");
    json = json.concat("\"clientId\":\"").concat("WFPREV_UI").concat("\"");
    json = json.concat("}");

    json = json.concat("}");

    out.write(json);
  } else {
    out.write("{}");
  }
%>
