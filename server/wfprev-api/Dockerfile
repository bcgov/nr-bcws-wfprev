# Use an official Tomcat image as a base image
FROM tomcat:10.1-jdk21

# Remove the default webapps to avoid conflicts
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file to the Tomcat webapps directory with the new name
COPY *.war /usr/local/tomcat/webapps/pub#wfprev-api.war

ENV TOMCAT_HOME=/usr/local/tomcat \
    CATALINA_HOME=/usr/local/tomcat \
    CATALINA_OUT=/usr/local/tomcat/logs \
    TOMCAT_MAJOR=10 \
    JAVA_OPTS="$JAVA_OPTS -Xss200k"

# Set permissions for logs, work, and temp directories
RUN chmod 766 /usr/local/tomcat/logs && chmod 766 /usr/local/tomcat/work && chmod 766 /usr/local/tomcat/temp

# Define volumes for logs, work, temp, and webapps directories
VOLUME /usr/local/tomcat/logs /usr/local/tomcat/work /usr/local/tomcat/temp /usr/local/tomcat/webapps

# Expose the Tomcat port (default is 8080)
EXPOSE 8080

# Start Tomcat when the container starts
CMD ["catalina.sh", "run"]