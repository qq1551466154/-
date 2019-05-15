package cn.itcast.demo;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

/**
 * fastDFS小demo
 * @author Session
 *
 */
public class FastDFS {
	public static void main(String[] args) throws Exception {
		//1.加载配置文件，配置文件中的内容就是tracker服务的地址
		ClientGlobal.init("C:\\Users\\Session\\eclipse-workspace\\fashDFS-demo\\src\\main\\resources\\fdfs_client.conf");
		//2.创建一个trackerClient对象，直接new一个
		TrackerClient client = new TrackerClient();
		//3.使用TrackerClient对象创建连接，获得一个TrackerClient对象
		TrackerServer trackerServer = client.getConnection();
		//4.创建一个StorageServer的引用，值为null
		StorageServer storageServer = null;
		
		//5.创建一个StorageClient对象，需要两个参数TrackerServer 对象、StorageServer的引用
		StorageClient storageClient = new StorageClient(trackerServer,storageServer);
		//6.使用storageClient对象上传图片
		//扩展名不带“."
		String[] strings = storageClient.upload_file("E:\\photoes\\timg.jpg", "jpg",null);
		
		//7.返回数组。包含组名和图片的路径
	for(String string:strings) {
		System.out.println(string);
	}
		
	}
	
}
