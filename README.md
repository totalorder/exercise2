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
./run_bank.sh
```

## Start marketplace on default name Blocket
```
./run_marketplace.sh
```
## Start client(s) with chosen username and password
```
./run_client user1 testtest
./run_client user2 Testtest
```