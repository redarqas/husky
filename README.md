### SQ

Like `jq`, but different. You will be able to use JS to manipulate the json output

#### How 

Let's start a new implem: 

- Scala cli to init the setup
- Scala-graal for a concrete implem
- HA as architecture style


### Package a native image

Using graalvm, make sure `js` is installed on your graal vm JAVA_HOME:

```
sc package . --java-home $JAVA_HOME --native-image -o sq \
    -- \
      --enable-all-security-services \
      --initialize-at-build-time \
      --language:js
```
