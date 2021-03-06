/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/tlkzzz/jeesite">JeeSite</a> All rights reserved.
 */
package com.tlkzzz.jeesite.modules.ck.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tlkzzz.jeesite.common.utils.DateUtils;
import com.tlkzzz.jeesite.common.utils.excel.ExportExcel;
import com.tlkzzz.jeesite.modules.ck.entity.*;
import com.tlkzzz.jeesite.modules.ck.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tlkzzz.jeesite.common.config.Global;
import com.tlkzzz.jeesite.common.persistence.Page;
import com.tlkzzz.jeesite.common.web.BaseController;
import com.tlkzzz.jeesite.common.utils.StringUtils;

/**
 * 仓库商品Controller
 * @author xrc
 * @version 2017-03-13
 */
@Controller
@RequestMapping(value = "${adminPath}/ck/cHgoods")
public class CHgoodsController extends BaseController {

	@Autowired
	private CHgoodsService cHgoodsService;
	@Autowired
	private CHouseService cHouseService;
	@Autowired
	private CGoodsService cGoodsService;
	@Autowired
	private CCgzbinfoService cCgzbinfoService;
	@Autowired
	private CSupplierService cSupplierService;
	
	@ModelAttribute
	public CHgoods get(@RequestParam(required=false) String id) {
		CHgoods entity = null;
		if (StringUtils.isNotBlank(id)){
			entity = cHgoodsService.get(id);
		}
		if (entity == null){
			entity = new CHgoods();
		}
		return entity;
	}

	@RequiresPermissions("ck:cHgoods:view")
	@RequestMapping(value = "")
	public String index() {
		return "modules/ck/cHgoodsIndex";
	}

	@RequiresPermissions("ck:cHgoods:view")
	@RequestMapping(value = "tree")
	public String tree(Model model) {
		model.addAttribute("houseList", cHouseService.findList(new CHouse()));
		return "modules/ck/cHgoodsTree";
	}

	@RequiresPermissions("ck:cHgoods:view")
	@RequestMapping(value = "none")
	public String none() {
		return "modules/ck/cHgoodsNone";
	}

	@RequiresPermissions("ck:cHgoods:view")
	@RequestMapping(value = "list")
	public String list(CHgoods cHgoods, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<CHgoods> page = cHgoodsService.findPage(new Page<CHgoods>(request, response), cHgoods); 
		model.addAttribute("page", page);
		model.addAttribute("cHgoods", cHgoods);
		return "modules/ck/cHgoodsList";
	}

	@RequiresPermissions("ck:cHgoods:view")
	@RequestMapping(value = "form")
	public String form(CHgoods cHgoods, Model model) {//其他入库
		if(cHgoods!=null&&cHgoods.getGoods()!=null)
		    cHgoods.setCbj(Double.parseDouble(cHgoods.getGoods().getCbj()));
		model.addAttribute("cHgoods", cHgoods);
		model.addAttribute("houseList", cHouseService.findList(new CHouse()));
		model.addAttribute("goodsList", cGoodsService.findList(new CGoods()));
		return "modules/ck/cHgoodsForm";
	}

	@RequiresPermissions("ck:cHgoods:view")
	@RequestMapping(value = "cgInHouse")
	public String cgInHouse(String cGddId, Model model) {//采购入库
		if(StringUtils.isNotBlank(cGddId)) {
			CHgoods cHgoods = new CHgoods();
			CCgzbinfo cCgzbinfo = cCgzbinfoService.get(cGddId);
			cHgoods.setGoods(cCgzbinfo.getGoods());
			model.addAttribute("cGddId", cGddId);
			model.addAttribute("cHgoods", cHgoods);
			model.addAttribute("cCgzbinfo", cCgzbinfo);
			model.addAttribute("houseList", cHouseService.findList(new CHouse()));
			model.addAttribute("supplierList", cSupplierService.findList(new CSupplier()));
			return "modules/ck/cHgoodsCgHouse";
		}else {
			return "error/400";
		}
	}

