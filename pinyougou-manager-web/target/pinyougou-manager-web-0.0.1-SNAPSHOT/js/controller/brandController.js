app.controller('brandController',function($scope,$http,$controller,brandService){
    		//继承 	{$scope:$scope}把父类中的scope拿到该类中，让其是通用的
			//采用这种办法实现继承
			$controller('baseController',{$scope:$scope});
			
			//查询品牌列表
    		$scope.findAll = function(){
    			brandService.findAll().success(
    				function(response){
    					$scope.list = response;
    				}
    			);
    		}
    		
    		//请求分页数据 这个东西貌似没有用了
    		$scope.findPage = function(page,size){
    			brandService.findPage(page,size).success(
    				function(response){
    					$scope.list = response.rows;	//显示当前页的数据
    					//alert($scope.list);
    					$scope.paginationConf.totalItems = response.total;	//更新总记录数
    				}
    			);
    		}
    		
    		//既新增 又修改
    		$scope.save = function(){
    			var object = null;
    			if($scope.entity.id != null){
    				//id不为null  那肯定就是修改
    				object = brandService.update($scope.entity);
    			}
    			else{
    				object = brandService.add($scope.entity);
    			}
    			object.success(
    				function(response){
    					if(response.success){
    						$scope.reloadList();//刷新
    					}
    					else{
    						alert(response.message);
    					}
    				}
    			);
    		}
    		
    		//根据id查询一个实体
    		$scope.findOne = function(id){
    			brandService.findOne(id).success(
    				function(response){
    					$scope.entity = response;
    				}
    			);
    		}
    		
    		
    		//删除
    		$scope.del = function(){
    			brandService.del($scope.selectIds).success(
    				function(response){
    					if(response.success){
    						$scope.reloadList();
    					}
    					else{
    						alert(response.message);
    					}
    				}
    			);
    		}
    		
    		//条件查询
    		$scope.searchEntity = {};
    		$scope.search = function(page,size){
    			brandService.search(page,size,$scope.searchEntity).success(
    				function(response){
    					//alert(response.rows.length);
    					$scope.list = response.rows;	//显示当前页的数据，一个封装了结果集的列表
    					//alert($scope.list);
    					$scope.paginationConf.totalItems = response.total;	//更新总记录数
    				}
    			);
    		}
});