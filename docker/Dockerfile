FROM 192.168.84.23:5000/library/anyrobot-graph-baseimage:latest

MAINTAINER anyrobot-graph Docker Maintainers "bao.zengkai@eisoo.com"

# 拷贝版本文件
COPY version.json /anyrobot/
COPY version /bin/

# 拷贝 graph_server
COPY graph_server /anyrobot/graph/graph_server

#拷贝 gradle-repo
COPY gradle-repo /anyrbot/graph/gradle-repo

# 设置工作目录
WORKDIR /anyrobot/graph/graph_server