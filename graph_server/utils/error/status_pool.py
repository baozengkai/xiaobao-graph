#!/usr/bin/env python
# encoding=utf-8
from arpylibs.statuscode.module_code_def import ModuleCode


class GRAPH_STATUS_POOL(object):
    """
    关系网络错误码
    """
    MODULE_CODE = ModuleCode.MC_AR_GRAPH

    ID_SEARCH_RESULT_INDEX_IS_EMPTY = [1, "ID_SEARCH_RESULT_INDEX_IS_EMPTY"]
    ID_DSL_NOT_CORRECT_PARSE = [2, "ID_DSL_NOT_CORRECT_PARSE"]

MODULE_LIST = [GRAPH_STATUS_POOL]
