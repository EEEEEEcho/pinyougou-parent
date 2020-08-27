app.controller('baseController',function($scope){
	
	//分页控件配置
	//currentPage:当前页码,totalItems:总记录数
	//itemsPerPage:每页记录数,perPageOptions:分页选项
	//onChange:当页码变更后自动触发的方法。
	$scope.paginationConf = {
		currentPage : 1,
		totalItems : 10,
		itemsPerPage : 10,
		perPageOptions:[10,20,30,40,50],
		onChange:function(){
			$scope.reloadList();
		}
	}
	
	//刷新列表
	$scope.reloadList = function(){
		$scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
	}
	
	//用户勾选的ID集合
	$scope.selectIds=[];
	$scope.updateSelection = function($event,id){
		//通过$event获取事件源，如果被勾选才触发
		if($event.target.checked){
			$scope.selectIds.push(id);	//push方法向集合中添加元素
		}
		else{
			//查找值的位置
			var index = $scope.selectIds.indexOf(id);
			//参数1：移除的位置，参数2：移除的个数
			$scope.selectIds.splice(index,1);
		}
	}
	
	$scope.jsonToString = function(jsonString,key){
		var json = JSON.parse(jsonString);
		var value = "";
		for(var i=0; i < json.length;i ++){
			if(i > 0){
				value +=",";
			}
			value +=json[i][key];
		}
		return value;
	}
});