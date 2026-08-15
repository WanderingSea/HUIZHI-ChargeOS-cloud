package com.hcp.operator;

import com.hcp.common.swagger.annotation.EnableCustomSwagger2;
import com.hcp.common.security.annotation.EnableCustomConfig;
import com.hcp.common.security.annotation.EnableHcpFeignClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 系统模块
 *
 * @author abdl
 */
@Slf4j
@EnableCustomConfig
@EnableCustomSwagger2
@EnableHcpFeignClients
@SpringBootApplication
@EnableScheduling
public class HcpOperatorApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HcpOperatorApplication.class, args);
        log.info(" (^^)／▽ ▽＼(^^)------hcp运营模块启动成功---(^_^)／★＼(^_^) \n");
    }
}
