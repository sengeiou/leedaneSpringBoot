var pageSize = 9;
var currentIndex = 0;
var materials;
var totalPage = 0;
var fileIndex = 0; //未上传文件的索引
var isUploading = false;//标记当前是否在上传
var $progressBar;
var $formUpload;
var $uploadImgModal;
var $materialListContainer;
var materialListImgHight;
$(function(){
	$materialListContainer = $("#material-row-container");
	$uploadImgModal = $("#upload-img-modal");
	var containerWidth = $materialListContainer.width();
	//动态计算模态框的宽度，适配手机
	if($(window).width() > (1100 + 30)){//浏览器当前窗口可视区域宽度
		$uploadImgModal.find(".modal-dialog").css("width", 1100);
		var materialListImgWidth = Math.floor((containerWidth / 3));
		materialListImgHight = materialListImgWidth - 30;
	}else{
		//为什么这里减去30是因为弹出的模态框离窗口有边距
		$uploadImgModal.find(".modal-dialog").css("width", $(window).width() -30);
		materialListImgHight = $(window).width() - 30;
	}
	
	$(document).on("click", "div.thumbnail", function(event){
		var $obj = $(this);
		if($obj.hasClass("click-select")){
			$obj.removeClass("click-select");
			$obj.find(".thumbnail-top").remove();
		}else{
			$obj.addClass("click-select");
			$obj.append('<div class="thumbnail-top"></div>');
		}
	});
	$progressBar = $(".progress-bar");
	$progressBar.closest(".progress").hide();
	$formUpload = $("#form-upload");
	$formUpload.hide();
	//alert($(document).width());//浏览器当前窗口文档对象宽度
	//alert($(document.body).width());//浏览器当前窗口文档body的宽度
	//alert($(document.body).outerWidth(true));//浏览器当前窗口文档body的总宽度 包括border padding margin
	
	getMaterials();
});

/**
 * 获取素材列表
 * @param bid
 */
function getMaterials(){
	var loadi = layer.load('努力加载中…'); //需关闭加载层时，执行layer.close(loadi)即可
	var params = {page_size: pageSize, current: currentIndex, total: totalPage, t: Math.random()};
	$.ajax({
		url : "/mt/materials?"+ jsonToGetRequestParams(params),
		dataType: 'json',
		beforeSend:function(){
			$materialListContainer.empty();
			$(".pagination").empty();
		},
		success : function(data) {
			layer.close(loadi);
			if(data.isSuccess){
				materials = data.message;
				if(materials.length == 0){
					$materialListContainer.append('空空的，还没有数据');
					return;
				}
				for(var i = 0; i < materials.length; i++){
					$materialListContainer.append(buildEachMaterialRow(i, materials[i]));
				}
				pageDivUtil(data.total);
			}else{
				ajaxError(data);
			}
			isLoad = false;
		},
		error : function(data) {
			isLoad = false;
			layer.close(loadi);
			ajaxError(data);
		}
	});
}

/**
 * 添加单个文件
 */
function addFile(){
	fileIndex ++;
	$("#add-file").remove();
	$formUpload.append('<input id="add-file-'+ fileIndex +'" type="file" name="myfile" onchange="haveFileInut(this);"/>');
	$('#add-file-'+ fileIndex +'').data("index", fileIndex);
	$('#add-file-'+ fileIndex +'').click();
}
/**
 * 添加单个图片
 */
function addImage(){
	$uploadImgModal.modal({backdrop: 'static', keyboard: false});
	$uploadImgModal.modal('show');
}

/**
 * 对文件选择后的监听
 * @param obj
 */
