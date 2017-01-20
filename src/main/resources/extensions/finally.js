var nashornBukkit = Java.type("me.finalchild.nashornbukkit.NashornBukkit").getInstance();

Function.prototype.on = function(event, eventPriority) {
  if (eventPriority == undefined) {
    script.on(event, this);
  } else {
    script.on(event, this, eventPriority)
  }
}

Function.prototype.onCommand = function(name) {
  return script.onCommand(name, this);
}

Function.prototype.runTask = function() {
  return script.runTask(this);
}

Function.prototype.runTaskAsynchronously = function() {
  return script.runTaskAsynchronously(this);
}

Function.prototype.async = function() {
  return script.runTaskAsynchronously(this);
}

Function.prototype.runTaskLater = function(delay) {
  return script.runTaskLater(this, delay);
}

Function.prototype.later = function(delay) {
  return script.runTaskLater(this, delay);
}

Function.prototype.runTaskLaterAsynchronously = function(delay) {
  return script.runTaskLaterAsynchronously(this, delay);
}

Function.prototype.laterAsync = function(delay) {
  return script.runTaskLaterAsynchronously(this, delay);
}

Function.prototype.runTaskTimer = function(delay, period) {
  return script.runTaskTimer(this, delay, period);
}

Function.prototype.timer = function(delay, period) {
  return script.runTaskTimer(this, delay, period);
}

Function.prototype.runTaskTimerAsynchronously = function(delay, period) {
  return script.runTaskTimerAsynchronously(this, delay, period);
}

Function.prototype.timerAsync = function(delay, period) {
  return script.runTaskTimerAsynchronously(this, delay, period);
}
