package com.pinyougou.seckill.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;
import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {
	
	@Reference
	private  WeixinPayService weixinPayService;
	
	@Reference
	private SeckillOrderService seckillOrderService;
	
	@RequestMapping("/createNative")
	public Map createNative(){
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		TbSeckillOrder seckillOrder =seckillOrderService.searchOrderFromRedisByUserId(username);
		if(seckillOrder != null) {
			return weixinPayService.createNative(seckillOrder.getId()+"",(long)(seckillOrder.getMoney().doubleValue()*100)+"");
		}
		else {
			return new HashMap();
		}
	}

	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no){
		//获取当前用户		
		String userId=SecurityContextHolder.getContext().getAuthentication().getName();
		Result result=null;		
		int x=0;		
		while(true){
			//调用查询接口
			Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
			if(map==null){//出错			
				result=new  Result(false, "支付出错");
				break;
			}			
			if(map.get("trade_state").equals("SUCCESS")){//如果成功				
				result=new  Result(true, "支付成功");				
				seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), map.get("transaction_id"));
				break;
			}			
			try {
				Thread.sleep(3000);//间隔三秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
			x++;//设置超时时间为5分钟
			if(x>100){
				result=new  Result(false, "二维码超时");
				break;
			}			
		}
		return result;
	}
}
