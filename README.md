# config-server
Silly java server, allowing to distribute configuration to worker and keep track of posted results.

Run
```
java -cp myJar.jar se.kth.App -s srcDir -d destDir -errorDir (-p port)
```

 * **srcDir** contains the configuration to distribute to workers (route `/getConfig`)
 * **destDir** where to put results (route `/postResult` with a header `transformation.directory:path/to/config`)

## Routes

 * `/getConfig` returns any configuration not yet distributed. (If its parent directory contains a file `properties.properties`, those properties will be forwarded through http headers.)
 * `/postResult` requires a header `transformation.directory:path/to/config`.
 * `/reload` reload files (in case new config to be processed have been added).
 * `/overview` get an html summary of config processed, to be processed or in progress.
