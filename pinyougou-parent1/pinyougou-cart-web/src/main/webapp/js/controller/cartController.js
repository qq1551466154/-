app.controller('cartController',function($scope,cartService){
	$scope.findCartList=function(){
		
		cartService.findCartList().success(
			function(response){
				
				$scope.cartList=response;
				
				$scope.totalValue=cartService.sum($scope.cartList);//求合计数
				
			}	
		);
	}
	
	$scope.addGoodsToCartList=function(itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(
				function(response){
					if(response.success){
						$scope.findCartList();//刷新列表
					}else{
						alert(response.message);//弹出错误提示
					}
					
				}
		);
	}
	
	$scope.findAddressList=function(){
		cartService.findAddressList().success(
				function(response){
					$scope.addressList=response;
					for(var i=0;$scope.addressList.length;i++){
						if($scope.addressList[i].isDefault=='1'){
							$scope.address=$scope.addressList[i];
							break;
						}
					}
				}
		);
	}
	
	//选择地址
	$scope.selectAddress=function(address){
		$scope.address=address;
	}
	
	//判断是否是当前选中的地址
	$scope.isSelectedAddress=function(address){
		
		if(address==$scope.address){
			return true;
		}else{
			return false;
		}
	}
	
	$scope.order={patmentType:'1'};
	
	//选择支付方式
	$scope.selectPayType=function(type){
		$scope.order.paymentType=type;
	}
	
	$scope.submitOrder=function(){
		$scope.order.receiverAreaName=$scope.address.address;//地址
		$scope.order.receiverMobile=$scope.address.mobile;//手机
		$scope.order.receiver=$scope.address.contact;//联系人
		
		cartService.submitOrder($scope.order).success(
				function(response){
					if(response.success){
						//页面跳转
						if($scope.order.paymentType=='1'){
							location.href="pay.html";//如果是微信支付，跳转到支付页面
						}else{
							location.href="paysuccess.html";//如果是活到付款，跳转到提示页面
						}
					}else{
						alert(response.message);//也可以跳转到提示页面 
					}
				}
		);
	}
	
	
	
});