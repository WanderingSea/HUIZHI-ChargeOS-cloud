package com.hcp.system.service;

import java.util.List;

import com.hcp.common.core.domain.R;
import com.hcp.common.core.web.domain.AjaxResult;
import com.hcp.system.api.domain.dto.RegisterMerchantDTO;
import com.hcp.system.api.domain.dto.RegisterMerchantSubAccountDTO;
import com.hcp.system.domain.SysTenant;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 运营商（租户）管理Service接口
 *
 * @author JHan
 * @date 2026-08-10
 */
public interface ISysTenantService
{
    /**
     * 平台级管理员注册运营商接口
     */
    R<String> registerMerchant(RegisterMerchantDTO dto);

    /**
     * 运营商注册子账户接口（员工账号）
     */
    R<String> registerSubAccount(RegisterMerchantSubAccountDTO dto);

    /**
     * 查询运营商（租户）管理
     *
     * @param id 租户管理主键
     * @return 租户管理
     */
    SysTenant selectSysTenantById(Long id);

    /**
     * 查询租户管理列表-分页
     *
     * @param sysTenant 租户管理
     * @return 租户管理集合
     */
    IPage<SysTenant> selectSysTenantPage(SysTenant sysTenant);
    /**
     * 查询租户管理列表
     *
     * @param sysTenant 租户管理
     * @return 租户管理集合
     */
    List<SysTenant> selectSysTenantList(SysTenant sysTenant);

    /**
     * 新增租户管理
     *
     * @param sysTenant 租户管理
     * @return 结果
     */
    AjaxResult insertSysTenant(SysTenant sysTenant);

    /**
     * 修改租户管理
     *
     * @param sysTenant 租户管理
     * @return 结果
     */
    int updateSysTenant(SysTenant sysTenant);

    /**
     * 批量删除租户管理
     *
     * @param ids 需要删除的租户管理主键集合
     * @return 结果
     */
    int deleteSysTenantByIds(Long[] ids);

    /**
     * 删除租户管理信息
     *
     * @param id 租户管理主键
     * @return 结果
     */
    int deleteSysTenantById(Long id);


}
