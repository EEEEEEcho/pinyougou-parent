package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;

@RestController
@RequestMapping("/cart")
public class CartController {
	@Reference(timeout=6000)
	private CartService cartService;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;
	/**
	 * 购物车列表
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		//当前登陆人账号
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登陆人:" + username);
		String cartListString = util.CookieUtil.getCookieValue(request, "cartList","UTF-8");
		if(cartListString == null || cartListString.equals("")) {
			cartListString = "[]";
		}
		List<Cart> carList_cookie = JSON.parseArray(cartListString, Cart.class);
		if(username.equals("anonymousUser")) {
			//未登录 从cookie中提取
			System.out.println("从Cookie中提取购物车");
			return carList_cookie;
		}
		else {
			//如果已登录 从redis中提取
			//获取redis购物车
			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			if(carList_cookie.size() > 0) {	//判断当本地购物车中存在数据
				//得到合并后的购物车
				List<Cart> cartList = cartService.mergeCartList(carList_cookie, cartList_redis);
				//List<Cart> cartList_redis = cartService.findCartListFromRedis(name);
				//合并后的购物车存入redis
				cartService.saveCartListToRedis(username, cartList);
				//本地购物车清除
				util.CookieUtil.deleteCookie(request, response, "cartList");
				System.out.println("执行了合并购物车的逻辑");
				return cartList;
			}
			return cartList_redis;
		}
		
	}
	
	@RequestMapping("/addGoodsToCartList")
	@CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
	public Result addGoodsToCartList(Long itemId,Integer num) {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登陆人:" + name);
		
		try {
			List<Cart> cartList = findCartList();
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			if(name.equals("anonymousUser")) {
				util.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList),3600*24,"UTF-8");
				System.out.println("向cookie中存入购物车");
			}
			else {
				cartService.saveCartListToRedis(name, cartList);
			}
			return new Result(true, "添加成功");
		}
		catch(Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}
}
