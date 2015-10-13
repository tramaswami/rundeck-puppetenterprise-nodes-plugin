FROM ubuntu:15.04

MAINTAINER Roberto Paez

RUN apt-get -yqq update && \
  apt-get -yqq upgrade

RUN apt-get -yqq install vim ssh curl tmux

RUN mkdir workspace/
WORKDIR workspace/

COPY ssl/ ssl/
COPY scripts/* workspace/

RUN chmod +x *.sh

CMD bash
