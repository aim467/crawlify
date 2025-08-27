package org.crawlify.platform.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class AnonymousInterceptor implements HandlerInterceptor {

    private static final String HEADER_NAME = "X-Crawlify-Token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径（去除上下文路径）
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        String path = contextPath.isEmpty() ? requestURI : requestURI.substring(contextPath.length());


        // 检查是否为目标接口
        if ("/spiderTask/async".equals(path) || "/saveNode".equals(path)) {
            // 校验请求头是否存在
            log.info("check header: {}", HEADER_NAME);
            String headerValue = request.getHeader(HEADER_NAME);
            if (headerValue == null || headerValue.isEmpty() || !headerValue.equals("crawlify_node")) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write("{\"msg\": \"invalid request\", \"code\": 400}");
                return false; // 拦截请求
            }
        }
        return true; // 放行
    }
}
