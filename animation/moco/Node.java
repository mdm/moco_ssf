package moco;

import java.util.LinkedList;
import java.util.Stack;

public class Node {
	public int id;
	public double posX;
	public double posY;
	public double posZ;
	public int neighbors;
	private Stack<Integer> power_stack = new Stack<Integer>();
	LinkedList<Integer> linkList = new LinkedList<Integer>();
	
	public Node(int initial_power) {
		power_stack.push(initial_power);
	}
	public Node(double posX, double posY, double posZ) {
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
	}
	
	public void addLink(int destination) {
		linkList.add(destination);
	}
	
	public void removeLink(int destination) {
		linkList.remove(destination);
	}

	public int peekPower() {
		return power_stack.peek();
	}

	public int popPower() {
		return power_stack.pop();
	}
	
	public void pushPower(int power) {
		power_stack.push(power);
	}
}
