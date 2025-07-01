# DynDns-Refresh-Bot
A bot that will refresh you IP-Address at several DynDns Providers


Define the Properties of this tool over a Env-Variable called "JAVA_OPTS".
This variable can be used to set any amount of Options to the jar execution call.
The reason is to set properties over the env that will then set them over the jar call like this:

```$JAVA_OPTS="-Dtimeouts.http=3000```

With the ``-D`` parameter you can set Properties. 

### So use it like this:
````yaml
version: "3.8"

services:
  dyndns-bot:
    build: .
    environment:
      JAVA_OPTS: "-Drefresh.domains=https://my.first.dyndns.org;https://my.seconds.dyndns.org -Dtimeouts.loop=5"
````

### Following arguments are possible:
- ``refresh.domains``: all domains that you use to refresh your dyndns seperated by a ``;``
- ``timeouts.loop``: how long the loop is going to stop for each cycle (in seconds)
- ``timeouts.http``: how long it takes for a http call to timeout (in ms)
- ``startup-ip``: the ip-address that will be used at the start of the service (does not play a big difference)


Log level can be set by setting env ``LOG_LEVEL`` to TRACE, DEBUG, INFO, WARN, or ERROR