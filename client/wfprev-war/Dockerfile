# Use an official Tomcat image with JDK 8
FROM tomcat:9.0.91-jdk17
#Install at very top layer to ease caching issues for devs
RUN apt-get -y update &&\
    apt-get install -y unzip
ARG CONTAINER_NAME=wfprev-war
ENV CATALINA_HOME /usr/local/tomcat
# Define build arguments
ARG WAR_FILE=*.war
ARG CONTAINER_NAME
# Add the application's war to the container and rename it to pub#<warname>.war
ADD ${WAR_FILE} .
# Copy the context.xml.template template and other configuration files
# COPY docker-files/server.xml $CATALINA_HOME/conf/
# Create the tomcat user and group, and set permissions
# TEMPNAME step is to remove "-war" from the path
RUN rm -rf /usr/local/tomcat/webapps/ROOT  &&\
    for i in $(ls *.war) ; \
        do \
            export TEMPNAME="$(echo $i | sed 's/(.*)((-api)|(-war))-.*/\1\3/' -r)" &&\ 
            unzip $i -d /usr/local/tomcat/webapps/pub#$TEMPNAME ; \
        done &&\
    addgroup --system tomcat &&\
    adduser --system --ingroup tomcat tomcat &&\
    chown -R tomcat:tomcat `readlink -f ${CATALINA_HOME}` &&\
    chmod -R 770 `readlink -f ${CATALINA_HOME}` &&\
    chown -h tomcat:tomcat ${CATALINA_HOME} &&\
    mkdir -p /usr/local/tomcat/temp &&\
    mkdir -p /usr/local/tomcat/logs/wfhr &&\
    mkdir -p /${CATALINA_HOME}/webapps/pub#${CONTAINER_NAME} &&\
    chown -R tomcat:tomcat /usr/local/tomcat/logs/wfhr &&\
    chmod 766 /usr/local/tomcat/logs &&\
    chmod 766 /usr/local/tomcat/logs/wfhr &&\
    chmod 766 /usr/local/tomcat/work &&\
    chmod 766 /usr/local/tomcat/temp &&\
    chmod 766 /${CATALINA_HOME}/webapps/pub#${CONTAINER_NAME} &&\
    echo org.apache.tomcat.util.digester.PROPERTY_SOURCE=org.apache.tomcat.util.digester.EnvironmentPropertySource >> /usr/local/tomcat/conf/catalina.properties
VOLUME /usr/local/tomcat/logs /usr/local/tomcat/work /usr/local/tomcat/temp
# Expose the ports
EXPOSE 8080 8000
USER tomcat
# Run the Tomcat server with JPDA settings for remote debugging
CMD ["catalina.sh", "run"]