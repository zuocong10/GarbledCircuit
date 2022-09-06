package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class WRFile {
	public static String readTxt(String txtPath) {
		File file = new File(txtPath);
		if (file.isFile() && file.exists()) {
			try {
				FileInputStream fileInputStream = new FileInputStream(file);
				InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

				StringBuffer sb = new StringBuffer();
				String text = null;
				while ((text = bufferedReader.readLine()) != null) {
					sb.append(text + "\n");
				}
				bufferedReader.close();
				return sb.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("File does not exist!");
		return null;
	}

	public static String[] readTxt(String txtPath, int n) {
		File file = new File(txtPath);
		if (file.isFile() && file.exists()) {
			try {
				FileInputStream fileInputStream = new FileInputStream(file);
				InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

				// StringBuffer sb = new StringBuffer();
				// String text = null;
				String[] s = new String[n];
				String text = null;
				for (int i = 0; i < n; i++) {
					if ((text = bufferedReader.readLine()) != null) {
						s[i] = text;
					}
				}
				bufferedReader.close();
				return s;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("File does not exist!");
		return null;
	}

	/**
	 * 使用FileOutputStream来写入txt文件
	 * 
	 * @param txtPath txt文件路径
	 * @param content 需要写入的文本
	 */
	public static void writeTxt(String txtPath, String content) {
		FileOutputStream fileOutputStream = null;
		File file = new File(txtPath);
		try {
			if (!file.exists()) {
				// 判断文件是否存在，如果不存在就新建一个txt
				file.createNewFile();
			}
			fileOutputStream = new FileOutputStream(file, true);
			fileOutputStream.write(content.getBytes());
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}