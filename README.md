# Installing
## Tools required
```
sudo apt-get install git maven
```
 
## Getting the code
```
git clone https://github.com/totalorder/excercise2
```

## Compiling
```
mvn package
```

# Running

## Start bank on default name Nordea
```
java -cp target/ex2-1.0-SNAPSHOT.jar se.kth.id2212.ex2.bankrmi.Server
```

## Start marketplace on default name Blocket
```
java -cp target/u1-1.0-SNAPSHOT.jar org.deadlock.id2212.Main schedule2.txt 5001 127.0.0.1 5000
```
## Start client(s) with chosen username
```
java -cp target/ex2-1.0-SNAPSHOT.jar se.kth.id2212.ex2.marketplace.ClientImpl anton2
```