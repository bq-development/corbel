FROM      java:8
MAINTAINER Corbel Team @ bq <corbel-dev@bq.com>

EXPOSE ${port}
EXPOSE ${port.admin}

ADD run.sh /
ADD ${project.name}-${project.version}-bin.tar.gz /

ENTRYPOINT ["/bin/sh", "/run.sh"]

CMD ["server"]