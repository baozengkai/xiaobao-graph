#!/usr/bin/env python
# -*- coding:utf-8 -*-
from utils.error.status_pool import GRAPH_STATUS_POOL
from arpylibs.base_error import ARBaseErrorException


class ARGraphException(ARBaseErrorException):
    """
    搜索模块异常类
    """
    def __init__(self, status_info, *args):
        """
        """
        super(ARGraphException, self).__init__(GRAPH_STATUS_POOL.MODULE_CODE,
                                                status_info[0],
                                                status_info[1],
                                                *args)
