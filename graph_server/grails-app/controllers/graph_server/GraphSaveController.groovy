package graph_server

import grails.converters.JSON
import grails.core.*
import jdk.nashorn.internal.objects.Global
import org.json.JSONObject
import org.springframework.context.MessageSource
import info.GlobalInfo

/**
 * 关系图谱持久化控制器
 * @author bao.zengkai
 */

class GraphSaveController {

    def graphSaveService

    // save方法处理保存关系图谱的
    def save(){
        try{
            graphSaveService.save_record(request.JSON)
            def success_info =["status":"OK","detail":"Save Success!"]
            render success_info as JSON
        }
        //如果捕捉到了无参异常 则显示错误
        catch(NoSuchFieldException e)
        {
            Locale locale = graphSaveService.getLocale()
            def no_such_field_err_info = graphSaveService.getMessage(locale)
            render no_such_field_err_info as JSON
        }
        catch(Exception e)
        {
            def err_info =["status":"FAILED","description":"Save Failed!","detail":e.getCause()]
            render err_info as JSON
        }
    }

    // replace方法更新保存的关系图谱
    def replace(){
        try{
            graphSaveService.update_reocrd(request.JSON)
            def success_info = ["status":"OK","detail":"Replace Success!"]
            render success_info as JSON
        }
        catch(NoSuchFieldException e)
        {
            Locale locale = graphSaveService.getLocale()
            def no_such_field_err_info = graphSaveService.getMessage(locale)
            render no_such_field_err_info as JSON
        }
        catch(Exception e)
        {
            def err_info = ["status":"FAILED","detail":"Replace Failed!"]
            render err_info as JSON
        }
    }

    // get方法获取关系图谱符合某个关系图谱名称记录
    def load(){
        //先调用is_exists_record方法判断，该关系图谱是否存在
        try {
            if (graphSaveService.is_exists_record(request.JSON)) {
                def success_info = graphSaveService.load_record(request.JSON)
                render success_info as JSON
            } else {
                def err_info = ["status": "FAILED", "detail": "Load Failed.${request.JSON.graphName}  does not esists!"]
                render err_info as JSON
            }
        }catch(NoSuchFieldException e)
        {
            Locale locale = graphSaveService.getLocale()
            def no_such_field_err_info = graphSaveService.getMessage(locale)
            render no_such_field_err_info as JSON
        }
    }

    // 判断该关系图谱是否存在
    def exists(){
        // 如果图谱存在
        try{
            if(graphSaveService.is_exists_record(request.JSON)){
                def success_info=["status": "OK", "detail": "${request.JSON.graphName} exists" ]
                render success_info as JSON
            }
            else{
                def err_info=["status": "FAILED", "detail": "${request.JSON.graphName} not exists" ]
                render err_info as JSON
            }
        }catch(NoSuchFieldException e)
        {
            Locale locale = graphSaveService.getLocale()
            def no_such_field_err_info = graphSaveService.getMessage(locale)
            render no_such_field_err_info as JSON
        }
    }

    // 显示所有关系图谱名称
    def list(){
        def graph_names = ["graphName":graphSaveService.list_record()]
        render graph_names
    }

    //删除所有关系图谱记录
    def delete(){
        try {
            if (graphSaveService.is_exists_record(request.JSON)) {
                graphSaveService.delete_record(request.JSON)
                def success_info = ["status": "OK", "detail": "Delete ${request.JSON.graphName} record success!"]
                render success_info as JSON
            } else {
                def err_info = ["status": "FAILED", "detail": "Delete Failed.The %s record does not esists." % (req["graphName"])]
                render err_info as JSON
            }
        }catch(NoSuchFieldException e)
        {
            Locale locale = graphSaveService.getLocale()
            def no_such_field_err_info = graphSaveService.getMessage(locale)
            render no_such_field_err_info as JSON
        }
    }
}
