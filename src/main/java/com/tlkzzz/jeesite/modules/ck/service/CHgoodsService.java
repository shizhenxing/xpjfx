/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/tlkzzz/jeesite">JeeSite</a> All rights reserved.
 */
package com.tlkzzz.jeesite.modules.ck.service;

import java.text.DecimalFormat;
import java.util.List;

import com.tlkzzz.jeesite.modules.ck.dao.CGoodsDao;
import com.tlkzzz.jeesite.modules.ck.entity.CGoods;
import com.tlkzzz.jeesite.modules.sys.utils.ToolsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tlkzzz.jeesite.common.persistence.Page;
import com.tlkzzz.jeesite.common.service.CrudService;
import com.tlkzzz.jeesite.common.utils.StringUtils;
import com.tlkzzz.jeesite.modules.ck.entity.CHgoods;
import com.tlkzzz.jeesite.modules.ck.dao.CHgoodsDao;

/**
 * 仓库商品Service
 * @author xrc
 * @version 2017-03-13
 */
@Service
@Transactional(readOnly = true)
public class CHgoodsService extends CrudService<CHgoodsDao, CHgoods> {

	@Autowired
	private CGoodsDao cGoodsDao;
	@Autowired
	private CRkinfoService cRkinfoService;
	@Autowired
	private CYkinfoService cYkinfoService;
	
	public CHgoods get(String id) {
		CHgoods cHgoods = super.get(id);
		return cHgoods;
	}
	
	public List<CHgoods> findList(CHgoods cHgoods) {
		return super.findList(cHgoods);
	}

	public Integer findStockNum(CHgoods cHgoods) {
		return dao.findStockNum(cHgoods);
	}

	public Page<CHgoods> findPage(Page<CHgoods> page, CHgoods cHgoods) {
		page = super.findPage(page, cHgoods);
		DecimalFormat df = new DecimalFormat("#.####");
		for(CHgoods hGoods: page.getList()){
			CGoods goods = hGoods.getGoods();
			String[] unit = {goods.getBig().getName(),goods.getZong().getName(),goods.getSmall().getName()};
			hGoods.setUnit(ToolsUtils.unitTools(goods.getSpec().getName(),unit,Integer.parseInt(hGoods.getNub())));
			if(StringUtils.isNotBlank(goods.getCbj()))goods.setCbj(df.format(Double.parseDouble(goods.getCbj())));
			if(StringUtils.isNotBlank(goods.getSj()))goods.setSj(df.format(Double.parseDouble(goods.getSj())));
		}
		return page;
	}

	@Transactional(readOnly = false)
	public void saveStockGoods(CHgoods cHgoods,CHgoods oldHgoods,CGoods goods){
		double stockPrice = Double.parseDouble(goods.getCbj());//原来成本价
		double dBPrice = ToolsUtils.priceDynamicBalanceTools(dao.findStockSumNum(goods.getId()),
				Integer.parseInt(cHgoods.getNub()),stockPrice,cHgoods.getCbj());//动态平衡后的成本价
		String newNum = cHgoods.getNub();//添加的数量
		try{
			if(oldHgoods!=null){//商品库存已存在
				String numStr = String.valueOf(dao.findStockNum(cHgoods));//库存数
				int stockNum = (!numStr.equals("null"))?Integer.parseInt(numStr):0;
				newNum = String.valueOf(stockNum+Integer.parseInt(newNum));
				if(StringUtils.isNotBlank(cHgoods.getYjnub()))
					oldHgoods.setYjnub(cHgoods.getYjnub());
				oldHgoods.setNub(newNum);
				super.save(oldHgoods);
			}else {//商品库存不存在
				super.save(cHgoods);
			}
			goods.setCbj(String.valueOf(dBPrice));
			cGoodsDao.updateCBJ(goods);
			cHgoods.setGoods(goods);//添加入库记录
			cRkinfoService.saveInfo(cHgoods, String.valueOf(stockPrice), (oldHgoods!=null)?oldHgoods.getNub():"0");
		}catch (Exception e){//出现异常情况回滚信息
			e.printStackTrace();
			goods.setCbj(String.valueOf(stockPrice));
			cGoodsDao.updateCBJ(goods);
			if(oldHgoods!=null)super.save(oldHgoods);
		}
	}

	@Transactional(readOnly = false)
	public void save(CHgoods cHgoods) {
		CHgoods oldHgoods = dao.findHGByHG(cHgoods);
		CGoods goods = cGoodsDao.get(cHgoods.getGoods());
		saveStockGoods(cHgoods, oldHgoods, goods);
		//添加财务流程
	}

	@Transactional(readOnly = false)
	public void moveSave(CHgoods cHgoods){
	    String inHouseId = cHgoods.getHouse().getCode();
	    String outHouseId = cHgoods.getHouse().getId();
        dao.minStock(cHgoods);
        cHgoods.getHouse().setId(inHouseId);
        CHgoods hg = dao.findHGByHG(cHgoods);
        if(hg!=null){
            dao.addStock(cHgoods);
        }else{
            super.save(cHgoods);
        }
		cYkinfoService.saveInfo(cHgoods,outHouseId);
	}
	
	@Transactional(readOnly = false)
	public void delete(CHgoods cHgoods) {
		super.delete(cHgoods);
	}

	public Page<CHgoods> findUser(Page<CHgoods> page, CHgoods cHgoods ) {
		// 生成数据权限过滤条件（dsf为dataScopeFilter的简写，在xml中使用 ${sqlMap.dsf}调用权限SQL）
		//tCmMsg.getSqlMap().put("dsf", dataScopeFilter(tCmMsg.getCurrentUser(), "o", "a"));
		// 设置分页参数
		cHgoods.setPage(page);
		List<CHgoods> list = dao.findList(cHgoods);
		page.setList((list));
		return page;
	}
	
}