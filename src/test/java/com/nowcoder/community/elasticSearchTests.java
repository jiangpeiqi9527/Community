package com.nowcoder.community;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class elasticSearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
//    @Qualifier("elasticsearchClient")
    private RestHighLevelClient elasticsearchClient;

    @Test
    public void getTest() {
        System.out.println(elasticsearchClient);
    }

    /**
     * 测试存储数据到es
     */
    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("9"); // 数据的id
//        indexRequest.source("username", "zhangsan", "age", 18, "gender", "男");
        User user = new User();
        user.setAge(18);
        user.setUserName("zhangsan");
        user.setGender("男");
        String jsonString = JSONObject.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON); // 要保存的内容
        // 执行操作
        IndexResponse index = elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
        // 提取有用的响应数据
        System.out.println(index);
    }

    /**
     * 成功将discusspost实体类插入到es服务器中
     *
     * @throws IOException
     */
    @Test
    public void testInsert() throws IOException {
        DiscussPost post = discussPostMapper.selectDiscussPostById(241);
        IndexRequest indexRequest = new IndexRequest("discusspost");
        indexRequest.id(Integer.toString(241));
        String jsonString = JSONObject.toJSONString(post);
        indexRequest.source(jsonString, XContentType.JSON);
        IndexResponse indexResponse = elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(indexResponse);
    }

    @Test
    public void testInsertList() throws IOException {
        insertDiscussPosts(discussPostMapper.selectDiscussPosts(101, 0, 100));
        insertDiscussPosts(discussPostMapper.selectDiscussPosts(102, 0, 100));
        insertDiscussPosts(discussPostMapper.selectDiscussPosts(103, 0, 100));
        insertDiscussPosts(discussPostMapper.selectDiscussPosts(111, 0, 100));
        insertDiscussPosts(discussPostMapper.selectDiscussPosts(112, 0, 100));
        insertDiscussPosts(discussPostMapper.selectDiscussPosts(131, 0, 100));
        insertDiscussPosts(discussPostMapper.selectDiscussPosts(132, 0, 100));
        insertDiscussPosts(discussPostMapper.selectDiscussPosts(133, 0, 100));
        insertDiscussPosts(discussPostMapper.selectDiscussPosts(134, 0, 100));
    }

    /**
     * 插入多条帖子
     *
     * @param list
     * @throws IOException
     */
    private void insertDiscussPosts(List<DiscussPost> list) throws IOException {
        if (list != null && !list.isEmpty()) {
            for (DiscussPost post : list) {
                insertDiscussPost(post);
            }
        }
    }

    /**
     * 插入单条帖子数据
     *
     * @param post
     * @throws IOException
     */
    private void insertDiscussPost(DiscussPost post) throws IOException {
        IndexRequest indexRequest = new IndexRequest("discusspost");
        int postId = post.getId();
        indexRequest.id(Integer.toString(postId));
        String jsonString = JSONObject.toJSONString(post);
        indexRequest.source(jsonString, XContentType.JSON);
        IndexResponse indexResponse = elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(indexResponse);
    }

    /**
     * 更新数据
     *
     * @param id
     * @throws IOException
     */
    private void updateDiscussPostById(int id) throws IOException {
        DiscussPost post = discussPostMapper.selectDiscussPostById(id);
        post.setContent("我是新人，使劲灌水");
        UpdateRequest request = new UpdateRequest(
                "discusspost",
                Integer.toString(id));
        request.doc(JSON.toJSONString(post), XContentType.JSON);
        //执行更新请求
        UpdateResponse updateResponse = elasticsearchClient.update(request, RequestOptions.DEFAULT);
        System.out.println(updateResponse);
    }

    private void deleteDiscussPostById(int id) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("discusspost", Integer.toString(id));
        elasticsearchClient.delete(deleteRequest, RequestOptions.DEFAULT);
    }

    @Test
    public void testUpdate() throws IOException {
        updateDiscussPostById(231);
    }

    @Test
    public void testDelete() throws IOException {
        deleteDiscussPostById(231);
    }

    @Test
    public void searchTest() throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0)
                .size(10)
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC));
        //高亮
        HighlightBuilder highlight = new HighlightBuilder();
        highlight.field("title").preTags("<em>").postTags("</em>");
        highlight.field("content").preTags("<em>").postTags("</em>");
        //关闭多个高亮
        highlight.requireFieldMatch(false);
        searchSourceBuilder.highlighter(highlight);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();
        System.out.println(hits.length);
        System.out.println(searchResponse);
        for (SearchHit searchHit : hits) {
            System.out.println(searchHit);
        }

        // 处理一下高亮数据
        SearchHits searchHits = searchResponse.getHits();
        if (searchHits.getHits() == null) {
            return;
        }

        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit hit : searchHits.getHits()) {
            DiscussPost post = new DiscussPost();

            String id = hit.getSourceAsMap().get("id").toString();
            post.setId(Integer.parseInt(id));

            String userId = hit.getSourceAsMap().get("userId").toString();
            post.setUserId(Integer.parseInt(userId));

            String title = hit.getSourceAsMap().get("title").toString();
            post.setTitle(title);

            String content = hit.getSourceAsMap().get("content").toString();
            post.setContent(content);

            String status = hit.getSourceAsMap().get("status").toString();
            post.setStatus(Integer.parseInt(status));

            String createTime = hit.getSourceAsMap().get("createTime").toString();
            post.setCreateTime(new Date(Long.parseLong(createTime)));

            String commentCount = hit.getSourceAsMap().get("commentCount").toString();
            post.setCommentCount(Integer.parseInt(commentCount));

            // 处理高亮显示的结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField titleField = highlightFields.get("title");
            if (titleField != null) {
                Text[] fragments = titleField.getFragments();
                post.setTitle(fragments[0].toString());
            }

            HighlightField contentField = highlightFields.get("content");
            if (contentField != null) {
                Text[] fragments = contentField.getFragments();
                post.setContent(fragments[0].toString());
            }

            list.add(post);
        }
        System.out.println("======================");
        System.out.println(list);
    }
//    @Test
//    public void testDeleteAll() throws IOException {
//        deleteDiscussPostAll();
//    }

    /**
     * 删除所有数据
     */
//    private void deleteDiscussPostAll() throws IOException {
//        DeleteRequest deleteRequest = new DeleteRequest("discusspost");
//        deleteRequest.index("discusspost");
//        elasticsearchClient.delete(deleteRequest, RequestOptions.DEFAULT);
//    }

    class User {

        private String gender;
        private Integer age;
        private String userName;

        @Override
        public String toString() {
            return "User{" +
                    "userName='" + userName + '\'' +
                    ", gender='" + gender + '\'' +
                    ", age=" + age +
                    '}';
        }

        public User() {
        }

        public User(String userName, String gender, Integer age) {
            this.userName = userName;
            this.gender = gender;
            this.age = age;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

    }
}