function haveFileInut(obj){
	var files = $(obj);
	var filePath = files.val();
	var file = files[0];
	//var fileName = file.val();
	var fileName=filePath.replace(/^.+?\\([^\\]+?)(\.[^\.\\]*?)?$/gi,"$1");  //正则表达式获取文件名，不带后缀
	var fileExt=filePath.replace(/.+\./,"");   //正则表达式获取后缀
	var fileFullName = fileName + "." + fileExt; //文件的全称，包括后缀
	
	$("#upload-material-table").append('<tr class="each-row" id="add-file-row-'+ fileIndex +'">'+ 
											'<td>'+ fileFullName +'</td>'+ 
											'<td onclick="addDesc(this);" onkeydown="saveDesc(event, this);"></td>'+
											'<td class="file-status">未上传</td>'+ 
											'<td><a href="javascript:void(0);" onclick="deleteFile(this);" style="margin-left: 10px;">移除</a></td>'+ 
										'</tr>');
	$("#add-file-row-"+ fileIndex).data("path", fileFullName);//把值保存在内存中
	$("#add-file-row-"+fileIndex).data("index", fileIndex);
	$("#add-file-row-"+fileIndex).data("length", file.files[0].size);
	//$("#add-file").remove();
}

var uuid;
var task;
var preAddFileIndex = 1;

/**
 * 全部上传
 */
function uploadFiles(){
	
	if(isUploading){
		layer.msg("当前有任务在上传，请稍等。。。");
		return;
	}
	
	if($formUpload.find("input").length < 1){
		layer.msg("请先添加文件");
		return;
	}
	
	$progressBar.closest(".progress").show();
	//加载层-风格4
	layer.msg('正在上传，请勿关闭窗口。', {
	  icon: 16
	  ,shade: 0.5, //弹出层背景的透明度
	  time: -1 //禁止手动关闭窗口
	});
	
	showProgressStyle(0);
	isUploading = true;
	var formData = new FormData(document.getElementById("form-upload"));//表单id
	task = setInterval("getProgress()","100");
	try{
		$.ajax({
		    url: '/wul/uploads',
		    type: 'POST',
		    cache: false,
		    data: formData,
		    processData: false,
		    contentType: false,
		    dataType: 'json', 
		}).success(function(data) {
			isUploading = false;
			clearInterval(task);
			layer.closeAll();
			if(data == null || data.isSuccess){
				var qiniuLinks = data.message;
				for(var i = 0; i < qiniuLinks.length; i++){
					var index = $formUpload.find("input").eq(i).data("index");
					if(index){
						var $row = $("#add-file-row-"+ index);
						$row.addClass("complete");
						$row.find(".file-status").text("已上传");
						//$row.find(".uploadFile").remove();
						$row.data("qiniu_path", qiniuLinks[i]);
					}
				}
				$formUpload.empty(); //把任务form清空
				$progressBar.closest(".progress").hide();
			}else{
				layer.msg(data.message);
			}
		}).error(function(data) {
			isUploading = false;
			clearInterval(task);
			if(data.isSuccess){
				layer.msg(data.message);
			}
			else{
				layer.msg("上传出现异常！");
			}
		});
	}catch(err){
		ajaxError(err);
	}
	
}

/**
 * 移除单个文件
 * @param obj
 */
function deleteFile(obj){
	layer.confirm('您要移除该文件记录吗？', {
		  btn: ['确定','点错了'] //按钮
	}, function(){
		$(obj).closest("tr").remove();
		layer.closeAll();
	}, function(){
	});
}

/**
 * 获取进度条
 */
function getProgress(){
	$.ajax({
		url : "/wul/getProgress",
		dataType: 'json', 
		beforeSend:function(){
		},
		success : function(data) {
			if(data != null && data.isSuccess){
				var status = data.message;
				var pro = Math.round(status.bytesRead / status.contentLength *100 );
				if(pro == 100){
					clearInterval(task);
				}
				//var items = status.items;
				showProgressStyle(pro);
				console.log("当前的进度是："+ pro);
			}
		},
		error : function(data) {
			isUploading = false;
			ajaxError(data);
		}
	});
}

/**
 * 控制进度的展示
 * @param progress 进度
 */
function showProgressStyle(progress){
	$progressBar.css("width", progress + "%");
	$progressBar.find(".p-show").text(progress +"% 已上传完成");
}


/**
 * 添加描述信息
 * @param obj
 */
