# DydDns-Refresh-Bot
A bot that will refresh you IP-Address at several DynDns Providers


Define the Properties of this tool over a Env-Variable called "JAVA_OPTS".
This variable can be used to set any amount of Options to the jar execution call.
The reason is to set properties over the env that will then set them over the jar call like this:

```$JAVA_OPTS="-Dtimeouts.http=3000```

With the ``-D`` parameter you can set Properties. 