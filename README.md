# the eryn programming language

An interpreted programming language made in Java for learning purposes. Currently not finished (and maybe never will be).

# building
>mvn package

# running (need java 21 or higher)
>java -jar ea target/eryn.jar <SOURCE>

# examples
```ruby
def main()
    println("Hello, world!")
end
```

```ruby
def say(message: string)
    println(message)
end

def main()
    let message: string = "Hello, world!"
    say(message)
end
```
