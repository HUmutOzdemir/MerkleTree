package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

import downloader.Downloader;
import project.MTNode;
import project.MerkleTree;

public class Main {

	public static void main(String[] args){
		
		
		MerkleTree m0 = new MerkleTree("data/1_bad.txt");		
		String hash = m0.getRoot().getLeft().getRight().getData();
		System.out.println(hash);
		
		boolean valid = m0.checkAuthenticity("data/1meta.txt");
		System.out.println(valid);
		
		// The following just is an example for you to see the usage. 
		// Although there is none in reality, assume that there are two corrupt chunks in this example.
		ArrayList<Stack<String>> corrupts = m0.findCorruptChunks("data/1meta.txt");
		while(!corrupts.get(0).isEmpty())
		System.out.println("Corrupt hash of first corrupt chunk is: " + corrupts.get(0).pop());
//		System.out.println("Corrupt hash of second corrupt chunk is: " + corrupts.get(1).pop());
		
		download("secondaryPart/data/download_from_trusted.txt");
		
	}
	/**
	 * Download all true chunks in path file.
	 * @param path contains links of meta files and 2 alternative sources for each data.
	 */
	public static void download(String path) {
		File file = new File(path);
		ArrayList<String> datas = new ArrayList<String>();
		if(file.exists()&&file.canRead()) {
			Scanner input=null;
			try {
				input = new Scanner(file);
			} catch (FileNotFoundException e) {
				System.out.println("There is no file in this path. Control your path and run the program again.");
				e.printStackTrace();
			}
			while(input.hasNextLine()) {
				String temp = input.nextLine();
				if(!temp.equals("")) {
					datas.add(temp);
				}
			}
			input.close();
		}
		if(!datas.isEmpty()) {
			for(int i=0;i<datas.size()/3;i++) {
				try {
					String filePath = Downloader.downloadAllChunks(datas.get(1+3*i));
					MerkleTree mt = new MerkleTree(filePath);
					URL url = new URL(datas.get(3*i));
					if(!mt.checkAuthenticity(url)){
						ArrayList<Stack<MTNode>> curruptList = mt.findCorruptChunkNodes(url);
						URL anotherSource = new URL(datas.get(2+3*i));
						changeCorruptChunks(curruptList, anotherSource);
					}
				} catch (NoSuchAlgorithmException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * Download corrupt chunks from alternative source.
	 * @param list the list of stack of corrupt MTNodes
	 * @param anotherSource URL object of alternative source
	 */
	private static void changeCorruptChunks(ArrayList<Stack<MTNode>> list,URL anotherSource) throws IOException, NoSuchAlgorithmException {
		BufferedReader input =  new BufferedReader(new InputStreamReader(anotherSource.openStream()));
		String urlLine = null;
		for(Stack<MTNode> stack : list) {
			MTNode temp = stack.pop();
			String adress = temp.getDB().getData();
			String nameOfData = adress.substring(adress.lastIndexOf("\\")+1);
			stack.push(temp);
			while((urlLine=input.readLine())!=null) {
				if(urlLine.endsWith(nameOfData)) {
					new Downloader(urlLine,new File(adress)).download();
					break;
				}
			}
		}
		setHashesOfTree(list);
	}
	/**
	 * Sets hashes of tree after changing the corrupt chunks.
	 * @param list the list of stack of corrupt MTNodes
	 */
	private static void setHashesOfTree(ArrayList<Stack<MTNode>> list) throws NoSuchAlgorithmException, IOException {
		for(Stack<MTNode> stack : list) {
			while(!stack.isEmpty()) {
				MTNode temp = stack.pop();
				MerkleTree.setHash(temp);
			}
		}
	}
}
