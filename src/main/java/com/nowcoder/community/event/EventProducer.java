package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    // 处理事件
    public void fileEvent(Event event) {
        // 将事件发布到指定的主题
        kafkaTemplate.send(event.getTopic(),
                /*这里需要一个字符串数据，我们将event这个对象整个的转换成json格式的字符串*/
                JSONObject.toJSONString(event)
        );

    }
}
