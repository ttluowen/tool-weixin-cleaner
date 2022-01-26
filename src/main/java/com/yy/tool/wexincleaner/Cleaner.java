package com.yy.tool.wexincleaner;

import com.yy.java.config.JavaConfig;
import com.yy.log.Logger;
import com.yy.util.FileUtil;
import com.yy.util.PropertyUtil;
import com.yy.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Luowen
 * @date 2022-01-26
 */
public class Cleaner {

	private String root;
	private List<String> exts;
	private List<String> weixinIds;


	public static void main(String[] args) {

		JavaConfig.javaInit();
		new Cleaner().start();
	}


	public void start() {

		init();
		clean(new File(root));
		mergeImage2();
	}


	private void init() {

		Properties config = PropertyUtil.read(JavaConfig.getConfigPath() + "config.properties");
		root = JavaConfig.formatDirRelativePath(config.getProperty("root"));

		exts = new ArrayList<>();
		for (String item : config.getProperty("exts").trim().split(",")) {
			String ext = item.trim().toLowerCase();
			if (!StringUtil.isEmpty(ext)) {
				exts.add(ext);
			}
		}

		weixinIds = new ArrayList<>();
		for (String item : config.getProperty("weixinIds").trim().split(",")) {
			String id = item.trim();
			if (!StringUtil.isEmpty(id)) {
				weixinIds.add(id);
			}
		}

		Logger.log("root: " + root);
		Logger.log("exts: " + exts);
		Logger.log("weixinIds: " + weixinIds);
	}


	private void clean(File file) {

		if (file.isDirectory()) {
			for (File item : file.listFiles()) {
				clean(item);
			}

			if (file.listFiles().length == 0) {
				Logger.log("删除空文件夹 " + file);
				FileUtil.deleteDir(file);
			}
		} else {
			String filename = file.getName();
			String ext = "";
			int lastPoint = filename.lastIndexOf(".");
			if (lastPoint != -1) {
				ext = filename.substring(lastPoint + 1).toLowerCase();
			}

			if (StringUtil.isEmpty(ext) || exts.indexOf(ext) == -1) {
				Logger.log("删除文件 " + file);
				FileUtil.delete(file);
			}
		}
	}


	private void mergeImage2() {

		for (String id : weixinIds) {
			File dir = new File(root + id + "\\image2\\");
			File[] files = dir.listFiles();

			if (files != null) {
				for (File file : files) {
					mergeImage2(file, dir);
				}
			}
		}
	}


	private void mergeImage2(File file, File moveTo) {

		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File item : files) {
					mergeImage2(item, moveTo);
				}
			}

			if (files.length == 0) {
				Logger.log("删除文件夹 " + file);
				FileUtil.deleteDir(file);
			}
		} else {
			try {
				String filename = file.getName();
				File to = new File(moveTo, filename);

				if (file.getAbsolutePath().contentEquals(to.getAbsolutePath())) {
					Logger.log(file + " 移动的目标位置与原始位置相同，不需要操作");
					return;
				}

				Logger.log("移动 " + file + " 到 " + to);
				FileUtil.copy(file, to);
				FileUtil.delete(file);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	}
}