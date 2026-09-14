package com.evops.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evops.entity.SecurityAccount;
import com.evops.mapper.SecurityAccountMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 从请求头解析当前账号身份：
 *   X-Account-Id       账号主键（优先）
 *   X-Account-Username 账号登录名（备选）
 * 缺省（两个头都没有）按平台运营处理，兼容既有匿名运营接口与历史数据。
 * 给出但无法解析或已停用的账号返回 401，杜绝伪造身份越权。
 */
@Component
public class AccountContextFilter extends OncePerRequestFilter {
    public static final String HEADER_ACCOUNT_ID = "X-Account-Id";
    public static final String HEADER_USERNAME = "X-Account-Username";

    private final SecurityAccountMapper accountMapper;

    public AccountContextFilter(SecurityAccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String idText = request.getHeader(HEADER_ACCOUNT_ID);
        String username = request.getHeader(HEADER_USERNAME);
        boolean identityGiven = StringUtils.hasText(idText) || StringUtils.hasText(username);
        try {
            if (identityGiven) {
                SecurityAccount account = loadAccount(idText, username);
                if (account == null || !"ACTIVE".equals(account.getStatus())) {
                    writeUnauthorized(response);
                    return;
                }
                TenantContext.set(new CurrentUser(account.getId(), account.getTenantId(),
                        account.getRoleCode()));
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private SecurityAccount loadAccount(String idText, String username) {
        if (StringUtils.hasText(idText)) {
            try {
                return accountMapper.selectById(Long.parseLong(idText.trim()));
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return accountMapper.selectOne(new LambdaQueryWrapper<SecurityAccount>()
                .eq(SecurityAccount::getUsername, username.trim()));
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"账号身份无效或已停用\",\"data\":null}");
    }
}
