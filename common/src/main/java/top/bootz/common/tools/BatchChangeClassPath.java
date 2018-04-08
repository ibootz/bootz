package top.bootz.common.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 批量更新指定目录下的class文件的package路径
 * @author John
 *
 */
public class BatchChangeClassPath {

	private static String root = "F:/GitRepo/bootz/common/src/main/java/top/bootz/common";

	private static String pathPattern = ".*src\\\\main\\\\java\\\\(.*)\\\\.*.java";

	public static void main(String[] args) {
		List<File> files = new ArrayList<>();
		listFiles(root, files);
		Pattern pattern = Pattern.compile(pathPattern);
		for (File file : files) {
			Matcher matcher = pattern.matcher(file.getAbsolutePath());
			String path = "";
			if (matcher.find()) {
				path = matcher.group(1);
			}
			path = path.replaceAll("\\\\", ".");
			try {
				replaceClasspath(file, path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void replaceClasspath(File file, String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuilder sb = new StringBuilder();
		for (String temp = null; (temp = br.readLine()) != null; temp = null) {
			if (temp.indexOf("package ") != -1) {
				temp = temp.replace(temp.substring(8), path);
				temp += ";";
			}
			sb.append(temp).append(System.getProperty("line.separator"));
		}
		br.close();
		PrintWriter pw = new PrintWriter(file.getAbsolutePath());
		pw.write(sb.toString());
		pw.flush();
		pw.close();
		System.out.println("file [" + file.getAbsolutePath() + "] 更新类路径成功！");
	}

	private static void listFiles(String rootPath, List<File> files) {
		File file = new File(rootPath);
		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				listFiles(subFile.getPath(), files);
			}
		} else if (file.getAbsolutePath().indexOf("BatchChangeClassPath") == -1) { // 排除当前类
			files.add(file);
		}
	}

}
