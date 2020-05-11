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

### Examples

````
cat json_output.log | jsq 'i => i.message'
````
