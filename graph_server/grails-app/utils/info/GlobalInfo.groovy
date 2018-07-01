package info

class GlobalInfo {

    def GRAPH_SERVER
    def ELASTIC_SERVER
    def SEARCH_SERVER
    GlobalInfo(def grailsConfig)
    {
        GRAPH_SERVER = "http://${grailsConfig.getProperty("graph_server.host")}:${grailsConfig.getProperty("graph_server.port")}"
        ELASTIC_SERVER = "http://${grailsConfig.getProperty("elastic_server.host")}:${grailsConfig.getProperty("elastic_server.port")}"
        SEARCH_SERVER = "http://${grailsConfig.getProperty("search_server.host")}:${grailsConfig.getProperty("search_server.port")}"
    }
}
