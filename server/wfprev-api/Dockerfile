# Use an official Tomcat image as a base image
FROM tomcat:10.1-jdk17-corretto

# Install unzip at the top layer to ease caching issues for devs
RUN yum update -y && \
    yum install -y unzip && \
    yum clean all

# Define build arguments
ARG WAR_FILE=target/wfprev-api-1.0.0-SNAPSHOT.war
ARG CONTAINER_NAME=wfprev-api

# Set environment variables
ENV CATALINA_HOME=/usr/local/tomcat \
    CATALINA_OUT=/usr/local/tomcat/logs \
    TOMCAT_MAJOR=10 \
    JAVA_OPTS="$JAVA_OPTS -Xss200k"

# Add WAR file
ADD ${WAR_FILE} .

# Set up directories and permissions
RUN rm -rf /usr/local/tomcat/webapps/ROOT && \
    for i in $(ls *.war); do \
        export TEMPNAME="$(echo $i | sed -E 's/(.*)((-api)|(-war))-.*/\1\3/')" && \
        unzip $i -d /usr/local/tomcat/webapps/pub#$TEMPNAME; \
    done && \
    chmod -R 770 ${CATALINA_HOME} && \
    mkdir -p /usr/local/tomcat/temp && \
    mkdir -p ${CATALINA_HOME}/webapps/pub#${CONTAINER_NAME} && \
    chmod 766 /usr/local/tomcat/logs && \
    chmod 766 /usr/local/tomcat/work && \
    chmod 766 /usr/local/tomcat/temp && \
    chmod 766 ${CATALINA_HOME}/webapps/pub#${CONTAINER_NAME} && \
    echo org.apache.tomcat.util.digester.PROPERTY_SOURCE=org.apache.tomcat.util.digester.EnvironmentPropertySource >> /usr/local/tomcat/conf/catalina.properties

# Define volumes for logs, work, and temp directories
VOLUME /usr/local/tomcat/logs /usr/local/tomcat/work /usr/local/tomcat/temp

# Expose the Tomcat port (default is 8080)
EXPOSE 8080

# Start Tomcat when the container
CMD ["catalina.sh", "run"]