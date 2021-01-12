package com.nowcoder.community.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
public class ElasticsearchService {

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    /**
     * 存储帖子
     * @param post
     * @throws IOException
     */
    public void saveDiscussPost(DiscussPost post) throws IOException {
        IndexRequest indexRequest = new IndexRequest("discusspost");
        int postId = post.getId();
        indexRequest.id(Integer.toString(postId));
        String jsonString = JSONObject.toJSONString(post);
        indexRequest.source(jsonString, XContentType.JSON);
        /*IndexResponse indexResponse = */elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
//        System.out.println(indexResponse);
    }

    /**
     * 修改帖子
     * @param post 修改完的帖子
     */
    public void updateDiscussPost(DiscussPost post) throws IOException {
        UpdateRequest request = new UpdateRequest(
                "discusspost",
                Integer.toString(post.getId()));
        request.doc(JSON.toJSONString(post), XContentType.JSON);
        //执行更新请求
        /*UpdateResponse updateResponse = */elasticsearchClient.update(request, RequestOptions.DEFAULT);
//        System.out.println(updateResponse);
    }

    /**
     * 删除指定的id的帖子
     * @param id
     * @throws IOException
     */
    public void deleteDiscussPost(int id) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("discusspost", Integer.toString(id));
        elasticsearchClient.delete(deleteRequest, RequestOptions.DEFAULT);
    }

    public List<DiscussPost> searchDiscussPost(String keyword, int from, int limit) throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(from)
                .size(limit)
                .query(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
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
            return null;
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
        return list;
    }
}
