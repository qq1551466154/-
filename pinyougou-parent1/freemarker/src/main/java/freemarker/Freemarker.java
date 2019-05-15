package freemarker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Freemarker {

	public static void main(String[] args) throws IOException, TemplateException {
		//创建配置类
		Configuration configuration = new Configuration(Configuration.getVersion());
		//设置模板所在的目录
		configuration.setDirectoryForTemplateLoading(new File("C://Users//Session//eclipse-workspace//freemarker//src//main//resources//"));
		//设置字符集
		configuration.setDefaultEncoding("utf-8");
		//加载模板
		Template template = configuration.getTemplate("test.ftl");
		//创建数据类型
		Map map = new HashMap();
		map.put("name","张三");
		map.put("message","欢迎来到神奇的世界");
		map.put("success", true);
		List goodsList=new ArrayList();
		Map goods1=new HashMap();
		
		goods1.put("name", "苹果");
		goods1.put("price", 5.8);
		Map goods2=new HashMap();
		goods2.put("name", "香蕉");
		goods2.put("price", 2.5);
		Map goods3=new HashMap();
		goods3.put("name", "橘子");
		goods3.put("price", 3.2);
		goodsList.add(goods1);
		goodsList.add(goods2);
		goodsList.add(goods3);
		map.put("goodsList", goodsList);
		map.put("today",new Date());
		map.put("point", 11111111);
		//6.创建Writer对象
		Writer writer = new FileWriter(new File("D:\\upload\\test.html"));
		//7.输出
		template.process(map,writer);
		//关闭writer对象
		writer.close();
	}

}
