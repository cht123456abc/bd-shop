package com.bigdatan.b2c.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import util.SessionUtil;

import com.bigdatan.b2c.entity.user.User;

public class QqWebInterceptor implements HandlerInterceptor{
	

	public void afterCompletion(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		
	}

	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2, ModelAndView arg3) throws Exception {
		
	}
	/**
	 *  true表示继续流程（如调用下一个拦截器或处理器）；
     *  false表示流程中断（如登录检查失败），不会继续调用其他的拦截器或处理器，此时我们需要通过response来产生响应；
	 */
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler) throws Exception {
//			User user = SessionUtil.getUser(request);
//			if(user!=null){
//				//session 中有用户信息
//
//				return true;
//			}
//			response.getWriter().write("{\"res\":301}");
//
//			return false;
		return true;
		}
}
