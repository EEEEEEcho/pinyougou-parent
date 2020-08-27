package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
@Service
@Transactional
public class BrandServiceImpl implements BrandService{
	@Autowired
	private TbBrandMapper brandMapper;
	
	@Override
	public List<TbBrand> findAll() {
		// TODO Auto-generated method stub
		return brandMapper.selectByExample(null);
	}

	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		//使用mybatis的分页插件，pageHelper，该插件在dao层的mybatis配置文件中进行了配置,自动实现分页
		PageHelper.startPage(pageNum, pageSize);
		
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);

		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(TbBrand brand) {
		// TODO Auto-generated method stub
		brandMapper.insert(brand);
	}

	@Override
	public TbBrand findOne(Long id) {
		// TODO Auto-generated method stub
		return brandMapper.selectByPrimaryKey(id);
	}

	@Override
	public void update(TbBrand brand) {
		// TODO Auto-generated method stub
		brandMapper.updateByPrimaryKey(brand);
	}

	@Override
	public void delete(Long[] ids) {
		// TODO Auto-generated method stub
		for(Long id : ids) {
			brandMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		//使用mybatis的分页插件，pageHelper，该插件在dao层的mybatis配置文件中进行了配置,自动实现分页
		PageHelper.startPage(pageNum, pageSize);
		//创建查询条件
		TbBrandExample example = new TbBrandExample();
		//使用mybatis的条件查询构造
		Criteria criteria = example.createCriteria();
		if(brand != null) {
			if(brand.getName() != null && brand.getName().length() > 0) {
				//System.out.println(brand.getName());
				criteria.andNameLike("%" + brand.getName() + "%");
			}
			if(brand.getFirstChar() != null && brand.getFirstChar().length() > 0) {
				//System.out.println(brand.getFirstChar());
				criteria.andFirstCharLike("%" + brand.getFirstChar() + "%");
			}
		}
		
		
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);

		return new PageResult(page.getTotal(), page.getResult());
		
	}
	
	@Override
	public List<Map> selectOptionList(){
		return brandMapper.selectOptionList();
	}
}
