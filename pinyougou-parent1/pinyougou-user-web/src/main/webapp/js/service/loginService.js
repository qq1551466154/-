app.service('loginService',function($http){
	//读取列表数据到表单中
	this.showName=function(){
		return $http.get('../login/name.do');
	}
});