package com.pinyougou.shop.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import entity.Result;
import util.FastDFSClient;

@RestController
public class UploadController {
	//将配置文件中的值注入给变量
	@Value("${FILE_SERVER_URL}")
	private String file_server_url; 
	
	@RequestMapping("/upload")
	public Result upload(MultipartFile file) {
		String originalFileName = file.getOriginalFilename();	//获取文件名
		//得到扩展名
		String extendName = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
		
		try {
			util.FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");
			String fileId = client.uploadFile(file.getBytes(), extendName);
			String url = file_server_url + fileId;	//图片完整地址
			System.out.println(url);
			return new Result(true, url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "上传失败");
		}
	}
}
