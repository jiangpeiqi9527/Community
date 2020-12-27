package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 点赞like:entity:entityType:entityId  --> set(userId)
     * 点赞操作
     *
     * @param userId     点赞的那个人的id
     * @param entityType 点赞对象的类型
     * @param entityId   点赞对象的编号
     */
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        // 由于点赞这个操作需要更新like:entity:entityType:entityId和like:user:userId这两个key，所以需要redis事务操作
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//        // 判断集合里有没有userId
//        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if (isMember) {
//            // 删除entityLikeKey(set)集合中的userId
//            redisTemplate.opsForSet().remove(entityLikeKey, userId);
//        } else {
//            // 添加entityLikeKey(set)集合里userId
//            redisTemplate.opsForSet().add(entityLikeKey, userId);
//        }
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);
                operations.multi();

                if (isMember) {
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }

                return operations.exec();
            }
        });
    }

    // 查询某实体点赞的数量
    public long findEntityLikeCount(int entityType, int entityId) {
        Long size = redisTemplate.opsForSet().size(RedisKeyUtil.getEntityLikeKey(entityType, entityId));
        return size;
    }

    // 查询某人对某实体的点赞状态
    // 返回值使用int类型而没有使用boolean是因为，int型的扩展性要好一些
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    // 查询某个用户获得的赞的数量
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer)redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }
}
