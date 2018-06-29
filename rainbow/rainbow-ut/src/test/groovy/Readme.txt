#!/bin/bash

############################################################################################
# 本地UT运行环境搭建
############################################################################################

## 一、手工准备 UT 依赖环境：elasticsearch

docker rm -f rainbow-es
docker pull 192.168.84.23:5000/library/elasticsearch:dcos-5.0.2
#sudo sysctl -w vm.max_map_count=262144
docker run -itd --name rainbow-es --network bridge -p 9201:9200 -e CLUSTER_NAME=pool -e NODE_NAME=es1 -e NODE_MASTER=true -e PUBLISH_HOST=172.17.0.1 -e HTTP_PORT=9200 -e TCP_PORT=9300 -e PING_UNICAST_HOSTS=172.17.0.1:9300 192.168.84.23:5000/library/elasticsearch:dcos-5.0.2
docker logs -f rainbow-es

## 二、手工修改 elasticsearch 访问地址为上述访问地址
# resources/es.properties : es.url


############################################################################################
# jenkins 构建UT运行环境搭建
############################################################################################
# anyrobot-graph/Jenkinsfile 中已配置：
# stage "Prepare rainbow-ut env"
# stage "Clear rainbow-ut env"