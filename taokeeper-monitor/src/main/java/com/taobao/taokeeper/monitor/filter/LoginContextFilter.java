package com.taobao.taokeeper.monitor.filter;

import com.taobao.taokeeper.monitor.util.UserAuthorityUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: yijunzhang
 * Date: 13-12-26
 * Time: 下午4:24
 */
public class LoginContextFilter implements Filter {

    private static final String KEEPER_DEBUG = "keeperDebug";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String url = httpRequest.getRequestURI() == null ? "" : httpRequest.getRequestURI();
        boolean hasAuthority = false;
        if (url.contains("/manager/") || url.contains("/manager.do") || url.contains("/admin.do")) {
            String debugStr = System.getProperty(KEEPER_DEBUG);
            if (!StringUtils.isBlank(debugStr) && Boolean.valueOf(debugStr)) {
                hasAuthority = true;
            } else {
                //白名单检查
                Cookie[] cookies = httpRequest.getCookies();
                for (Cookie cookie : cookies) {
                    if (cookie != null && StringUtils.isNotBlank(cookie.getName()) && StringUtils.isNotBlank(cookie.getValue())) {
                        String name = cookie.getName();
                        if (name.equals("c_u")) {
                            String userName = cookie.getValue();
                            String clusterIdStr = request.getParameter("clusterId");
                            request.setAttribute("loginUserName",userName);
                            //request.getParameterMap().put("loginUserName", userName);
                            if (StringUtils.isBlank(clusterIdStr)) {
                                int defaultClusterId = UserAuthorityUtil.getClusterIdByName(userName);
                                if (defaultClusterId == 0) {
                                    hasAuthority = true;
                                } else if (defaultClusterId != -1) {
                                    //request.setAttribute("clusterId",defaultClusterId);
                                    //request.getParameterMap().put("clusterId", defaultClusterId);
                                    hasAuthority = true;
                                }
                            } else {
                                hasAuthority = UserAuthorityUtil.hasAuthoritied(userName, Integer.parseInt(clusterIdStr));
                            }
                        }
                    }
                }
            }
        }else{
            hasAuthority = true;
        }
        if(hasAuthority){
           chain.doFilter(request,response);
        }else{
            //没有权限
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendRedirect("http://index.tv.sohuno.com/index");
            return;
        }

    }

    @Override
    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
