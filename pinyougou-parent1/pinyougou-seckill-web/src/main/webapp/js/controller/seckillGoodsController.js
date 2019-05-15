app.controller('seckillGoodsController',function($scope,seckillGoodsService,$location,$interval){
	$scope.findList=function(){
		seckillGoodsService.findList().success(
				function(response){
					$scope.list = response;
					
				}
		);
	}
	
	$scope.findOne=function(){
		var id = $location.search()['id']
		seckillGoodsService.findOneFromRedis(id).success(
				function(response){
					$scope.entity=response;
					allsecond =Math.floor((new Date($scope.entity.endTime).getTime()-(new Date().getTime()))/1000);
					
					time=$interval(function(){
						if(allsecond>0){
							allsecond=allsecond-1;
							$scope.timeString = convertTimeString(allsecond);
							
						}else{
							$interval.cancel(time);
							
							
						}
					},1000);
				}
		);
	}
	
	convertTimeString = function(allsecond){
		var days = Math.floor(allsecond/(60*60*24));//天数
		var hours = Math.floor((allsecond-days*60*60*24)/(60*60));//小时数
		var minutes = Math.floor((allsecond-days*60*60*24-hours*60*60)/60);// 分钟数 
		var seconds=  allsecond-days*60*60*24-hours*60*60-minutes*60;//秒数
		var timeString = ""
		if(days>0){
			timeString=days+"天";
		}
	
		
		
		return timeString+hours+":"+minutes+":"+seconds;
	}

	
	$scope.submitOrder=function(){
		seckillGoodsService.submitOrder($scope.entity.id).success(
				function(response){
					if(response.success){
						alert("下单成功，请在 1 分钟内完成支付");
						location.href="pay.html";
					}else{
						alert(response.message);
					}
				}
		);
	}
	
});
