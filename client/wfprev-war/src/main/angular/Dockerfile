FROM tomcat:9.0.91-jdk17-openjdk

COPY /wfnews-war/target/wfnews-war-1.0.0.war .

ENV TOMCAT_HOME=/usr/local/tomcat \
  CATALINA_HOME=/usr/local/tomcat \
  CATALINA_OUT=/usr/local/tomcat/logs \
  TOMCAT_MAJOR=9 \
  JAVA_OPTS="$JAVA_OPTS" 

# Install gzip along with sed
RUN apt-get update &&\
  apt-get install -y sed gzip &&\
  rm -rf /usr/local/tomcat/webapps/ROOT  &&\
  mkdir /usr/local/tomcat/webapps/ROOT &&\
  unzip -d /usr/local/tomcat/webapps/ROOT/ '*.war' &&\
  adduser --system tomcat &&\
  chown -R tomcat:0 `readlink -f ${CATALINA_HOME}` &&\
  chmod -R 770 `readlink -f ${CATALINA_HOME}` &&\
  chown -h tomcat:0 ${CATALINA_HOME}

VOLUME /usr/local/tomcat/logs /usr/local/tomcat/work

RUN chmod 766 /usr/local/tomcat/logs && chmod 766 /usr/local/tomcat/work
# run as tomcat user
USER tomcat

EXPOSE 8080
CMD ["catalina.sh", "run"]
