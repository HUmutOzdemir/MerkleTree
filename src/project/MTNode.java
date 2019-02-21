package project;

public class MTNode {
	
	MTNode left,right;
	private String data;
	DataBlock db;
	MTNode(String info, DataBlock db){
		this.db=db;
		this.data=info;
	}
	/**
	 * Getter of DataBlock 
	 * @return the DataBlock field of object(which contains the path of data)S
	 */
	public DataBlock getDB() {
		return db;
	}
	/**
	 * Getter for hash of a MTNode
	 * @return the hash value of a MTNode
	 */
	public String getData() {
		return data;
	}
	/**
	 * Sets the hash of a MTNode
	 * @param info new hash value of MTNode
	 */
	public void setData(String info) {
		this.data = info;
	}
	/**
	 * Getter for left child of MTNode
	 * @return left child of MTNode
	 */
	public MTNode getLeft() {
		return left;
	}
	/**
	 * Getter for right child of MTNode
	 * @return right child of MTNode
	 */
	public MTNode getRight() {
		return right;
	}
	
}
