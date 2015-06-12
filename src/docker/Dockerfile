FROM      java:8
MAINTAINER Corbel Team @ bq <corbel-dev@bq.com>

EXPOSE ${port}
EXPOSE ${port.admin}

ADD ${project.name}-${project.version}-bin.tar.gz /

ENTRYPOINT ["/${project.name}/bin/${project.name}"]

CMD ["server"]