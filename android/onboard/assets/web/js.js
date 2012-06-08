$(document).ready(function() {
	bindConnect("#connect")
	bindDisconnect("#disconnect")
	bindCmd("#forward", "forward")
	bindCmd("#left", "left")
	bindCmd("#stop", "stop")
	bindCmd("#right", "right")
	bindCmd("#backward", "backward")
})

var bindCmd = function(button, command) {
	$(button).click(function() {
		$.ajax({
			url : "command",
			type : "POST",
			data : {
				cmd : command,
				value: updateSpeed(command)
			},
			success : function(result) {
				$("#text").text(result)
			}
		})
	})
}

var bindConnect = function(button) {
	$(button).click(function() {
		$.ajax({
			url : "command",
			type : "POST",
			data : {
				cmd : "connect"
			},
			success : function(result) {
				$("#forward").removeAttr("disabled")
				$("#left").removeAttr("disabled")
				$("#stop").removeAttr("disabled")
				$("#right").removeAttr("disabled")
				$("#backward").removeAttr("disabled")
				$("#disconnect").removeAttr("disabled")
				$("#connect").attr("disabled", "disabled");
				$("#text").text("");
				resetSpeed();
			}
		})
	})
}

var bindDisconnect = function(button) {
	$(button).click(function() {
		$.ajax({
			url : "command",
			type : "POST",
			data : {
				cmd : "disconnect"
			},
			success : function(result) {
				$("#forward").attr("disabled", "disabled");
				$("#left").attr("disabled", "disabled");
				$("#stop").attr("disabled", "disabled");
				$("#right").attr("disabled", "disabled");
				$("#backward").attr("disabled", "disabled");
				$("#connect").removeAttr("disabled");
				$("#disconnect").attr("disabled", "disabled");
				$("#text").text("");
			}
		})
	})
}

var speed = {};

var updateSpeed = function(command) {
	var currSpeed = speed[command];
	resetSpeed();
	if (command == "stop") {
		return speed[command];
	}
	if (currSpeed < 3) {
		speed[command] = currSpeed + 1;
	} else {
		speed[command] = 3;
	}
	showSpeedMeter(command, speed[command]);

	return speed[command]
}

var showSpeedMeter = function(command, val) {
	for (i = 1; i <= val; i++) {
		$("#" + command + "_" + i).css("visibility", "visible");
	}
}

var hideSpeedMeter = function(command) {
    $("#" + command + "_1").css("visibility", "hidden");
    $("#" + command + "_2").css("visibility", "hidden");
    $("#" + command + "_3").css("visibility", "hidden");
}

var resetSpeed = function () {
    speed["forward"] = 0;
    hideSpeedMeter("forward");
    speed["left"] = 0;
    hideSpeedMeter("left");
    speed["right"] = 0;
    hideSpeedMeter("right");
    speed["backward"] = 0;
    hideSpeedMeter("backward");
    speed["stop"] = -1;
}
