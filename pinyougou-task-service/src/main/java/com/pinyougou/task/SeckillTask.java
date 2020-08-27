package com.pinyougou.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

@Component
public class SeckillTask {
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	@Autowired
	private RedisTemplate redisTemplate;

	// 10 * * * * ? 每分钟第十秒执行一次
	// 20 10 * * * ? 每小时10：20
	// 20 15 9 * * ? 每天得9点15分20秒
	// 20 15 9 20 * ? 每月得20日9点15分20秒
	// 20 15 9 20 11 ? 每年11月得20日9点15分20秒 执行一次
	// 20-30 15 9 20 11-12 ? 每年11月到12月得20日 9点15分 得20-30秒执行一次
	// 0/3 * * * * ? 从第0秒开始 每隔3秒执行一次
	@Scheduled(cron = "0 * * * * ?") // 每秒执行一次
	public void refreshSeckillGoods() {
		System.out.println("执行了秒杀商品增量更新" + new Date());

		List goodIdList = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());

		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");// 审核通过
		criteria.andStockCountGreaterThan(0);// 剩余库存大于0
		// criteria.andStartTimeLessThanOrEqualTo(new Date());// 开始时间小于等于当前时间
		// criteria.andEndTimeGreaterThan(new Date());// 结束时间大于当前时间
		System.out.println(goodIdList);
		// 缓存中有数据执行增量更新
		if (goodIdList.size() > 0) {
			criteria.andIdNotIn(goodIdList);
		}

		List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
		// 将商品列表装入缓存
		System.out.println("将秒杀商品列表装入缓存");
		for (TbSeckillGoods seckillGoods : seckillGoodsList) {
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
			System.out.println("增量更新秒杀商品ID:" + seckillGoods.getId());
		}
		System.out.println("...end");
	}

	@Scheduled(cron = "* * * * * ?")
	public void removeSeckillGoods() {
		System.out.println("移除秒杀商品任务在执行");
		// 扫描缓存中秒杀商品列表，发现过期的移除
		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
		for (TbSeckillGoods seckill : seckillGoodsList) {
			if (seckill.getEndTime().getTime() < new Date().getTime()) {// 如果结束日期小于当前日期，则表示过期
				seckillGoodsMapper.updateByPrimaryKey(seckill);// 向数据库保存记录
				redisTemplate.boundHashOps("seckillGoods").delete(seckill.getId());// 移除缓存数据
				System.out.println("移除秒杀商品" + seckill.getId());
			}
		}
		System.out.println("移除秒杀商品任务结束");
	}
}
