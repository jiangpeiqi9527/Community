package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(149);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
//        user.setActivationCode();
        user.setCreateTime(new Date());
        user.setEmail("test@qq.com");
//        user.setHeaderUrl();
        user.setPassword("123456");
        user.setSalt("abc");
//        user.setStatus();
//        user.setType();
        user.setUsername("test");
        int res = userMapper.insertUser(user);
        System.out.println(res);
        System.out.println(user.getId());
    }

    @Test
    public void updateUser() {
        int i = userMapper.updateStatus(150, 1);
        System.out.println(i);

        i = userMapper.updatePassword(150, "1111");
        System.out.println(i);
    }

    @Test
    public void testSelectPosts() {
//        List<DiscussPost> postList = discussPostMapper.selectDiscussPosts(0, 0, 10);
//        for (DiscussPost post : postList) {
//            System.out.println(post);
//        }
//
//        int rows = discussPostMapper.selectDiscussPostRows(0);
//        System.out.println(rows);

        List<DiscussPost> postList = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for (DiscussPost post : postList) {
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket(101,"abc",0,new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println(loginTicket);
    }

    @Test
    public void testSelectLoginTicketAndUpdateLoginTicket() {
        LoginTicket lt = loginTicketMapper.selectByTicket("abc");
        loginTicketMapper.updateStatus("abc",1);
        System.out.println(loginTicketMapper.selectByTicket("abc"));
    }
}
