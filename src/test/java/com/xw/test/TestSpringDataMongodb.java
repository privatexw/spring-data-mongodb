package com.xw.test;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.xw.MongodbDataApplication;
import com.xw.pojo.Order;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author xw
 * @create 2022-04-29 10:45
 */
@SpringBootTest(classes = {MongodbDataApplication.class})
//@RunWith作用是告诉java你这个类通过用什么运行环境运行，例如启动和创建spring的应用上下文 否则你需要为此在启动时写一堆的环境配置代码。
//当测试类A用到类B的实例的时候，是直接从容器中获取，而不是去new一个B
//如果没有@RunWith(SpringRunner.class)时候，会报错，空指针异常
@RunWith(SpringRunner.class)
public class TestSpringDataMongodb {
    @Autowired
    MongoTemplate mongoTemplate;

    /**
     * 单条新增 <T> T insert(T obj)
     * 批量新增 <T> java.util.Collection<T> insert(java.util.Collection<? extends T> obj,Class<?> class)
     */
    @Test
    public void testInsert() {
        Order o1 = new Order();
        o1.setTitle("测试标题1");
        o1.setPayment("100");
        o1.setItems(Arrays.asList("商品1", "商品2", "商品3"));

        System.out.println("新增前id: " + o1.getId());
        o1 = mongoTemplate.insert(o1);
        System.out.println("新增后id: " + o1.getId());

        Collection<Order> orders = new ArrayList<Order>();
        //批量新增
        for (int i = 0; i < 2; i++) {
            Order o2 = new Order();
            o2.setTitle("测试标题1");
            o2.setPayment("100");
            o2.setItems(Arrays.asList("商品1", "商品2", "商品3"));
            orders.add(o2);
        }
        System.out.println("批量新增前id: " + orders.toString());
        orders = mongoTemplate.insert(orders, Order.class);
        System.out.println("批量新增后id: " + orders.toString());
    }

    /**
     * 删除-主键删除 remove(Object obj) 只需对象中的主键数据即可删除
     * 删除-条件删除 remove(Query query,Class class) 根据条件删除，new Query()作为条件表示删除全表数据 (慎用! 没有回滚或提交步骤)
     */
    @Test
    public void testDelete() {
        Order o1 = new Order();
        o1.setId("626b5a220d4fa64852c16ab2");
        DeleteResult remove = mongoTemplate.remove(o1);
        System.out.println("对象id查询删除了:" + remove.getDeletedCount());

        Query query = Query.query(Criteria.where("id").is("626b5a220d4fa64852c16ab0"));
        DeleteResult removeQuery = mongoTemplate.remove(query, Order.class);
        System.out.println("条件查询删除了:" + removeQuery.getDeletedCount());
        //mongoTemplate.findAndRemove(query,Order.class);

        //删除全部
        //mongoTemplate.remove(new Query(),Order.class);

    }

    /**
     * 修改--全量替换/覆盖
     * 基于java对象的新增或修改
     */
    @Test
    public void testSave() {
        Order o1 = new Order();
        o1.setId("626b55c104b94571296ec2af");
        o1.setTitle("测试标题1");
        o1.setPayment("100");
        o1.setItems(Arrays.asList("商品1", "商品2", "商品3"));
        mongoTemplate.save(o1/*,"order"*/);
    }

    /**
     * 修改--表达式更新($set $inc $put)
     * updateFirst = db.collectionName.update({条件},{更新操作符})
     * updateMulti = db.collectionName.update({条件},{更新操作符},{multi:true})
     * updateFirst(Query query,Update update,Class class) 根据实体类中的注解配置或默认映射，实现修改
     * updateFirst(Query query,Update update,String collectionName) 根据集合中的字段命名，实现修改
     * 基于Bson修改
     */
    @Test
    public void testUpdate() {
        //简写 类型中的静态方法
        //where字段名称 is比较字段 = {"title":"02"}
        Query query = Query.query(Criteria.where("title").is("测试标题1"));
        //{"$set":{"title":"更新数据"}} return (new Update()).set(key, value);
        Update update = Update.update("title", "更新测试标题1").set("payment", "999");
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Order.class);
        System.out.println(updateResult.getMatchedCount() + " " + updateResult.getModifiedCount() + " " + updateResult.getUpsertedId());

        //多行更新
        //写法2
        Query queryMulti = new Query();
        Criteria criteria = Criteria.where("title").in("04", "05");//查询条件
        queryMulti.addCriteria(criteria);

        Update updateMulti = new Update();
        updateMulti.set("payment", "1000")
                .set("items", Arrays.asList("订单0", "订单00", "订单000"));

