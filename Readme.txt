#!/bin/bash

############################################################################################
# docker 镜像
############################################################################################

## 一、服务镜像
# 192.168.84.23:5000/library/anyrobot-graph:{ar_vertion}
# 说明：
#   1. 包含了anyrobot-graph服务，可直接运行
#   2. 或者从宿主机上挂载graph_server的配置文件 graph_server/config.yml 来运行
docker pull 192.168.84.23:5000/library/anyrobot-graph:2.0.12


## 二、运行环境基础镜像
# 192.168.84.23:5000/library/anyrobot-graph-baseimage:latest
# 说明：
#   1. 为anyrobot-graph服务的运行环境基础镜像，包含了anyrobot-graph服务所依赖的运行环境
#   2. 但并不包含anyrobot-graph服务
docker pull 192.168.84.23:5000/library/anyrobot-graph-baseimage:latest


## 三、构建环境基础镜像
# 192.168.84.23:5000/library/anyrobot-graph-baseimage:dev
# 说明：
#   1. 为anyrobot-graph服务的开发/构建环境基础镜像，包含了anyrobot-graph服务开发/构建所依赖的环境
#   2. 可用于 anyrobot-graph 模块的编译、构建、代码检查、单元测试等
docker pull 192.168.84.23:5000/library/anyrobot-graph-baseimage:dev


############################################################################################
# 运行环境搭建 in Linux
############################################################################################

## 方式一、基于服务镜像

    # 方式1. 直接运行
    docker pull 192.168.84.23:5000/library/anyrobot-graph:2.0.12
    docker run -it --name graph -p 11004:11004 192.168.84.23:5000/library/anyrobot-graph:2.0.12 bash

    # 方式2. 从宿主机挂载 graph_server 配置文件
    docker run -it --name graph -p 11004:11004 -v /root/bigdata/anyrobot-graph/graph_server/config.yml:/anyrobot/graph/graph_server/config.yml 192.168.84.23:5000/library/anyrobot-graph:2.0.12 bash

    # 运行服务
    # [in docker]
    python /anyrobot/graph/graph_server/graph_server.py

    # 方式3. 使用 anyrobot-install 中的启动命令
    docker run -tid --restart always --network ar_bridge --name anyrobot-graph --ip anyrobot-graph-ip -p 11004:11004 -v /anyrobot/language.conf:/anyrobot/language.conf -v /anyrobot/devicespec.conf:/anyrobot/devicespec.conf -v /etc/localtime:/etc/localtime:ro -v /anyrobot/graph:/anyrobot/graph -v /anyrobot/logs/graph:/anyrobot/logs/graph 192.168.84.23:5000/library/anyrobot-graph:2.0.12 python /anyrobot/graph/graph_server/graph_server.py

## 方式二、基于运行环境基础镜像

    # 1. 下载 graph_server 服务包
    cd /root/graph
    wget ftp://192.168.84.21/anyrobot-graph/anyrobot-graph-service-2.0.12.tar
    tar xvf anyrobot-graph-service-2.0.12.tar

    # 2. 启动容器
    docker pull 192.168.84.23:5000/library/anyrobot-graph-baseimage:latest
    docker run -it --name graph -p 11004:11004 -v /root/graph/graph_server:/anyrobot/graph/graph_server 192.168.84.23:5000/library/anyrobot-graph-baseimage:latest bash

    # 3. 运行服务
    # [in docker]
    python /anyrobot/graph/graph_server/graph_server.py

## 方式三、基于构建环境基础镜像

    # 1. 启动容器
    docker volume create --name maven-repo
    docker pull 192.168.84.23:5000/library/anyrobot-graph-baseimage:dev
    docker run -it --name graph-dev -v maven-repo:/root/.m2 -p 11004:11004 192.168.84.23:5000/library/anyrobot-graph-baseimage:dev bash

    # 2. 下载 anyrobot-graph 代码
    # [in docker]
    mkdir -p /anyrobot
    cd /anyrobot
    git clone http://192.168.84.20/bigdata/anyrobot-graph.git
    mv anyrobot-graph graph

    # 3. 构建
    ## 【rainbow】: 执行 maven 构建
    # [in docker]
    cd /anyrobot/graph/rainbow
    mvn clean install
    ## 【graph_server】: 构建语言资源文件
    # [in docker]
    python /anyrobot/graph/graph_server/locale/make.py

    # 4. 运行服务
    # [in docker]
    python /anyrobot/graph/graph_server/graph_server.py

    # 5. 执行单元测试
    ## 【rainbow】：？
    ## 【graph_server】
    # [in docker]
    nosetests -v -d --where=/anyrobot/graph/graph_server/tests


############################################################################################
# REST API 接口说明文档
############################################################################################
http://192.168.84.24/graph/graph.html
