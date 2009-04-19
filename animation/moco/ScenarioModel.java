package moco;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;

public class ScenarioModel {
	
	double area_width;
	double area_height;
	Node[] nodes;
	HashMap<Integer, HashMap<Integer, Integer>> links = new HashMap<Integer, HashMap<Integer, Integer>>();
	
	double time;
	double duration;
	Stack<Event> past_events;
	Stack<Event> future_events;
	
	int min_nb;
	int max_nb;
	
	public void setArea(double width, double height) {
		this.area_width = width;
		this.area_height = height;
	}
	
	public int loadNodes(String filename) {
		BufferedReader pos_trace;
		StringTokenizer tokenizer;
		int num_nodes = 0;
		
		try {
			pos_trace = new BufferedReader(new FileReader(filename));
			
			tokenizer = new StringTokenizer(pos_trace.readLine());
			tokenizer.nextToken();
			area_width = Double.valueOf(tokenizer.nextToken());
			
			tokenizer = new StringTokenizer(pos_trace.readLine());
			tokenizer.nextToken();
			area_height = Double.valueOf(tokenizer.nextToken());
			
			tokenizer = new StringTokenizer(pos_trace.readLine());
			tokenizer.nextToken();
			duration = Double.valueOf(tokenizer.nextToken());
			
			tokenizer = new StringTokenizer(pos_trace.readLine());
			tokenizer.nextToken();
			int initial_power = (int) (1000 * Float.valueOf(tokenizer.nextToken()));
			
			tokenizer = new StringTokenizer(pos_trace.readLine());
			tokenizer.nextToken();
			min_nb = Integer.valueOf(tokenizer.nextToken());
			
			tokenizer = new StringTokenizer(pos_trace.readLine());
			tokenizer.nextToken();
			max_nb = Integer.valueOf(tokenizer.nextToken());
			
			tokenizer = new StringTokenizer(pos_trace.readLine());
			tokenizer.nextToken();
			num_nodes = Integer.valueOf(tokenizer.nextToken());
			nodes = new Node[num_nodes];

			if (min_nb == 0) {
				max_nb = num_nodes; // make all nodes "green"
			}

			for (int i = 0; i < num_nodes; i++) {
				tokenizer = new StringTokenizer(pos_trace.readLine());
				nodes[i] = new Node(initial_power);
				
				tokenizer.nextToken(); // "id"
				tokenizer.nextToken(); // id
				tokenizer.nextToken(); // "x"
				nodes[i].posX = Double.valueOf(tokenizer.nextToken()); // x
				tokenizer.nextToken(); // "y"
				nodes[i].posY = Double.valueOf(tokenizer.nextToken()); // y
				tokenizer.nextToken(); // "z"
				nodes[i].posZ = Double.valueOf(tokenizer.nextToken()); // z
			}
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			num_nodes = 0;
		}
		
		return num_nodes;
	}
	
	public int loadEvents(String filename) {
		BufferedReader ctc_trace;
		String line;
		StringTokenizer tokenizer;
		Event event;
		String type;
		
		past_events = new Stack<Event>();
		future_events = new Stack<Event>();
		
		try {
			ctc_trace = new BufferedReader(new FileReader(filename));
			
			while ((line = ctc_trace.readLine()) != null) {
				event = new Event();
				tokenizer = new StringTokenizer(line);
								
				event.time = Double.valueOf(tokenizer.nextToken());
				tokenizer.nextToken();
				event.node_id = Integer.valueOf(tokenizer.nextToken());
				type = tokenizer.nextToken();
				if (type.compareTo("up") == 0) {
					event.type = Event.Type.UP;
					event.param = Integer.valueOf(tokenizer.nextToken());
				}
				else if (type.compareTo("down") == 0) {
					event.type = Event.Type.DOWN;
					event.param = Integer.valueOf(tokenizer.nextToken());
				}
				else if (type.compareTo("power") == 0) {
					event.type = Event.Type.POWER;
					event.param = (int) (1000 * Float.valueOf(tokenizer.nextToken()));
				}
				else {
					// we don't handle this type of event. throw it away.
					continue;
				}
				
				// push events onto past_events to ensure correct ordering
				past_events.push(event);
			}
			
			// transfer all events to future_events
			while (!past_events.empty()) {
				future_events.push(past_events.pop());
			}
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			past_events.clear();
			future_events.clear();
		}
		
		return future_events.size();
	}
	
	public boolean progressTime (double target_time) {
		boolean world_changed = false;
		Event event;
				
		if (time < target_time) { // travel foreward
			while ((!future_events.empty()) && (future_events.peek().time <= target_time)) {
				event = future_events.pop();
				past_events.push(event);
				
				switch (event.type) {
				case UP:
					if (changeLink(event.node_id, event.param, 1) == 1) {
						// old score was 1. new score is 2.
						// --> both nodes reported the link up
						world_changed = true;
						nodes[event.node_id].neighbors++;
						nodes[event.param].neighbors++;
					}
					break;
				case DOWN:
					if (changeLink(event.node_id, event.param, -1) == 2) {
						// old score was 2. new score is 1.
						// --> link is not reported up by both nodes anymore
						world_changed = true;
						nodes[event.node_id].neighbors--;
						nodes[event.param].neighbors--;
					}
					break;
				case POWER:
					nodes[event.node_id].pushPower(event.param);
					break;
				}
			}
		}
		else if (time > target_time) { // travel backward
			while ((!past_events.empty()) && (past_events.peek().time >= target_time)) {
				event = past_events.pop();
				future_events.push(event);
				
				switch (event.type) {
				case UP:
					if (changeLink(event.node_id, event.param, -1) == 2) {
						// old score was 2. new score is 1.
						// --> link is not reported up by both nodes anymore
						world_changed = true;
						nodes[event.node_id].neighbors--;
						nodes[event.param].neighbors--;
					}
					break;
				case DOWN:
					if (changeLink(event.node_id, event.param, 1) == 1) {
						// old score was 1. new score is 2.
						// --> both nodes reported the link up
						world_changed = true;
						nodes[event.node_id].neighbors++;
						nodes[event.param].neighbors++;
					}
					break;
				case POWER:
					nodes[event.node_id].popPower();
					break;
				}
			}
		}
		
		// NOTE: if already time == target_time the world does not change.
		
		time = target_time;
		
		return world_changed;
	}
	
	private int changeLink(int source_id, int destination_id, int amount) {
		int temp;
		HashMap <Integer, Integer> link_score;
	
		if (source_id > destination_id) { // swap
			temp = source_id;
			source_id = destination_id;
			destination_id = temp;
		}
		
		if (!links.containsKey(source_id)) {
			link_score = new HashMap <Integer, Integer>();
			links.put(source_id, link_score);
		}
		else {
			link_score = links.get(source_id); 
		}
		
		if (link_score.containsKey(destination_id)) {
			temp = link_score.get(destination_id);
		}
		else {
			temp = 0;
		}
		
		link_score.put(destination_id, temp + amount);
		
		return temp;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}
}
