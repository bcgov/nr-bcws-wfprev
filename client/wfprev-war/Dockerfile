# Use an official Tomcat image with JDK 17
FROM tomcat:9.0.91-jdk17

# Install at very top layer to ease caching issues for devs
RUN apt-get -y update && \
    apt-get install -y unzip

# Define build arguments
ARG CONTAINER_NAME=wfprev-war
ARG WAR_FILE=*.war

# Set environment variables
ENV CATALINA_HOME /usr/local/tomcat

# Add the application's war to the container and unzip it
ADD ${WAR_FILE} .
RUN rm -rf /usr/local/tomcat/webapps/ROOT && \
    TEMPNAME=$(echo ${WAR_FILE} | sed 's/\(.*\)\(-api\|-war\).*/\1/') && \
    unzip ${WAR_FILE} -d /usr/local/tomcat/webapps/pub#$TEMPNAME && \
    addgroup --system tomcat && \
    adduser --system --ingroup tomcat tomcat && \
    chown -R tomcat:tomcat ${CATALINA_HOME} && \
    chmod -R 770 ${CATALINA_HOME} && \
    mkdir -p /usr/local/tomcat/temp && \
    mkdir -p /usr/local/tomcat/logs/wfhr && \
    chown -R tomcat:tomcat /usr/local/tomcat/logs/wfhr && \
    chmod 755 /usr/local/tomcat/logs && \
    chmod 755 /usr/local/tomcat/logs/wfhr && \
    chmod 755 /usr/local/tomcat/work && \
    chmod 755 /usr/local/tomcat/temp && \
    echo org.apache.tomcat.util.digester.PROPERTY_SOURCE=org.apache.tomcat.util.digester.EnvironmentPropertySource >> /usr/local/tomcat/conf/catalina.properties

# Expose the ports
EXPOSE 8080

# Run the Tomcat server
USER tomcat
CMD ["catalina.sh", "run"]
