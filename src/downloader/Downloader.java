/**
 * Taken from https://www.youtube.com/watch?v=rd6m-6l2xQQ
 * and https://www.journaldev.com/924/java-download-file-url
 */

package downloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader {
	String link;
	public File out;
	/**
	 * Constructor of download object.
	 * @param link the download link of file
	 * @param out where you want to download file
	 */
	public Downloader(String link,File out) throws IOException {
		this.link=link;
		this.out=out;
		File parent = out.getParentFile();
		if(!parent.exists()) {
			parent.mkdirs();
		}
	}
	/**
	 * Downloads the file.
	 */
	public void download() throws IOException {
		URL url = new URL(link);
		HttpURLConnection http = (HttpURLConnection)url.openConnection();
		BufferedInputStream input = new BufferedInputStream(http.getInputStream());
		FileOutputStream fos = new FileOutputStream(this.out);
		BufferedOutputStream out = new BufferedOutputStream(fos,1024);
		byte[] buffer = new byte[1024];
		int read = 0;
		while((read=input.read(buffer,0,1024))>=0) {
			out.write(buffer,0,read);
		}
		out.close();
		input.close();
	}
	/**
	 * Download all chunks in an URL.
	 * @param urlString the address of URL
	 * @return the path of the file which has directories of all chunks
	 */
	public static String downloadAllChunks(String urlString) throws IOException {
		URL url = new URL(urlString);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String urlLine=null;
		String filePath=null;
		while((urlLine = in.readLine()) != null) {
			Downloader down = new Downloader(urlLine,new File("secondaryPart/data/split/"+fileName(urlLine)));
			down.download();
			File file = new File("secondaryPart/data/"+directoryName(urlLine)+".txt");
			appendToTextFile(file, down.out.getPath());
			filePath=file.getPath();
		}
		return filePath;
	}
	/**
	 * Appends a string to a file.
	 * @param file file
	 * @param text text that will be appended.
	 */
	private static void appendToTextFile(File file,String text) throws IOException {
		if(!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file,true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(text);
		bw.newLine();
		bw.close();
		fw.close();
	}
	/**
	 * Returns the name of file from a URL link.
	 * @param urlLine URL link
	 * @return the name of file
	 */
	private static String fileName(String urlLine) {
		String end = urlLine.substring(urlLine.lastIndexOf("/"));
		String temp = urlLine.substring(0, urlLine.lastIndexOf("/"));
		String start = temp.substring(temp.lastIndexOf("/")+1);
		return start+end;
	}
	/**
	 * Returns the folder name of downloaded file from URL link.
	 * @param urlLine URL link
	 * @return the folder name of downloaded file
	 */
	private static String directoryName(String urlLine) {
		String temp = urlLine.substring(0, urlLine.lastIndexOf("/"));
		temp = temp.substring(temp.lastIndexOf("/")+1);
		return temp;
	}
}
