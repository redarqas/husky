### JSQ
Like `jq`, but different. You will be able to use JS to manipulate the json output

### How 

To manipulate the json, you will be able to use a regular javascript arrow function

```
in => in.message 

Or 

in => {
  // statements
  return result;
}
``` 

### Native version

You can produce a native using `coursier`  

```
cs bootstrap -f  -r ivy2Local io.redcats:jsq_2.12:0.0.1-SNAPSHOT -o jsqo  -M io.redcats.jsq.App --native-image --graalvm 20.0.0
```

To use your own graal instance

```
native-image --class-path $(coursier fetch -r ivy2Local io.redcats:jsq_2.12:8db16a71255ef46206ce87e7a37f53ba1bd723d4-SNAPSHOT -p) io.redcats.jsq.App
```
### Examples

````
cat json_output.log | jsq 'i => i.message'
````

### Python and Ruby support : ???

To make python work

`export GRAAL_PYTHONHOME=$GRAA_HOME/languages/python `

Ruby is not working, I tried this:

`export TRUFFLERUBYOPT="--home=$GRAA_HOME/languages/ruby"` 

 but still not working
