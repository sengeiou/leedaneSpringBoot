layui.use(['layer'], function(){
   layer = layui.layer;

    $("#help").on("click", function(){
		layer.msg("暂无帮助信息，请稍后重试！");
	});

	$("#write").on("click", function(){
        window.open("/ev/manage", "_self");
    });

    var loadi = layer.load('努力加载中…'); //需关闭加载层时，执行layer.close(loadi)即可
    $.ajax({
        url : "/ev/all",
        dataType: 'json',
        beforeSend:function(){
            $(".layui-timeline").empty();
        },
        success : function(data) {
            layer.close(loadi);
            var events = data.message;
            if(data != null && data.isSuccess){
                if(events.length == 0){
                    $(".layui-timeline").append("<li>还没有发布任何大事件</li>");
                    return;
                }
                for(var i = 0; i < events.length; i++){
                    var event = events[i];
                    var html = '<li class="layui-timeline-item">'+
                                    '<i class="layui-icon layui-timeline-axis"></i>'+
                                    '<div class="layui-timeline-content layui-text">'+
                                        '<h3 class="layui-timeline-title">'+ setTimeAgo(event.modify_time) +'</h3>';
                            if(!isEmpty(event.content)){
                                html += '<p>'+ event.content +'</p>';
                            }
                            html +=  '</div>'+
                                '</li>';
                    $(".layui-timeline").append(html);
                }
            }else{
                ajaxError(data);
            }
        },
        error : function(data) {
            layer.close(loadi);
            ajaxError(data);
        }
    });
});