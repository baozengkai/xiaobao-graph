package graph_server

class UrlMappings {

    static mappings = {
        "/graph/index" (controller:'graph',action:'index', method:'GET')
        "/v1/graph/vertex/filter"(controller: 'graph', action: 'vertex_filter', method: 'POST')
        "/v1/graph/vertex/neighbors"(controller: 'graph', action: 'vertex_neighbors', method: 'POST')
        "/v1/graph/vertex/property/value/search"(controller: 'graph', action: 'vertex_seach', method: 'POST')
        "/v1/graph/edge/filter"(controller: 'graph', action: 'edge_filter', method: 'POST')
        "/v1/graph/edge/groupCountByLabel"(controller: 'graph', action: 'edge_group_by_label', method: 'POST')
        "/v1/graph/edge/groupCountByProperty"(controller: 'graph', action: 'edge_group_by_property', method: 'POST')

        "/v1/graph/save"(controller: "graphSave", action:"save", method:"POST")
        "/v1/graph/load"(controller: "graphSave", action:"load", method:"POST")
        "/v1/graph/list"(controller: "graphSave", action:"list", method:"GET")
        "/v1/graph/replace"(controller: "graphSave", action:"replace", method:"POST")
        "/v1/graph/delete"(controller: "graphSave", action:"delete", method:"POST")
        "/v1/graph/exists"(controller: "graphSave", action:"exists", method:"POST")

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
