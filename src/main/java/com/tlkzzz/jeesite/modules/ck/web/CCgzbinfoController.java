/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/tlkzzz/jeesite">JeeSite</a> All rights reserved.
 */
package com.tlkzzz.jeesite.modules.ck.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tlkzzz.jeesite.modules.ck.entity.*;
import com.tlkzzz.jeesite.modules.ck.service.*;
import com.tlkzzz.jeesite.modules.sys.utils.UserUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tlkzzz.jeesite.common.config.Global;
import com.tlkzzz.jeesite.common.persistence.Page;
import com.tlkzzz.jeesite.common.web.BaseController;
import com.tlkzzz.jeesite.common.utils.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 采购订单Controller
 * @author xrc
 * @version 2017-03-17
 */
@Controller
@RequestMapping(value = "${adminPath}/ck/cCgzbinfo")
public class CCgzbinfoController extends BaseController {

	@Autowired
	private CRkckddinfoService cRkckddinfoService;
	@Autowired
	private CSupplierService cSupplierService;
	@Autowired
	private CCgzbinfoService cCgzbinfoService;
	@Autowired
	private CGoodsService cGoodsService;
	@Autowired
	private CHouseService cHouseService;
	@Autowired
	private CDdinfoService cDdinfoService;
	
	@ModelAttribute
	public CCgzbinfo get(@RequestParam(required=false) String id) {
		CCgzbinfo entity = null;
		if (StringUtils.isNotBlank(id)){
			entity = cCgzbinfoService.get(id);
		}
		if (entity == null){
			entity = new CCgzbinfo();
		}
		return entity;
	}
	
	@RequiresPermissions("ck:cCgzbinfo:view")
	@RequestMapping(value = {"list", ""})
	public String list(CCgzbinfo cCgzbinfo, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<CCgzbinfo> page = cCgzbinfoService.findPage(new Page<CCgzbinfo>(request, response), cCgzbinfo);
		model.addAttribute("goodsList", cGoodsService.findList(new CGoods()));
		model.addAttribute("cCgzbinfo", cCgzbinfo);
		model.addAttribute("page", page);
		return "modules/ck/cCgzbinfoList";
	}

	/**
	 * 送货上门填写页面
	 * @param cCgzbinfo
	 * @param model
	 * @return
	 */
	@RequiresPermissions("ck:cCgzbinfo:view")
	@RequestMapping(value = "form")
	public String form(CCgzbinfo cCgzbinfo, Model model) {
		model.addAttribute("supplierList", cSupplierService.findList(new CSupplier()));
		model.addAttribute("houseList", cHouseService.findList(new CHouse()));
		model.addAttribute("goodsList", cGoodsService.findList(new CGoods()));
		model.addAttribute("cCgzbinfo", cCgzbinfo);
		return "modules/ck/cCgzbinfoForm";
	}

	/**
	 * 送货上门保存信息
	 * @param cCgzbinfo
	 * @param model
	 * @param redirectAttributes
	 * @return
	 */
	@RequiresPermissions("ck:cCgzbinfo:edit")
	@RequestMapping(value = "save")
	public String save(CCgzbinfo cCgzbinfo, Model model, RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, cCgzbinfo)){
			return form(cCgzbinfo, model);
		}
		cCgzbinfoService.save(cCgzbinfo);
		addMessage(redirectAttributes, "保存采购订单成功");
		return "redirect:"+Global.getAdminPath()+"/ck/cCgzbinfo/?repage";
	}
	
	@RequiresPermissions("ck:cCgzbinfo:edit")
	@RequestMapping(value = "delete")
	public String delete(CCgzbinfo cCgzbinfo, RedirectAttributes redirectAttributes) {
		cCgzbinfoService.delete(cCgzbinfo);
		addMessage(redirectAttributes, "删除采购订单成功");
		return "redirect:"+Global.getAdminPath()+"/ck/cCgzbinfo/?repage";
	}

	@ResponseBody
	@RequestMapping(value = "changeState")
	public String changeState(String id,String state){
		String retStr = "false";
		if(StringUtils.isNotBlank(id)&&StringUtils.isNotBlank(state)){
			try {
				cCgzbinfoService.changeState(id, state);
				retStr = "true";
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return retStr;
	}

	/**
	 * 审批总订单，通过总订单ID将子订单信息汇总到采购汇总表中
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "auditing")
	public String auditing(String id){
		String retStr = "false";
		if(StringUtils.isNotBlank(id)){
			CRkckddinfo cRkckddinfo = new CRkckddinfo(id);
			cRkckddinfo.setIssp("1");
			cRkckddinfo.setSpr(UserUtils.getUser().getId());
			cRkckddinfo.setSpsj(new Date());
			cRkckddinfoService.changeIssp(cRkckddinfo);
			CDdinfo cDdinfo = new CDdinfo();
			cDdinfo.setRkckddinfo(cRkckddinfo);
			List<CDdinfo> cdList = cDdinfoService.findList(cDdinfo);
			for (CDdinfo cd: cdList) {
				cCgzbinfoService.saveInfo(cd);
			}
			retStr = "true";
		}
		return retStr;
	}
}