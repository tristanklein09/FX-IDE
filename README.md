# **FX-IDE**



FX-IDE is a basic IDE created in java using javaFX. It is still a work in progress, so the features may be limited.


## Features:

-Open projects

-Create new projects

-Edit and save files

-Save all edited files

-Create new files

-Browse the folder tree

-Compile and run the project


## Dependencies:

There are some important things to note about the structure of this project in order to get it working. 

FX-IDE relies on the BellSoft Liberica JDK (v25.0.2), as it includes javafx and offers good support for computers that do not necessarily use an x86 CPU architecture. I highly recommend that you use this when running the program, although I have not tested alternate methods of using javafx. 

It also also heavily relies on richtextfx (fat v0.11.7), to display the text. Therefore, to run this project, you should also install that dependency.

FX-IDE also uses jackson3 for .json handling. It is important to use the correct version of jackson: jackson-core v3.0.4, jackson-databind v3.0.4 and jackson-annotations v3.0-rc4. These can be installed from the Maven Repository (tools.jackson)

#####  

<br>

Feel free to suggest improvement that can be made to the codebase.





##### 




---

