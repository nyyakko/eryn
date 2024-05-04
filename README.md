# the eryn programming language

An interpreted programming language made in Java for learning purposes. Currently not finished (and maybe never will be).

# building
``>mvn package``

# running (need java 22 or higher)
``>java -jar ea target/eryn.jar <SOURCE>``

# examples
```
import "./std.eryn"

fn main()
    println("Hello, world!")
end
```

```
import "./std.eryn"

fn say(message: string)
    println(message)
end

fn main()
    let message: string = "Hello, world!"
    say(message)
end
```
