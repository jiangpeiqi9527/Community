package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞like:entity:entityType:entityId  --> set(userId)
    public void like(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 判断集合里有没有userId
        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if (isMember) {
            // 删除entityLikeKey(set)集合中的userId
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        } else {
            // 添加entityLikeKey(set)集合里userId
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }
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
}
