<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/taglibs.jsp"%>
<link rel="stylesheet" href="<c:url value="/css/zTreeStyle/zTreeStyle.css" />" type="text/css">
<script type="text/javascript" src="<c:url value="/js/jquery.ztree.core-3.5.min.js" />"></script>

<style>
    .mytable {align:center;border-collapse:collapse;border:solid #6AA70B;border-width:0px 0 0 0px;width:600;}
    .mytable table tr {list-style:none; border-bottom:#6AA70B 1px dotted ;font-size: 12px;;height:20px;}
    .mytable table tr.t1 {background-color:#EEEEEE;}/* 第一行的背景色 */
    .mytable table tr.t2{background-color:#;}/* 第二行的背景色 */
    .mytable table tr.t3 {background-color:#CCCCCC;}/* 鼠标经过时的背景色 */
</style>

<style type="text/css">
    a:link { text-decoration: none}
    a:active { text-decoration:none}
    a:hover { text-decoration:none}
    a:visited { text-decoration:none}
</style>
<h1>管理员-ZooKeeper 集群操作</h1>

<select id="clusterSelector" onchange="javascript:location.href=this.value;" >
    <c:forEach var="zooKeeperCluster" items="${zooKeeperClusterMap}">
        <c:choose>
            <c:when test="${ zooKeeperCluster.key eq clusterId }"><option value="manager.do?clusterId=${zooKeeperCluster.key}"  selected>${zooKeeperCluster.value.clusterName}</option></c:when>
            <c:otherwise><option value="manager.do?clusterId=${zooKeeperCluster.key}">${zooKeeperCluster.value.clusterName}</option></c:otherwise>
        </c:choose>
    </c:forEach>
</select>
<div align="center" class="mytable" id="tab">
    <div style="float: left">
        <ul id="treeBox" class="ztree"></ul>
    </div>
    <div id="valueNodeBox" title="test" path="" class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-dialog-buttons ui-draggable ui-resizable">
    </div>
    <div id="editNodeBox" title="" class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-dialog-buttons ui-draggable ui-resizable">
        <label for="name" style="display:block">value:</label>
        <textarea rows="10" cols="60"></textarea>
        <p style="color:#ff4b43" class="editError"></p>
    </div>
    <div id="createNodeBox" title="" class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-dialog-buttons ui-draggable ui-resizable">
        <label for="name" style="display:block">Name:</label>
        <input type="text"  style="display:block;margin-bottom:12px; width:95%; padding: .4em;" name="name" class="text ui-widget-content ui-corner-all">
        <label for="name" style="display:block">Data:</label>
        <textarea rows="10" cols="60" style="margin-bottom:12px; width:95%; padding: .4em;"></textarea>
        <p style="color:#ff4b43" class="createTips"></p>
    </div>
    <div id="deleteNodeBox" class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-dialog-buttons ui-draggable ui-resizable">
        <p class="deleteInfo"></p>
        <p style="color:#ff4b43" class="createTips"></p>
    </div>

</div>



<script type="text/javascript">
    //全局clusterId
    var clusterId = '${clusterId}';

    var setting = {
        async: {
            enable: true,
            url:"<c:url value="manager/json/childKeeperNodes.do?clusterId=${clusterId}"/>",
            autoParam:["id=path"],
            dataFilter: filter,
            type:"get"
        },
        data: {
            simpleData: {
                enable: true
            }
        },
        callback: {
            onClick: znodeClick
        }
    };

    function znodeClick(event, treeId, treeNode, msg) {
        console.log("name=" + treeNode.name +"\ndataURL"+ treeNode.dataURL);
        $.getJSON(treeNode.dataURL,function(result){
            if(result){
                var valueNodeBox = $('#valueNodeBox');
                valueNodeBox.html(result['data']);
                valueNodeBox.dialog({'title':"path:" + result['path']});
                valueNodeBox.dialog("open" );
                valueNodeBox.attr("path",result['path']);

                $('#editNodeBox textarea').val(result['data']);
            }else{
                alert('出错啦,悲剧~~');
            }
        });
    }

    function filter(treeId, parentNode, childNodes) {
        if (!childNodes) return null;
        for (var i=0, l=childNodes.length; i<l; i++) {
            childNodes[i].name = childNodes[i].name.replace(/\.n/g, '.');
        }
        return childNodes;
    }

    var zNodes =[];
        <c:if test="${keeperNode != null}">
        zNodes.push({ id:"${keeperNode.id}", pId:"${keeperNode.pid}", name:"${keeperNode.name}",open:true,dataURL:"manager/json/getKeeperNodeData.do?clusterId=${clusterId}&path=${keeperNode.path}"});
        <c:if test="${keeperNode.childList != null}">
            <c:forEach var="node" items="${keeperNode.childList}">
                zNodes.push({ id: "${node.id}", pId: "${node.pid}", name: "${node.name}", dataURL: "manager/json/getKeeperNodeData.do?clusterId=${clusterId}&path=${node.path}", isParent: ${node.hasChild}});
            </c:forEach>
        </c:if>
    </c:if>

    $(document).ready(function(){
        // zTree init
        $.fn.zTree.init($("#treeBox"), setting, zNodes);

        // valueNodeBox dialog init
        $("#valueNodeBox").dialog({
            autoOpen: false,
            minWidth:550,
            minHeight:350,
            position: 'center',
            buttons: {
                "Create znode": function() {
                    closeAllBox();
                    $("#createNodeBox").dialog({'title':"Parent znode path" + $('#valueNodeBox').attr('path')});
                    $("#createNodeBox").dialog("open");
                },
                "Edit znode": function() {
                    closeAllBox();
                    $("#editNodeBox").dialog({'title':"znode path" + $('#valueNodeBox').attr('path')});
                    $("#editNodeBox").dialog("open");
                },
                "Delete znode": function() {
                    closeAllBox();
                    $("#deleteNodeBox .deleteInfo").html("确认删除:" + $("#valueNodeBox").attr("path") + " ?");
                    $("#deleteNodeBox").dialog("open");
                }
            }
        });
        function closeAllBox(){
            $("#valueNodeBox").dialog("close");
            $("#editNodeBox").dialog("close");
            $("#deleteNodeBox").dialog("close");
            $("#createNodeBox").dialog("close");
        }

        //editNodeBox dialog init
        $("#editNodeBox").dialog({
            autoOpen: false,
            minWidth:550,
            minHeight:350,
            position: 'center',
            buttons: {
                "Submit edit": function() {
                    var editTextArea = $("#editNodeBox textarea");
                    var value = editTextArea.val();
                    var path =  $('#valueNodeBox').attr('path');
                    $.getJSON('manager/json/updateKeeperNodeData.do',{
                        clusterId:clusterId ,
                        path:path ,
                        value:value
                    },function(result){
                        if(result && result['success']){
                            alert("修改成功");
                            closeAllBox();
                        }
                    });
                },
                "Cancel edit": function() {
                    closeAllBox();
                    $("#valueNodeBox").dialog("open");
                }
            }
        });

        $("#deleteNodeBox").dialog({
            autoOpen: false,
            position: 'top',
            minWidth:450,
            minHeight:150,
            modal: true,
            buttons: {
                "Delete znode": function() {
                    $(this).dialog( "close" );
                    var path =  $('#valueNodeBox').attr('path');
                    $.getJSON('manager/json/deleteKeeperNode.do',{
                        clusterId:clusterId ,
                        path:path
                    },function(result){
                        if(result){
                            if(result['success']){
                                alert("删除成功");
                                var zTree = $.fn.zTree.getZTreeObj("treeBox");
                                var nodes = zTree.getSelectedNodes();
                                var treeNode = nodes[0];
                                if (nodes.length == 0) {
                                    return;
                                }
                                var callbackFlag = $("#callbackTrigger").attr("checked");
                                zTree.removeNode(treeNode, callbackFlag);

                                closeAllBox();
                            }else{
                                $("#deleteNodeBox .createTips").html(result['error']);
                            }
                        }else{
                            $("#deleteNodeBox .createTips").html("出错啦,悲剧~");
                        }
                    });
                },
                "Cancel delete": function() {
                    $(this).dialog( "close" );
                    $("#valueNodeBox").dialog("open");
                }
            }
        });

        $("#createNodeBox").dialog({
            autoOpen: false,
            minWidth:450,
            minHeight:300,
            position: 'center',
            buttons: {
                "Submit create": function() {
                    var inputNode = $("#createNodeBox input");
                    var name = inputNode.val();
                    var createTipNode = $("#createNodeBox .createTips");
                    var valueNode = $("#createNodeBox textarea");
                    var value = valueNode.val();
                    if($.trim(name) == ''){
                        createTipNode.html("节点名称不能为空");
                        return;
                    }else{
                        createTipNode.empty();
                    }
                    var path =  $('#valueNodeBox').attr('path');
                    $.getJSON('manager/json/createKeeperNode.do',{
                        clusterId:clusterId ,
                        path:path ,
                        name:name ,
                        value:value
                    },function(result){
                        if(result ){
                            if(result['success']){
                                alert("添加成功");
                                var newKeeperNode = result["data"];
                                var zTree = $.fn.zTree.getZTreeObj("treeBox");
                                var nodes = zTree.getSelectedNodes();
                                var treeNode = nodes[0];
                                if (treeNode) {
                                    treeNode = zTree.addNodes(treeNode, newKeeperNode);
                                } else {
                                    treeNode = zTree.addNodes(null, newKeeperNode);
                                }
                                closeAllBox();
                            }else{
                                createTipNode.html(result["error"]);
                            }
                        }else{
                            createTipNode.html("出错啦,悲剧~");
                        }

                    });
                },
                "Cancel create": function() {
                    closeAllBox();
                    $("#valueNodeBox").dialog("open");
                }
            }
        });
    });
</script>



