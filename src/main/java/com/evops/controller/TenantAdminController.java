package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.dto.AccountReq;
import com.evops.dto.GrantReq;
import com.evops.dto.TenantReq;
import com.evops.entity.DataGrant;
import com.evops.entity.SecurityAccount;
import com.evops.entity.SecurityTenant;
import com.evops.service.TenantAdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 多租户、账号与对象级授权管理（运营/平台侧）。
 */
@RestController
@RequestMapping("/api/icecore/admin")
public class TenantAdminController {

    private final TenantAdminService tenantAdminService;

    public TenantAdminController(TenantAdminService tenantAdminService) {
        this.tenantAdminService = tenantAdminService;
    }

    @PostMapping("/tenants")
    public ApiResponse<SecurityTenant> createTenant(@Valid @RequestBody TenantReq req) {
        return ApiResponse.ok(tenantAdminService.createTenant(req));
    }

    @PostMapping("/accounts")
    public ApiResponse<SecurityAccount> createAccount(@Valid @RequestBody AccountReq req) {
        return ApiResponse.ok(tenantAdminService.createAccount(req));
    }

    @GetMapping("/accounts")
    public ApiResponse<List<Map<String, Object>>> accounts() {
        return ApiResponse.ok(tenantAdminService.listAccounts());
    }

    /** 批量授权：把对象授权给账号 */
    @PostMapping("/grants")
    public ApiResponse<Map<String, Object>> grant(@Valid @RequestBody GrantReq req) {
        int created = tenantAdminService.grant(req);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("granted", created);
        return ApiResponse.ok(data);
    }

    /** 回收授权 */
    @PostMapping("/grants/revoke")
    public ApiResponse<Map<String, Object>> revoke(@Valid @RequestBody GrantReq req) {
        int removed = tenantAdminService.revoke(req);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("revoked", removed);
        return ApiResponse.ok(data);
    }

    @GetMapping("/grants")
    public ApiResponse<List<DataGrant>> grants(@RequestParam Long accountId) {
        return ApiResponse.ok(tenantAdminService.listGrants(accountId));
    }
}
