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
				cmd : command
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