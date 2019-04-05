FROM 		hseeberger/scala-sbt:8u181_2.12.6_1.2.1 as builder
WORKDIR		/scala
COPY		build.sbt /scala/
RUN 		mkdir /scala/project
COPY		project/plugins.sbt /scala/project
# download all dependencies
RUN         sbt update
# copy over all project files and build a standalone bundle
COPY		src /scala/src
RUN			sbt universal:stage

FROM        java:8-jre-alpine
RUN         apk add --update bash
RUN         mkdir /app
WORKDIR		/app
COPY 		--from=builder /scala/target/universal/stage .
ENTRYPOINT  /app/bin/omg-scala-template
