layui.use(['layer'], function(){
	layer = layui.layer;
	initPage(".pagination", "getRoles");
	$addOrEditModal = $("#add-or-edit-role");
	$("#totalCheckbox").change(function(){
		if(!$(this).hasClass("checked")){
			$("tr.each-row").find("input[type='checkbox']").each(function(){
				$(this).prop("checked", true);
			});
			$(this).addClass("checked");
		}else{
			$("tr.each-row").find("input[type='checkbox']").each(function(){
				$(this).removeAttr("checked");
			});
			$(this).removeClass("checked");
		}
			
	});
	$("tr.each-row").find("input[type='checkbox']").on("click", function(){
		alert("ddd");
	});
	
	$tableContainer = $(".table");
	//默认查询操作
	getRoles();  
});
var totalPage = 0;
var $addOrEditModal;
var $tableContainer;

/**
* 查询
*/
function getRoles(){
	var loadi = layer.load('努力加载中…'); //需关闭加载层时，执行layer.close(loadi)即可
	$.ajax({
		url : "/rl/roles?"+ jsonToGetRequestParams(getQueryPagingParams()),
		dataType: 'json', 
		beforeSend:function(){
		
		},
		success : function(data) {
			layer.close(loadi);
			//清空原来的数据
			$tableContainer.find(".each-row").remove();
			if(data.isSuccess){
				if(data.message.length == 0){
					if(currentIndex == 0){
						$tableContainer.append('<tr class="each-row"><td colspan="9">暂时还没有任何角色！</td></tr>');
					}else{
						$tableContainer.append('<tr class="each-row"><td colspan="9">已经没有更多的角色啦，请重新选择！</td></tr>');
						pageDivUtil(data.total);
					}
					return;
				}
				
				roles = data.message;
				for(var i = 0; i < roles.length; i++){
					$tableContainer.append(buildRow(roles[i], i));
					if(roles[i].status != 1)
						$tableContainer.find(".each-row").eq(i).addClass("status-disabled-row");
				}
				
				pageDivUtil(data.total);
			}else{
				ajaxError(data);
			}
		},
		error : function(data) {
			layer.close(loadi);
			isLoad = false;
			ajaxError(data);
		}
	});
}

/**
 * 添加角色
 */
function addRole(){
	$addOrEditModal.attr("data-id", "");
	$addOrEditModal.find('input[name="code"]').val("");
	$addOrEditModal.find('input[name="name"]').val("");
	$addOrEditModal.find('input[name="order"]').val("");
	$addOrEditModal.find('textarea[name="desc"]').val("");
	$addOrEditModal.find('input[name1="status_normal"]').prop("checked", true);
	$addOrEditModal.find('input[name1="status_disabled"]').removeAttr("checked");

	$addOrEditModal.modal("show");
}

/**
 * 修改角色
 */
function editRole(){
	var checkboxs = $("tr.each-row").find('input[type="checkbox"]:checked');
	
	if(!checkboxs || checkboxs.length == 0){
		layer.msg("请选择您要编辑的角色");
		return;
	}
	if(checkboxs && checkboxs.length > 1 ){
		layer.msg("一次只能编辑一个角色");
		return;
	}
	
	var role = roles[$(checkboxs[0]).closest("tr").attr("index")];
	$addOrEditModal.attr("data-id", role.id);
	$addOrEditModal.find('input[name="code"]').val(role.role_code);
	$addOrEditModal.find('input[name="name"]').val(role.role_name);
	$addOrEditModal.find('input[name="order"]').val(role.role_order);
	$addOrEditModal.find('textarea[name="desc"]').val(role.role_desc);
	if(role.status == 1){
		$addOrEditModal.find('input[name1="status_normal"]').prop("checked", true);
		$addOrEditModal.find('input[name1="status_disabled"]').removeAttr("checked");
	}else{
		$addOrEditModal.find('input[name1="status_disabled"]').prop("checked", true);
		$addOrEditModal.find('input[name1="status_normal"]').removeAttr("checked");
	}
	$addOrEditModal.modal("show");
}

/**
 * 修改角色
 */
