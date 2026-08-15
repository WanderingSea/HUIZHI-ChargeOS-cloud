package com.hcp.common.core.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.function.Predicate;

/**
 * <p>
 * UserNameGenerator
 * </p>
 * 1.后台管理级用户(平台管理员 和 运营商)账号生成
 * 2.运营商子账号生成
 * 3.用户昵称生成
 *
 * @author JHan
 * @Date 2026-08-09 15:47
 */
public class UserGenerator {

    /** 用户昵称固定前缀 */
    private static final String NICKNAME_PREFIX = "用户";

    /** 后台管理级用户登录账号固定前缀 */
    private static final String ADMIN_ACCOUNT_PREFIX  = "XZC";

    /** 运营商对外编号前缀 */
    private static final String MERCHANT_NO_PREFIX = "XZC";

    /** 运营商子账号前缀 */
    private static final String SUB_ACCOUNT_PREFIX = "SUB";

    /** 随机数生成器 */
    private static final Random RANDOM = new Random();

    /** 时间格式 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    /*
     * 管理级用户昵称生成
     * 规则：昵称为 "用户"+用户注册手机号
     * */
    public static String genNickname(String phone) {
        return NICKNAME_PREFIX + phone;
    }

    /**
     * 生成后台管理级用户账号（平台管理员和运营商）
     * 规则：XZC + 时分秒(6位) + 4位随机数
     * 示例：XZC 15 47 23 5891
     *
     * @return 生成的唯一账号
     */
    public static String genAdminAccount() {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);  // 取后6位
        String random = String.format("%04d", RANDOM.nextInt(10000));
        return ADMIN_ACCOUNT_PREFIX  + timestamp + random;
    }

    /**
     * 生成后台管理级用户账号（带唯一性校验）
     * 规则：XZC + 年月日时分秒(12位) + 3位随机数
     * 如果生成的账号已存在，会重新生成（最多重试10次）
     *
     * @param existsChecker 检查账号是否存在的函数，返回true表示已存在
     * @return 生成的唯一账号
     * @throws RuntimeException 如果超过最大重试次数仍无法生成唯一账号
     */
    public static String genAdminAccount(Predicate<String> existsChecker) {
        int maxRetry = 10;
        for (int i = 0; i < maxRetry; i++) {
            String account = genAdminAccount();
            if (!existsChecker.test(account)) {
                return account;
            }
            // 短暂休眠，避免生成相同时间戳
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        throw new RuntimeException("无法生成唯一的后台管理级用户账号，请稍后重试");
    }

    /**
     * 生成运营商对外编号（merchantNo）
     * 规则：XZC + yyMMddHHmmss + 4位随机数字
     * 示例：XZC 26 0809 123456 7890
     */
    public static String genMerchantNo() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);  // yyMMddHHmmss
        String randomNum = String.format("%04d", RANDOM.nextInt(10000));
        return MERCHANT_NO_PREFIX + dateStr + randomNum;
    }

    /**
     * 生成运营商对外编号（带唯一性校验）
     */
    public static String genMerchantNo(Predicate<String> existsChecker) {
        int maxRetry = 10;
        for (int i = 0; i < maxRetry; i++) {
            String merchantNo = genMerchantNo();
            if (!existsChecker.test(merchantNo)) {
                return merchantNo;
            }
        }
        throw new RuntimeException("无法生成唯一的运营商编号，请稍后重试");
    }

    /**
     * 生成运营商子账号
     * 规则：SUB + 租户ID(补齐6位) + 3位序号
     * 示例：SUB000101001
     *
     * @param tenantId 租户ID
     * @param sequence 子账号序号（从1开始）
     * @return 生成的子账号
     */
    public static String genSubAccount(Long tenantId, int sequence) {
        return SUB_ACCOUNT_PREFIX + String.format("%06d%03d", tenantId, sequence);
    }

    /**
     * 生成运营商子账号（带唯一性校验，自动递增序号）
     * 规则：SUB + 租户ID(补齐6位) + 3位序号
     *
     * @param tenantId      租户ID
     * @param startSequence 起始序号
     * @param existsChecker 检查账号是否存在的函数，返回true表示已存在
     * @return 生成的唯一子账号
     * @throws RuntimeException 如果序号超过999仍无法生成唯一账号
     */
    public static String genSubAccount(Long tenantId, int startSequence, Predicate<String> existsChecker) {
        int maxSequence = 999;
        for (int seq = startSequence; seq <= maxSequence; seq++) {
            String account = genSubAccount(tenantId, seq);
            if (!existsChecker.test(account)) {
                return account;
            }
        }
        throw new RuntimeException("该租户下子账号数量已达上限(999个)");
    }

    /**
     * 生成运营商子账号（从序号1开始自动查找可用序号）
     * 规则：SUB + 租户ID(补齐6位) + 3位序号
     *
     * @param tenantId      租户ID
     * @param existsChecker 检查账号是否存在的函数，返回true表示已存在
     * @return 生成的唯一子账号
     */
    public static String genSubAccount(Long tenantId, Predicate<String> existsChecker) {
        return genSubAccount(tenantId, 1, existsChecker);
    }

}
