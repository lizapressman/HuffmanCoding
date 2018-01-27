
/* 

*** README ***

Liza Pressman
lpressma
class ID: 78
Contact: lpressma@u.rochester.edu

CSC 172 Project 3 (HuffmanCoding):
	My huffman encoding works for both the sample alice30.txt and ur.jpg.
	
	I would like to note however that my frequency files are writen as decimal to frequency instead of binary to frequency.
	I chose to do this because I used the binaryIn method read bytes to read my input files and this reads them as decimal.
	I know the project called for it to be binary, however because my program encodes and decodes perfectly, I figured this would be acceptable as long as I explained in my README file.
	
	***NOTE:
	alice.enc is encoded txt file
	alicefreq.txt is frequency file
	alice_dec.txt is decoded txt file
	ur.enc is encoded jpg file
	urfreq.txt is frequency file
	ur_dec.jpg is decoded jpg file
*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class HuffmanSubmit implements Huffman {

	public class Node implements Comparable<Node> {
		public int data;
		public int frequency;
		public Node left;
		public Node right;

		public Node(Integer word, Integer freq, Node leftLeaf, Node rightLeaf) {
			data = word;
			frequency = freq;
			left = leftLeaf;
			right = rightLeaf;
		}

		public boolean isLeaf(Node root) {
			if (root.left == null && root.right == null)
				return true;
			return false;
		}

		public int compareTo(Node other) {
			return this.frequency - other.frequency;
		}
	}

	public void makeFrequencyFile(String inputFile, String freqFile, HashMap<Integer, Integer> frequencyMap) throws IOException {
		BinaryIn in = new BinaryIn(inputFile);
		int c = in.readByte();
		while (!in.isEmpty()) {
			if (frequencyMap.containsKey(c)) {
				frequencyMap.put(c, frequencyMap.get(c) + 1);
			} else {
				frequencyMap.put(c, 1);
			}
			c = in.readByte();
		}
		
		FileWriter file = new FileWriter(freqFile);
		BufferedWriter writer = new BufferedWriter(file);
		for (Integer key : frequencyMap.keySet()) {
			String str = (key + ":" + frequencyMap.get(key) + "\n");
			writer.write(str);
		}
		writer.flush();
		writer.close();
	}

	public void getCode(Node node, Map<Integer, String> map, String str) {
		if (node.left == null && node.right == null) {
			map.put(node.data, str);
			return;
		}
		getCode(node.left, map, str + '1');
		getCode(node.right, map, str + '0');
	}
	
	public Node makeTree(HashMap<Integer, Integer> frequencyMap) {
		PriorityQueue<Node> queue = new PriorityQueue<Node>();
		for (HashMap.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
			Node node = new Node(entry.getKey(), entry.getValue(), null, null);
			queue.offer(node);
		}
		while (queue.size() > 1) {
			Node n1 = queue.poll();
			Node n2 = queue.poll();
			Node n3 = new Node(0, n2.frequency + n1.frequency, n1, n2);
			queue.offer(n3);
		}
		return queue.peek();
	}
	
	public void writeOutput(String inputFile, Node root, String outputFile) {
		BinaryIn in = new BinaryIn(inputFile);
		BinaryOut out = new BinaryOut(outputFile);
		try {
			boolean next;
			while (true) {
				Node temp = root;
				while (temp.left != null || temp.right != null) {
					next = in.readBoolean();
					if (next == false) {
						temp = temp.right;
					} else {
						temp = temp.left;
					}
					if (temp.left == null && temp.right == null) {
						out.write(temp.data, 8);
						break;
					}
				}
			}
		} catch (NoSuchElementException e) {
			out.close();
		}
	}

	public void encode(String inputFile, String outputFile, String freqFile) {
		try {
			// writes the frequencyFile and initializes frequencyMap to be
			// (decimal:frequency)
			HashMap<Integer, Integer> frequencyMap = new HashMap<Integer, Integer>();
			makeFrequencyFile(inputFile, freqFile, frequencyMap);

			// makes tree
			Node root = makeTree(frequencyMap);

			// initializes codes to be (decimal:binary)
			HashMap<Integer, String> codes = new HashMap<Integer, String>();
			getCode(root, codes, "");

			// writes the encoded file using binaryOut method write(boolean)
			BinaryIn in = new BinaryIn(inputFile);
			BinaryOut out = new BinaryOut(outputFile);
			int c = in.readByte();
			while (true) {
				try {
					String bin = codes.get(c);
					for (int i = 0; i < bin.length(); i++) {
						if (bin.charAt(i) == '1') {
							out.write(true);
						} else {
							out.write(false);
						}
					}
					c = in.readByte();
				} catch (NoSuchElementException e) {
					out.flush();
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void decode(String inputFile, String outputFile, String freqFile) {
		try {
			// frequencyMap is map of (decimal:frequency)
			HashMap<Integer, Integer> frequencyMap = new HashMap<Integer, Integer>();
			
			//reads the freqFile to create frequencyMap
			FileReader reader = new FileReader(freqFile);
			@SuppressWarnings("resource")
			BufferedReader buffer = new BufferedReader(reader);
			String line = buffer.readLine();
			while (line != null) {
				int i = 0;
				while (i < line.length()) {
					if (line.charAt(i) == ':') {
						break;
					}
					i++;
				}
					int c = Integer.parseInt(line.substring(0, i));
					String num = line.substring(i + 1, line.length());
					int freq = Integer.parseInt(num);
					// initializes frequencyMap with numbers from the freqFile
					frequencyMap.put(c, freq);
					line = buffer.readLine();
			}
			
			// makes tree
			Node root = makeTree(frequencyMap);

			// calls writeOutput which takes in the root of the tree, the inputFile and the outputFile
			// creates outputFile which is the decoded file and should be the same as the inputFile from the encode method
			writeOutput(inputFile, root, outputFile);
		} catch (IOException e) {

		}
	}

	public static void main(String[] args) {
		Huffman huffman = new HuffmanSubmit();
		// alice.enc is encoded txt file
		// alicefreq.txt is frequency file
		// alice_dec.txt is decoded txt file

		huffman.encode("alice30.txt", "alice.enc", "alicefreq.txt");
		huffman.decode("alice.enc", "alice_dec.txt", "alicefreq.txt");

		// ur.enc is encoded jpg file
		// urfreq.txt is frequency file
		// ur_dec.jpg is decoded jpg file

		huffman.encode("ur.jpg", "ur.enc", "urfreq.txt");
		huffman.decode("ur.enc", "ur_dec.jpg", "urfreq.txt");

	}
}