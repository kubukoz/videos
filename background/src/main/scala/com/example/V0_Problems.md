# Problems with "forgetting" background processes

- wasting resources (e.g. keeping a DB connection open when it's not needed anymore)
- might lead to resource starvation
- if a background process runs forever (e.g. message processor), we can't easily stop it gracefully
- can keep application or test from exiting cleanly
- can cause data corruption (e.g. we failed to replace a running process with another, so the wrong program is processing our data)