function addDesc(obj){
	var $tr = $(obj).closest("tr");
	var desc;
	if($(obj).find("input") && $(obj).find("input").length > 0){
		desc = $(obj).find("input").val();
	}else{
		desc = $tr.data("material_desc");
	}
	
	$(obj).text('');
	$(obj).append('<input type="text" class="form-control" placeholder="回车保存描述"/>');
	$(obj).find("input").focus();
	if(isNotEmpty(desc))
		$(obj).find("input").val(desc);
}

/**
 * 保存描述信息
 * @param obj
 */
function saveDesc(event, obj){
	var e = event || window.event || arguments.callee.caller.arguments[0];
    if(e && e.keyCode==13){ // enter 键
    	var text = $(obj).find("input").val();
    	$(obj).find("input").remove();
    	$(obj).text(text);
    	$(obj).closest("tr").data("material_desc", text);
   }
}

/**
 * 构建每一行消息html
 * @param index
 * @param material
 * @returns {String}
 */
function buildEachMaterialRow(index, material){
		var html = '<div class="col-lg-4 col-md-4 col-sm-12 col-xs-12">'+
						'<div class="thumbnail">'+
						      '<img width="100%" style="height: '+materialListImgHight+'px;" src="'+ material.qiniu_path +'" alt="...">'+
						      '<div class="caption">'+
						        	'<div class="cut-text">'+ changeNotNullString(material.create_time) +'</div>';
			        	if(isEmpty(material.material_desc)){
			        		html += '<h5 class="cut-text">暂无描述</h5>';   	
			        	}else{
			        		html += '<h5 class="cut-text" title="'+ material.material_desc  +'">'+ material.material_desc +'</h5>';
			        	}
						
			        		html += '<p><a href="javascript:void(0);" class="btn btn-primary btn-sm" role="button" onclick="editMaterial(event, '+material.id+');">编辑</a> <a href="javascript:void(0);" class="btn btn-default btn-sm" role="button" onclick="deleteMaterial(event, '+material.id+');">删除</a></p>'+
							'</div>'+
					    '</div>'+
				   '</div>';
	
	return html;
}

/**
 * 删除素材
 * @param event
 * @param materialId
 */
