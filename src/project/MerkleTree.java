package project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

import util.HashGeneration;

public class MerkleTree {

	private MTNode root;
	private int numOfLeaves;
	private MerkleTree() {
		root=null;
		numOfLeaves=0;
	}
	/**
	 * Creates a Merkle Tree with a text file containing directories of chunks. 
	 * @param path text file containing directories of chunks
	 */
	public MerkleTree(String path) {
		File file = new File(path);
		if(file.exists()&& file.canRead()) {
			Scanner input=null;
			try {
				input = new Scanner(file);
			} catch (FileNotFoundException e) {
				System.out.println("There is no file in this path. Control your path and run the program again.");
				e.printStackTrace();
			}
			while(input.hasNextLine()) {
				String dataPath = input.nextLine();
				DataBlock newDB = new DataBlock(dataPath);
				try {
					addDataBlock(newDB);
				} catch (NoSuchAlgorithmException | IOException e) {
					e.printStackTrace();
				}
			}
			input.close();
		}
		
	}
	public MTNode getRoot() {
		return this.root;
	}
	/**
	 * Adds a data to Merkle Tree
	 * @param db DataBlock object of a data
	 */
	private void addDataBlock(DataBlock db) throws NoSuchAlgorithmException, IOException {
		root = add(root, db,height(),numOfLeaves);
		numOfLeaves++;
	}
	/**
	 * Recursive method for adding a data to merkle tree.
	 */
	private MTNode add(MTNode node, DataBlock db, int height,int numOfLeaves) throws NoSuchAlgorithmException, IOException {
		if(node==null) {
			if(height==0||height==-1) {
				if(db!=null)
					return new MTNode(HashGeneration.generateSHA256(new File(db.dataPath)),db);
				else
					return new MTNode("",null);
			}else {
				node = new MTNode("",null);
				node.left=add(node.left,db,height-1,numOfLeaves);
				setHash(node);
				return node;
			}
		}
		int maxLeaf = (int) Math.pow(2, height);
		if(numOfLeaves==maxLeaf) {
			MTNode newRoot = new MTNode("",null);
			newRoot.left=node;
			newRoot.right=add(newRoot.right,db,height,0);
			setHash(newRoot);
			return newRoot;
		}
		if(numOfLeaves>=maxLeaf/2) {
			node.right=add(node.right,db,height-1,numOfLeaves-maxLeaf/2);
		}else if(numOfLeaves<maxLeaf/2) {
			node.left=add(node.left,db,height-1,numOfLeaves);
		}
		setHash(node);
		return node;
	}
	/**
	 * Sets hash of a MTNode.
 	 * @param node MTNode object
	 */
	public static void setHash(MTNode node) throws NoSuchAlgorithmException, IOException {
		if(node == null) {
			return;
		}if(node.db!=null) {
			node.setData(HashGeneration.generateSHA256(new File(node.db.dataPath)));
			return;
		}
		if(node.left==null) {
			node.setData(HashGeneration.generateSHA256(node.right.getData()));
			return;
		}else if(node.right==null) {
			node.setData(HashGeneration.generateSHA256(node.left.getData()));
			return;
		}else if(node.left!=null&&node.right!=null){
			node.setData(HashGeneration.generateSHA256(node.left.getData()+node.right.getData()));
			return;
		}else {
			node.setData("");
			return;
		}
	}
	/**
	 * Checks all data blocks are true by checking the root of tree.
	 * @param hashFile path of a text file which has true hashes of tree.
	 * @return the datas are correct or not
	 */
	public boolean checkAuthenticity(String hashFile){
		File file = new File(hashFile);
		if(file.exists()&&file.canRead()) {
			Scanner input=null;
			try {
				input = new Scanner(file);
			} catch (FileNotFoundException e) {
				System.out.println("There is no file in this path. Control your path and run the program again.");
				e.printStackTrace();
			}
			if(input.hasNextLine()) {
				String hash = input.nextLine();
				input.close();
				return hash.equals(root.getData());
			}
			input.close();
		}
		return false;
			
	}
	/**
	 * Checks all data blocks are true by checking the root of tree.(Uses URL)
	 * @param url URL which has true hashes of tree.
	 * @return the datas are correct or not
	 */
	public boolean checkAuthenticity(URL url) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		String firstHash = null;
		if((firstHash=br.readLine())!=null) {
			return root.getData().equals(firstHash);
		}
		return false;
	}
	/**
	 * Returns the list of stacks of corrupt nodes' hashes.
	 * @param hashFile path of a text file which has true hashes of tree
	 * @return the list of stacks of corrupt nodes' hashes
	 */
	public ArrayList<Stack<String>> findCorruptChunks(String hashFile) {
		ArrayList<Stack<String>> list = new ArrayList<Stack<String>>();
		if(!checkAuthenticity(hashFile)) {
			Stack<String> stack = new Stack<String>();
			stack.push(root.getData());
			list.add(stack);
			MTNode trueHash=null;
			try {
				trueHash = createTrueTree(hashFile);
			} catch (NoSuchAlgorithmException | IOException e) {
				e.printStackTrace();
			}
			createCorruptionList(root, trueHash, list, stack);
		}
		return list;
	}
	/**
	 * Recursive method that creates the list of stacks of corrupt nodes' hashes
	 */
	private void createCorruptionList(MTNode node, MTNode trueHash,ArrayList<Stack<String>> list,Stack<String>stack) {
		if(node==null) {
			return;
		}
		if(node.left!=null&&node.right!=null) {
			if(!node.left.getData().equals(trueHash.left.getData())&&!node.right.getData().equals(trueHash.right.getData())) {
				Stack<String> newStack = copyStack(stack);
				newStack.push(node.right.getData());
				stack.push(node.left.getData());
				list.add(newStack);
				createCorruptionList(node.left, trueHash.left, list, stack);
				createCorruptionList(node.right, trueHash.right, list, newStack);
			}else if(!node.left.getData().equals(trueHash.left.getData())) {
				stack.push(node.left.getData());
				createCorruptionList(node.left, trueHash.left, list, stack);
			}else if(!node.right.getData().equals(trueHash.right.getData())) {
				stack.push(node.right.getData());
				createCorruptionList(node.right, trueHash.right, list, stack);
			}
		}else if(node.left!=null) {
			if(!node.left.getData().equals(trueHash.left.getData())) {
				stack.push(node.left.getData());
				createCorruptionList(node.left, trueHash.left, list, stack);
			}
		}else if(node.right!=null) {
			if(!node.right.getData().equals(trueHash.right.getData())) {
				stack.push(node.right.getData());
				createCorruptionList(node.right, trueHash.right, list, stack);
			}
		}
	}
	/**
	 * Creates a copy of this tree with true hash values.
	 * @param hashFile path of a text file which has true hashes of tree
	 * @return root of that tree.
	 */
	private MTNode createTrueTree(String hashFile) throws NoSuchAlgorithmException, IOException {
		MerkleTree mt = new MerkleTree();
		for(int i=0;i<numOfLeaves;i++) {
			mt.addDataBlock(null);
		}
		File file = new File(hashFile);
		Queue<MTNode> queue = new LinkedList<MTNode>();
		queue.add(mt.getRoot());
		if(file.exists()&&file.canRead()) {
			Scanner input = new Scanner(file);
			while(!queue.isEmpty()&&input.hasNext()) {
				MTNode temp = queue.poll();
				temp.setData(input.next());
				addChilds(queue, temp);
			}
			input.close();
		}
		return mt.getRoot();
	}
	/**
	 * Returns the list of stacks of corrupt nodes.
	 * @param url URL which has true hashes of tree.
	 * @return the list of stacks of corrupt nodes
	 */
	public ArrayList<Stack<MTNode>> findCorruptChunkNodes(URL url) throws NoSuchAlgorithmException, IOException{
		ArrayList<Stack<MTNode>> list = new ArrayList<Stack<MTNode>>();
		if(!checkAuthenticity(url)) {
			Stack<MTNode> stack = new Stack<MTNode>();
			stack.push(root);
			list.add(stack);
			MTNode trueHash = createTrueTree(url);
			createCorruptionNodeList(root, trueHash, list, stack);
		}
		return list;
	}
	/**
	 * Recursive method that creates the list of stacks of corrupt nodes.
	 */
	private void createCorruptionNodeList(MTNode node, MTNode trueHash,ArrayList<Stack<MTNode>> list,Stack<MTNode>stack) {
		if(node==null) {
			return;
		}
		if(node.left!=null&&node.right!=null) {
			if(!node.left.getData().equals(trueHash.left.getData())&&!node.right.getData().equals(trueHash.right.getData())) {
				Stack<MTNode> newStack = copyStackOfNodes(stack);
				newStack.push(node.right);
				stack.push(node.left);
				list.add(newStack);
				createCorruptionNodeList(node.left, trueHash.left, list, stack);
				createCorruptionNodeList(node.right, trueHash.right, list, newStack);
			}else if(!node.left.getData().equals(trueHash.left.getData())) {
				stack.push(node.left);
				createCorruptionNodeList(node.left, trueHash.left, list, stack);
			}else if(!node.right.getData().equals(trueHash.right.getData())) {
				stack.push(node.right);
				createCorruptionNodeList(node.right, trueHash.right, list, stack);
			}
		}else if(node.left!=null) {
			if(!node.left.getData().equals(trueHash.left.getData())) {
				stack.push(node.left);
				createCorruptionNodeList(node.left, trueHash.left, list, stack);
			}
		}else if(node.right!=null) {
			if(!node.right.getData().equals(trueHash.right.getData())) {
				stack.push(node.right);
				createCorruptionNodeList(node.right, trueHash.right, list, stack);
			}
		}
	}
	/**
	 * Creates a copy of this tree with true hash values.
	 * @param url URL which has true hashes of tree.
	 * @return root of this tree
	 */
	private MTNode createTrueTree(URL url) throws IOException, NoSuchAlgorithmException {
		MerkleTree mt = new MerkleTree();
		for(int i=0;i<numOfLeaves;i++) {
			mt.addDataBlock(null);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		String firstHash = null;
		Queue<MTNode> queue = new LinkedList<MTNode>();
		queue.add(mt.getRoot());
		while((firstHash=br.readLine())!=null&&!queue.isEmpty()) {
			MTNode temp = queue.poll();
			temp.setData(firstHash);
			addChilds(queue, temp);
		}
		return mt.getRoot();
	}
	/**
	 * A Static method adds childs of a node to a queue.
	 * @param queue queue of MTNode
	 * @param node parent of nodes will be added to queue
	 */
	private static void addChilds(Queue<MTNode> queue,MTNode node) {
		if(node!=null) {
			if(node.left!=null) {
				queue.add(node.left);
			}
			if(node.right!=null) {
				queue.add(node.right);
			}
			
		}
	}
	/**
	 * Copies a stack.
	 * @param stack stack will be copied
	 * @return the copy of stack
	 */
	private static Stack<String> copyStack(Stack<String> stack){
		Stack<String> copy = new Stack<String>();
		Stack<String> aux = new Stack<String>();
		while(!stack.isEmpty()) {
			aux.push(stack.pop());

		}
		while(!aux.isEmpty()) {
			String temp = aux.pop();
			copy.push(temp);
			stack.push(temp);
		}
		return copy;
	}
	/**
	 * Copies a stack.
	 * @param stack stack will be copied
	 * @return the copy of stack
	 */
	private static Stack<MTNode> copyStackOfNodes(Stack<MTNode> stack){
		Stack<MTNode> copy = new Stack<MTNode>();
		Stack<MTNode> aux = new Stack<MTNode>();
		while(!stack.isEmpty()) {
			aux.push(stack.pop());

		}
		while(!aux.isEmpty()) {
			MTNode temp = aux.pop();
			copy.push(temp);
			stack.push(temp);
		}
		return copy;
	}
	/**
	 * @return the height of tree
	 */
	private int height() {
		return height(root);
	}
	/**
	 * Recursive method for calculating height of a node.
	 */
	private int height(MTNode node) {
		if(node==null) {
			return -1;
		}
		return Math.max(height(node.right),height(node.left))+1;
	}
}
