package graph_server

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import info.GlobalInfo
import org.grails.web.json.JSONObject
import org.springframework.context.MessageSource


@Transactional
// 关系图谱保存服务类，对应于python中Module模块内容
class GraphSaveService {

    MessageSource messageSource

    GrailsApplication grailsApplication

    // 保存某个关系图谱
    def save_record(Object request){
        // 如果参数没有给全，那么抛出自定义异常
        if(!request.graphName || !request.eventName || !request.value)
            throw new NoSuchFieldException()
        def graph=new Graph(graph_name:request.graphName,event_name:request.eventName,value:request.value)
        graph.save(flush:true,failOnError: true)
        def test
    }


    // 根据language.conf创建locale对象
    def getLocale()
    {
        def locale
        def file = new File("C:\\Users\\Baozengkai\\Desktop\\language.conf")
        if(file.text.contains("zh_CN"))
        {
            locale = new Locale("zh","CN")
        }else if(file.text.contains("en_US"))
        {
            locale = new Locale("es")
        }
        return locale
    }

    //根据locale对象选择指定的语言资源
    def getMessage(Locale locale)
    {
        def status = messageSource.getMessage("status",[] as Object[], locale)
        def status_value =  messageSource.getMessage("status.value",[] as Object[], locale)
        def description = messageSource.getMessage("description",[] as Object[], locale)
        def description_value = messageSource.getMessage("description.value",[] as Object[], locale)

        def no_such_field_err_info = ["${status}":status_value, "${description}":description_value]
        return no_such_field_err_info
    }

    // 判断记录是否存在
    def is_exists_record(Object request){
        if(!request.graphName)
            throw new NoSuchFieldException()
        def query_results = Graph.executeQuery("select distinct a.event_name from Graph a where a.graph_name=${request.graphName}")
        if(query_results.size()!=0){
            return  true
        }else{
            return false
        }
    }


    //更新某个关系图谱
    def update_reocrd(Object request){
        if(!request.oldGraphName || !request.newGraphName || !request.eventName || !request.value)
            throw new NoSuchFieldException()
        return Graph.executeUpdate("update Graph a SET a.graph_name=${request.newGraphName},a.event_name=${request.eventName}," +
                "a.value=${request.value.toString()} where graph_name=${request.oldGraphName}")
    }

    //获取某个关系图谱
    def load_record(Object request){
        if(!request.graphName)
            throw new NoSuchFieldException()
        def query_results = Graph.executeQuery("select distinct a.event_name,a.value from Graph a where a.graph_name=${request.graphName}")
        def info = ["status":"OK","detail":"Load Success!","eventName":query_results[0][0],"value": JSON.parse(query_results[0][1])]
        return info
    }

    //获取关系图谱名称列表
    def list_record(){
        def query_results = Graph.executeQuery("select a.graph_name from Graph a")
        return query_results
    }

    //删除关系图谱
    def delete_record(Object request){
        if(!request.graphName)
            throw new NoSuchFieldException()
        Graph.executeUpdate("delete from Graph a where a.graph_name=${request.graphName}")
    }
}