function deleteMaterial(event, materialId){
	event.stopPropagation();//阻止冒泡 
	if(materialId > 0){
		layer.confirm('您要删除该素材吗？', {
			  btn: ['确定','点错了'] //按钮
		}, function(){
			var loadi = layer.load('努力加载中…'); //需关闭加载层时，执行layer.close(loadi)即可
			$.ajax({
				type : "delete",
				dataType: 'json',  
				url : "/mt/material/"+ materialId,
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
}

/**
 * 编辑素材
 * @param event
 * @param materialId
 */
function editMaterial(event, materialId){
	event.stopPropagation();//阻止冒泡 
}

/**
 * 生成分页div
 * @param total
 */
function pageDivUtil(total){
	var html = '<li>'+
					'<a href="javascript:void(0);" onclick="pre();" aria-label="Previous">'+
						'<span aria-hidden="true">&laquo;</span>'+
					'</a>'+
				'</li>';
	totalPage = parseInt(Math.ceil(total / pageSize));
	var start = 0;
	var end = totalPage > start + 10 ? start + 10: totalPage;
	
	var selectHtml = '<li><select class="form-control" onchange="optionChange()">';
	for(var i = 0; i < totalPage; i++){
		if(currentIndex == i)
			selectHtml += '<option name="pageIndex" selected="selected" value="'+ i +'">'+ (i + 1) +'</option>';
		else
			selectHtml += '<option name="pageIndex" value="'+ i +'">'+ (i + 1) +'</option>';
	}
	
	for(var i = start; i < end; i++){
		if(currentIndex == i)
			html += '<li class="active"><a href="javascript:void(0);" onclick="goIndex('+ i +');">'+ (i+1) +'</a></li>';
		else
			html += '<li><a href="javascript:void(0);" onclick="goIndex('+ i +');">'+ (i+1) +'</a></li>';
	}
	html += '<li>'+
				'<a href="javascript:void(0);" onclick="next();" aria-label="Next">'+
					'<span aria-hidden="true">&raquo;</span>'+
				'</a>'+
			'</li>';
	
	selectHtml += '</select></li>';
	
	html += selectHtml;
	$(".pagination").html(html);
}

/**
 * 选择改变的监听
 */
function optionChange(){
	var objS = document.getElementsByTagName("select")[0];
    var index = objS.options[objS.selectedIndex].value;
    currentIndex = index;
    getMaterials();
}

/**
 * 点击向左的按钮
 */
function goIndex(index){
	currentIndex = index;
	getMaterials();
}

/**
 * 点击向左的按钮
 */
function pre(){
	currentIndex --;
	if(currentIndex < 0)
		currentIndex = 0;
	getMaterials();
}


/**
 * 点击向右的按钮
 */
function next(){
	currentIndex ++;
	if(currentIndex > totalPage)
		currentIndex = totalPage;
	getMaterials();
}

/**
 * 获取子页面的getdata()
 */
function getData(){
	var formData;
	try{
		formData = document.getElementById("add-photo-iframe").contentWindow.getData();
	}catch(err){
		//在这里处理错误
		layer.msg("请选择图片："+err.message);
		return;
	 }
	
	var loadi = layer.load('努力加载中…'); //需关闭加载层时，执行layer.close(loadi)即可
	$.ajax({
	    url: '/wul/upload/imgage',
	    type: 'POST',
	    cache: false,
	    data: formData,
	    processData: false,
	    contentType: false,
	    dataType: 'json', 
	}).success(function(data) {
		layer.close(loadi);//关闭
		
		if(data != null && data.isSuccess){
			fileIndex++; //新增1
			var fileName = formData.get("file-name");
			var qiniuPath = data.message;//获取新的链接
			$("#upload-material-table").append('<tr class="complete each-row" id="add-file-row-'+ fileIndex +'">'+ 
					'<td>'+ fileName +'</td>'+ 
					'<td onclick="addDesc(this);" onkeydown="saveDesc(event, this);"></td>'+
					'<td class="file-status">已上传</td>'+ 
					'<td><a href="javascript:void(0);" onclick="deleteFile(this);" style="margin-left: 10px;">移除</a></td>'+ 
				'</tr>');
			$("#add-file-row-"+ fileIndex).data("path", fileName);//把值保存在内存中
			$("#add-file-row-"+ fileIndex).data("qiniu_path", qiniuPath);//把值保存在内存中
			$("#add-file-row-"+ fileIndex).data("index", fileIndex);//把值保存在内存中
			$("#add-file-row-"+ fileIndex).data("length", formData.get("file-size"));//把值保存在内存中
			$('#upload-img-modal').modal('hide');
		}else
			ajaxError(data);
	}).error(function(res) {
		layer.close(loadi);
		layer.msg(res);
	});
}

/**
 * 提交所有的已经上传的文件
 */
function submitFiles(){
	//找到两种都满足的tr
	var $trs = $("#upload-material-table").find(".each-row.complete");
	if($trs && $trs.length < 1){
		layer.msg("请先上传文件！");
		return;
	}

	var materials = new Array();
	var flag = false; //标记是否符合要求
	$trs.each(function(index){
		//没有七牛云存储路径的不让提交
		if(isEmpty($(this).data("qiniu_path")))
			flag = true;
		
		materials.push($(this).data());
	});
	
	if(flag){
		layer.msg("无法获取服务器文件路径，请刷新重试！");
		return;
	}
	
	$.ajax({
		type: 'post',
		url : "/mt/material",
		data: {"materials": JSON.stringify(materials)},
		dataType: 'json', 
		beforeSend:function(){
		},
		success : function(data) {
			if(data != null && data.isSuccess){
				layer.msg(data.message +",1秒钟后自动刷新");
				setTimeout("window.location.reload();", 1000);
			}else{
				ajaxError(data);
			}
		},
		error : function(data) {
			ajaxError(data);
		}
	});
}