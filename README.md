### SQ

Like `jq`, but different. Transform Json data in a streaming manner using JavaScript

#### How 

Let's start a new implem: 

- Scala cli to init the setup
- Scala-graal for a concrete implem
- HA as architecture style


### Package a native image

Using graalvm, make sure `js` is installed on your graal vm `JAVA_HOME/bin`:

```
scala-cli package . --java-home $JAVA_HOME --native-image -o sq \
    --graalvm-args --no-fallback \
    -- \
      --language:js
```

### Usage

##### Simple transfomation

- Piping

```
tail -f service1.log --files service2.json | sq 'log => log.message'
```

- Files

```
sq 'log => log.message' --files service1.log --files service2.json

```

`log => log.message` is an example of a js lambda that you can apply. 

for more multiline example you can define the transfo fucntion outside 

```
transfo=$(cat << EOF
i => {
  return {
  "msg": log.message,
  "log_level": i.level
  }
}
EOF
)

sq $transfo --files service1.log --files service2.json
```

##### Advanced transfomation

- Transformation that results on an array is flattened
- `[]` empty can be used to skip lines

```
{
  "load": 0,
  "coords": [{"lon": 1, "lat": 2}, {"lon": 3, "lat": 4}]
}
{
  "load": 10,
  "coords": [{"lon": 5, "lat": 6}, {"lon": 7, "lat": 8}]
}
```
Based on the file above, extract latitudes from postions with a positive load:

```
> tail -f f.json | sq 's => { return s.load > 0 ? s.coords:[]}' | sq 'coord => coord.lat'
6
8
```