	@RequiresPermissions("ck:cHgoods:edit")
	@RequestMapping(value = "move")
	public String move(Model model) {
		model.addAttribute("cHgoods", new CHgoods());
		model.addAttribute("houseList", cHouseService.findList(new CHouse()));
		model.addAttribute("goodsList", cGoodsService.findList(new CGoods()));
		return "modules/ck/cHgoodsMove";
	}

	@RequiresPermissions("ck:cHgoods:edit")
	@RequestMapping(value = "save")
	public String save(CHgoods cHgoods, Model model, RedirectAttributes redirectAttributes) {
		//其他入库库存保存方法
		if (!beanValidator(model, cHgoods)){
			return form(cHgoods, model);
		}
		cHgoodsService.save(cHgoods);
		addMessage(redirectAttributes, "保存仓库商品成功");
		return "redirect:"+Global.getAdminPath()+"/ck/cHgoods/list?repage";
	}

	@RequiresPermissions("ck:cHgoods:edit")
	@RequestMapping(value = "saveCG")
	public String saveCG(CHgoods cHgoods, Model model, RedirectAttributes redirectAttributes) {
		//采购入库库存保存方法
		if (!beanValidator(model, cHgoods)){
			return form(cHgoods, model);
		}
		cHgoodsService.save(cHgoods);
		cCgzbinfoService.savePrice(cHgoods);//添加入库信息到采购订单表
		addMessage(redirectAttributes, "保存仓库商品成功");
		return "redirect:"+Global.getAdminPath()+"/ck/cHgoods/list?repage";
	}

	@RequiresPermissions("ck:cHgoods:edit")
	@RequestMapping(value = "moveSave")
	public String moveSave(CHgoods cHgoods, Model model, RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, cHgoods)){
			return form(cHgoods, model);
		}
		cHgoodsService.moveSave(cHgoods);
		addMessage(redirectAttributes, "保存仓库商品成功");
		return "redirect:"+Global.getAdminPath()+"/ck/cHgoods/list?repage";
	}
	
	@RequiresPermissions("ck:cHgoods:edit")
	@RequestMapping(value = "delete")
	public String delete(CHgoods cHgoods, RedirectAttributes redirectAttributes) {
		cHgoodsService.delete(cHgoods);
		addMessage(redirectAttributes, "删除仓库商品成功");
		return "redirect:"+Global.getAdminPath()+"/ck/cHgoods/list?repage";
	}

	@ResponseBody
    @RequestMapping(value = "checkStockNum")
    public String checkStockNum(CHgoods cHgoods,String goodsId,String outId){
		String retStr = "true";
		if(StringUtils.isNotBlank(goodsId)&&StringUtils.isNotBlank(outId)){
			cHgoods.setGoods(new CGoods(goodsId));
			cHgoods.setHouse(new CHouse(outId));
			String numStr = String.valueOf(cHgoodsService.findStockNum(cHgoods));
			int sunNum = (!"null".equals(numStr))?Integer.parseInt(numStr):0;
			if(sunNum<Integer.parseInt(cHgoods.getNub()))retStr = "false";
		}
	    return retStr;
    }
	/**
	 * 导出供应商数据
	 * @param cHgoods
	 * @param request
	 * @param response
	 * @param redirectAttributes
	 * @return
	 */
	@RequiresPermissions("sys:user:view")
	@RequestMapping(value = "exportFile", method = RequestMethod.POST)
	public String exportFile(CHgoods cHgoods, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
		try {
			String fileName = "供应商数据" + DateUtils.getDate("yyyyMMddHHmmss") + ".xlsx";

			Page<CHgoods> page = cHgoodsService.findUser(new Page<CHgoods>(request, response, -1), cHgoods);
			new ExportExcel("导出数据", CHgoods.class).setDataList(page.getList()).write(response, fileName).dispose();
			//return null;
		} catch (Exception e) {
			addMessage(redirectAttributes, "导出用户失败！失败信息：" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

}