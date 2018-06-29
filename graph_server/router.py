#!/usr/bin/env python
# -*- coding:utf-8 -*-

from handlers.graph.vertex_filter_handler import VertexFilterHandler
from handlers.graph.vertex_neighbors_handler import VertexNeighborsHandler
from handlers.graph.vertex_search_property_handler import VertexPropertyHandler

from handlers.graph.edge_filter_handler import EdgeFilterHandler
from handlers.graph.edge_group_count_by_property_handler import EdgeGroupCountByPropertyHandler
from handlers.graph.edge_group_count_by_label_handler import EdgeGroupCountByLabelHandler

from handlers.graph_db.graph_save_handler import GraphSaveHandler
from handlers.graph_db.graph_load_handler import GraphLoadHandler
from handlers.graph_db.graph_delete_handler import GraphDeleteHandler
from handlers.graph_db.graph_exists_handler import GraphExistsHandler
from handlers.graph_db.graph_list_handler import GraphListHandler
from handlers.graph_db.graph_replace_handler import GraphReplaceHandler



ROUTER = [
    (r'/v1/graph/vertex/filter', VertexFilterHandler),
    (r'/v1/graph/vertex/neighbors', VertexNeighborsHandler),
    (r'/v1/graph/vertex/property/value/search', VertexPropertyHandler),
    (r'/v1/graph/edge/filter', EdgeFilterHandler),
    (r'/v1/graph/edge/groupCountByProperty', EdgeGroupCountByPropertyHandler),
    (r'/v1/graph/edge/groupCountByLabel', EdgeGroupCountByLabelHandler),
    (r'/v1/graph/save', GraphSaveHandler),
    (r'/v1/graph/replace', GraphReplaceHandler),
    (r'/v1/graph/load', GraphLoadHandler),
    (r'/v1/graph/delete', GraphDeleteHandler),
    (r'/v1/graph/exists', GraphExistsHandler),
    (r'/v1/graph/list',GraphListHandler)
]
