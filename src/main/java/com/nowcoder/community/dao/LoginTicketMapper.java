package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@Deprecated // 声明这个类以后不建议使用
public interface LoginTicketMapper {

    int insertLoginTicket(LoginTicket loginTicket);

    // 通过查找login_ticket表中的ticket字段来获取登陆凭证
    LoginTicket selectByTicket(String ticket);

    int updateStatus(String ticket, int status);
}
