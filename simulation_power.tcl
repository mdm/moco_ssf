# define options
set val(chan)	Channel/WirelessChannel
set val(prop)	Propagation/SimpleRice
set val(ant)	Antenna/OmniAntenna
set val(ll)	LL
set val(ifq)	Queue/DropTail/PriQueue
set val(ifqlen)	50
set val(netif)	Phy/WirelessPhy
set val(mac)	Mac/802_11
set val(rp)	AODV
set val(nn)	80
set val(area_x) 500
set val(area_y) 500
set val(duration) 400.0
set val(initial_power) 0.04
set val(min_nb) 0
set val(max_nb) 7
set val(up_thresh) 1.0
set val(down_thresh) 0.8

# create new simulator object
set ns [new Simulator]

# open trace files for writing
set ns_trace [open "| bzip2 > ns_trace.bz2" w]
$ns trace-all $ns_trace
if {$val(min_nb) == 0} {
	set ctc_trace [open "normal_$val(up_thresh)_$val(down_thresh).ctc" w]
	set pos_trace [open "normal_$val(up_thresh)_$val(down_thresh).pos" w]
} else {
	set ctc_trace [open "power_$val(up_thresh)_$val(down_thresh).ctc" w]
	set pos_trace [open "power_$val(up_thresh)_$val(down_thresh).pos" w]
}

# set up topography
set topo [new Topography]
$topo load_flatgrid $val(area_x) $val(area_y)

# blasphemy
create-god $val(nn)

# configure nodes
set channel [new $val(chan)]
#$val(netif) set Pt_ .04
Phy/WirelessPhy set Pt_ $val(initial_power)

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
puts $pos_trace "area_x $val(area_x)"
puts $pos_trace "area_y $val(area_y)"
puts $pos_trace "duration $val(duration)"
puts $pos_trace "initial_power $val(initial_power)"
puts $pos_trace "min_nb $val(min_nb)"
puts $pos_trace "max_nb $val(max_nb)"
puts $pos_trace "nodes $val(nn)"

#set rng_x [new RNG]
set rv_x [new RandomVariable/Uniform]
$rv_x set min_ 0
$rv_x set max_ $val(area_x)
#$rv_x use-rng $rng_x

#set rng_y [new RNG]
#$rng_y next-substream
set rv_y [new RandomVariable/Uniform]
$rv_y set min_ 0
$rv_y set max_ $val(area_y)
#$rv_y use-rng $rng_y

for {set i 0} {$i < $val(nn)} {incr i} {
	set mobile_node($i) [$ns node]
	$mobile_node($i) random-motion 0
	set pos_x [$rv_x value]
	set pos_y [$rv_y value]
	set pos_z 0.0
	$mobile_node($i) set X_ $pos_x
	$mobile_node($i) set Y_ $pos_y
	$mobile_node($i) set Z_ $pos_z
	puts $pos_trace "id $i x $pos_x y $pos_y z $pos_z"

	set ctc_agent($i) [$mobile_node($i) set pwrctrl_agent_]
	$ctc_agent($i) up_beacons 15
	$ctc_agent($i) link_up_threshold $val(up_thresh)
	$ctc_agent($i) link_down_threshold $val(down_thresh)
	if {$val(min_nb) > 0} {
		$ctc_agent($i) min_nb $val(min_nb)
		$ctc_agent($i) max_nb $val(max_nb)
		$ctc_agent($i) powerlevels 0.01 0.016 0.025 0.04 0.063 0.1
	}
	$ctc_agent($i) log $ctc_trace

	$ns at $val(duration) "$mobile_node($i) reset";
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
$ns at [expr $val(duration) + 0.0001] "finish"

# run simulation
$ns run

