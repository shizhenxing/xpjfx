<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>品牌管理</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
			
		});
		function page(n,s){
			$("#pageNo").val(n);
			$("#pageSize").val(s);
			$("#searchForm").submit();
        	return false;
        }
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="${ctx}/ck/cBands/">品牌列表</a></li>
		<shiro:hasPermission name="ck:cBands:edit"><li><a href="${ctx}/ck/cBands/form">品牌添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="cBands" action="${ctx}/ck/cBands/" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<ul class="ul-form">
			<li><label>品牌名称：</label>
				<form:input path="name" htmlEscape="false" maxlength="64" class="input-medium"/>
			</li>
			<li class="btns"><input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/></li>
			<li class="clearfix"></li>
		</ul>
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed">
		<thead>
			<tr>
				<th>品牌名称</th>
				<th>创建时间</th>
				<th>备注</th>
				<shiro:hasPermission name="ck:cBands:edit"><th>操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${page.list}" var="cBands">
			<tr>
				<td><a href="${ctx}/ck/cBands/form?id=${cBands.id}">
					${cBands.name}
				</a></td>
				<td>
					<fmt:formatDate value="${cBands.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/>
				</td>
				<td>
					${cBands.remarks}
				</td>
				<shiro:hasPermission name="ck:cBands:edit"><td>
    				<a href="${ctx}/ck/cBands/form?id=${cBands.id}">修改</a>
					<a href="${ctx}/ck/cBands/delete?id=${cBands.id}" onclick="return confirmx('确认要删除该品牌吗？', this.href)">删除</a>
				</td></shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>