package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
@Service
public class CartServiceImpl implements CartService{
	
	@Autowired
	TbItemMapper itemMapper;
	
	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		// TODO Auto-generated method stub
		//1.根据skuID查询商品明细SKU的对象
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if(item == null) {
			throw new RuntimeException("商品不存在");
		}
		if(!item.getStatus().equals("1")) {
			throw new RuntimeException("商品状态无效");
		}
		//2.根据SKU对象获得商家ID
		String sellerId = item.getSellerId();
		//3.根据商家ID在购物车列表中查询购物车对象
		Cart cart = searchCartBySellerId(cartList,sellerId);
		
		if(cart == null) {
			//4.如果购物车列表中不存在该商家的购物车
			//4.1创建一个新的购物车对象
			cart = new Cart();
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			TbOrderItem orderItem = createOrderItem(item,num);
			List<TbOrderItem> orderItemList = new ArrayList();
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			cartList.add(cart);
		}
		else {
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
			if(orderItem == null) {
				//如果没有，新增购物车明细
				orderItem = createOrderItem(item,num);
				cart.getOrderItemList().add(orderItem);
			}
			else {
				//如果有，在原购物车明细上添加数量更改金额
				orderItem.setNum(orderItem.getNum() + num);
				orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));
				if(orderItem.getNum() <= 0) {
					cart.getOrderItemList().remove(orderItem);
				}
				if(cart.getOrderItemList().size() == 0) {
					cartList.remove(cart);
				}
			}
		}
		return cartList;
	}

	/**
	 * 根据商家ID查询购物车对象
	 * @param cartList
	 * @param seller
	 * @return
	 */
	private Cart searchCartBySellerId(List<Cart> cartList,String sellerId) {
		for(Cart cart:cartList) {
			if(cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}
	
	/**
	 * 根据商品明细ID查询
	 * @param orderItemList
	 * @param itemId
	 * @return
	 */
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId) {
		for(TbOrderItem orderItem : orderItemList) {
			if(orderItem.getItemId().longValue() == itemId.longValue()) {
				return orderItem;
			}
		}
		return null;
	}
	
	private TbOrderItem createOrderItem(TbItem item,Integer num) {
		if(num <= 0) {
			throw new RuntimeException("数量非法");
		}
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
		return orderItem;
	}
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Override
	public List<Cart> findCartListFromRedis(String username) {
		// TODO Auto-generated method stub
		System.out.println("从redis中提取购物车" + username);
		List<Cart> cartList = (List<Cart>)redisTemplate.boundHashOps("cartList").get(username);
		if(cartList == null) {
			cartList = new ArrayList<Cart>();
		}
		return cartList;
	}

	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		// TODO Auto-generated method stub
		System.out.println("向redis中存入购物车" + username);
		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}

	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		// TODO Auto-generated method stub
		//return cartList1.addAll(cartList2);不能简单合并
		//return null;
		for(Cart cart : cartList2) {
			for(TbOrderItem orderItem : cart.getOrderItemList()) {
				addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
			}
		}
		return cartList1;
	}
}
