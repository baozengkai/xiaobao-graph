package graph_server

class Graph {
    String graph_name
    String event_name
    String value
    static constraints = {
    }
    static mapping = {
        value sqlType:"longtext"
    }
}
