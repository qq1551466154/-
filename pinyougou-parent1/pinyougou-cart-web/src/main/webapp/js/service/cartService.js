//购物车服务层
app.service('cartService',function($http){
	this.findCartList=function(){
		return $http.get('cart/findCartList.do');
	}
	
	this.addGoodsToCartList=function(itemId,num){
		return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
	}
	
	//求合计数
	this.sum=function(cartList){
		var totalValue={totalNum:0,totalMoney:0 };
		
		for(var i=0;i<cartList.length ;i++){
			var cart=cartList[i];//购物车对象
			
			for(var j=0;j<cart.orderItemsList.length;j++){
				
				var orderItem=  cart.orderItemsList[j];//购物车明细
				totalValue.totalNum+=orderItem.num;//累加数量
				totalValue.totalMoney+=orderItem.totalFee;//累加金额				
			}			
		}
		return totalValue;
		
	}
	
	this.findAddressList=function(){
		return $http.get('address/findListByLoginUser.do');
	}
	//保存订单
	this.submitOrder=function(order){
		return $http.post('order/add.do',order);
	}
	this.createNative=function(){
		return $http.get('pay/createNative.do');
	}
	
	this.queryPayStatus=function(out_trade_no){
		return $http.get('pay/queryPayStatus.do?out_trade_no='+out_trade_no);
	}
});