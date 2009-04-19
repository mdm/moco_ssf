# define options
set val(chan)	Channel/WirelessChannel
set val(prop)	Propagation/SimpleRice
set val(ant)	Antenna/OmniAntenna
set val(ll)	LL
set val(ifq)	Queue/DropTail/PriQueue
set val(ifqlen)	50
set val(netif)	Phy/WirelessPhy
set val(mac)	Mac/802_11
set val(rp)	DSDV
set val(nn)	80

# create new simulator object
set ns [new Simulator]

# open trace files for writing
$ns use-newtrace
set ns_trace [open "| bzip2 > ns_trace.bz2" w]
$ns trace-all $ns_trace
#set ctc_trace [open "| bzip2 > ctc_trace.bz2" w]
set ctc_trace [open "ctc_trace" w]
set pos_trace [open "pos_trace" w]

# set up topography
set topo [new Topography]
$topo load_flatgrid 500 500

# blasphemy
create-god $val(nn)

# configure nodes
set channel [new $val(chan)]
$val(netif) set Pt_ .04

$ns node-config -adhocRouting $val(rp) \
		-pwrCtrl CTC \
                -llType $val(ll) \
                -macType $val(mac) \
                -ifqType $val(ifq) \
                -ifqLen $val(ifqlen) \
                -antType $val(ant) \
                -propType $val(prop) \
                -phyType $val(netif) \
                -topoInstance $topo \
                -channel $channel \
                -agentTrace ON \
                -routerTrace ON \
                -macTrace OFF \
                -movementTrace OFF

# create nodes
puts $pos_trace "$val(nn) nodes"
for {set i 0} {$i < $val(nn)} {incr i} {
	set mobile_node($i) [$ns node]
	$mobile_node($i) random-motion 0
	set pos_x [expr rand()*500]
	set pos_y [expr rand()*500]
	set pos_z 0.0
	$mobile_node($i) set X_ $pos_x
	$mobile_node($i) set Y_ $pos_y
	$mobile_node($i) set Z_ $pos_z
	puts $pos_trace "id $i x $pos_x y $pos_y z $pos_z"

	set ctc_agent($i) [$mobile_node($i) set pwrctrl_agent_]
	$ctc_agent($i) up_beacons 15
	$ctc_agent($i) link_up_threshold 1.0
	$ctc_agent($i) link_down_threshold 0.8
	$ctc_agent($i) log $ctc_trace

	$ns at 400.0 "$mobile_node($i) reset";
}

# procedure to close trace files
proc finish {} {
	global ns ns_trace ctc_trace pos_trace
	$ns flush-trace
	close $ns_trace
	close $ctc_trace
	close $pos_trace
	exit 0
}

# finish simulation after 5.0 seconds
$ns at 400.0001 "finish"

# run simulation
$ns run