        UpdateResult updateResult1 = mongoTemplate.updateMulti(queryMulti, updateMulti, Order.class);
        System.out.println(updateResult1.getMatchedCount() + " " + updateResult1.getModifiedCount() + " " + updateResult1.getUpsertedId());

    }


    /**
     * 查询-查询全部
     * <T> java.util.List<T> findAll(Class<T> entityClass)
     * <T> java.util.List<T> findAll(Class<T> entityClass,String collectionName
     */
    @Test
    public void testQuery() {
        List<Order> orderList = mongoTemplate.findAll(Order.class);
        List<Order> orderList1 = mongoTemplate.findAll(Order.class, "order");

        printOrderList(orderList);

        printOrderList(orderList1);
    }


    public void printOrderList(List<Order> orderList) {
        System.out.println("======================>");
        for (int i = 0; i < orderList.size(); i++) {
            System.out.println(orderList.get(i));
        }
        System.out.println("<======================");
    }

    /**
     * 查询单对象 使用主键作为查询条件 无数据返回null
     * public <T> T findById(Object id, Class<T> entityClass)
     * 根据条件查询匹配的第一条数据
     * public <T> T findOne(Query query,Class<T> entityClass)
     */
    @Test
    public void testFindById() {
        Order o1 = mongoTemplate.findById("626b55c104b94571296ec2af", Order.class);
        System.out.println("findById:" + o1);

        Order o2 = mongoTemplate.findOne(Query.query(Criteria.where("title").is("测试标题1")), Order.class);
        System.out.println("findOne:" + o2);
    }

    /**
     * 条件查询
     */
    @Test
    public void testFind() {
        //查询全部 等同于 findAll java.util.List<T> findAll(Class<T> entityClass)
        /*List<Order> orderList = mongoTemplate.find(new Query(), Order.class);
        printOrderList(orderList);*/

        //等值
        /*Query query = Query.query(Criteria.where("_class").is("com.xw.pojo.Order"));
        List<Order> orderList1 = mongoTemplate.find(query, Order.class);
        printOrderList(orderList1);*/

        //不等值查询 ne
        //printOrderList(mongoTemplate.find(Query.query(Criteria.where("_class").ne("com.xw.pojo.Order")), Order.class));

        //不等值范围查询 > < >= <=
        //printOrderList(mongoTemplate.find(Query.query(Criteria.where("price").gt(150).lt(400)), Order.class));

        //in | nin | exists(字段是否存在或值是否为空)
        /*printOrderList(mongoTemplate.find(Query.query(Criteria.where("items").in("订单0","订单00","订单000")), Order.class));
        printOrderList(mongoTemplate.find(Query.query(Criteria.where("items").nin("订单0","订单00","订单000")), Order.class));
        printOrderList(mongoTemplate.find(Query.query(Criteria.where("_class").exists(true)), Order.class));*/

        //正则查询  以1结尾的titile java中不需要写// 在mongodb中//标记正则表达式的开始和结束
        //printOrderList(mongoTemplate.find(Query.query(Criteria.where("title").regex("1$")), Order.class));

        //去除重复数据查询 public <T> java.util.List<T> findDistinct(Query query,
        //                                          String field, 去除重复字段名
        //                                          Class<?> entityClass, 集合类型
        //                                          Class<T> resultClass) 投影字段类型
       /* List<String> title = mongoTemplate.findDistinct(new Query(), "title", Order.class, String.class);
        System.out.println(title);*/

        //复合条件查询  and
        //混合条件代表一个字段有多个情况满足其一即可 如果是数字全部满足 并列 title="测试标题1" and title regex /1$/
        //eg Criteria.where("price").gte(160).lte(180)
        Query query = Query.query(
                Criteria.where("title").is("测试标题1").regex("1$")
        );
        List<Order> orderList = mongoTemplate.find(query, Order.class);
        printOrderList(orderList);
        //多条件字段不同使用 andOperator实现
        Query query1 = Query.query(new Criteria().andOperator(
                Criteria.where("title").is("测试标题1"),
                Criteria.where("price").gte(160).lte(180)
        ));
        List<Order> orderList1 = mongoTemplate.find(query1, Order.class);
        printOrderList(orderList1);

        //复合条件查询  or
        Query query2 = Query.query(new Criteria().orOperator(
                Criteria.where("title").is("测试标题1"),
                Criteria.where("price").gte(170)
        ));
        List<Order> orderList2 = mongoTemplate.find(query2, Order.class);
        printOrderList(orderList2);

        //复合条件查询  and + or 注意嵌套查询
        Criteria c1 = new Criteria().andOperator(
                Criteria.where("title").is("测试标题1"),
                Criteria.where("price").gte(170)
        );
        Criteria c2 = new Criteria().andOperator(
                Criteria.where("title").ne("测试标题1"),
                Criteria.where("price").gt(250)
        );

        List<Order> orderList3 = mongoTemplate.find(Query.query(new Criteria().orOperator(
                c1, c2
        )), Order.class);
        printOrderList(orderList3);
    }

    /**
     * 多条件复合查询
     * 动态条件拼接
     */
    @Test
    public void testFind2() {
        String title = "测试标题1";
        String price = "170";
        Query query = new Query();

/*        if(!StringUtils.isEmpty(title)) {
            //大小写不敏感
            Pattern pattern = Pattern.compile("^.*"+title+".*$",Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("title").regex(pattern));
        }
        if(!StringUtils.isEmpty(price)) {
            query.addCriteria(Criteria.where("price").gt(Double.parseDouble(price)));
        }
        //实体类的id和mongodb中的_id都可以查询
        query.addCriteria(Criteria.where("id").is("626b8dac286bfc0058736220"));*/

        //或者 也可以
/*        if(!StringUtils.isEmpty(title) && !StringUtils.isEmpty(price)) {
            Pattern pattern = Pattern.compile("^.*"+title+".*$",Pattern.CASE_INSENSITIVE);
            query.addCriteria(
                    Criteria.where("title").regex(pattern)
                            .and("price").gt(Double.parseDouble(price))
                            .and("id").is("626b8dac286bfc0058736220")
            );
        }*/

        //或者
        //标题等于测试标题 或者(价格在160-170之间) 或者id等于多少
        Map<String,Object> map = new HashMap<>();
        map.put("patientId","1111");
        map.put("userId","3");
        map.put("encounterId","");

        List<Criteria> list = new ArrayList<Criteria>();
        if(!StringUtils.isEmpty(map.get("patientId"))) {
            Criteria criteria1 = Criteria.where("title").is(title);
            list.add(criteria1);
        }
        if(!StringUtils.isEmpty(map.get("userId"))) {
            Criteria criteria2 = Criteria.where("price").gte(140).lte(160);
            list.add(criteria2);
        }
        if(!StringUtils.isEmpty(map.get("encounterId"))) {
            Criteria criteria3 = Criteria.where("id").is("626b55c104b94571296ec2b3");
           list.add(criteria3);
        }
        if(list.size() > 0) {
            Criteria[] cs = new Criteria[list.size()];
            for (int i = 0; i < list.size(); i++) {
                cs[i] = list.get(i);
            }
            query = Query.query(new Criteria().andOperator(
                    cs
            ));
        }

        List<Order> orderList = mongoTemplate.find(query, Order.class);
        printOrderList(orderList);
    }

    /**
     * 分页排序测试
     */
    @Test
    public void testSort() {
        Query query = new Query();
        //页码从第一页开始
        query.with(PageRequest.of(0, 10) );
        //query.with(Sort.by(Sort.Direction.DESC,"title"));
        query.with(Sort.by(Sort.Order.desc("title"),Sort.Order.asc("payment")));
        //或者多条件排序
        //query.with(Sort.by(Sort.Order.desc("title"),Sort.Order.asc("price")));
        List<Order> orderList = mongoTemplate.find(query, Order.class);
        printOrderList(orderList);
    }

    /**
     * 聚合函数测试
     * public <T> AggregationResults<T> aggregate(TypedAggregation<?> aggregation,Class<O> outputType)
     * public static TypedAggregation<T> newAggregation(Class<T> type,AggregationOperation... operations)
     */
    @Test
    public void testAggregate() {
         Pattern pattern = Pattern.compile("^测试标题.*",Pattern.CASE_INSENSITIVE);
        //eq 是is
        TypedAggregation typedAggregation = TypedAggregation.newAggregation(Order.class,
                //match放在group前相当于where,放在group 后相当于having
                Aggregation.match(Criteria.where("title").is(pattern)),
                Aggregation.group("title").sum("price").as("totalPrice"),
                //聚合操作中也可以做分组和排序 先跳过一行再查下一行出来有先后顺序
                Aggregation.sort(Sort.by(Sort.Order.desc("totalPrice")))
                //[{_id=测试标题5, totalPrice=190.0}, {_id=测试标题4, totalPrice=180.0}, {_id=测试标题2, totalPrice=170.0}]

                //返回{_id=测试标题4, totalPrice=180.0}
                //Aggregation.skip(1l),
                //Aggregation.limit(1l)
                );
        AggregationResults<Map> aggregate = mongoTemplate.aggregate(typedAggregation, Map.class);
        List<Map> list = aggregate.getMappedResults();//多行数据
        //Map map = aggregate.getUniqueMappedResult();//单行数据，如果有多行数据返回报错 不推荐
        System.out.println(list);
    }
}
