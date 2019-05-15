app.controller('searchController',function($scope,$location,searchService){
	
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sortFiled':'','sort':''};//搜索对象
	//搜索
	$scope.search=function(){
		$scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
		searchService.search($scope.searchMap).success(
			function(response){
				$scope.resultMap=response;	
				buildPageLabel();
			}
		);		
	}
	//添加搜索选项
	$scope.addSearchItem=function(key,value){
		if(key=='category' || key=='brand' || key=='price'){//如果点击的是分类或者是品牌
			$scope.searchMap[key]=value;
		}else{
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//执行搜索
	}
	
	//移除搜索选项
	$scope.removeSearchItem=function(key){
		if(key=='category' || key=='brand' || key=='price'){//如果点击的是分类或者是品牌
			$scope.searchMap[key]="";
		}else{
		delete $scope.searchMap.spec[key];//移除此属性
		}
		$scope.search();//执行搜索
	}
	
	//构建分页标签（totalpages为总页数）
	buildPageLabel=function(){
		
		$scope.pageLabel=[];//新增分页栏数据
		var maxPageNo  =$scope.resultMap.totalPages;//得到最后页码
		var firstPage=1;//开始页码
		var lastPage = maxPageNo;//截至页码
		$scope.firstDot=true;//前面有点
		$scope.lastDot=true;//后面有点
		if($scope.resultMap.totalPages>5){
			if($scope.searchMap.pageNo<=3){
				lastPage=5
				$scope.firstDot=false;
			}else if($scope.resultMap.pageNo>=lastPage-2){
				firstPage = maxPageNo-4;//后5页
				
				$scope.lastDot=false;
			}else{
				firstPage = $scope.resultMap.pageNo-2;
				lastPage = $scope.resultMap.pageNo+2;
			}
			
		}else{
			$scope.firstDot=false;
			$scope.lastDot=false;
		}
		//构建页码
		for(var i =firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);//新增分页栏数据
		}
	}
	
	//根据页码查询
	$scope.queryByPage=function(pageNo){
		//页码验证
		if(pageNo<1||pageNo>$scope.resultMap.totalPages){
			return;
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();
	}
	
	//判断当前页码为第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	
	//判断当前页是否为最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true
		}else{
			return false;
		}
	}
	
	//设置排序规则
	$scope.sortSearch=function(sortFiled,sort){
		$scope.searchMap.sortFiled=sortFiled;
		$scope.searchMap.sort=sort;
		$scope.search();
	}
	
	//判断关键字是不是品牌
	$scope.keywordsIsBrand=function(){
		for(var i = 0;i<$scope.resultMap.brandList.length;i++){
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>0){
				//如果包含
				return true;
			}
		}
		return false;
	}
	
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords=$location.search()['keywords'];
		$scope.search();
	}
	
});