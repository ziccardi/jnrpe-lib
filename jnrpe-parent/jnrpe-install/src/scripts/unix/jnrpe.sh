JNRPE_JAR=`ls "%{INSTALL_PATH}"/bin/jnrpe-server*.jar`
%{JAVA_HOME}/bin/java -jar "$JNRPE_JAR"
