#!/usr/bin/env python
# encoding=utf-8
import yaml

CONFIG = {}


def load_config():
    """
    """
    c_file = "/anyrobot/graph/graph_server/config.yml"

    with open(c_file) as f:
        global CONFIG
        CONFIG = yaml.load(f.read())

load_config()

# GRAPH服务
GRAPH_SERVER = "http://%s:%s" % (CONFIG['graph_server']['host'],
                                            CONFIG['graph_server']['port'])
#ELASTIC服务器
ELASTIC_SERVER = "http://%s:%s" % (CONFIG['elastic_server']['host'],
                                            CONFIG['elastic_server']['port'])
# SEARCH模块
SEARCH_PARSE = "http://%s:%s/v1/search/parse" % (CONFIG['search_server']['host'],
                                            CONFIG['search_server']['port'])

# MYSQL的IP
MYSQL_IP = CONFIG['mysql_server']['host']

# MYSQL的PORT
MYSQL_PORT = CONFIG['mysql_server']['port']