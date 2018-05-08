package com.bigdatan.b2c.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import constant.SystemCode;
import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.WordUtils;
//import org.apache.commons.lang3.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import util.*;
import util.Encryp.Sha1Util;
import util.http.HttpsUtil;

import com.alibaba.fastjson.JSONObject;
import com.bigdatan.b2c.entity.user.User;
import com.bigdatan.b2c.service.user.IUserService;


/**
 *
 * @Date：2016年12月9日 9:38:31
 * 微信授权 回调
 */
@Controller
@RequestMapping("/wxAuthor")
public class WxAuthor {
	private static Logger log =LoggerFactory.getLogger(WxAuthor.class);

	@Resource
	private IUserService userService;


	/**
	 * 微信公众号消息接入服务器验证
	 * @param request
	 * @param response
	 * @param signature
	 * @param timestamp
	 * @param nonce
	 * @param echostr
	 * @throws IOException
	 */
	@RequestMapping("/wxConfirm")
	public void wxConfirm(HttpServletRequest request, HttpServletResponse response,
							 @RequestParam(value="signature")String signature,
							 @RequestParam(value="timestamp")String timestamp,
							 @RequestParam(value="nonce")String nonce,
							 @RequestParam(value="echostr")String echostr
						  ) throws IOException {
		String[] arr = new String[]{Configure.TOKEN,timestamp,nonce};
		//排序
		Arrays.sort(arr);
		//生成字符串
		StringBuffer content = new StringBuffer();
		for(int i=0;i<arr.length;i++){
			content.append(arr[i]);
		}
		//SHA1加密
		String temp = Sha1Util.getSha1(content.toString());
		//比对signature
		PrintWriter out = response.getWriter();
		if(temp.equals(signature)){
			out.print(echostr);
		}
	}


	/**
	 * 拉取微信用户信息，并且登录用户，并跳转前端页面
	 *
	 * 用户授权后得到微信服务器返回的code
	 * 用code来换取access_token
	 * 再用access_token换取和oppen_id
	 * 再用access_token和oppen_id拉取用户信息
	 */
	@RequestMapping("/user")
	public JsonResponse<User> getUserInfo(@RequestParam(required=false,value="code")String code, HttpServletRequest request, HttpServletResponse response, @RequestParam(required=false,value="state")String state){
		log.info("开始到了微信回调");
		JSONObject jsonObject =null;//处理微信返回json数据
		
		User user=null;
		String openid="";//微信用户openid
		String access_token="";//授权码
		String resultStr ="";//接口返回字符
		String requesUrl = Configure.WX_GET_ACCESS_URL;//获取openid地址
		String userinfoUrl= Configure.WX_GET_USER_INFO_URL;//拉取用户信息地址
		try {
			if(!StringUtils.isBlank(code)&&!StringUtils.isBlank(requesUrl)){//开始获取用户openid
				requesUrl = requesUrl.replace("APP_ID",Configure.APP_ID).replace("APP_SECRET",Configure.APP_SECRET).replace("USE_CODE", code);
				resultStr = HttpsUtil.httpsRequest(requesUrl, "GET", null);//调用获取openid接口
				if(!StringUtils.isBlank(resultStr)&&resultStr.indexOf("openid")>0){
					jsonObject =JSONObject.parseObject(resultStr);
					openid=jsonObject.get("openid").toString();
					log.info("userService.getOneByOpenid" + "...." + openid);
					user = userService.getOneByOpenid(openid);
					//如果用户为空
					if(user==null){
						log.info("user==null");
						if(resultStr.indexOf("access_token")>0){
							access_token=jsonObject.get("access_token").toString();
						}
						//拉取用户信息
						userinfoUrl= userinfoUrl.replace("ACCESS_TOKEN", access_token).replace("OPEN_ID", openid);
						resultStr = HttpsUtil.httpsRequest(userinfoUrl, "GET", null);
						if(!StringUtils.isBlank(resultStr) && resultStr.indexOf("openid")>0){//判断返回值是否含有openid
							jsonObject = JSONObject.parseObject(resultStr);
							//这里可以用oppenid获取数据库t_user表里的user信息判断是否已经存在该user
								user=CommondUtil.getWxUser(jsonObject);
								//开始插入用户信息
								String nickName=StringFormat.toSubString(user.getNickname());
								if("".equals(nickName)){
									user.setNickname("...");
								}else{
									user.setNickname(nickName);
								}
								user.setCreateTime(new Date());
								userService.insertSelective(user);
								log.error("userService.insertSelective" + user);
						}
				}
				SessionUtil.setUser(request, user);
//				response.sendRedirect("*****?openid="+openid);//跳转至前端 html
				//response.sendRedirect("http://localhost:4865/");
			}else{//授权失败或者网络异常
				log.error("授权失败,返回code为："+code+"获取openid地址:"+requesUrl);
			}
		}else{
			//获取code失败 用户不同意授权，以游客身份进入
			log.error("获取code失败....");
		}
	} catch (Exception e) {
			log.error("发生异常："+e.getMessage());
	}
		log.info("微信回调结束");
		JsonResponse<User> result = new JsonResponse<>();
		result.setRes(SystemCode.SUCCESS);
		result.setObj(user);
		result.setResult(SystemCode.GetErrorDesc(SystemCode.SUCCESS));
		return result;
	}

}
