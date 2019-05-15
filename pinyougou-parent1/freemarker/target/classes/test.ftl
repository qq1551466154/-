<html>
<head>
	<meta charset="utf-8">
	<title>freemarker小demo</title>
</head>
<#-- 注释-->
<#include "head.ftl">
${name},你好${message}
<#assign linkman="张小姐">
联系人：${linkman}<br>
<#assign info={"mobile":"1232143124","address":"湖南省长沙市"}>
电话：${info.mobile} 地址：${info.address}
<#if success=true>
你已通过实名认证
<#else>
你未通过实名认证
</#if>
<br>
<#list goodsList as goods>
${goods_index+1} 商品名称： ${goods.name} 价格：${goods.price}<br>
</#list>

${goodsList?size}

<#assign text="{'bank':'工商银行','account':'12312313312'}"/>
<#assign data=text?eval/>
开户行：${data.bank} 账户：${data.account}<br>


当前日期：${today?date}<br>
当前时间：${today?time}<br>
当前日期+时间：${today?datetime}<br>
日期格式化：${today?string("yyyy年MM月")}<br>

累计积分：${point}<br>

<#if aaa??>

	aaa变量存在
	
<#else>
	
	aaa变量不存在
	
</#if>
<br>
${aaa!"-"}

</html>