package com.xw.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * @author xw
 * @create 2022-04-29 10:50
 *
 * value 对应mongodb中集合的名称 类名首字母小写
 * 需要写setter和getter
 */
@Document("order")
@Data
public class Order {
    //主键字段，可以用@id字段标识 如果属性名是 id|_id 注解可以省略
    @Id
    private  String id;

    /**
     * 用@Field字段来配置java中实体类属性和mogodb中集合field字段的匹配映射
     */
    @Field("title")
    private String title;

    private String payment;

    private List<String> items;

    private String _class;//对象实体类路径

    private  double price;//价格


}
