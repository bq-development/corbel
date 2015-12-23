FROM java:8

MAINTAINER Corbel Team @ bq <corbel-dev@bq.com>

RUN mkdir -p /opt/corbel/resources/plugins

ADD ${project.name}-${project.parent.version}.jar /resources/plugins/${project.name}/${project.name}.jar

VOLUME /resources/plugins/${project.name}