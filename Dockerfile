FROM corbot/corbel-maven-build

ADD . /usr/src/app

WORKDIR /usr/src/app

RUN mvn dependency:resolve