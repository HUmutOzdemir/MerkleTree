package project;

public class DataBlock {
	/**
	 * Stores the path of data. Only leaf nodes has a data block object.
	 */
	String dataPath;

	public String getData() {
		return dataPath;
	}
	
	DataBlock(String dataPath) {
		this.dataPath = dataPath;
	}
	
}
