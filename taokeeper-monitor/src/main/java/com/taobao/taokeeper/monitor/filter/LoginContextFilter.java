package com.taobao.taokeeper.monitor.filter;


import javax.servlet.*;
import java.io.IOException;

/**
 * 
 * @author leifu
 * @Date 2014年11月15日
 * @Time 上午11:58:07
 */
public class LoginContextFilter implements Filter {

    private static final String KEEPER_DEBUG = "keeperDebug";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {
    }
}
