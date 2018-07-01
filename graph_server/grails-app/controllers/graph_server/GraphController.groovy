package graph_server

import grails.converters.JSON


/**
 * 关系图谱基础查询控制器
 * @author bao.zengkai
 */


class GraphController {

    GraphService graphService

    // 筛选起始实体接口
    def vertex_filter()
    {
        log.info("Handle For Vertex Filter Request......")
        def g
        def graph_result
        try {
            g = graphService.get_graph(request.JSON)
            graph_result = graphService.get_start_vertex(request.JSON, g)
        }
        catch(Exception e){
            def exception_info = ["code":500 ,"decription":"graph server have encountered a internal error","detail":e]
            render exception_info as JSON
        }
        def vertex_response = ["vertices":graph_result]
        render vertex_response  as JSON
    }

    // 邻居实体接口
    def vertex_neighbors()
    {
        log.info("Handle For Vertex Neighbors Request......")
        def g
        def graph_result
        try {
            g = graphService.get_graph(request.JSON)
            graph_result = graphService.get_vertex_neighbors(request.JSON, g)
        }
        catch(Exception e){
            def exception_info = ["code":500 ,"decription":"graph server have encountered a internal error","detail":e]
            render exception_info as JSON
        }

        def vertex_response = ["vertices":graph_result]
        render vertex_response  as JSON
    }

    // 搜索实体接口
    def vertex_seach()
    {
        log.info("Handle For Vertex Property Search Request......")
        def g
        def graph_result
        try {
            g = graphService.get_graph(request.JSON)
            graph_result = graphService.get_vertex_search(request.JSON, g)
        }
        catch(Exception e){
            def exception_info = ["code":500 ,"decription":"graph server have encountered a internal error","detail":e]
            render exception_info as JSON
        }
        def vertex_response = ["values":graph_result]
        render vertex_response  as JSON
    }

    // 筛选关系接口
    def edge_filter()
    {
        log.info("Handler For Edge Filter Request......")
        def g
        def graph_result
        try {
            g = graphService.get_graph(request.JSON)
            graph_result = graphService.get_edge_filter(request.JSON, g)
        }
        catch(Exception e)
        {
            def exception_info = ["code":500 ,"decription":"graph server have encountered a internal error","detail":e]
            render exception_info as JSON
        }
        def edge_response = ["edges":graph_result]
        render edge_response  as JSON
    }

    // 关系标签分组接口
    def edge_group_by_label()
    {
        log.info("Handle for Edge GroupBy Label Request...... ")
        def g
        def graph_result
        try {
            g = graphService.get_graph(request.JSON)
            graph_result = graphService.get_edge_group_count_by_label(request.JSON, g)
        }
        catch (Exception e)
        {
            def exception_info = ["code":500 ,"decription":"graph server have encountered a internal error","detail":e]
            render exception_info as JSON
        }
        def edge_response = ["buckets":graph_result]
        render edge_response  as JSON
    }

    // 关系属性分组接口
    def edge_group_by_property()
    {
        log.info("Handle for Edge GroupBy Property Request...... ")
        def g
        def graph_result
        try {
            g = graphService.get_graph(request.JSON)
            graph_result = graphService.get_edge_group_count_by_property(request.JSON, g)
        }
        catch (Exception e)
        {
            def exception_info = ["code":500 ,"decription":"graph server have encountered a internal error","detail":e]
            render exception_info as JSON
        }
        def edge_response = ["buckets":graph_result]
        render edge_response  as JSON
    }
}
