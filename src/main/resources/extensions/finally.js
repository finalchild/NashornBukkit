var nashornBukkit = Java.type("me.finalchild.nashornbukkit.NashornBukkit").getInstance();

Function.prototype.on = function(event, eventPriority) {
  if (eventPriority == undefined) {
    script.on(event, this);
  } else {
    script.on(event, this, eventPriority)
  }
}

Function.prototype.onCommand = function(name) {
  script.onCommand(name, this);
}

Function.prototype.runTask = function() {
  script.runTask(this);
}

Function.prototype.runTaskAsynchronously = function() {
  script.runTaskAsynchronously(this);
}

Function.prototype.runTaskLater = function(delay) {
  script.runTaskLater(this, delay);
}

Function.prototype.runTaskLaterAsynchronously = function(delay) {
  script.runTaskLaterAsynchronously(this, delay);
}

Function.prototype.runTaskTimer = function(delay, period) {
  script.runTaskTimer(this, delay, period);
}

Function.prototype.runTaskTimerAsynchronously = function(delay, period) {
  script.runTaskTimerAsynchronously(this, delay, period);
}
