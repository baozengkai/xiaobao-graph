import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;


/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public class PrintHelper {
    public static void print_e(final GraphTraversal traversal) {
        System.out.println(">>>>>");
        System.out.println("traversal: " + traversal);
        List<Object> values = traversal.toList();
        System.out.println("size: " + values.size());
        List<JSONObject> jsons = values.stream()
                .map(value -> new JSONObject(value.toString()))
                .collect(Collectors.toList());
        jsons.stream().forEach(json -> {
            System.out.println(json.getJSONObject("__.properties").getString("__.desc.#") + " : " + json.toString());
        });
        System.out.println("<<<<<<");
    }

    public static void print_v(final GraphTraversal traversal) {
        System.out.println(">>>>>");
        System.out.println("traversal: " + traversal);
        List values = traversal.toList();
        System.out.println("size: " + values.size());
        values.stream().forEach(System.out::println);
        System.out.println("<<<<<<");
    }

    public static void print_c(final GraphTraversal traversal) {
        System.out.println(">>>>>");
        System.out.println("traversal: " + traversal);
        List<Object> values = traversal.toList();
        System.out.println("count: " + values.toString());
        System.out.println("<<<<<<");
    }

    public static void print_s(final GraphTraversal traversal) {
        System.out.println(">>>>>");
        System.out.println("traversal: " + traversal);
        System.out.println(traversal.explain());
        System.out.println("<<<<<<");
    }

    public static void print(Object obj) {
        System.out.println(">>>>>");
        if (obj instanceof List) {
            if (((List) obj).size() > 0) {
                ((List) obj).stream().forEach(System.out::println);
            } else {
                System.out.println(obj);
            }
        } else {
            System.out.println(obj);
        }
    }
}