function rowEditRole(obj){
	var tr = $(obj).closest("tr.each-row");
	var index = tr.attr("index");
	
	var role = roles[index];
	$addOrEditModal.attr("data-id", role.id);
	$addOrEditModal.find('input[name="code"]').val(role.role_code);
	$addOrEditModal.find('input[name="name"]').val(role.role_name);
	$addOrEditModal.find('input[name="order"]').val(role.role_order);
	$addOrEditModal.find('textarea[name="desc"]').val(role.role_desc);
	if(role.status == 1){
		$addOrEditModal.find('input[name1="status_normal"]').prop("checked", true);
		$addOrEditModal.find('input[name1="status_disabled"]').removeAttr("checked");
	}else{
		$addOrEditModal.find('input[name1="status_disabled"]').prop("checked", true);
		$addOrEditModal.find('input[name1="status_normal"]').removeAttr("checked");
	}
	$addOrEditModal.modal("show");
}

/**
 * 删除单个角色
 */
function rowDeleteRole(obj){
	var tr = $(obj).closest("tr.each-row");
	var dataId = tr.attr("data-id");
	
	layer.confirm('您要删除这1条角色记录吗？', {
		  btn: ['确定','点错了'] //按钮
	}, function(){
		var loadi = layer.load('努力加载中…'); //需关闭加载层时，执行layer.close(loadi)即可
		$.ajax({
			type: "delete",
			url : "/rl/role/" + dataId,
			dataType: 'json', 
			beforeSend:function(){
			},
			success : function(data) {
				layer.close(loadi);
				if(data.isSuccess){
					layer.msg(data.message + ",1秒后自动刷新");
					reloadPage(1000);
				}else{
					ajaxError(data);
				}
			},
			error : function(data) {
				layer.close(loadi);
				ajaxError(data);
			}
		});
	}, function(){
	});
	
}

/**
 * 删除多个角色
 */
function deletesRole(){
	var checkboxs = $("tr.each-row").find('input[type="checkbox"]:checked');
	
	if(!checkboxs || checkboxs.length == 0){
		layer.msg("请选择您要删除的角色");
		return;
	}
	
	layer.confirm('您要删除这'+ checkboxs.length +'条角色记录吗？', {
		  btn: ['确定','点错了'] //按钮
	}, function(){
		var rlids = "";
		for(var i = 0; i < checkboxs.length; i++){
			rlids += $(checkboxs[i]).closest("tr.each-row").attr("data-id") +",";
		}
		
		rlids = deleteLastStr(rlids);
		
		var loadi = layer.load('努力加载中…'); //需关闭加载层时，执行layer.close(loadi)即可
		$.ajax({
			type: "delete",
			url : "/rl/roles?rlids=" + rlids,
			dataType: 'json', 
			beforeSend:function(){
			},
			success : function(data) {
				layer.close(loadi);
				if(data.isSuccess){
					layer.msg(data.message + ",1秒后自动刷新");
					reloadPage(1000);
				}else{
					ajaxError(data);
				}
			},
			error : function(data) {
				layer.close(loadi);
				ajaxError(data);
			}
		});
	}, function(){
	});
	
}

/**
 * 展示角色列表
 * @param obj
 * @param rlid
 */
function showUserList(obj, rlid){
	//获取所有的角色列表
	var loadi = layer.load('努力加载中…'); //需关闭加载层时，执行layer.close(loadi)即可
	$.ajax({
		url : "/rl/role/"+ rlid +"/users",
		dataType: 'json', 
		beforeSend:function(){
			$("#alreadyUsers").html("");
			$("#notUsers").html("");
		},
		success : function(data) {
			layer.close(loadi);
			if(data.isSuccess){
				for(var i = 0 ; i < data.message.length; i++){
					if(isEmpty(data.message[i].has)){
						buildNotUsersHtml(data.message[i]);
					}else{
						buildAlreadyUsersHtml(data.message[i]);
					}
				}
				$("#role-list").modal("show");
				$("#role-list").attr("data-id", rlid);
			}else{
				ajaxError(data);
			}
		},
		error : function(data) {
			layer.close(loadi);
			ajaxError(data);
		}
	});
	
}

/**
 * 添加没有分配角色的html
 * @param user
 */
function buildNotUsersHtml(user){
	var html = '<span class="label label-info" data-id="'+ user.id +'" onclick="notClick(this);">'+ user.account +'</span>';
	$("#notUsers").append(html);
}

/**
 * 添加已经分配角色的html
 * @param user
 */
function buildAlreadyUsersHtml(user){
	var html = '<span class="label label-primary" data-id="'+ user.id +'" onclick="alreadyClick(this);">'+ user.account +'</span>';
	$("#alreadyUsers").append(html);
}

