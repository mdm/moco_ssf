package moco;

import java.util.ArrayList;

public class Event {
	enum Type { UP, DOWN, POWER }
	
	double time;
	int node_id;
	Type type;
	int param;
}
