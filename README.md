# My BAnQ
!! This app is not official !!

## Description
If you are subscribed to BAnQ (Bibliothèque et Archives nationales du Québec) and you forget to return your borrowed items in time, this app is for you !
My BAnQ allows you to :
* list your borrowed items
* receive notifications when the return date is approaching
* renew your borrowed items
* add several BAnQ accounts

## How to build / import in IDE
This is a gradle based project; just clone this repository and run 

    ./gradlew build

to build both the java api project, and the Android app
But it may be even more interesting to run

    ./gradlew build installDebug

so that it pushes the app to a connected device (provided APT is started on your machine)
To run all of the tests, you need to specify a valid BAnQ username and a valid BAnQ password

    ./gradlew test -Dusername=00000000 -Dpassword=00000000

To work on the project, use Android Studio and import the root.

## License
This app is open source and under the GPL v3 license; it is developed by Anthony Dahanne and Guilhem de Miollis, hoping that it will be as useful to you as it is for us !