/**
 * 将已经分配的取消，加入待分配列表
 * @param obj
 */
function notClick(obj){
	var user = {};
	user.id = $(obj).attr("data-id");
	user.account = $(obj).text();
	$(obj).remove();
	buildAlreadyUsersHtml(user);
}

/**
 * 将已经分配的取消，加入待分配列表
 * @param obj
 */
function alreadyClick(obj){
	var user = {};
	user.id = $(obj).attr("data-id");
	user.account = $(obj).text();
	$(obj).remove();
	buildNotUsersHtml(user);
}

/**
 * 分配角色操作
 * @param obj
 */
function role(obj){
	var modal = $(obj).closest(".modal");
	var rlid = modal.attr("data-id");
	var alreadyUsers = modal.find("#alreadyUsers span");
	var userIds = "";
	alreadyUsers.each(function(){
		userIds += $(this).attr("data-id") +",";
	});
	
	userIds = deleteLastStr(userIds);
	var loadi = layer.load('努力加载中…'); //需关闭加载层时，执行layer.close(loadi)即可
	$.ajax({
		url : "/rl/role/"+ rlid +"/users/allot?users="+ userIds,
		dataType: 'json', 
		beforeSend:function(){
		},
		success : function(data) {
			layer.close(loadi);
			if(data.isSuccess){
				layer.msg(data.message + ",1秒后自动刷新");
				reloadPage(1000);
			}else{
				ajaxError(data);
			}
		},
		error : function(data) {
			layer.close(loadi);
			ajaxError(data);
		}
	});
}

/**
 * 添加获取编辑的提交操作
 * @param obj
 */
function addOrEditCommit(obj){
	var modal = $(obj).closest(".modal");
	var dataId = modal.attr("data-id");
	
	var formControl = modal.find(".form-control");
	var params = {};
	var flag = true;
	formControl.each(function(index){
		var name = $(this).attr("name");
		var empty = $(this).attr("empty");
		if(empty && empty == "false" && isEmpty($(this).val())){
			$(this).focus();
			layer.msg($(this).attr("placeholder"));
			flag = false;
			return flag;
		}
			
		params[name] = $(this).val();
	});
	
	if(flag){
		params.id = dataId;
		params.status = modal.find('input[name="status"]:checked').val();
		
		console.log(params);
		var loadi = layer.load('努力加载中…'); //需关闭加载层时，执行layer.close(loadi)即可
		$.ajax({
			type : isEmpty(dataId) ? "post" : "put",
			data : params,
			url : "/rl/role",
			dataType: 'json', 
			beforeSend:function(){
			},
			success : function(data) {
				layer.close(loadi);
				if(data.isSuccess){
					layer.msg(data.message +",1秒钟后自动刷新");
					setTimeout("window.location.reload();", 1000);
				}else{
					ajaxError(data);
				}
			},
			error : function(data) {
				layer.close(loadi);
				ajaxError(data);
			}
		});
	}
	
}

/**
 * 获取请求列表
 */
function getQueryPagingParams(){
	return {page_size: pageSize, current: currentIndex, total: totalPage, t: Math.random()};
}

function buildRow(role, index){
	var html = '<tr class="each-row" data-id="'+ role.id+'" index="'+ index +'">'+
					'<td><input type="checkbox" /></td>'+
					'<td>'+ changeNotNullString(role.role_name) +'</td>'+
					'<td>'+ role.role_code+'</td>'+
					'<td>'+ changeNotNullString(role.role_order) +'</td>'+
					'<td>'+ changeNotNullString(role.role_desc) +'</td>';
		if(isNotEmpty(role.users)){
			var users = role.users.split(",");
			html += '<td>';
			for(var rl = 0; rl < users.length; rl++)
				html += '<span class="label label-primary">'+ users[rl] +'</span>';
			
			html += '</td>';
		}else{
			html += '<td></td>';
		}
		html += '<td>'+ (role.status == 1? '正常': '禁用')+'</td>'+
					'<td>'+ role.create_time+'</td>'+
					'<td><a href="javascript:void(0);" onclick="showUserList(this, '+ role.id +');" style="margin-right: 10px;">分配</a><a href="javascript:void(0);" onclick="rowEditRole(this);" style="margin-right: 10px;">编辑</a><a href="javascript:void(0);" onclick="rowDeleteRole(this);" style="margin-right: 10px;">删除</a></td>'+
				'</tr>';
	return html;
}