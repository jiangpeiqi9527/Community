package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    // 动态sql
    // 这个方法参数虽热是动态的sql，但有三个参数，所以不是必须用@Param
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // 查询出一共有多少条数据(这是一个动态的sql，带userId的时候用于个人主页查询，不带userId的时候用于查询所有)
    // 注意：如果啊，这个sql需要动态的拼一个条件，并且这个方法有且只有一个参数，
    // 那么，这个参数就必须用@Param注解取别名，不然报错。
    int selectDiscussPostRows(@Param("userId") int userId); // @Param用于给参数取别名，这里呢别名和传参名一致了，没关系

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

}
